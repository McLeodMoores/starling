/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.transport.jaxrs;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.FudgeMsgEnvelope;

/**
 * Allow introspection of a large FudgeFieldContainer. This is useful for constructing
 * direct URLs to deep data without the client having to process the full message.
 */
public class FudgeFieldContainerBrowser {

  private final FudgeMsg _message;

  /**
   * Constructs an instance.
   *
   * @param message  the message
   */
  public FudgeFieldContainerBrowser(final FudgeMsg message) {
    _message = message;
  }

  /**
   * Gets the message wrapped in an envelope.
   *
   * @return  the message
   */
  @GET
  public FudgeMsgEnvelope get() {
    return new FudgeMsgEnvelope(_message);
  }

  /**
   * Returns either null or a sub-message wrapped in a container browser.
   *
   * @param fudgeField  the field name
   * @return  null if there is no value for the field or if the value is not a message, the sub-message wrapped
   * in the browser otherwise
   */
  @Path("{fieldName}")
  public FudgeFieldContainerBrowser get(@PathParam("fieldName") final String fudgeField) {
    final FudgeField field = _message.getByName(fudgeField);
    if (field == null) {
      return null;
    }
    if (!(field.getValue() instanceof FudgeMsg)) {
      return null;
    }
    return new FudgeFieldContainerBrowser((FudgeMsg) field.getValue());
  }

}
