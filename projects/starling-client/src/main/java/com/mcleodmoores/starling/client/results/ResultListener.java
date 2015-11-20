/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.starling.client.results;

/**
 * Interface defining an asynchronous result listener for results delivered back from an AnalyticService.
 */
public interface ResultListener {
  /**
   * Called by the AnalyticService when the calculation is complete and the result is available.
   * @param resultModel  the model of the results
   * @param asynchronousJob  the job which initiated the calculation, provided so the job can be closed.
   */
  void calculationComplete(ResultModel resultModel, AsynchronousJob asynchronousJob);
}
