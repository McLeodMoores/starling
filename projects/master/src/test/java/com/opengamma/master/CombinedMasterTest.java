/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.mockito.Matchers;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.CombinedMaster.SearchCallback;
import com.opengamma.master.CombinedMaster.SearchStrategy;
import com.opengamma.master.holiday.HolidayDocument;
import com.opengamma.master.holiday.HolidayMaster;
import com.opengamma.master.holiday.HolidaySearchRequest;
import com.opengamma.master.holiday.HolidaySearchResult;
import com.opengamma.util.paging.Paging;
import com.opengamma.util.paging.PagingRequest;
import com.opengamma.util.test.TestGroup;

/**
 *
 */
@Test(groups = TestGroup.UNIT)
public class CombinedMasterTest {

  private HolidayMaster _m1;
  private HolidayMaster _m2;

  private ObjectId _o1;
  private ObjectId _o2;
  private UniqueId _u1;
  private UniqueId _u2;

  private HolidayDocument _d1;
  private HolidayDocument _d2;

  private CombinedMaster<HolidayDocument, HolidayMaster> _cMaster;

  /**
   *
   */
  @BeforeMethod
  public void beforeMethod() {
    _m1 = mock(HolidayMaster.class);
    _m2 = mock(HolidayMaster.class);
    _o1 = ObjectId.of("TestScheme", "123");
    _o2 = ObjectId.of("TestScheme2", "234");
    _u1 = UniqueId.of(_o1, "v123");
    _u2 = UniqueId.of(_o2, "v234");
    _d1 = mock(HolidayDocument.class);
    _d2 = mock(HolidayDocument.class);
    when(_d1.getUniqueId()).thenReturn(_u1);
    when(_d1.getObjectId()).thenReturn(_o1);
    _cMaster = new CombinedMaster<HolidayDocument, HolidayMaster>(ImmutableList.of(_m1, _m2)) {
    };
  }

  /**
   *
   */
  @Test
  public void add() {
    when(_m1.add(_d1)).thenReturn(_d1);

    _cMaster.add(_d1);

    verify(_m1).add(_d1);
  }

  /**
   *
   */
  @Test
  public void addVersionToM1() {
    // test multiple invocations here
    when(_m1.replaceVersions(_o1, Collections.singletonList(_d1))).thenReturn(Collections.singletonList(_u1));
    when(_m1.replaceVersions(_o1, Collections.singletonList(_d1))).thenReturn(Collections.singletonList(_u1));

    _cMaster.replaceVersions(_o1, Collections.singletonList(_d1));
    _cMaster.replaceVersions(_o1, Collections.singletonList(_d1));

    verify(_m1, times(2)).replaceVersions(_o1, Collections.singletonList(_d1));
  }

  /**
   *
   */
  @Test
  public void addVersionToM2() {

    when(_m1.replaceVersions(_o1, Collections.singletonList(_d1))).thenThrow(new IllegalArgumentException());
    when(_m2.replaceVersions(_o1, Collections.singletonList(_d1))).thenReturn(Collections.singletonList(_u1));

    _cMaster.replaceVersions(_o1, Collections.singletonList(_d1));

    verify(_m1).replaceVersions(_o1, Collections.singletonList(_d1));
  }

  /**
   *
   */
  @Test(expectedExceptions = { IllegalArgumentException.class })
  public void addVersionException() {

    when(_m1.replaceVersions(_o1, Collections.singletonList(_d1))).thenThrow(new IllegalArgumentException());
    when(_m2.replaceVersions(_o1, Collections.singletonList(_d1))).thenThrow(new IllegalArgumentException());

    _cMaster.replaceVersions(_o1, Collections.singletonList(_d1));
  }

