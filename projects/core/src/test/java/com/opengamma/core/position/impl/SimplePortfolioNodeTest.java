/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.position.impl;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotSame;
import static org.testng.AssertJUnit.assertSame;

import java.math.BigDecimal;
import java.util.Arrays;

import org.testng.annotations.Test;

import com.opengamma.id.ExternalId;
import com.opengamma.id.UniqueId;
import com.opengamma.util.test.TestGroup;

/**
 * Test {@link SimplePortfolioNode}.
 */
@Test(groups = TestGroup.UNIT)
public class SimplePortfolioNodeTest {

  public void test_construction() {
    final SimplePortfolioNode test = new SimplePortfolioNode();
    assertEquals(null, test.getUniqueId());
    assertEquals("", test.getName());
    assertEquals(0, test.getChildNodes().size());
    assertEquals(0, test.getPositions().size());
    assertEquals(0, test.size());
    assertEquals("PortfolioNode[, 0 child-nodes, 0 positions]", test.toString());
  }

  //-------------------------------------------------------------------------
  public void test_construction_String() {
    final SimplePortfolioNode test = new SimplePortfolioNode(UniqueId.of("A", "B"), "Name");
    assertEquals(UniqueId.of("A", "B"), test.getUniqueId());
    assertEquals("Name", test.getName());
    assertEquals(0, test.getChildNodes().size());
    assertEquals(0, test.getPositions().size());
    assertEquals(0, test.size());
    assertEquals("PortfolioNode[A~B, 0 child-nodes, 0 positions]", test.toString());
  }

  public void test_construction_String_null() {
    final SimplePortfolioNode test = new SimplePortfolioNode(UniqueId.of("A", "B"), null);
    assertEquals(UniqueId.of("A", "B"), test.getUniqueId());
    assertEquals("", test.getName());
    assertEquals(0, test.getChildNodes().size());
    assertEquals(0, test.getPositions().size());
    assertEquals(0, test.size());
    assertEquals("PortfolioNode[A~B, 0 child-nodes, 0 positions]", test.toString());
  }

