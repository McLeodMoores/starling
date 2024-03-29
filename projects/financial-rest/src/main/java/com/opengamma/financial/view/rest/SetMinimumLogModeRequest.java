/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.view.rest;

import java.util.Map;
import java.util.Set;

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

import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ExecutionLogMode;
import com.opengamma.engine.view.client.ViewClient;
import com.opengamma.util.tuple.Pair;

/**
 * Encapsulates the arguments of a call to
 * {@link ViewClient#setMinimumLogMode(com.opengamma.engine.view.ExecutionLogMode, java.util.Set)}.
 */
@BeanDefinition
public class SetMinimumLogModeRequest extends DirectBean {

  @PropertyDefinition
  private ExecutionLogMode _minimumLogMode;

  @PropertyDefinition
  private Set<Pair<String, ValueSpecification>> _targets;

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code SetMinimumLogModeRequest}.
   * @return the meta-bean, not null
   */
  public static SetMinimumLogModeRequest.Meta meta() {
    return SetMinimumLogModeRequest.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(SetMinimumLogModeRequest.Meta.INSTANCE);
  }

  @Override
  public SetMinimumLogModeRequest.Meta metaBean() {
    return SetMinimumLogModeRequest.Meta.INSTANCE;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the minimumLogMode.
   * @return the value of the property
   */
  public ExecutionLogMode getMinimumLogMode() {
    return _minimumLogMode;
  }

  /**
   * Sets the minimumLogMode.
   * @param minimumLogMode  the new value of the property
   */
  public void setMinimumLogMode(ExecutionLogMode minimumLogMode) {
    this._minimumLogMode = minimumLogMode;
  }

  /**
   * Gets the the {@code minimumLogMode} property.
   * @return the property, not null
   */
  public final Property<ExecutionLogMode> minimumLogMode() {
    return metaBean().minimumLogMode().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the targets.
   * @return the value of the property
   */
  public Set<Pair<String, ValueSpecification>> getTargets() {
    return _targets;
  }

  /**
   * Sets the targets.
   * @param targets  the new value of the property
   */
  public void setTargets(Set<Pair<String, ValueSpecification>> targets) {
    this._targets = targets;
  }

  /**
   * Gets the the {@code targets} property.
   * @return the property, not null
   */
  public final Property<Set<Pair<String, ValueSpecification>>> targets() {
    return metaBean().targets().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public SetMinimumLogModeRequest clone() {
    return JodaBeanUtils.cloneAlways(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      SetMinimumLogModeRequest other = (SetMinimumLogModeRequest) obj;
      return JodaBeanUtils.equal(getMinimumLogMode(), other.getMinimumLogMode()) &&
          JodaBeanUtils.equal(getTargets(), other.getTargets());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash = hash * 31 + JodaBeanUtils.hashCode(getMinimumLogMode());
    hash = hash * 31 + JodaBeanUtils.hashCode(getTargets());
    return hash;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(96);
    buf.append("SetMinimumLogModeRequest{");
    int len = buf.length();
    toString(buf);
    if (buf.length() > len) {
      buf.setLength(buf.length() - 2);
    }
    buf.append('}');
    return buf.toString();
  }

  protected void toString(StringBuilder buf) {
    buf.append("minimumLogMode").append('=').append(JodaBeanUtils.toString(getMinimumLogMode())).append(',').append(' ');
    buf.append("targets").append('=').append(JodaBeanUtils.toString(getTargets())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code SetMinimumLogModeRequest}.
   */
  public static class Meta extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code minimumLogMode} property.
     */
    private final MetaProperty<ExecutionLogMode> _minimumLogMode = DirectMetaProperty.ofReadWrite(
        this, "minimumLogMode", SetMinimumLogModeRequest.class, ExecutionLogMode.class);
    /**
     * The meta-property for the {@code targets} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<Set<Pair<String, ValueSpecification>>> _targets = DirectMetaProperty.ofReadWrite(
        this, "targets", SetMinimumLogModeRequest.class, (Class) Set.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "minimumLogMode",
        "targets");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case -665941223:  // minimumLogMode
          return _minimumLogMode;
        case -1538277118:  // targets
          return _targets;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends SetMinimumLogModeRequest> builder() {
      return new DirectBeanBuilder<SetMinimumLogModeRequest>(new SetMinimumLogModeRequest());
    }

    @Override
    public Class<? extends SetMinimumLogModeRequest> beanType() {
      return SetMinimumLogModeRequest.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code minimumLogMode} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ExecutionLogMode> minimumLogMode() {
      return _minimumLogMode;
    }

    /**
     * The meta-property for the {@code targets} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Set<Pair<String, ValueSpecification>>> targets() {
      return _targets;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -665941223:  // minimumLogMode
          return ((SetMinimumLogModeRequest) bean).getMinimumLogMode();
        case -1538277118:  // targets
          return ((SetMinimumLogModeRequest) bean).getTargets();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -665941223:  // minimumLogMode
          ((SetMinimumLogModeRequest) bean).setMinimumLogMode((ExecutionLogMode) newValue);
          return;
        case -1538277118:  // targets
          ((SetMinimumLogModeRequest) bean).setTargets((Set<Pair<String, ValueSpecification>>) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
