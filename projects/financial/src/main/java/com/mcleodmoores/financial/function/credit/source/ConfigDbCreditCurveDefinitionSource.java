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
 * A source that retrieves {@link CreditCurveDefinition}s from a {@link ConfigSource}. If a definition is changed, the query is re-initialized.
 */
public class ConfigDbCreditCurveDefinitionSource {

  /**
   * Initializes and returns the source. If a {@link CreditCurveDefinition} is added or changed, the source will re-initialize.
   *
   * @param context
   *          a function compilation context, not null
   * @param function
   *          the function that is using this source, not null
   * @return the source
   */
  public static ConfigDbCreditCurveDefinitionSource init(final FunctionCompilationContext context, final FunctionDefinition function) {
    ArgumentChecker.notNull(context, "context");
    ArgumentChecker.notNull(function, "function");
    final ConfigDbCreditCurveDefinitionSource source = new ConfigDbCreditCurveDefinitionSource(OpenGammaCompilationContext.getConfigSource(context),
        context.getFunctionInitializationVersionCorrection());
    source._query.reinitOnChange(context, function);
    return source;
  }

  private final ConfigSourceQuery<CreditCurveDefinition> _query;

  private ConfigDbCreditCurveDefinitionSource(final ConfigSource configSource, final VersionCorrection versionCorrection) {
    _query = new ConfigSourceQuery<>(configSource, CreditCurveDefinition.class, versionCorrection);
  }

  /**
   * Gets the latest version of a {@link CreditCurveDefinition} for the identifier.
   *
   * @param identifier
   *          the identifier, not null
   * @return the credit curve definition or null if not found
   */
  public CreditCurveDefinition getDefinition(final CreditCurveIdentifier identifier) {
    return getDefinition(identifier, _query.getVersionCorrection());
  }

  /**
   * Gets a {@link CreditCurveDefinition} for the identifier.
   *
   * @param identifier
   *          the identifier, not null
   * @param versionCorrection
   *          the version of the definition, not null
   * @return the credit curve definition or null if not found
   */
  public CreditCurveDefinition getDefinition(final CreditCurveIdentifier identifier, final VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(identifier, "identifier");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    return _query.get(identifier.toString(), versionCorrection);
  }
}
