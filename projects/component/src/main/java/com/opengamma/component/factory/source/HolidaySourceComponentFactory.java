/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.component.factory.source;

import java.util.LinkedHashMap;
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

import com.opengamma.component.ComponentInfo;
import com.opengamma.component.ComponentRepository;
import com.opengamma.component.factory.AbstractComponentFactory;
import com.opengamma.component.factory.ComponentInfoAttributes;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.holiday.impl.CachedHolidaySource;
import com.opengamma.core.holiday.impl.DataHolidaySourceResource;
import com.opengamma.core.holiday.impl.RemoteHolidaySource;
import com.opengamma.master.holiday.HolidayMaster;
import com.opengamma.master.holiday.impl.MasterHolidaySource;

/**
 * Component factory providing the {@code HolidaySource}.
 * <p>
 * This implementation uses the dedicated {@link CachedHolidaySource}.
 */
@BeanDefinition
public class HolidaySourceComponentFactory extends AbstractComponentFactory {

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
   * The underlying holiday master.
   */
  @PropertyDefinition(validate = "notNull")
  private HolidayMaster _holidayMaster;
  /**
   * Whether all holiday calendars should be cached.
   */
  @PropertyDefinition
  private boolean _cacheHolidays = true;

  //-------------------------------------------------------------------------
  /**
   * Initializes the holiday source, setting up component information and REST.
   * Override using {@link #createHolidaySource(ComponentRepository)}.
   *
   * @param repo  the component repository, not null
   * @param configuration  the remaining configuration, not null
   */
  @Override
  public void init(final ComponentRepository repo, final LinkedHashMap<String, String> configuration) {
    final HolidaySource source = createHolidaySource(repo);

    final ComponentInfo info = new ComponentInfo(HolidaySource.class, getClassifier());
    info.addAttribute(ComponentInfoAttributes.LEVEL, 1);
    if (isPublishRest()) {
      info.addAttribute(ComponentInfoAttributes.REMOTE_CLIENT_JAVA, RemoteHolidaySource.class);
    }
    repo.registerComponent(info, source);
    if (isPublishRest()) {
      repo.getRestComponents().publish(info, new DataHolidaySourceResource(source));
    }
  }

  /**
   * Creates the holiday source without registering it.
   *
   * @param repo  the component repository, only used to register secondary items like lifecycle, not null
   * @return the holiday source, not null
   */
  protected HolidaySource createHolidaySource(final ComponentRepository repo) {
    return new MasterHolidaySource(getHolidayMaster(), isCacheHolidays());
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code HolidaySourceComponentFactory}.
   * @return the meta-bean, not null
   */
  public static HolidaySourceComponentFactory.Meta meta() {
    return HolidaySourceComponentFactory.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(HolidaySourceComponentFactory.Meta.INSTANCE);
  }

