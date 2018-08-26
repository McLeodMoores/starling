/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.volatilityswap;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 *
 */
@Test(groups = TestGroup.UNIT)
public class VolatilitySwapCalculatorResultTest {

  private static final double[] PUT_WEIGHTS = new double[] {0., -1., 1.5, };
  private static final double STRADDLE_WEIGHT = 1.e2;
  private static final double[] CALL_WEIGHTS = new double[] {11. / 7. };
  private static final double[] PUT_PRICES = new double[] {3.1, 4., 5.214 };
  private static final double STRADDLE_PRICE = 2.2;
  private static final double[] CALL_PRICES = new double[] {33. };
  private static final double CASH = 11.3;

  private static final double[] PUT_STRIKES = new double[] {1.1, 1.3, 1.4 };
  private static final double[] CALL_STRIKES = new double[] {1.6 };

  /**
   *
   */
  @Test
  public void accessTest() {

    final VolatilitySwapCalculatorResult res =
        new VolatilitySwapCalculatorResult(PUT_WEIGHTS, STRADDLE_WEIGHT, CALL_WEIGHTS, PUT_PRICES, STRADDLE_PRICE, CALL_PRICES, CASH);
    final VolatilitySwapCalculatorResultWithStrikes resStrikes =
        new VolatilitySwapCalculatorResultWithStrikes(PUT_STRIKES, CALL_STRIKES, PUT_WEIGHTS, STRADDLE_WEIGHT, CALL_WEIGHTS, PUT_PRICES,
        STRADDLE_PRICE, CALL_PRICES, CASH);
    final int nPuts = PUT_WEIGHTS.length;
    final int nCalls = CALL_WEIGHTS.length;

    double optionTotal = STRADDLE_WEIGHT * STRADDLE_PRICE;
    for (int i = 0; i < nPuts; ++i) {
      assertEquals(PUT_WEIGHTS[i], res.getPutWeights()[i]);
      assertEquals(PUT_PRICES[i], res.getPutPrices()[i]);
      assertEquals(PUT_STRIKES[i], resStrikes.getPutStrikes()[i]);
      optionTotal += PUT_WEIGHTS[i] * PUT_PRICES[i];
    }
    assertEquals(STRADDLE_WEIGHT, res.getStraddleWeight());
    assertEquals(STRADDLE_PRICE, res.getStraddlePrice());
    for (int i = 0; i < nCalls; ++i) {
      assertEquals(CALL_WEIGHTS[i], res.getCallWeights()[i]);
      assertEquals(CALL_PRICES[i], res.getCallPrices()[i]);
      assertEquals(CALL_STRIKES[i], resStrikes.getCallStrikes()[i]);
      optionTotal += CALL_WEIGHTS[i] * CALL_PRICES[i];
    }

    assertEquals(CASH, res.getCash());
    assertEquals(optionTotal, res.getOptionTotal());
    assertEquals(optionTotal + CASH, res.getFairValue());

    final VolatilitySwapCalculatorResultWithStrikes resStrikesFrom = res.withStrikes(PUT_STRIKES, CALL_STRIKES);
    assertEquals(resStrikes.hashCode(), resStrikesFrom.hashCode());
    assertEquals(resStrikes, resStrikesFrom);
  }

