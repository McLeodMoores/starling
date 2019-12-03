/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.web.json.convention;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.Test;

import com.opengamma.financial.convention.StubType;
import com.opengamma.financial.convention.VanillaIborLegConvention;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.master.convention.impl.InMemoryConventionMaster;
import com.opengamma.util.time.Tenor;

/**
 * Unit tests for {@link VanillaIborLegConventionJsonBuilder}.
 */
public class VanillaIborLegConventionJsonBuilderTest {
  private static final VanillaIborLegConventionJsonBuilder BUILDER = new VanillaIborLegConventionJsonBuilder(new InMemoryConventionMaster());

  /**
   * Tests a round trip.
   */
  @Test
  public void test() {
    final Map<String, String> attributes = new HashMap<>();
    attributes.put("ATTR1", "VAL1");
    attributes.put("ATTR2", "VAL2");
    final VanillaIborLegConvention convention = new VanillaIborLegConvention("NAME", ExternalIdBundle.of("eid", "1"), ExternalId.of("convention", "ibor"), true,
        "NONE", Tenor.SIX_MONTHS, 2, true, StubType.BOTH, true, 1);
    convention.setAttributes(attributes);
    assertEquals(convention, BUILDER.fromJSON(BUILDER.toJSON(convention)));
    // template convention
    final String conventionJson = BUILDER.getTemplate();
    assertEquals(conventionJson, BUILDER.toJSON(BUILDER.fromJSON(conventionJson)));
  }

  /**
   * Tests the copy.
   */
  @Test
  public void testCopy() {
    final Map<String, String> attributes = new HashMap<>();
    attributes.put("ATTR1", "VAL1");
    attributes.put("ATTR2", "VAL2");
    final VanillaIborLegConvention convention = new VanillaIborLegConvention("NAME", ExternalIdBundle.of("eid", "1"), ExternalId.of("convention", "ibor"), true,
        "NONE", Tenor.SIX_MONTHS, 2, true, StubType.BOTH, true, 1);
    final VanillaIborLegConvention copy = BUILDER.getCopy(convention);
    copy.addAttribute("ATTR3", "VAL3");
    assertNotEquals(convention, copy);
    assertEquals(convention, new VanillaIborLegConvention("NAME", ExternalIdBundle.of("eid", "1"), ExternalId.of("convention", "ibor"), true, "NONE",
        Tenor.SIX_MONTHS, 2, true, StubType.BOTH, true, 1));
  }
}
