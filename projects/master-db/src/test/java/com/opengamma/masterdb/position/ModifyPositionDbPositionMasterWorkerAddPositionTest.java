/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.position;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;
import org.threeten.bp.OffsetDateTime;
import org.threeten.bp.OffsetTime;

import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.position.ManageableTrade;
import com.opengamma.master.position.PositionDocument;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.DbTest;
import com.opengamma.util.test.TestGroup;

/**
 * Tests ModifyPositionDbPositionMasterWorker.
 */
@Test(groups = TestGroup.UNIT_DB)
public class ModifyPositionDbPositionMasterWorkerAddPositionTest extends AbstractDbPositionMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger LOGGER = LoggerFactory.getLogger(ModifyPositionDbPositionMasterWorkerAddPositionTest.class);

  @Factory(dataProvider = "databases", dataProviderClass = DbTest.class)
  public ModifyPositionDbPositionMasterWorkerAddPositionTest(final String databaseType, final String databaseVersion) {
    super(databaseType, databaseVersion, false);
    LOGGER.info("running testcases for {}", databaseType);
  }

  //-------------------------------------------------------------------------
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_add_nullDocument() {
    _posMaster.add(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_add_noPosition() {
    final PositionDocument doc = new PositionDocument();
    _posMaster.add(doc);
  }

  @Test
  public void test_add_add() {
    final Instant now = Instant.now(_posMaster.getClock());

    final ManageablePosition position = new ManageablePosition(BigDecimal.TEN, ExternalId.of("A", "B"));
    final PositionDocument doc = new PositionDocument();
    doc.setPosition(position);
    final PositionDocument test = _posMaster.add(doc);

    final UniqueId uniqueId = test.getUniqueId();
    assertNotNull(uniqueId);
    assertEquals("DbPos", uniqueId.getScheme());
    assertTrue(uniqueId.isVersioned());
    assertTrue(Long.parseLong(uniqueId.getValue()) >= 1000);
    assertEquals("0", uniqueId.getVersion());
    assertEquals(now, test.getVersionFromInstant());
    assertEquals(null, test.getVersionToInstant());
    assertEquals(now, test.getCorrectionFromInstant());
    assertEquals(null, test.getCorrectionToInstant());
    final ManageablePosition testPosition = test.getPosition();
    assertNotNull(testPosition);
    assertEquals(uniqueId, testPosition.getUniqueId());
    assertEquals(BigDecimal.TEN, testPosition.getQuantity());
    final ExternalIdBundle secKey = testPosition.getSecurityLink().getExternalId();
    assertEquals(1, secKey.size());
    assertTrue(secKey.getExternalIds().contains(ExternalId.of("A", "B")));
  }

  @Test
  public void test_addWithOneTrade_add() {
    final LocalDate tradeDate = _now.toLocalDate();
    final OffsetTime tradeTime = _now.toOffsetTime().minusSeconds(500);

    final ManageablePosition position = new ManageablePosition(BigDecimal.TEN, ExternalId.of("A", "B"));
    position.getTrades().add(new ManageableTrade(BigDecimal.TEN, ExternalId.of("A", "B"), tradeDate, tradeTime, ExternalId.of("CPS", "CPV")));

    final PositionDocument doc = new PositionDocument();
    doc.setPosition(position);
    final PositionDocument test = _posMaster.add(doc);

    final Instant now = Instant.now(_posMaster.getClock());
    final UniqueId uniqueId = test.getUniqueId();
    assertNotNull(uniqueId);
    assertEquals("DbPos", uniqueId.getScheme());
    assertTrue(uniqueId.isVersioned());
    assertTrue(Long.parseLong(uniqueId.getValue()) >= 1000);
    assertEquals("0", uniqueId.getVersion());
    assertEquals(now, test.getVersionFromInstant());
    assertEquals(null, test.getVersionToInstant());
    assertEquals(now, test.getCorrectionFromInstant());
    assertEquals(null, test.getCorrectionToInstant());
    final ManageablePosition testPosition = test.getPosition();
    assertNotNull(testPosition);
    assertEquals(uniqueId, testPosition.getUniqueId());
    assertEquals(BigDecimal.TEN, testPosition.getQuantity());
    final ExternalIdBundle secKey = testPosition.getSecurityLink().getExternalId();
    assertEquals(1, secKey.size());
    assertTrue(secKey.getExternalIds().contains(ExternalId.of("A", "B")));

    assertNotNull(testPosition.getTrades());
    assertEquals(1, testPosition.getTrades().size());
    final ManageableTrade testTrade = testPosition.getTrades().get(0);
    assertNotNull(testTrade);
    assertEquals(BigDecimal.TEN, testTrade.getQuantity());
    assertEquals(tradeDate, testTrade.getTradeDate());
    assertEquals(tradeTime, testTrade.getTradeTime());
    assertEquals(ExternalId.of("CPS", "CPV"), testTrade.getCounterpartyExternalId());
    assertEquals(secKey, testTrade.getSecurityLink().getExternalId());
  }

  @Test
  public void test_addWithOnePremiumTrade_add() {

    final LocalDate tradeDate = _now.toLocalDate();
    final OffsetTime tradeTime = _now.toOffsetTime().minusSeconds(500);

    final ManageablePosition position = new ManageablePosition(BigDecimal.TEN, ExternalId.of("A", "B"));
    final ManageableTrade trade = new ManageableTrade(BigDecimal.TEN, ExternalId.of("A", "B"), tradeDate, tradeTime, ExternalId.of("CPS", "CPV"));
    trade.setPremium(1000000.00);
    trade.setPremiumCurrency(Currency.USD);
    trade.setPremiumDate(tradeDate.plusDays(1));
    trade.setPremiumTime(tradeTime);
    position.getTrades().add(trade);

    final PositionDocument doc = new PositionDocument();
    doc.setPosition(position);
    final PositionDocument test = _posMaster.add(doc);

    final Instant now = Instant.now(_posMaster.getClock());
    final UniqueId uniqueId = test.getUniqueId();
    assertNotNull(uniqueId);
    assertEquals("DbPos", uniqueId.getScheme());
    assertTrue(uniqueId.isVersioned());
    assertTrue(Long.parseLong(uniqueId.getValue()) >= 1000);
    assertEquals("0", uniqueId.getVersion());
    assertEquals(now, test.getVersionFromInstant());
    assertEquals(null, test.getVersionToInstant());
    assertEquals(now, test.getCorrectionFromInstant());
    assertEquals(null, test.getCorrectionToInstant());
    final ManageablePosition testPosition = test.getPosition();
    assertNotNull(testPosition);
    assertEquals(uniqueId, testPosition.getUniqueId());
    assertEquals(BigDecimal.TEN, testPosition.getQuantity());
    final ExternalIdBundle secKey = testPosition.getSecurityLink().getExternalId();
    assertEquals(1, secKey.size());
    assertTrue(secKey.getExternalIds().contains(ExternalId.of("A", "B")));

    assertNotNull(testPosition.getTrades());
    assertEquals(1, testPosition.getTrades().size());
    final ManageableTrade testTrade = testPosition.getTrades().get(0);
    assertNotNull(testTrade);
    assertEquals(BigDecimal.TEN, testTrade.getQuantity());
    assertEquals(tradeDate, testTrade.getTradeDate());
    assertEquals(tradeTime, testTrade.getTradeTime());
    assertEquals(ExternalId.of("CPS", "CPV"), testTrade.getCounterpartyExternalId());
    assertEquals(secKey, testTrade.getSecurityLink().getExternalId());
    assertEquals(1000000.00, testTrade.getPremium());
    assertEquals(Currency.USD, testTrade.getPremiumCurrency());
    assertEquals(tradeDate.plusDays(1), testTrade.getPremiumDate());
    assertEquals(tradeTime, testTrade.getPremiumTime());
  }

  @Test
  public void test_addWithOnePremiumTrade_addThenGet() {
    final ManageablePosition position = new ManageablePosition(BigDecimal.TEN, ExternalId.of("A", "B"));

    final LocalDate tradeDate = _now.toLocalDate();
    final OffsetTime tradeTime = _now.toOffsetTime().minusSeconds(500);

    final ManageableTrade trade = new ManageableTrade(BigDecimal.TEN, ExternalId.of("A", "B"), tradeDate, tradeTime, ExternalId.of("CPS", "CPV"));
    trade.setPremium(1000000.00);
    trade.setPremiumCurrency(Currency.USD);
    trade.setPremiumDate(tradeDate.plusDays(1));
    trade.setPremiumTime(tradeTime);

    position.getTrades().add(trade);

    final PositionDocument doc = new PositionDocument();
    doc.setPosition(position);
    final PositionDocument added = _posMaster.add(doc);
    assertNotNull(added);
    assertNotNull(added.getUniqueId());

    final PositionDocument fromDb = _posMaster.get(added.getUniqueId());
    assertNotNull(fromDb);
    assertNotNull(fromDb.getUniqueId());

    assertEquals(added, fromDb);
  }

  @Test
  public void test_addWithTwoTrades_add() {
    final Instant now = Instant.now(_posMaster.getClock());

    final OffsetDateTime offsetDateTime = OffsetDateTime.now();

    final ManageablePosition position = new ManageablePosition(BigDecimal.TEN, ExternalId.of("A", "B"));
    position.getTrades().add(new ManageableTrade(BigDecimal.TEN, ExternalId.of("A", "C"), offsetDateTime.toLocalDate(), offsetDateTime.minusSeconds(600).toOffsetTime(), ExternalId.of("CPS", "CPV")));
    position.getTrades().add(new ManageableTrade(BigDecimal.TEN, ExternalId.of("A", "D"), offsetDateTime.toLocalDate(), offsetDateTime.minusSeconds(500).toOffsetTime(), ExternalId.of("CPS", "CPV")));

    final PositionDocument doc = new PositionDocument();
    doc.setPosition(position);
    final PositionDocument test = _posMaster.add(doc);

    final UniqueId portfolioId = test.getUniqueId();
    assertNotNull(portfolioId);
    assertEquals("DbPos", portfolioId.getScheme());
    assertTrue(portfolioId.isVersioned());
    assertTrue(Long.parseLong(portfolioId.getValue()) >= 1000);
    assertEquals("0", portfolioId.getVersion());
    assertEquals(now, test.getVersionFromInstant());
    assertEquals(null, test.getVersionToInstant());
    assertEquals(now, test.getCorrectionFromInstant());
    assertEquals(null, test.getCorrectionToInstant());
    final ManageablePosition testPosition = test.getPosition();
    assertNotNull(testPosition);
    assertEquals(portfolioId, testPosition.getUniqueId());
    assertEquals(BigDecimal.TEN, testPosition.getQuantity());
    final ExternalIdBundle secKey = testPosition.getSecurityLink().getExternalId();
    assertEquals(1, secKey.size());
    assertTrue(secKey.getExternalIds().contains(ExternalId.of("A", "B")));

    assertNotNull(testPosition.getTrades());
    assertTrue(testPosition.getTrades().size() == 2);
    for (final ManageableTrade testTrade : testPosition.getTrades()) {
      assertNotNull(testTrade);
      final UniqueId tradeId = testTrade.getUniqueId();
      assertNotNull(tradeId);
      assertEquals("DbPos", portfolioId.getScheme());
      assertTrue(portfolioId.isVersioned());
      assertTrue(Long.parseLong(portfolioId.getValue()) >= 1000);
      assertEquals("0", portfolioId.getVersion());
    }
  }

  @Test
  public void test_add_addThenGet() {
    final ManageablePosition position = new ManageablePosition(BigDecimal.TEN, ExternalId.of("A", "B"));
    final PositionDocument doc = new PositionDocument();
    doc.setPosition(position);
    final PositionDocument added = _posMaster.add(doc);
    assertNotNull(added);
    assertNotNull(added.getUniqueId());

    final PositionDocument fromDb = _posMaster.get(added.getUniqueId());
    assertNotNull(fromDb);
    assertNotNull(fromDb.getUniqueId());

    assertEquals(added, fromDb);
  }

  @Test
  public void test_addWithTradesAndAttributes_addThenGet() {
    final ManageablePosition position = new ManageablePosition(BigDecimal.TEN, ExternalId.of("A", "B"));
    position.addAttribute("PA1", "A");
    position.addAttribute("PA2", "B");

    final LocalDate tradeDate = _now.toLocalDate();
    final OffsetTime tradeTime = _now.toOffsetTime().minusSeconds(500);
    final ManageableTrade trade1 = new ManageableTrade(BigDecimal.TEN, ExternalId.of("A", "B"), tradeDate, tradeTime, ExternalId.of("CPS", "CPV"));
    trade1.addAttribute("TA11", "C");
    trade1.addAttribute("TA12", "D");
    trade1.addAttribute("TA13", "E");
    position.getTrades().add(trade1);

    final ManageableTrade trade2 = new ManageableTrade(BigDecimal.ONE, ExternalId.of("C", "D"), tradeDate, tradeTime, ExternalId.of("CPS", "CPV"));
    trade2.addAttribute("TA21", "F");
    trade2.addAttribute("TA22", "G");
    trade2.addAttribute("TA23", "H");
    trade2.addAttribute("TA24", "I");
    position.getTrades().add(trade2);

    final PositionDocument doc = new PositionDocument();
    doc.setPosition(position);
    final PositionDocument added = _posMaster.add(doc);
    assertNotNull(added);
    assertNotNull(added.getUniqueId());

    final PositionDocument fromDb = _posMaster.get(added.getUniqueId());
    assertNotNull(fromDb);
    assertNotNull(fromDb.getUniqueId());

    assertEquals(added, fromDb);
  }

  @Test
  public void test_addWithOneTrade_addThenGet() {
    final ManageablePosition position = new ManageablePosition(BigDecimal.TEN, ExternalId.of("A", "B"));

    final LocalDate tradeDate = _now.toLocalDate();
    final OffsetTime tradeTime = _now.toOffsetTime().minusSeconds(500);

    final ManageableTrade trade = new ManageableTrade(BigDecimal.TEN, ExternalId.of("A", "B"), tradeDate, tradeTime, ExternalId.of("CPS", "CPV"));
    trade.setProviderId(ExternalId.of("TRD", "123"));
    position.getTrades().add(trade);

    final PositionDocument doc = new PositionDocument();
    doc.setPosition(position);
    final PositionDocument added = _posMaster.add(doc);
    assertNotNull(added);
    assertNotNull(added.getUniqueId());

    final PositionDocument fromDb = _posMaster.get(added.getUniqueId());
    assertNotNull(fromDb);
    assertNotNull(fromDb.getUniqueId());

    assertEquals(added, fromDb);
  }

  @Test
  public void test_addWithTwoTrades_addThenGet() {
    final ManageablePosition position = new ManageablePosition(BigDecimal.valueOf(20), ExternalId.of("A", "B"));

    final OffsetDateTime offsetDateTime = OffsetDateTime.now();

    position.getTrades().add(new ManageableTrade(BigDecimal.TEN, ExternalId.of("A", "B"), offsetDateTime.toLocalDate(), offsetDateTime.minusSeconds(600).toOffsetTime(), ExternalId.of("CPS", "CPV")));
    position.getTrades().add(new ManageableTrade(BigDecimal.TEN, ExternalId.of("A", "C"), offsetDateTime.toLocalDate(), offsetDateTime.minusSeconds(500).toOffsetTime(), ExternalId.of("CPS", "CPV")));

    final PositionDocument doc = new PositionDocument();
    doc.setPosition(position);
    final PositionDocument added = _posMaster.add(doc);
    assertNotNull(added);
    assertNotNull(added.getUniqueId());

    final PositionDocument fromDb = _posMaster.get(added.getUniqueId());
    assertNotNull(fromDb);
    assertNotNull(fromDb.getUniqueId());

    assertEquals(added, fromDb);
  }

  @Test
  public void test_addTradeDeal_add() {
    final Instant now = Instant.now(_posMaster.getClock());

    final ManageablePosition position = new ManageablePosition(BigDecimal.TEN, ExternalId.of("A", "B"));

    final LocalDate tradeDate = _now.toLocalDate();
    final OffsetTime tradeTime = _now.toOffsetTime().minusSeconds(500);
    final ManageableTrade trade = new ManageableTrade(BigDecimal.TEN, ExternalId.of("A", "B"), tradeDate, tradeTime, ExternalId.of("CPS", "CPV"));
    trade.addAttribute("TA1", "C");
    trade.addAttribute("TA2", "D");
    trade.setDeal(new MockDeal("propOne", "propTwo"));
    position.getTrades().add(trade);

    final PositionDocument doc = new PositionDocument();
    doc.setPosition(position);
    final PositionDocument test = _posMaster.add(doc);

    final UniqueId uniqueId = test.getUniqueId();
    assertNotNull(uniqueId);
    assertEquals("DbPos", uniqueId.getScheme());
    assertTrue(uniqueId.isVersioned());
    assertTrue(Long.parseLong(uniqueId.getValue()) >= 1000);
    assertEquals("0", uniqueId.getVersion());
    assertEquals(now, test.getVersionFromInstant());
    assertEquals(null, test.getVersionToInstant());
    assertEquals(now, test.getCorrectionFromInstant());
    assertEquals(null, test.getCorrectionToInstant());
    final ManageablePosition testPosition = test.getPosition();
    assertNotNull(testPosition);
    assertEquals(uniqueId, testPosition.getUniqueId());
    assertEquals(BigDecimal.TEN, testPosition.getQuantity());
    final ExternalIdBundle secKey = testPosition.getSecurityLink().getExternalId();
    assertEquals(1, secKey.size());
    assertTrue(secKey.getExternalIds().contains(ExternalId.of("A", "B")));

    assertNotNull(testPosition.getTrades());
    assertEquals(1, testPosition.getTrades().size());
    final ManageableTrade testTrade = testPosition.getTrades().get(0);
    assertNotNull(testTrade);
    assertEquals(BigDecimal.TEN, testTrade.getQuantity());
    assertEquals(tradeDate, testTrade.getTradeDate());
    assertEquals(tradeTime, testTrade.getTradeTime());
    assertEquals(ExternalId.of("CPS", "CPV"), testTrade.getCounterpartyExternalId());
    assertEquals(secKey, testTrade.getSecurityLink().getExternalId());
  }

  @Test
  public void test_addTradeDeal_addThenGet() {
    final ManageablePosition position = new ManageablePosition(BigDecimal.TEN, ExternalId.of("A", "B"));

    final LocalDate tradeDate = _now.toLocalDate();
    final OffsetTime tradeTime = _now.toOffsetTime().minusSeconds(500);
    final ManageableTrade trade = new ManageableTrade(BigDecimal.TEN, ExternalId.of("A", "B"), tradeDate, tradeTime, ExternalId.of("CPS", "CPV"));
    trade.addAttribute("TA1", "C");
    trade.addAttribute("TA2", "D");
//    trade.setDeal(new MockDeal("propOne", "propTwo"));  // TODO: test deal persistence
    position.getTrades().add(trade);

    final PositionDocument doc = new PositionDocument();
    doc.setPosition(position);
    final PositionDocument added = _posMaster.add(doc);
    assertNotNull(added);
    assertNotNull(added.getUniqueId());

    final PositionDocument fromDb = _posMaster.get(added.getUniqueId());
    assertNotNull(fromDb);
    assertNotNull(fromDb.getUniqueId());

    assertEquals(added, fromDb);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_add_addWithMissingQuantityProperty() {
    final ManageablePosition position = new ManageablePosition();
    final PositionDocument doc = new PositionDocument(position);
    _posMaster.add(doc);
  }

  @Test
  public void test_add_addWithMinimalProperties() {
    final ManageablePosition position = new ManageablePosition();
    position.setQuantity(BigDecimal.ONE);
    final PositionDocument doc = new PositionDocument(position);
    _posMaster.add(doc);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_add_addTradeWithMissingTradeDateProperty() {
    final ManageablePosition position = new ManageablePosition();
    position.setQuantity(BigDecimal.ONE);
    final ManageableTrade trade = new ManageableTrade();
    trade.setCounterpartyExternalId(ExternalId.of("ABC", "DEF"));
    trade.setQuantity(BigDecimal.ONE);
    position.addTrade(trade);
    final PositionDocument doc = new PositionDocument(position);
    _posMaster.add(doc);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_add_addTradeWithMissingCounterpartyExternalIdProperty() {
    final ManageablePosition position = new ManageablePosition();
    position.setQuantity(BigDecimal.ONE);
    final ManageableTrade trade = new ManageableTrade();
    trade.setTradeDate(_now.toLocalDate());
    trade.setQuantity(BigDecimal.ONE);
    position.addTrade(trade);
    final PositionDocument doc = new PositionDocument(position);
    _posMaster.add(doc);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_add_addTradeWithMissingQuantityProperty() {
    final ManageablePosition position = new ManageablePosition();
    position.setQuantity(BigDecimal.ONE);
    final ManageableTrade trade = new ManageableTrade();
    trade.setTradeDate(_now.toLocalDate());
    trade.setCounterpartyExternalId(ExternalId.of("ABC", "DEF"));
    position.addTrade(trade);
    final PositionDocument doc = new PositionDocument(position);
    _posMaster.add(doc);
  }

  @Test
  public void test_add_addTradeWithMinimalProperties() {
    final ManageablePosition position = new ManageablePosition();
    position.setQuantity(BigDecimal.ONE);
    final ManageableTrade trade = new ManageableTrade();
    trade.setTradeDate(_now.toLocalDate());
    trade.setCounterpartyExternalId(ExternalId.of("ABC", "DEF"));
    trade.setQuantity(BigDecimal.ONE);
    position.addTrade(trade);
    final PositionDocument doc = new PositionDocument(position);
    _posMaster.add(doc);
  }

}
