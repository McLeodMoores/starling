/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.integration.copier.portfolio.writer;

import java.util.ArrayList;
import java.util.List;

import org.springframework.util.StringUtils;

import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.ObjectsPair;

/**
 * A dummy portfolio writer, which pretty-prints information instead of persisting.
 */
public class PrettyPrintingPositionWriter implements PositionWriter {

  private String[] _currentPath = new String[] {};
  private final boolean _prettyPrint;

  public PrettyPrintingPositionWriter(final boolean prettyPrint) {
    _prettyPrint = prettyPrint;
  }

  private ManageableSecurity writeSecurity(final ManageableSecurity security) {

    if (_prettyPrint && security != null) {
      System.out.println("Security: " + security.toString());
    }
    return security;
  }

  @Override
  public void addAttribute(final String key, final String value) {
    // Not supported
  }

  @Override
  public ObjectsPair<ManageablePosition, ManageableSecurity[]> writePosition(final ManageablePosition position, final ManageableSecurity[] securities) {

    ArgumentChecker.notNull(position, "position");
    ArgumentChecker.notNull(securities, "securities");

    final List<ManageableSecurity> writtenSecurities = new ArrayList<>();

    // Write securities
    for (final ManageableSecurity security : securities) {
      final ManageableSecurity writtenSecurity = writeSecurity(security);
      if (writtenSecurity != null) {
        writtenSecurities.add(writtenSecurity);
      }
    }

    if (_prettyPrint) {
      System.out.println("Position: " + position.toString());
    }
    return ObjectsPair.of(position,
        writtenSecurities.toArray(new ManageableSecurity[writtenSecurities.size()]));
  }

  @Override
  public void flush() {
    if (_prettyPrint) {
      System.out.println("Flushed writer");
    }
  }

  @Override
  public void close() {
    if (_prettyPrint) {
      System.out.println("Closed writer");
    }
  }

  @Override
  public void setPath(final String[] newPath) {

    ArgumentChecker.notNull(newPath, "newPath");
    _currentPath = newPath;
    if (_prettyPrint) {
      System.out.println("Set path to: " + StringUtils.arrayToDelimitedString(newPath, "/"));
    }
  }

  @Override
  public String[] getCurrentPath() {
    return _currentPath;
  }

}
