/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.execution;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.threeten.bp.Instant;

import com.opengamma.util.ArgumentChecker;

/**
 * Provides a finite sequence of view cycles.
 */
public class ArbitraryViewCycleExecutionSequence extends MergingViewCycleExecutionSequence {

  /**
   * Node in a singly linked list of cycles. This makes the copy operation cheaper.
   */
  private static final class Cycle {

    private final ViewCycleExecutionOptions _options;

    private Cycle _next;

    public Cycle(final ViewCycleExecutionOptions options) {
      _options = options;
    }

    public ViewCycleExecutionOptions getOptions() {
      return _options;
    }

    public void setNext(final Cycle next) {
      _next = next;
    }

    public Cycle getNext() {
      return _next;
    }

  }

  private Cycle _executionSequenceHead;
  private int _size;

  public ArbitraryViewCycleExecutionSequence(final Collection<ViewCycleExecutionOptions> executionSequence) {
    ArgumentChecker.notNull(executionSequence, "executionSequence");
    final Iterator<ViewCycleExecutionOptions> itr = executionSequence.iterator();
    if (itr.hasNext()) {
      _size = 1;
      _executionSequenceHead = new Cycle(itr.next());
      Cycle tail = _executionSequenceHead;
      while (itr.hasNext()) {
        final Cycle next = new Cycle(itr.next());
        tail.setNext(next);
        tail = next;
        _size++;
      }
    }
  }

  protected ArbitraryViewCycleExecutionSequence(final ArbitraryViewCycleExecutionSequence copyFrom) {
    _executionSequenceHead = copyFrom._executionSequenceHead;
    _size = copyFrom._size;
  }

  /**
   * Gets a sequence for a single cycle which relies on default cycle execution options.
   *
   * @return the sequence, not null
   */
  public static ArbitraryViewCycleExecutionSequence single() {
    return new ArbitraryViewCycleExecutionSequence(Collections.singletonList(new ViewCycleExecutionOptions()));
  }

  /**
   * Gets a sequence for a single cycle.
   *
   * @param executionOptions the execution options for the single cycle, not null
   * @return the sequence, not null
   */
  public static ArbitraryViewCycleExecutionSequence single(final ViewCycleExecutionOptions executionOptions) {
    return new ArbitraryViewCycleExecutionSequence(Collections.singletonList(executionOptions));
  }

  /**
   * Gets a sequence for a collection of valuation times.
   *
   * @param valuationTimeProviders the valuation times, not null
   * @return the sequence, not null
   */
  public static ArbitraryViewCycleExecutionSequence of(final Instant... valuationTimeProviders) {
    return of(Arrays.asList(valuationTimeProviders));
  }

  /**
   * Gets a sequence for a collection of valuation times.
   *
   * @param valuationTimeProviders the valuation times, not null
   * @return the sequence, not null
   */
  public static ArbitraryViewCycleExecutionSequence of(final Collection<Instant> valuationTimeProviders) {
    ArgumentChecker.notNull(valuationTimeProviders, "valuationTimeProviders");
    final List<ViewCycleExecutionOptions> executionSequence = new ArrayList<>(valuationTimeProviders.size());
    final ViewCycleExecutionOptions.Builder builder = ViewCycleExecutionOptions.builder();
    for (final Instant valuationTimeProvider : valuationTimeProviders) {
      final ViewCycleExecutionOptions options = builder.setValuationTime(valuationTimeProvider).create();
      executionSequence.add(options);
    }
    return new ArbitraryViewCycleExecutionSequence(executionSequence);
  }

  /**
   * Gets a sequence for a collection of cycles.
   *
   * @param executionSequence the sequence, not null
   * @return the sequence, not null
   */
  public static ArbitraryViewCycleExecutionSequence of(final ViewCycleExecutionOptions... executionSequence) {
    return new ArbitraryViewCycleExecutionSequence(Arrays.asList(executionSequence));
  }

  public List<ViewCycleExecutionOptions> getRemainingSequence() {
    final List<ViewCycleExecutionOptions> result = new ArrayList<>(_size);
    Cycle itr = _executionSequenceHead;
    while (itr != null) {
      result.add(itr.getOptions());
      itr = itr.getNext();
    }
    return result;
  }

  @Override
  public ViewCycleExecutionOptions poll(final ViewCycleExecutionOptions defaultExecutionOptions) {
    final Cycle head = _executionSequenceHead;
    if (head != null) {
      _executionSequenceHead = head.getNext();
      _size--;
      return merge(head.getOptions(), defaultExecutionOptions);
    } else {
      return null;
    }
  }

  @Override
  public boolean isEmpty() {
    return _executionSequenceHead == null;
  }

  @Override
  public int estimateRemaining() {
    return _size;
  }

  @Override
  public ViewCycleExecutionSequence copy() {
    return new ArbitraryViewCycleExecutionSequence(this);
  }

}
