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

import java.util.Map;

import org.springframework.web.server.ServerWebExchange;
import org.springcloud.gateway.core.commons.model.IamRequestLimiterFilterFactory;
import org.springcloud.gateway.core.commons.model.config.IamRequestLimiterProperties.LimiterProperties.AbstractLimiterProperties;
import org.springcloud.gateway.core.commons.model.interceptor.IamRequestLimiter.RequestLimiterPrivoder;
import org.springcloud.gateway.core.commons.model.interceptor.quota.RedisQuotaRequestLimiterStrategy;
import org.springcloud.gateway.core.commons.model.interceptor.rate.RedisRateRequestLimiterStrategy;
import org.springcloud.gateway.core.framework.operator.Operator;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import reactor.core.publisher.Mono;

/**
 * {@link IamRequestLimiter}
 * 
 * @author springcloudgateway &lt;springcloudgateway@163.com, springcloudgateway@163.com&gt;
 * @version v1.0.0
 * @since v3.0.0
 */
public interface IamRequestLimiter extends Operator<RequestLimiterPrivoder> {

    Mono<LimitedResult> isAllowed(
            IamRequestLimiterFilterFactory.Config config,
            ServerWebExchange exchange,
            String routeId,
            String limitKey);

    AbstractLimiterProperties getDefaultLimiter();

    @Getter
    @ToString
    @AllArgsConstructor
    public static class LimitedResult {
        private final boolean allowed;
        private final long tokensLeft;
        private final Map<String, String> headers;
    }

    @Getter
    @AllArgsConstructor
    public static enum RequestLimiterPrivoder {
        RedisRateLimiter(RedisRateRequestLimiterStrategy.class),

        RedisQuotaLimiter(RedisQuotaRequestLimiterStrategy.class);

        private final Class<? extends RequestLimiterStrategy> strategyClass;
    }

}
