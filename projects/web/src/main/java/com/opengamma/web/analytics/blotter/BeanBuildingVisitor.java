/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.blotter;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.joda.beans.Bean;
import org.joda.beans.BeanBuilder;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaBean;
import org.joda.beans.MetaProperty;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.id.ExternalId;
import com.opengamma.util.ArgumentChecker;

/**
 * Builds a bean instance by traversing the {@link MetaBean} and populating the {@link MetaBean#builder() builder}
 * with data from a {@link BeanDataSource}. The {@link BeanBuilder builder} is returned so calling code can update
 * it before building the instance, e.g. to set invariant values that aren't in the data source but are necessary
 * to build a valid object.
 * @param <T> The type of bean to build
 */
/* package */ class BeanBuildingVisitor<T extends Bean> implements BeanVisitor<BeanBuilder<T>> {

  /** The source of data for building the bean */
  private final BeanDataSource _data;
  /** For looking up {@link MetaBean}s for building the bean and any sub-beans */
  private final MetaBeanFactory _metaBeanFactory;
  /** For converting the data to property values */
  private final Converters _converters;

  /** The builder for the instance being built */
  private BeanBuilder<T> _builder;

  /* package */ BeanBuildingVisitor(final BeanDataSource data, final MetaBeanFactory metaBeanFactory, final Converters converters) {
    ArgumentChecker.notNull(data, "data");
    ArgumentChecker.notNull(metaBeanFactory, "metaBeanFactory");
    ArgumentChecker.notNull(converters, "converters");
    _converters = converters;
    _metaBeanFactory = metaBeanFactory;
    _data = data;
  }

  @SuppressWarnings("unchecked")
  @Override
  public void visitMetaBean(final MetaBean metaBean) {
    _builder = (BeanBuilder<T>) metaBean.builder();
  }

  @Override
  public void visitBeanProperty(final MetaProperty<?> property, final BeanTraverser traverser) {
    visitProperty(property, traverser);
  }

  @Override
  public void visitSetProperty(final MetaProperty<?> property, final BeanTraverser traverser) {
    final List<?> dataValues = _data.getCollectionValues(property.name());
    if (dataValues == null) {
      return;
    }
    final Set<Object> values = Sets.newHashSetWithExpectedSize(dataValues.size());
    final Class<?> collectionType = JodaBeanUtils.collectionType(property, property.declaringType());
    for (final Object dataValue : dataValues) {
      values.add(convert(dataValue, property, collectionType, traverser));
    }
    _builder.set(property, values);
  }

  @Override
  public void visitListProperty(final MetaProperty<?> property, final BeanTraverser traverser) {
    final List<?> dataValues = _data.getCollectionValues(property.name());
    if (dataValues == null) {
      return;
    }
    final List<Object> values = Lists.newArrayList();
    final Class<?> collectionType = JodaBeanUtils.collectionType(property, property.declaringType());
    for (final Object dataValue : dataValues) {
      values.add(convert(dataValue, property, collectionType, traverser));
    }
    _builder.set(property, values);
  }

  @Override
  public void visitCollectionProperty(final MetaProperty<?> property, final BeanTraverser traverser) {
    visitListProperty(property, traverser);
  }

  @Override
  public void visitMapProperty(final MetaProperty<?> property, final BeanTraverser traverser) {
    _builder.set(property, buildMap(property, traverser));
  }

  @Override
  public void visitProperty(final MetaProperty<?> property, final BeanTraverser traverser) {
    _builder.set(property, convert(_data.getValue(property.name()), property, property.propertyType(), traverser));
  }

  @Override
  public BeanBuilder<T> finish() {
    return _builder;
  }

  private Object convert(final Object value, final MetaProperty<?> property, final Class<?> expectedType, final BeanTraverser traverser) {
    final Object convertedValue = _converters.convert(value, property, expectedType);
    if (convertedValue != Converters.CONVERSION_FAILED) {
      return convertedValue;
    }
    if (value instanceof BeanDataSource) {
      final BeanDataSource beanData = (BeanDataSource) value;
      final BeanBuildingVisitor<?> visitor = new BeanBuildingVisitor<>(beanData, _metaBeanFactory, _converters);
      final MetaBean metaBean = _metaBeanFactory.beanFor(beanData);
      return ((BeanBuilder<?>) traverser.traverse(metaBean, visitor)).build();
    }
    throw new IllegalArgumentException("Unable to convert " + value + " to " + expectedType.getName());
  }

  private Map<?, ?> buildMap(final MetaProperty<?> property, final BeanTraverser traverser) {
    final Map<?, ?> sourceData = _data.getMapValues(property.name());
    final Class<? extends Bean> beanType = property.metaBean().beanType();
    final Class<?> keyType = JodaBeanUtils.mapKeyType(property, beanType);
    final Class<?> valueType = JodaBeanUtils.mapValueType(property, beanType);
    final Map<Object, Object> map = Maps.newHashMapWithExpectedSize(sourceData.size());
    for (final Map.Entry<?, ?> entry : sourceData.entrySet()) {
      final Object key = convert(entry.getKey(), property, keyType, traverser);
      final Object value = convert(entry.getValue(), property, valueType, traverser);
      map.put(key, value);
    }
    return map;
  }
}

/**
 * Converter that ignores its input and always returns {@code FINANCIAL_REGION~GB}. This is for building FX securities
 * which always have the same region.
 */
/* package */ class FXRegionConverter implements Converter<Object, ExternalId> {

  /** Great Britain region */
  private static final ExternalId GB_REGION = ExternalId.of(ExternalSchemes.FINANCIAL, "GB");

  /**
   * Ignores its input, always returns {@code FINANCIAL_REGION~GB}.
   * @param notUsed Not used
   * @return {@code FINANCIAL_REGION~GB}
   */
  @Override
  public ExternalId convert(final Object notUsed) {
    return GB_REGION;
  }
}
