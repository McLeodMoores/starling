/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.financial.analytics.model.forex.option.black;

import java.util.HashSet;
import java.util.Set;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.forex.definition.ForexDefinition;
import com.opengamma.analytics.financial.forex.definition.ForexOptionVanillaDefinition;
import com.opengamma.analytics.financial.forex.derivative.ForexOptionVanilla;
import com.opengamma.analytics.financial.instrument.payment.PaymentFixedDefinition;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.financial.analytics.model.forex.FXUtils;
import com.opengamma.financial.security.option.BarrierDirection;
import com.opengamma.financial.security.option.BarrierType;
import com.opengamma.financial.security.option.FXBarrierOptionSecurity;
import com.opengamma.util.money.Currency;

/**
 *
 */
public final class OneLookBarrierOptionUtils {

  private OneLookBarrierOptionUtils() {}

  private static Set<ForexOptionVanilla> vanillaDecomposition(final FXBarrierOptionSecurity barrierSec,
      final double smoothingFullWidth, final double overhedge, final ZonedDateTime valTime, final Set<ValueRequirement> desiredValues) {

    final HashSet<ForexOptionVanilla> vanillas = new HashSet<>();
    // Unpack the barrier security
    final boolean isLong = barrierSec.getLongShort().isLong();
    final ZonedDateTime expiry = barrierSec.getExpiry().getExpiry();
    final ZonedDateTime settlement = barrierSec.getSettlementDate();

    // The barrier has four types
    final BarrierDirection bInOut = barrierSec.getBarrierDirection(); // KNOCK_IN, KNOCK_OUT,
    final BarrierType bUpDown = barrierSec.getBarrierType(); // UP, DOWN, DOUBLE
    final double barrier = barrierSec.getBarrierLevel();

    // Put and Call Amounts, along with market convention for quote/base ccy define the strike, notional, and call/put interpretation
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final String putCurveName = desiredValue.getConstraint("PUT_CURVE");
    final String callCurveName = desiredValue.getConstraint("CALL_CURVE");
    final double callAmt = barrierSec.getCallAmount();
    final Currency callCcy = barrierSec.getCallCurrency();
    final double putAmt = barrierSec.getPutAmount();
    final Currency putCcy = barrierSec.getPutCurrency();

    final boolean inOrder = FXUtils.isInBaseQuoteOrder(putCcy, callCcy);
    double baseAmt; // This is the Notional of the option if interpreted as N*max(w(X-K),0)
    double quoteAmt;
    Currency baseCcy;
    Currency quoteCcy; // This is the valuation currency in the (X,K) interpretation
    String baseCurveName;
    String quoteCurveName;
    boolean linearIsCall; //
    if (inOrder) {
      linearIsCall = false; // putCcy == baseCcy => Put
      baseAmt = putAmt;
      baseCcy = putCcy;
      baseCurveName = putCurveName + "_" + putCcy.getCode();
      quoteAmt = callAmt;
      quoteCcy = callCcy;
      quoteCurveName = callCurveName + "_" + callCcy.getCode();
    } else {
      linearIsCall = true; // callCcy == baseCcy => Call
      baseAmt = callAmt;
      baseCcy = callCcy;
      baseCurveName = callCurveName + "_" + callCcy.getCode();
      quoteAmt = putAmt;
      quoteCcy = putCcy;
      quoteCurveName = putCurveName + "_" + putCcy.getCode();
    }
    final double strike = quoteAmt / baseAmt;
    final String[] baseQuoteCurveNames = new String[] { baseCurveName, quoteCurveName };

    // parameters to model binary as call/put spread
    final double oh = overhedge;
    final double width = barrier * smoothingFullWidth;
    final double size;

    // There are four cases: UP and IN, UP and OUT, DOWN and IN, DOWN and OUT
    // Switch on direction: If UP, use Call Spreads. If DOWN, use Put spreads.
    boolean useCallSpread;
    double nearStrike;
    double farStrike;
    switch (bUpDown) {
      case UP:
        useCallSpread = true;
        if (!linearIsCall) {
          throw new OpenGammaRuntimeException(
              "ONE_LOOK Barriers do not apply to an UP type of Barrier unless the option itself is a Call. Check Call/Put currencies.");
        }
        if (barrier < strike) {
          throw new OpenGammaRuntimeException("Encountered an UP type of BarrierOption where barrier, " + barrier + ", is below strike, " + strike);
        }
        size = (barrier - strike) / width;
        nearStrike = barrier + oh - 0.5 * width;
        farStrike = barrier + oh + 0.5 * width;
        if (nearStrike < 0.0 || farStrike < 0.0) {
          throw new OpenGammaRuntimeException(
              "A strike in the put binary approximation is negative. Look at the BinaryOverhedge and BinarySmoothingFullWidth properties.");
        }
        break;
      case DOWN:
        useCallSpread = false;
        if (linearIsCall) {
          throw new OpenGammaRuntimeException(
              "ONE_LOOK Barriers do not apply to a DOWN type of Barrier unless the option itself is a Put. Check Call/Put currencies.");
        }
        if (barrier > strike) {
          throw new OpenGammaRuntimeException("Encountered a DOWN type of BarrierOption where barrier, " + barrier + ", is above strike, " + strike);
        }
        size = (strike - barrier) / width;
        nearStrike = barrier + oh + 0.5 * width;
        farStrike = barrier + oh - 0.5 * width;
        break;
      case DOUBLE:
        throw new OpenGammaRuntimeException("Encountered an EquityBarrierOption where barrierType is DOUBLE. This isn't yet handled.");
      default:
        throw new OpenGammaRuntimeException("Encountered an EquityBarrierOption with unexpected BarrierType of: " + bUpDown);
    }

    // ForexVanillaOption's are defined in terms of the underlying forward FX transaction, the exchange of two fixed amounts in different currencies.
    // The relative size of the payments implicitly defines the option's strike. So we will build a number of ForexDefinition's below
    final PaymentFixedDefinition quoteCcyPayment = new PaymentFixedDefinition(quoteCcy, settlement, -1 * quoteAmt);
    final PaymentFixedDefinition baseCcyPayment = new PaymentFixedDefinition(baseCcy, settlement, baseAmt);
    final ForexDefinition fxFwd = new ForexDefinition(baseCcyPayment, quoteCcyPayment); // This is what defines the strike, K = quoteAmt / baseAmt
    // We restrike an option by changing the underlying Forex, adjusting the Payments to match the formulae: k = A2/A1, N = A1.
    final ForexDefinition fxFwdForBarrier = new ForexDefinition(baseCcyPayment, new PaymentFixedDefinition(quoteCcy, settlement, -1 * barrier * baseAmt));

    // For the binaries, we do this by adjusting A1' = size * A1; A2' = A1' * newStrike as A1 is the Notional in this interpretation
    final double baseAmtForSpread = size * baseAmt;
    final PaymentFixedDefinition baseCcyPmtForSpread = new PaymentFixedDefinition(baseCcy, settlement, baseAmtForSpread);
    final ForexDefinition fxFwdForNearStrike = new ForexDefinition(baseCcyPmtForSpread,
        new PaymentFixedDefinition(quoteCcy, settlement, -1 * nearStrike * baseAmtForSpread));
    final ForexDefinition fxFwdForFarStrike = new ForexDefinition(baseCcyPmtForSpread,
        new PaymentFixedDefinition(quoteCcy, settlement, -1 * farStrike * baseAmtForSpread));

    // Switch on type
    switch (bInOut) {
      case KNOCK_OUT: // Long a linear at strike, short a linear at barrier, short a binary at barrier of size (barrier-strike)

        final ForexOptionVanillaDefinition longLinearK = new ForexOptionVanillaDefinition(fxFwd, expiry, useCallSpread, isLong);
        final ForexOptionVanillaDefinition shortLinearB = new ForexOptionVanillaDefinition(fxFwdForBarrier, expiry, useCallSpread, !isLong);
        vanillas.add(longLinearK.toDerivative(valTime, baseQuoteCurveNames));
        vanillas.add(shortLinearB.toDerivative(valTime, baseQuoteCurveNames));
        // Short a binary of size, barrier - strike. Modelled as call spread struck around strike + oh, with spread of 2*eps
        final ForexOptionVanillaDefinition shortNear = new ForexOptionVanillaDefinition(fxFwdForNearStrike, expiry, useCallSpread, !isLong);
        final ForexOptionVanillaDefinition longFar = new ForexOptionVanillaDefinition(fxFwdForFarStrike, expiry, useCallSpread, isLong);
        vanillas.add(shortNear.toDerivative(valTime, baseQuoteCurveNames));
        vanillas.add(longFar.toDerivative(valTime, baseQuoteCurveNames));
        break;

      case KNOCK_IN: // Long a linear at barrier, long a binary at barrier of size (barrier - strike)

        final ForexOptionVanillaDefinition longLinearB = new ForexOptionVanillaDefinition(fxFwdForBarrier, expiry, useCallSpread, isLong);
        vanillas.add(longLinearB.toDerivative(valTime, baseQuoteCurveNames));
        // Long a binary of size, barrier - strike. Modelled as call spread struck around strike + oh, with spread of 2*eps
        final ForexOptionVanillaDefinition longNear = new ForexOptionVanillaDefinition(fxFwdForNearStrike, expiry, useCallSpread, isLong);
        final ForexOptionVanillaDefinition shortFar = new ForexOptionVanillaDefinition(fxFwdForFarStrike, expiry, useCallSpread, !isLong);
        vanillas.add(longNear.toDerivative(valTime, baseQuoteCurveNames));
        vanillas.add(shortFar.toDerivative(valTime, baseQuoteCurveNames));
        break;
      default:
        throw new OpenGammaRuntimeException("Encountered an EquityBarrierOption with unexpected BarrierDirection of: " + bUpDown);
    }
    return vanillas;
  }
}
