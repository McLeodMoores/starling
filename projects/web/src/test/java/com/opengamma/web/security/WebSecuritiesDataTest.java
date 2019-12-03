/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.web.security;

import static org.testng.Assert.assertEquals;

import java.util.Arrays;
import java.util.SortedMap;
import java.util.TreeMap;

import org.joda.beans.Bean;
import org.testng.annotations.Test;
import org.threeten.bp.Instant;

import com.opengamma.id.UniqueId;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.util.test.TestGroup;
import com.opengamma.web.AbstractBeanTestCase;

/**
 * Tests for {@link WebSecuritiesData}.
 */
@Test(groups = TestGroup.UNIT)
public class WebSecuritiesDataTest extends AbstractBeanTestCase {
  private static final String NAME = "security";
  private static final String SECURITY_URI = "sec";
  private static final String VERSION_URI = "version=1";
  private static final ManageableSecurity SECURITY = new ManageableSecurity(NAME);
  private static final SecurityDocument DOCUMENT = new SecurityDocument();
  private static final SecurityDocument VERSIONED = new SecurityDocument();
  private static final SortedMap<String, String> SECURITY_TYPES = new TreeMap<>();
  static {
    DOCUMENT.setSecurity(SECURITY);
    VERSIONED.setSecurity(SECURITY);
    VERSIONED.setVersionFromInstant(Instant.now());
    SECURITY_TYPES.put("equity", "EquitySecurity");
  }
  private static final WebSecuritiesData DATA = new WebSecuritiesData();
  static {
    DOCUMENT.setUniqueId(UniqueId.of("sec", "0"));
    DATA.setSecurity(DOCUMENT);
    DATA.setUriSecurityId(SECURITY_URI);
    DATA.setUriVersionId(VERSION_URI);
    DATA.setSecurityTypes(SECURITY_TYPES);
  }

  @Override
  public JodaBeanProperties<? extends Bean> getJodaBeanProperties() {
    return new JodaBeanProperties<>(WebSecuritiesData.class, Arrays.asList("uriSecurityId", "uriVersionId", "security", "versioned", "securityTypes"),
        Arrays.asList(SECURITY_URI, VERSION_URI, DOCUMENT, VERSIONED, SECURITY_TYPES),
        Arrays.asList(VERSION_URI, SECURITY_URI, VERSIONED, DOCUMENT, new TreeMap<>()));
  }

  /**
   * Tests getting the best security if the override id is not null.
   */
  public void testBestSecurityOverrideId() {
    final UniqueId uid = UniqueId.of("sec", "1");
    assertEquals(DATA.getBestSecurityUriId(uid), uid.toString());
  }

  /**
   * Tests getting the best security if there is no security document.
   */
  public void testBestSecurityNoSecurityDocument() {
    final WebSecuritiesData data = DATA.clone();
    data.setSecurity(null);
    assertEquals(data.getBestSecurityUriId(null), SECURITY_URI);
  }

  /**
   * Tests getting the best security from the document.
   */
  public void testBestSecurityFromDocument() {
    assertEquals(DATA.getBestSecurityUriId(null), DOCUMENT.getUniqueId().toString());
  }

  /**
   * Fudge does not deserialize the type map correctly.
   */
  @Override
  @Test(dataProvider = "propertyValues")
  protected <TYPE extends Bean> void testCycle(final JodaBeanProperties<TYPE> properties) {
    final TYPE data = constructAndPopulateBeanBuilder(properties).build();
    assertEquals(data, cycleObjectJodaXml(properties.getType(), data));
  }

}
