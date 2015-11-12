/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.starling.client.portfolio;

import com.opengamma.core.position.Position;

/**
 * Created by jim on 30/04/15.
 */
public interface Trade {
  Position toPosition();
}
