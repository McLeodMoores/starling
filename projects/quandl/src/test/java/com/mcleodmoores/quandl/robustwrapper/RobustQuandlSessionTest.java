package com.mcleodmoores.quandl.robustwrapper;
import org.testng.annotations.Test;

import com.jimmoores.quandl.MultiDataSetRequest;
import com.jimmoores.quandl.QuandlCodeRequest;
import com.jimmoores.quandl.QuandlSession;
import com.opengamma.util.test.TestGroup;


@Test(groups = TestGroup.UNIT)
public class RobustQuandlSessionTest {
  @Test
  public void testMultiGet() {
    final RobustQuandlSession session = new RobustQuandlSession(QuandlSession.create());
    session.getDataSets(MultiDataSetRequest.Builder.of(QuandlCodeRequest.allColumns("FRED/DSWP10")).build());
  }
}
