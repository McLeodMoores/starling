/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.config.impl;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.testng.annotations.Test;

import com.opengamma.core.marketdatasnapshot.CurveKey;
import com.opengamma.id.UniqueId;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Tests the {@link ConfigItem} class.
 */
@Test(groups = TestGroup.UNIT)
public class ConfigItemTest {

  /**
   * Tests construction with null.
   */
  @Test
  public void testOfNull() {
    final UniqueId uid = UniqueId.of("UID", "1");
    ConfigItem<Object> item = ConfigItem.of(null);
    item.setUniqueId(uid);
    // unique id must be set to get object id
    assertNull(item.getName());
    assertEquals(item.getObjectId(), uid.getObjectId());
    assertNull(item.getType());
    assertEquals(item.getUniqueId(), uid);
    assertNull(item.getValue());

    item = ConfigItem.of(null, "p");
    item.setUniqueId(uid);
    assertEquals(item.getName(), "p");
    assertEquals(item.getObjectId(), uid.getObjectId());
    assertNull(item.getType());
    assertEquals(item.getUniqueId(), uid);
    assertNull(item.getValue());
  }

  /**
   * Tests construction.
   */
  @Test
  public void testOf() {
    final TestDocument doc = new TestDocument();
    final UniqueId uid = UniqueId.of("UID", "2");
    ConfigItem<TestDocument> item = ConfigItem.of(doc);
    item.setUniqueId(uid);
    assertNull(item.getName());
    assertEquals(item.getObjectId(), uid.getObjectId());
    assertEquals(item.getType(), TestDocument.class);
    assertEquals(item.getUniqueId(), uid);
    assertEquals(item.getValue(), doc);

    item = ConfigItem.of(doc, "o");
    item.setUniqueId(uid);
    assertEquals(item.getName(), "o");
    assertEquals(item.getObjectId(), uid.getObjectId());
    assertEquals(item.getType(), TestDocument.class);
    assertEquals(item.getUniqueId(), uid);
    assertEquals(item.getValue(), doc);
  }

  /**
   * Tests construction.
   */
  @Test
  public void testOfNamed() {
    final UniqueId uid = UniqueId.of("UID", "3");
    final NamedTestDocument doc = new NamedTestDocument("n");
    ConfigItem<NamedTestDocument> item = ConfigItem.of(doc);
    item.setUniqueId(uid);
    assertEquals(item.getName(), "n");
    assertEquals(item.getObjectId(), uid.getObjectId());
    assertEquals(item.getType(), NamedTestDocument.class);
    assertEquals(item.getUniqueId(), uid);
    assertEquals(item.getValue(), doc);

    item = ConfigItem.of(doc, "m");
    item.setUniqueId(uid);
    assertEquals(item.getName(), "m");
    assertEquals(item.getObjectId(), uid.getObjectId());
    assertEquals(item.getType(), NamedTestDocument.class);
    assertEquals(item.getUniqueId(), uid);
    assertEquals(item.getValue(), doc);
  }

  /**
   * Tests construction using a bean.
   */
  @Test
  public void testOfBean() {
    final DoublesPair pair = DoublesPair.of(1., 3.);
    final UniqueId uid = UniqueId.of("UID", "4");
    ConfigItem<DoublesPair> item = ConfigItem.of(pair);
    item.setUniqueId(uid);
    assertNull(item.getName());
    assertEquals(item.getObjectId(), uid.getObjectId());
    assertEquals(item.getType(), DoublesPair.class);
    assertEquals(item.getUniqueId(), uid);
    assertEquals(item.getValue(), pair);

    item = ConfigItem.of(pair, "i");
    item.setUniqueId(uid);
    assertEquals(item.getName(), "i");
    assertEquals(item.getObjectId(), uid.getObjectId());
    assertEquals(item.getType(), DoublesPair.class);
    assertEquals(item.getUniqueId(), uid);
    assertEquals(item.getValue(), pair);
  }

