/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 *
 * Modified by McLeod Moores Software Limited.
 *
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.integration.copier.sheet.writer;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *  Util to create and output .xls files.
 */
public class XlsWriter {

  private static final Logger LOGGER = LoggerFactory.getLogger(XlsWriter.class);
  private final HSSFWorkbook _workbook;
  private final String _fileName;
  private final OutputStream _output;

  /**
   * @param filename  the file name, not null
   */
  public XlsWriter(final String filename) {
    _workbook = new HSSFWorkbook();
    _fileName = filename;
    _output = SheetWriter.openFile(_fileName);
  }

  /**
   * Gets the workbook.
   *
   * @return  the workbook
   */
  public HSSFWorkbook getWorkbook() {
    return _workbook;
  }

  /**
   * Closes the file and output stream.
   */
  public void close() {
    try {
      _workbook.write(_output);
      _output.close();
      LOGGER.info("XLS successfully written: {}", _fileName);
    } catch (final IOException e) {
      LOGGER.error("Error writing/outputting XLS: {}", _fileName);
      e.printStackTrace();
    }
  }
}
