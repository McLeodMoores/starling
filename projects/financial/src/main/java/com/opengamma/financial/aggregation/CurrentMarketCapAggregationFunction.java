/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.aggregation;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.threeten.bp.LocalDate;

import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.core.position.Position;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityVisitor;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.financial.security.option.EquityOptionSecurity;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.util.CompareUtils;
import com.opengamma.util.tuple.Pair;

/**
 * Function to classify positions by Currency.
 *
 */
public class CurrentMarketCapAggregationFunction implements AggregationFunction<String> {

  private final boolean _useAttributes;
  private static final String NAME = "Market Cap";
  private static final String FIELD = "CUR_MKT_CAP";
  private static final String RESOLUTION_KEY = "DEFAULT_TSS_CONFIG";
  private static final String NO_CUR_MKT_CAP = "N/A";

  private static final double NANO_CAP_UPPER_THRESHOLD = 10;
  private static final double MICRO_CAP_UPPER_THRESHOLD = 100;
  private static final double SMALL_CAP_UPPER_THRESHOLD = 1000;
  private static final double MID_CAP_UPPER_THRESHOLD = 10E3;
  //private static final double LARGE_CAP_UPPER_THRESHOLD = 100E3;

  private static final String NANO_CAP = "Nano Cap";
  private static final String MICRO_CAP = "Micro Cap";
  private static final String SMALL_CAP = "Small Cap";
  private static final String MID_CAP = "Mid Cap";
  private static final String LARGE_CAP = "Large Cap";

  private static final List<String> REQUIRED = Arrays.asList(LARGE_CAP, MID_CAP, SMALL_CAP, MICRO_CAP, NANO_CAP, NO_CUR_MKT_CAP);

  private final HistoricalTimeSeriesSource _htsSource;
  private final SecuritySource _secSource;
  private final boolean _caching = true;
  private final Map<UniqueId, Double> _currMktCapCache = new HashMap<>();

  public CurrentMarketCapAggregationFunction(final SecuritySource secSource, final HistoricalTimeSeriesSource htsSource, final boolean useAttributes) {
    _secSource = secSource;
    _htsSource = htsSource;
    _useAttributes = useAttributes;
  }


  public CurrentMarketCapAggregationFunction(final SecuritySource secSource, final HistoricalTimeSeriesSource htsSource) {
    this(secSource, htsSource, false);
  }

  private final FinancialSecurityVisitor<Double> _equitySecurityVisitor = new FinancialSecurityVisitorAdapter<Double>() {
    @Override
    public Double visitEquitySecurity(final EquitySecurity security) {
      return getCurrentMarketCap(security);
    }
  };

  private final FinancialSecurityVisitor<Double> _equityOptionSecurityVisitor = new FinancialSecurityVisitorAdapter<Double>() {
    @Override
    public Double visitEquityOptionSecurity(final EquityOptionSecurity security) {
      final EquitySecurity underlying = (EquitySecurity) _secSource.get(ExternalIdBundle.of(security.getUnderlyingId()));
      return getCurrentMarketCap(underlying);
    }
  };

  protected Double getCurrentMarketCap(final Security security) {
    try {
      if (_caching && security.getUniqueId() != null) {
        if (_currMktCapCache.containsKey(security.getUniqueId())) {
          return _currMktCapCache.get(security.getUniqueId());
        }
      }
      final ExternalIdBundle externalIdBundle = security.getExternalIdBundle();
      final Pair<LocalDate, Double> latest = _htsSource.getLatestDataPoint(FIELD, externalIdBundle, RESOLUTION_KEY);
      if (latest != null && latest.getSecond() != null) {
        _currMktCapCache.put(security.getUniqueId(), latest.getSecond());
        return latest.getSecond();
      }
      _currMktCapCache.put(security.getUniqueId(), null);
      return null;
    } catch (final UnsupportedOperationException ex) {
      return null;
    }
  }

  private String getCurrentMarketCapCategory(final Double currentMarketCap) {
    if (currentMarketCap != null) {
      if (currentMarketCap < NANO_CAP_UPPER_THRESHOLD) {
        return NANO_CAP;
      } else if (currentMarketCap < MICRO_CAP_UPPER_THRESHOLD) {
        return MICRO_CAP;
      } else if (currentMarketCap < SMALL_CAP_UPPER_THRESHOLD) {
        return SMALL_CAP;
      } else if (currentMarketCap < MID_CAP_UPPER_THRESHOLD) {
        return MID_CAP;
      } else {
        return LARGE_CAP;
      }
    }
    return NO_CUR_MKT_CAP;
  }

  @Override
  public String classifyPosition(final Position position) {
    if (_useAttributes) {
      final Map<String, String> attributes = position.getAttributes();
      if (attributes.containsKey(getName())) {
        return attributes.get(getName());
      }
      return NO_CUR_MKT_CAP;
    }
    final FinancialSecurityVisitor<Double> visitorAdapter = FinancialSecurityVisitorAdapter.<Double> builder().equitySecurityVisitor(_equitySecurityVisitor)
        .equityOptionVisitor(_equityOptionSecurityVisitor).create();
    final FinancialSecurity security = (FinancialSecurity) position.getSecurityLink().resolve(_secSource);
    final Double currMarketCap = security.accept(visitorAdapter);
    final String classification = getCurrentMarketCapCategory(currMarketCap);
    return classification == null ? NO_CUR_MKT_CAP : classification;
  }

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public Collection<String> getRequiredEntries() {
    return REQUIRED;
  }


  @Override
  public int compare(final String marketCapBucket1, final String marketCapBucket2) {
    return CompareUtils.compareByList(REQUIRED, marketCapBucket1, marketCapBucket2);
  }

  private class PositionComparator implements Comparator<Position> {
    @Override
    public int compare(final Position position1, final Position position2) {
      final FinancialSecurityVisitor<Double> visitorAdapter = FinancialSecurityVisitorAdapter.<Double>builder()
          .equitySecurityVisitor(_equitySecurityVisitor)
          .equityOptionVisitor(_equityOptionSecurityVisitor)
          .create();
      final FinancialSecurity security1 = (FinancialSecurity) position1.getSecurityLink().resolve(_secSource);
      final FinancialSecurity security2 = (FinancialSecurity) position2.getSecurityLink().resolve(_secSource);
      final Double currMktCap1 = security1.accept(visitorAdapter);
      final Double currMktCap2 = security2.accept(visitorAdapter);
      return CompareUtils.compareWithNullLow(currMktCap1, currMktCap2);
    }
  }


  @Override
  public Comparator<Position> getPositionComparator() {
    return new PositionComparator();
  }

}
