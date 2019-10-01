/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.push;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Context;

import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.api.model.AbstractMethod;
import com.sun.jersey.api.model.AbstractResourceMethod;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;
import com.sun.jersey.spi.container.ContainerResponse;
import com.sun.jersey.spi.container.ContainerResponseFilter;
import com.sun.jersey.spi.container.ResourceFilter;
import com.sun.jersey.spi.container.ResourceFilterFactory;

/**
 * test filter for exploring the data available in the {@code filter} methods.
 */
public class TestFilterFactory implements ResourceFilterFactory {

  @Context
  HttpContext _httpContext;

  @Override
  public List<ResourceFilter> create(final AbstractMethod abstractMethod) {
    final List<ResourceFilter> filters = new ArrayList<>();
    filters.add(new TestFilter(abstractMethod));
    return filters;
  }

  class TestFilter implements ResourceFilter {

    private final AbstractMethod _method;

    TestFilter(final AbstractMethod method) {
      _method = method;
    }

    @Override
    public ContainerRequestFilter getRequestFilter() {
      return null;
    }

    @Override
    public ContainerResponseFilter getResponseFilter() {
      return new TestResponseFilter();
    }

    class TestResponseFilter implements ContainerResponseFilter {

      @Override
      public ContainerResponse filter(final ContainerRequest request, final ContainerResponse response) {
        final AbstractResourceMethod matchedMethod = _httpContext.getUriInfo().getMatchedMethod();
        final boolean methodsEqual = matchedMethod.equals(_method);
        final String methodName = methodName(_method);
        final String matchedMethodName = methodName(matchedMethod);
        System.out.println(methodsEqual);
        System.out.println(methodName);
        System.out.println(matchedMethodName);
        return response;
      }
    }
  }

  private static String methodName(final AbstractMethod abstractMethod) {
    final Method method = abstractMethod.getMethod();
    return method.getDeclaringClass().getSimpleName() + "." + method.getName() + "()";
  }
}
