/**
 * Copyright (C) 2020 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.analytics.examples.curveconstruction;

import java.io.PrintStream;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.threeten.bp.Month;
import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.temporal.ChronoUnit;

import com.mcleodmoores.analytics.financial.curve.interestrate.curvebuilder.CurveBuilder;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitorAdapter;
import com.opengamma.analytics.financial.instrument.cash.CashDefinition;
import com.opengamma.analytics.financial.instrument.cash.DepositIborDefinition;
import com.opengamma.analytics.financial.instrument.fra.ForwardRateAgreementDefinition;
import com.opengamma.analytics.financial.instrument.future.InterestRateFutureTransactionDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedONDefinition;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlock;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * Prints the output of curve construction.
 */
public class CurvePrintUtils {
  private static final DecimalFormat TIME_FORMAT = new DecimalFormat("0.000000");
  private static final DecimalFormat YIELD_FORMAT = new DecimalFormat("0.000000");
  private static final DecimalFormat JACOBIAN_FORMAT = new DecimalFormat("0.000000");

  /**
   * Prints a yield curve.
   *
   * @param out
   *          the output
   * @param curveName
   *          the curve name
   * @param curve
   *          the curve
   */
  public static void printAtNodes(final PrintStream out, final String curveName, final YieldAndDiscountCurve curve) {
    if (!(curve instanceof YieldCurve)) {
      throw new UnsupportedOperationException("Can only print YieldCurve");
    }
    if (!(((YieldCurve) curve).getCurve() instanceof InterpolatedDoublesCurve)) {
      throw new UnsupportedOperationException("Can only print interpolated curves");
    }
    out.println(curveName);
    out.print("\ttime (years)");
    final Double[] t = ((YieldCurve) curve).getCurve().getXData();
    Arrays.stream(t).forEach(e -> out.print("\t" + TIME_FORMAT.format(e)));
    out.print("\n\tyield (%)");
    Arrays.stream(t).forEach(e -> out.print("\t" + YIELD_FORMAT.format(curve.getInterestRate(e) * 100)));
    out.println();
    out.println();
  }

  /**
   * Prints a yield curve.
   *
   * @param out
   *          the output
   * @param curveName
   *          the curve name
   * @param curve
   *          the curve
   * @param start
   *          the start time
   * @param end
   *          the end time
   * @param delta
   *          the delta
   */
  public static void printCurve(final PrintStream out, final String curveName, final YieldAndDiscountCurve curve, final double start,
      final double end, final double delta) {
    out.println();
    IntStream.range(0, 1 + (int) ((end - start) / delta))
        .mapToObj(i -> Pairs.of(start + i * delta, curve.getInterestRate(start + i * delta)))
        .forEach(e -> out.println(e.getFirst() + "\t" + e.getSecond()));
    out.println();
  }

  /**
   * Prints the inverse Jacobians created during curve construction.
   *
   * @param out
   *          the output
   * @param inverseJacobians
   *          the Jacobians
   * @param curveBuilder
   *          the curve builder
   */
  public static void printJacobians(final PrintStream out, final CurveBuildingBlockBundle inverseJacobians, final CurveBuilder<?> curveBuilder) {
    final Map<String, List<String>> nodeNames = new HashMap<>();
    for (final Object objectEntry : curveBuilder.getNodes().entrySet()) {
      final Map.Entry<String, List<InstrumentDefinition<?>>> entry = (Map.Entry<String, List<InstrumentDefinition<?>>>) objectEntry;
      final List<String> names = entry.getValue().stream().map(e -> e.accept(NodeNameVisitor.INSTANCE)).collect(Collectors.toList());
      nodeNames.put(entry.getKey(), names);
    }
    final Map<String, Pair<CurveBuildingBlock, DoubleMatrix2D>> data = inverseJacobians.getData();
    data.entrySet().stream().forEach(e1 -> {
      final DoubleMatrix2D m = e1.getValue().getSecond();
      final Iterator<String> names = nodeNames.get(e1.getKey()).iterator();
      Arrays.stream(m.getData()).forEach(e2 -> {
        final String nodeName = names.next();
        out.print(nodeName);
        for (int i = 0; i < 20 - nodeName.length(); i++) {
          out.print(" ");
        }
        Arrays.stream(e2).map(e3 -> Math.abs(e3) < 1e-6 ? 0 : e3).forEach(e3 -> out.print("\t" + JACOBIAN_FORMAT.format(e3)));
        out.println();
      });
    });
  }

