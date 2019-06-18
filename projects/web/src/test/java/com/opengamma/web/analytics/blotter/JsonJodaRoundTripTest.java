/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.blotter;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import java.util.List;

import org.joda.beans.Bean;
import org.joda.beans.BeanBuilder;
import org.joda.beans.MetaBean;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.annotations.Test;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.convention.frequency.SimpleFrequency;
import com.opengamma.financial.security.future.BondFutureDeliverable;
import com.opengamma.financial.security.future.BondFutureSecurity;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.financial.security.swap.FixedInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingRateType;
import com.opengamma.financial.security.swap.InterestRateNotional;
import com.opengamma.financial.security.swap.SwapLeg;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.Expiry;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
@SuppressWarnings("unchecked")
public class JsonJodaRoundTripTest {

  // TODO move to BlotterUtils?
  private static final BeanVisitorDecorator PROPERTY_FILTER = new PropertyFilter(ManageableSecurity.meta().securityType());

  /**
   * Simple security
   * 
   * @throws JSONException
   *           if there is a problem producing the JSON
   */
  @Test
  public void fxForward() throws JSONException {
    final ZonedDateTime forwardDate = zdt(2012, 12, 21, 11, 0, 0, 0, ZoneOffset.UTC);
    final ExternalId regionId = ExternalId.of(ExternalSchemes.FINANCIAL, "GB");
    final FXForwardSecurity fxForward = new FXForwardSecurity(Currency.USD, 150, Currency.GBP, 100, forwardDate, regionId);
    fxForward.setName("GBP/USD forward");

    final JsonDataSink sink = new JsonDataSink(BlotterUtils.getJsonBuildingConverters());
    final BeanVisitor<JSONObject> writingVisitor = new BuildingBeanVisitor<>(fxForward, sink);
    final BeanTraverser traverser = new BeanTraverser(PROPERTY_FILTER);
    final JSONObject json = (JSONObject) traverser.traverse(FXForwardSecurity.meta(), writingVisitor);
    assertNotNull(json);

    final JsonBeanDataSource dataSource = new JsonBeanDataSource(new JSONObject(json.toString()));
    final MetaBeanFactory metaBeanFactory = new MapMetaBeanFactory(ImmutableSet.<MetaBean>of(FXForwardSecurity.meta()));
    final BeanVisitor<BeanBuilder<Bean>> readingVisitor =
        new BeanBuildingVisitor<>(dataSource, metaBeanFactory, BlotterUtils.getBeanBuildingConverters());
    final BeanBuilder<FXForwardSecurity> beanBuilder =
        (BeanBuilder<FXForwardSecurity>) traverser.traverse(FXForwardSecurity.meta(), readingVisitor);
    final FXForwardSecurity fxForward2 = beanBuilder.build();
    assertEquals(fxForward, fxForward2);
  }

  /**
   * Complicated security with nested beans
   * 
   * @throws JSONException
   *           if there is a problem producing the JSON
   */
  @Test
  public void swap() throws JSONException {
    final ZonedDateTime tradeDate = zdt(2012, 12, 21, 11, 0, 0, 0, ZoneOffset.UTC);
    final ZonedDateTime effectiveDate = zdt(2013, 1, 21, 11, 0, 0, 0, ZoneOffset.UTC);
    final ZonedDateTime maturityDate = zdt(2013, 12, 21, 11, 0, 0, 0, ZoneOffset.UTC);
    final SwapLeg payLeg = new FixedInterestRateLeg(
        DayCounts.ACT_360,
        SimpleFrequency.MONTHLY,
        ExternalId.of(ExternalSchemes.FINANCIAL, "123"),
        BusinessDayConventions.FOLLOWING,
        new InterestRateNotional(Currency.GBP, 123),
        false,
        0.01);
    final SwapLeg receiveLeg = new FloatingInterestRateLeg(
        DayCounts.ACT_ACT_ISDA,
        SimpleFrequency.QUARTERLY,
        ExternalId.of(ExternalSchemes.FINANCIAL, "123"),
        BusinessDayConventions.MODIFIED_FOLLOWING,
        new InterestRateNotional(Currency.GBP, 234),
        false,
        ExternalId.of("Rate", "asdf"),
        FloatingRateType.IBOR);
    final SwapSecurity security = new SwapSecurity(tradeDate, effectiveDate, maturityDate, "cpty", payLeg, receiveLeg);
    security.setName("Test swap");

    final JsonDataSink sink = new JsonDataSink(BlotterUtils.getJsonBuildingConverters());
    final BeanTraverser traverser = new BeanTraverser(PROPERTY_FILTER);
    final BeanVisitor<JSONObject> writingVisitor = new BuildingBeanVisitor<>(security, sink);
    final JSONObject json = (JSONObject) traverser.traverse(SwapSecurity.meta(), writingVisitor);
    assertNotNull(json);

    final JsonBeanDataSource dataSource = new JsonBeanDataSource(new JSONObject(json.toString()));
    final MetaBeanFactory metaBeanFactory = new MapMetaBeanFactory(ImmutableSet.<MetaBean>of(
        SwapSecurity.meta(),
        FixedInterestRateLeg.meta(),
        FloatingInterestRateLeg.meta(),
        InterestRateNotional.meta()));
    final BeanVisitor<BeanBuilder<SwapSecurity>> readingVisitor =
        new BeanBuildingVisitor<>(dataSource, metaBeanFactory, BlotterUtils.getBeanBuildingConverters());
    final BeanBuilder<SwapSecurity> beanBuilder =
        (BeanBuilder<SwapSecurity>) traverser.traverse(SwapSecurity.meta(), readingVisitor);
    final SwapSecurity security2 = beanBuilder.build();
    assertEquals(security, security2);
  }

