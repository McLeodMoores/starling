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
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.ObjectsPair;
import com.opengamma.util.tuple.Pair;

/**
 * A class for importing portfolio data from XLS worksheets
 */
public class XlsSheetReader extends SheetReader {
  private static final Logger LOGGER = LoggerFactory.getLogger(XlsSheetReader.class);

  private Sheet _sheet;
  private final Workbook _workbook;
  private int _currentRowIndex;
  private InputStream _inputStream;

  /**
   * Creates an Excel sheet reader.
   *
   * @param filename  the .xls file name, not null or empty
   * @param sheetIndex  the index of the sheet to read
   */
  public XlsSheetReader(final String filename, final int sheetIndex) {
    ArgumentChecker.notEmpty(filename, "filename");

    _inputStream = openFile(filename);
    _workbook = getWorkbook(_inputStream);
    _sheet = _workbook.getSheetAt(sheetIndex);
    _currentRowIndex = _sheet.getFirstRowNum();

    // Read in the header row
    final Row rawRow = _sheet.getRow(_currentRowIndex++);

    // Normalise read-in headers (to lower case) and set as columns
    setColumns(getColumnNames(rawRow));
  }

  /**
   * Creates an Excel sheet reader.
   *
   * @param filename  the .xls file name, not null or empty
   * @param sheetName  the name of the sheet to read, not null or empty
   */
  public XlsSheetReader(final String filename, final String sheetName) {

    ArgumentChecker.notEmpty(filename, "filename");
    ArgumentChecker.notEmpty(sheetName, "sheetName");

    final InputStream fileInputStream = openFile(filename);
    _workbook = getWorkbook(fileInputStream);
    _sheet = getSheetSafely(sheetName);
    _currentRowIndex = _sheet.getFirstRowNum();

    // Read in the header row
    final Row rawRow = _sheet.getRow(_currentRowIndex++);

    // Normalise read-in headers (to lower case) and set as columns
    setColumns(getColumnNames(rawRow));
  }

  /**
   * Creates an Excel sheet reader.
   *
   * @param inputStream  a file stream, not null
   * @param sheetIndex  the index of the sheet to read
   */
  public XlsSheetReader(final InputStream inputStream, final int sheetIndex) {
    ArgumentChecker.notNull(inputStream, "inputStream");

    _workbook = getWorkbook(inputStream);
    _sheet = _workbook.getSheetAt(sheetIndex);
    _currentRowIndex = _sheet.getFirstRowNum();

    // Read in the header row
    final Row rawRow = _sheet.getRow(_currentRowIndex++);

    // Normalise read-in headers (to lower case) and set as columns
    setColumns(getColumnNames(rawRow));
  }

  /**
   * Creates an Excel sheet reader.
   *
   * @param inputStream  a file stream, not null
   * @param sheetName  the name of the sheet to read
   */
  public XlsSheetReader(final InputStream inputStream, final String sheetName) {

    ArgumentChecker.notNull(inputStream, "inputStream");
    ArgumentChecker.notEmpty(sheetName, "sheetName");

    _workbook = getWorkbook(inputStream);
    _sheet = getSheetSafely(sheetName);
    _currentRowIndex = _sheet.getFirstRowNum();

    // Read in the header row
    final Row rawRow = _sheet.getRow(_currentRowIndex++);

    final String[] columns = getColumnNames(rawRow);
    setColumns(columns);
  }

  /**
   * Creates an Excel sheet reader.
   *
   * @param workbook  a .xls workbook, not null
   * @param sheetName  the name of the sheet to read, not null or empty
   */
  public XlsSheetReader(final Workbook workbook, final String sheetName) {
    ArgumentChecker.notNull(workbook, "workbook");
    ArgumentChecker.notEmpty(sheetName, "sheetName");
    _workbook = workbook;
    _sheet = getSheetSafely(sheetName);
    if (_sheet == null) {
      _sheet = _workbook.createSheet(sheetName);
      LOGGER.warn("Workbook does not contain a sheet for {}", sheetName);
    }
    _currentRowIndex = _sheet.getFirstRowNum();
  }

  private Sheet getSheetSafely(final String sheetName) {
    Sheet sheet = _workbook.getSheet(sheetName);
    if (sheet == null) {
      sheet = _workbook.createSheet(sheetName);
      LOGGER.warn("Workbook does not contain a sheet for {}, temporary sheet created", sheetName);
    }
    return sheet;
  }

  private static Workbook getWorkbook(final InputStream inputStream) {
    try {
      return new HSSFWorkbook(inputStream);
    } catch (final IOException ex) {
      throw new OpenGammaRuntimeException("Error opening Excel workbook: " + ex.getMessage());
    }
  }

  @Override
  public Map<String, String> loadNextRow() {

    // Get a reference to the next Excel row
    final Row rawRow = _sheet.getRow(_currentRowIndex++);

    // If the row is empty return null (assume end of table)
    if (rawRow == null || rawRow.getFirstCellNum() == -1) {
      return null; // new HashMap<String, String>();
    }

    // Map read-in row onto expected columns
    final Map<String, String> result = new HashMap<>();
    for (int i = 0; i < getColumns().length; i++) {
      final String cell = getCell(rawRow, rawRow.getFirstCellNum() + i).trim();
      if (cell != null && cell.length() > 0) {
        result.put(getColumns()[i], cell);
      }
    }

    return result;
  }

  private static String[] getColumnNames(final Row rawRow) {
    final String[] columns = new String[rawRow.getPhysicalNumberOfCells()];
    for (int i = 0; i < rawRow.getPhysicalNumberOfCells(); i++) {
      columns[i] = getCell(rawRow, i).trim().toLowerCase();
    }
    return columns;
  }

