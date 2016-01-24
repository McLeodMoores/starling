/**
 * Copyright (C) 2015 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.starling.client.results;

/**
 * Common interface for a synchronous analytic job.
 */
public interface SynchronousJob extends AutoCloseable {
  /**
   * Run the job synchronously and return the ResultModel, closing the job afterwards.
   * @return the result model
   */
  ResultModel run();
}
