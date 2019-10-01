package com.opengamma.financial.analytics.test.unittest.dealstest;

import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.testng.annotations.Test;

import com.google.common.collect.Lists;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.instrument.cash.DepositZeroDefinition;
import com.opengamma.analytics.financial.instrument.fra.ForwardRateAgreementDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapDefinition;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.model.interestrate.curve.DiscountCurve;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.financial.analytics.conversion.FRASecurityConverterDeprecated;
import com.opengamma.financial.analytics.conversion.SwapSecurityConverterDeprecated;
import com.opengamma.financial.analytics.conversion.ZeroDepositConverter;
import com.opengamma.financial.analytics.test.IRCurveParser;
import com.opengamma.financial.analytics.test.IRSwapSecurity;
import com.opengamma.financial.analytics.test.IRSwapTradeParser;
import com.opengamma.financial.mock.AbstractMockSourcesTest;
import com.opengamma.util.ResourceUtils;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

/**
 * Unit tests for USD deals
 *
 * @deprecated Deprecated
 */
@Deprecated
@Test(groups = TestGroup.UNIT)
public class USDTest extends AbstractMockSourcesTest {
  private static final Logger LOGGER = LoggerFactory.getLogger(USDTest.class);
  private static final String CURRENCY = "USD";

  private static final String ON_NAME = "USD_FEDFUNDS_1D_ERS";
  private static final String ONE_MONTH_NAME = "USD_LIBOR_1M_ERS";
  private static final String THREE_MONTH_NAME = "USD_LIBOR_3M_ERS";
  private static final String SIX_MONTH_NAME = "USD_LIBOR_6M_ERS";
  private static final String DISCOUNTING_CURVE_NAME = "Discounting";
  private static final String FORWARD_1M_CURVE_NAME = "Forward 1M";
  private static final String FORWARD_3M_CURVE_NAME = "Forward 3M";
  private static final String FORWARD_6M_CURVE_NAME = "Forward 6M";

  private static final Currency CCY = Currency.USD;

  private static final String PAY_CURRENCY = "LEG1_CCY";