  @Override
  public HolidaySourceComponentFactory.Meta metaBean() {
    return HolidaySourceComponentFactory.Meta.INSTANCE;
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
   * Gets the underlying holiday master.
   * @return the value of the property, not null
   */
  public HolidayMaster getHolidayMaster() {
    return _holidayMaster;
  }

  /**
   * Sets the underlying holiday master.
   * @param holidayMaster  the new value of the property, not null
   */
  public void setHolidayMaster(HolidayMaster holidayMaster) {
    JodaBeanUtils.notNull(holidayMaster, "holidayMaster");
    this._holidayMaster = holidayMaster;
  }

  /**
   * Gets the the {@code holidayMaster} property.
   * @return the property, not null
   */
  public final Property<HolidayMaster> holidayMaster() {
    return metaBean().holidayMaster().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets whether all holiday calendars should be cached.
   * @return the value of the property
   */
  public boolean isCacheHolidays() {
    return _cacheHolidays;
  }

  /**
   * Sets whether all holiday calendars should be cached.
   * @param cacheHolidays  the new value of the property
   */
  public void setCacheHolidays(boolean cacheHolidays) {
    this._cacheHolidays = cacheHolidays;
  }

  /**
   * Gets the the {@code cacheHolidays} property.
   * @return the property, not null
   */
  public final Property<Boolean> cacheHolidays() {
    return metaBean().cacheHolidays().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public HolidaySourceComponentFactory clone() {
    return JodaBeanUtils.cloneAlways(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      HolidaySourceComponentFactory other = (HolidaySourceComponentFactory) obj;
      return JodaBeanUtils.equal(getClassifier(), other.getClassifier()) &&
          (isPublishRest() == other.isPublishRest()) &&
          JodaBeanUtils.equal(getHolidayMaster(), other.getHolidayMaster()) &&
          (isCacheHolidays() == other.isCacheHolidays()) &&
          super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = hash * 31 + JodaBeanUtils.hashCode(getClassifier());
    hash = hash * 31 + JodaBeanUtils.hashCode(isPublishRest());
    hash = hash * 31 + JodaBeanUtils.hashCode(getHolidayMaster());
    hash = hash * 31 + JodaBeanUtils.hashCode(isCacheHolidays());
    return hash ^ super.hashCode();
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(160);
    buf.append("HolidaySourceComponentFactory{");
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
    buf.append("holidayMaster").append('=').append(JodaBeanUtils.toString(getHolidayMaster())).append(',').append(' ');
    buf.append("cacheHolidays").append('=').append(JodaBeanUtils.toString(isCacheHolidays())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code HolidaySourceComponentFactory}.
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
        this, "classifier", HolidaySourceComponentFactory.class, String.class);
    /**
     * The meta-property for the {@code publishRest} property.
     */
    private final MetaProperty<Boolean> _publishRest = DirectMetaProperty.ofReadWrite(
        this, "publishRest", HolidaySourceComponentFactory.class, Boolean.TYPE);
    /**
     * The meta-property for the {@code holidayMaster} property.
     */
    private final MetaProperty<HolidayMaster> _holidayMaster = DirectMetaProperty.ofReadWrite(
        this, "holidayMaster", HolidaySourceComponentFactory.class, HolidayMaster.class);
    /**
     * The meta-property for the {@code cacheHolidays} property.
     */
    private final MetaProperty<Boolean> _cacheHolidays = DirectMetaProperty.ofReadWrite(
        this, "cacheHolidays", HolidaySourceComponentFactory.class, Boolean.TYPE);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "classifier",
        "publishRest",
        "holidayMaster",
        "cacheHolidays");

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
        case 246258906:  // holidayMaster
          return _holidayMaster;
        case 1571574973:  // cacheHolidays
          return _cacheHolidays;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends HolidaySourceComponentFactory> builder() {
      return new DirectBeanBuilder<HolidaySourceComponentFactory>(new HolidaySourceComponentFactory());
    }

    @Override
    public Class<? extends HolidaySourceComponentFactory> beanType() {
      return HolidaySourceComponentFactory.class;
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
     * The meta-property for the {@code holidayMaster} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<HolidayMaster> holidayMaster() {
      return _holidayMaster;
    }

    /**
     * The meta-property for the {@code cacheHolidays} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Boolean> cacheHolidays() {
      return _cacheHolidays;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -281470431:  // classifier
          return ((HolidaySourceComponentFactory) bean).getClassifier();
        case -614707837:  // publishRest
          return ((HolidaySourceComponentFactory) bean).isPublishRest();
        case 246258906:  // holidayMaster
          return ((HolidaySourceComponentFactory) bean).getHolidayMaster();
        case 1571574973:  // cacheHolidays
          return ((HolidaySourceComponentFactory) bean).isCacheHolidays();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -281470431:  // classifier
          ((HolidaySourceComponentFactory) bean).setClassifier((String) newValue);
          return;
        case -614707837:  // publishRest
          ((HolidaySourceComponentFactory) bean).setPublishRest((Boolean) newValue);
          return;
        case 246258906:  // holidayMaster
          ((HolidaySourceComponentFactory) bean).setHolidayMaster((HolidayMaster) newValue);
          return;
        case 1571574973:  // cacheHolidays
          ((HolidaySourceComponentFactory) bean).setCacheHolidays((Boolean) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

    @Override
    protected void validate(Bean bean) {
      JodaBeanUtils.notNull(((HolidaySourceComponentFactory) bean)._classifier, "classifier");
      JodaBeanUtils.notNull(((HolidaySourceComponentFactory) bean)._holidayMaster, "holidayMaster");
      super.validate(bean);
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
