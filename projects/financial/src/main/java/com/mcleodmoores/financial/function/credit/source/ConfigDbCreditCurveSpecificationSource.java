/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.financial.function.credit.source;

import java.util.Collection;
import java.util.HashSet;

import org.threeten.bp.LocalDate;

import com.mcleodmoores.financial.function.credit.configs.CreditCurveDefinition;
import com.mcleodmoores.financial.function.credit.configs.CreditCurveSpecification;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionDefinition;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.analytics.curve.CurveNodeIdMapper;
import com.opengamma.financial.analytics.curve.CurveNodeWithIdentifierBuilder;
import com.opengamma.financial.analytics.ircurve.strips.CurveNode;
import com.opengamma.financial.analytics.ircurve.strips.CurveNodeWithIdentifier;
import com.opengamma.financial.config.ConfigSourceQuery;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.credit.CreditCurveIdentifier;

/**
 *
 */
public class ConfigDbCreditCurveSpecificationSource {

  public static ConfigDbCreditCurveSpecificationSource init(final FunctionCompilationContext context, final FunctionDefinition function) {
    final ConfigDbCreditCurveSpecificationSource source = new ConfigDbCreditCurveSpecificationSource(OpenGammaCompilationContext.getConfigSource(context),
        context.getFunctionInitializationVersionCorrection(), ConfigDbCreditCurveDefinitionSource.init(context, function));
    source._curveNodeIdMapperQuery.reinitOnChange(context, function);
    return source;
  }

  private final ConfigSourceQuery<CurveNodeIdMapper> _curveNodeIdMapperQuery;
  private final ConfigDbCreditCurveDefinitionSource _definitionSource;
  private final VersionCorrection _versionCorrection;

  private ConfigDbCreditCurveSpecificationSource(final ConfigSource configSource, final VersionCorrection versionCorrection,
      final ConfigDbCreditCurveDefinitionSource definitionSource) {
    _curveNodeIdMapperQuery = new ConfigSourceQuery<>(configSource, CurveNodeIdMapper.class, versionCorrection);
    _definitionSource = definitionSource;
    _versionCorrection = versionCorrection;
  }

  public CreditCurveSpecification getCreditCurveSpecification(final CreditCurveIdentifier identifier, final LocalDate curveDate) {
    final CreditCurveDefinition definition = _definitionSource.getDefinition(identifier, _versionCorrection);
    if (definition == null) {
      throw new IllegalArgumentException("Could not get CreditCurveDefinition called " + identifier.toString());
    }
    final Collection<CurveNodeWithIdentifier> nodes = new HashSet<>();
    for (final CurveNode node : definition.getNodes()) {
      final CurveNodeIdMapper idMapper = _curveNodeIdMapperQuery.get(node.getCurveNodeIdMapperName());
      final CurveNodeWithIdentifierBuilder builder = new CurveNodeWithIdentifierBuilder(curveDate, idMapper);
      nodes.add(node.accept(builder));
    }
    return new CreditCurveSpecification(curveDate, identifier, nodes);
  }
}
