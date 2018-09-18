/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.util.annotation;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.util.Arrays;
import java.util.Set;

import org.testng.annotations.Test;
import org.threeten.bp.Instant;
import org.threeten.bp.temporal.ChronoUnit;

import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link AnnotationScannerImpl}.
 */
@Test(groups = TestGroup.UNIT)
public class AnnotationScannerImplTest {

  /**
   * Tests that the class name cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullAnnotationClass() {
    new AnnotationScannerImpl().scan(null);
  }

  /**
   * Tests the scan. The classes are loaded from the cache and so can be any that are set in the cache.
   */
  public void testLoadClassesFromCache() {
    final File temp = createTempFolder("testLoadClassesFromCache");
    try {
      System.setProperty(AnnotationCache.CACHE_PATH_PROPERTY, temp.toString());
      final Instant timestamp = new ClasspathScanner().getTimestamp().plus(1000, ChronoUnit.MINUTES);
      final AnnotationCache cache = AnnotationCache.create(timestamp, MockType.class,
          Arrays.asList(AnnotationCacheTest.class.getName(), AnnotationScannerImplTest.class.getName()));
      cache.save();
      final Set<Class<?>> result = new AnnotationScannerImpl().scan(MockType.class);
      if (result.size() == 1) {
        return;
      }
      assertEquals(result.size(), 2);
      assertTrue(result.contains(AnnotationCacheTest.class));
      assertTrue(result.contains(AnnotationScannerImplTest.class));
    } finally {
      temp.delete();
    }
  }

  /**
   * Tests the scan. The classes are loaded from the classpath and so contain only the classes that are
   * actually annotated with MockType.
   */
  public void testScanClasses() {
    final File temp = createTempFolder("testScanClasses");
    try {
      System.setProperty(AnnotationCache.CACHE_PATH_PROPERTY, temp.toString());
      final Instant timestamp = new ClasspathScanner().getTimestamp().minus(1000, ChronoUnit.DAYS);
      final AnnotationCache cache = AnnotationCache.create(timestamp, MockType.class,
          Arrays.asList(AnnotationCacheTest.class.getName(), AnnotationScannerImplTest.class.getName()));
      cache.save();
      final Set<Class<?>> result = new AnnotationScannerImpl().scan(MockType.class);
      assertEquals(result.size(), 1);
      assertTrue(result.contains(MockAnnotation.class));
    } finally {
      temp.delete();
    }
  }

  private File createTempFolder(final String testName) {
    final File tmp = new File(System.getProperty("java.io.tmpdir"));
    assertTrue(tmp.exists());
    final File privateTmp = new File(tmp, System.getProperty("user.name") + "-" + getClass().getName() + "-"
        + testName + "-" + System.currentTimeMillis());
    assertFalse(privateTmp.exists());
    privateTmp.mkdir();
    return privateTmp;
  }
}
