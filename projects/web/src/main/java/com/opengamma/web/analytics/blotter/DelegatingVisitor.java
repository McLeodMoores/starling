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
 * @param <T>
 *          the type of the bean
 */
// TODO do I need this?
/* package */ abstract class DelegatingVisitor<T> implements BeanVisitor<T> {

  private final BeanVisitor<T> _delegate;

  /* package */ DelegatingVisitor(final BeanVisitor<T> delegate) {
    ArgumentChecker.notNull(delegate, "delegate");
    _delegate = delegate;
  }

  @Override
  public void visitMetaBean(final MetaBean metaBean) {
    _delegate.visitMetaBean(metaBean);
  }

  @Override
  public void visitBeanProperty(final MetaProperty<?> property, final BeanTraverser traverser) {
    _delegate.visitBeanProperty(property, traverser);
  }

  @Override
  public void visitCollectionProperty(final MetaProperty<?> property, final BeanTraverser traverser) {
    _delegate.visitCollectionProperty(property, traverser);
  }

  @Override
  public void visitSetProperty(final MetaProperty<?> property, final BeanTraverser traverser) {
    _delegate.visitSetProperty(property, traverser);
  }

  @Override
  public void visitListProperty(final MetaProperty<?> property, final BeanTraverser traverser) {
    _delegate.visitListProperty(property, traverser);
  }

  @Override
  public void visitMapProperty(final MetaProperty<?> property, final BeanTraverser traverser) {
    _delegate.visitMapProperty(property, traverser);
  }

  @Override
  public void visitProperty(final MetaProperty<?> property, final BeanTraverser traverser) {
    _delegate.visitProperty(property, traverser);
  }

  @Override
  public T finish() {
    return _delegate.finish();
  }
}
