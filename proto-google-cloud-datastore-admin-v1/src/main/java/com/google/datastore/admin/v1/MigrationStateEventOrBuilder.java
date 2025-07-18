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
// source: google/datastore/admin/v1/migration.proto

// Protobuf Java Version: 3.25.8
package com.google.datastore.admin.v1;

public interface MigrationStateEventOrBuilder
    extends
    // @@protoc_insertion_point(interface_extends:google.datastore.admin.v1.MigrationStateEvent)
    com.google.protobuf.MessageOrBuilder {

  /**
   *
   *
   * <pre>
   * The new state of the migration.
   * </pre>
   *
   * <code>.google.datastore.admin.v1.MigrationState state = 1;</code>
   *
   * @return The enum numeric value on the wire for state.
   */
  int getStateValue();

  /**
   *
   *
   * <pre>
   * The new state of the migration.
   * </pre>
   *
   * <code>.google.datastore.admin.v1.MigrationState state = 1;</code>
   *
   * @return The state.
   */
  com.google.datastore.admin.v1.MigrationState getState();
}
