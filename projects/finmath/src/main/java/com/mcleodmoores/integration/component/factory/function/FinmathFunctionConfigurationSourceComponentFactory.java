/**
 * Copyright (C) 2014 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.integration.component.factory.function;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
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
import org.threeten.bp.Instant;

import com.mcleodmoores.integration.function.curve.CurveFunctions;
import com.opengamma.component.ComponentInfo;
import com.opengamma.component.ComponentRepository;
import com.opengamma.component.factory.AbstractComponentFactory;
import com.opengamma.component.factory.ComponentInfoAttributes;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.engine.function.config.CombiningFunctionConfigurationSource;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.FunctionConfigurationBundle;
import com.opengamma.engine.function.config.FunctionConfigurationSource;
import com.opengamma.engine.function.config.ParameterizedFunctionConfiguration;
import com.opengamma.engine.function.config.StaticFunctionConfiguration;
import com.opengamma.financial.function.rest.DataRepositoryConfigurationSourceResource;
import com.opengamma.financial.function.rest.RemoteFunctionConfigurationSource;
import com.opengamma.master.config.ConfigMaster;

/**
 *
 */
@BeanDefinition
public class FinmathFunctionConfigurationSourceComponentFactory extends AbstractComponentFactory {

  /**
   * The classifier that the factory should publish under.
   */
  @PropertyDefinition(validate = "notNull")
  private String _classifier;
  /**
   * The flag determining whether the component should be published by REST (default true).
   */
  @PropertyDefinition
  private boolean _publishRest = true;
  /**
   * The config master.
   */
  @PropertyDefinition(validate = "notNull")
  private ConfigMaster _configMaster;

  //-------------------------------------------------------------------------
  @Override
  public void init(final ComponentRepository repo, final LinkedHashMap<String, String> configuration) {
    final FunctionConfigurationSource source = initSource();

    final ComponentInfo info = new ComponentInfo(FunctionConfigurationSource.class, getClassifier());
    info.addAttribute(ComponentInfoAttributes.LEVEL, 1);
    info.addAttribute(ComponentInfoAttributes.REMOTE_CLIENT_JAVA, RemoteFunctionConfigurationSource.class);
    repo.registerComponent(info, source);

    if (isPublishRest()) {
      repo.getRestComponents().publish(info, new DataRepositoryConfigurationSourceResource(source));
    }
  }

  /**
   * Debug utility to sort a repository. This allows two to be compared more easily.
   *
   * @param source the raw repository configuration source
   * @return a source that return a sorted list of functions
   */
  protected FunctionConfigurationSource sorted(final FunctionConfigurationSource source) {
    return new FunctionConfigurationSource() {

      @Override
      public FunctionConfigurationBundle getFunctionConfiguration(final Instant version) {
        final List<FunctionConfiguration> functions = new ArrayList<>(source.getFunctionConfiguration(version).getFunctions());
        Collections.sort(functions, new Comparator<FunctionConfiguration>() {

          @Override
          public int compare(final FunctionConfiguration o1, final FunctionConfiguration o2) {
            if (o1 instanceof ParameterizedFunctionConfiguration) {
              if (o2 instanceof ParameterizedFunctionConfiguration) {
                final ParameterizedFunctionConfiguration p1 = (ParameterizedFunctionConfiguration) o1;
                final ParameterizedFunctionConfiguration p2 = (ParameterizedFunctionConfiguration) o2;
                // Order by class name
                int c = p1.getDefinitionClassName().compareTo(p2.getDefinitionClassName());
                if (c != 0) {
                  return c;
                }
                // Order by parameter lengths
                c = p1.getParameter().size() - p2.getParameter().size();
                if (c != 0) {
                  return c;
                }
                // Order by parameters
                for (int i = 0; i < p1.getParameter().size(); i++) {
                  c = p1.getParameter().get(i).compareTo(p2.getParameter().get(i));
                  if (c != 0) {
                    return c;
                  }
                }
                // Equal? Put a breakpoint here; we don't really want this to be happening.
                //assert false;
                return 0;
              } else if (o2 instanceof StaticFunctionConfiguration) {
                // Static goes first
                return 1;
              }
            } else if (o1 instanceof StaticFunctionConfiguration) {
              if (o2 instanceof ParameterizedFunctionConfiguration) {
                // Static goes first
                return -1;
              } else if (o2 instanceof StaticFunctionConfiguration) {
                // Sort by class name
                return ((StaticFunctionConfiguration) o1).getDefinitionClassName().compareTo(((StaticFunctionConfiguration) o2).getDefinitionClassName());
              }
            }
            throw new UnsupportedOperationException("Can't compare " + o1.getClass() + " and " + o2.getClass());
          }

        });
        return new FunctionConfigurationBundle(functions);
      }

      @Override
      public ChangeManager changeManager() {
        return source.changeManager();
      }

    };
  }

