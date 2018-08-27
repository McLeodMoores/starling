/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.MutableFudgeMsg;

import com.opengamma.id.ExternalId;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;

/**
 * This is intentionally NOT a TestNG test. It should be run manually just to test
 * Redis performance.
 *
 */
public class RedisLastKnownValueStorePerformanceTest {
  public static final int NUM_SECURITIES = 25000;
  public static final int NUM_FIELDS_PER_SECURITY = 5;
  public static final int NUM_CYCLES = 20;
  public static final int NUM_THREADS = 2;
  public static final FudgeContext FUDGE_CONTEXT = OpenGammaFudgeContext.getInstance();
  public static final String REDIS_SERVER = "localhost";
  private RedisLastKnownValueStoreProvider _provider;
  private List<LastKnownValueStore> _valueStores;

  public void constructProvider() {
    final RedisLastKnownValueStoreProvider provider = new RedisLastKnownValueStoreProvider();
    provider.setServer(REDIS_SERVER);
    provider.setWriteThrough(true);
    _provider = provider;
  }

  public void constructValueStores() {
    final List<LastKnownValueStore> valueStores = new LinkedList<>();
    for (int i = 0; i < NUM_SECURITIES; i++) {
      final String idName = "Security-" + i;
      final ExternalId id = ExternalId.of("PerformanceTest", idName);
      final LastKnownValueStore store = _provider.newInstance(id, "Performance Test");
      valueStores.add(store);

      final MutableFudgeMsg msg = FUDGE_CONTEXT.newMessage();
      for (int j = 0; j < NUM_FIELDS_PER_SECURITY; j++) {
        msg.add("Field-" + j, (double) System.nanoTime());
      }
      store.updateFields(msg);
    }
    _valueStores = new CopyOnWriteArrayList<>(valueStores);
  }

  public void oneCycle() {
    final double nanoTime = System.nanoTime();
    final List<LastKnownValueStore> shuffled = new ArrayList<>(_valueStores);
    Collections.shuffle(shuffled);
    for (final LastKnownValueStore store : shuffled) {
      final MutableFudgeMsg msg = FUDGE_CONTEXT.newMessage();
      for (int j = 0; j < NUM_FIELDS_PER_SECURITY; j++) {
        msg.add("Field-" + j, nanoTime);
      }
      store.updateFields(msg);
    }
  }

  public void runTest(final int nThreads) throws Exception {
    final List<Thread> threads = new LinkedList<>();
    for (int i = 0; i < nThreads; i++) {
      final Thread t = new Thread(new Runnable() {
        @Override
        public void run() {
          for (int i = 0; i < NUM_CYCLES; i++) {
            oneCycle();
            //if ((i % 5) == 0) {
              System.out.println("" + i + " cycles done.");
            //}
          }
        }
      });
      t.setName("Perf test worker " + i);
      t.setDaemon(false);
      threads.add(t);
    }

    final long startTime = System.nanoTime();
    for (final Thread t : threads) {
      t.start();
    }
    for (final Thread t : threads) {
      t.join();
    }
    final long endTime = System.nanoTime();
    final long delta = endTime - startTime;
    double totalUpdates = (double) NUM_CYCLES * (double) NUM_FIELDS_PER_SECURITY;
    totalUpdates *= NUM_SECURITIES;
    totalUpdates *= nThreads;
    final double splitTime = delta / totalUpdates;
    System.out.println("........" + splitTime + " nanos per update.");

    final double splitTimeInSec = splitTime * 1.0e-9;
    final double numPerSec = 1.0 / splitTimeInSec;
    System.out.println("........" + numPerSec + " updates per second.");
  }

  /**
   * @param args
   */
  public static void main(final String[] args) throws Exception {
    final RedisLastKnownValueStorePerformanceTest test = new RedisLastKnownValueStorePerformanceTest();
    test.constructProvider();
    test.constructValueStores();

    System.out.println("THREADS : 1");
    test.runTest(1);
    System.out.println("THREADS : 2");
    test.runTest(2);
    System.out.println("THREADS : 3");
    test.runTest(3);
    System.out.println("THREADS : 4");
    test.runTest(4);
  }

}
