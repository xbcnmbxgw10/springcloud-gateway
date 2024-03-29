/*
 * Copyright 2017 ~ 2025 the original author or authors. <springcloudgateway@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springcloud.gateway.core.core;

import static java.lang.reflect.Modifier.isAbstract;
import static java.lang.reflect.Modifier.isPublic;
import static java.lang.reflect.Modifier.isFinal;
import static java.lang.reflect.Modifier.isNative;
import static java.lang.reflect.Modifier.isStatic;
import static java.lang.reflect.Modifier.isSynchronized;
import static java.lang.reflect.Modifier.isTransient;
import static java.lang.reflect.Modifier.isVolatile;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.startsWith;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.UndeclaredThrowableException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotNull;

import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springcloud.gateway.core.collection.ConcurrentReferenceHashMap;

import static org.springcloud.gateway.core.core.TypeUtils2.isSimpleCollectionType;
import static org.springcloud.gateway.core.core.TypeUtils2.isSimpleType;
import static org.springcloud.gateway.core.lang.Assert2.*;

/**
 * Enhanced utility class for working with the reflection API and handling
 * reflection exceptions.
 *
 * <p>
 * Only intended for internal use.
 *
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @author Rod Johnson
 * @author Costin Leau
 * @author Sam Brannen
 * @author Chris Beams
 * @since 1.2.2 {@link org.springframework.util.ReflectionUtils}
 */
public abstract class ReflectionUtils2 {

    // --- Extended reflection's. ---

    /**
     * Assert whether the two types are compatible
     * 
     * @return
     */
    public static boolean isCompatibleType(@NotNull Class<?> clazz1, @NotNull Class<?> clazz2) {
        isTrueOf((nonNull(clazz1) && nonNull(clazz2)), "clazz1 != null and clazz2 != null");
        return clazz1.isAssignableFrom(clazz2) || clazz2.isAssignableFrom(clazz1);
    }

    /**
     * Check for common security accessible modifiers.
     * 
     * @param modifer
     * @return
     */
    public static boolean isGenericModifier(int modifer) {
        return !(isFinal(modifer) || isStatic(modifer) || isTransient(modifer) || isNative(modifer) || isVolatile(modifer)
                || isSynchronized(modifer));
    }

    /**
     * Invoke the given callback on all fields in the target class, going up the
     * class hierarchy to get all declared fields.
     * 
     * @param clazz
     *            the target class to analyze
     * @param ff
     *            the filter that determines the fields to apply the callback to
     * @param fc
     *            the callback to invoke for each field
     */
    public static void doFullWithFields(final Object obj, FieldFilter ff, FieldHandler fc) {
        isTrue((nonNull(ff) && nonNull(fc)), "FieldFilter and FieldCallback can't null");

        // No continue
        if (isNull(obj)) {
            return;
        }

        // Keep backing up the inheritance hierarchy.
        Class<?> cls = obj.getClass();
        do {
            for (Field field : getDeclaredFields(cls)) {
                if (ff != null && !ff.matches(field)) {
                    continue;
                }
                try {
                    makeAccessible(field);
                    Class<?> fieldCls = field.getType();
                    if (!ff.describeForObjField(field) || isSimpleType(fieldCls) || isSimpleCollectionType(fieldCls)) {
                        fc.doWith(field, obj);
                    }
                    // Recursive traversal matching and processing
                    else {
                        doFullWithFields(getField(field, obj), ff, fc);
                    }
                } catch (IllegalAccessException ex) {
                    throw new IllegalStateException("Not allowed to access field '" + field.getName() + "': " + ex);
                }
            }
        } while (nonNull(cls = cls.getSuperclass()) && cls != Object.class);
    }

    /**
     * Gets the value list of the class field, which can be filtered according
     * to {@link #excludeFieldModifiers} and {@link #excludeFieldNamePrefixs}
     * conditions.
     * 
     * @param <T>
     * @param objectOrClass
     * @param excludeFieldModifiers
     *            Access modifiers for fields to exclude, ignored when the
     *            condition is null.
     * @param excludeFieldNamePrefixs
     *            Access fields name for fields to exclude, ignored when the
     *            condition is null.
     * @return Returns a collection of values for fields that meet both
     *         condition {@link #excludeFieldModifiers} and condition
     *         {@link #excludeFieldNamePrefixs}
     */
    @SuppressWarnings("unchecked")
    public static <T> List<T> getFieldValues(
            @NotNull Object objectOrClass,
            @Nullable int[] excludeFieldModifiers,
            @Nullable String... excludeFieldNamePrefixs) {
        notNullOf(objectOrClass, "objectOrClass");

        Class<?> clazz = null; // [NotNull]
        Object obj = null; // [Nullable]
        if (objectOrClass instanceof Class) {
            clazz = ((Class<?>) objectOrClass);
        } else {
            clazz = objectOrClass.getClass();
            obj = objectOrClass;
        }

        List<T> fieldVals = new ArrayList<>(8);
        for (Field f : getDeclaredFields(clazz)) {
            int mod = f.getModifiers();
            // When the exclusion object is null, only static fields must be
            // obtained.
            if (isNull(obj) && !isStatic(mod)) {
                continue;
            }

            // Filtering field name prefix.
            boolean exclude1 = false;
            if (nonNull(excludeFieldNamePrefixs)) {
                String fname = f.getName();
                for (String prefix : excludeFieldNamePrefixs) {
                    if (startsWith(fname, prefix)) {
                        exclude1 = true;
                        break;
                    }
                }
            }
            // Filtering field modifiers.
            boolean exclude2 = false;
            if (nonNull(excludeFieldModifiers)) {
                for (int m : excludeFieldModifiers) {
                    if ((m & mod) != 0) { // Matched
                        exclude2 = true;
                        break;
                    }
                }
            }
            if (!exclude1 && !exclude2) {
                fieldVals.add((T) getField(f, obj));
            }
        }

        return fieldVals;
    }

