/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.db.management.jmx;

import static org.testng.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.mockito.Matchers;
import org.mockito.Mockito;
import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Tests the {@link HSQLDatabaseMBean} class.
 */
@Test(groups = TestGroup.UNIT)
public class HSQLDatabaseMBeanTest {

  private static File tmpdir() {
    return new File(System.getProperty("java.io.tmpdir"));
  }

  private static String name() {
    return "test" + System.nanoTime();
  }

  private static void delete(final File file) {
    final File[] subfiles = file.listFiles();
    if (subfiles != null) {
      for (final File subfile : subfiles) {
        if (!subfile.getName().startsWith(".")) {
          delete(subfile);
        }
      }
    }
    file.delete();
  }

  private static HSQLDatabaseMBean construct(final String jdbc, final DataSource ds) {
    final DatabaseMBean.Local impl = new DatabaseMBean.Local(HSQLDatabaseMBean.DRIVER_CLASS, ds);
    impl.setLocalJdbc(jdbc);
    final DatabaseMBean mbean = impl.mbean();
    assertEquals(mbean.getClass(), HSQLDatabaseMBean.class);
    return (HSQLDatabaseMBean) mbean;
  }

  private static HSQLDatabaseMBean construct(final String jdbc) {
    return construct(jdbc, Mockito.mock(DataSource.class));
  }

  /**
   * Tests creation of the backup name.
   */
  public void testCreateBackupName() {
    HSQLDatabaseMBean.flush();
    final HSQLDatabaseMBean mb1 = construct("jdbc:file:/path/to/something/Foo");
    final HSQLDatabaseMBean mb2 = construct("jdbc:file:/path/to/something/Foo");
    final HSQLDatabaseMBean mb3 = construct("jdbc:file:/path/to/something/Bar");
    final HSQLDatabaseMBean mb4 = construct("jdbc:file:/path/to/foo/Cow");
    final HSQLDatabaseMBean mb5 = construct("jdbc:file:/path/to/bar/Cow");
    final HSQLDatabaseMBean mb6 = construct("jdbc:file:/path/to/foo/Dog");
    assertEquals(mb1.createBackupName(), "Foo");
    assertEquals(mb2.createBackupName(), "Foo");
    assertEquals(mb3.createBackupName(), "Bar");
    assertEquals(mb4.createBackupName(), "foo-Cow");
    assertEquals(mb5.createBackupName(), "bar-Cow");
    assertEquals(mb6.createBackupName(), "Dog");
  }

  /**
   * Tests failure of backup name creation.
   */
  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testCreateBackupNameFail() {
    HSQLDatabaseMBean.flush();
    final HSQLDatabaseMBean mb1 = construct("jdbc:file:/a/x");
    final HSQLDatabaseMBean mb2 = construct("jdbc:file:/b/x");
    final HSQLDatabaseMBean mb3 = construct("jdbc:file:/a-x");
    mb1.createBackupName();
    mb2.createBackupName();
    mb3.createBackupName();
  }

  /**
   * Tests the behaviour when there is no property.
   */
  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testCreateBackupPathNoproperty() {
    final String preserve = System.getProperty("backup.dir");
    try {
      System.getProperties().remove("backup.dir");
      HSQLDatabaseMBean.flush();
      final HSQLDatabaseMBean mbean = construct("jdbc:file:/path/to/something/XYZ");
      mbean.createBackupPath();
    } finally {
      if (preserve != null) {
        System.setProperty("backup.dir", preserve);
      }
    }
  }

  /**
   * Tests the behaviour when the path is incorrect.
   *
   * @throws IOException  if there is a problem
   */
  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testCreateBackupPathCantcreate() throws IOException {
    final String preserve = System.getProperty("backup.dir");
    try {
      final File tmp = new File(tmpdir(), name());
      try {
        tmp.createNewFile();
        System.setProperty("backup.dir", tmp.getAbsolutePath());
        HSQLDatabaseMBean.flush();
        final HSQLDatabaseMBean mbean = construct("jdbc:file:/path/to/something/XYZ");
        mbean.createBackupPath();
      } finally {
        delete(tmp);
      }
    } finally {
      if (preserve != null) {
        System.setProperty("backup.dir", preserve);
      }
    }
  }

  /**
   * Tests the behaviour when the path does not point to a folder.
   *
   * @throws IOException  if there is an unexpected problem
   */
  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testCreateBackupPathNotFolder() throws IOException {
    final String preserve = System.getProperty("backup.dir");
    try {
      final File tmp = new File(tmpdir(), name());
      try {
        new File(tmp, "hsqldb").mkdirs();
        new File(tmp, "hsqldb" + File.separatorChar + "XYZ").createNewFile();
        System.setProperty("backup.dir", tmp.getAbsolutePath());
        HSQLDatabaseMBean.flush();
        final HSQLDatabaseMBean mbean = construct("jdbc:file:/path/to/something/XYZ");
        mbean.createBackupPath();
      } finally {
        delete(tmp);
      }
    } finally {
      if (preserve != null) {
        System.setProperty("backup.dir", preserve);
      }
    }
  }

