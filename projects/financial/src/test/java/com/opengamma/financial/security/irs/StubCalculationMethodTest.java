/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.irs;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.financial.convention.StubType;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalScheme;
import com.opengamma.util.test.TestGroup;

/**
 *
 */
@Test(groups = TestGroup.UNIT)
public class StubCalculationMethodTest {

  private static final String USD_LIBOR1M = "1MLIBOR";
  private static final String USD_LIBOR2M = "2MLIBOR";
  private static final String USD_LIBOR3M = "3MLIBOR";

  private static final ExternalId FIRST_STUB_START = ExternalId.of(ExternalScheme.of("CONVENTION"), USD_LIBOR1M);
  private static final ExternalId FIRST_STUB_END = ExternalId.of(ExternalScheme.of("CONVENTION"), USD_LIBOR3M);
  private static final ExternalId SECOND_STUB_START = ExternalId.of(ExternalScheme.of("CONVENTION"), USD_LIBOR2M);
  private static final ExternalId SECOND_STUB_END = FIRST_STUB_END;

  /**
   *
   */
  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void shortStartStubIllegalDefnTest() {

    final StubCalculationMethod.Builder stubBuilder = StubCalculationMethod.builder()
        .type(StubType.SHORT_START)
        .firstStubEndReferenceRateId(FIRST_STUB_END);

    final StubCalculationMethod stub = stubBuilder.build();
    stub.validate();
  }

  /**
   *
   */
  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void longStartStubIllegalDefnTest() {

    final StubCalculationMethod.Builder stubBuilder = StubCalculationMethod.builder()
        .type(StubType.LONG_START)
        .firstStubStartReferenceRateId(FIRST_STUB_START);

    final StubCalculationMethod stub = stubBuilder.build();
    stub.validate();
  }

  /**
   *
   */
  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void shortEndStubIllegalDefnTest() {

    final StubCalculationMethod.Builder stubBuilder = StubCalculationMethod.builder()
        .type(StubType.SHORT_END)
        .lastStubStartReferenceRateId(SECOND_STUB_START);

    final StubCalculationMethod stub = stubBuilder.build();
    stub.validate();
  }

  /**
   *
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void longEndStubIllegalDefnTest() {

    final StubCalculationMethod.Builder stubBuilder = StubCalculationMethod.builder()
        .firstStubEndReferenceRateId(FIRST_STUB_END);

    final StubCalculationMethod stub = stubBuilder.build();
    stub.validate();
  }

  /**
   *
   */
  @Test
  public void shortStubBuilderValidationTest() {

    final StubCalculationMethod.Builder stubBuilder = StubCalculationMethod.builder()
        .type(StubType.SHORT_START)
        .firstStubStartReferenceRateId(FIRST_STUB_START)
        .firstStubEndReferenceRateId(FIRST_STUB_END);
    final StubCalculationMethod stub = stubBuilder.build();
    stub.validate();
  }

  /**
   *
   */
  @Test
  public void shortStubBuilderValidationTest2() {

    final StubCalculationMethod.Builder stubBuilder = StubCalculationMethod.builder()
        .type(StubType.BOTH)
        .firstStubEndDate(LocalDate.of(2014, 06, 18))
        .firstStubStartReferenceRateId(FIRST_STUB_START)
        .firstStubEndReferenceRateId(FIRST_STUB_END)
        .lastStubEndDate(LocalDate.of(2016, 06, 18))
        .lastStubStartReferenceRateId(SECOND_STUB_START)
        .lastStubEndReferenceRateId(SECOND_STUB_END);
    final StubCalculationMethod stub = stubBuilder.build();
    stub.validate();
  }
}
