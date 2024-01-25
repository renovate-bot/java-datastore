/*
 * Copyright 2023 Google LLC
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
// source: google/datastore/v1/query_profile.proto

package com.google.datastore.v1;

public final class QueryProfileProto {
  private QueryProfileProto() {}

  public static void registerAllExtensions(com.google.protobuf.ExtensionRegistryLite registry) {}

  public static void registerAllExtensions(com.google.protobuf.ExtensionRegistry registry) {
    registerAllExtensions((com.google.protobuf.ExtensionRegistryLite) registry);
  }

  static final com.google.protobuf.Descriptors.Descriptor
      internal_static_google_datastore_v1_QueryPlan_descriptor;
  static final com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_google_datastore_v1_QueryPlan_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
      internal_static_google_datastore_v1_ResultSetStats_descriptor;
  static final com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_google_datastore_v1_ResultSetStats_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor getDescriptor() {
    return descriptor;
  }

  private static com.google.protobuf.Descriptors.FileDescriptor descriptor;

  static {
    java.lang.String[] descriptorData = {
      "\n\'google/datastore/v1/query_profile.prot"
          + "o\022\023google.datastore.v1\032\034google/protobuf/"
          + "struct.proto\"7\n\tQueryPlan\022*\n\tplan_info\030\001"
          + " \001(\0132\027.google.protobuf.Struct\"r\n\016ResultS"
          + "etStats\0222\n\nquery_plan\030\001 \001(\0132\036.google.dat"
          + "astore.v1.QueryPlan\022,\n\013query_stats\030\002 \001(\013"
          + "2\027.google.protobuf.Struct*.\n\tQueryMode\022\n"
          + "\n\006NORMAL\020\000\022\010\n\004PLAN\020\001\022\013\n\007PROFILE\020\002B\303\001\n\027co"
          + "m.google.datastore.v1B\021QueryProfileProto"
          + "P\001Z<google.golang.org/genproto/googleapi"
          + "s/datastore/v1;datastore\252\002\031Google.Cloud."
          + "Datastore.V1\312\002\031Google\\Cloud\\Datastore\\V1"
          + "\352\002\034Google::Cloud::Datastore::V1b\006proto3"
    };
    descriptor =
        com.google.protobuf.Descriptors.FileDescriptor.internalBuildGeneratedFileFrom(
            descriptorData,
            new com.google.protobuf.Descriptors.FileDescriptor[] {
              com.google.protobuf.StructProto.getDescriptor(),
            });
    internal_static_google_datastore_v1_QueryPlan_descriptor =
        getDescriptor().getMessageTypes().get(0);
    internal_static_google_datastore_v1_QueryPlan_fieldAccessorTable =
        new com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
            internal_static_google_datastore_v1_QueryPlan_descriptor,
            new java.lang.String[] {
              "PlanInfo",
            });
    internal_static_google_datastore_v1_ResultSetStats_descriptor =
        getDescriptor().getMessageTypes().get(1);
    internal_static_google_datastore_v1_ResultSetStats_fieldAccessorTable =
        new com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
            internal_static_google_datastore_v1_ResultSetStats_descriptor,
            new java.lang.String[] {
              "QueryPlan", "QueryStats",
            });
    com.google.protobuf.StructProto.getDescriptor();
  }

  // @@protoc_insertion_point(outer_class_scope)
}