/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.security;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.joda.beans.Bean;
import org.joda.beans.BeanBuilder;
import org.joda.beans.BeanDefinition;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectBean;
import org.joda.beans.impl.direct.DirectBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaBean;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;

import com.opengamma.core.security.Security;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.MutableUniqueIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.PublicSPI;
import com.opengamma.util.auth.Permissionable;

/**
 * A security that it may be possible to hold a position in.
 * <p>
 * A security generically defined as anything that a position can be held in. This includes the security defined in "OTC" trades, permitting back-to-back trades
 * to be linked correctly.
 */
@PublicSPI
@BeanDefinition
public class ManageableSecurity extends DirectBean implements Serializable, Security, MutableUniqueIdentifiable, Permissionable {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  /**
   * The security type name.
   */
  private static final String SECURITY_TYPE = "MANAGEABLE";

  /**
   * The unique identifier of the security. This must be null when adding to a master and not null when retrieved from a master.
   */
  @PropertyDefinition(overrideGet = true, overrideSet = true)
  private UniqueId _uniqueId;
  /**
   * The bundle of external identifiers that define the security. Each external system will typically refer to a
   * security using a different identifier. Thus the bundle consists of a set of identifiers, one for each external system.
   */
  @PropertyDefinition(validate = "notNull", overrideGet = true)
  private ExternalIdBundle _externalIdBundle = ExternalIdBundle.EMPTY;
  /**
   * The name of the security intended for display purposes.
   */
  @PropertyDefinition(validate = "notNull", overrideGet = true)
  private String _name = "";
  /**
   * The security type.
   */
  @PropertyDefinition(validate = "notNull", set = "private", overrideGet = true)
  private String _securityType;
  /**
   * The general purpose trade attributes, which can be used for aggregating in portfolios.
   */
  @PropertyDefinition(overrideGet = true, overrideSet = true)
  private final Map<String, String> _attributes = new HashMap<>();
  /**
   * The set of required permissions. This is a set of permissions that a user needs to be able to view a security.
   */
  @PropertyDefinition(validate = "notNull", overrideGet = true)
  private final Set<String> _requiredPermissions = new TreeSet<>();

  /**
   * Creates an empty instance.
   * <p>
   * The security details should be set before use.
   */
  public ManageableSecurity() {
    _securityType = SECURITY_TYPE;
  }

  /**
   * Creates an instance with a security type.
   *
   * @param securityType
   *          the security type, not null
   */
  public ManageableSecurity(final String securityType) {
    ArgumentChecker.notEmpty(securityType, "securityType");
    _securityType = securityType;
  }

  /**
   * Creates a fully populated instance.
   *
   * @param uniqueId
   *          the security unique identifier, may be null
   * @param name
   *          the display name, not null
   * @param securityType
   *          the security type, not null
   * @param bundle
   *          the security external identifier bundle, not null
   */
  public ManageableSecurity(final UniqueId uniqueId, final String name, final String securityType, final ExternalIdBundle bundle) {
    this(securityType);
    setUniqueId(uniqueId);
    setName(name);
    setExternalIdBundle(bundle);
  }

  // -------------------------------------------------------------------------
  /**
   * Adds an external identifier to the bundle representing this security.
   *
   * @param externalId
   *          the identifier to add, not null
   */
  public void addExternalId(final ExternalId externalId) {
    setExternalIdBundle(getExternalIdBundle().withExternalId(externalId));
  }

  @Override
  public void addAttribute(final String key, final String value) {
    ArgumentChecker.notNull(key, "key");
    ArgumentChecker.notNull(value, "value");
    _attributes.put(key, value);
  }

  /**
   * @deprecated use {@link #getRequiredPermissions()}
   * @return the permissions
   */
  @Deprecated
  public Set<String> getPermissions() {
    return getRequiredPermissions();
  }

