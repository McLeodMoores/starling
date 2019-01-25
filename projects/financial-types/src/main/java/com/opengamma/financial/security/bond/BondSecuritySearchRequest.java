/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.bond;

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

import com.opengamma.master.security.SecurityDocument;
import com.opengamma.master.security.SecuritySearchRequest;
import com.opengamma.util.PublicSPI;
import com.opengamma.util.RegexUtils;

/**
 * Request to search for bond securities.
 * <p>
 * This extends the basic search request with criteria that are specific to bonds.
 * It also sets the security type to search for to be bonds.
 */
@PublicSPI
@BeanDefinition
public class BondSecuritySearchRequest extends SecuritySearchRequest {

  /**
   * The issuer name of the bond, wildcards allowed, null not to match on issuer name.
   */
  @PropertyDefinition
  private String _issuerName;
  /**
   * The issuer type of the bond, wildcards allowed, null not to match on issuer name.
   */
  @PropertyDefinition
  private String _issuerType;

  /**
   * Creates an instance.
   * This sets the security type in the search.
   */
  public BondSecuritySearchRequest() {
    setSecurityType(BondSecurity.SECURITY_TYPE);
  }

  //-------------------------------------------------------------------------
  /**
   * Checks if this search matches the specified document.
   *
   * @param document  the document to match, null returns false
   * @return true if matches
   */
  public boolean matches(final SecurityDocument document) {
    if (super.matches(document) == false || !(document.getSecurity() instanceof BondSecurity)) {
      return false;
    }
    final BondSecurity security = (BondSecurity) document.getSecurity();
    if (getIssuerName() != null && RegexUtils.wildcardMatch(getIssuerName(), security.getIssuerName()) == false) {
      return false;
    }
    if (getIssuerType() != null && RegexUtils.wildcardMatch(getIssuerType(), security.getIssuerType()) == false) {
      return false;
    }
    return true;
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code BondSecuritySearchRequest}.
   * @return the meta-bean, not null
   */
  public static BondSecuritySearchRequest.Meta meta() {
    return BondSecuritySearchRequest.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(BondSecuritySearchRequest.Meta.INSTANCE);
  }

  @Override
  public BondSecuritySearchRequest.Meta metaBean() {
    return BondSecuritySearchRequest.Meta.INSTANCE;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the issuer name of the bond, wildcards allowed, null not to match on issuer name.
   * @return the value of the property
   */
  public String getIssuerName() {
    return _issuerName;
  }

  /**
   * Sets the issuer name of the bond, wildcards allowed, null not to match on issuer name.
   * @param issuerName  the new value of the property
   */
  public void setIssuerName(String issuerName) {
    this._issuerName = issuerName;
  }

  /**
   * Gets the the {@code issuerName} property.
   * @return the property, not null
   */
  public final Property<String> issuerName() {
    return metaBean().issuerName().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the issuer type of the bond, wildcards allowed, null not to match on issuer name.
   * @return the value of the property
   */
  public String getIssuerType() {
    return _issuerType;
  }

  /**
   * Sets the issuer type of the bond, wildcards allowed, null not to match on issuer name.
   * @param issuerType  the new value of the property
   */
  public void setIssuerType(String issuerType) {
    this._issuerType = issuerType;
  }

  /**
   * Gets the the {@code issuerType} property.
   * @return the property, not null
   */
  public final Property<String> issuerType() {
    return metaBean().issuerType().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public BondSecuritySearchRequest clone() {
    return JodaBeanUtils.cloneAlways(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      BondSecuritySearchRequest other = (BondSecuritySearchRequest) obj;
      return JodaBeanUtils.equal(getIssuerName(), other.getIssuerName()) &&
          JodaBeanUtils.equal(getIssuerType(), other.getIssuerType()) &&
          super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = hash * 31 + JodaBeanUtils.hashCode(getIssuerName());
    hash = hash * 31 + JodaBeanUtils.hashCode(getIssuerType());
    return hash ^ super.hashCode();
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(96);
    buf.append("BondSecuritySearchRequest{");
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
    buf.append("issuerName").append('=').append(JodaBeanUtils.toString(getIssuerName())).append(',').append(' ');
    buf.append("issuerType").append('=').append(JodaBeanUtils.toString(getIssuerType())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code BondSecuritySearchRequest}.
   */
  public static class Meta extends SecuritySearchRequest.Meta {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code issuerName} property.
     */
    private final MetaProperty<String> _issuerName = DirectMetaProperty.ofReadWrite(
        this, "issuerName", BondSecuritySearchRequest.class, String.class);
    /**
     * The meta-property for the {@code issuerType} property.
     */
    private final MetaProperty<String> _issuerType = DirectMetaProperty.ofReadWrite(
        this, "issuerType", BondSecuritySearchRequest.class, String.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "issuerName",
        "issuerType");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case 1459772644:  // issuerName
          return _issuerName;
        case 1459974547:  // issuerType
          return _issuerType;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends BondSecuritySearchRequest> builder() {
      return new DirectBeanBuilder<BondSecuritySearchRequest>(new BondSecuritySearchRequest());
    }

    @Override
    public Class<? extends BondSecuritySearchRequest> beanType() {
      return BondSecuritySearchRequest.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code issuerName} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> issuerName() {
      return _issuerName;
    }

    /**
     * The meta-property for the {@code issuerType} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> issuerType() {
      return _issuerType;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 1459772644:  // issuerName
          return ((BondSecuritySearchRequest) bean).getIssuerName();
        case 1459974547:  // issuerType
          return ((BondSecuritySearchRequest) bean).getIssuerType();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 1459772644:  // issuerName
          ((BondSecuritySearchRequest) bean).setIssuerName((String) newValue);
          return;
        case 1459974547:  // issuerType
          ((BondSecuritySearchRequest) bean).setIssuerType((String) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
