// Automatically created - do not modify - CSOFF
///CLOVER:OFF
package com.opengamma.engine.cache.msg;
public class IdentifierLookupRequest extends com.opengamma.engine.cache.msg.CacheMessage implements java.io.Serializable {
  @Override
  public CacheMessage accept (final CacheMessageVisitor visitor) { return visitor.visitIdentifierLookupRequest (this); }
  private static final long serialVersionUID = 38975840645l;
  private java.util.List<com.opengamma.engine.value.ValueSpecification> _specification;
  public static final String SPECIFICATION_KEY = "specification";
  public IdentifierLookupRequest (final java.util.Collection<? extends com.opengamma.engine.value.ValueSpecification> specification) {
    if (specification == null) {
      throw new NullPointerException ("'specification' cannot be null");
    } else {
      final java.util.List<com.opengamma.engine.value.ValueSpecification> fudge0 = new java.util.ArrayList<> (specification);
      if (specification.size () == 0) {
        throw new IllegalArgumentException ("'specification' cannot be an empty list");
      }
      for (final java.util.ListIterator<com.opengamma.engine.value.ValueSpecification> fudge1 = fudge0.listIterator (); fudge1.hasNext ();) {
        final com.opengamma.engine.value.ValueSpecification fudge2 = fudge1.next ();
        if (fudge2 == null) {
          throw new NullPointerException ("List element of 'specification' cannot be null");
        }
        fudge1.set (fudge2);
      }
      _specification = fudge0;
    }
  }
  protected IdentifierLookupRequest (final org.fudgemsg.mapping.FudgeDeserializer deserializer, final org.fudgemsg.FudgeMsg fudgeMsg) {
    super (deserializer, fudgeMsg);
    java.util.List<org.fudgemsg.FudgeField> fudgeFields;
    fudgeFields = fudgeMsg.getAllByName (SPECIFICATION_KEY);
    if (fudgeFields.size () == 0) {
      throw new IllegalArgumentException ("Fudge message is not a IdentifierLookupRequest - field 'specification' is not present");
    }
    _specification = new java.util.ArrayList<> (fudgeFields.size ());
    for (final org.fudgemsg.FudgeField fudge1 : fudgeFields) {
      try {
        final com.opengamma.engine.value.ValueSpecification fudge2;
        fudge2 = deserializer.fieldValueToObject (com.opengamma.engine.value.ValueSpecification.class, fudge1);
        _specification.add (fudge2);
      }
      catch (final IllegalArgumentException e) {
        throw new IllegalArgumentException ("Fudge message is not a IdentifierLookupRequest - field 'specification' is not ValueSpecification message", e);
      }
    }
  }
  public IdentifierLookupRequest (final Long correlationId, final java.util.Collection<? extends com.opengamma.engine.value.ValueSpecification> specification) {
    super (correlationId);
    if (specification == null) {
      throw new NullPointerException ("'specification' cannot be null");
    } else {
      final java.util.List<com.opengamma.engine.value.ValueSpecification> fudge0 = new java.util.ArrayList<> (specification);
      if (specification.size () == 0) {
        throw new IllegalArgumentException ("'specification' cannot be an empty list");
      }
      for (final java.util.ListIterator<com.opengamma.engine.value.ValueSpecification> fudge1 = fudge0.listIterator (); fudge1.hasNext ();) {
        final com.opengamma.engine.value.ValueSpecification fudge2 = fudge1.next ();
        if (fudge2 == null) {
          throw new NullPointerException ("List element of 'specification' cannot be null");
        }
        fudge1.set (fudge2);
      }
      _specification = fudge0;
    }
  }
  protected IdentifierLookupRequest (final IdentifierLookupRequest source) {
    super (source);
    if (source == null) {
      throw new NullPointerException ("'source' must not be null");
    }
    if (source._specification == null) {
      _specification = null;
    } else {
      final java.util.List<com.opengamma.engine.value.ValueSpecification> fudge0 = new java.util.ArrayList<> (source._specification);
      for (final java.util.ListIterator<com.opengamma.engine.value.ValueSpecification> fudge1 = fudge0.listIterator (); fudge1.hasNext();) {
        final com.opengamma.engine.value.ValueSpecification fudge2 = fudge1.next ();
        fudge1.set (fudge2);
      }
      _specification = fudge0;
    }
  }
  @Override
  public IdentifierLookupRequest clone () {
    return new IdentifierLookupRequest (this);
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
    if (_specification != null)  {
      for (final com.opengamma.engine.value.ValueSpecification fudge1 : _specification) {
        serializer.addToMessageWithClassHeaders (msg, SPECIFICATION_KEY, null, fudge1, com.opengamma.engine.value.ValueSpecification.class);
      }
    }
  }
  public static IdentifierLookupRequest fromFudgeMsg (final org.fudgemsg.mapping.FudgeDeserializer deserializer, final org.fudgemsg.FudgeMsg fudgeMsg) {
    final java.util.List<org.fudgemsg.FudgeField> types = fudgeMsg.getAllByOrdinal (0);
    for (final org.fudgemsg.FudgeField field : types) {
      final String className = (String)field.getValue ();
      if ("com.opengamma.engine.cache.msg.IdentifierLookupRequest".equals (className)) {
        break;
      }
      try {
        return (com.opengamma.engine.cache.msg.IdentifierLookupRequest)Class.forName (className).getDeclaredMethod ("fromFudgeMsg", org.fudgemsg.mapping.FudgeDeserializer.class, org.fudgemsg.FudgeMsg.class).invoke (null, deserializer, fudgeMsg);
      }
      catch (final Throwable t) {
        // no-action
      }
    }
    return new IdentifierLookupRequest (deserializer, fudgeMsg);
  }
  public java.util.List<com.opengamma.engine.value.ValueSpecification> getSpecification () {
    return java.util.Collections.unmodifiableList (_specification);
  }
  public void setSpecification (final com.opengamma.engine.value.ValueSpecification specification) {
    if (specification == null) {
      throw new NullPointerException ("'specification' cannot be null");
    } else {
      _specification = new java.util.ArrayList<> (1);
      addSpecification (specification);
    }
  }
  public void setSpecification (final java.util.Collection<? extends com.opengamma.engine.value.ValueSpecification> specification) {
    if (specification == null) {
      throw new NullPointerException ("'specification' cannot be null");
    } else {
      final java.util.List<com.opengamma.engine.value.ValueSpecification> fudge0 = new java.util.ArrayList<> (specification);
      if (specification.size () == 0) {
        throw new IllegalArgumentException ("'specification' cannot be an empty list");
      }
      for (final java.util.ListIterator<com.opengamma.engine.value.ValueSpecification> fudge1 = fudge0.listIterator (); fudge1.hasNext ();) {
        final com.opengamma.engine.value.ValueSpecification fudge2 = fudge1.next ();
        if (fudge2 == null) {
          throw new NullPointerException ("List element of 'specification' cannot be null");
        }
        fudge1.set (fudge2);
      }
      _specification = fudge0;
    }
  }
  public void addSpecification (final com.opengamma.engine.value.ValueSpecification specification) {
    if (specification == null) {
      throw new NullPointerException ("'specification' cannot be null");
    }
    if (_specification == null) {
      _specification = new java.util.ArrayList<> ();
    }
    _specification.add (specification);
  }
  @Override
  public String toString () {
    return org.apache.commons.lang.builder.ToStringBuilder.reflectionToString(this, org.apache.commons.lang.builder.ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
///CLOVER:ON - CSON
