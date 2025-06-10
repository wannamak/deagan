#include "org_spcgreenville_deagan_physical_EdgeDetector.h"

#include <cstdlib>
#include <cstring>

#include <pthread.h>
#include <errno.h>
#include <gpiod.h>
#include <linux/gpio.h>

// https://github.com/Tieske/rpi-gpio/blob/master/source/event_gpio.c
// https://github.com/Tieske/rpi-gpio/blob/master/source/py_gpio.c
// py_add_event_detect
// add_edge_detect (starts thread)
// add_py_callback
// add_edge_callback

// new gpio
// https://github.com/torvalds/linux/blob/master/tools/gpio/gpio-event-mon.c


pthread_t threads;
void *poll_thread(void *arg);

struct poll_thread_context {
  jobject java_edge_type_enum_rising;
  jobject java_edge_type_enum_falling;
  jobject java_callback_object;
  jmethodID java_callback_method;
  gpiod_line_request* line_request;
  int num_pins;
  JNIEnv *env;
};

/**
 * Returns 0 on success, 1 on error.
 */
JNIEXPORT jint JNICALL Java_org_spcgreenville_deagan_physical_Event_beginEdgeDetection(
  JNIEnv *env, jobject obj, jstring chip_path, jintArray pins, jobject java_callback_object) {

  poll_thread_context* context = (poll_thread_context*) calloc(1, sizeof(poll_thread_context));
  context->env = env;
  context->java_callback_object = env->NewGlobalRef(java_callback_object);
  jclass java_callback_class = env->GetObjectClass(java_callback_object);
  context->java_callback_method =
      env->GetMethodID(java_callback_class, "onCallback", "(Ljava/lang/Integer;)V");

  jclass java_edge_type_class =
      env->FindClass("org/spcgreenville/deagan/physical/EdgeDetectCallback$EdgeType");
  jfieldID rising_field_id = env->GetStaticFieldID(java_edge_type_class,
      "RISING_EDGE", "Lorg/spcgreenville/deagan/physical/EdgeDetectCallback$EdgeType;");
  jfieldID falling_field_id = env->GetStaticFieldID(java_edge_type_class,
      "FALLOMG_EDGE", "Lorg/spcgreenville/deagan/physical/EdgeDetectCallback$EdgeType;");
  context->java_edge_type_enum_rising = env->GetStaticObjectField(
      java_edge_type_class, rising_field_id);
  context->java_edge_type_enum_falling = env->GetStaticObjectField(
      java_edge_type_class, falling_field_id);

  struct gpiod_chip *chip;
  const char* c_chip_path = env->GetStringUTFChars(chip_path, 0);
  chip = gpiod_chip_open(c_chip_path);
  if (!chip) {
    printf("open %s failed: %s\n", c_chip_path, strerror(errno));
    env->ReleaseStringUTFChars(chip_path, c_chip_path);
		return 1;
  }
  env->ReleaseStringUTFChars(chip_path, c_chip_path);

  struct gpiod_line_settings *settings = gpiod_line_settings_new();
  if (!settings) {
    printf("gpiod_line_settings_new failed: %s\n", strerror(errno));
    gpiod_chip_close(chip);
    return 1;
  }

  gpiod_line_settings_set_direction(settings, GPIOD_LINE_DIRECTION_INPUT);

  struct gpiod_line_config *line_cfg = gpiod_line_config_new();
  if (!line_cfg) {
    printf("gpiod_line_config_new failed: %s\n", strerror(errno));
    gpiod_line_settings_free(settings);
    gpiod_chip_close(chip);
    return 1;
  }

  context->num_pins = env->GetArrayLength(pins);
  unsigned int* native_pins = (unsigned int*)
      calloc(context->num_pins, sizeof(int));
  for (int i = 0; i < context->num_pins; i++) {
    jboolean isCopy_ignore;
    native_pins[i] = *env->GetIntArrayElements(pins, &isCopy_ignore);
  }
  if (gpiod_line_config_add_line_settings(line_cfg, native_pins, context->num_pins, settings)) {
    printf("gpiod_line_config_add_line_settings failed: %s\n", strerror(errno));
    gpiod_line_config_free(line_cfg);
    gpiod_line_settings_free(settings);
    gpiod_chip_close(chip);
    free(native_pins);
    return 1;
  }
  free(native_pins);

  struct gpiod_request_config *req_cfg = gpiod_request_config_new();
  if (!req_cfg) {
    printf("gpiod_request_config_new failed: %s\n", strerror(errno));
    gpiod_line_config_free(line_cfg);
    gpiod_line_settings_free(settings);
    gpiod_chip_close(chip);
    return 1;
  }

  context->line_request =
      gpiod_chip_request_lines(chip, req_cfg, line_cfg);
  if (!context->line_request) {
    printf("gpiod_chip_request_lines failed: %s\n", strerror(errno));
    gpiod_request_config_free(req_cfg);
    gpiod_line_config_free(line_cfg);
    gpiod_line_settings_free(settings);
    gpiod_chip_close(chip);
    return 1;
  }

  if (pthread_create(&threads, NULL, poll_thread, (void *)context) != 0) {
    printf("pthread_create failed: %s\n", strerror(errno));
    gpiod_request_config_free(req_cfg);
    gpiod_line_config_free(line_cfg);
    gpiod_line_settings_free(settings);
    gpiod_chip_close(chip);
    return 1;
  }

  return 0;
}

void *poll_thread(void *arg) {
  poll_thread_context* context = (poll_thread_context*) arg;
  while (1) {
    int result = gpiod_line_request_wait_edge_events(context->line_request, -1 /* wait forever */);
    if (result != 1 /* event pending */) {
      printf("Unexpect result %d from gpiod_line_request_wait_edge_events: %s", result, strerror(errno));
      continue;
    }
    int default_kernel_size = 16 * context->num_pins;
    struct gpiod_edge_event_buffer* edge_event_buffer =
        gpiod_edge_event_buffer_new(default_kernel_size);

    int num_events = gpiod_line_request_read_edge_events(
        context->line_request, edge_event_buffer, default_kernel_size);
    if (num_events == -1) {
      printf("gpiod_line_request_read_edge_events error: %s", strerror(errno));
      gpiod_edge_event_buffer_free(edge_event_buffer);
      continue;
    }

    for (int i = 0; i < num_events; i++) {
      gpiod_edge_event* edge_event = gpiod_edge_event_buffer_get_event(
          edge_event_buffer, i);

      gpiod_edge_event_type event_type = gpiod_edge_event_get_event_type(edge_event);
      int pin = gpiod_edge_event_get_line_offset(edge_event);

      jobject edge_type = event_type == GPIOD_EDGE_EVENT_RISING_EDGE
          ? context->java_edge_type_enum_rising
          : context->java_edge_type_enum_falling;

      context->env->CallVoidMethod(
          context->java_callback_object, context->java_callback_method, edge_type, pin);
    }
    gpiod_edge_event_buffer_free(edge_event_buffer);
  }

  pthread_exit(NULL);
}

