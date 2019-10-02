/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.fudgemsg;

import org.testng.annotations.Test;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.target.ComputationTargetReference;
import com.opengamma.engine.target.ComputationTargetRequirement;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.id.ExternalId;
import com.opengamma.id.UniqueId;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the {@link ComputationTargetReferenceFudgeBuilder} class.
 */
@Test(groups = TestGroup.UNIT)
public class ComputationTargetReferenceFudgeBuilderTest extends AbstractFudgeBuilderTestCase {

  /**
   *
   */
  public void testRequirement() {
    assertEncodeDecodeCycle(ComputationTargetReference.class, new ComputationTargetRequirement(ComputationTargetType.PRIMITIVE, ExternalId.of("Foo", "Bar")));
  }

  /**
   *
   */
  public void testSpecification() {
    assertEncodeDecodeCycle(ComputationTargetReference.class, new ComputationTargetSpecification(ComputationTargetType.SECURITY, UniqueId.of("Foo", "Bar")));
  }

  /**
   *
   */
  public void testRequirementNull() {
    assertEncodeDecodeCycle(ComputationTargetReference.class, new ComputationTargetSpecification(ComputationTargetType.NULL, null));
    // assertEncodeDecodeCycle(ComputationTargetReference.class, new ComputationTargetRequirement(ComputationTargetType.NULL, (ExternalIdBundle) null));
  }

  /**
   *
   */
  public void testSpecificationNull() {
    assertEncodeDecodeCycle(ComputationTargetReference.class, new ComputationTargetSpecification(ComputationTargetType.NULL, null));
  }

  /**
   *
   */
  public void testRequirementNested() {
    ComputationTargetRequirement req = new ComputationTargetRequirement(ComputationTargetType.SECURITY, ExternalId.of("Foo", "Bar"));
    req = req.containing(ComputationTargetType.SECURITY, ExternalId.of("Foo", "Underlying"));
    assertEncodeDecodeCycle(ComputationTargetReference.class, req);
  }

  /**
   *
   */
  public void testSpecificationNested() {
    ComputationTargetSpecification spec = new ComputationTargetSpecification(ComputationTargetType.SECURITY, UniqueId.of("Foo", "Bar"));
    spec = spec.containing(ComputationTargetType.SECURITY, UniqueId.of("Foo", "Underlying"));
    assertEncodeDecodeCycle(ComputationTargetReference.class, spec);
  }

  /**
   *
   */
  public void testRequirementMultiple() {
    final ComputationTargetRequirement req = new ComputationTargetRequirement(ComputationTargetType.POSITION.or(ComputationTargetType.SECURITY),
        ExternalId.of("Foo", "Bar"));
    assertEncodeDecodeCycle(ComputationTargetReference.class, req);
  }

  /**
   *
   */
  public void testSpecificationMultiple() {
    final ComputationTargetSpecification spec = new ComputationTargetSpecification(ComputationTargetType.POSITION.or(ComputationTargetType.SECURITY),
        UniqueId.of("Foo", "Bar"));
    assertEncodeDecodeCycle(ComputationTargetReference.class, spec);
  }

}
