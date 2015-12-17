/**
 *
 */
/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.loader;

import java.net.URI;
import java.util.Map;

import org.testng.annotations.Test;

import com.google.common.collect.Sets;
import com.opengamma.bbg.referencedata.ReferenceDataProvider;
import com.opengamma.bbg.referencedata.impl.RemoteReferenceDataProvider;
import com.opengamma.master.security.ManageableSecurity;

/**
 *
 */
@Test
public class BondLoaderTest {

  public void test() {
    final ReferenceDataProvider referenceDataProvider = new RemoteReferenceDataProvider(URI.create("http://marketdataserver-lx-1:8090/jax/components/ReferenceDataProvider/bloomberg"));
    final BondLoader bondLoader = new BondLoader(referenceDataProvider);
    final Map<String, ManageableSecurity> loadSecurities = bondLoader.loadSecurities(Sets.newHashSet("/ticker/NGGLN 2.983 07/08/18 Corp"));
    System.err.println(loadSecurities.get("/ticker/NGGLN 2.983 07/08/18 Corp").getAttributes());
  }
}
