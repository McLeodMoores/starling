/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.analytics.financial.data;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.joda.beans.Bean;
import org.joda.beans.BeanDefinition;
import org.joda.beans.ImmutableBean;
import org.joda.beans.ImmutableConstructor;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectFieldsBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaBean;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.primitives.Doubles;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantYieldCurve;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Wrapper for {@link ISDACompliantYieldCurve}s.
 */
@BeanDefinition
public class IsdaCurveProvider extends MulticurveProviderDiscount implements ImmutableBean {

  public static IsdaCurveProvider of(final Map<Currency, ISDACompliantYieldCurve> discountingCurves, final Map<IborIndex, ISDACompliantYieldCurve> iborCurves,
      final Map<IndexON, ISDACompliantYieldCurve> overnightCurves) {
    return new IsdaCurveProvider(discountingCurves, iborCurves, overnightCurves);
  }

  /** The discounting curves. */
  @PropertyDefinition
  private final Map<Currency, YieldAndDiscountCurve> _discountingCurves;
  /** The ibor index curves. */
  @PropertyDefinition
  private final Map<IborIndex, YieldAndDiscountCurve> _iborCurves;
  /** The overnight curves. */
  @PropertyDefinition
  private final Map<IndexON, YieldAndDiscountCurve> _overnightCurves;
  /** The curve names. */
  @PropertyDefinition
  private final Set<String> _curveNames;
  /** All curves. */
  @PropertyDefinition
  private final Map<String, YieldAndDiscountCurve> _allCurves;

  private IsdaCurveProvider(final Map<Currency, ISDACompliantYieldCurve> discountingCurves, final Map<IborIndex, ISDACompliantYieldCurve> iborCurves,
      final Map<IndexON, ISDACompliantYieldCurve> overnightCurves) {
    final Set<String> curveNames = new HashSet<>();
    final Map<String, YieldAndDiscountCurve> allCurves = new HashMap<>();
    if (discountingCurves == null) {
      _discountingCurves = Collections.unmodifiableMap(Collections.<Currency, YieldAndDiscountCurve> emptyMap());
    } else {
      final Map<Currency, YieldAndDiscountCurve> map = new LinkedHashMap<>();
      for (final Map.Entry<Currency, ISDACompliantYieldCurve> entry : discountingCurves.entrySet()) {
        final YieldAndDiscountCurve curve = IsdaCompliantYieldCurveAdapter.of(entry.getValue());
        map.put(entry.getKey(), curve);
        curveNames.add(curve.getName());
        allCurves.put(curve.getName(), curve);
      }
      _discountingCurves = Collections.unmodifiableMap(map);
    }
    if (iborCurves == null) {
      _iborCurves = Collections.unmodifiableMap(Collections.<IborIndex, YieldAndDiscountCurve> emptyMap());
    } else {
      final Map<IborIndex, YieldAndDiscountCurve> map = new LinkedHashMap<>();
      for (final Map.Entry<IborIndex, ISDACompliantYieldCurve> entry : iborCurves.entrySet()) {
        final YieldAndDiscountCurve curve = IsdaCompliantYieldCurveAdapter.of(entry.getValue());
        map.put(entry.getKey(), curve);
        curveNames.add(entry.getValue().getName());
        allCurves.put(curve.getName(), curve);
      }
      _iborCurves = Collections.unmodifiableMap(map);
    }
    if (overnightCurves == null) {
      _overnightCurves = Collections.unmodifiableMap(Collections.<IndexON, YieldAndDiscountCurve> emptyMap());
    } else {
      final Map<IndexON, YieldAndDiscountCurve> map = new LinkedHashMap<>();
      for (final Map.Entry<IndexON, ISDACompliantYieldCurve> entry : overnightCurves.entrySet()) {
        final YieldAndDiscountCurve curve = IsdaCompliantYieldCurveAdapter.of(entry.getValue());
        map.put(entry.getKey(), curve);
        curveNames.add(entry.getValue().getName());
        allCurves.put(curve.getName(), curve);
      }
      _overnightCurves = Collections.unmodifiableMap(map);
    }
    _curveNames = Collections.unmodifiableSet(curveNames);
    _allCurves = Collections.unmodifiableMap(allCurves);
  }

