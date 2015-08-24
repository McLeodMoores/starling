/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.convention;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import org.testng.annotations.Test;
import org.threeten.bp.LocalTime;

import com.opengamma.core.convention.Convention;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.convention.IborIndexConvention;
import com.opengamma.financial.convention.OvernightIndexConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.id.ExternalId;
import com.opengamma.util.i18n.Country;
import com.opengamma.util.money.Currency;

/**
 * Unit tests for {@link ConventionValidationInfo}.
 */
public class ConventionValidationInfoTest {
  /** An overnight convention */
  private static final OvernightIndexConvention OVERNIGHT_INDEX_CONVENTION = new OvernightIndexConvention("USD Overnight",
      ExternalSchemes.syntheticSecurityId("USD O/N").toBundle(), DayCounts.ACT_360, 0, Currency.USD, ExternalSchemes.countryRegionId(Country.US));
  /** An ibor convention */
  private static final IborIndexConvention IBOR_INDEX_CONVENTION_1 = new IborIndexConvention("USD 1M Libor",
      ExternalSchemes.syntheticSecurityId("USD 1M Libor").toBundle(), DayCounts.ACT_365, BusinessDayConventions.MODIFIED_FOLLOWING, 2, false,
      Currency.USD, LocalTime.of(11, 0), "GB", ExternalSchemes.countryRegionId(Country.GB), ExternalSchemes.countryRegionId(Country.GB), "");
  /** An ibor convention */
  private static final IborIndexConvention IBOR_INDEX_CONVENTION_2 = new IborIndexConvention("USD 3M Libor",
      ExternalSchemes.syntheticSecurityId("USD 3M Libor").toBundle(), DayCounts.ACT_365, BusinessDayConventions.MODIFIED_FOLLOWING, 2, false,
      Currency.USD, LocalTime.of(11, 0), "GB", ExternalSchemes.countryRegionId(Country.GB), ExternalSchemes.countryRegionId(Country.GB), "");
  /** Type of the validated conventions */
  private static final Class<IborIndexConvention> TYPE = IborIndexConvention.class;
  /** The validated conventions */
  private static final Collection<IborIndexConvention> VALIDATED = new HashSet<>();
  /** Missing conventions */
  private static final Collection<ExternalId> MISSING = new HashSet<>();
  /** Duplicated conventions */
  private static final Collection<ExternalId> DUPLICATED = new HashSet<>();
  /** Unsupported conventions */
  private static final Collection<Convention> UNSUPPORTED = new HashSet<>();
  /** Convention validation info */
  private static final ConventionValidationInfo<IborIndexConvention> INFO;

  static {
    // note that this isn't actually how the ibor index conventions should be used - the convention should contain common information for USD Libor
    // tickers and the tickers added to the external id bundle
    VALIDATED.add(IBOR_INDEX_CONVENTION_1);
    VALIDATED.add(IBOR_INDEX_CONVENTION_2);
    MISSING.add(ExternalSchemes.syntheticSecurityId("USD 6M Libor"));
    DUPLICATED.add(ExternalSchemes.syntheticSecurityId("USD 9M Libor"));
    UNSUPPORTED.add(OVERNIGHT_INDEX_CONVENTION);
    INFO = new ConventionValidationInfo<>(TYPE, VALIDATED, MISSING, DUPLICATED, UNSUPPORTED);
  }

