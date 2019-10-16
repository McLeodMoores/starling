/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.mcleodmoores.examples.simulated.component;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import org.joda.beans.Bean;
import org.joda.beans.BeanBuilder;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.impl.direct.DirectBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;
import org.springframework.core.io.Resource;

import com.google.common.collect.ImmutableList;
import com.mcleodmoores.examples.simulated.livedata.ExampleCreditLiveDataServer;
import com.mcleodmoores.examples.simulated.livedata.ExamplesIdResolver;
import com.opengamma.component.ComponentRepository;
import com.opengamma.component.factory.livedata.AbstractStandardLiveDataServerComponentFactory;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.examples.simulated.component.ExampleMarketDataComponentFactory;
import com.opengamma.examples.simulated.livedata.ExampleJmsTopicNameResolver;
import com.opengamma.examples.simulated.livedata.ExampleLiveDataServer;
import com.opengamma.examples.simulated.livedata.ExampleLiveDataServerMBean;
import com.opengamma.examples.simulated.livedata.NormalizationRules;
import com.opengamma.financial.credit.CdsRecoveryRateIdentifier;
import com.opengamma.livedata.normalization.NormalizationRuleSet;
import com.opengamma.livedata.normalization.StandardRuleResolver;
import com.opengamma.livedata.normalization.StandardRules;
import com.opengamma.livedata.resolver.DefaultDistributionSpecificationResolver;
import com.opengamma.livedata.server.StandardLiveDataServer;
import com.opengamma.livedata.server.combining.PriorityResolvingCombiningLiveDataServer;
import com.opengamma.livedata.server.distribution.JmsSenderFactory;
import com.opengamma.provider.livedata.LiveDataMetaData;
import com.opengamma.provider.livedata.LiveDataServerTypes;

import net.sf.ehcache.CacheManager;
import org.joda.beans.MetaBean;
import org.joda.beans.gen.BeanDefinition;
import org.joda.beans.gen.PropertyDefinition;

/**
 * Component factory for producing simulated live data.
 */
@BeanDefinition
public class ExampleLiveDataServerComponentFactory extends AbstractStandardLiveDataServerComponentFactory {
  /**
   * The JMS connector.
   */
  @PropertyDefinition(validate = "notNull")
  private Resource _simulatedData;
  /**
   * The JSM connector.
   */
  @PropertyDefinition(validate = "notNull")
  private Resource _simulatedCreditData;
  /**
   * The EH cache configuration.
   */
  @PropertyDefinition(validate = "notNull")
  private CacheManager _cacheManager;
  /**
   * Maximum millis between ticks.
   */
  @PropertyDefinition
  private Integer _maxMillisBetweenTicks = ExampleLiveDataServer.MAX_MILLIS_BETWEEN_TICKS;
  /**
   * Scaling factor.
   */
  @PropertyDefinition
  private Double _scalingFactor = ExampleLiveDataServer.SCALING_FACTOR;

  // -------------------------------------------------------------------------
  @Override
  protected StandardLiveDataServer initServer(final ComponentRepository repo) {
    final ExampleLiveDataServer marketData = new ExampleLiveDataServer(getCacheManager(), getSimulatedData(), getScalingFactor(), getMaxMillisBetweenTicks());
    final ExampleLiveDataServer creditMarketData = new ExampleCreditLiveDataServer(getCacheManager(), getSimulatedCreditData(), getScalingFactor(),
        getMaxMillisBetweenTicks());
    final StandardLiveDataServer server = new PriorityResolvingCombiningLiveDataServer(Arrays.asList(marketData, creditMarketData), getCacheManager());
    final Collection<NormalizationRuleSet> rules = ImmutableList.of(StandardRules.getNoNormalization(), NormalizationRules.getMarketValueNormalization());
    final DefaultDistributionSpecificationResolver marketDataDistSpecResolver = new DefaultDistributionSpecificationResolver(
        new ExamplesIdResolver(ExternalSchemes.OG_SYNTHETIC_TICKER), new StandardRuleResolver(rules), new ExampleJmsTopicNameResolver());
    final DefaultDistributionSpecificationResolver creditMarketDataDistSpecResolver = new DefaultDistributionSpecificationResolver(
        new ExamplesIdResolver(CdsRecoveryRateIdentifier.SAMEDAY_CDS_SCHEME), new StandardRuleResolver(rules), new ExampleJmsTopicNameResolver());
    marketData.setDistributionSpecificationResolver(marketDataDistSpecResolver);
    creditMarketData.setDistributionSpecificationResolver(creditMarketDataDistSpecResolver);

    final JmsSenderFactory senderFactory = new JmsSenderFactory(getJmsConnector());
    marketData.setMarketDataSenderFactory(senderFactory);
    creditMarketData.setMarketDataSenderFactory(senderFactory);

    repo.registerMBean(new ExampleLiveDataServerMBean(marketData));
    repo.registerMBean(new ExampleLiveDataServerMBean(creditMarketData));
    return server;
  }

