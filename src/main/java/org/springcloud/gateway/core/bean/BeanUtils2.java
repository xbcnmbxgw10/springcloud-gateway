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
package org.springcloud.gateway.core.bean;

import static org.springcloud.gateway.core.core.ReflectionUtils2.findField;
import static org.springcloud.gateway.core.core.ReflectionUtils2.isCompatibleType;
import static org.springcloud.gateway.core.core.ReflectionUtils2.isGenericModifier;
import static org.springcloud.gateway.core.core.ReflectionUtils2.makeAccessible;
import static org.springcloud.gateway.core.core.TypeUtils2.isSimpleCollectionType;
import static org.springcloud.gateway.core.core.TypeUtils2.isSimpleType;
import static org.springcloud.gateway.core.lang.ClassUtils2.anyTypeOf;
import static java.lang.String.format;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.startsWithAny;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.springcloud.gateway.core.core.ReflectionUtils2.FieldFilter;

/**
 * Enhanced static convenience methods for JavaBeans: for instantiating beans,
 * checking bean property types, copying bean properties, etc. </br>
 * Enhanced for: {@link org.springframework.beans.BeanUtils}
 * 
 * @author springcloudgateway <springcloudgateway@gmail.com>
 * @version v1.0.0
 * @since
 */
public abstract class BeanUtils2 {

    /**
     * Calls the given callback on all fields of the target class, recursively
     * running the class hierarchy up to copy all declared fields.</br>
     * It will contain all the fields defined by all parent or super classes. At
     * the same time, the target and the source object must be compatible.
     * 
     * @param target
     *            The target object to copy to
     * @param src
     *            Source object
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     */
    public static void deepCopyFieldState(@NotNull Object target, @NotNull Object src)
            throws IllegalArgumentException, IllegalAccessException {
        deepCopyFieldState(target, src, DEFAULT_FIELD_FILTER, DEFAULT_FIELD_COPYER);
    }

    /**
     * Calls the given callback on all fields of the target class, recursively
     * running the class hierarchy up to copy all declared fields.</br>
     * It will contain all the fields defined by all parent or super classes. At
     * the same time, the target and the source object must be compatible.
     * 
     * @param target
     *            The target object to copy to
     * @param src
     *            Source object
     * @param ff
     *            Field filter
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     */
    public static void deepCopyFieldState(@NotNull Object target, @NotNull Object src, @NotNull FieldFilter ff)
            throws IllegalArgumentException, IllegalAccessException {
        deepCopyFieldState(target, src, ff, DEFAULT_FIELD_COPYER);
    }

    /**
     * Calls the given callback on all fields of the target class, recursively
     * running the class hierarchy up to copy all declared fields.</br>
     * It will contain all the fields defined by all parent or super classes. At
     * the same time, the target and the source object must be compatible.
     *
     * @param target
     *            The target object to copy to
     * @param src
     *            Source object
     * @param fc
     *            Field copyer
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     */
    public static void deepCopyFieldState(@NotNull Object target, @NotNull Object src, @NotNull FieldProcessor fc)
            throws IllegalArgumentException, IllegalAccessException {
        deepCopyFieldState(target, src, DEFAULT_FIELD_FILTER, fc);
    }

    /**
     * Calls the given callback on all fields of the target class, recursively
     * running the class hierarchy up to copy all declared fields.</br>
     * It will contain all the fields defined by all parent or super classes. At
     * the same time, the target and the source object must be compatible.
     * 
     * @param target
     *            The target object to copy to
     * @param src
     *            Source object
     * @param ff
     *            Field filter
     * @param fp
     *            Customizable copyer
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     */
    public static void deepCopyFieldState(
            @NotNull Object target,
            @NotNull Object src,
            @NotNull FieldFilter ff,
            @NotNull FieldProcessor fp) throws IllegalArgumentException, IllegalAccessException {
        if (!(target != null && src != null && ff != null && fp != null)) {
            throw new IllegalArgumentException("Target and source FieldFilter and FieldProcessor must not be null");
        }

        // Check if the target is compatible with the source object
        Class<?> targetClass = target.getClass(), sourceClass = src.getClass();
        if (!isCompatibleType(target.getClass(), src.getClass())) {
            throw new IllegalArgumentException(
                    format("Incompatible the objects, target class: %s, source class: %s", targetClass, sourceClass));
        }

        Class<?> targetCls = target.getClass(); // [MARK0]
        do {
            doDeepCopyFields(targetCls, target, src, ff, fp);
        } while ((targetCls = targetCls.getSuperclass()) != Object.class);
    }

