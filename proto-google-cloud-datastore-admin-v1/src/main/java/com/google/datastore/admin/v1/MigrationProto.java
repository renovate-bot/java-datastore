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

public final class MigrationProto {
  private MigrationProto() {}

  public static void registerAllExtensions(com.google.protobuf.ExtensionRegistryLite registry) {}

  public static void registerAllExtensions(com.google.protobuf.ExtensionRegistry registry) {
    registerAllExtensions((com.google.protobuf.ExtensionRegistryLite) registry);
  }

  static final com.google.protobuf.Descriptors.Descriptor
      internal_static_google_datastore_admin_v1_MigrationStateEvent_descriptor;
  static final com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_google_datastore_admin_v1_MigrationStateEvent_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
      internal_static_google_datastore_admin_v1_MigrationProgressEvent_descriptor;
  static final com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_google_datastore_admin_v1_MigrationProgressEvent_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
      internal_static_google_datastore_admin_v1_MigrationProgressEvent_PrepareStepDetails_descriptor;
  static final com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_google_datastore_admin_v1_MigrationProgressEvent_PrepareStepDetails_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
      internal_static_google_datastore_admin_v1_MigrationProgressEvent_RedirectWritesStepDetails_descriptor;
  static final com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_google_datastore_admin_v1_MigrationProgressEvent_RedirectWritesStepDetails_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor getDescriptor() {
    return descriptor;
  }

  private static com.google.protobuf.Descriptors.FileDescriptor descriptor;

  static {
    java.lang.String[] descriptorData = {
      "\n)google/datastore/admin/v1/migration.pr"
          + "oto\022\031google.datastore.admin.v1\"O\n\023Migrat"
          + "ionStateEvent\0228\n\005state\030\001 \001(\0162).google.da"
          + "tastore.admin.v1.MigrationState\"\241\005\n\026Migr"
          + "ationProgressEvent\0226\n\004step\030\001 \001(\0162(.googl"
          + "e.datastore.admin.v1.MigrationStep\022d\n\024pr"
          + "epare_step_details\030\002 \001(\0132D.google.datast"
          + "ore.admin.v1.MigrationProgressEvent.Prep"
          + "areStepDetailsH\000\022s\n\034redirect_writes_step"
          + "_details\030\003 \001(\0132K.google.datastore.admin."
          + "v1.MigrationProgressEvent.RedirectWrites"
          + "StepDetailsH\000\032q\n\022PrepareStepDetails\022[\n\020c"
          + "oncurrency_mode\030\001 \001(\0162A.google.datastore"
          + ".admin.v1.MigrationProgressEvent.Concurr"
          + "encyMode\032x\n\031RedirectWritesStepDetails\022[\n"
          + "\020concurrency_mode\030\001 \001(\0162A.google.datasto"
          + "re.admin.v1.MigrationProgressEvent.Concu"
          + "rrencyMode\"w\n\017ConcurrencyMode\022 \n\034CONCURR"
          + "ENCY_MODE_UNSPECIFIED\020\000\022\017\n\013PESSIMISTIC\020\001"
          + "\022\016\n\nOPTIMISTIC\020\002\022!\n\035OPTIMISTIC_WITH_ENTI"
          + "TY_GROUPS\020\003B\016\n\014step_details*X\n\016Migration"
          + "State\022\037\n\033MIGRATION_STATE_UNSPECIFIED\020\000\022\013"
          + "\n\007RUNNING\020\001\022\n\n\006PAUSED\020\002\022\014\n\010COMPLETE\020\003*\343\001"
          + "\n\rMigrationStep\022\036\n\032MIGRATION_STEP_UNSPEC"
          + "IFIED\020\000\022\013\n\007PREPARE\020\006\022\t\n\005START\020\001\022\036\n\032APPLY"
          + "_WRITES_SYNCHRONOUSLY\020\007\022\023\n\017COPY_AND_VERI"
          + "FY\020\002\022(\n$REDIRECT_EVENTUALLY_CONSISTENT_R"
          + "EADS\020\003\022&\n\"REDIRECT_STRONGLY_CONSISTENT_R"
          + "EADS\020\004\022\023\n\017REDIRECT_WRITES\020\005B\326\001\n\035com.goog"
          + "le.datastore.admin.v1B\016MigrationProtoP\001Z"
          + "9cloud.google.com/go/datastore/admin/api"
          + "v1/adminpb;adminpb\252\002\037Google.Cloud.Datast"
          + "ore.Admin.V1\312\002\037Google\\Cloud\\Datastore\\Ad"
          + "min\\V1\352\002#Google::Cloud::Datastore::Admin"
          + "::V1b\006proto3"
    };
    descriptor =
        com.google.protobuf.Descriptors.FileDescriptor.internalBuildGeneratedFileFrom(
            descriptorData, new com.google.protobuf.Descriptors.FileDescriptor[] {});
    internal_static_google_datastore_admin_v1_MigrationStateEvent_descriptor =
        getDescriptor().getMessageTypes().get(0);
    internal_static_google_datastore_admin_v1_MigrationStateEvent_fieldAccessorTable =
        new com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
            internal_static_google_datastore_admin_v1_MigrationStateEvent_descriptor,
            new java.lang.String[] {
              "State",
            });
    internal_static_google_datastore_admin_v1_MigrationProgressEvent_descriptor =
        getDescriptor().getMessageTypes().get(1);
    internal_static_google_datastore_admin_v1_MigrationProgressEvent_fieldAccessorTable =
        new com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
            internal_static_google_datastore_admin_v1_MigrationProgressEvent_descriptor,
            new java.lang.String[] {
              "Step", "PrepareStepDetails", "RedirectWritesStepDetails", "StepDetails",
            });
    internal_static_google_datastore_admin_v1_MigrationProgressEvent_PrepareStepDetails_descriptor =
        internal_static_google_datastore_admin_v1_MigrationProgressEvent_descriptor
            .getNestedTypes()
            .get(0);
    internal_static_google_datastore_admin_v1_MigrationProgressEvent_PrepareStepDetails_fieldAccessorTable =
        new com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
            internal_static_google_datastore_admin_v1_MigrationProgressEvent_PrepareStepDetails_descriptor,
            new java.lang.String[] {
              "ConcurrencyMode",
            });
    internal_static_google_datastore_admin_v1_MigrationProgressEvent_RedirectWritesStepDetails_descriptor =
        internal_static_google_datastore_admin_v1_MigrationProgressEvent_descriptor
            .getNestedTypes()
            .get(1);
    internal_static_google_datastore_admin_v1_MigrationProgressEvent_RedirectWritesStepDetails_fieldAccessorTable =
        new com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
            internal_static_google_datastore_admin_v1_MigrationProgressEvent_RedirectWritesStepDetails_descriptor,
            new java.lang.String[] {
              "ConcurrencyMode",
            });
  }

  // @@protoc_insertion_point(outer_class_scope)
}
