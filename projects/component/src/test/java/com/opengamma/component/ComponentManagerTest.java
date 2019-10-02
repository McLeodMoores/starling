/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.component;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.util.List;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class ComponentManagerTest {

  /**
   *
   */
  @Test
  public void testLoadInitProperties() {
    final ComponentManager mgr = new ComponentManager("testserver");
    mgr.load("classpath:com/opengamma/component/test-success.properties");
    mgr.init();
    assertEquals("testserver", mgr.getServerName());
    assertEquals(3, mgr.getConfigIni().getGroups().size());
    assertEquals(1, mgr.getConfigIni().getGroup("global").size());
    assertEquals(3, mgr.getConfigIni().getGroup("one").size());
    assertEquals(5, mgr.getConfigIni().getGroup("two").size());
    assertEquals("one", mgr.getConfigIni().getGroup("one").getValue("alpha"));
  }

  /**
   *
   */
  @Test
  public void testLoadInitIni() {
    final ComponentManager mgr = new ComponentManager("testserver");
    mgr.load("classpath:com/opengamma/component/test-success.ini");
    mgr.init();
    assertEquals("testserver", mgr.getServerName());
    assertEquals(3, mgr.getConfigIni().getGroups().size());
    assertEquals(1, mgr.getConfigIni().getGroup("global").size());
    assertEquals(3, mgr.getConfigIni().getGroup("one").size());
    assertEquals(5, mgr.getConfigIni().getGroup("two").size());
    assertEquals("two", mgr.getConfigIni().getGroup("one").getValue("alpha"));
    final List<?> list = mgr.getRepository().getInstance(List.class, "two");
    assertEquals(3, list.size());
  }

  // -------------------------------------------------------------------------
  /**
   *
   */
  @Test(expectedExceptions = ComponentConfigException.class)
  public void testLoadBadFileType() {
    final ComponentManager mgr = new ComponentManager("testserver");
    try {
      mgr.load("classpath:com/opengamma/component/test-badfiletype.txt");
    } catch (final ComponentConfigException ex) {
      assertTrue(ex.getMessage().contains("Unknown file format"));
      throw ex;
    }
  }

  /**
   *
   */
  @Test(expectedExceptions = ComponentConfigException.class)
  public void testLoadPropertiesNoManagerNext() {
    final ComponentManager mgr = new ComponentManager("testserver");
    try {
      mgr.load("classpath:com/opengamma/component/test-nomanagernext.properties");
    } catch (final ComponentConfigException ex) {
      assertTrue(ex.getMessage().contains("The properties file must contain the key 'MANAGER.NEXT.FILE' to specify the next file to load"));
      throw ex;
    }
  }

  // -------------------------------------------------------------------------
  /**
   *
   */
  @Test(expectedExceptions = ComponentConfigException.class)
  public void testLoadInitMissingComponent() {
    final ComponentManager mgr = new ComponentManager("testserver");
    mgr.load("classpath:com/opengamma/component/test-missingcomponentref.ini");
    try {
      mgr.init();
    } catch (final ComponentConfigException ex) {
      assertTrue(ex.getCause().getMessage().contains("Unable to find component reference"));
      assertTrue(ex.getCause().getMessage().contains("three"));
      assertTrue(ex.getCause().getMessage().contains("epsilon"));
      throw ex;
    }
  }

  /**
   *
   */
  @Test(expectedExceptions = ComponentConfigException.class)
  public void testLoadInitDuplicateGroup() {
    final ComponentManager mgr = new ComponentManager("testserver");
    mgr.load("classpath:com/opengamma/component/test-duplicategroup.ini");
    try {
      mgr.init();
    } catch (final ComponentConfigException ex) {
      assertTrue(ex.getMessage().contains("one"));
      throw ex;
    }
  }

  /**
   *
   */
  @Test(expectedExceptions = ComponentConfigException.class)
  public void testLoadInitDuplicateProperty() {
    final ComponentManager mgr = new ComponentManager("testserver");
    mgr.load("classpath:com/opengamma/component/test-duplicateproperty.ini");
    try {
      mgr.init();
    } catch (final ComponentConfigException ex) {
      assertTrue(ex.getMessage().contains("two"));
      assertTrue(ex.getMessage().contains("delta"));
      throw ex;
    }
  }

  /**
   *
   */
  @Test(expectedExceptions = ComponentConfigException.class)
  public void testLoadInitUnusedProperty() {
    final ComponentManager mgr = new ComponentManager("testserver");
    mgr.load("classpath:com/opengamma/component/test-unused.ini");
    try {
      mgr.init();
    } catch (final ComponentConfigException ex) {
      assertTrue(ex.getCause().getMessage().contains("Configuration was specified but not used"));
      assertTrue(ex.getCause().getMessage().contains("foobar"));
      throw ex;
    }
  }

}
