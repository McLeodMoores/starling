/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.transport;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.fail;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Test InMemoryBatchMessageDispatcher.
 */
@Test(groups = TestGroup.INTEGRATION)
public class InMemoryBatchMessageDispatcherTest {

  /**
   * @throws Exception
   *           if there is an interruption
   */
  public void testBaseUsage() throws Exception {
    final InMemoryBatchMessageDispatcher dispatcher = new InMemoryBatchMessageDispatcher();
    final List<Integer> batchSizes = Collections.synchronizedList(new ArrayList<Integer>());
    final BatchByteArrayMessageReceiver receiver = new BatchByteArrayMessageReceiver() {
      @Override
      public void messagesReceived(final List<byte[]> messages) {
        batchSizes.add(messages.size());
      }
    };
    dispatcher.addReceiver(receiver);

    // Before start, put 5 messages in to make sure that we get 5 messages in one batch.
    dispatcher.getQueue().add(new byte[10]);
    dispatcher.getQueue().add(new byte[10]);
    dispatcher.getQueue().add(new byte[10]);
    dispatcher.getQueue().add(new byte[10]);
    dispatcher.getQueue().add(new byte[10]);

    dispatcher.start();

    assertBatchSize(batchSizes, 5);

    batchSizes.clear();

    dispatcher.getQueue().add(new byte[20]);

    assertBatchSize(batchSizes, 1);

    dispatcher.stop();
  }

  private static void assertBatchSize(final List<Integer> batchSizes, final Integer batchSize) throws InterruptedException {
    final long startTime = System.currentTimeMillis();
    while (batchSizes.isEmpty()) {
      Thread.sleep(100);
      if (System.currentTimeMillis() - startTime > 5000L) {
        fail("Did not receive a batch in 5 seconds.");
      }
    }
    assertEquals(1, batchSizes.size());
    assertEquals(batchSize, batchSizes.get(0));
  }

}
