/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.cogda.msg;

import org.fudgemsg.FudgeMsg;

/**
 * An enumeration of all message types to support ease of event loop processing.
 */
public enum CogdaMessageType {
  /** {@link ConnectionRequestMessage}. */
  CONNECTION_REQUEST,
  /** {@link ConnectionResponseMessage}. */
  CONNECTION_RESPONSE,
  /** {@link CogdaLiveDataSnapshotRequestMessage}. */
  SNAPSHOT_REQUEST,
  /** {@link CogdaLiveDataSnapshotResponseMessage}. */
  SNAPSHOT_RESPONSE,
  /** {@link CogdaLiveDataSubscriptionRequestMessage}. */
  SUBSCRIPTION_REQUEST,
  /** {@link CogdaLiveDataSubscriptionResponseMessage}. */
  SUBSCRIPTION_RESPONSE,
  /** {@link CogdaLiveDataUnsubscribeMessage}. */
  UNSUBSCRIBE,
  /** {@link CogdaLiveDataUpdateMessage}. */
  LIVE_DATA_UPDATE;

  public static CogdaMessageType getFromMessage(final FudgeMsg msg) {
    if (msg == null) {
      return null;
    }
    if (!msg.hasField("MESSAGE_TYPE")) {
      return null;
    }
    return CogdaMessageType.valueOf(msg.getString("MESSAGE_TYPE"));
  }

}
