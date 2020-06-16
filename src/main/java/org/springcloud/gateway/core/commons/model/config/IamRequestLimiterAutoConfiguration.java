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

import static org.springcloud.gateway.core.collection.CollectionUtils2.safeList;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.cloud.gateway.support.ConfigurationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;

import org.springcloud.gateway.core.common.constant.GatewayMAIConstants;
import org.springcloud.gateway.core.commons.microtag.GatewayMetricsFacade;
import org.springcloud.gateway.core.commons.model.IamRequestLimiterFilterFactory;
import org.springcloud.gateway.core.commons.model.configurer.LimiterStrategyConfigurer;
import org.springcloud.gateway.core.commons.model.configurer.RedisLimiterStrategyConfigurer;
import org.springcloud.gateway.core.commons.model.event.DefaultRedisRequestLimitEventRecorder;
import org.springcloud.gateway.core.commons.model.interceptor.IamRequestLimiter;
import org.springcloud.gateway.core.commons.model.interceptor.IamRequestLimiter.RequestLimiterPrivoder;
import org.springcloud.gateway.core.commons.model.interceptor.quota.RedisQuotaIamRequestLimiter;
import org.springcloud.gateway.core.commons.model.interceptor.rate.RedisRateIamRequestLimiter;
import org.springcloud.gateway.core.commons.model.key.HeaderIamKeyResolver;
import org.springcloud.gateway.core.commons.model.key.HostIamKeyResolver;
import org.springcloud.gateway.core.commons.model.key.IamKeyResolver;
import org.springcloud.gateway.core.commons.model.key.IntervalIamKeyResolver;
import org.springcloud.gateway.core.commons.model.key.IpRangeIamKeyResolver;
import org.springcloud.gateway.core.commons.model.key.PathIamKeyResolver;
import org.springcloud.gateway.core.commons.model.key.PrincipalIamKeyResolver;
import org.springcloud.gateway.core.commons.model.key.IamKeyResolver.KeyResolverProvider;
import org.springcloud.gateway.core.commons.model.key.IamKeyResolver.KeyResolverStrategy;
import org.springcloud.gateway.core.eventbus.EventBusSupport;
import org.springcloud.gateway.core.framework.operator.GenericOperatorAdapter;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * {@link IamRequestLimiterAutoConfiguration}
 * 
 * @author springcloudgateway &lt;springcloudgateway@163.com, springcloudgateway@163.com&gt;
 * @version v1.0.0
 * @since v1.0.0
 */
@Slf4j
public class IamRequestLimiterAutoConfiguration {

    //
    // SpringCloud rate limiter configuration.
    //

    @Bean
    @ConfigurationProperties(prefix = GatewayMAIConstants.CONF_PREFIX_SCG_GATEWAY_REQUESTLIMIT)
    public IamRequestLimiterProperties iamRequestLimiterProperties() {
        return new IamRequestLimiterProperties();
    }

    @Bean
    @ConditionalOnMissingBean
    public LimiterStrategyConfigurer redisLimiterStrategyConfigurer() {
        return new RedisLimiterStrategyConfigurer();
    }

    //
    // SpringCloud Key resolver.
    //

    @Bean
    public IamKeyResolver<? extends KeyResolverStrategy> hostIamKeyResolver() {
        return new HostIamKeyResolver();
    }

    @Bean
    public IamKeyResolver<? extends KeyResolverStrategy> ipRangeIamKeyResolver() {
        return new IpRangeIamKeyResolver();
    }

    @Bean
    public IamKeyResolver<? extends KeyResolverStrategy> headerIamKeyResolver() {
        return new HeaderIamKeyResolver();
    }

    @Bean
    public IamKeyResolver<? extends KeyResolverStrategy> pathIamKeyResolver() {
        return new PathIamKeyResolver();
    }

    @Bean
    public IamKeyResolver<? extends KeyResolverStrategy> principalNameIamKeyResolver() {
        return new PrincipalIamKeyResolver();
    }

    @Bean
    public IamKeyResolver<? extends KeyResolverStrategy> intervalIamKeyResolver() {
        return new IntervalIamKeyResolver();
    }

    @Bean
    public GenericOperatorAdapter<KeyResolverProvider, IamKeyResolver<? extends KeyResolverStrategy>> iamKeyResolverAdapter(
            List<IamKeyResolver<? extends KeyResolverStrategy>> resolvers) {
        return new GenericOperatorAdapter<KeyResolverProvider, IamKeyResolver<? extends KeyResolverStrategy>>(resolvers) {
        };
    }

    //
    // SpringCloud request limiter.
    //

    /**
     * {@link org.springframework.cloud.gateway.config.GatewayRedisAutoConfiguration#redisRateLimite}
     */
    @Bean
    public RedisRateLimiter warningDeprecatedRedisRateLimiter(
            ReactiveStringRedisTemplate redisTemplate,
            @Qualifier(RedisRateLimiter.REDIS_SCRIPT_NAME) RedisScript<List<Long>> redisScript,
            ConfigurationService configurationService) {
        return new WarningDeprecatedRedisRateLimiter(redisTemplate, redisScript, configurationService);
    }

