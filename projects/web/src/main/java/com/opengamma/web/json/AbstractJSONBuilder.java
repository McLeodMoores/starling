/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.json;

import java.io.StringReader;
import java.io.StringWriter;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;

import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;

/**
 * Partial implementation of {@link JSONBuilder}
 *
 * @param <T> the config document parameter type
 */
public abstract class AbstractJSONBuilder<T> implements JSONBuilder<T> {

  private static final FudgeContext FUDGE_CONTEXT = OpenGammaFudgeContext.getInstance();

  protected <E> E fromJSON(final Class<E> clazz, final String json) {
    final FudgeMsg fudgeMsg = toFudgeMsg(json);
    return new FudgeDeserializer(FUDGE_CONTEXT).fudgeMsgToObject(clazz, fudgeMsg);
  }

  private static FudgeMsg toFudgeMsg(final String json) {
    final FudgeMsgJSONReader fudgeJSONReader = new FudgeMsgJSONReader(FUDGE_CONTEXT, new StringReader(json));
    return fudgeJSONReader.readMessage();
  }

  public static String fudgeToJson(final Object configObj) {
    final FudgeMsg fudgeMsg = FUDGE_CONTEXT.toFudgeMsg(configObj).getMessage();
    final StringWriter sw = new StringWriter();
    try (FudgeMsgJSONWriter fudgeJSONWriter = new FudgeMsgJSONWriter(FUDGE_CONTEXT, sw)) {
      fudgeJSONWriter.writeMessage(fudgeMsg);
      return sw.toString();
    }
  }

}
