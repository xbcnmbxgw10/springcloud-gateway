/*
 * Copyright (C) 2017 ~ 2025 the original author or authors.
 * <springcloudgateway@gmail.com> Technology CO.LTD.
 * All rights reserved.
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
 * 
 * Reference to website: http://springcloud.gateway.com
 */
package org.springcloud.gateway.core.bridge;

import static org.springcloud.gateway.core.core.ReflectionUtils2.findFieldNullable;
import static org.springcloud.gateway.core.core.ReflectionUtils2.findMethodNullable;
import static org.springcloud.gateway.core.core.ReflectionUtils2.getField;
import static org.springcloud.gateway.core.core.ReflectionUtils2.invokeMethod;
import static org.springcloud.gateway.core.core.ReflectionUtils2.makeAccessible;
import static org.springcloud.gateway.core.lang.ClassUtils2.resolveClassNameNullable;
import static java.util.Objects.nonNull;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * {@link RpcContextProviderInterceptorBridges}
 * 
 * @author springcloudgateway &lt;springcloudgateway@gmail.com&gt;
 * @version v1.0.0
 * @sine v1.0
 * @see https://blog.csdn.net/zoinsung_lee/article/details/82529624
 */
public abstract class RpcContextProviderInterceptorBridges {

    public static int invokeFieldOrder() {
        if (nonNull(ORDER_FIELD)) {
            return (int) getField(ORDER_FIELD, null, true);
        }
        return 0;
    }

    public static boolean invokeCheckSupportTypeProxy(Object target, Class<?> actualOriginalTargetClass) {
        if (nonNull(checkSupportTypeProxyMethod)) {
            makeAccessible(checkSupportTypeProxyMethod);
            return (boolean) invokeMethod(checkSupportTypeProxyMethod, null, new Object[] { target, actualOriginalTargetClass });
        }
        return false;
    }

    public static boolean invokeCheckSupportMethodProxy(Object target, Method method, Class<?> actualOriginalTargetClass,
            Object... args) {
        if (nonNull(checkSupportMethodProxyMethod)) {
            makeAccessible(checkSupportMethodProxyMethod);
            return (boolean) invokeMethod(checkSupportMethodProxyMethod, null,
                    new Object[] { target, method, actualOriginalTargetClass, args });
        }
        return false;
    }

    public static boolean hasFeignRpcContextProcessorClass() {
        return nonNull(rpcContextProviderProxyInterceptorClass);
    }

    public static final String rpcContextProviderProxyInterceptorClassName = "org.springcloudgatewayinxcnm.integration.feign.core.context.interceptor.RpcContextProviderProxyInterceptor";
    private static final Class<?> rpcContextProviderProxyInterceptorClass = resolveClassNameNullable(
            rpcContextProviderProxyInterceptorClassName);

    private static final Method checkSupportTypeProxyMethod = findMethodNullable(rpcContextProviderProxyInterceptorClass,
            "checkSupportTypeProxy", Object.class, Class.class);
    private static final Method checkSupportMethodProxyMethod = findMethodNullable(rpcContextProviderProxyInterceptorClass,
            "checkSupportMethodProxy", Object.class, Method.class, Class.class, Object[].class);

    private static final Field ORDER_FIELD = findFieldNullable(rpcContextProviderProxyInterceptorClass, "ORDER", int.class);

}
