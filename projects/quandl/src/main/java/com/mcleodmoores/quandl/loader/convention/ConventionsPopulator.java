/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.quandl.loader.convention;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Iterables;
import com.mcleodmoores.quandl.QuandlConstants;
import com.mcleodmoores.quandl.convention.QuandlStirFutureConvention;
import com.opengamma.financial.convention.IborIndexConvention;
import com.opengamma.financial.convention.OvernightIndexConvention;
import com.opengamma.financial.convention.VanillaIborLegConvention;
import com.opengamma.financial.convention.initializer.ConventionMasterInitializer;
import com.opengamma.financial.security.index.IborIndex;
import com.opengamma.financial.security.index.OvernightIndex;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.master.convention.ConventionMaster;
import com.opengamma.master.convention.ManageableConvention;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.time.Tenor;

/**
 * Adds conventions to a {@link ConventionMaster}.
 */
public class ConventionsPopulator extends ConventionMasterInitializer {
  /** The logger */
  private static final Logger LOGGER = LoggerFactory.getLogger(ConventionsPopulator.class);
  /** The conventions to be added to the master */
  private final Set<ManageableConvention> _conventions;

  /**
   * Creates an instance.
   * @param conventions  the conventions, not null
   */
  public ConventionsPopulator(final Set<ManageableConvention> conventions) {
    ArgumentChecker.notNull(conventions, "conventions");
    _conventions = conventions;
  }

  @Override
  public void init(final ConventionMaster master, final SecurityMaster securityMaster) {
    ArgumentChecker.notNull(master, "master");
    final Set<VanillaIborLegConvention> iborLegConventions = new HashSet<>();
    final Set<QuandlStirFutureConvention> stirFutureConventions = new HashSet<>();
    final Set<IborIndexConvention> iborIndexConventions = new HashSet<>();
    for (final ManageableConvention convention : _conventions) {
      addConvention(master, convention);
      if (convention instanceof VanillaIborLegConvention) {
        iborLegConventions.add((VanillaIborLegConvention) convention);
      } else if (convention instanceof QuandlStirFutureConvention) {
        stirFutureConventions.add((QuandlStirFutureConvention) convention);
      } else if (convention instanceof IborIndexConvention) {
        iborIndexConventions.add((IborIndexConvention) convention);
      } else if (convention instanceof OvernightIndexConvention) {
        final OvernightIndexConvention indexConvention = (OvernightIndexConvention) convention;
        final Set<ExternalId> quandlId = indexConvention.getExternalIdBundle().getExternalIds(QuandlConstants.QUANDL_CODE);
        if (quandlId == null) {
          LOGGER.error("Could not get Quandl code id from {}: not adding overnight index to security master", indexConvention.getExternalIdBundle());
          continue;
        }
        if (quandlId.size() != 1) {
          LOGGER.error("Could not get unique Quandl code from {}: not adding overnight index to security master", indexConvention.getExternalIdBundle());
          return;
        }
        final OvernightIndex index = new OvernightIndex(indexConvention.getName(), indexConvention.getName(), Iterables.getOnlyElement(quandlId),
            ExternalIdBundle.of(quandlId));
        addSecurity(securityMaster, index);
      }
    }
    for (final VanillaIborLegConvention iborLegConvention : iborLegConventions) {
      for (final IborIndexConvention indexConvention : iborIndexConventions) {
        final ExternalIdBundle idBundle = indexConvention.getExternalIdBundle();
        if (idBundle.contains(iborLegConvention.getIborIndexConvention())) {
          createAndAddIborIndex(securityMaster, idBundle, indexConvention.getName(), iborLegConvention.getResetTenor());
        }
      }
    }
    for (final QuandlStirFutureConvention stirFutureConvention : stirFutureConventions) {
      for (final IborIndexConvention indexConvention : iborIndexConventions) {
        final ExternalIdBundle idBundle = indexConvention.getExternalIdBundle();
        if (idBundle.contains(stirFutureConvention.getUnderlyingConventionId())) {
          createAndAddIborIndex(securityMaster, idBundle, indexConvention.getName(), stirFutureConvention.getUnderlyingTenor());
        }
      }
    }
  }

  @Override
  public void init(final ConventionMaster master) {
    LOGGER.error("SecurityMaster not supplied: index securities will not be created");
    for (final ManageableConvention convention : _conventions) {
      addConvention(master, convention);
    }
  }

  /**
   * If the id bundle of the ibor index convention contains a Quandl code, creates an {@link IborIndex} security and stores it
   * in the master.
   * @param securityMaster  the security master
   * @param indexIdBundle  the id bundle of the ibor index convention
   * @param indexName  the ibor index convention name
   * @param tenor  the tenor of the index
   */
  private void createAndAddIborIndex(final SecurityMaster securityMaster, final ExternalIdBundle indexIdBundle, final String indexName, final Tenor tenor) {
    final Set<ExternalId> quandlId = indexIdBundle.getExternalIds(QuandlConstants.QUANDL_CODE);
    if (quandlId == null) {
      LOGGER.error("Could not get Quandl code id from {}: not adding ibor index to security master", indexIdBundle);
      return;
    }
    if (quandlId.size() != 1) {
      LOGGER.error("Could not get unique Quandl code from {}: not adding ibor index to security master", indexIdBundle);
      return;
    }
    final String tenorString;
    if (tenor.isBusinessDayTenor()) {
      tenorString = tenor.toFormattedString();
    } else {
      tenorString = tenor.toFormattedString().substring(1);
    }
    final String securityName = tenorString + " " + indexName;
    final IborIndex iborIndex = new IborIndex(securityName, securityName, tenor, Iterables.getOnlyElement(quandlId), ExternalIdBundle.of(quandlId));
    addSecurity(securityMaster, iborIndex);
  }
}
