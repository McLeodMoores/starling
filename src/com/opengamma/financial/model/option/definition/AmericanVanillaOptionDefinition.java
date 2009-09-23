package com.opengamma.financial.model.option.definition;

import com.opengamma.math.function.Function;
import com.opengamma.math.function.Function1D;
import com.opengamma.util.time.Expiry;

public class AmericanVanillaOptionDefinition extends OptionDefinition {

  public AmericanVanillaOptionDefinition(double strike, Expiry expiry, boolean isCall) {
    super(strike, expiry, isCall);
  }

  @Override
  protected void initPayoffAndExerciseFunctions() {
    _payoffFunction = new Function1D<Double, Double>() {

      @Override
      public Double evaluate(Double spot) {
        return isCall() ? Math.max(0, spot - getStrike()) : Math.max(0, getStrike() - spot);
      }

    };
    _exerciseFunction = new Function<Double, Boolean>() {

      @Override
      public Boolean evaluate(Double... x) {
        double spot = x[0];
        double option = x[1];
        return isCall() ? option > getStrike() - spot : option > spot - getStrike();
      }

    };
  }
}
