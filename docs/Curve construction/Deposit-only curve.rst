=============
Deposit curve
=============

The code for this example can be found here_.

This example builds a single USD curve called *USD DEPOSIT* from cash deposit rates. The process is:

* decide on the model to be used
* define the type (shape) of the curve
* define the use of the curve
* add nodes to the curve
* build

model
    The model that will be used is a discounting model. The root-finder finds the spread over the deposit rate that     makes the cash instruments price to zero.

.. code-block:: java

    builder = DiscountingMethodCurveBuilder.setUp()

curve shape
    The curve is an interpolated curve, using a monotonic constrained cubic spline interpolator with flat left extrapolation and linear right extrapolation. 

.. code-block:: java

  interpolator = NamedInterpolator1dFactory.of(
      MonotonicConstrainedCubicSplineInterpolator1dAdapter.NAME,
      FlatExtrapolator1dAdapter.NAME,
      LinearExtrapolator1dAdapter.NAME);

curve use
    The curve will be used to discount all USD payments.

.. code-block:: java

  builder.building("USD DEPOSIT").using("USD DEPOSIT").forDiscounting(Currency.USD).withInterpolator(interpolator)

generate the nodal instruments
    The cash instruments can be created by calling CashDefinition_ directly. However, it's often easier to use an instrument generator, which stores the conventions used for all instruments and generates new instruments with different tenors and market data.

For example, instruments for this curve can be generated using

.. code-block:: java

  generator = CashGenerator.builder()
      .withCurrency(Currency.USD)
      .withBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING)
      .withCalendar(WeekendWorkingDayCalendar.SATURDAY_SUNDAY)
      .withDayCount(DayCounts.ACT_360)
      .withEndOfMonthConvention(EndOfMonthConvention.IGNORE_END_OF_MONTH)
      .withSpotLag(2)
      .build();

add nodes to the curve
    The nodes are then added to the builder:

.. code-block:: java

  builder.addNode("USD DEPOSIT", generator.toCurveInstrument(valuationDate, startTenor, endTenor, marketQuote))

=====


.. code-block:: java

  // valuation date/time
  private static final LocalDate VALUATION_DATE = LocalDate.now();
  private static final LocalTime VALUATION_TIME = LocalTime.of(9, 0);
  private static final ZoneId VALUATION_ZONE = ZoneId.of("Europe/London");
  // get the interpolator
  private static final Interpolator1D INTERPOLATOR = NamedInterpolator1dFactory.of(
      MonotonicConstrainedCubicSplineInterpolator1dAdapter.NAME,
      FlatExtrapolator1dAdapter.NAME,
      LinearExtrapolator1dAdapter.NAME);

  // contains conventions for USD deposit instruments and will generate instruments
  private static final CashGenerator CASH_CONVENTION = CashGenerator.builder()
      .withCurrency(Currency.USD)
      .withBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING)
      .withCalendar(WeekendWorkingDayCalendar.SATURDAY_SUNDAY)
      .withDayCount(DayCounts.ACT_360)
      .withEndOfMonthConvention(EndOfMonthConvention.IGNORE_END_OF_MONTH)
      .withSpotLag(2)
      .build();

  // the tenors of the deposit instruments that will be used
  private static final Tenor[] CURVE_TENORS = new Tenor[] {
      Tenor.ON,
      Tenor.ONE_WEEK,
      Tenor.TWO_WEEKS,
      Tenor.THREE_WEEKS,
      Tenor.ONE_MONTH,
      Tenor.TWO_MONTHS,
      Tenor.THREE_MONTHS,
      Tenor.FOUR_MONTHS,
      Tenor.FIVE_MONTHS,
      Tenor.SIX_MONTHS,
      Tenor.NINE_MONTHS,
      Tenor.ONE_YEAR,
      Tenor.TWO_YEARS,
      Tenor.THREE_YEARS,
      Tenor.FOUR_YEARS,
      Tenor.FIVE_YEARS };

  // the market quotes of the instruments
  private static final double[] MARKET_QUOTES = new double[] {
      0.002,
      0.003,
      0.0034,
      0.0036,
      0.004,
      0.0047,
      0.005,
      0.0052,
      0.0058,
      0.006,
      0.0079,
      0.01,
      0.013,
      0.017,
      0.02,
      0.026 };

  // the curve name
  private static final String CURVE_NAME = "USD DEPOSIT";

  public static void constructCurve() {
    final ZonedDateTime valuationDate = ZonedDateTime.of(VALUATION_DATE, VALUATION_TIME, VALUATION_ZONE);
    // first construct the builder
    final DiscountingMethodCurveSetUp curveBuilder = DiscountingMethodCurveBuilder.setUp()
        .building(CURVE_NAME)
        .using(CURVE_NAME).forDiscounting(Currency.USD).withInterpolator(INTERPOLATOR);
    // add the nodes to the builder
    IntStream.range(0, CURVE_TENORS.length).forEach(
        i -> curveBuilder.addNode(CURVE_NAME, 
                 CASH_CONVENTION.toCurveInstrument(valuationDate, 
                                                   Tenor.of(Period.ZERO), 
                                                   CURVE_TENORS[i], 
                                                   1, 
                                                   MARKET_QUOTES[i])));
    // build the curves
    final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> result = 
        curveBuilder.getBuilder().buildCurves(valuationDate);

The result is a data bundle that contains the curves and an object that contains sensitivity (sensitivity of the curve to the yield) information.

========
**The output**

|curve plot|

The yields at the nodes are shown in the table below.

USD DEPOSIT

+------------+----------+
|time (years)|yield (%) |	
+============+==========+
|0.002732    |0.005066  |
+------------+----------+	 	
|0.024590    |0.014734  |
+------------+----------+	 	
|0.043716    |0.021095  |	
+------------+----------+	 	
|0.062842	|0.032629   |
+------------+----------+	 	
|0.087432	|0.047739	|
+------------+----------+	 	
|0.172131	|0.099785   |	
+------------+----------+	 	
|0.254098	|0.150152   |	
+------------+----------+	 	
|0.344262	|0.205556   |
+------------+----------+	 	
|0.423497	|0.254244   |
+------------+----------+	 	
|0.505464	|0.304611   |	
+------------+----------+	 	
|0.757040	|0.459199   |
+------------+----------+	 	
|1.003616	|0.610714   |
+------------+----------+	 	
|2.003616	|1.225193   |
+------------+----------+	 	
|3.000876	|1.837988   |
+------------+----------+	 	
|4.005464	|2.455286   |
+------------+----------+	 	
|5.003616    |3.068628  |
+------------+----------+	 	
										


.. _here:

.. _CashDefinition: https://github.com/McLeodMoores/starling/blob/master/projects/analytics/src/main/java/com/opengamma/analytics/financial/instrument/cash/CashDefinition.java

.. image:: https://github.com/McLeodMoores/starling/blob/master/docs/Curve construction/usd_deposit.png