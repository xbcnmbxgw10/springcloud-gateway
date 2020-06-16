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
package org.springcloud.gateway.core.commons.model.config;

import static org.springcloud.gateway.core.common.constant.GatewayMAIConstants.CACHE_PREFIX_SCG_GWTEWAY_REQUESTLIMIT_CONF_QUOTA;
import static org.springcloud.gateway.core.common.constant.GatewayMAIConstants.CACHE_PREFIX_SCG_GWTEWAY_REQUESTLIMIT_CONF_RATE;
import static org.springcloud.gateway.core.common.constant.GatewayMAIConstants.CACHE_PREFIX_SCG_GWTEWAY_REQUESTLIMIT_EVENT_HITS_RATE;
import static org.springcloud.gateway.core.common.constant.GatewayMAIConstants.CACHE_PREFIX_SCG_GWTEWAY_REQUESTLIMIT_EVENT_HITS_QUOTA;
import static org.springcloud.gateway.core.common.constant.GatewayMAIConstants.CACHE_PREFIX_SCG_GWTEWAY_REQUESTLIMIT_TOKEN_QUOTA;
import static org.springcloud.gateway.core.common.constant.GatewayMAIConstants.CACHE_PREFIX_SCG_GWTEWAY_REQUESTLIMIT_TOKEN_RATE;
import static org.springcloud.gateway.core.common.constant.GatewayMAIConstants.CACHE_SUFFIX_IAM_GATEWAY_EVENT_YYMMDD;

import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springcloud.gateway.core.commons.model.interceptor.quota.RedisQuotaRequestLimiterStrategy;
import org.springcloud.gateway.core.commons.model.interceptor.rate.RedisRateRequestLimiterStrategy;
import org.springcloud.gateway.core.commons.model.key.HeaderIamKeyResolver.HeaderKeyResolverStrategy;
import org.springcloud.gateway.core.commons.model.key.HostIamKeyResolver.HostKeyResolverStrategy;
import org.springcloud.gateway.core.commons.model.key.IntervalIamKeyResolver.IntervalKeyResolverStrategy;
import org.springcloud.gateway.core.commons.model.key.IpRangeIamKeyResolver.IpRangeKeyResolverStrategy;
import org.springcloud.gateway.core.commons.model.key.PathIamKeyResolver.PathKeyResolverStrategy;
import org.springcloud.gateway.core.commons.model.key.PrincipalIamKeyResolver.PrincipalKeyResolverStrategy;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * {@link IamRequestLimiterProperties}
 * 
 * @author springcloudgateway &lt;springcloudgateway@163.com, springcloudgateway@163.com&gt;
 * @version v1.0.0
 * @since v1.0.0
 */
@Getter
@Setter
@ToString
@Validated
public class IamRequestLimiterProperties {

    /**
     * Switch to deny requests if the Key Resolver returns an empty key,
     * defaults to true.
     */
    private boolean denyEmptyKey = true;

    /**
     * HttpStatus to return when denyEmptyKey is true, defaults to FORBIDDEN.
     */
    private String emptyKeyStatusCode = HttpStatus.FORBIDDEN.name();

    /**
     * HttpStatus to return when limiter is true, defaults to TOO_MANY_REQUESTS.
     */
    private String statusCode = HttpStatus.TOO_MANY_REQUESTS.name();

    /**
     * The default key resolver configuration properties.
     */
    private KeyResolverProperties defaultKeyResolver = new KeyResolverProperties();

    /**
     * The default limiter configuration properties.
     */
    private LimiterProperties limiter = new LimiterProperties();

    /**
     * The global event recorder configuration properties.
     */
    private EventRecorderProperties eventRecorder = new EventRecorderProperties();

    /**
     * The default key resolver configuration properties.
     */
    @Getter
    @Setter
    @ToString
    @Validated
    @AllArgsConstructor
    @NoArgsConstructor
    public static class KeyResolverProperties {

        /**
         * The key resolver based on request headers.
         */
        private HeaderKeyResolverStrategy header = new HeaderKeyResolverStrategy();

        /**
         * The key resolver based on request host.
         */
        private HostKeyResolverStrategy host = new HostKeyResolverStrategy();

