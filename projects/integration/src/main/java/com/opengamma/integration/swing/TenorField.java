/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.swing;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JTextField;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//import com.jidesoft.hints.ListDataIntelliHints;
import com.opengamma.util.time.Tenor;

/**
 * Class for implementing a Tenor component.
 */
public class TenorField extends JTextField {

  private static final Logger LOGGER = LoggerFactory.getLogger(TenorField.class);

  public TenorField() {
    super();
  }

  private List<Tenor> getAllTenors() {
    final List<Tenor> tenors = new ArrayList<>();
    final Field[] fields = Tenor.class.getFields();
    for (final Field field : fields) {
      if (field.isAccessible() && field.getType().isAssignableFrom(Tenor.class)) {
        try {
          tenors.add((Tenor) field.get(field));
        } catch (IllegalArgumentException | IllegalAccessException ex) {
          // TODO Auto-generated catch block
          LOGGER.debug("problem accessing Tenor field {}", field);
        }
      }
    }
    return tenors;
  }

}
