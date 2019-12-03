/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web;

/**
 * Known sheet formats. Copied from the integration package.
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
    }
    return SheetFormat.UNKNOWN;
  }

}