        /**
         * The key resolver based on request interval.
         */
        private IntervalKeyResolverStrategy interval = new IntervalKeyResolverStrategy();

        /**
         * The key resolver based on request path.
         */
        private PathKeyResolverStrategy path = new PathKeyResolverStrategy();

        /**
         * The key resolver based on request principal.
         */
        private PrincipalKeyResolverStrategy principal = new PrincipalKeyResolverStrategy();

        /**
         * The key resolver based on request client IPs.
         */
        private IpRangeKeyResolverStrategy ipRange = new IpRangeKeyResolverStrategy();

    }

    /**
     * The request limiter configuration properties.
     */
    @Getter
    @Setter
    @ToString
    @Validated
    @AllArgsConstructor
    @NoArgsConstructor
    public static class LimiterProperties {

        /**
         * The default rate limiting configuration.
         */
        private RedisRateLimiterProperties rate = new RedisRateLimiterProperties();

        /**
         * The default quota limiting configuration.
         */
        private RedisQuotaLimiterProperties quota = new RedisQuotaLimiterProperties();

        @Getter
        @Setter
        @ToString
        @Validated
        public static abstract class AbstractLimiterProperties {

            /**
             * The name of the header that returns number of remaining requests
             * during the current second.
             */
            private String remainingHeader;

            /**
             * The name of the header that returns number of limited key the
             * current request.
             */
            private String limitKeyHeader;

            /**
             * The name of the deny header that empty key got obtained.
             */
            private String denyEmptyKeyHeader;
        }

        /**
         * The request rate limiter properties.
         */
        @Getter
        @Setter
        @ToString
        @Validated
        @AllArgsConstructor
        public static class RedisRateLimiterProperties extends AbstractLimiterProperties {

            /**
             * Redis tokens rate limiter user-level configuration key prefix.
             */
            private String configPrefix = CACHE_PREFIX_SCG_GWTEWAY_REQUESTLIMIT_CONF_RATE;

            /**
             * Redis tokens rate limiter user-level token computational key
             * prefix.
             */
            private String tokenPrefix = CACHE_PREFIX_SCG_GWTEWAY_REQUESTLIMIT_TOKEN_RATE;

            /**
             * The name of the header that returns the burst capacity
             * configuration.
             */
            private String burstCapacityHeader = RATE_BURST_CAPACITY_HEADER;

            /**
             * The name of the header that returns the replenish rate
             * configuration.
             */
            private String replenishRateHeader = RATE_REPLENISH_RATE_HEADER;

            /**
             * The name of the header that returns the requested tokens
             * configuration.
             */
            private String requestedTokensHeader = RATE_REQUESTED_TOKENS_HEADER;

            /**
             * The default strategy configuration of request current limiter
             * based on redis rate.
             */
            private RedisRateRequestLimiterStrategy defaultStrategy = new RedisRateRequestLimiterStrategy();

            public RedisRateLimiterProperties() {
                setRemainingHeader(RATE_REMAINING_HEADER);
                setLimitKeyHeader(RATE_LIMITKEY_HEADER);
                setDenyEmptyKeyHeader(RATE_DENY_EMPTYKEY_HEADER);
            }

            /**
             * Burst Capacity header name.
             */
            public static final String RATE_BURST_CAPACITY_HEADER = "X-Iscg-RateLimit-Burst-Capacity";

            /**
             * Replenish Rate Limit header name.
             */
            public static final String RATE_REPLENISH_RATE_HEADER = "X-Iscg-RateLimit-Replenish-Rate";

            /**
             * Requested Tokens header name.
             */
            public static final String RATE_REQUESTED_TOKENS_HEADER = "X-Iscg-RateLimit-Requested-Tokens";

            /**
             * Remaining Rate Limit header name.
             */
            public static final String RATE_REMAINING_HEADER = "X-Iscg-RateLimit-Remaining";

            /**
             * The limit key Rate Limit header name.
             */
            public static final String RATE_LIMITKEY_HEADER = "X-Iscg-RateLimit-LimitKey";

            /**
             * The name of the deny header that empty key got obtained.
             */
            public static final String RATE_DENY_EMPTYKEY_HEADER = "X-Iscg-RateLimit-Deny-EmptyKey";
        }

