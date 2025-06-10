package org.spcgreenville.deagan;

import com.google.protobuf.TextFormat;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Logger;

public class ConfigReader {
  private final Logger logger = Logger.getLogger(ConfigReader.class.getName());

  public Proto.Config readConfig(String pathToConfig) throws IOException {
    Proto.Config.Builder configBuilder = Proto.Config.newBuilder();
    logger.info("Reading config from " + pathToConfig);
    try (BufferedReader br = new BufferedReader(new FileReader(pathToConfig))) {
      TextFormat.merge(br, configBuilder);
    }
    return configBuilder.build();
  }
}
