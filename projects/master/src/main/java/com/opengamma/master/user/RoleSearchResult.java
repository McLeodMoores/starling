/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.user;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.paging.Paging;

/**
 * Result from searching for roles.
 * <p>
 * The returned documents will match the search criteria.
 * See {@link RoleSearchRequest} for more details.
 */
@BeanDefinition
public class RoleSearchResult implements Bean {

  /**
   * The paging information, not null if correctly created.
   */
  @PropertyDefinition
  private Paging _paging;
  /**
   * The roles that matched the search.
   */
  @PropertyDefinition
  private final List<ManageableRole> _roles = new ArrayList<>();

  /**
   * Creates an instance.
   */
  protected RoleSearchResult() {
  }

  /**
   * Creates an instance from a collection of roles.
   *
   * @param paging  the paging information, not null
   * @param roles  the collection of roles to add, not null
   */
  public RoleSearchResult(final Paging paging, final Collection<ManageableRole> roles) {
    _paging = ArgumentChecker.notNull(paging, "paging");
    _roles.addAll(ArgumentChecker.notNull(roles, "roles"));
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code RoleSearchResult}.
   * @return the meta-bean, not null
   */
  public static RoleSearchResult.Meta meta() {
    return RoleSearchResult.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(RoleSearchResult.Meta.INSTANCE);
  }

  @Override
  public RoleSearchResult.Meta metaBean() {
    return RoleSearchResult.Meta.INSTANCE;
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
   * Gets the paging information, not null if correctly created.
   * @return the value of the property
   */
  public Paging getPaging() {
    return _paging;
  }

  /**
   * Sets the paging information, not null if correctly created.
   * @param paging  the new value of the property
   */
  public void setPaging(Paging paging) {
    this._paging = paging;
  }

  /**
   * Gets the the {@code paging} property.
   * @return the property, not null
   */
  public final Property<Paging> paging() {
    return metaBean().paging().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the roles that matched the search.
   * @return the value of the property, not null
   */
  public List<ManageableRole> getRoles() {
    return _roles;
  }

  /**
   * Sets the roles that matched the search.
   * @param roles  the new value of the property, not null
   */
  public void setRoles(List<ManageableRole> roles) {
    JodaBeanUtils.notNull(roles, "roles");
    this._roles.clear();
    this._roles.addAll(roles);
  }

  /**
   * Gets the the {@code roles} property.
   * @return the property, not null
   */
  public final Property<List<ManageableRole>> roles() {
    return metaBean().roles().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public RoleSearchResult clone() {
    return JodaBeanUtils.cloneAlways(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      RoleSearchResult other = (RoleSearchResult) obj;
      return JodaBeanUtils.equal(getPaging(), other.getPaging()) &&
          JodaBeanUtils.equal(getRoles(), other.getRoles());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash = hash * 31 + JodaBeanUtils.hashCode(getPaging());
    hash = hash * 31 + JodaBeanUtils.hashCode(getRoles());
    return hash;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(96);
    buf.append("RoleSearchResult{");
    int len = buf.length();
    toString(buf);
    if (buf.length() > len) {
      buf.setLength(buf.length() - 2);
    }
    buf.append('}');
    return buf.toString();
  }

  protected void toString(StringBuilder buf) {
    buf.append("paging").append('=').append(JodaBeanUtils.toString(getPaging())).append(',').append(' ');
    buf.append("roles").append('=').append(JodaBeanUtils.toString(getRoles())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code RoleSearchResult}.
   */
  public static class Meta extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code paging} property.
     */
    private final MetaProperty<Paging> _paging = DirectMetaProperty.ofReadWrite(
        this, "paging", RoleSearchResult.class, Paging.class);
    /**
     * The meta-property for the {@code roles} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<List<ManageableRole>> _roles = DirectMetaProperty.ofReadWrite(
        this, "roles", RoleSearchResult.class, (Class) List.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "paging",
        "roles");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case -995747956:  // paging
          return _paging;
        case 108695229:  // roles
          return _roles;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends RoleSearchResult> builder() {
      return new DirectBeanBuilder<RoleSearchResult>(new RoleSearchResult());
    }

    @Override
    public Class<? extends RoleSearchResult> beanType() {
      return RoleSearchResult.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code paging} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Paging> paging() {
      return _paging;
    }

    /**
     * The meta-property for the {@code roles} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<List<ManageableRole>> roles() {
      return _roles;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -995747956:  // paging
          return ((RoleSearchResult) bean).getPaging();
        case 108695229:  // roles
          return ((RoleSearchResult) bean).getRoles();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -995747956:  // paging
          ((RoleSearchResult) bean).setPaging((Paging) newValue);
          return;
        case 108695229:  // roles
          ((RoleSearchResult) bean).setRoles((List<ManageableRole>) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

    @Override
    protected void validate(Bean bean) {
      JodaBeanUtils.notNull(((RoleSearchResult) bean)._roles, "roles");
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
