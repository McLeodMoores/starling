/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.web.exchange;

import static org.testng.Assert.assertEquals;

import java.util.Arrays;

import org.joda.beans.Bean;
import org.testng.annotations.Test;
import org.threeten.bp.Instant;

import com.opengamma.id.UniqueId;
import com.opengamma.master.exchange.ExchangeDocument;
import com.opengamma.master.exchange.ManageableExchange;
import com.opengamma.util.test.TestGroup;
import com.opengamma.web.AbstractBeanTestCase;

/**
 * Tests for {@link WebExchangeData}.
 */
@Test(groups = TestGroup.UNIT)
public class WebExchangeDataTest extends AbstractBeanTestCase {
  private static final String NAME = "exchange";
  private static final String EXCHANGE_URI = "exch";
  private static final String VERSION_URI = "version=1";
  private static final ManageableExchange EXCHANGE = new ManageableExchange();
  private static final ExchangeDocument DOCUMENT = new ExchangeDocument();
  private static final ExchangeDocument VERSIONED = new ExchangeDocument();
  static {
    EXCHANGE.setName(NAME);
    DOCUMENT.setExchange(EXCHANGE);
    VERSIONED.setExchange(EXCHANGE);
    VERSIONED.setVersionFromInstant(Instant.now());
  }
  private static final WebExchangeData DATA = new WebExchangeData();
  static {
    DOCUMENT.setUniqueId(UniqueId.of("hol", "0"));
    DATA.setExchange(DOCUMENT);
    DATA.setUriExchangeId(EXCHANGE_URI);
    DATA.setUriVersionId(VERSION_URI);
  }

  @Override
  public JodaBeanProperties<? extends Bean> getJodaBeanProperties() {
    return new JodaBeanProperties<>(WebExchangeData.class, Arrays.asList("uriExchangeId", "uriVersionId", "exchange", "versioned"),
        Arrays.asList(EXCHANGE_URI, VERSION_URI, DOCUMENT, VERSIONED), Arrays.asList(VERSION_URI, EXCHANGE_URI, VERSIONED, DOCUMENT));
  }

  /**
   * Tests getting the best exchange if the override id is not null.
   */
  public void testBestExchangeOverrideId() {
    final UniqueId uid = UniqueId.of("exch", "1");
    assertEquals(DATA.getBestExchangeUriId(uid), uid.toString());
  }

  /**
   * Tests getting the best exchange if there is no exchange document.
   */
  public void testBestExchangeNoExchangeDocument() {
    final WebExchangeData data = DATA.clone();
    data.setExchange(null);
    assertEquals(data.getBestExchangeUriId(null), EXCHANGE_URI);
  }

  /**
   * Tests getting the best exchange from the document.
   */
  public void testBestExchangeFromDocument() {
    assertEquals(DATA.getBestExchangeUriId(null), DOCUMENT.getUniqueId().toString());
  }

}
