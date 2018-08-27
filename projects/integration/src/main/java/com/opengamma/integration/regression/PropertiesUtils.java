/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.regression;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.springframework.core.io.Resource;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.ResourceUtils;

class PropertiesUtils {

  static Properties createProperties(final String configFile) {
    final Resource res = ResourceUtils.createResource(configFile);
    final Properties props = new Properties();
    try (InputStream in = res.getInputStream()) {
      if (in == null) {
        throw new FileNotFoundException(configFile);
      }
      props.load(in);
    } catch (final IOException e) {
      throw new OpenGammaRuntimeException("Failed to load config", e);
    }
    return props;
  }


}
