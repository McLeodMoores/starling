/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.replay;

import java.util.concurrent.BlockingQueue;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.bbg.replay.BloombergTicksReplayer.Mode;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.TerminatableJob;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;

/**
 *
 */
public class TicksPlayerJob extends TerminatableJob {

  /** Logger/ */
  private static final Logger LOGGER = LoggerFactory.getLogger(TicksPlayerJob.class);

  private final BlockingQueue<FudgeMsg> _ticksQueue;
  private final BloombergTickReceiver _tickReceiver;
  private final Mode _mode;
  private final Thread _ticksLoaderThread;

  public TicksPlayerJob(final BlockingQueue<FudgeMsg> ticksQueue, final BloombergTickReceiver tickReceiver, final Mode mode, final Thread ticksLoaderThread) {
    ArgumentChecker.notNull(ticksQueue, "ticksQueue");
    ArgumentChecker.notNull(tickReceiver, "tickReceiver");
    ArgumentChecker.notNull(mode, "mode");
    ArgumentChecker.notNull(ticksLoaderThread, "ticksLoaderThread");
    _ticksQueue = ticksQueue;
    _tickReceiver = tickReceiver;
    _mode = mode;
    _ticksLoaderThread = ticksLoaderThread;
  }

  @Override
  public void terminate() {
    LOGGER.debug("ticksPlayer terminating...");
    super.terminate();
  }

  @Override
  protected void runOneCycle() {
    if (!_ticksLoaderThread.isAlive() && _ticksQueue.isEmpty()) {
      terminate();
    } else {
      playNextTick();
    }
  }

  /**
   * @param nextTick
   *
   */
  private void playNextTick() {
    final FudgeDeserializer deserializer = new FudgeDeserializer(OpenGammaFudgeContext.getInstance());
    switch (_mode) {
      case ORIGINAL_LATENCY:
        BloombergTick currentTick = null;;
        try {
          final FudgeMsg msg = _ticksQueue.take();
          if (msg != null && BloombergTickReplayUtils.isTerminateMsg(msg)) {
            LOGGER.debug("received terminate message");
            terminate();
            return;
          }
          currentTick = BloombergTick.fromFudgeMsg(deserializer, msg);
          final long ts1 = System.currentTimeMillis();
          _tickReceiver.tickReceived(currentTick);
          final long ts2 = System.currentTimeMillis();
          final FudgeMsg nextMsg = _ticksQueue.peek();
          if (nextMsg != null && !BloombergTickReplayUtils.isTerminateMsg(nextMsg)) {
            final BloombergTick nextTick = BloombergTick.fromFudgeMsg(deserializer, nextMsg);
            final long tickLatency = nextTick.getReceivedTS() - currentTick.getReceivedTS();
            final long sleepTime = tickLatency - (ts2 - ts1);
            LOGGER.debug("sleeping for {}ms,", sleepTime);
            if (sleepTime > 0) {
              try {
                Thread.sleep(sleepTime);
              } catch (final InterruptedException e) {
                Thread.interrupted();
                LOGGER.warn("interrupted from keeping time difference between ticks");
              }
            }
          }
        } catch (final InterruptedException e1) {
          Thread.interrupted();
          LOGGER.warn("interrupted while waiting to read ticks to play");
        }
        break;
      case AS_FAST_AS_POSSIBLE:
        BloombergTick tick = null;
        try {
          final FudgeMsg msg = _ticksQueue.take();
          if (msg != null && BloombergTickReplayUtils.isTerminateMsg(msg)) {
            LOGGER.debug("received terminate message");
            terminate();
            return;
          }
          tick = BloombergTick.fromFudgeMsg(deserializer, msg);
          _tickReceiver.tickReceived(tick);
        } catch (final InterruptedException e) {
          Thread.interrupted();
          LOGGER.warn("interrupted while waiting to read ticks to play");
        }
        break;
      default:
        break;
    }
  }
}