  private static Cell getCellSafe(final Row rawRow, final int column) {
    return rawRow.getCell(column, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
  }

  private static String getCell(final Row rawRow, final int column) {
    return getCellAsString(getCellSafe(rawRow, column));
  }

  private static String getCellAsString(final Cell cell) {

    if (cell == null) {
      return "";
    }
    switch (cell.getCellTypeEnum()) {
      case NUMERIC:
        return Double.toString(cell.getNumericCellValue());
      case STRING:
        return cell.getStringCellValue();
      case BOOLEAN:
        return Boolean.toString(cell.getBooleanCellValue());
      case BLANK:
        return "";
      default:
        return null;
    }
  }

  @Override
  public void close() {
    try {
      if (_inputStream != null) { //if sheet is multi sheeted, the first call with close input stream
        _inputStream.close();
      }
    } catch (final IOException ex) {
      throw new OpenGammaRuntimeException("Error closing Excel workbook: " + ex.getMessage());
    }
  }

  /**
   * Gets and increments the current row index.
   *
   * @return the current row index
   */
  public int getCurrentRowIndex() {
    return _currentRowIndex++;
  }

  /**
   * Reads a sheet from the start row and column and returns a map of key / values.
   *
   * @param startRow  int to specify starting point, _currentRowIndex is set to startRow
   * @param startCol  int to specify starting point
   * @return Map<String, String> of all key/values until and empty row is reached.
   */
  public Map<String, String> readKeyValueBlock(final int startRow, final int startCol) {
    final Map<String, String> keyValueMap = new HashMap<>();
    _currentRowIndex = startRow;
    Row row = _sheet.getRow(_currentRowIndex);
    while (row != null) {
      final Cell keyCell = row.getCell(startCol);
      final Cell valueCell = row.getCell(startCol + 1);
      keyValueMap.put(getCellAsString(keyCell), getCellAsString(valueCell));
      _currentRowIndex++;
      row = _sheet.getRow(_currentRowIndex);
    }
    _currentRowIndex++; //increment to prepare for next read method
    return keyValueMap;
  }

  /**
   * Reads a sheet from the start row and column and returns a map of key / pair values.
   *
   * @param startRow  int to specify starting point, _currentRowIndex is set to startRow
   * @param startCol  int to specify starting point
   * @return Map<String, ObjectsPair<String, String>> of all key/value-pair until and empty row is reached.
   */
  public Map<String, ObjectsPair<String, String>> readKeyPairBlock(final int startRow, final int startCol) {
    final Map<String, ObjectsPair<String, String>> keyPairMap = new HashMap<>();
    _currentRowIndex = startRow;
    Row row = _sheet.getRow(_currentRowIndex);
    while (row != null) {
      final Cell keyCell = row.getCell(startCol);
      final Cell firstValueCell = row.getCell(startCol + 1);
      final Cell secondValueCell = row.getCell(startCol + 2);
      try {
        final String stringCellValue = getCellAsString(keyCell);
        final String stringFirstCellValue = getCellAsString(firstValueCell);
        final String stringSecondCellValue = getCellAsString(secondValueCell);
        keyPairMap.put(stringCellValue,
                       ObjectsPair.of(stringFirstCellValue, stringSecondCellValue));
      } catch (final IllegalStateException ise) {
        LOGGER.error("Could not extract String value from cell col={} row={} sheet={}", startCol, _currentRowIndex, _sheet.getSheetName(), ise);
      }
      _currentRowIndex++;
      row = _sheet.getRow(_currentRowIndex);
    }
    _currentRowIndex++; //increment to prepare for next read method
    return keyPairMap;
  }

  /**
   * Reads a sheet from the start row and column and returns a map of ordinal-key / values.
   *
   * @param startRow  int to specify starting point, _currentRowIndex is set to startRow
   * @param startCol  int to specify starting point
   * @return Map<Pair<String, String>, String> of all ordinal-pair/value until and empty row is reached.
   */
  public Map<Pair<String, String>, String> readMatrix(final int startRow, final int startCol) {
    final Map<Pair<String, String>, String> valueMap = new HashMap<>();
    _currentRowIndex = startRow;
    int tempRowIndex = _currentRowIndex + 1; // Ignore top left cell
    //Maps used to store the index of each x and y axis
    final Map<Integer, String> colIndexToXAxis = new HashMap<>();
    final Map<Integer, String> rowIndexToYAxis = new HashMap<>();

    final Row xAxisRow = _sheet.getRow(_currentRowIndex);
    for (final Cell cell : xAxisRow) {
      final int columnIndex = cell.getColumnIndex();
      if (columnIndex != startCol) { // Ignore top left cell
        colIndexToXAxis.put(columnIndex, getCellAsString(cell));
      }
    }

    while (true) {
      final Row yAxisRow = _sheet.getRow(tempRowIndex);
      if (yAxisRow == null) {
        break;
      }
      final Cell yAxisCell = yAxisRow.getCell(startCol);
      rowIndexToYAxis.put(yAxisCell.getRowIndex(), getCellAsString(yAxisCell));
      tempRowIndex++;
    }

    _currentRowIndex++; //move to first row after x-axis

    while (true) {
      final Row valueRow = _sheet.getRow(_currentRowIndex);
      if (valueRow == null) {
        break;
      }
      for (final Cell valueCell : valueRow) {
        final int columnIndex = valueCell.getColumnIndex();
        if (columnIndex != startCol) { // Ignore left y axis cells
          final String xAxis = colIndexToXAxis.get(columnIndex);
          final String yAxis = rowIndexToYAxis.get(_currentRowIndex);
          valueMap.put(ObjectsPair.of(xAxis, yAxis), valueCell.getStringCellValue());
        }
      }
      _currentRowIndex++;
    }
    _currentRowIndex++; //increment to prepare for next read method

    return valueMap;
  }
}
