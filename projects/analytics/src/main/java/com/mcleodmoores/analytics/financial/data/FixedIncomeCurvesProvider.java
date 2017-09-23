/**
 * Copyright (C) 2017 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.analytics.financial.data;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
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
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 *
 */
@BeanDefinition
public class FixedIncomeCurvesProvider implements FxDataProvider, DiscountingCurveProvider, IndexCurveProvider, ImmutableBean {
  @PropertyDefinition(get = "manual")
  private final Map<UniqueIdentifiable, YieldAndDiscountCurve> _discountingCurves;
  @PropertyDefinition(get = "manual")
  private final Map<Index, YieldAndDiscountCurve> _indexCurves;
  @PropertyDefinition(get = "manual")
  private final FXMatrix _fxMatrix;

  @ImmutableConstructor
  private FixedIncomeCurvesProvider(final Map<UniqueIdentifiable, YieldAndDiscountCurve> discountingCurves,
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
  public FixedIncomeCurvesProvider copy() {
    return new FixedIncomeCurvesProvider(_discountingCurves, _indexCurves, _fxMatrix);
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
  public YieldAndDiscountCurve getCurve(final UniqueIdentifiable id) {
    YieldAndDiscountCurve curve = _discountingCurves.get(id);
    if (curve != null) {
      return curve;
    }
    curve = _indexCurves.get(id);
    if (curve != null) {
      return curve;
    }
    throw new IllegalArgumentException("Could not get curve with id " + id);
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
  public double getFxRate(final Currency ccy1, final Currency ccy2) {
    return _fxMatrix.getFxRate(ccy1, ccy2);
  }

  public Map<UniqueIdentifiable, YieldAndDiscountCurve> getDiscountingCurves() {
    return Collections.unmodifiableMap(_discountingCurves);
  }

  public Map<Index, YieldAndDiscountCurve> getIndexCurves() {
    return Collections.unmodifiableMap(_indexCurves);
  }

  public FXMatrix getFxMatrix() {
    final double[][] rates = _fxMatrix.getRates();
    final double[][] copy = new double[rates.length][];
    int i = 0;
    for (final double[] row : rates) {
      copy[i] = new double[row.length];
      System.arraycopy(row, 0, copy[i++], 0, row.length);
    }
    final Map<Currency, Integer> currencies = Collections.unmodifiableMap(new LinkedHashMap<>(_fxMatrix.getCurrencies()));
    return new FXMatrix(currencies, copy);
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code FixedIncomeCurvesProvider}.
   * @return the meta-bean, not null
   */
  public static FixedIncomeCurvesProvider.Meta meta() {
    return FixedIncomeCurvesProvider.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(FixedIncomeCurvesProvider.Meta.INSTANCE);
  }

  /**
   * Returns a builder used to create an instance of the bean.
   * @return the builder, not null
   */
  public static FixedIncomeCurvesProvider.Builder builder() {
    return new FixedIncomeCurvesProvider.Builder();
  }

  @Override
  public FixedIncomeCurvesProvider.Meta metaBean() {
    return FixedIncomeCurvesProvider.Meta.INSTANCE;
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
      FixedIncomeCurvesProvider other = (FixedIncomeCurvesProvider) obj;
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
    buf.append("FixedIncomeCurvesProvider{");
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
   * The meta-bean for {@code FixedIncomeCurvesProvider}.
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
        this, "discountingCurves", FixedIncomeCurvesProvider.class, (Class) Map.class);
    /**
     * The meta-property for the {@code indexCurves} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<Map<Index, YieldAndDiscountCurve>> _indexCurves = DirectMetaProperty.ofImmutable(
        this, "indexCurves", FixedIncomeCurvesProvider.class, (Class) Map.class);
    /**
     * The meta-property for the {@code fxMatrix} property.
     */
    private final MetaProperty<FXMatrix> _fxMatrix = DirectMetaProperty.ofImmutable(
        this, "fxMatrix", FixedIncomeCurvesProvider.class, FXMatrix.class);
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
    public FixedIncomeCurvesProvider.Builder builder() {
      return new FixedIncomeCurvesProvider.Builder();
    }

    @Override
    public Class<? extends FixedIncomeCurvesProvider> beanType() {
      return FixedIncomeCurvesProvider.class;
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
          return ((FixedIncomeCurvesProvider) bean).getDiscountingCurves();
        case 886361302:  // indexCurves
          return ((FixedIncomeCurvesProvider) bean).getIndexCurves();
        case -1198118093:  // fxMatrix
          return ((FixedIncomeCurvesProvider) bean).getFxMatrix();
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
   * The bean-builder for {@code FixedIncomeCurvesProvider}.
   */
  public static class Builder extends DirectFieldsBeanBuilder<FixedIncomeCurvesProvider> {

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
    protected Builder(FixedIncomeCurvesProvider beanToCopy) {
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
    public FixedIncomeCurvesProvider build() {
      return new FixedIncomeCurvesProvider(
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
      buf.append("FixedIncomeCurvesProvider.Builder{");
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
