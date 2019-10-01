/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.engine.function.dsl;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.fail;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.joda.beans.Bean;
import org.joda.beans.BeanBuilder;
import org.joda.beans.ImmutableBean;
import org.joda.beans.impl.direct.DirectBean;
import org.joda.beans.impl.direct.DirectFieldsBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaBean;
import org.joda.beans.impl.direct.DirectPrivateBeanBuilder;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;

/**
 * An abstract base test case for Joda beans that tests Object methods (equals, hashCode) and Fudge encoding.
 */
public abstract class AbstractBeanTestCase extends AbstractFudgeBuilderTestCase {

  /**
   * Returns a list of {@link JodaBeanProperties} which contains:
   * <ul>
   * <li>The type to be tested</li>
   * <li>The property names to be set</li>
   * <li>The property values</li>
   * <li>Alternative property values to produce a different bean (e.g. to test equality).
   * </ul>
   *
   * @return an array of bean properties
   */
  @DataProvider(name = "propertyValues")
  public Object[][] propertyValues() {
    final Collection<JodaBeanProperties<? extends Bean>> propertyCollection = getMultipleJodaBeanProperties();
    final Object[][] values = new Object[propertyCollection.size()][];
    int i = 0;
    for (final JodaBeanProperties<? extends Bean> properties : propertyCollection) {
      values[i++] = new Object[] { properties };
    }
    return values;
  }

  /**
   * Gets the names of the properties and values.
   *
   * @return the properties
   */
  public abstract JodaBeanProperties<? extends Bean> getJodaBeanProperties();

  /**
   * Gets the names of the properties and values.
   *
   * @return the properties
   */
  protected Collection<JodaBeanProperties<? extends Bean>> getMultipleJodaBeanProperties() {
    return Collections.<JodaBeanProperties<? extends Bean>> singleton(getJodaBeanProperties());
  }

  /**
   * Tests equality and hashCode.
   *
   * @param properties
   *          information about the properties to be tested
   * @param <TYPE>
   *          the type of the bean
   */
  @Test(dataProvider = "propertyValues")
  protected <TYPE extends Bean> void testObject(final JodaBeanProperties<TYPE> properties) {
    try {
      final BeanBuilder<TYPE> builder = constructAndPopulateBeanBuilder(properties);
      final BeanBuilder<TYPE> otherBuilder = constructAndPopulateBeanBuilder(properties);
      final TYPE bean = builder.build();
      final TYPE other = otherBuilder.build();
      if (DirectBean.class.isAssignableFrom(properties.getType())) {
        // test the clone() method - immutable beans do not have this method
        final Method cloneMethod = bean.getClass().getMethod("clone", (Class<?>[]) null);
        cloneMethod.setAccessible(true);
        assertEquals(bean, cloneMethod.invoke(bean, (Object[]) null));
      }
      // test Object methods
      assertEquals(bean, bean);
      assertNotEquals(null, bean);
      assertNotEquals(builder, bean);
      assertEquals(bean, other);
      assertEquals(bean.hashCode(), other.hashCode());
      // test getters and bean
      for (int i = 0; i < properties.size(); i++) {
        final String propertyName = properties.getPropertyName(i);
        final Object propertyValue = properties.getPropertyValue(i);
        if (propertyValue != null) {
          assertNotNull(builder.get(propertyName));
        }
        assertEquals(bean.property(propertyName).get(), propertyValue);
        assertEquals(builder.get(propertyName), propertyValue);
      }
    } catch (final Exception e) {
      fail(e.getMessage());
    }
  }

  /**
   * Tests non-equality of beans with different property values.
   *
   * @param properties
   *          information about the properties to be tested
   * @param <TYPE>
   *          the type of the bean
   */
  @Test(dataProvider = "propertyValues")
  protected <TYPE extends Bean> void testNotEquals(final JodaBeanProperties<TYPE> properties) {
    final BeanBuilder<TYPE> builder = constructAndPopulateBeanBuilder(properties);
    final BeanBuilder<TYPE> otherBuilder = constructAndPopulateBeanBuilder(properties);
    assertEquals(builder.build(), otherBuilder.build());
    for (int i = 0; i < properties.size(); i++) {
      // set to a different value
      final String propertyName = properties.getPropertyName(i);
      builder.set(propertyName, properties.getOtherPropertyValue(i));
      assertNotEquals(builder.build(), otherBuilder.build(), "Expected different values for " + propertyName);
      // set back to original
      builder.set(propertyName, properties.getPropertyValue(i));
    }
    assertNotEquals(null, otherBuilder.build());
    assertNotEquals(otherBuilder, otherBuilder.build());
  }

