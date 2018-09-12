/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.core.link;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.fail;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import org.mockito.Mockito;
import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.DateSet;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.service.ServiceContext;
import com.opengamma.service.ThreadLocalServiceContext;
import com.opengamma.service.VersionCorrectionProvider;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link ResolvableConfigLink}.
 */
@Test(groups = TestGroup.UNIT)
public class ResolvableConfigLinkTest extends AbstractFudgeBuilderTestCase {
  private static final ConfigItem<DateSet> CONFIG_1 = ConfigItem.of(DateSet.of(Collections.singleton(LocalDate.of(2018, 1, 1))));
  private static final ConfigItem<DateSet> CONFIG_2 = ConfigItem.of(DateSet.of(Collections.singleton(LocalDate.of(2019, 1, 1))));
  private static final ConfigSource CONFIG_SOURCE = Mockito.mock(ConfigSource.class);
  private static final ServiceContext CONTEXT;
  static {
    CONFIG_1.setUniqueId(UniqueId.of("uid", "1"));
    CONFIG_2.setUniqueId(UniqueId.of("uid", "2"));
    Mockito.when(CONFIG_SOURCE.get(DateSet.class, "DS 1", (VersionCorrection) null)).thenReturn(Collections.singleton(CONFIG_1));
    Mockito.when(CONFIG_SOURCE.get(DateSet.class, "DS 2", (VersionCorrection) null)).thenReturn(Collections.singleton(CONFIG_2));
    final Map<Class<?>, Object> services = new HashMap<>();
    services.put(VersionCorrectionProvider.class, Mockito.mock(VersionCorrectionProvider.class));
    services.put(ConfigSource.class, CONFIG_SOURCE);
    CONTEXT = ServiceContext.of(services);
  }
  private static final LinkResolver<String, DateSet> LINK_RESOLVER = new ServiceContextConfigLinkResolver<>(CONTEXT);
  private static final ResolvableConfigLink<DateSet> RESOLVER = new ResolvableConfigLink<>("DS 1", DateSet.class, LINK_RESOLVER);

  /**
   * Tests that the identifiers cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullIdentifiers() {
    new ResolvableConfigLink<>(null, DateSet.class, LINK_RESOLVER);
  }

  /**
   * Tests that the type cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullType() {
    new ResolvableConfigLink<>("DS 1", null, LINK_RESOLVER);
  }

  /**
   * Tests that the link resolver cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullResolver() {
    new ResolvableConfigLink<>("DS 1", DateSet.class, null);
  }

  /**
   * Tests the type of the link identifier.
   */
  @Test
  public void testGetTargetType() {
    assertEquals(RESOLVER.getTargetType(), DateSet.class);
  }

  /**
   * Tests the resolution of the config.
   */
  @Test
  public void testResolve() {
    ThreadLocalServiceContext.init(CONTEXT);
    assertEquals(RESOLVER.resolve(), CONFIG_1.getValue());
    assertEquals(ConfigLink.resolvable("DS 1", DateSet.class).resolve(), CONFIG_1.getValue());
    assertEquals(ConfigLink.resolvable("DS 2", DateSet.class).resolve(), CONFIG_2.getValue());
    assertEquals(ConfigLink.resolvable("DS 1", DateSet.class, CONTEXT).resolve(), CONFIG_1.getValue());
    assertEquals(ConfigLink.resolvable("DS 2", DateSet.class, CONTEXT).resolve(), CONFIG_2.getValue());
  }

  /**
   * Tests the config link that is returned.
   */
  @Test
  public void testConfigLink() {
    final ConfigLink<DateSet> resolved = ConfigLink.resolved(CONFIG_1.getValue());
    assertEquals(resolved.getTargetType(), DateSet.class);
  }

  /**
   * Tests the behaviour when the ids do not resolve to a config.
   */
  @Test(expectedExceptions = DataNotFoundException.class)
  public void testIdsCannotResolve() {
    final ResolvableConfigLink<DateSet> resolver = new ResolvableConfigLink<>("DS 3", DateSet.class, LINK_RESOLVER);
    resolver.resolve();
  }

  /**
   * Tests hashCode and equals.
   */
  @Test
  public void testHashCodeEquals() {
    assertEquals(RESOLVER, RESOLVER);
    assertNotEquals(null, RESOLVER);
    assertNotEquals(LINK_RESOLVER, RESOLVER);
    ResolvableConfigLink<DateSet> other = new ResolvableConfigLink<>("DS 1", DateSet.class, LINK_RESOLVER);
    assertEquals(other, RESOLVER);
    assertEquals(other.hashCode(), RESOLVER.hashCode());
    other = new ResolvableConfigLink<>("DS 2", DateSet.class, LINK_RESOLVER);
    assertNotEquals(other, RESOLVER);
    final LinkResolver<String, String> link = new ServiceContextConfigLinkResolver<>(CONTEXT);
    assertNotEquals(new ResolvableConfigLink<>("DS 1", String.class, link), RESOLVER);
  }

  /**
   * Tests an encoding / decoding cycle.
   */
  @Test
  public void testCycle() {
    assertEquals(cycleObjectJodaXml(ResolvableConfigLink.class, RESOLVER), RESOLVER);
  }

  /**
   * Tests the bean.
   */
  @Test
  public void testBean() {
    assertNotNull(RESOLVER.metaBean());
    assertNotNull(RESOLVER.metaBean().identifier());
    assertEquals(RESOLVER.metaBean().identifier().get(RESOLVER), LinkIdentifier.of("DS 1", DateSet.class));
    assertEquals(RESOLVER.property("identifier").get(), LinkIdentifier.of("DS 1", DateSet.class));
    try {
      assertNull(RESOLVER.property("resolver").get());
      fail();
    } catch (final NoSuchElementException e) {
      // expected
    }
  }

}
