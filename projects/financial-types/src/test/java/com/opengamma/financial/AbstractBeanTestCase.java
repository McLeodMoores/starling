/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.financial;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.fail;

import java.lang.reflect.Constructor;
import java.util.List;

import org.joda.beans.BeanBuilder;
import org.joda.beans.impl.direct.DirectBean;
import org.joda.beans.impl.direct.DirectMetaBean;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;

/**
 * An abstract base test case for Joda beans that tests Object methods (equals,
 * hashCode) and Fudge encoding.
 */
public abstract class AbstractBeanTestCase extends AbstractFudgeBuilderTestCase {

  /**
   * Returns a list of {@link JodaBeanProperties} which contains:
   * <ul>
   * <li>The type to be tested</li>
   * <li>The property names to be set</li>
   * <li>The property values</li>
   * <li>Alternative property values to produce a different bean (e.g. to test
   * equality).
   * </ul>
   *
   * @return an array of bean properties
   */
  @DataProvider(name = "propertyValues")
  public abstract Object[][] propertyValues();

  /**
   * Tests equality and hashCode.
   *
   * @param properties
   *          information about the properties to be tested
   */
  @Test(dataProvider = "propertyValues")
  protected <TYPE extends DirectBean> void testObject(final JodaBeanProperties<TYPE> properties) {
    final BeanBuilder<TYPE> builder = constructAndPopulateBeanBuilder(properties);
    final BeanBuilder<TYPE> otherBuilder = constructAndPopulateBeanBuilder(properties);
    final TYPE bean = builder.build();
    final TYPE other = otherBuilder.build();
    // test Object methods
    assertEquals(bean, bean);
    assertEquals(bean, bean.clone());
    assertNotEquals(null, bean);
    assertNotEquals(builder, bean);
    assertEquals(bean, other);
    assertEquals(bean.hashCode(), other.hashCode());
    // test getters and bean
    for (int i = 0; i < properties.size(); i++) {
      final String propertyName = properties.getPropertyName(i);
      assertEquals(bean.property(propertyName).get(), properties.getPropertyValue(i));
      assertEquals(builder.get(propertyName), properties.getPropertyValue(i));
    }
  }

  /**
   * Tests non-equality of beans with different property values.
   *
   * @param properties
   *          information about the properties to be tested
   */
  @Test(dataProvider = "propertyValues")
  protected <TYPE extends DirectBean> void testNotEquals(final JodaBeanProperties<TYPE> properties) {
    final BeanBuilder<TYPE> builder = constructAndPopulateBeanBuilder(properties);
    final BeanBuilder<TYPE> otherBuilder = constructAndPopulateBeanBuilder(properties);
    assertEquals(builder.build(), otherBuilder.build());
    for (int i = 0; i < properties.size(); i++) {
      // set to a different value
      builder.set(properties.getPropertyName(i), properties.getOtherPropertyValue(i));
      assertNotEquals(builder.build(), otherBuilder.build());
      // set back to original
      builder.set(properties.getPropertyName(i), properties.getPropertyValue(i));
    }
  }

  /**
   * Tests a cycle.
   *
   * @param properties
   *          information about the properties to be tested
   */
  @Test(dataProvider = "propertyValues")
  protected <TYPE extends DirectBean> void testCycle(final JodaBeanProperties<TYPE> properties) {
    final TYPE bean = constructAndPopulateBeanBuilder(properties).build();
    assertEncodeDecodeCycle(properties.getType(), bean);
  }

  /**
   * Constructs a bean builder and populates the properties that have values.
   *
   * @param properties
   *          the property information
   * @return a bean builder
   */
  @SuppressWarnings("unchecked")
  protected static <TYPE extends DirectBean> BeanBuilder<TYPE> constructAndPopulateBeanBuilder(final JodaBeanProperties<TYPE> properties) {
    try {
      final Class<TYPE> beanClass = properties.getType();
      final Constructor<TYPE> constructor = beanClass.getDeclaredConstructor();
      constructor.setAccessible(true);
      final TYPE emptyBean = constructor.newInstance();
      final DirectMetaBean meta = (DirectMetaBean) emptyBean.metaBean();
      final BeanBuilder<TYPE> builder = (BeanBuilder<TYPE>) meta.builder();
      for (int i = 0; i < properties.size(); i++) {
        builder.set(properties.getPropertyName(i), properties.getPropertyValue(i));
      }
      return builder;
    } catch (final Exception e) {
      fail("Could not construct meta bean of type " + properties.getType().getSimpleName() + ", error was : " + e.getMessage());
      return null;
    }
  }

  /**
   * A class containing information about a Joda bean:
   * <ul>
   * <li>The type to be tested</li>
   * <li>The property names to be set</li>
   * <li>The property values</li>
   * <li>Alternative property values to produce a different bean (e.g. to test
   * equality).
   * </ul>
   *
   * @param <T>
   *          the type of the bean
   */
  public class JodaBeanProperties<T extends DirectBean> {
    private final Class<T> _type;
    private final List<String> _propertyNames;
    private final List<Object> _propertyValues;
    private final List<Object> _otherPropertyValues;

    /**
     * @param beanType
     *          the type of the bean
     * @param propertyNames
     *          the property names to be tested
     * @param propertyValues
     *          the values of the properties
     */
    public JodaBeanProperties(final Class<T> beanType, final List<String> propertyNames, final List<Object> propertyValues) {
      ArgumentChecker.isTrue(propertyNames.size() == propertyValues.size(), "Must have one property value for each name");
      _type = beanType;
      _propertyNames = propertyNames;
      _propertyValues = propertyValues;
      _otherPropertyValues = null;
    }

    /**
     * @param beanType
     *          the type of the bean
     * @param propertyNames
     *          the property names to be tested
     * @param propertyValues
     *          the values of the properties
     * @param otherPropertyValues
     *          different values of the properties
     */
    public JodaBeanProperties(final Class<T> beanType, final List<String> propertyNames, final List<Object> propertyValues,
        final List<Object> otherPropertyValues) {
      ArgumentChecker.isTrue(propertyNames.size() == propertyValues.size(), "Must have one property value for each name");
      ArgumentChecker.isTrue(propertyNames.size() == otherPropertyValues.size(), "Must have one property value for each name");
      _type = beanType;
      _propertyNames = propertyNames;
      _propertyValues = propertyValues;
      _otherPropertyValues = otherPropertyValues;
    }

    /**
     * Gets the type of the bean.
     *
     * @return the type
     */
    public Class<T> getType() {
      return _type;
    }

    /**
     * Gets the number of properties.
     *
     * @return the number of properties
     */
    public int size() {
      return _propertyNames.size();
    }

    /**
     * Gets the ith property name (0-based).
     *
     * @param i
     *          the number of the property
     * @return the property name
     */
    public String getPropertyName(final int i) {
      return _propertyNames.get(i);
    }

    /**
     * Gets the ith property value (0-based).
     *
     * @param i
     *          the number of the property
     * @return the property value
     */
    public Object getPropertyValue(final int i) {
      return _propertyValues.get(i);
    }

    /**
     * Gets the ith property value (0-based).
     *
     * @param i
     *          the number of the property
     * @return the property value
     */
    public Object getOtherPropertyValue(final int i) {
      if (_otherPropertyValues == null) {
        throw new IllegalStateException();
      }
      return _otherPropertyValues.get(i);
    }
  }

}