  /**
   * Tests a cycle.
   *
   * @param properties
   *          information about the properties to be tested
   * @param <TYPE>
   *          the type of the bean
   */
  @Test(dataProvider = "propertyValues")
  protected <TYPE extends Bean> void testCycle(final JodaBeanProperties<TYPE> properties) {
    final TYPE bean = constructAndPopulateBeanBuilder(properties).build();
    assertEncodeDecodeCycle(properties.getType(), bean);
  }

  /**
   * Constructs a bean builder and populates the properties that have values.
   *
   * @param properties
   *          the property information
   * @param <TYPE>
   *          the type of the bean
   * @return a bean builder
   */
  @SuppressWarnings("unchecked")
  protected static <TYPE extends Bean> BeanBuilder<TYPE> constructAndPopulateBeanBuilder(final JodaBeanProperties<TYPE> properties) {
    try {
      final Class<TYPE> beanClass = properties.getType();
      if (ImmutableBean.class.isAssignableFrom(beanClass)) {
        final Class<?> clazz = Class.forName(beanClass.getName() + "$Builder");
        if (DirectPrivateBeanBuilder.class.isAssignableFrom(clazz)) {
          final Class<DirectPrivateBeanBuilder<TYPE>> builderClass = (Class<DirectPrivateBeanBuilder<TYPE>>) clazz;
          final Constructor<DirectPrivateBeanBuilder<TYPE>> constructor = builderClass.getDeclaredConstructor();
          constructor.setAccessible(true);
          final DirectPrivateBeanBuilder<TYPE> builder = constructor.newInstance();
          for (int i = 0; i < properties.size(); i++) {
            builder.set(properties.getPropertyName(i), properties.getPropertyValue(i));
          }
          return builder;
        } else if (DirectFieldsBeanBuilder.class.isAssignableFrom(clazz)) {
          final Class<DirectFieldsBeanBuilder<TYPE>> builderClass = (Class<DirectFieldsBeanBuilder<TYPE>>) clazz;
          final Constructor<DirectFieldsBeanBuilder<TYPE>> constructor = builderClass.getDeclaredConstructor();
          constructor.setAccessible(true);
          final DirectFieldsBeanBuilder<TYPE> builder = constructor.newInstance();
          for (int i = 0; i < properties.size(); i++) {
            builder.set(properties.getPropertyName(i), properties.getPropertyValue(i));
          }
          return builder;
        }
      } else if (Bean.class.isAssignableFrom(beanClass)) {
        final Constructor<TYPE> constructor = beanClass.getDeclaredConstructor();
        constructor.setAccessible(true);
        final TYPE emptyBean = constructor.newInstance();
        final DirectMetaBean meta = (DirectMetaBean) emptyBean.metaBean();
        final BeanBuilder<TYPE> builder = (BeanBuilder<TYPE>) meta.builder();
        for (int i = 0; i < properties.size(); i++) {
          builder.set(properties.getPropertyName(i), properties.getPropertyValue(i));
        }
        return builder;
      }
      throw new IllegalArgumentException("Unsupported bean class type " + properties.getClass().getSimpleName());
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
   * <li>Alternative property values to produce a different bean (e.g. to test equality).
   * </ul>
   *
   * @param <T>
   *          the type of the bean
   */
  public class JodaBeanProperties<T extends Bean> {
    private final Class<T> _type;
    private final List<String> _propertyNames;
    private final List<? extends Object> _propertyValues;
    private final List<? extends Object> _otherPropertyValues;

    /**
     * @param beanType
     *          the type of the bean
     * @param propertyNames
     *          the property names to be tested
     * @param propertyValues
     *          the values of the properties
     */
    public JodaBeanProperties(final Class<T> beanType, final List<String> propertyNames, final List<? extends Object> propertyValues) {
      ArgumentChecker.isTrue(propertyNames.size() == propertyValues.size(),
          "Must have one property value for each name: have " + propertyNames.size() + " property names and " + propertyValues.size() + " property values");
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
    public JodaBeanProperties(final Class<T> beanType, final List<String> propertyNames, final List<? extends Object> propertyValues,
        final List<? extends Object> otherPropertyValues) {
      ArgumentChecker.isTrue(propertyNames.size() == propertyValues.size(),
          "Must have one property value for each name: have " + propertyNames.size() + " property names and " + propertyValues.size() + " property values");
      ArgumentChecker.isTrue(propertyNames.size() == otherPropertyValues.size(), "Must have one property value for each name: have " + propertyNames.size()
          + " property names and " + otherPropertyValues.size() + " property values");
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
        throw new IllegalStateException("No other property values available");
      }
      return _otherPropertyValues.get(i);
    }
  }

}
