/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.security;

import java.util.Map;
import java.util.Set;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.id.ExternalIdBundleFudgeBuilder;
import com.opengamma.id.UniqueIdFudgeBuilder;
import com.opengamma.util.fudgemsg.AbstractFudgeBuilder;

/**
 * A Fudge builder for {@code ManageableSecurity}.
 */
@FudgeBuilderFor(ManageableSecurity.class)
public class ManageableSecurityFudgeBuilder extends AbstractFudgeBuilder implements FudgeBuilder<ManageableSecurity> {

  /** Field name. */
  public static final String UNIQUE_ID_FIELD_NAME = "uniqueId";
  /** Field name. */
  public static final String NAME_FIELD_NAME = "name";
  /** Field name. */
  public static final String SECURITY_TYPE_FIELD_NAME = "securityType";
  /** Field name. */
  public static final String IDENTIFIERS_FIELD_NAME = "identifiers";
  /** Field name. */
  public static final String ATTRIBUTES_FIELD_NAME = "attributes";
  /** Field name. */
  public static final String PERMISSIONS_FIELD_NAME = "permissions";

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final ManageableSecurity object) {
    final MutableFudgeMsg msg = serializer.newMessage();
    ManageableSecurityFudgeBuilder.toFudgeMsg(serializer, object, msg);
    return msg;
  }

  /**
   * Serializes a security.
   *
   * @param serializer  the serializer, not null
   * @param security  the security, not null
   * @param msg  the message to add the security message to
   */
  public static void toFudgeMsg(final FudgeSerializer serializer, final ManageableSecurity security, final MutableFudgeMsg msg) {
    addToMessage(msg, UNIQUE_ID_FIELD_NAME, UniqueIdFudgeBuilder.toFudgeMsg(serializer, security.getUniqueId()));
    addToMessage(msg, NAME_FIELD_NAME, security.getName());
    addToMessage(msg, SECURITY_TYPE_FIELD_NAME, security.getSecurityType());
    addToMessage(msg, IDENTIFIERS_FIELD_NAME, ExternalIdBundleFudgeBuilder.toFudgeMsg(serializer, security.getExternalIdBundle()));
    addToMessage(msg, ATTRIBUTES_FIELD_NAME, serializer.objectToFudgeMsg(security.getAttributes()));
    if (!security.getRequiredPermissions().isEmpty()) {
      addToMessage(msg, PERMISSIONS_FIELD_NAME, serializer.objectToFudgeMsg(security.getRequiredPermissions()));
    }
  }

  @Override
  public ManageableSecurity buildObject(final FudgeDeserializer deserializer, final FudgeMsg msg) {
    final ManageableSecurity object = new ManageableSecurity(msg.getString(SECURITY_TYPE_FIELD_NAME));
    ManageableSecurityFudgeBuilder.fromFudgeMsg(deserializer, msg, object);
    return object;
  }

  /**
   * Adds fields from a message to a security.
   *
   * @param deserializer  the deserializer, not null
   * @param msg  the message, not null
   * @param security  the security
   */
  @SuppressWarnings("unchecked")
  public static void fromFudgeMsg(final FudgeDeserializer deserializer, final FudgeMsg msg, final ManageableSecurity security) {
    security.setUniqueId(UniqueIdFudgeBuilder.fromFudgeMsg(deserializer, msg.getMessage(UNIQUE_ID_FIELD_NAME)));
    security.setName(msg.getString(NAME_FIELD_NAME));
    security.setExternalIdBundle(ExternalIdBundleFudgeBuilder.fromFudgeMsg(deserializer, msg.getMessage(IDENTIFIERS_FIELD_NAME)));
    security.setAttributes((Map<String, String>) deserializer.fieldValueToObject(msg.getByName(ATTRIBUTES_FIELD_NAME)));
    if (msg.hasField(PERMISSIONS_FIELD_NAME)) {
      security.setRequiredPermissions((Set<String>) deserializer.fieldValueToObject(msg.getByName(PERMISSIONS_FIELD_NAME)));
    }
  }

}