  /**
   * Initializes the source.
   * <p>
   * Calls {@link #initSources()} and combines the result using {@link CombiningFunctionConfigurationSource}.
   *
   * @return the list of base sources to be combined, not null
   */
  protected FunctionConfigurationSource initSource() {
    final List<FunctionConfigurationSource> underlying = initSources();
    final FunctionConfigurationSource[] array = underlying.toArray(new FunctionConfigurationSource[underlying.size()]);
    return CombiningFunctionConfigurationSource.of(array);
  }

  protected FunctionConfigurationSource curveConfigurations() {
    return CurveFunctions.providers(getConfigMaster());
  }

  protected FunctionConfigurationSource curveParameterConfigurations() {
    return CurveFunctions.parameterProviders(getConfigMaster());
  }

  /**
   * Initializes the list of sources to be combined.
   *
   * @return the list of base sources to be combined, not null
   */
  protected List<FunctionConfigurationSource> initSources() {
    final List<FunctionConfigurationSource> sources = new LinkedList<>();
    sources.addAll(curveAndSurfaceSources());
    return sources;
  }

  /**
   * Gets the list of curve and surface function configuration sources.
   *
   * @return the curve and surface function configuration sources, not null
   */
  protected List<FunctionConfigurationSource> curveAndSurfaceSources() {
    final List<FunctionConfigurationSource> sources = new LinkedList<>();
    sources.add(curveConfigurations());
    sources.add(curveParameterConfigurations());
    return sources;
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code FinmathFunctionConfigurationSourceComponentFactory}.
   * @return the meta-bean, not null
   */
  public static FinmathFunctionConfigurationSourceComponentFactory.Meta meta() {
    return FinmathFunctionConfigurationSourceComponentFactory.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(FinmathFunctionConfigurationSourceComponentFactory.Meta.INSTANCE);
  }

