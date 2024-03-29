/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.copier.portfolio.reader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.integration.copier.portfolio.rowparser.JodaBeanRowParser;
import com.opengamma.integration.copier.portfolio.rowparser.RowParser;
import com.opengamma.integration.copier.sheet.reader.CsvSheetReader;
import com.opengamma.integration.copier.sheet.reader.SheetReader;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.ObjectsPair;

/**
 * Portfolio reader that reads multiple CSV files within a ZIP archive, identifies the correct parser class for each, using the file name, and persists all
 * loaded trades/entries using the specified portfolio writer. Folder structure in the ZIP archive is replicated in the portfolio node structure.
 */
public class ZippedPositionReader implements PositionReader {

  private static final Logger LOGGER = LoggerFactory.getLogger(ZippedPositionReader.class);

  private static final String SHEET_EXTENSION = ".csv";

  private ZipFile _zipFile;
  private final Map<String, Integer> _versionMap = new HashMap<>();

  private Enumeration<ZipEntry> _zipEntries;
  private PositionReader _currentReader;
  private String[] _currentPath = new String[0];
  private final boolean _ignoreVersion;

  @SuppressWarnings("unchecked")
  public ZippedPositionReader(final String filename, final boolean ignoreVersion) {

    ArgumentChecker.notNull(filename, "filename");

    try {
      _zipFile = new ZipFile(filename);
      _zipEntries = (Enumeration<ZipEntry>) _zipFile.entries();
    } catch (final IOException ex) {
      throw new OpenGammaRuntimeException("Could not open " + filename);
    }

    _ignoreVersion = ignoreVersion;

    if (!_ignoreVersion) {
      // Retrieve security hashes listed in config file
      readMetaData("METADATA.INI");
    }

    LOGGER.info("Using ZIP archive " + filename);
  }

  @Override
  public ObjectsPair<ManageablePosition, ManageableSecurity[]> readNext() {

    while (true) {
      // Try to get the next row from the current csv in the zip archive
      final ObjectsPair<ManageablePosition, ManageableSecurity[]> next = _currentReader == null ? null : _currentReader.readNext();

      if (next != null) {
        return next;

        // If no more rows to get in the current csv, and no more zip entries, then we've reached the end
      } else if (!_zipEntries.hasMoreElements()) {
        return null;

        // More zip entries, try get another csv and return a row from it
      } else {
        // Get the portfolio reader for this csv file
        _currentReader = getReader(_zipEntries.nextElement());
      }
    }
  }

  private PositionReader getReader(final ZipEntry entry) {

    if (!entry.isDirectory() && entry.getName().substring(entry.getName().lastIndexOf('.')).equalsIgnoreCase(SHEET_EXTENSION)) {
      try {
        // Extract full path
        final String[] fullPath = entry.getName().split("/");

        // Extract security name
        final String secType = fullPath[fullPath.length - 1].substring(0, fullPath[fullPath.length - 1].lastIndexOf('.'));

        _currentPath = (String[]) ArrayUtils.subarray(fullPath, 0, fullPath.length - 1);

        // Set up a sheet reader and a row parser for the current CSV file in the ZIP archive
        final SheetReader sheet = new CsvSheetReader(_zipFile.getInputStream(entry));

        final RowParser parser = JodaBeanRowParser.newJodaBeanRowParser(secType);
        if (parser == null) {
          LOGGER.error("Could not build a row parser for security type '" + secType + "'");
          return null;
        }
        if (!_ignoreVersion) {
          if (_versionMap.get(secType) == null) {
            LOGGER.error("Versioning hash for security type '" + secType + "' could not be found");
            return null;
          }
          if (parser.getSecurityHashCode() != _versionMap.get(secType)) {
            LOGGER.error("The parser version for the '" + secType + "' security (hash "
                + Integer.toHexString(parser.getSecurityHashCode())
                + ") does not match the data stored in the archive (hash "
                + Integer.toHexString(_versionMap.get(secType)) + ")");
            return null;
          }
        }

        LOGGER.info("Processing rows in archive entry " + entry.getName() + " as " + secType);

        // Create a simple portfolio reader for the current sheet
        return new SingleSheetSimplePositionReader(sheet, parser);

      } catch (final Throwable ex) {
        LOGGER.warn("Could not import from " + entry.getName() + ", skipping file (exception is " + ex + ")");
        return null;
      }
    }
    return null;
  }

  private void readMetaData(final String filename) {

    InputStream cfgInputStream;
    final ZipEntry cfgEntry = _zipFile.getEntry(filename);
    if (cfgEntry != null) {
      try {
        cfgInputStream = _zipFile.getInputStream(cfgEntry);
        final BufferedReader cfgReader = new BufferedReader(new InputStreamReader(cfgInputStream));

        String input;
        while ((input = cfgReader.readLine()) != null && !input.equals("[securityHashes]")) {
          // CSIGNORE
        }

        while ((input = cfgReader.readLine()) != null) {
          final String[] line = input.split("=", 2);
          if (line.length == 2) {
            try {
              _versionMap.put(line[0].trim(), (int) Long.parseLong(line[1].trim(), 16));
            } catch (final NumberFormatException e) {
              continue;
            }
          } else if (input.contains("[]")) {
            break;
          } else {
            continue;
          }
        }

      } catch (final IOException ex) {
        throw new OpenGammaRuntimeException("Could not open METADATA.INI");
      }
    } else {
      throw new OpenGammaRuntimeException("Could not find METADATA.INI");
    }
  }

  @Override
  public String[] getCurrentPath() {
    return _currentPath;
  }

  @Override
  public void close() {
    try {
      _zipFile.close();
    } catch (final IOException ex) {
    }
  }

  @Override
  public String getPortfolioName() {
    return null;
  }
}
