/**
 * Utility class copied from RESTEasy (ASLv2) and reformatted and generified.
 */
package com.opengamma.util;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.HashMap;
import java.util.Map;

/**
 * Type conversions and generic type manipulations
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class Types {
  /**
   * Is the genericType of a certain class?
   * @param clazz  the class
   * @param pType  the parameterized type
   * @return true if the parameterized type is of the given class
   */
  public static boolean isA(final Class<?> clazz, final ParameterizedType pType) {
    return clazz.isAssignableFrom((Class<?>) pType.getRawType());
  }

  /**
   * Gets the index-th type argument.
   * @param pType  the parameterized type
   * @param index  the index of the type argument
   * @return the class of the index-th type argument
   */
  public static Class<?> getArgumentType(final ParameterizedType pType, final int index) {
    return (Class<?>) pType.getActualTypeArguments()[index];
  }

  public static Class<?> getTemplateParameterOfInterface(final Class<?> base, final Class<?> desiredInterface) {
    final Object rtn = searchForInterfaceTemplateParameter(base, desiredInterface);
    if (rtn != null && rtn instanceof Class) {
      return (Class<?>) rtn;
    }
    return null;
  }

  private static Object searchForInterfaceTemplateParameter(final Class<?> base, final Class<?> desiredInterface) {
    for (int i = 0; i < base.getInterfaces().length; i++) {
      final Class<?> intf = base.getInterfaces()[i];
      if (intf.equals(desiredInterface)) {
        final Type generic = base.getGenericInterfaces()[i];
        if (generic instanceof ParameterizedType) {
          final ParameterizedType p = (ParameterizedType) generic;
          final Type type = p.getActualTypeArguments()[0];
          final Class<?> rtn = getRawTypeNoException(type);
          if (rtn != null) {
            return rtn;
          }
          return type;
        } else {
          return null;
        }
      }
    }
    if (base.getSuperclass() == null || base.getSuperclass().equals(Object.class)) {
      return null;
    }
    final Object rtn = searchForInterfaceTemplateParameter(base.getSuperclass(), desiredInterface);
    if (rtn == null || rtn instanceof Class) {
      return rtn;
    }
    if (!(rtn instanceof TypeVariable)) {
      return null;
    }

    final String name = ((TypeVariable<?>) rtn).getName();
    int index = -1;
    final TypeVariable<?>[] variables = base.getSuperclass().getTypeParameters();
    if (variables == null || variables.length < 1) {
      return null;
    }

    for (int i = 0; i < variables.length; i++) {
      if (variables[i].getName().equals(name)) {
        index = i;
      }
    }
    if (index == -1) {
      return null;
    }

    final Type genericSuperclass = base.getGenericSuperclass();
    if (!(genericSuperclass instanceof ParameterizedType)) {
      return null;
    }

    final ParameterizedType pt = (ParameterizedType) genericSuperclass;
    final Type type = pt.getActualTypeArguments()[index];

    final Class<?> clazz = getRawTypeNoException(type);
    if (clazz != null) {
      return clazz;
    }
    return type;
  }

  /**
   * See if the two methods are compatible, that is they have the same relative signature
   *
   * @param method  the method
   * @param intfMethod  the other method
   * @return true, if methods have same relative signature
   */
  public static boolean isCompatible(final Method method, final Method intfMethod) {
    if (method == intfMethod) {
      return true;
    }

    if (!method.getName().equals(intfMethod.getName())) {
      return false;
    }
    if (method.getParameterTypes().length != intfMethod.getParameterTypes().length) {
      return false;
    }

    for (int i = 0; i < method.getParameterTypes().length; i++) {
      final Class<?> rootParam = method.getParameterTypes()[i];
      final Class<?> intfParam = intfMethod.getParameterTypes()[i];
      if (!intfParam.isAssignableFrom(rootParam)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Given a method and a root class, find the actual method declared in the root that implements the method.
   *
   * @param clazz  the class
   * @param intfMethod  the method
   * @return the implementing method
   */
  public static Method getImplementingMethod(final Class<?> clazz, final Method intfMethod)
  {
    final Class<?> declaringClass = intfMethod.getDeclaringClass();
    if (declaringClass.equals(clazz)) {
      return intfMethod;
    }

    Class<?>[] paramTypes = intfMethod.getParameterTypes();

    if (declaringClass.getTypeParameters().length > 0 && paramTypes.length > 0) {
      final Type[] intfTypes = findParameterizedTypes(clazz, declaringClass);
      final Map<String, Type> typeVarMap = new HashMap<>();
      final TypeVariable<? extends Class<?>>[] vars = declaringClass.getTypeParameters();
      for (int i = 0; i < vars.length; i++) {
        if (intfTypes != null && i < intfTypes.length) {
          typeVarMap.put(vars[i].getName(), intfTypes[i]);
        } else {
          // Interface type parameters may not have been filled out
          typeVarMap.put(vars[i].getName(), vars[i].getGenericDeclaration());
        }
      }
      final Type[] paramGenericTypes = intfMethod.getGenericParameterTypes();
      paramTypes = new Class[paramTypes.length];

      for (int i = 0; i < paramTypes.length; i++) {
        if (paramGenericTypes[i] instanceof TypeVariable) {
          final TypeVariable<?> tv = (TypeVariable<?>) paramGenericTypes[i];
          final Type t = typeVarMap.get(tv.getName());
          if (t == null) {
            throw new RuntimeException("Unable to resolve type variable");
          }
          paramTypes[i] = getRawType(t);
        } else {
          paramTypes[i] = getRawType(paramGenericTypes[i]);
        }
      }

    }

    try {
      return clazz.getMethod(intfMethod.getName(), paramTypes);
    } catch (final NoSuchMethodException e) {
    }

    try {
      final Method tmp = clazz.getMethod(intfMethod.getName(), intfMethod.getParameterTypes());
      return tmp;
    } catch (final NoSuchMethodException e) {
    }
    return intfMethod;
  }

  public static Class<?> getRawType(final Type type) {
    if (type instanceof Class<?>) {
      // type is a normal class.
      return (Class<?>) type;
    } else if (type instanceof ParameterizedType) {
      final ParameterizedType parameterizedType = (ParameterizedType) type;
      final Type rawType = parameterizedType.getRawType();
      return (Class<?>) rawType;
    } else if (type instanceof GenericArrayType) {
      final GenericArrayType genericArrayType = (GenericArrayType) type;
      final Class<?> componentRawType = getRawType(genericArrayType.getGenericComponentType());
      return Array.newInstance(componentRawType, 0).getClass();
    } else if (type instanceof TypeVariable) {
      final TypeVariable<?> typeVar = (TypeVariable<?>) type;
      if (typeVar.getBounds() != null && typeVar.getBounds().length > 0) {
        return getRawType(typeVar.getBounds()[0]);
      }
    }
    throw new RuntimeException("Unable to determine base class from Type");
  }

  public static Class<?> getRawTypeNoException(final Type type) {
    if (type instanceof Class<?>) {
      // type is a normal class.
      return (Class<?>) type;
    } else if (type instanceof ParameterizedType) {
      final ParameterizedType parameterizedType = (ParameterizedType) type;
      final Type rawType = parameterizedType.getRawType();
      return (Class<?>) rawType;
    } else if (type instanceof GenericArrayType) {
      final GenericArrayType genericArrayType = (GenericArrayType) type;
      final Class<?> componentRawType = getRawType(genericArrayType.getGenericComponentType());
      return Array.newInstance(componentRawType, 0).getClass();
    }
    return null;
  }

  /**
   * Returns the type argument from a parameterized type
   *
   * @param genericType  the type
   * @return null if there is no type parameter
   */
  public static Class<?> getTypeArgument(final Type genericType) {
    if (!(genericType instanceof ParameterizedType)) {
      return null;
    }
    final ParameterizedType parameterizedType = (ParameterizedType) genericType;
    final Class<?> typeArg = (Class<?>) parameterizedType.getActualTypeArguments()[0];
    return typeArg;
  }

  public static Class<?> getCollectionBaseType(final Class<?> type, final Type genericType) {
    if (genericType instanceof ParameterizedType) {
      final ParameterizedType parameterizedType = (ParameterizedType) genericType;
      final Type componentGenericType = parameterizedType.getActualTypeArguments()[0];
      return getRawType(componentGenericType);
    } else if (genericType instanceof GenericArrayType) {
      final GenericArrayType genericArrayType = (GenericArrayType) genericType;
      final Type componentGenericType = genericArrayType.getGenericComponentType();
      return getRawType(componentGenericType);
    } else if (type.isArray()) {
      return type.getComponentType();
    }
    return null;
  }

  public static Class<?> getMapKeyType(final Type genericType) {
    if (genericType instanceof ParameterizedType) {
      final ParameterizedType parameterizedType = (ParameterizedType) genericType;
      final Type componentGenericType = parameterizedType.getActualTypeArguments()[0];
      return getRawType(componentGenericType);
    }
    return null;
  }

  public static Class<?> getMapValueType(final Type genericType) {
    if (genericType instanceof ParameterizedType) {
      final ParameterizedType parameterizedType = (ParameterizedType) genericType;
      final Type componentGenericType = parameterizedType.getActualTypeArguments()[1];
      return getRawType(componentGenericType);
    }
    return null;
  }

  public static Type resolveTypeVariables(final Class<?> root, final Type type) {
    if (type instanceof TypeVariable) {
      final Type newType = resolveTypeVariable(root, (TypeVariable<?>) type);
      return newType == null ? type : newType;
    } else if (type instanceof ParameterizedType) {
      final ParameterizedType param = (ParameterizedType) type;
      final Type[] actuals = new Type[param.getActualTypeArguments().length];
      for (int i = 0; i < actuals.length; i++) {
        final Type newType = resolveTypeVariables(root, param.getActualTypeArguments()[i]);
        actuals[i] = newType == null ? param.getActualTypeArguments()[i] : newType;
      }
      return new ParameterizedType() {
        @Override
        public Type[] getActualTypeArguments() {
          return actuals;
        }

        @Override
        public Type getRawType() {
          return param.getRawType();
        }

        @Override
        public Type getOwnerType() {
          return param.getOwnerType();
        }
      };
    } else if (type instanceof GenericArrayType) {
      final GenericArrayType arrayType = (GenericArrayType) type;
      final Type componentType = resolveTypeVariables(root, arrayType.getGenericComponentType());
      if (componentType == null) {
        return type;
      }
      return new GenericArrayType() {
        @Override
        public Type getGenericComponentType() {
          return componentType;
        }
      };
    } else {
      return type;
    }
  }

  /**
   * Finds an actual value of a type variable. The method looks in a class hierarchy for a class defining the variable
   * and returns the value if present.
   *
   * @param root  the root class
   * @param typeVariable  the type variable
   * @return actual type of the type variable
   */
  public static Type resolveTypeVariable(final Class<?> root, final TypeVariable<?> typeVariable) {
    if (typeVariable.getGenericDeclaration() instanceof Class<?>) {
      final Class<?> classDeclaringTypeVariable = (Class<?>) typeVariable.getGenericDeclaration();
      final Type[] types = findParameterizedTypes(root, classDeclaringTypeVariable);
      if (types == null) {
        return null;
      }
      for (int i = 0; i < types.length; i++) {
        final TypeVariable<?> tv = classDeclaringTypeVariable.getTypeParameters()[i];
        if (tv.equals(typeVariable)) {
          return types[i];
        }
      }
    }
    return null;
  }

  /**
   * Given a class and an interfaces, go through the class hierarchy to find the interface and return its type arguments.
   *
   * @param classToSearch  class whose hierarchy to search
   * @param interfaceToFind  interface being looked for
   * @return type arguments of the interface
   */
  public static Type[] getActualTypeArgumentsOfAnInterface(final Class<?> classToSearch, final Class<?> interfaceToFind) {
    final Type[] types = findParameterizedTypes(classToSearch, interfaceToFind);
    if (types == null) {
      throw new RuntimeException("Unable to find type arguments of " + interfaceToFind);
    }
    return types;
  }

  private static final Type[] EMPTY_TYPE_ARRAY = {};

  /**
   * Search for the given interface or class within the root's class/interface hierarchy.
   * If the searched for class/interface is a generic return an array of real types that fill it out.
   *
   * @param root  class whose hierarchy to search
   * @param searchedFor  interface or class to search for
   * @return array of real types that fill it out
   */
  public static Type[] findParameterizedTypes(final Class<?> root, final Class<?> searchedFor) {
    if (searchedFor.isInterface()) {
      return findInterfaceParameterizedTypes(root, null, searchedFor);
    }
    return findClassParameterizedTypes(root, null, searchedFor);
  }

  public static Type[] findClassParameterizedTypes(final Class<?> root, final ParameterizedType rootType, final Class<?> searchedForClass) {
    if (Object.class.equals(root)) {
      return null;
    }

    final Map<String, Type> typeVarMap = populateParameterizedMap(root, rootType);

    final Class<?> superclass = root.getSuperclass();
    final Type genericSuper = root.getGenericSuperclass();

    if (superclass.equals(searchedForClass)) {
      return extractTypes(typeVarMap, genericSuper);
    }

    if (genericSuper instanceof ParameterizedType) {
      final ParameterizedType intfParam = (ParameterizedType) genericSuper;
      final Type[] types = findClassParameterizedTypes(superclass, intfParam, searchedForClass);
      if (types != null) {
        return extractTypeVariables(typeVarMap, types);
      }
    } else {
      final Type[] types = findClassParameterizedTypes(superclass, null, searchedForClass);
      if (types != null) {
        return types;
      }
    }
    return null;
  }

  private static Map<String, Type> populateParameterizedMap(final Class<?> root, final ParameterizedType rootType) {
    final Map<String, Type> typeVarMap = new HashMap<>();
    if (rootType != null) {
      final TypeVariable<? extends Class<?>>[] vars = root.getTypeParameters();
      for (int i = 0; i < vars.length; i++) {
        typeVarMap.put(vars[i].getName(), rootType.getActualTypeArguments()[i]);
      }
    }
    return typeVarMap;
  }

  public static Type[] findInterfaceParameterizedTypes(final Class<?> root, final ParameterizedType rootType, final Class<?> searchedForInterface)
  {
    final Map<String, Type> typeVarMap = populateParameterizedMap(root, rootType);

    for (int i = 0; i < root.getInterfaces().length; i++) {
      final Class<?> sub = root.getInterfaces()[i];
      final Type genericSub = root.getGenericInterfaces()[i];
      if (sub.equals(searchedForInterface)) {
        return extractTypes(typeVarMap, genericSub);
      }
    }

    for (int i = 0; i < root.getInterfaces().length; i++) {
      final Type genericSub = root.getGenericInterfaces()[i];
      final Class<?> sub = root.getInterfaces()[i];

      final Type[] types = recurseSuperclassForInterface(searchedForInterface, typeVarMap, genericSub, sub);
      if (types != null) {
        return types;
      }
    }
    if (root.isInterface()) {
      return null;
    }

    final Class<?> superclass = root.getSuperclass();
    final Type genericSuper = root.getGenericSuperclass();

    return recurseSuperclassForInterface(searchedForInterface, typeVarMap, genericSuper, superclass);
  }

  private static Type[] recurseSuperclassForInterface(final Class<?> searchedForInterface, final Map<String, Type> typeVarMap, final Type genericSub, final Class<?> sub) {
    if (genericSub instanceof ParameterizedType) {
      final ParameterizedType intfParam = (ParameterizedType) genericSub;
      final Type[] types = findInterfaceParameterizedTypes(sub, intfParam, searchedForInterface);
      if (types != null) {
        return extractTypeVariables(typeVarMap, types);
      }
    } else {
      final Type[] types = findInterfaceParameterizedTypes(sub, null, searchedForInterface);
      if (types != null) {
        return types;
      }
    }
    return null;
  }

  private static Type[] extractTypeVariables(final Map<String, Type> typeVarMap, final Type[] types) {
    for (int j = 0; j < types.length; j++) {
      if (types[j] instanceof TypeVariable) {
        final TypeVariable<?> tv = (TypeVariable<?>) types[j];
        types[j] = typeVarMap.get(tv.getName());
      } else {
        types[j] = types[j];
      }
    }
    return types;
  }

  private static Type[] extractTypes(final Map<String, Type> typeVarMap, final Type genericSub) {
    if (genericSub instanceof ParameterizedType) {
      final ParameterizedType param = (ParameterizedType) genericSub;
      final Type[] types = param.getActualTypeArguments();
      final Type[] returnTypes = new Type[types.length];
      System.arraycopy(types, 0, returnTypes, 0, types.length);
      extractTypeVariables(typeVarMap, returnTypes);
      return returnTypes;
    } else {
      return EMPTY_TYPE_ARRAY;
    }
  }
}