  /**
   * @deprecated use {@link #setRequiredPermissions}
   * @param permissions
   *          the permissions
   */
  @Deprecated
  public void setPermissions(final Set<String> permissions) {
    setRequiredPermissions(permissions);
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code ManageableSecurity}.
   * @return the meta-bean, not null
   */
  public static ManageableSecurity.Meta meta() {
    return ManageableSecurity.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(ManageableSecurity.Meta.INSTANCE);
  }

  @Override
  public ManageableSecurity.Meta metaBean() {
    return ManageableSecurity.Meta.INSTANCE;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the unique identifier of the security. This must be null when adding to a master and not null when retrieved from a master.
   * @return the value of the property
   */
  @Override
  public UniqueId getUniqueId() {
    return _uniqueId;
  }

  /**
   * Sets the unique identifier of the security. This must be null when adding to a master and not null when retrieved from a master.
   * @param uniqueId  the new value of the property
   */
  @Override
  public void setUniqueId(UniqueId uniqueId) {
    this._uniqueId = uniqueId;
  }

  /**
   * Gets the the {@code uniqueId} property.
   * @return the property, not null
   */
  public final Property<UniqueId> uniqueId() {
    return metaBean().uniqueId().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the bundle of external identifiers that define the security. Each external system will typically refer to a
   * security using a different identifier. Thus the bundle consists of a set of identifiers, one for each external system.
   * @return the value of the property, not null
   */
  @Override
  public ExternalIdBundle getExternalIdBundle() {
    return _externalIdBundle;
  }

  /**
   * Sets the bundle of external identifiers that define the security. Each external system will typically refer to a
   * security using a different identifier. Thus the bundle consists of a set of identifiers, one for each external system.
   * @param externalIdBundle  the new value of the property, not null
   */
  public void setExternalIdBundle(ExternalIdBundle externalIdBundle) {
    JodaBeanUtils.notNull(externalIdBundle, "externalIdBundle");
    this._externalIdBundle = externalIdBundle;
  }

  /**
   * Gets the the {@code externalIdBundle} property.
   * security using a different identifier. Thus the bundle consists of a set of identifiers, one for each external system.
   * @return the property, not null
   */
  public final Property<ExternalIdBundle> externalIdBundle() {
    return metaBean().externalIdBundle().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the name of the security intended for display purposes.
   * @return the value of the property, not null
   */
  @Override
  public String getName() {
    return _name;
  }

  /**
   * Sets the name of the security intended for display purposes.
   * @param name  the new value of the property, not null
   */
  public void setName(String name) {
    JodaBeanUtils.notNull(name, "name");
    this._name = name;
  }

  /**
   * Gets the the {@code name} property.
   * @return the property, not null
   */
  public final Property<String> name() {
    return metaBean().name().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the security type.
   * @return the value of the property, not null
   */
  @Override
  public String getSecurityType() {
    return _securityType;
  }

  /**
   * Sets the security type.
   * @param securityType  the new value of the property, not null
   */
  private void setSecurityType(String securityType) {
    JodaBeanUtils.notNull(securityType, "securityType");
    this._securityType = securityType;
  }

  /**
   * Gets the the {@code securityType} property.
   * @return the property, not null
   */
  public final Property<String> securityType() {
    return metaBean().securityType().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the general purpose trade attributes, which can be used for aggregating in portfolios.
   * @return the value of the property, not null
   */
  @Override
  public Map<String, String> getAttributes() {
    return _attributes;
  }

  /**
   * Sets the general purpose trade attributes, which can be used for aggregating in portfolios.
   * @param attributes  the new value of the property, not null
   */
  @Override
  public void setAttributes(Map<String, String> attributes) {
    JodaBeanUtils.notNull(attributes, "attributes");
    this._attributes.clear();
    this._attributes.putAll(attributes);
  }

  /**
   * Gets the the {@code attributes} property.
   * @return the property, not null
   */
  public final Property<Map<String, String>> attributes() {
    return metaBean().attributes().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the set of required permissions. This is a set of permissions that a user needs to be able to view a security.
   * @return the value of the property, not null
   */
  @Override
  public Set<String> getRequiredPermissions() {
    return _requiredPermissions;
  }

  /**
   * Sets the set of required permissions. This is a set of permissions that a user needs to be able to view a security.
   * @param requiredPermissions  the new value of the property, not null
   */
  public void setRequiredPermissions(Set<String> requiredPermissions) {
    JodaBeanUtils.notNull(requiredPermissions, "requiredPermissions");
    this._requiredPermissions.clear();
    this._requiredPermissions.addAll(requiredPermissions);
  }

  /**
   * Gets the the {@code requiredPermissions} property.
   * @return the property, not null
   */
  public final Property<Set<String>> requiredPermissions() {
    return metaBean().requiredPermissions().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public ManageableSecurity clone() {
    return JodaBeanUtils.cloneAlways(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      ManageableSecurity other = (ManageableSecurity) obj;
      return JodaBeanUtils.equal(getUniqueId(), other.getUniqueId()) &&
          JodaBeanUtils.equal(getExternalIdBundle(), other.getExternalIdBundle()) &&
          JodaBeanUtils.equal(getName(), other.getName()) &&
          JodaBeanUtils.equal(getSecurityType(), other.getSecurityType()) &&
          JodaBeanUtils.equal(getAttributes(), other.getAttributes()) &&
          JodaBeanUtils.equal(getRequiredPermissions(), other.getRequiredPermissions());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash = hash * 31 + JodaBeanUtils.hashCode(getUniqueId());
    hash = hash * 31 + JodaBeanUtils.hashCode(getExternalIdBundle());
    hash = hash * 31 + JodaBeanUtils.hashCode(getName());
    hash = hash * 31 + JodaBeanUtils.hashCode(getSecurityType());
    hash = hash * 31 + JodaBeanUtils.hashCode(getAttributes());
    hash = hash * 31 + JodaBeanUtils.hashCode(getRequiredPermissions());
    return hash;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(224);
    buf.append("ManageableSecurity{");
    int len = buf.length();
    toString(buf);
    if (buf.length() > len) {
      buf.setLength(buf.length() - 2);
    }
    buf.append('}');
    return buf.toString();
  }

  protected void toString(StringBuilder buf) {
    buf.append("uniqueId").append('=').append(JodaBeanUtils.toString(getUniqueId())).append(',').append(' ');
    buf.append("externalIdBundle").append('=').append(JodaBeanUtils.toString(getExternalIdBundle())).append(',').append(' ');
    buf.append("name").append('=').append(JodaBeanUtils.toString(getName())).append(',').append(' ');
    buf.append("securityType").append('=').append(JodaBeanUtils.toString(getSecurityType())).append(',').append(' ');
    buf.append("attributes").append('=').append(JodaBeanUtils.toString(getAttributes())).append(',').append(' ');
    buf.append("requiredPermissions").append('=').append(JodaBeanUtils.toString(getRequiredPermissions())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code ManageableSecurity}.
   */
  public static class Meta extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code uniqueId} property.
     */
    private final MetaProperty<UniqueId> _uniqueId = DirectMetaProperty.ofReadWrite(
        this, "uniqueId", ManageableSecurity.class, UniqueId.class);
    /**
     * The meta-property for the {@code externalIdBundle} property.
     */
    private final MetaProperty<ExternalIdBundle> _externalIdBundle = DirectMetaProperty.ofReadWrite(
        this, "externalIdBundle", ManageableSecurity.class, ExternalIdBundle.class);
    /**
     * The meta-property for the {@code name} property.
     */
    private final MetaProperty<String> _name = DirectMetaProperty.ofReadWrite(
        this, "name", ManageableSecurity.class, String.class);
    /**
     * The meta-property for the {@code securityType} property.
     */
    private final MetaProperty<String> _securityType = DirectMetaProperty.ofReadWrite(
        this, "securityType", ManageableSecurity.class, String.class);
    /**
     * The meta-property for the {@code attributes} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<Map<String, String>> _attributes = DirectMetaProperty.ofReadWrite(
        this, "attributes", ManageableSecurity.class, (Class) Map.class);
    /**
     * The meta-property for the {@code requiredPermissions} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<Set<String>> _requiredPermissions = DirectMetaProperty.ofReadWrite(
        this, "requiredPermissions", ManageableSecurity.class, (Class) Set.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "uniqueId",
        "externalIdBundle",
        "name",
        "securityType",
        "attributes",
        "requiredPermissions");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case -294460212:  // uniqueId
          return _uniqueId;
        case -736922008:  // externalIdBundle
          return _externalIdBundle;
        case 3373707:  // name
          return _name;
        case 808245914:  // securityType
          return _securityType;
        case 405645655:  // attributes
          return _attributes;
        case 132663141:  // requiredPermissions
          return _requiredPermissions;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends ManageableSecurity> builder() {
      return new DirectBeanBuilder<ManageableSecurity>(new ManageableSecurity());
    }

    @Override
    public Class<? extends ManageableSecurity> beanType() {
      return ManageableSecurity.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code uniqueId} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<UniqueId> uniqueId() {
      return _uniqueId;
    }

    /**
     * The meta-property for the {@code externalIdBundle} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ExternalIdBundle> externalIdBundle() {
      return _externalIdBundle;
    }

    /**
     * The meta-property for the {@code name} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> name() {
      return _name;
    }

    /**
     * The meta-property for the {@code securityType} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> securityType() {
      return _securityType;
    }

    /**
     * The meta-property for the {@code attributes} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Map<String, String>> attributes() {
      return _attributes;
    }

    /**
     * The meta-property for the {@code requiredPermissions} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Set<String>> requiredPermissions() {
      return _requiredPermissions;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -294460212:  // uniqueId
          return ((ManageableSecurity) bean).getUniqueId();
        case -736922008:  // externalIdBundle
          return ((ManageableSecurity) bean).getExternalIdBundle();
        case 3373707:  // name
          return ((ManageableSecurity) bean).getName();
        case 808245914:  // securityType
          return ((ManageableSecurity) bean).getSecurityType();
        case 405645655:  // attributes
          return ((ManageableSecurity) bean).getAttributes();
        case 132663141:  // requiredPermissions
          return ((ManageableSecurity) bean).getRequiredPermissions();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -294460212:  // uniqueId
          ((ManageableSecurity) bean).setUniqueId((UniqueId) newValue);
          return;
        case -736922008:  // externalIdBundle
          ((ManageableSecurity) bean).setExternalIdBundle((ExternalIdBundle) newValue);
          return;
        case 3373707:  // name
          ((ManageableSecurity) bean).setName((String) newValue);
          return;
        case 808245914:  // securityType
          ((ManageableSecurity) bean).setSecurityType((String) newValue);
          return;
        case 405645655:  // attributes
          ((ManageableSecurity) bean).setAttributes((Map<String, String>) newValue);
          return;
        case 132663141:  // requiredPermissions
          ((ManageableSecurity) bean).setRequiredPermissions((Set<String>) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

    @Override
    protected void validate(Bean bean) {
      JodaBeanUtils.notNull(((ManageableSecurity) bean)._externalIdBundle, "externalIdBundle");
      JodaBeanUtils.notNull(((ManageableSecurity) bean)._name, "name");
      JodaBeanUtils.notNull(((ManageableSecurity) bean)._securityType, "securityType");
      JodaBeanUtils.notNull(((ManageableSecurity) bean)._attributes, "attributes");
      JodaBeanUtils.notNull(((ManageableSecurity) bean)._requiredPermissions, "requiredPermissions");
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------

}
