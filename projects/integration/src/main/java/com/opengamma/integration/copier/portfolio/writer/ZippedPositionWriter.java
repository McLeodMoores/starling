/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.integration.copier.portfolio.writer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.integration.copier.portfolio.rowparser.JodaBeanRowParser;
import com.opengamma.integration.copier.portfolio.rowparser.RowParser;
import com.opengamma.integration.copier.sheet.SheetFormat;
import com.opengamma.integration.copier.sheet.writer.CsvSheetWriter;
import com.opengamma.integration.copier.sheet.writer.SheetWriter;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.ObjectsPair;

/**
 * This class writes positions/securities to a zip file, using the zip file's directory structure to represent the portfolio
 * node structure.
 */
public class ZippedPositionWriter implements PositionWriter {

  private static final Logger LOGGER = LoggerFactory.getLogger(ZippedPositionWriter.class);

  private static final String DIRECTORY_SEPARATOR = "/";

  private ZipOutputStream _zipFile;
  private final Map<String, Integer> _versionMap = new HashMap<>();
  private String[] _currentPath = new String[] {};
  private SingleSheetMultiParserPositionWriter _currentWriter;
  private Map<String, SingleSheetMultiParserPositionWriter> _writerMap = new HashMap<>();
  private Map<String, ByteArrayOutputStream> _bufferMap = new HashMap<>();

  /** Write multiple trades within a position as separate rows */
  private boolean _includeTrades;

  public ZippedPositionWriter(final SheetFormat sheetFormat, final OutputStream outputStream) {

    ArgumentChecker.notNull(sheetFormat, "sheetFormat");
    ArgumentChecker.notNull(outputStream, "outputStream");

    // Create zip file
    _zipFile = new ZipOutputStream(outputStream);

    _includeTrades = false;
  }

  public ZippedPositionWriter(final SheetFormat sheetFormat, final OutputStream outputStream, final boolean includeTrades) {
    this(sheetFormat, outputStream);
    _includeTrades = includeTrades;
  }

  public ZippedPositionWriter(final String filename) {

    ArgumentChecker.notEmpty(filename, "filename");

    // Confirm file doesn't already exist
    final File file = new File(filename);
    if (file.exists()) {
      file.delete();
      if (file.exists()) {
        throw new OpenGammaRuntimeException("Existing file " + filename + " could not be deleted");
      }
    }

    // Create zip file
    try {
      _zipFile = new ZipOutputStream(new FileOutputStream(filename));
    } catch (final IOException ex) {
      throw new OpenGammaRuntimeException("Could not create zip archive " + filename + " for writing: " + ex.getMessage());
    }

    _includeTrades = false;
  }

  public ZippedPositionWriter(final String filename, final boolean includeTrades) {
    this(filename);
    _includeTrades = includeTrades;
  }

  @Override
  public void addAttribute(final String key, final String value) {
    // Not supported
  }

  @Override
  public ObjectsPair<ManageablePosition, ManageableSecurity[]> writePosition(final ManageablePosition position, final ManageableSecurity[] securities) {

    ArgumentChecker.notNull(position, "position");
    ArgumentChecker.notNull(securities, "securities");

    identifyOrCreatePortfolioWriter(securities[0]);

    if (_currentWriter != null) {
      return _currentWriter.writePosition(position, securities);

    }
    LOGGER.warn("Could not identify a suitable parser for position: " + position.getName());
    return ObjectsPair.of(null, null);
  }

  @Override
  public void setPath(final String[] newPath) {

    ArgumentChecker.notNull(newPath, "newPath");

    // First, write the current set of CSVs to the zip file and clear the map of writers
    if (!getPathString(newPath).equals(getPathString(_currentPath))) {
      flushCurrentBuffers();

      // Change to the new path in the zip file (might need to create)
      if (newPath.length > 0) {
        final String path = StringUtils.arrayToDelimitedString(newPath, DIRECTORY_SEPARATOR) + DIRECTORY_SEPARATOR;
        final ZipEntry entry = new ZipEntry(path);
        try {
          _zipFile.putNextEntry(entry);
          _zipFile.closeEntry();
        } catch (final IOException ex) {
          // if failed, assume the directory already exists
          //throw new OpenGammaRuntimeException("Could not create folder " + entry.getName() + " in zip file: " + ex.getMessage());
        }
      }

      _currentPath = newPath;
    }
  }

