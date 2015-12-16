/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.quandl.loader.convention;

import java.util.Set;

import com.opengamma.master.convention.ManageableConvention;

/**
 * Classes that create conventions from a file.
 * @param <T>  the type of the conventions created
 */
public interface ConventionsLoader<T extends ManageableConvention> {

  /**
   * Creates a set of conventions from a file.
   * @return  a set of conventions, empty if no conventions could be created
   * @throws Exception  an exception if there is a problem opening the file
   */
  Set<T> loadConventionsFromFile() throws Exception;
}
