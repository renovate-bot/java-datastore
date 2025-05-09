/*
 * Copyright 2016 Google LLC
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

package com.google.cloud.datastore.testing;

import com.google.api.core.InternalApi;
import com.google.api.gax.retrying.RetrySettings;
import com.google.cloud.TransportOptions;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOpenTelemetryOptions;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.Query;
import com.google.cloud.datastore.QueryResults;
import com.google.cloud.datastore.StructuredQuery;
import com.google.cloud.grpc.GrpcTransportOptions;
import com.google.cloud.http.HttpTransportOptions;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import java.time.Duration;
import java.util.UUID;
import javax.annotation.Nullable;

/**
 * Utility to create a remote datastore configuration for testing. Datastore options can be obtained
 * via the {@link #getOptions()} method. Returned options use a randomly generated namespace ({@link
 * DatastoreOptions#getNamespace()}) that can be used to run the tests. Once the tests are run, all
 * entities in the namespace can be deleted using {@link #deleteNamespace()}. Returned options also
 * have custom {@link DatastoreOptions#getRetrySettings()}: {@link RetrySettings#getMaxAttempts()}
 * is {@code 10}, {@link RetrySettings#getMaxRetryDelay()} is {@code 30000}, {@link
 * RetrySettings#getTotalTimeout()} is {@code 120000} and {@link
 * RetrySettings#getInitialRetryDelay()} is {@code 250}. {@link
 * HttpTransportOptions#getConnectTimeout()} and {@link HttpTransportOptions#getReadTimeout()} are
 * both both set to {@code 60000}. If an OpenTelemetrySdk object is passed in, OpenTelemetry Trace
 * collection will be enabled for the Client application.
 *
 * <p>Internal testing use only
 */
@InternalApi
public class RemoteDatastoreHelper {
  private final DatastoreOptions options;
  private final Datastore datastore;
  private final String namespace;

  private RemoteDatastoreHelper(DatastoreOptions options) {
    this.options = options;
    this.datastore = options.getService();
    this.namespace = options.getNamespace();
  }

  /**
   * Returns a {@link DatastoreOptions} object to be used for testing. The options are associated to
   * a randomly generated namespace.
   */
  public DatastoreOptions getOptions() {
    return options;
  }

  /** Deletes all entities in the namespace associated with this {@link RemoteDatastoreHelper}. */
  public void deleteNamespace() {
    StructuredQuery<Key> query = Query.newKeyQueryBuilder().setNamespace(namespace).build();
    QueryResults<Key> keys = datastore.run(query);
    while (keys.hasNext()) {
      datastore.delete(keys.next());
    }
  }

  /** Creates a {@code RemoteStorageHelper} object. */
  public static RemoteDatastoreHelper create() {
    return create(
        "", DatastoreOptions.getDefaultHttpTransportOptions(), /* openTelemetrySdk= */ null);
  }

  public static RemoteDatastoreHelper create(String databaseId) {
    return create(
        databaseId,
        DatastoreOptions.getDefaultHttpTransportOptions(),
        /* openTelemetrySdk= */ null);
  }

  public static RemoteDatastoreHelper create(TransportOptions transportOptions) {
    return create("", transportOptions, /* openTelemetrySdk= */ null);
  }

  public static RemoteDatastoreHelper create(
      String databaseId, @Nullable OpenTelemetrySdk openTelemetrySdk) {
    return create(databaseId, DatastoreOptions.getDefaultHttpTransportOptions(), openTelemetrySdk);
  }

  public static RemoteDatastoreHelper create(String databaseId, TransportOptions transportOptions) {
    return create(databaseId, transportOptions, /* openTelemetrySdk= */ null);
  }

  /** Creates a {@code RemoteStorageHelper} object. */
  public static RemoteDatastoreHelper create(
      String databaseId,
      TransportOptions transportOptions,
      @Nullable OpenTelemetrySdk openTelemetrySdk) {
    DatastoreOptions.Builder datastoreOptionBuilder =
        DatastoreOptions.newBuilder()
            .setDatabaseId(databaseId)
            .setNamespace(UUID.randomUUID().toString())
            .setRetrySettings(retrySettings());
    if (transportOptions instanceof GrpcTransportOptions) {
      datastoreOptionBuilder =
          datastoreOptionBuilder.setTransportOptions((GrpcTransportOptions) transportOptions);
    } else {
      datastoreOptionBuilder = datastoreOptionBuilder.setTransportOptions(transportOptions);
    }

    if (openTelemetrySdk != null) {
      datastoreOptionBuilder.setOpenTelemetryOptions(
          DatastoreOpenTelemetryOptions.newBuilder()
              .setOpenTelemetry(openTelemetrySdk)
              .setTracingEnabled(true)
              .build());
    }
    return new RemoteDatastoreHelper(datastoreOptionBuilder.build());
  }

  private static RetrySettings retrySettings() {
    return RetrySettings.newBuilder()
        .setMaxAttempts(10)
        .setMaxRetryDelayDuration(Duration.ofMillis(30000L))
        .setTotalTimeoutDuration(Duration.ofMillis(120000L))
        .setInitialRetryDelayDuration(Duration.ofMillis(250L))
        .setRetryDelayMultiplier(1.0)
        .setInitialRpcTimeoutDuration(Duration.ofMillis(120000L))
        .setRpcTimeoutMultiplier(1.0)
        .setMaxRpcTimeoutDuration(Duration.ofMillis(120000L))
        .build();
  }
}
