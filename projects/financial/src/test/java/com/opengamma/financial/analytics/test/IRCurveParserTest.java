package com.opengamma.financial.analytics.test;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.testng.annotations.Test;

import com.opengamma.analytics.financial.model.interestrate.curve.DayPeriodPreCalculatedDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.DiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.math.curve.DoublesCurve;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.factory.ClampedCubicSplineInterpolator1dAdapter;
import com.opengamma.analytics.math.interpolation.factory.LinearExtrapolator1dAdapter;
import com.opengamma.analytics.math.interpolation.factory.LogLinearExtrapolator1dAdapter;
import com.opengamma.analytics.math.interpolation.factory.MonotonicLogNaturalCubicSplineInterpolator1dAdapter;
import com.opengamma.analytics.math.interpolation.factory.NamedInterpolator1dFactory;
import com.opengamma.util.ResourceUtils;
import com.opengamma.util.test.TestGroup;

@Test(groups = TestGroup.UNIT)
public class IRCurveParserTest {
  private static final Logger LOGGER = LoggerFactory.getLogger(IRSwapTradeParserTest.class);

  public void test() throws Exception {
    final IRCurveParser curveParser = new IRCurveParser();
    final Resource resource = ResourceUtils.createResource("classpath:com/opengamma/financial/analytics/test/Base_Curves_20131014_Clean.csv");
    final List<InterpolatedDoublesCurve> curves = curveParser.parseCSVFile(resource.getURL());
    for (final InterpolatedDoublesCurve interpolatedDoublesCurve : curves) {


    }
    LOGGER.info("Got {} trades", curves.size());
  }

  @Test
  public void testInterpolation() {
    final double[] x = { 0.249144422, 0.501026694, 0.750171116, 0.999315537, 1.25119781, 1.500342231, 1.749486653};
    final double[] y = { 0.999297948, 0.998546826, 0.997720761, 0.996770227, 0.995642429, 0.994330655, 0.992795137 };

    final Interpolator1D interpolator = NamedInterpolator1dFactory.of(
        "LogNaturalCubicWithMonotonicity",
        "FlatExtrapolator",
        "LinearExtrapolator");

    final DoublesCurve doublesCurve = InterpolatedDoublesCurve.from(x, y, interpolator);

    final double value0 = doublesCurve.getYValue(0.0);
    final double value = doublesCurve.getYValue(.31);

    final double[] r = new double[y.length];
    for (int i = 0; i < r.length; i++) {
      r[i] = -Math.log(y[i]) / x[i];
    }
    final InterpolatedDoublesCurve curve = new InterpolatedDoublesCurve(x, y,
        NamedInterpolator1dFactory.of(MonotonicLogNaturalCubicSplineInterpolator1dAdapter.NAME, LogLinearExtrapolator1dAdapter.NAME,
            LinearExtrapolator1dAdapter.NAME),
        true, "curve");
    final double value2 = curve.getYValue(.31);

    final InterpolatedDoublesCurve curve2 = new InterpolatedDoublesCurve(x, r,
        NamedInterpolator1dFactory.of(ClampedCubicSplineInterpolator1dAdapter.NAME, LogLinearExtrapolator1dAdapter.NAME,
            LinearExtrapolator1dAdapter.NAME),
        true, "curve");

    final double value3 = Math.exp(-curve2.getYValue(.31) * .31);

    final InterpolatedDoublesCurve curveForYieldCurve = new InterpolatedDoublesCurve(x, y,
        NamedInterpolator1dFactory.of(MonotonicLogNaturalCubicSplineInterpolator1dAdapter.NAME, LogLinearExtrapolator1dAdapter.NAME,
            LinearExtrapolator1dAdapter.NAME),
        true, "curve");
    final YieldAndDiscountCurve yieldcurve = DiscountCurve.from(curveForYieldCurve);
    final double value4 = yieldcurve.getDiscountFactor(.31);

    final DayPeriodPreCalculatedDiscountCurve discountCurve = new DayPeriodPreCalculatedDiscountCurve("", curveForYieldCurve);
    final double value5 = discountCurve.getDiscountFactor(.31);
  }

}
