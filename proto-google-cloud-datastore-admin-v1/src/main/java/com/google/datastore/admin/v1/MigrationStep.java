/*
 * Copyright 2024 Google LLC
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

// Protobuf Java Version: 3.25.4
package com.google.datastore.admin.v1;

/**
 *
 *
 * <pre>
 * Steps in a migration.
 * </pre>
 *
 * Protobuf enum {@code google.datastore.admin.v1.MigrationStep}
 */
public enum MigrationStep implements com.google.protobuf.ProtocolMessageEnum {
  /**
   *
   *
   * <pre>
   * Unspecified.
   * </pre>
   *
   * <code>MIGRATION_STEP_UNSPECIFIED = 0;</code>
   */
  MIGRATION_STEP_UNSPECIFIED(0),
  /**
   *
   *
   * <pre>
   * Pre-migration: the database is prepared for migration.
   * </pre>
   *
   * <code>PREPARE = 6;</code>
   */
  PREPARE(6),
  /**
   *
   *
   * <pre>
   * Start of migration.
   * </pre>
   *
   * <code>START = 1;</code>
   */
  START(1),
  /**
   *
   *
   * <pre>
   * Writes are applied synchronously to at least one replica.
   * </pre>
   *
   * <code>APPLY_WRITES_SYNCHRONOUSLY = 7;</code>
   */
  APPLY_WRITES_SYNCHRONOUSLY(7),
  /**
   *
   *
   * <pre>
   * Data is copied to Cloud Firestore and then verified to match the data in
   * Cloud Datastore.
   * </pre>
   *
   * <code>COPY_AND_VERIFY = 2;</code>
   */
  COPY_AND_VERIFY(2),
  /**
   *
   *
   * <pre>
   * Eventually-consistent reads are redirected to Cloud Firestore.
   * </pre>
   *
   * <code>REDIRECT_EVENTUALLY_CONSISTENT_READS = 3;</code>
   */
  REDIRECT_EVENTUALLY_CONSISTENT_READS(3),
  /**
   *
   *
   * <pre>
   * Strongly-consistent reads are redirected to Cloud Firestore.
   * </pre>
   *
   * <code>REDIRECT_STRONGLY_CONSISTENT_READS = 4;</code>
   */
  REDIRECT_STRONGLY_CONSISTENT_READS(4),
  /**
   *
   *
   * <pre>
   * Writes are redirected to Cloud Firestore.
   * </pre>
   *
   * <code>REDIRECT_WRITES = 5;</code>
   */
  REDIRECT_WRITES(5),
  UNRECOGNIZED(-1),
  ;

  /**
   *
   *
   * <pre>
   * Unspecified.
   * </pre>
   *
   * <code>MIGRATION_STEP_UNSPECIFIED = 0;</code>
   */
  public static final int MIGRATION_STEP_UNSPECIFIED_VALUE = 0;
  /**
   *
   *
   * <pre>
   * Pre-migration: the database is prepared for migration.
   * </pre>
   *
   * <code>PREPARE = 6;</code>
   */
  public static final int PREPARE_VALUE = 6;
  /**
   *
   *
   * <pre>
   * Start of migration.
   * </pre>
   *
   * <code>START = 1;</code>
   */
  public static final int START_VALUE = 1;
  /**
   *
   *
   * <pre>
   * Writes are applied synchronously to at least one replica.
   * </pre>
   *
   * <code>APPLY_WRITES_SYNCHRONOUSLY = 7;</code>
   */
  public static final int APPLY_WRITES_SYNCHRONOUSLY_VALUE = 7;
  /**
   *
   *
   * <pre>
   * Data is copied to Cloud Firestore and then verified to match the data in
   * Cloud Datastore.
   * </pre>
   *
   * <code>COPY_AND_VERIFY = 2;</code>
   */
  public static final int COPY_AND_VERIFY_VALUE = 2;
  /**
   *
   *
   * <pre>
   * Eventually-consistent reads are redirected to Cloud Firestore.
   * </pre>
   *
   * <code>REDIRECT_EVENTUALLY_CONSISTENT_READS = 3;</code>
   */
  public static final int REDIRECT_EVENTUALLY_CONSISTENT_READS_VALUE = 3;
  /**
   *
   *
   * <pre>
   * Strongly-consistent reads are redirected to Cloud Firestore.
   * </pre>
   *
   * <code>REDIRECT_STRONGLY_CONSISTENT_READS = 4;</code>
   */
  public static final int REDIRECT_STRONGLY_CONSISTENT_READS_VALUE = 4;
  /**
   *
   *
   * <pre>
   * Writes are redirected to Cloud Firestore.
   * </pre>
   *
   * <code>REDIRECT_WRITES = 5;</code>
   */
  public static final int REDIRECT_WRITES_VALUE = 5;