    //
    // --- org.springframework.util.ReflectionUtils. ---
    //

    /**
     * Attempt to find a {@link Field field} on the supplied {@link Class} with
     * the supplied {@code name}. Searches all superclasses up to
     * {@link Object}.
     * 
     * @param clazz
     *            the class to introspect
     * @param name
     *            the name of the field
     * @param type
     *            the type of the field (may be {@code null} if name is
     *            specified)
     * @return the corresponding Field object, or {@code null} if not found
     */
    public static Field findFieldNullable(@Nullable Class<?> clazz, @NotNull String name, @Nullable Class<?> type) {
        return nonNull(clazz) ? findField(clazz, name, type) : null;
    }

    /**
     * Attempt to find a {@link Field field} on the supplied {@link Class} with
     * the supplied {@code name}. Searches all superclasses up to
     * {@link Object}.
     * 
     * @param clazz
     *            the class to introspect
     * @param name
     *            the name of the field
     * @return the corresponding Field object, or {@code null} if not found
     */
    public static Field findField(@NotNull Class<?> clazz, @NotNull String name) {
        return findField(clazz, name, null);
    }

    /**
     * Attempt to find a {@link Field field} on the supplied {@link Class} with
     * the supplied {@code name} and/or {@link Class type}. Searches all
     * superclasses up to {@link Object}.
     * 
     * @param clazz
     *            the class to introspect
     * @param name
     *            the name of the field (may be {@code null} if type is
     *            specified)
     * @param type
     *            the type of the field (may be {@code null} if name is
     *            specified)
     * @return the corresponding Field object, or {@code null} if not found
     */
    public static Field findField(@NotNull Class<?> clazz, @NotNull String name, Class<?> type) {
        notNull(clazz, "Class must not be null");
        isTrue(name != null || type != null, "Either name or type of the field must be specified");
        Class<?> searchType = clazz;
        while (Object.class != searchType && searchType != null) {
            Field[] fields = getDeclaredFields(searchType);
            for (Field field : fields) {
                if ((name == null || name.equals(field.getName())) && (type == null || type.equals(field.getType()))) {
                    return field;
                }
            }
            searchType = searchType.getSuperclass();
        }
        return null;
    }

    /**
     * Attempt to find a {@link Field field} on the supplied {@link Class} with
     * the supplied {@code name} and/or {@link Class type}. Searches all
     * superclasses up to {@link Object}.
     * 
     * @param clazz
     *            the class to introspect
     * @param orderByJsonPropertyIndex
     *            Whether to sort according to
     *            {@link com.fasterxml.jackson.annotation.JsonProperty#index}
     * @return
     */
    public static List<Field> findAllDeclaredFields(@NotNull Class<?> clazz, boolean orderByJsonPropertyIndex) {
        notNull(clazz, "Class must not be null");
        List<Field> allFields = new ArrayList<>(32);
        Class<?> searchType = clazz;
        while (Object.class != searchType && searchType != null) {
            Field[] fields = getDeclaredFields(searchType);
            for (Field f : fields) {
                allFields.add(f);
            }
            searchType = searchType.getSuperclass();
        }
        if (orderByJsonPropertyIndex) {
            Collections.sort(allFields, (f1, f2) -> {
                JsonProperty jp1 = ((Field) f1).getAnnotation(JsonProperty.class);
                JsonProperty jp2 = ((Field) f2).getAnnotation(JsonProperty.class);
                int order1 = nonNull(jp1) ? jp1.index() : Integer.MAX_VALUE;
                int order2 = nonNull(jp2) ? jp2.index() : Integer.MAX_VALUE;
                return order1 - order2;
            });
        }
        return allFields;
    }

