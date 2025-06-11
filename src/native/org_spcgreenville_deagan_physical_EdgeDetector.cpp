#include "org_spcgreenville_deagan_physical_EdgeDetector.h"

#include "gpio_context.h"

#include <cstdlib>
#include <cstring>

#include <pthread.h>
#include <errno.h>
#include <gpiod.h>
#include <linux/gpio.h>

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
  JavaVM* jvm;
};

/**
 * Returns 0 on success, 1 on error.
 */
JNIEXPORT jint JNICALL Java_org_spcgreenville_deagan_physical_EdgeDetector_beginEdgeDetection(
  JNIEnv *env, jobject obj, jlong gpio_context_long, jobject java_callback_object) {

  poll_thread_context* context = new poll_thread_context;

  env->GetJavaVM(&context->jvm);

  context->java_callback_object = env->NewGlobalRef(java_callback_object);
  jclass java_callback_class = env->GetObjectClass(java_callback_object);
  context->java_callback_method =
      env->GetMethodID(
          java_callback_class,
          "onCallback",
          // javap -s EdgeDetectCallback.class
          "(Lorg/spcgreenville/deagan/physical/EdgeDetectCallback$EdgeType;I)V");

  jclass java_edge_type_class =
      env->FindClass("org/spcgreenville/deagan/physical/EdgeDetectCallback$EdgeType");
  jfieldID rising_field_id = env->GetStaticFieldID(java_edge_type_class,
      "RISING_EDGE", "Lorg/spcgreenville/deagan/physical/EdgeDetectCallback$EdgeType;");
  jfieldID falling_field_id = env->GetStaticFieldID(java_edge_type_class,
      "FALLING_EDGE", "Lorg/spcgreenville/deagan/physical/EdgeDetectCallback$EdgeType;");
  context->java_edge_type_enum_rising = env->GetStaticObjectField(
      java_edge_type_class, rising_field_id);
  context->java_edge_type_enum_falling = env->GetStaticObjectField(
      java_edge_type_class, falling_field_id);

  struct org_spcgreenville_deagan_gpio_context *gpio_context =
      (org_spcgreenville_deagan_gpio_context*) gpio_context_long;
  context->line_request = gpio_context->line_request;

  if (pthread_create(&threads, NULL, poll_thread, (void *)context) != 0) {
    printf("pthread_create failed: %s\n", strerror(errno));
    delete context;
    return 1;
  }

  return 0;
}

void *poll_thread(void *arg) {
  poll_thread_context* context = (poll_thread_context*) arg;

  // in the new thread:
  JNIEnv* env;
  //JavaVMAttachArgs args;
  //args.version = JNI_VERSION_1_6; // choose your JNI version
  //args.name = NULL; // you might want to give the java thread a name
  //args.group = NULL; // you might want to assign the java thread to a ThreadGroup
  context->jvm->AttachCurrentThread((void**)&env, NULL);

  while (1) {
    int result = gpiod_line_request_wait_edge_events(
        context->line_request,
        -1 /* wait forever */);
    if (result != 1 /* event pending */) {
      printf("Unexpect result %d from gpiod_line_request_wait_edge_events: %s",
          result, strerror(errno));
      continue;
    }

    int default_kernel_size = 16; // * num_pins
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

      env->CallVoidMethod(
          context->java_callback_object, context->java_callback_method, edge_type, pin);
    }
    gpiod_edge_event_buffer_free(edge_event_buffer);
  }

  pthread_exit(NULL);
}