  private static class NodeNameVisitor extends InstrumentDefinitionVisitorAdapter<Void, String> {
    public static final NodeNameVisitor INSTANCE = new NodeNameVisitor();

    @Override
    public String visitCashDefinition(final CashDefinition cash) {
      return "CASH " + dateString(cash.getStartDate(), cash.getEndDate(), true);
    }

    @Override
    public String visitDepositIborDefinition(final DepositIborDefinition ibor) {
      return "IBOR " + dateString(ibor.getStartDate(), ibor.getEndDate(), true);
    }

    @Override
    public String visitSwapFixedIborDefinition(final SwapFixedIborDefinition swap) {
      return "IBOR SWAP " + dateString(swap.getFixedLeg().getNthPayment(0).getAccrualStartDate(),
          swap.getFixedLeg().getNthPayment(swap.getFixedLeg().getNumberOfPayments() - 1).getAccrualEndDate(), false);
    }

    @Override
    public String visitSwapDefinition(final SwapDefinition swap) {
      if (swap instanceof SwapFixedONDefinition) {
        final SwapFixedONDefinition ois = (SwapFixedONDefinition) swap;
        return "OIS " + dateString(ois.getFixedLeg().getNthPayment(0).getAccrualStartDate(),
            ois.getFixedLeg().getNthPayment(ois.getFixedLeg().getNumberOfPayments() - 1).getAccrualEndDate(), false);
      }
      throw new UnsupportedOperationException();
    }

    @Override
    public String visitForwardRateAgreementDefinition(final ForwardRateAgreementDefinition fra) {
      return dateString(ZonedDateTime.now(), fra.getFixingPeriodStartDate(), true) + "x"
          + dateString(ZonedDateTime.now(), fra.getFixingPeriodEndDate(), true) + " FRA";
    }

    @Override
    public String visitInterestRateFutureTransactionDefinition(final InterestRateFutureTransactionDefinition stirFuture) {
      final Month startMonth = stirFuture.getUnderlyingSecurity().getFixingPeriodStartDate().getMonth();
      final int year = stirFuture.getUnderlyingSecurity().getFixingPeriodStartDate().getYear();
      final String yearString = Integer.toString(year - 10 * (year / 10));
      switch (startMonth) {
        case JANUARY:
          return "F" + yearString;
        case FEBRUARY:
          return "G" + yearString;
        case MARCH:
          return "H" + yearString;
        case APRIL:
          return "J" + yearString;
        case MAY:
          return "K" + yearString;
        case JUNE:
          return "M" + yearString;
        case JULY:
          return "N" + yearString;
        case AUGUST:
          return "Q" + yearString;
        case SEPTEMBER:
          return "U" + yearString;
        case OCTOBER:
          return "V" + yearString;
        case NOVEMBER:
          return "X" + yearString;
        case DECEMBER:
          return "Z" + yearString;
        default:
          throw new IllegalStateException();
      }
    }

    private static String dateString(final ZonedDateTime startDate, final ZonedDateTime endDate, final boolean preferMonths) {
      final long months = ChronoUnit.MONTHS.between(startDate, endDate);
      if (months % 12 == 0 && !preferMonths) {
        final long years = ChronoUnit.YEARS.between(startDate, endDate);
        if (years > 0) {
          return Long.toString(years) + "Y";
        }
      }
      if (months > 0) {
        return Long.toString(months) + "M";
      }
      final long days = ChronoUnit.DAYS.between(startDate, endDate);
      return Long.toString(days) + "D";
    }
  }
}
