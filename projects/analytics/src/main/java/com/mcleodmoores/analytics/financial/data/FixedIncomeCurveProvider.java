/**
 * Copyright (C) 2017 - present McLeod Moores Software Limited.  All rights reserved.
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
import com.mcleodmoores.analytics.financial.index.Index;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.ForwardSensitivity;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.DoublesPair;

/**
 *
 */
@BeanDefinition
public class FixedIncomeCurveProvider implements FxDataProvider, DiscountingCurveProvider, IndexCurveProvider, ImmutableBean {
  @PropertyDefinition
  private final Map<UniqueIdentifiable, YieldAndDiscountCurve> _discountingCurves;
  @PropertyDefinition
  private final Map<Index, YieldAndDiscountCurve> _indexCurves;
  @PropertyDefinition
  private final FXMatrix _fxMatrix;

  @ImmutableConstructor
  private FixedIncomeCurveProvider(final Map<UniqueIdentifiable, YieldAndDiscountCurve> discountingCurves,
      final Map<Index, YieldAndDiscountCurve> indexCurves,
      final FXMatrix fxMatrix) {
    _discountingCurves = discountingCurves == null
        ? Collections.<UniqueIdentifiable, YieldAndDiscountCurve>emptyMap() : Collections.unmodifiableMap(new HashMap<>(discountingCurves));
    _indexCurves = indexCurves == null
        ? Collections.<Index, YieldAndDiscountCurve>emptyMap() : Collections.unmodifiableMap(new HashMap<>(indexCurves));
    final double[][] rates = fxMatrix.getRates();
    final double[][] copy = new double[rates.length][];
    int i = 0;
    for (final double[] row : rates) {
      copy[i] = new double[row.length];
      System.arraycopy(row, 0, copy[i++], 0, row.length);
    }
    final Map<Currency, Integer> currencies = Collections.unmodifiableMap(new LinkedHashMap<>(fxMatrix.getCurrencies()));
    _fxMatrix = new FXMatrix(currencies, copy);
  }

  @Override
  public FixedIncomeCurveProvider copy() {
    return new FixedIncomeCurveProvider(_discountingCurves, _indexCurves, _fxMatrix);
  }

  @Override
  public Set<UniqueIdentifiable> getIdentifiers() {
    final Set<UniqueIdentifiable> result = new HashSet<>();
    result.addAll(_discountingCurves.keySet());
    result.addAll(_indexCurves.keySet());
    return result;
  }

  @Override
  public Set<UniqueIdentifiable> getIdentifiersForScheme(final String scheme) {
    final Set<UniqueIdentifiable> result = new HashSet<>();
    for (final Map.Entry<UniqueIdentifiable, ?> entry : _discountingCurves.entrySet()) {
      if (entry.getKey().getUniqueId().getScheme().equals(scheme)) {
        result.add(entry.getKey());
      }
    }
    for (final Map.Entry<Index, ?> entry : _indexCurves.entrySet()) {
      if (entry.getKey().getUniqueId().getScheme().equals(scheme)) {
        result.add(entry.getKey());
      }
    }
    return result;
  }

  @Override
  public double getInvestmentFactor(final Index index, final double startTime, final double endTime, final double accrualFactor) {
    final YieldAndDiscountCurve curve = _indexCurves.get(index);
    if (curve == null) {
      throw new IllegalArgumentException("Index curve with id " + index + " not found");
    }
    return curve.getDiscountFactor(startTime) / curve.getDiscountFactor(endTime);
  }

  @Override
  public double getForwardRate(final Index index, final double startTime, final double endTime, final double accrualFactor,
      final CompoundingType compoundingType) {
    ArgumentChecker.isFalse(accrualFactor == 0.0, "The accrual factor cannot be zero");
    final YieldAndDiscountCurve curve = _indexCurves.get(index);
    if (curve == null) {
      throw new IllegalArgumentException("Index curve with id " + index + " not found");
    }
    switch (compoundingType) {
      case SIMPLE:
        return (curve.getDiscountFactor(startTime) / curve.getDiscountFactor(endTime) - 1) / accrualFactor;
      case ANNUAL:
        return Math.pow(curve.getDiscountFactor(startTime) / curve.getDiscountFactor(endTime), 1 / accrualFactor) - 1;
      default:
        throw new IllegalArgumentException("Unhandled compounding type " + compoundingType);
    }
  }

