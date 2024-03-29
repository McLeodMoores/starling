/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.replay;

import static com.opengamma.bbg.replay.BloombergTick.RECEIVED_TS_KEY;
import static com.opengamma.bbg.replay.BloombergTick.SECURITY_KEY;

import java.util.List;
import java.util.Random;
import java.util.concurrent.BlockingQueue;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Clock;
import org.threeten.bp.Instant;

import com.opengamma.bbg.test.BloombergTestUtils;
import com.opengamma.util.TerminatableJob;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;

/**
 * A job to generate random ticks.
 */
public class RandomTicksGeneratorJob extends TerminatableJob {

  /** Logger. */
  private static final Logger LOGGER = LoggerFactory.getLogger(RandomTicksGeneratorJob.class);
  /**
   * The maximum message size.
   */
  private static final int MAX_MESSAGE_SIZE = 5;
  /**
   * A random seed.
   */
  public static final long RANDOM_SEED = 100L;

  /**
   * The list of required securities.
   */
  private final List<String> _securities;
  /**
   * The queue of messages.
   */
  private final BlockingQueue<FudgeMsg> _writerQueue;
  /**
   * The random number generator.
   */
  private final Random _valueGenerator = new Random(RANDOM_SEED);

  /**
   * Message size generator
   */
  private final Random _messageSizeGenerator = new Random();

  /**
   * Creates a job for a list of securities.
   *
   * @param securities  the securities to, not null
   * @param writerQueue  the queue to use, not null
   */
  public RandomTicksGeneratorJob(final List<String> securities, final BlockingQueue<FudgeMsg> writerQueue) {
    super();
    _securities = securities;
    _writerQueue = writerQueue;
  }

  @Override
  public void terminate() {
    LOGGER.debug("terminating ticksGeneratorJob");
    super.terminate();
  }

  @Override
  protected void runOneCycle() {
    LOGGER.debug("queueSize {} ", _writerQueue.size());
    for (final String security : _securities) {
      final int msgSize = _messageSizeGenerator.nextInt(MAX_MESSAGE_SIZE);
      for (int i = 0; i < msgSize; i++) {
        try {
          final MutableFudgeMsg msg = getRandomMessage();
          final Instant instant = Clock.systemUTC().instant();
          final long epochMillis = instant.toEpochMilli();
          msg.add(RECEIVED_TS_KEY, epochMillis);
          msg.add(SECURITY_KEY, security);
          LOGGER.debug("generating {}", msg);
          _writerQueue.put(msg);
        } catch (final InterruptedException e) {
          Thread.interrupted();
          LOGGER.warn("interrupted exception while putting ticks message on queue");
        }
      }
    }

  }

  private MutableFudgeMsg getRandomMessage() {
    return BloombergTestUtils.makeRandomStandardTick(_valueGenerator, OpenGammaFudgeContext.getInstance());
  }

}
