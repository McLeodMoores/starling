/**
 * This file originates from RESTEasy, which is available under ASLv2.  It has been reformatted.
 */
package com.opengamma.util;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * This class is a trick used to extract GenericType information at runtime.  Java does not allow you get generic
 * type information easily, so this class does the trick.  For example:
 * <p/>
 * <pre>
 * Type genericType = (new GenericType<List<String>>() {}).getGenericType();
 * </pre>
 * <p/>
 * The above code will get you the genericType for List<String>
 * @param <T>  the generic type
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class GenericType<T> {
  private final Class<T> _type;
  private final Type _genericType;

  /**
   * Constructs a new generic entity. Derives represented class from type
    * parameter. Note that this constructor is protected, users should create
    * a (usually anonymous) subclass as shown above.
    * @throws IllegalArgumentException if entity is null
    */
  @SuppressWarnings("unchecked")
  protected GenericType() {
    Type superclass = getClass().getGenericSuperclass();
    if (!(superclass instanceof ParameterizedType)) {
      throw new RuntimeException("Missing type parameter.");
    }
    ParameterizedType parameterized = (ParameterizedType) superclass;
    _genericType = parameterized.getActualTypeArguments()[0];
    _type = (Class<T>) Types.getRawType(_genericType);
  }

  /**
   * Gets the raw type of the enclosed entity. Note that this is the raw type of
   * the instance, not the raw type of the type parameter. I.e. in the example
   * in the introduction, the raw type is {@code ArrayList} not {@code List}.
   *
   * @return the raw type
   */
  public Class<T> getType() {
    return _type;
  }

  /**
   * Gets underlying {@code Type} instance. Note that this is derived from the
   * type parameter, not the enclosed instance. I.e. in the example
   * in the introduction, the type is {@code List<String>} not
   * {@code ArrayList<String>}.
   *
   * @return the type
   */
  public Type getGenericType() {
    return _genericType;
  }

}
