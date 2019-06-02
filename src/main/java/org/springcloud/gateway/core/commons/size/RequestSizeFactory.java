/*
 * Copyright 2013-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springcloud.gateway.core.commons.size;

import static org.springcloud.gateway.core.lang.Assert2.notNullOf;
import static org.springframework.cloud.gateway.support.GatewayToStringStyler.filterToStringCreator;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.util.ObjectUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springcloud.gateway.core.commons.fault.IamGatewayFault.SafeFilterOrdered;
import org.springcloud.gateway.core.commons.size.config.IamRequestSizeProperties;
import org.springcloud.gateway.core.commons.size.config.IamRequestSizeProperties.RequestSizeProperties;
import org.springcloud.gateway.core.bean.ConfigBeanUtils;

import lombok.AllArgsConstructor;
import reactor.core.publisher.Mono;

/**
 * {@link RequestSizeFactory}
 * 
 * @author springcloudgateway &lt;springcloudgateway@163.com, springcloudgateway@163.com&gt;
 * @version v1.0.0
 * @since v3.0.0
 * @see {@link org.springframework.cloud.gateway.filter.factory.RequestSizeGatewayFilterFactory}
 */
public class RequestSizeFactory extends AbstractGatewayFilterFactory<RequestSizeFactory.Config> {

    private static String PREFIX = "kMGTPE";
    private static String ERROR = "Request size is larger than permissible limit."
            + " Request size is %s where permissible limit is %s";

    private final IamRequestSizeProperties requestSizeConfig;

    public RequestSizeFactory(IamRequestSizeProperties requestSizeConfig) {
        super(RequestSizeFactory.Config.class);
        this.requestSizeConfig = notNullOf(requestSizeConfig, "requestSizeConfig");
    }

    @Override
    public String name() {
        return BEAN_NAME;
    }

    private static String getErrorMessage(Long currentRequestSize, Long maxSize) {
        return String.format(ERROR, getReadableByteCount(currentRequestSize), getReadableByteCount(maxSize));
    }

    private static String getReadableByteCount(long bytes) {
        int unit = 1000;
        if (bytes < unit) {
            return bytes + " B";
        }
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = Character.toString(PREFIX.charAt(exp - 1));
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

    @Override
    public GatewayFilter apply(RequestSizeFactory.Config config) {
        applyDefaultToConfig(config);
        config.validate();
        return new IamRequestSizeGatewayFilter(config);
    }

    private void applyDefaultToConfig(Config config) {
        try {
            ConfigBeanUtils.configureWithDefault(new Config(), config, requestSizeConfig.getRequestSize());
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    public static class Config extends RequestSizeProperties {
    }

    @AllArgsConstructor
    class IamRequestSizeGatewayFilter implements GatewayFilter, Ordered {
        private final Config config;

        @Override
        public int getOrder() {
            return SafeFilterOrdered.ORDER_REQUEST_SIZE;
        }

        @Override
        public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
            ServerHttpRequest request = exchange.getRequest();
            String contentLength = request.getHeaders().getFirst("content-length");
            if (!ObjectUtils.isEmpty(contentLength)) {
                Long currentRequestSize = Long.valueOf(contentLength);
                if (currentRequestSize > config.getMaxBodySize().toBytes()) {
                    exchange.getResponse().setStatusCode(HttpStatus.PAYLOAD_TOO_LARGE);
                    if (!exchange.getResponse().isCommitted()) {
                        exchange.getResponse().getHeaders().add("errorMessage",
                                getErrorMessage(currentRequestSize, config.getMaxBodySize().toBytes()));
                    }
                    return exchange.getResponse().setComplete();
                }
            }
            return chain.filter(exchange);
        }

        @Override
        public String toString() {
            return filterToStringCreator(RequestSizeFactory.this).append("max", config.getMaxBodySize()).toString();
        }

    }

    public static final String BEAN_NAME = "IamRequestSize";

}
