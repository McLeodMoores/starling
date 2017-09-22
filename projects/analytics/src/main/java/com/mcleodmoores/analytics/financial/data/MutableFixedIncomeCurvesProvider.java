/**
 * Copyright (C) 2017 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.analytics.financial.data;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.joda.beans.Bean;
import org.joda.beans.BeanBuilder;
import org.joda.beans.BeanDefinition;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaBean;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;

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
public class MutableFixedIncomeCurvesProvider implements MutableCurveProvider, DiscountingCurveProvider, IndexCurveProvider, FxDataProvider, Bean {
  @PropertyDefinition(set = "private")
  private Map<UniqueIdentifiable, YieldAndDiscountCurve> _discountingCurves;
  @PropertyDefinition(set = "private")
  private Map<Index, YieldAndDiscountCurve> _indexCurves;
  @PropertyDefinition(set = "manual")
  private FXMatrix _fxMatrix;

  private MutableFixedIncomeCurvesProvider() {
    _discountingCurves = new HashMap<>();
    _indexCurves = new HashMap<>();
    _fxMatrix = new FXMatrix();
  }

  public MutableFixedIncomeCurvesProvider(final Map<UniqueIdentifiable, YieldAndDiscountCurve> discountingCurves,
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
  public MutableFixedIncomeCurvesProvider copy() {
    return new MutableFixedIncomeCurvesProvider(_discountingCurves, _indexCurves, _fxMatrix);
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

  @Override
  public boolean setDiscountingCurve(final UniqueIdentifiable id, final YieldAndDiscountCurve curve) {

    return false;
  }

  @Override
  public boolean setIndexCurve(final UniqueIdentifiable id, final YieldAndDiscountCurve curve) {

    return false;
  }

  @Override
  public boolean setAll(final CurveProvider provider) {
    return false;
  }

  @Override
  public boolean setFxMatrix(final FXMatrix fxMatrix) {
    _fxMatrix = fxMatrix;
    return true;
  }

  @Override
  public boolean removeCurve(final UniqueIdentifiable id) {
    return false;
  }

  @Override
  public void clear() {
    _discountingCurves = new LinkedHashMap<>();
    _indexCurves = new LinkedHashMap<>();
    _fxMatrix = new FXMatrix();
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code MutableFixedIncomeCurvesProvider}.
   * @return the meta-bean, not null
   */
  public static MutableFixedIncomeCurvesProvider.Meta meta() {
    return MutableFixedIncomeCurvesProvider.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(MutableFixedIncomeCurvesProvider.Meta.INSTANCE);
  }

  @Override
  public MutableFixedIncomeCurvesProvider.Meta metaBean() {
    return MutableFixedIncomeCurvesProvider.Meta.INSTANCE;
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

  /**
   * Sets the discountingCurves.
   * @param discountingCurves  the new value of the property
   */
  private void setDiscountingCurves(Map<UniqueIdentifiable, YieldAndDiscountCurve> discountingCurves) {
    this._discountingCurves = discountingCurves;
  }

  /**
   * Gets the the {@code discountingCurves} property.
   * @return the property, not null
   */
  public final Property<Map<UniqueIdentifiable, YieldAndDiscountCurve>> discountingCurves() {
    return metaBean().discountingCurves().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the indexCurves.
   * @return the value of the property
   */
  public Map<Index, YieldAndDiscountCurve> getIndexCurves() {
    return _indexCurves;
  }

  /**
   * Sets the indexCurves.
   * @param indexCurves  the new value of the property
   */
  private void setIndexCurves(Map<Index, YieldAndDiscountCurve> indexCurves) {
    this._indexCurves = indexCurves;
  }

  /**
   * Gets the the {@code indexCurves} property.
   * @return the property, not null
   */
  public final Property<Map<Index, YieldAndDiscountCurve>> indexCurves() {
    return metaBean().indexCurves().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the fxMatrix.
   * @return the value of the property
   */
  public FXMatrix getFxMatrix() {
    return _fxMatrix;
  }

  /**
   * Gets the the {@code fxMatrix} property.
   * @return the property, not null
   */
  public final Property<FXMatrix> fxMatrix() {
    return metaBean().fxMatrix().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public MutableFixedIncomeCurvesProvider clone() {
    return JodaBeanUtils.cloneAlways(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      MutableFixedIncomeCurvesProvider other = (MutableFixedIncomeCurvesProvider) obj;
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
    buf.append("MutableFixedIncomeCurvesProvider{");
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
   * The meta-bean for {@code MutableFixedIncomeCurvesProvider}.
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
    private final MetaProperty<Map<UniqueIdentifiable, YieldAndDiscountCurve>> _discountingCurves = DirectMetaProperty.ofReadWrite(
        this, "discountingCurves", MutableFixedIncomeCurvesProvider.class, (Class) Map.class);
    /**
     * The meta-property for the {@code indexCurves} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<Map<Index, YieldAndDiscountCurve>> _indexCurves = DirectMetaProperty.ofReadWrite(
        this, "indexCurves", MutableFixedIncomeCurvesProvider.class, (Class) Map.class);
    /**
     * The meta-property for the {@code fxMatrix} property.
     */
    private final MetaProperty<FXMatrix> _fxMatrix = DirectMetaProperty.ofReadWrite(
        this, "fxMatrix", MutableFixedIncomeCurvesProvider.class, FXMatrix.class);
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
    public BeanBuilder<? extends MutableFixedIncomeCurvesProvider> builder() {
      return new DirectBeanBuilder<MutableFixedIncomeCurvesProvider>(new MutableFixedIncomeCurvesProvider());
    }

    @Override
    public Class<? extends MutableFixedIncomeCurvesProvider> beanType() {
      return MutableFixedIncomeCurvesProvider.class;
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
          return ((MutableFixedIncomeCurvesProvider) bean).getDiscountingCurves();
        case 886361302:  // indexCurves
          return ((MutableFixedIncomeCurvesProvider) bean).getIndexCurves();
        case -1198118093:  // fxMatrix
          return ((MutableFixedIncomeCurvesProvider) bean).getFxMatrix();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -1937730619:  // discountingCurves
          ((MutableFixedIncomeCurvesProvider) bean).setDiscountingCurves((Map<UniqueIdentifiable, YieldAndDiscountCurve>) newValue);
          return;
        case 886361302:  // indexCurves
          ((MutableFixedIncomeCurvesProvider) bean).setIndexCurves((Map<Index, YieldAndDiscountCurve>) newValue);
          return;
        case -1198118093:  // fxMatrix
          ((MutableFixedIncomeCurvesProvider) bean).setFxMatrix((FXMatrix) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