  /**
   *
   */
  @Test
  public void applyPaging() {
    final HolidayDocument m1h1 = holidayDocWithId("m1", "1");
    final HolidayDocument m1h2 = holidayDocWithId("m1", "2");
    final HolidayDocument m1h3 = holidayDocWithId("m1", "3");
    final HolidayDocument m1h4 = holidayDocWithId("m1", "4");
    HolidaySearchResult sr;
    sr = new HolidaySearchResult(ImmutableList.of(m1h1, m1h2, m1h3, m1h4));

    PagingRequest ofIndex;
    ofIndex = PagingRequest.ofIndex(1, 3);

    _cMaster.applyPaging(sr, ofIndex);

    assertEquals(Paging.of(ofIndex, 4), sr.getPaging());
    assertEquals(ImmutableList.of(m1h2, m1h3, m1h4), sr.getDocuments());

    sr = new HolidaySearchResult(ImmutableList.of(m1h1, m1h2, m1h3, m1h4));

    ofIndex = PagingRequest.ofIndex(100, 103);

    _cMaster.applyPaging(sr, ofIndex);

    assertEquals(Paging.of(ofIndex, 4), sr.getPaging());
    assertEquals(ImmutableList.of(), sr.getDocuments());

  }

  /**
   *
   */
  @Test
  public void correct() {
    // test multiple invocations here
    _cMaster.correct(_d1);
    _cMaster.correct(_d1);

    verify(_m1, times(2)).correct(_d1);
  }

  /**
   *
   */
  @Test
  public void getUniqueId() {
    // test multiple invocations here
    when(_m1.get(_u1)).thenReturn(_d1);

    _cMaster.get(_u1);
    _cMaster.get(_u1);

    verify(_m1, times(2)).get(_u1);
  }

  /**
   *
   */
  @Test
  public void getObjectIdentifiableVersionCorrection() {
    final VersionCorrection vc = VersionCorrection.LATEST;
    // test multiple invocations here
    when(_m1.get(_o1, vc)).thenReturn(_d1);

    _cMaster.get(_o1, vc);
    _cMaster.get(_o1, vc);

    verify(_m1, times(2)).get(_o1, vc);
  }

  /**
   *
   */
  @Test
  public void getMasterList() {
    assertEquals(ImmutableList.of(_m1, _m2), _cMaster.getMasterList());
  }

  /**
   *
   */
  @Test
  public void remove() {
    // test multiple invocations here
    _cMaster.remove(_d1);
    _cMaster.remove(_d1);

    verify(_m1, times(2)).remove(_d1);
  }

  /**
   *
   */
  @Test
  public void removeVersion() {
    _cMaster.removeVersion(_u1);
    _cMaster.removeVersion(_u1);

    verify(_m1, times(2)).replaceVersion(_u1, Collections.<HolidayDocument> emptyList());
  }

  /**
   *
   */
  @Test
  public void replaceAllVersions() {
    _cMaster.replaceAllVersions(_o1, Lists.newArrayList(_d1));
    _cMaster.replaceAllVersions(_o1, Lists.newArrayList(_d1));

    verify(_m1, times(2)).replaceAllVersions(_o1, Lists.newArrayList(_d1));
  }

  /**
   *
   */
  @Test
  public void replaceVersionUniqueIdListD() {
    _cMaster.replaceVersion(_u1, Lists.newArrayList(_d1));
    _cMaster.replaceVersion(_u1, Lists.newArrayList(_d1));

    verify(_m1, times(2)).replaceVersion(_u1, Lists.newArrayList(_d1));

  }

  /**
   *
   */
  @Test
  public void replaceVersionD() {
    _cMaster.replaceVersion(_d1);
    _cMaster.replaceVersion(_d1);

    verify(_m1, times(2)).replaceVersion(_u1, Collections.singletonList(_d1));
  }

  /**
   *
   */
  @Test
  public void replaceVersions() {
    _cMaster.replaceVersions(_o1, Collections.singletonList(_d1));
    _cMaster.replaceVersions(_o1, Collections.singletonList(_d1));

    verify(_m1, times(2)).replaceVersions(_o1, Collections.singletonList(_d1));

  }

