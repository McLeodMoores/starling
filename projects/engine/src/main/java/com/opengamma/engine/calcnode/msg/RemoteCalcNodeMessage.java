// Automatically created - do not modify - CSOFF
///CLOVER:OFF
package com.opengamma.engine.calcnode.msg;
public class RemoteCalcNodeMessage implements java.io.Serializable {
  public void accept (final RemoteCalcNodeMessageVisitor visitor) { throw new UnsupportedOperationException (); }
  private static final long serialVersionUID = 1l;
  public RemoteCalcNodeMessage () {
  }
  protected RemoteCalcNodeMessage (final org.fudgemsg.mapping.FudgeDeserializer deserializer, final org.fudgemsg.FudgeMsg fudgeMsg) {
  }
  protected RemoteCalcNodeMessage (final RemoteCalcNodeMessage source) {
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
  }
  public static RemoteCalcNodeMessage fromFudgeMsg (final org.fudgemsg.mapping.FudgeDeserializer deserializer, final org.fudgemsg.FudgeMsg fudgeMsg) {
    final java.util.List<org.fudgemsg.FudgeField> types = fudgeMsg.getAllByOrdinal (0);
    for (final org.fudgemsg.FudgeField field : types) {
      final String className = (String)field.getValue ();
      if ("com.opengamma.engine.calcnode.msg.RemoteCalcNodeMessage".equals (className)) {
        break;
      }
      try {
        return (com.opengamma.engine.calcnode.msg.RemoteCalcNodeMessage)Class.forName (className).getDeclaredMethod ("fromFudgeMsg", org.fudgemsg.mapping.FudgeDeserializer.class, org.fudgemsg.FudgeMsg.class).invoke (null, deserializer, fudgeMsg);
      }
      catch (final Throwable t) {
        // no-action
      }
    }
    return new RemoteCalcNodeMessage (deserializer, fudgeMsg);
  }
  @Override
  public String toString () {
    return org.apache.commons.lang.builder.ToStringBuilder.reflectionToString(this, org.apache.commons.lang.builder.ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
///CLOVER:ON - CSON
