/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.portfolio;

import java.io.Serializable;
import java.util.Map;

import org.joda.beans.*;
import org.joda.beans.impl.direct.DirectBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;

import com.opengamma.id.UniqueId;
import com.opengamma.master.AbstractDocument;
import com.opengamma.master.DocumentVisibility;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.PublicSPI;
import org.joda.beans.BeanBuilder;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;

/**
 * A document used to pass a portfolio into and out of the portfolio master.
 * <p>
 * The portfolio consists of a tree of nodes with position identifiers.
 * To find the detail of each position, a separate search must be performed using the position master.
 */
@PublicSPI
@BeanDefinition
public class PortfolioDocument extends AbstractDocument<ManageablePortfolio> implements Serializable {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  /**
   * The portfolio unique identifier.
   * This field is managed by the master but must be set for updates.
   */
  @PropertyDefinition
  private UniqueId _uniqueId;

  /**
   * The visibility level of the portfolio.
   */
  @PropertyDefinition(validate = "notNull")
  private DocumentVisibility _visibility = DocumentVisibility.VISIBLE;

  /**
   * Creates an instance.
   */
  public PortfolioDocument() {
  }

  /**
   * Creates an instance.
   *
   * @param portfolioTree  the portfolio tree, not null
   */
  public PortfolioDocument(final ManageablePortfolio portfolioTree) {
    ArgumentChecker.notNull(portfolioTree, "portfolioTree");
    setUniqueId(portfolioTree.getUniqueId());
    setObject(portfolioTree);
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code PortfolioDocument}.
   * @return the meta-bean, not null
   */
  @SuppressWarnings("unchecked")
  public static PortfolioDocument.Meta meta() {
    return PortfolioDocument.Meta.INSTANCE;
  }
  static {
    JodaBeanUtils.registerMetaBean(PortfolioDocument.Meta.INSTANCE);
  }

  @Override
  public PortfolioDocument.Meta metaBean() {
    return PortfolioDocument.Meta.INSTANCE;
  }

  @Override
  protected Object propertyGet(String propertyName, boolean quiet) {
    switch (propertyName.hashCode()) {
      case -294460212:  // uniqueId
        return getUniqueId();
      case 1941332754:  // visibility
        return getVisibility();
    }
    return super.propertyGet(propertyName, quiet);
  }

  @Override
  protected void propertySet(String propertyName, Object newValue, boolean quiet) {
    switch (propertyName.hashCode()) {
      case -294460212:  // uniqueId
        setUniqueId((UniqueId) newValue);
        return;
      case 1941332754:  // visibility
        setVisibility((DocumentVisibility) newValue);
        return;
    }
    super.propertySet(propertyName, newValue, quiet);
  }

  @Override
  protected void validate() {
    JodaBeanUtils.notNull(_visibility, "visibility");
    super.validate();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      PortfolioDocument other = (PortfolioDocument) obj;
      return JodaBeanUtils.equal(getUniqueId(), other.getUniqueId()) &&
          JodaBeanUtils.equal(getVisibility(), other.getVisibility()) &&
          super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash += hash * 31 + JodaBeanUtils.hashCode(getUniqueId());
    hash += hash * 31 + JodaBeanUtils.hashCode(getVisibility());
    return hash ^ super.hashCode();
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the portfolio unique identifier.
   * This field is managed by the master but must be set for updates.
   * @return the value of the property
   */
  public UniqueId getUniqueId() {
    return _uniqueId;
  }

  /**
   * Sets the portfolio unique identifier.
   * This field is managed by the master but must be set for updates.
   * @param uniqueId  the new value of the property
   */
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
  /**
   * Gets the visibility level of the portfolio.
   * @return the value of the property, not null
   */
  public DocumentVisibility getVisibility() {
    return _visibility;
  }

  /**
   * Sets the visibility level of the portfolio.
   * @param visibility  the new value of the property, not null
   */
  public void setVisibility(DocumentVisibility visibility) {
    JodaBeanUtils.notNull(visibility, "visibility");
    this._visibility = visibility;
  }

  /**
   * Gets the the {@code visibility} property.
   * @return the property, not null
   */
  public final Property<DocumentVisibility> visibility() {
    return metaBean().visibility().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code PortfolioDocument}.
   */
  public static class Meta extends AbstractDocument.Meta<ManageablePortfolio> {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code uniqueId} property.
     */
    private final MetaProperty<UniqueId> _uniqueId = DirectMetaProperty.ofReadWrite(
        this, "uniqueId", PortfolioDocument.class, UniqueId.class);
    /**
     * The meta-property for the {@code visibility} property.
     */
    private final MetaProperty<DocumentVisibility> _visibility = DirectMetaProperty.ofReadWrite(
        this, "visibility", PortfolioDocument.class, DocumentVisibility.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
      this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "uniqueId",
        "visibility");

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
        case 1941332754:  // visibility
          return _visibility;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends PortfolioDocument> builder() {
      return new DirectBeanBuilder<PortfolioDocument>(new PortfolioDocument());
    }

    @Override
    public Class<? extends PortfolioDocument> beanType() {
      return PortfolioDocument.class;
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
     * The meta-property for the {@code visibility} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<DocumentVisibility> visibility() {
      return _visibility;
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