  /**
   *
   */
  @Test
  public void search() {

    final HolidayDocument m1h1 = holidayDocWithId("m1", "1");
    final HolidayDocument m1h2 = holidayDocWithId("m1", "2");
    final HolidayDocument m1h3 = holidayDocWithId("m1", "3");
    final HolidayDocument m1h4 = holidayDocWithId("m1", "4");

    final HolidayDocument m2h3 = holidayDocWithId("m2", "3");
    final HolidayDocument m2h4 = holidayDocWithId("m2", "4");
    final HolidayDocument m2h5 = holidayDocWithId("m2", "5");
    final HolidayDocument m2h6 = holidayDocWithId("m2", "6");
    final HolidayDocument m2h7 = holidayDocWithId("m2", "7");

    final HolidaySearchResult m1Result = new HolidaySearchResult(Lists.newArrayList(m1h1, m1h2, m1h2, m1h3, m1h4, m1h4));
    final HolidaySearchResult m2Result = new HolidaySearchResult(Lists.newArrayList(m2h3, m2h4, m2h5, m2h6, m2h7));

    final List<HolidayDocument> resultList = Lists.newArrayList();

    final SearchCallback<HolidayDocument, HolidayMaster> cbDelegate = mock(SearchCallback.class);

    _cMaster.search(Lists.newArrayList(m1Result, m2Result, null), new SearchCallback<HolidayDocument, HolidayMaster>() {

      @Override
      public int compare(final HolidayDocument arg0, final HolidayDocument arg1) {
        return arg0.getUniqueId().getValue().compareTo(arg1.getUniqueId().getValue());
      }

      @Override
      public boolean include(final HolidayDocument document) {
        return !m2h7.equals(document);
      }

      @Override
      public void accept(final HolidayDocument document, final HolidayMaster master, final boolean masterUnique, final boolean clientUnique) {
        cbDelegate.accept(document, master, masterUnique, clientUnique);
        resultList.add(document);
      }
    });

    verify(cbDelegate).accept(m1h1, _m1, true, true);
    verify(cbDelegate, times(2)).accept(m1h2, _m1, false, true);
    verify(cbDelegate).accept(m1h3, _m1, true, false);
    verify(cbDelegate).accept(m2h3, _m2, true, false);
    verify(cbDelegate, times(2)).accept(m1h4, _m1, false, false);
    verify(cbDelegate).accept(m2h4, _m2, true, false);
    verify(cbDelegate).accept(m2h5, _m2, true, true);
    verify(cbDelegate).accept(m2h6, _m2, true, true);
    verifyNoMoreInteractions(cbDelegate);

    final ArrayList<HolidayDocument> sortedResultList = Lists.newArrayList(resultList);
    Collections.sort(sortedResultList, cbDelegate);
    assertEquals(sortedResultList, resultList);

  }

  private static HolidayDocument holidayDocWithId(final String scheme, final String id) {
    final HolidayDocument holidayDocument = new HolidayDocument();
    holidayDocument.setUniqueId(UniqueId.of(scheme, id));
    return holidayDocument;
  }

  /**
   *
   */
  @Test
  public void get() {
    final ArrayList<UniqueId> getList = Lists.newArrayList(_u1, _u2);

    when(_m1.get(_u2)).thenThrow(new IllegalArgumentException());

    _cMaster.get(getList);
    _cMaster.get(getList);

    verify(_m1, times(2)).get(_u1);
    verify(_m2, times(2)).get(_u2);
  }

  /**
   *
   */
  @Test
  public void update() {
    // test multiple invocations here
    _cMaster.update(_d1);
    _cMaster.update(_d1);

    verify(_m1, times(2)).update(_d1);

  }

  /**
   *
   */
  @Test
  public void pagedSearchEmpty() {
    final PagingRequest pr = PagingRequest.ALL;
    final List<HolidayDocument> m1Result = Lists.newArrayList();
    final List<HolidayDocument> m2Result = Lists.newArrayList();

    final List<HolidayDocument> result = runPagedSearch(pr, m1Result, 0, m2Result, 0);

    assertTrue(result.isEmpty());
  }

