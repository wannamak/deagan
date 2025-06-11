#include <gpiod.h>
//#include <linux/gpio.h>

struct org_spcgreenville_deagan_gpio_context {
  struct gpiod_chip *chip;
  struct gpiod_line_request *line_request;
  int pin;
};