    /**
     * Sets the field represented by the supplied {@link Field field object} on
     * the specified {@link Object target object} to the specified
     * {@code value}. In accordance with {@link Field#set(Object, Object)}
     * semantics, the new value is automatically unwrapped if the underlying
     * field has a primitive type.
     * <p>
     * Thrown exceptions are handled via a call to
     * {@link #handleReflectionException(Exception)}.
     * 
     * @param field
     *            the field to set
     * @param target
     *            the target object on which to set the field
     * @param value
     *            the value to set (may be {@code null})
     */
    public static void setField(Field field, Object target, Object value) {
        setField(field, target, value, false);
    }

    /**
     * Sets the field represented by the supplied {@link Field field object} on
     * the specified {@link Object target object} to the specified
     * {@code value}. In accordance with {@link Field#set(Object, Object)}
     * semantics, the new value is automatically unwrapped if the underlying
     * field has a primitive type.
     * <p>
     * Thrown exceptions are handled via a call to
     * {@link #handleReflectionException(Exception)}.
     * 
     * @param field
     *            the field to set
     * @param target
     *            the target object on which to set the field
     * @param value
     *            the value to set (may be {@code null})
     * @param forceAccessible
     *            force accessible
     */
    public static void setField(Field field, Object target, Object value, boolean forceAccessible) {
        try {
            if (forceAccessible && !field.isAccessible()) {
                makeAccessible(field);
            }
            field.set(target, value);
        } catch (IllegalAccessException ex) {
            handleReflectionException(ex);
            throw new IllegalStateException(
                    "Unexpected reflection exception - " + ex.getClass().getName() + ": " + ex.getMessage());
        }
    }

    /**
     * Get the field represented by the supplied {@link Field field object} on
     * the specified {@link Object target object}. In accordance with
     * {@link Field#get(Object)} semantics, the returned value is automatically
     * wrapped if the underlying field has a primitive type.
     * <p>
     * Thrown exceptions are handled via a call to
     * {@link #handleReflectionException(Exception)}.
     * 
     * @param field
     *            the field to get
     * @param target
     *            the target object from which to get the field
     * @return the field's current value
     */
    public static <T> T getField(Field field, Object target) {
        return getField(field, target, false);
    }

    /**
     * Get the field represented by the supplied {@link Field field object} on
     * the specified {@link Object target object}. In accordance with
     * {@link Field#get(Object)} semantics, the returned value is automatically
     * wrapped if the underlying field has a primitive type.
     * <p>
     * Thrown exceptions are handled via a call to
     * {@link #handleReflectionException(Exception)}.
     * 
     * @param field
     *            the field to get
     * @param target
     *            the target object from which to get the field
     * @param forceAccessible
     *            force accessible
     * @return the field's current value
     */
    @SuppressWarnings("unchecked")
    public static <T> T getField(Field field, Object target, boolean forceAccessible) {
        try {
            if (forceAccessible && !field.isAccessible()) {
                makeAccessible(field);
            }
            return (T) field.get(target);
        } catch (IllegalAccessException ex) {
            handleReflectionException(ex);
            throw new IllegalStateException(
                    "Unexpected reflection exception - " + ex.getClass().getName() + ": " + ex.getMessage());
        }
    }

    /**
     * Attempt to find a {@link Method} on the supplied class with the supplied
     * name and parameter types. Searches all superclasses up to {@code Object}.
     * <p>
     * Returns {@code null} if no {@link Method} can be found.
     * 
     * @param clazz
     *            the class to introspect
     * @param name
     *            the name of the method
     * @param paramTypes
     *            the parameter types of the method (may be {@code null} to
     *            indicate any signature)
     * @return the Method object, or {@code null} if none found
     */
    public static Method findMethodNullable(@Nullable Class<?> clazz, @NotNull String name, @Nullable Class<?>... paramTypes) {
        return nonNull(clazz) ? findMethod(clazz, name, paramTypes) : null;
    }

    /**
     * Attempt to find a {@link Method} on the supplied class with the supplied
     * name and no parameters. Searches all superclasses up to {@code Object}.
     * <p>
     * Returns {@code null} if no {@link Method} can be found.
     * 
     * @param clazz
     *            the class to introspect
     * @param name
     *            the name of the method
     * @return the Method object, or {@code null} if none found
     */
    public static Method findMethod(Class<?> clazz, String name) {
        return findMethod(clazz, name, new Class<?>[0]);
    }

    /**
     * Attempt to find a {@link Method} on the supplied class with the supplied
     * name and parameter types. Searches all superclasses up to {@code Object}.
     * <p>
     * Returns {@code null} if no {@link Method} can be found.
     * 
     * @param clazz
     *            the class to introspect
     * @param name
     *            the name of the method
     * @param paramTypes
     *            the parameter types of the method (may be {@code null} to
     *            indicate any signature)
     * @return the Method object, or {@code null} if none found
     */
    public static Method findMethod(Class<?> clazz, String name, Class<?>... paramTypes) {
        notNull(clazz, "Class must not be null");
        notNull(name, "Method name must not be null");
        Class<?> searchType = clazz;
        while (searchType != null) {
            Method[] methods = (searchType.isInterface() ? searchType.getMethods() : getDeclaredMethods(searchType));
            for (Method method : methods) {
                if (name.equals(method.getName())
                        && (isNull(paramTypes) || Arrays.equals(paramTypes, method.getParameterTypes()))) {
                    return method;
                }
            }
            searchType = searchType.getSuperclass();
        }
        return null;
    }