  /**
   *
   */
  @Test
  public void pagedSearchOneElement() {
    final PagingRequest pr = PagingRequest.ALL;
    final List<HolidayDocument> singleton = Lists.newArrayList(_d1);
    final List<HolidayDocument> empty = Lists.newArrayList();

    final List<HolidayDocument> result = runPagedSearch(pr, singleton, 1, empty, 0);
    assertEquals(1, result.size());

    final List<HolidayDocument> result2 = runPagedSearch(pr, empty, 0, singleton, 1);
    assertEquals(1, result2.size());

  }

  /**
   *
   */
  @Test
  public void pagedSearchTwoElements() {
    PagingRequest pr;
    final List<HolidayDocument> singleton = Lists.newArrayList(_d1);
    final List<HolidayDocument> empty = Lists.newArrayList();

    pr = PagingRequest.ofIndex(0, 1);
    final List<HolidayDocument> result2 = runPagedSearch(pr, singleton, 1, singleton, 1);
    assertEquals(1, result2.size());
    verifyNoMoreInteractions(_m2);

    pr = PagingRequest.ALL;
    final List<HolidayDocument> result = runPagedSearch(pr, singleton, 1, singleton, 1);
    assertEquals(2, result.size());

    pr = PagingRequest.ofIndex(1, 1);
    final List<HolidayDocument> result3 = runPagedSearch(pr, empty, 1, singleton, 1);
    assertEquals(1, result3.size());
  }

  /**
   *
   */
  @Test
  public void pagedSearchMultiple() {
    final PagingRequest pr = PagingRequest.ofIndex(2, 2);
    final List<HolidayDocument> m1Result = Lists.newArrayList(_d1);
    final List<HolidayDocument> m2Result = Lists.newArrayList(_d2);
    final List<HolidayDocument> expected = Lists.newArrayList(_d1, _d2);

    final List<HolidayDocument> result = runPagedSearch(pr, m1Result, 3, m2Result, 1);

    assertEquals(2, result.size());

    assertEquals(expected, result);

  }

  /**
   *
   */
  @Test
  public void pagedSearchOnlyFirst() {
    final List<HolidayDocument> m2Result = Lists.newArrayList();
    List<HolidayDocument> result;

    result = runPagedSearch(PagingRequest.ofIndex(0, 2), Lists.newArrayList(_d1, _d1), 3, m2Result, 0);
    assertEquals(2, result.size());

    result = runPagedSearch(PagingRequest.ofIndex(1, 2), Lists.newArrayList(_d1, _d1), 3, m2Result, 0);
    assertEquals(2, result.size());

    result = runPagedSearch(PagingRequest.ofIndex(0, 3), Lists.newArrayList(_d1, _d1, _d1), 3, m2Result, 0);
    assertEquals(3, result.size());

    verifyNoMoreInteractions(_m2);

  }

  private List<HolidayDocument> runPagedSearch(final PagingRequest pr, final List<HolidayDocument> m1Result, final int m1Total,
      final List<HolidayDocument> m2Result, final int m2Total) {
    final HolidaySearchResult result = new HolidaySearchResult();
    final HolidaySearchRequest searchRequest = new HolidaySearchRequest();
    searchRequest.setPagingRequest(pr);
    final HolidaySearchResult m1SearchResult = new HolidaySearchResult();
    m1SearchResult.setDocuments(m1Result);
    m1SearchResult.setPaging(Paging.of(PagingRequest.ofIndex(0, m1Result.size()), m1Total));
    final HolidaySearchResult m2SearchResult = new HolidaySearchResult();
    m2SearchResult.setPaging(Paging.of(PagingRequest.ofIndex(0, m2Result.size()), m2Total));
    m2SearchResult.setDocuments(m2Result);

    when(_m1.search(Matchers.<HolidaySearchRequest> any())).thenReturn(m1SearchResult);
    when(_m2.search(Matchers.<HolidaySearchRequest> any())).thenReturn(m2SearchResult);

    _cMaster.pagedSearch(new SearchStrategy<HolidayDocument, HolidayMaster, HolidaySearchRequest>() {

      @Override
      public HolidaySearchResult search(final HolidayMaster master, final HolidaySearchRequest hsr) {
        return master.search(hsr);
      }
    }, result, searchRequest);

    return result.getDocuments();
  }
}
