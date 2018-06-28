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
package org.springcloud.gateway.core.commons.model.interceptor.quota;

import static org.springcloud.gateway.core.lang.DateUtils2.getDate;
import static java.lang.System.nanoTime;

import java.util.HashMap;
import java.util.Map;

import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.web.server.ServerWebExchange;
import org.springcloud.gateway.core.commons.microtag.GatewayMetricsFacade;
import org.springcloud.gateway.core.commons.microtag.GatewayMetricsFacade.MetricsName;
import org.springcloud.gateway.core.commons.model.IamRequestLimiterFilterFactory;
import org.springcloud.gateway.core.commons.model.config.IamRequestLimiterProperties;
import org.springcloud.gateway.core.commons.model.config.IamRequestLimiterProperties.LimiterProperties.AbstractLimiterProperties;
import org.springcloud.gateway.core.commons.model.config.IamRequestLimiterProperties.LimiterProperties.RedisQuotaLimiterProperties;
import org.springcloud.gateway.core.commons.model.configurer.LimiterStrategyConfigurer;
import org.springcloud.gateway.core.commons.model.event.QuotaLimitHitEvent;
import org.springcloud.gateway.core.commons.model.interceptor.AbstractRedisIamRequestLimiter;
import org.springcloud.gateway.core.eventbus.EventBusSupport;

import reactor.core.publisher.Mono;

/**
 * {@link RedisQuotaIamRequestLimiter}
 * 
 * @author springcloudgateway &lt;springcloudgateway@163.com, springcloudgateway@163.com&gt;
 * @version v1.0.0
 * @since v3.0.0
 */
public class RedisQuotaIamRequestLimiter extends AbstractRedisIamRequestLimiter<RedisQuotaRequestLimiterStrategy> {

    public RedisQuotaIamRequestLimiter(IamRequestLimiterProperties requestLimiterConfig, LimiterStrategyConfigurer configurer,
            ReactiveStringRedisTemplate redisTemplate, EventBusSupport eventBus, GatewayMetricsFacade metricsFacade) {
        super(requestLimiterConfig, configurer, redisTemplate, eventBus, metricsFacade);
    }

    @Override
    public RequestLimiterPrivoder kind() {
        return RequestLimiterPrivoder.RedisQuotaLimiter;
    }

    @Override
    public Mono<LimitedResult> isAllowed(
            IamRequestLimiterFilterFactory.Config config,
            ServerWebExchange exchange,
            String routeId,
            String limitKey) {
        metricsFacade.counter(MetricsName.REDIS_QUOTALIMIT_TOTAL, routeId, 1);
        final long beginTime = nanoTime();

        return configurer.loadQuotaStrategy(routeId, limitKey)
                .defaultIfEmpty(((RedisQuotaLimiterProperties) getDefaultLimiter()).getDefaultStrategy())
                .flatMap(strategy -> {
                    try {
                        String cyclePattern = getDate(strategy.getCycleDatePattern());
                        String prefix = getPrefixKey(strategy, cyclePattern);
                        String hashKey = getHashKey(routeId, limitKey);
                        return redisTemplate.opsForHash().increment(prefix, hashKey, 1).onErrorResume(ex -> {
                            if (log.isDebugEnabled()) {
                                log.debug("Error calling quota limiter redis", ex);
                            }
                            return Mono.empty();
                        }).map(accumulated -> {
                            long requestCapacity = strategy.getRequestCapacity();
                            long tokensLeft = requestCapacity - accumulated;
                            boolean allowed = accumulated < requestCapacity;

                            LimitedResult result = new LimitedResult(allowed, tokensLeft,
                                    createHeaders(strategy, cyclePattern, tokensLeft, limitKey));
                            if (log.isTraceEnabled()) {
                                log.trace("response: {}", result);
                            }
                            metricsFacade.timer(MetricsName.REDIS_QUOTALIMIT_TIME, routeId, beginTime);

                            if (!allowed) { // Total hits metric
                                metricsFacade.counter(MetricsName.REDIS_QUOTALIMIT_HITS_TOTAL, routeId, 1);
                                eventBus.post(
                                        new QuotaLimitHitEvent(routeId, limitKey, exchange.getRequest().getURI().getPath()));
                            }
                            return result;
                        });
                    } catch (Exception e) {
                        /*
                         * We don't want a hard dependency on Redis to allow
                         * traffic. Make sure to set an alert so you know if
                         * this is happening too much. Stripe's observed failure
                         * rate is 0.01%.
                         */
                        log.error("Error determining if user allowed quota from redis", e);
                    }

                    // When getting the time period mode error, only the mode
                    // raw string can be returned. e.g: yyyyMMdd
                    return Mono.just(
                            new LimitedResult(true, -1L, createHeaders(strategy, strategy.getCycleDatePattern(), -1L, limitKey)));
                });
    }

    @Override
    public AbstractLimiterProperties getDefaultLimiter() {
        return requestLimiterConfig.getLimiter().getQuota();
    }

    protected String getPrefixKey(RedisQuotaRequestLimiterStrategy strategy, String cyclePattern) {
        return requestLimiterConfig.getLimiter().getQuota().getTokenPrefix().concat(":").concat(cyclePattern);
    }

    protected String getHashKey(String routeId, String limitKey) {
        return routeId.concat(":").concat(limitKey);
    }

    protected Map<String, String> createHeaders(
            RedisQuotaRequestLimiterStrategy strategy,
            String cyclePattern,
            Long tokensLeft,
            String limitKey) {
        Map<String, String> headers = new HashMap<>();
        if (strategy.isIncludeHeaders()) {
            RedisQuotaLimiterProperties config = requestLimiterConfig.getLimiter().getQuota();
            headers.put(config.getRequestCapacityHeader(), String.valueOf(strategy.getRequestCapacity()));
            headers.put(config.getRemainingHeader(), String.valueOf(tokensLeft));
            headers.put(config.getCyclePatternHeader(), String.valueOf(cyclePattern));
            headers.put(config.getLimitKeyHeader(), String.valueOf(limitKey));
        }
        return headers;
    }

}