  @ImmutableConstructor
  private IsdaCurveProvider(final Map<Currency, YieldAndDiscountCurve> discountingCurves, final Map<IborIndex, YieldAndDiscountCurve> iborCurves,
      final Map<IndexON, YieldAndDiscountCurve> overnightCurves, final Set<String> curveNames, final Map<String, YieldAndDiscountCurve> allCurves) {
    _discountingCurves = Collections.unmodifiableMap(discountingCurves);
    _iborCurves = Collections.unmodifiableMap(iborCurves);
    _overnightCurves = Collections.unmodifiableMap(overnightCurves);
    _curveNames = Collections.unmodifiableSet(curveNames);
    _allCurves = Collections.unmodifiableMap(allCurves);
  }

  public ISDACompliantYieldCurve getIsdaDiscountingCurve(final Currency currency) {
    final YieldAndDiscountCurve curve = _discountingCurves.get(currency);
    if (curve != null && curve instanceof IsdaCompliantYieldCurveAdapter) {
      return ((IsdaCompliantYieldCurveAdapter) curve).getUnderlying();
    }
    throw new IllegalArgumentException("Could not get ISDA yield curve for " + currency);
  }

  public ISDACompliantYieldCurve getIsdaIborCurve(final IborIndex index) {
    final YieldAndDiscountCurve curve = _iborCurves.get(index);
    if (curve != null && curve instanceof IsdaCompliantYieldCurveAdapter) {
      return ((IsdaCompliantYieldCurveAdapter) curve).getUnderlying();
    }
    throw new IllegalArgumentException("Could not get ISDA yield curve for " + index);
  }

  public ISDACompliantYieldCurve getIsdaOvernightCurve(final IndexON index) {
    final YieldAndDiscountCurve curve = _overnightCurves.get(index);
    if (curve != null && curve instanceof IsdaCompliantYieldCurveAdapter) {
      return ((IsdaCompliantYieldCurveAdapter) curve).getUnderlying();
    }
    throw new IllegalArgumentException("Could not get ISDA yield curve for " + index);
  }

  @Override
  public MulticurveProviderInterface getMulticurveProvider() {
    return this;
  }

  // @Override
  // public double[] parameterSensitivity(final String name, final List<DoublesPair> pointSensitivity) {
  // final YieldAndDiscountCurve curve = _allCurves.get(name);
  // if (curve == null) {
  // throw new UnsupportedOperationException("Cannot get sensitivities for curve called " + name);
  // }
  // final int nbParameters = curve.getNumberOfParameters();
  // final double[] result = new double[nbParameters];
  // if (pointSensitivity != null && pointSensitivity.size() > 0) {
  // for (final DoublesPair timeAndS : pointSensitivity) {
  // final double[] sensi1Point = curve.getInterestRateParameterSensitivity(timeAndS.getFirst());
  // for (int i = 0; i < nbParameters; i++) {
  // result[i] += timeAndS.getSecond() * sensi1Point[i];
  // }
  // }
  // }
  // return result;
  // }
  //
  // @Override
  // public double[] parameterForwardSensitivity(final String name, final List<ForwardSensitivity> pointSensitivity) {
  // final YieldAndDiscountCurve curve = _allCurves.get(name);
  // if (curve == null) {
  // throw new UnsupportedOperationException("Cannot get sensitivities for curve called " + name);
  // }
  // final int nbParameters = curve.getNumberOfParameters();
  // final double[] result = new double[nbParameters];
  // if (pointSensitivity != null && pointSensitivity.size() > 0) {
  // for (final ForwardSensitivity timeAndS : pointSensitivity) {
  // final double startTime = timeAndS.getStartTime();
  // final double endTime = timeAndS.getEndTime();
  // final double forwardBar = timeAndS.getValue();
  // // Implementation note: only the sensitivity to the forward is available. The sensitivity to the pseudo-discount factors need to be computed.
  // final double dfForwardStart = curve.getDiscountFactor(startTime);
  // final double dfForwardEnd = curve.getDiscountFactor(endTime);
  // final double dFwddyStart = timeAndS.derivativeToYieldStart(dfForwardStart, dfForwardEnd);
  // final double dFwddyEnd = timeAndS.derivativeToYieldEnd(dfForwardStart, dfForwardEnd);
  // final double[] sensiPtStart = curve.getInterestRateParameterSensitivity(startTime);
  // final double[] sensiPtEnd = curve.getInterestRateParameterSensitivity(endTime);
  // for (int i = 0; i < nbParameters; i++) {
  // result[i] += dFwddyStart * sensiPtStart[i] * forwardBar;
  // result[i] += dFwddyEnd * sensiPtEnd[i] * forwardBar;
  // }
  // }
  // }
  // return result;
  // }

