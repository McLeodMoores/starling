/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.component.factory.engine;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;

import org.fudgemsg.FudgeContext;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Duration;

import com.opengamma.component.ComponentInfo;
import com.opengamma.component.ComponentRepository;
import com.opengamma.component.factory.AbstractComponentFactory;
import com.opengamma.component.factory.ComponentInfoAttributes;
import com.opengamma.financial.user.DefaultFinancialUsersTracker;
import com.opengamma.financial.user.FinancialUserManager;
import com.opengamma.financial.user.FinancialUserServices;
import com.opengamma.financial.user.rest.DataFinancialUserManagerResource;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotMaster;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;

/**
 * A version of {@link com.opengamma.component.factory.engine.FinancialUserManagerComponentFactory} that does
 * not reference deprecated components or components that are only used by deprecated functionality (e.g.
 * {@link com.opengamma.financial.analytics.ircurve.InterpolatedYieldCurveDefinitionMaster}. The only sources
 * that are required to be populated are the {@link SecurityMaster}, {@link PositionMaster} and {@link PortfolioMaster}.
 * <p>
 * This factory only populates {@link com.opengamma.core.Source}s, although it could be extended to use masters
 * if required.
 */
@BeanDefinition
public class MinimalFinancialUserManagerComponentFactory extends AbstractComponentFactory {
  /** The logger */
  private static final Logger LOGGER = LoggerFactory.getLogger(MinimalFinancialUserManagerComponentFactory.class);

  /**
   * The classifier that the factory should publish under.
   */
  @PropertyDefinition(validate = "notNull")
  private String _classifier;

  /**
   * The flag determining whether the component should be published by REST (default true).
   */
  @PropertyDefinition
  private boolean _publishRest = true;

  /**
   * The fudge context.
   */
  @PropertyDefinition(validate = "notNull")
  private FudgeContext _fudgeContext = OpenGammaFudgeContext.getInstance();

  /**
   * The security master.
   */
  @PropertyDefinition(validate = "notNull")
  private SecurityMaster _securityMaster;

  /**
   * The position master.
   */
  @PropertyDefinition(validate = "notNull")
  private PositionMaster _positionMaster;

  /**
   * The portfolio master.
   */
  @PropertyDefinition(validate = "notNull")
  private PortfolioMaster _portfolioMaster;

  /**
   * The snapshot master.
   */
  @PropertyDefinition
  private MarketDataSnapshotMaster _snapshotMaster;

  /**
   * The config master.
   */
  @PropertyDefinition
  private ConfigMaster _configMaster;

  /**
   * The scheduler for deleting clients.
   */
  @PropertyDefinition
  private ScheduledExecutorService _scheduler;

  /**
   * The time out for clients (default 30 minutes).
   */
  @PropertyDefinition
  private Duration _clientTimeOut = Duration.ofMinutes(30);

  @Override
  public void init(final ComponentRepository repo, final LinkedHashMap<String, String> configuration) {
    final FinancialUserServices services = new FinancialUserServices();
    services.setFudgeContext(getFudgeContext());
    services.setUserSecurityMaster(getSecurityMaster());
    services.setUserPositionMaster(getPositionMaster());
    services.setUserPortfolioMaster(getPortfolioMaster());
    if (getSnapshotMaster() != null) {
      services.setUserSnapshotMaster(getSnapshotMaster());
    } else {
      LOGGER.warn("SnapshotMaster not set in MinimalFinancialUserManagerComponentFactory");
    }
    if (getConfigMaster() != null) {
      services.setUserConfigMaster(getConfigMaster());
    } else {
      LOGGER.warn("ConfigMaster not set in MinimalFinancialUserManagerComponentFactory");
    }
    final DefaultFinancialUsersTracker tracker = new DefaultFinancialUsersTracker(services);
    final FinancialUserManager manager = new FinancialUserManager(services, tracker, tracker);
    manager.createDeleteTask(getScheduler(), getClientTimeOut());

    final ComponentInfo info = new ComponentInfo(FinancialUserManager.class, getClassifier());
    info.addAttribute(ComponentInfoAttributes.TIMEOUT, getClientTimeOut().toString());
    repo.registerComponent(info, manager);

    if (isPublishRest()) {
      final DataFinancialUserManagerResource resource = new DataFinancialUserManagerResource(manager);
      repo.getRestComponents().publish(info, resource);
    }
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code MinimalFinancialUserManagerComponentFactory}.
   * @return the meta-bean, not null
   */
  public static MinimalFinancialUserManagerComponentFactory.Meta meta() {
    return MinimalFinancialUserManagerComponentFactory.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(MinimalFinancialUserManagerComponentFactory.Meta.INSTANCE);
  }