  /**
   * Tests the behaviour when the type is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullType1() {
    new ConventionValidationInfo<>(null, VALIDATED, MISSING, DUPLICATED);
  }

  /**
   * Tests the behaviour when the type is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullType2() {
    new ConventionValidationInfo<>(null, VALIDATED, MISSING, DUPLICATED, UNSUPPORTED);
  }

  /**
   * Tests the behaviour when the validated convention collection is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullValidated1() {
    new ConventionValidationInfo<>(TYPE, null, MISSING, DUPLICATED);
  }

  /**
   * Tests the behaviour when the validated convention collection is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullValidated2() {
    new ConventionValidationInfo<>(TYPE, null, MISSING, DUPLICATED, UNSUPPORTED);
  }

  /**
   * Tests the behaviour when the missing conventions collection is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullMissing1() {
    new ConventionValidationInfo<>(TYPE, VALIDATED, null, DUPLICATED);
  }

  /**
   * Tests the behaviour when the missing conventions collection is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullMissing2() {
    new ConventionValidationInfo<>(TYPE, VALIDATED, null, DUPLICATED, UNSUPPORTED);
  }

  /**
   * Tests the behaviour when the duplicated conventions collection is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDuplicated1() {
    new ConventionValidationInfo<>(TYPE, VALIDATED, MISSING, null);
  }

  /**
   * Tests the behaviour when the duplicated conventions collection is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDuplicated2() {
    new ConventionValidationInfo<>(TYPE, VALIDATED, MISSING, null, UNSUPPORTED);
  }

  /**
   * Tests the behaviour when the unsupported conventions collection is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullUnsupported() {
    new ConventionValidationInfo<>(TYPE, VALIDATED, MISSING, DUPLICATED, null);
  }

  /**
   * Tests that the validated conventions collection returned is unmodifiable.
   */
  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testUnmodifiableValidatedConventions() {
    final Collection<? super IborIndexConvention> validated = INFO.getValidatedObjects();
    validated.add(VALIDATED.iterator().next());
  }

  /**
   * Tests that the missing conventions collection returned is unmodifiable.
   */
  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testUnmodifiableMissingConventions() {
    final Collection<ExternalId> missing = INFO.getMissingConventionIds();
    missing.add(ExternalSchemes.syntheticSecurityId("USD 12M Libor"));
  }

  /**
   * Tests that the duplicated conventions collection returned is unmodifiable.
   */
  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testUnmodifiableDuplicatedConventions() {
    final Collection<ExternalId> duplicated = INFO.getDuplicatedConventionIds();
    duplicated.add(ExternalSchemes.syntheticSecurityId("USD 3M Libor"));
  }

  /**
   * Tests that the unsupported conventions collection returned is unmodifiable.
   */
  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testUnmodifiableUnsupportedConventions() {
    final Collection<Convention> unsupported = INFO.getUnsupportedConventions();
    unsupported.add(OVERNIGHT_INDEX_CONVENTION);
  }

  /**
   * Tests the object.
   */
  @Test
  public void testObject() {
    final Collection<Convention> validated = new HashSet<>();
    validated.add(IBOR_INDEX_CONVENTION_1);
    validated.add(IBOR_INDEX_CONVENTION_2);
    final Collection<ExternalId> missing = new HashSet<>();
    missing.add(ExternalSchemes.syntheticSecurityId("USD 6M Libor"));
    final Collection<ExternalId> duplicated = new HashSet<>();
    duplicated.add(ExternalSchemes.syntheticSecurityId("USD 9M Libor"));
    final Collection<Convention> unsupported = new HashSet<>();
    unsupported.add(OVERNIGHT_INDEX_CONVENTION);
    assertEquals(INFO.getType(), TYPE);
    assertEquals(INFO.getValidatedObjects(), validated);
    assertEquals(INFO.getMissingConventionIds(), missing);
    assertEquals(INFO.getDuplicatedConventionIds(), duplicated);
    assertEquals(INFO.getUnsupportedConventions(), unsupported);
    ConventionValidationInfo<? extends Convention> other = new ConventionValidationInfo<>(TYPE, VALIDATED, MISSING, DUPLICATED, UNSUPPORTED);
    assertEquals(other, INFO);
    assertEquals(other.hashCode(), INFO.hashCode());
    other = new ConventionValidationInfo<>(Convention.class, Collections.singleton(IBOR_INDEX_CONVENTION_1), MISSING, DUPLICATED, UNSUPPORTED);
    assertNotEquals(other, INFO);
    other = new ConventionValidationInfo<>(TYPE, Collections.singleton(IBOR_INDEX_CONVENTION_1), MISSING, DUPLICATED, UNSUPPORTED);
    assertNotEquals(other, INFO);
    other = new ConventionValidationInfo<>(TYPE, VALIDATED, DUPLICATED, DUPLICATED, UNSUPPORTED);
    assertNotEquals(other, INFO);
    other = new ConventionValidationInfo<>(TYPE, VALIDATED, MISSING, MISSING, UNSUPPORTED);
    assertNotEquals(other, INFO);
    other = new ConventionValidationInfo<>(TYPE, VALIDATED, MISSING, DUPLICATED, VALIDATED);
    assertNotEquals(other, INFO);
  }
}