    /**
     * {@link org.springframework.cloud.gateway.config.GatewayRedisAutoConfiguration#redisRateLimite}
     */
    @Bean
    public IamRequestLimiter redisRateIamRequestLimiter(
            RedisScript<List<Long>> redisScript,
            IamRequestLimiterProperties requestLimiterConfig,
            LimiterStrategyConfigurer configurer,
            ReactiveStringRedisTemplate redisTemplate,
            @Qualifier(BEAN_REDIS_RATELIMITE_EVENTBUS) EventBusSupport eventBus,
            GatewayMetricsFacade metricsFacade) {
        return new RedisRateIamRequestLimiter(redisScript, requestLimiterConfig, configurer, redisTemplate, eventBus,
                metricsFacade);
    }

    @Bean
    public IamRequestLimiter redisQuotaIamRequestLimiter(
            IamRequestLimiterProperties requestLimiterConfig,
            LimiterStrategyConfigurer configurer,
            ReactiveStringRedisTemplate redisTemplate,
            @Qualifier(BEAN_REDIS_RATELIMITE_EVENTBUS) EventBusSupport eventBus,
            GatewayMetricsFacade metricsFacade) {
        return new RedisQuotaIamRequestLimiter(requestLimiterConfig, configurer, redisTemplate, eventBus, metricsFacade);
    }

    @Bean
    public GenericOperatorAdapter<RequestLimiterPrivoder, IamRequestLimiter> iamRequestLimiterAdapter(
            List<IamRequestLimiter> rqeuestLimiters) {
        return new GenericOperatorAdapter<RequestLimiterPrivoder, IamRequestLimiter>(rqeuestLimiters) {
        };
    }

    /**
     * @see {@link org.springframework.cloud.gateway.config.GatewayAutoConfiguration#requestRateLimiterGatewayFilterFactory}
     */
    @Bean
    public IamRequestLimiterFilterFactory iamRequestLimiterFilterFactory(
            IamRequestLimiterProperties requsetLimiterConfig,
            GenericOperatorAdapter<KeyResolverProvider, IamKeyResolver<? extends KeyResolverStrategy>> keyResolverAdapter,
            GenericOperatorAdapter<RequestLimiterPrivoder, IamRequestLimiter> requestLimiterAdapter) {
        return new IamRequestLimiterFilterFactory(requsetLimiterConfig, keyResolverAdapter, requestLimiterAdapter);
    }

    //
    // SpringCloud limiter event.
    //

    @Bean(name = BEAN_REDIS_RATELIMITE_EVENTBUS, destroyMethod = "close")
    public EventBusSupport redisRateLimiteEventBusSupport(IamRequestLimiterProperties requestLimiteConfig) {
        return new EventBusSupport(requestLimiteConfig.getEventRecorder().getPublishEventBusThreads());
    }

    @Bean
    public DefaultRedisRequestLimitEventRecorder redisRateLimiteEventRecoder(
            @Qualifier(BEAN_REDIS_RATELIMITE_EVENTBUS) EventBusSupport eventBus) {
        DefaultRedisRequestLimitEventRecorder recorder = new DefaultRedisRequestLimitEventRecorder();
        eventBus.register(recorder);
        return recorder;
    }

    class WarningDeprecatedRedisRateLimiter extends RedisRateLimiter implements ApplicationRunner {
        private @Lazy @Autowired RouteDefinitionLocator routeLocator;

        public WarningDeprecatedRedisRateLimiter(ReactiveStringRedisTemplate redisTemplate, RedisScript<List<Long>> script,
                ConfigurationService configurationService) {
            super(redisTemplate, script, configurationService);
        }

        @Override
        public void run(ApplicationArguments args) throws Exception {
            boolean useDefaultRedisRateLimiter = routeLocator.getRouteDefinitions().collectList().block().stream().anyMatch(
                    r -> safeList(r.getFilters()).stream().anyMatch(f -> StringUtils.equals(f.getName(), "RequestRateLimiter")));
            if (useDefaultRedisRateLimiter) {
                log.warn(LOG_MESSAGE_WARNING_REDIS_RATE_LIMITER);
            }
        }

        @Override
        public Mono<Response> isAllowed(String routeId, String id) {
            log.warn(LOG_MESSAGE_WARNING_REDIS_RATE_LIMITER);
            return Mono.empty(); // Ignore
        }
    }

    public static final String BEAN_REDIS_RATELIMITE_EVENTBUS = "redisRateLimiteEventBusSupport";
    public static final String LOG_MESSAGE_WARNING_REDIS_RATE_LIMITER = "\n[WARNING]: The default redisRateLimiter is deprecated, please use the SpringCloud rate limiter with the configuration key prefix: 'spring.iam.gateway.ratelimit'\n";

}
