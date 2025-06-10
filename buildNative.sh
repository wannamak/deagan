#!/bin/sh

HEADER_OUT_DIR=src/native
CLASS_OUT_DIR=bin/production
JAVA_SRC_DIR=src/java/main/org/spcgreenville/deagan/physical
NATIVE_SRC_DIR=src/native
CLASSPATH=lib/guava-21.0.jar:$CLASS_OUT_DIR
PREFIX=org_spcgreenville_deagan
JAVA_VERSION=21

set -e

javac \
  -h $HEADER_OUT_DIR \
  -d $CLASS_OUT_DIR \
  -cp $CLASSPATH \
  $JAVA_SRC_DIR/EdgeDetector.java

javac \
  -h $HEADER_OUT_DIR \
  -d $CLASS_OUT_DIR \
  -cp $CLASSPATH \
  $JAVA_SRC_DIR/GPIOController.java

javac \
  -h $HEADER_OUT_DIR \
  -d $CLASS_OUT_DIR \
  -cp $CLASSPATH \
  $JAVA_SRC_DIR/GPIOChipInfoProvider.java

javac \
  -h $HEADER_OUT_DIR \
  -d $CLASS_OUT_DIR \
  -cp $CLASSPATH \
  $JAVA_SRC_DIR/SystemManagementBus.java

# Can't figure out how to get libgpiod2.so to load.
# For now, link it directly to the JNI shared library.
#  --verbose \
LIBRARY_PATH=libgpiod2 aarch64-linux-gnu-gcc \
  -shared \
  -fPIC \
  -O3 \
  -Ilibgpiod2 \
  -I/usr/include \
  -I/usr/lib/x86_64-linux-gnu/glib-2.0/include \
  -I/usr/lib/jvm/java-${JAVA_VERSION}-openjdk-amd64/include \
  -I/usr/lib/jvm/java-${JAVA_VERSION}-openjdk-amd64/include/linux \
  $NATIVE_SRC_DIR/${PREFIX}_physical_EdgeDetector.cpp \
  $NATIVE_SRC_DIR/${PREFIX}_physical_SystemManagementBus.cpp \
  $NATIVE_SRC_DIR/${PREFIX}_physical_GPIOController.cpp \
  $NATIVE_SRC_DIR/${PREFIX}_physical_GPIOChipInfoProvider.cpp \
  libgpiod2/libgpiod.a \
  -o bin/libdeagan.so