  /**
   * Tests construction using a named bean.
   */
  @Test
  public void testOfNamedBean() {
    final CurveKey key = CurveKey.of("curve");
    final UniqueId uid = UniqueId.of("UID", "5");
    ConfigItem<CurveKey> item = ConfigItem.of(key);
    item.setUniqueId(uid);
    assertEquals(item.getName(), "curve");
    assertEquals(item.getObjectId(), uid.getObjectId());
    assertEquals(item.getType(), CurveKey.class);
    assertEquals(item.getUniqueId(), uid);
    assertEquals(item.getValue(), key);

    item = ConfigItem.of(key, "u");
    item.setUniqueId(uid);
    assertEquals(item.getName(), "u");
    assertEquals(item.getObjectId(), uid.getObjectId());
    assertEquals(item.getType(), CurveKey.class);
    assertEquals(item.getUniqueId(), uid);
    assertEquals(item.getValue(), key);
  }

  /**
   * Tests an encoding/decoding cycle.
   */
  @Test
  public void testFudgeCycle() {
    final ConfigItem<CurveKey> item = ConfigItem.of(CurveKey.of("curve"), "name");
    final MutableFudgeMsg msg = OpenGammaFudgeContext.getInstance().newMessage();
    item.toFudgeMsg(new FudgeSerializer(OpenGammaFudgeContext.getInstance()), msg);
    assertEquals(ConfigItem.fromFudgeMsg(new FudgeDeserializer(OpenGammaFudgeContext.getInstance()), msg), item);
  }

  /**
   * Tests the serialization of an item.
   *
   * @throws Exception  if there is a problem serializing or deserializing an object.
   */
  public void testJavaSerialization() throws Exception {
    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    final ObjectOutputStream out = new ObjectOutputStream(baos);
    final TestDocument d1 = new TestDocument();
    d1.setFoo(42);
    final ConfigItem<?> item1 = ConfigItem.of(d1, "SerializationTest1", TestDocument.class);
    final TestDocument d2 = new TestDocument();
    d2.setFoo(96);
    final ConfigItem<?> item2 = ConfigItem.of(d2, "SerializationTest2", TestDocument.class);
    item2.setUniqueId(UniqueId.of("UID", "1"));
    out.writeObject(item1);
    out.writeObject(item2);
    out.flush();
    final ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
    final ObjectInputStream in = new ObjectInputStream(bais);
    ConfigItem<?> item = (ConfigItem<?>) in.readObject();
    assertEquals(item.getType(), TestDocument.class);
    assertEquals(item.getName(), "SerializationTest1");
    assertEquals(item.getValue(), d1);
    assertEquals(item.getUniqueId(), null);
    assertEquals(item, item1);
    item = (ConfigItem<?>) in.readObject();
    assertEquals(item.getType(), TestDocument.class);
    assertEquals(item.getName(), "SerializationTest2");
    assertEquals(item.getValue(), d2);
    assertEquals(item, item2);
    assertEquals(item2.getUniqueId(), UniqueId.of("UID", "1"));
  }

  /**
   * A test config document.
   */
  public static class TestDocument {
    private int _foo;

    /**
     * Sets an underlying.
     *
     * @param foo  the underlying
     */
    public void setFoo(final int foo) {
      _foo = foo;
    }

    /**
     * Gets the underlying.
     *
     * @return  the underlying
     */
    public int getFoo() {
      return _foo;
    }

    @Override
    public String toString() {
      return "ConfigDocument[" + _foo + "]";
    }

    @Override
    public int hashCode() {
      return _foo;
    }

    @Override
    public boolean equals(final Object o) {
      return o instanceof TestDocument && ((TestDocument) o)._foo == _foo;
    }
  }

  /**
   * A named test config document.
   */
  public static final class NamedTestDocument extends TestDocument {
    private final String _name;

    /**
     * Constructor.
     *
     * @param name  the name
     */
    public NamedTestDocument(final String name) {
      super();
      _name = name;
    }

    /**
     * Gets the name.
     *
     * @return  the name
     */
    public String getName() {
      return _name;
    }

    @Override
    public int hashCode() {
      return super.hashCode() ^ _name.hashCode();
    }

    @Override
    public boolean equals(final Object o) {
      if (o instanceof NamedTestDocument) {
        return super.equals(o) && _name.equals(((NamedTestDocument) o)._name);
      }
      return false;
    }
  }
}
