/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.provider;

import org.apache.commons.math.stat.descriptive.rank.Min;

import com.opengamma.analytics.financial.interestrate.bond.provider.BondSecurityDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuturesSecurity;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Method to compute the bond futures security results with the price computed as the cheapest forward.
 */
public final class BondFuturesSecurityDiscountingMethod extends FuturesSecurityIssuerMethod {

  /**
   * Creates the method unique instance.
   */
  private static final BondFuturesSecurityDiscountingMethod INSTANCE = new BondFuturesSecurityDiscountingMethod();

  /**
   * Return the method unique instance.
   *
   * @return The instance.
   */
  public static BondFuturesSecurityDiscountingMethod getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  private BondFuturesSecurityDiscountingMethod() {
  }

  /**
   * The method to compute bond security figures.
   */
  private static final BondSecurityDiscountingMethod BOND_METHOD = BondSecurityDiscountingMethod.getInstance();
  /**
   * Method used to compute the minimum of an array.
   */
  private static final Min MIN_FUNCTION = new Min();

  /**
   * Computes the future price from the curves used to price the underlying bonds.
   *
   * @param future
   *          The future security.
   * @param issuerMulticurves
   *          The issuer and multi-curves provider.
   * @return The future price.
   */
  public double price(final BondFuturesSecurity future, final IssuerProviderInterface issuerMulticurves) {
    return priceFromNetBasis(future, issuerMulticurves, 0.0);
  }

  /**
   * Compute the present value of a future transaction from a quoted price.
   * 
   * @param future
   *          The future.
   * @param price
   *          The quoted price.
   * @return The present value.
   */
  public MultipleCurrencyAmount presentValueFromPrice(final BondFuturesSecurity future, final double price) {
    final double referencePrice = 1; // TODO
    final double pv = (price - referencePrice) * future.getNotional();
    return MultipleCurrencyAmount.of(future.getCurrency(), pv);
  }

  /**
   * Computes the future price curve sensitivity.
   *
   * @param future
   *          The future security.
   * @param issuerMulticurves
   *          The issuer and multi-curves provider.
   * @return The curve sensitivity.
   */
  public MulticurveSensitivity priceCurveSensitivity(final BondFuturesSecurity future, final IssuerProviderInterface issuerMulticurves) {
    ArgumentChecker.notNull(future, "Future");
    ArgumentChecker.notNull(issuerMulticurves, "Issuer and multi-curves provider");
    final double[] priceFromBond = new double[future.getDeliveryBasketAtDeliveryDate().length];
    int indexCTD = 0;
    double priceMin = 2.0;
    for (int i = 0; i < future.getDeliveryBasketAtDeliveryDate().length; i++) {
      priceFromBond[i] = BOND_METHOD.cleanPriceFromCurves(future.getDeliveryBasketAtDeliveryDate()[i], issuerMulticurves)
          / future.getConversionFactor()[i];
      if (priceFromBond[i] < priceMin) {
        priceMin = priceFromBond[i];
        indexCTD = i;
      }
    }
    final MulticurveSensitivity result = BOND_METHOD.dirtyPriceCurveSensitivity(future.getDeliveryBasketAtDeliveryDate()[indexCTD],
        issuerMulticurves);
    return result.multipliedBy(1.0 / future.getConversionFactor()[indexCTD]);
  }

  /**
   * Compute the present value sensitivity to rates of a bond future by discounting.
   *
   * @param future
   *          The future.
   * @param issuerMulticurves
   *          The issuer and multi-curves provider.
   * @return The present value rate sensitivity.
   */
  public MultipleCurrencyMulticurveSensitivity presentValueCurveSensitivity(final BondFuturesSecurity future,
      final IssuerProviderInterface issuerMulticurves) {
    final Currency ccy = future.getCurrency();
    final MulticurveSensitivity priceSensitivity = priceCurveSensitivity(future, issuerMulticurves);
    final MultipleCurrencyMulticurveSensitivity transactionSensitivity = MultipleCurrencyMulticurveSensitivity.of(ccy,
        priceSensitivity.multipliedBy(future.getNotional()));
    return transactionSensitivity;
  }

  /**
   * Computes the futures price from the curves used to price the underlying bonds and the net basis.
   *
   * @param futures
   *          The future security.
   * @param issuerMulticurves
   *          The issuer and multi-curves provider.
   * @param netBasis
   *          The net basis associated to the future.
   * @return The future price.
   */
  public double priceFromNetBasis(final BondFuturesSecurity futures, final IssuerProviderInterface issuerMulticurves,
      final double netBasis) {
    ArgumentChecker.notNull(futures, "Future");
    ArgumentChecker.notNull(issuerMulticurves, "Issuer and multi-curves provider");
    final double[] priceFromBond = new double[futures.getDeliveryBasketAtDeliveryDate().length];
    for (int i = 0; i < futures.getDeliveryBasketAtDeliveryDate().length; i++) {
      priceFromBond[i] = (BOND_METHOD.cleanPriceFromCurves(futures.getDeliveryBasketAtDeliveryDate()[i], issuerMulticurves) - netBasis)
          / futures.getConversionFactor()[i];
    }
    final double priceFuture = MIN_FUNCTION.evaluate(priceFromBond);
    return priceFuture;
  }

