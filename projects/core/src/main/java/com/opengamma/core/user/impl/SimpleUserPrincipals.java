/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.user.impl;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import org.joda.beans.Bean;
import org.joda.beans.BeanBuilder;
import org.joda.beans.BeanDefinition;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaBean;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;

import com.opengamma.core.user.UserAccount;
import com.opengamma.core.user.UserPrincipals;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.ArgumentChecker;

/**
 * Simple implementation of {@code UserPrincipals}.
 * <p>
 * This is the simplest possible implementation of the {@link UserPrincipals} interface.
 * <p>
 * This class is mutable and not thread-safe.
 * It is intended to primarily be used via the read-only {@code UserPrincipals} interface.
 */
@BeanDefinition
public class SimpleUserPrincipals implements Bean, UserPrincipals, Serializable {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  /**
   * The user name that uniquely identifies the user.
   * This is the primary identifier of a user.
   */
  @PropertyDefinition(validate = "notNull", overrideGet = true)
  private String _userName;
  /**
   * The bundle of alternate user identifiers.
   * <p>
   * This allows the user identifiers of external systems to be associated with the account
   * Some of these may be unique within the external system, others may be more descriptive.
   */
  @PropertyDefinition(validate = "notNull", overrideGet = true)
  private ExternalIdBundle _alternateIds = ExternalIdBundle.EMPTY;
  /**
   * The network address of the user, which is intended to be an IP address.
   * <p>
   * Unfortunately it is not possible to guarantee the presence of accuracy of the IP address,
   * notably as a result of web browser and network proxy restrictions.
   */
  @PropertyDefinition(overrideGet = true)
  private String _networkAddress;
  /**
   * The primary email address associated with the account.
   */
  @PropertyDefinition(overrideGet = true)
  private String _emailAddress;

  //-------------------------------------------------------------------------
  /**
   * Creates a {@code SimpleUserPrincipals} from another instance.
   *
   * @param principalsToCopy  the principals to copy, not null
   * @return the new principals, not null
   */
  public static SimpleUserPrincipals from(final UserPrincipals principalsToCopy) {
    ArgumentChecker.notNull(principalsToCopy, "profileToCopy");
    final SimpleUserPrincipals copy = new SimpleUserPrincipals();
    copy.setUserName(principalsToCopy.getUserName());
    copy.setAlternateIds(principalsToCopy.getAlternateIds());
    copy.setNetworkAddress(principalsToCopy.getNetworkAddress());
    copy.setEmailAddress(principalsToCopy.getEmailAddress());
    return copy;
  }

  /**
   * Creates a {@code SimpleUserPrincipals} from an account.
   * <p>
   * The network address will be null.
   *
   * @param account  the account to copy, not null
   * @return the new principals, not null
   */
  public static SimpleUserPrincipals from(final UserAccount account) {
    ArgumentChecker.notNull(account, "account");
    final SimpleUserPrincipals principals = new SimpleUserPrincipals();
    principals.setUserName(account.getUserName());
    principals.setAlternateIds(account.getAlternateIds());
    principals.setEmailAddress(account.getEmailAddress());
    return principals;
  }

  //-------------------------------------------------------------------------
  /**
   * Creates a user principals.
   */
  public SimpleUserPrincipals() {
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code SimpleUserPrincipals}.
   * @return the meta-bean, not null
   */
  public static SimpleUserPrincipals.Meta meta() {
    return SimpleUserPrincipals.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(SimpleUserPrincipals.Meta.INSTANCE);
  }

