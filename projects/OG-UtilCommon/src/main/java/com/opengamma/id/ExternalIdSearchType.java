/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.id;

import com.opengamma.util.PublicAPI;

/**
 * The type of identifier search.
 */
@PublicAPI
public enum ExternalIdSearchType {

  /**
   * Match requires that the target must contain exactly the same set of identifiers.
   */
  EXACT,
  /**
   * Match requires that the target must contain all of the search identifiers.
   */
  ALL,
  /**
   * Match requires that the target must contain any of the search identifiers.
   */
  ANY,
  /**
   * Match requires that the target must contain none of the search identifiers.
   */
  NONE,

}
