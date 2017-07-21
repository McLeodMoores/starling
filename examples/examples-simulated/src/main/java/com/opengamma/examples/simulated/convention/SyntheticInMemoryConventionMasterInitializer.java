/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.examples.simulated.convention;

import com.opengamma.financial.convention.initializer.ConventionMasterInitializer;
import com.opengamma.financial.convention.initializer.USFXConventions;
import com.opengamma.master.convention.ConventionMaster;
import com.opengamma.master.convention.impl.InMemoryConventionMaster;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.master.security.impl.InMemorySecurityMaster;

/**
 * The default set of conventions for examples-simulated that have been hard-coded.
 */
public class SyntheticInMemoryConventionMasterInitializer extends ConventionMasterInitializer {

  /** Singleton. */
  public static final ConventionMasterInitializer INSTANCE = new SyntheticInMemoryConventionMasterInitializer();

  /**
   * Creates an {@code InMemoryConventionMaster} populated with default hard-coded conventions.
   *
   * @return the populated master, not null
   */
  public static InMemoryConventionMaster createPopulated() {
    final InMemoryConventionMaster conventionMaster = new InMemoryConventionMaster();
    final InMemorySecurityMaster securityMaster = new InMemorySecurityMaster();
    SyntheticInMemoryConventionMasterInitializer.INSTANCE.init(conventionMaster, securityMaster);
    return conventionMaster;
  }

  //-------------------------------------------------------------------------
  /**
   * Restricted constructor.
   */
  protected SyntheticInMemoryConventionMasterInitializer() {
  }

  //-------------------------------------------------------------------------
  @Override
  public void init(final ConventionMaster conventionMaster, final SecurityMaster securityMaster) {
    USFXConventions.INSTANCE.init(conventionMaster, securityMaster);
    ExampleUGConventions.INSTANCE.init(conventionMaster, securityMaster);
    ExampleUSConventions.INSTANCE.init(conventionMaster, securityMaster);
  }

  @Override
  public void init(final ConventionMaster conventionMaster) {
    USFXConventions.INSTANCE.init(conventionMaster);
    ExampleUGConventions.INSTANCE.init(conventionMaster);
    ExampleUSConventions.INSTANCE.init(conventionMaster);
  }
}
