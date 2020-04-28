/**
 * Copyright (C) 2017 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.analytics.financial.data;

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

import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Pair;

/**
 *
 */
@BeanDefinition
public class TimeDeltaVolatilitySurfaceProvider implements ImmutableBean, VolatilityProvider<Pair<Double, Double>> {
  @PropertyDefinition
  private final UniqueIdentifiable _id;
  @PropertyDefinition(get = "manual")
  private final double[] _times;
  @PropertyDefinition(get = "manual")
  private final double[][] _deltas;
  @PropertyDefinition(get = "manual")
  private final double[][] _volatilities;
  @PropertyDefinition
  private final boolean _isPutDelta;
  @PropertyDefinition
  private final Interpolator1D _timeInterpolator;
  @PropertyDefinition
  private final Interpolator1D _deltaInterpolator;

  public static TimeDeltaVolatilitySurfaceProvider ofDeltas(final UniqueIdentifiable id, final double[] times, final double[][] deltas,
      final double[][] volatilities, final boolean isPutDelta, final Interpolator1D timeInterpolator,
      final Interpolator1D deltaInterpolator) {
    return new TimeDeltaVolatilitySurfaceProvider(id, times, deltas, volatilities, isPutDelta, timeInterpolator, deltaInterpolator);
  }

  public static TimeDeltaVolatilitySurfaceProvider ofStrangleRiskReversal(final UniqueIdentifiable id, final double[] times,
      final double[][] deltas,
      final double[] atms, final double[][] strangles, final double[][] riskReversals, final Interpolator1D timeInterpolator,
      final Interpolator1D deltaInterpolator) {
    return new TimeDeltaVolatilitySurfaceProvider(id, times, deltas, atms, strangles, riskReversals, timeInterpolator, deltaInterpolator);
  }

  @ImmutableConstructor
  private TimeDeltaVolatilitySurfaceProvider(final UniqueIdentifiable id, final double[] times, final double[][] deltas,
      final double[][] volatilities,
      final boolean isPutDelta, final Interpolator1D timeInterpolator, final Interpolator1D deltaInterpolator) {
    _id = ArgumentChecker.notNull(id, "id");
    ArgumentChecker.notNull(times, "times");
    ArgumentChecker.notNull(deltas, "deltas");
    ArgumentChecker.notNull(volatilities, "volatilities");
    final int n = times.length;
    ArgumentChecker.isTrue(n == deltas.length, "Must have one array of deltas for each time");
    ArgumentChecker.isTrue(n == volatilities.length, "Must have one array of volatilities for each time");
    _times = new double[n];
    _deltas = new double[n][];
    _isPutDelta = isPutDelta;
    _volatilities = new double[n][];
    System.arraycopy(times, 0, _times, 0, n);
    for (int i = 0; i < n; i++) {
      final int m = deltas[i].length;
      ArgumentChecker.isTrue(m == volatilities.length, "Must have one volatility per delta");
      _deltas[i] = new double[m];
      _volatilities[i] = new double[m];
      System.arraycopy(deltas[i], 0, _deltas[i], 0, m);
      System.arraycopy(volatilities[i], 0, _volatilities[i], 0, m);
    }
    _timeInterpolator = ArgumentChecker.notNull(timeInterpolator, "timeInterpolator");
    _deltaInterpolator = ArgumentChecker.notNull(deltaInterpolator, "deltaInterpolator");
  }

