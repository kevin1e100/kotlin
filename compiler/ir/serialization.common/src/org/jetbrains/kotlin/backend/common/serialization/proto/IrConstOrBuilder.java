// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: compiler/ir/serialization.common/src/KotlinIr.proto

package org.jetbrains.kotlin.backend.common.serialization.proto;

public interface IrConstOrBuilder extends
    // @@protoc_insertion_point(interface_extends:org.jetbrains.kotlin.backend.common.serialization.proto.IrConst)
    org.jetbrains.kotlin.protobuf.MessageLiteOrBuilder {

  /**
   * <code>optional bool null = 1;</code>
   */
  boolean hasNull();
  /**
   * <code>optional bool null = 1;</code>
   */
  boolean getNull();

  /**
   * <code>optional bool boolean = 2;</code>
   */
  boolean hasBoolean();
  /**
   * <code>optional bool boolean = 2;</code>
   */
  boolean getBoolean();

  /**
   * <code>optional int32 char = 3;</code>
   */
  boolean hasChar();
  /**
   * <code>optional int32 char = 3;</code>
   */
  int getChar();

  /**
   * <code>optional int32 byte = 4;</code>
   */
  boolean hasByte();
  /**
   * <code>optional int32 byte = 4;</code>
   */
  int getByte();

  /**
   * <code>optional int32 short = 5;</code>
   */
  boolean hasShort();
  /**
   * <code>optional int32 short = 5;</code>
   */
  int getShort();

  /**
   * <code>optional int32 int = 6;</code>
   */
  boolean hasInt();
  /**
   * <code>optional int32 int = 6;</code>
   */
  int getInt();

  /**
   * <code>optional int64 long = 7;</code>
   */
  boolean hasLong();
  /**
   * <code>optional int64 long = 7;</code>
   */
  long getLong();

  /**
   * <code>optional float float = 8;</code>
   */
  boolean hasFloat();
  /**
   * <code>optional float float = 8;</code>
   */
  float getFloat();

  /**
   * <code>optional double double = 9;</code>
   */
  boolean hasDouble();
  /**
   * <code>optional double double = 9;</code>
   */
  double getDouble();

  /**
   * <code>optional .org.jetbrains.kotlin.backend.common.serialization.proto.IrDataIndex string = 10;</code>
   */
  boolean hasString();
  /**
   * <code>optional .org.jetbrains.kotlin.backend.common.serialization.proto.IrDataIndex string = 10;</code>
   */
  org.jetbrains.kotlin.backend.common.serialization.proto.IrDataIndex getString();
}