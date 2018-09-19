/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.value;

import static org.testng.Assert.assertNotEquals;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import java.math.BigDecimal;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.testng.annotations.Test;

import com.opengamma.core.position.Position;
import com.opengamma.core.position.impl.SimplePosition;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.test.TestGroup;

/**
 * Test ValueRequirement.
 */
@Test(groups = TestGroup.UNIT)
public class ValueRequirementTest {

  private static final UniqueId USD = UniqueId.of("currency", "USD");
  private static final UniqueId GBP = UniqueId.of("currency", "GBP");
  private static final Position POSITION = new SimplePosition(UniqueId.of("A", "B"), new BigDecimal(1), ExternalIdBundle.EMPTY);
  private static final ComputationTargetSpecification SPEC = ComputationTargetSpecification.of(POSITION);

  /**
   * Tests construction from a name and computation target spec of a position.
   */
  public void testConstructorPosition() {
    final ValueRequirement test = new ValueRequirement("DATA", SPEC);
    assertEquals("DATA", test.getValueName());
    assertEquals(SPEC, test.getTargetReference());
  }

  /**
   * Tests that the name cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testConstructorNullValue() {
    new ValueRequirement(null, SPEC);
  }

  /**
   * Tests that the computation target specification cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testConstructorNullSpec() {
    new ValueRequirement("DATA", (ComputationTargetSpecification) null);
  }

  /**
   * Tests construction from a name, position target type and position identifier.
   */
  public void testConstructorTypeUniqueIdPosition() {
    final ValueRequirement test = new ValueRequirement("DATA", ComputationTargetType.POSITION, POSITION.getUniqueId());
    assertEquals("DATA", test.getValueName());
    assertEquals(SPEC, test.getTargetReference());
  }

  /**
   * Tests that the name cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testConstructorTypeUniqueIdNullValue() {
    new ValueRequirement(null, ComputationTargetType.POSITION, POSITION.getUniqueId());
  }

  /**
   * Tests that the target type cannot be null.
   */
  @Test(expectedExceptions = AssertionError.class)
  public void testConstructorTypeUniqueIdNullType() {
    new ValueRequirement("DATA", null, POSITION.getUniqueId());
  }

  /**
   * Tests that the name cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testConstructorTypeIdentifierNullValue() {
    new ValueRequirement(null, ComputationTargetType.PRIMITIVE, USD);
  }

  /**
   * Tests that the target type cannot be null.
   */
  @Test(expectedExceptions = AssertionError.class)
  public void testConstructorTypeIdentifierNullType() {
    new ValueRequirement("DATA", null, USD);
  }

  /**
   * Tests construction from a name, target type and position id.
   */
  public void testConstructorObjectPosition() {
    final ValueRequirement test = new ValueRequirement("DATA", ComputationTargetType.POSITION, POSITION.getUniqueId());
    assertEquals("DATA", test.getValueName());
    assertEquals(SPEC, test.getTargetReference());
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the equals method.
   */
  public void testEquals() {
    final ValueRequirement req1 = new ValueRequirement(ValueRequirementNames.DISCOUNT_CURVE, ComputationTargetType.PRIMITIVE, USD);
    assertTrue(req1.equals(req1));
    assertNotEquals(null, req1);
    assertNotEquals("Rubbish", req1);

    ValueRequirement req2 = new ValueRequirement(ValueRequirementNames.DISCOUNT_CURVE, ComputationTargetType.PRIMITIVE, USD);
    assertTrue(req1.equals(req2));
    assertTrue(req2.equals(req1));

    req2 = new ValueRequirement(ValueRequirementNames.VOLATILITY_SURFACE, ComputationTargetType.PRIMITIVE, USD);
    assertFalse(req1.equals(req2));
    req2 = new ValueRequirement(ValueRequirementNames.DISCOUNT_CURVE, ComputationTargetType.POSITION, POSITION.getUniqueId());
    assertFalse(req1.equals(req2));
    req2 = new ValueRequirement(ValueRequirementNames.DISCOUNT_CURVE, ComputationTargetType.PRIMITIVE, GBP);
    assertFalse(req1.equals(req2));
    req2 = new ValueRequirement(ValueRequirementNames.DISCOUNT_CURVE, ComputationTargetType.NULL, (UniqueId) null);
    assertFalse(req1.equals(req2));
  }

  /**
   * Tests the hash-code.
   */
  public void testHashCode() {
    final ValueRequirement req1 = new ValueRequirement(ValueRequirementNames.DISCOUNT_CURVE, ComputationTargetType.PRIMITIVE, USD);
    ValueRequirement req2 = new ValueRequirement(ValueRequirementNames.DISCOUNT_CURVE, ComputationTargetType.PRIMITIVE, USD);

    assertTrue(req1.hashCode() == req2.hashCode());
    req2 = new ValueRequirement(ValueRequirementNames.VOLATILITY_SURFACE, ComputationTargetType.PRIMITIVE, USD);
    assertFalse(req1.hashCode() == req2.hashCode());
    req2 = new ValueRequirement(ValueRequirementNames.DISCOUNT_CURVE, ComputationTargetType.POSITION, POSITION.getUniqueId());
    assertFalse(req1.hashCode() == req2.hashCode());
    req2 = new ValueRequirement(ValueRequirementNames.DISCOUNT_CURVE, ComputationTargetType.PRIMITIVE, GBP);
    assertFalse(req1.hashCode() == req2.hashCode());
    req2 = new ValueRequirement(ValueRequirementNames.DISCOUNT_CURVE, ComputationTargetType.NULL, (UniqueId) null);
    assertFalse(req1.hashCode() == req2.hashCode());
  }

  /**
   * Tests the toString method.
   */
  public void testToString() {
    final ValueRequirement valueReq = new ValueRequirement(ValueRequirementNames.DISCOUNT_CURVE, ComputationTargetType.PRIMITIVE, USD);
    final String toString = valueReq.toString();
    assertNotNull(toString);
    assertTrue(toString.contains("USD"));
    assertTrue(toString.contains(ValueRequirementNames.DISCOUNT_CURVE));
    assertTrue(toString.contains(ComputationTargetType.PRIMITIVE.toString()));
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the Fudge encoding.
   */
  public void testFudgeEncoding() {
    final FudgeContext context = OpenGammaFudgeContext.getInstance();
    final FudgeSerializer serializationContext = new FudgeSerializer(context);
    final FudgeDeserializer deserializationContext = new FudgeDeserializer(context);
    ValueRequirement test = new ValueRequirement("DATA", ComputationTargetType.PRIMITIVE, USD);
    MutableFudgeMsg inMsg = serializationContext.objectToFudgeMsg(test);
    assertNotNull(inMsg);
    assertEquals(3, inMsg.getNumFields());
    FudgeMsg outMsg = context.deserialize(context.toByteArray(inMsg)).getMessage();
    ValueRequirement decoded = deserializationContext.fudgeMsgToObject(ValueRequirement.class, outMsg);
    assertEquals(test, decoded);
    test = new ValueRequirement("DATA", ComputationTargetType.PRIMITIVE, USD, ValueProperties.with(ValuePropertyNames.FUNCTION, "Foo").get());
    inMsg = serializationContext.objectToFudgeMsg(test);
    assertNotNull(inMsg);
    assertEquals(4, inMsg.getNumFields());
    outMsg = context.deserialize(context.toByteArray(inMsg)).getMessage();
    decoded = deserializationContext.fudgeMsgToObject(ValueRequirement.class, outMsg);
    assertEquals(test, decoded);
  }

}
