/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.security.auditlog;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.FudgeMsgFactory;
import org.fudgemsg.MutableFudgeMsg;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.ArgumentChecker;

/**
 * An audit log entry describes an operation a user attempted on an object.
 * The entry is timestamped. Both successful and failed attempts are logged.
 */
public class AuditLogEntry {

  private Long _id;
  private String _user;
  private String _originatingSystem;
  private String _object;
  private String _operation;
  private String _description;
  private boolean _success;
  private Date _timestamp;

  /**
   * Constructs an instance.
   *
   * @param user  the user, not null
   * @param originatingSystem  the originating system, not null
   * @param object  the object, not null
   * @param operation  the operation, not null
   * @param description  the description, not null
   * @param success  true if the operation was a success
   * @param timestamp  the time stamp, not null
   */
  public AuditLogEntry(final String user,
      final String originatingSystem,
      final String object,
      final String operation,
      final String description,
      final boolean success,
      final Date timestamp) {
    _id = null;
    _user = ArgumentChecker.notNull(user, "user");
    _originatingSystem = ArgumentChecker.notNull(originatingSystem, "originatingSystem");
    _object = ArgumentChecker.notNull(object, "objectName");
    _operation = ArgumentChecker.notNull(operation, "operationName");
    _description = description;
    _success = success;
    _timestamp = ArgumentChecker.notNull(timestamp, "timestamp");
  }

  /**
   * Constructs an empty instance.
   */
  protected AuditLogEntry() {
  }

  /**
   * Gets the id.
   *
   * @return  the id
   */
  public Long getId() {
    return _id;
  }

  /**
   * Sets the id.
   *
   * @param id  the id
   */
  public void setId(final Long id) {
    _id = id;
  }

  /**
   * Gets the user.
   *
   * @return  the user
   */
  public String getUser() {
    return _user;
  }

  /**
   * Sets the user.
   *
   * @param user  the user, not null
   */
  public void setUser(final String user) {
    _user = ArgumentChecker.notNull(user, "user");
  }

  /**
   * Gets the originating system.
   *
   * @return  the originating system
   */
  public String getOriginatingSystem() {
    return _originatingSystem;
  }

  /**
   * Sets the originating system.
   *
   * @param originatingSystem  the originating system, not null
   */
  public void setOriginatingSystem(final String originatingSystem) {
    _originatingSystem = ArgumentChecker.notNull(originatingSystem, "originatingSystem");
  }

  /**
   * Gets the object.
   *
   * @return  the object
   */
  public String getObject() {
    return _object;
  }

  /**
   * Sets the object.
   *
   * @param object  the object, not null
   */
  public void setObject(final String object) {
    _object = ArgumentChecker.notNull(object, "object");
  }

  /**
   * Gets the operation.
   *
   * @return  the operation
   */
  public String getOperation() {
    return _operation;
  }

  /**
   * Sets the operation.
   *
   * @param operation  the operation, not null
   */
  public void setOperation(final String operation) {
    _operation = operation;
  }

  /**
   * Gets the description.
   *
   * @return  the description
   */
  public String getDescription() {
    return _description;
  }

  /**
   * Sets the description.
   *
   * @param description  the description
   */
  public void setDescription(final String description) {
    _description = description;
  }

  /**
   * True if the operation was a success.
   *
   * @return  true if the operation was a success
   */
  public boolean isSuccess() {
    return _success;
  }

  /**
   * Sets the success of the operation.
   *
   * @param success  true if the operation was a success
   */
  public void setSuccess(final boolean success) {
    _success = success;
  }

  /**
   * Gets the time stamp.
   *
   * @return  the time stamp
   */
  public Date getTimestamp() {
    return _timestamp;
  }

  /**
   * Sets the time stamp.
   *
   * @param timestamp  the time stamp
   */
  public void setTimestamp(final Date timestamp) {
    _timestamp = ArgumentChecker.notNull(timestamp, "timestamp");
  }

  /**
   * Converts the entry to a Fudge message.
   *
   * @param fudgeMessageFactory  a message factory
   * @return  the message
   */
  public FudgeMsg toFudgeMsg(final FudgeMsgFactory fudgeMessageFactory) {
    final MutableFudgeMsg msg = fudgeMessageFactory.newMessage();
    msg.add("user", getUser());
    msg.add("originatingSystem", getOriginatingSystem());
    msg.add("object", getObject());
    msg.add("operation", getOperation());
    if (getDescription() != null) {
      msg.add("description", getDescription());
    }
    msg.add("success", isSuccess());
    final String yyyymmdd = new SimpleDateFormat("yyyyMMddHHmmssZ").format(getTimestamp());
    msg.add("timestamp", yyyymmdd); // change as soon as Fudge supports Date natively
    return msg;
  }

  /**
   * Converts a Fudge message to an entry.
   *
   * @param msg  the message
   * @return  the entry
   */
  public static AuditLogEntry fromFudgeMsg(final FudgeMsg msg) {
    final String user = msg.getString("user");
    final String originatingSystem = msg.getString("originatingSystem");
    final String object = msg.getString("object");
    final String operation = msg.getString("operation");
    final String description = msg.getString("description");
    final Boolean success = msg.getBoolean("success");
    final String yyyymmdd = msg.getString("timestamp"); // change as soon as Fudge supports Date natively
    Date timestamp;
    try {
      timestamp = new SimpleDateFormat("yyyyMMddHHmmssZ").parse(yyyymmdd);
    } catch (final ParseException e) {
      throw new OpenGammaRuntimeException("Invalid Fudge message", e);
    }

    AuditLogEntry logEntry;
    try {
      logEntry = new AuditLogEntry(user, originatingSystem, object, operation, description, success, timestamp);
    } catch (final NullPointerException e) {
      throw new OpenGammaRuntimeException("Invalid Fudge message", e);
    }
    return logEntry;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (_id == null ? 0 : _id.hashCode());
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final AuditLogEntry other = (AuditLogEntry) obj;
    if (_id == null) {
      if (other._id != null) {
        return false;
      }
    } else if (!_id.equals(other._id)) {
      return false;
    }
    return true;
  }

}
