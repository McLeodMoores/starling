/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.financial.function.credit.source;

import com.mcleodmoores.financial.function.credit.configs.CreditCurveDefinition;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionDefinition;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.config.ConfigSourceQuery;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.credit.CreditCurveIdentifier;

/**
 *
 */
public class ConfigDbCreditCurveDefinitionSource {

  public static ConfigDbCreditCurveDefinitionSource init(final FunctionCompilationContext context, final FunctionDefinition function) {
    final ConfigDbCreditCurveDefinitionSource source = new ConfigDbCreditCurveDefinitionSource(OpenGammaCompilationContext.getConfigSource(context),
        context.getFunctionInitializationVersionCorrection());
    source._query.reinitOnChange(context, function);
    return source;
  }

  private final ConfigSourceQuery<CreditCurveDefinition> _query;

  private ConfigDbCreditCurveDefinitionSource(final ConfigSource configSource, final VersionCorrection versionCorrection) {
    _query = new ConfigSourceQuery<>(configSource, CreditCurveDefinition.class, versionCorrection);
  }

  public CreditCurveDefinition getDefinition(final CreditCurveIdentifier identifier) {
    return getDefinition(identifier, _query.getVersionCorrection());
  }

  public CreditCurveDefinition getDefinition(final CreditCurveIdentifier identifier, final VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(identifier, "identifier");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    return _query.get(identifier.toString(), versionCorrection);
  }
}
