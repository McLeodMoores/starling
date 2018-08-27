/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.blotter;

import org.joda.beans.MetaBean;
import org.joda.beans.MetaProperty;

import com.opengamma.util.ArgumentChecker;

/**
 * Decorator that filters out read-only properties.
 * TODO is this ever going to be used?
 */
/* package */ class WritablePropertyDecorator implements BeanVisitorDecorator {

  private final BeanVisitor<?> _delegate;

  /* package */ WritablePropertyDecorator(final BeanVisitor<?> delegate) {
    ArgumentChecker.notNull(delegate, "delegate");
    _delegate = delegate;
  }

  @Override
  public BeanVisitor<?> decorate(final BeanVisitor<?> visitor) {
    return new BeanVisitor<Object>() {
      @Override
      public void visitMetaBean(final MetaBean metaBean) {
        _delegate.visitMetaBean(metaBean);
      }

      @Override
      public void visitBeanProperty(final MetaProperty<?> property, final BeanTraverser traverser) {
        if (isWriteable(property)) {
          _delegate.visitBeanProperty(property, traverser);
        }
      }

      @Override
      public void visitCollectionProperty(final MetaProperty<?> property, final BeanTraverser traverser) {
        if (isWriteable(property)) {
          _delegate.visitCollectionProperty(property, traverser);
        }
      }

      @Override
      public void visitSetProperty(final MetaProperty<?> property, final BeanTraverser traverser) {
        if (isWriteable(property)) {
          _delegate.visitSetProperty(property, traverser);
        }
      }

      @Override
      public void visitListProperty(final MetaProperty<?> property, final BeanTraverser traverser) {
        if (isWriteable(property)) {
          _delegate.visitListProperty(property, traverser);
        }
      }

      @Override
      public void visitMapProperty(final MetaProperty<?> property, final BeanTraverser traverser) {
        if (isWriteable(property)) {
          _delegate.visitMapProperty(property, traverser);
        }
      }

      @Override
      public void visitProperty(final MetaProperty<?> property, final BeanTraverser traverser) {
        if (isWriteable(property)) {
          _delegate.visitProperty(property, traverser);
        }
      }

      @Override
      public Object finish() {
        return _delegate.finish();
      }
    };
  }

  private static boolean isWriteable(final MetaProperty<?> property) {
    return property.style().isWritable();
  }
}
