/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.mcleodmoores.integration.simulatedexamples.data;

import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;

import com.opengamma.livedata.server.LiveDataServerMBean;

/**
 * JMX management of a {@link ExampleLiveDataServer}
 */
public class TestLiveDataServerMBean extends LiveDataServerMBean {

  private final TestLiveDataServer _server;

  public TestLiveDataServerMBean(final TestLiveDataServer server) {
    super(server);
    _server = server;
  }

  @ManagedOperation(description = "Reset initial market value for a security")
  @ManagedOperationParameters({@ManagedOperationParameter(name = "uniqueId", description = "Unique secuirty Id.)"),
    @ManagedOperationParameter(name = "fieldName", description = "field name.)"),
    @ManagedOperationParameter(name = "initialValue", description = "initial value.)") })
  public void resetInitialMarketValue(final String uniqueId, final String fieldName, final Double initialValue) {
    _server.addTicks(uniqueId, fieldName, initialValue);
  }

  @ManagedAttribute(description = "The scaling factor used to wriggle initial market value.")
  public double getScalingFactor() {
    return _server.getScalingFactor();
  }

  @ManagedOperation(description = "Set the scaling factor for wriggling initial values")
  @ManagedOperationParameters({
      @ManagedOperationParameter(name = "scalingFactor", description = "set the scaling factor used for wriggling initial values.)") })
  public void setScalingFactor(final double scalingFactor) {
    _server.setScalingFactor(scalingFactor);
  }

  @ManagedAttribute(description = "The maximum millis between ticks.")
  public int getMaxMillisBetweenTicks() {
    return _server.getMaxMillisBetweenTicks();
  }

  @ManagedOperation(description = "Set the maximum milli seconds between ticks.")
  @ManagedOperationParameters({
      @ManagedOperationParameter(name = "maxMillisBetweenTicks", description = "set the maximum milli seconds between ticks.)") })
  public void setMaxMillisBetweenTicks(final int maxMillisBetweenTicks) {
    _server.setMaxMillisBetweenTicks(maxMillisBetweenTicks);
  }

}
