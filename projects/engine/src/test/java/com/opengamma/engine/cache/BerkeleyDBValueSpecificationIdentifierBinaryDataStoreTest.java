/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.cache;

import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import com.opengamma.util.monitor.OperationTimer;
import com.opengamma.util.test.TestGroup;
import com.sleepycat.je.Environment;

/**
 *
 */
@Test(groups = TestGroup.INTEGRATION)
public class BerkeleyDBValueSpecificationIdentifierBinaryDataStoreTest {
  private static final Logger LOGGER = LoggerFactory.getLogger(BerkeleyDBValueSpecificationIdentifierBinaryDataStoreTest.class);

  private static Set<File> s_dbDirsToDelete = new HashSet<>();

  protected File createDbDir(final String methodName) {
    final File tmpDir = new File(System.getProperty("java.io.tmpdir"));
    final File dbDir = new File(tmpDir, "BerkeleyDBBinaryDataStore-" + System.currentTimeMillis() + "-" + methodName);
    dbDir.mkdirs();
    s_dbDirsToDelete.add(dbDir);
    return dbDir;
  }

  @AfterClass(alwaysRun = true)
  public static void deleteDbDirs() {
    for (final File f : s_dbDirsToDelete) {
      try {
        LOGGER.info("Deleting temp directory {}", f);
        FileUtils.deleteDirectory(f);
      } catch (final IOException ioe) {
        LOGGER.warn("Unable to recursively delete directory {}", f);
        // Just swallow it.
      }
    }
    s_dbDirsToDelete.clear();
  }

  public void putPerformanceTest() {
    final int numEntries = 5000;
    final int minEntrySize = 50;
    final int maxEntrySize = 1000;
    final Random random = new Random();

    final File dbDir = createDbDir("putPerformanceTest");
    final Environment dbEnvironment = BerkeleyDBViewComputationCacheSource.constructDatabaseEnvironment(dbDir, false);

    final BerkeleyDBBinaryDataStore dataStore = new BerkeleyDBBinaryDataStore(dbEnvironment, "putPerformanceTest");
    dataStore.start();

    final OperationTimer timer = new OperationTimer(LOGGER, "Writing {} entries", numEntries);

    final int randRange = maxEntrySize - minEntrySize;
    for (int i = 0; i < numEntries; i++) {
      final int nBytes = minEntrySize + random.nextInt(randRange);
      final byte[] bytes = new byte[nBytes];
      random.nextBytes(bytes);
      dataStore.put(i, bytes);
    }

    final long numMillis = timer.finished();

    final double msPerPut = (double) numMillis / (double) numEntries;
    final double putsPerSecond = 1000.0 / msPerPut;

    LOGGER.info("for {} entries, {} ms/put, {} puts/sec", new Object[] {numEntries, msPerPut, putsPerSecond});

    dataStore.delete();
    dataStore.stop();
    dbEnvironment.close();
  }

  public void getPerformanceTest() {
    final int numEntries = 500;
    final int numCycles = 5;
    final int numGets = numCycles * numEntries;
    final int minEntrySize = 50;
    final int maxEntrySize = 1000;
    final Random random = new Random();

    final File dbDir = createDbDir("getPerformanceTest");
    final Environment dbEnvironment = BerkeleyDBViewComputationCacheSource.constructDatabaseEnvironment(dbDir, false);

    final BerkeleyDBBinaryDataStore dataStore = new BerkeleyDBBinaryDataStore(dbEnvironment, "getPerformanceTest");
    dataStore.start();

    final int randRange = maxEntrySize - minEntrySize;
    for (int i = 0; i < numEntries; i++) {
      final int nBytes = minEntrySize + random.nextInt(randRange);
      final byte[] bytes = new byte[nBytes];
      random.nextBytes(bytes);
      dataStore.put(i, bytes);
    }

    final OperationTimer timer = new OperationTimer(LOGGER, "Loading {} entries", numGets);
    for (int j = 0; j < numCycles; j++) {
      for (int i = 0; i < numEntries; i++) {
        final byte[] data = dataStore.get(i);
        assertNotNull(data);
        assertTrue(data.length >= minEntrySize);
        assertTrue(data.length <= maxEntrySize);
      }
    }

    final long numMillis = timer.finished();

    final double msPerGet = (double) numMillis / (double) numGets;
    final double getsPerSecond = 1000.0 / msPerGet;

    LOGGER.info("for {} gets, {} ms/get, {} gets/sec", new Object[] {numGets, msPerGet, getsPerSecond});

    dataStore.delete();
    dataStore.stop();
    dbEnvironment.close();
  }

  public void parallelPutGetTest() throws InterruptedException {
    final int numEntries = 5000;
    final int numCycles = 1;
    final int numGets = numCycles * numEntries;
    final Random random = new Random();

    final File dbDir = createDbDir("parallelPutGetTest");
    final Environment dbEnvironment = BerkeleyDBViewComputationCacheSource.constructDatabaseEnvironment(dbDir, false);

    final BerkeleyDBBinaryDataStore dataStore = new BerkeleyDBBinaryDataStore(dbEnvironment, "parallelPutGetTest");
    dataStore.start();

    final AtomicLong currentMaxIdentifier = new AtomicLong(0L);
    final byte[] bytes = new byte[100];
    random.nextBytes(bytes);
    final Thread tPut = new Thread(new Runnable() {
      @Override
      public void run() {
        final OperationTimer timer = new OperationTimer(LOGGER, "Putting {} entries", numEntries);
        for (int i = 0; i < numEntries; i++) {
          random.nextBytes(bytes);
          dataStore.put(i, bytes);
          currentMaxIdentifier.set(i);
        }
        final long numMillis = timer.finished();

        final double msPerPut = (double) numMillis / (double) numGets;
        final double putsPerSecond = 1000.0 / msPerPut;

        LOGGER.info("for {} puts, {} ms/put, {} puts/sec", new Object[] {numEntries, msPerPut, putsPerSecond});
      }

    }, "Putter");

    class GetRunner implements Runnable {
      @Override
      public void run() {
        final OperationTimer timer = new OperationTimer(LOGGER, "Getting {} entries", numGets);
        for (int i = 0; i < numGets; i++) {
          final int maxIdentifier = (int) currentMaxIdentifier.get();
          final long actualIdentifier = random.nextInt(maxIdentifier);
          dataStore.get(actualIdentifier);
        }
        final long numMillis = timer.finished();

        final double msPerGet = (double) numMillis / (double) numGets;
        final double getsPerSecond = 1000.0 / msPerGet;

        LOGGER.info("for {} gets, {} ms/get, {} gets/sec", new Object[] {numGets, msPerGet, getsPerSecond});
      }
    };
    final Thread tGet1 = new Thread(new GetRunner(), "getter-1");
    final Thread tGet2 = new Thread(new GetRunner(), "getter-2");
    //Thread tGet3 = new Thread(new GetRunner(), "getter-3");
    //Thread tGet4 = new Thread(new GetRunner(), "getter-4");
    //Thread tGet5 = new Thread(new GetRunner(), "getter-5");

    tPut.start();
    Thread.sleep(5L);
    tGet1.start();
    tGet2.start();
    //tGet3.start();
    //tGet4.start();
    //tGet5.start();

    tPut.join();
    tGet1.join();
    tGet2.join();
    //tGet3.join();
    //tGet4.join();
    //tGet5.join();

    dataStore.delete();
    dataStore.stop();
    dbEnvironment.close();
  }
}
