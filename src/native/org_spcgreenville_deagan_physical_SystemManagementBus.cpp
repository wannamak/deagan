#include "org_spcgreenville_deagan_physical_SystemManagementBus.h"

#include <cstdint>

#include <sys/ioctl.h>
#include <fcntl.h>
#include <unistd.h>

#define I2C_SLAVE	0x0703
#define I2C_SMBUS	0x0720
#define I2C_SMBUS_READ	1
#define I2C_SMBUS_WRITE	0
#define I2C_SMBUS_BLOCK_MAX	32
#define I2C_SMBUS_BYTE_DATA 2

union i2c_smbus_data {
  uint8_t byte;
  uint16_t word;
  uint8_t block[I2C_SMBUS_BLOCK_MAX + 2];
};

struct i2c_smbus_ioctl_data {
  char read_write;
  uint8_t command;
  int size;
  union i2c_smbus_data *data;
};

static inline int i2c_smbus_access(int fd, char rw, uint8_t command, int size, union i2c_smbus_data *data) {
  struct i2c_smbus_ioctl_data args;

  args.read_write = rw;
  args.command = command;
  args.size = size;
  args.data = data;
  return ioctl(fd, I2C_SMBUS, &args);
}

JNIEXPORT jint JNICALL Java_chimebox_physical_SystemManagementBus_readByte(
    JNIEnv *env, jobject obj, jint fd, jint reg) {
  union i2c_smbus_data data;
  if (i2c_smbus_access(fd, I2C_SMBUS_READ, reg, I2C_SMBUS_BYTE_DATA, &data)) {
    return -1;
  } else {
    return data.byte & 0xFF;
  }
}

JNIEXPORT jint JNICALL Java_chimebox_physical_SystemManagementBus_writeByte(
    JNIEnv *env, jobject obj, jint fd, jint reg, jint value) {
  union i2c_smbus_data data;
  data.byte = value;
  return i2c_smbus_access(fd, I2C_SMBUS_WRITE, reg, I2C_SMBUS_BYTE_DATA, &data);
}

JNIEXPORT jint JNICALL Java_chimebox_physical_SystemManagementBus_initializeFileDescriptor(
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

