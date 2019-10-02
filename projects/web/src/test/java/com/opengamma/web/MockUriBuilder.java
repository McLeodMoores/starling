/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.Formatter;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.Path;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriBuilderException;

import com.opengamma.util.ArgumentChecker;

/**
 * MockUriBuilder intended for testing in memory web resources.
 */
public class MockUriBuilder extends UriBuilder {

  private static final Pattern PATH_PATTERN = Pattern.compile("\\{\\w+\\}");

  private String _pathFormat = "";
  private final StringBuilder _query = new StringBuilder();

  @Override
  public UriBuilder clone() {
    return this;
  }

  @Override
  public UriBuilder uri(final URI uri) throws IllegalArgumentException {
    return this;
  }

  @Override
  public UriBuilder scheme(final String scheme) throws IllegalArgumentException {
    return this;
  }

  @Override
  public UriBuilder schemeSpecificPart(final String ssp) throws IllegalArgumentException {
    return this;
  }

  @Override
  public UriBuilder userInfo(final String ui) {
    return this;
  }

  @Override
  public UriBuilder host(final String host) throws IllegalArgumentException {
    return this;
  }

  @Override
  public UriBuilder port(final int port) throws IllegalArgumentException {
    return this;
  }

  @Override
  public UriBuilder replacePath(final String path) {
    return this;
  }

  @Override
  public UriBuilder path(final String path) throws IllegalArgumentException {
    ArgumentChecker.notNull(path, "path");
    formathPath(path);
    return this;
  }

  @Override
  public UriBuilder path(final Class resource) throws IllegalArgumentException {
    ArgumentChecker.notNull(resource, "class");
    final Annotation annotation = resource.getAnnotation(Path.class);
    if (annotation == null) {
      throw new IllegalArgumentException();
    }
    formatPath((Path) annotation);
    return this;
  }

  private void formatPath(final Path annotation) {
    final String path = annotation.value();
    formathPath(path);
  }

  private void formathPath(final String path) {
    final Matcher matcher = PATH_PATTERN.matcher(path);
    int start = 0;
    int end = 0;
    final StringBuilder buf = new StringBuilder();
    int count = 0;
    while (matcher.find()) {
      end = matcher.start();
      buf.append(path.substring(start, end)).append("%" + ++count + "$s");
      start = matcher.end();
    }
    buf.append(path.substring(start, path.length()));

    if (path.startsWith("/")) {
      _pathFormat += buf.toString();
    } else {
      _pathFormat += "/" + buf.toString();
    }
  }

  @Override
  public UriBuilder path(final Class resource, final String methodName) throws IllegalArgumentException {
    ArgumentChecker.notNull(resource, "class");
    final Method[] methods = resource.getMethods();
    Method method = null;
    for (final Method aMethod : methods) {
      if (aMethod.getName().equals(methodName)) {
        method = aMethod;
        break;
      }
    }
    if (method == null) {
      throw new IllegalArgumentException("Method " + methodName + " can not be found in class " + resource);
    }
    path(method);
    return this;
  }

  @Override
  public UriBuilder path(final Method method) throws IllegalArgumentException {
    ArgumentChecker.notNull(method, "method");
    final Path annotation = method.getAnnotation(Path.class);
    if (annotation == null) {
      throw new IllegalArgumentException("Path annotation missing in method " + method.getName());
    }
    formatPath(annotation);
    return this;
  }

  @Override
  public UriBuilder segment(final String... segments) throws IllegalArgumentException {
    return this;
  }

  @Override
  public UriBuilder replaceMatrix(final String matrix) throws IllegalArgumentException {
    return this;
  }

  @Override
  public UriBuilder matrixParam(final String name, final Object... values) throws IllegalArgumentException {
    return this;
  }

  @Override
  public UriBuilder replaceMatrixParam(final String name, final Object... values) throws IllegalArgumentException {
    return this;
  }

  @Override
  public UriBuilder replaceQuery(final String query) throws IllegalArgumentException {
    return this;
  }

  @Override
  public UriBuilder queryParam(final String name, final Object... values) throws IllegalArgumentException {
    if (values == null || values.length == 0) {
      return this;
    }
    for (final Object value : values) {
      if (value != null) {
        _query.append('&');
        _query.append(name);
        _query.append('=');
        _query.append(value.toString());
      }
    }
    return this;
  }

  @Override
  public UriBuilder replaceQueryParam(final String name, final Object... values) throws IllegalArgumentException {
    return this;
  }

  @Override
  public UriBuilder fragment(final String fragment) {
    return this;
  }

  @Override
  public URI buildFromMap(final Map<String, ? extends Object> values) throws IllegalArgumentException, UriBuilderException {
    return null;
  }

  @Override
  public URI buildFromEncodedMap(final Map<String, ? extends Object> values) throws IllegalArgumentException, UriBuilderException {
    return null;
  }

  @Override
  public URI build(final Object... values) throws IllegalArgumentException, UriBuilderException {
    String url = null;
    try (Formatter formatter = new Formatter()) {
      url = formatter.format(_pathFormat, values).toString();
      if (_query.length() > 0) {
        url += "?" + _query.toString();
      }
    } catch (final Exception ex) {
      throw new UriBuilderException("Problem building url from format[" + _pathFormat + "] and values[" + values + "]", ex);
    }
    return URI.create(url);
  }

  @Override
  public URI buildFromEncoded(final Object... values) throws IllegalArgumentException, UriBuilderException {
    return null;
  }

}
