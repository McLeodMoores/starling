/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.livedata.resolver;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import java.util.Arrays;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.testng.annotations.Test;

import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.msg.ResolveRequest;
import com.opengamma.livedata.msg.ResolveResponse;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link IdResolverServer}.
 */
@Test(groups = TestGroup.UNIT)
public class IdResolverServerTest {
  private static final IdResolverServer SERVER = new IdResolverServer(SchemeChangingResolver.RESOLVER);
  private static final FudgeSerializer SERIALIZER = new FudgeSerializer(OpenGammaFudgeContext.getInstance());
  private static final FudgeDeserializer DESERIALIZER = new FudgeDeserializer(OpenGammaFudgeContext.getInstance());
  private static final LiveDataSpecification INITIAL_SPEC = new LiveDataSpecification("rules", Arrays.asList(ExternalId.of("eid", "1")));

  /**
   * Tests that the id resolver cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullIdResolver() {
    new IdResolverServer(null);
  }

  /**
   * Tests creation of the resolved request.
   */
  public void testResolvedRequest() {
    final ResolveRequest request = new ResolveRequest(INITIAL_SPEC);
    final MutableFudgeMsg msg = SERIALIZER.objectToFudgeMsg(request);
    final FudgeMsgEnvelope envelope = new FudgeMsgEnvelope(msg);
    final FudgeMsg resolvedMsg = SERVER.requestReceived(DESERIALIZER, envelope);
    final ResolveResponse resolved = DESERIALIZER.fudgeMsgToObject(ResolveResponse.class, resolvedMsg);
    final LiveDataSpecification resolvedSpec = resolved.getResolvedSpecification();
    assertEquals(resolvedSpec.getNormalizationRuleSetId(), INITIAL_SPEC.getNormalizationRuleSetId());
    final ExternalIdBundle initialIds = INITIAL_SPEC.getIdentifiers();
    final ExternalIdBundle resolvedIds = resolvedSpec.getIdentifiers();
    assertEquals(resolvedIds.size(), initialIds.size());
    assertNotEquals(resolvedIds.getExternalIds().iterator().next().getScheme(), initialIds.getExternalIds().iterator().next().getScheme());
    assertEquals(resolvedIds.getExternalIds().iterator().next().getValue(), initialIds.getExternalIds().iterator().next().getValue());
  }

  private static class SchemeChangingResolver extends AbstractResolver<ExternalIdBundle, ExternalId> implements IdResolver {
    public static final SchemeChangingResolver RESOLVER = new SchemeChangingResolver();
    public static final String NEW_SCHEME = "neweid";

    private SchemeChangingResolver() {
    }

    @Override
    public ExternalId resolve(final ExternalIdBundle ids) {
      final ExternalId id = ids.getExternalIds().iterator().next();
      return ExternalId.of(NEW_SCHEME, id.getValue());
    }
  }
}
