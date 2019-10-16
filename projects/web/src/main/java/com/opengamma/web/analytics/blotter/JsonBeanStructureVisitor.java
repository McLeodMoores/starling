/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.blotter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.joda.beans.Bean;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaBean;
import org.joda.beans.MetaProperty;
import org.joda.convert.StringConvert;
import org.json.JSONObject;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.ArgumentChecker;
import org.joda.beans.gen.PropertyDefinition;

/**
 * TODO can this be deleted?
 * TODO how should read-only properties be handled? I guess it depends on the use case of the data
 */
// TODO do this as HTML, easier to consume
/* package */ class JsonBeanStructureVisitor implements BeanVisitor<JSONObject> {

  private static final Map<Class<?>, String> TYPES = Maps.newHashMap();
  private static final String NUMBER = "number";
  private static final String BOOLEAN = "boolean";
  private static final String STRING = "string";

  static {
    TYPES.put(Double.TYPE, NUMBER);
    TYPES.put(Double.class, NUMBER);
    TYPES.put(Float.TYPE, NUMBER);
    TYPES.put(Float.class, NUMBER);
    TYPES.put(Long.TYPE, NUMBER);
    TYPES.put(Long.class, NUMBER);
    TYPES.put(Short.TYPE, NUMBER);
    TYPES.put(Short.class, NUMBER);
    TYPES.put(Integer.TYPE, NUMBER);
    TYPES.put(Integer.class, NUMBER);
    TYPES.put(Byte.TYPE, NUMBER);
    TYPES.put(Byte.class, NUMBER);
    TYPES.put(BigDecimal.class, NUMBER);
    TYPES.put(Boolean.TYPE, BOOLEAN);
    TYPES.put(Boolean.class, BOOLEAN);
    TYPES.put(Character.TYPE, STRING);
    TYPES.put(Character.class, STRING);
    TYPES.put(String.class, STRING);
  }

  private final Map<String, Object> _json = Maps.newHashMap();
  private final BeanHierarchy _beanHierarchy;
  private final StringConvert _stringConvert;

  /* package */ JsonBeanStructureVisitor(final Set<MetaBean> metaBeans) {
    ArgumentChecker.notNull(metaBeans, "metaBeans");
    _beanHierarchy = new BeanHierarchy(metaBeans);
    // TODO parameter for this
    _stringConvert = JodaBeanUtils.stringConverter();
  }

  @Override
  public void visitMetaBean(final MetaBean metaBean) {
    // TODO configurable field name
    _json.clear();
    _json.put("type", metaBean.beanType().getSimpleName());
  }

  @Override
  public void visitBeanProperty(final MetaProperty<?> property, final BeanTraverser traverser) {
    final Set<Class<? extends Bean>> argumentTypes = _beanHierarchy.subtypes(property.propertyType());
    if (argumentTypes.isEmpty()) {
      throw new OpenGammaRuntimeException("No bean types are available to satisfy property " + property);
    }
    final List<String> beanTypeNames = Lists.newArrayListWithCapacity(argumentTypes.size());
    for (final Class<? extends Bean> argumentType : argumentTypes) {
      beanTypeNames.add(argumentType.getSimpleName());
    }
    _json.put(property.name(), optional(property, StringUtils.join(beanTypeNames, "|")));
  }

  @Override
  public void visitCollectionProperty(final MetaProperty<?> property, final BeanTraverser traverser) {
    _json.put(property.name(), arrayType(property));
  }

  @Override
  public void visitSetProperty(final MetaProperty<?> property, final BeanTraverser traverser) {
    _json.put(property.name(), arrayType(property));
  }

  @Override
  public void visitListProperty(final MetaProperty<?> property, final BeanTraverser traverser) {
    _json.put(property.name(), arrayType(property));
  }

  @Override
  public void visitMapProperty(final MetaProperty<?> property, final BeanTraverser traverser) {
    final Class<? extends Bean> beanType = property.metaBean().beanType();
    final Class<?> keyType = JodaBeanUtils.mapKeyType(property, beanType);
    final Class<?> valueType = JodaBeanUtils.mapValueType(property, beanType);
    _json.put(property.name(), optional(property,  "{" + typeFor(keyType) + ":" + typeFor(valueType) + "}"));
  }

  @Override
  public void visitProperty(final MetaProperty<?> property, final BeanTraverser traverser) {
    _json.put(property.name(), optional(property, typeFor(property)));
  }

  private static String optional(final MetaProperty<?> property, final String type) {
    if (nullable(property)) {
      return type + "?";
    }
    return type;
  }

  @Override
  public JSONObject finish() {
    return new JSONObject(_json);
  }

  private String arrayType(final MetaProperty<?> property) {
    return optional(property, "[" + typeFor(property.propertyType()) + "]");
  }

  private String typeFor(final MetaProperty<?> property) {
    return typeFor(property.propertyType());
  }

  private String typeFor(final Class<?> type) {
    final String typeName = TYPES.get(type);
    if (typeName != null) {
      return typeName;
    }
    try {
      _stringConvert.findConverter(type);
      return STRING;
    } catch (final Exception e) {
      throw new OpenGammaRuntimeException("No type mapping found for class " + type.getName(), e);
    }
  }

  private static boolean nullable(final MetaProperty<?> property) {
    if (property.propertyType().isPrimitive()) {
      return false;
    }
    final PropertyDefinition definitionAnnotation = property.annotation(PropertyDefinition.class);
    return !definitionAnnotation.validate().equals("notNull");
  }
}
