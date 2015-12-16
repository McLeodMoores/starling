/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.quandl.loader.index;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import org.apache.commons.lang.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mcleodmoores.quandl.QuandlConstants;
import com.mcleodmoores.quandl.loader.QuandlSecurityLoader;
import com.opengamma.core.convention.ConventionSource;
import com.opengamma.financial.security.index.IborIndex;
import com.opengamma.financial.security.index.OvernightIndex;
import com.opengamma.financial.security.index.SwapIndex;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.scripts.Scriptable;
import com.opengamma.util.time.Tenor;

/**
 * Index security loader that uses codes and information from Quandl. If any errors occur, this loader
 * will return null, as it is likely to be used by other classes that will try to load multiple
 * securities.
 */
@Scriptable
public class QuandlIndexGenerator extends QuandlSecurityLoader {
  /** The logger */
  private static final Logger LOGGER = LoggerFactory.getLogger(QuandlIndexGenerator.class);

  /**
   * Ibor index category.
   */
  public static final String IBOR_INDEX_CATEGORY = "IBOR INDEX";
  /**
   * Overnight index category.
   */
  public static final String OVERNIGHT_INDEX_CATEGORY = "OVERNIGHT INDEX";
  /**
   * Swap index category.
   */
  public static final String SWAP_INDEX_CATEGORY = "SWAP INDEX";

  /**
   * Main method to run this tool.
   * @param args The program arguments
   */
  public static void main(final String[] args) {
    new QuandlIndexGenerator().invokeAndTerminate(args);
  }

  @Override
  protected ManageableSecurity createSecurity(final String[] inputs) {
    if (inputs == null) {
      LOGGER.error("Input was null");
      return null;
    }
    if (inputs.length < 3) {
      LOGGER.error("Input {} did not contain sufficient elements", Arrays.toString(inputs));
      return null;
    }
    final String quandlCode = inputs[0];
    final String category = inputs[1];
    final String name = inputs[2];
    final ManageableSecurity security;
    final ExternalIdBundle idBundle;
    switch (category.trim().toUpperCase()) {
      case IBOR_INDEX_CATEGORY: {
        if (inputs.length < 4) {
          LOGGER.error("Input {} for ibor index creation did not contain sufficient elements", Arrays.toString(inputs));
          return null;
        }
        final Tenor tenor;
        try {
          tenor = Tenor.parse(inputs[3]);
        } catch (final Exception e) {
          LOGGER.error("Could not parse tenor string {} for {}", inputs[3], quandlCode);
          return null;
        }
        final ExternalId indexConventionId = createConventionId(inputs[4], quandlCode);
        idBundle = createIdentifiers(quandlCode, inputs.length == 5 ? null : inputs[5]);
        security = new IborIndex(name, tenor, indexConventionId);
        break;
      }
      case OVERNIGHT_INDEX_CATEGORY: {
        final ExternalId indexConventionId = createConventionId(inputs[3], quandlCode);
        idBundle = createIdentifiers(quandlCode, inputs.length == 4 ? null : inputs[4]);
        security = new OvernightIndex(name, indexConventionId);
        break;
      }
      case SWAP_INDEX_CATEGORY: {
        if (inputs.length < 4) {
          LOGGER.error("Input {} for ibor index creation did not contain sufficient elements", Arrays.toString(inputs));
          return null;
        }
        final Tenor tenor;
        try {
          tenor = Tenor.parse(inputs[3]);
        } catch (final Exception e) {
          LOGGER.error("Could not parse tenor string {} for {}", inputs[3], quandlCode);
          return null;
        }
        final ExternalId indexConventionId = createConventionId(inputs[4], quandlCode);
        idBundle = createIdentifiers(quandlCode, inputs.length == 5 ? null : inputs[5]);
        security = new SwapIndex(name, tenor, indexConventionId);
        break;
      }
      default:
        LOGGER.error("Unrecognised index category {} for {}", category, quandlCode);
        return null;
    }
    security.setExternalIdBundle(idBundle);
    return security;
  }

  @Override
  protected ManageableSecurity createSecurity(final ConventionSource conventionSource, final String quandlCode) {
    throw new NotImplementedException();
  }

  /**
   * Creates a convention id from an identifier string, falling back to using a Quandl code if
   * the identifier string is null or empty.
   * @param identifierString The identifier string
   * @param quandlCode The Quandl code
   * @return The convention id
   */
  private static ExternalId createConventionId(final String identifierString, final String quandlCode) {
    if (identifierString != null && !identifierString.isEmpty()) {
      try {
        return ExternalId.parse(identifierString);
      } catch (final Exception e) {
        LOGGER.error("Could not parse {}; using Quandl code as convention identifier", identifierString);
      }
    }
    return QuandlConstants.ofCode(quandlCode);
  }

  /**
   * Creates an external id bundle from the quandl code and any additional identifiers supplied. The
   * additional identifiers must be parseable in the form "SCHEME~VALUE" and multiple identifiers separated
   * by ";".
   * @param quandlCode The Quandl code.
   * @param additionalIdentifiers The additional identifiers, can be null or empty
   * @return The external id bundle of an index.
   */
  private static ExternalIdBundle createIdentifiers(final String quandlCode, final String additionalIdentifiers) {
    final Collection<ExternalId> externalIds = new HashSet<>();
    externalIds.add(QuandlConstants.ofCode(quandlCode));
    if (additionalIdentifiers != null && additionalIdentifiers.length() > 0) {
      final String[] identifiers = additionalIdentifiers.split(";");
      for (final String identifier : identifiers) {
        final String[] schemeValue = identifier.split("~");
        if (schemeValue.length == 2) {
          externalIds.add(ExternalId.of(schemeValue[0], schemeValue[1]));
        }
      }
    }
    return ExternalIdBundle.of(externalIds);
  }
}