  @Override
  public Set<String> getAllCurveNames() {
    return _curveNames;
  }

  @Override
  public IsdaCurveProvider copy() {
    return new IsdaCurveProvider(_discountingCurves, _iborCurves, _overnightCurves, _curveNames, _allCurves);
  }

  @Override
  public double getDiscountFactor(final Currency ccy, final Double time) {
    final YieldAndDiscountCurve curve = _discountingCurves.get(ccy);
    if (curve == null) {
      throw new IllegalArgumentException("Could not get discounting curve for " + ccy);
    }
    return curve.getDiscountFactor(time);
  }

  @Override
  public double getInvestmentFactor(final IborIndex index, final double startTime, final double endTime, final double accrualFactor) {
    final YieldAndDiscountCurve curve = _iborCurves.get(index);
    if (curve == null) {
      throw new IllegalArgumentException("Could not get ibor index curve for " + index);
    }
    return curve.getDiscountFactor(startTime) / curve.getDiscountFactor(endTime);
  }

  @Override
  public double getSimplyCompoundForwardRate(final IborIndex index, final double startTime, final double endTime, final double accrualFactor) {
    final YieldAndDiscountCurve curve = _iborCurves.get(index);
    if (curve == null) {
      throw new IllegalArgumentException("Could not get ibor index curve for " + index);
    }
    return (curve.getDiscountFactor(startTime) / curve.getDiscountFactor(endTime) - 1) / accrualFactor;
  }

  @Override
  public double getSimplyCompoundForwardRate(final IborIndex index, final double startTime, final double endTime) {
    ArgumentChecker.isFalse(Doubles.compare(startTime, endTime) == 0, "Start time should be different from end time");
    final YieldAndDiscountCurve curve = _iborCurves.get(index);
    if (curve == null) {
      throw new IllegalArgumentException("Could not get ibor index curve for " + index);
    }
    final double accrualFactor = endTime - startTime;
    return getSimplyCompoundForwardRate(index, startTime, endTime, accrualFactor);
  }

  @Override
  public double getAnnuallyCompoundForwardRate(final IborIndex index, final double startTime, final double endTime, final double accrualFactor) {
    final YieldAndDiscountCurve curve = _iborCurves.get(index);
    if (curve == null) {
      throw new IllegalArgumentException("Could not get ibor index curve for " + index);
    }
    return Math.pow(curve.getDiscountFactor(startTime) / curve.getDiscountFactor(endTime), 1 / accrualFactor) - 1;
  }

  @Override
  public double getAnnuallyCompoundForwardRate(final IborIndex index, final double startTime, final double endTime) {
    ArgumentChecker.isFalse(Doubles.compare(startTime, endTime) == 0, "Start time should be different from end time");
    final YieldAndDiscountCurve curve = _iborCurves.get(index);
    if (curve == null) {
      throw new IllegalArgumentException("Could not get ibor index curve for " + index);
    }
    final double accrualFactor = endTime - startTime;
    return getAnnuallyCompoundForwardRate(index, startTime, endTime, accrualFactor);
  }

  @Override
  public double getInvestmentFactor(final IndexON index, final double startTime, final double endTime, final double accrualFactor) {
    final YieldAndDiscountCurve curve = _overnightCurves.get(index);
    if (curve == null) {
      throw new IllegalArgumentException("Could not get overnight index curve for " + index);
    }
    return curve.getDiscountFactor(startTime) / curve.getDiscountFactor(endTime);
  }

  @Override
  public double getSimplyCompoundForwardRate(final IndexON index, final double startTime, final double endTime, final double accrualFactor) {
    final YieldAndDiscountCurve curve = _overnightCurves.get(index);
    if (curve == null) {
      throw new IllegalArgumentException("Could not get overnight index curve for " + index);
    }
    return (curve.getDiscountFactor(startTime) / curve.getDiscountFactor(endTime) - 1) / accrualFactor;
  }

