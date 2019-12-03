/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.annotation;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;

import org.testng.annotations.Test;
import org.threeten.bp.Instant;

import com.opengamma.util.test.TestGroup;

/**
 * Tests the {@link AnnotationCache} class.
 */
@Test(groups = TestGroup.UNIT)
public class AnnotationCacheTest {

  private static final String CACHE_FILE_NAME = ".MockType";

  private File createTempFolder(final String methodName) {
    final File tmp = new File(System.getProperty("java.io.tmpdir"));
    assertTrue(tmp.exists());
    final File privateTmp = new File(tmp, System.getProperty("user.name") + "-" + getClass().getName()
        + "-" + methodName + "-" + System.currentTimeMillis());
    assertFalse(privateTmp.exists());
    privateTmp.mkdir();
    return privateTmp;
  }

  /**
   * Tests the behaviour when the path does not contain the appropriate property.
   */
  public void testLoadNoPathEntry() {
    System.clearProperty(AnnotationCache.CACHE_PATH_PROPERTY);
    final AnnotationCache cache = AnnotationCache.load(MockType.class);
    assertNotNull(cache);
    assertEquals(cache.getTimestamp(), Instant.EPOCH);
    assertEquals(cache.getAnnotationClass(), MockType.class);
  }

  /**
   * Tests the behaviour when the path does not contain the appropriate property.
   */
  public void testSaveNoPathEntry() {
    System.clearProperty(AnnotationCache.CACHE_PATH_PROPERTY);
    final AnnotationCache cache = AnnotationCache.load(MockType.class);
    cache.save(); // don't expect an error
  }

  /**
   * Tests the behaviour when the path does not contain the appropriate property.
   */
  public void testGetClassesNoPathEntry() {
    System.clearProperty(AnnotationCache.CACHE_PATH_PROPERTY);
    final AnnotationCache cache = AnnotationCache.load(MockType.class);
    assertNotNull(cache);
    assertTrue(cache.getClasses().isEmpty());
  }

  /**
   * Tests the value when the file does not exist.
   */
  public void testLoadMissing() {
    final File temp = createTempFolder("testLoadMissing");
    try {
      System.setProperty(AnnotationCache.CACHE_PATH_PROPERTY, temp.toString());
      final AnnotationCache cache = AnnotationCache.load(MockType.class);
      assertNotNull(cache);
      assertEquals(cache.getTimestamp(), Instant.EPOCH);
    } finally {
      temp.delete();
    }
  }

  /**
   * Tests saving and loading a function cache to disk.
   */
  public void testSaveAndLoad() {
    final File temp = createTempFolder("testSaveAndLoad");
    try {
      System.setProperty(AnnotationCache.CACHE_PATH_PROPERTY, temp.toString());
      final Instant ts = Instant.now();
      AnnotationCache cache = AnnotationCache.create(ts, MockType.class, Arrays.asList(AnnotationCacheTest.class.getName()));
      cache.save();
      try {
        assertTrue(new File(temp, CACHE_FILE_NAME).exists());
        cache = AnnotationCache.load(MockType.class);
        assertEquals(cache.getTimestamp(), ts);
        assertEquals(cache.getClassNames().size(), 1);
        assertEquals(cache.getClassNames().iterator().next(), AnnotationCacheTest.class.getName());
      } finally {
        new File(temp, CACHE_FILE_NAME).delete();
      }
    } finally {
      temp.delete();
    }
  }

  /**
   * Tests the creation of a function cache.
   */
  public void testGetClasses() {
    final AnnotationCache cache = AnnotationCache.create(Instant.now(), MockType.class, Arrays.asList(AnnotationCacheTest.class.getName()));
    final Collection<Class<?>> classes = cache.getClasses();
    assertEquals(classes.size(), 1);
    assertEquals(classes.iterator().next(), AnnotationCacheTest.class);
  }

}
