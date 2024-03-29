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
import com.opengamma.core.user.UserSource;
import com.opengamma.core.user.impl.DataUserSourceResource;
import com.opengamma.core.user.impl.RemoteUserSource;
import com.opengamma.master.user.UserMaster;

/**
 * Component factory providing the {@code UserSource}.
 */
@BeanDefinition
public class UserSourceComponentFactory extends AbstractComponentFactory {

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
   * The underlying user master.
   */
  @PropertyDefinition(validate = "notNull")
  private UserMaster _userMaster;

  //-------------------------------------------------------------------------
  /**
   * Initializes the user source, setting up component information and REST.
   * Override using {@link #createUserSource(ComponentRepository)}.
   *
   * @param repo  the component repository, not null
   * @param configuration  the remaining configuration, not null
   */
  @Override
  public void init(final ComponentRepository repo, final LinkedHashMap<String, String> configuration) {
    final UserSource source = createUserSource(repo);

    final ComponentInfo info = new ComponentInfo(UserSource.class, getClassifier());
    info.addAttribute(ComponentInfoAttributes.LEVEL, 1);
    if (isPublishRest()) {
      info.addAttribute(ComponentInfoAttributes.REMOTE_CLIENT_JAVA, RemoteUserSource.class);
    }
    repo.registerComponent(info, source);
    if (isPublishRest()) {
      repo.getRestComponents().publish(info, new DataUserSourceResource(source));
    }
  }

  /**
   * Creates the user source without registering it.
   *
   * @param repo  the component repository, only used to register secondary items like lifecycle, not null
   * @return the user source, not null
   */
  protected UserSource createUserSource(final ComponentRepository repo) {
    return getUserMaster();
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code UserSourceComponentFactory}.
   * @return the meta-bean, not null
   */
  public static UserSourceComponentFactory.Meta meta() {
    return UserSourceComponentFactory.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(UserSourceComponentFactory.Meta.INSTANCE);
  }

  @Override
  public UserSourceComponentFactory.Meta metaBean() {
    return UserSourceComponentFactory.Meta.INSTANCE;
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
   * Gets the underlying user master.
   * @return the value of the property, not null
   */
  public UserMaster getUserMaster() {
    return _userMaster;
  }

  /**
   * Sets the underlying user master.
   * @param userMaster  the new value of the property, not null
   */
  public void setUserMaster(UserMaster userMaster) {
    JodaBeanUtils.notNull(userMaster, "userMaster");
    this._userMaster = userMaster;
  }

  /**
   * Gets the the {@code userMaster} property.
   * @return the property, not null
   */
  public final Property<UserMaster> userMaster() {
    return metaBean().userMaster().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public UserSourceComponentFactory clone() {
    return JodaBeanUtils.cloneAlways(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      UserSourceComponentFactory other = (UserSourceComponentFactory) obj;
      return JodaBeanUtils.equal(getClassifier(), other.getClassifier()) &&
          (isPublishRest() == other.isPublishRest()) &&
          JodaBeanUtils.equal(getUserMaster(), other.getUserMaster()) &&
          super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = hash * 31 + JodaBeanUtils.hashCode(getClassifier());
    hash = hash * 31 + JodaBeanUtils.hashCode(isPublishRest());
    hash = hash * 31 + JodaBeanUtils.hashCode(getUserMaster());
    return hash ^ super.hashCode();
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(128);
    buf.append("UserSourceComponentFactory{");
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
    buf.append("userMaster").append('=').append(JodaBeanUtils.toString(getUserMaster())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code UserSourceComponentFactory}.
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
        this, "classifier", UserSourceComponentFactory.class, String.class);
    /**
     * The meta-property for the {@code publishRest} property.
     */
    private final MetaProperty<Boolean> _publishRest = DirectMetaProperty.ofReadWrite(
        this, "publishRest", UserSourceComponentFactory.class, Boolean.TYPE);
    /**
     * The meta-property for the {@code userMaster} property.
     */
    private final MetaProperty<UserMaster> _userMaster = DirectMetaProperty.ofReadWrite(
        this, "userMaster", UserSourceComponentFactory.class, UserMaster.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "classifier",
        "publishRest",
        "userMaster");

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
        case 1402846733:  // userMaster
          return _userMaster;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends UserSourceComponentFactory> builder() {
      return new DirectBeanBuilder<UserSourceComponentFactory>(new UserSourceComponentFactory());
    }

    @Override
    public Class<? extends UserSourceComponentFactory> beanType() {
      return UserSourceComponentFactory.class;
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
     * The meta-property for the {@code userMaster} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<UserMaster> userMaster() {
      return _userMaster;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -281470431:  // classifier
          return ((UserSourceComponentFactory) bean).getClassifier();
        case -614707837:  // publishRest
          return ((UserSourceComponentFactory) bean).isPublishRest();
        case 1402846733:  // userMaster
          return ((UserSourceComponentFactory) bean).getUserMaster();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -281470431:  // classifier
          ((UserSourceComponentFactory) bean).setClassifier((String) newValue);
          return;
        case -614707837:  // publishRest
          ((UserSourceComponentFactory) bean).setPublishRest((Boolean) newValue);
          return;
        case 1402846733:  // userMaster
          ((UserSourceComponentFactory) bean).setUserMaster((UserMaster) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

    @Override
    protected void validate(Bean bean) {
      JodaBeanUtils.notNull(((UserSourceComponentFactory) bean)._classifier, "classifier");
      JodaBeanUtils.notNull(((UserSourceComponentFactory) bean)._userMaster, "userMaster");
      super.validate(bean);
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
