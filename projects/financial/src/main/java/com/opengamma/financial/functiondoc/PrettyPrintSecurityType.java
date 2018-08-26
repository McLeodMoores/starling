/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.functiondoc;

import java.util.HashMap;
import java.util.Map;

import com.opengamma.financial.security.bond.BondSecurity;
import com.opengamma.financial.security.capfloor.CapFloorCMSSpreadSecurity;
import com.opengamma.financial.security.capfloor.CapFloorSecurity;
import com.opengamma.financial.security.cash.CashSecurity;
import com.opengamma.financial.security.deposit.PeriodicZeroDepositSecurity;
import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.financial.security.equity.EquityVarianceSwapSecurity;
import com.opengamma.financial.security.forward.CommodityForwardSecurity;
import com.opengamma.financial.security.fra.FRASecurity;
import com.opengamma.financial.security.future.FutureSecurity;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.financial.security.fx.NonDeliverableFXForwardSecurity;
import com.opengamma.financial.security.option.BondFutureOptionSecurity;
import com.opengamma.financial.security.option.CommodityFutureOptionSecurity;
import com.opengamma.financial.security.option.EquityBarrierOptionSecurity;
import com.opengamma.financial.security.option.EquityIndexDividendFutureOptionSecurity;
import com.opengamma.financial.security.option.EquityIndexOptionSecurity;
import com.opengamma.financial.security.option.EquityOptionSecurity;
import com.opengamma.financial.security.option.FXBarrierOptionSecurity;
import com.opengamma.financial.security.option.FXDigitalOptionSecurity;
import com.opengamma.financial.security.option.FXOptionSecurity;
import com.opengamma.financial.security.option.IRFutureOptionSecurity;
import com.opengamma.financial.security.option.NonDeliverableFXDigitalOptionSecurity;
import com.opengamma.financial.security.option.NonDeliverableFXOptionSecurity;
import com.opengamma.financial.security.option.SwaptionSecurity;
import com.opengamma.financial.security.swap.SwapSecurity;

/**
 * Temporary measure for formatting an internal security type string into a
 * form that can be displayed in the documentation. Note that the web gui
 * requires the same behaviour and will get a proper implementation. When that
 * happens, use that and delete this class.
 */
public final class PrettyPrintSecurityType {

  private static final Map<String, String> DATA;

  static {
    DATA = new HashMap<String, String>();
    DATA.put(BondSecurity.SECURITY_TYPE, "Bond");
    DATA.put(BondFutureOptionSecurity.SECURITY_TYPE, "Bond Future Option");
    DATA.put(CapFloorSecurity.SECURITY_TYPE, "Cap/Floor");
    DATA.put(CapFloorCMSSpreadSecurity.SECURITY_TYPE, "Cap/Floor CMS Spread");
    DATA.put(CashSecurity.SECURITY_TYPE, "Cash");
    DATA.put(CommodityForwardSecurity.SECURITY_TYPE, "Commodity Forward");
    DATA.put(CommodityFutureOptionSecurity.SECURITY_TYPE, "Commodity Future Option");
    DATA.put(EquitySecurity.SECURITY_TYPE, "Equity");
    DATA.put(EquityBarrierOptionSecurity.SECURITY_TYPE, "Equity Barrier Option");
    DATA.put(EquityIndexOptionSecurity.SECURITY_TYPE, "Equity Index Option");
    DATA.put(EquityIndexDividendFutureOptionSecurity.SECURITY_TYPE, "Equity Index Future Option");
    DATA.put(EquityVarianceSwapSecurity.SECURITY_TYPE, "Equity Variance Swap");
    DATA.put(EquityOptionSecurity.SECURITY_TYPE, "Equity Option");
    DATA.put("EXTERNAL_SENSITIVITIES_SECURITY", "Externally Calculated Sensitivities");
    DATA.put(FRASecurity.SECURITY_TYPE, "FRA");
    DATA.put(FutureSecurity.SECURITY_TYPE, "Future");
    DATA.put(FXBarrierOptionSecurity.SECURITY_TYPE, "FX Barrier Option");
    DATA.put(FXDigitalOptionSecurity.SECURITY_TYPE, "FX Digital Option");
    DATA.put(FXForwardSecurity.SECURITY_TYPE, "FX Forward");
    DATA.put(FXOptionSecurity.SECURITY_TYPE, "FX Option");
    DATA.put(IRFutureOptionSecurity.SECURITY_TYPE, "IR Future Option");
    DATA.put(NonDeliverableFXDigitalOptionSecurity.SECURITY_TYPE, "Non-deliverable FX Digital Option");
    DATA.put(NonDeliverableFXOptionSecurity.SECURITY_TYPE, "Non-deliverable FX Option");
    DATA.put(NonDeliverableFXForwardSecurity.SECURITY_TYPE, "Non-deliverable FX Forward");
    DATA.put(PeriodicZeroDepositSecurity.SECURITY_TYPE, "Periodic Zero Deposit");
    DATA.put(SwapSecurity.SECURITY_TYPE, "Swap");
    DATA.put(SwaptionSecurity.SECURITY_TYPE, "Swaption");
  }

  private PrettyPrintSecurityType() {

  }

  public static String getTypeString(final String type) {
    final String value = DATA.get(type);
    if (value != null) {
      return value;
    }
    return type;
  }

}