  @Override
  public double getForwardRate(final Index index, final double startTime, final double endTime, final CompoundingType compoundingType) {
    return getForwardRate(index, startTime, endTime, endTime - startTime, compoundingType);
  }

  @Override
  public double getDiscountFactor(final UniqueIdentifiable id, final double time) {
    final YieldAndDiscountCurve curve = _discountingCurves.get(id);
    if (curve == null) {
      throw new IllegalArgumentException("Discounting curve with id " + id + " not found");
    }
    return curve.getDiscountFactor(time);
  }

  @Override
  public double[] parameterSensitivity(final UniqueIdentifiable id, final List<DoublesPair> pointSensitivity) {
    YieldAndDiscountCurve curve = _discountingCurves.get(id);
    if (curve == null) {
      curve = _indexCurves.get(id);
        if (curve == null) {
          throw new IllegalArgumentException("Could not get curve with id " + id);
      }
    }
    // want to only return results for cases where a curve is present, even if they're empty
    final int n = curve.getNumberOfParameters();
    if (pointSensitivity == null || pointSensitivity.size() <= 0) {
      // sensitivities are 0
      return new double[n];
    }
    final double[] result = new double[n];
    for (final DoublesPair pair : pointSensitivity) {
      final double t = pair.getFirst();
      final double dy = pair.getSecond();
      final double[] sensitivities = curve.getInterestRateParameterSensitivity(t);
      for (int i = 0; i < n; i++) {
        result[i] += dy * sensitivities[i];
      }
    }
    return result;
  }

  @Override
  public double[] parameterForwardSensitivity(final UniqueIdentifiable id, final List<ForwardSensitivity> pointSensitivity) {
    YieldAndDiscountCurve curve = _discountingCurves.get(id);
    if (curve == null) {
      curve = _indexCurves.get(id);
        if (curve == null) {
          throw new IllegalArgumentException("Could not get curve with id " + id);
      }
    }
    final int n = curve.getNumberOfParameters();
    final double[] result = new double[n];
    // want to only return results for cases where a curve is present, even if they're empty
    if (pointSensitivity == null || pointSensitivity.size() <= 0) {
      // sensitivities are 0
      return new double[n];
    }
    for (final ForwardSensitivity pair : pointSensitivity) {
      final double t1 = pair.getStartTime();
      final double t2 = pair.getEndTime();
      final double fBar = pair.getValue();
      // only the sensitivity to the forward is available; the sensitivity to the pseudo-discount factors needs to be computed.
      final double dfT1 = curve.getDiscountFactor(t1);
      final double dfT2 = curve.getDiscountFactor(t2);
      final double dFdyT1 = pair.derivativeToYieldStart(dfT1, dfT2);
      final double dFdyT2 = pair.derivativeToYieldEnd(dfT1, dfT2);
      final double[] sensitivityT1 = curve.getInterestRateParameterSensitivity(t1);
      final double[] sensitivityT2 = curve.getInterestRateParameterSensitivity(t2);
      for (int i = 0; i < n; i++) {
        result[i] += dFdyT1 * sensitivityT1[i] * fBar;
        result[i] += dFdyT2 * sensitivityT2[i] * fBar;
      }
    }
    return result;
  }

  @Override
  public double getFxRate(final Currency ccy1, final Currency ccy2) {
    return _fxMatrix.getFxRate(ccy1, ccy2);
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code FixedIncomeCurveProvider}.
   * @return the meta-bean, not null
   */
  public static FixedIncomeCurveProvider.Meta meta() {
    return FixedIncomeCurveProvider.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(FixedIncomeCurveProvider.Meta.INSTANCE);
  }

