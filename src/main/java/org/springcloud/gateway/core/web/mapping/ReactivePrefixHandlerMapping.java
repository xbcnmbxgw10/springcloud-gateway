/*
 * Copyright 2017 ~ 2025 the original author or authors. <springcloudgateway@gmail.com, >
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
package org.springcloud.gateway.core.web.mapping;

import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.isBlank;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.springcloud.gateway.core.web.mapping.PrefixHandlerMappingSupport.PathUtils;
import org.springframework.beans.BeansException;
import org.springframework.web.reactive.result.condition.PatternsRequestCondition;
import org.springframework.web.reactive.result.method.RequestMappingInfo;
import org.springframework.web.reactive.result.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.util.pattern.PathPattern;

public class ReactivePrefixHandlerMapping extends RequestMappingHandlerMapping {

    private final String mappingPrefix;
    private final Object handlers[];

    public ReactivePrefixHandlerMapping(@Nullable String mappingPrefix, @NotNull Object... handlers) {
        // Default by empty
        this.mappingPrefix = isBlank(mappingPrefix) ? "" : mappingPrefix;
        this.handlers = handlers.clone();
        setOrder(-50);
    }

    @Override
    public void afterPropertiesSet() {
        super.afterPropertiesSet();
        for (Object handler : handlers) {
            detectHandlerMethods(handler);
        }
    }

    @Override
    protected void initApplicationContext() throws BeansException {
        super.initApplicationContext();
    }

    @Override
    protected boolean isHandler(Class<?> beanType) {
        return false;
    }

    @Override
    protected void registerHandlerMethod(Object handler, Method method, RequestMappingInfo mapping) {
        if (mapping == null) {
            return;
        }
        super.registerHandlerMethod(handler, method, withPrefix(mapping));
    }

    @SuppressWarnings("deprecation")
    private RequestMappingInfo withPrefix(RequestMappingInfo mapping) {
        PatternsRequestCondition patterns = new PatternsRequestCondition(
                withNewPatterns(mapping.getPatternsCondition().getPatterns()));
        return new RequestMappingInfo(patterns, mapping.getMethodsCondition(), mapping.getParamsCondition(),
                mapping.getHeadersCondition(), mapping.getConsumesCondition(), mapping.getProducesCondition(),
                mapping.getCustomCondition());
    }

    private List<PathPattern> withNewPatterns(Set<PathPattern> patterns) {
        return patterns.stream()
                .map(pattern -> getPathPatternParser().parse(PathUtils.normalizePath(mappingPrefix.concat(pattern.toString()))))
                .collect(toList());
    }

}