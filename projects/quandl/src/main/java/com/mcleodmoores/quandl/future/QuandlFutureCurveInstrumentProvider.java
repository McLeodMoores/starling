/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.quandl.future;

import java.util.Objects;

import org.threeten.bp.LocalDate;
import org.threeten.bp.Period;

import com.mcleodmoores.quandl.QuandlConstants;
import com.opengamma.financial.analytics.ircurve.CurveInstrumentProvider;
import com.opengamma.financial.analytics.ircurve.IndexType;
import com.opengamma.financial.analytics.ircurve.strips.DataFieldType;
import com.opengamma.id.ExternalId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.time.Tenor;

/**
 * An instrument provider for curves that generates Quandl codes for futures. The codes take the form
 * <code>[Prefix][Month code][YYYY]</code> e.g. CME/EDZ2015.
 */
public class QuandlFutureCurveInstrumentProvider implements CurveInstrumentProvider {
  /** The future prefix */
  private final String _futurePrefix;
  /** The data field */
  private final String _dataField;
  /** The data field type */
  private final DataFieldType _fieldType;

  /**
   * Creates an instance.
   * @param futurePrefix  the future prefix, not null
   * @param dataField  the data field, not null
   * @param fieldType  the field type, not null
   */
  public QuandlFutureCurveInstrumentProvider(final String futurePrefix, final String dataField, final DataFieldType fieldType) {
    ArgumentChecker.notNull(futurePrefix, "futurePrefix");
    ArgumentChecker.notNull(dataField, "dataField");
    ArgumentChecker.notNull(fieldType, "fieldType");
    _futurePrefix = futurePrefix;
    _dataField = dataField;
    _fieldType = fieldType;
  }

  @Override
  public ExternalId getInstrument(final LocalDate date, final Tenor tenor) {
    throw new UnsupportedOperationException("Only futures supported");
  }

  @Override
  public ExternalId getInstrument(final LocalDate date, final Tenor tenor, final int nthFuture) {
    return getInstrument(date, Tenor.of(Period.ZERO), tenor, nthFuture);
  }

  @Override
  public ExternalId getInstrument(final LocalDate date, final Tenor startTenor, final Tenor futureTenor, final int nthFuture) {
    final StringBuilder futureCode = new StringBuilder(_futurePrefix);
    final LocalDate offsetDate = date.plus(startTenor.getPeriod());
    final String expiryCode = QuandlFutureUtils.getCodeForFuture(futureTenor, nthFuture, offsetDate);
    futureCode.append(expiryCode);
    return QuandlConstants.ofCode(futureCode.toString());
  }

  @Override
  public ExternalId getInstrument(final LocalDate date, final Tenor startTenor, final int startImmPeriods, final int endImmPeriods) {
    throw new UnsupportedOperationException("Only futures supported");
  }

  @Override
  public ExternalId getInstrument(final LocalDate date, final Tenor tenor, final int periodsPerYear, final boolean isPeriodicZeroDeposit) {
    throw new UnsupportedOperationException("Only futures supported");
  }

  @Override
  public ExternalId getInstrument(final LocalDate date, final Tenor tenor, final Tenor resetTenor, final IndexType indexType) {
    throw new UnsupportedOperationException("Only futures supported");
  }

  @Override
  public ExternalId getInstrument(final LocalDate date, final Tenor tenor, final Tenor payTenor, final Tenor receiveTenor,
      final IndexType payIndexType, final IndexType receiveIndexType) {
    throw new UnsupportedOperationException("Only futures supported");
  }

  /**
   * Gets the future prefix.
   * @return  the prefix
   */
  public String getFuturePrefix() {
    return _futurePrefix;
  }

  @Override
  public String getMarketDataField() {
    return _dataField;
  }

  @Override
  public DataFieldType getDataFieldType() {
    return _fieldType;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _dataField.hashCode();
    result = prime * result + _fieldType.hashCode();
    result = prime * result + _futurePrefix.hashCode();
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof QuandlFutureCurveInstrumentProvider)) {
      return false;
    }
    final QuandlFutureCurveInstrumentProvider other = (QuandlFutureCurveInstrumentProvider) obj;
    if (!Objects.equals(_futurePrefix, other._futurePrefix)) {
      return false;
    }
    if (!Objects.equals(_dataField, other._dataField)) {
      return false;
    }
    if (!Objects.equals(_fieldType, other._fieldType)) {
      return false;
    }
    return true;
  }


}
