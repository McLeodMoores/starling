/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.replay;

import static com.opengamma.bbg.replay.BloombergTick.BUID_KEY;
import static com.opengamma.bbg.replay.BloombergTick.FIELDS_KEY;
import static com.opengamma.bbg.replay.BloombergTick.RECEIVED_TS_KEY;
import static com.opengamma.bbg.replay.BloombergTick.SECURITY_KEY;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.lang.time.StopWatch;
import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.wire.FudgeMsgWriter;
import org.fudgemsg.wire.FudgeSize;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Clock;
import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.ImmutableMap;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.TerminatableJob;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;

/**
 *
 */
public class BloombergTickWriter extends TerminatableJob {

  /** Logger. */
  private static final Logger LOGGER = LoggerFactory.getLogger(BloombergTickWriter.class);
  //interval in millis
  private static final long DEFAULT_REPORT_INTERVAL = 10000L;
  /**
   * The default name for the file that contains all ticks.
   */
  public static final String ALL_TICKS_FILENAME = "allTicks.dat";

  private final FudgeContext _fudgeContext;
  private final ConcurrentMap<String, BlockingQueue<FudgeMsg>> _securityMapQueue = new ConcurrentHashMap<>();
  private final BlockingQueue<FudgeMsg> _allTicksQueue;
  private final Map<String, String> _ticker2Buid;
  private final String _rootDir;
  private int _nTicks;
  private int _nWrites;
  private int _nBlocks;
  private final StopWatch _stopWatch = new StopWatch();
  private final long _reportInterval;
  private final StorageMode _storageMode;

  public BloombergTickWriter(final BlockingQueue<FudgeMsg> allTicksQueue, final Map<String, String> ticker2Buid,
      final String rootDir, final StorageMode storageMode, final BloombergTicksCollector ticksGenerator) {
    this(OpenGammaFudgeContext.getInstance(), allTicksQueue, ticker2Buid, rootDir, storageMode);
  }

  public BloombergTickWriter(final FudgeContext fudgeContext, final BlockingQueue<FudgeMsg> allTicksQueue, final Map<String, String> ticker2Buid,
      final String rootDir, final StorageMode storageMode) {
    this(fudgeContext, allTicksQueue, ticker2Buid, rootDir, storageMode, DEFAULT_REPORT_INTERVAL);
  }

  public BloombergTickWriter(
      final FudgeContext fudgeContext,
      final BlockingQueue<FudgeMsg> allTicksQueue,
      final Map<String, String> ticker2Buid,
      final String rootDir,
      final StorageMode storageMode,
      final long reportInterval) {
    ArgumentChecker.notNull(fudgeContext, "fudgeContext");
    ArgumentChecker.notNull(allTicksQueue, "allTicksQueue");
    ArgumentChecker.notNull(ticker2Buid, "ticker2Buid");
    ArgumentChecker.notNull(rootDir, "rootDir");
    ArgumentChecker.notNull(storageMode, "storageMode");

    _fudgeContext = fudgeContext;
    _allTicksQueue = allTicksQueue;
    _rootDir = rootDir;
    _stopWatch.start();
    _reportInterval = reportInterval;
    _storageMode = storageMode;
    _ticker2Buid = ImmutableMap.<String, String>builder().putAll(ticker2Buid).build();
    LOGGER.info("BloombergTickWriter started in {} mode writing to {}", _storageMode, _rootDir);
  }

  private FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

