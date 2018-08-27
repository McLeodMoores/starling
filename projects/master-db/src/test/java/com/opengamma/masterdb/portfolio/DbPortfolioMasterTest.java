/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.portfolio;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.opengamma.master.portfolio.ManageablePortfolio;
import com.opengamma.master.portfolio.ManageablePortfolioNode;
import com.opengamma.master.portfolio.PortfolioDocument;
import com.opengamma.master.portfolio.PortfolioSearchRequest;
import com.opengamma.master.portfolio.PortfolioSearchResult;
import com.opengamma.util.test.AbstractDbTest;
import com.opengamma.util.test.DbTest;
import com.opengamma.util.test.TestGroup;

/**
 * Test DbPortfolioMaster.
 */
@Test(groups = TestGroup.UNIT_DB)
public class DbPortfolioMasterTest extends AbstractDbTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(DbPortfolioMasterTest.class);

  private DbPortfolioMaster _prtMaster;

  @Factory(dataProvider = "databases", dataProviderClass = DbTest.class)
  public DbPortfolioMasterTest(final String databaseType, final String databaseVersion) {
    super(databaseType, databaseVersion);
    LOGGER.info("running testcases for {}", databaseType);
  }

  //-------------------------------------------------------------------------
  @Override
  protected void doSetUp() {
    _prtMaster = new DbPortfolioMaster(getDbConnector());
  }

  @Override
  protected void doTearDown() {
    _prtMaster = null;
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_basics() throws Exception {
    assertNotNull(_prtMaster);
    assertEquals(true, _prtMaster.getUniqueIdScheme().equals("DbPrt"));
    assertNotNull(_prtMaster.getDbConnector());
    assertNotNull(_prtMaster.getClock());
  }

  @Test(description = "[PLAT-1723]")
  public void test_duplicate_names() throws Exception {
    final PortfolioDocument a = new PortfolioDocument();
    a.setPortfolio( new ManageablePortfolio("Name"));
    _prtMaster.add(a);

    final PortfolioDocument b = new PortfolioDocument();
    b.setPortfolio( new ManageablePortfolio("Name"));
    _prtMaster.add(b);

    final PortfolioSearchResult search = _prtMaster.search(new PortfolioSearchRequest());
    assertEquals(2, search.getPortfolios().size());
  }

  @Test(description = "[PLAT-1723]")
  public void test_duplicate_names_complex() throws Exception {

    //Try to make the table big enough that database looses presumed order guarantees
    for (int i=0;i<10;i++)
    {
      final String portfolioName = "Portfolio";
      final PortfolioDocument a = new PortfolioDocument();
      a.setPortfolio( new ManageablePortfolio(portfolioName));
      _prtMaster.add(a);
      for (int j = 0;j<10;j++){
        final ManageablePortfolioNode child = new ManageablePortfolioNode("X");
        child.addChildNode(new ManageablePortfolioNode("Y"));
        a.getPortfolio().getRootNode().addChildNode(child);
        _prtMaster.update(a);
      }

      final PortfolioDocument b = new PortfolioDocument();
      b.setPortfolio( new ManageablePortfolio(portfolioName));
      for (int j = 0;j<10;j++){
        final ManageablePortfolioNode childB = new ManageablePortfolioNode("X");
        childB.addChildNode(new ManageablePortfolioNode("Y"));
        b.getPortfolio().getRootNode().addChildNode(childB);
      }
      _prtMaster.add(b);

      for (int j = 0;j<10;j++){
        final ManageablePortfolioNode child = new ManageablePortfolioNode("X");
        child.addChildNode(new ManageablePortfolioNode("Y"));
        a.getPortfolio().getRootNode().addChildNode(child);
        _prtMaster.update(a);

        final PortfolioSearchRequest request = new PortfolioSearchRequest();
        request.setName(portfolioName);
        final PortfolioSearchResult search = _prtMaster.search(request);
        assertEquals(2 * (i+1), search.getPortfolios().size());
      }

      final PortfolioSearchRequest request = new PortfolioSearchRequest();
      request.setName(portfolioName);
      final PortfolioSearchResult search = _prtMaster.search(request);
      assertEquals(2 * (i+1), search.getPortfolios().size());
    }
  }


  //-------------------------------------------------------------------------
  @Test
  public void test_toString() {
    assertEquals("DbPortfolioMaster[DbPrt]", _prtMaster.toString());
  }

}
