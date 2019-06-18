/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 *
 * Modified by McLeod Moores Software Limited.
 *
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.integration.copier.sheet.reader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.ArgumentChecker;

import au.com.bytecode.opencsv.CSVReader;

/**
 * A class to facilitate importing portfolio data from comma-separated value files.
 */
public class CsvSheetReader extends SheetReader {

  private final CSVReader _csvReader;

  /**
   * @param filename
   *          the portfolio data file, not null or empty
   */
  public CsvSheetReader(final String filename) {
    ArgumentChecker.notEmpty(filename, "filename");

    // Open file
    final InputStream fileInputStream = openFile(filename);

    // Set up CSV reader
    _csvReader = new CSVReader(new InputStreamReader(fileInputStream));

    // Set columns
    setColumns(readHeaderRow());
  }

  /**
   * @param inputStream
   *          a portfolio data file stream, not null
   */
  public CsvSheetReader(final InputStream inputStream) {
    ArgumentChecker.notNull(inputStream, "inputStream");

    // Set up CSV reader
    _csvReader = new CSVReader(new InputStreamReader(inputStream));

    // Set columns
    setColumns(readHeaderRow());
  }

  @Override
  public Map<String, String> loadNextRow() {

    // Read in next row
    String[] rawRow;
    try {
      rawRow = _csvReader.readNext();
    } catch (final IOException ex) {
      throw new OpenGammaRuntimeException("Error reading CSV file data row: " + ex.getMessage());
    }

    // Return null if EOF
    if (rawRow == null) {
      return null;
    }

    // Map read-in row onto expected columns
    final Map<String, String> result = new HashMap<>();
    for (int i = 0; i < getColumns().length; i++) {
      if (i >= rawRow.length) {
        break;
      }
      if (rawRow[i] != null && rawRow[i].trim().length() > 0) {
        result.put(getColumns()[i], rawRow[i]);
      }
    }

    return result;
  }

  private String[] readHeaderRow() {
    // Read in the header row
    String[] rawRow;
    try {
      rawRow = _csvReader.readNext();
    } catch (final IOException ex) {
      throw new OpenGammaRuntimeException("Error reading CSV file header row: " + ex.getMessage());
    }

    // Normalise read-in headers (to lower case) and set as columns
    final String[] columns = new String[rawRow.length];
    for (int i = 0; i < rawRow.length; i++) {
      columns[i] = rawRow[i].trim().toLowerCase();
    }

    return columns;
  }

  @Override
  public void close() {
    try {
      _csvReader.close();
    } catch (final IOException ex) {

    }
  }
}
