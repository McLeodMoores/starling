/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.quandl.future;

import java.util.Objects;

import com.mcleodmoores.quandl.util.ArgumentChecker;
import com.opengamma.financial.analytics.ircurve.strips.DataFieldType;
import com.opengamma.id.ExternalId;

/**
 * An instrument provider for curves that generates Quandl codes for Federal funds futures. This provider contains
 * additional information about the underlying ticker and data field, which is used to get the Federal funds overnight
 * rate, which is required for pricing.
 */
public class QuandlFedFundsFutureCurveInstrumentProvider extends QuandlFutureCurveInstrumentProvider {
  /** The identifier of the underlying rate */
  private final ExternalId _underlyingId;
  /** The data field of the underlying rate */
  private final String _underlyingDataField;

  /**
   * Creates an instance.
   * @param futurePrefix  the future prefix, not null
   * @param dataField  the data field for the future, not null
   * @param fieldType  the field type for the future, not null
   * @param underlyingId  the underlying id, not null
   * @param underlyingDataField  the underlying data field, not null
   */
  public QuandlFedFundsFutureCurveInstrumentProvider(final String futurePrefix, final String dataField, final DataFieldType fieldType,
      final ExternalId underlyingId, final String underlyingDataField) {
    super(futurePrefix, dataField, fieldType);
    ArgumentChecker.notNull(underlyingId, "underlyingId");
    ArgumentChecker.notNull(underlyingDataField, "underlyingDataField");
    _underlyingId = underlyingId;
    _underlyingDataField = underlyingDataField;
  }

  /**
   * Gets the underlying id.
   * @return  the underlying id
   */
  public ExternalId getUnderlyingId() {
    return _underlyingId;
  }

  /**
   * Gets the underlying data field.
   * @return  the underlying data field
   */
  public String getUnderlyingDataField() {
    return _underlyingDataField;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + _underlyingDataField.hashCode();
    result = prime * result + _underlyingId.hashCode();
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (!(obj instanceof QuandlFedFundsFutureCurveInstrumentProvider)) {
      return false;
    }
    final QuandlFedFundsFutureCurveInstrumentProvider other = (QuandlFedFundsFutureCurveInstrumentProvider) obj;
    if (!Objects.equals(_underlyingDataField, other._underlyingDataField)) {
      return false;
    }
    if (!Objects.equals(_underlyingId, other._underlyingId)) {
      return false;
    }
    return true;
  }

}
