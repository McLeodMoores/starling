/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.loader;

import static com.opengamma.bbg.BloombergConstants.FIELD_ID_BBG_UNIQUE;
import static com.opengamma.bbg.BloombergConstants.FIELD_ID_CUSIP;
import static com.opengamma.bbg.BloombergConstants.FIELD_ID_ISIN;
import static com.opengamma.bbg.BloombergConstants.FIELD_ID_SEDOL1;
import static com.opengamma.bbg.BloombergConstants.FIELD_INDX_SOURCE;
import static com.opengamma.bbg.BloombergConstants.FIELD_PARSEKYABLE_DES;
import static com.opengamma.bbg.BloombergConstants.FIELD_SECURITY_DES;
import static com.opengamma.bbg.util.BloombergDataUtils.isValidField;

import java.util.Collections;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.fudgemsg.FudgeMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.bbg.referencedata.ReferenceDataProvider;
import com.opengamma.bbg.util.BloombergDataUtils;
import com.opengamma.financial.security.index.IborIndex;
import com.opengamma.financial.security.index.Index;
import com.opengamma.financial.security.index.OvernightIndex;
import com.opengamma.financial.security.index.PriceIndex;
import com.opengamma.financial.security.index.SwapIndex;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalScheme;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.util.time.Tenor;

/**
 * Loads the data for an Index Future from Bloomberg.
 */
public class IndexLoader extends SecurityLoader {

  private static final String BLOOMBERG_INDEX_TYPE = "Index";
  /**
   * Valid Security type values for this index
   */
  public static final Set<String> VALID_SECURITY_TYPES = Collections.unmodifiableSet(Sets.newHashSet(BLOOMBERG_INDEX_TYPE));
  /** Logger. */
  private static final Logger LOGGER = LoggerFactory.getLogger(IndexLoader.class);
  /**
   * The fields to load from Bloomberg.
   */
  private static final Set<String> BLOOMBERG_INDEX_FIELDS = Collections.unmodifiableSet(Sets.newHashSet(
      FIELD_SECURITY_DES,
      FIELD_PARSEKYABLE_DES,
      FIELD_INDX_SOURCE,
      FIELD_ID_BBG_UNIQUE,
      FIELD_ID_CUSIP,
      FIELD_ID_ISIN,
      FIELD_ID_SEDOL1));

  private static final Pattern TENOR_FROM_DES = Pattern.compile("(.*?)(Overnight.*?|O\\/N.*?|OVERNIGHT.*?|Tomorrow[\\s\\/]Next.*?|T[\\s\\/]N.*?|TOM[\\s\\/]NEXT.*?|\\d+\\s*.*?)");
  private static final Pattern OVERNIGHT = Pattern.compile(".*?(Overnight|O\\/N|OVERNIGHT).*?");
  private static final Pattern TOM_NEXT = Pattern.compile(".*?(Tomorrow[\\s\\/]Next|T[\\s\\/]N|TOM[\\s\\/]NEXT).*?");
  private static final Pattern NUMBER_FROM_TIME_UNIT = Pattern.compile("(\\d+)\\s*(.*?)");
  private static final String BLOOMBERG_CONVENTION_NAME = "BLOOMBERG_CONVENTION_NAME";
  private static final String BLOOMBERG_INDEX_FAMILY = "BLOOMBERG_INDEX_FAMILY";

  private static final String FED_FUNDS_SECURITY_DES = "Federal Funds Effective Rate U";
  private static final Set<String> BLOOMBERG_SECURITY_DES_OVERNIGHT_EXCEPTIONS = Collections.unmodifiableSet(Sets.newHashSet(
      FED_FUNDS_SECURITY_DES
      ));

  /**
   * Creates an instance.
   * @param referenceDataProvider  the provider, not null
   */
  public IndexLoader(final ReferenceDataProvider referenceDataProvider) {
    super(LOGGER, referenceDataProvider, SecurityType.INDEX);
  }

