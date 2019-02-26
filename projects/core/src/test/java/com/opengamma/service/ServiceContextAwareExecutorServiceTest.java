/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link ServiceContextAwareExecutorService} and {@link ThreadLocalServiceContext}.
 */
@Test(groups = TestGroup.UNIT)
@SuppressWarnings("unchecked")
public class ServiceContextAwareExecutorServiceTest {

  private final ExecutorService _underlying = Executors.newSingleThreadExecutor();
  private final ExecutorService _executor = new ServiceContextAwareExecutorService(_underlying);

  /**
   * Sets up an empty thread local context.
   *
   * @throws Exception
   *           if the context cannot be created
   */
  @BeforeMethod
  public void setUp() throws Exception {
    ThreadLocalServiceContext.init(ServiceContext.of(Collections.<Class<?>, Object> emptyMap()));
  }

  /**
   * Removes services after each method.
   *
   * @throws Exception
   *           if the services cannot be removed
   */
  @AfterMethod
  public void tearDown() throws Exception {
    ThreadLocalServiceContext.init(null);
  }

  /**
   * @throws ExecutionException
   *           if there is a problem with the execution
   * @throws InterruptedException
   *           if there is an interruption
   */
  @Test
  public void submitCallable() throws ExecutionException, InterruptedException {
    assertFalse(_underlying.submit(callable()).get());
    assertTrue(_executor.submit(callable()).get());
  }

  /**
   * @throws InterruptedException
   *           if there is an interruption
   */
  @Test
  public void submitRunnable() throws InterruptedException {
    final ArrayBlockingQueue<Boolean> queue = new ArrayBlockingQueue<>(1);
    final Runnable r = new Runnable() {
      @Override
      public void run() {
        final boolean b = ThreadLocalServiceContext.getInstance() != null;
        try {
          queue.put(b);
        } catch (final InterruptedException e) {
          throw new RuntimeException(e);
        }
      }
    };
    _underlying.submit(r);
    assertFalse(queue.take());
    _executor.submit(r);
    assertTrue(queue.take());
    _underlying.submit(r, null);
    assertFalse(queue.take());
    _executor.submit(r, null);
    assertTrue(queue.take());
  }

  /**
   * @throws InterruptedException
   *           if there is an interruption
   */
  @Test
  public void invokeAll() throws InterruptedException {
    final List<Callable<Boolean>> tasks2 = Lists.newArrayList(callable(), callable());
    final List<Future<Boolean>> futures2 = _executor.invokeAll(tasks2);
    assertTrue(Iterables.all(futures2, predicate()));

    final List<Callable<Boolean>> tasks1 = Lists.newArrayList(callable(), callable());
    final List<Future<Boolean>> futures1 = _underlying.invokeAll(tasks1);
    assertTrue(Iterables.all(futures1, Predicates.not(predicate())));
  }

  /**
   * @throws ExecutionException
   *           if there is a problem with the execution
   * @throws InterruptedException
   *           if there is an interruption
   */
  @Test
  public void amendContext() throws InterruptedException, ExecutionException {
    ThreadLocalServiceContext.init(ServiceContext.of(String.class, "StringService"));
    final ExecutorService executor = new ServiceContextAwareExecutorService(_underlying);
    assertThat(executor.submit(stringCallable()).get(), is("WithString"));

    // Now change to a different context, without StringService
    ThreadLocalServiceContext.init(ServiceContext.of(Integer.class, 42));

    assertThat(executor.submit(stringCallable()).get(), is("WithoutString"));
  }

  private static Callable<String> stringCallable() {
    return new Callable<String>() {
      @Override
      public String call() throws Exception {

        if (ThreadLocalServiceContext.getInstance() == null) {
          return "None";
        }
        try {
          ThreadLocalServiceContext.getInstance().get(String.class);
          return "WithString";
        } catch (final IllegalArgumentException e) {
          return "WithoutString";
        }
      }
    };
  }

  private static Predicate<Future<Boolean>> predicate() {
    return new Predicate<Future<Boolean>>() {
      @Override
      public boolean apply(final Future<Boolean> future) {
        try {
          return future.get();
        } catch (InterruptedException | ExecutionException e) {
          throw new RuntimeException(e);
        }
      }
    };
  }

  private static Callable<Boolean> callable() {
    return new Callable<Boolean>() {
      @Override
      public Boolean call() throws Exception {
        return ThreadLocalServiceContext.getInstance() != null;
      }
    };
  }
}
