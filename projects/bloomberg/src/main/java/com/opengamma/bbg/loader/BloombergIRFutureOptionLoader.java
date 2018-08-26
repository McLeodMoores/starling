/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.loader;

import java.util.Collection;
import java.util.Set;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.google.common.collect.Sets;
import com.opengamma.bbg.referencedata.ReferenceDataProvider;
import com.opengamma.bbg.util.BloombergDataUtils;
import com.opengamma.bbg.util.BloombergDomainIdentifierResolver;
import com.opengamma.financial.security.DefaultSecurityLoader;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.masterdb.security.DbSecurityMaster;
import com.opengamma.provider.security.SecurityProvider;
import com.opengamma.util.PlatformConfigUtils;

/**
 * Little util for loading options for Interest rate future
 */
public class BloombergIRFutureOptionLoader {

  /* package */static final String CONTEXT_CONFIGURATION_PATH = "/com/opengamma/bbg/loader/bloomberg-security-loader-context.xml";

  private static ConfigurableApplicationContext getApplicationContext() {
    final ConfigurableApplicationContext context = new ClassPathXmlApplicationContext(CONTEXT_CONFIGURATION_PATH);
    context.start();
    return context;
  }

  /**
   * Little util for loading options for Interest rate future.
   * @param args command line params
   */
  public static void main(final String[] args) {
    final ExternalId underlyingId = ExternalId.parse(args[0]);

    PlatformConfigUtils.configureSystemProperties();
    final ConfigurableApplicationContext appcontext = getApplicationContext();

    final ReferenceDataProvider bbgRefDataProvider = appcontext.getBean("sharedReferenceDataProvider", ReferenceDataProvider.class);
    final String bloombergKey = BloombergDomainIdentifierResolver.toBloombergKey(underlyingId);
    final SecurityProvider secProvider = appcontext.getBean("bloombergSecurityProvider", SecurityProvider.class);
    final DbSecurityMaster secMaster = appcontext.getBean("dbSecurityMaster", DbSecurityMaster.class);
    final DefaultSecurityLoader loader = new DefaultSecurityLoader(secMaster, secProvider);

    final Set<ExternalId> optionChain = BloombergDataUtils.getOptionChain(bbgRefDataProvider, bloombergKey);
    if (optionChain != null && !optionChain.isEmpty()) {
      loader.loadSecurities(toBundles(optionChain));
    }
  }

  private static Collection<ExternalIdBundle> toBundles(final Set<ExternalId> optionChain) {
    final Set<ExternalIdBundle> results = Sets.newHashSet();
    for (final ExternalId identifier : optionChain) {
      results.add(ExternalIdBundle.of(identifier));
    }
    return results;
  }

}