    /**
     * Calls the given callback on all fields of the target class, recursively
     * running the class hierarchy up to copy all declared fields.</br>
     * Note: that it does not contain fields defined by the parent or super
     * class. At the same time, the target and the source object must be
     * compatible.</br>
     * Note: Attribute fields of parent and superclass are not included
     * 
     * @param currentTargetClass
     *            The level of the class currently copied to (upward recursion)
     * @param target
     *            The target object to copy to
     * @param src
     *            Source object
     * @param ff
     *            Field filter
     * @param fp
     *            Customizable copyer
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     */
    private static void doDeepCopyFields(
            Class<?> currentTargetClass,
            @NotNull Object target,
            @NotNull Object src,
            @NotNull FieldFilter ff,
            @NotNull FieldProcessor fp) throws IllegalArgumentException, IllegalAccessException {

        if (isNull(currentTargetClass) || isNull(ff) || isNull(fp)) {
            throw new IllegalArgumentException(
                    "Hierarchy current target class or source FieldFilter and FieldProcessor can't null");
        }
        // Skip the current level copy.
        if (isNull(src) || isNull(target)) {
            return;
        }

        // Recursive traversal matching and processing
        Class<?> sourceClass = src.getClass();
        for (Field tf : currentTargetClass.getDeclaredFields()) {
            // Must be filtered over.
            // [BUGFIX]: for example when recursively getting
            // java.nio.charset.Charset, there will be an infinite loop stack
            // overflow (jvm8 defaults to 1024)
            if (Modifier.isFinal(tf.getModifiers())
                    || startsWithAny(tf.getDeclaringClass().getName(), "java.nio", "java.util", "org.apache.commons.lang",
                            "org.springframework.util", "org.springframework.web.util", "org.springframework.boot.util")) {
                continue;
            }

            makeAccessible(tf);
            Object targetPropertyValue = tf.get(target); // See:[MARK0]
            Object sourcePropertyValue = null;
            Field sf = findField(sourceClass, tf.getName());
            if (nonNull(sf)) {
                makeAccessible(sf);
                sourcePropertyValue = sf.get(src);
            }

            // Base type or collection type or enum?
            if (isSimpleType(tf.getType()) || isSimpleCollectionType(tf.getType()) || tf.getType().isEnum()) {
                // [MARK2] Filter matching property
                if (nonNull(fp) && ff.matches(tf)) {
                    fp.doProcess(target, tf, sf, sourcePropertyValue);
                }
            } else {
                doDeepCopyFields(tf.getType(), targetPropertyValue, sourcePropertyValue, ff, fp);
            }
        }
    }

    /**
     * Enhanced callback interface invoked on each field in the hierarchy.
     */
    public static interface FieldProcessor {

        /**
         * Use the given field processing(for example: copying).
         * 
         * @param target
         * @param tf
         * @param sf
         * @param sourcePropertyValue
         *            The value of the attributes of the source bean
         * @throws IllegalAccessException
         * @throws IllegalArgumentException
         */
        void doProcess(@NotNull Object target, @NotNull Field tf, @NotNull Field sf, @Nullable Object sourcePropertyValue)
                throws IllegalArgumentException, IllegalAccessException;
    }

    /**
     * Default field filter of {@link FieldFilter}.
     * @see:{@link org.springcloud.gateway.core.core.ReflectionUtils2#isGenericAccessibleModifier(int)}
     */
    public static final FieldFilter DEFAULT_FIELD_FILTER = targetField -> isGenericModifier(targetField.getModifiers());

    /**
     * Default COPYER of {@link FieldProcessor}.
     */
    public static final FieldProcessor DEFAULT_FIELD_COPYER = (target, tf, sf, sourcePropertyValue) -> {
        if (nonNull(sourcePropertyValue)) {
            tf.setAccessible(true);
            tf.set(target, sourcePropertyValue);
        }
    };

    /**
     * Simple merging COPYER of {@link FieldProcessor}.
     */
    public static final FieldProcessor SIMPLE_MERGE_FIELD_COPYER = (target, tf, sf, sourcePropertyValue) -> {
        if (nonNull(sourcePropertyValue)) {
            tf.setAccessible(true);
            Object targetPropertyValue = tf.get(target);
            // Check if the conditions for replication are met.
            if (isNull(targetPropertyValue)) {
                tf.set(target, sourcePropertyValue);
            } else if (anyTypeOf(targetPropertyValue.getClass(), int.class, Integer.class)
                    && ((Integer) targetPropertyValue) <= 0) {
                tf.set(target, sourcePropertyValue);
            } else if (anyTypeOf(targetPropertyValue.getClass(), float.class, Float.class)
                    && ((Float) targetPropertyValue) <= 0) {
                tf.set(target, sourcePropertyValue);
            } else if (anyTypeOf(targetPropertyValue.getClass(), double.class, Double.class)
                    && ((Double) targetPropertyValue) <= 0) {
                tf.set(target, sourcePropertyValue);
            } else if (anyTypeOf(targetPropertyValue.getClass(), short.class, Short.class)
                    && ((Short) targetPropertyValue) <= 0) {
                tf.set(target, sourcePropertyValue);
            } else { // String/custom Type/...
                // Ignore
            }
        }
    };

}