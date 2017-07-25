/**
 *
 */
package com.opengamma.financial.analytics.model.black;

import static com.opengamma.engine.value.ValuePropertyNames.CURVE_EXPOSURES;
import static com.opengamma.engine.value.ValuePropertyNames.SURFACE;
import static com.opengamma.engine.value.ValueRequirementNames.FX_PRESENT_VALUE;
import static com.opengamma.financial.analytics.model.InterpolatedDataProperties.LEFT_X_EXTRAPOLATOR_NAME;
import static com.opengamma.financial.analytics.model.InterpolatedDataProperties.RIGHT_X_EXTRAPOLATOR_NAME;
import static com.opengamma.financial.analytics.model.InterpolatedDataProperties.X_INTERPOLATOR_NAME;

import java.util.Collections;
import java.util.Set;

import com.opengamma.core.security.Security;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.financial.property.DefaultPropertyFunction;
import com.opengamma.financial.security.option.FXOptionSecurity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.UnorderedCurrencyPair;

/**
 *
 */
public class BlackDiscountingVanillaFxOptionDefaults extends DefaultPropertyFunction {
  private static final String[] VALUE_REQUIREMENTS = new String[] {
      FX_PRESENT_VALUE
  };
  private final UnorderedCurrencyPair _underlying;
  private final String _surfaceName;
  private final String _curveExposuresName;
  private final String _xInterpolatorName;
  private final String _leftXExtrapolatorName;
  private final String _rightXExtrapolatorName;

  public BlackDiscountingVanillaFxOptionDefaults(final String ccy1, final String ccy2, final String surfaceName,
      final String curveExposuresName, final String xInterpolatorName, final String leftXExtrapolatorName, final String rightXExtrapolatorName) {
    super(ComputationTargetType.TRADE, true);
    ArgumentChecker.notNull(ccy1, "ccy1");
    ArgumentChecker.notNull(ccy2, "ccy2");
    _underlying = UnorderedCurrencyPair.of(Currency.of(ccy1), Currency.of(ccy2));
    _surfaceName = ArgumentChecker.notNull(surfaceName, "surfaceName");
    _curveExposuresName = ArgumentChecker.notNull(curveExposuresName, "curveExposuresName");
    _xInterpolatorName = ArgumentChecker.notNull(xInterpolatorName, "interpolatorName");
    _leftXExtrapolatorName = ArgumentChecker.notNull(leftXExtrapolatorName, "leftXExtrapolatorName");
    _rightXExtrapolatorName = ArgumentChecker.notNull(rightXExtrapolatorName, "rightXExtrapolatorName");
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    final Security security = target.getTrade().getSecurity();
    if (!(security instanceof FXOptionSecurity)) {
      return false;
    }
    final FXOptionSecurity fxOption = (FXOptionSecurity) security;
    return UnorderedCurrencyPair.of(fxOption.getCallCurrency(), fxOption.getPutCurrency()).equals(_underlying);
  }

  @Override
  protected void getDefaults(final PropertyDefaults defaults) {
    for (final String valueRequirement : VALUE_REQUIREMENTS) {
      defaults.addValuePropertyName(valueRequirement, SURFACE);
      defaults.addValuePropertyName(valueRequirement, CURVE_EXPOSURES);
      defaults.addValuePropertyName(valueRequirement, X_INTERPOLATOR_NAME);
      defaults.addValuePropertyName(valueRequirement, LEFT_X_EXTRAPOLATOR_NAME);
      defaults.addValuePropertyName(valueRequirement, RIGHT_X_EXTRAPOLATOR_NAME);
    }
  }

  @Override
  protected Set<String> getDefaultValue(final FunctionCompilationContext context, final ComputationTarget target,
      final ValueRequirement desiredValue, final String propertyName) {
    switch (propertyName) {
      case SURFACE:
        return Collections.singleton(_surfaceName);
      case CURVE_EXPOSURES:
        return Collections.singleton(_curveExposuresName);
      case X_INTERPOLATOR_NAME:
        return Collections.singleton(_xInterpolatorName);
      case LEFT_X_EXTRAPOLATOR_NAME:
        return Collections.singleton(_leftXExtrapolatorName);
      case RIGHT_X_EXTRAPOLATOR_NAME:
        return Collections.singleton(_rightXExtrapolatorName);
      default:
        return null;
    }
  }
}
