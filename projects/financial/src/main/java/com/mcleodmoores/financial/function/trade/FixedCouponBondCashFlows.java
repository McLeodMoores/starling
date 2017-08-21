package com.mcleodmoores.financial.function.trade;

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
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.CurrencyAmount;

/**
 * Container for the relevant details for pricing a fixed coupon bond, with the entries
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
 * </ul>
 * There is an entry for each coupon in the bond.
 */
@BeanDefinition
public class FixedCouponBondCashFlows implements ImmutableBean {
  /**
   * The accrual year fraction label.
   */
  public static final String ACCRUAL_YEAR_FRACTION = "Accrual Year Fraction";
  /**
   * The payment amount label.
   */
  public static final String PAYMENT_AMOUNT = "Payment Amount";
  /**
   * The discount factor label.
   */
  public static final String DISCOUNT_FACTOR = "Discount Factor";
  /**
   * The coupon rate label.
   */
  public static final String COUPON_RATE = "Coupon Rate";
  /**
   * The discounted payment amount.
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
   * The nominal payment label.
   */
  public static final String NOMINAL_PAYMENT_DATES = "Nominal Payment Date";
  /**
   * Accrual start dates.
   */
  @PropertyDefinition
  private final List<LocalDate> _accrualStart;
  /**
   * Accrual end dates.
   */
  @PropertyDefinition
  private final List<LocalDate> _accrualEnd;
  /**
   * Discount factors for the payments.
   */
  @PropertyDefinition
  private final List<Double> _discountFactors;
  /**
   * Payment times.
   */
  @PropertyDefinition
  private final List<Double> _paymentTimes;
  /**
   * Accrual year fractions.
   */
  @PropertyDefinition
  private final List<Double> _accrualFractions;
  /**
   * Payment amounts.
   */
  @PropertyDefinition
  private final List<CurrencyAmount> _paymentAmounts;
  /**
   * Notionals.
   */
  @PropertyDefinition
  private final List<CurrencyAmount> _notionals;
  /**
   * Coupon rates.
   */
  @PropertyDefinition
  private final List<Double> _couponRates;
  /**
   * The payment dates of the nominal amount.
   */
  @PropertyDefinition
  private final List<LocalDate> _nominalPaymentDates;

  /**
    * Constructs the cash-flow object.
    * @param startAccrualDates  the coupon start accrual dates, not null
    * @param endAccrualDates  the coupon end accrual dates, not null
    * @param discountFactors  the discount factors used at payment time, not null
    * @param paymentTimes  the payment times, not null
    * @param accrualFractions  the accrual year fractions calculated from the day-count convention, not null
    * @param paymentAmounts  the payment amounts, not null
    * @param notionals  the notionals, not null
    * @param couponRates  the coupon rates, not null
   */
  @ImmutableConstructor
  public FixedCouponBondCashFlows(final List<LocalDate> startAccrualDates, final List<LocalDate> endAccrualDates, final List<Double> discountFactors,
                                  final List<Double> paymentTimes, final List<Double> accrualFractions, final List<CurrencyAmount> paymentAmounts,
                                  final List<CurrencyAmount> notionals, final List<Double> couponRates, final List<LocalDate> nominalPaymentDates) {
    _accrualStart = Collections.unmodifiableList(new ArrayList<>(ArgumentChecker.notNull(startAccrualDates, "startAccrualDates")));
    _accrualEnd = Collections.unmodifiableList(new ArrayList<>(ArgumentChecker.notNull(endAccrualDates, "endAccrualDates")));
    _notionals = Collections.unmodifiableList(new ArrayList<>(ArgumentChecker.notNull(notionals, "notionals")));
    _paymentTimes = Collections.unmodifiableList(new ArrayList<>(ArgumentChecker.notNull(paymentTimes, "paymentTimes")));
    _discountFactors = Collections.unmodifiableList(new ArrayList<>(ArgumentChecker.notNull(discountFactors, "discountFactors")));
    _accrualFractions = Collections.unmodifiableList(new ArrayList<>(ArgumentChecker.notNull(accrualFractions, "accrualFractions")));
    _paymentAmounts = Collections.unmodifiableList(new ArrayList<>(ArgumentChecker.notNull(paymentAmounts, "paymentAmounts")));
    _couponRates = Collections.unmodifiableList(new ArrayList<>(ArgumentChecker.notNull(couponRates, "couponRates")));
    _nominalPaymentDates = Collections.unmodifiableList(new ArrayList<>(ArgumentChecker.notNull(nominalPaymentDates, "nominalPaymentDates")));
    final int n = startAccrualDates.size();
    ArgumentChecker.isTrue(n == endAccrualDates.size(), "Must have same number of start and end accrual dates");
    ArgumentChecker.isTrue(n == discountFactors.size(), "Must have same number of start accrual dates and discount factors");
    ArgumentChecker.isTrue(n == paymentTimes.size(), "Must have same number of start accrual dates and payment times");
    ArgumentChecker.isTrue(n == accrualFractions.size(), "Must have same number of start accrual dates and accrual year fractions");
    ArgumentChecker.isTrue(n == paymentAmounts.size(), "Must have same number of start accrual dates and payment amounts");
    ArgumentChecker.isTrue(n == notionals.size(), "Must have same number of start accrual dates and notionals");
    ArgumentChecker.isTrue(n == couponRates.size(), "Must have same number of start accrual dates and coupon rates");
    ArgumentChecker.isTrue(n == nominalPaymentDates.size(), "Must have same number of nominal payment dates and start accrual dates");
  }

