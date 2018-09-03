/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.fixedincome;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.joda.beans.Bean;
import org.joda.beans.BeanDefinition;
import org.joda.beans.DerivedProperty;
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
import org.threeten.bp.LocalDate;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.CurrencyAmount;

/**
 * Container for the relevant details for pricing a fixed swap leg, with the entries
 * <ul>
 * <li>Start accrual date</li>
 * <li>End accrual date</li>
 * <li>Payment time</li>
 * <li>Payment year fraction</li>
 * <li>Payment amount (non discounted)</li>
 * <li>Discount factor</li>
 * <li>Notional</li>
 * <li>Rate</li>
 * <li>Discounted payment amount</li>
 * <ul>
 * There is an entry for each coupon in a fixed leg.
 */
@BeanDefinition
public class FixedLegCashFlows implements ImmutableBean, SwapLegCashFlows {

  //TODO replace these static strings with an annotation and reference linked to the corresponding variable - PLAT-6507
  /**
   * The payment year fraction label.
   */
  public static final String PAYMENT_YEAR_FRACTION = "Payment Year Fraction";
  /**
   * The payment amount label.
   */
  public static final String PAYMENT_AMOUNT = "Payment Amount";
  /**
   * The discount factor label.
   */
  public static final String DISCOUNT_FACTOR = "Discount Factor";
  /**
   * The fixed rate label.
   */
  public static final String FIXED_RATE = "Fixed Rate";
  /**
   * The discounted payment amount
   */
  public static final String DISCOUNTED_PAYMENT_AMOUNT = "Discounted Payment Amount";
  /**
   * The start accrual dates label.
   */
  public static final String START_ACCRUAL_DATES = "Start Accrual Date";
  /**
   * The end accrual dates label.
   */
  public static final String END_ACCRUAL_DATES = "End Accrual Date";
  /**
   * The notional label.
   */
  public static final String NOTIONAL = "Notional";
  /**
   * The payment time label.
   */
  public static final String PAYMENT_TIME = "Payment Time";
  /**
   * An array of accrual start dates.
   */
  @PropertyDefinition(validate = "notNull")
  private final List<LocalDate> _accrualStart;
  /**
   * An array of accrual end dates.
   */
  @PropertyDefinition(validate = "notNull")
  private final List<LocalDate> _accrualEnd;
  /**
   * An array of discount factors for the payments.
   */
  @PropertyDefinition(validate = "notNull")
  private final List<Double> _discountFactors;
  /**
   * An array of payment times.
   */
  @PropertyDefinition(validate = "notNull")
  private final List<Double> _paymentTimes;
  /**
   * An array of payment year fractions.
   */
  @PropertyDefinition(validate = "notNull")
  private final List<Double> _paymentFractions;
  /**
   * An array of payment amounts.
   */
  @PropertyDefinition(validate = "notNull")
  private final List<CurrencyAmount> _paymentAmounts;
  /**
   * An array of notionals.
   */
  @PropertyDefinition(validate = "notNull")
  private final List<CurrencyAmount> _notionals;
  /**
   * An array of fixed rates.
   */
  @PropertyDefinition(validate = "notNull")
  private final List<Double> _fixedRates;

