/**
 *
 */
package com.mcleodmoores.financial.function.trade;

import org.joda.beans.BeanDefinition;
import org.joda.beans.ImmutableBean;
import org.joda.beans.ImmutableConstructor;
import org.joda.beans.PropertyDefinition;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.bond.BillTransactionDefinition;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProviderInterface;
import com.opengamma.util.ArgumentChecker;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import org.joda.beans.Bean;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.impl.direct.DirectFieldsBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaBean;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;

/**
 *
 */
@BeanDefinition
public class DiscountBondDetailsProvider implements ImmutableBean, InstrumentDetailsProvider<BillTransactionDefinition, IssuerProviderInterface> {

  /**
   * The curves data.
   */
  @PropertyDefinition
  private final IssuerProviderInterface _curves;
  /**
   * The valuation time.
   */
  @PropertyDefinition
  private final ZonedDateTime _valuationTime;
  /**
   * The bond definition
   */
  @PropertyDefinition
  private final BillTransactionDefinition _definition;

  @ImmutableConstructor
  public DiscountBondDetailsProvider(final IssuerProviderInterface curves, final ZonedDateTime valuationTime, final BillTransactionDefinition bill) {
    _curves = ArgumentChecker.notNull(curves, "curves");
    _valuationTime = ArgumentChecker.notNull(valuationTime, "valuationTime");
    _definition = ArgumentChecker.notNull(bill, "bill");
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code DiscountBondDetailsProvider}.
   * @return the meta-bean, not null
   */
  public static DiscountBondDetailsProvider.Meta meta() {
    return DiscountBondDetailsProvider.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(DiscountBondDetailsProvider.Meta.INSTANCE);
  }

  /**
   * Returns a builder used to create an instance of the bean.
   * @return the builder, not null
   */
  public static DiscountBondDetailsProvider.Builder builder() {
    return new DiscountBondDetailsProvider.Builder();
  }