  public final int getNumber() {
    if (this == UNRECOGNIZED) {
      throw new java.lang.IllegalArgumentException(
          "Can't get the number of an unknown enum value.");
    }
    return value;
  }

  /**
   * @param value The numeric wire value of the corresponding enum entry.
   * @return The enum associated with the given numeric wire value.
   * @deprecated Use {@link #forNumber(int)} instead.
   */
  @java.lang.Deprecated
  public static MigrationStep valueOf(int value) {
    return forNumber(value);
  }

  /**
   * @param value The numeric wire value of the corresponding enum entry.
   * @return The enum associated with the given numeric wire value.
   */
  public static MigrationStep forNumber(int value) {
    switch (value) {
      case 0:
        return MIGRATION_STEP_UNSPECIFIED;
      case 6:
        return PREPARE;
      case 1:
        return START;
      case 7:
        return APPLY_WRITES_SYNCHRONOUSLY;
      case 2:
        return COPY_AND_VERIFY;
      case 3:
        return REDIRECT_EVENTUALLY_CONSISTENT_READS;
      case 4:
        return REDIRECT_STRONGLY_CONSISTENT_READS;
      case 5:
        return REDIRECT_WRITES;
      default:
        return null;
    }
  }

  public static com.google.protobuf.Internal.EnumLiteMap<MigrationStep> internalGetValueMap() {
    return internalValueMap;
  }

  private static final com.google.protobuf.Internal.EnumLiteMap<MigrationStep> internalValueMap =
      new com.google.protobuf.Internal.EnumLiteMap<MigrationStep>() {
        public MigrationStep findValueByNumber(int number) {
          return MigrationStep.forNumber(number);
        }
      };

  public final com.google.protobuf.Descriptors.EnumValueDescriptor getValueDescriptor() {
    if (this == UNRECOGNIZED) {
      throw new java.lang.IllegalStateException(
          "Can't get the descriptor of an unrecognized enum value.");
    }
    return getDescriptor().getValues().get(ordinal());
  }

  public final com.google.protobuf.Descriptors.EnumDescriptor getDescriptorForType() {
    return getDescriptor();
  }

  public static final com.google.protobuf.Descriptors.EnumDescriptor getDescriptor() {
    return com.google.datastore.admin.v1.MigrationProto.getDescriptor().getEnumTypes().get(1);
  }

  private static final MigrationStep[] VALUES = values();

  public static MigrationStep valueOf(com.google.protobuf.Descriptors.EnumValueDescriptor desc) {
    if (desc.getType() != getDescriptor()) {
      throw new java.lang.IllegalArgumentException("EnumValueDescriptor is not for this type.");
    }
    if (desc.getIndex() == -1) {
      return UNRECOGNIZED;
    }
    return VALUES[desc.getIndex()];
  }

  private final int value;

  private MigrationStep(int value) {
    this.value = value;
  }

  // @@protoc_insertion_point(enum_scope:google.datastore.admin.v1.MigrationStep)
}
