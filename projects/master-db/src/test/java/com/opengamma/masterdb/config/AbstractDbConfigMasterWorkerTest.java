/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.config;

import static com.google.common.collect.Lists.newArrayList;
import static com.opengamma.util.db.DbDateUtils.MAX_SQL_TIMESTAMP;
import static com.opengamma.util.db.DbDateUtils.toSqlTimestamp;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.threeten.bp.temporal.ChronoUnit.HOURS;
import static org.threeten.bp.temporal.ChronoUnit.MINUTES;

import java.sql.Types;
import java.util.List;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsgEnvelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.jdbc.core.support.SqlLobValue;
import org.springframework.jdbc.support.lob.DefaultLobHandler;
import org.springframework.jdbc.support.lob.LobHandler;
import org.testng.annotations.Test;
import org.threeten.bp.Clock;
import org.threeten.bp.Instant;
import org.threeten.bp.ZoneOffset;

import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.test.AbstractDbTest;
import com.opengamma.util.test.TestGroup;

/**
 * Base tests for DbConfigMasterWorker via DbConfigMaster.
 */
@Test(groups = TestGroup.UNIT_DB)
public abstract class AbstractDbConfigMasterWorkerTest extends AbstractDbTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractDbConfigMasterWorkerTest.class);
  private static final FudgeContext FUDGE_CONTEXT = OpenGammaFudgeContext.getInstance();

  protected DbConfigMaster _cfgMaster;
  protected Instant _version1aInstant;
  protected Instant _version1bInstant;
  protected Instant _version1cInstant;
  protected Instant _version2Instant;
  protected int _totalConfigs;
  protected int _totalExternalIds;
  protected int _totalBundles;

  public AbstractDbConfigMasterWorkerTest(final String databaseType, final String databaseVersion, final boolean readOnly) {
    super(databaseType, databaseVersion);
    LOGGER.info("running testcases for {}", databaseType);
  }

  //-------------------------------------------------------------------------
  @Override
  protected void doSetUp() {
    init();
  }

  @Override
  protected void doTearDown() {
    _cfgMaster = null;
  }

  @Override
  protected void doTearDownClass() {
    _cfgMaster = null;
  }

  //-------------------------------------------------------------------------
  protected ObjectId setupTestData(final Instant now) {
    final Clock origClock = _cfgMaster.getClock();
    try {
      _cfgMaster.setClock(Clock.fixed(now, ZoneOffset.UTC));

      final String initialValue = "initial";
      final ConfigItem<String> initial = ConfigItem.of(initialValue, "some_name");
      _cfgMaster.add(new ConfigDocument(initial));

      final ObjectId baseOid = initial.getObjectId();

      final List<ConfigDocument> firstReplacement = newArrayList();
      for (int i = 0; i < 5; i++) {
        final String val = "setup_" + i;
        final ConfigItem<String> item = ConfigItem.of(val, "some_name_" + i);
        final ConfigDocument doc = new ConfigDocument(item);
        doc.setVersionFromInstant(now.plus(i, MINUTES));
        firstReplacement.add(doc);
      }
      _cfgMaster.setClock(Clock.fixed(now.plus(1, HOURS), ZoneOffset.UTC));
      _cfgMaster.replaceVersions(baseOid.getObjectId(), firstReplacement);
      return baseOid;

    } finally {
      _cfgMaster.setClock(origClock);
    }
  }

  private void init() {
    _cfgMaster = new DbConfigMaster(getDbConnector());
    final Instant now = Instant.now();
    _cfgMaster.setClock(Clock.fixed(now, ZoneOffset.UTC));
    _version1aInstant = now.minusSeconds(102);
    _version1bInstant = now.minusSeconds(101);
    _version1cInstant = now.minusSeconds(100);
    _version2Instant = now.minusSeconds(50);
    addExternalIds();
    addExternalIdBundles();
    _totalConfigs = 6;
  }

  private void addExternalIds() {
    final FudgeMsgEnvelope env = FUDGE_CONTEXT.toFudgeMsg(ExternalId.of("A", "B"));
    final byte[] bytes = FUDGE_CONTEXT.toByteArray(env.getMessage());
    final String cls = ExternalId.class.getName();
    final LobHandler lobHandler = new DefaultLobHandler();
    final JdbcOperations template = _cfgMaster.getDbConnector().getJdbcOperations();
    template.update("INSERT INTO cfg_config VALUES (?,?,?,?,?, ?,?,?,?)",
        101, 101, toSqlTimestamp(_version1aInstant), MAX_SQL_TIMESTAMP, toSqlTimestamp(_version1aInstant), MAX_SQL_TIMESTAMP, "TestConfig101", cls,
        new SqlParameterValue(Types.BLOB, new SqlLobValue(bytes, lobHandler)));
    template.update("INSERT INTO cfg_config VALUES (?,?,?,?,?, ?,?,?,?)",
        102, 102, toSqlTimestamp(_version1bInstant), MAX_SQL_TIMESTAMP, toSqlTimestamp(_version1bInstant), MAX_SQL_TIMESTAMP, "TestConfig102", cls,
        new SqlParameterValue(Types.BLOB, new SqlLobValue(bytes, lobHandler)));
    template.update("INSERT INTO cfg_config VALUES (?,?,?,?,?, ?,?,?,?)",
        201, 201, toSqlTimestamp(_version1cInstant), toSqlTimestamp(_version2Instant), toSqlTimestamp(_version1cInstant), MAX_SQL_TIMESTAMP, "TestConfig201", cls,
        new SqlParameterValue(Types.BLOB, new SqlLobValue(bytes, lobHandler)));
    template.update("INSERT INTO cfg_config VALUES (?,?,?,?,?, ?,?,?,?)",
        202, 201, toSqlTimestamp(_version2Instant), MAX_SQL_TIMESTAMP, toSqlTimestamp(_version2Instant), MAX_SQL_TIMESTAMP, "TestConfig202", cls,
        new SqlParameterValue(Types.BLOB, new SqlLobValue(bytes, lobHandler)));
    _totalExternalIds = 3;
  }

  private void addExternalIdBundles() {
    final FudgeMsgEnvelope env = FUDGE_CONTEXT.toFudgeMsg(ExternalIdBundle.of(ExternalId.of("C", "D"), ExternalId.of("E", "F")));
    final byte[] bytes = FUDGE_CONTEXT.toByteArray(env.getMessage());
    final String cls = ExternalIdBundle.class.getName();
    final LobHandler lobHandler = new DefaultLobHandler();
    final JdbcOperations template = _cfgMaster.getDbConnector().getJdbcOperations();
    template.update("INSERT INTO cfg_config VALUES (?,?,?,?,?, ?,?,?,?)",
        301, 301, toSqlTimestamp(_version1aInstant), MAX_SQL_TIMESTAMP, toSqlTimestamp(_version1aInstant), MAX_SQL_TIMESTAMP, "TestConfig301", cls,
        new SqlParameterValue(Types.BLOB, new SqlLobValue(bytes, lobHandler)));
    template.update("INSERT INTO cfg_config VALUES (?,?,?,?,?, ?,?,?,?)",
        302, 302, toSqlTimestamp(_version1bInstant), MAX_SQL_TIMESTAMP, toSqlTimestamp(_version1bInstant), MAX_SQL_TIMESTAMP, "TestConfig302", cls,
        new SqlParameterValue(Types.BLOB, new SqlLobValue(bytes, lobHandler)));
    template.update("INSERT INTO cfg_config VALUES (?,?,?,?,?, ?,?,?,?)",
        401, 401, toSqlTimestamp(_version1cInstant), toSqlTimestamp(_version2Instant), toSqlTimestamp(_version1cInstant), MAX_SQL_TIMESTAMP, "TestConfig401", cls,
        new SqlParameterValue(Types.BLOB, new SqlLobValue(bytes, lobHandler)));
    template.update("INSERT INTO cfg_config VALUES (?,?,?,?,?, ?,?,?,?)",
        402, 401, toSqlTimestamp(_version2Instant), MAX_SQL_TIMESTAMP, toSqlTimestamp(_version2Instant), MAX_SQL_TIMESTAMP, "TestConfig402", cls,
        new SqlParameterValue(Types.BLOB, new SqlLobValue(bytes, lobHandler)));
    _totalBundles = 3;
  }

  //-------------------------------------------------------------------------
  protected void assert101(final ConfigDocument test) {
    final UniqueId uniqueId = UniqueId.of("DbCfg", "101", "0");
    assertNotNull(test);
    assertEquals(uniqueId, test.getUniqueId());
    assertEquals(_version1aInstant, test.getVersionFromInstant());
    assertEquals(null, test.getVersionToInstant());
    assertEquals(_version1aInstant, test.getCorrectionFromInstant());
    assertEquals(null, test.getCorrectionToInstant());
    assertEquals("TestConfig101", test.getName());
    assertEquals(ExternalId.of("A", "B"), test.getConfig().getValue());
  }

  protected void assert102(final ConfigDocument test) {
    final UniqueId uniqueId = UniqueId.of("DbCfg", "102", "0");
    assertNotNull(test);
    assertEquals(uniqueId, test.getUniqueId());
    assertEquals(_version1bInstant, test.getVersionFromInstant());
    assertEquals(null, test.getVersionToInstant());
    assertEquals(_version1bInstant, test.getCorrectionFromInstant());
    assertEquals(null, test.getCorrectionToInstant());
    assertEquals("TestConfig102", test.getName());
    assertEquals(ExternalId.of("A", "B"), test.getConfig().getValue());
  }

  protected void assert201(final ConfigDocument test) {
    final UniqueId uniqueId = UniqueId.of("DbCfg", "201", "0");
    assertNotNull(test);
    assertEquals(uniqueId, test.getUniqueId());
    assertEquals(_version1cInstant, test.getVersionFromInstant());
    assertEquals(_version2Instant, test.getVersionToInstant());
    assertEquals(_version1cInstant, test.getCorrectionFromInstant());
    assertEquals(null, test.getCorrectionToInstant());
    assertEquals("TestConfig201", test.getName());
    assertEquals(ExternalId.of("A", "B"), test.getConfig().getValue());
  }

  protected void assert202(final ConfigDocument test) {
    final UniqueId uniqueId = UniqueId.of("DbCfg", "201", "1");
    assertNotNull(test);
    assertEquals(uniqueId, test.getUniqueId());
    assertEquals(_version2Instant, test.getVersionFromInstant());
    assertEquals(null, test.getVersionToInstant());
    assertEquals(_version2Instant, test.getCorrectionFromInstant());
    assertEquals(null, test.getCorrectionToInstant());
    assertEquals(ExternalId.of("A", "B"), test.getConfig().getValue());
  }

}
