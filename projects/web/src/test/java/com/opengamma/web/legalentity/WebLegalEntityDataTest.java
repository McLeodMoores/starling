/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.web.legalentity;

import static org.testng.Assert.assertEquals;

import java.util.Arrays;

import org.joda.beans.Bean;
import org.testng.annotations.Test;

import com.opengamma.core.legalentity.impl.SimpleLegalEntity;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.master.legalentity.LegalEntityDocument;
import com.opengamma.master.legalentity.ManageableLegalEntity;
import com.opengamma.util.test.TestGroup;
import com.opengamma.web.AbstractBeanTestCase;

/**
 * Tests for {@link WebLegalEntityData}.
 */
@Test(groups = TestGroup.UNIT)
public class WebLegalEntityDataTest extends AbstractBeanTestCase {
  private static final String NAME = "len";
  private static final ExternalIdBundle IDS = ExternalIdBundle.of("eid", "20");
  private static final String LEGAL_ENTITY_URI = "legal entity/AU";
  private static final String VERSION_URI = "version=1";
  private static final ManageableLegalEntity LEGAL_ENTITY = new ManageableLegalEntity(NAME, IDS);
  private static final LegalEntityDocument DOCUMENT = new LegalEntityDocument();
  static {
    DOCUMENT.setLegalEntity(LEGAL_ENTITY);
  }
  private static final WebLegalEntityData DATA = new WebLegalEntityData();
  static {
    DOCUMENT.setUniqueId(UniqueId.of("reg", "0"));
    DATA.setLegalEntity(DOCUMENT);
    DATA.setUriLegalEntityId(LEGAL_ENTITY_URI);
    DATA.setUriVersionId(VERSION_URI);
  }

  @Override
  public JodaBeanProperties<? extends Bean> getJodaBeanProperties() {
    return new JodaBeanProperties<>(WebLegalEntityData.class,
        Arrays.asList("type", "uriLegalEntityId", "uriVersionId", "legalEntity", "versioned"),
        Arrays.asList(ManageableLegalEntity.class, LEGAL_ENTITY_URI, VERSION_URI, DOCUMENT, DOCUMENT),
        Arrays.asList(SimpleLegalEntity.class, VERSION_URI, LEGAL_ENTITY_URI, new LegalEntityDocument(new ManageableLegalEntity("name", IDS)),
            new LegalEntityDocument(new ManageableLegalEntity("name", IDS))));
  }

  /**
   * Tests getting the best legal entity if the override id is not null.
   */
  public void testBestLegalEntityOverrideId() {
    final UniqueId uid = UniqueId.of("reg", "1");
    assertEquals(DATA.getBestLegalEntityUriId(uid), uid.toString());
  }

  /**
   * Tests getting the best legal entity if there is no legal entity document.
   */
  public void testBestLegalEntityNoLegalEntityDocument() {
    final WebLegalEntityData data = DATA.clone();
    data.setLegalEntity(null);
    assertEquals(data.getBestLegalEntityUriId(null), LEGAL_ENTITY_URI);
  }

  /**
   * Tests getting the best legal entity from the document.
   */
  public void testBestLegalEntityFromDocument() {
    assertEquals(DATA.getBestLegalEntityUriId(null), DOCUMENT.getUniqueId().toString());
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