  //-------------------------------------------------------------------------
  public void test_setUniqueId() {
    final SimplePortfolio test = new SimplePortfolio(UniqueId.of("Scheme", "Id"), "Name");
    test.setUniqueId(UniqueId.of("Scheme2", "Id2"));
    assertEquals(UniqueId.of("Scheme2", "Id2"), test.getUniqueId());
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_setUniqueId_null() {
    final SimplePortfolio test = new SimplePortfolio(UniqueId.of("Scheme", "Id"), "Name");
    test.setUniqueId(null);
  }

  //-------------------------------------------------------------------------
  public void test_setName() {
    final SimplePortfolio test = new SimplePortfolio(UniqueId.of("Scheme", "Id"), "Name");
    test.setName("Name2");
    assertEquals("Name2", test.getName());
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_setName_null() {
    final SimplePortfolio test = new SimplePortfolio(UniqueId.of("Scheme", "Id"), "Name");
    test.setName(null);
  }

  //-------------------------------------------------------------------------
  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void test_getChildNodes_immutable() {
    final SimplePortfolioNode test = new SimplePortfolioNode(UniqueId.of("A", "test"), "test");
    final SimplePortfolioNode child = new SimplePortfolioNode(UniqueId.of("A", "child"), "child");
    test.getChildNodes().add(child);
  }

  public void test_addChildNode() {
    final SimplePortfolioNode test = new SimplePortfolioNode(UniqueId.of("A", "test"), "test");
    final SimplePortfolioNode child = new SimplePortfolioNode(UniqueId.of("A", "child"), "child");
    child.setParentNodeId(test.getUniqueId());
    test.addChildNode(child);
    assertEquals(1, test.getChildNodes().size());
    assertEquals(child, test.getChildNodes().get(0));
    assertEquals(0, test.getPositions().size());
    assertEquals(1, test.size());
  }

  public void test_addChildNodes() {
    final SimplePortfolioNode test = new SimplePortfolioNode(UniqueId.of("A", "test"), "test");
    final SimplePortfolioNode child0 = new SimplePortfolioNode(UniqueId.of("A", "child0"), "child0");
    final SimplePortfolioNode child1 = new SimplePortfolioNode(UniqueId.of("A", "child1"), "child1");
    child0.setParentNodeId(test.getUniqueId());
    child1.setParentNodeId(test.getUniqueId());
    test.addChildNodes(Arrays.asList(child0, child1));
    assertEquals(2, test.getChildNodes().size());
    assertEquals(child0, test.getChildNodes().get(0));
    assertEquals(child1, test.getChildNodes().get(1));
    assertEquals(0, test.getPositions().size());
    assertEquals(2, test.size());
  }

  public void test_removeChildNode_match() {
    final SimplePortfolioNode test = new SimplePortfolioNode(UniqueId.of("A", "test"), "test");
    final SimplePortfolioNode child = new SimplePortfolioNode(UniqueId.of("A", "child"), "child");
    child.setParentNodeId(test.getUniqueId());
    test.addChildNode(child);
    assertEquals(1, test.getChildNodes().size());
    test.removeChildNode(child);
    assertEquals(0, test.getChildNodes().size());
  }

  public void test_removeChildNode_noMatch() {
    final SimplePortfolioNode test = new SimplePortfolioNode(UniqueId.of("A", "test"), "test");
    final SimplePortfolioNode child = new SimplePortfolioNode(UniqueId.of("A", "child"), "child");
    final SimplePortfolioNode removing = new SimplePortfolioNode(UniqueId.of("A", "removing"), "removing");
    child.setParentNodeId(test.getUniqueId());
    test.addChildNode(child);
    assertEquals(1, test.getChildNodes().size());
    test.removeChildNode(removing);
    assertEquals(1, test.getChildNodes().size());
    assertEquals(child, test.getChildNodes().get(0));
  }

  //-------------------------------------------------------------------------
  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void test_getPositions_immutable() {
    final SimplePortfolioNode test = new SimplePortfolioNode();
    final SimplePosition child = new SimplePosition(BigDecimal.ONE, ExternalId.of("K", "V"));
    test.getPositions().add(child);
  }

  public void test_addPosition() {
    final SimplePortfolioNode test = new SimplePortfolioNode();
    final SimplePosition child = new SimplePosition(BigDecimal.ONE, ExternalId.of("K", "V"));
    test.addPosition(child);
    assertEquals(1, test.getPositions().size());
    assertEquals(child, test.getPositions().get(0));
    assertEquals(0, test.getChildNodes().size());
    assertEquals(1, test.size());
  }

  public void test_addPositions() {
    final SimplePortfolioNode test = new SimplePortfolioNode();
    final SimplePosition child0 = new SimplePosition(BigDecimal.ONE, ExternalId.of("K", "V"));
    final SimplePosition child1 = new SimplePosition(BigDecimal.ONE, ExternalId.of("K", "V"));
    test.addPositions(Arrays.asList(child0, child1));
    assertEquals(2, test.getPositions().size());
    assertEquals(child0, test.getPositions().get(0));
    assertEquals(child1, test.getPositions().get(1));
    assertEquals(0, test.getChildNodes().size());
    assertEquals(2, test.size());
  }

  public void test_removePosition_match() {
    final SimplePortfolioNode test = new SimplePortfolioNode();
    final SimplePosition child = new SimplePosition(BigDecimal.ONE, ExternalId.of("K", "V"));
    test.addPosition(child);
    assertEquals(1, test.getPositions().size());
    test.removePosition(child);
    assertEquals(0, test.getPositions().size());
  }

  public void test_removePosition_noMatch() {
    final SimplePortfolioNode test = new SimplePortfolioNode();
    final SimplePosition child = new SimplePosition(BigDecimal.ONE, ExternalId.of("K", "V"));
    final SimplePosition removing = new SimplePosition(BigDecimal.ONE, ExternalId.of("K", "OTHER"));
    test.addPosition(child);
    assertEquals(1, test.getPositions().size());
    test.removePosition(removing);
    assertEquals(1, test.getPositions().size());
    assertEquals(child, test.getPositions().get(0));
  }

  //-------------------------------------------------------------------------
  public void test_size() {
    final SimplePortfolioNode test = new SimplePortfolioNode();
    final SimplePortfolioNode child1 = new SimplePortfolioNode();
    final SimplePosition child2 = new SimplePosition(BigDecimal.ONE, ExternalId.of("K", "V"));
    test.addChildNode(child1);
    test.addPosition(child2);
    assertEquals(1, test.getChildNodes().size());
    assertEquals(1, test.getPositions().size());
    assertEquals(2, test.size());
  }

  //-------------------------------------------------------------------------
  public void test_getNode_UniqueId() {
    final SimplePortfolioNode root = new SimplePortfolioNode(UniqueId.of("Root", "A"), "Name");
    final SimplePortfolioNode child1 = new SimplePortfolioNode(UniqueId.of("Child", "A"), "Name");
    root.addChildNode(child1);
    final SimplePortfolioNode child2 = new SimplePortfolioNode(UniqueId.of("Child", "B"), "Name");
    child2.setParentNodeId(root.getUniqueId());
    root.addChildNode(child2);
    assertSame(root, root.getNode(UniqueId.of("Root", "A")));
    assertNotSame(child1, root.getNode(UniqueId.of("Child", "A")));
    // equal except for the parent link
    assertFalse(child1.equals(root.getNode(UniqueId.of("Child", "A"))));
    child1.setParentNodeId(root.getUniqueId());
    assertEquals(child1, root.getNode(UniqueId.of("Child", "A")));
    assertSame(child2, root.getNode(UniqueId.of("Child", "B")));
    assertEquals(null, root.getNode(UniqueId.of("NotFound", "A")));
  }

  public void test_getPosition_UniqueId() {
    final SimplePortfolioNode root = new SimplePortfolioNode(UniqueId.of("Root", "A"), "Name");
    final SimplePortfolioNode child = new SimplePortfolioNode(UniqueId.of("Child", "A"), "Name");
    final SimplePosition position1 = new SimplePosition(UniqueId.of("Child", "A"), BigDecimal.ZERO, ExternalId.of("A", "B"));
    final SimplePosition position2 = new SimplePosition(UniqueId.of("Child", "B"), BigDecimal.ZERO, ExternalId.of("A", "B"));
    child.addPosition(position1);
    child.addPosition(position2);
    root.addChildNode(child);
    assertNotSame(position1, root.getPosition(UniqueId.of("Child", "A")));
    // equal except for the parent link
    assertEquals(position1, root.getPosition(UniqueId.of("Child", "A")));
    assertEquals(position2, root.getPosition(UniqueId.of("Child", "B")));
    assertEquals(null, root.getPosition(UniqueId.of("NotFound", "A")));
  }

}
