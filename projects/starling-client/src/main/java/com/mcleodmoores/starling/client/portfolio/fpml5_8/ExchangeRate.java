/**
 * Copyright (C) 2016 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.starling.client.portfolio.fpml5_8;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.joda.beans.Bean;
import org.joda.beans.BeanDefinition;
import org.joda.beans.ImmutableBean;
import org.joda.beans.ImmutableValidator;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectFieldsBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaBean;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;

import com.opengamma.util.money.Currency;

/**
 * An object defining an exchange rate between two currencies. The rate can be defined in one of three mutually-exclusive ways:
 * <ul>
 *  <li> Combining two cross rates. The cross rate is calculated using the {@link CrossRate#toExchangeRate(CrossRate)} method.
 *       If the rate is set in the builder, it must be equal to the calculated cross rate to 6 decimal places. The
 *       spot, forward and forward quote basis must not be set.
 *  <li> Combining a spot and forward points quote. The forward points quote must be in the same unit as the spot rate i.e. no
 *       normalization is performed. If the rate is set in the builder, it must be equal to the calculated rate to 6 decimal places.
 *       The cross rates must not be set.
 *  <li> Setting the rate and quoted currency pair. In this case, the cross rates, spot, forward points and forward quote basis
 *       must not be set.
 * </ul>
 * Examples of how to construct an exchange rate rate given below:
 * <ol>
 *  <li> Direct construction using the rate:<br>
 *    <pre>
 *      ExchangeRate.builder()
 *        .quotedCurrencyPair(USDJPY_CURRENCY_PAIR)
 *        .rate(USDJPY_RATE)
 *        .build()
 *    </pre>
 *  <li> Construction from two cross rates:<br>
 *    <pre>
 *      ExchangeRate.builder()
 *        .quotedCurrencyPair(GBPJPY_CURRENCY_PAIR)
 *        .crossRate1(GBPUSD_CROSS)
 *        .crossRate2(USDJPY_CROSS)
 *        .build()
 *    </pre>
 *  <li> Construction from two cross rates with the final rate also set:<br>
 *    <pre>
 *      ExchangeRate.builder()
 *        .quotedCurrencyPair(GBPJPY_CURRENCY_PAIR)
 *        .crossRate1(GBPUSD_CROSS)
 *        .crossRate2(USDJPY_CROSS)
 *        .rate(GBPJPY)
 *        .build()
 *    </pre>
 *  <li> Construction from FX spot and forward points:<br>
 *    <pre>
 *      ExchangeRate.builder()
 *        .quotedCurrencyPair(GBPJPY_CURRENCY_PAIR)
 *        .spotRate(GBPJPY_SPOT)
 *        .forwardPoints(GBPJPY_FWD_PTS)
 *        .fxForwardQuoteBasis(GBPJPY_CURRENCY_PAIR)
 *        .build()
 *    </pre>
 *  <li> Construction from FX spot and forward points with the final rate also set:<br>
 *    <pre>
 *      ExchangeRate.builder()
 *        .quotedCurrencyPair(GBPJPY_CURRENCY_PAIR)
 *        .rate(GBPJPY)
 *        .spotRate(GBPJPY_SPOT)
 *        .forwardPoints(GBPJPY_FWD_PTS)
 *        .fxForwardQuoteBasis(GBPJPY_CURRENCY_PAIR)
 *        .build()
 *    </pre>
 * </ol>
 */
@BeanDefinition
public class ExchangeRate implements ImmutableBean {

  /**
   * The quoted currency pair.
   */
  @PropertyDefinition(validate = "notNull", get = "manual")
  private final QuotedCurrencyPair _quotedCurrencyPair;

  //TODO 4 dp unless JPY to 2dp?
  //TODO double-check that 5.8 requires that this is set
  /**
   * The rate for this quote.
   */
  @PropertyDefinition(get = "manual")
  private final BigDecimal _rate;

  //--------------------------------------------
  /**
   * The FX spot rate.
   */
  @PropertyDefinition
  private final BigDecimal _spotRate;

