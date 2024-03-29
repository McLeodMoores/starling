/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.option;

import java.util.Map;

import org.joda.beans.Bean;
import org.joda.beans.BeanBuilder;
import org.joda.beans.BeanDefinition;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.util.time.Expiry;

/**
 * The simple chooser payoff style.
 */
@BeanDefinition
public class SimpleChooserPayoffStyle extends PayoffStyle {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  /**
   * The choose date.
   */
  @PropertyDefinition(validate = "notNull")
  private ZonedDateTime _chooseDate;
  /**
   * The underlying strike.
   */
  @PropertyDefinition
  private double _underlyingStrike;
  /**
   * The underlying expiry.
   */
  @PropertyDefinition(validate = "notNull")
  private Expiry _underlyingExpiry;

  /**
   * Creates an instance.
   */
  private SimpleChooserPayoffStyle() {
  }

  /**
   * Creates an instance.
   *
   * @param chooseDate  the choose date, not null
   * @param underlyingStrike  the underlying strike
   * @param underlyingExpiry  the underlying expiry, not null
   */
  public SimpleChooserPayoffStyle(final ZonedDateTime chooseDate, final double underlyingStrike, final Expiry underlyingExpiry) {
    setChooseDate(chooseDate);
    setUnderlyingStrike(underlyingStrike);
    setUnderlyingExpiry(underlyingExpiry);
  }

  //-------------------------------------------------------------------------
  @Override
  public <T> T accept(final PayoffStyleVisitor<T> visitor) {
    return visitor.visitSimpleChooserPayoffStyle(this);
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code SimpleChooserPayoffStyle}.
   * @return the meta-bean, not null
   */
  public static SimpleChooserPayoffStyle.Meta meta() {
    return SimpleChooserPayoffStyle.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(SimpleChooserPayoffStyle.Meta.INSTANCE);
  }