    /**
     * Invoke the specified {@link Method} against the supplied target object
     * with no arguments. The target object can be {@code null} when invoking a
     * static {@link Method}.
     * <p>
     * Thrown exceptions are handled via a call to
     * {@link #handleReflectionException}.
     * 
     * @param method
     *            the method to invoke
     * @param target
     *            the target object to invoke the method on
     * @return the invocation result, if any
     * @see #invokeMethod(java.lang.reflect.Method, Object, Object[])
     */
    public static Object invokeMethod(Method method, Object target) {
        return invokeMethod(method, target, new Object[0]);
    }

    /**
     * Invoke the specified {@link Method} against the supplied target object
     * with the supplied arguments. The target object can be {@code null} when
     * invoking a static {@link Method}.
     * <p>
     * Thrown exceptions are handled via a call to
     * {@link #handleReflectionException}.
     * 
     * @param method
     *            the method to invoke
     * @param target
     *            the target object to invoke the method on
     * @param args
     *            the invocation arguments (may be {@code null})
     * @return the invocation result, if any
     */
    public static Object invokeMethod(Method method, Object target, Object... args) {
        try {
            return method.invoke(target, args);
        } catch (Exception ex) {
            handleReflectionException(ex);
        }
        throw new IllegalStateException("Should never get here");
    }

    /**
     * Invoke the specified JDBC API {@link Method} against the supplied target
     * object with no arguments.
     * 
     * @param method
     *            the method to invoke
     * @param target
     *            the target object to invoke the method on
     * @return the invocation result, if any
     * @throws SQLException
     *             the JDBC API SQLException to rethrow (if any)
     * @see #invokeJdbcMethod(java.lang.reflect.Method, Object, Object[])
     */
    public static Object invokeJdbcMethod(Method method, Object target) throws SQLException {
        return invokeJdbcMethod(method, target, new Object[0]);
    }

    /**
     * Invoke the specified JDBC API {@link Method} against the supplied target
     * object with the supplied arguments.
     * 
     * @param method
     *            the method to invoke
     * @param target
     *            the target object to invoke the method on
     * @param args
     *            the invocation arguments (may be {@code null})
     * @return the invocation result, if any
     * @throws SQLException
     *             the JDBC API SQLException to rethrow (if any)
     * @see #invokeMethod(java.lang.reflect.Method, Object, Object[])
     */
    public static Object invokeJdbcMethod(Method method, Object target, Object... args) throws SQLException {
        try {
            return method.invoke(target, args);
        } catch (IllegalAccessException ex) {
            handleReflectionException(ex);
        } catch (InvocationTargetException ex) {
            if (ex.getTargetException() instanceof SQLException) {
                throw (SQLException) ex.getTargetException();
            }
            handleInvocationTargetException(ex);
        }
        throw new IllegalStateException("Should never get here");
    }

    /**
     * Handle the given reflection exception. Should only be called if no
     * checked exception is expected to be thrown by the target method.
     * <p>
     * Throws the underlying RuntimeException or Error in case of an
     * InvocationTargetException with such a root cause. Throws an
     * IllegalStateException with an appropriate message or
     * UndeclaredThrowableException otherwise.
     * 
     * @param ex
     *            the reflection exception to handle
     */
    public static void handleReflectionException(Exception ex) {
        if (ex instanceof NoSuchMethodException) {
            throw new IllegalStateException("Method not found: " + ex.getMessage());
        }
        if (ex instanceof IllegalAccessException) {
            throw new IllegalStateException("Could not access method: " + ex.getMessage());
        }
        if (ex instanceof InvocationTargetException) {
            handleInvocationTargetException((InvocationTargetException) ex);
        }
        if (ex instanceof RuntimeException) {
            throw (RuntimeException) ex;
        }
        throw new UndeclaredThrowableException(ex);
    }

    /**
     * Handle the given invocation target exception. Should only be called if no
     * checked exception is expected to be thrown by the target method.
     * <p>
     * Throws the underlying RuntimeException or Error in case of such a root
     * cause. Throws an UndeclaredThrowableException otherwise.
     * 
     * @param ex
     *            the invocation target exception to handle
     */
    public static void handleInvocationTargetException(InvocationTargetException ex) {
        rethrowRuntimeException(ex.getTargetException());
    }