  /**
   * Gets the discounted payment amounts.
   * @return  the discounted cashflows
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
   * @return  the total number of cash-flows
   */
  @DerivedProperty
  public int getNumberOfCashFlows() {
    return getNotionals().size();
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code FixedCouponBondCashFlows}.
   * @return the meta-bean, not null
   */
  public static FixedCouponBondCashFlows.Meta meta() {
    return FixedCouponBondCashFlows.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(FixedCouponBondCashFlows.Meta.INSTANCE);
  }

  /**
   * Returns a builder used to create an instance of the bean.
   * @return the builder, not null
   */
  public static FixedCouponBondCashFlows.Builder builder() {
    return new FixedCouponBondCashFlows.Builder();
  }

  @Override
  public FixedCouponBondCashFlows.Meta metaBean() {
    return FixedCouponBondCashFlows.Meta.INSTANCE;
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
   * Gets accrual start dates.
   * @return the value of the property
   */
  public List<LocalDate> getAccrualStart() {
    return _accrualStart;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets accrual end dates.
   * @return the value of the property
   */
  public List<LocalDate> getAccrualEnd() {
    return _accrualEnd;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets discount factors for the payments.
   * @return the value of the property
   */
  public List<Double> getDiscountFactors() {
    return _discountFactors;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets payment times.
   * @return the value of the property
   */
  public List<Double> getPaymentTimes() {
    return _paymentTimes;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets accrual year fractions.
   * @return the value of the property
   */
  public List<Double> getAccrualFractions() {
    return _accrualFractions;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets payment amounts.
   * @return the value of the property
   */
  public List<CurrencyAmount> getPaymentAmounts() {
    return _paymentAmounts;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets notionals.
   * @return the value of the property
   */
  public List<CurrencyAmount> getNotionals() {
    return _notionals;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets coupon rates.
   * @return the value of the property
   */
  public List<Double> getCouponRates() {
    return _couponRates;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the payment dates of the nominal amount.
   * @return the value of the property
   */
  public List<LocalDate> getNominalPaymentDates() {
    return _nominalPaymentDates;
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
      FixedCouponBondCashFlows other = (FixedCouponBondCashFlows) obj;
      return JodaBeanUtils.equal(getAccrualStart(), other.getAccrualStart()) &&
          JodaBeanUtils.equal(getAccrualEnd(), other.getAccrualEnd()) &&
          JodaBeanUtils.equal(getDiscountFactors(), other.getDiscountFactors()) &&
          JodaBeanUtils.equal(getPaymentTimes(), other.getPaymentTimes()) &&
          JodaBeanUtils.equal(getAccrualFractions(), other.getAccrualFractions()) &&
          JodaBeanUtils.equal(getPaymentAmounts(), other.getPaymentAmounts()) &&
          JodaBeanUtils.equal(getNotionals(), other.getNotionals()) &&
          JodaBeanUtils.equal(getCouponRates(), other.getCouponRates()) &&
          JodaBeanUtils.equal(getNominalPaymentDates(), other.getNominalPaymentDates());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash = hash * 31 + JodaBeanUtils.hashCode(getAccrualStart());
    hash = hash * 31 + JodaBeanUtils.hashCode(getAccrualEnd());
    hash = hash * 31 + JodaBeanUtils.hashCode(getDiscountFactors());
    hash = hash * 31 + JodaBeanUtils.hashCode(getPaymentTimes());
    hash = hash * 31 + JodaBeanUtils.hashCode(getAccrualFractions());
    hash = hash * 31 + JodaBeanUtils.hashCode(getPaymentAmounts());
    hash = hash * 31 + JodaBeanUtils.hashCode(getNotionals());
    hash = hash * 31 + JodaBeanUtils.hashCode(getCouponRates());
    hash = hash * 31 + JodaBeanUtils.hashCode(getNominalPaymentDates());
    return hash;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(384);
    buf.append("FixedCouponBondCashFlows{");
    int len = buf.length();
    toString(buf);
    if (buf.length() > len) {
      buf.setLength(buf.length() - 2);
    }
    buf.append('}');
    return buf.toString();
  }

  protected void toString(StringBuilder buf) {
    buf.append("accrualStart").append('=').append(JodaBeanUtils.toString(getAccrualStart())).append(',').append(' ');
    buf.append("accrualEnd").append('=').append(JodaBeanUtils.toString(getAccrualEnd())).append(',').append(' ');
    buf.append("discountFactors").append('=').append(JodaBeanUtils.toString(getDiscountFactors())).append(',').append(' ');
    buf.append("paymentTimes").append('=').append(JodaBeanUtils.toString(getPaymentTimes())).append(',').append(' ');
    buf.append("accrualFractions").append('=').append(JodaBeanUtils.toString(getAccrualFractions())).append(',').append(' ');
    buf.append("paymentAmounts").append('=').append(JodaBeanUtils.toString(getPaymentAmounts())).append(',').append(' ');
    buf.append("notionals").append('=').append(JodaBeanUtils.toString(getNotionals())).append(',').append(' ');
    buf.append("couponRates").append('=').append(JodaBeanUtils.toString(getCouponRates())).append(',').append(' ');
    buf.append("nominalPaymentDates").append('=').append(JodaBeanUtils.toString(getNominalPaymentDates())).append(',').append(' ');
    buf.append("discountedPaymentAmounts").append('=').append(JodaBeanUtils.toString(getDiscountedPaymentAmounts())).append(',').append(' ');
    buf.append("numberOfCashFlows").append('=').append(JodaBeanUtils.toString(getNumberOfCashFlows())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code FixedCouponBondCashFlows}.
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
        this, "accrualStart", FixedCouponBondCashFlows.class, (Class) List.class);
    /**
     * The meta-property for the {@code accrualEnd} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<List<LocalDate>> _accrualEnd = DirectMetaProperty.ofImmutable(
        this, "accrualEnd", FixedCouponBondCashFlows.class, (Class) List.class);
    /**
     * The meta-property for the {@code discountFactors} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<List<Double>> _discountFactors = DirectMetaProperty.ofImmutable(
        this, "discountFactors", FixedCouponBondCashFlows.class, (Class) List.class);
    /**
     * The meta-property for the {@code paymentTimes} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<List<Double>> _paymentTimes = DirectMetaProperty.ofImmutable(
        this, "paymentTimes", FixedCouponBondCashFlows.class, (Class) List.class);
    /**
     * The meta-property for the {@code accrualFractions} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<List<Double>> _accrualFractions = DirectMetaProperty.ofImmutable(
        this, "accrualFractions", FixedCouponBondCashFlows.class, (Class) List.class);
    /**
     * The meta-property for the {@code paymentAmounts} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<List<CurrencyAmount>> _paymentAmounts = DirectMetaProperty.ofImmutable(
        this, "paymentAmounts", FixedCouponBondCashFlows.class, (Class) List.class);
    /**
     * The meta-property for the {@code notionals} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<List<CurrencyAmount>> _notionals = DirectMetaProperty.ofImmutable(
        this, "notionals", FixedCouponBondCashFlows.class, (Class) List.class);
    /**
     * The meta-property for the {@code couponRates} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<List<Double>> _couponRates = DirectMetaProperty.ofImmutable(
        this, "couponRates", FixedCouponBondCashFlows.class, (Class) List.class);
    /**
     * The meta-property for the {@code nominalPaymentDates} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<List<LocalDate>> _nominalPaymentDates = DirectMetaProperty.ofImmutable(
        this, "nominalPaymentDates", FixedCouponBondCashFlows.class, (Class) List.class);
    /**
     * The meta-property for the {@code discountedPaymentAmounts} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<List<CurrencyAmount>> _discountedPaymentAmounts = DirectMetaProperty.ofDerived(
        this, "discountedPaymentAmounts", FixedCouponBondCashFlows.class, (Class) List.class);
    /**
     * The meta-property for the {@code numberOfCashFlows} property.
     */
    private final MetaProperty<Integer> _numberOfCashFlows = DirectMetaProperty.ofDerived(
        this, "numberOfCashFlows", FixedCouponBondCashFlows.class, Integer.TYPE);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "accrualStart",
        "accrualEnd",
        "discountFactors",
        "paymentTimes",
        "accrualFractions",
        "paymentAmounts",
        "notionals",
        "couponRates",
        "nominalPaymentDates",
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
        case 1288547778:  // accrualFractions
          return _accrualFractions;
        case -1875448267:  // paymentAmounts
          return _paymentAmounts;
        case 1910080819:  // notionals
          return _notionals;
        case 1716367117:  // couponRates
          return _couponRates;
        case 853404443:  // nominalPaymentDates
          return _nominalPaymentDates;
        case 178231285:  // discountedPaymentAmounts
          return _discountedPaymentAmounts;
        case -338982286:  // numberOfCashFlows
          return _numberOfCashFlows;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public FixedCouponBondCashFlows.Builder builder() {
      return new FixedCouponBondCashFlows.Builder();
    }

    @Override
    public Class<? extends FixedCouponBondCashFlows> beanType() {
      return FixedCouponBondCashFlows.class;
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
     * The meta-property for the {@code accrualFractions} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<List<Double>> accrualFractions() {
      return _accrualFractions;
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
     * The meta-property for the {@code couponRates} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<List<Double>> couponRates() {
      return _couponRates;
    }

    /**
     * The meta-property for the {@code nominalPaymentDates} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<List<LocalDate>> nominalPaymentDates() {
      return _nominalPaymentDates;
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
          return ((FixedCouponBondCashFlows) bean).getAccrualStart();
        case 1846909100:  // accrualEnd
          return ((FixedCouponBondCashFlows) bean).getAccrualEnd();
        case -91613053:  // discountFactors
          return ((FixedCouponBondCashFlows) bean).getDiscountFactors();
        case -507430688:  // paymentTimes
          return ((FixedCouponBondCashFlows) bean).getPaymentTimes();
        case 1288547778:  // accrualFractions
          return ((FixedCouponBondCashFlows) bean).getAccrualFractions();
        case -1875448267:  // paymentAmounts
          return ((FixedCouponBondCashFlows) bean).getPaymentAmounts();
        case 1910080819:  // notionals
          return ((FixedCouponBondCashFlows) bean).getNotionals();
        case 1716367117:  // couponRates
          return ((FixedCouponBondCashFlows) bean).getCouponRates();
        case 853404443:  // nominalPaymentDates
          return ((FixedCouponBondCashFlows) bean).getNominalPaymentDates();
        case 178231285:  // discountedPaymentAmounts
          return ((FixedCouponBondCashFlows) bean).getDiscountedPaymentAmounts();
        case -338982286:  // numberOfCashFlows
          return ((FixedCouponBondCashFlows) bean).getNumberOfCashFlows();
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
   * The bean-builder for {@code FixedCouponBondCashFlows}.
   */
  public static class Builder extends DirectFieldsBeanBuilder<FixedCouponBondCashFlows> {

    private List<LocalDate> _accrualStart;
    private List<LocalDate> _accrualEnd;
    private List<Double> _discountFactors;
    private List<Double> _paymentTimes;
    private List<Double> _accrualFractions;
    private List<CurrencyAmount> _paymentAmounts;
    private List<CurrencyAmount> _notionals;
    private List<Double> _couponRates;
    private List<LocalDate> _nominalPaymentDates;

    /**
     * Restricted constructor.
     */
    protected Builder() {
    }

    /**
     * Restricted copy constructor.
     * @param beanToCopy  the bean to copy from, not null
     */
    protected Builder(FixedCouponBondCashFlows beanToCopy) {
      this._accrualStart = (beanToCopy.getAccrualStart() != null ? ImmutableList.copyOf(beanToCopy.getAccrualStart()) : null);
      this._accrualEnd = (beanToCopy.getAccrualEnd() != null ? ImmutableList.copyOf(beanToCopy.getAccrualEnd()) : null);
      this._discountFactors = (beanToCopy.getDiscountFactors() != null ? ImmutableList.copyOf(beanToCopy.getDiscountFactors()) : null);
      this._paymentTimes = (beanToCopy.getPaymentTimes() != null ? ImmutableList.copyOf(beanToCopy.getPaymentTimes()) : null);
      this._accrualFractions = (beanToCopy.getAccrualFractions() != null ? ImmutableList.copyOf(beanToCopy.getAccrualFractions()) : null);
      this._paymentAmounts = (beanToCopy.getPaymentAmounts() != null ? ImmutableList.copyOf(beanToCopy.getPaymentAmounts()) : null);
      this._notionals = (beanToCopy.getNotionals() != null ? ImmutableList.copyOf(beanToCopy.getNotionals()) : null);
      this._couponRates = (beanToCopy.getCouponRates() != null ? ImmutableList.copyOf(beanToCopy.getCouponRates()) : null);
      this._nominalPaymentDates = (beanToCopy.getNominalPaymentDates() != null ? ImmutableList.copyOf(beanToCopy.getNominalPaymentDates()) : null);
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
        case 1288547778:  // accrualFractions
          return _accrualFractions;
        case -1875448267:  // paymentAmounts
          return _paymentAmounts;
        case 1910080819:  // notionals
          return _notionals;
        case 1716367117:  // couponRates
          return _couponRates;
        case 853404443:  // nominalPaymentDates
          return _nominalPaymentDates;
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
        case 1288547778:  // accrualFractions
          this._accrualFractions = (List<Double>) newValue;
          break;
        case -1875448267:  // paymentAmounts
          this._paymentAmounts = (List<CurrencyAmount>) newValue;
          break;
        case 1910080819:  // notionals
          this._notionals = (List<CurrencyAmount>) newValue;
          break;
        case 1716367117:  // couponRates
          this._couponRates = (List<Double>) newValue;
          break;
        case 853404443:  // nominalPaymentDates
          this._nominalPaymentDates = (List<LocalDate>) newValue;
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
    public FixedCouponBondCashFlows build() {
      return new FixedCouponBondCashFlows(
          _accrualStart,
          _accrualEnd,
          _discountFactors,
          _paymentTimes,
          _accrualFractions,
          _paymentAmounts,
          _notionals,
          _couponRates,
          _nominalPaymentDates);
    }

    //-----------------------------------------------------------------------
    /**
     * Sets accrual start dates.
     * @param accrualStart  the new value
     * @return this, for chaining, not null
     */
    public Builder accrualStart(List<LocalDate> accrualStart) {
      this._accrualStart = accrualStart;
      return this;
    }

    /**
     * Sets the {@code accrualStart} property in the builder
     * from an array of objects.
     * @param accrualStart  the new value
     * @return this, for chaining, not null
     */
    public Builder accrualStart(LocalDate... accrualStart) {
      return accrualStart(ImmutableList.copyOf(accrualStart));
    }

    /**
     * Sets accrual end dates.
     * @param accrualEnd  the new value
     * @return this, for chaining, not null
     */
    public Builder accrualEnd(List<LocalDate> accrualEnd) {
      this._accrualEnd = accrualEnd;
      return this;
    }

    /**
     * Sets the {@code accrualEnd} property in the builder
     * from an array of objects.
     * @param accrualEnd  the new value
     * @return this, for chaining, not null
     */
    public Builder accrualEnd(LocalDate... accrualEnd) {
      return accrualEnd(ImmutableList.copyOf(accrualEnd));
    }

    /**
     * Sets discount factors for the payments.
     * @param discountFactors  the new value
     * @return this, for chaining, not null
     */
    public Builder discountFactors(List<Double> discountFactors) {
      this._discountFactors = discountFactors;
      return this;
    }

    /**
     * Sets the {@code discountFactors} property in the builder
     * from an array of objects.
     * @param discountFactors  the new value
     * @return this, for chaining, not null
     */
    public Builder discountFactors(Double... discountFactors) {
      return discountFactors(ImmutableList.copyOf(discountFactors));
    }

    /**
     * Sets payment times.
     * @param paymentTimes  the new value
     * @return this, for chaining, not null
     */
    public Builder paymentTimes(List<Double> paymentTimes) {
      this._paymentTimes = paymentTimes;
      return this;
    }

    /**
     * Sets the {@code paymentTimes} property in the builder
     * from an array of objects.
     * @param paymentTimes  the new value
     * @return this, for chaining, not null
     */
    public Builder paymentTimes(Double... paymentTimes) {
      return paymentTimes(ImmutableList.copyOf(paymentTimes));
    }

    /**
     * Sets accrual year fractions.
     * @param accrualFractions  the new value
     * @return this, for chaining, not null
     */
    public Builder accrualFractions(List<Double> accrualFractions) {
      this._accrualFractions = accrualFractions;
      return this;
    }

    /**
     * Sets the {@code accrualFractions} property in the builder
     * from an array of objects.
     * @param accrualFractions  the new value
     * @return this, for chaining, not null
     */
    public Builder accrualFractions(Double... accrualFractions) {
      return accrualFractions(ImmutableList.copyOf(accrualFractions));
    }

    /**
     * Sets payment amounts.
     * @param paymentAmounts  the new value
     * @return this, for chaining, not null
     */
    public Builder paymentAmounts(List<CurrencyAmount> paymentAmounts) {
      this._paymentAmounts = paymentAmounts;
      return this;
    }

    /**
     * Sets the {@code paymentAmounts} property in the builder
     * from an array of objects.
     * @param paymentAmounts  the new value
     * @return this, for chaining, not null
     */
    public Builder paymentAmounts(CurrencyAmount... paymentAmounts) {
      return paymentAmounts(ImmutableList.copyOf(paymentAmounts));
    }

    /**
     * Sets notionals.
     * @param notionals  the new value
     * @return this, for chaining, not null
     */
    public Builder notionals(List<CurrencyAmount> notionals) {
      this._notionals = notionals;
      return this;
    }

    /**
     * Sets the {@code notionals} property in the builder
     * from an array of objects.
     * @param notionals  the new value
     * @return this, for chaining, not null
     */
    public Builder notionals(CurrencyAmount... notionals) {
      return notionals(ImmutableList.copyOf(notionals));
    }

    /**
     * Sets coupon rates.
     * @param couponRates  the new value
     * @return this, for chaining, not null
     */
    public Builder couponRates(List<Double> couponRates) {
      this._couponRates = couponRates;
      return this;
    }

    /**
     * Sets the {@code couponRates} property in the builder
     * from an array of objects.
     * @param couponRates  the new value
     * @return this, for chaining, not null
     */
    public Builder couponRates(Double... couponRates) {
      return couponRates(ImmutableList.copyOf(couponRates));
    }

    /**
     * Sets the payment dates of the nominal amount.
     * @param nominalPaymentDates  the new value
     * @return this, for chaining, not null
     */
    public Builder nominalPaymentDates(List<LocalDate> nominalPaymentDates) {
      this._nominalPaymentDates = nominalPaymentDates;
      return this;
    }

    /**
     * Sets the {@code nominalPaymentDates} property in the builder
     * from an array of objects.
     * @param nominalPaymentDates  the new value
     * @return this, for chaining, not null
     */
    public Builder nominalPaymentDates(LocalDate... nominalPaymentDates) {
      return nominalPaymentDates(ImmutableList.copyOf(nominalPaymentDates));
    }

    //-----------------------------------------------------------------------
    @Override
    public String toString() {
      StringBuilder buf = new StringBuilder(320);
      buf.append("FixedCouponBondCashFlows.Builder{");
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
      buf.append("accrualFractions").append('=').append(JodaBeanUtils.toString(_accrualFractions)).append(',').append(' ');
      buf.append("paymentAmounts").append('=').append(JodaBeanUtils.toString(_paymentAmounts)).append(',').append(' ');
      buf.append("notionals").append('=').append(JodaBeanUtils.toString(_notionals)).append(',').append(' ');
      buf.append("couponRates").append('=').append(JodaBeanUtils.toString(_couponRates)).append(',').append(' ');
      buf.append("nominalPaymentDates").append('=').append(JodaBeanUtils.toString(_nominalPaymentDates)).append(',').append(' ');
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