  private TimeDeltaVolatilitySurfaceProvider(final UniqueIdentifiable id, final double[] times, final double[][] deltas,
      final double[] atms,
      final double[][] strangles, final double[][] riskReversals, final Interpolator1D timeInterpolator,
      final Interpolator1D deltaInterpolator) {
    _id = ArgumentChecker.notNull(id, "id");
    ArgumentChecker.notNull(times, "times");
    final int n = times.length;
    ArgumentChecker.notNull(deltas, "deltas");
    ArgumentChecker.notNull(atms, "atms");
    ArgumentChecker.notNull(strangles, "strangles");
    ArgumentChecker.notNull(riskReversals, "riskReversals");
    ArgumentChecker.isTrue(n == deltas.length, "Must have one array of deltas for each time");
    ArgumentChecker.isTrue(n == atms.length, "Must have one ATM value for each time");
    ArgumentChecker.isTrue(n == strangles.length, "Must have one strangle array for each time");
    ArgumentChecker.isTrue(n == riskReversals.length, "Must have one risk reversal array for each time");
    _times = new double[n];
    _deltas = new double[n][];
    _volatilities = new double[n][];
    System.arraycopy(times, 0, _times, 0, n);
    for (int i = 0; i < n; i++) {
      final int m = deltas.length;
      ArgumentChecker.isTrue(m == strangles.length, "Must have one strangle quote per delta");
      ArgumentChecker.isTrue(m == riskReversals.length, "Must have one risk reversal quote per delta");
      _deltas[i] = new double[m * 2 + 1];
      _volatilities[i] = new double[m * 2 + 1];
      _deltas[i][m] = 0;
      _volatilities[i][m] = atms[i];
      for (int j = 0; j < m; j++) {
        _deltas[i][j] = deltas[i][j];
        _deltas[i][m * 2 - j] = 1 - deltas[i][j];
        // put
        _volatilities[i][j] = strangles[i][j] + atms[i] - riskReversals[i][j] / 2;
        // call
        _volatilities[i][m * 2 - j] = strangles[i][j] + atms[i] + riskReversals[i][j] / 2;
      }
    }
    _isPutDelta = true;
    _timeInterpolator = ArgumentChecker.notNull(timeInterpolator, "timeInterpolator");
    _deltaInterpolator = ArgumentChecker.notNull(deltaInterpolator, "deltaInterpolator");
  }

  @Override
  public UniqueIdentifiable getIdentifier() {
    return _id;
  }

  @Override
  public TimeDeltaVolatilitySurfaceProvider copy() {
    return new TimeDeltaVolatilitySurfaceProvider(_id, _times, _deltas, _volatilities, _isPutDelta, _timeInterpolator, _deltaInterpolator);
  }

  @Override
  public double getVolatility(final Pair<Double, Double> xy) {
    return 0;
  }

  public double[] getTimes() {
    final double[] times = new double[_times.length];
    System.arraycopy(_times, 0, times, 0, _times.length);
    return times;
  }

  public double[][] getDeltas() {
    final double[][] deltas = new double[_deltas.length][];
    for (int i = 0; i < _deltas.length; i++) {
      deltas[i] = new double[_deltas[i].length];
      System.arraycopy(_deltas[i], 0, deltas[i], 0, _deltas[i].length);
    }
    return deltas;
  }

  public double[][] getVolatilities() {
    final double[][] volatilities = new double[_volatilities.length][];
    for (int i = 0; i < _volatilities.length; i++) {
      volatilities[i] = new double[_volatilities[i].length];
      System.arraycopy(_volatilities[i], 0, volatilities[i], 0, _volatilities[i].length);
    }
    return volatilities;
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code TimeDeltaVolatilitySurfaceProvider}.
   * @return the meta-bean, not null
   */
  public static TimeDeltaVolatilitySurfaceProvider.Meta meta() {
    return TimeDeltaVolatilitySurfaceProvider.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(TimeDeltaVolatilitySurfaceProvider.Meta.INSTANCE);
  }

  /**
   * Returns a builder used to create an instance of the bean.
   * @return the builder, not null
   */
  public static TimeDeltaVolatilitySurfaceProvider.Builder builder() {
    return new TimeDeltaVolatilitySurfaceProvider.Builder();
  }

