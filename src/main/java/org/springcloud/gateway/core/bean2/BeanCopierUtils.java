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
package org.springcloud.gateway.core.bean2;

import static java.lang.String.format;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.springframework.cglib.beans.BeanCopier.create;
import static org.springframework.util.Assert.notNull;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.validation.constraints.NotNull;

import org.springframework.cglib.beans.BeanCopier;
import org.springframework.cglib.core.Converter;
import org.springframework.objenesis.Objenesis;
import org.springframework.objenesis.ObjenesisStd;

/**
 * {@link BeanCopierUtils}
 * 
 * @author springcloudgateway &lt;springcloudgateway@gmail.com&gt;
 * @version v1.0.0
 * @see
 */
public abstract class BeanCopierUtils {

    /***
     * Copy bean object the properties of new objects. Note: only when there is
     * a corresponding setter method can be copied.
     * 
     * @param src
     *            source object
     * @param <O>
     * @param <T>
     * @return return target dst object.
     */
    @SuppressWarnings("unchecked")
    public static @NotNull <O> O clone(@NotNull O src) {
        notNull(src, "Mapper bean source object is required");
        checkSupport(src, null);
        try {
            O newObj = (O) objenesis.newInstance(src.getClass());
            return (O) mapper(src, newObj, null);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /***
     * Copy the properties of class objects. Note: only when the properties are
     * the same and there is a corresponding setter method can you copy them
     * 
     * @param src
     *            source object
     * @param dst
     *            target new object
     * @param <O>
     * @param <T>
     * @return return target dst object.
     */
    public static @NotNull <O, T> T mapper(@NotNull O src, @NotNull T dst) {
        return mapper(src, dst, null);
    }

    /***
     * Copy the properties of class objects. Note: only when the properties are
     * the same and there is a corresponding setter method can you copy them
     * 
     * @param src
     *            source object
     * @param dst
     *            target new object
     * @param converter
     *            copier fields converter
     * @param <O>
     * @param <T>
     * @return return target dst object.
     */
    public static @NotNull <O, T> T mapper(@NotNull O src, @NotNull T dst, @NotNull Converter converter) {
        return doBeanMapper(src, dst, converter);
    }

    /**
     * Do bean copying fields mapper
     * 
     * @param src
     * @param dst
     * @param converter
     * @return
     */
    private static @NotNull <O, T> T doBeanMapper(@NotNull O src, @NotNull T dst, @NotNull Converter converter) {
        notNull(src, "Mapper bean source object is required");
        notNull(dst, "Mapper bean target object is required");
        checkSupport(src, dst);

        // Gets cache key.
        String baseKey = generateKey(src.getClass(), dst.getClass());

        // Gets BeanCopier
        BeanCopier copier = mapCaches.get(baseKey);
        if (isNull(copier)) {
            mapCaches.put(baseKey, (copier = create(src.getClass(), dst.getClass(), nonNull(converter))));
        }

        copier.copy(src, dst, converter);
        return dst;
    }

    /**
     * Gets generate Key
     * 
     * @param src
     * @param dst
     * @return
     */
    private static String generateKey(Class<?> src, Class<?> dst) {
        return src.toString().concat("-").concat(dst.toString());
    }

    /**
     * Check is supported input type of src and dest.
     * 
     * @param src
     * @param dst
     */
    private static void checkSupport(Object src, Object dst) {
        if (!isNull(src)) {
            Class<?> clazz = src.getClass();
            if (Collection.class.isAssignableFrom(clazz) || Map.class.isAssignableFrom(clazz)) {
                throw new IllegalArgumentException(format("Not supported copier input type source: %s", clazz));
            }
        }
        if (!isNull(dst)) {
            Class<?> clazz = dst.getClass();
            if (Collection.class.isAssignableFrom(clazz) || Map.class.isAssignableFrom(clazz)) {
                throw new IllegalArgumentException(format("Not supported copier input type target: %s", clazz));
            }
        }
    }

    /**
     * Use cache to improve efficiency
     */
    private static final Map<String, BeanCopier> mapCaches = new ConcurrentHashMap<>(64);

    /**
     * Object instantiator
     */
    private static final Objenesis objenesis = new ObjenesisStd(true);

}