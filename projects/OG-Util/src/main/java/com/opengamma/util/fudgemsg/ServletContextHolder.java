/**
 * Copyright (C) 2014 McLeod Moores Software Limited.
 */
package com.opengamma.util.fudgemsg;

import javax.servlet.ServletContext;

/**
 * This class holds an optional ServletContext in the case OpenGamma is running inside a servlet.  This allows the OpenGammaFudgeContext to scan
 * the servlet classes if available.
 */
public class ServletContextHolder {
  private static ServletContext s_context;
  
  public static void setContext(ServletContext context) {
    s_context = context;
  }
  
  public static ServletContext getContext() {
    return s_context;
  }
}
