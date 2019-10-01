/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.function.dsl.properties;

import java.util.stream.Stream;

import com.opengamma.util.ArgumentChecker;

/**
 * Records the value properties from a value requirement.
 */
public final class RecordingValueProperties {
  private Stream<ValuePropertiesModifier> _recordedValueProperties = Stream.empty();
  private final String _copiedFrom;

  private RecordingValueProperties(final String copiedFrom) {
    _copiedFrom = copiedFrom;
  }

  /**
   * Records the requirement name from which value properties should be copied.
   *
   * @param copiedFrom
   *          the requirement name, not null
   * @return recording value properties
   */
  public static RecordingValueProperties copyFrom(final String copiedFrom) {
    return new RecordingValueProperties(ArgumentChecker.notNull(copiedFrom, "copiedFrom"));
  }

  /**
   * Gets empty properties.
   *
   * @return recording value properties
   */
  public static RecordingValueProperties desiredValue() {
    return new RecordingValueProperties(null);
  }

  /**
   * Gets the value requirement name from which the properties are copied.
   *
   * @return the name
   */
  public String getCopiedFrom() {
    return _copiedFrom;
  }

  /**
   * Returns a stream of recorded value properties.
   *
   * @return the value properties
   */
  public Stream<ValuePropertiesModifier> getRecordedValueProperties() {
    return _recordedValueProperties;
  }

  /**
   * Removes a property and records which property was removed.
   *
   * @param propertyName
   *          the property name, not null
   * @return the value properties
   */
  public RecordingValueProperties withoutAny(final String propertyName) {
    _recordedValueProperties = Stream.concat(_recordedValueProperties, Stream.of(new WithoutAny(propertyName)));
    return this;
  }

  /**
   * Adds a property and records which property was added.
   *
   * @param propertyName
   *          the property name, not null
   * @param propertyValue
   *          the property value, not null
   * @return the value properties
   */
  public RecordingValueProperties with(final String propertyName, final String... propertyValue) {
    ArgumentChecker.noNulls(propertyValue, "propertyValue");
    _recordedValueProperties = Stream.concat(_recordedValueProperties, Stream.of(new With(propertyName, propertyValue)));
    return this;
  }

  /**
   * Replaces a property and records which property was replaced.
   *
   * @param propertyName
   *          the property name, not null
   * @param propertyValue
   *          the property value, not null
   * @return the value properties
   */
  public RecordingValueProperties withReplacement(final String propertyName, final String... propertyValue) {
    ArgumentChecker.noNulls(propertyValue, "propertyValue");
    _recordedValueProperties = Stream.concat(_recordedValueProperties, Stream.of(new WithReplacement(propertyName, propertyValue)));
    return this;
  }

  /**
   * Adds a property and records which property was added.
   *
   * @param propertyName
   *          the property name, not null
   * @return the value properties
   */
  public RecordingValueProperties withAny(final String propertyName) {
    _recordedValueProperties = Stream.concat(_recordedValueProperties, Stream.of(new WithAny(propertyName)));
    return this;
  }

  /**
   * Adds a property and records which property was added.
   *
   * @param propertyName
   *          the property name, not null
   * @return the value properties
   */
  public RecordingValueProperties withOptional(final String propertyName) {
    _recordedValueProperties = Stream.concat(_recordedValueProperties, Stream.of(new WithOptional(propertyName)));
    return this;
  }

}
