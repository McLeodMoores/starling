/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 *
 * Modified by McLeod Moores Software Limited.
 *
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.financial.security.equity;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.MoreObjects;

/**
 * The description of a GICS code.
 * <p>
 * This provides a description for {@link GICSCode}.
 * <p>
 * This is an effective singleton.
 *
 * S&P provides an Excel file of GICS code mappings. To load support for these mappings, the file provided by S&P must be renamed 'gics_map.xls' and be located
 * in the classpath. If the file does not have this name or the Classloader cannot find it, an error will be logged and GICS code mapping will not be available.
 *
 * @see <a href="http://www.standardandpoors.com/indices/gics/en/us">Standard and Poors</a>
 */
final class GICSCodeDescription {

  /**
   * A static instance.
   */
  public static final GICSCodeDescription INSTANCE = new GICSCodeDescription();
  /** Logger. */
  private static final Logger LOGGER = LoggerFactory.getLogger(GICSCodeDescription.class);

  private static final String GICS_FILE_NAME = "gics_map.xls";

  /**
   * The descriptions.
   */
  private static final Map<String, String> DESCRIPTIONS = new HashMap<>();
  static {
    try (InputStream xlsStream = GICSCodeDescription.class.getClassLoader().getResourceAsStream(GICS_FILE_NAME)) {
      processGICSExcelWorkbook(xlsStream, DESCRIPTIONS);
    } catch (final IOException e) {
      LOGGER.warn("Problem processing S&P GICS mapping file: ", e);
    }
  }

  /**
   * Load S&P GICS code mappings from an Excel file stream.
   *
   * @param inputStream
   *          opened stream based on Excel file
   * @param gicsMap
   *          map to add mappings to
   * @throws IOException
   *           if the stream cannot be read
   */
  static void processGICSExcelWorkbook(final InputStream inputStream, final Map<String, String> gicsMap) throws IOException {
    try (Workbook workbook = new HSSFWorkbook(new BufferedInputStream(inputStream))) {
      processGICSExcelWorkbook(workbook, gicsMap);
    } catch (final IOException e) {
      LOGGER.warn("Unable to find S&P GICS Code Mapping file '" + GICS_FILE_NAME
          + "' in classpath; unable to use GICS Codes: " + e);
      return;
    }
  }

