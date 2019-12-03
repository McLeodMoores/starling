/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.component;

import static org.testng.AssertJUnit.assertEquals;

import org.springframework.core.io.Resource;
import org.springframework.security.util.InMemoryResource;
import org.testng.annotations.Test;

import com.google.common.base.Charsets;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link ComponentConfigIniLoader}.
 */
@Test(groups = TestGroup.UNIT)
public class ComponentConfigIniLoaderTest {

  private static final ComponentLogger LOGGER = ComponentLogger.Sink.INSTANCE;
  private static final String NEWLINE = "\n";

  /**
   * Tests loading a valid ini file.
   */
  public void testLoadValid() {
    final ConfigProperties properties = new ConfigProperties();
    final ComponentConfigIniLoader loader = new ComponentConfigIniLoader(LOGGER, properties);
    final String text =
        "# comment" + NEWLINE
        + "[global]" + NEWLINE
        + "a = c" + NEWLINE
        + "b = d" + NEWLINE
        + "" + NEWLINE
        + "[block]" + NEWLINE
        + "m = p" + NEWLINE
        + "n = ${a}" + NEWLINE       // property from [global]
        + "o = ${input}" + NEWLINE;  // property from injected properties
    final Resource resource = new InMemoryResource(text.getBytes(Charsets.UTF_8), "Test");
    properties.put("input", "text");

    final ComponentConfig test = new ComponentConfig();
    loader.load(resource, 0, test);
    assertEquals(2, test.getGroups().size());

    final ConfigProperties testGlobal = test.getGroup("global");
    assertEquals(2, testGlobal.size());
    assertEquals("c", testGlobal.getValue("a"));
    assertEquals("d", testGlobal.getValue("b"));

    final ConfigProperties testBlock = test.getGroup("block");
    assertEquals(3, testBlock.size());
    assertEquals("p", testBlock.getValue("m"));
    assertEquals("c", testBlock.getValue("n"));
    assertEquals("text", testBlock.getValue("o"));
  }

  /**
   * Tests loading a valid ini file that does not have a global section.
   */
  public void testLoadValidEmptyGlobal() {
    final ConfigProperties properties = new ConfigProperties();
    final ComponentConfigIniLoader loader = new ComponentConfigIniLoader(LOGGER, properties);
    final String text =
        "# comment" + NEWLINE
        + "[global]" + NEWLINE
        + "" + NEWLINE
        + "[block]" + NEWLINE
        + "m = p" + NEWLINE;
    final Resource resource = new InMemoryResource(text.getBytes(Charsets.UTF_8), "Test");

    final ComponentConfig test = new ComponentConfig();
    loader.load(resource, 0, test);
    assertEquals(2, test.getGroups().size());

    final ConfigProperties testGlobal = test.getGroup("global");
    assertEquals(0, testGlobal.size());

    final ConfigProperties testBlock = test.getGroup("block");
    assertEquals(1, testBlock.size());
    assertEquals("p", testBlock.getValue("m"));
  }

  /**
   * Tests loading a valid ini file that has a valid group property override.
   */
  public void testLoadValidGroupPropertyOverride() {
    final ConfigProperties properties = new ConfigProperties();
    final ComponentConfigIniLoader loader = new ComponentConfigIniLoader(LOGGER, properties);
    final String text =
        "# comment" + NEWLINE
        + "[block]" + NEWLINE
        + "m = p" + NEWLINE;
    final Resource resource = new InMemoryResource(text.getBytes(Charsets.UTF_8), "Test");
    properties.put("[block].m", "override");

    final ComponentConfig test = new ComponentConfig();
    loader.load(resource, 0, test);
    assertEquals(1, test.getGroups().size());

    final ConfigProperties testBlock = test.getGroup("block");
    assertEquals(1, testBlock.size());
    assertEquals("override", testBlock.getValue("m"));
  }

  //-------------------------------------------------------------------------
  /**
   * Tests that a key cannot be loaded twice.
   */
  @Test(expectedExceptions = ComponentConfigException.class)
  public void testLoadInvalidDoubleKey() {
    final ConfigProperties properties = new ConfigProperties();
    final ComponentConfigIniLoader loader = new ComponentConfigIniLoader(LOGGER, properties);
    final Resource resource = new InMemoryResource(
        "[block]" + NEWLINE
        + "m = p" + NEWLINE
        + "m = s" + NEWLINE
        );

    loader.load(resource, 0, new ComponentConfig());
  }

  /**
   * Tests that the string to be replaced must be valid.
   */
  @Test(expectedExceptions = ComponentConfigException.class)
  public void testLoadInvalidReplacementNotFound() {
    final ConfigProperties properties = new ConfigProperties();
    final ComponentConfigIniLoader loader = new ComponentConfigIniLoader(LOGGER, properties);
    final Resource resource = new InMemoryResource(
        "[block]" + NEWLINE
        + "m = ${notFound}" + NEWLINE
        );

    loader.load(resource, 0, new ComponentConfig());
  }

  /**
   * Tests that a property must be in a group.
   */
  @Test(expectedExceptions = ComponentConfigException.class)
  public void testLoadInvalidPropertyNotInGroup() {
    final ConfigProperties properties = new ConfigProperties();
    final ComponentConfigIniLoader loader = new ComponentConfigIniLoader(LOGGER, properties);
    final Resource resource = new InMemoryResource(
        "m = foo" + NEWLINE
        );

    loader.load(resource, 0, new ComponentConfig());
  }

  /**
   * Tests that there must be an equals sign between the property key and value.
   */
  @Test(expectedExceptions = ComponentConfigException.class)
  public void testLoadInvalidPropertyNoEquals() {
    final ConfigProperties properties = new ConfigProperties();
    final ComponentConfigIniLoader loader = new ComponentConfigIniLoader(LOGGER, properties);
    final Resource resource = new InMemoryResource(
        "[block]" + NEWLINE
        + "m" + NEWLINE
        );

    loader.load(resource, 0, new ComponentConfig());
  }

  /**
   * Test that the property key cannot be empty.
   */
  @Test(expectedExceptions = ComponentConfigException.class)
  public void testLoadInvalidPropertyEmptyKey() {
    final ConfigProperties properties = new ConfigProperties();
    final ComponentConfigIniLoader loader = new ComponentConfigIniLoader(LOGGER, properties);
    final Resource resource = new InMemoryResource(
        "[block]" + NEWLINE
        + "= foo" + NEWLINE
        );

    loader.load(resource, 0, new ComponentConfig());
  }

}
