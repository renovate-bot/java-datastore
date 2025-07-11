/*
 * Copyright 2025 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: google/datastore/v1/datastore.proto

// Protobuf Java Version: 3.25.8
package com.google.datastore.v1;

public interface LookupRequestOrBuilder
    extends
    // @@protoc_insertion_point(interface_extends:google.datastore.v1.LookupRequest)
    com.google.protobuf.MessageOrBuilder {

  /**
   *
   *
   * <pre>
   * Required. The ID of the project against which to make the request.
   * </pre>
   *
   * <code>string project_id = 8 [(.google.api.field_behavior) = REQUIRED];</code>
   *
   * @return The projectId.
   */
  java.lang.String getProjectId();

  /**
   *
   *
   * <pre>
   * Required. The ID of the project against which to make the request.
   * </pre>
   *
   * <code>string project_id = 8 [(.google.api.field_behavior) = REQUIRED];</code>
   *
   * @return The bytes for projectId.
   */
  com.google.protobuf.ByteString getProjectIdBytes();

  /**
   *
   *
   * <pre>
   * The ID of the database against which to make the request.
   *
   * '(default)' is not allowed; please use empty string '' to refer the default
   * database.
   * </pre>
   *
   * <code>string database_id = 9;</code>
   *
   * @return The databaseId.
   */
  java.lang.String getDatabaseId();

  /**
   *
   *
   * <pre>
   * The ID of the database against which to make the request.
   *
   * '(default)' is not allowed; please use empty string '' to refer the default
   * database.
   * </pre>
   *
   * <code>string database_id = 9;</code>
   *
   * @return The bytes for databaseId.
   */
  com.google.protobuf.ByteString getDatabaseIdBytes();

  /**
   *
   *
   * <pre>
   * The options for this lookup request.
   * </pre>
   *
   * <code>.google.datastore.v1.ReadOptions read_options = 1;</code>
   *
   * @return Whether the readOptions field is set.
   */
  boolean hasReadOptions();

  /**
   *
   *
   * <pre>
   * The options for this lookup request.
   * </pre>
   *
   * <code>.google.datastore.v1.ReadOptions read_options = 1;</code>
   *
   * @return The readOptions.
   */
  com.google.datastore.v1.ReadOptions getReadOptions();

  /**
   *
   *
   * <pre>
   * The options for this lookup request.
   * </pre>
   *
   * <code>.google.datastore.v1.ReadOptions read_options = 1;</code>
   */
  com.google.datastore.v1.ReadOptionsOrBuilder getReadOptionsOrBuilder();

  /**
   *
   *
   * <pre>
   * Required. Keys of entities to look up.
   * </pre>
   *
   * <code>repeated .google.datastore.v1.Key keys = 3 [(.google.api.field_behavior) = REQUIRED];
   * </code>
   */
  java.util.List<com.google.datastore.v1.Key> getKeysList();

  /**
   *
   *
   * <pre>
   * Required. Keys of entities to look up.
   * </pre>
   *
   * <code>repeated .google.datastore.v1.Key keys = 3 [(.google.api.field_behavior) = REQUIRED];
   * </code>
   */
  com.google.datastore.v1.Key getKeys(int index);

  /**
   *
   *
   * <pre>
   * Required. Keys of entities to look up.
   * </pre>
   *
   * <code>repeated .google.datastore.v1.Key keys = 3 [(.google.api.field_behavior) = REQUIRED];
   * </code>
   */
  int getKeysCount();

  /**
   *
   *
   * <pre>
   * Required. Keys of entities to look up.
   * </pre>
   *
   * <code>repeated .google.datastore.v1.Key keys = 3 [(.google.api.field_behavior) = REQUIRED];
   * </code>
   */
  java.util.List<? extends com.google.datastore.v1.KeyOrBuilder> getKeysOrBuilderList();

  /**
   *
   *
   * <pre>
   * Required. Keys of entities to look up.
   * </pre>
   *
   * <code>repeated .google.datastore.v1.Key keys = 3 [(.google.api.field_behavior) = REQUIRED];
   * </code>
   */
  com.google.datastore.v1.KeyOrBuilder getKeysOrBuilder(int index);

  /**
   *
   *
   * <pre>
   * The properties to return. Defaults to returning all properties.
   *
   * If this field is set and an entity has a property not referenced in the
   * mask, it will be absent from [LookupResponse.found.entity.properties][].
   *
   * The entity's key is always returned.
   * </pre>
   *
   * <code>.google.datastore.v1.PropertyMask property_mask = 5;</code>
   *
   * @return Whether the propertyMask field is set.
   */
  boolean hasPropertyMask();

  /**
   *
   *
   * <pre>
   * The properties to return. Defaults to returning all properties.
   *
   * If this field is set and an entity has a property not referenced in the
   * mask, it will be absent from [LookupResponse.found.entity.properties][].
   *
   * The entity's key is always returned.
   * </pre>
   *
   * <code>.google.datastore.v1.PropertyMask property_mask = 5;</code>
   *
   * @return The propertyMask.
   */
  com.google.datastore.v1.PropertyMask getPropertyMask();

  /**
   *
   *
   * <pre>
   * The properties to return. Defaults to returning all properties.
   *
   * If this field is set and an entity has a property not referenced in the
   * mask, it will be absent from [LookupResponse.found.entity.properties][].
   *
   * The entity's key is always returned.
   * </pre>
   *
   * <code>.google.datastore.v1.PropertyMask property_mask = 5;</code>
   */
  com.google.datastore.v1.PropertyMaskOrBuilder getPropertyMaskOrBuilder();
}
