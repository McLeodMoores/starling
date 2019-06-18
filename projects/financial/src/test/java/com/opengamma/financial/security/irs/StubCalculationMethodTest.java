package com.opengamma.financial.security.irs;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.financial.convention.StubType;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalScheme;
import com.opengamma.util.test.TestGroup;

@Test(groups = TestGroup.UNIT)
public class StubCalculationMethodTest {

  String USD_LIBOR1M = "1MLIBOR";
  String USD_LIBOR2M = "2MLIBOR";
  String USD_LIBOR3M = "3MLIBOR";

  ExternalId _firstStubStartReferenceRateId = ExternalId.of(ExternalScheme.of("CONVENTION"), USD_LIBOR1M);
  ExternalId _firstStubEndReferenceRateId = ExternalId.of(ExternalScheme.of("CONVENTION"), USD_LIBOR3M);
  ExternalId _secondStubStartIndexId = ExternalId.of(ExternalScheme.of("CONVENTION"), USD_LIBOR2M);
  ExternalId secondStubEndIndexId = _firstStubEndReferenceRateId;

  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  private void shortStartStubIllegalDefnTest() {

    final StubCalculationMethod.Builder stubBuilder = StubCalculationMethod.builder()
        .type(StubType.SHORT_START)
        .firstStubEndReferenceRateId(_firstStubEndReferenceRateId);

    final StubCalculationMethod stub = stubBuilder.build();
    stub.validate();
  }

  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  private void longStartStubIllegalDefnTest() {

    final StubCalculationMethod.Builder stubBuilder = StubCalculationMethod.builder()
        .type(StubType.LONG_START)
        .firstStubStartReferenceRateId(_firstStubStartReferenceRateId);

    final StubCalculationMethod stub = stubBuilder.build();
    stub.validate();
  }

  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  private void shortEndStubIllegalDefnTest() {

    final StubCalculationMethod.Builder stubBuilder = StubCalculationMethod.builder()
        .type(StubType.SHORT_END)
        .lastStubStartReferenceRateId(_secondStubStartIndexId);

    final StubCalculationMethod stub = stubBuilder.build();
    stub.validate();
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  private void longEndStubIllegalDefnTest() {

    final StubCalculationMethod.Builder stubBuilder = StubCalculationMethod.builder()
        .firstStubEndReferenceRateId(_firstStubEndReferenceRateId);

    final StubCalculationMethod stub = stubBuilder.build();
    stub.validate();
  }

  @Test
  private void shortStubBuilderValidationTest() {

    final StubCalculationMethod.Builder stubBuilder = StubCalculationMethod.builder()
        .type(StubType.SHORT_START)
        .firstStubStartReferenceRateId(_firstStubStartReferenceRateId)
        .firstStubEndReferenceRateId(_firstStubEndReferenceRateId);
    final StubCalculationMethod stub = stubBuilder.build();
    stub.validate();
  }

  @Test
  private void shortStubBuilderValidationTest2() {

    final StubCalculationMethod.Builder stubBuilder = StubCalculationMethod.builder()
        .type(StubType.BOTH)
        .firstStubEndDate(LocalDate.of(2014, 06, 18))
        .firstStubStartReferenceRateId(_firstStubStartReferenceRateId)
        .firstStubEndReferenceRateId(_firstStubEndReferenceRateId)
        .lastStubEndDate(LocalDate.of(2016, 06, 18))
        .lastStubStartReferenceRateId(_secondStubStartIndexId)
        .lastStubEndReferenceRateId(secondStubEndIndexId);
    final StubCalculationMethod stub = stubBuilder.build();
    stub.validate();
  }
}