  /**
   * Tests a backup.
   *
   * @throws SQLException  if there is a problem
   */
  public void testHotBackup() throws SQLException {
    final String preserve = System.getProperty("backup.dir");
    try {
      final File tmp = new File(tmpdir(), name());
      try {
        System.setProperty("backup.dir", tmp.getAbsolutePath());
        HSQLDatabaseMBean.flush();
        final DataSource ds = Mockito.mock(DataSource.class);
        final Connection c = Mockito.mock(Connection.class);
        Mockito.when(ds.getConnection()).thenReturn(c);
        final PreparedStatement ps = Mockito.mock(PreparedStatement.class);
        Mockito.when(c.prepareStatement("BACKUP DATABASE TO '" + tmp.getAbsolutePath() + File.separatorChar
            + "hsqldb" + File.separatorChar + "XYZ" + File.separatorChar + "' NOT BLOCKING"))
            .thenReturn(ps);
        final HSQLDatabaseMBean mbean = construct("jdbc:file:/path/to/something/XYZ", ds);
        assertEquals(mbean.hotBackup(), "Files backed up to:\n" + tmp.getAbsolutePath() + File.separatorChar
            + "hsqldb" + File.separatorChar + "XYZ" + File.separatorChar);
      } finally {
        delete(tmp);
      }
    } finally {
      if (preserve != null) {
        System.setProperty("backup.dir", preserve);
      }
    }
  }

  /**
   * Tests a backup when there is an error.
   *
   * @throws SQLException  if there is an unexpected problem
   */
  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testHotBackupError() throws SQLException {
    final String preserve = System.getProperty("backup.dir");
    try {
      final File tmp = new File(tmpdir(), name());
      try {
        System.setProperty("backup.dir", tmp.getAbsolutePath());
        HSQLDatabaseMBean.flush();
        final DataSource ds = Mockito.mock(DataSource.class);
        final Connection c = Mockito.mock(Connection.class);
        Mockito.when(ds.getConnection()).thenReturn(c);
        Mockito.when(c.prepareStatement(Matchers.anyString())).thenThrow(new SQLException());
        final HSQLDatabaseMBean mbean = construct("jdbc:file:/path/to/something/XYZ", ds);
        mbean.hotBackup();
      } finally {
        delete(tmp);
      }
    } finally {
      if (preserve != null) {
        System.setProperty("backup.dir", preserve);
      }
    }
  }

  /**
   * Tests a checkpointed backup.
   *
   * @throws SQLException  if there is a problem
   */
  public void testCheckpointedBackup() throws SQLException {
    final String preserve = System.getProperty("backup.dir");
    try {
      final File tmp = new File(tmpdir(), name());
      try {
        System.setProperty("backup.dir", tmp.getAbsolutePath());
        HSQLDatabaseMBean.flush();
        final DataSource ds = Mockito.mock(DataSource.class);
        final Connection c = Mockito.mock(Connection.class);
        Mockito.when(ds.getConnection()).thenReturn(c);
        final PreparedStatement ps = Mockito.mock(PreparedStatement.class);
        Mockito.when(c.prepareStatement("BACKUP DATABASE TO '" + tmp.getAbsolutePath() + File.separatorChar
            + "hsqldb" + File.separatorChar + "XYZ" + File.separatorChar + "' BLOCKING"))
            .thenReturn(ps);
        final HSQLDatabaseMBean mbean = construct("jdbc:file:/path/to/something/XYZ", ds);
        assertEquals(mbean.onlineBackup(), "Files backed up to:\n" + tmp.getAbsolutePath() + File.separatorChar
            + "hsqldb" + File.separatorChar + "XYZ" + File.separatorChar);
      } finally {
        delete(tmp);
      }
    } finally {
      if (preserve != null) {
        System.setProperty("backup.dir", preserve);
      }
    }
  }

  /**
   * Tests a checkpointed backup when there is an error.
   *
   * @throws SQLException  if there is an unexpected error
   */
  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testCheckpointedBackupError() throws SQLException {
    final String preserve = System.getProperty("backup.dir");
    try {
      final File tmp = new File(tmpdir(), name());
      try {
        System.setProperty("backup.dir", tmp.getAbsolutePath());
        HSQLDatabaseMBean.flush();
        final DataSource ds = Mockito.mock(DataSource.class);
        final Connection c = Mockito.mock(Connection.class);
        Mockito.when(ds.getConnection()).thenReturn(c);
        Mockito.when(c.prepareStatement(Matchers.anyString())).thenThrow(new SQLException());
        final HSQLDatabaseMBean mbean = construct("jdbc:file:/path/to/something/XYZ", ds);
        mbean.onlineBackup();
      } finally {
        delete(tmp);
      }
    } finally {
      if (preserve != null) {
        System.setProperty("backup.dir", preserve);
      }
    }
  }
}