  /**
   * Equals and hashcode are tested
   */
  @Test
  public void hashEqualsTest() {
    final VolatilitySwapCalculatorResult res1 = new VolatilitySwapCalculatorResult(PUT_WEIGHTS, STRADDLE_WEIGHT, CALL_WEIGHTS, PUT_PRICES, STRADDLE_PRICE, CALL_PRICES, CASH);
    final VolatilitySwapCalculatorResult res2 = new VolatilitySwapCalculatorResult(PUT_WEIGHTS, STRADDLE_WEIGHT, CALL_WEIGHTS, PUT_PRICES, STRADDLE_PRICE, CALL_PRICES, CASH);
    final VolatilitySwapCalculatorResult res3 = new VolatilitySwapCalculatorResult(new double[] {0., 1., 1.5, }, STRADDLE_WEIGHT, CALL_WEIGHTS, PUT_PRICES, STRADDLE_PRICE, CALL_PRICES, CASH);
    final VolatilitySwapCalculatorResult res4 = new VolatilitySwapCalculatorResult(PUT_WEIGHTS, STRADDLE_WEIGHT + 2., CALL_WEIGHTS, PUT_PRICES, STRADDLE_PRICE, CALL_PRICES, CASH);
    final VolatilitySwapCalculatorResult res5 = new VolatilitySwapCalculatorResult(PUT_WEIGHTS, STRADDLE_WEIGHT, new double[] {1.5, }, PUT_PRICES, STRADDLE_PRICE, CALL_PRICES, CASH);
    final VolatilitySwapCalculatorResult res6 = new VolatilitySwapCalculatorResult(PUT_WEIGHTS, STRADDLE_WEIGHT, CALL_WEIGHTS, new double[] {1., 1., 1.5, }, STRADDLE_PRICE, CALL_PRICES, CASH);
    final VolatilitySwapCalculatorResult res7 = new VolatilitySwapCalculatorResult(PUT_WEIGHTS, STRADDLE_WEIGHT, CALL_WEIGHTS, PUT_PRICES, STRADDLE_PRICE + 1., CALL_PRICES, CASH);
    final VolatilitySwapCalculatorResult res8 = new VolatilitySwapCalculatorResult(PUT_WEIGHTS, STRADDLE_WEIGHT, CALL_WEIGHTS, PUT_PRICES, STRADDLE_PRICE, new double[] {2. }, CASH);
    final VolatilitySwapCalculatorResult res9 = new VolatilitySwapCalculatorResult(PUT_WEIGHTS, STRADDLE_WEIGHT, CALL_WEIGHTS, PUT_PRICES, STRADDLE_PRICE, CALL_PRICES, CASH + 1.);

    assertTrue(res1.equals(res1));

    assertTrue(res1.equals(res2));
    assertTrue(res2.equals(res1));
    assertEquals(res1.hashCode(), res2.hashCode());

    assertTrue(!res1.equals(res3));
    assertTrue(!res3.equals(res1));

    assertTrue(!res1.equals(res3));
    assertTrue(!res3.equals(res1));

    assertTrue(!res1.equals(res4));
    assertTrue(!res4.equals(res1));

    assertTrue(!res1.equals(res5));
    assertTrue(!res5.equals(res1));

    assertTrue(!res1.equals(res6));
    assertTrue(!res6.equals(res1));

    assertTrue(!res1.equals(res7));
    assertTrue(!res7.equals(res1));

    assertTrue(!res1.equals(res8));
    assertTrue(!res8.equals(res1));

    assertTrue(!res1.equals(res9));
    assertTrue(!res9.equals(res1));

    assertTrue(!res1.equals(null));
    assertTrue(!res1.equals(new CarrLeeSeasonedSyntheticVolatilitySwapCalculator()));

    final int size = 8;
    final List<VolatilitySwapCalculatorResult> list = new ArrayList<>(size);
    list.add(res2);
    list.add(res3);
    list.add(res4);
    list.add(res5);
    list.add(res6);
    list.add(res7);
    list.add(res8);
    list.add(res9);

    for (int i = 0; i < size; ++i) {
      if (res1.hashCode() != list.get(i).hashCode()) {
        assertTrue(!res1.equals(list.get(i)));
      }
    }

    final VolatilitySwapCalculatorResultWithStrikes resStrikesFrom1 = res1.withStrikes(PUT_STRIKES, CALL_STRIKES);
    final VolatilitySwapCalculatorResultWithStrikes resStrikesFrom3 = res3.withStrikes(PUT_STRIKES, CALL_STRIKES);
    final VolatilitySwapCalculatorResultWithStrikes resStrikesFrom11 = res1.withStrikes(PUT_STRIKES, CALL_WEIGHTS);
    final VolatilitySwapCalculatorResultWithStrikes resStrikesFrom12 = res1.withStrikes(PUT_WEIGHTS, CALL_STRIKES);

    assertTrue(resStrikesFrom1.equals(resStrikesFrom1));
    assertTrue(!resStrikesFrom1.equals(null));
    assertTrue(!resStrikesFrom1.equals(res1));

    assertTrue(!resStrikesFrom1.equals(resStrikesFrom3));
    assertTrue(!resStrikesFrom1.equals(resStrikesFrom11));
    assertTrue(!resStrikesFrom1.equals(resStrikesFrom12));
  }
}
