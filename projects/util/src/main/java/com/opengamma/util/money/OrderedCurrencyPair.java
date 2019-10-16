/**
 * Copyright (C) 2017 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.util.money;

import java.io.Serializable;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.regex.Pattern;

import org.joda.beans.Bean;
import org.joda.beans.BeanBuilder;
import org.joda.beans.ImmutableBean;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaBean;
import org.joda.beans.MetaProperty;
import org.joda.beans.gen.BeanDefinition;
import org.joda.beans.gen.ImmutableConstructor;
import org.joda.beans.gen.PropertyDefinition;
import org.joda.beans.impl.direct.DirectMetaBean;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;
import org.joda.beans.impl.direct.DirectPrivateBeanBuilder;
import org.joda.convert.FromString;

import com.opengamma.id.ObjectId;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
@BeanDefinition(builderScope = "private")
public final class OrderedCurrencyPair implements ImmutableBean, UniqueIdentifiable, ObjectIdentifiable, Serializable {
  private static final long serialVersionUID = 1L;

  /**
   * The scheme to use in the identifiers.
   */
  public static final String OBJECT_SCHEME = "OrderedCurrencyPair";

  /**
   * The first currency.
   */
  @PropertyDefinition(validate = "notNull")
  private final Currency _firstCurrency;
  /**
   * The second currency.
   */
  @PropertyDefinition(validate = "notNull")
  private final Currency _secondCurrency;
  private final String _idValue;

  /**
   * Obtains an {@code OrderedCurrencyPair} from two currencies.
   *
   * @param ccy1
   *          the first currency, not null
   * @param ccy2
   *          the second currency, not null
   * @return the pair, not null
   */
  public static OrderedCurrencyPair of(final Currency ccy1, final Currency ccy2) {
    return new OrderedCurrencyPair(ccy1, ccy2);
  }

  /**
   * Extracts an {@code OrderedCurrencyPair} from a unique identifier.
   *
   * @param uniqueId
   *          the unique identifier, not null
   * @return the pair, not null
   * @throws IllegalArgumentException
   *           if the input is invalid
   */
  public static OrderedCurrencyPair of(final UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    if (uniqueId.getScheme().equals(OBJECT_SCHEME)) {
      final Pattern validate = Pattern.compile("[A-Z]{6}");
      final String value = uniqueId.getValue();
      if (validate.matcher(value).matches()) {
        final Currency ccy1 = Currency.of(value.substring(0, 3));
        final Currency ccy2 = Currency.of(value.substring(3));
        return new OrderedCurrencyPair(ccy1, ccy2);
      }
    }
    throw new IllegalArgumentException("Cannot create an OrderedCurrencyPair from this "
        + "UniqueId; " + uniqueId.getScheme());
  }

  /**
   * Parses the string to produce a {@code OrderedCurrencyPair}.
   * <p>
   * This parses the {@code toString} format of '${currency1}${currency2}'
   *
   * @param string
   *          the amount string, not null
   * @return the currency amount
   * @throws IllegalArgumentException
   *           if the amount cannot be parsed
   */
  @FromString
  public static OrderedCurrencyPair parse(final String string) {
    ArgumentChecker.notNull(string, "string");
    ArgumentChecker.isTrue(string.length() == 6, "Unable to parse OrderedCurrencyPair, invalid format: {}", string);
    try {
      final Currency cur1 = Currency.parse(string.substring(0, 3));
      final Currency cur2 = Currency.parse(string.substring(3));
      return new OrderedCurrencyPair(cur1, cur2);
    } catch (final RuntimeException ex) {
      throw new IllegalArgumentException("Unable to parse string: " + string, ex);
    }
  }

  @ImmutableConstructor
  private OrderedCurrencyPair(final Currency ccy1, final Currency ccy2) {
    _firstCurrency = ArgumentChecker.notNull(ccy1, "ccy1");
    _secondCurrency = ArgumentChecker.notNull(ccy2, "ccy2");
    _idValue = _firstCurrency.getCode() + _secondCurrency.getCode();
  }

  @Override
  public ObjectId getObjectId() {
    return ObjectId.of(OBJECT_SCHEME, _idValue);
  }

  @Override
  public UniqueId getUniqueId() {
    return UniqueId.of(OBJECT_SCHEME, _idValue);
  }

  /**
   * True if the input is the inverse pair of this ordered pair i.e. the numerator and denominator are switched.
   *
   * @param other
   *          the other pair, not null
   * @return true if the input is the inverse of this pair
   */
  public boolean isInverse(final OrderedCurrencyPair other) {
    ArgumentChecker.notNull(other, "other");
    return other._firstCurrency.equals(_secondCurrency) && other._secondCurrency.equals(_firstCurrency);
  }

  /**
   * Gets the inverse of this pair.
   *
   * @return the inverse pair
   */
  public OrderedCurrencyPair getInverse() {
    return new OrderedCurrencyPair(_secondCurrency, _firstCurrency);
  }

  //------------------------- AUTOGENERATED START -------------------------
  /**
   * The meta-bean for {@code OrderedCurrencyPair}.
   * @return the meta-bean, not null
   */
  public static OrderedCurrencyPair.Meta meta() {
    return OrderedCurrencyPair.Meta.INSTANCE;
  }

  static {
    MetaBean.register(OrderedCurrencyPair.Meta.INSTANCE);
  }

  @Override
  public OrderedCurrencyPair.Meta metaBean() {
    return OrderedCurrencyPair.Meta.INSTANCE;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the first currency.
   * @return the value of the property, not null
   */
  public Currency getFirstCurrency() {
    return _firstCurrency;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the second currency.
   * @return the value of the property, not null
   */
  public Currency getSecondCurrency() {
    return _secondCurrency;
  }

  //-----------------------------------------------------------------------
  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      OrderedCurrencyPair other = (OrderedCurrencyPair) obj;
      return JodaBeanUtils.equal(_firstCurrency, other._firstCurrency) &&
          JodaBeanUtils.equal(_secondCurrency, other._secondCurrency);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash = hash * 31 + JodaBeanUtils.hashCode(_firstCurrency);
    hash = hash * 31 + JodaBeanUtils.hashCode(_secondCurrency);
    return hash;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(96);
    buf.append("OrderedCurrencyPair{");
    buf.append("firstCurrency").append('=').append(_firstCurrency).append(',').append(' ');
    buf.append("secondCurrency").append('=').append(JodaBeanUtils.toString(_secondCurrency));
    buf.append('}');
    return buf.toString();
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code OrderedCurrencyPair}.
   */
  public static final class Meta extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code firstCurrency} property.
     */
    private final MetaProperty<Currency> _firstCurrency = DirectMetaProperty.ofImmutable(
        this, "firstCurrency", OrderedCurrencyPair.class, Currency.class);
    /**
     * The meta-property for the {@code secondCurrency} property.
     */
    private final MetaProperty<Currency> _secondCurrency = DirectMetaProperty.ofImmutable(
        this, "secondCurrency", OrderedCurrencyPair.class, Currency.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "firstCurrency",
        "secondCurrency");

    /**
     * Restricted constructor.
     */
    private Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case -1878034719:  // firstCurrency
          return _firstCurrency;
        case 564126885:  // secondCurrency
          return _secondCurrency;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends OrderedCurrencyPair> builder() {
      return new OrderedCurrencyPair.Builder();
    }

    @Override
    public Class<? extends OrderedCurrencyPair> beanType() {
      return OrderedCurrencyPair.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code firstCurrency} property.
     * @return the meta-property, not null
     */
    public MetaProperty<Currency> firstCurrency() {
      return _firstCurrency;
    }

    /**
     * The meta-property for the {@code secondCurrency} property.
     * @return the meta-property, not null
     */
    public MetaProperty<Currency> secondCurrency() {
      return _secondCurrency;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -1878034719:  // firstCurrency
          return ((OrderedCurrencyPair) bean).getFirstCurrency();
        case 564126885:  // secondCurrency
          return ((OrderedCurrencyPair) bean).getSecondCurrency();
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
   * The bean-builder for {@code OrderedCurrencyPair}.
   */
  private static final class Builder extends DirectPrivateBeanBuilder<OrderedCurrencyPair> {

    private Currency _firstCurrency;
    private Currency _secondCurrency;

    /**
     * Restricted constructor.
     */
    private Builder() {
    }

    //-----------------------------------------------------------------------
    @Override
    public Object get(String propertyName) {
      switch (propertyName.hashCode()) {
        case -1878034719:  // firstCurrency
          return _firstCurrency;
        case 564126885:  // secondCurrency
          return _secondCurrency;
        default:
          throw new NoSuchElementException("Unknown property: " + propertyName);
      }
    }

    @Override
    public Builder set(String propertyName, Object newValue) {
      switch (propertyName.hashCode()) {
        case -1878034719:  // firstCurrency
          this._firstCurrency = (Currency) newValue;
          break;
        case 564126885:  // secondCurrency
          this._secondCurrency = (Currency) newValue;
          break;
        default:
          throw new NoSuchElementException("Unknown property: " + propertyName);
      }
      return this;
    }

    @Override
    public OrderedCurrencyPair build() {
      return new OrderedCurrencyPair(
          _firstCurrency,
          _secondCurrency);
    }

    //-----------------------------------------------------------------------
    @Override
    public String toString() {
      StringBuilder buf = new StringBuilder(96);
      buf.append("OrderedCurrencyPair.Builder{");
      buf.append("firstCurrency").append('=').append(JodaBeanUtils.toString(_firstCurrency)).append(',').append(' ');
      buf.append("secondCurrency").append('=').append(JodaBeanUtils.toString(_secondCurrency));
      buf.append('}');
      return buf.toString();
    }

  }

  //-------------------------- AUTOGENERATED END --------------------------
}
