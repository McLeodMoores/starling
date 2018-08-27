// Automatically created - do not modify - CSOFF
///CLOVER:OFF
package com.opengamma.engine.cache.msg;
public class SpecificationLookupRequest extends com.opengamma.engine.cache.msg.CacheMessage implements java.io.Serializable {
  @Override
  public CacheMessage accept (final CacheMessageVisitor visitor) { return visitor.visitSpecificationLookupRequest (this); }
  private static final long serialVersionUID = -51269628524l;
  private java.util.List<Long> _identifier;
  public static final String IDENTIFIER_KEY = "identifier";
  public SpecificationLookupRequest (final java.util.Collection<? extends Long> identifier) {
    if (identifier == null) {
      throw new NullPointerException ("'identifier' cannot be null");
    } else {
      final java.util.List<Long> fudge0 = new java.util.ArrayList<> (identifier);
      if (identifier.size () == 0) {
        throw new IllegalArgumentException ("'identifier' cannot be an empty list");
      }
      for (final java.util.ListIterator<Long> fudge1 = fudge0.listIterator (); fudge1.hasNext ();) {
        final Long fudge2 = fudge1.next ();
        if (fudge2 == null) {
          throw new NullPointerException ("List element of 'identifier' cannot be null");
        }
      }
      _identifier = fudge0;
    }
  }
  protected SpecificationLookupRequest (final org.fudgemsg.mapping.FudgeDeserializer deserializer, final org.fudgemsg.FudgeMsg fudgeMsg) {
    super (deserializer, fudgeMsg);
    java.util.List<org.fudgemsg.FudgeField> fudgeFields;
    fudgeFields = fudgeMsg.getAllByName (IDENTIFIER_KEY);
    if (fudgeFields.size () == 0) {
      throw new IllegalArgumentException ("Fudge message is not a SpecificationLookupRequest - field 'identifier' is not present");
    }
    _identifier = new java.util.ArrayList<> (fudgeFields.size ());
    for (final org.fudgemsg.FudgeField fudge1 : fudgeFields) {
      try {
        _identifier.add (fudgeMsg.getFieldValue (Long.class, fudge1));
      }
      catch (final IllegalArgumentException e) {
        throw new IllegalArgumentException ("Fudge message is not a SpecificationLookupRequest - field 'identifier' is not long", e);
      }
    }
  }
  public SpecificationLookupRequest (final Long correlationId, final java.util.Collection<? extends Long> identifier) {
    super (correlationId);
    if (identifier == null) {
      throw new NullPointerException ("'identifier' cannot be null");
    } else {
      final java.util.List<Long> fudge0 = new java.util.ArrayList<> (identifier);
      if (identifier.size () == 0) {
        throw new IllegalArgumentException ("'identifier' cannot be an empty list");
      }
      for (final java.util.ListIterator<Long> fudge1 = fudge0.listIterator (); fudge1.hasNext ();) {
        final Long fudge2 = fudge1.next ();
        if (fudge2 == null) {
          throw new NullPointerException ("List element of 'identifier' cannot be null");
        }
      }
      _identifier = fudge0;
    }
  }
  protected SpecificationLookupRequest (final SpecificationLookupRequest source) {
    super (source);
    if (source == null) {
      throw new NullPointerException ("'source' must not be null");
    }
    if (source._identifier == null) {
      _identifier = null;
    } else {
      _identifier = new java.util.ArrayList<> (source._identifier);
    }
  }
  @Override
  public SpecificationLookupRequest clone () {
    return new SpecificationLookupRequest (this);
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
    if (_identifier != null)  {
      for (final Long fudge1 : _identifier) {
        msg.add (IDENTIFIER_KEY, null, fudge1);
      }
    }
  }
  public static SpecificationLookupRequest fromFudgeMsg (final org.fudgemsg.mapping.FudgeDeserializer deserializer, final org.fudgemsg.FudgeMsg fudgeMsg) {
    final java.util.List<org.fudgemsg.FudgeField> types = fudgeMsg.getAllByOrdinal (0);
    for (final org.fudgemsg.FudgeField field : types) {
      final String className = (String)field.getValue ();
      if ("com.opengamma.engine.cache.msg.SpecificationLookupRequest".equals (className)) {
        break;
      }
      try {
        return (com.opengamma.engine.cache.msg.SpecificationLookupRequest)Class.forName (className).getDeclaredMethod ("fromFudgeMsg", org.fudgemsg.mapping.FudgeDeserializer.class, org.fudgemsg.FudgeMsg.class).invoke (null, deserializer, fudgeMsg);
      }
      catch (final Throwable t) {
        // no-action
      }
    }
    return new SpecificationLookupRequest (deserializer, fudgeMsg);
  }
  public java.util.List<Long> getIdentifier () {
    return java.util.Collections.unmodifiableList (_identifier);
  }
  public void setIdentifier (final Long identifier) {
    if (identifier == null) {
      throw new NullPointerException ("'identifier' cannot be null");
    } else {
      _identifier = new java.util.ArrayList<> (1);
      addIdentifier (identifier);
    }
  }
  public void setIdentifier (final java.util.Collection<? extends Long> identifier) {
    if (identifier == null) {
      throw new NullPointerException ("'identifier' cannot be null");
    } else {
      final java.util.List<Long> fudge0 = new java.util.ArrayList<> (identifier);
      if (identifier.size () == 0) {
        throw new IllegalArgumentException ("'identifier' cannot be an empty list");
      }
      for (final java.util.ListIterator<Long> fudge1 = fudge0.listIterator (); fudge1.hasNext ();) {
        final Long fudge2 = fudge1.next ();
        if (fudge2 == null) {
          throw new NullPointerException ("List element of 'identifier' cannot be null");
        }
      }
      _identifier = fudge0;
    }
  }
  public void addIdentifier (final Long identifier) {
    if (identifier == null) {
      throw new NullPointerException ("'identifier' cannot be null");
    }
    if (_identifier == null) {
      _identifier = new java.util.ArrayList<> ();
    }
    _identifier.add (identifier);
  }
  @Override
  public String toString () {
    return org.apache.commons.lang.builder.ToStringBuilder.reflectionToString(this, org.apache.commons.lang.builder.ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
///CLOVER:ON - CSON
