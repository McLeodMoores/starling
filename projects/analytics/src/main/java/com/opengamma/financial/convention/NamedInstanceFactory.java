/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention;

/**
 * An interface for named instances.
 * <p>
 * DEPRECATED: Use {@link com.opengamma.util.NamedInstanceFactory} - this is
 * here to minimize code changes.
 * <p>
 * A named instance is a type where each instance is uniquely identified by a
 * name. This factory provides access to all the instances.
 * <p>
 * Implementations should typically be singletons with a public static factory
 * instance named 'INSTANCE' accessible using
 * {@link com.opengamma.util.ClassUtils#singletonInstance(Class)}.
 *
 * @param <T>
 *          type of objects returned
 * @deprecated use {@link com.opengamma.util.NamedInstanceFactory}
 */
@Deprecated
public interface NamedInstanceFactory<T extends NamedInstance> extends com.opengamma.util.NamedInstanceFactory<T> {

}
