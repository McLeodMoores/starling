/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.blotter;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.joda.beans.Bean;
import org.joda.beans.MetaBean;
import org.joda.beans.MetaProperty;

import com.google.common.collect.Lists;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
/*
 * TODO is there a generally useful way to have pluggable handlers to override
 * default behaviour for specific properties? or would that have to be done in
 * the visitors? could also handle it by property name instead of using the
 * metaproperty
 */
/* package */ class BeanTraverser {

  /** Decorators that are applied to the visitor in the {@link #traverse} method. */
  private final List<BeanVisitorDecorator> _decorators;

  /* package */ BeanTraverser() {
    _decorators = Collections.emptyList();
  }

  /* package */ BeanTraverser(final BeanVisitorDecorator... decorators) {
    _decorators = Arrays.asList(decorators);
    // first decorator in the list should be on the outside, need to reverse before wrapping
    Collections.reverse(_decorators);
  }

  /* package */ Object traverse(final MetaBean metaBean, final BeanVisitor<?> visitor) {
    final BeanVisitor<?> decoratedVisitor = decorate(visitor);
    decoratedVisitor.visitMetaBean(metaBean);
    final List<BeanTraversalFailure> failures = Lists.newArrayList();
    for (final MetaProperty<?> property : metaBean.metaPropertyIterable()) {
      final Class<?> propertyType = property.propertyType();
      try {
        if (Bean.class.isAssignableFrom(propertyType)) {
          decoratedVisitor.visitBeanProperty(property, this);
        } else if (Set.class.isAssignableFrom(propertyType)) {
          decoratedVisitor.visitSetProperty(property, this);
        } else if (List.class.isAssignableFrom(propertyType)) {
          decoratedVisitor.visitListProperty(property, this);
        } else if (Collection.class.isAssignableFrom(propertyType)) {
          decoratedVisitor.visitCollectionProperty(property, this);
        } else if (Map.class.isAssignableFrom(propertyType)) {
          decoratedVisitor.visitMapProperty(property, this);
        } else {
          decoratedVisitor.visitProperty(property, this);
        }
      } catch (final Exception e) {
        failures.add(new BeanTraversalFailure(e, property));
      }
    }
    if (failures.isEmpty()) {
      return decoratedVisitor.finish();
    }
    throw new BeanTraversalException(metaBean, visitor, failures);
  }

  private BeanVisitor<?> decorate(final BeanVisitor<?> visitor) {
    BeanVisitor<?> decoratedVisitor = visitor;
    for (final BeanVisitorDecorator decorator : _decorators) {
      decoratedVisitor = decorator.decorate(decoratedVisitor);
    }
    return decoratedVisitor;
  }
}

/**
 * Wraps a property and the exception that occurred when the traverser tried to visit the property.
 */
/* package */ class BeanTraversalFailure {

  /** The exception triggered by the attempted visit. */
  private final Exception _exception;
  /** The visited property. */
  private final MetaProperty<?> _property;

  /* package */ BeanTraversalFailure(final Exception exception, final MetaProperty<?> property) {
    ArgumentChecker.notNull(exception, "exception");
    ArgumentChecker.notNull(property, "property");
    _exception = exception;
    _property = property;
  }

  /* package */ Exception getException() {
    return _exception;
  }

  @Override
  public String toString() {
    final String message = _exception.getMessage() == null ? null : "'" + _exception.getMessage() + "'";
    return "[" + _property.toString() + ", " + message + "]";
  }
}

/**
 * Exception thrown after a bean traversal that threw exceptions. All exceptions thrown during traversal are added
 * to this exception as {@link #addSuppressed suppressed} exceptions.
 */
/* package */ class BeanTraversalException extends OpenGammaRuntimeException {

  /** Serialization version. */
  private static final long serialVersionUID = -3048022694152981946L;

  /* package */ BeanTraversalException(final MetaBean metaBean, final BeanVisitor<?> visitor, final List<BeanTraversalFailure> failures) {
    super(buildMessage(metaBean, visitor, failures));
    for (final BeanTraversalFailure failure : failures) {
      addSuppressed(failure.getException());
    }
  }

  private static String buildMessage(final MetaBean metaBean, final BeanVisitor<?> visitor, final List<BeanTraversalFailure> failures) {
    ArgumentChecker.notNull(metaBean, "metaBean");
    ArgumentChecker.notEmpty(failures, "failures");
    ArgumentChecker.notNull(visitor, "visitor");
    return "Bean traversal failed. " +
    "bean: " + metaBean + ", " +
    "visitor: " + visitor + ", " +
    "failures: [" + StringUtils.join(failures, ", ") + "]";
  }

}