  /**
   * Computes the gross basis of the bonds in the underlying basket from their clean prices.
   *
   * @param futures
   *          The future security.
   * @param cleanPrices
   *          The clean prices (at standard bond market spot date) of the bond in the basket.
   * @param futurePrice
   *          The future price.
   * @return The gross basis for each bond in the basket.
   */
  public double[] grossBasisFromPrices(final BondFuturesSecurity futures, final double[] cleanPrices, final double futurePrice) {
    ArgumentChecker.notNull(futures, "futures");
    ArgumentChecker.notNull(cleanPrices, "cleanPrices");
    final int nbBasket = futures.getDeliveryBasketAtDeliveryDate().length;
    ArgumentChecker.isTrue(cleanPrices.length == nbBasket, "Number of clean prices");
    final double[] grossBasis = new double[nbBasket];
    for (int i = 0; i < futures.getDeliveryBasketAtDeliveryDate().length; i++) {
      grossBasis[i] = cleanPrices[i] - futurePrice * futures.getConversionFactor()[i];
    }
    return grossBasis;
  }

  /**
   * Computes the gross basis of the bonds in the underlying basket from the curves.
   *
   * @param futures
   *          The future security.
   * @param issuerMulticurves
   *          The issuer and multi-curves provider.
   * @param futurePrice
   *          The future price.
   * @return The gross basis for each bond in the basket.
   */
  public double[] grossBasisFromCurves(final BondFuturesSecurity futures, final IssuerProviderInterface issuerMulticurves,
      final double futurePrice) {
    ArgumentChecker.notNull(futures, "future");
    ArgumentChecker.notNull(issuerMulticurves, "issuerMulticurves");
    final int nbBasket = futures.getDeliveryBasketAtDeliveryDate().length;
    final double[] grossBasis = new double[nbBasket];
    for (int i = 0; i < futures.getDeliveryBasketAtDeliveryDate().length; i++) {
      final double cleanPrice = BOND_METHOD.cleanPriceFromCurves(futures.getDeliveryBasketAtSpotDate()[i], issuerMulticurves);
      grossBasis[i] = cleanPrice - futurePrice * futures.getConversionFactor()[i];
    }
    return grossBasis;
  }

  /**
   * Computes the net basis of all the bonds in the underlying basket from the curves and the future price.
   *
   * @param futures
   *          The future security.
   * @param issuerMulticurves
   *          The issuer and multi-curves provider.
   * @param futurePrice
   *          The future price.
   * @return The net basis for each bond in the basket.
   */
  public double[] netBasisAllBonds(final BondFuturesSecurity futures, final IssuerProviderInterface issuerMulticurves,
      final double futurePrice) {
    ArgumentChecker.notNull(futures, "Future");
    ArgumentChecker.notNull(issuerMulticurves, "Issuer and multi-curves provider");
    final int nbBasket = futures.getDeliveryBasketAtDeliveryDate().length;
    final double[] netBasis = new double[nbBasket];
    for (int i = 0; i < futures.getDeliveryBasketAtDeliveryDate().length; i++) {
      final double cleanPrice = BOND_METHOD.cleanPriceFromCurves(futures.getDeliveryBasketAtDeliveryDate()[i], issuerMulticurves);
      netBasis[i] = cleanPrice - futurePrice * futures.getConversionFactor()[i];
    }
    return netBasis;
  }

  /**
   * Computes the net basis of associated to the cheapest to deliver bonds in the underlying basket from the curves and the future price.
   *
   * @param futures
   *          The future security.
   * @param issuerMulticurves
   *          The issuer and multi-curves provider.
   * @param futurePrice
   *          The future price.
   * @return The net basis.
   */
  public double netBasisCheapest(final BondFuturesSecurity futures, final IssuerProviderInterface issuerMulticurves,
      final double futurePrice) {
    ArgumentChecker.notNull(futures, "Future");
    ArgumentChecker.notNull(issuerMulticurves, "Issuer and multi-curves provider");
    final int nbBasket = futures.getDeliveryBasketAtDeliveryDate().length;
    final double[] netBasis = new double[nbBasket];
    for (int i = 0; i < futures.getDeliveryBasketAtDeliveryDate().length; i++) {
      netBasis[i] = BOND_METHOD.cleanPriceFromCurves(futures.getDeliveryBasketAtDeliveryDate()[i], issuerMulticurves)
          - futurePrice * futures.getConversionFactor()[i];
    }
    return MIN_FUNCTION.evaluate(netBasis);
  }

}
