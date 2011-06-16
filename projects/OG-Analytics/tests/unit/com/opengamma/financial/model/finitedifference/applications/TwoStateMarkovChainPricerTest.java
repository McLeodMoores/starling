/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.finitedifference.applications;

import org.testng.annotations.Test;

import com.opengamma.financial.model.finitedifference.ExponentialMeshing;
import com.opengamma.financial.model.finitedifference.HyperbolicMeshing;
import com.opengamma.financial.model.finitedifference.MarkovChain;
import com.opengamma.financial.model.finitedifference.MarkovChainApprox;
import com.opengamma.financial.model.finitedifference.MeshingFunction;
import com.opengamma.financial.model.finitedifference.PDEFullResults1D;
import com.opengamma.financial.model.finitedifference.PDEGrid1D;
import com.opengamma.financial.model.interestrate.curve.ForwardCurve;

/**
 * 
 */
public class TwoStateMarkovChainPricerTest {

  private static final double SPOT = 0.03;
  private static final double RATE = 0.0;
  private static final double T = 5.0;

  private static final double VOL1 = 0.15;
  private static final double VOL2 = 0.70;
  private static final double LAMBDA12 = 0.3;
  private static final double LAMBDA21 = 4.0;
  private static final double P0 = 1.0;
  private static final double BETA1 = 0.6;
  private static final double BETA2 = 0.6;

  private static final ForwardCurve FORWARD_CURVE;

  private static final TwoStateMarkovChainPricer PRICER;
  private static final MarkovChain CHAIN;
  private static final MarkovChainApprox CHAIN_APPROX;

  static {
    FORWARD_CURVE = new ForwardCurve(SPOT, RATE);

    PRICER = new TwoStateMarkovChainPricer(FORWARD_CURVE, VOL1, VOL2, LAMBDA12, LAMBDA21, P0, BETA1, BETA2);
    CHAIN = new MarkovChain(VOL1, VOL2, LAMBDA12, LAMBDA21, P0);
    CHAIN_APPROX = new MarkovChainApprox(VOL1, VOL2, LAMBDA12, LAMBDA21, P0, T);
  }

  @Test
  public void test() {
    int tNodes = 51;
    int xNodes = 151;
    MeshingFunction timeMesh = new ExponentialMeshing(0, T, tNodes, 7.5);
    MeshingFunction spaceMesh = new HyperbolicMeshing(0, 10 * SPOT, SPOT, xNodes, 0.01);
    PDEGrid1D grid = new PDEGrid1D(timeMesh, spaceMesh);
    PDEFullResults1D res = PRICER.solve(grid);

    double[] expiries = timeMesh.getPoints();
    double[] strikes = spaceMesh.getPoints();
    double[] forwards = new double[tNodes];
    double[] df = new double[tNodes];
    for (int i = 0; i < tNodes; i++) {

      forwards[i] = FORWARD_CURVE.getForward(expiries[i]);
    }

    // double[][] sims = CHAIN.simulate(timeMesh.getPoints(), 1000);
    //double[][] mcPrices = CHAIN.price(forwards,df,strikes,expiries,sims);
    double[] sims = CHAIN.simulate(T, 10000);
    for (int i = 0; i < xNodes; i++) {

      double mcPrice = CHAIN.priceCEV(FORWARD_CURVE.getForward(T), FORWARD_CURVE.getSpot() / FORWARD_CURVE.getForward(T),
          strikes[i], T, BETA1, sims);
      double approxPrice = CHAIN_APPROX.priceCEV(FORWARD_CURVE.getForward(T), FORWARD_CURVE.getSpot() / FORWARD_CURVE.getForward(T),
          strikes[i], BETA1);

      double price = res.getFunctionValue(i, tNodes - 1);

      System.out.println(strikes[i] + "\t" + mcPrice + "\t" + approxPrice + "\t" + price);
    }

    //  printResults(res);
  }

  private void printResults(PDEFullResults1D results) {
    int xNodes = results.getNumberSpaceNodes();
    int tNodes = results.getNumberTimeNodes();

    for (int i = 0; i < xNodes; i++) {
      System.out.print("\t" + results.getSpaceValue(i));
    }
    System.out.print("\n");

    for (int j = 0; j < tNodes; j++) {
      System.out.print(results.getTimeValue(j));
      for (int i = 0; i < xNodes; i++) {
        System.out.print("\t" + results.getFunctionValue(i, j));
      }
      System.out.print("\n");
    }
    System.out.print("\n");
  }

}