  /**
   * The normalized FX forward points.
   */
  @PropertyDefinition
  private final BigDecimal _forwardPoints;

  /**
   * The FX forward quote basis.
   */
  @PropertyDefinition
  private final QuoteBasis _fxForwardQuoteBasis;

  //--------------------------------------------
  /**
   * The first cross rate.
   */
  @PropertyDefinition
  private final CrossRate _crossRate1;

  /**
   * The second cross rate.
   */
  @PropertyDefinition
  private final CrossRate _crossRate2;

  /**
   * The effective quoted currency pair that has been constructed from the inputs.
   */
  private QuotedCurrencyPair _effectiveQuotedCurrencyPair;

  /**
   * The effective rate that has been constructed from the inputs.
   */
  private BigDecimal _effectiveRate;

  //-----------------------------------------------------------------------
  /**
   * Gets the quotedCurrencyPair.
   * @return the value of the property, not null
   */
  public QuotedCurrencyPair getQuotedCurrencyPair() {
    return _effectiveQuotedCurrencyPair;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the rate.
   * @return the value of the property
   */
  public BigDecimal getRate() {
    return _effectiveRate;
  }

  /**
   * Validates the inputs and calculates the effective rate and quoted currency pair.
   */
  @ImmutableValidator
  private void validate() {
    final QuoteBasis quoteBasis;
    final Currency currency1 = _quotedCurrencyPair.getCurrency1();
    final Currency currency2 = _quotedCurrencyPair.getCurrency2();
    final QuoteBasis builderQuoteBasis = _quotedCurrencyPair.getQuoteBasis();
    if (_crossRate1 != null && _crossRate2 != null) {
      // used both cross rates
      if (_fxForwardQuoteBasis != null || _spotRate != null || _forwardPoints != null) {
        throw new IllegalStateException("ExchangeRate is being constructed using two cross rates: "
            + "cannot set the spot rate, forward points or FX forward quote basis");
      }
      final ExchangeRate calculatedExchangeRate = _crossRate1.toExchangeRate(_crossRate2);
      final Currency exchangeRateCurrency1 = calculatedExchangeRate.getQuotedCurrencyPair().getCurrency1();
      final Currency exchangeRateCurrency2 = calculatedExchangeRate.getQuotedCurrencyPair().getCurrency2();
      final QuoteBasis exchangeRateQuoteBasis = calculatedExchangeRate.getQuotedCurrencyPair().getQuoteBasis();
      final BigDecimal calculatedRate = calculatedExchangeRate.getRate();
      if (exchangeRateCurrency1.equals(currency1) && exchangeRateCurrency2.equals(currency2)) {
        // quote and calculated rate are in the same order
        if (_rate != null) {
          // check that the rates match if the rate was set in the builder
          JodaBeanUtils.notNull(builderQuoteBasis, "builderQuoteBasis");
          final BigDecimal rate6dp = BigDecimal.valueOf(_rate.doubleValue()).setScale(6,  RoundingMode.HALF_UP);
          if (builderQuoteBasis == exchangeRateQuoteBasis) {
            final BigDecimal calculatedRate6dp = BigDecimal.valueOf(calculatedRate.doubleValue()).setScale(6, RoundingMode.HALF_UP);
            if (rate6dp.compareTo(calculatedRate6dp) != 0) {
              throw new IllegalStateException("Calculated cross rate " + calculatedRate6dp + " not equal to " + rate6dp
                  + " for " + exchangeRateCurrency1 + "/" + exchangeRateCurrency2);
            }
          } else {
            final BigDecimal calculatedRate6dp = BigDecimal.valueOf(1 / calculatedRate.doubleValue()).setScale(6, RoundingMode.HALF_UP);
            if (rate6dp.compareTo(calculatedRate6dp) != 0) {
              throw new IllegalStateException("Calculated cross rate " + calculatedRate6dp + " not equal to " + rate6dp
                  + " for " + exchangeRateCurrency1 + "/" + exchangeRateCurrency2);
            }
          }
          _effectiveRate = _rate;
          quoteBasis = builderQuoteBasis;
        } else {
          // the rate wasn't set, so the calculated rate is used
          _effectiveRate = calculatedRate;
          quoteBasis = exchangeRateQuoteBasis;
        }
      } else if (exchangeRateCurrency1.equals(currency2) && exchangeRateCurrency2.equals(currency1)) {
        // calculated rate is the inverse of the quote
        if (_rate != null) {
          // check that the rates match if the rate was set in the builder
          JodaBeanUtils.notNull(builderQuoteBasis, "builderQuoteBasis");
          final BigDecimal rate6dp = BigDecimal.valueOf(_rate.doubleValue()).setScale(6,  RoundingMode.HALF_UP);
          if (builderQuoteBasis == exchangeRateQuoteBasis) {
            final BigDecimal calculatedRate6dp = BigDecimal.valueOf(1 / calculatedRate.doubleValue()).setScale(6, RoundingMode.HALF_UP);
            if (rate6dp.compareTo(calculatedRate6dp) != 0) {
              throw new IllegalStateException("Calculated cross rate " + calculatedRate6dp + " not equal to " + rate6dp
                  + " for " + exchangeRateCurrency2 + "/" + exchangeRateCurrency1);
            }
          } else {
            final BigDecimal calculatedRate6dp = BigDecimal.valueOf(calculatedRate.doubleValue()).setScale(6, RoundingMode.HALF_UP);
            if (rate6dp.compareTo(calculatedRate6dp) != 0) {
              throw new IllegalStateException("Calculated cross rate " + calculatedRate6dp + " not equal to " + rate6dp
                  + " for " + exchangeRateCurrency2 + "/" + exchangeRateCurrency1);
            }
          }
          _effectiveRate = _rate;
          quoteBasis = builderQuoteBasis;
        } else {
          _effectiveRate = BigDecimal.valueOf(1 / calculatedRate.doubleValue());
          quoteBasis = exchangeRateQuoteBasis;
        }
      } else {
        throw new IllegalStateException("Unmatched currencies: have " + currency1 + "/" + currency2
            + " and " + exchangeRateCurrency1 + "/" + exchangeRateCurrency2);
      }
    } else if (_fxForwardQuoteBasis != null && _spotRate != null && _forwardPoints != null) {
      // used spot + forward
      if (_crossRate1 != null || _crossRate2 != null) {
        throw new IllegalStateException("ExchangeRate is being constructed using the spot rate and forward points: "
            + "cannot set cross rates");
      }
      final BigDecimal calculatedRate = _spotRate.add(_forwardPoints);
      if (_rate != null) {
        // check that the rates match if the rate was set in the builder
        JodaBeanUtils.notNull(builderQuoteBasis, "builderQuoteBasis");
        final BigDecimal rate6dp = BigDecimal.valueOf(_rate.doubleValue()).setScale(6,  RoundingMode.HALF_UP);
        if (builderQuoteBasis == _fxForwardQuoteBasis) {
          final BigDecimal calculatedRate6dp = BigDecimal.valueOf(calculatedRate.doubleValue()).setScale(6, RoundingMode.HALF_UP);
          if (rate6dp.compareTo(calculatedRate6dp) != 0) {
            throw new IllegalStateException("Calculated rate " + calculatedRate6dp + " not equal to " + rate6dp
                + " for " + currency1 + "/" + currency2);
          }
        } else {
          final BigDecimal calculatedRate6dp = BigDecimal.valueOf(1 / calculatedRate.doubleValue()).setScale(6, RoundingMode.HALF_UP);
          if (rate6dp.compareTo(calculatedRate6dp) != 0) {
            throw new IllegalStateException("Calculated rate " + calculatedRate6dp + " not equal to " + rate6dp
                + " for " + currency2 + "/" + currency1);
          }
        }
        _effectiveRate = _rate;
        quoteBasis = builderQuoteBasis;
      } else {
        // check that the rates match
        if (builderQuoteBasis == null) {
          quoteBasis = _fxForwardQuoteBasis;
          _effectiveRate = calculatedRate;
        } else {
          quoteBasis = builderQuoteBasis;
          if (builderQuoteBasis == _fxForwardQuoteBasis) {
            _effectiveRate = calculatedRate;
          } else {
            _effectiveRate = BigDecimal.valueOf(1 / calculatedRate.doubleValue());
          }
        }
      }
    } else if (_fxForwardQuoteBasis != null || _spotRate != null || _forwardPoints != null || _crossRate1 != null || _crossRate2 != null) {
      throw new IllegalStateException("Inconsistent state: the following fields should be null: _fxForwardQuoteBasis=" + _fxForwardQuoteBasis
          + " _spotRate=" + _spotRate + " _forwardPoints=" + _forwardPoints + " _crossRate1=" + _crossRate1 + " _crossRate2=" + _crossRate2);
    } else {
      // used only the rate and quote basis
      JodaBeanUtils.notNull(_rate, "rate");
      JodaBeanUtils.notNull(builderQuoteBasis, "builderQuoteBasis");
      quoteBasis = builderQuoteBasis;
      _effectiveRate = _rate;
    }
    _effectiveQuotedCurrencyPair = QuotedCurrencyPair.builder()
        .currency1(currency1)
        .currency2(currency2)
        .quoteBasis(quoteBasis)
        .build();
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code ExchangeRate}.
   * @return the meta-bean, not null
   */
  public static ExchangeRate.Meta meta() {
    return ExchangeRate.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(ExchangeRate.Meta.INSTANCE);
  }

  /**
   * Returns a builder used to create an instance of the bean.
   * @return the builder, not null
   */
  public static ExchangeRate.Builder builder() {
    return new ExchangeRate.Builder();
  }

  /**
   * Restricted constructor.
   * @param builder  the builder to copy from, not null
   */
  protected ExchangeRate(ExchangeRate.Builder builder) {
    JodaBeanUtils.notNull(builder._quotedCurrencyPair, "quotedCurrencyPair");
    this._quotedCurrencyPair = builder._quotedCurrencyPair;
    this._rate = builder._rate;
    this._spotRate = builder._spotRate;
    this._forwardPoints = builder._forwardPoints;
    this._fxForwardQuoteBasis = builder._fxForwardQuoteBasis;
    this._crossRate1 = builder._crossRate1;
    this._crossRate2 = builder._crossRate2;
    validate();
  }

  @Override
  public ExchangeRate.Meta metaBean() {
    return ExchangeRate.Meta.INSTANCE;
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
   * Gets the FX spot rate.
   * @return the value of the property
   */
  public BigDecimal getSpotRate() {
    return _spotRate;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the normalized FX forward points.
   * @return the value of the property
   */
  public BigDecimal getForwardPoints() {
    return _forwardPoints;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the FX forward quote basis.
   * @return the value of the property
   */
  public QuoteBasis getFxForwardQuoteBasis() {
    return _fxForwardQuoteBasis;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the first cross rate.
   * @return the value of the property
   */
  public CrossRate getCrossRate1() {
    return _crossRate1;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the second cross rate.
   * @return the value of the property
   */
  public CrossRate getCrossRate2() {
    return _crossRate2;
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
      ExchangeRate other = (ExchangeRate) obj;
      return JodaBeanUtils.equal(getQuotedCurrencyPair(), other.getQuotedCurrencyPair()) &&
          JodaBeanUtils.equal(getRate(), other.getRate()) &&
          JodaBeanUtils.equal(getSpotRate(), other.getSpotRate()) &&
          JodaBeanUtils.equal(getForwardPoints(), other.getForwardPoints()) &&
          JodaBeanUtils.equal(getFxForwardQuoteBasis(), other.getFxForwardQuoteBasis()) &&
          JodaBeanUtils.equal(getCrossRate1(), other.getCrossRate1()) &&
          JodaBeanUtils.equal(getCrossRate2(), other.getCrossRate2());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash = hash * 31 + JodaBeanUtils.hashCode(getQuotedCurrencyPair());
    hash = hash * 31 + JodaBeanUtils.hashCode(getRate());
    hash = hash * 31 + JodaBeanUtils.hashCode(getSpotRate());
    hash = hash * 31 + JodaBeanUtils.hashCode(getForwardPoints());
    hash = hash * 31 + JodaBeanUtils.hashCode(getFxForwardQuoteBasis());
    hash = hash * 31 + JodaBeanUtils.hashCode(getCrossRate1());
    hash = hash * 31 + JodaBeanUtils.hashCode(getCrossRate2());
    return hash;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(256);
    buf.append("ExchangeRate{");
    int len = buf.length();
    toString(buf);
    if (buf.length() > len) {
      buf.setLength(buf.length() - 2);
    }
    buf.append('}');
    return buf.toString();
  }

  protected void toString(StringBuilder buf) {
    buf.append("quotedCurrencyPair").append('=').append(JodaBeanUtils.toString(getQuotedCurrencyPair())).append(',').append(' ');
    buf.append("rate").append('=').append(JodaBeanUtils.toString(getRate())).append(',').append(' ');
    buf.append("spotRate").append('=').append(JodaBeanUtils.toString(getSpotRate())).append(',').append(' ');
    buf.append("forwardPoints").append('=').append(JodaBeanUtils.toString(getForwardPoints())).append(',').append(' ');
    buf.append("fxForwardQuoteBasis").append('=').append(JodaBeanUtils.toString(getFxForwardQuoteBasis())).append(',').append(' ');
    buf.append("crossRate1").append('=').append(JodaBeanUtils.toString(getCrossRate1())).append(',').append(' ');
    buf.append("crossRate2").append('=').append(JodaBeanUtils.toString(getCrossRate2())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code ExchangeRate}.
   */
  public static class Meta extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code quotedCurrencyPair} property.
     */
    private final MetaProperty<QuotedCurrencyPair> _quotedCurrencyPair = DirectMetaProperty.ofImmutable(
        this, "quotedCurrencyPair", ExchangeRate.class, QuotedCurrencyPair.class);
    /**
     * The meta-property for the {@code rate} property.
     */
    private final MetaProperty<BigDecimal> _rate = DirectMetaProperty.ofImmutable(
        this, "rate", ExchangeRate.class, BigDecimal.class);
    /**
     * The meta-property for the {@code spotRate} property.
     */
    private final MetaProperty<BigDecimal> _spotRate = DirectMetaProperty.ofImmutable(
        this, "spotRate", ExchangeRate.class, BigDecimal.class);
    /**
     * The meta-property for the {@code forwardPoints} property.
     */
    private final MetaProperty<BigDecimal> _forwardPoints = DirectMetaProperty.ofImmutable(
        this, "forwardPoints", ExchangeRate.class, BigDecimal.class);
    /**
     * The meta-property for the {@code fxForwardQuoteBasis} property.
     */
    private final MetaProperty<QuoteBasis> _fxForwardQuoteBasis = DirectMetaProperty.ofImmutable(
        this, "fxForwardQuoteBasis", ExchangeRate.class, QuoteBasis.class);
    /**
     * The meta-property for the {@code crossRate1} property.
     */
    private final MetaProperty<CrossRate> _crossRate1 = DirectMetaProperty.ofImmutable(
        this, "crossRate1", ExchangeRate.class, CrossRate.class);
    /**
     * The meta-property for the {@code crossRate2} property.
     */
    private final MetaProperty<CrossRate> _crossRate2 = DirectMetaProperty.ofImmutable(
        this, "crossRate2", ExchangeRate.class, CrossRate.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "quotedCurrencyPair",
        "rate",
        "spotRate",
        "forwardPoints",
        "fxForwardQuoteBasis",
        "crossRate1",
        "crossRate2");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case -1256824813:  // quotedCurrencyPair
          return _quotedCurrencyPair;
        case 3493088:  // rate
          return _rate;
        case -1831573246:  // spotRate
          return _spotRate;
        case -483724280:  // forwardPoints
          return _forwardPoints;
        case -1401028331:  // fxForwardQuoteBasis
          return _fxForwardQuoteBasis;
        case 366866161:  // crossRate1
          return _crossRate1;
        case 366866162:  // crossRate2
          return _crossRate2;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public ExchangeRate.Builder builder() {
      return new ExchangeRate.Builder();
    }

    @Override
    public Class<? extends ExchangeRate> beanType() {
      return ExchangeRate.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code quotedCurrencyPair} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<QuotedCurrencyPair> quotedCurrencyPair() {
      return _quotedCurrencyPair;
    }

    /**
     * The meta-property for the {@code rate} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<BigDecimal> rate() {
      return _rate;
    }

    /**
     * The meta-property for the {@code spotRate} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<BigDecimal> spotRate() {
      return _spotRate;
    }

    /**
     * The meta-property for the {@code forwardPoints} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<BigDecimal> forwardPoints() {
      return _forwardPoints;
    }

    /**
     * The meta-property for the {@code fxForwardQuoteBasis} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<QuoteBasis> fxForwardQuoteBasis() {
      return _fxForwardQuoteBasis;
    }

    /**
     * The meta-property for the {@code crossRate1} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<CrossRate> crossRate1() {
      return _crossRate1;
    }

    /**
     * The meta-property for the {@code crossRate2} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<CrossRate> crossRate2() {
      return _crossRate2;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -1256824813:  // quotedCurrencyPair
          return ((ExchangeRate) bean).getQuotedCurrencyPair();
        case 3493088:  // rate
          return ((ExchangeRate) bean).getRate();
        case -1831573246:  // spotRate
          return ((ExchangeRate) bean).getSpotRate();
        case -483724280:  // forwardPoints
          return ((ExchangeRate) bean).getForwardPoints();
        case -1401028331:  // fxForwardQuoteBasis
          return ((ExchangeRate) bean).getFxForwardQuoteBasis();
        case 366866161:  // crossRate1
          return ((ExchangeRate) bean).getCrossRate1();
        case 366866162:  // crossRate2
          return ((ExchangeRate) bean).getCrossRate2();
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
   * The bean-builder for {@code ExchangeRate}.
   */
  public static class Builder extends DirectFieldsBeanBuilder<ExchangeRate> {

    private QuotedCurrencyPair _quotedCurrencyPair;
    private BigDecimal _rate;
    private BigDecimal _spotRate;
    private BigDecimal _forwardPoints;
    private QuoteBasis _fxForwardQuoteBasis;
    private CrossRate _crossRate1;
    private CrossRate _crossRate2;

    /**
     * Restricted constructor.
     */
    protected Builder() {
    }

    /**
     * Restricted copy constructor.
     * @param beanToCopy  the bean to copy from, not null
     */
    protected Builder(ExchangeRate beanToCopy) {
      this._quotedCurrencyPair = beanToCopy.getQuotedCurrencyPair();
      this._rate = beanToCopy.getRate();
      this._spotRate = beanToCopy.getSpotRate();
      this._forwardPoints = beanToCopy.getForwardPoints();
      this._fxForwardQuoteBasis = beanToCopy.getFxForwardQuoteBasis();
      this._crossRate1 = beanToCopy.getCrossRate1();
      this._crossRate2 = beanToCopy.getCrossRate2();
    }

    //-----------------------------------------------------------------------
    @Override
    public Object get(String propertyName) {
      switch (propertyName.hashCode()) {
        case -1256824813:  // quotedCurrencyPair
          return _quotedCurrencyPair;
        case 3493088:  // rate
          return _rate;
        case -1831573246:  // spotRate
          return _spotRate;
        case -483724280:  // forwardPoints
          return _forwardPoints;
        case -1401028331:  // fxForwardQuoteBasis
          return _fxForwardQuoteBasis;
        case 366866161:  // crossRate1
          return _crossRate1;
        case 366866162:  // crossRate2
          return _crossRate2;
        default:
          throw new NoSuchElementException("Unknown property: " + propertyName);
      }
    }

    @Override
    public Builder set(String propertyName, Object newValue) {
      switch (propertyName.hashCode()) {
        case -1256824813:  // quotedCurrencyPair
          this._quotedCurrencyPair = (QuotedCurrencyPair) newValue;
          break;
        case 3493088:  // rate
          this._rate = (BigDecimal) newValue;
          break;
        case -1831573246:  // spotRate
          this._spotRate = (BigDecimal) newValue;
          break;
        case -483724280:  // forwardPoints
          this._forwardPoints = (BigDecimal) newValue;
          break;
        case -1401028331:  // fxForwardQuoteBasis
          this._fxForwardQuoteBasis = (QuoteBasis) newValue;
          break;
        case 366866161:  // crossRate1
          this._crossRate1 = (CrossRate) newValue;
          break;
        case 366866162:  // crossRate2
          this._crossRate2 = (CrossRate) newValue;
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
    public ExchangeRate build() {
      return new ExchangeRate(this);
    }

    //-----------------------------------------------------------------------
    /**
     * Sets the quoted currency pair.
     * @param quotedCurrencyPair  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder quotedCurrencyPair(QuotedCurrencyPair quotedCurrencyPair) {
      JodaBeanUtils.notNull(quotedCurrencyPair, "quotedCurrencyPair");
      this._quotedCurrencyPair = quotedCurrencyPair;
      return this;
    }

    /**
     * Sets the rate for this quote.
     * @param rate  the new value
     * @return this, for chaining, not null
     */
    public Builder rate(BigDecimal rate) {
      this._rate = rate;
      return this;
    }

    /**
     * Sets the FX spot rate.
     * @param spotRate  the new value
     * @return this, for chaining, not null
     */
    public Builder spotRate(BigDecimal spotRate) {
      this._spotRate = spotRate;
      return this;
    }

    /**
     * Sets the normalized FX forward points.
     * @param forwardPoints  the new value
     * @return this, for chaining, not null
     */
    public Builder forwardPoints(BigDecimal forwardPoints) {
      this._forwardPoints = forwardPoints;
      return this;
    }

    /**
     * Sets the FX forward quote basis.
     * @param fxForwardQuoteBasis  the new value
     * @return this, for chaining, not null
     */
    public Builder fxForwardQuoteBasis(QuoteBasis fxForwardQuoteBasis) {
      this._fxForwardQuoteBasis = fxForwardQuoteBasis;
      return this;
    }

    /**
     * Sets the first cross rate.
     * @param crossRate1  the new value
     * @return this, for chaining, not null
     */
    public Builder crossRate1(CrossRate crossRate1) {
      this._crossRate1 = crossRate1;
      return this;
    }

    /**
     * Sets the second cross rate.
     * @param crossRate2  the new value
     * @return this, for chaining, not null
     */
    public Builder crossRate2(CrossRate crossRate2) {
      this._crossRate2 = crossRate2;
      return this;
    }

    //-----------------------------------------------------------------------
    @Override
    public String toString() {
      StringBuilder buf = new StringBuilder(256);
      buf.append("ExchangeRate.Builder{");
      int len = buf.length();
      toString(buf);
      if (buf.length() > len) {
        buf.setLength(buf.length() - 2);
      }
      buf.append('}');
      return buf.toString();
    }

    protected void toString(StringBuilder buf) {
      buf.append("quotedCurrencyPair").append('=').append(JodaBeanUtils.toString(_quotedCurrencyPair)).append(',').append(' ');
      buf.append("rate").append('=').append(JodaBeanUtils.toString(_rate)).append(',').append(' ');
      buf.append("spotRate").append('=').append(JodaBeanUtils.toString(_spotRate)).append(',').append(' ');
      buf.append("forwardPoints").append('=').append(JodaBeanUtils.toString(_forwardPoints)).append(',').append(' ');
      buf.append("fxForwardQuoteBasis").append('=').append(JodaBeanUtils.toString(_fxForwardQuoteBasis)).append(',').append(' ');
      buf.append("crossRate1").append('=').append(JodaBeanUtils.toString(_crossRate1)).append(',').append(' ');
      buf.append("crossRate2").append('=').append(JodaBeanUtils.toString(_crossRate2)).append(',').append(' ');
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
