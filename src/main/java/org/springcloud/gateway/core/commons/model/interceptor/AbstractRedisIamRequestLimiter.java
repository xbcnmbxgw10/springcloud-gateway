/*
 * Copyright 2017 ~ 2025 the original author or authors.<springcloudgateway@163.com>
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
package org.springcloud.gateway.core.commons.model.interceptor;

import static org.springcloud.gateway.core.lang.Assert2.notNullOf;
import static org.springcloud.gateway.core.log.SmartLoggerFactory.getLogger;

import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springcloud.gateway.core.commons.microtag.GatewayMetricsFacade;
import org.springcloud.gateway.core.commons.model.config.IamRequestLimiterProperties;
import org.springcloud.gateway.core.commons.model.configurer.LimiterStrategyConfigurer;
import org.springcloud.gateway.core.eventbus.EventBusSupport;
import org.springcloud.gateway.core.log.SmartLogger;

/**
 * {@link AbstractRedisIamRequestLimiter}
 * 
 * @author springcloudgateway &lt;springcloudgateway@163.com, springcloudgateway@163.com&gt;
 * @version v1.0.0
 * @since v3.0.0
 */
public abstract class AbstractRedisIamRequestLimiter<S extends RequestLimiterStrategy> implements IamRequestLimiter {
    protected final SmartLogger log = getLogger(getClass());

    protected final IamRequestLimiterProperties requestLimiterConfig;
    protected final LimiterStrategyConfigurer configurer;
    protected final ReactiveStringRedisTemplate redisTemplate;
    protected final EventBusSupport eventBus;
    protected final GatewayMetricsFacade metricsFacade;

    public AbstractRedisIamRequestLimiter(IamRequestLimiterProperties requestLimiterConfig, LimiterStrategyConfigurer configurer,
            ReactiveStringRedisTemplate redisTemplate, EventBusSupport eventBus, GatewayMetricsFacade metricsFacade) {
        this.requestLimiterConfig = notNullOf(requestLimiterConfig, "requestLimiterConfig");
        this.configurer = notNullOf(configurer, "configurer");
        this.redisTemplate = notNullOf(redisTemplate, "redisTemplate");
        this.eventBus = notNullOf(eventBus, "eventBus");
        this.metricsFacade = notNullOf(metricsFacade, "metricsFacade");
    }

}
