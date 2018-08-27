/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.livedata.normalization;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeField;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;

import com.opengamma.bbg.BloombergConstants;
import com.opengamma.bbg.BloombergPermissions;
import com.opengamma.livedata.normalization.NormalizationRule;
import com.opengamma.livedata.permission.PermissionUtils;
import com.opengamma.livedata.server.FieldHistoryStore;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;

/**
 *
 */
public class BloombergEidFieldValueNormalizer implements NormalizationRule {

  private static final String EID_LIVE_DATA = BloombergConstants.EID_LIVE_DATA_FIELD;
  private static final String EID_REF_DATA = BloombergConstants.EID_DATA.toString();
  private final FudgeContext _fudgeContext = OpenGammaFudgeContext.getInstance();

  @Override
  public MutableFudgeMsg apply(final MutableFudgeMsg msg, final String securityUniqueId, final FieldHistoryStore fieldHistory) {
    final MutableFudgeMsg normalizedMsg = _fudgeContext.newMessage();
    final FudgeDeserializer fudgeDeserializer = new FudgeDeserializer(_fudgeContext);

    for (final FudgeField field : msg) {
      if (field.getName().equalsIgnoreCase(EID_LIVE_DATA) || field.getName().equalsIgnoreCase(EID_REF_DATA)) {
        try {
          final Integer eidValue = fudgeDeserializer.fieldValueToObject(Integer.class, field);
          normalizedMsg.add(PermissionUtils.LIVE_DATA_PERMISSION_FIELD, BloombergPermissions.createEidPermissionString(eidValue));
        } catch (final Exception ex) {
          //ignore
        }
      } else {
        normalizedMsg.add(field);
      }
    }
    return normalizedMsg;
  }
}
