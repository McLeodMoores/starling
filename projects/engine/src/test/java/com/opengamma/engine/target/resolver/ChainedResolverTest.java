/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.target.resolver;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;

import org.mockito.Mockito;
import org.testng.annotations.Test;

import com.opengamma.engine.target.logger.ResolutionLogger;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

/**
 * Test the {@link ChainedResolver} class.
 */
@Test(groups = TestGroup.UNIT)
public class ChainedResolverTest {

  public void testFirst() {
    final ObjectResolver<Currency> first = Mockito.mock(ObjectResolver.class);
    final ObjectResolver<Currency> second = Mockito.mock(ObjectResolver.class);
    final ObjectResolver<?> chained = ChainedResolver.CREATE.execute(second, first);
    Mockito.when(first.resolveObject(UniqueId.of("Foo", "1"), VersionCorrection.LATEST)).thenReturn(Currency.USD);
    Mockito.when(second.resolveObject(UniqueId.of("Foo", "1"), VersionCorrection.LATEST)).thenReturn(Currency.GBP);
    assertEquals(chained.resolveObject(UniqueId.of("Foo", "1"), VersionCorrection.LATEST), Currency.USD);
    Mockito.verify(second, Mockito.only()).deepResolver();
  }

  public void testSecond() {
    final ObjectResolver<Currency> first = Mockito.mock(ObjectResolver.class);
    final ObjectResolver<Currency> second = Mockito.mock(ObjectResolver.class);
    final ObjectResolver<?> chained = ChainedResolver.CREATE.execute(second, first);
    Mockito.when(first.resolveObject(UniqueId.of("Foo", "1"), VersionCorrection.LATEST)).thenReturn(null);
    Mockito.when(second.resolveObject(UniqueId.of("Foo", "1"), VersionCorrection.LATEST)).thenReturn(Currency.GBP);
    assertEquals(chained.resolveObject(UniqueId.of("Foo", "1"), VersionCorrection.LATEST), Currency.GBP);
  }

  public void testDeepResolverFirst() {
    final ObjectResolver<?> first = Mockito.mock(ObjectResolver.class);
    final DeepResolver deepFirst = Mockito.mock(DeepResolver.class);
    final ObjectResolver<?> second = Mockito.mock(ObjectResolver.class);
    Mockito.when(first.deepResolver()).thenReturn(deepFirst);
    Mockito.when(second.deepResolver()).thenReturn(null);
    final ObjectResolver<?> chained = ChainedResolver.CREATE.execute(second, first);
    assertSame(chained.deepResolver(), deepFirst);
  }

  public void testDeepResolverSecond() {
    final ObjectResolver<?> first = Mockito.mock(ObjectResolver.class);
    final ObjectResolver<?> second = Mockito.mock(ObjectResolver.class);
    final DeepResolver deepSecond = Mockito.mock(DeepResolver.class);
    Mockito.when(first.deepResolver()).thenReturn(null);
    Mockito.when(second.deepResolver()).thenReturn(deepSecond);
    final ObjectResolver<?> chained = ChainedResolver.CREATE.execute(second, first);
    assertSame(chained.deepResolver(), deepSecond);
  }

  public void testDeepResolverNone() {
    final ObjectResolver<?> first = Mockito.mock(ObjectResolver.class);
    final ObjectResolver<?> second = Mockito.mock(ObjectResolver.class);
    Mockito.when(first.deepResolver()).thenReturn(null);
    Mockito.when(second.deepResolver()).thenReturn(null);
    final ObjectResolver<?> chained = ChainedResolver.CREATE.execute(second, first);
    assertNull(chained.deepResolver());
  }

  public void testDeepResolverChained() {
    final ObjectResolver<?> first = Mockito.mock(ObjectResolver.class);
    final DeepResolver deepFirst = Mockito.mock(DeepResolver.class);
    final ObjectResolver<?> second = Mockito.mock(ObjectResolver.class);
    final DeepResolver deepSecond = Mockito.mock(DeepResolver.class);
    Mockito.when(first.deepResolver()).thenReturn(deepFirst);
    Mockito.when(second.deepResolver()).thenReturn(deepSecond);
    final ObjectResolver<?> chained = ChainedResolver.CREATE.execute(second, first);
    final DeepResolver deep = chained.deepResolver();
    final ResolutionLogger logger = Mockito.mock(ResolutionLogger.class);
    assertNotNull(deep);
    final UniqueIdentifiable o1 = Mockito.mock(UniqueIdentifiable.class);
    final UniqueIdentifiable o2 = Mockito.mock(UniqueIdentifiable.class);
    Mockito.when(deepFirst.withLogger(o1, logger)).thenReturn(o1);
    Mockito.when(deepFirst.withLogger(o2, logger)).thenReturn(null);
    Mockito.when(deepSecond.withLogger(o2, logger)).thenReturn(o2);
    assertSame(deep.withLogger(o1, logger), o1);
    assertSame(deep.withLogger(o2, logger), o2);
    Mockito.verify(deepFirst).withLogger(o1, logger);
    Mockito.verify(deepFirst).withLogger(o2, logger);
    Mockito.verifyNoMoreInteractions(deepFirst);
    Mockito.verify(deepSecond).withLogger(o2, logger);
    Mockito.verifyNoMoreInteractions(deepSecond);
  }

}
