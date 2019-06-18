/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.master.security;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.Test;

import com.opengamma.core.security.Security;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link SecurityLoaderResult}.
 */
@Test(groups = TestGroup.UNIT)
public class SecurityLoaderResultTest extends AbstractFudgeBuilderTestCase {
  private static final ExternalId EID_1 = ExternalId.of("eid1", "value1");
  private static final ExternalId EID_2 = ExternalId.of("eid2", "value2");
  private static final ExternalId EID_3 = ExternalId.of("eid3", "value3");
  private static final ExternalId EID_4 = ExternalId.of("eid4", "value4");
  private static final ObjectId OID_1 = ObjectId.of("oid1", "v1");
  private static final ObjectId OID_2 = ObjectId.of("oid2", "v2");
  private static final UniqueId UID_1 = UniqueId.of(OID_1, "v1");
  private static final UniqueId UID_2 = UniqueId.of(OID_2, "v2");
  private static final ManageableSecurity SEC_1 = new ManageableSecurity();
  private static final ManageableSecurity SEC_2 = new ManageableSecurity();
  private static final Map<ExternalIdBundle, Security> RETRIEVED = new HashMap<>();
  private static final Map<UniqueId, Security> RESULT_SEC = new HashMap<>();
  private static final Map<ExternalIdBundle, ObjectId> RESULT_OIDS = new HashMap<>();
  private static final Map<ExternalIdBundle, UniqueId> RESULT_UIDS = new HashMap<>();
  static {
    final ExternalIdBundle eids1 = ExternalIdBundle.of(EID_1, EID_2);
    final ExternalIdBundle eids2 = ExternalIdBundle.of(EID_3);
    final ExternalIdBundle eids3 = ExternalIdBundle.of(EID_4);
    SEC_1.setUniqueId(UID_1);
    SEC_1.setExternalIdBundle(eids1);
    SEC_2.setUniqueId(UID_2);
    SEC_2.setExternalIdBundle(eids2);
    RETRIEVED.put(eids1, SEC_1);
    RETRIEVED.put(eids2, SEC_2);
    RETRIEVED.put(eids3, null);
    RESULT_SEC.put(UID_1, SEC_1);
    RESULT_SEC.put(UID_2, SEC_2);
    RESULT_UIDS.put(eids1, UID_1);
    RESULT_UIDS.put(eids2, UID_2);
    RESULT_UIDS.put(eids3, null);
    RESULT_OIDS.put(eids1, OID_1);
    RESULT_OIDS.put(eids2, OID_2);
    RESULT_OIDS.put(eids3, null);
  }

  /**
   * Tests that the results cannot be null.
   */
  @Test(expectedExceptions = NullPointerException.class)
  public void testNullResult() {
    new SecurityLoaderResult(null, false);
  }

  /**
   * Tests the object.
   */
  @Test
  public void testObject() {
    // full results
    SecurityLoaderResult result = new SecurityLoaderResult(RETRIEVED, true);
    assertEquals(result.getResultMap(), RESULT_UIDS);
    assertEquals(result.getResultMapAsObjectId(), RESULT_OIDS);
    assertEquals(result.getSecurityMap(), RESULT_SEC);
    SecurityLoaderResult other = new SecurityLoaderResult(RETRIEVED, true);
    assertEquals(result, other);
    assertEquals(result.hashCode(), other.hashCode());
    assertNotEquals(null, result);
    assertEquals(result.toString(), "SecurityLoaderResult{resultMap={Bundle[eid1~value1, eid2~value2]=oid1~v1~v1, "
        + "Bundle[eid3~value3]=oid2~v2~v2, Bundle[eid4~value4]=null}, "
        + "securityMap={oid1~v1~v1=ManageableSecurity{uniqueId=oid1~v1~v1, externalIdBundle=Bundle[eid1~value1, eid2~value2], name=, "
        + "securityType=MANAGEABLE, attributes={}, requiredPermissions=[]}, oid2~v2~v2=ManageableSecurity{uniqueId=oid2~v2~v2, "
        + "externalIdBundle=Bundle[eid3~value3], name=, securityType=MANAGEABLE, attributes={}, requiredPermissions=[]}}}");
    // no security map
    result = new SecurityLoaderResult(RETRIEVED, false);
    assertEquals(result.getResultMap(), RESULT_UIDS);
    assertEquals(result.getResultMapAsObjectId(), RESULT_OIDS);
    assertEquals(result.getSecurityMap(), Collections.emptyMap());
    other = new SecurityLoaderResult(RETRIEVED, false);
    assertEquals(result, other);
    assertEquals(result.hashCode(), other.hashCode());
    assertNotEquals(null, result);
    assertEquals(result.toString(), "SecurityLoaderResult{resultMap={Bundle[eid1~value1, eid2~value2]=oid1~v1~v1, "
        + "Bundle[eid3~value3]=oid2~v2~v2, Bundle[eid4~value4]=null}, securityMap={}}");
    other = new SecurityLoaderResult(Collections.<ExternalIdBundle, Security>emptyMap(), false);
    assertNotEquals(result, other);
    other = new SecurityLoaderResult(RETRIEVED, true);
    assertNotEquals(result, other);
  }

  /**
   * Tests the bean.
   */
  @Test
  public void testBean() {
    SecurityLoaderResult result = new SecurityLoaderResult(RETRIEVED, true);
    assertEquals(result.metaBean().resultMap().get(result), RESULT_UIDS);
    assertEquals(result.metaBean().securityMap().get(result), RESULT_SEC);
    assertEquals(result.property("resultMap").get(), RESULT_UIDS);
    assertEquals(result.property("securityMap").get(), RESULT_SEC);
    result = new SecurityLoaderResult(RETRIEVED, false);
    assertEquals(result.metaBean().resultMap().get(result), RESULT_UIDS);
    assertEquals(result.metaBean().securityMap().get(result), Collections.emptyMap());
    assertEquals(result.property("resultMap").get(), RESULT_UIDS);
    assertEquals(result.property("securityMap").get(), Collections.emptyMap());
  }

  /**
   * Tests a cycle.
   */
  @Test
  public void testCycle() {
    SecurityLoaderResult result = new SecurityLoaderResult(RETRIEVED, true);
    assertEncodeDecodeCycle(SecurityLoaderResult.class, result);
    result = new SecurityLoaderResult(RETRIEVED, false);
    assertEncodeDecodeCycle(SecurityLoaderResult.class, result);
  }
}
