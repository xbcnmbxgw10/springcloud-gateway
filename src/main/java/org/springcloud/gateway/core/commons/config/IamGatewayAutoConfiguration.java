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
package org.springcloud.gateway.core.commons.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.web.filter.reactive.HiddenHttpMethodFilter;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import org.springcloud.gateway.core.commons.boostrap.config.IamSecurityAutoConfiguration;
import org.springcloud.gateway.core.commons.cb.config.CBAutoConfiguration;
import org.springcloud.gateway.core.commons.entrypoint.config.LoggingAutoConfiguration;
import org.springcloud.gateway.core.commons.fj.config.FaultAutoConfiguration;
import org.springcloud.gateway.core.commons.helper.config.IamRetryAutoConfiguration;
import org.springcloud.gateway.core.commons.ipfs.config.IPFSAutoConfiguration;
import org.springcloud.gateway.core.commons.kernel.config.ResponseCacheAutoConfiguration;
import org.springcloud.gateway.core.commons.metrics.config.CanaryLoadbalanceAutoConfiguration;
import org.springcloud.gateway.core.commons.microtag.config.KernalMetAutoConfiguration;
import org.springcloud.gateway.core.commons.model.config.IamRequestLimiterAutoConfiguration;
import org.springcloud.gateway.core.commons.server.config.GatewayWebServerAutoConfiguration;
import org.springcloud.gateway.core.commons.size.config.IamRequestSizeAutoConfiguration;
import org.springcloud.gateway.core.commons.tc.config.TrafficAutoConfiguration;
import org.springcloud.gateway.core.commons.telemtry.config.GrayTraceAutoConfiguration;
import org.springcloud.gateway.core.commons.zookeeper.config.RouteAutoConfiguration;

import reactor.core.publisher.Mono;

/**
 * SpringCloud gateway auto configuration.
 *
 * @author springcloudgateway<springcloudgateway@163.com>
 * @version v1.0.0
 * @since
 * @see {@link org.springframework.cloud.gateway.config.GatewayAutoConfiguration}
 */
@Configuration(proxyBeanMethods = false)
@Import({ GatewayWebServerAutoConfiguration.class, IPFSAutoConfiguration.class, IamRequestSizeAutoConfiguration.class,
        IamRequestLimiterAutoConfiguration.class, CBAutoConfiguration.class, RouteAutoConfiguration.class,
        CanaryLoadbalanceAutoConfiguration.class, IamSecurityAutoConfiguration.class, GrayTraceAutoConfiguration.class,
        LoggingAutoConfiguration.class, KernalMetAutoConfiguration.class, FaultAutoConfiguration.class,
        IamRetryAutoConfiguration.class, TrafficAutoConfiguration.class, ResponseCacheAutoConfiguration.class })
public class IamGatewayAutoConfiguration {

    @Bean
    public ReactiveByteArrayRedisTemplate reactiveByteArrayRedisTemplate(ReactiveRedisConnectionFactory connectionFactory) {
        return new ReactiveByteArrayRedisTemplate(connectionFactory);
    }

    @Bean
    public HiddenHttpMethodFilter hiddenHttpMethodFilter() {
        return new HiddenHttpMethodFilter() {
            @Override
            public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
                return chain.filter(exchange);
            }
        };
    }

    // @Bean
    // public WebClient webClient() {
    // final int maxMemorySize = 256 * 1024 * 1024;
    // final ExchangeStrategies strategies = ExchangeStrategies.builder()
    // .codecs(codecs -> codecs.defaultCodecs().maxInMemorySize(maxMemorySize))
    // .build();
    // return WebClient.builder().exchangeStrategies(strategies).build();
    // }

    // @Bean
    // public WebFilter corsWebFilter() {
    // return (ServerWebExchange ctx, WebFilterChain chain) -> {
    // ServerHttpRequest request = ctx.getRequest();
    // if (!CorsUtils.isCorsRequest(request)) {
    // return chain.filter(ctx);
    // }
    //
    // HttpHeaders requestHeaders = request.getHeaders();
    // ServerHttpResponse response = ctx.getResponse();
    // HttpMethod requestMethod =
    // requestHeaders.getAccessControlRequestMethod();
    // HttpHeaders headers = response.getHeaders();
    // headers.add(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN,
    // requestHeaders.getOrigin());
    // headers.addAll(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS,
    // requestHeaders.getAccessControlRequestHeaders());
    // if (requestMethod != null) {
    // headers.add(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS,
    // requestMethod.name());
    // }
    // headers.add(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
    // headers.add(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "*");
    // headers.add(HttpHeaders.ACCESS_CONTROL_MAX_AGE, "18000L");
    // if (request.getMethod() == HttpMethod.OPTIONS) {
    // response.setStatusCode(HttpStatus.OK);
    // return Mono.empty();
    // }
    // return chain.filter(ctx);
    // };
    // }

}