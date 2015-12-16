package com.mcleodmoores.quandl.robustwrapper;
import org.testng.annotations.Test;

import com.jimmoores.quandl.MultiDataSetRequest;
import com.jimmoores.quandl.QuandlCodeRequest;
import com.jimmoores.quandl.QuandlSession;
import com.mcleodmoores.quandl.robustwrapper.RobustQuandlSession;


@Test
public class RobustQuandlSessionTest {
  @Test
  public void testMultiGet() {
    RobustQuandlSession session = new RobustQuandlSession(QuandlSession.create());
    session.getDataSets(MultiDataSetRequest.Builder.of(QuandlCodeRequest.allColumns("FRED/DSWP10")).build());
  }
}
