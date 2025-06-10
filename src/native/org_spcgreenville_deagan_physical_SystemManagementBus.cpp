#include "org_spcgreenville_deagan_physical_SystemManagementBus.h"

#include <cerrno>
#include <cstdint>
#include <cstring>

#include <sys/ioctl.h>
#include <fcntl.h>
#include <unistd.h>
#include <linux/i2c-dev.h>
#include <linux/i2c.h>

static inline int i2c_smbus_read_write_byte(int fd, char rw, uint8_t device_register,
    union i2c_smbus_data *data) {
  struct i2c_smbus_ioctl_data block;

  block.read_write = rw;
  block.command = device_register;
  block.size = I2C_SMBUS_BYTE_DATA;
  block.data = data;
  int result = ioctl(fd, I2C_SMBUS, &block);
  if (result < 0) {
    printf("ioctl error %d %s\n", errno, strerror(errno));
  }
  return result;
}

JNIEXPORT jint JNICALL Java_org_spcgreenville_deagan_physical_SystemManagementBus_readByteNative(
    JNIEnv *env, jobject obj, jint fd, jint device_register) {
  union i2c_smbus_data data;
  if (i2c_smbus_read_write_byte(fd, I2C_SMBUS_READ, device_register, &data)) {
    return -1;
  } else {
    return data.byte & 0xFF;
  }
}

JNIEXPORT jint JNICALL Java_org_spcgreenville_deagan_physical_SystemManagementBus_writeByteNative(
    JNIEnv *env, jobject obj, jint fd, jint device_register, jint value) {
  union i2c_smbus_data data;
  data.byte = value;
  return i2c_smbus_read_write_byte(fd, I2C_SMBUS_WRITE, device_register, &data);
}

JNIEXPORT jint JNICALL Java_org_spcgreenville_deagan_physical_SystemManagementBus_initializeFileDescriptor(
    JNIEnv *env, jobject obj, jstring device_path, jint device_id) {
  const char* c_device_path = env->GetStringUTFChars(device_path, 0);
  int fd;
  if ((fd = open(c_device_path, O_RDWR)) < 0) {
    fd = -2;
  } else if (ioctl(fd, I2C_SLAVE, device_id) < 0) {
    fd = -3;
  }
  env->ReleaseStringUTFChars(device_path, c_device_path);
  return fd;
}