  @Override
  protected void runOneCycle() {
    // Andrew 2010-01-27 -- If the queue is empty, this will loop round in a big no-op.
    // Checking for this and including a blocking 'take' on the queue seemed to result in lower throughput.
    // This might not be the case outside of the high load test case where data arrives at high speed and the blocking is a rarity.
    List<FudgeMsg> ticks = new ArrayList<>(_allTicksQueue.size());
    _allTicksQueue.drainTo(ticks);
    final FudgeMsg msg = writeAllTicksToSingleFile(ticks);
    if (_storageMode == StorageMode.MULTI) {
      if (msg != null && BloombergTickReplayUtils.isTerminateMsg(msg)) {
        ticks.remove(msg);
      }
      buildSecurityMapQueue(ticks);
      writeOutSecurityMapQueue();
    }
    if (LOGGER.isDebugEnabled()) {
      writeReport();
    }
    if (msg != null && BloombergTickReplayUtils.isTerminateMsg(msg)) {
      LOGGER.info("received terminate message, ..terminating");
      terminate();
    }
    ticks.clear();
    ticks = null;
  }

  /**
   * @param ticks
   */
  private void buildSecurityMapQueue(final List<FudgeMsg> ticks) {
    for (final FudgeMsg fudgeMsg : ticks) {
      final String securityDes = fudgeMsg.getString(SECURITY_KEY);
      if (_securityMapQueue.containsKey(securityDes)) {
        final BlockingQueue<FudgeMsg> queue = _securityMapQueue.get(securityDes);
        try {
          queue.put(fudgeMsg);
        } catch (final InterruptedException e) {
          Thread.interrupted();
          LOGGER.warn("interrupted from putting message on queue");
        }
      } else {
        final LinkedBlockingQueue<FudgeMsg> queue = new LinkedBlockingQueue<>();
        try {
          queue.put(fudgeMsg);
        } catch (final InterruptedException e) {
          Thread.interrupted();
          LOGGER.warn("interrupted from putting message on queue");
        }
        _securityMapQueue.put(securityDes, queue);
      }
    }
  }

  private void writeSecurityTicks(final File dir, final String buid, final String securityDes, final List<FudgeMsg> tickMsgList) {
    if (tickMsgList.isEmpty()) {
      return;
    }
    LOGGER.debug("writing {} messages for {}:{}", new Object[]{tickMsgList.size(), securityDes, buid});
    //sort ticks per time
    final Map<String, List<FudgeMsg>> fileTicksMap = new HashMap<>();
    for (final FudgeMsg tickMsg : tickMsgList) {
      final String filename = makeFileName(tickMsg);
      List<FudgeMsg> fileTicks = fileTicksMap.get(filename);
      if (fileTicks == null) {
        fileTicks = new ArrayList<>();
        fileTicks.add(tickMsg);
        fileTicksMap.put(filename, fileTicks);
      } else {
        fileTicks.add(tickMsg);
      }
    }
    for (final Entry<String, List<FudgeMsg>> entry : fileTicksMap.entrySet()) {
      final String filename = entry.getKey();
      final List<FudgeMsg> ticks = entry.getValue();
      final String fullPath = new StringBuilder(dir.getAbsolutePath()).append(File.separator).append(filename).toString();
      FileOutputStream fos = null;

      try {
        fos = new FileOutputStream(fullPath, true);
        final BufferedOutputStream bos = new BufferedOutputStream(fos, 4096);
        final FudgeMsgWriter fmsw = getFudgeContext().createMessageWriter(bos);
        for (final FudgeMsg tick : ticks) {
          _nBlocks += FudgeSize.calculateMessageSize(tick);
          fmsw.writeMessage(tick, 0);
          fmsw.flush();
        }
        _nWrites++;
      } catch (final FileNotFoundException e) {
        LOGGER.warn("cannot open file {} for writing", fullPath);
        throw new OpenGammaRuntimeException("Cannot open " + fullPath + " for writing", e);
      } finally {
        if (fos != null) {
          try {
            fos.close();
          } catch (final IOException e) {
            LOGGER.warn("cannot close file {}", fullPath);
          }
        }
      }
    }
    _nTicks += tickMsgList.size();
  }