  @Override
  public double getSimplyCompoundForwardRate(final IndexON index, final double startTime, final double endTime) {
    ArgumentChecker.isFalse(Doubles.compare(startTime, endTime) == 0, "Start time should be different from end time");
    final YieldAndDiscountCurve curve = _overnightCurves.get(index);
    if (curve == null) {
      throw new IllegalArgumentException("Could not get overnight index curve for " + index);
    }
    final double accrualFactor = endTime - startTime;
    return getSimplyCompoundForwardRate(index, startTime, endTime, accrualFactor);
  }

  @Override
  public double getAnnuallyCompoundForwardRate(final IndexON index, final double startTime, final double endTime, final double accrualFactor) {
    final YieldAndDiscountCurve curve = _overnightCurves.get(index);
    if (curve == null) {
      throw new IllegalArgumentException("Could not get overnight index curve for " + index);
    }
    return Math.pow(curve.getDiscountFactor(startTime) / curve.getDiscountFactor(endTime), 1 / accrualFactor) - 1;
  }

  @Override
  public double getAnnuallyCompoundForwardRate(final IndexON index, final double startTime, final double endTime) {
    ArgumentChecker.isFalse(Doubles.compare(startTime, endTime) == 0, "Start time should be different from end time");
    final YieldAndDiscountCurve curve = _overnightCurves.get(index);
    if (curve == null) {
      throw new IllegalArgumentException("Could not get overnight index curve for " + index);
    }
    final double accrualFactor = endTime - startTime;
    return getAnnuallyCompoundForwardRate(index, startTime, endTime, accrualFactor);
  }

  @Override
  public double getFxRate(final Currency ccy1, final Currency ccy2) {
    throw new UnsupportedOperationException();
  }

  @Override
  public YieldAndDiscountCurve getCurve(final String name) {
    return _allCurves.get(name);
  }

  @Override
  public Integer getNumberOfParameters(final String name) {
    final YieldAndDiscountCurve curve = getCurve(name);
    if (curve == null) {
      throw new IllegalArgumentException("Could not get curve called " + name);
    }
    return curve.getNumberOfParameters();
  }

  @Override
  public List<String> getUnderlyingCurvesNames(final String name) {
    return Collections.singletonList(name);
  }

  @Override
  public String getName(final Currency ccy) {
    final YieldAndDiscountCurve curve = _discountingCurves.get(ccy);
    if (curve == null) {
      throw new IllegalArgumentException("Could not get discounting curve for " + ccy);
    }
    return curve.getName();
  }

  @Override
  public Set<Currency> getCurrencies() {
    return _discountingCurves.keySet();
  }

  @Override
  public String getName(final IborIndex index) {
    final YieldAndDiscountCurve curve = _iborCurves.get(index);
    if (curve == null) {
      throw new IllegalArgumentException("Could not get ibor curve for " + index);
    }
    return curve.getName();
  }

  @Override
  public Set<IborIndex> getIndexesIbor() {
    return _iborCurves.keySet();
  }

  @Override
  public String getName(final IndexON index) {
    final YieldAndDiscountCurve curve = _overnightCurves.get(index);
    if (curve == null) {
      throw new IllegalArgumentException("Could not get overnight curve for " + index);
    }
    return curve.getName();
  }

  @Override
  public Set<IndexON> getIndexesON() {
    return _overnightCurves.keySet();
  }

  @Override
  public FXMatrix getFxRates() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Set<String> getAllNames() {
    return _curveNames;
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code IsdaCurveProvider}.
   * @return the meta-bean, not null
   */
  public static IsdaCurveProvider.Meta meta() {
    return IsdaCurveProvider.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(IsdaCurveProvider.Meta.INSTANCE);
  }

  /**
   * Returns a builder used to create an instance of the bean.
   * @return the builder, not null
   */
  public static IsdaCurveProvider.Builder builder() {
    return new IsdaCurveProvider.Builder();
  }

  @Override
  public IsdaCurveProvider.Meta metaBean() {
    return IsdaCurveProvider.Meta.INSTANCE;
  }

  @Override
  public <R> Property<R> property(String propertyName) {
    return metaBean().<R>metaProperty(propertyName).createProperty(this);
  }