  @Override
  public DiscountBondDetailsProvider.Meta metaBean() {
    return DiscountBondDetailsProvider.Meta.INSTANCE;
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
   * Gets the curves data.
   * @return the value of the property
   */
  public IssuerProviderInterface getCurves() {
    return _curves;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the valuation time.
   * @return the value of the property
   */
  public ZonedDateTime getValuationTime() {
    return _valuationTime;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the bond definition
   * @return the value of the property
   */
  public BillTransactionDefinition getDefinition() {
    return _definition;
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
      DiscountBondDetailsProvider other = (DiscountBondDetailsProvider) obj;
      return JodaBeanUtils.equal(getCurves(), other.getCurves()) &&
          JodaBeanUtils.equal(getValuationTime(), other.getValuationTime()) &&
          JodaBeanUtils.equal(getDefinition(), other.getDefinition());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash = hash * 31 + JodaBeanUtils.hashCode(getCurves());
    hash = hash * 31 + JodaBeanUtils.hashCode(getValuationTime());
    hash = hash * 31 + JodaBeanUtils.hashCode(getDefinition());
    return hash;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(128);
    buf.append("DiscountBondDetailsProvider{");
    int len = buf.length();
    toString(buf);
    if (buf.length() > len) {
      buf.setLength(buf.length() - 2);
    }
    buf.append('}');
    return buf.toString();
  }

  protected void toString(StringBuilder buf) {
    buf.append("curves").append('=').append(JodaBeanUtils.toString(getCurves())).append(',').append(' ');
    buf.append("valuationTime").append('=').append(JodaBeanUtils.toString(getValuationTime())).append(',').append(' ');
    buf.append("definition").append('=').append(JodaBeanUtils.toString(getDefinition())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code DiscountBondDetailsProvider}.
   */
  public static class Meta extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code curves} property.
     */
    private final MetaProperty<IssuerProviderInterface> _curves = DirectMetaProperty.ofImmutable(
        this, "curves", DiscountBondDetailsProvider.class, IssuerProviderInterface.class);
    /**
     * The meta-property for the {@code valuationTime} property.
     */
    private final MetaProperty<ZonedDateTime> _valuationTime = DirectMetaProperty.ofImmutable(
        this, "valuationTime", DiscountBondDetailsProvider.class, ZonedDateTime.class);
    /**
     * The meta-property for the {@code definition} property.
     */
    private final MetaProperty<BillTransactionDefinition> _definition = DirectMetaProperty.ofImmutable(
        this, "definition", DiscountBondDetailsProvider.class, BillTransactionDefinition.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "curves",
        "valuationTime",
        "definition");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case -1349116572:  // curves
          return _curves;
        case 113591406:  // valuationTime
          return _valuationTime;
        case -1014418093:  // definition
          return _definition;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public DiscountBondDetailsProvider.Builder builder() {
      return new DiscountBondDetailsProvider.Builder();
    }

    @Override
    public Class<? extends DiscountBondDetailsProvider> beanType() {
      return DiscountBondDetailsProvider.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code curves} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<IssuerProviderInterface> curves() {
      return _curves;
    }

    /**
     * The meta-property for the {@code valuationTime} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ZonedDateTime> valuationTime() {
      return _valuationTime;
    }

    /**
     * The meta-property for the {@code definition} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<BillTransactionDefinition> definition() {
      return _definition;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -1349116572:  // curves
          return ((DiscountBondDetailsProvider) bean).getCurves();
        case 113591406:  // valuationTime
          return ((DiscountBondDetailsProvider) bean).getValuationTime();
        case -1014418093:  // definition
          return ((DiscountBondDetailsProvider) bean).getDefinition();
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
   * The bean-builder for {@code DiscountBondDetailsProvider}.
   */
  public static class Builder extends DirectFieldsBeanBuilder<DiscountBondDetailsProvider> {

    private IssuerProviderInterface _curves;
    private ZonedDateTime _valuationTime;
    private BillTransactionDefinition _definition;

    /**
     * Restricted constructor.
     */
    protected Builder() {
    }

    /**
     * Restricted copy constructor.
     * @param beanToCopy  the bean to copy from, not null
     */
    protected Builder(DiscountBondDetailsProvider beanToCopy) {
      this._curves = beanToCopy.getCurves();
      this._valuationTime = beanToCopy.getValuationTime();
      this._definition = beanToCopy.getDefinition();
    }

    //-----------------------------------------------------------------------
    @Override
    public Object get(String propertyName) {
      switch (propertyName.hashCode()) {
        case -1349116572:  // curves
          return _curves;
        case 113591406:  // valuationTime
          return _valuationTime;
        case -1014418093:  // definition
          return _definition;
        default:
          throw new NoSuchElementException("Unknown property: " + propertyName);
      }
    }

    @Override
    public Builder set(String propertyName, Object newValue) {
      switch (propertyName.hashCode()) {
        case -1349116572:  // curves
          this._curves = (IssuerProviderInterface) newValue;
          break;
        case 113591406:  // valuationTime
          this._valuationTime = (ZonedDateTime) newValue;
          break;
        case -1014418093:  // definition
          this._definition = (BillTransactionDefinition) newValue;
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
    public DiscountBondDetailsProvider build() {
      return new DiscountBondDetailsProvider(
          _curves,
          _valuationTime,
          _definition);
    }

    //-----------------------------------------------------------------------
    /**
     * Sets the curves data.
     * @param curves  the new value
     * @return this, for chaining, not null
     */
    public Builder curves(IssuerProviderInterface curves) {
      this._curves = curves;
      return this;
    }

    /**
     * Sets the valuation time.
     * @param valuationTime  the new value
     * @return this, for chaining, not null
     */
    public Builder valuationTime(ZonedDateTime valuationTime) {
      this._valuationTime = valuationTime;
      return this;
    }

    /**
     * Sets the bond definition
     * @param definition  the new value
     * @return this, for chaining, not null
     */
    public Builder definition(BillTransactionDefinition definition) {
      this._definition = definition;
      return this;
    }

    //-----------------------------------------------------------------------
    @Override
    public String toString() {
      StringBuilder buf = new StringBuilder(128);
      buf.append("DiscountBondDetailsProvider.Builder{");
      int len = buf.length();
      toString(buf);
      if (buf.length() > len) {
        buf.setLength(buf.length() - 2);
      }
      buf.append('}');
      return buf.toString();
    }

    protected void toString(StringBuilder buf) {
      buf.append("curves").append('=').append(JodaBeanUtils.toString(_curves)).append(',').append(' ');
      buf.append("valuationTime").append('=').append(JodaBeanUtils.toString(_valuationTime)).append(',').append(' ');
      buf.append("definition").append('=').append(JodaBeanUtils.toString(_definition)).append(',').append(' ');
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