  private FudgeMsg writeAllTicksToSingleFile(final List<FudgeMsg> ticks) {
    if (ticks.isEmpty()) {
      return null;
    }
    FudgeMsg terminateMsg = null;
    final File fullPath = getTicksFile();
    FileOutputStream fos = null;
    try {
      fos = new FileOutputStream(fullPath, true);
      final BufferedOutputStream bos = new BufferedOutputStream(fos, 4096);
      final FudgeMsgWriter fmsw = getFudgeContext().createMessageWriter(bos);
      for (final FudgeMsg tick : ticks) {
        if (BloombergTickReplayUtils.isTerminateMsg(tick)) {
          terminateMsg = tick;
          continue;
        }
        _nBlocks += FudgeSize.calculateMessageSize(tick);
        final String securityDes = tick.getString(SECURITY_KEY);
        final String buid = getBloombergBUID(securityDes);
        ((MutableFudgeMsg) tick).add(BUID_KEY, buid);
        fmsw.writeMessage(tick, 0);
        fmsw.flush();
      }
      _nWrites++;
    } catch (final FileNotFoundException e) {
      LOGGER.warn("cannot open file {} for writing", fullPath);
      throw new OpenGammaRuntimeException("Cannot open file " + fullPath + " for writing", e);
    } finally {
      if (fos != null) {
        try {
          fos.close();
        } catch (final IOException e) {
          LOGGER.warn("cannot close {}", fullPath);
        }
      }

    }
    _nTicks += ticks.size();
    return terminateMsg;
  }

  private File getTicksFile() {
    final String baseDirectory = makeBaseDirectoryName();
    final File dir = new File(baseDirectory);
    if (!dir.exists()) {
      createDirectory(dir);
    }
    return new File(new StringBuilder().append(baseDirectory).append(File.separator).append(ALL_TICKS_FILENAME).toString());
  }

  /**
   * @return
   */
  private String makeBaseDirectoryName() {
    final Clock clock = Clock.systemUTC();
    final LocalDate today = LocalDate.now(clock);
    final StringBuilder buf = new StringBuilder();
    buf.append(_rootDir).append(File.separator);
    final int year = today.getYear();
    if (year < 10) {
      buf.append("0").append(year);
    } else {
      buf.append(year);
    }
    buf.append(File.separator);
    final int month = today.getMonthValue();
    if (month < 10) {
      buf.append("0").append(month);
    } else {
      buf.append(month);
    }
    buf.append(File.separator);
    final int dayOfMonth = today.getDayOfMonth();
    if (dayOfMonth < 10) {
      buf.append("0").append(dayOfMonth);
    } else {
      buf.append(dayOfMonth);
    }
    return buf.toString();
  }

  /**
   *
   */
  private void writeOutSecurityMapQueue() {
    for (final Entry<String, BlockingQueue<FudgeMsg>> entry : _securityMapQueue.entrySet()) {
      final String security = entry.getKey();
      final BlockingQueue<FudgeMsg> queue = entry.getValue();
      if (queue.isEmpty()) {
        continue;
      }
      List<FudgeMsg> tickMsgList = new ArrayList<>(queue.size());
      queue.drainTo(tickMsgList);
      final String buid = getBloombergBUID(security);

      //get first message
      final FudgeMsg tickMsg = tickMsgList.get(0);
      final Long epochMillis = tickMsg.getLong(RECEIVED_TS_KEY);

      final File dir = buildSecurityDirectory(buid, epochMillis);
      if (!dir.exists()) {
        createDirectory(dir);
      }
      writeSecurityTicks(dir, buid, security, tickMsgList);
      tickMsgList.clear();
      tickMsgList = null;
    }
  }

  /**
   *
   */
  private void writeReport() {
    LOGGER.debug("writing reports");
    _stopWatch.suspend();
    final long time = _stopWatch.getTime();
    if (time >= _reportInterval) {
      double result = (double) _nTicks / (double) time * 1000.;
      LOGGER.debug("ticks {}/s", result);
      result = (double) _nWrites / (double) time * 1000.;
      LOGGER.debug("fileOperations {}/s", result);
      result = (double) _nBlocks / (double) _nWrites;
      LOGGER.debug("average blocks {}bytes", result);
      _nWrites = 0;
      _nTicks = 0;
      _nBlocks = 0;
      _stopWatch.reset();
      _stopWatch.start();
    } else {
      _stopWatch.resume();
    }
  }

