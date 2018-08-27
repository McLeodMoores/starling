/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.link;

import static org.mockito.Mockito.mock;

import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.opengamma.core.config.ConfigSource;
import com.opengamma.id.VersionCorrection;
import com.opengamma.service.ServiceContext;
import com.opengamma.service.ThreadLocalServiceContext;
import com.opengamma.service.VersionCorrectionProvider;
import com.opengamma.util.test.TestGroup;

@Test(groups = TestGroup.UNIT)
public class SourceLinkResolverTest {

  @BeforeMethod
  public void setup() {
    // Ensure we don't have a thread local service context which could be used accidentally
    ThreadLocalServiceContext.init(null);
  }

  @Test(expectedExceptions = IllegalStateException.class)
  public void noThreadLocalContextGivesError() {

    final SourceLinkResolver<String, Object, ConfigSource> resolver = createSourceLinkResolver();
    resolver.resolve(createIdentifier("id"));
  }

  private LinkIdentifier<String, Object> createIdentifier(final String id) {
    return LinkIdentifier.of(id, Object.class);
  }

  public void threadLocalContextGetsUsed() {

    final ServiceContext serviceContext = createContext(ConfigSource.class, VersionCorrectionProvider.class);

    ThreadLocalServiceContext.init(serviceContext);
    final SourceLinkResolver<String, Object, ConfigSource> resolver = createSourceLinkResolver();

    resolver.resolve(createIdentifier("id"));
  }

  private ServiceContext createContext(final Class<?>... services) {

    final Map<Class<?>, Object> serviceMap = new HashMap<>();
    for (final Class<?> aClass : services) {
      serviceMap.put(aClass, mock(aClass));
    }
    return ServiceContext.of(serviceMap);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void noVersionCorrectionGivesError() {

    final ServiceContext serviceContext = createContext(ConfigSource.class);
    final SourceLinkResolver<String, Object, ConfigSource> resolver = createSourceLinkResolver(serviceContext);

    resolver.resolve(createIdentifier("id"));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void noSourceGivesError() {

    final ServiceContext serviceContext = createContext(VersionCorrectionProvider.class);
    final SourceLinkResolver<String, Object, ConfigSource> resolver = createSourceLinkResolver(serviceContext);

    resolver.resolve(createIdentifier("id"));
  }

  private SourceLinkResolver<String, Object, ConfigSource> createSourceLinkResolver() {
    return new SourceLinkResolver<String, Object, ConfigSource>() {
        @Override
        protected Class<ConfigSource> getSourceClass() {
          return ConfigSource.class;
        }

        @Override
        protected VersionCorrection getVersionCorrection(final VersionCorrectionProvider vcProvider) {
          return vcProvider.getConfigVersionCorrection();
        }

        @Override
        protected Object executeQuery(final ConfigSource source, final Class<Object> type, final String identifier, final VersionCorrection versionCorrection) {
          return source.getLatestByName(Object.class, identifier);
        }
    };
  }

  private SourceLinkResolver<String, Object, ConfigSource> createSourceLinkResolver(final ServiceContext serviceContext) {
    return new SourceLinkResolver<String, Object, ConfigSource>(serviceContext) {
        @Override
        protected Class<ConfigSource> getSourceClass() {
          return ConfigSource.class;
        }

        @Override
        protected VersionCorrection getVersionCorrection(final VersionCorrectionProvider vcProvider) {
          return vcProvider.getConfigVersionCorrection();
        }

        @Override
        protected Object executeQuery(final ConfigSource source, final Class<Object> type, final String identifier, final VersionCorrection versionCorrection) {
          return source.getLatestByName(Object.class, identifier);
        }
    };
  }

}