  @Override
  public Set<String> propertyNames() {
    return metaBean().metaPropertyMap().keySet();
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the discounting curves.
   * @return the value of the property
   */
  public Map<Currency, YieldAndDiscountCurve> getDiscountingCurves() {
    return _discountingCurves;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the ibor index curves.
   * @return the value of the property
   */
  public Map<IborIndex, YieldAndDiscountCurve> getIborCurves() {
    return _iborCurves;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the overnight curves.
   * @return the value of the property
   */
  public Map<IndexON, YieldAndDiscountCurve> getOvernightCurves() {
    return _overnightCurves;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the curve names.
   * @return the value of the property
   */
  public Set<String> getCurveNames() {
    return _curveNames;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets all curves.
   * @return the value of the property
   */
  public Map<String, YieldAndDiscountCurve> getAllCurves() {
    return _allCurves;
  }

  //-----------------------------------------------------------------------
  /**
   * Returns a builder that allows this bean to be mutated.
   * @return the mutable builder, not null
   */
  public Builder toBuilder() {
    return new Builder(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      IsdaCurveProvider other = (IsdaCurveProvider) obj;
      return JodaBeanUtils.equal(_discountingCurves, other._discountingCurves) &&
          JodaBeanUtils.equal(_iborCurves, other._iborCurves) &&
          JodaBeanUtils.equal(_overnightCurves, other._overnightCurves) &&
          JodaBeanUtils.equal(_curveNames, other._curveNames) &&
          JodaBeanUtils.equal(_allCurves, other._allCurves);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash = hash * 31 + JodaBeanUtils.hashCode(_discountingCurves);
    hash = hash * 31 + JodaBeanUtils.hashCode(_iborCurves);
    hash = hash * 31 + JodaBeanUtils.hashCode(_overnightCurves);
    hash = hash * 31 + JodaBeanUtils.hashCode(_curveNames);
    hash = hash * 31 + JodaBeanUtils.hashCode(_allCurves);
    return hash;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(192);
    buf.append("IsdaCurveProvider{");
    int len = buf.length();
    toString(buf);
    if (buf.length() > len) {
      buf.setLength(buf.length() - 2);
    }
    buf.append('}');
    return buf.toString();
  }

  protected void toString(StringBuilder buf) {
    buf.append("discountingCurves").append('=').append(JodaBeanUtils.toString(_discountingCurves)).append(',').append(' ');
    buf.append("iborCurves").append('=').append(JodaBeanUtils.toString(_iborCurves)).append(',').append(' ');
    buf.append("overnightCurves").append('=').append(JodaBeanUtils.toString(_overnightCurves)).append(',').append(' ');
    buf.append("curveNames").append('=').append(JodaBeanUtils.toString(_curveNames)).append(',').append(' ');
    buf.append("allCurves").append('=').append(JodaBeanUtils.toString(_allCurves)).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code IsdaCurveProvider}.
   */
  public static class Meta extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code discountingCurves} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<Map<Currency, YieldAndDiscountCurve>> _discountingCurves = DirectMetaProperty.ofImmutable(
        this, "discountingCurves", IsdaCurveProvider.class, (Class) Map.class);
    /**
     * The meta-property for the {@code iborCurves} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<Map<IborIndex, YieldAndDiscountCurve>> _iborCurves = DirectMetaProperty.ofImmutable(
        this, "iborCurves", IsdaCurveProvider.class, (Class) Map.class);
    /**
     * The meta-property for the {@code overnightCurves} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<Map<IndexON, YieldAndDiscountCurve>> _overnightCurves = DirectMetaProperty.ofImmutable(
        this, "overnightCurves", IsdaCurveProvider.class, (Class) Map.class);
    /**
     * The meta-property for the {@code curveNames} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<Set<String>> _curveNames = DirectMetaProperty.ofImmutable(
        this, "curveNames", IsdaCurveProvider.class, (Class) Set.class);
    /**
     * The meta-property for the {@code allCurves} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<Map<String, YieldAndDiscountCurve>> _allCurves = DirectMetaProperty.ofImmutable(
        this, "allCurves", IsdaCurveProvider.class, (Class) Map.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "discountingCurves",
        "iborCurves",
        "overnightCurves",
        "curveNames",
        "allCurves");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case -1937730619:  // discountingCurves
          return _discountingCurves;
        case 108381504:  // iborCurves
          return _iborCurves;
        case 299905608:  // overnightCurves
          return _overnightCurves;
        case -1864031335:  // curveNames
          return _curveNames;
        case -888600987:  // allCurves
          return _allCurves;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public IsdaCurveProvider.Builder builder() {
      return new IsdaCurveProvider.Builder();
    }

    @Override
    public Class<? extends IsdaCurveProvider> beanType() {
      return IsdaCurveProvider.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code discountingCurves} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Map<Currency, YieldAndDiscountCurve>> discountingCurves() {
      return _discountingCurves;
    }

    /**
     * The meta-property for the {@code iborCurves} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Map<IborIndex, YieldAndDiscountCurve>> iborCurves() {
      return _iborCurves;
    }

    /**
     * The meta-property for the {@code overnightCurves} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Map<IndexON, YieldAndDiscountCurve>> overnightCurves() {
      return _overnightCurves;
    }

    /**
     * The meta-property for the {@code curveNames} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Set<String>> curveNames() {
      return _curveNames;
    }

    /**
     * The meta-property for the {@code allCurves} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Map<String, YieldAndDiscountCurve>> allCurves() {
      return _allCurves;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -1937730619:  // discountingCurves
          return ((IsdaCurveProvider) bean).getDiscountingCurves();
        case 108381504:  // iborCurves
          return ((IsdaCurveProvider) bean).getIborCurves();
        case 299905608:  // overnightCurves
          return ((IsdaCurveProvider) bean).getOvernightCurves();
        case -1864031335:  // curveNames
          return ((IsdaCurveProvider) bean).getCurveNames();
        case -888600987:  // allCurves
          return ((IsdaCurveProvider) bean).getAllCurves();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      metaProperty(propertyName);
      if (quiet) {
        return;
      }
      throw new UnsupportedOperationException("Property cannot be written: " + propertyName);
    }

  }

  //-----------------------------------------------------------------------
  /**
   * The bean-builder for {@code IsdaCurveProvider}.
   */
  public static class Builder extends DirectFieldsBeanBuilder<IsdaCurveProvider> {

    private Map<Currency, YieldAndDiscountCurve> _discountingCurves;
    private Map<IborIndex, YieldAndDiscountCurve> _iborCurves;
    private Map<IndexON, YieldAndDiscountCurve> _overnightCurves;
    private Set<String> _curveNames;
    private Map<String, YieldAndDiscountCurve> _allCurves;

    /**
     * Restricted constructor.
     */
    protected Builder() {
    }

    /**
     * Restricted copy constructor.
     * @param beanToCopy  the bean to copy from, not null
     */
    protected Builder(IsdaCurveProvider beanToCopy) {
      this._discountingCurves = (beanToCopy.getDiscountingCurves() != null ? ImmutableMap.copyOf(beanToCopy.getDiscountingCurves()) : null);
      this._iborCurves = (beanToCopy.getIborCurves() != null ? ImmutableMap.copyOf(beanToCopy.getIborCurves()) : null);
      this._overnightCurves = (beanToCopy.getOvernightCurves() != null ? ImmutableMap.copyOf(beanToCopy.getOvernightCurves()) : null);
      this._curveNames = (beanToCopy.getCurveNames() != null ? ImmutableSet.copyOf(beanToCopy.getCurveNames()) : null);
      this._allCurves = (beanToCopy.getAllCurves() != null ? ImmutableMap.copyOf(beanToCopy.getAllCurves()) : null);
    }

    //-----------------------------------------------------------------------
    @Override
    public Object get(String propertyName) {
      switch (propertyName.hashCode()) {
        case -1937730619:  // discountingCurves
          return _discountingCurves;
        case 108381504:  // iborCurves
          return _iborCurves;
        case 299905608:  // overnightCurves
          return _overnightCurves;
        case -1864031335:  // curveNames
          return _curveNames;
        case -888600987:  // allCurves
          return _allCurves;
        default:
          throw new NoSuchElementException("Unknown property: " + propertyName);
      }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Builder set(String propertyName, Object newValue) {
      switch (propertyName.hashCode()) {
        case -1937730619:  // discountingCurves
          this._discountingCurves = (Map<Currency, YieldAndDiscountCurve>) newValue;
          break;
        case 108381504:  // iborCurves
          this._iborCurves = (Map<IborIndex, YieldAndDiscountCurve>) newValue;
          break;
        case 299905608:  // overnightCurves
          this._overnightCurves = (Map<IndexON, YieldAndDiscountCurve>) newValue;
          break;
        case -1864031335:  // curveNames
          this._curveNames = (Set<String>) newValue;
          break;
        case -888600987:  // allCurves
          this._allCurves = (Map<String, YieldAndDiscountCurve>) newValue;
          break;
        default:
          throw new NoSuchElementException("Unknown property: " + propertyName);
      }
      return this;
    }

    @Override
    public Builder set(MetaProperty<?> property, Object value) {
      super.set(property, value);
      return this;
    }

    /**
     * @deprecated Use Joda-Convert in application code
     */
    @Override
    @Deprecated
    public Builder setString(String propertyName, String value) {
      setString(meta().metaProperty(propertyName), value);
      return this;
    }

    /**
     * @deprecated Use Joda-Convert in application code
     */
    @Override
    @Deprecated
    public Builder setString(MetaProperty<?> property, String value) {
      super.setString(property, value);
      return this;
    }

    /**
     * @deprecated Loop in application code
     */
    @Override
    @Deprecated
    public Builder setAll(Map<String, ? extends Object> propertyValueMap) {
      super.setAll(propertyValueMap);
      return this;
    }

    @Override
    public IsdaCurveProvider build() {
      return new IsdaCurveProvider(
          _discountingCurves,
          _iborCurves,
          _overnightCurves,
          _curveNames,
          _allCurves);
    }

    //-----------------------------------------------------------------------
    /**
     * Sets the discounting curves.
     * @param discountingCurves  the new value
     * @return this, for chaining, not null
     */
    public Builder discountingCurves(Map<Currency, YieldAndDiscountCurve> discountingCurves) {
      this._discountingCurves = discountingCurves;
      return this;
    }

    /**
     * Sets the ibor index curves.
     * @param iborCurves  the new value
     * @return this, for chaining, not null
     */
    public Builder iborCurves(Map<IborIndex, YieldAndDiscountCurve> iborCurves) {
      this._iborCurves = iborCurves;
      return this;
    }

    /**
     * Sets the overnight curves.
     * @param overnightCurves  the new value
     * @return this, for chaining, not null
     */
    public Builder overnightCurves(Map<IndexON, YieldAndDiscountCurve> overnightCurves) {
      this._overnightCurves = overnightCurves;
      return this;
    }

    /**
     * Sets the curve names.
     * @param curveNames  the new value
     * @return this, for chaining, not null
     */
    public Builder curveNames(Set<String> curveNames) {
      this._curveNames = curveNames;
      return this;
    }

    /**
     * Sets the {@code curveNames} property in the builder
     * from an array of objects.
     * @param curveNames  the new value
     * @return this, for chaining, not null
     */
    public Builder curveNames(String... curveNames) {
      return curveNames(ImmutableSet.copyOf(curveNames));
    }

    /**
     * Sets all curves.
     * @param allCurves  the new value
     * @return this, for chaining, not null
     */
    public Builder allCurves(Map<String, YieldAndDiscountCurve> allCurves) {
      this._allCurves = allCurves;
      return this;
    }

    //-----------------------------------------------------------------------
    @Override
    public String toString() {
      StringBuilder buf = new StringBuilder(192);
      buf.append("IsdaCurveProvider.Builder{");
      int len = buf.length();
      toString(buf);
      if (buf.length() > len) {
        buf.setLength(buf.length() - 2);
      }
      buf.append('}');
      return buf.toString();
    }

    protected void toString(StringBuilder buf) {
      buf.append("discountingCurves").append('=').append(JodaBeanUtils.toString(_discountingCurves)).append(',').append(' ');
      buf.append("iborCurves").append('=').append(JodaBeanUtils.toString(_iborCurves)).append(',').append(' ');
      buf.append("overnightCurves").append('=').append(JodaBeanUtils.toString(_overnightCurves)).append(',').append(' ');
      buf.append("curveNames").append('=').append(JodaBeanUtils.toString(_curveNames)).append(',').append(' ');
      buf.append("allCurves").append('=').append(JodaBeanUtils.toString(_allCurves)).append(',').append(' ');
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