  @Override
  public TimeDeltaVolatilitySurfaceProvider.Meta metaBean() {
    return TimeDeltaVolatilitySurfaceProvider.Meta.INSTANCE;
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
   * Gets the id.
   * @return the value of the property
   */
  public UniqueIdentifiable getId() {
    return _id;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the isPutDelta.
   * @return the value of the property
   */
  public boolean isIsPutDelta() {
    return _isPutDelta;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the timeInterpolator.
   * @return the value of the property
   */
  public Interpolator1D getTimeInterpolator() {
    return _timeInterpolator;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the deltaInterpolator.
   * @return the value of the property
   */
  public Interpolator1D getDeltaInterpolator() {
    return _deltaInterpolator;
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
      TimeDeltaVolatilitySurfaceProvider other = (TimeDeltaVolatilitySurfaceProvider) obj;
      return JodaBeanUtils.equal(_id, other._id) &&
          JodaBeanUtils.equal(_times, other._times) &&
          JodaBeanUtils.equal(_deltas, other._deltas) &&
          JodaBeanUtils.equal(_volatilities, other._volatilities) &&
          (_isPutDelta == other._isPutDelta) &&
          JodaBeanUtils.equal(_timeInterpolator, other._timeInterpolator) &&
          JodaBeanUtils.equal(_deltaInterpolator, other._deltaInterpolator);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash = hash * 31 + JodaBeanUtils.hashCode(_id);
    hash = hash * 31 + JodaBeanUtils.hashCode(_times);
    hash = hash * 31 + JodaBeanUtils.hashCode(_deltas);
    hash = hash * 31 + JodaBeanUtils.hashCode(_volatilities);
    hash = hash * 31 + JodaBeanUtils.hashCode(_isPutDelta);
    hash = hash * 31 + JodaBeanUtils.hashCode(_timeInterpolator);
    hash = hash * 31 + JodaBeanUtils.hashCode(_deltaInterpolator);
    return hash;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(256);
    buf.append("TimeDeltaVolatilitySurfaceProvider{");
    int len = buf.length();
    toString(buf);
    if (buf.length() > len) {
      buf.setLength(buf.length() - 2);
    }
    buf.append('}');
    return buf.toString();
  }

  protected void toString(StringBuilder buf) {
    buf.append("id").append('=').append(JodaBeanUtils.toString(_id)).append(',').append(' ');
    buf.append("times").append('=').append(JodaBeanUtils.toString(_times)).append(',').append(' ');
    buf.append("deltas").append('=').append(JodaBeanUtils.toString(_deltas)).append(',').append(' ');
    buf.append("volatilities").append('=').append(JodaBeanUtils.toString(_volatilities)).append(',').append(' ');
    buf.append("isPutDelta").append('=').append(JodaBeanUtils.toString(_isPutDelta)).append(',').append(' ');
    buf.append("timeInterpolator").append('=').append(JodaBeanUtils.toString(_timeInterpolator)).append(',').append(' ');
    buf.append("deltaInterpolator").append('=').append(JodaBeanUtils.toString(_deltaInterpolator)).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code TimeDeltaVolatilitySurfaceProvider}.
   */
  public static class Meta extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code id} property.
     */
    private final MetaProperty<UniqueIdentifiable> _id = DirectMetaProperty.ofImmutable(
        this, "id", TimeDeltaVolatilitySurfaceProvider.class, UniqueIdentifiable.class);
    /**
     * The meta-property for the {@code times} property.
     */
    private final MetaProperty<double[]> _times = DirectMetaProperty.ofImmutable(
        this, "times", TimeDeltaVolatilitySurfaceProvider.class, double[].class);
    /**
     * The meta-property for the {@code deltas} property.
     */
    private final MetaProperty<double[][]> _deltas = DirectMetaProperty.ofImmutable(
        this, "deltas", TimeDeltaVolatilitySurfaceProvider.class, double[][].class);
    /**
     * The meta-property for the {@code volatilities} property.
     */
    private final MetaProperty<double[][]> _volatilities = DirectMetaProperty.ofImmutable(
        this, "volatilities", TimeDeltaVolatilitySurfaceProvider.class, double[][].class);
    /**
     * The meta-property for the {@code isPutDelta} property.
     */
    private final MetaProperty<Boolean> _isPutDelta = DirectMetaProperty.ofImmutable(
        this, "isPutDelta", TimeDeltaVolatilitySurfaceProvider.class, Boolean.TYPE);
    /**
     * The meta-property for the {@code timeInterpolator} property.
     */
    private final MetaProperty<Interpolator1D> _timeInterpolator = DirectMetaProperty.ofImmutable(
        this, "timeInterpolator", TimeDeltaVolatilitySurfaceProvider.class, Interpolator1D.class);
    /**
     * The meta-property for the {@code deltaInterpolator} property.
     */
    private final MetaProperty<Interpolator1D> _deltaInterpolator = DirectMetaProperty.ofImmutable(
        this, "deltaInterpolator", TimeDeltaVolatilitySurfaceProvider.class, Interpolator1D.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "id",
        "times",
        "deltas",
        "volatilities",
        "isPutDelta",
        "timeInterpolator",
        "deltaInterpolator");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case 3355:  // id
          return _id;
        case 110364486:  // times
          return _times;
        case -1335444549:  // deltas
          return _deltas;
        case -625639549:  // volatilities
          return _volatilities;
        case 978604339:  // isPutDelta
          return _isPutDelta;
        case -587914188:  // timeInterpolator
          return _timeInterpolator;
        case -1470462817:  // deltaInterpolator
          return _deltaInterpolator;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public TimeDeltaVolatilitySurfaceProvider.Builder builder() {
      return new TimeDeltaVolatilitySurfaceProvider.Builder();
    }

    @Override
    public Class<? extends TimeDeltaVolatilitySurfaceProvider> beanType() {
      return TimeDeltaVolatilitySurfaceProvider.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code id} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<UniqueIdentifiable> id() {
      return _id;
    }

    /**
     * The meta-property for the {@code times} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<double[]> times() {
      return _times;
    }

    /**
     * The meta-property for the {@code deltas} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<double[][]> deltas() {
      return _deltas;
    }

    /**
     * The meta-property for the {@code volatilities} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<double[][]> volatilities() {
      return _volatilities;
    }

    /**
     * The meta-property for the {@code isPutDelta} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Boolean> isPutDelta() {
      return _isPutDelta;
    }

    /**
     * The meta-property for the {@code timeInterpolator} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Interpolator1D> timeInterpolator() {
      return _timeInterpolator;
    }

    /**
     * The meta-property for the {@code deltaInterpolator} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Interpolator1D> deltaInterpolator() {
      return _deltaInterpolator;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 3355:  // id
          return ((TimeDeltaVolatilitySurfaceProvider) bean).getId();
        case 110364486:  // times
          return ((TimeDeltaVolatilitySurfaceProvider) bean).getTimes();
        case -1335444549:  // deltas
          return ((TimeDeltaVolatilitySurfaceProvider) bean).getDeltas();
        case -625639549:  // volatilities
          return ((TimeDeltaVolatilitySurfaceProvider) bean).getVolatilities();
        case 978604339:  // isPutDelta
          return ((TimeDeltaVolatilitySurfaceProvider) bean).isIsPutDelta();
        case -587914188:  // timeInterpolator
          return ((TimeDeltaVolatilitySurfaceProvider) bean).getTimeInterpolator();
        case -1470462817:  // deltaInterpolator
          return ((TimeDeltaVolatilitySurfaceProvider) bean).getDeltaInterpolator();
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
   * The bean-builder for {@code TimeDeltaVolatilitySurfaceProvider}.
   */
  public static class Builder extends DirectFieldsBeanBuilder<TimeDeltaVolatilitySurfaceProvider> {

    private UniqueIdentifiable _id;
    private double[] _times;
    private double[][] _deltas;
    private double[][] _volatilities;
    private boolean _isPutDelta;
    private Interpolator1D _timeInterpolator;
    private Interpolator1D _deltaInterpolator;

    /**
     * Restricted constructor.
     */
    protected Builder() {
    }

    /**
     * Restricted copy constructor.
     * @param beanToCopy  the bean to copy from, not null
     */
    protected Builder(TimeDeltaVolatilitySurfaceProvider beanToCopy) {
      this._id = beanToCopy.getId();
      this._times = (beanToCopy.getTimes() != null ? beanToCopy.getTimes().clone() : null);
      this._deltas = beanToCopy.getDeltas();
      this._volatilities = beanToCopy.getVolatilities();
      this._isPutDelta = beanToCopy.isIsPutDelta();
      this._timeInterpolator = beanToCopy.getTimeInterpolator();
      this._deltaInterpolator = beanToCopy.getDeltaInterpolator();
    }

    //-----------------------------------------------------------------------
    @Override
    public Object get(String propertyName) {
      switch (propertyName.hashCode()) {
        case 3355:  // id
          return _id;
        case 110364486:  // times
          return _times;
        case -1335444549:  // deltas
          return _deltas;
        case -625639549:  // volatilities
          return _volatilities;
        case 978604339:  // isPutDelta
          return _isPutDelta;
        case -587914188:  // timeInterpolator
          return _timeInterpolator;
        case -1470462817:  // deltaInterpolator
          return _deltaInterpolator;
        default:
          throw new NoSuchElementException("Unknown property: " + propertyName);
      }
    }

    @Override
    public Builder set(String propertyName, Object newValue) {
      switch (propertyName.hashCode()) {
        case 3355:  // id
          this._id = (UniqueIdentifiable) newValue;
          break;
        case 110364486:  // times
          this._times = (double[]) newValue;
          break;
        case -1335444549:  // deltas
          this._deltas = (double[][]) newValue;
          break;
        case -625639549:  // volatilities
          this._volatilities = (double[][]) newValue;
          break;
        case 978604339:  // isPutDelta
          this._isPutDelta = (Boolean) newValue;
          break;
        case -587914188:  // timeInterpolator
          this._timeInterpolator = (Interpolator1D) newValue;
          break;
        case -1470462817:  // deltaInterpolator
          this._deltaInterpolator = (Interpolator1D) newValue;
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
    public TimeDeltaVolatilitySurfaceProvider build() {
      return new TimeDeltaVolatilitySurfaceProvider(
          _id,
          _times,
          _deltas,
          _volatilities,
          _isPutDelta,
          _timeInterpolator,
          _deltaInterpolator);
    }

    //-----------------------------------------------------------------------
    /**
     * Sets the id.
     * @param id  the new value
     * @return this, for chaining, not null
     */
    public Builder id(UniqueIdentifiable id) {
      this._id = id;
      return this;
    }

    /**
     * Sets the times.
     * @param times  the new value
     * @return this, for chaining, not null
     */
    public Builder times(double... times) {
      this._times = times;
      return this;
    }

    /**
     * Sets the deltas.
     * @param deltas  the new value
     * @return this, for chaining, not null
     */
    public Builder deltas(double[][] deltas) {
      this._deltas = deltas;
      return this;
    }

    /**
     * Sets the volatilities.
     * @param volatilities  the new value
     * @return this, for chaining, not null
     */
    public Builder volatilities(double[][] volatilities) {
      this._volatilities = volatilities;
      return this;
    }

    /**
     * Sets the isPutDelta.
     * @param isPutDelta  the new value
     * @return this, for chaining, not null
     */
    public Builder isPutDelta(boolean isPutDelta) {
      this._isPutDelta = isPutDelta;
      return this;
    }

    /**
     * Sets the timeInterpolator.
     * @param timeInterpolator  the new value
     * @return this, for chaining, not null
     */
    public Builder timeInterpolator(Interpolator1D timeInterpolator) {
      this._timeInterpolator = timeInterpolator;
      return this;
    }

    /**
     * Sets the deltaInterpolator.
     * @param deltaInterpolator  the new value
     * @return this, for chaining, not null
     */
    public Builder deltaInterpolator(Interpolator1D deltaInterpolator) {
      this._deltaInterpolator = deltaInterpolator;
      return this;
    }

    //-----------------------------------------------------------------------
    @Override
    public String toString() {
      StringBuilder buf = new StringBuilder(256);
      buf.append("TimeDeltaVolatilitySurfaceProvider.Builder{");
      int len = buf.length();
      toString(buf);
      if (buf.length() > len) {
        buf.setLength(buf.length() - 2);
      }
      buf.append('}');
      return buf.toString();
    }

    protected void toString(StringBuilder buf) {
      buf.append("id").append('=').append(JodaBeanUtils.toString(_id)).append(',').append(' ');
      buf.append("times").append('=').append(JodaBeanUtils.toString(_times)).append(',').append(' ');
      buf.append("deltas").append('=').append(JodaBeanUtils.toString(_deltas)).append(',').append(' ');
      buf.append("volatilities").append('=').append(JodaBeanUtils.toString(_volatilities)).append(',').append(' ');
      buf.append("isPutDelta").append('=').append(JodaBeanUtils.toString(_isPutDelta)).append(',').append(' ');
      buf.append("timeInterpolator").append('=').append(JodaBeanUtils.toString(_timeInterpolator)).append(',').append(' ');
      buf.append("deltaInterpolator").append('=').append(JodaBeanUtils.toString(_deltaInterpolator)).append(',').append(' ');
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