  @Override
  public SimpleUserPrincipals.Meta metaBean() {
    return SimpleUserPrincipals.Meta.INSTANCE;
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
   * Gets the user name that uniquely identifies the user.
   * This is the primary identifier of a user.
   * @return the value of the property, not null
   */
  @Override
  public String getUserName() {
    return _userName;
  }

  /**
   * Sets the user name that uniquely identifies the user.
   * This is the primary identifier of a user.
   * @param userName  the new value of the property, not null
   */
  public void setUserName(String userName) {
    JodaBeanUtils.notNull(userName, "userName");
    this._userName = userName;
  }

  /**
   * Gets the the {@code userName} property.
   * This is the primary identifier of a user.
   * @return the property, not null
   */
  public final Property<String> userName() {
    return metaBean().userName().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the bundle of alternate user identifiers.
   * <p>
   * This allows the user identifiers of external systems to be associated with the account
   * Some of these may be unique within the external system, others may be more descriptive.
   * @return the value of the property, not null
   */
  @Override
  public ExternalIdBundle getAlternateIds() {
    return _alternateIds;
  }

  /**
   * Sets the bundle of alternate user identifiers.
   * <p>
   * This allows the user identifiers of external systems to be associated with the account
   * Some of these may be unique within the external system, others may be more descriptive.
   * @param alternateIds  the new value of the property, not null
   */
  public void setAlternateIds(ExternalIdBundle alternateIds) {
    JodaBeanUtils.notNull(alternateIds, "alternateIds");
    this._alternateIds = alternateIds;
  }

  /**
   * Gets the the {@code alternateIds} property.
   * <p>
   * This allows the user identifiers of external systems to be associated with the account
   * Some of these may be unique within the external system, others may be more descriptive.
   * @return the property, not null
   */
  public final Property<ExternalIdBundle> alternateIds() {
    return metaBean().alternateIds().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the network address of the user, which is intended to be an IP address.
   * <p>
   * Unfortunately it is not possible to guarantee the presence of accuracy of the IP address,
   * notably as a result of web browser and network proxy restrictions.
   * @return the value of the property
   */
  @Override
  public String getNetworkAddress() {
    return _networkAddress;
  }

  /**
   * Sets the network address of the user, which is intended to be an IP address.
   * <p>
   * Unfortunately it is not possible to guarantee the presence of accuracy of the IP address,
   * notably as a result of web browser and network proxy restrictions.
   * @param networkAddress  the new value of the property
   */
  public void setNetworkAddress(String networkAddress) {
    this._networkAddress = networkAddress;
  }

  /**
   * Gets the the {@code networkAddress} property.
   * <p>
   * Unfortunately it is not possible to guarantee the presence of accuracy of the IP address,
   * notably as a result of web browser and network proxy restrictions.
   * @return the property, not null
   */
  public final Property<String> networkAddress() {
    return metaBean().networkAddress().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the primary email address associated with the account.
   * @return the value of the property
   */
  @Override
  public String getEmailAddress() {
    return _emailAddress;
  }

  /**
   * Sets the primary email address associated with the account.
   * @param emailAddress  the new value of the property
   */
  public void setEmailAddress(String emailAddress) {
    this._emailAddress = emailAddress;
  }

  /**
   * Gets the the {@code emailAddress} property.
   * @return the property, not null
   */
  public final Property<String> emailAddress() {
    return metaBean().emailAddress().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public SimpleUserPrincipals clone() {
    return JodaBeanUtils.cloneAlways(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      SimpleUserPrincipals other = (SimpleUserPrincipals) obj;
      return JodaBeanUtils.equal(getUserName(), other.getUserName()) &&
          JodaBeanUtils.equal(getAlternateIds(), other.getAlternateIds()) &&
          JodaBeanUtils.equal(getNetworkAddress(), other.getNetworkAddress()) &&
          JodaBeanUtils.equal(getEmailAddress(), other.getEmailAddress());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash = hash * 31 + JodaBeanUtils.hashCode(getUserName());
    hash = hash * 31 + JodaBeanUtils.hashCode(getAlternateIds());
    hash = hash * 31 + JodaBeanUtils.hashCode(getNetworkAddress());
    hash = hash * 31 + JodaBeanUtils.hashCode(getEmailAddress());
    return hash;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(160);
    buf.append("SimpleUserPrincipals{");
    int len = buf.length();
    toString(buf);
    if (buf.length() > len) {
      buf.setLength(buf.length() - 2);
    }
    buf.append('}');
    return buf.toString();
  }

  protected void toString(StringBuilder buf) {
    buf.append("userName").append('=').append(JodaBeanUtils.toString(getUserName())).append(',').append(' ');
    buf.append("alternateIds").append('=').append(JodaBeanUtils.toString(getAlternateIds())).append(',').append(' ');
    buf.append("networkAddress").append('=').append(JodaBeanUtils.toString(getNetworkAddress())).append(',').append(' ');
    buf.append("emailAddress").append('=').append(JodaBeanUtils.toString(getEmailAddress())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code SimpleUserPrincipals}.
   */
  public static class Meta extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code userName} property.
     */
    private final MetaProperty<String> _userName = DirectMetaProperty.ofReadWrite(
        this, "userName", SimpleUserPrincipals.class, String.class);
    /**
     * The meta-property for the {@code alternateIds} property.
     */
    private final MetaProperty<ExternalIdBundle> _alternateIds = DirectMetaProperty.ofReadWrite(
        this, "alternateIds", SimpleUserPrincipals.class, ExternalIdBundle.class);
    /**
     * The meta-property for the {@code networkAddress} property.
     */
    private final MetaProperty<String> _networkAddress = DirectMetaProperty.ofReadWrite(
        this, "networkAddress", SimpleUserPrincipals.class, String.class);
    /**
     * The meta-property for the {@code emailAddress} property.
     */
    private final MetaProperty<String> _emailAddress = DirectMetaProperty.ofReadWrite(
        this, "emailAddress", SimpleUserPrincipals.class, String.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "userName",
        "alternateIds",
        "networkAddress",
        "emailAddress");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case -266666762:  // userName
          return _userName;
        case -1805823010:  // alternateIds
          return _alternateIds;
        case 1443604966:  // networkAddress
          return _networkAddress;
        case -1070931784:  // emailAddress
          return _emailAddress;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends SimpleUserPrincipals> builder() {
      return new DirectBeanBuilder<SimpleUserPrincipals>(new SimpleUserPrincipals());
    }

    @Override
    public Class<? extends SimpleUserPrincipals> beanType() {
      return SimpleUserPrincipals.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code userName} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> userName() {
      return _userName;
    }

    /**
     * The meta-property for the {@code alternateIds} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ExternalIdBundle> alternateIds() {
      return _alternateIds;
    }

    /**
     * The meta-property for the {@code networkAddress} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> networkAddress() {
      return _networkAddress;
    }

    /**
     * The meta-property for the {@code emailAddress} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> emailAddress() {
      return _emailAddress;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -266666762:  // userName
          return ((SimpleUserPrincipals) bean).getUserName();
        case -1805823010:  // alternateIds
          return ((SimpleUserPrincipals) bean).getAlternateIds();
        case 1443604966:  // networkAddress
          return ((SimpleUserPrincipals) bean).getNetworkAddress();
        case -1070931784:  // emailAddress
          return ((SimpleUserPrincipals) bean).getEmailAddress();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -266666762:  // userName
          ((SimpleUserPrincipals) bean).setUserName((String) newValue);
          return;
        case -1805823010:  // alternateIds
          ((SimpleUserPrincipals) bean).setAlternateIds((ExternalIdBundle) newValue);
          return;
        case 1443604966:  // networkAddress
          ((SimpleUserPrincipals) bean).setNetworkAddress((String) newValue);
          return;
        case -1070931784:  // emailAddress
          ((SimpleUserPrincipals) bean).setEmailAddress((String) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

    @Override
    protected void validate(Bean bean) {
      JodaBeanUtils.notNull(((SimpleUserPrincipals) bean)._userName, "userName");
      JodaBeanUtils.notNull(((SimpleUserPrincipals) bean)._alternateIds, "alternateIds");
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