  @Override
  protected LiveDataMetaData createMetaData(final ComponentRepository repo) {
    return new LiveDataMetaData(
        ImmutableList.of(ExternalSchemes.OG_SYNTHETIC_TICKER, CdsRecoveryRateIdentifier.SAMEDAY_CDS_SCHEME, CdsRecoveryRateIdentifier.COMPOSITE_CDS_SCHEME),
        LiveDataServerTypes.STANDARD, ExampleMarketDataComponentFactory.SIMULATED_LIVE_SOURCE_NAME);
  }

  //------------------------- AUTOGENERATED START -------------------------
  /**
   * The meta-bean for {@code ExampleLiveDataServerComponentFactory}.
   * @return the meta-bean, not null
   */
  public static ExampleLiveDataServerComponentFactory.Meta meta() {
    return ExampleLiveDataServerComponentFactory.Meta.INSTANCE;
  }

  static {
    MetaBean.register(ExampleLiveDataServerComponentFactory.Meta.INSTANCE);
  }

  @Override
  public ExampleLiveDataServerComponentFactory.Meta metaBean() {
    return ExampleLiveDataServerComponentFactory.Meta.INSTANCE;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the JMS connector.
   * @return the value of the property, not null
   */
  public Resource getSimulatedData() {
    return _simulatedData;
  }

  /**
   * Sets the JMS connector.
   * @param simulatedData  the new value of the property, not null
   */
  public void setSimulatedData(Resource simulatedData) {
    JodaBeanUtils.notNull(simulatedData, "simulatedData");
    this._simulatedData = simulatedData;
  }

  /**
   * Gets the the {@code simulatedData} property.
   * @return the property, not null
   */
  public final Property<Resource> simulatedData() {
    return metaBean().simulatedData().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the JSM connector.
   * @return the value of the property, not null
   */
  public Resource getSimulatedCreditData() {
    return _simulatedCreditData;
  }

  /**
   * Sets the JSM connector.
   * @param simulatedCreditData  the new value of the property, not null
   */
  public void setSimulatedCreditData(Resource simulatedCreditData) {
    JodaBeanUtils.notNull(simulatedCreditData, "simulatedCreditData");
    this._simulatedCreditData = simulatedCreditData;
  }

  /**
   * Gets the the {@code simulatedCreditData} property.
   * @return the property, not null
   */
  public final Property<Resource> simulatedCreditData() {
    return metaBean().simulatedCreditData().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the EH cache configuration.
   * @return the value of the property, not null
   */
  public CacheManager getCacheManager() {
    return _cacheManager;
  }

  /**
   * Sets the EH cache configuration.
   * @param cacheManager  the new value of the property, not null
   */
  public void setCacheManager(CacheManager cacheManager) {
    JodaBeanUtils.notNull(cacheManager, "cacheManager");
    this._cacheManager = cacheManager;
  }

  /**
   * Gets the the {@code cacheManager} property.
   * @return the property, not null
   */
  public final Property<CacheManager> cacheManager() {
    return metaBean().cacheManager().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets maximum millis between ticks.
   * @return the value of the property
   */
  public Integer getMaxMillisBetweenTicks() {
    return _maxMillisBetweenTicks;
  }

  /**
   * Sets maximum millis between ticks.
   * @param maxMillisBetweenTicks  the new value of the property
   */
  public void setMaxMillisBetweenTicks(Integer maxMillisBetweenTicks) {
    this._maxMillisBetweenTicks = maxMillisBetweenTicks;
  }

  /**
   * Gets the the {@code maxMillisBetweenTicks} property.
   * @return the property, not null
   */
  public final Property<Integer> maxMillisBetweenTicks() {
    return metaBean().maxMillisBetweenTicks().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets scaling factor.
   * @return the value of the property
   */
  public Double getScalingFactor() {
    return _scalingFactor;
  }

  /**
   * Sets scaling factor.
   * @param scalingFactor  the new value of the property
   */
  public void setScalingFactor(Double scalingFactor) {
    this._scalingFactor = scalingFactor;
  }

  /**
   * Gets the the {@code scalingFactor} property.
   * @return the property, not null
   */
  public final Property<Double> scalingFactor() {
    return metaBean().scalingFactor().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public ExampleLiveDataServerComponentFactory clone() {
    return JodaBeanUtils.cloneAlways(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      ExampleLiveDataServerComponentFactory other = (ExampleLiveDataServerComponentFactory) obj;
      return JodaBeanUtils.equal(getSimulatedData(), other.getSimulatedData()) &&
          JodaBeanUtils.equal(getSimulatedCreditData(), other.getSimulatedCreditData()) &&
          JodaBeanUtils.equal(getCacheManager(), other.getCacheManager()) &&
          JodaBeanUtils.equal(getMaxMillisBetweenTicks(), other.getMaxMillisBetweenTicks()) &&
          JodaBeanUtils.equal(getScalingFactor(), other.getScalingFactor()) &&
          super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = hash * 31 + JodaBeanUtils.hashCode(getSimulatedData());
    hash = hash * 31 + JodaBeanUtils.hashCode(getSimulatedCreditData());
    hash = hash * 31 + JodaBeanUtils.hashCode(getCacheManager());
    hash = hash * 31 + JodaBeanUtils.hashCode(getMaxMillisBetweenTicks());
    hash = hash * 31 + JodaBeanUtils.hashCode(getScalingFactor());
    return hash ^ super.hashCode();
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(192);
    buf.append("ExampleLiveDataServerComponentFactory{");
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
    buf.append("simulatedData").append('=').append(JodaBeanUtils.toString(getSimulatedData())).append(',').append(' ');
    buf.append("simulatedCreditData").append('=').append(JodaBeanUtils.toString(getSimulatedCreditData())).append(',').append(' ');
    buf.append("cacheManager").append('=').append(JodaBeanUtils.toString(getCacheManager())).append(',').append(' ');
    buf.append("maxMillisBetweenTicks").append('=').append(JodaBeanUtils.toString(getMaxMillisBetweenTicks())).append(',').append(' ');
    buf.append("scalingFactor").append('=').append(JodaBeanUtils.toString(getScalingFactor())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code ExampleLiveDataServerComponentFactory}.
   */
  public static class Meta extends AbstractStandardLiveDataServerComponentFactory.Meta {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code simulatedData} property.
     */
    private final MetaProperty<Resource> _simulatedData = DirectMetaProperty.ofReadWrite(
        this, "simulatedData", ExampleLiveDataServerComponentFactory.class, Resource.class);
    /**
     * The meta-property for the {@code simulatedCreditData} property.
     */
    private final MetaProperty<Resource> _simulatedCreditData = DirectMetaProperty.ofReadWrite(
        this, "simulatedCreditData", ExampleLiveDataServerComponentFactory.class, Resource.class);
    /**
     * The meta-property for the {@code cacheManager} property.
     */
    private final MetaProperty<CacheManager> _cacheManager = DirectMetaProperty.ofReadWrite(
        this, "cacheManager", ExampleLiveDataServerComponentFactory.class, CacheManager.class);
    /**
     * The meta-property for the {@code maxMillisBetweenTicks} property.
     */
    private final MetaProperty<Integer> _maxMillisBetweenTicks = DirectMetaProperty.ofReadWrite(
        this, "maxMillisBetweenTicks", ExampleLiveDataServerComponentFactory.class, Integer.class);
    /**
     * The meta-property for the {@code scalingFactor} property.
     */
    private final MetaProperty<Double> _scalingFactor = DirectMetaProperty.ofReadWrite(
        this, "scalingFactor", ExampleLiveDataServerComponentFactory.class, Double.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "simulatedData",
        "simulatedCreditData",
        "cacheManager",
        "maxMillisBetweenTicks",
        "scalingFactor");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case -349682038:  // simulatedData
          return _simulatedData;
        case -1947559645:  // simulatedCreditData
          return _simulatedCreditData;
        case -1452875317:  // cacheManager
          return _cacheManager;
        case -1944334024:  // maxMillisBetweenTicks
          return _maxMillisBetweenTicks;
        case -794828874:  // scalingFactor
          return _scalingFactor;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends ExampleLiveDataServerComponentFactory> builder() {
      return new DirectBeanBuilder<>(new ExampleLiveDataServerComponentFactory());
    }

    @Override
    public Class<? extends ExampleLiveDataServerComponentFactory> beanType() {
      return ExampleLiveDataServerComponentFactory.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code simulatedData} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Resource> simulatedData() {
      return _simulatedData;
    }

    /**
     * The meta-property for the {@code simulatedCreditData} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Resource> simulatedCreditData() {
      return _simulatedCreditData;
    }

    /**
     * The meta-property for the {@code cacheManager} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<CacheManager> cacheManager() {
      return _cacheManager;
    }

    /**
     * The meta-property for the {@code maxMillisBetweenTicks} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Integer> maxMillisBetweenTicks() {
      return _maxMillisBetweenTicks;
    }

    /**
     * The meta-property for the {@code scalingFactor} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Double> scalingFactor() {
      return _scalingFactor;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -349682038:  // simulatedData
          return ((ExampleLiveDataServerComponentFactory) bean).getSimulatedData();
        case -1947559645:  // simulatedCreditData
          return ((ExampleLiveDataServerComponentFactory) bean).getSimulatedCreditData();
        case -1452875317:  // cacheManager
          return ((ExampleLiveDataServerComponentFactory) bean).getCacheManager();
        case -1944334024:  // maxMillisBetweenTicks
          return ((ExampleLiveDataServerComponentFactory) bean).getMaxMillisBetweenTicks();
        case -794828874:  // scalingFactor
          return ((ExampleLiveDataServerComponentFactory) bean).getScalingFactor();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -349682038:  // simulatedData
          ((ExampleLiveDataServerComponentFactory) bean).setSimulatedData((Resource) newValue);
          return;
        case -1947559645:  // simulatedCreditData
          ((ExampleLiveDataServerComponentFactory) bean).setSimulatedCreditData((Resource) newValue);
          return;
        case -1452875317:  // cacheManager
          ((ExampleLiveDataServerComponentFactory) bean).setCacheManager((CacheManager) newValue);
          return;
        case -1944334024:  // maxMillisBetweenTicks
          ((ExampleLiveDataServerComponentFactory) bean).setMaxMillisBetweenTicks((Integer) newValue);
          return;
        case -794828874:  // scalingFactor
          ((ExampleLiveDataServerComponentFactory) bean).setScalingFactor((Double) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

    @Override
    protected void validate(Bean bean) {
      JodaBeanUtils.notNull(((ExampleLiveDataServerComponentFactory) bean)._simulatedData, "simulatedData");
      JodaBeanUtils.notNull(((ExampleLiveDataServerComponentFactory) bean)._simulatedCreditData, "simulatedCreditData");
      JodaBeanUtils.notNull(((ExampleLiveDataServerComponentFactory) bean)._cacheManager, "cacheManager");
      super.validate(bean);
    }

  }

  //-------------------------- AUTOGENERATED END --------------------------
}
