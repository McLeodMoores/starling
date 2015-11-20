/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import java.math.BigDecimal;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.position.impl.MockPositionSource;
import com.opengamma.core.position.impl.SimplePortfolio;
import com.opengamma.core.position.impl.SimplePortfolioNode;
import com.opengamma.core.position.impl.SimplePosition;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.id.ExternalId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdSupplier;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for the {@link PortfolioStructure} class.
 */
@Test(groups = TestGroup.UNIT)
public class PortfolioStructureTest {

  private FunctionCompilationContext _context;
  private SimplePortfolioNode _root;
  private SimplePortfolioNode _child1;
  private SimplePortfolioNode _child2;
  private SimplePortfolioNode _badChild;
  private SimplePosition _position1;
  private SimplePosition _position2;
  private SimplePosition _badPosition;

  @BeforeMethod
  public void createPortfolio() {
    final UniqueIdSupplier uid = new UniqueIdSupplier("Test");
    final MockPositionSource positionSource = new MockPositionSource();
    final PortfolioStructure resolver = new PortfolioStructure(positionSource);
    final SimplePortfolio portfolio = new SimplePortfolio(uid.get(), "Test");
    _root = new SimplePortfolioNode(uid.get(), "root");
    _child1 = new SimplePortfolioNode(uid.get(), "child 1");
    _child2 = new SimplePortfolioNode(uid.get(), "child 2");
    _position1 = new SimplePosition(uid.get(), new BigDecimal(10), ExternalId.of("Security", "Foo"));
    _child2.addPosition(_position1);
    _position2 = new SimplePosition(uid.get(), new BigDecimal(20), ExternalId.of("Security", "Bar"));
    _child2.addPosition(_position2);
    _child2.setParentNodeId(_child1.getUniqueId());
    _child1.addChildNode(_child2);
    _child1.setParentNodeId(_root.getUniqueId());
    _root.addChildNode(_child1);
    portfolio.setRootNode(_root);
    positionSource.addPortfolio(portfolio);
    _badChild = new SimplePortfolioNode(uid.get(), "child 3");
    _badChild.setParentNodeId(uid.get());
    _badPosition = new SimplePosition(uid.get(), new BigDecimal(10), ExternalId.of("Security", "Cow"));
    _context = new FunctionCompilationContext();
    _context.setPortfolioStructure(resolver);
  }

  private PortfolioStructure getPortfolioStructure() {
    return _context.getPortfolioStructure();
  }

  //-------------------------------------------------------------------------
  public void test_getParentNode_portfolioNode() {
    final PortfolioStructure resolver = getPortfolioStructure();
    assertEquals(_child1, resolver.getParentNode(_child2));
    assertEquals(_root, resolver.getParentNode(_child1));
    assertEquals(null, resolver.getParentNode(_root));
  }

  @Test(expectedExceptions = DataNotFoundException.class)
  public void test_getParentNode_portfolioNode_badChild() {
    final PortfolioStructure resolver = getPortfolioStructure();
    resolver.getParentNode(_badChild);
  }

  //-------------------------------------------------------------------------
  public void test_getParentNode_position() {
    final PortfolioStructure resolver = getPortfolioStructure();
    assertNotNull(resolver);
    assertEquals(_child2,
        resolver.getParentNode(new ComputationTarget(new ComputationTargetSpecification(ComputationTargetType.PORTFOLIO_NODE, _child2.getUniqueId()).containing(
            ComputationTargetType.POSITION, _position1.getUniqueId()), _position1)));
    assertEquals(_child2,
        resolver.getParentNode(new ComputationTarget(new ComputationTargetSpecification(ComputationTargetType.PORTFOLIO_NODE, _root.getUniqueId()).containing(
            ComputationTargetType.PORTFOLIO_NODE, _child2.getUniqueId()).containing(ComputationTargetType.POSITION, _position2.getUniqueId()), _position2)));
  }

  @Test(expectedExceptions = {IllegalArgumentException.class })
  public void test_getParentNode_position_badTarget() {
    final PortfolioStructure resolver = getPortfolioStructure();
    assertNotNull(resolver);
    resolver.getParentNode(new ComputationTarget(ComputationTargetType.POSITION, _position1));
  }

  @Test(expectedExceptions = DataNotFoundException.class)
  public void test_getParentNode_position_badChild() {
    final PortfolioStructure resolver = getPortfolioStructure();
    resolver.getParentNode(new ComputationTarget(new ComputationTargetSpecification(ComputationTargetType.PORTFOLIO_NODE, UniqueId.of("Invalid", "0")).containing(ComputationTargetType.POSITION,
        _badPosition.getUniqueId()), _badPosition));
  }

  //-------------------------------------------------------------------------
  public void test_getRootPortfolioNode_portfolioNode() {
    final PortfolioStructure resolver = getPortfolioStructure();
    assertNotNull(resolver);
    assertEquals(_root, resolver.getRootPortfolioNode(_child1));
    assertEquals(_root, resolver.getRootPortfolioNode(_child2));
    assertEquals(_root, resolver.getRootPortfolioNode(_root));
  }

  @Test(expectedExceptions = DataNotFoundException.class)
  public void test_getRootPortfolioNode_portfolioNode_badChild() {
    final PortfolioStructure resolver = getPortfolioStructure();
    resolver.getRootPortfolioNode(_badChild);
  }

  //-------------------------------------------------------------------------
  public void test_getRootPortfolioNode_position() {
    final PortfolioStructure resolver = getPortfolioStructure();
    assertNotNull(resolver);
    assertEquals(_root,
        resolver.getRootPortfolioNode(new ComputationTarget(new ComputationTargetSpecification(ComputationTargetType.PORTFOLIO_NODE, _child2.getUniqueId()).containing(ComputationTargetType.POSITION,
            _position1.getUniqueId()), _position1)));
    assertEquals(
        _root,
        resolver.getRootPortfolioNode(new ComputationTarget(new ComputationTargetSpecification(ComputationTargetType.PORTFOLIO_NODE, _root.getUniqueId()).containing(
            ComputationTargetType.PORTFOLIO_NODE, _child2.getUniqueId()).containing(
            ComputationTargetType.POSITION, _position2.getUniqueId()), _position2)));
  }

  @Test(expectedExceptions = {IllegalArgumentException.class })
  public void test_getRootPortfolioNode_position_badTarget() {
    final PortfolioStructure resolver = getPortfolioStructure();
    resolver.getRootPortfolioNode(new ComputationTarget(ComputationTargetType.POSITION, _position1));
  }

  @Test(expectedExceptions = DataNotFoundException.class)
  public void test_getRootPortfolioNode_position_badChild() {
    final PortfolioStructure resolver = getPortfolioStructure();
    resolver.getRootPortfolioNode(new ComputationTarget(new ComputationTargetSpecification(ComputationTargetType.PORTFOLIO_NODE, UniqueId.of("Invalid", "0")).containing(
        ComputationTargetType.POSITION, _badPosition.getUniqueId()), _badPosition));
  }

}
