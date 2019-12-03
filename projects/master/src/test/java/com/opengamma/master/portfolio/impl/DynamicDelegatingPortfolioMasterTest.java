/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.portfolio.impl;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.DataNotFoundException;
import com.opengamma.id.ObjectIdSupplier;
import com.opengamma.id.UniqueId;
import com.opengamma.master.portfolio.ManageablePortfolio;
import com.opengamma.master.portfolio.ManageablePortfolioNode;
import com.opengamma.master.portfolio.PortfolioDocument;
import com.opengamma.util.test.TestGroup;

/**
 * Tests {@link DynamicDelegatingPortfolioMaster}.
 */
@Test(groups = TestGroup.UNIT)
public class DynamicDelegatingPortfolioMasterTest {

  private final String _schemeA = "A";
  private final String _schemeB = "B";
  private final ObjectIdSupplier _schemeAProvider = new ObjectIdSupplier(_schemeA);
  private final ObjectIdSupplier _schemeBProvider = new ObjectIdSupplier(_schemeB);

  /**
   *
   */
  @Test(expectedExceptions = DataNotFoundException.class)
  void testDefaultDelegateShouldNotFindAnyData() {
    final UniqueId doesNotExist = UniqueId.of(_schemeA, "DoesNotExist");
    final DynamicDelegatingPortfolioMaster sut = new DynamicDelegatingPortfolioMaster();
    sut.get(doesNotExist);
  }

  /**
   *
   */
  @Test
  void testAddSomeDelegates() {
    final PortfolioDocument portA = generatePortfolio("PortA", _schemeAProvider);
    final PortfolioDocument portB = generatePortfolio("PortB", _schemeBProvider);

    final DynamicDelegatingPortfolioMaster sut = new DynamicDelegatingPortfolioMaster();

    sut.register(_schemeA, new InMemoryPortfolioMaster(_schemeAProvider));
    final PortfolioDocument addedPortA = sut.add(_schemeA, portA);
    assertEquals(addedPortA, portA, "adding the document had unexpected side effect");
    PortfolioDocument fetchedPortA = sut.get(addedPortA.getUniqueId());
    assertEquals(fetchedPortA, portA, "unable to fetch same document right after adding");

    sut.register(_schemeB, new InMemoryPortfolioMaster(_schemeBProvider));
    final PortfolioDocument addedPortB = sut.add(_schemeB, portB);
    assertEquals(addedPortB, portB, "adding the document had unexpected side effect");
    PortfolioDocument fetchedPortB = sut.get(addedPortB.getUniqueId());
    assertEquals(fetchedPortB, portB, "unable to fetch same document right after adding");

    fetchedPortA = sut.get(addedPortA.getUniqueId());
    assertEquals(fetchedPortA, portA, "unable to fetch document a second time");

    fetchedPortB = sut.get(addedPortB.getUniqueId());
    assertEquals(fetchedPortB, portB, "unable to fetch document a second time");
  }

  /**
   *
   */
  @Test(expectedExceptions = DataNotFoundException.class)
  void testRemovingDelegates() {
    final PortfolioDocument portA = generatePortfolio("PortA", _schemeAProvider);
    final PortfolioDocument portB = generatePortfolio("PortB", _schemeBProvider);

    final DynamicDelegatingPortfolioMaster sut = new DynamicDelegatingPortfolioMaster();

    sut.register(_schemeA, new InMemoryPortfolioMaster(_schemeAProvider));
    final UniqueId addedPort = sut.add(_schemeA, portA).getUniqueId();

    sut.register(_schemeB, new InMemoryPortfolioMaster(_schemeBProvider));
    sut.add(_schemeB, portB);

    sut.deregister(_schemeA);

    sut.get(addedPort); // will throw data not found exception because we deregistered scheme A
  }

  private PortfolioDocument generatePortfolio(final String name, final ObjectIdSupplier provider) {
    final ManageablePortfolioNode rootNode = generatePortfolioNodes(name, provider, 2, 2);
    final PortfolioDocument document = new PortfolioDocument(new ManageablePortfolio(name, rootNode));
    return document;
  }

  private ManageablePortfolioNode generatePortfolioNodes(final String namePrefix, final ObjectIdSupplier provider, final int width, final int depth) {
    final ManageablePortfolioNode root = new ManageablePortfolioNode(namePrefix);
    root.addPosition(provider.get());
    if (depth > 0) {
      for (int i = 0; i < width; i++) {
        root.addChildNode(generatePortfolioNodes(namePrefix + "-" + depth + "-" + i, provider, width, depth - 1));
      }
    }
    return root;
  }
}
