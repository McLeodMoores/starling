/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.blotter;

import java.util.Arrays;
import java.util.Set;

import org.joda.beans.MetaBean;
import org.joda.beans.MetaProperty;

import com.google.common.collect.Sets;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
/* package */ class PropertyFilter implements BeanVisitorDecorator {

  /** The properties this will be filtered out. */
  private final Set<MetaProperty<?>> _properties;

  /* package */ PropertyFilter(final MetaProperty<?>... properties) {
    ArgumentChecker.notNull(properties, "properties");
    _properties = Sets.newHashSet(Arrays.asList(properties));
  }

  @Override
  public BeanVisitor<?> decorate(final BeanVisitor<?> visitor) {
    return new BeanVisitor<Object>() {
      @Override
      public void visitMetaBean(final MetaBean metaBean) {
        visitor.visitMetaBean(metaBean);
      }

      @Override
      public void visitBeanProperty(final MetaProperty<?> property, final BeanTraverser traverser) {
        if (!_properties.contains(property)) {
          visitor.visitBeanProperty(property, traverser);
        }
      }

      @Override
      public void visitCollectionProperty(final MetaProperty<?> property, final BeanTraverser traverser) {
        if (!_properties.contains(property)) {
          visitor.visitCollectionProperty(property, traverser);
        }
      }

      @Override
      public void visitSetProperty(final MetaProperty<?> property, final BeanTraverser traverser) {
        if (!_properties.contains(property)) {
          visitor.visitSetProperty(property, traverser);
        }
      }

      @Override
      public void visitListProperty(final MetaProperty<?> property, final BeanTraverser traverser) {
        if (!_properties.contains(property)) {
          visitor.visitListProperty(property, traverser);
        }
      }

      @Override
      public void visitMapProperty(final MetaProperty<?> property, final BeanTraverser traverser) {
        if (!_properties.contains(property)) {
          visitor.visitMapProperty(property, traverser);
        }
      }

      @Override
      public void visitProperty(final MetaProperty<?> property, final BeanTraverser traverser) {
        if (!_properties.contains(property)) {
          visitor.visitProperty(property, traverser);
        }
      }

      @Override
      public Object finish() {
        return visitor.finish();
      }
    };
  }
}