    /**
     * Rethrow the given {@link Throwable exception}, which is presumably the
     * <em>target exception</em> of an {@link InvocationTargetException}. Should
     * only be called if no checked exception is expected to be thrown by the
     * target method.
     * <p>
     * Rethrows the underlying exception cast to a {@link RuntimeException} or
     * {@link Error} if appropriate; otherwise, throws an
     * {@link UndeclaredThrowableException}.
     * 
     * @param ex
     *            the exception to rethrow
     * @throws RuntimeException
     *             the rethrown exception
     */
    public static void rethrowRuntimeException(Throwable ex) {
        if (ex instanceof RuntimeException) {
            throw (RuntimeException) ex;
        }
        if (ex instanceof Error) {
            throw (Error) ex;
        }
        throw new UndeclaredThrowableException(ex);
    }

    /**
     * Rethrow the given {@link Throwable exception}, which is presumably the
     * <em>target exception</em> of an {@link InvocationTargetException}. Should
     * only be called if no checked exception is expected to be thrown by the
     * target method.
     * <p>
     * Rethrows the underlying exception cast to an {@link Exception} or
     * {@link Error} if appropriate; otherwise, throws an
     * {@link UndeclaredThrowableException}.
     * 
     * @param ex
     *            the exception to rethrow
     * @throws Exception
     *             the rethrown exception (in case of a checked exception)
     */
    public static void rethrowException(Throwable ex) throws Exception {
        if (ex instanceof Exception) {
            throw (Exception) ex;
        }
        if (ex instanceof Error) {
            throw (Error) ex;
        }
        throw new UndeclaredThrowableException(ex);
    }