        /**
         * The request quota limiter properties.
         */
        @Getter
        @Setter
        @ToString
        @Validated
        @AllArgsConstructor
        public static class RedisQuotaLimiterProperties extends AbstractLimiterProperties {

            /**
             * The quota limiter user-level configuration key prefix.
             */
            private String configPrefix = CACHE_PREFIX_SCG_GWTEWAY_REQUESTLIMIT_CONF_QUOTA;

            /**
             * The quota limiter user-level tokens computational key prefix.
             */
            private String tokenPrefix = CACHE_PREFIX_SCG_GWTEWAY_REQUESTLIMIT_TOKEN_QUOTA;

            /**
             * The name of the header that returns the request capacity
             * configuration.
             */
            private String requestCapacityHeader = QUOTA_REQUEST_CAPACITY_HEADER;

            /**
             * The name of the header that returns the time cycle pattern of the
             * request limiting.
             */
            private String cyclePatternHeader = QUOTA_CYCLE_HEADER;

            /**
             * The defaultStrategy configuration of request current limiter
             * based on redis quota.
             */
            private RedisQuotaRequestLimiterStrategy defaultStrategy = new RedisQuotaRequestLimiterStrategy();

            public RedisQuotaLimiterProperties() {
                setRemainingHeader(QUOTA_REMAINING_HEADER);
                setLimitKeyHeader(QUOTA_LIMITKEY_HEADER);
                setDenyEmptyKeyHeader(QUOTA_DENY_EMPTYKEY_HEADER);
            }

            /**
             * Request capacity header name.
             */
            public static final String QUOTA_REQUEST_CAPACITY_HEADER = "X-Iscg-QuotaLimit-Request-Capacity";

            /**
             * Remaining Rate Limit header name.
             */
            public static final String QUOTA_REMAINING_HEADER = "X-Iscg-QuotaLimit-Remaining";

            /**
             * The limit key Rate Limit header name.
             */
            public static final String QUOTA_LIMITKEY_HEADER = "X-Iscg-QuotaLimit-LimitKey";

            /**
             * The name of the header that returns the time cycle pattern of the
             * request limiting.
             */
            public static final String QUOTA_CYCLE_HEADER = "X-Iscg-QuotaLimit-Cycle";

            /**
             * The name of the deny header that empty key got obtained.
             */
            public static final String QUOTA_DENY_EMPTYKEY_HEADER = "X-Iscg-QuotaLimit-Deny-EmptyKey";
        }

    }

    /**
     * Request limiting event recorder configuration properties.
     */
    @Getter
    @Setter
    @ToString
    @Validated
    @AllArgsConstructor
    @NoArgsConstructor
    public static class EventRecorderProperties {

        /**
         * Publish eventRecorder bus threads.
         */
        private int publishEventBusThreads = 1;

        /**
         * Based on whether the redis eventRecorder logger enables logging, if
         * it is turned on, it can be used as a downgrade recovery
         * defaultStrategy when data is lost due to a catastrophic failure of
         * the persistent accumulator.
         */
        private boolean localLogEnabled = true;

        /**
         * Limited event recorder based on redis properties.
         */
        private RedisLimitEventRecorderProperties redis = new RedisLimitEventRecorderProperties();

        @Getter
        @Setter
        @ToString
        @Validated
        @AllArgsConstructor
        @NoArgsConstructor
        public static class RedisLimitEventRecorderProperties {

            /**
             * Event logging key prefix for redis based rate limiter
             */
            private String rateHitsCumulatorPrefix = CACHE_PREFIX_SCG_GWTEWAY_REQUESTLIMIT_EVENT_HITS_RATE;

            /**
             * Event logging key prefix for redis based quota limiter
             */
            private String quotaHitsCumulatorPrefix = CACHE_PREFIX_SCG_GWTEWAY_REQUESTLIMIT_EVENT_HITS_QUOTA;

            /**
             * Redis event recorder recorder accumulator suffix of date format
             * pattern.
             */
            private String cumulatorSuffixOfDatePattern = CACHE_SUFFIX_IAM_GATEWAY_EVENT_YYMMDD;
        }

    }

}
