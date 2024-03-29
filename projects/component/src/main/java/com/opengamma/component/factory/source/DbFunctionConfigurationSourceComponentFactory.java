/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.component.factory.source;

import java.util.ArrayList;
import java.util.List;
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

import com.opengamma.core.change.ChangeEvent;
import com.opengamma.engine.function.config.CombiningFunctionConfigurationSource;
import com.opengamma.engine.function.config.DynamicFunctionConfigurationSource;
import com.opengamma.engine.function.config.FunctionConfigurationBundle;
import com.opengamma.engine.function.config.FunctionConfigurationDefinition;
import com.opengamma.engine.function.config.FunctionConfigurationDefinitionAggregator;
import com.opengamma.engine.function.config.FunctionConfigurationSource;
import com.opengamma.financial.config.ConfigMasterChangeProvider;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.impl.MasterConfigSource;

/**
 * Component factory providing the {@code FunctionConfigurationSource} read from a {@code ConfigMaster}.
 */
@BeanDefinition
public class DbFunctionConfigurationSourceComponentFactory extends FunctionConfigurationSourceComponentFactory {
  /**
   * The function configuration definition name.
   */
  @PropertyDefinition(validate = "notNull")
  private String _functionDefinitionName;

  // -------------------------------------------------------------------------
  @Override
  protected FunctionConfigurationSource initSource() {
    final List<FunctionConfigurationSource> underlying = new ArrayList<>();
    underlying.add(new DbFunctionConfigurationSource(getConfigMaster(), getFunctionDefinitionName()));
    underlying.addAll(curveAndSurfaceSources());
    underlying.addAll(cubeSources());
    final FunctionConfigurationSource[] array = underlying.toArray(new FunctionConfigurationSource[underlying.size()]);
    return CombiningFunctionConfigurationSource.of(array);
  }

  private static class DbFunctionConfigurationSource extends DynamicFunctionConfigurationSource {

    private final ConfigMaster _configMaster;
    private final String _definitionName;

    DbFunctionConfigurationSource(final ConfigMaster configMaster, final String definitionName) {
      super(ConfigMasterChangeProvider.of(configMaster));
      _configMaster = configMaster;
      _definitionName = definitionName;
    }

    @Override
    protected boolean isPropogateEvent(final ChangeEvent event) {
      // TODO: This is a bit heavy handed; we could just watch for the definitions directly referenced
      return FunctionConfigurationDefinition.class.getName().equals(event.getObjectId().getValue());
    }

    @Override
    protected FunctionConfigurationBundle getFunctionConfiguration(final VersionCorrection version) {
      return new FunctionConfigurationDefinitionAggregator(new MasterConfigSource(_configMaster)).aggregate(_definitionName, version);
    }

  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code DbFunctionConfigurationSourceComponentFactory}.
   * @return the meta-bean, not null
   */
  public static DbFunctionConfigurationSourceComponentFactory.Meta meta() {
    return DbFunctionConfigurationSourceComponentFactory.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(DbFunctionConfigurationSourceComponentFactory.Meta.INSTANCE);
  }

  @Override
  public DbFunctionConfigurationSourceComponentFactory.Meta metaBean() {
    return DbFunctionConfigurationSourceComponentFactory.Meta.INSTANCE;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the function configuration definition name.
   * @return the value of the property, not null
   */
  public String getFunctionDefinitionName() {
    return _functionDefinitionName;
  }

  /**
   * Sets the function configuration definition name.
   * @param functionDefinitionName  the new value of the property, not null
   */
  public void setFunctionDefinitionName(String functionDefinitionName) {
    JodaBeanUtils.notNull(functionDefinitionName, "functionDefinitionName");
    this._functionDefinitionName = functionDefinitionName;
  }

  /**
   * Gets the the {@code functionDefinitionName} property.
   * @return the property, not null
   */
  public final Property<String> functionDefinitionName() {
    return metaBean().functionDefinitionName().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public DbFunctionConfigurationSourceComponentFactory clone() {
    return JodaBeanUtils.cloneAlways(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      DbFunctionConfigurationSourceComponentFactory other = (DbFunctionConfigurationSourceComponentFactory) obj;
      return JodaBeanUtils.equal(getFunctionDefinitionName(), other.getFunctionDefinitionName()) &&
          super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = hash * 31 + JodaBeanUtils.hashCode(getFunctionDefinitionName());
    return hash ^ super.hashCode();
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(64);
    buf.append("DbFunctionConfigurationSourceComponentFactory{");
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
    buf.append("functionDefinitionName").append('=').append(JodaBeanUtils.toString(getFunctionDefinitionName())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code DbFunctionConfigurationSourceComponentFactory}.
   */
  public static class Meta extends FunctionConfigurationSourceComponentFactory.Meta {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code functionDefinitionName} property.
     */
    private final MetaProperty<String> _functionDefinitionName = DirectMetaProperty.ofReadWrite(
        this, "functionDefinitionName", DbFunctionConfigurationSourceComponentFactory.class, String.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "functionDefinitionName");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case -1999640458:  // functionDefinitionName
          return _functionDefinitionName;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends DbFunctionConfigurationSourceComponentFactory> builder() {
      return new DirectBeanBuilder<DbFunctionConfigurationSourceComponentFactory>(new DbFunctionConfigurationSourceComponentFactory());
    }

    @Override
    public Class<? extends DbFunctionConfigurationSourceComponentFactory> beanType() {
      return DbFunctionConfigurationSourceComponentFactory.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code functionDefinitionName} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> functionDefinitionName() {
      return _functionDefinitionName;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -1999640458:  // functionDefinitionName
          return ((DbFunctionConfigurationSourceComponentFactory) bean).getFunctionDefinitionName();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -1999640458:  // functionDefinitionName
          ((DbFunctionConfigurationSourceComponentFactory) bean).setFunctionDefinitionName((String) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

    @Override
    protected void validate(Bean bean) {
      JodaBeanUtils.notNull(((DbFunctionConfigurationSourceComponentFactory) bean)._functionDefinitionName, "functionDefinitionName");
      super.validate(bean);
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