  @Override
  public MinimalFinancialUserManagerComponentFactory.Meta metaBean() {
    return MinimalFinancialUserManagerComponentFactory.Meta.INSTANCE;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the classifier that the factory should publish under.
   * @return the value of the property, not null
   */
  public String getClassifier() {
    return _classifier;
  }

  /**
   * Sets the classifier that the factory should publish under.
   * @param classifier  the new value of the property, not null
   */
  public void setClassifier(String classifier) {
    JodaBeanUtils.notNull(classifier, "classifier");
    this._classifier = classifier;
  }

  /**
   * Gets the the {@code classifier} property.
   * @return the property, not null
   */
  public final Property<String> classifier() {
    return metaBean().classifier().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the flag determining whether the component should be published by REST (default true).
   * @return the value of the property
   */
  public boolean isPublishRest() {
    return _publishRest;
  }

  /**
   * Sets the flag determining whether the component should be published by REST (default true).
   * @param publishRest  the new value of the property
   */
  public void setPublishRest(boolean publishRest) {
    this._publishRest = publishRest;
  }

  /**
   * Gets the the {@code publishRest} property.
   * @return the property, not null
   */
  public final Property<Boolean> publishRest() {
    return metaBean().publishRest().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the fudge context.
   * @return the value of the property, not null
   */
  public FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

  /**
   * Sets the fudge context.
   * @param fudgeContext  the new value of the property, not null
   */
  public void setFudgeContext(FudgeContext fudgeContext) {
    JodaBeanUtils.notNull(fudgeContext, "fudgeContext");
    this._fudgeContext = fudgeContext;
  }

  /**
   * Gets the the {@code fudgeContext} property.
   * @return the property, not null
   */
  public final Property<FudgeContext> fudgeContext() {
    return metaBean().fudgeContext().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the security master.
   * @return the value of the property, not null
   */
  public SecurityMaster getSecurityMaster() {
    return _securityMaster;
  }

  /**
   * Sets the security master.
   * @param securityMaster  the new value of the property, not null
   */
  public void setSecurityMaster(SecurityMaster securityMaster) {
    JodaBeanUtils.notNull(securityMaster, "securityMaster");
    this._securityMaster = securityMaster;
  }

  /**
   * Gets the the {@code securityMaster} property.
   * @return the property, not null
   */
  public final Property<SecurityMaster> securityMaster() {
    return metaBean().securityMaster().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the position master.
   * @return the value of the property, not null
   */
  public PositionMaster getPositionMaster() {
    return _positionMaster;
  }

  /**
   * Sets the position master.
   * @param positionMaster  the new value of the property, not null
   */
  public void setPositionMaster(PositionMaster positionMaster) {
    JodaBeanUtils.notNull(positionMaster, "positionMaster");
    this._positionMaster = positionMaster;
  }

  /**
   * Gets the the {@code positionMaster} property.
   * @return the property, not null
   */
  public final Property<PositionMaster> positionMaster() {
    return metaBean().positionMaster().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the portfolio master.
   * @return the value of the property, not null
   */
  public PortfolioMaster getPortfolioMaster() {
    return _portfolioMaster;
  }

  /**
   * Sets the portfolio master.
   * @param portfolioMaster  the new value of the property, not null
   */
  public void setPortfolioMaster(PortfolioMaster portfolioMaster) {
    JodaBeanUtils.notNull(portfolioMaster, "portfolioMaster");
    this._portfolioMaster = portfolioMaster;
  }

  /**
   * Gets the the {@code portfolioMaster} property.
   * @return the property, not null
   */
  public final Property<PortfolioMaster> portfolioMaster() {
    return metaBean().portfolioMaster().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the snapshot master.
   * @return the value of the property
   */
  public MarketDataSnapshotMaster getSnapshotMaster() {
    return _snapshotMaster;
  }

  /**
   * Sets the snapshot master.
   * @param snapshotMaster  the new value of the property
   */
  public void setSnapshotMaster(MarketDataSnapshotMaster snapshotMaster) {
    this._snapshotMaster = snapshotMaster;
  }

  /**
   * Gets the the {@code snapshotMaster} property.
   * @return the property, not null
   */
  public final Property<MarketDataSnapshotMaster> snapshotMaster() {
    return metaBean().snapshotMaster().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the config master.
   * @return the value of the property
   */
  public ConfigMaster getConfigMaster() {
    return _configMaster;
  }

  /**
   * Sets the config master.
   * @param configMaster  the new value of the property
   */
  public void setConfigMaster(ConfigMaster configMaster) {
    this._configMaster = configMaster;
  }

  /**
   * Gets the the {@code configMaster} property.
   * @return the property, not null
   */
  public final Property<ConfigMaster> configMaster() {
    return metaBean().configMaster().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the scheduler for deleting clients.
   * @return the value of the property
   */
  public ScheduledExecutorService getScheduler() {
    return _scheduler;
  }

  /**
   * Sets the scheduler for deleting clients.
   * @param scheduler  the new value of the property
   */
  public void setScheduler(ScheduledExecutorService scheduler) {
    this._scheduler = scheduler;
  }

  /**
   * Gets the the {@code scheduler} property.
   * @return the property, not null
   */
  public final Property<ScheduledExecutorService> scheduler() {
    return metaBean().scheduler().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the time out for clients (default 30 minutes).
   * @return the value of the property
   */
  public Duration getClientTimeOut() {
    return _clientTimeOut;
  }

  /**
   * Sets the time out for clients (default 30 minutes).
   * @param clientTimeOut  the new value of the property
   */
  public void setClientTimeOut(Duration clientTimeOut) {
    this._clientTimeOut = clientTimeOut;
  }

  /**
   * Gets the the {@code clientTimeOut} property.
   * @return the property, not null
   */
  public final Property<Duration> clientTimeOut() {
    return metaBean().clientTimeOut().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public MinimalFinancialUserManagerComponentFactory clone() {
    return JodaBeanUtils.cloneAlways(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      MinimalFinancialUserManagerComponentFactory other = (MinimalFinancialUserManagerComponentFactory) obj;
      return JodaBeanUtils.equal(getClassifier(), other.getClassifier()) &&
          (isPublishRest() == other.isPublishRest()) &&
          JodaBeanUtils.equal(getFudgeContext(), other.getFudgeContext()) &&
          JodaBeanUtils.equal(getSecurityMaster(), other.getSecurityMaster()) &&
          JodaBeanUtils.equal(getPositionMaster(), other.getPositionMaster()) &&
          JodaBeanUtils.equal(getPortfolioMaster(), other.getPortfolioMaster()) &&
          JodaBeanUtils.equal(getSnapshotMaster(), other.getSnapshotMaster()) &&
          JodaBeanUtils.equal(getConfigMaster(), other.getConfigMaster()) &&
          JodaBeanUtils.equal(getScheduler(), other.getScheduler()) &&
          JodaBeanUtils.equal(getClientTimeOut(), other.getClientTimeOut()) &&
          super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = hash * 31 + JodaBeanUtils.hashCode(getClassifier());
    hash = hash * 31 + JodaBeanUtils.hashCode(isPublishRest());
    hash = hash * 31 + JodaBeanUtils.hashCode(getFudgeContext());
    hash = hash * 31 + JodaBeanUtils.hashCode(getSecurityMaster());
    hash = hash * 31 + JodaBeanUtils.hashCode(getPositionMaster());
    hash = hash * 31 + JodaBeanUtils.hashCode(getPortfolioMaster());
    hash = hash * 31 + JodaBeanUtils.hashCode(getSnapshotMaster());
    hash = hash * 31 + JodaBeanUtils.hashCode(getConfigMaster());
    hash = hash * 31 + JodaBeanUtils.hashCode(getScheduler());
    hash = hash * 31 + JodaBeanUtils.hashCode(getClientTimeOut());
    return hash ^ super.hashCode();
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(352);
    buf.append("MinimalFinancialUserManagerComponentFactory{");
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
    buf.append("classifier").append('=').append(JodaBeanUtils.toString(getClassifier())).append(',').append(' ');
    buf.append("publishRest").append('=').append(JodaBeanUtils.toString(isPublishRest())).append(',').append(' ');
    buf.append("fudgeContext").append('=').append(JodaBeanUtils.toString(getFudgeContext())).append(',').append(' ');
    buf.append("securityMaster").append('=').append(JodaBeanUtils.toString(getSecurityMaster())).append(',').append(' ');
    buf.append("positionMaster").append('=').append(JodaBeanUtils.toString(getPositionMaster())).append(',').append(' ');
    buf.append("portfolioMaster").append('=').append(JodaBeanUtils.toString(getPortfolioMaster())).append(',').append(' ');
    buf.append("snapshotMaster").append('=').append(JodaBeanUtils.toString(getSnapshotMaster())).append(',').append(' ');
    buf.append("configMaster").append('=').append(JodaBeanUtils.toString(getConfigMaster())).append(',').append(' ');
    buf.append("scheduler").append('=').append(JodaBeanUtils.toString(getScheduler())).append(',').append(' ');
    buf.append("clientTimeOut").append('=').append(JodaBeanUtils.toString(getClientTimeOut())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code MinimalFinancialUserManagerComponentFactory}.
   */
  public static class Meta extends AbstractComponentFactory.Meta {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code classifier} property.
     */
    private final MetaProperty<String> _classifier = DirectMetaProperty.ofReadWrite(
        this, "classifier", MinimalFinancialUserManagerComponentFactory.class, String.class);
    /**
     * The meta-property for the {@code publishRest} property.
     */
    private final MetaProperty<Boolean> _publishRest = DirectMetaProperty.ofReadWrite(
        this, "publishRest", MinimalFinancialUserManagerComponentFactory.class, Boolean.TYPE);
    /**
     * The meta-property for the {@code fudgeContext} property.
     */
    private final MetaProperty<FudgeContext> _fudgeContext = DirectMetaProperty.ofReadWrite(
        this, "fudgeContext", MinimalFinancialUserManagerComponentFactory.class, FudgeContext.class);
    /**
     * The meta-property for the {@code securityMaster} property.
     */
    private final MetaProperty<SecurityMaster> _securityMaster = DirectMetaProperty.ofReadWrite(
        this, "securityMaster", MinimalFinancialUserManagerComponentFactory.class, SecurityMaster.class);
    /**
     * The meta-property for the {@code positionMaster} property.
     */
    private final MetaProperty<PositionMaster> _positionMaster = DirectMetaProperty.ofReadWrite(
        this, "positionMaster", MinimalFinancialUserManagerComponentFactory.class, PositionMaster.class);
    /**
     * The meta-property for the {@code portfolioMaster} property.
     */
    private final MetaProperty<PortfolioMaster> _portfolioMaster = DirectMetaProperty.ofReadWrite(
        this, "portfolioMaster", MinimalFinancialUserManagerComponentFactory.class, PortfolioMaster.class);
    /**
     * The meta-property for the {@code snapshotMaster} property.
     */
    private final MetaProperty<MarketDataSnapshotMaster> _snapshotMaster = DirectMetaProperty.ofReadWrite(
        this, "snapshotMaster", MinimalFinancialUserManagerComponentFactory.class, MarketDataSnapshotMaster.class);
    /**
     * The meta-property for the {@code configMaster} property.
     */
    private final MetaProperty<ConfigMaster> _configMaster = DirectMetaProperty.ofReadWrite(
        this, "configMaster", MinimalFinancialUserManagerComponentFactory.class, ConfigMaster.class);
    /**
     * The meta-property for the {@code scheduler} property.
     */
    private final MetaProperty<ScheduledExecutorService> _scheduler = DirectMetaProperty.ofReadWrite(
        this, "scheduler", MinimalFinancialUserManagerComponentFactory.class, ScheduledExecutorService.class);
    /**
     * The meta-property for the {@code clientTimeOut} property.
     */
    private final MetaProperty<Duration> _clientTimeOut = DirectMetaProperty.ofReadWrite(
        this, "clientTimeOut", MinimalFinancialUserManagerComponentFactory.class, Duration.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "classifier",
        "publishRest",
        "fudgeContext",
        "securityMaster",
        "positionMaster",
        "portfolioMaster",
        "snapshotMaster",
        "configMaster",
        "scheduler",
        "clientTimeOut");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case -281470431:  // classifier
          return _classifier;
        case -614707837:  // publishRest
          return _publishRest;
        case -917704420:  // fudgeContext
          return _fudgeContext;
        case -887218750:  // securityMaster
          return _securityMaster;
        case -1840419605:  // positionMaster
          return _positionMaster;
        case -772274742:  // portfolioMaster
          return _portfolioMaster;
        case -2046916282:  // snapshotMaster
          return _snapshotMaster;
        case 10395716:  // configMaster
          return _configMaster;
        case -160710469:  // scheduler
          return _scheduler;
        case -893669642:  // clientTimeOut
          return _clientTimeOut;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends MinimalFinancialUserManagerComponentFactory> builder() {
      return new DirectBeanBuilder<MinimalFinancialUserManagerComponentFactory>(new MinimalFinancialUserManagerComponentFactory());
    }

    @Override
    public Class<? extends MinimalFinancialUserManagerComponentFactory> beanType() {
      return MinimalFinancialUserManagerComponentFactory.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code classifier} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> classifier() {
      return _classifier;
    }

    /**
     * The meta-property for the {@code publishRest} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Boolean> publishRest() {
      return _publishRest;
    }

    /**
     * The meta-property for the {@code fudgeContext} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<FudgeContext> fudgeContext() {
      return _fudgeContext;
    }

    /**
     * The meta-property for the {@code securityMaster} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<SecurityMaster> securityMaster() {
      return _securityMaster;
    }

    /**
     * The meta-property for the {@code positionMaster} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<PositionMaster> positionMaster() {
      return _positionMaster;
    }

    /**
     * The meta-property for the {@code portfolioMaster} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<PortfolioMaster> portfolioMaster() {
      return _portfolioMaster;
    }

    /**
     * The meta-property for the {@code snapshotMaster} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<MarketDataSnapshotMaster> snapshotMaster() {
      return _snapshotMaster;
    }

    /**
     * The meta-property for the {@code configMaster} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ConfigMaster> configMaster() {
      return _configMaster;
    }

    /**
     * The meta-property for the {@code scheduler} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ScheduledExecutorService> scheduler() {
      return _scheduler;
    }

    /**
     * The meta-property for the {@code clientTimeOut} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Duration> clientTimeOut() {
      return _clientTimeOut;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -281470431:  // classifier
          return ((MinimalFinancialUserManagerComponentFactory) bean).getClassifier();
        case -614707837:  // publishRest
          return ((MinimalFinancialUserManagerComponentFactory) bean).isPublishRest();
        case -917704420:  // fudgeContext
          return ((MinimalFinancialUserManagerComponentFactory) bean).getFudgeContext();
        case -887218750:  // securityMaster
          return ((MinimalFinancialUserManagerComponentFactory) bean).getSecurityMaster();
        case -1840419605:  // positionMaster
          return ((MinimalFinancialUserManagerComponentFactory) bean).getPositionMaster();
        case -772274742:  // portfolioMaster
          return ((MinimalFinancialUserManagerComponentFactory) bean).getPortfolioMaster();
        case -2046916282:  // snapshotMaster
          return ((MinimalFinancialUserManagerComponentFactory) bean).getSnapshotMaster();
        case 10395716:  // configMaster
          return ((MinimalFinancialUserManagerComponentFactory) bean).getConfigMaster();
        case -160710469:  // scheduler
          return ((MinimalFinancialUserManagerComponentFactory) bean).getScheduler();
        case -893669642:  // clientTimeOut
          return ((MinimalFinancialUserManagerComponentFactory) bean).getClientTimeOut();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -281470431:  // classifier
          ((MinimalFinancialUserManagerComponentFactory) bean).setClassifier((String) newValue);
          return;
        case -614707837:  // publishRest
          ((MinimalFinancialUserManagerComponentFactory) bean).setPublishRest((Boolean) newValue);
          return;
        case -917704420:  // fudgeContext
          ((MinimalFinancialUserManagerComponentFactory) bean).setFudgeContext((FudgeContext) newValue);
          return;
        case -887218750:  // securityMaster
          ((MinimalFinancialUserManagerComponentFactory) bean).setSecurityMaster((SecurityMaster) newValue);
          return;
        case -1840419605:  // positionMaster
          ((MinimalFinancialUserManagerComponentFactory) bean).setPositionMaster((PositionMaster) newValue);
          return;
        case -772274742:  // portfolioMaster
          ((MinimalFinancialUserManagerComponentFactory) bean).setPortfolioMaster((PortfolioMaster) newValue);
          return;
        case -2046916282:  // snapshotMaster
          ((MinimalFinancialUserManagerComponentFactory) bean).setSnapshotMaster((MarketDataSnapshotMaster) newValue);
          return;
        case 10395716:  // configMaster
          ((MinimalFinancialUserManagerComponentFactory) bean).setConfigMaster((ConfigMaster) newValue);
          return;
        case -160710469:  // scheduler
          ((MinimalFinancialUserManagerComponentFactory) bean).setScheduler((ScheduledExecutorService) newValue);
          return;
        case -893669642:  // clientTimeOut
          ((MinimalFinancialUserManagerComponentFactory) bean).setClientTimeOut((Duration) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

    @Override
    protected void validate(Bean bean) {
      JodaBeanUtils.notNull(((MinimalFinancialUserManagerComponentFactory) bean)._classifier, "classifier");
      JodaBeanUtils.notNull(((MinimalFinancialUserManagerComponentFactory) bean)._fudgeContext, "fudgeContext");
      JodaBeanUtils.notNull(((MinimalFinancialUserManagerComponentFactory) bean)._securityMaster, "securityMaster");
      JodaBeanUtils.notNull(((MinimalFinancialUserManagerComponentFactory) bean)._positionMaster, "positionMaster");
      JodaBeanUtils.notNull(((MinimalFinancialUserManagerComponentFactory) bean)._portfolioMaster, "portfolioMaster");
      super.validate(bean);
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