  /**
   * Returns a builder used to create an instance of the bean.
   * @return the builder, not null
   */
  public static FixedIncomeCurveProvider.Builder builder() {
    return new FixedIncomeCurveProvider.Builder();
  }

  @Override
  public FixedIncomeCurveProvider.Meta metaBean() {
    return FixedIncomeCurveProvider.Meta.INSTANCE;
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
   * Gets the discountingCurves.
   * @return the value of the property
   */
  public Map<UniqueIdentifiable, YieldAndDiscountCurve> getDiscountingCurves() {
    return _discountingCurves;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the indexCurves.
   * @return the value of the property
   */
  public Map<Index, YieldAndDiscountCurve> getIndexCurves() {
    return _indexCurves;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the fxMatrix.
   * @return the value of the property
   */
  public FXMatrix getFxMatrix() {
    return _fxMatrix;
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
      FixedIncomeCurveProvider other = (FixedIncomeCurveProvider) obj;
      return JodaBeanUtils.equal(getDiscountingCurves(), other.getDiscountingCurves()) &&
          JodaBeanUtils.equal(getIndexCurves(), other.getIndexCurves()) &&
          JodaBeanUtils.equal(getFxMatrix(), other.getFxMatrix());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash = hash * 31 + JodaBeanUtils.hashCode(getDiscountingCurves());
    hash = hash * 31 + JodaBeanUtils.hashCode(getIndexCurves());
    hash = hash * 31 + JodaBeanUtils.hashCode(getFxMatrix());
    return hash;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(128);
    buf.append("FixedIncomeCurveProvider{");
    int len = buf.length();
    toString(buf);
    if (buf.length() > len) {
      buf.setLength(buf.length() - 2);
    }
    buf.append('}');
    return buf.toString();
  }

  protected void toString(StringBuilder buf) {
    buf.append("discountingCurves").append('=').append(JodaBeanUtils.toString(getDiscountingCurves())).append(',').append(' ');
    buf.append("indexCurves").append('=').append(JodaBeanUtils.toString(getIndexCurves())).append(',').append(' ');
    buf.append("fxMatrix").append('=').append(JodaBeanUtils.toString(getFxMatrix())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code FixedIncomeCurveProvider}.
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
    private final MetaProperty<Map<UniqueIdentifiable, YieldAndDiscountCurve>> _discountingCurves = DirectMetaProperty.ofImmutable(
        this, "discountingCurves", FixedIncomeCurveProvider.class, (Class) Map.class);
    /**
     * The meta-property for the {@code indexCurves} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<Map<Index, YieldAndDiscountCurve>> _indexCurves = DirectMetaProperty.ofImmutable(
        this, "indexCurves", FixedIncomeCurveProvider.class, (Class) Map.class);
    /**
     * The meta-property for the {@code fxMatrix} property.
     */
    private final MetaProperty<FXMatrix> _fxMatrix = DirectMetaProperty.ofImmutable(
        this, "fxMatrix", FixedIncomeCurveProvider.class, FXMatrix.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "discountingCurves",
        "indexCurves",
        "fxMatrix");

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
        case 886361302:  // indexCurves
          return _indexCurves;
        case -1198118093:  // fxMatrix
          return _fxMatrix;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public FixedIncomeCurveProvider.Builder builder() {
      return new FixedIncomeCurveProvider.Builder();
    }

    @Override
    public Class<? extends FixedIncomeCurveProvider> beanType() {
      return FixedIncomeCurveProvider.class;
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
    public final MetaProperty<Map<UniqueIdentifiable, YieldAndDiscountCurve>> discountingCurves() {
      return _discountingCurves;
    }

    /**
     * The meta-property for the {@code indexCurves} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Map<Index, YieldAndDiscountCurve>> indexCurves() {
      return _indexCurves;
    }

    /**
     * The meta-property for the {@code fxMatrix} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<FXMatrix> fxMatrix() {
      return _fxMatrix;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -1937730619:  // discountingCurves
          return ((FixedIncomeCurveProvider) bean).getDiscountingCurves();
        case 886361302:  // indexCurves
          return ((FixedIncomeCurveProvider) bean).getIndexCurves();
        case -1198118093:  // fxMatrix
          return ((FixedIncomeCurveProvider) bean).getFxMatrix();
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
   * The bean-builder for {@code FixedIncomeCurveProvider}.
   */
  public static class Builder extends DirectFieldsBeanBuilder<FixedIncomeCurveProvider> {

    private Map<UniqueIdentifiable, YieldAndDiscountCurve> _discountingCurves;
    private Map<Index, YieldAndDiscountCurve> _indexCurves;
    private FXMatrix _fxMatrix;

    /**
     * Restricted constructor.
     */
    protected Builder() {
    }

    /**
     * Restricted copy constructor.
     * @param beanToCopy  the bean to copy from, not null
     */
    protected Builder(FixedIncomeCurveProvider beanToCopy) {
      this._discountingCurves = (beanToCopy.getDiscountingCurves() != null ? ImmutableMap.copyOf(beanToCopy.getDiscountingCurves()) : null);
      this._indexCurves = (beanToCopy.getIndexCurves() != null ? ImmutableMap.copyOf(beanToCopy.getIndexCurves()) : null);
      this._fxMatrix = beanToCopy.getFxMatrix();
    }

    //-----------------------------------------------------------------------
    @Override
    public Object get(String propertyName) {
      switch (propertyName.hashCode()) {
        case -1937730619:  // discountingCurves
          return _discountingCurves;
        case 886361302:  // indexCurves
          return _indexCurves;
        case -1198118093:  // fxMatrix
          return _fxMatrix;
        default:
          throw new NoSuchElementException("Unknown property: " + propertyName);
      }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Builder set(String propertyName, Object newValue) {
      switch (propertyName.hashCode()) {
        case -1937730619:  // discountingCurves
          this._discountingCurves = (Map<UniqueIdentifiable, YieldAndDiscountCurve>) newValue;
          break;
        case 886361302:  // indexCurves
          this._indexCurves = (Map<Index, YieldAndDiscountCurve>) newValue;
          break;
        case -1198118093:  // fxMatrix
          this._fxMatrix = (FXMatrix) newValue;
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

    @Override
    public Builder setString(String propertyName, String value) {
      setString(meta().metaProperty(propertyName), value);
      return this;
    }

    @Override
    public Builder setString(MetaProperty<?> property, String value) {
      super.setString(property, value);
      return this;
    }

    @Override
    public Builder setAll(Map<String, ? extends Object> propertyValueMap) {
      super.setAll(propertyValueMap);
      return this;
    }

    @Override
    public FixedIncomeCurveProvider build() {
      return new FixedIncomeCurveProvider(
          _discountingCurves,
          _indexCurves,
          _fxMatrix);
    }

    //-----------------------------------------------------------------------
    /**
     * Sets the discountingCurves.
     * @param discountingCurves  the new value
     * @return this, for chaining, not null
     */
    public Builder discountingCurves(Map<UniqueIdentifiable, YieldAndDiscountCurve> discountingCurves) {
      this._discountingCurves = discountingCurves;
      return this;
    }

    /**
     * Sets the indexCurves.
     * @param indexCurves  the new value
     * @return this, for chaining, not null
     */
    public Builder indexCurves(Map<Index, YieldAndDiscountCurve> indexCurves) {
      this._indexCurves = indexCurves;
      return this;
    }

    /**
     * Sets the fxMatrix.
     * @param fxMatrix  the new value
     * @return this, for chaining, not null
     */
    public Builder fxMatrix(FXMatrix fxMatrix) {
      this._fxMatrix = fxMatrix;
      return this;
    }

    //-----------------------------------------------------------------------
    @Override
    public String toString() {
      StringBuilder buf = new StringBuilder(128);
      buf.append("FixedIncomeCurveProvider.Builder{");
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
      buf.append("indexCurves").append('=').append(JodaBeanUtils.toString(_indexCurves)).append(',').append(' ');
      buf.append("fxMatrix").append('=').append(JodaBeanUtils.toString(_fxMatrix)).append(',').append(' ');
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
