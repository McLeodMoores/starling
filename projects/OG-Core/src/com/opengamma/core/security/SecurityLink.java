/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.security;

import java.util.Map;

import org.joda.beans.BeanBuilder;
import org.joda.beans.BeanDefinition;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.impl.direct.DirectBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;

import com.opengamma.core.Link;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.ObjectIdentifier;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.PublicSPI;

/**
 * A flexible link between an object and a security.
 * <p>
 * The security link represents a connection from an entity to a security.
 * The connection can be held by {@code UniqueIdentifier}, {@code IdentifierBundle}
 * or by a resolved reference to the security itself.
 * <p>
 * This class is mutable and not thread-safe.
 */
@PublicSPI
@BeanDefinition
public class SecurityLink extends Link<Security> {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  /**
   * Creates an new instance.
   */
  public SecurityLink() {
    super();
  }

  /**
   * Creates a link from an object identifier.
   * 
   * @param objectId  the object identifier, not null
   */
  public SecurityLink(final ObjectIdentifier objectId) {
    super(objectId);
  }

  /**
   * Creates a link from a unique identifier, only storing the object identifier.
   * 
   * @param uniqueId  the unique identifier, not null
   */
  public SecurityLink(final UniqueIdentifier uniqueId) {
    super(uniqueId);
  }

  /**
   * Creates a link from an identifier.
   * 
   * @param identifier  the identifier, not null
   */
  public SecurityLink(final Identifier identifier) {
    super(IdentifierBundle.of(identifier));
  }

  /**
   * Creates a link from an identifier bundle.
   * 
   * @param bundle  the identifier bundle, not null
   */
  public SecurityLink(final IdentifierBundle bundle) {
    super(bundle);
  }

  /**
   * Creates a link from a security.
   * 
   * @param target  the resolved security, not null
   */
  public SecurityLink(final Security target) {
    super(target);
    setIdBundle(target.getIdentifiers());
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the best descriptive name.
   * 
   * @return the best descriptive name, not null
   */
  public String getBestName() {
    Security security = getTarget();
    ObjectIdentifier objectId = getObjectId();
    IdentifierBundle bundle = getIdBundle();
    if (security != null) {
      bundle = security.getIdentifiers();
    }
    if (bundle != null && bundle.size() > 0) {
      if (bundle.getIdentifierValue(SecurityUtils.BLOOMBERG_TICKER) != null) {
        return bundle.getIdentifierValue(SecurityUtils.BLOOMBERG_TICKER);
      } else if (bundle.getIdentifierValue(SecurityUtils.RIC) != null) {
        return bundle.getIdentifierValue(SecurityUtils.RIC);
      } else if (bundle.getIdentifierValue(SecurityUtils.ACTIVFEED_TICKER) != null) {
        return bundle.getIdentifierValue(SecurityUtils.ACTIVFEED_TICKER);
      } else {
        return bundle.getIdentifiers().iterator().next().getValue();
      }
    }
    if (objectId != null) {
      return objectId.toString();
    }
    return "";
  }

  /**
   * Clones this link, sharing the target security.
   * 
   * @return the clone, not null
   */
  @Override
  public SecurityLink clone() {
    return (SecurityLink) super.clone();
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code SecurityLink}.
   * @return the meta-bean, not null
   */
  @SuppressWarnings("unchecked")
  public static SecurityLink.Meta meta() {
    return SecurityLink.Meta.INSTANCE;
  }
  static {
    JodaBeanUtils.registerMetaBean(SecurityLink.Meta.INSTANCE);
  }

  @Override
  public SecurityLink.Meta metaBean() {
    return SecurityLink.Meta.INSTANCE;
  }

  @Override
  protected Object propertyGet(String propertyName, boolean quiet) {
    return super.propertyGet(propertyName, quiet);
  }

  @Override
  protected void propertySet(String propertyName, Object newValue, boolean quiet) {
    super.propertySet(propertyName, newValue, quiet);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      return super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    return hash ^ super.hashCode();
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code SecurityLink}.
   */
  public static class Meta extends Link.Meta<Security> {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<Object>> _map = new DirectMetaPropertyMap(
      this, (DirectMetaPropertyMap) super.metaPropertyMap());

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    public BeanBuilder<? extends SecurityLink> builder() {
      return new DirectBeanBuilder<SecurityLink>(new SecurityLink());
    }

    @Override
    public Class<? extends SecurityLink> beanType() {
      return SecurityLink.class;
    }

    @Override
    public Map<String, MetaProperty<Object>> metaPropertyMap() {
      return _map;
    }

    //-----------------------------------------------------------------------
  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