  @Override
  public FinmathFunctionConfigurationSourceComponentFactory.Meta metaBean() {
    return FinmathFunctionConfigurationSourceComponentFactory.Meta.INSTANCE;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the classifier that the factory should publish under.
   * @return the value of the property, not null
   */
  public String getClassifier() {
    return _classifier;
  }

  /**
   * Sets the classifier that the factory should publish under.
   * @param classifier  the new value of the property, not null
   */
  public void setClassifier(String classifier) {
    JodaBeanUtils.notNull(classifier, "classifier");
    this._classifier = classifier;
  }

  /**
   * Gets the the {@code classifier} property.
   * @return the property, not null
   */
  public final Property<String> classifier() {
    return metaBean().classifier().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the flag determining whether the component should be published by REST (default true).
   * @return the value of the property
   */
  public boolean isPublishRest() {
    return _publishRest;
  }

  /**
   * Sets the flag determining whether the component should be published by REST (default true).
   * @param publishRest  the new value of the property
   */
  public void setPublishRest(boolean publishRest) {
    this._publishRest = publishRest;
  }

  /**
   * Gets the the {@code publishRest} property.
   * @return the property, not null
   */
  public final Property<Boolean> publishRest() {
    return metaBean().publishRest().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the config master.
   * @return the value of the property, not null
   */
  public ConfigMaster getConfigMaster() {
    return _configMaster;
  }

  /**
   * Sets the config master.
   * @param configMaster  the new value of the property, not null
   */
  public void setConfigMaster(ConfigMaster configMaster) {
    JodaBeanUtils.notNull(configMaster, "configMaster");
    this._configMaster = configMaster;
  }

  /**
   * Gets the the {@code configMaster} property.
   * @return the property, not null
   */
  public final Property<ConfigMaster> configMaster() {
    return metaBean().configMaster().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public FinmathFunctionConfigurationSourceComponentFactory clone() {
    return JodaBeanUtils.cloneAlways(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      FinmathFunctionConfigurationSourceComponentFactory other = (FinmathFunctionConfigurationSourceComponentFactory) obj;
      return JodaBeanUtils.equal(getClassifier(), other.getClassifier()) &&
          (isPublishRest() == other.isPublishRest()) &&
          JodaBeanUtils.equal(getConfigMaster(), other.getConfigMaster()) &&
          super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = hash * 31 + JodaBeanUtils.hashCode(getClassifier());
    hash = hash * 31 + JodaBeanUtils.hashCode(isPublishRest());
    hash = hash * 31 + JodaBeanUtils.hashCode(getConfigMaster());
    return hash ^ super.hashCode();
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(128);
    buf.append("FinmathFunctionConfigurationSourceComponentFactory{");
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
    buf.append("classifier").append('=').append(JodaBeanUtils.toString(getClassifier())).append(',').append(' ');
    buf.append("publishRest").append('=').append(JodaBeanUtils.toString(isPublishRest())).append(',').append(' ');
    buf.append("configMaster").append('=').append(JodaBeanUtils.toString(getConfigMaster())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code FinmathFunctionConfigurationSourceComponentFactory}.
   */
  public static class Meta extends AbstractComponentFactory.Meta {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code classifier} property.
     */
    private final MetaProperty<String> _classifier = DirectMetaProperty.ofReadWrite(
        this, "classifier", FinmathFunctionConfigurationSourceComponentFactory.class, String.class);
    /**
     * The meta-property for the {@code publishRest} property.
     */
    private final MetaProperty<Boolean> _publishRest = DirectMetaProperty.ofReadWrite(
        this, "publishRest", FinmathFunctionConfigurationSourceComponentFactory.class, Boolean.TYPE);
    /**
     * The meta-property for the {@code configMaster} property.
     */
    private final MetaProperty<ConfigMaster> _configMaster = DirectMetaProperty.ofReadWrite(
        this, "configMaster", FinmathFunctionConfigurationSourceComponentFactory.class, ConfigMaster.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "classifier",
        "publishRest",
        "configMaster");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case -281470431:  // classifier
          return _classifier;
        case -614707837:  // publishRest
          return _publishRest;
        case 10395716:  // configMaster
          return _configMaster;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends FinmathFunctionConfigurationSourceComponentFactory> builder() {
      return new DirectBeanBuilder<FinmathFunctionConfigurationSourceComponentFactory>(new FinmathFunctionConfigurationSourceComponentFactory());
    }

    @Override
    public Class<? extends FinmathFunctionConfigurationSourceComponentFactory> beanType() {
      return FinmathFunctionConfigurationSourceComponentFactory.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code classifier} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> classifier() {
      return _classifier;
    }

    /**
     * The meta-property for the {@code publishRest} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Boolean> publishRest() {
      return _publishRest;
    }

    /**
     * The meta-property for the {@code configMaster} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ConfigMaster> configMaster() {
      return _configMaster;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -281470431:  // classifier
          return ((FinmathFunctionConfigurationSourceComponentFactory) bean).getClassifier();
        case -614707837:  // publishRest
          return ((FinmathFunctionConfigurationSourceComponentFactory) bean).isPublishRest();
        case 10395716:  // configMaster
          return ((FinmathFunctionConfigurationSourceComponentFactory) bean).getConfigMaster();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -281470431:  // classifier
          ((FinmathFunctionConfigurationSourceComponentFactory) bean).setClassifier((String) newValue);
          return;
        case -614707837:  // publishRest
          ((FinmathFunctionConfigurationSourceComponentFactory) bean).setPublishRest((Boolean) newValue);
          return;
        case 10395716:  // configMaster
          ((FinmathFunctionConfigurationSourceComponentFactory) bean).setConfigMaster((ConfigMaster) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

    @Override
    protected void validate(Bean bean) {
      JodaBeanUtils.notNull(((FinmathFunctionConfigurationSourceComponentFactory) bean)._classifier, "classifier");
      JodaBeanUtils.notNull(((FinmathFunctionConfigurationSourceComponentFactory) bean)._configMaster, "configMaster");
      super.validate(bean);
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
