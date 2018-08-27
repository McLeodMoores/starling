/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.cache;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.id.UniqueId;
import com.opengamma.transport.DirectFudgeConnection;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.test.TestGroup;

/**
 *
 */
@Test(groups = TestGroup.INTEGRATION)
public class RemoteCacheRequestResponseTest {
  private static final Logger LOGGER = LoggerFactory.getLogger(RemoteCacheRequestResponseTest.class);
  private static final FudgeContext FUDGE_CONTEXT = OpenGammaFudgeContext.getInstance();

  @Test(timeOut = 10000l)
  public void singleThreadSpecLookupDifferentIdentifierValues() {
    final InMemoryViewComputationCacheSource cache = new InMemoryViewComputationCacheSource(FUDGE_CONTEXT);
    final ViewComputationCacheServer server = new ViewComputationCacheServer(cache);
    final DirectFudgeConnection conduit = new DirectFudgeConnection(cache.getFudgeContext());
    final RemoteCacheClient client = new RemoteCacheClient(conduit.getEnd1());
    conduit.connectEnd2(server);
    final IdentifierMap identifierMap = new RemoteIdentifierMap(client);

    final ValueSpecification[] valueSpec = new ValueSpecification[10];
    for (int i = 0; i < valueSpec.length; i++) {
      valueSpec[i] = new ValueSpecification("Test Value", ComputationTargetSpecification.of(UniqueId.of("Kirk", "Value" + i)),
          ValueProperties.with(ValuePropertyNames.FUNCTION, "mockFunctionId").get());
    }
    // Make single value calls
    LOGGER.debug("Begin single value lookup");
    final BitSet seenIds = new BitSet();
    for (final ValueSpecification element : valueSpec) {
      final long id = identifierMap.getIdentifier(element);
      assertTrue(id <= Integer.MAX_VALUE);
      assertFalse(seenIds.get((int) id));
      seenIds.set((int) id);
    }
    LOGGER.debug("End single value lookup");
    // Make a bulk lookup call
    LOGGER.debug("Begin bulk lookup");
    final Map<ValueSpecification, Long> identifiers = identifierMap.getIdentifiers(Arrays.asList(valueSpec));
    assertNotNull(identifiers);
    assertEquals(valueSpec.length, identifiers.size());
    for (final ValueSpecification spec : valueSpec) {
      assertTrue(identifiers.containsKey(spec));
      assertTrue(seenIds.get((int) (long) identifiers.get(spec)));
    }
    LOGGER.debug("End bulk lookup");
  }

  @Test(timeOut = 10000l)
  public void singleThreadLookupDifferentIdentifierValuesRepeated() {
    final InMemoryViewComputationCacheSource cache = new InMemoryViewComputationCacheSource(FUDGE_CONTEXT);
    final ViewComputationCacheServer server = new ViewComputationCacheServer(cache);
    final DirectFudgeConnection conduit = new DirectFudgeConnection(cache.getFudgeContext());
    conduit.connectEnd2(server);
    final RemoteCacheClient client = new RemoteCacheClient(conduit.getEnd1());
    final IdentifierMap identifierMap = new RemoteIdentifierMap(client);

    final Map<String, Long> idsByValueName = new HashMap<>();
    for (int i = 0; i < 10; i++) {
      final String valueName = "Value" + i;
      final ValueSpecification valueSpec = new ValueSpecification("Test Value",
          ComputationTargetSpecification.of(UniqueId.of("Kirk", valueName)), ValueProperties.with(ValuePropertyNames.FUNCTION, "mockFunctionId").get());
      final long id = identifierMap.getIdentifier(valueSpec);
      idsByValueName.put(valueName, id);
    }

    for (int i = 0; i < 10; i++) {
      final String valueName = "Value" + i;
      final ValueSpecification valueSpec = new ValueSpecification("Test Value",
          ComputationTargetSpecification.of(UniqueId.of("Kirk", valueName)), ValueProperties.with(ValuePropertyNames.FUNCTION, "mockFunctionId").get());
      final long id = identifierMap.getIdentifier(valueSpec);
      assertEquals(idsByValueName.get(valueName), new Long(id));
    }
  }

