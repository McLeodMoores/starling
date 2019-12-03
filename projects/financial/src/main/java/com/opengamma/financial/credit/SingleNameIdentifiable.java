/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.financial.credit;

import org.threeten.bp.Period;

import com.opengamma.analytics.financial.credit.isdastandardmodel.StubType;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ObjectId;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.util.ArgumentChecker;

/**
 * Class holding properties of a single name.
 */
public class SingleNameIdentifiable implements UniqueIdentifiable, ObjectIdentifiable {

  /**
   * Scheme used for a single name.
   */
  public static final String SCHEME = "SingleName";
  /**
   * The separator.
   */
  private static final String SEPARATOR = "-";

  private final String _name;
  private final UniqueId _id;
  private final ExternalId _referenceEntity;
  private final BusinessDayConvention _badDayConvention;
  private final DayCount _daycount;
  private final Period _couponFrequency;
  private final StubType _stubType;

  public SingleNameIdentifiable(final String name,
                                final ExternalId referenceEntity,
                                final BusinessDayConvention badDayConvention,
                                final DayCount daycount,
                                final Period couponFrequency,
                                final StubType stubType) {
    _name = name;
    _referenceEntity = referenceEntity;
    _badDayConvention = badDayConvention;
    _daycount = daycount;
    _couponFrequency = couponFrequency;
    _stubType = stubType;
    _id = UniqueId.of(SCHEME, name + SEPARATOR + referenceEntity.getScheme() + SEPARATOR + badDayConvention.getName()
        + SEPARATOR + daycount.getName() + SEPARATOR + couponFrequency + SEPARATOR + stubType);
  }

  public static SingleNameIdentifiable of(final UniqueId id) {
    final String[] tokens = id.getValue().split(SEPARATOR);
    ArgumentChecker.isTrue(tokens.length == 6, "Incorrect number of params for SingleNameIdentifiable");
    final String name = tokens[0];
    final ExternalId reference = ExternalId.of(tokens[1], name);
    final BusinessDayConvention badDayConvention = BusinessDayConventionFactory.of(tokens[2]);
    final DayCount dayCount = DayCountFactory.of(tokens[3]);
    final Period couponFrequency = Period.parse(tokens[4]);
    final StubType stubType = StubType.valueOf(tokens[5]);
    return new SingleNameIdentifiable(name, reference, badDayConvention, dayCount, couponFrequency, stubType);
  }

  @Override
  public UniqueId getUniqueId() {
    return _id;
  }

  @Override
  public ObjectId getObjectId() {
    return getUniqueId().getObjectId();
  }

  public String getName() {
    return _name;
  }

  public ExternalId getReferenceEntity() {
    return _referenceEntity;
  }

  public BusinessDayConvention getBadDayConvention() {
    return _badDayConvention;
  }

  public DayCount getDaycount() {
    return _daycount;
  }

  public Period getCouponFrequency() {
    return _couponFrequency;
  }

  public StubType getStubType() {
    return _stubType;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    final SingleNameIdentifiable that = (SingleNameIdentifiable) o;

    if (_badDayConvention != null ? !_badDayConvention.equals(that._badDayConvention) : that._badDayConvention != null) {
      return false;
    }
    if (_couponFrequency != null ? !_couponFrequency.equals(that._couponFrequency) : that._couponFrequency != null) {
      return false;
    }
    if (_daycount != null ? !_daycount.equals(that._daycount) : that._daycount != null) {
      return false;
    }
    if (_id != null ? !_id.equals(that._id) : that._id != null) {
      return false;
    }
    if (_name != null ? !_name.equals(that._name) : that._name != null) {
      return false;
    }
    if (_referenceEntity != null ? !_referenceEntity.equals(that._referenceEntity) : that._referenceEntity != null) {
      return false;
    }
    if (_stubType != that._stubType) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result;
    result = _name != null ? _name.hashCode() : 0;
    result = 31 * result + (_id != null ? _id.hashCode() : 0);
    result = 31 * result + (_referenceEntity != null ? _referenceEntity.hashCode() : 0);
    result = 31 * result + (_badDayConvention != null ? _badDayConvention.hashCode() : 0);
    result = 31 * result + (_daycount != null ? _daycount.hashCode() : 0);
    result = 31 * result + (_couponFrequency != null ? _couponFrequency.hashCode() : 0);
    result = 31 * result + (_stubType != null ? _stubType.hashCode() : 0);
    return result;
  }
}
