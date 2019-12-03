// Automatically created - do not modify - CSOFF
///CLOVER:OFF
package com.opengamma.engine.cache.msg;
public class GetResponse extends com.opengamma.engine.cache.msg.CacheMessage implements java.io.Serializable {
  @Override
  public CacheMessage accept (final CacheMessageVisitor visitor) { return visitor.visitGetResponse (this); }
  private static final long serialVersionUID = -490550345l;
  private java.util.List<org.fudgemsg.FudgeMsg> _data;
  public static final String DATA_KEY = "data";
  public GetResponse (final java.util.Collection<? extends org.fudgemsg.FudgeMsg> data) {
    if (data == null) {
      throw new NullPointerException ("'data' cannot be null");
    } else {
      final java.util.List<org.fudgemsg.FudgeMsg> fudge0 = new java.util.ArrayList<> (data);
      if (data.size () == 0) {
        throw new IllegalArgumentException ("'data' cannot be an empty list");
      }
      for (final java.util.ListIterator<org.fudgemsg.FudgeMsg> fudge1 = fudge0.listIterator (); fudge1.hasNext ();) {
        final org.fudgemsg.FudgeMsg fudge2 = fudge1.next ();
        if (fudge2 == null) {
          throw new NullPointerException ("List element of 'data' cannot be null");
        }
      }
      _data = fudge0;
    }
  }
  protected GetResponse (final org.fudgemsg.mapping.FudgeDeserializer deserializer, final org.fudgemsg.FudgeMsg fudgeMsg) {
    super (deserializer, fudgeMsg);
    java.util.List<org.fudgemsg.FudgeField> fudgeFields;
    fudgeFields = fudgeMsg.getAllByName (DATA_KEY);
    if (fudgeFields.size () == 0) {
      throw new IllegalArgumentException ("Fudge message is not a GetResponse - field 'data' is not present");
    }
    _data = new java.util.ArrayList<> (fudgeFields.size ());
    for (final org.fudgemsg.FudgeField fudge1 : fudgeFields) {
      try {
        final org.fudgemsg.FudgeMsg fudge2;
        fudge2 = fudgeMsg.getFieldValue (org.fudgemsg.FudgeMsg.class, fudge1);
        _data.add (fudge2);
      }
      catch (final IllegalArgumentException e) {
        throw new IllegalArgumentException ("Fudge message is not a GetResponse - field 'data' is not anonymous/unknown message", e);
      }
    }
  }
  public GetResponse (final Long correlationId, final java.util.Collection<? extends org.fudgemsg.FudgeMsg> data) {
    super (correlationId);
    if (data == null) {
      throw new NullPointerException ("'data' cannot be null");
    } else {
      final java.util.List<org.fudgemsg.FudgeMsg> fudge0 = new java.util.ArrayList<> (data);
      if (data.size () == 0) {
        throw new IllegalArgumentException ("'data' cannot be an empty list");
      }
      for (final java.util.ListIterator<org.fudgemsg.FudgeMsg> fudge1 = fudge0.listIterator (); fudge1.hasNext ();) {
        final org.fudgemsg.FudgeMsg fudge2 = fudge1.next ();
        if (fudge2 == null) {
          throw new NullPointerException ("List element of 'data' cannot be null");
        }
      }
      _data = fudge0;
    }
  }
  protected GetResponse (final GetResponse source) {
    super (source);
    if (source == null) {
      throw new NullPointerException ("'source' must not be null");
    }
    if (source._data == null) {
      _data = null;
    } else {
      _data = new java.util.ArrayList<> (source._data);
    }
  }
  @Override
  public GetResponse clone () {
    return new GetResponse (this);
  }
  @Override
  public org.fudgemsg.FudgeMsg toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializer serializer) {
    if (serializer == null) {
      throw new NullPointerException ("serializer must not be null");
    }
    final org.fudgemsg.MutableFudgeMsg msg = serializer.newMessage ();
    toFudgeMsg (serializer, msg);
    return msg;
  }
  @Override
  public void toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializer serializer, final org.fudgemsg.MutableFudgeMsg msg) {
    super.toFudgeMsg (serializer, msg);
    if (_data != null)  {
      for (final org.fudgemsg.FudgeMsg fudge1 : _data) {
        msg.add (DATA_KEY, null, fudge1 instanceof org.fudgemsg.MutableFudgeMsg ? serializer.newMessage (fudge1) : fudge1);
      }
    }
  }
  public static GetResponse fromFudgeMsg (final org.fudgemsg.mapping.FudgeDeserializer deserializer, final org.fudgemsg.FudgeMsg fudgeMsg) {
    final java.util.List<org.fudgemsg.FudgeField> types = fudgeMsg.getAllByOrdinal (0);
    for (final org.fudgemsg.FudgeField field : types) {
      final String className = (String)field.getValue ();
      if ("com.opengamma.engine.cache.msg.GetResponse".equals (className)) {
        break;
      }
      try {
        return (com.opengamma.engine.cache.msg.GetResponse)Class.forName (className).getDeclaredMethod ("fromFudgeMsg", org.fudgemsg.mapping.FudgeDeserializer.class, org.fudgemsg.FudgeMsg.class).invoke (null, deserializer, fudgeMsg);
      }
      catch (final Throwable t) {
        // no-action
      }
    }
    return new GetResponse (deserializer, fudgeMsg);
  }
  public java.util.List<org.fudgemsg.FudgeMsg> getData () {
    return java.util.Collections.unmodifiableList (_data);
  }
  public void setData (final org.fudgemsg.FudgeMsg data) {
    if (data == null) {
      throw new NullPointerException ("'data' cannot be null");
    } else {
      _data = new java.util.ArrayList<> (1);
      addData (data);
    }
  }
  public void setData (final java.util.Collection<? extends org.fudgemsg.FudgeMsg> data) {
    if (data == null) {
      throw new NullPointerException ("'data' cannot be null");
    } else {
      final java.util.List<org.fudgemsg.FudgeMsg> fudge0 = new java.util.ArrayList<> (data);
      if (data.size () == 0) {
        throw new IllegalArgumentException ("'data' cannot be an empty list");
      }
      for (final java.util.ListIterator<org.fudgemsg.FudgeMsg> fudge1 = fudge0.listIterator (); fudge1.hasNext ();) {
        final org.fudgemsg.FudgeMsg fudge2 = fudge1.next ();
        if (fudge2 == null) {
          throw new NullPointerException ("List element of 'data' cannot be null");
        }
      }
      _data = fudge0;
    }
  }
  public void addData (final org.fudgemsg.FudgeMsg data) {
    if (data == null) {
      throw new NullPointerException ("'data' cannot be null");
    }
    if (_data == null) {
      _data = new java.util.ArrayList<> ();
    }
    _data.add (data);
  }
  @Override
  public String toString () {
    return org.apache.commons.lang.builder.ToStringBuilder.reflectionToString(this, org.apache.commons.lang.builder.ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
///CLOVER:ON - CSON