  /**
   * Load S&P GICS code mappings from an Apache POI {@link HSSFWorkbook}.
   *
   * @param workbook
   *          HSSFWorkbook to parse S&P GCIS Excel
   * @param gicsMap
   *          map to add mappings to
   */
  static void processGICSExcelWorkbook(final Workbook workbook, final Map<String, String> gicsMap) {
    // Assume 1 sheet
    final Sheet sheet = workbook.getSheetAt(0);
    if (sheet == null) {
      return;
    }
    for (int rowNum = sheet.getFirstRowNum(); rowNum <= sheet.getLastRowNum(); rowNum++) {
      final Row row = sheet.getRow(rowNum);
      if (row == null) {
        continue;
      }
      for (int cellNum = 0; cellNum < row.getPhysicalNumberOfCells(); cellNum++) {
        final Cell cell = row.getCell(cellNum, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
        if (isNumeric(cell)) {
          // worst case if the Excel file is in an incorrect (or updated) format
          // is that number -> random or empty string mappings will be created
          gicsMap.put(getGICSCellValue(cell), getGICSCellValue(row, cellNum + 1));
        }
      }
    }
  }

  /**
   * Get the value of the Apache POI Cell as a String. If the Cell type is numeric (always a double with POI), the value is converted to an integer. The GCIS
   * file does not contain any floating point values so (at this time) this is a valid operation.
   *
   * @param cell
   *          Apache POI Cell
   * @return String value
   */
  static String getGICSCellValue(final Cell cell) {
    if (cell == null) {
      return "";
    }
    switch (cell.getCellTypeEnum()) {
      case NUMERIC:
        return Integer.valueOf((int) cell.getNumericCellValue()).toString();
      case STRING:
        return cell.getStringCellValue();
      case BOOLEAN:
        return Boolean.toString(cell.getBooleanCellValue());
      case BLANK:
        return "";
      default:
        return "null";
    }
  }

  /**
   * Get the value of the Apache POI Cell specified by the row and cell num (column) as a String. If row,cellNum defines a null or blank cell, an empty String
   * is returned.
   *
   * @param row
   *          Apache POI Row
   * @param cellNum
   *          cell number in Row
   * @return String value of specified cell, or empty String if invalid cell
   */
  static String getGICSCellValue(final Row row, final int cellNum) {
    return getGICSCellValue(row.getCell(cellNum, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK));

  }

  /**
   * Determine if specified Cell contains a number or something else based on the cell type defined in the source Excel file.
   *
   * @param cell
   *          Apache POI Cell
   * @return true if numeric, false if any other type
   */
  static boolean isNumeric(final Cell cell) {
    return cell.getCellTypeEnum() == CellType.NUMERIC;
  }

  /**
   * Creates an instance.
   */
  private GICSCodeDescription() {
  }

  // -------------------------------------------------------------------------
  /**
   * Gets the description for the code.
   *
   * @param code
   *          the code to lookup, not null
   * @return the description, "Unknown" if not found
   */
  static String getDescription(final String code) {
    final String desc = DESCRIPTIONS.get(code);
    return MoreObjects.firstNonNull(desc, "Unknown");
  }

  /**
   * Gets all the sector descriptions.
   *
   * @return a collection of all the sector description strings
   */
  static Collection<String> getAllSectorDescriptions() {
    return getAllDescriptions(2);
  }

  /**
   * Gets all the industry group descriptions.
   *
   * @return a collection of all the industry group description strings
   */
  static Collection<String> getAllIndustryGroupDescriptions() {
    return getAllDescriptions(4);
  }

  /**
   * Gets all the industry descriptions.
   *
   * @return a collection of all the industry description strings
   */
  static Collection<String> getAllIndustryDescriptions() {
    return getAllDescriptions(6);
  }

  /**
   * Gets all the sub-industry descriptions.
   *
   * @return a collection of all the sub-industry description strings
   */
  static Collection<String> getAllSubIndustryDescriptions() {
    return getAllDescriptions(8);
  }

  /**
   * Get all descriptions with a particular code length.
   *
   * @param codeLength
   *          the number of digits in the code
   * @return a collection of all the description strings
   */
  private static Collection<String> getAllDescriptions(final int codeLength) {
    final Collection<String> results = new ArrayList<>();
    for (final Map.Entry<String, String> entry : DESCRIPTIONS.entrySet()) {
      if (entry.getKey().length() == codeLength) {
        results.add(entry.getValue());
      }
    }
    return results;
  }

  // -------------------------------------------------------------------------
  /**
   * Gets a simple string description for the class.
   *
   * @return the string, not null
   */
  @Override
  public String toString() {
    return getClass().getSimpleName();
  }

  /**
   * Output the current contents of the GICS map to the log.
   *
   * @param gicsMap
   *          Map of GICS code -> description
   */
  static void dumpGICSMap(final Map<String, String> gicsMap) {
    for (final Map.Entry<String, String> entry : gicsMap.entrySet()) {
      LOGGER.info(" {}  -> {} ", entry.getKey(), entry.getValue());
    }
  }

  /**
   * For testing. Logs the contents of the GICS code->description map that is loaded statically.
   *
   * @param args
   * @return
   */
  private boolean run(final String[] args) {
    LOGGER.info(this.toString() + " is initialising...");
    LOGGER.info("Current working directory is " + System.getProperty("user.dir"));
    dumpGICSMap(DESCRIPTIONS);
    LOGGER.info(this.toString() + " is finished.");
    return true;
  }

  /**
   * For standalone testing.
   *
   * @param args
   *          command line arguments
   */
  public static void main(final String[] args) { // CSIGNORE
    final boolean success = new GICSCodeDescription().run(args);
    System.exit(success ? 0 : 1);
  }

}
