// Automatically created - do not modify - CSOFF
///CLOVER:OFF
package com.opengamma.livedata.msg;
public class LiveDataSubscriptionResponseMsg implements java.io.Serializable {
  private static final long serialVersionUID = -46399868574077l;
  private com.opengamma.livedata.UserPrincipal _requestingUser;
  public static final String REQUESTING_USER_KEY = "requestingUser";
  private java.util.List<com.opengamma.livedata.msg.LiveDataSubscriptionResponse> _responses;
  public static final String RESPONSES_KEY = "responses";
  public LiveDataSubscriptionResponseMsg (final com.opengamma.livedata.UserPrincipal requestingUser, final java.util.Collection<? extends com.opengamma.livedata.msg.LiveDataSubscriptionResponse> responses) {
    if (requestingUser == null) {
      throw new NullPointerException ("'requestingUser' cannot be null");
    } else {
      _requestingUser = requestingUser;
    }
    if (responses == null) {
      throw new NullPointerException ("'responses' cannot be null");
    } else {
      final java.util.List<com.opengamma.livedata.msg.LiveDataSubscriptionResponse> fudge0 = new java.util.ArrayList<> (responses);
      if (responses.size () == 0) {
        throw new IllegalArgumentException ("'responses' cannot be an empty list");
      }
      for (final java.util.ListIterator<com.opengamma.livedata.msg.LiveDataSubscriptionResponse> fudge1 = fudge0.listIterator (); fudge1.hasNext ();) {
        final com.opengamma.livedata.msg.LiveDataSubscriptionResponse fudge2 = fudge1.next ();
        if (fudge2 == null) {
          throw new NullPointerException ("List element of 'responses' cannot be null");
        }
        fudge1.set (fudge2.clone ());
      }
      _responses = fudge0;
    }
  }
  protected LiveDataSubscriptionResponseMsg (final org.fudgemsg.mapping.FudgeDeserializer deserializer, final org.fudgemsg.FudgeMsg fudgeMsg) {
    org.fudgemsg.FudgeField fudgeField;
    java.util.List<org.fudgemsg.FudgeField> fudgeFields;
    fudgeField = fudgeMsg.getByName (REQUESTING_USER_KEY);
    if (fudgeField == null) {
      throw new IllegalArgumentException ("Fudge message is not a LiveDataSubscriptionResponseMsg - field 'requestingUser' is not present");
    }
    try {
      _requestingUser = deserializer.fieldValueToObject (com.opengamma.livedata.UserPrincipal.class, fudgeField);
    }
    catch (final IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a LiveDataSubscriptionResponseMsg - field 'requestingUser' is not UserPrincipal message", e);
    }
    fudgeFields = fudgeMsg.getAllByName (RESPONSES_KEY);
    if (fudgeFields.size () == 0) {
      throw new IllegalArgumentException ("Fudge message is not a LiveDataSubscriptionResponseMsg - field 'responses' is not present");
    }
    _responses = new java.util.ArrayList<> (fudgeFields.size ());
    for (final org.fudgemsg.FudgeField fudge1 : fudgeFields) {
      try {
        final com.opengamma.livedata.msg.LiveDataSubscriptionResponse fudge2;
        fudge2 = com.opengamma.livedata.msg.LiveDataSubscriptionResponse.fromFudgeMsg (deserializer, fudgeMsg.getFieldValue (org.fudgemsg.FudgeMsg.class, fudge1));
        _responses.add (fudge2);
      }
      catch (final IllegalArgumentException e) {
        throw new IllegalArgumentException ("Fudge message is not a LiveDataSubscriptionResponseMsg - field 'responses' is not LiveDataSubscriptionResponse message", e);
      }
    }
  }
  protected LiveDataSubscriptionResponseMsg (final LiveDataSubscriptionResponseMsg source) {
    if (source == null) {
      throw new NullPointerException ("'source' must not be null");
    }
    if (source._requestingUser == null) {
      _requestingUser = null;
    } else {
      _requestingUser = source._requestingUser;
    }
    if (source._responses == null) {
      _responses = null;
    } else {
      final java.util.List<com.opengamma.livedata.msg.LiveDataSubscriptionResponse> fudge0 = new java.util.ArrayList<> (source._responses);
      for (final java.util.ListIterator<com.opengamma.livedata.msg.LiveDataSubscriptionResponse> fudge1 = fudge0.listIterator (); fudge1.hasNext ();) {
        final com.opengamma.livedata.msg.LiveDataSubscriptionResponse fudge2 = fudge1.next ();
        fudge1.set (fudge2.clone ());
      }
      _responses = fudge0;
    }
  }
  @Override
  public LiveDataSubscriptionResponseMsg clone () {
    return new LiveDataSubscriptionResponseMsg (this);
  }
  public org.fudgemsg.FudgeMsg toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializer serializer) {
    if (serializer == null) {
      throw new NullPointerException ("serializer must not be null");
    }
    final org.fudgemsg.MutableFudgeMsg msg = serializer.newMessage ();
    toFudgeMsg (serializer, msg);
    return msg;
  }
  public void toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializer serializer, final org.fudgemsg.MutableFudgeMsg msg) {
    if (_requestingUser != null)  {
      serializer.addToMessageWithClassHeaders (msg, REQUESTING_USER_KEY, null, _requestingUser, com.opengamma.livedata.UserPrincipal.class);
    }
    if (_responses != null)  {
      for (final com.opengamma.livedata.msg.LiveDataSubscriptionResponse fudge1 : _responses) {
        final org.fudgemsg.MutableFudgeMsg fudge2 = org.fudgemsg.mapping.FudgeSerializer.addClassHeader (serializer.newMessage (), fudge1.getClass (), com.opengamma.livedata.msg.LiveDataSubscriptionResponse.class);
        fudge1.toFudgeMsg (serializer, fudge2);
        msg.add (RESPONSES_KEY, null, fudge2);
      }
    }
  }
  public static LiveDataSubscriptionResponseMsg fromFudgeMsg (final org.fudgemsg.mapping.FudgeDeserializer deserializer, final org.fudgemsg.FudgeMsg fudgeMsg) {
    final java.util.List<org.fudgemsg.FudgeField> types = fudgeMsg.getAllByOrdinal (0);
    for (final org.fudgemsg.FudgeField field : types) {
      final String className = (String)field.getValue ();
      if ("com.opengamma.livedata.msg.LiveDataSubscriptionResponseMsg".equals (className)) {
        break;
      }
      try {
        return (com.opengamma.livedata.msg.LiveDataSubscriptionResponseMsg)Class.forName (className).getDeclaredMethod ("fromFudgeMsg", org.fudgemsg.mapping.FudgeDeserializer.class, org.fudgemsg.FudgeMsg.class).invoke (null, deserializer, fudgeMsg);
      }
      catch (final Throwable t) {
        // no-action
      }
    }
    return new LiveDataSubscriptionResponseMsg (deserializer, fudgeMsg);
  }
  public com.opengamma.livedata.UserPrincipal getRequestingUser () {
    return _requestingUser;
  }
  public void setRequestingUser (final com.opengamma.livedata.UserPrincipal requestingUser) {
    if (requestingUser == null) {
      throw new NullPointerException ("'requestingUser' cannot be null");
    } else {
      _requestingUser = requestingUser;
    }
  }
  public java.util.List<com.opengamma.livedata.msg.LiveDataSubscriptionResponse> getResponses () {
    return java.util.Collections.unmodifiableList (_responses);
  }
  public void setResponses (final com.opengamma.livedata.msg.LiveDataSubscriptionResponse responses) {
    if (responses == null) {
      throw new NullPointerException ("'responses' cannot be null");
    } else {
      _responses = new java.util.ArrayList<> (1);
      addResponses (responses);
    }
  }
  public void setResponses (final java.util.Collection<? extends com.opengamma.livedata.msg.LiveDataSubscriptionResponse> responses) {
    if (responses == null) {
      throw new NullPointerException ("'responses' cannot be null");
    } else {
      final java.util.List<com.opengamma.livedata.msg.LiveDataSubscriptionResponse> fudge0 = new java.util.ArrayList<> (responses);
      if (responses.size () == 0) {
        throw new IllegalArgumentException ("'responses' cannot be an empty list");
      }
      for (final java.util.ListIterator<com.opengamma.livedata.msg.LiveDataSubscriptionResponse> fudge1 = fudge0.listIterator (); fudge1.hasNext ();) {
        final com.opengamma.livedata.msg.LiveDataSubscriptionResponse fudge2 = fudge1.next ();
        if (fudge2 == null) {
          throw new NullPointerException ("List element of 'responses' cannot be null");
        }
        fudge1.set (fudge2.clone ());
      }
      _responses = fudge0;
    }
  }
  public void addResponses (final com.opengamma.livedata.msg.LiveDataSubscriptionResponse responses) {
    if (responses == null) {
      throw new NullPointerException ("'responses' cannot be null");
    }
    if (_responses == null) {
      _responses = new java.util.ArrayList<> ();
    }
    _responses.add (responses.clone ());
  }
  @Override
  public String toString () {
    return org.apache.commons.lang.builder.ToStringBuilder.reflectionToString(this, org.apache.commons.lang.builder.ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
///CLOVER:ON - CSON
