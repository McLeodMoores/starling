/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.db.management;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.opengamma.OpenGammaRuntimeException;

/**
 * A database catalog creation strategy implementation that uses SQL.
 */
public class Oracle11gCatalogCreationStrategy implements CatalogCreationStrategy {

  private final AbstractDbManagement _dbManagement;
  private final String _user;
  private final String _password;
  private final String _systemUser;
  private final String _systemPassword;
  private final String _allCatalogsSql;
  private final String _blankCatalog;

  /**
   * Creates an instance.
   * @param dbManagement  the dialect-specific db management class
   * @param user  the user name
   * @param password  the password
   * @param systemUser the system user name
   * @param systemPassword the system password
   * @param getAllCatalogsSql  the SQL to get all catalogs, not null
   * @param blankCatalog  the catalog name to create, not null
   */
  public Oracle11gCatalogCreationStrategy(
      final AbstractDbManagement dbManagement,
      final String user,
      final String password,
      final String systemUser,
      final String systemPassword,
      final String getAllCatalogsSql,
      final String blankCatalog) {
    _dbManagement = dbManagement;
    _user = user;
    _password = password;
    _systemUser = systemUser;
    _systemPassword = systemPassword;
    _allCatalogsSql = getAllCatalogsSql;
    _blankCatalog = blankCatalog;
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean catalogExists(final String catalog) {
    Connection conn = null;
    try {
      if (_systemUser != null && !_systemUser.equals("")) {
        conn = DriverManager.getConnection(getCatalogToConnectTo(), _systemUser, _systemPassword);
      } else if (_user != null && !_user.equals("")) {
        conn = DriverManager.getConnection(getCatalogToConnectTo(), _user, _password);
      } else {
        // PLAT-2745, if we do not have a user, then client may be
        // attempting to login to MSSQL using integratedSecurity
        // and just the url should be sufficient
        conn = DriverManager.getConnection(getCatalogToConnectTo());
      }
      conn.setAutoCommit(true);

      final Statement statement = conn.createStatement();
      final ResultSet rs = statement.executeQuery(_allCatalogsSql);

      boolean catalogAlreadyExists = false;
      while (rs.next()) {
        final String name = rs.getString("name");
        if (name.equalsIgnoreCase(_user)) {
          catalogAlreadyExists = true;
        }
      }

      rs.close();
      statement.close();

      return catalogAlreadyExists;

    } catch (final SQLException e) {
      throw new OpenGammaRuntimeException("Failed to create catalog", e);
    } finally {
      if (conn != null) {
        try {
          conn.close();
        } catch (final SQLException e) {
        }
      }
    }
  }

  private String getCatalogToConnectTo() {
    if (_blankCatalog == null) {
      return _dbManagement.getDbHost();
    }
    return _dbManagement.getCatalogToConnectTo(_blankCatalog);
  }

  @Override
  public void create(final String catalog) {
    if (catalogExists(catalog)) {
      return; // nothing to do
    }

    Connection conn = null;
    try {
      if (_systemUser != null && !_systemUser.equals("")) {
        conn = DriverManager.getConnection(getCatalogToConnectTo(), _systemUser, _systemPassword);
      } else {
        conn = DriverManager.getConnection(getCatalogToConnectTo());
      }
      conn.setAutoCommit(true);

      final String createCatalogSql = "CREATE USER " + _user + " IDENTIFIED BY " + _password + "\n"
          + "DEFAULT TABLESPACE users\n"
          + "TEMPORARY TABLESPACE temp\n"
          + "QUOTA UNLIMITED ON users";
      //"GRANT CONNECT TO " + _user + ";\n" +
      //"GRANT CREATE TABLE TO " + _user + ";\n" +
      //"GRANT CREATE SEQUENCE TO " + _user + ";";

      final Statement statement = conn.createStatement();
      //statement.addBatch("DROP USER " + _user + " CASCADE");
      statement.addBatch(createCatalogSql);
      statement.addBatch("GRANT CONNECT TO " + _user);
      statement.addBatch("GRANT CREATE TABLE TO " + _user);
      statement.addBatch("GRANT CREATE SEQUENCE TO " + _user);
      statement.executeBatch();
      statement.close();

    } catch (final SQLException e) {
      throw new OpenGammaRuntimeException("Failed to create catalog", e);
    } finally {
      if (conn != null) {
        try {
          conn.close();
        } catch (final SQLException e) {
        }
      }
    }
  }

}
