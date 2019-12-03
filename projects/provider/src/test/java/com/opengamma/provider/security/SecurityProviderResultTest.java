/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.provider.security;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.joda.beans.Bean;
import org.testng.annotations.Test;

import com.opengamma.core.security.Security;
import com.opengamma.core.security.impl.SimpleSecurity;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.provider.AbstractBeanTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link SecurityProviderResult}.
 */
@Test(groups = TestGroup.UNIT)
public class SecurityProviderResultTest extends AbstractBeanTestCase {
  private static final Map<ExternalIdBundle, Security> RESULT_MAP = new HashMap<>();
  static {
    RESULT_MAP.put(ExternalIdBundle.of("eid", "1"), new SimpleSecurity("name1"));
    RESULT_MAP.put(ExternalIdBundle.of("eid", "2"), new SimpleSecurity("name2"));
    RESULT_MAP.put(ExternalIdBundle.of("eid", "3"), new SimpleSecurity("name3"));
  }

  @Override
  public JodaBeanProperties<? extends Bean> getJodaBeanProperties() {
    return new JodaBeanProperties<>(SecurityProviderResult.class, Arrays.asList("resultMap"), Arrays.asList(RESULT_MAP),
        Arrays.asList(Collections.singletonMap(ExternalIdBundle.of("eid", "1"), new SimpleSecurity("name1"))));
  }

}
