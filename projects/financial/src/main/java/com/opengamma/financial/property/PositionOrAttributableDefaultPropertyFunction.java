/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.property;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValueRequirement;

/**
 * Dummy function to inject default properties from a position or trade's attributes into the dependency graph.
 * <p>
 * Any attributes of the form <code><em>ValueName</em>.DEFAULT_<em>PropertyName</em></code> will be
 * processed to introduce a default value for any omitted <em>PropertyName</em> on <em>ValueName</em> for the target.
 */
/* package */abstract class PositionOrAttributableDefaultPropertyFunction extends DefaultPropertyFunction {

  private static final Logger s_logger = LoggerFactory.getLogger(PositionOrAttributableDefaultPropertyFunction.class);

  private static final String WILDCARD = "*";
  private static final String SEP = ".DEFAULT_";

  public PositionOrAttributableDefaultPropertyFunction(final ComputationTargetType type) {
    super(type, false);
  }

  protected abstract Map<String, String> getAttributes(ComputationTarget target);

  @Override
  protected void getDefaults(final PropertyDefaults defaults) {
    for (final Map.Entry<String, String> attribute : getAttributes(defaults.getTarget()).entrySet()) {
      final int i = attribute.getKey().indexOf(SEP);
      if (i > 0) {
        final String valueName = attribute.getKey().substring(0, i);
        final String propertyName = attribute.getKey().substring(i + SEP.length());
        if (WILDCARD.equals(valueName)) {
          defaults.addAllValuesPropertyName(propertyName);
        } else {
          defaults.addValuePropertyName(valueName, propertyName);
        }
        s_logger.debug("Found default {}[{}]", valueName, propertyName);
      }
    }
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    final Map<String, String> attributes = getAttributes(target);
    if ((attributes == null) || attributes.isEmpty()) {
      s_logger.debug("No attributes for target {}", target);
      return false;
    }
    for (final Map.Entry<String, String> attribute : attributes.entrySet()) {
      final int i = attribute.getKey().indexOf(SEP);
      if (i > 0) {
        s_logger.debug("Found attribute {} for target {}", attribute.getKey(), target);
        return true;
      }
    }
    s_logger.debug("No matching attributes for target {}", target);
    return false;
  }

  @Override
  protected Set<String> getDefaultValue(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue, final String propertyName) {
    final Map<String, String> attributes = getAttributes(target);
    String defaultValue = attributes.get(desiredValue.getValueName() + SEP + propertyName);
    if (defaultValue != null) {
      return Collections.singleton(defaultValue);
    }
    defaultValue = attributes.get(WILDCARD + SEP + propertyName);
    assert defaultValue != null;
    return Collections.singleton(defaultValue);
  }

  /**
   * Position and trade default functions are declared a lower priority so that the normal functions that work
   * from the calculation configuration can override their behavior.
   *
   * @return {@link com.opengamma.financial.property.DefaultPropertyFunction.PriorityClass#BELOW_NORMAL}
   */
  @Override
  public PriorityClass getPriority() {
    return PriorityClass.BELOW_NORMAL;
  }

}
