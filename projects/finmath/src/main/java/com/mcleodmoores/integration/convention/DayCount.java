/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.integration.convention;

import com.opengamma.util.NamedInstance;

/**
 * A marker interface for day count conventions. This interface extends {@link NamedInstance}, which
 * aids in the creation of factories for implementations.
 */
public interface DayCount extends NamedInstance {

}
