/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.function.dsl;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.dsl.properties.RecordingValueProperties;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.id.UniqueId;
import com.opengamma.util.ArgumentChecker;

/**
 * A function gate. This is a builder-like class that is used to help create a function.
 *
 * @param <T>
 *          the sub-type
 */
public class FunctionGate<T extends FunctionGate<T>> {

  /**
   * The name.
   */
  public static final String NAME = "NAME";
  /**
   * The type.
   */
  public static final String TYPE = "TYPE";
  /**
   * The properties.
   */
  public static final String PROPERTIES = "PROPERTIES";

  private final String _name;
  private ComputationTargetSpecification _cts;
  private ValueProperties _dslValueProperties;
  private RecordingValueProperties _recordingValueProperties;
  private TargetSpecificationReference _targetSpecificationReference;

  /**
   * @param name
   *          the name, not null
   */
  public FunctionGate(final String name) {
    _name = ArgumentChecker.notNull(name, "name");
  }

  @SuppressWarnings("unchecked")
  private T subType() {
    return (T) this;
  }

  // -------------------------------------------------------------------------
  /**
   * Sets the value properties.
   *
   * @param valueProperties
   *          the value properties, not null
   * @return this instance
   */
  public T properties(final ValueProperties valueProperties) {
    _dslValueProperties = ArgumentChecker.notNull(valueProperties, "valueProperties");
    return subType();
  }

  /**
   * Sets the value properties.
   *
   * @param builder
   *          the builder, not null
   * @return this instance
   */
  public T properties(final ValueProperties.Builder builder) {
    _dslValueProperties = ArgumentChecker.notNull(builder, "builder").get();
    return subType();
  }

  /**
   * Gets the value properties.
   *
   * @return the value properties
   */
  public ValueProperties getValueProperties() {
    return _dslValueProperties;
  }

  /**
   * Gets the recorded value properties.
   *
   * @return the value properties
   */
  public RecordingValueProperties getRecordingValueProperties() {
    return _recordingValueProperties;
  }

  /**
   * Sets the recorded value properties.
   *
   * @param recordingValueProperties
   *          the value properties, not null
   * @return this instance
   */
  public T properties(final RecordingValueProperties recordingValueProperties) {
    _recordingValueProperties = ArgumentChecker.notNull(recordingValueProperties, "recordingValueProperties");
    return subType();
  }

  /**
   * Gets the function name.
   *
   * @return the function name
   */
  public String getName() {
    return _name;
  }

  /**
   * Sets the computation target specification using the target type and function unique id.
   *
   * @param computationTargetType
   *          the target type, not null
   * @param uid
   *          the function unique id, not null
   * @return this instance
   */
  public T targetSpec(final ComputationTargetType computationTargetType, final UniqueId uid) {
    _cts = new ComputationTargetSpecification(computationTargetType, uid);
    return subType();
  }

  /**
   * Sets the computation target specification.
   *
   * @param computationTargetSpecification
   *          the target specification, not null
   * @return this instance
   */
  public T targetSpec(final ComputationTargetSpecification computationTargetSpecification) {
    _cts = ArgumentChecker.notNull(computationTargetSpecification, "computationTargetSpecification");
    return subType();
  }

  /**
   * Gets the computation target specification.
   *
   * @return the target specification
   */
  public ComputationTargetSpecification getComputationTargetSpecification() {
    return _cts;
  }

  /**
   * Sets the target specification reference.
   *
   * @param targetSpecificationReference
   *          the target reference
   * @return this instance
   */
  public T targetSpec(final TargetSpecificationReference targetSpecificationReference) {
    _targetSpecificationReference = ArgumentChecker.notNull(targetSpecificationReference, "targetSpecificationReference");
    return subType();
  }

  /**
   * Gets the target specification reference.
   *
   * @return the target reference
   */
  public TargetSpecificationReference getTargetSpecificationReference() {
    return _targetSpecificationReference;
  }

}
