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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;

import com.opengamma.util.ArgumentChecker;

/**
 * A class to facilitate importing portfolio data from a JDBC query result.
 */
public class JdbcSheetReader extends SheetReader {
  private JdbcTemplate _jdbcTemplate;
  private List<Map<String, String>> _results;
  private Iterator<Map<String, String>> _iterator;

  /**
   * @param dataSource  a data source, not null
   * @param query  the query, not null
   */
  public JdbcSheetReader(final DataSource dataSource, final String query) {
    init(new JdbcTemplate(dataSource), query);
  }

  /**
   * @param jdbcTemplate  a template, not null
   * @param query  the query, not null
   */
  public JdbcSheetReader(final JdbcTemplate jdbcTemplate, final String query) {
    init(jdbcTemplate, query);
  }

  /**
   * Checks that the template and query are not null.
   *
   * @param jdbcTemplate  a template, not null
   * @param query  the query, not null
   */
  protected void init(final JdbcTemplate jdbcTemplate, final String query) {
    ArgumentChecker.notNull(jdbcTemplate, "jdbcTemplate");
    ArgumentChecker.notEmpty(query, "query");

    _jdbcTemplate = jdbcTemplate;

    final ResultSetExtractor<List<Map<String, String>>> extractor = new ResultSetExtractor<List<Map<String, String>>>() {
      @Override
      public List<Map<String, String>> extractData(final ResultSet rs) throws SQLException, DataAccessException {
        final String[] columns = new String[rs.getMetaData().getColumnCount()];
        for (int i = 0; i < rs.getMetaData().getColumnCount(); i++) {
          columns[i] = rs.getMetaData().getColumnName(i + 1);
        }
        setColumns(columns);
        final List<Map<String, String>> entries = new ArrayList<>();
        while (rs.next()) {
          final String[] rawRow = new String[rs.getMetaData().getColumnCount()];
          for (int i = 0; i < rs.getMetaData().getColumnCount(); i++) {
            rawRow[i] = rs.getString(i + 1);
          }
          final Map<String, String> result = new HashMap<>();
          // Map read-in row onto expected columns
          for (int i = 0; i < getColumns().length; i++) {
            if (i >= rawRow.length) {
              break;
            }
            if (rawRow[i] != null && rawRow[i].trim().length() > 0) {
              result.put(getColumns()[i], rawRow[i]);
            }
          }
          entries.add(result);
        }
        return entries;
      }
    };
    _results = getJDBCTemplate().query(query, extractor);
    _iterator = _results.iterator();
  }

  private JdbcTemplate getJDBCTemplate() {
    return _jdbcTemplate;
  }

  @Override
  public Map<String, String> loadNextRow() {
    if (_iterator.hasNext()) {
      return _iterator.next();
    }
    return null;
  }

  @Override
  public void close() {
  }
}