    /**
     * Determine whether the given method explicitly declares the given
     * exception or one of its superclasses, which means that an exception of
     * that type can be propagated as-is within a reflective invocation.
     * 
     * @param method
     *            the declaring method
     * @param exceptionType
     *            the exception to throw
     * @return {@code true} if the exception can be thrown as-is; {@code false}
     *         if it needs to be wrapped
     */
    public static boolean declaresException(Method method, Class<?> exceptionType) {
        notNull(method, "Method must not be null");
        Class<?>[] declaredExceptions = method.getExceptionTypes();
        for (Class<?> declaredException : declaredExceptions) {
            if (declaredException.isAssignableFrom(exceptionType)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Determine whether the given field is a "public static final" constant.
     * 
     * @param field
     *            the field to check
     */
    public static boolean isPublicStaticFinal(Field field) {
        int modifiers = field.getModifiers();
        return (Modifier.isPublic(modifiers) && Modifier.isStatic(modifiers) && Modifier.isFinal(modifiers));
    }

    /**
     * Determine whether the given method is an "equals" method.
     * 
     * @see java.lang.Object#equals(Object)
     */
    public static boolean isEqualsMethod(Method method) {
        if (method == null || !method.getName().equals("equals")) {
            return false;
        }
        Class<?>[] paramTypes = method.getParameterTypes();
        return (paramTypes.length == 1 && paramTypes[0] == Object.class);
    }

    /**
     * Determine whether the given method is a "hashCode" method.
     * 
     * @see java.lang.Object#hashCode()
     */
    public static boolean isHashCodeMethod(Method method) {
        return (method != null && method.getName().equals("hashCode") && method.getParameterTypes().length == 0);
    }

    /**
     * Determine whether the given method is a "toString" method.
     * 
     * @see java.lang.Object#toString()
     */
    public static boolean isToStringMethod(Method method) {
        return (method != null && method.getName().equals("toString") && method.getParameterTypes().length == 0);
    }

    /**
     * Determine whether the given method is originally declared by
     * {@link java.lang.Object}.
     */
    public static boolean isObjectMethod(Method method) {
        if (method == null) {
            return false;
        }
        try {
            Object.class.getDeclaredMethod(method.getName(), method.getParameterTypes());
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * Determine whether the given method is a CGLIB 'renamed' method, following
     * the pattern "CGLIB$methodName$0".
     * 
     * @param renamedMethod
     *            the method to check
     * @see org.springframework.cglib.proxy.Enhancer#rename
     */
    public static boolean isCglibRenamedMethod(Method renamedMethod) {
        String name = renamedMethod.getName();
        if (name.startsWith(CGLIB_RENAMED_METHOD_PREFIX)) {
            int i = name.length() - 1;
            while (i >= 0 && Character.isDigit(name.charAt(i))) {
                i--;
            }
            return ((i > CGLIB_RENAMED_METHOD_PREFIX.length()) && (i < name.length() - 1) && name.charAt(i) == '$');
        }
        return false;
    }

    /**
     * Make the given field accessible, explicitly setting it accessible if
     * necessary. The {@code setAccessible(true)} method is only called when
     * actually necessary, to avoid unnecessary conflicts with a JVM
     * SecurityManager (if active).
     * 
     * @param field
     *            the field to make accessible
     * @see java.lang.reflect.Field#setAccessible
     */
    public static void makeAccessible(Field field) {
        if ((!Modifier.isPublic(field.getModifiers()) || !Modifier.isPublic(field.getDeclaringClass().getModifiers())
                || Modifier.isFinal(field.getModifiers())) && !field.isAccessible()) {
            field.setAccessible(true);
        }
    }

    /**
     * Make the given method accessible, explicitly setting it accessible if
     * necessary. The {@code setAccessible(true)} method is only called when
     * actually necessary, to avoid unnecessary conflicts with a JVM
     * SecurityManager (if active).
     * 
     * @param method
     *            the method to make accessible
     * @see java.lang.reflect.Method#setAccessible
     */
    public static void makeAccessible(Method method) {
        if ((!isPublic(method.getModifiers()) || !isPublic(method.getDeclaringClass().getModifiers()))
                && !method.isAccessible()) {
            method.setAccessible(true);
        }
    }

    /**
     * Make the given constructor accessible, explicitly setting it accessible
     * if necessary. The {@code setAccessible(true)} method is only called when
     * actually necessary, to avoid unnecessary conflicts with a JVM
     * SecurityManager (if active).
     * 
     * @param ctor
     *            the constructor to make accessible
     * @see java.lang.reflect.Constructor#setAccessible
     */
    public static void makeAccessible(Constructor<?> ctor) {
        if ((!isPublic(ctor.getModifiers()) || !isPublic(ctor.getDeclaringClass().getModifiers())) && !ctor.isAccessible()) {
            ctor.setAccessible(true);
        }
    }

    /**
     * Perform the given callback operation on all matching methods of the given
     * class, as locally declared or equivalent thereof (such as default methods
     * on Java 8 based interfaces that the given class implements).
     * 
     * @param clazz
     *            the class to introspect
     * @param mc
     *            the callback to invoke for each method
     * @since 4.2
     * @see #doWithMethods
     */
    public static void doWithLocalMethods(Class<?> clazz, MethodCallback mc) {
        Method[] methods = getDeclaredMethods(clazz);
        for (Method method : methods) {
            try {
                mc.doWith(method);
            } catch (IllegalAccessException ex) {
                throw new IllegalStateException("Not allowed to access method '" + method.getName() + "': " + ex);
            }
        }
    }

    /**
     * Perform the given callback operation on all matching methods of the given
     * class and superclasses.
     * <p>
     * The same named method occurring on subclass and superclass will appear
     * twice, unless excluded by a {@link MethodFilter}.
     * 
     * @param clazz
     *            the class to introspect
     * @param mc
     *            the callback to invoke for each method
     * @see #doWithMethods(Class, MethodCallback, MethodFilter)
     */
    public static void doWithMethods(Class<?> clazz, MethodCallback mc) {
        doWithMethods(clazz, mc, null);
    }

    /**
     * Perform the given callback operation on all matching methods of the given
     * class and superclasses (or given interface and super-interfaces).
     * <p>
     * The same named method occurring on subclass and superclass will appear
     * twice, unless excluded by the specified {@link MethodFilter}.
     * 
     * @param clazz
     *            the class to introspect
     * @param mc
     *            the callback to invoke for each method
     * @param mf
     *            the filter that determines the methods to apply the callback
     *            to
     */
    public static void doWithMethods(Class<?> clazz, MethodCallback mc, MethodFilter mf) {
        // Keep backing up the inheritance hierarchy.
        Method[] methods = getDeclaredMethods(clazz);
        for (Method method : methods) {
            if (mf != null && !mf.matches(method)) {
                continue;
            }
            try {
                mc.doWith(method);
            } catch (IllegalAccessException ex) {
                throw new IllegalStateException("Not allowed to access method '" + method.getName() + "': " + ex);
            }
        }
        if (clazz.getSuperclass() != null) {
            doWithMethods(clazz.getSuperclass(), mc, mf);
        } else if (clazz.isInterface()) {
            for (Class<?> superIfc : clazz.getInterfaces()) {
                doWithMethods(superIfc, mc, mf);
            }
        }
    }

    /**
     * Get all declared methods on the leaf class and all superclasses. Leaf
     * class methods are included first.
     * 
     * @param leafClass
     *            the class to introspect
     */
    public static Method[] getAllDeclaredMethods(Class<?> leafClass) {
        final List<Method> methods = new ArrayList<Method>(32);
        doWithMethods(leafClass, new MethodCallback() {
            @Override
            public void doWith(Method method) {
                methods.add(method);
            }
        });
        return methods.toArray(new Method[methods.size()]);
    }

    /**
     * Get the unique set of declared methods on the leaf class and all
     * superclasses. Leaf class methods are included first and while traversing
     * the superclass hierarchy any methods found with signatures matching a
     * method already included are filtered out.
     * 
     * @param leafClass
     *            the class to introspect
     */
    public static Method[] getUniqueDeclaredMethods(Class<?> leafClass) {
        final List<Method> methods = new ArrayList<Method>(32);
        doWithMethods(leafClass, new MethodCallback() {
            @Override
            public void doWith(Method method) {
                boolean knownSignature = false;
                Method methodBeingOverriddenWithCovariantReturnType = null;
                for (Method existingMethod : methods) {
                    if (method.getName().equals(existingMethod.getName())
                            && Arrays.equals(method.getParameterTypes(), existingMethod.getParameterTypes())) {
                        // Is this a covariant return type situation?
                        if (existingMethod.getReturnType() != method.getReturnType()
                                && existingMethod.getReturnType().isAssignableFrom(method.getReturnType())) {
                            methodBeingOverriddenWithCovariantReturnType = existingMethod;
                        } else {
                            knownSignature = true;
                        }
                        break;
                    }
                }
                if (methodBeingOverriddenWithCovariantReturnType != null) {
                    methods.remove(methodBeingOverriddenWithCovariantReturnType);
                }
                if (!knownSignature && !isCglibRenamedMethod(method)) {
                    methods.add(method);
                }
            }
        });
        return methods.toArray(new Method[methods.size()]);
    }

    /**
     * This variant retrieves {@link Class#getDeclaredMethods()} from a local
     * cache in order to avoid the JVM's SecurityManager check and defensive
     * array copying. In addition, it also includes Java 8 default methods from
     * locally implemented interfaces, since those are effectively to be treated
     * just like declared methods.
     * 
     * @param clazz
     *            the class to introspect
     * @return the cached array of methods
     * @see Class#getDeclaredMethods()
     */
    private static Method[] getDeclaredMethods(Class<?> clazz) {
        notNull(clazz, "Class must not be null");
        Method[] result = declaredMethodsCache.get(clazz);
        if (result == null) {
            Method[] declaredMethods = clazz.getDeclaredMethods();
            List<Method> defaultMethods = findConcreteMethodsOnInterfaces(clazz);
            if (defaultMethods != null) {
                result = new Method[declaredMethods.length + defaultMethods.size()];
                System.arraycopy(declaredMethods, 0, result, 0, declaredMethods.length);
                int index = declaredMethods.length;
                for (Method defaultMethod : defaultMethods) {
                    result[index] = defaultMethod;
                    index++;
                }
            } else {
                result = declaredMethods;
            }
            declaredMethodsCache.put(clazz, (result.length == 0 ? NO_METHODS : result));
        }
        return result;
    }

    private static List<Method> findConcreteMethodsOnInterfaces(Class<?> clazz) {
        List<Method> result = null;
        for (Class<?> ifc : clazz.getInterfaces()) {
            for (Method ifcMethod : ifc.getMethods()) {
                if (!isAbstract(ifcMethod.getModifiers())) {
                    if (result == null) {
                        result = new LinkedList<Method>();
                    }
                    result.add(ifcMethod);
                }
            }
        }
        return result;
    }

    /**
     * Invoke the given callback on all locally declared fields in the given
     * class.
     * 
     * @param clazz
     *            the target class to analyze
     * @param fc
     *            the callback to invoke for each field
     * @since 4.2
     * @see #doWithFields
     */
    public static void doWithLocalFields(Class<?> clazz, FieldCallback fc) {
        for (Field field : getDeclaredFields(clazz)) {
            try {
                fc.doWith(field);
            } catch (IllegalAccessException ex) {
                throw new IllegalStateException("Not allowed to access field '" + field.getName() + "': " + ex);
            }
        }
    }

    /**
     * Invoke the given callback on all fields in the target class, going up the
     * class hierarchy to get all declared fields.
     * 
     * @param clazz
     *            the target class to analyze
     * @param fc
     *            the callback to invoke for each field
     */
    public static void doWithFields(Class<?> clazz, FieldCallback fc) {
        doWithFields(clazz, fc, null);
    }

    /**
     * Invoke the given callback on all fields in the target class, going up the
     * class hierarchy to get all declared fields.
     * 
     * @param clazz
     *            the target class to analyze
     * @param fc
     *            the callback to invoke for each field
     * @param ff
     *            the filter that determines the fields to apply the callback to
     */
    public static void doWithFields(Class<?> clazz, FieldCallback fc, FieldFilter ff) {
        // Keep backing up the inheritance hierarchy.
        Class<?> targetClass = clazz;
        do {
            Field[] fields = getDeclaredFields(targetClass);
            for (Field field : fields) {
                if (ff != null && !ff.matches(field)) {
                    continue;
                }
                try {
                    fc.doWith(field);
                } catch (IllegalAccessException ex) {
                    throw new IllegalStateException("Not allowed to access field '" + field.getName() + "': " + ex);
                }
            }
            targetClass = targetClass.getSuperclass();
        } while (targetClass != null && targetClass != Object.class);
    }

    /**
     * This variant retrieves {@link Class#getDeclaredFields()} from a local
     * cache in order to avoid the JVM's SecurityManager check and defensive
     * array copying.
     * 
     * @param clazz
     *            the class to introspect
     * @return the cached array of fields
     * @see Class#getDeclaredFields()
     */
    public static Field[] getDeclaredFields(Class<?> clazz) {
        notNull(clazz, "Class must not be null");
        Field[] result = declaredFieldsCache.get(clazz);
        if (result == null) {
            result = clazz.getDeclaredFields();
            declaredFieldsCache.put(clazz, (result.length == 0 ? NO_FIELDS : result));
        }
        return result;
    }

    /**
     * Given the source object and the destination, which must be the same class
     * or a subclass, copy all fields, including inherited fields. Designed to
     * work on objects with public no-arg constructors.
     */
    public static void shallowCopyFieldState(final Object src, final Object dest) {
        notNull(src, "Source for field copy cannot be null");
        notNull(dest, "Destination for field copy cannot be null");
        if (!src.getClass().isAssignableFrom(dest.getClass())) {
            throw new IllegalArgumentException("Destination class [" + dest.getClass().getName()
                    + "] must be same or subclass as source class [" + src.getClass().getName() + "]");
        }
        doWithFields(src.getClass(), new FieldCallback() {
            @Override
            public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
                makeAccessible(field);
                Object srcValue = field.get(src);
                field.set(dest, srcValue);
            }
        }, COPYABLE_FIELDS);
    }

    /**
     * Clear the internal method/field cache.
     * 
     * @since 4.2.4
     */
    public static void clearCache() {
        declaredMethodsCache.clear();
        declaredFieldsCache.clear();
    }

    /**
     * Action to take on each method.
     */
    public interface MethodCallback {

        /**
         * Perform an operation using the given method.
         * 
         * @param method
         *            the method to operate on
         */
        void doWith(Method method) throws IllegalArgumentException, IllegalAccessException;
    }

    /**
     * Callback optionally used to filter methods to be operated on by a method
     * callback.
     */
    public interface MethodFilter {

        /**
         * Determine whether the given method matches.
         * 
         * @param method
         *            the method to check
         */
        boolean matches(Method method);
    }

    /**
     * Callback interface invoked on each field in the hierarchy.
     */
    public interface FieldCallback {

        /**
         * Perform an operation using the given field.
         * 
         * @param field
         *            the field to operate on
         */
        void doWith(Field field) throws IllegalArgumentException, IllegalAccessException;
    }

    /**
     * Callback interface invoked on each field in the hierarchy.
     */
    public static interface FieldHandler {

        /**
         * Perform an operation using the given field.
         * 
         * @param field
         *            The field to operate on
         * @param objOfField
         *            Value of class field.
         */
        void doWith(final Field field, final Object objOfField) throws IllegalArgumentException, IllegalAccessException;
    }

    /**
     * Callback optionally used to filter fields to be operated on by a field
     * callback.
     */
    public static interface FieldFilter {

        /**
         * Determine whether the given field matches.
         * 
         * @param field
         *            the field to check
         */
        boolean matches(Field field);

        /**
         * It is used to control whether to continue to reflect the structure of
         * the field recursively if the field traversed by reflection is of
         * object type.
         * 
         * @param field
         * @return
         */
        default boolean describeForObjField(Field field) {
            return true;
        }

    }

    /**
     * Pre-built FieldFilter that matches all non-static, non-final fields.
     */
    public static final FieldFilter COPYABLE_FIELDS = new FieldFilter() {

        @Override
        public boolean matches(Field field) {
            return !(isStatic(field.getModifiers()) || isFinal(field.getModifiers()));
        }
    };

    /**
     * Pre-built MethodFilter that matches all non-bridge methods.
     */
    public static final MethodFilter NON_BRIDGED_METHODS = new MethodFilter() {

        @Override
        public boolean matches(Method method) {
            return !method.isBridge();
        }
    };

    /**
     * Pre-built MethodFilter that matches all non-bridge methods which are not
     * declared on {@code java.lang.Object}.
     */
    public static final MethodFilter USER_DECLARED_METHODS = new MethodFilter() {

        @Override
        public boolean matches(Method method) {
            return (!method.isBridge() && method.getDeclaringClass() != Object.class);
        }
    };

    /**
     * Naming prefix for CGLIB-renamed methods.
     * 
     * @see #isCglibRenamedMethod
     */
    private static final String CGLIB_RENAMED_METHOD_PREFIX = "CGLIB$";
    private static final Method[] NO_METHODS = {};
    private static final Field[] NO_FIELDS = {};

    /**
     * Cache for {@link Class#getDeclaredMethods()} plus equivalent default
     * methods from Java 8 based interfaces, allowing for fast iteration.
     */
    private static final Map<Class<?>, Method[]> declaredMethodsCache;

    /**
     * Cache for {@link Class#getDeclaredFields()}, allowing for fast iteration.
     */
    private static final Map<Class<?>, Field[]> declaredFieldsCache;

    static {
        declaredMethodsCache = new ConcurrentReferenceHashMap<Class<?>, Method[]>(256);
        declaredFieldsCache = new ConcurrentReferenceHashMap<Class<?>, Field[]>(256);
    }

}