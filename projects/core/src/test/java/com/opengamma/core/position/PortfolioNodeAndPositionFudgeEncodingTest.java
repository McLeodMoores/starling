/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.position;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import java.math.BigDecimal;
import java.util.List;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.OffsetTime;

import com.opengamma.core.position.impl.SimpleCounterparty;
import com.opengamma.core.position.impl.SimplePortfolio;
import com.opengamma.core.position.impl.SimplePortfolioNode;
import com.opengamma.core.position.impl.SimplePosition;
import com.opengamma.core.position.impl.SimpleTrade;
import com.opengamma.core.security.impl.SimpleSecurityLink;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdSupplier;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Test {@link PortfolioNodeFudgeBuilder} and {@link PositionFudgeBuilder}.
 */
@Test(groups = TestGroup.UNIT)
public class PortfolioNodeAndPositionFudgeEncodingTest extends AbstractFudgeBuilderTestCase {

  private static final Logger LOGGER = LoggerFactory.getLogger(PortfolioNodeAndPositionFudgeEncodingTest.class);

  private UniqueIdSupplier _uniqueIdSupplier;

  /**
   * Initialises the id supplier.
   */
  @BeforeMethod
  public void init() {
    _uniqueIdSupplier = new UniqueIdSupplier("PortfolioNodeBuilderTest");
  }

  private UniqueId nextUniqueId() {
    return _uniqueIdSupplier.get();
  }

  /**
   * Adds a child to a parent.
   *
   * @param parent  the parent
   * @param child  the child
   */
  private static void linkNodes(final SimplePortfolioNode parent, final SimplePortfolioNode child) {
    child.setParentNodeId(parent.getUniqueId());
    parent.addChildNode(child);
  }

  private SimplePortfolioNode[] createPortfolioNodes() {
    final SimplePortfolioNode[] nodes = new SimplePortfolioNode[7];
    for (int i = 0; i < nodes.length; i++) {
      nodes[i] = new SimplePortfolioNode(nextUniqueId(), "node " + i);
    }
    linkNodes(nodes[0], nodes[1]);
    linkNodes(nodes[0], nodes[2]);
    linkNodes(nodes[1], nodes[3]);
    linkNodes(nodes[1], nodes[4]);
    linkNodes(nodes[2], nodes[5]);
    linkNodes(nodes[2], nodes[6]);
    return nodes;
  }

  private void addPositions(final SimplePortfolioNode node, final int num) {
    for (int i = 0; i < num; i++) {
      node.addPosition(new SimplePosition(nextUniqueId(), new BigDecimal(10), ExternalId.of("Security", "Foo")));
    }
  }

  private SimplePortfolioNode createPortfolioWithPositions() {
    final SimplePortfolioNode[] nodes = createPortfolioNodes();
    addPositions(nodes[1], 1);
    addPositions(nodes[3], 2);
    addPositions(nodes[5], 1);
    addPositions(nodes[6], 2);
    return nodes[0];
  }

  private SimplePortfolio createPortfolio() {
    final SimplePortfolioNode[] nodes = createPortfolioNodes();
    addPositions(nodes[1], 1);
    addPositions(nodes[3], 2);
    addPositions(nodes[5], 1);
    addPositions(nodes[6], 2);
    final SimplePortfolio portfolio = new SimplePortfolio("portfolio", nodes[0]);
    portfolio.addAttribute("key100", "value100");
    portfolio.addAttribute("key200", "value200");
    return portfolio;
  }

  private static void assertPortfolioEquals(final Portfolio expected, final Portfolio actual) {
    assertNotNull(expected);
    assertNotNull(actual);
    LOGGER.debug("testing portfolio node {}", expected.getUniqueId());
    assertEquals(expected.getUniqueId(), actual.getUniqueId());
    assertEquals(expected.getName(), actual.getName());
    assertPortfolioNodeEquals(expected.getRootNode(), actual.getRootNode());
  }

  private static void assertPortfolioNodeEquals(final PortfolioNode expected, final PortfolioNode actual) {
    assertNotNull(expected);
    assertNotNull(actual);
    LOGGER.debug("testing portfolio node {}", expected.getUniqueId());
    assertEquals(expected.getUniqueId(), actual.getUniqueId());
    assertEquals(expected.getName(), actual.getName());
    assertEquals(expected.size(), actual.size());
    assertEquals(expected.getParentNodeId(), actual.getParentNodeId());
    final List<PortfolioNode> expectedChildren = expected.getChildNodes();
    final List<PortfolioNode> actualChildren = actual.getChildNodes();
    assertNotNull(expectedChildren);
    assertNotNull(actualChildren);
    assertEquals(expectedChildren.size(), actualChildren.size());
    for (int i = 0; i < expectedChildren.size(); i++) {
      LOGGER.debug("testing child {} of {}", i, actual.getUniqueId());
      assertPortfolioNodeEquals(expectedChildren.get(i), actualChildren.get(i));
    }
    final List<Position> expectedPositions = expected.getPositions();
    final List<Position> actualPositions = actual.getPositions();
    assertNotNull(expectedPositions);
    assertNotNull(actualPositions);
    assertEquals(expectedPositions.size(), actualPositions.size());
    for (int i = 0; i < expectedPositions.size(); i++) {
      LOGGER.debug("testing position {} of {}", i, actual.getUniqueId());
      assertPositionEquals(expectedPositions.get(i), actualPositions.get(i));
    }
  }