  //-------------------------------------------------------------------------
  @Override
  protected ManageableSecurity createSecurity(final FudgeMsg fieldData) {
    final String securityDes = fieldData.getString(FIELD_SECURITY_DES);
    final String name = BloombergDataUtils.removeDuplicateWhiteSpace(fieldData.getString(FIELD_SECURITY_DES), " ");
    final String bbgUnique = fieldData.getString(FIELD_ID_BBG_UNIQUE);
    final String indexSource = fieldData.getString(FIELD_INDX_SOURCE);

    if (!isValidField(bbgUnique)) {
      LOGGER.warn("bbgUnique is null, cannot construct index");
      return null;
    }
    if (!isValidField(securityDes)) {
      LOGGER.warn("security description is null, cannot construct index");
      return null;
    }
    if (!isValidField(indexSource)) {
      LOGGER.warn("index source is null, cannot construct index");
      return null;
    }

    Index index;
    final Tenor tenor = decodeTenor(securityDes);
    final ExternalId conventionId = createConventionId(securityDes);
    final ExternalId familyId = ExternalId.of(ExternalScheme.of(BLOOMBERG_INDEX_FAMILY), conventionId.getValue());

    if (indexSource.toUpperCase().contains("STAT")) {
      // guess it's a price index as source is STATistics agency.  Crude, but hopefully effective.
      index = new PriceIndex(name, conventionId);
      index.setIndexFamilyId(null);
    } else if (securityDes.toUpperCase().contains("ISDAFIX") && tenor != null) {
      // guess it's a swap index
      index = new SwapIndex(name, tenor, conventionId);
      index.setIndexFamilyId(familyId);
    } else if (tenor != null) {
      // Ibor or overnight
      if (tenor.equals(Tenor.ON)) {
        index = new OvernightIndex(name, conventionId);
        index.setIndexFamilyId(familyId);
      } else {
        index = new IborIndex(name, tenor, conventionId);
        index.setIndexFamilyId(familyId);
      }
      index.setName(name);
    } else {
      LOGGER.error("Could not load index {}, source={}, tenor={}, securityDes={}", name, indexSource, tenor, securityDes);
      return null;
    }

    // set identifiers
    parseIdentifiers(fieldData, index);
    return index;
  }

  // public visible for tests
  public static ExternalId createConventionId(final String securityDes) {
    final Matcher matcher = TENOR_FROM_DES.matcher(securityDes);
    if (matcher.matches()) {
      final String descriptionPart = matcher.group(1); // remember, groups are 1 indexed!
      return ExternalId.of(ExternalScheme.of(BLOOMBERG_CONVENTION_NAME), descriptionPart.trim());
    }
    return ExternalId.of(ExternalScheme.of(BLOOMBERG_CONVENTION_NAME), securityDes.trim());
  }

  // public visible for tests
  public static Tenor decodeTenor(final String securityDes) {
    if (BLOOMBERG_SECURITY_DES_OVERNIGHT_EXCEPTIONS.contains(securityDes)) {
      return Tenor.ON;
    }
    final Matcher matcher = TENOR_FROM_DES.matcher(securityDes);
    if (matcher.matches()) {
      final String tenorPart = matcher.group(2); // remember, groups are 1 indexed!
      if (OVERNIGHT.matcher(tenorPart).matches()) {
        return Tenor.ON;
      } else if (TOM_NEXT.matcher(tenorPart).matches()) {
        return Tenor.TN;
      } else {
        final Matcher numberFromTimeMatcher = NUMBER_FROM_TIME_UNIT.matcher(tenorPart);
        if (numberFromTimeMatcher.matches()) {
          final String numberStr = numberFromTimeMatcher.group(1).trim();
          final int number = Integer.parseInt(numberStr);
          final String timeUnit = numberFromTimeMatcher.group(2).trim().toUpperCase();
          if (timeUnit.length() == 0) {
            throw new OpenGammaRuntimeException("Could not decode tenor from description " + securityDes);
          }
          switch (timeUnit.charAt(0)) {
            case 'D':
              // assume days!
              return Tenor.ofDays(number);
            case 'M':
              // assume months!
              return Tenor.ofMonths(number);
            case 'Y':
              // assume years!
              return Tenor.ofYears(number);
          }
        }
      }
    }
    return null;
  }

  @Override
  protected Set<String> getBloombergFields() {
    return BLOOMBERG_INDEX_FIELDS;
  }

}
