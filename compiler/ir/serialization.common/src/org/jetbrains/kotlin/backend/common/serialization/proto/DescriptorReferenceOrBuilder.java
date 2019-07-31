// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: compiler/ir/serialization.common/src/KotlinIr.proto

package org.jetbrains.kotlin.backend.common.serialization.proto;

public interface DescriptorReferenceOrBuilder extends
    // @@protoc_insertion_point(interface_extends:org.jetbrains.kotlin.backend.common.serialization.proto.DescriptorReference)
    org.jetbrains.kotlin.protobuf.MessageLiteOrBuilder {

  /**
   * <code>required .org.jetbrains.kotlin.backend.common.serialization.proto.FqName package_fq_name = 1;</code>
   */
  boolean hasPackageFqName();
  /**
   * <code>required .org.jetbrains.kotlin.backend.common.serialization.proto.FqName package_fq_name = 1;</code>
   */
  org.jetbrains.kotlin.backend.common.serialization.proto.FqName getPackageFqName();

  /**
   * <code>required .org.jetbrains.kotlin.backend.common.serialization.proto.FqName class_fq_name = 2;</code>
   */
  boolean hasClassFqName();
  /**
   * <code>required .org.jetbrains.kotlin.backend.common.serialization.proto.FqName class_fq_name = 2;</code>
   */
  org.jetbrains.kotlin.backend.common.serialization.proto.FqName getClassFqName();

  /**
   * <code>required .org.jetbrains.kotlin.backend.common.serialization.proto.IrDataIndex name = 3;</code>
   */
  boolean hasName();
  /**
   * <code>required .org.jetbrains.kotlin.backend.common.serialization.proto.IrDataIndex name = 3;</code>
   */
  org.jetbrains.kotlin.backend.common.serialization.proto.IrDataIndex getName();

  /**
   * <code>optional .org.jetbrains.kotlin.backend.common.serialization.proto.UniqId uniq_id = 4;</code>
   */
  boolean hasUniqId();
  /**
   * <code>optional .org.jetbrains.kotlin.backend.common.serialization.proto.UniqId uniq_id = 4;</code>
   */
  org.jetbrains.kotlin.backend.common.serialization.proto.UniqId getUniqId();

  /**
   * <code>optional bool is_getter = 5 [default = false];</code>
   */
  boolean hasIsGetter();
  /**
   * <code>optional bool is_getter = 5 [default = false];</code>
   */
  boolean getIsGetter();

  /**
   * <code>optional bool is_setter = 6 [default = false];</code>
   */
  boolean hasIsSetter();
  /**
   * <code>optional bool is_setter = 6 [default = false];</code>
   */
  boolean getIsSetter();

  /**
   * <code>optional bool is_backing_field = 7 [default = false];</code>
   */
  boolean hasIsBackingField();
  /**
   * <code>optional bool is_backing_field = 7 [default = false];</code>
   */
  boolean getIsBackingField();

  /**
   * <code>optional bool is_fake_override = 8 [default = false];</code>
   */
  boolean hasIsFakeOverride();
  /**
   * <code>optional bool is_fake_override = 8 [default = false];</code>
   */
  boolean getIsFakeOverride();

  /**
   * <code>optional bool is_default_constructor = 9 [default = false];</code>
   */
  boolean hasIsDefaultConstructor();
  /**
   * <code>optional bool is_default_constructor = 9 [default = false];</code>
   */
  boolean getIsDefaultConstructor();

  /**
   * <code>optional bool is_enum_entry = 10 [default = false];</code>
   */
  boolean hasIsEnumEntry();
  /**
   * <code>optional bool is_enum_entry = 10 [default = false];</code>
   */
  boolean getIsEnumEntry();

  /**
   * <code>optional bool is_enum_special = 11 [default = false];</code>
   */
  boolean hasIsEnumSpecial();
  /**
   * <code>optional bool is_enum_special = 11 [default = false];</code>
   */
  boolean getIsEnumSpecial();

  /**
   * <code>optional bool is_type_parameter = 12 [default = false];</code>
   *
   * <pre>
   * TODO optional bool is_type_alias = 13 [default = false];
   * </pre>
   */
  boolean hasIsTypeParameter();
  /**
   * <code>optional bool is_type_parameter = 12 [default = false];</code>
   *
   * <pre>
   * TODO optional bool is_type_alias = 13 [default = false];
   * </pre>
   */
  boolean getIsTypeParameter();
}