  @Override
  public String[] getCurrentPath() {
    return _currentPath;
  }

  @Override
  public void flush() {
    try {
      _zipFile.flush();
    } catch (final IOException ex) {
      throw new OpenGammaRuntimeException("Could not flush data into zip archive");
    }
  }

  @Override
  public void close() {

    flushCurrentBuffers();
    flush();
    try {
      // Write version file
      writeMetaData("METADATA.INI");

      // Close zip archive
      _zipFile.close();
    } catch (final IOException ex) {
      throw new OpenGammaRuntimeException("Could not close zip file");
    }
  }


  private String getPathString(final String[] path) {
    String pathString = StringUtils.arrayToDelimitedString(path, DIRECTORY_SEPARATOR);
    if (pathString.length() > 0) {
      pathString += DIRECTORY_SEPARATOR;
    }
    return pathString;
  }

  private void flushCurrentBuffers() {

    final String path = getPathString(_currentPath);

    LOGGER.info("Flushing CSV buffers for ZIP directory " + path);

    for (final Map.Entry<String, ByteArrayOutputStream> entry : _bufferMap.entrySet()) {

      _writerMap.get(entry.getKey()).flush();
      _writerMap.get(entry.getKey()).close();

      final ZipEntry zipEntry = new ZipEntry(path + entry.getKey() + ".csv");
      LOGGER.info("Writing " + zipEntry.getName() + " to ZIP archive");
      try {
        _zipFile.putNextEntry(zipEntry);
        entry.getValue().writeTo(_zipFile);
        _zipFile.closeEntry();
        _zipFile.flush();
      } catch (final IOException ex) {
        throw new OpenGammaRuntimeException("Could not write file entry " + zipEntry.getName() + " to zip archive: " + ex.getMessage());
      }
    }

    _bufferMap = new HashMap<>();
    _writerMap = new HashMap<>();
  }

  private PositionWriter identifyOrCreatePortfolioWriter(final ManageableSecurity security) {

    // Identify the correct portfolio writer for this security
    String className = security.getClass().toString();
    className = className.substring(className.lastIndexOf('.') + 1).replace("Security", "");
    _currentWriter = _writerMap.get(className);
    RowParser parser;

    // create writer/output buffer map entry if not there for this security type
    if (_currentWriter == null) {

      LOGGER.info("Creating a new row parser for " + className + " securities");

      parser = JodaBeanRowParser.newJodaBeanRowParser(className);
      if (parser == null) {
        return null;
      }
      final Map<String, RowParser> parserMap = new HashMap<>();
      parserMap.put(className, parser);

      final ByteArrayOutputStream out = new ByteArrayOutputStream();
      final SheetWriter sheet = new CsvSheetWriter(out, parser.getColumns());

      _currentWriter = new SingleSheetMultiParserPositionWriter(sheet, parserMap, _includeTrades);

      _writerMap.put(className, _currentWriter);
      _bufferMap.put(className, out);
      _versionMap.put(className, parser.getSecurityHashCode());
    }

    return _currentWriter;
  }

  private void writeMetaData(final String filename) {
    try {
      _zipFile.putNextEntry(new ZipEntry(filename));
      _zipFile.write("# Generated by OpenGamma zipped portfolio writer\n\n".getBytes());
      _zipFile.write("[securityHashes]\n".getBytes());
      for (final Entry<String, Integer> entry : _versionMap.entrySet()) {
        _zipFile.write((entry.getKey() + " = " + Integer.toHexString(entry.getValue()) + "\n").getBytes());
      }
      _zipFile.closeEntry();
      _zipFile.flush();
    } catch (final IOException ex) {
      throw new OpenGammaRuntimeException("Could not write METADATA.INI to zip archive");
    }
  }

}
