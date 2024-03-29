/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.convention;

import java.io.Serializable;
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

import com.opengamma.id.UniqueId;
import com.opengamma.master.AbstractDocument;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.PublicSPI;

/**
 * A document used to pass into and out of the convention master.
 */
@PublicSPI
@BeanDefinition
public class ConventionDocument extends AbstractDocument implements Serializable {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  /**
   * The convention object held by the document.
   */
  @PropertyDefinition
  private ManageableConvention _convention;
  /**
   * The convention unique identifier.
   * This field is managed by the master but must be set for updates.
   */
  @PropertyDefinition(overrideGet = true, overrideSet = true)
  private UniqueId _uniqueId;
  /**
   * The document name.
   */
  private String _name;

  /**
   * Creates an instance.
   */
  public ConventionDocument() {
  }

  /**
   * Creates an instance from a convention.
   * @param convention  the convention, not null
   */
  public ConventionDocument(final ManageableConvention convention) {
    ArgumentChecker.notNull(convention, "convention");
    setUniqueId(convention.getUniqueId());
    setConvention(convention);
  }

  //-------------------------------------------------------------------------
  @Override
  public ManageableConvention getValue() {
    return getConvention();
  }

  /**
   * Gets the name of the convention.
   * <p>
   * This is derived from the convention itself.
   *
   * @return the name, null if no name
   */
  public String getName() {
    if (_name == null && getConvention() != null && getConvention().getName() != null) {
      _name = getConvention().getName();
    }
    return _name;
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code ConventionDocument}.
   * @return the meta-bean, not null
   */
  public static ConventionDocument.Meta meta() {
    return ConventionDocument.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(ConventionDocument.Meta.INSTANCE);
  }

  @Override
  public ConventionDocument.Meta metaBean() {
    return ConventionDocument.Meta.INSTANCE;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the convention object held by the document.
   * @return the value of the property
   */
  public ManageableConvention getConvention() {
    return _convention;
  }

  /**
   * Sets the convention object held by the document.
   * @param convention  the new value of the property
   */
  public void setConvention(ManageableConvention convention) {
    this._convention = convention;
  }

  /**
   * Gets the the {@code convention} property.
   * @return the property, not null
   */
  public final Property<ManageableConvention> convention() {
    return metaBean().convention().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the convention unique identifier.
   * This field is managed by the master but must be set for updates.
   * @return the value of the property
   */
  @Override
  public UniqueId getUniqueId() {
    return _uniqueId;
  }

  /**
   * Sets the convention unique identifier.
   * This field is managed by the master but must be set for updates.
   * @param uniqueId  the new value of the property
   */
  @Override
  public void setUniqueId(UniqueId uniqueId) {
    this._uniqueId = uniqueId;
  }

  /**
   * Gets the the {@code uniqueId} property.
   * This field is managed by the master but must be set for updates.
   * @return the property, not null
   */
  public final Property<UniqueId> uniqueId() {
    return metaBean().uniqueId().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public ConventionDocument clone() {
    return JodaBeanUtils.cloneAlways(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      ConventionDocument other = (ConventionDocument) obj;
      return JodaBeanUtils.equal(getConvention(), other.getConvention()) &&
          JodaBeanUtils.equal(getUniqueId(), other.getUniqueId()) &&
          super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = hash * 31 + JodaBeanUtils.hashCode(getConvention());
    hash = hash * 31 + JodaBeanUtils.hashCode(getUniqueId());
    return hash ^ super.hashCode();
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(96);
    buf.append("ConventionDocument{");
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
    buf.append("convention").append('=').append(JodaBeanUtils.toString(getConvention())).append(',').append(' ');
    buf.append("uniqueId").append('=').append(JodaBeanUtils.toString(getUniqueId())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code ConventionDocument}.
   */
  public static class Meta extends AbstractDocument.Meta {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code convention} property.
     */
    private final MetaProperty<ManageableConvention> _convention = DirectMetaProperty.ofReadWrite(
        this, "convention", ConventionDocument.class, ManageableConvention.class);
    /**
     * The meta-property for the {@code uniqueId} property.
     */
    private final MetaProperty<UniqueId> _uniqueId = DirectMetaProperty.ofReadWrite(
        this, "uniqueId", ConventionDocument.class, UniqueId.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "convention",
        "uniqueId");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case 2039569265:  // convention
          return _convention;
        case -294460212:  // uniqueId
          return _uniqueId;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends ConventionDocument> builder() {
      return new DirectBeanBuilder<ConventionDocument>(new ConventionDocument());
    }

    @Override
    public Class<? extends ConventionDocument> beanType() {
      return ConventionDocument.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code convention} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ManageableConvention> convention() {
      return _convention;
    }

    /**
     * The meta-property for the {@code uniqueId} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<UniqueId> uniqueId() {
      return _uniqueId;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 2039569265:  // convention
          return ((ConventionDocument) bean).getConvention();
        case -294460212:  // uniqueId
          return ((ConventionDocument) bean).getUniqueId();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 2039569265:  // convention
          ((ConventionDocument) bean).setConvention((ManageableConvention) newValue);
          return;
        case -294460212:  // uniqueId
          ((ConventionDocument) bean).setUniqueId((UniqueId) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
