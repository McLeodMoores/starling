/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.result;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertSame;
import static org.testng.AssertJUnit.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.opengamma.util.test.TestGroup;

@Test(groups = TestGroup.UNIT)
public class ResultTest {

  private static final Function<String, Result<Integer>> FUNCTION_STRLEN = new Function<String, Result<Integer>>() {
    @Override
    public Result<Integer> apply(final String input) {
      return Result.success(input.length());
    }
  };
  private static final Function2<String, String, Result<String>> FUNCTION_MERGE = new Function2<String, String, Result<String>>() {
    @Override
    public Result<String> apply(final String t, final String u) {
      return Result.success(t + " " + u);
    }
  };

  //-------------------------------------------------------------------------
  @Test
  public void success() {
    final Result<String> test = Result.success("success");
    assertEquals(true, test.isSuccess());
    assertEquals(SuccessStatus.SUCCESS, test.getStatus());
    assertEquals("success", test.getValue());
  }

  @Test(expectedExceptions = IllegalStateException.class)
  public void success_getFailureMessage() {
    final Result<String> test = Result.success("success");
    test.getFailureMessage();
  }

  @Test(expectedExceptions = IllegalStateException.class)
  public void success_getFailures() {
    final Result<String> test = Result.success("success");
    test.getFailures();
  }

  @Test
  public void success_flatMap() {
    final Result<String> success = Result.success("success");
    final Result<Integer> test = success.flatMap(FUNCTION_STRLEN);
    assertEquals(true, test.isSuccess());
    assertEquals(SuccessStatus.SUCCESS, test.getStatus());
    assertEquals(Integer.valueOf(7), test.getValue());
  }

  @Test
  public void success_ifSuccess() {
    final Result<String> success = Result.success("success");
    final Result<Integer> test = success.ifSuccess(FUNCTION_STRLEN);
    assertEquals(true, test.isSuccess());
    assertEquals(SuccessStatus.SUCCESS, test.getStatus());
    assertEquals(Integer.valueOf(7), test.getValue());
  }

  @Test
  public void success_combineWith_success() {
    final Result<String> success1 = Result.success("Hello");
    final Result<String> success2 = Result.success("World");
    final Result<String> test = success1.combineWith(success2, FUNCTION_MERGE);
    assertEquals(true, test.isSuccess());
    assertEquals(SuccessStatus.SUCCESS, test.getStatus());
    assertEquals("Hello World", test.getValue());
  }