  /**
   * BondFutureSecurity contains a collection of bean instances
   * (BondFutureDeliverable)
   * 
   * @throws JSONException
   *           if there is a problem producing the JSON
   */
  @Test
  public void bondFuture() throws JSONException {
    final ZonedDateTime firstDeliveryDate = zdt(2012, 12, 21, 11, 0, 0, 0, ZoneOffset.UTC);
    final ZonedDateTime lastDeliveryDate = zdt(2013, 12, 21, 11, 0, 0, 0, ZoneOffset.UTC);
    final ZonedDateTime expiryDate = zdt(2013, 12, 22, 11, 0, 0, 0, ZoneOffset.UTC);
    final ExternalIdBundle bundle1 = ExternalIdBundle.of(ExternalId.of("sch1", "123"), ExternalId.of("sch1", "234"));
    final ExternalIdBundle bundle2 = ExternalIdBundle.of(ExternalId.of("sch1", "345"));
    final List<BondFutureDeliverable> basket = Lists.newArrayList(
        new BondFutureDeliverable(bundle1, 111),
        new BondFutureDeliverable(bundle2, 222));
    final BondFutureSecurity security = new BondFutureSecurity(new Expiry(expiryDate), "exch", "settExch", Currency.GBP, 1234,
        basket, firstDeliveryDate, lastDeliveryDate, "category");
    security.setName("a bond future");

    // TODO this isn't converting ExternalIdBundle properly
    final JsonDataSink sink = new JsonDataSink(BlotterUtils.getJsonBuildingConverters());
    final BeanTraverser traverser = new BeanTraverser(PROPERTY_FILTER);
    final BeanVisitor<JSONObject> writingVisitor = new BuildingBeanVisitor<>(security, sink);
    final JSONObject json = (JSONObject) traverser.traverse(BondFutureSecurity.meta(), writingVisitor);
    assertNotNull(json);

    final JsonBeanDataSource dataSource = new JsonBeanDataSource(new JSONObject(json.toString()));
    final MetaBeanFactory metaBeanFactory = new MapMetaBeanFactory(ImmutableSet.<MetaBean>of(
        BondFutureSecurity.meta(),
        BondFutureDeliverable.meta()));
    final BeanVisitor<BeanBuilder<BondFutureSecurity>> readingVisitor =
        new BeanBuildingVisitor<>(dataSource, metaBeanFactory, BlotterUtils.getBeanBuildingConverters());
    final BeanBuilder<BondFutureSecurity> beanBuilder =
        (BeanBuilder<BondFutureSecurity>) traverser.traverse(BondFutureSecurity.meta(), readingVisitor);
    final BondFutureSecurity security2 = beanBuilder.build();
    assertEquals(security, security2);
  }

  // TODO test for FRA to endure region is handled correctly

  //-------------------------------------------------------------------------
  private static ZonedDateTime zdt(final int y, final int m, final int d, final int hr, final int min, final int sec, final int nanos, final ZoneId zone) {
    return LocalDateTime.of(y, m, d, hr, min, sec, nanos).atZone(zone);
  }

}
