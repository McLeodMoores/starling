/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.security;

import static org.testng.AssertJUnit.fail;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.mapping.FudgeSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import com.opengamma.core.security.Security;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.test.TestGroup;

/**
 * Test the Fudge encoding of securities.
 */
@Test(groups = TestGroup.UNIT)
public class FudgeSecurityEncodingTest extends SecurityTestCase {

  /** Logger. */
  private static final Logger LOGGER = LoggerFactory.getLogger(FudgeSecurityEncodingTest.class);

  /**
   * The Fudge context.
   */
  private static final FudgeContext FUDGE_CONTEXT = OpenGammaFudgeContext.getInstance();

  @Override
  protected <T extends ManageableSecurity> void assertSecurity(Class<T> securityClass, T security) {
    final FudgeSerializer serializer = new FudgeSerializer(FUDGE_CONTEXT);
    FudgeMsg msg = serializer.objectToFudgeMsg(security);
    LOGGER.debug("Security {}", security);
    LOGGER.debug("Encoded to {}", msg);
    final byte[] bytes = FUDGE_CONTEXT.toByteArray(msg);
    msg = FUDGE_CONTEXT.deserialize(bytes).getMessage();
    LOGGER.debug("Serialised to to {}", msg);
    final Security decoded = FUDGE_CONTEXT.fromFudgeMsg(securityClass, msg);
    LOGGER.debug("Decoded to {}", decoded);
    if (!security.equals(decoded)) {
      LOGGER.warn("Expected {}", security);
      LOGGER.warn("Received {}", decoded);
      fail();
    }
  }

}