  /**
   * All arrays must be the same length.
   * @param startAccrualDates The start accrual dates, not null
   * @param endAccrualDates The end accrual dates, not null
   * @param discountFactors The discount factors, not null
   * @param paymentTimes The payment times, not null
   * @param paymentFractions The payment year fractions, not null
   * @param paymentAmounts The payment amounts, not null
   * @param notionals The notionals, not null
   * @param fixedRates The fixed rates, not null
   */
  @ImmutableConstructor
  public FixedLegCashFlows(final List<LocalDate> startAccrualDates,
                           final List<LocalDate> endAccrualDates,
                           final List<Double> discountFactors,
                           final List<Double> paymentTimes,
                           final List<Double> paymentFractions,
                           final List<CurrencyAmount> paymentAmounts,
                           final List<CurrencyAmount> notionals,
                           final List<Double> fixedRates) {

    ArgumentChecker.notNull(startAccrualDates, "startAccrualDates");
    ArgumentChecker.notNull(endAccrualDates, "endAccrualDates");
    ArgumentChecker.notNull(discountFactors, "discountFactors");
    ArgumentChecker.notNull(paymentTimes, "paymentTimes");
    ArgumentChecker.notNull(paymentFractions, "paymentFractions");
    ArgumentChecker.notNull(paymentAmounts, "paymentAmounts");
    ArgumentChecker.notNull(notionals, "notionals");
    ArgumentChecker.notNull(fixedRates, "fixedRates");

    _accrualStart = Collections.unmodifiableList(Lists.newArrayList(startAccrualDates));
    _accrualEnd = Collections.unmodifiableList(Lists.newArrayList(endAccrualDates));
    _notionals = Collections.unmodifiableList(Lists.newArrayList(notionals));
    _paymentTimes = Collections.unmodifiableList(Lists.newArrayList(paymentTimes));
    _discountFactors = Collections.unmodifiableList(Lists.newArrayList(discountFactors));
    _paymentFractions = Collections.unmodifiableList(Lists.newArrayList(paymentFractions));
    _paymentAmounts = Collections.unmodifiableList(Lists.newArrayList(paymentAmounts));
    _fixedRates = Collections.unmodifiableList(Lists.newArrayList(fixedRates));

    final int n = startAccrualDates.size();
    ArgumentChecker.isTrue(n == endAccrualDates.size(), "Must have same number of start and end accrual dates");
    ArgumentChecker.isTrue(n == discountFactors.size(), "Must have same number of start accrual dates and discount factors");
    ArgumentChecker.isTrue(n == paymentTimes.size(), "Must have same number of start accrual dates and payment times");
    ArgumentChecker.isTrue(n == paymentFractions.size(), "Must have same number of start accrual dates and payment year fractions");
    ArgumentChecker.isTrue(n == paymentAmounts.size(), "Must have same number of start accrual dates and payment amounts");
    ArgumentChecker.isTrue(n == notionals.size(), "Must have same number of start accrual dates and notionals");
    ArgumentChecker.isTrue(n == fixedRates.size(), "Must have same number of start accrual dates and fixed rates");
  }

  /**
   * Gets the discounted payment amounts.
   * @return the discounted cashflows
   */
  @DerivedProperty
  public List<CurrencyAmount> getDiscountedPaymentAmounts() {
    final List<CurrencyAmount> cashflows = new ArrayList<>();
    for (int i = 0; i < getNumberOfCashFlows(); i++) {
      final CurrencyAmount payment = getPaymentAmounts().get(i);
      if (payment == null) {
        cashflows.add(null);
        continue;
      }
      final double df = getDiscountFactors().get(i);
      cashflows.add(CurrencyAmount.of(payment.getCurrency(), payment.getAmount() * df));
    }
    return cashflows;
  }

  /**
   * Gets the total number of cash-flows.
   * @return The total number of cash-flows
   */
  @DerivedProperty
  public int getNumberOfCashFlows() {
    return getNotionals().size();
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code FixedLegCashFlows}.
   * @return the meta-bean, not null
   */
  public static FixedLegCashFlows.Meta meta() {
    return FixedLegCashFlows.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(FixedLegCashFlows.Meta.INSTANCE);
  }

  /**
   * Returns a builder used to create an instance of the bean.
   * @return the builder, not null
   */
  public static FixedLegCashFlows.Builder builder() {
    return new FixedLegCashFlows.Builder();
  }

