/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.simpleinstruments.derivative;

/**
 *
 * @param <S>
 *          The type of the data
 * @param <T>
 *          The return type of the calculation
 */
public interface SimpleInstrumentVisitor<S, T> {

  T visit(SimpleInstrument derivative, S data);

  T visitSimpleFuture(SimpleFuture future, S data);

  T visitSimpleFXFuture(SimpleFXFuture future, S data);

  T visit(SimpleInstrument derivative);

  T visitSimpleFuture(SimpleFuture future);

  T visitSimpleFXFuture(SimpleFXFuture future);
}
