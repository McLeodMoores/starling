/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.fudgemsg;

import static com.google.common.collect.Lists.newArrayList;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.FudgeRuntimeException;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.fudgemsg.wire.types.FudgeWireType;

import com.opengamma.OpenGammaRuntimeException;

/**
 * Builder to convert inner classes to and from Fudge.
 *
 * @param <T> the bean type
 * @param <K> the type
 */
public final class InnerClassFudgeBuilder<T extends AutoFudgable<K>, K> implements FudgeBuilder<T> {

  /**
   * Creates a builder for inner class.
   */
  public InnerClassFudgeBuilder() { }

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final T auto) {
    final MutableFudgeMsg outerMsg = serializer.newMessage();
    outerMsg.add(null, FudgeSerializer.TYPES_HEADER_ORDINAL, FudgeWireType.STRING, AutoFudgable.class.getName());
    final K inner = auto.object();
    assertValid(inner.getClass());
    try {
      final MutableFudgeMsg msg = outerMsg.addSubMessage("inner", null);
      //save the internal class name
      msg.add(null, FudgeSerializer.TYPES_HEADER_ORDINAL, FudgeWireType.STRING, inner.getClass().getName());

      //save the ctor parameters
      final List<Object> parameters = AccessController.doPrivileged(new PrivilegedAction<List<Object>>() {
        @Override
        public List<Object> run() {
          try {
            final Constructor<?>[] ctors = inner.getClass().getDeclaredConstructors();
            if (ctors.length != 1) {
              throw new IllegalArgumentException("Inner class does not have a single constructor: " + inner.getClass());
            }
            // find all declared parameters of the inner class
            final List<Field> fs = new ArrayList<>(Arrays.asList(inner.getClass().getDeclaredFields()));
            // remove the field representing the parent object from the list
            // only want the compiler synthesized fields which corresponds to ctor parameters
            for (final Iterator<Field> it = fs.iterator(); it.hasNext();) {
              final Field field = it.next();
              if (field.getType().isAssignableFrom(inner.getClass().getEnclosingClass()) || "$jacocoData".equals(field.getName())) {
                it.remove();
              }
            }
            final List<Object> parametersList = newArrayList();
            for (final Field paramField : fs) {
              paramField.setAccessible(true);
              parametersList.add(paramField.get(inner));
            }
            return parametersList;

          } catch (final IllegalAccessException ex) {
            throw new OpenGammaRuntimeException(ex.getMessage());
          }
        }
      });

      for (final Object parameter : parameters) {
        //save the ctor parameter
        serializer.addToMessageWithClassHeaders(msg, null, 1, parameter);
      }
      return outerMsg;

    } catch (final RuntimeException ex) {
      throw new FudgeRuntimeException("Unable to serialize: " + inner.getClass().getName(), ex);
    }
  }

  //-------------------------------------------------------------------------
  @SuppressWarnings("unchecked")
  @Override
  public T buildObject(final FudgeDeserializer deserializer, final FudgeMsg outerMsg) {
    final FudgeField fudgeField = outerMsg.getByName("inner");
    final FudgeMsg msg = (FudgeMsg) fudgeField.getValue();

    final FudgeField classNameField = msg.getByOrdinal(FudgeSerializer.TYPES_HEADER_ORDINAL);
    final String className = (String) classNameField.getValue();
    try {
      final List<Object> parameters = newArrayList();
      parameters.add(null);  //the omitted enclosing object
      for (final FudgeField parameterField : msg.getAllByOrdinal(1)) {
        parameters.add(deserializer.fieldValueToObject(parameterField));
      }

      return (T) AccessController.doPrivileged(new PrivilegedAction<Object>() {
        @Override
        public Object run() {
          final Object[] array = parameters.toArray();
          // find constructor
          Constructor<?> ctor = null;
          try {
            final Class<?> innerClass = Class.forName(className);
            final Constructor<?>[] ctors = innerClass.getDeclaredConstructors();
            if (ctors.length != 1) {
              throw new IllegalArgumentException("Inner class does not have a single constructor: " + className);
            }
            ctor = ctors[0];
            ctor.setAccessible(true);   // solution
          } catch (final ClassNotFoundException ex) {
            throw new RuntimeException(ex);
          }

          // invoke constructor
          try {
            final Object inner = ctor.newInstance(array);
            return new AutoFudgable<>(inner);

          } catch (final IllegalAccessException ex) {
            throw new RuntimeException(ex);
          } catch (final InstantiationException ex) {
            throw new RuntimeException(ex);
          } catch (final InvocationTargetException ex) {
            throw new RuntimeException(ex);
          }
        }
      });

    } catch (final RuntimeException ex) {
      throw new FudgeRuntimeException("Unable to deserialize: " + className, ex);
    }
  }

  private static void assertValid(final Class<?> clazz) {
    if (clazz.getEnclosingClass() == null) {
      throw new OpenGammaRuntimeException("AutoFudgable can be used only with inner classes");
    }
    if (clazz.getSuperclass().getEnclosingClass() != null) {
      throw new OpenGammaRuntimeException("AutoFudgable can be used only with inner classes which enclosing classes are not inner themselves.");
    }
    if (!hasSingleZeroArgConstructor(clazz.getSuperclass())) {
      throw new OpenGammaRuntimeException("AutoFudgable can be used only with inner classes which enclosing classes have single, zero argument constructor.");
    }
  }

  private static boolean hasSingleZeroArgConstructor(final Class<?> clazz) {
    return AccessController.doPrivileged(new PrivilegedAction<Boolean>() {
      @Override
      public Boolean run() {
        final Constructor<?>[] ctors = clazz.getDeclaredConstructors();
        return ctors.length == 1 && ctors[0].getParameterTypes().length == 0;
      }
    });
  }

}
