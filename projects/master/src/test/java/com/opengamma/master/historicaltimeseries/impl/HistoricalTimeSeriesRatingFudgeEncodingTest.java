/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.historicaltimeseries.impl;

import static com.opengamma.master.historicaltimeseries.impl.HistoricalTimeSeriesRatingFieldNames.DATA_PROVIDER_NAME;
import static com.opengamma.master.historicaltimeseries.impl.HistoricalTimeSeriesRatingFieldNames.DATA_SOURCE_NAME;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.testng.annotations.Test;

import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.test.TestGroup;

/**
 * Test {@link HistoricalTimeSeriesRating} Fudge.
 */
@Test(groups = TestGroup.UNIT)
public class HistoricalTimeSeriesRatingFudgeEncodingTest {

  private static final FudgeContext FUDGE_CONTEXT = OpenGammaFudgeContext.getInstance();

  public void fudgeEncoding() {
    List<HistoricalTimeSeriesRatingRule> rules = new ArrayList<HistoricalTimeSeriesRatingRule>();
    rules.add(HistoricalTimeSeriesRatingRule.of(DATA_SOURCE_NAME, "BLOOMBERG", 2));
    rules.add(HistoricalTimeSeriesRatingRule.of(DATA_SOURCE_NAME, "REUTERS", 1));
    rules.add(HistoricalTimeSeriesRatingRule.of(DATA_PROVIDER_NAME, "CMPL", 3));
    HistoricalTimeSeriesRating inputConfig = HistoricalTimeSeriesRating.of(rules);
    
    FudgeSerializer serializationContext = new FudgeSerializer(FUDGE_CONTEXT);
    MutableFudgeMsg inputMsg = serializationContext.objectToFudgeMsg(inputConfig);
    FudgeMsg outputMsg = FUDGE_CONTEXT.deserialize(FUDGE_CONTEXT.toByteArray(inputMsg)).getMessage();
    assertNotNull(outputMsg);
    assertEquals(3, outputMsg.getNumFields());
    
    FudgeDeserializer deserializationContext = new FudgeDeserializer(FUDGE_CONTEXT);
    HistoricalTimeSeriesRating outputConfig = deserializationContext.fudgeMsgToObject(HistoricalTimeSeriesRating.class, outputMsg);
    
    assertEquals(inputConfig, outputConfig);
  }

}