  @Override
  public SimpleChooserPayoffStyle.Meta metaBean() {
    return SimpleChooserPayoffStyle.Meta.INSTANCE;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the choose date.
   * @return the value of the property, not null
   */
  public ZonedDateTime getChooseDate() {
    return _chooseDate;
  }

  /**
   * Sets the choose date.
   * @param chooseDate  the new value of the property, not null
   */
  public void setChooseDate(ZonedDateTime chooseDate) {
    JodaBeanUtils.notNull(chooseDate, "chooseDate");
    this._chooseDate = chooseDate;
  }

  /**
   * Gets the the {@code chooseDate} property.
   * @return the property, not null
   */
  public final Property<ZonedDateTime> chooseDate() {
    return metaBean().chooseDate().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the underlying strike.
   * @return the value of the property
   */
  public double getUnderlyingStrike() {
    return _underlyingStrike;
  }

  /**
   * Sets the underlying strike.
   * @param underlyingStrike  the new value of the property
   */
  public void setUnderlyingStrike(double underlyingStrike) {
    this._underlyingStrike = underlyingStrike;
  }

  /**
   * Gets the the {@code underlyingStrike} property.
   * @return the property, not null
   */
  public final Property<Double> underlyingStrike() {
    return metaBean().underlyingStrike().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the underlying expiry.
   * @return the value of the property, not null
   */
  public Expiry getUnderlyingExpiry() {
    return _underlyingExpiry;
  }

  /**
   * Sets the underlying expiry.
   * @param underlyingExpiry  the new value of the property, not null
   */
  public void setUnderlyingExpiry(Expiry underlyingExpiry) {
    JodaBeanUtils.notNull(underlyingExpiry, "underlyingExpiry");
    this._underlyingExpiry = underlyingExpiry;
  }

  /**
   * Gets the the {@code underlyingExpiry} property.
   * @return the property, not null
   */
  public final Property<Expiry> underlyingExpiry() {
    return metaBean().underlyingExpiry().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public SimpleChooserPayoffStyle clone() {
    return JodaBeanUtils.cloneAlways(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      SimpleChooserPayoffStyle other = (SimpleChooserPayoffStyle) obj;
      return JodaBeanUtils.equal(getChooseDate(), other.getChooseDate()) &&
          JodaBeanUtils.equal(getUnderlyingStrike(), other.getUnderlyingStrike()) &&
          JodaBeanUtils.equal(getUnderlyingExpiry(), other.getUnderlyingExpiry()) &&
          super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = hash * 31 + JodaBeanUtils.hashCode(getChooseDate());
    hash = hash * 31 + JodaBeanUtils.hashCode(getUnderlyingStrike());
    hash = hash * 31 + JodaBeanUtils.hashCode(getUnderlyingExpiry());
    return hash ^ super.hashCode();
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(128);
    buf.append("SimpleChooserPayoffStyle{");
    int len = buf.length();
    toString(buf);
    if (buf.length() > len) {
      buf.setLength(buf.length() - 2);
    }
    buf.append('}');
    return buf.toString();
  }

  @Override
  protected void toString(StringBuilder buf) {
    super.toString(buf);
    buf.append("chooseDate").append('=').append(JodaBeanUtils.toString(getChooseDate())).append(',').append(' ');
    buf.append("underlyingStrike").append('=').append(JodaBeanUtils.toString(getUnderlyingStrike())).append(',').append(' ');
    buf.append("underlyingExpiry").append('=').append(JodaBeanUtils.toString(getUnderlyingExpiry())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code SimpleChooserPayoffStyle}.
   */
  public static class Meta extends PayoffStyle.Meta {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code chooseDate} property.
     */
    private final MetaProperty<ZonedDateTime> _chooseDate = DirectMetaProperty.ofReadWrite(
        this, "chooseDate", SimpleChooserPayoffStyle.class, ZonedDateTime.class);
    /**
     * The meta-property for the {@code underlyingStrike} property.
     */
    private final MetaProperty<Double> _underlyingStrike = DirectMetaProperty.ofReadWrite(
        this, "underlyingStrike", SimpleChooserPayoffStyle.class, Double.TYPE);
    /**
     * The meta-property for the {@code underlyingExpiry} property.
     */
    private final MetaProperty<Expiry> _underlyingExpiry = DirectMetaProperty.ofReadWrite(
        this, "underlyingExpiry", SimpleChooserPayoffStyle.class, Expiry.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "chooseDate",
        "underlyingStrike",
        "underlyingExpiry");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case 2023159397:  // chooseDate
          return _chooseDate;
        case 205707631:  // underlyingStrike
          return _underlyingStrike;
        case -191465744:  // underlyingExpiry
          return _underlyingExpiry;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends SimpleChooserPayoffStyle> builder() {
      return new DirectBeanBuilder<SimpleChooserPayoffStyle>(new SimpleChooserPayoffStyle());
    }

    @Override
    public Class<? extends SimpleChooserPayoffStyle> beanType() {
      return SimpleChooserPayoffStyle.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code chooseDate} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ZonedDateTime> chooseDate() {
      return _chooseDate;
    }

    /**
     * The meta-property for the {@code underlyingStrike} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Double> underlyingStrike() {
      return _underlyingStrike;
    }

    /**
     * The meta-property for the {@code underlyingExpiry} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Expiry> underlyingExpiry() {
      return _underlyingExpiry;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 2023159397:  // chooseDate
          return ((SimpleChooserPayoffStyle) bean).getChooseDate();
        case 205707631:  // underlyingStrike
          return ((SimpleChooserPayoffStyle) bean).getUnderlyingStrike();
        case -191465744:  // underlyingExpiry
          return ((SimpleChooserPayoffStyle) bean).getUnderlyingExpiry();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 2023159397:  // chooseDate
          ((SimpleChooserPayoffStyle) bean).setChooseDate((ZonedDateTime) newValue);
          return;
        case 205707631:  // underlyingStrike
          ((SimpleChooserPayoffStyle) bean).setUnderlyingStrike((Double) newValue);
          return;
        case -191465744:  // underlyingExpiry
          ((SimpleChooserPayoffStyle) bean).setUnderlyingExpiry((Expiry) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

    @Override
    protected void validate(Bean bean) {
      JodaBeanUtils.notNull(((SimpleChooserPayoffStyle) bean)._chooseDate, "chooseDate");
      JodaBeanUtils.notNull(((SimpleChooserPayoffStyle) bean)._underlyingExpiry, "underlyingExpiry");
      super.validate(bean);
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