  @Override
  public FixedLegCashFlows.Meta metaBean() {
    return FixedLegCashFlows.Meta.INSTANCE;
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
   * Gets an array of accrual start dates.
   * @return the value of the property, not null
   */
  public List<LocalDate> getAccrualStart() {
    return _accrualStart;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets an array of accrual end dates.
   * @return the value of the property, not null
   */
  public List<LocalDate> getAccrualEnd() {
    return _accrualEnd;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets an array of discount factors for the payments.
   * @return the value of the property, not null
   */
  public List<Double> getDiscountFactors() {
    return _discountFactors;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets an array of payment times.
   * @return the value of the property, not null
   */
  public List<Double> getPaymentTimes() {
    return _paymentTimes;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets an array of payment year fractions.
   * @return the value of the property, not null
   */
  public List<Double> getPaymentFractions() {
    return _paymentFractions;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets an array of payment amounts.
   * @return the value of the property, not null
   */
  public List<CurrencyAmount> getPaymentAmounts() {
    return _paymentAmounts;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets an array of notionals.
   * @return the value of the property, not null
   */
  public List<CurrencyAmount> getNotionals() {
    return _notionals;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets an array of fixed rates.
   * @return the value of the property, not null
   */
  public List<Double> getFixedRates() {
    return _fixedRates;
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
      FixedLegCashFlows other = (FixedLegCashFlows) obj;
      return JodaBeanUtils.equal(_accrualStart, other._accrualStart) &&
          JodaBeanUtils.equal(_accrualEnd, other._accrualEnd) &&
          JodaBeanUtils.equal(_discountFactors, other._discountFactors) &&
          JodaBeanUtils.equal(_paymentTimes, other._paymentTimes) &&
          JodaBeanUtils.equal(_paymentFractions, other._paymentFractions) &&
          JodaBeanUtils.equal(_paymentAmounts, other._paymentAmounts) &&
          JodaBeanUtils.equal(_notionals, other._notionals) &&
          JodaBeanUtils.equal(_fixedRates, other._fixedRates);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash = hash * 31 + JodaBeanUtils.hashCode(_accrualStart);
    hash = hash * 31 + JodaBeanUtils.hashCode(_accrualEnd);
    hash = hash * 31 + JodaBeanUtils.hashCode(_discountFactors);
    hash = hash * 31 + JodaBeanUtils.hashCode(_paymentTimes);
    hash = hash * 31 + JodaBeanUtils.hashCode(_paymentFractions);
    hash = hash * 31 + JodaBeanUtils.hashCode(_paymentAmounts);
    hash = hash * 31 + JodaBeanUtils.hashCode(_notionals);
    hash = hash * 31 + JodaBeanUtils.hashCode(_fixedRates);
    return hash;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(288);
    buf.append("FixedLegCashFlows{");
    int len = buf.length();
    toString(buf);
    if (buf.length() > len) {
      buf.setLength(buf.length() - 2);
    }
    buf.append('}');
    return buf.toString();
  }

  protected void toString(StringBuilder buf) {
    buf.append("accrualStart").append('=').append(JodaBeanUtils.toString(_accrualStart)).append(',').append(' ');
    buf.append("accrualEnd").append('=').append(JodaBeanUtils.toString(_accrualEnd)).append(',').append(' ');
    buf.append("discountFactors").append('=').append(JodaBeanUtils.toString(_discountFactors)).append(',').append(' ');
    buf.append("paymentTimes").append('=').append(JodaBeanUtils.toString(_paymentTimes)).append(',').append(' ');
    buf.append("paymentFractions").append('=').append(JodaBeanUtils.toString(_paymentFractions)).append(',').append(' ');
    buf.append("paymentAmounts").append('=').append(JodaBeanUtils.toString(_paymentAmounts)).append(',').append(' ');
    buf.append("notionals").append('=').append(JodaBeanUtils.toString(_notionals)).append(',').append(' ');
    buf.append("fixedRates").append('=').append(JodaBeanUtils.toString(_fixedRates)).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code FixedLegCashFlows}.
   */
  public static class Meta extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code accrualStart} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<List<LocalDate>> _accrualStart = DirectMetaProperty.ofImmutable(
        this, "accrualStart", FixedLegCashFlows.class, (Class) List.class);
    /**
     * The meta-property for the {@code accrualEnd} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<List<LocalDate>> _accrualEnd = DirectMetaProperty.ofImmutable(
        this, "accrualEnd", FixedLegCashFlows.class, (Class) List.class);
    /**
     * The meta-property for the {@code discountFactors} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<List<Double>> _discountFactors = DirectMetaProperty.ofImmutable(
        this, "discountFactors", FixedLegCashFlows.class, (Class) List.class);
    /**
     * The meta-property for the {@code paymentTimes} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<List<Double>> _paymentTimes = DirectMetaProperty.ofImmutable(
        this, "paymentTimes", FixedLegCashFlows.class, (Class) List.class);
    /**
     * The meta-property for the {@code paymentFractions} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<List<Double>> _paymentFractions = DirectMetaProperty.ofImmutable(
        this, "paymentFractions", FixedLegCashFlows.class, (Class) List.class);
    /**
     * The meta-property for the {@code paymentAmounts} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<List<CurrencyAmount>> _paymentAmounts = DirectMetaProperty.ofImmutable(
        this, "paymentAmounts", FixedLegCashFlows.class, (Class) List.class);
    /**
     * The meta-property for the {@code notionals} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<List<CurrencyAmount>> _notionals = DirectMetaProperty.ofImmutable(
        this, "notionals", FixedLegCashFlows.class, (Class) List.class);
    /**
     * The meta-property for the {@code fixedRates} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<List<Double>> _fixedRates = DirectMetaProperty.ofImmutable(
        this, "fixedRates", FixedLegCashFlows.class, (Class) List.class);
    /**
     * The meta-property for the {@code discountedPaymentAmounts} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<List<CurrencyAmount>> _discountedPaymentAmounts = DirectMetaProperty.ofDerived(
        this, "discountedPaymentAmounts", FixedLegCashFlows.class, (Class) List.class);
    /**
     * The meta-property for the {@code numberOfCashFlows} property.
     */
    private final MetaProperty<Integer> _numberOfCashFlows = DirectMetaProperty.ofDerived(
        this, "numberOfCashFlows", FixedLegCashFlows.class, Integer.TYPE);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "accrualStart",
        "accrualEnd",
        "discountFactors",
        "paymentTimes",
        "paymentFractions",
        "paymentAmounts",
        "notionals",
        "fixedRates",
        "discountedPaymentAmounts",
        "numberOfCashFlows");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case 1071260659:  // accrualStart
          return _accrualStart;
        case 1846909100:  // accrualEnd
          return _accrualEnd;
        case -91613053:  // discountFactors
          return _discountFactors;
        case -507430688:  // paymentTimes
          return _paymentTimes;
        case 1206997835:  // paymentFractions
          return _paymentFractions;
        case -1875448267:  // paymentAmounts
          return _paymentAmounts;
        case 1910080819:  // notionals
          return _notionals;
        case 1695350911:  // fixedRates
          return _fixedRates;
        case 178231285:  // discountedPaymentAmounts
          return _discountedPaymentAmounts;
        case -338982286:  // numberOfCashFlows
          return _numberOfCashFlows;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public FixedLegCashFlows.Builder builder() {
      return new FixedLegCashFlows.Builder();
    }

    @Override
    public Class<? extends FixedLegCashFlows> beanType() {
      return FixedLegCashFlows.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code accrualStart} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<List<LocalDate>> accrualStart() {
      return _accrualStart;
    }

    /**
     * The meta-property for the {@code accrualEnd} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<List<LocalDate>> accrualEnd() {
      return _accrualEnd;
    }

    /**
     * The meta-property for the {@code discountFactors} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<List<Double>> discountFactors() {
      return _discountFactors;
    }

    /**
     * The meta-property for the {@code paymentTimes} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<List<Double>> paymentTimes() {
      return _paymentTimes;
    }

    /**
     * The meta-property for the {@code paymentFractions} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<List<Double>> paymentFractions() {
      return _paymentFractions;
    }

    /**
     * The meta-property for the {@code paymentAmounts} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<List<CurrencyAmount>> paymentAmounts() {
      return _paymentAmounts;
    }

    /**
     * The meta-property for the {@code notionals} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<List<CurrencyAmount>> notionals() {
      return _notionals;
    }

    /**
     * The meta-property for the {@code fixedRates} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<List<Double>> fixedRates() {
      return _fixedRates;
    }

    /**
     * The meta-property for the {@code discountedPaymentAmounts} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<List<CurrencyAmount>> discountedPaymentAmounts() {
      return _discountedPaymentAmounts;
    }

    /**
     * The meta-property for the {@code numberOfCashFlows} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Integer> numberOfCashFlows() {
      return _numberOfCashFlows;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 1071260659:  // accrualStart
          return ((FixedLegCashFlows) bean).getAccrualStart();
        case 1846909100:  // accrualEnd
          return ((FixedLegCashFlows) bean).getAccrualEnd();
        case -91613053:  // discountFactors
          return ((FixedLegCashFlows) bean).getDiscountFactors();
        case -507430688:  // paymentTimes
          return ((FixedLegCashFlows) bean).getPaymentTimes();
        case 1206997835:  // paymentFractions
          return ((FixedLegCashFlows) bean).getPaymentFractions();
        case -1875448267:  // paymentAmounts
          return ((FixedLegCashFlows) bean).getPaymentAmounts();
        case 1910080819:  // notionals
          return ((FixedLegCashFlows) bean).getNotionals();
        case 1695350911:  // fixedRates
          return ((FixedLegCashFlows) bean).getFixedRates();
        case 178231285:  // discountedPaymentAmounts
          return ((FixedLegCashFlows) bean).getDiscountedPaymentAmounts();
        case -338982286:  // numberOfCashFlows
          return ((FixedLegCashFlows) bean).getNumberOfCashFlows();
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
   * The bean-builder for {@code FixedLegCashFlows}.
   */
  public static class Builder extends DirectFieldsBeanBuilder<FixedLegCashFlows> {

    private List<LocalDate> _accrualStart = ImmutableList.of();
    private List<LocalDate> _accrualEnd = ImmutableList.of();
    private List<Double> _discountFactors = ImmutableList.of();
    private List<Double> _paymentTimes = ImmutableList.of();
    private List<Double> _paymentFractions = ImmutableList.of();
    private List<CurrencyAmount> _paymentAmounts = ImmutableList.of();
    private List<CurrencyAmount> _notionals = ImmutableList.of();
    private List<Double> _fixedRates = ImmutableList.of();

    /**
     * Restricted constructor.
     */
    protected Builder() {
    }

    /**
     * Restricted copy constructor.
     * @param beanToCopy  the bean to copy from, not null
     */
    protected Builder(FixedLegCashFlows beanToCopy) {
      this._accrualStart = ImmutableList.copyOf(beanToCopy.getAccrualStart());
      this._accrualEnd = ImmutableList.copyOf(beanToCopy.getAccrualEnd());
      this._discountFactors = ImmutableList.copyOf(beanToCopy.getDiscountFactors());
      this._paymentTimes = ImmutableList.copyOf(beanToCopy.getPaymentTimes());
      this._paymentFractions = ImmutableList.copyOf(beanToCopy.getPaymentFractions());
      this._paymentAmounts = ImmutableList.copyOf(beanToCopy.getPaymentAmounts());
      this._notionals = ImmutableList.copyOf(beanToCopy.getNotionals());
      this._fixedRates = ImmutableList.copyOf(beanToCopy.getFixedRates());
    }

    //-----------------------------------------------------------------------
    @Override
    public Object get(String propertyName) {
      switch (propertyName.hashCode()) {
        case 1071260659:  // accrualStart
          return _accrualStart;
        case 1846909100:  // accrualEnd
          return _accrualEnd;
        case -91613053:  // discountFactors
          return _discountFactors;
        case -507430688:  // paymentTimes
          return _paymentTimes;
        case 1206997835:  // paymentFractions
          return _paymentFractions;
        case -1875448267:  // paymentAmounts
          return _paymentAmounts;
        case 1910080819:  // notionals
          return _notionals;
        case 1695350911:  // fixedRates
          return _fixedRates;
        default:
          throw new NoSuchElementException("Unknown property: " + propertyName);
      }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Builder set(String propertyName, Object newValue) {
      switch (propertyName.hashCode()) {
        case 1071260659:  // accrualStart
          this._accrualStart = (List<LocalDate>) newValue;
          break;
        case 1846909100:  // accrualEnd
          this._accrualEnd = (List<LocalDate>) newValue;
          break;
        case -91613053:  // discountFactors
          this._discountFactors = (List<Double>) newValue;
          break;
        case -507430688:  // paymentTimes
          this._paymentTimes = (List<Double>) newValue;
          break;
        case 1206997835:  // paymentFractions
          this._paymentFractions = (List<Double>) newValue;
          break;
        case -1875448267:  // paymentAmounts
          this._paymentAmounts = (List<CurrencyAmount>) newValue;
          break;
        case 1910080819:  // notionals
          this._notionals = (List<CurrencyAmount>) newValue;
          break;
        case 1695350911:  // fixedRates
          this._fixedRates = (List<Double>) newValue;
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
    public FixedLegCashFlows build() {
      return new FixedLegCashFlows(
          _accrualStart,
          _accrualEnd,
          _discountFactors,
          _paymentTimes,
          _paymentFractions,
          _paymentAmounts,
          _notionals,
          _fixedRates);
    }

    //-----------------------------------------------------------------------
    /**
     * Sets an array of accrual start dates.
     * @param accrualStart  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder accrualStart(List<LocalDate> accrualStart) {
      JodaBeanUtils.notNull(accrualStart, "accrualStart");
      this._accrualStart = accrualStart;
      return this;
    }

    /**
     * Sets the {@code accrualStart} property in the builder
     * from an array of objects.
     * @param accrualStart  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder accrualStart(LocalDate... accrualStart) {
      return accrualStart(ImmutableList.copyOf(accrualStart));
    }

    /**
     * Sets an array of accrual end dates.
     * @param accrualEnd  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder accrualEnd(List<LocalDate> accrualEnd) {
      JodaBeanUtils.notNull(accrualEnd, "accrualEnd");
      this._accrualEnd = accrualEnd;
      return this;
    }

    /**
     * Sets the {@code accrualEnd} property in the builder
     * from an array of objects.
     * @param accrualEnd  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder accrualEnd(LocalDate... accrualEnd) {
      return accrualEnd(ImmutableList.copyOf(accrualEnd));
    }

    /**
     * Sets an array of discount factors for the payments.
     * @param discountFactors  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder discountFactors(List<Double> discountFactors) {
      JodaBeanUtils.notNull(discountFactors, "discountFactors");
      this._discountFactors = discountFactors;
      return this;
    }

    /**
     * Sets the {@code discountFactors} property in the builder
     * from an array of objects.
     * @param discountFactors  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder discountFactors(Double... discountFactors) {
      return discountFactors(ImmutableList.copyOf(discountFactors));
    }

    /**
     * Sets an array of payment times.
     * @param paymentTimes  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder paymentTimes(List<Double> paymentTimes) {
      JodaBeanUtils.notNull(paymentTimes, "paymentTimes");
      this._paymentTimes = paymentTimes;
      return this;
    }

    /**
     * Sets the {@code paymentTimes} property in the builder
     * from an array of objects.
     * @param paymentTimes  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder paymentTimes(Double... paymentTimes) {
      return paymentTimes(ImmutableList.copyOf(paymentTimes));
    }

    /**
     * Sets an array of payment year fractions.
     * @param paymentFractions  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder paymentFractions(List<Double> paymentFractions) {
      JodaBeanUtils.notNull(paymentFractions, "paymentFractions");
      this._paymentFractions = paymentFractions;
      return this;
    }

    /**
     * Sets the {@code paymentFractions} property in the builder
     * from an array of objects.
     * @param paymentFractions  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder paymentFractions(Double... paymentFractions) {
      return paymentFractions(ImmutableList.copyOf(paymentFractions));
    }

    /**
     * Sets an array of payment amounts.
     * @param paymentAmounts  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder paymentAmounts(List<CurrencyAmount> paymentAmounts) {
      JodaBeanUtils.notNull(paymentAmounts, "paymentAmounts");
      this._paymentAmounts = paymentAmounts;
      return this;
    }

    /**
     * Sets the {@code paymentAmounts} property in the builder
     * from an array of objects.
     * @param paymentAmounts  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder paymentAmounts(CurrencyAmount... paymentAmounts) {
      return paymentAmounts(ImmutableList.copyOf(paymentAmounts));
    }

    /**
     * Sets an array of notionals.
     * @param notionals  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder notionals(List<CurrencyAmount> notionals) {
      JodaBeanUtils.notNull(notionals, "notionals");
      this._notionals = notionals;
      return this;
    }

    /**
     * Sets the {@code notionals} property in the builder
     * from an array of objects.
     * @param notionals  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder notionals(CurrencyAmount... notionals) {
      return notionals(ImmutableList.copyOf(notionals));
    }

    /**
     * Sets an array of fixed rates.
     * @param fixedRates  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder fixedRates(List<Double> fixedRates) {
      JodaBeanUtils.notNull(fixedRates, "fixedRates");
      this._fixedRates = fixedRates;
      return this;
    }

    /**
     * Sets the {@code fixedRates} property in the builder
     * from an array of objects.
     * @param fixedRates  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder fixedRates(Double... fixedRates) {
      return fixedRates(ImmutableList.copyOf(fixedRates));
    }

    //-----------------------------------------------------------------------
    @Override
    public String toString() {
      StringBuilder buf = new StringBuilder(288);
      buf.append("FixedLegCashFlows.Builder{");
      int len = buf.length();
      toString(buf);
      if (buf.length() > len) {
        buf.setLength(buf.length() - 2);
      }
      buf.append('}');
      return buf.toString();
    }

    protected void toString(StringBuilder buf) {
      buf.append("accrualStart").append('=').append(JodaBeanUtils.toString(_accrualStart)).append(',').append(' ');
      buf.append("accrualEnd").append('=').append(JodaBeanUtils.toString(_accrualEnd)).append(',').append(' ');
      buf.append("discountFactors").append('=').append(JodaBeanUtils.toString(_discountFactors)).append(',').append(' ');
      buf.append("paymentTimes").append('=').append(JodaBeanUtils.toString(_paymentTimes)).append(',').append(' ');
      buf.append("paymentFractions").append('=').append(JodaBeanUtils.toString(_paymentFractions)).append(',').append(' ');
      buf.append("paymentAmounts").append('=').append(JodaBeanUtils.toString(_paymentAmounts)).append(',').append(' ');
      buf.append("notionals").append('=').append(JodaBeanUtils.toString(_notionals)).append(',').append(' ');
      buf.append("fixedRates").append('=').append(JodaBeanUtils.toString(_fixedRates)).append(',').append(' ');
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
