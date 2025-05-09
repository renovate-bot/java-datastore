/*
 * Copyright 2015 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.cloud.datastore;

import static com.google.cloud.datastore.telemetry.TraceUtil.ATTRIBUTES_KEY_DEFERRED;
import static com.google.cloud.datastore.telemetry.TraceUtil.ATTRIBUTES_KEY_DOCUMENT_COUNT;
import static com.google.cloud.datastore.telemetry.TraceUtil.ATTRIBUTES_KEY_MISSING;
import static com.google.cloud.datastore.telemetry.TraceUtil.ATTRIBUTES_KEY_MORE_RESULTS;
import static com.google.cloud.datastore.telemetry.TraceUtil.ATTRIBUTES_KEY_READ_CONSISTENCY;
import static com.google.cloud.datastore.telemetry.TraceUtil.ATTRIBUTES_KEY_RECEIVED;
import static com.google.cloud.datastore.telemetry.TraceUtil.ATTRIBUTES_KEY_TRANSACTIONAL;
import static com.google.cloud.datastore.telemetry.TraceUtil.ATTRIBUTES_KEY_TRANSACTION_ID;
import static com.google.cloud.datastore.telemetry.TraceUtil.SPAN_NAME_ALLOCATE_IDS;
import static com.google.cloud.datastore.telemetry.TraceUtil.SPAN_NAME_BEGIN_TRANSACTION;
import static com.google.cloud.datastore.telemetry.TraceUtil.SPAN_NAME_COMMIT;
import static com.google.cloud.datastore.telemetry.TraceUtil.SPAN_NAME_LOOKUP;
import static com.google.cloud.datastore.telemetry.TraceUtil.SPAN_NAME_RESERVE_IDS;
import static com.google.cloud.datastore.telemetry.TraceUtil.SPAN_NAME_ROLLBACK;
import static com.google.cloud.datastore.telemetry.TraceUtil.SPAN_NAME_RUN_QUERY;
import static com.google.cloud.datastore.telemetry.TraceUtil.SPAN_NAME_TRANSACTION_COMMIT;
import static com.google.cloud.datastore.telemetry.TraceUtil.SPAN_NAME_TRANSACTION_LOOKUP;
import static com.google.cloud.datastore.telemetry.TraceUtil.SPAN_NAME_TRANSACTION_RUN;
import static com.google.cloud.datastore.telemetry.TraceUtil.SPAN_NAME_TRANSACTION_RUN_QUERY;

import com.google.api.core.BetaApi;
import com.google.api.gax.retrying.RetrySettings;
import com.google.cloud.BaseService;
import com.google.cloud.ExceptionHandler;
import com.google.cloud.RetryHelper;
import com.google.cloud.RetryHelper.RetryHelperException;
import com.google.cloud.ServiceOptions;
import com.google.cloud.datastore.execution.AggregationQueryExecutor;
import com.google.cloud.datastore.spi.v1.DatastoreRpc;
import com.google.cloud.datastore.telemetry.TraceUtil;
import com.google.cloud.datastore.telemetry.TraceUtil.Scope;
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.google.datastore.v1.CommitResponse;
import com.google.datastore.v1.ExplainOptions;
import com.google.datastore.v1.ReadOptions;
import com.google.datastore.v1.ReserveIdsRequest;
import com.google.datastore.v1.RunQueryResponse;
import com.google.datastore.v1.TransactionOptions;
import com.google.protobuf.ByteString;
import io.opentelemetry.context.Context;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

final class DatastoreImpl extends BaseService<DatastoreOptions> implements Datastore {

  Logger logger = Logger.getLogger(Datastore.class.getName());
  private final DatastoreRpc datastoreRpc;
  private final RetrySettings retrySettings;
  private static final ExceptionHandler TRANSACTION_EXCEPTION_HANDLER =
      TransactionExceptionHandler.build();
  private static final ExceptionHandler TRANSACTION_OPERATION_EXCEPTION_HANDLER =
      TransactionOperationExceptionHandler.build();

  private final com.google.cloud.datastore.telemetry.TraceUtil otelTraceUtil =
      getOptions().getTraceUtil();

  private final ReadOptionProtoPreparer readOptionProtoPreparer;
  private final AggregationQueryExecutor aggregationQueryExecutor;

  DatastoreImpl(DatastoreOptions options) {
    super(options);
    this.datastoreRpc = options.getDatastoreRpcV1();
    retrySettings =
        MoreObjects.firstNonNull(options.getRetrySettings(), ServiceOptions.getNoRetrySettings());

    readOptionProtoPreparer = new ReadOptionProtoPreparer();
    aggregationQueryExecutor =
        new AggregationQueryExecutor(
            new RetryAndTraceDatastoreRpcDecorator(
                datastoreRpc, otelTraceUtil, retrySettings, options),
            options);
  }

  @Override
  public Batch newBatch() {
    return new BatchImpl(this);
  }

  @Override
  public Transaction newTransaction(TransactionOptions transactionOptions) {
    return new TransactionImpl(this, transactionOptions);
  }

  @Override
  public Transaction newTransaction() {
    return new TransactionImpl(this);
  }

  static class TracedReadWriteTransactionCallable<T> implements Callable<T> {
    private final Datastore datastore;
    private final TransactionCallable<T> callable;
    private volatile TransactionOptions options;
    private volatile Transaction transaction;

    private final TraceUtil.Span parentSpan;

    TracedReadWriteTransactionCallable(
        Datastore datastore,
        TransactionCallable<T> callable,
        TransactionOptions options,
        @Nullable com.google.cloud.datastore.telemetry.TraceUtil.Span parentSpan) {
      this.datastore = datastore;
      this.callable = callable;
      this.options = options;
      this.transaction = null;
      this.parentSpan = parentSpan;
    }

    Datastore getDatastore() {
      return datastore;
    }

    TransactionOptions getOptions() {
      return options;
    }

    Transaction getTransaction() {
      return transaction;
    }

    void setPrevTransactionId(ByteString transactionId) {
      TransactionOptions.ReadWrite readWrite =
          TransactionOptions.ReadWrite.newBuilder().setPreviousTransaction(transactionId).build();
      options = options.toBuilder().setReadWrite(readWrite).build();
    }

    @Override
    public T call() throws DatastoreException {
      try (io.opentelemetry.context.Scope ignored =
          Context.current().with(parentSpan.getSpan()).makeCurrent()) {
        transaction = datastore.newTransaction(options);
        T value = callable.run(transaction);
        transaction.commit();
        return value;
      } catch (Exception ex) {
        transaction.rollback();
        throw DatastoreException.propagateUserException(ex);
      } finally {
        if (transaction.isActive()) {
          transaction.rollback();
        }
        if (options != null
            && options.getModeCase().equals(TransactionOptions.ModeCase.READ_WRITE)) {
          setPrevTransactionId(transaction.getTransactionId());
        }
      }
    }
  }

  @Override
  public void close() throws Exception {
    try {
      datastoreRpc.close();
    } catch (Exception e) {
      logger.log(Level.WARNING, "Failed to close channels", e);
    }
  }

  @Override
  public boolean isClosed() {
    return datastoreRpc.isClosed();
  }

  static class ReadWriteTransactionCallable<T> implements Callable<T> {
    private final Datastore datastore;
    private final TransactionCallable<T> callable;
    private volatile TransactionOptions options;
    private volatile Transaction transaction;

    ReadWriteTransactionCallable(
        Datastore datastore, TransactionCallable<T> callable, TransactionOptions options) {
      this.datastore = datastore;
      this.callable = callable;
      this.options = options;
      this.transaction = null;
    }

    Datastore getDatastore() {
      return datastore;
    }

    TransactionOptions getOptions() {
      return options;
    }

    Transaction getTransaction() {
      return transaction;
    }

    void setPrevTransactionId(ByteString transactionId) {
      TransactionOptions.ReadWrite readWrite =
          TransactionOptions.ReadWrite.newBuilder().setPreviousTransaction(transactionId).build();
      options = options.toBuilder().setReadWrite(readWrite).build();
    }

    @Override
    public T call() throws DatastoreException {
      try {
        transaction = datastore.newTransaction(options);
        T value = callable.run(transaction);
        transaction.commit();
        return value;
      } catch (Exception ex) {
        transaction.rollback();
        throw DatastoreException.propagateUserException(ex);
      } finally {
        if (transaction.isActive()) {
          transaction.rollback();
        }
        if (options != null
            && options.getModeCase().equals(TransactionOptions.ModeCase.READ_WRITE)) {
          setPrevTransactionId(transaction.getTransactionId());
        }
      }
    }
  }

  @Override
  public <T> T runInTransaction(final TransactionCallable<T> callable) {
    TraceUtil.Span span = otelTraceUtil.startSpan(SPAN_NAME_TRANSACTION_RUN);
    Callable<T> transactionCallable =
        (getOptions().getOpenTelemetryOptions().isEnabled()
            ? new TracedReadWriteTransactionCallable<T>(
                this, callable, /* transactionOptions= */ null, span)
            : new ReadWriteTransactionCallable<T>(this, callable, /* transactionOptions= */ null));
    try (Scope ignored = span.makeCurrent()) {
      return RetryHelper.runWithRetries(
          transactionCallable,
          retrySettings,
          TRANSACTION_EXCEPTION_HANDLER,
          getOptions().getClock());
    } catch (RetryHelperException e) {
      span.end(e);
      throw DatastoreException.translateAndThrow(e);
    } finally {
      span.end();
    }
  }

  @Override
  public <T> T runInTransaction(
      final TransactionCallable<T> callable, TransactionOptions transactionOptions) {
    TraceUtil.Span span = otelTraceUtil.startSpan(SPAN_NAME_TRANSACTION_RUN);

    Callable<T> transactionCallable =
        (getOptions().getOpenTelemetryOptions().isEnabled()
            ? new TracedReadWriteTransactionCallable<T>(this, callable, transactionOptions, span)
            : new ReadWriteTransactionCallable<T>(this, callable, transactionOptions));

    try (Scope ignored = span.makeCurrent()) {
      return RetryHelper.runWithRetries(
          transactionCallable,
          retrySettings,
          TRANSACTION_EXCEPTION_HANDLER,
          getOptions().getClock());
    } catch (RetryHelperException e) {
      span.end(e);
      throw DatastoreException.translateAndThrow(e);
    } finally {
      span.end();
    }
  }

  @Override
  public <T> QueryResults<T> run(Query<T> query) {
    return run(Optional.empty(), query, null);
  }

  @Override
  public <T> QueryResults<T> run(Query<T> query, ReadOption... options) {
    return run(toReadOptionsPb(options), query, null);
  }

  @Override
  @BetaApi
  public <T> QueryResults<T> run(
      Query<T> query,
      com.google.cloud.datastore.models.ExplainOptions explainOptions,
      ReadOption... options) {
    return run(toReadOptionsPb(options), query, explainOptions.toPb());
  }

  @SuppressWarnings("unchecked")
  <T> QueryResults<T> run(
      Optional<ReadOptions> readOptionsPb, Query<T> query, ExplainOptions explainOptions) {
    return new QueryResultsImpl<T>(
        this, readOptionsPb, (RecordQuery<T>) query, query.getNamespace(), explainOptions);
  }

  @Override
  public AggregationResults runAggregation(AggregationQuery query) {
    return aggregationQueryExecutor.execute(query, null);
  }

  @Override
  public AggregationResults runAggregation(AggregationQuery query, ReadOption... options) {
    return aggregationQueryExecutor.execute(query, null, options);
  }

  @Override
  @BetaApi
  public AggregationResults runAggregation(
      AggregationQuery query, com.google.cloud.datastore.models.ExplainOptions explainOptions) {
    return aggregationQueryExecutor.execute(query, explainOptions);
  }

  @Override
  @BetaApi
  public AggregationResults runAggregation(
      AggregationQuery query,
      com.google.cloud.datastore.models.ExplainOptions explainOptions,
      ReadOption... options) {
    return aggregationQueryExecutor.execute(query, explainOptions, options);
  }

  com.google.datastore.v1.RunQueryResponse runQuery(
      final com.google.datastore.v1.RunQueryRequest requestPb) {
    ReadOptions readOptions = requestPb.getReadOptions();
    boolean isTransactional = readOptions.hasTransaction() || readOptions.hasNewTransaction();
    String spanName = (isTransactional ? SPAN_NAME_TRANSACTION_RUN_QUERY : SPAN_NAME_RUN_QUERY);
    com.google.cloud.datastore.telemetry.TraceUtil.Span span = otelTraceUtil.startSpan(spanName);

    try (com.google.cloud.datastore.telemetry.TraceUtil.Scope ignored = span.makeCurrent()) {
      RunQueryResponse response =
          RetryHelper.runWithRetries(
              () -> datastoreRpc.runQuery(requestPb),
              retrySettings,
              requestPb.getReadOptions().getTransaction().isEmpty()
                  ? EXCEPTION_HANDLER
                  : TRANSACTION_OPERATION_EXCEPTION_HANDLER,
              getOptions().getClock());
      span.addEvent(
          spanName + " complete.",
          new ImmutableMap.Builder<String, Object>()
              .put(ATTRIBUTES_KEY_DOCUMENT_COUNT, response.getBatch().getEntityResultsCount())
              .put(ATTRIBUTES_KEY_TRANSACTIONAL, isTransactional)
              .put(ATTRIBUTES_KEY_READ_CONSISTENCY, readOptions.getReadConsistency().toString())
              .put(
                  ATTRIBUTES_KEY_TRANSACTION_ID,
                  (isTransactional
                      ? requestPb.getReadOptions().getTransaction().toStringUtf8()
                      : ""))
              .put(ATTRIBUTES_KEY_MORE_RESULTS, response.getBatch().getMoreResults().toString())
              .build());
      return response;
    } catch (RetryHelperException e) {
      span.end(e);
      throw DatastoreException.translateAndThrow(e);
    } finally {
      span.end();
    }
  }

  @Override
  public Key allocateId(IncompleteKey key) {
    return DatastoreHelper.allocateId(this, key);
  }

  private boolean verifyIncompleteKeyType(IncompleteKey... keys) {
    for (IncompleteKey key : keys) {
      if (key instanceof Key) {
        return false;
      }
    }
    return true;
  }

  @Override
  public List<Key> allocateId(IncompleteKey... keys) {
    Preconditions.checkArgument(
        verifyIncompleteKeyType(keys), "keys must be IncompleteKey instances");
    if (keys.length == 0) {
      return Collections.emptyList();
    }
    com.google.datastore.v1.AllocateIdsRequest.Builder requestPb =
        com.google.datastore.v1.AllocateIdsRequest.newBuilder();
    for (IncompleteKey key : keys) {
      requestPb.addKeys(trimNameOrId(key).toPb());
    }
    requestPb.setProjectId(getOptions().getProjectId());
    requestPb.setDatabaseId(getOptions().getDatabaseId());
    com.google.datastore.v1.AllocateIdsResponse responsePb = allocateIds(requestPb.build());
    ImmutableList.Builder<Key> keyList = ImmutableList.builder();
    for (com.google.datastore.v1.Key keyPb : responsePb.getKeysList()) {
      keyList.add(Key.fromPb(keyPb));
    }
    return keyList.build();
  }

  private com.google.datastore.v1.AllocateIdsResponse allocateIds(
      final com.google.datastore.v1.AllocateIdsRequest requestPb) {
    com.google.cloud.datastore.telemetry.TraceUtil.Span span =
        otelTraceUtil.startSpan(SPAN_NAME_ALLOCATE_IDS);
    try (com.google.cloud.datastore.telemetry.TraceUtil.Scope ignored = span.makeCurrent()) {
      return RetryHelper.runWithRetries(
          new Callable<com.google.datastore.v1.AllocateIdsResponse>() {
            @Override
            public com.google.datastore.v1.AllocateIdsResponse call() throws DatastoreException {
              return datastoreRpc.allocateIds(requestPb);
            }
          },
          retrySettings,
          EXCEPTION_HANDLER,
          getOptions().getClock());
    } catch (RetryHelperException e) {
      span.end(e);
      throw DatastoreException.translateAndThrow(e);
    } finally {
      span.end();
    }
  }

  private IncompleteKey trimNameOrId(IncompleteKey key) {
    if (key instanceof Key) {
      return IncompleteKey.newBuilder(key).build();
    }
    return key;
  }

  @Override
  public Entity add(FullEntity<?> entity) {
    return DatastoreHelper.add(this, entity);
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<Entity> add(FullEntity<?>... entities) {
    if (entities.length == 0) {
      return Collections.emptyList();
    }
    List<com.google.datastore.v1.Mutation> mutationsPb = new ArrayList<>();
    Map<Key, Entity> completeEntities = new LinkedHashMap<>();
    for (FullEntity<?> entity : entities) {
      Entity completeEntity = null;
      if (entity.getKey() instanceof Key) {
        completeEntity = Entity.convert((FullEntity<Key>) entity);
      }
      if (completeEntity != null) {
        if (completeEntities.put(completeEntity.getKey(), completeEntity) != null) {
          throw DatastoreException.throwInvalidRequest(
              "Duplicate entity with the key %s", entity.getKey());
        }
      } else {
        Preconditions.checkArgument(entity.hasKey(), "Entity %s is missing a key", entity);
      }
      mutationsPb.add(
          com.google.datastore.v1.Mutation.newBuilder().setInsert(entity.toPb()).build());
    }
    com.google.datastore.v1.CommitResponse commitResponse = commitMutation(mutationsPb);
    Iterator<com.google.datastore.v1.MutationResult> mutationResults =
        commitResponse.getMutationResultsList().iterator();
    ImmutableList.Builder<Entity> responseBuilder = ImmutableList.builder();
    for (FullEntity<?> entity : entities) {
      Entity completeEntity = completeEntities.get(entity.getKey());
      if (completeEntity != null) {
        responseBuilder.add(completeEntity);
        mutationResults.next();
      } else {
        responseBuilder.add(
            Entity.newBuilder(Key.fromPb(mutationResults.next().getKey()), entity).build());
      }
    }
    return responseBuilder.build();
  }

  @Override
  public Entity get(Key key) {
    return DatastoreHelper.get(this, key);
  }

  @Override
  public Entity get(Key key, ReadOption... options) {
    return DatastoreHelper.get(this, key, options);
  }

  @Override
  public Iterator<Entity> get(Key... keys) {
    return get(Optional.empty(), keys);
  }

  @Override
  public Iterator<Entity> get(Iterable<Key> keys, ReadOption... options) {
    return get(toReadOptionsPb(options), Iterables.toArray(keys, Key.class));
  }

  private Optional<ReadOptions> toReadOptionsPb(ReadOption... options) {
    if (options == null) {
      return Optional.empty();
    }
    return this.readOptionProtoPreparer.prepare(Arrays.asList(options));
  }

  @Override
  public List<Entity> fetch(Key... keys) {
    return DatastoreHelper.fetch(this, keys);
  }

  @Override
  public List<Entity> fetch(Iterable<Key> keys, ReadOption... options) {
    return DatastoreHelper.fetch(this, Iterables.toArray(keys, Key.class), options);
  }

  Iterator<Entity> get(Optional<ReadOptions> readOptionsPb, final Key... keys) {
    if (keys.length == 0) {
      return Collections.emptyIterator();
    }
    com.google.datastore.v1.LookupRequest.Builder requestPb =
        com.google.datastore.v1.LookupRequest.newBuilder();
    readOptionsPb.ifPresent(requestPb::setReadOptions);
    for (Key k : Sets.newLinkedHashSet(Arrays.asList(keys))) {
      requestPb.addKeys(k.toPb());
    }
    requestPb.setProjectId(getOptions().getProjectId());
    requestPb.setDatabaseId(getOptions().getDatabaseId());
    return new ResultsIterator(requestPb);
  }

  final class ResultsIterator extends AbstractIterator<Entity> {

    private final com.google.datastore.v1.LookupRequest.Builder requestPb;
    Iterator<com.google.datastore.v1.EntityResult> iter;

    ResultsIterator(com.google.datastore.v1.LookupRequest.Builder requestPb) {
      this.requestPb = requestPb;
      loadResults();
    }

    private void loadResults() {
      com.google.datastore.v1.LookupResponse responsePb = lookup(requestPb.build());
      iter = responsePb.getFoundList().iterator();
      requestPb.clearKeys();
      if (responsePb.getDeferredCount() > 0) {
        requestPb.addAllKeys(responsePb.getDeferredList());
      }
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Entity computeNext() {
      while (!iter.hasNext()) {
        if (requestPb.getKeysCount() == 0) {
          return endOfData();
        }
        loadResults();
      }
      return Entity.fromPb(iter.next().getEntity());
    }
  }

  com.google.datastore.v1.LookupResponse lookup(
      final com.google.datastore.v1.LookupRequest requestPb) {
    ReadOptions readOptions = requestPb.getReadOptions();
    boolean isTransactional = readOptions.hasTransaction() || readOptions.hasNewTransaction();
    String spanName = (isTransactional ? SPAN_NAME_TRANSACTION_LOOKUP : SPAN_NAME_LOOKUP);
    com.google.cloud.datastore.telemetry.TraceUtil.Span span = otelTraceUtil.startSpan(spanName);

    try (com.google.cloud.datastore.telemetry.TraceUtil.Scope ignored = span.makeCurrent()) {
      return RetryHelper.runWithRetries(
          () -> {
            com.google.datastore.v1.LookupResponse response = datastoreRpc.lookup(requestPb);
            span.addEvent(
                spanName + " complete.",
                new ImmutableMap.Builder<String, Object>()
                    .put(ATTRIBUTES_KEY_RECEIVED, response.getFoundCount())
                    .put(ATTRIBUTES_KEY_MISSING, response.getMissingCount())
                    .put(ATTRIBUTES_KEY_DEFERRED, response.getDeferredCount())
                    .put(ATTRIBUTES_KEY_TRANSACTIONAL, isTransactional)
                    .put(
                        ATTRIBUTES_KEY_TRANSACTION_ID,
                        isTransactional ? readOptions.getTransaction().toStringUtf8() : "")
                    .build());
            return response;
          },
          retrySettings,
          requestPb.getReadOptions().getTransaction().isEmpty()
              ? EXCEPTION_HANDLER
              : TRANSACTION_OPERATION_EXCEPTION_HANDLER,
          getOptions().getClock());
    } catch (RetryHelperException e) {
      span.end(e);
      throw DatastoreException.translateAndThrow(e);
    } finally {
      span.end();
    }
  }

  @Override
  public List<Key> reserveIds(Key... keys) {
    ReserveIdsRequest.Builder requestPb = ReserveIdsRequest.newBuilder();
    for (Key key : keys) {
      requestPb.addKeys(key.toPb());
    }
    requestPb.setProjectId(getOptions().getProjectId());
    requestPb.setDatabaseId(getOptions().getDatabaseId());
    com.google.datastore.v1.ReserveIdsResponse responsePb = reserveIds(requestPb.build());
    ImmutableList.Builder<Key> keyList = ImmutableList.builder();
    if (responsePb.isInitialized()) {
      for (Key key : keys) {
        keyList.add(key);
      }
    }
    return keyList.build();
  }

  com.google.datastore.v1.ReserveIdsResponse reserveIds(
      final com.google.datastore.v1.ReserveIdsRequest requestPb) {
    com.google.cloud.datastore.telemetry.TraceUtil.Span span =
        otelTraceUtil.startSpan(SPAN_NAME_RESERVE_IDS);
    try (com.google.cloud.datastore.telemetry.TraceUtil.Scope ignored = span.makeCurrent()) {
      return RetryHelper.runWithRetries(
          new Callable<com.google.datastore.v1.ReserveIdsResponse>() {
            @Override
            public com.google.datastore.v1.ReserveIdsResponse call() throws DatastoreException {
              return datastoreRpc.reserveIds(requestPb);
            }
          },
          retrySettings,
          EXCEPTION_HANDLER,
          getOptions().getClock());
    } catch (RetryHelperException e) {
      span.end(e);
      throw DatastoreException.translateAndThrow(e);
    } finally {
      span.end();
    }
  }

  @Override
  public void update(Entity... entities) {
    if (entities.length > 0) {
      List<com.google.datastore.v1.Mutation> mutationsPb = new ArrayList<>();
      Map<Key, Entity> dedupEntities = new LinkedHashMap<>();
      for (Entity entity : entities) {
        dedupEntities.put(entity.getKey(), entity);
      }
      for (Entity entity : dedupEntities.values()) {
        mutationsPb.add(
            com.google.datastore.v1.Mutation.newBuilder().setUpdate(entity.toPb()).build());
      }
      commitMutation(mutationsPb);
    }
  }

  @Override
  public Entity put(FullEntity<?> entity) {
    return DatastoreHelper.put(this, entity);
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<Entity> put(FullEntity<?>... entities) {
    if (entities.length == 0) {
      return Collections.emptyList();
    }
    List<com.google.datastore.v1.Mutation> mutationsPb = new ArrayList<>();
    Map<Key, Entity> dedupEntities = new LinkedHashMap<>();
    for (FullEntity<?> entity : entities) {
      Preconditions.checkArgument(entity.hasKey(), "Entity %s is missing a key", entity);
      if (entity.getKey() instanceof Key) {
        Entity completeEntity = Entity.convert((FullEntity<Key>) entity);
        dedupEntities.put(completeEntity.getKey(), completeEntity);
      } else {
        mutationsPb.add(
            com.google.datastore.v1.Mutation.newBuilder().setUpsert(entity.toPb()).build());
      }
    }
    for (Entity entity : dedupEntities.values()) {
      mutationsPb.add(
          com.google.datastore.v1.Mutation.newBuilder().setUpsert(entity.toPb()).build());
    }
    com.google.datastore.v1.CommitResponse commitResponse = commitMutation(mutationsPb);
    Iterator<com.google.datastore.v1.MutationResult> mutationResults =
        commitResponse.getMutationResultsList().iterator();
    ImmutableList.Builder<Entity> responseBuilder = ImmutableList.builder();
    for (FullEntity<?> entity : entities) {
      Entity completeEntity = dedupEntities.get(entity.getKey());
      if (completeEntity != null) {
        responseBuilder.add(completeEntity);
      } else {
        responseBuilder.add(
            Entity.newBuilder(Key.fromPb(mutationResults.next().getKey()), entity).build());
      }
    }
    return responseBuilder.build();
  }

  @Override
  public void delete(Key... keys) {
    if (keys.length > 0) {
      List<com.google.datastore.v1.Mutation> mutationsPb = new ArrayList<>();
      Set<Key> dedupKeys = new LinkedHashSet<>(Arrays.asList(keys));
      for (Key key : dedupKeys) {
        mutationsPb.add(
            com.google.datastore.v1.Mutation.newBuilder().setDelete(key.toPb()).build());
      }
      commitMutation(mutationsPb);
    }
  }

  @Override
  public KeyFactory newKeyFactory() {
    return DatastoreHelper.newKeyFactory(getOptions());
  }

  private com.google.datastore.v1.CommitResponse commitMutation(
      List<com.google.datastore.v1.Mutation> mutationsPb) {
    com.google.datastore.v1.CommitRequest.Builder requestPb =
        com.google.datastore.v1.CommitRequest.newBuilder();
    requestPb.setMode(com.google.datastore.v1.CommitRequest.Mode.NON_TRANSACTIONAL);
    requestPb.setProjectId(getOptions().getProjectId());
    requestPb.setDatabaseId(getOptions().getDatabaseId());
    requestPb.addAllMutations(mutationsPb);
    return commit(requestPb.build());
  }

  com.google.datastore.v1.CommitResponse commit(
      final com.google.datastore.v1.CommitRequest requestPb) {
    final boolean isTransactional =
        requestPb.hasTransaction() || requestPb.hasSingleUseTransaction();
    final String spanName = isTransactional ? SPAN_NAME_TRANSACTION_COMMIT : SPAN_NAME_COMMIT;
    com.google.cloud.datastore.telemetry.TraceUtil.Span span = otelTraceUtil.startSpan(spanName);
    try (com.google.cloud.datastore.telemetry.TraceUtil.Scope ignored = span.makeCurrent()) {
      CommitResponse response =
          RetryHelper.runWithRetries(
              () -> datastoreRpc.commit(requestPb),
              retrySettings,
              requestPb.getTransaction().isEmpty()
                  ? EXCEPTION_HANDLER
                  : TRANSACTION_OPERATION_EXCEPTION_HANDLER,
              getOptions().getClock());
      span.addEvent(
          spanName + " complete.",
          new ImmutableMap.Builder<String, Object>()
              .put(ATTRIBUTES_KEY_DOCUMENT_COUNT, response.getMutationResultsCount())
              .put(ATTRIBUTES_KEY_TRANSACTIONAL, isTransactional)
              .put(
                  ATTRIBUTES_KEY_TRANSACTION_ID,
                  isTransactional ? requestPb.getTransaction().toStringUtf8() : "")
              .build());
      return response;
    } catch (RetryHelperException e) {
      span.end(e);
      throw DatastoreException.translateAndThrow(e);
    } finally {
      span.end();
    }
  }

  ByteString requestTransactionId(
      com.google.datastore.v1.BeginTransactionRequest.Builder requestPb) {
    return beginTransaction(requestPb.build()).getTransaction();
  }

  com.google.datastore.v1.BeginTransactionResponse beginTransaction(
      final com.google.datastore.v1.BeginTransactionRequest requestPb) {
    com.google.cloud.datastore.telemetry.TraceUtil.Span span =
        otelTraceUtil.startSpan(SPAN_NAME_BEGIN_TRANSACTION);
    try (com.google.cloud.datastore.telemetry.TraceUtil.Scope scope = span.makeCurrent()) {
      return RetryHelper.runWithRetries(
          () -> datastoreRpc.beginTransaction(requestPb),
          retrySettings,
          EXCEPTION_HANDLER,
          getOptions().getClock());
    } catch (RetryHelperException e) {
      span.end(e);
      throw DatastoreException.translateAndThrow(e);
    } finally {
      span.end();
    }
  }

  void rollbackTransaction(ByteString transaction) {
    com.google.datastore.v1.RollbackRequest.Builder requestPb =
        com.google.datastore.v1.RollbackRequest.newBuilder();
    requestPb.setTransaction(transaction);
    requestPb.setProjectId(getOptions().getProjectId());
    requestPb.setDatabaseId(getOptions().getDatabaseId());
    rollback(requestPb.build());
  }

  void rollback(final com.google.datastore.v1.RollbackRequest requestPb) {
    com.google.cloud.datastore.telemetry.TraceUtil.Span span =
        otelTraceUtil.startSpan(SPAN_NAME_ROLLBACK);
    try (Scope scope = span.makeCurrent()) {
      RetryHelper.runWithRetries(
          new Callable<Void>() {
            @Override
            public Void call() throws DatastoreException {
              datastoreRpc.rollback(requestPb);
              return null;
            }
          },
          retrySettings,
          EXCEPTION_HANDLER,
          getOptions().getClock());
      span.addEvent(
          SPAN_NAME_ROLLBACK,
          new ImmutableMap.Builder<String, Object>()
              .put(ATTRIBUTES_KEY_TRANSACTION_ID, requestPb.getTransaction().toStringUtf8())
              .build());
    } catch (RetryHelperException e) {
      span.end(e);
      throw DatastoreException.translateAndThrow(e);
    } finally {
      span.end();
    }
  }
}
