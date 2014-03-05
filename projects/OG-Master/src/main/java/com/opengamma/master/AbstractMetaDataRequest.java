/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master;

import java.util.Map;

import org.joda.beans.Bean;
import org.joda.beans.BeanBuilder;
import org.joda.beans.BeanDefinition;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectBean;
import org.joda.beans.impl.direct.DirectMetaBean;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;

import com.opengamma.util.PublicSPI;
import org.joda.beans.Property;
import org.joda.beans.impl.direct.DirectMetaProperty;

/**
 * Request for meta-data about a single master.
 * <p>
 * Some user interfaces require meta-data in order to operate, such as
 * a drop-down list of valid entries to select from. This abstract class
 * provides the basic ability to request such meta-data.
 */
@PublicSPI
@BeanDefinition
public abstract class AbstractMetaDataRequest extends DirectBean {
  
  /**
   * The uniqueIdScheme of the underlying master to search. Wildcards are not allowed.
   */
  @PropertyDefinition
  private String _uniqueIdScheme;

  /**
   * Creates an instance.
   */
  public AbstractMetaDataRequest() {
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code AbstractMetaDataRequest}.
   * @return the meta-bean, not null
   */
  public static AbstractMetaDataRequest.Meta meta() {
    return AbstractMetaDataRequest.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(AbstractMetaDataRequest.Meta.INSTANCE);
  }

  @Override
  public AbstractMetaDataRequest.Meta metaBean() {
    return AbstractMetaDataRequest.Meta.INSTANCE;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the uniqueIdScheme of the underlying master to search. Wildcards are not allowed.
   * @return the value of the property
   */
  public String getUniqueIdScheme() {
    return _uniqueIdScheme;
  }

  /**
   * Sets the uniqueIdScheme of the underlying master to search. Wildcards are not allowed.
   * @param uniqueIdScheme  the new value of the property
   */
  public void setUniqueIdScheme(String uniqueIdScheme) {
    this._uniqueIdScheme = uniqueIdScheme;
  }

  /**
   * Gets the the {@code uniqueIdScheme} property.
   * @return the property, not null
   */
  public final Property<String> uniqueIdScheme() {
    return metaBean().uniqueIdScheme().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public AbstractMetaDataRequest clone() {
    return JodaBeanUtils.cloneAlways(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      AbstractMetaDataRequest other = (AbstractMetaDataRequest) obj;
      return JodaBeanUtils.equal(getUniqueIdScheme(), other.getUniqueIdScheme());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash += hash * 31 + JodaBeanUtils.hashCode(getUniqueIdScheme());
    return hash;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(64);
    buf.append("AbstractMetaDataRequest{");
    int len = buf.length();
    toString(buf);
    if (buf.length() > len) {
      buf.setLength(buf.length() - 2);
    }
    buf.append('}');
    return buf.toString();
  }

  protected void toString(StringBuilder buf) {
    buf.append("uniqueIdScheme").append('=').append(JodaBeanUtils.toString(getUniqueIdScheme())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code AbstractMetaDataRequest}.
   */
  public static class Meta extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code uniqueIdScheme} property.
     */
    private final MetaProperty<String> _uniqueIdScheme = DirectMetaProperty.ofReadWrite(
        this, "uniqueIdScheme", AbstractMetaDataRequest.class, String.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "uniqueIdScheme");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case -1737146991:  // uniqueIdScheme
          return _uniqueIdScheme;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends AbstractMetaDataRequest> builder() {
      throw new UnsupportedOperationException("AbstractMetaDataRequest is an abstract class");
    }

    @Override
    public Class<? extends AbstractMetaDataRequest> beanType() {
      return AbstractMetaDataRequest.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code uniqueIdScheme} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> uniqueIdScheme() {
      return _uniqueIdScheme;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -1737146991:  // uniqueIdScheme
          return ((AbstractMetaDataRequest) bean).getUniqueIdScheme();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -1737146991:  // uniqueIdScheme
          ((AbstractMetaDataRequest) bean).setUniqueIdScheme((String) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