  public void test() throws Exception {
    final IRSwapTradeParser tradeParser = new IRSwapTradeParser();
    final Resource resource = ResourceUtils.createResource("classpath:com/opengamma/financial/analytics/test/Trades14Oct.csv");
    final List<IRSwapSecurity> trades = tradeParser.parseCSVFile(resource.getURL());
    final List<IRSwapSecurity> tradesClean = Lists.newArrayList();
    for (final IRSwapSecurity irSwapSecurity : trades) {

      final String currency = irSwapSecurity.getRawInput().getString(PAY_CURRENCY);
      if (currency.equals(CURRENCY)) {
        tradesClean.add(irSwapSecurity);
      }

    }
    // Build the curve bundle
    final HashMap<String, Currency> ccyMap = new HashMap<>();
    ccyMap.put(DISCOUNTING_CURVE_NAME, CCY);
    ccyMap.put(FORWARD_3M_CURVE_NAME, CCY);
    ccyMap.put(FORWARD_6M_CURVE_NAME, CCY);
    final FXMatrix fx = new FXMatrix(CCY);
    final YieldCurveBundle curvesClean = new YieldCurveBundle(fx, ccyMap);

    final IRCurveParser curveParser = new IRCurveParser();
    final Resource resourceCurve = ResourceUtils.createResource("classpath:com/opengamma/financial/analytics/test/Base_Curves_20131014_Clean.csv");
    final List<InterpolatedDoublesCurve> curves = curveParser.parseCSVFile(resourceCurve.getURL());

    for (final InterpolatedDoublesCurve interpolatedDoublesCurve : curves) {

      final String name = interpolatedDoublesCurve.getName();
      if (name.equals(ON_NAME)) {
        curvesClean.setCurve(DISCOUNTING_CURVE_NAME, DiscountCurve.from(interpolatedDoublesCurve));
      }
      if (name.equals(ONE_MONTH_NAME)) {
        curvesClean.setCurve(FORWARD_1M_CURVE_NAME, DiscountCurve.from(interpolatedDoublesCurve));
      }
      if (name.equals(THREE_MONTH_NAME)) {
        curvesClean.setCurve(FORWARD_3M_CURVE_NAME, DiscountCurve.from(interpolatedDoublesCurve));
      }
      if (name.equals(SIX_MONTH_NAME)) {
        curvesClean.setCurve(FORWARD_6M_CURVE_NAME, DiscountCurve.from(interpolatedDoublesCurve));
      }
    }

    // Convert the swap security into a swap definition
    final SwapSecurityConverterDeprecated swapConverter = new SwapSecurityConverterDeprecated(_holidaySource, _conventionBundleSource, _regionSource, false);
    final FRASecurityConverterDeprecated fraConverter = new FRASecurityConverterDeprecated(_holidaySource, _regionSource, _conventionBundleSource);
    final ZeroDepositConverter zeroCouponConverter = new ZeroDepositConverter(_conventionBundleSource, _holidaySource);
    final List<SwapDefinition> swapsDefinition = Lists.newArrayList();
    final List<ForwardRateAgreementDefinition> frasDefinition = Lists.newArrayList();
    final List<DepositZeroDefinition> zcsDefinition = Lists.newArrayList();
    /*
     * for (IRSwapSecurity irSwapSecurity : tradesClean) { switch (irSwapSecurity.getRawInput().getString("PRODUCT_TYPE")) { case "SWAP":
     * swapsDefinition.add((SwapDefinition) swapConverter.visitSwapSecurity(irSwapSecurity.getSwapSecurity()));
     *
     * // we don't treat the fra case at the moment case "FRA": frasDefinition.add((ForwardRateAgreementDefinition)
     * fraConverter.visitSwapSecurity(irSwapSecurity.getSwapSecurity())); case "OIS": swapsDefinition.add((SwapDefinition)
     * swapConverter.visitSwapSecurity(irSwapSecurity.getSwapSecurity()));
     *
     * // we don't treat the fra case at the moment case "ZCS": zcsDefinition.add((DepositZeroDefinition)
     * ZeroCouponConverter.visitSwapSecurity(irSwapSecurity.getSwapSecurity())); } }
     */
    // Load the historical time series from a csv file

    /*
     * NonVersionedRedisHistoricalTimeSeriesSource source = new NonVersionedRedisHistoricalTimeSeriesSource(getJedisPool(), getRedisPrefix());
     * CMECurveFixingTSLoader loader = new CMECurveFixingTSLoader(source);
     */
    /*
     * loader.loadCurveFixingCSVFile("/vols/ogdev/CME/curve-fixing/sample-cme-curve-fixing.csv");
     *
     * HistoricalTimeSeries historicalTimeSeries = source.getHistoricalTimeSeries(UniqueId.of(ExternalSchemes.ISDA.getName(), "CHF-LIBOR-BBA-6M"));
     * assertNotNull(historicalTimeSeries); LocalDateDoubleTimeSeries timeSeries = historicalTimeSeries.getTimeSeries(); assertNotNull(timeSeries);
     * assertEquals(5996, timeSeries.size());
     */

    // convert the definition into a derivative
    /*
     * List<Swap> swapDerivatives = Lists.newArrayList(); for (SwapDefinition swapDefinition : swapsDefinition) {
     * swapDerivatives.add(swapDefinition.toDerivative(TODAY, data, curvesClean)); } List<Swap> frasDerivatives = Lists.newArrayList(); for
     * (ForwardRateAgreementDefinition fraDefinition : frasDefinition) { frasDerivatives.add(fraDefinition.toDerivative(TODAY, data, curvesClean)); } List<Swap>
     * zcsDerivatives = Lists.newArrayList(); for (DepositZeroDefinition zcDefinition : zcsDefinition) { zcsDerivatives.add(zcDefinition.toDerivative(TODAY,
     * data, curvesClean)); }
     */

    // Check the npv

    LOGGER.warn("Got {} trades", trades.size());
  }
}
