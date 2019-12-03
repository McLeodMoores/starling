/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.function.dsl.properties;

import com.opengamma.lambdava.streams.Stream;
import com.opengamma.lambdava.streams.StreamI;

/**
 * Properties.
 */
public final class RecordingValueProperties {

  private StreamI<ValuePropertiesModifier> _recordedValueProperties = Stream.empty();
  private final String _copiedFrom;

  private RecordingValueProperties(final String copiedFrom) {
    _copiedFrom = copiedFrom;
  }

  public static RecordingValueProperties copyFrom(final String copiedFrom) {
    return new RecordingValueProperties(copiedFrom);
  }

  public static RecordingValueProperties desiredValue() {
    return new RecordingValueProperties(null);
  }

  public String getCopiedFrom() {
    return _copiedFrom;
  }

  public StreamI<ValuePropertiesModifier> getRecordedValueProperties() {
    return _recordedValueProperties;
  }

  public RecordingValueProperties withoutAny(final String propertyName) {
    _recordedValueProperties = _recordedValueProperties.cons(new WithoutAny(propertyName));
    return this;
  }

  public RecordingValueProperties with(final String propertyName, final String... propertyValue) {
    for (final String s : propertyValue) {
      if (s == null) {
        throw new IllegalArgumentException("propertyValues cannot contain null");
      }
    }
    _recordedValueProperties = _recordedValueProperties.cons(new With(propertyName, propertyValue));
    return this;
  }

  public RecordingValueProperties withReplacement(final String propertyName, final String... propertyValue) {
    for (final String s : propertyValue) {
      if (s == null) {
        throw new IllegalArgumentException("propertyValues cannot contain null");
      }
    }
    _recordedValueProperties = _recordedValueProperties.cons(new WithReplacement(propertyName, propertyValue));
    return this;
  }

  public RecordingValueProperties withAny(final String propertyName) {
    _recordedValueProperties = _recordedValueProperties.cons(new WithAny(propertyName));
    return this;
  }

  public RecordingValueProperties withOptional(final String propertyName) {
    _recordedValueProperties = _recordedValueProperties.cons(new WithOptional(propertyName));
    return this;
  }

}