  @Test
  public void success_combineWith_failure() {
    final Result<String> success = Result.success("Hello");
    final Result<String> failure = Result.failure(new IllegalArgumentException());
    final Result<String> test = success.combineWith(failure, FUNCTION_MERGE);
    assertEquals(false, test.isSuccess());
    assertEquals(FailureStatus.ERROR, test.getStatus());
    assertEquals(1, test.getFailures().size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void failure() {
    final Result<String> test = Result.failure(new IllegalArgumentException("failure"));
    assertEquals(false, test.isSuccess());
    assertEquals(FailureStatus.ERROR, test.getStatus());
    assertEquals("failure", test.getFailureMessage());
    assertEquals(1, test.getFailures().size());
    final Result<Integer> test2 = test.ifSuccess(FUNCTION_STRLEN);
    assertSame(test, test2);
    final Result<Integer> test3 = test.flatMap(FUNCTION_STRLEN);
    assertSame(test, test3);
  }

  @Test(expectedExceptions = IllegalStateException.class)
  public void failure_getValue() {
    final Result<String> test = Result.failure(new IllegalArgumentException());
    test.getValue();
  }

  @Test
  public void failure_combineWith_success() {
    final Result<String> failure = Result.failure(new IllegalArgumentException("failure"));
    final Result<String> success = Result.success("World");
    final Result<String> test = failure.combineWith(success, FUNCTION_MERGE);
    assertEquals(FailureStatus.ERROR, test.getStatus());
    assertEquals("failure", test.getFailureMessage());
  }

  @Test
  public void failure_combineWith_failure() {
    final Result<String> failure1 = Result.failure(new IllegalArgumentException("failure"));
    final Result<String> failure2 = Result.failure(new IllegalArgumentException("fail"));
    final Result<String> test = failure1.combineWith(failure2, FUNCTION_MERGE);
    assertEquals(FailureStatus.ERROR, test.getStatus());
    assertEquals("failure, fail", test.getFailureMessage());
  }

  //-------------------------------------------------------------------------
  @Test
  public void anyFailures_varargs() {
    final Result<String> success1 = Result.success("success 1");
    final Result<String> success2 = Result.success("success 1");
    final Result<Object> failure1 = Result.failure(FailureStatus.MISSING_DATA, "failure 1");
    final Result<Object> failure2 = Result.failure(FailureStatus.ERROR, "failure 2");
    assertTrue(Result.anyFailures(failure1, failure2));
    assertTrue(Result.anyFailures(failure1, success1));
    assertFalse(Result.anyFailures(success1, success2));
  }

  @Test
  public void anyFailures_iterable() {
    final Result<String> success1 = Result.success("success 1");
    final Result<String> success2 = Result.success("success 1");
    final Result<Object> failure1 = Result.failure(FailureStatus.MISSING_DATA, "failure 1");
    final Result<Object> failure2 = Result.failure(FailureStatus.ERROR, "failure 2");
    assertTrue(Result.anyFailures(ImmutableList.of(failure1, failure2)));
    assertTrue(Result.anyFailures(ImmutableList.of(failure1, success1)));
    assertFalse(Result.anyFailures(ImmutableList.of(success1, success2)));
  }

  @Test
  public void allSuccess_varargs() {
    final Result<String> success1 = Result.success("success 1");
    final Result<String> success2 = Result.success("success 1");
    final Result<Object> failure1 = Result.failure(FailureStatus.MISSING_DATA, "failure 1");
    final Result<Object> failure2 = Result.failure(FailureStatus.ERROR, "failure 2");
    assertFalse(Result.allSuccessful(failure1, failure2));
    assertFalse(Result.allSuccessful(failure1, success1));
    assertTrue(Result.allSuccessful(success1, success2));
  }

  @Test
  public void testAllSuccessIterable() {
    final Result<String> success1 = Result.success("success 1");
    final Result<String> success2 = Result.success("success 1");
    final Result<Object> failure1 = Result.failure(FailureStatus.MISSING_DATA, "failure 1");
    final Result<Object> failure2 = Result.failure(FailureStatus.ERROR, "failure 2");
    assertFalse(Result.allSuccessful(ImmutableList.of(failure1, failure2)));
    assertFalse(Result.allSuccessful(ImmutableList.of(failure1, success1)));
    assertTrue(Result.allSuccessful(ImmutableList.of(success1, success2)));
  }

  @Test
  public void propagateFailures() {
    final Result<String> success1 = Result.success("success 1");
    final Result<String> success2 = Result.success("success 1");
    final Result<Object> failure1 = Result.failure(FailureStatus.MISSING_DATA, "failure 1");
    final Result<Object> failure2 = Result.failure(FailureStatus.ERROR, "failure 2");
    final Result<Object> composite1 = Result.failure(success1, success2, failure1, failure2);
    final Collection<Failure> failures = composite1.getFailures();
    final Set<Failure> expected = new HashSet<>();
    expected.addAll(failure1.getFailures());
    expected.addAll(failure2.getFailures());
    assertEquals(expected, failures);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void propagateSuccesses() {
    final Result<String> success1 = Result.success("success 1");
    final Result<String> success2 = Result.success("success 1");
    Result.failure(success1, success2);
  }

  @Test
  public void generateFailureFromException() {
    final Exception exception = new Exception("something went wrong");
    final Result<Object> failure = Result.failure(exception);
    assertThat(failure.getStatus(), is((ResultStatus) FailureStatus.ERROR));
    assertThat(failure.getFailureMessage(), is("something went wrong"));
  }

  @Test
  public void generateFailureFromExceptionWithMessage() {
    final Exception exception = new Exception("something went wrong");
    final Result<Object> failure = Result.failure(exception, "my message");
    assertThat(failure.getStatus(), is((ResultStatus) FailureStatus.ERROR));
    assertThat(failure.getFailureMessage(), is("my message"));
  }

  @Test
  public void generateFailureFromExceptionWithCustomStatus() {
    final Exception exception = new Exception("something went wrong");
    final Result<Object> failure = Result.failure(FailureStatus.PERMISSION_DENIED, exception);
    assertThat(failure.getStatus(), is((ResultStatus) FailureStatus.PERMISSION_DENIED));
    assertThat(failure.getFailureMessage(), is("something went wrong"));
  }

  @Test
  public void generateFailureFromExceptionWithCustomStatusAndMessage() {
    final Exception exception = new Exception("something went wrong");
    final Result<Object> failure = Result.failure(FailureStatus.PERMISSION_DENIED, exception, "my message");
    assertThat(failure.getStatus(), is((ResultStatus) FailureStatus.PERMISSION_DENIED));
    assertThat(failure.getFailureMessage(), is("my message"));
  }

  //-------------------------------------------------------------------------
  @Test
  public void failureDeduplicateFailure() {
    final Result<Object> result = Result.failure(FailureStatus.MISSING_DATA, "failure");
    final Failure failure = result.getFailures().iterator().next();

    final Result<Object> test1 = Result.failure(result, result);
    assertEquals(1, test1.getFailures().size());
    assertEquals(ImmutableSet.of(failure), test1.getFailures());
    assertEquals("failure", test1.getFailureMessage());
  }

  @Test
  public void failureSameType() {
    final Result<Object> failure1 = Result.failure(FailureStatus.MISSING_DATA, "message 1");
    final Result<Object> failure2 = Result.failure(FailureStatus.MISSING_DATA, "message 2");
    final Result<Object> failure3 = Result.failure(FailureStatus.MISSING_DATA, "message 3");
    final List<Failure> failures = new ArrayList<>();
    failures.addAll(failure1.getFailures());
    failures.addAll(failure2.getFailures());
    failures.addAll(failure3.getFailures());
    final Result<?> composite = FailureResult.of(failures);
    AssertJUnit.assertEquals(FailureStatus.MISSING_DATA, composite.getStatus());
    AssertJUnit.assertEquals("message 1, message 2, message 3", composite.getFailureMessage());
  }

  @Test
  public void failureDifferentTypes() {
    final Result<Object> failure1 = Result.failure(FailureStatus.MISSING_DATA, "message 1");
    final Result<Object> failure2 = Result.failure(FailureStatus.CALCULATION_FAILED, "message 2");
    final Result<Object> failure3 = Result.failure(FailureStatus.ERROR, "message 3");
    final List<Failure> failures = new ArrayList<>();
    failures.addAll(failure1.getFailures());
    failures.addAll(failure2.getFailures());
    failures.addAll(failure3.getFailures());
    final Result<?> composite = FailureResult.of(failures);
    AssertJUnit.assertEquals(FailureStatus.MULTIPLE, composite.getStatus());
    AssertJUnit.assertEquals("message 1, message 2, message 3", composite.getFailureMessage());
  }

}
