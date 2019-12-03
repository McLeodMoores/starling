/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.bundle;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides a build stamp consisting of a version and time/date to be appended to resource urls.
 */
public class BuildData {

  /** Stamp to be appended to resource urls */
  private static final String STAMP;
  /** Logger. */
  private static final Logger LOGGER = LoggerFactory.getLogger(ScriptTag.class);

  static {
    final Properties prop = new Properties();
    FileInputStream input = null;
    String result;
    try {
      final String resource = ClassLoader.getSystemResource("com/opengamma/web/bundle/build-data.properties").getPath();
      input = new FileInputStream(new File(resource));
      prop.load(input);
      result = prop.getProperty("web.build.timestamp");
    } catch (final Exception e) {
      result = "default";
      LOGGER.warn("Failed to load build data for resource urls", e);
    } finally {
      IOUtils.closeQuietly(input);
    }
    STAMP = result;
  }

  /** @return the stamp of type String */

  public static String getBuildStamp() {
    return STAMP;
  }
}
