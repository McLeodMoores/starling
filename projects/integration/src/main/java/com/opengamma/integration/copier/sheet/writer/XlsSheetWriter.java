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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.poi.hssf.usermodel.HSSFPalette;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor.HSSFColorPredefined;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.ObjectsPair;
import com.opengamma.util.tuple.Pair;

/**
 * Provides the ability to create and write to sheets in a given workbook.
 */
public class XlsSheetWriter {
  private final HSSFSheet _sheet;
  private final HSSFWorkbook _workbook;
  private Integer _currentRowIndex = 0;
  private final CellStyle _keyBlockStyle;
  private final CellStyle _valueBlockStyle;
  private final CellStyle _axisStyle;
  private final HashSet<Integer> _columnIndices;

  /**
   * @param workbook  the workbook, not null
   * @param name  the sheet name, not null
   */
  public XlsSheetWriter(final HSSFWorkbook workbook, final String name) {

    ArgumentChecker.notEmpty(name, "name");
    ArgumentChecker.notNull(workbook, "workbook");

    _workbook = workbook;
    _sheet = _workbook.createSheet(name);
    _columnIndices = new HashSet<>();
    _keyBlockStyle = getKeyBlockStyle();
    _valueBlockStyle = getValueBlockStyle();
    _axisStyle = getAxisStyle();
  }

  /**
   * Auto size all accessed columns, note this should only be called just before the workbook is closed.
   */
  public void autoSizeAllColumns() {
    for (final int index : _columnIndices) {
      _sheet.autoSizeColumn(index);
    }
  }

  private Row getCurrentRow() {
    Row row = _sheet.getRow(_currentRowIndex);
    if (row == null) {
      row = _sheet.createRow(_currentRowIndex);
    }
    return row;
  }

  private Row getRow(final int rowIndex) {
    Row row = _sheet.getRow(rowIndex);
    if (row == null) {
      row = _sheet.createRow(rowIndex);
    }
    return row;
  }

  /**
   * Decrements the current row index.
   */
  public void decrementCurrentRowIndex() {
    _currentRowIndex--;
  }

  /**
   * @param row the current row
   * @param index the column index
   * @return Cell that matches the row/column co-ordinates
   * _columnIndices stores the unique column indices, needed for auto resize of columns
   */
  private Cell getCell(final Row row, final int index) {
    Cell cell = row.getCell(index);
    if (cell == null) {
      cell = row.createCell(index);
    }
    _columnIndices.add(index); //Store indices of columns
    return cell;
  }

  /**
   * @param row the current row
   * @param index the column index
   * @param cellType int that represents the type of cell
   * @return Cell that matches the row/column co-ordinates
   * _columnIndices stores the unique column indices, needed for auto resize of columns
   */
  private Cell getCell(final Row row, final int index, final CellType cellType) {
    Cell cell = row.getCell(index);
    if (cell == null) {
      cell = row.createCell(index, cellType);
    }
    _columnIndices.add(index); //Store indices of columns
    return cell;
  }

  private CellStyle getKeyBlockStyle() {
    final CellStyle style = _workbook.createCellStyle();
    final Font font = _workbook.createFont();
    font.setColor(HSSFColorPredefined.WHITE.getIndex());
    final HSSFPalette palette = _workbook.getCustomPalette();
    palette.setColorAtIndex(HSSFColorPredefined.BLUE.getIndex(), (byte) 3, (byte) 60, (byte) 90);
    style.setFillForegroundColor(HSSFColorPredefined.BLUE.getIndex());
    style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
    style.setFont(font);
    return style;
  }

  private CellStyle getValueBlockStyle() {
    final CellStyle style = _workbook.createCellStyle();
    final Font font = _workbook.createFont();
    final HSSFPalette palette = _workbook.getCustomPalette();
    palette.setColorAtIndex(HSSFColorPredefined.BLUE_GREY.getIndex(), (byte) 238, (byte) 238, (byte) 238);
    style.setFillForegroundColor(HSSFColorPredefined.BLUE_GREY.getIndex());
    style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
    style.setFont(font);
    return style;
  }

  private CellStyle getAxisStyle() {
    final CellStyle style = _workbook.createCellStyle();
    final Font font = _workbook.createFont();
    font.setColor(HSSFColorPredefined.WHITE.getIndex());
    final HSSFPalette palette = _workbook.getCustomPalette();
    palette.setColorAtIndex(HSSFColorPredefined.GREY_50_PERCENT.getIndex(), (byte) 68, (byte) 68, (byte) 68);
    style.setFillForegroundColor(HSSFColorPredefined.GREY_50_PERCENT.getIndex());
    style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
    style.setFont(font);
    return style;
  }

  /**
   * Writes a block.
   *
   * @param details  the details
   */
  public void writeKeyValueBlock(final Map<String, String> details) {
    ArgumentChecker.notNull(details, "details");

    for (final Map.Entry<String, String> entry : details.entrySet()) {
      final Row row = getCurrentRow();
      final Cell keyCell = getCell(row, 0);
      final Cell valueCell = getCell(row, 1);
      keyCell.setCellStyle(_keyBlockStyle);
      valueCell.setCellStyle(_valueBlockStyle);
      keyCell.setCellValue(entry.getKey());
      valueCell.setCellValue(entry.getValue());
      _currentRowIndex++;
    }
    _currentRowIndex++;
  }