  private static void assertPositionEquals(final Position expected, final Position actual) {
    assertNotNull(expected);
    assertNotNull(actual);
    LOGGER.debug("testing position {}", expected.getUniqueId());
    assertEquals(expected.getUniqueId(), actual.getUniqueId());
    assertEquals(expected.getQuantity(), actual.getQuantity());
    assertEquals(expected.getSecurityLink(), actual.getSecurityLink());
  }

  private FudgeMsg runPortfolioNodeTest(final PortfolioNode original) {
    final FudgeMsg message = getFudgeSerializer().objectToFudgeMsg(original);
    LOGGER.debug("Message = {}", message);
    final PortfolioNode portfolio = getFudgeDeserializer().fudgeMsgToObject(PortfolioNode.class, message);
    assertPortfolioNodeEquals(original, portfolio);
    return message;
  }

  private FudgeMsg runPortfolioTest(final Portfolio original) {
    final FudgeMsg message = getFudgeSerializer().objectToFudgeMsg(original);
    LOGGER.debug("Message = {}", message);
    final Portfolio portfolio = getFudgeDeserializer().fudgeMsgToObject(Portfolio.class, message);
    assertPortfolioEquals(original, portfolio);
    return message;
  }

  private int countParents(final FudgeMsg message) {
    int count = 0;
    for (final FudgeField field : message) {
      if (PortfolioNodeFudgeBuilder.PARENT_FIELD_NAME.equals(field.getName())) {
        LOGGER.debug("Found parent ref {}", field.getValue());
        count++;
      } else if (field.getValue() instanceof FudgeMsg) {
        count += countParents((FudgeMsg) field.getValue());
      }
    }
    return count;
  }

  /**
   * Tests a cycle of an empty portfolio.
   */
  public void testPortfolio() {
    final FudgeMsg message = runPortfolioNodeTest(createPortfolioNodes()[0]);
    assertEquals(0, countParents(message));
  }

  /**
   * Tests a cycle of a portfolio.
   */
  public void testPortfolioWithPositions() {
    final FudgeMsg message = runPortfolioNodeTest(createPortfolioWithPositions());
    assertEquals(0, countParents(message));
  }

  /**
   * Tests a cycle of a portfolio.
   */
  public void testTopLevelPortfolioWithPositions() {
    final FudgeMsg message = runPortfolioTest(createPortfolio());
    assertEquals(0, countParents(message));
  }

  /**
   * Tests a cycle of a portfolio.
   */
  public void testPortfolioWithParent() {
    final SimplePortfolioNode root = createPortfolioNodes()[0];
    root.setParentNodeId(nextUniqueId());
    final FudgeMsg message = runPortfolioNodeTest(root);
    assertEquals(1, countParents(message));
  }

  private FudgeMsg runPositionTest(final Position original) {
    final FudgeMsg message = getFudgeSerializer().objectToFudgeMsg(original);
    LOGGER.debug("Message = {}", message);
    final Position position = getFudgeDeserializer().fudgeMsgToObject(Position.class, message);
    assertPositionEquals(original, position);
    return message;
  }

  /**
   * Tests a cycle of an empty position.
   */
  public void testEmptyPosition() {
    final SimplePosition position = new SimplePosition();
    final FudgeMsg message = runPositionTest(position);
    assertEquals(0, countParents(message));
  }

  /**
   * Tests a cycle of a position.
   */
  public void testSimplePosition() {
    final SimplePosition position = new SimplePosition(nextUniqueId(), new BigDecimal(100),
        ExternalIdBundle.of(ExternalId.of("Scheme 1", "Id 1"), ExternalId.of("Scheme 2", "Id 2")));
    final FudgeMsg message = runPositionTest(position);
    assertEquals(0, countParents(message));
  }

  /**
   * Tests a cycle of a position.
   */
  public void testPosition() {
    final SimplePosition position = new SimplePosition(nextUniqueId(), new BigDecimal(100),
        ExternalIdBundle.of(ExternalId.of("Scheme 1", "Id 1"), ExternalId.of("Scheme 2", "Id 2")));
    position.addAttribute("key1", "value1");
    position.addAttribute("key2", "value2");
    final SimpleSecurityLink securityLink = (SimpleSecurityLink) position.getSecurityLink();
    securityLink.setObjectId(ObjectId.of("oid", "10000"));
    final FudgeMsg message = runPositionTest(position);
    assertEquals(0, countParents(message));
  }

  /**
   * Tests a cycle of a position.
   */
  public void testWithTrades() {
    final SimplePosition position = new SimplePosition();
    position.addAttribute("key1", "value1");
    position.addAttribute("key2", "value2");
    final SimpleCounterparty cpty = new SimpleCounterparty(ExternalId.of(Counterparty.DEFAULT_SCHEME, "abc"));
    final Trade trade1 = new SimpleTrade(new SimpleSecurityLink(ExternalId.of("scheme", "id 1")), BigDecimal.TEN, cpty, LocalDate.now(), OffsetTime.now());
    final Trade trade2 = new SimpleTrade(new SimpleSecurityLink(ExternalId.of("scheme", "id 2")), BigDecimal.ONE, cpty, LocalDate.now(), OffsetTime.now());
    position.addTrade(trade1);
    position.addTrade(trade2);
    final FudgeMsg message = runPositionTest(position);
    assertEquals(0, countParents(message));
  }
}
