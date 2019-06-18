/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 *
 * Modified by McLeod Moores Software Limited.
 *
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.integration.copier.sheet;

/**
 * Known sheet formats.
 */
public enum SheetFormat {

  /** CSV sheet. */
  CSV,

  /** XLS sheet. */
  XLS,

  /** XLSX sheet. */
  XLSX,

  /** XLS sheet. */
  XML,

  /** ZIP sheet. */
  ZIP,

  /** Unknown sheet. */
  UNKNOWN;

  /**
   * Returns the sheet format from the file extension.
   *
   * @param filename
   *          the file name
   * @return the sheet format
   */
  public static SheetFormat of(final String filename) {
    if (filename.lastIndexOf('.') < 0) {
      return SheetFormat.UNKNOWN;
    }
    final String extension = filename.substring(filename.lastIndexOf('.')).toLowerCase().trim();
    if (extension.equals(".csv")) {
      return SheetFormat.CSV;
    } else if (extension.equals(".xls")) {
      return SheetFormat.XLS;
    } else if (extension.equals(".xlsx")) {
      return SheetFormat.XLSX;
    } else if (extension.equals(".xml")) {
      return SheetFormat.XML;
    } else if (extension.equals(".zip")) {
      return SheetFormat.ZIP;
    } else {
      return SheetFormat.UNKNOWN;
    }
  }

}