  /**
   * @param buid
   * @param tickMsgList
   * @return
   */
  private File buildSecurityDirectory(final String buid, final long receivedTS) {
    final Instant instant = Instant.ofEpochMilli(receivedTS);
    final ZonedDateTime dateTime = ZonedDateTime.ofInstant(instant, ZoneOffset.UTC);
    final LocalDate today = dateTime.toLocalDate();
    final StringBuilder buf = new StringBuilder();
    buf.append(_rootDir).append(File.separator);
    buf.append(buid).append(File.separator).append(today.getYear()).append(File.separator);
    final int month = today.getMonthValue();
    if (month < 10) {
      buf.append("0").append(month);
    } else {
      buf.append(month);
    }
    buf.append(File.separator);
    final int dayOfMonth = today.getDayOfMonth();
    if (dayOfMonth < 10) {
      buf.append("0").append(dayOfMonth);
    } else {
      buf.append(dayOfMonth);
    }
    buf.append(File.separator);
    return new File(buf.toString());
  }

  private String getBloombergBUID(final String securityDes) {
    String buid = _ticker2Buid.get(securityDes);
    if (buid == null) {
      buid = securityDes;
    }
    return buid;
  }

  /**
   * @param tickMsg
   * @return
   */
  private String makeFileName(final FudgeMsg tickMsg) {
    String result = null;
    final FudgeMsg bbgTickAsMsg = tickMsg.getMessage(FIELDS_KEY);
    String eventTime = bbgTickAsMsg.getString("EVENT_TIME");
    if (eventTime == null) {
      eventTime = bbgTickAsMsg.getString("TIME");
    }
    if (eventTime == null) {
      //use received time stamp
      result = makeFileNameFromReceivedTimeStamp(tickMsg);
    } else {
      result = makeFileNameFromEventTime(eventTime);
      // if time/eventTime not in expected format
      if (result == null) {
        result = makeFileNameFromReceivedTimeStamp(tickMsg);
      }
    }
    return result;
  }

  /**
   * @param tickMsg
   * @return
   */
  private String makeFileNameFromReceivedTimeStamp(final FudgeMsg tickMsg) {
    String result = null;
    //LOGGER.warn("cannot determine event time in msg {}, using received timestamp", tickMsg); // Andrew - uncomment before checking back in
    final Long epochMillis = tickMsg.getLong(RECEIVED_TS_KEY);
    final Instant instant = Instant.ofEpochMilli(epochMillis);
    final ZonedDateTime dateTime = ZonedDateTime.ofInstant(instant, ZoneOffset.UTC);
    final int hourOfDay = dateTime.getHour();
    if (hourOfDay < 10) {
      result = new StringBuilder("0").append(hourOfDay).toString();
    } else {
      result = String.valueOf(hourOfDay);
    }
    return result;
  }

  /**
   *
   * @param eventTime expected time like 11:44:18.000+00:00
   * @return
   */
  private String makeFileNameFromEventTime(final String eventTime) {
    String result = null;
    final String[] split = eventTime.split(":");
    if (split.length == 4) {
      result = split[0];
    } else {
      LOGGER.warn("time {} is not in expected format", eventTime);
    }
    return result;
  }

  /**
   * @param dir
   */
  private void createDirectory(final File dir) {
    if (!dir.mkdirs()) {
      LOGGER.warn("cannot create {}", dir);
      throw new OpenGammaRuntimeException("cannot create directory " + dir);
    }
  }

  /**
   * @return the nTicks
   */
  public int getNTicks() {
    return _nTicks;
  }

  /**
   * @return the nWrites
   */
  public int getNWrites() {
    return _nWrites;
  }

  /**
   * @return the nBlocks
   */
  public int getNBlocks() {
    return _nBlocks;
  }

}
