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
 * TODO I don't know if I'll need this after all.
 */
/* package */ class PropertyNameFilter implements BeanVisitorDecorator {

  /** Names of the properties that will be filtered out. */
  private final Set<String> _propertyNames;

  /* package */ PropertyNameFilter(final String... propertyNames) {
    ArgumentChecker.notNull(propertyNames, "propertyNames");
    _propertyNames = Sets.newHashSet(Arrays.asList(propertyNames));
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
        if (!_propertyNames.contains(property.name())) {
          visitor.visitBeanProperty(property, traverser);
        }
      }

      @Override
      public void visitCollectionProperty(final MetaProperty<?> property, final BeanTraverser traverser) {
        if (!_propertyNames.contains(property.name())) {
          visitor.visitCollectionProperty(property, traverser);
        }
      }

      @Override
      public void visitSetProperty(final MetaProperty<?> property, final BeanTraverser traverser) {
        if (!_propertyNames.contains(property.name())) {
          visitor.visitSetProperty(property, traverser);
        }
      }

      @Override
      public void visitListProperty(final MetaProperty<?> property, final BeanTraverser traverser) {
        if (!_propertyNames.contains(property.name())) {
          visitor.visitListProperty(property, traverser);
        }
      }

      @Override
      public void visitMapProperty(final MetaProperty<?> property, final BeanTraverser traverser) {
        if (!_propertyNames.contains(property.name())) {
          visitor.visitMapProperty(property, traverser);
        }
      }

      @Override
      public void visitProperty(final MetaProperty<?> property, final BeanTraverser traverser) {
        if (!_propertyNames.contains(property.name())) {
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