  @Test(timeOut = 30000L)
  public void multiThreadLookupDifferentIdentifierValuesRepeatedSharedClient() throws InterruptedException {
    final InMemoryViewComputationCacheSource cache = new InMemoryViewComputationCacheSource(FUDGE_CONTEXT);
    final ViewComputationCacheServer server = new ViewComputationCacheServer(cache);
    final DirectFudgeConnection conduit1 = new DirectFudgeConnection(cache.getFudgeContext());
    conduit1.connectEnd2(server);
    final DirectFudgeConnection conduit2 = new DirectFudgeConnection(cache.getFudgeContext());
    conduit2.connectEnd2(server);
    final RemoteCacheClient client = new RemoteCacheClient(conduit1.getEnd1(), conduit2.getEnd1());
    final IdentifierMap identifierMap = new RemoteIdentifierMap(client);

    final ConcurrentMap<String, Long> idsByValueName = new ConcurrentHashMap<>();
    final Random rand = new Random();
    final AtomicBoolean failed = new AtomicBoolean(false);
    final List<Thread> threads = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
      final Thread t = new Thread(new Runnable() {
        @Override
        public void run() {
          try {
            for (int j = 0; j < 1000; j++) {
              final int randomValue = rand.nextInt(100);
              final String valueName = "Value" + randomValue;
              final ValueSpecification valueSpec = new ValueSpecification("Test Value",
                  ComputationTargetSpecification.of(UniqueId.of("Kirk", valueName)), ValueProperties.with(ValuePropertyNames.FUNCTION, "mockFunctionId").get());
              final long id = identifierMap.getIdentifier(valueSpec);
              final Long previousValue = idsByValueName.putIfAbsent(valueName, id);
              if (previousValue != null) {
                assertEquals(previousValue, new Long(id));
              }
            }
          } catch (final Exception e) {
            LOGGER.error("Failed", e);
            failed.set(true);
          }
        }
      });
      threads.add(t);
    }
    for (final Thread t : threads) {
      t.start();
    }
    for (final Thread t : threads) {
      t.join();
    }
    assertFalse("One thread failed. Check logs.", failed.get());
  }

  @Test(timeOut = 30000l)
  public void multiThreadLookupDifferentIdentifierValuesRepeatedDifferentClient() throws InterruptedException {
    final InMemoryViewComputationCacheSource cache = new InMemoryViewComputationCacheSource(FUDGE_CONTEXT);
    final ViewComputationCacheServer server = new ViewComputationCacheServer(cache);

    final ConcurrentMap<String, Long> idsByValueName = new ConcurrentHashMap<>();
    final Random rand = new Random();
    final AtomicBoolean failed = new AtomicBoolean(false);
    final List<Thread> threads = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
      final DirectFudgeConnection conduit = new DirectFudgeConnection(cache.getFudgeContext());
      conduit.connectEnd2(server);
      final Thread t = new Thread(new Runnable() {
        @Override
        public void run() {
          final RemoteCacheClient client = new RemoteCacheClient(conduit.getEnd1());
          final IdentifierMap identifierMap = new RemoteIdentifierMap(client);
          try {
            for (int j = 0; j < 1000; j++) {
              final int randomValue = rand.nextInt(100);
              final String valueName = "Value" + randomValue;
              final ValueSpecification valueSpec = new ValueSpecification("Test Value",
                  ComputationTargetSpecification.of(UniqueId.of("Kirk", valueName)), ValueProperties.with(ValuePropertyNames.FUNCTION, "mockFunctionId").get());
              final long id = identifierMap.getIdentifier(valueSpec);
              final Long previousValue = idsByValueName.putIfAbsent(valueName, id);
              if (previousValue != null) {
                assertEquals(previousValue, new Long(id));
              }
            }
          } catch (final Exception e) {
            LOGGER.error("Failed", e);
            failed.set(true);
          }
        }
      });
      threads.add(t);
    }
    for (final Thread t : threads) {
      t.start();
    }
    for (final Thread t : threads) {
      t.join();
    }
    assertFalse("One thread failed. Check logs.", failed.get());
  }

  // @Test(timeout=10000l)
  @Test
  public void singleThreadPutLoad() throws InterruptedException {
    final InMemoryViewComputationCacheSource cache = new InMemoryViewComputationCacheSource(FUDGE_CONTEXT);
    final ViewComputationCacheServer server = new ViewComputationCacheServer(cache);
    final DirectFudgeConnection conduit = new DirectFudgeConnection(cache.getFudgeContext());
    conduit.connectEnd2(server);
    final RemoteCacheClient client = new RemoteCacheClient(conduit.getEnd1());
    final FudgeMessageStore dataStore = new RemoteFudgeMessageStore(client, new ViewComputationCacheKey(UniqueId.of("Test", "ViewCycle1"), "Config1"));

    // Single value
    final MutableFudgeMsg inputValue1 = FUDGE_CONTEXT.newMessage();
    for (int i = 0; i < 32; i++) {
      inputValue1.add(i, Integer.toString(i));
    }
    long identifier1 = 1L;
    dataStore.put(identifier1, inputValue1);

    FudgeMsg outputValue = dataStore.get(identifier1);
    assertNotNull(outputValue);
    assertEquals(inputValue1.getAllFields(), outputValue.getAllFields());

    outputValue = dataStore.get(identifier1 + 1);
    assertNull(outputValue);

    outputValue = dataStore.get(identifier1);
    assertNotNull(outputValue);
    assertEquals(inputValue1.getAllFields(), outputValue.getAllFields());

    // Multiple value
    final MutableFudgeMsg inputValue2 = FUDGE_CONTEXT.newMessage();
    for (int i = 32; i < 64; i++) {
      inputValue2.add(i, Integer.toString(i));
    }
    final Map<Long, FudgeMsg> inputMap = new HashMap<>();
    identifier1++;
    final long identifier2 = identifier1 + 1;
    inputMap.put(identifier1, inputValue1);
    inputMap.put(identifier2, inputValue2);
    dataStore.put(inputMap);

    final Map<Long, FudgeMsg> outputMap = dataStore.get(Arrays.asList(identifier1, identifier2));
    assertEquals(2, outputMap.size());
    assertEquals(inputValue1.getAllFields(), outputMap.get(identifier1).getAllFields());
    assertEquals(inputValue2.getAllFields(), outputMap.get(identifier2).getAllFields());
  }

  @Test(timeOut = 10000l)
  public void singleThreadPutLoadPurgeLoad() throws InterruptedException {
    final InMemoryViewComputationCacheSource cache = new InMemoryViewComputationCacheSource(FUDGE_CONTEXT);
    final ViewComputationCacheServer server = new ViewComputationCacheServer(cache);
    final DirectFudgeConnection conduit = new DirectFudgeConnection(cache.getFudgeContext());
    conduit.connectEnd2(server);
    final RemoteCacheClient client = new RemoteCacheClient(conduit.getEnd1());
    final FudgeMessageStore dataStore = new RemoteFudgeMessageStore(client, new ViewComputationCacheKey(
        UniqueId.of("Test", "ViewCycle1"), "Config1"));
    final MutableFudgeMsg inputValue = FUDGE_CONTEXT.newMessage();
    for (int i = 0; i < 32; i++) {
      inputValue.add(i, Integer.toString(i));
    }
    final long identifier = 1L;
    dataStore.put(identifier, inputValue);

    FudgeMsg outputValue = dataStore.get(identifier);
    assertNotNull(outputValue);
    assertEquals(inputValue.getAllFields(), outputValue.getAllFields());

    dataStore.delete();

    outputValue = dataStore.get(identifier);
    assertNull(outputValue);
  }

}
