/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.id;

import java.lang.ref.PhantomReference;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.MapMaker;
import com.google.common.collect.Sets;

/**
 * Utility class for working with {@link VersionCorrection} instances.
 */
public final class VersionCorrectionUtils {

  private static final Logger LOGGER = LoggerFactory.getLogger(VersionCorrectionUtils.class);

  /**
   * Listener for locking events.
   */
  public interface VersionCorrectionLockListener {

    /**
     * Called when the last lock on a version/correction pair is released.
     * 
     * @param unlocked the version/correction pair unlocked
     * @param locked the version/correction pairs still locked
     */
    void versionCorrectionUnlocked(VersionCorrection unlocked, Collection<VersionCorrection> locked);

  }

  private static final Map<VersionCorrection, AtomicInteger> LOCKS = new HashMap<VersionCorrection, AtomicInteger>();

  private static final Set<VersionCorrectionLockListener> LISTENERS = Sets.newSetFromMap(new MapMaker().weakKeys().<VersionCorrectionLockListener, Boolean>makeMap());

  private static final Map<Reference<Object>, VersionCorrection> AUTO_LOCKS = new ConcurrentHashMap<Reference<Object>, VersionCorrection>();

  private static final ReferenceQueue<Object> AUTO_UNLOCKS = new ReferenceQueue<Object>();

  /**
   * Prevents instantiation.
   */
  private VersionCorrectionUtils() {
  }

  /**
   * Acquires a lock on a version/correction pair. It is possible for other threads to determine whether there are any outstanding locks, or to execute actions when the last lock is released. Must be
   * paired with a call to {@link #unlock}.
   * 
   * @param versionCorrection the version/correction pair to lock, not null
   */
  public static void lock(final VersionCorrection versionCorrection) {
    synchronized (LOCKS) {
      LOGGER.info("Acquiring lock on {}", versionCorrection);
      AtomicInteger locked = LOCKS.get(versionCorrection);
      if (locked == null) {
        locked = new AtomicInteger(1);
        LOCKS.put(versionCorrection, locked);
        LOGGER.debug("First lock acquired on {}", versionCorrection);
      } else {
        final int count = locked.incrementAndGet();
        LOGGER.debug("Lock {} acquired on {}", count);
      }
    }
  }

  /**
   * Acquires a lock on a version/correction pair for the lifetime of the monitor object. It is possible for other threads to determine whether there are any outstanding locks, or to execute actions
   * when the last lock is released. Must be paired with a call to {@link #unlock}.
   * 
   * @param versionCorrection the version/correction pair to lock, not null
   * @param monitor the monitor object - the lock will be released when this falls out of scope, not null
   */
  public static void lockForLifetime(VersionCorrection versionCorrection, final Object monitor) {
    lock(versionCorrection);
    AUTO_LOCKS.put(new PhantomReference<Object>(monitor, AUTO_UNLOCKS), versionCorrection);
    Reference<? extends Object> ref = AUTO_UNLOCKS.poll();
    while (ref != null) {
      versionCorrection = AUTO_LOCKS.remove(ref);
      if (versionCorrection != null) {
        unlock(versionCorrection);
      }
      ref = AUTO_UNLOCKS.poll();
    }
  }

  /**
   * Releases a lock on a version/correction pair. It is possible for other threads to determine whether there are any outstanding locks, or to execute actions when the last lock is released. Must be
   * paired with a call to {@link #unlock}.
   * 
   * @param versionCorrection the version/correction pair to lock, not null
   */
  public static void unlock(final VersionCorrection versionCorrection) {
    final Set<VersionCorrection> remaining;
    synchronized (LOCKS) {
      LOGGER.info("Releasing lock on {}", versionCorrection);
      AtomicInteger locked = LOCKS.get(versionCorrection);
      if (locked == null) {
        LOGGER.warn("{} not locked", versionCorrection);
        throw new IllegalStateException();
      }
      final int count = locked.decrementAndGet();
      if (count > 0) {
        LOGGER.debug("Released lock on {}, {} remaining", versionCorrection, count);
        return;
      }
      assert count == 0;
      LOGGER.debug("Last lock on {} released", versionCorrection);
      LOCKS.remove(versionCorrection);
      remaining = new HashSet<VersionCorrection>(LOCKS.keySet());
    }
    for (VersionCorrectionLockListener listener : LISTENERS) {
      listener.versionCorrectionUnlocked(versionCorrection, remaining);
    }
  }

  public static void addVersionCorrectionLockListener(final VersionCorrectionLockListener listener) {
    LISTENERS.add(listener);
  }

  public static void removeVersionCorrectionLockListener(final VersionCorrectionLockListener listener) {
    LISTENERS.remove(listener);
  }

}