  /**
   * Writes a block.
   *
   * @param details  the details
   */
  public void writeKeyPairBlock(final Map<String, ObjectsPair<String, String>> details) {
    ArgumentChecker.notNull(details, "details");
    CellStyle currentStyle = _keyBlockStyle;
    for (final Map.Entry<String, ObjectsPair<String, String>> entry : details.entrySet()) {
      final Row row = getCurrentRow();
      final Cell keyCell = getCell(row, 0);
      keyCell.setCellValue(entry.getKey());
      keyCell.setCellStyle(_keyBlockStyle);
      if (entry.getValue().getFirst() != null) {
        final Cell firstValueCell = getCell(row, 1);
        firstValueCell.setCellValue(entry.getValue().getFirst());
        firstValueCell.setCellStyle(currentStyle);
      }
      if (entry.getValue().getSecond() != null) {
        final Cell secondValueCell = getCell(row, 2);
        secondValueCell.setCellValue(entry.getValue().getSecond());
        secondValueCell.setCellStyle(currentStyle);
      }
      _currentRowIndex++;
      currentStyle = _valueBlockStyle;
    }
    _currentRowIndex++;
  }

  /**
   * Writes the data.
   *
   * @param xMap  Set of ordered labels for the x axis
   * @param yMap  Set of ordered labels for the y axis
   * @param label  String label for cell 0/0
   * @param valueMap  Map containing a Pair of x and y co-ordinates to value
   * @param cellValueType  int that represents the type of cell
   * @deprecated  use the method that takes {@link CellType}
   */
  @Deprecated
  public void writeMatrix(final Set<String> xMap,
                          final Set<String> yMap,
                          final String label,
                          final Map<Pair<String, String>, String> valueMap,
                          final int cellValueType) {
    writeMatrix(xMap, yMap, label, valueMap, CellType.forInt(cellValueType));
//    ArgumentChecker.notNull(xMap, "xMap");
//    ArgumentChecker.notNull(yMap, "yMap");
//    ArgumentChecker.notNull(valueMap, "valueMap");
//
//    //Maps used to store the index of each x and y axis
//    final Map<String, Integer> xCol = new HashMap<>();
//    final Map<String, Integer> yRow = new HashMap<>();
//
//    /* Print out the label */
//    final Row labelRow = getCurrentRow();
//    final Cell labelCell = getCell(labelRow, 0);
//    labelCell.setCellValue(label);
//    labelCell.setCellStyle(_axisStyle);
//
//    //Print out the x axis
//    int colIndex = 1;
//    for (final String entry : xMap) {
//      final Row row = getCurrentRow();
//      final Cell cell = getCell(row, colIndex);
//      cell.setCellValue(entry);
//      cell.setCellStyle(_axisStyle);
//      xCol.put(entry, colIndex);
//      colIndex++;
//    }
//
//    _currentRowIndex++;
//    //Print out the y axis
//    for (final String entry : yMap) {
//      final Row row = getCurrentRow();
//      final Cell cell = getCell(row, 0, cellValueType);
//      cell.setCellValue(entry);
//      cell.setCellStyle(_axisStyle);
//      yRow.put(entry, _currentRowIndex);
//      _currentRowIndex++;
//    }
//    _currentRowIndex++;
//
//    //Print out the values of the matrix, locate co-ordinates based on  key of valueMap and the xCol/yRow maps
//    for (final Map.Entry<Pair<String, String>, String> entry : valueMap.entrySet()) {
//      final Cell valueCell = getCell(getRow(yRow.get(entry.getKey().getSecond())), xCol.get(entry.getKey().getFirst()));
//      valueCell.setCellValue(entry.getValue());
//      valueCell.setCellStyle(_valueBlockStyle);
//    }
  }

  /**
   * Writes the data.
   *
   * @param xMap  Set of ordered labels for the x axis
   * @param yMap  Set of ordered labels for the y axis
   * @param label  String label for cell 0/0
   * @param valueMap  Map containing a Pair of x and y co-ordinates to value
   * @param cellType  int that represents the type of cell
   */
  public void writeMatrix(final Set<String> xMap,
                          final Set<String> yMap,
                          final String label,
                          final Map<Pair<String, String>, String> valueMap,
                          final CellType cellType) {
    ArgumentChecker.notNull(xMap, "xMap");
    ArgumentChecker.notNull(yMap, "yMap");
    ArgumentChecker.notNull(valueMap, "valueMap");

    //Maps used to store the index of each x and y axis
    final Map<String, Integer> xCol = new HashMap<>();
    final Map<String, Integer> yRow = new HashMap<>();

    /* Print out the label */
    final Row labelRow = getCurrentRow();
    final Cell labelCell = getCell(labelRow, 0);
    labelCell.setCellValue(label);
    labelCell.setCellStyle(_axisStyle);

    //Print out the x axis
    int colIndex = 1;
    for (final String entry : xMap) {
      final Row row = getCurrentRow();
      final Cell cell = getCell(row, colIndex);
      cell.setCellValue(entry);
      cell.setCellStyle(_axisStyle);
      xCol.put(entry, colIndex);
      colIndex++;
    }

    _currentRowIndex++;
    //Print out the y axis
    for (final String entry : yMap) {
      final Row row = getCurrentRow();
      final Cell cell = getCell(row, 0, cellType);
      cell.setCellValue(entry);
      cell.setCellStyle(_axisStyle);
      yRow.put(entry, _currentRowIndex);
      _currentRowIndex++;
    }
    _currentRowIndex++;

    //Print out the values of the matrix, locate co-ordinates based on  key of valueMap and the xCol/yRow maps
    for (final Map.Entry<Pair<String, String>, String> entry : valueMap.entrySet()) {
      final Cell valueCell = getCell(getRow(yRow.get(entry.getKey().getSecond())), xCol.get(entry.getKey().getFirst()));
      valueCell.setCellValue(entry.getValue());
      valueCell.setCellStyle(_valueBlockStyle);
    }
  }
}
