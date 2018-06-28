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
package org.springcloud.gateway.core.commons.serv;

import static org.springcloud.gateway.core.lang.Assert2.notNullOf;
import static org.springcloud.gateway.core.log.SmartLoggerFactory.getLogger;
import static java.lang.System.nanoTime;
import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.equalsAnyIgnoreCase;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR;
import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_SCHEME_PREFIX_ATTR;
import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.addOriginalRequestUrl;

import java.net.URI;
import java.time.Duration;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.DefaultResponse;
import org.springframework.cloud.client.loadbalancer.EmptyResponse;
import org.springframework.cloud.client.loadbalancer.LoadBalancerUriTools;
import org.springframework.cloud.client.loadbalancer.Response;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.ReactiveLoadBalancerClientFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.support.DelegatingServiceInstance;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.core.Ordered;
import org.springframework.web.server.ServerWebExchange;
import org.springcloud.gateway.core.commons.fault.IamGatewayFault.SafeFilterOrdered;
import org.springcloud.gateway.core.commons.metrics.chooser.CanaryLoadBalancerChooser;
import org.springcloud.gateway.core.commons.metrics.chooser.CanaryLoadBalancerChooser.LoadBalancerAlgorithm;
import org.springcloud.gateway.core.commons.metrics.config.CanaryLoadBalancerProperties;
import org.springcloud.gateway.core.commons.metrics.config.CanaryLoadBalancerProperties.ChooseProperties;
import org.springcloud.gateway.core.commons.metrics.config.CanaryLoadBalancerProperties.ProbeProperties;
import org.springcloud.gateway.core.commons.metrics.stats.LoadBalancerStats;
import org.springcloud.gateway.core.commons.microtag.GatewayMetricsFacade;
import org.springcloud.gateway.core.commons.microtag.GatewayMetricsFacade.MetricsName;
import org.springcloud.gateway.core.commons.microtag.GatewayMetricsFacade.MetricsTag;
import org.springcloud.gateway.core.bean.ConfigBeanUtils;
import org.springcloud.gateway.core.log.SmartLogger;
import org.springcloud.gateway.core.framework.operator.GenericOperatorAdapter;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SignalType;

/**
 * Grayscale Load-Balancer filter. </br>
 * </br>
 * 
 * Note: The retry filter should be executed before the load balancing filter,
 * so that other back-end servers can be selected when retrying. see to:
 * {@link org.springframework.cloud.gateway.handler.FilteringWebHandler.DefaultGatewayFilterChain#filter(ServerWebExchange)}
 * 
 * @author springcloudgateway &lt;springcloudgateway@163.com, springcloudgateway@163.com&gt;
 * @version v1.0.0
 * @since v3.0.0
 */
@Getter
@ToString
public class CanaryLoadBalancerFilterFactory extends AbstractGatewayFilterFactory<CanaryLoadBalancerFilterFactory.Config> {

    private final CanaryLoadBalancerProperties loadBalancerConfig;
    private final GenericOperatorAdapter<LoadBalancerAlgorithm, CanaryLoadBalancerChooser> ruleAdapter;
    private final LoadBalancerStats loadBalancerStats;
    private final GatewayMetricsFacade metricsFacade;

    public CanaryLoadBalancerFilterFactory(CanaryLoadBalancerProperties loadBalancerConfig,
            GenericOperatorAdapter<LoadBalancerAlgorithm, CanaryLoadBalancerChooser> ruleAdapter,
            LoadBalancerStats loadBalancerStats, GatewayMetricsFacade metricsFacade) {
        super(CanaryLoadBalancerFilterFactory.Config.class);
        this.loadBalancerConfig = notNullOf(loadBalancerConfig, "loadBalancerConfig");
        this.ruleAdapter = notNullOf(ruleAdapter, "ruleAdapter");
        this.loadBalancerStats = notNullOf(loadBalancerStats, "loadBalancerStats");
        this.metricsFacade = notNullOf(metricsFacade, "metricsFacade");
    }

    @Override
    public String name() {
        return NAME_CANARY_LOADBALANCER_FILTER;
    }

    @Override
    public GatewayFilter apply(Config config) {
        applyDefaultToConfig(config);
        return new CanaryLoadBalancerGatewayFilter(ruleAdapter, loadBalancerStats, config, metricsFacade);
    }

    private void applyDefaultToConfig(Config config) {
        try {
            ConfigBeanUtils.configureWithDefault(new ChooseProperties(), config.getChoose(),
                    loadBalancerConfig.getDefaultChoose());
            ConfigBeanUtils.configureWithDefault(new ProbeProperties(), loadBalancerConfig.getDefaultProbe(),
                    loadBalancerConfig.getDefaultProbe());
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    @Getter
    @Setter
    @ToString
    public static class Config extends ProbeProperties {

        /**
         * LoadBalancer choose properties.
         */
        private ChooseProperties choose = new ChooseProperties();

        /**
         * Health probe properties.
         */
        private ProbeProperties probe = new ProbeProperties();
    }

    @Getter
    public static class CanaryLoadBalancerGatewayFilter implements GatewayFilter, Ordered {
        private final SmartLogger log = getLogger(getClass());
        private final GenericOperatorAdapter<LoadBalancerAlgorithm, CanaryLoadBalancerChooser> ruleAdapter;
        private final LoadBalancerStats loadBalancerStats;
        private final Config config;
        private final GatewayMetricsFacade metricsFacade;

        public CanaryLoadBalancerGatewayFilter(
                GenericOperatorAdapter<LoadBalancerAlgorithm, CanaryLoadBalancerChooser> ruleAdapter,
                LoadBalancerStats loadBalancerStats, Config config, GatewayMetricsFacade metricsFacade) {
            this.ruleAdapter = notNullOf(ruleAdapter, "ruleAdapter");
            this.loadBalancerStats = notNullOf(loadBalancerStats, "loadBalancerStats");
            this.config = notNullOf(config, "config");
            this.metricsFacade = notNullOf(metricsFacade, "metricsFacade");
        }

        /**
         * Note: The Load balancing filter order must be before
         * {@link org.springframework.cloud.gateway.filter.ReactiveLoadBalancerClientFilter#LOAD_BALANCER_CLIENT_FILTER_ORDER}
         * {@link org.springframework.cloud.gateway.filter.ReactiveLoadBalancerClientFilter#filter#L116}
         * but after
         * {@link org.springframework.cloud.gateway.filter.RouteToRequestUrlFilter}
         * 
         * Note: The retry filter should be executed before the load balancing
         * filter, so that other back-end servers can be selected when retrying.
         * 
         * The correct and complete execution chain is as follows:
         * {@link org.springframework.cloud.gateway.handler.FilteringWebHandler#loadFilters()}
         * and
         * {@link org.springframework.cloud.gateway.handler.FilteringWebHandler#handle(ServerWebExchange)}
         * and
         * {@link org.springframework.cloud.gateway.filter.RouteToRequestUrlFilter#order}
         * and
         * {@link org.springcloud.gateway.core.commons.serv.CanaryLoadBalancerFilterFactory}
         * and
         * {@link org.springframework.cloud.gateway.filter.ReactiveLoadBalancerClientFilter#filter}
         * and
         * {@link org.springframework.cloud.gateway.filter.factory.RetryGatewayFilterFactory}
         */
        @Override
        public int getOrder() {
            return SafeFilterOrdered.ORDER_CANARY_LOADBALANCER;
        }

        @Override
        public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
            URI requestUri = exchange.getAttribute(GATEWAY_REQUEST_URL_ATTR);
            // see:org.springframework.cloud.gateway.filter.RouteToRequestUrlFilter#hasAnotherScheme
            String schemePrefix = exchange.getAttribute(GATEWAY_SCHEME_PREFIX_ATTR);

            // Ignore the URi prefix If it does not start with LB, go to the
            // next filter.
            if (isNull(requestUri) || (!equalsAnyIgnoreCase("LB", requestUri.getScheme(), schemePrefix))) {
                return chain.filter(exchange);
            }

            // According to the original URL of the gateway. Replace the URI of
            // http://IP:PORT/path
            addOriginalRequestUrl(exchange, requestUri);
            if (log.isTraceEnabled()) {
                log.trace(ReactiveLoadBalancerClientFilter.class.getSimpleName() + " url before: " + requestUri);
            }

            Response<ServiceInstance> response = choose(config, exchange);
            if (!response.hasServer()) {
                String errmsg = "Unable to find instance for ".concat(requestUri.getHost());
                log.warn(errmsg);
                throw NotFoundException.create(false, errmsg);
            }
            URI uri = exchange.getRequest().getURI();

            // if the `lb:<scheme>` mechanism was used, use `<scheme>` as the
            // default, if the loadbalancer doesn't provide one.
            String overrideScheme = !isBlank(schemePrefix) ? requestUri.getScheme() : null;
            DelegatingServiceInstance instance = new DelegatingServiceInstance(response.getServer(), overrideScheme);

            URI newRequestUri = LoadBalancerUriTools.reconstructURI(instance, uri);

            if (log.isTraceEnabled()) {
                log.trace("LoadBalancerClientFilter url chosen: {}", newRequestUri);
            }
            exchange.getAttributes().put(GATEWAY_REQUEST_URL_ATTR, newRequestUri);

            // PreFilter and PostFilter, https://blogs.springcloud.gateway.com/archives/3401
            return chain.filter(exchange).doOnRequest(v -> {
                loadBalancerStats.connect(exchange, instance);
            }).doFinally(signal -> {
                if (signal == SignalType.ON_COMPLETE || signal == SignalType.CANCEL || signal == SignalType.ON_ERROR) {
                    loadBalancerStats.disconnect(exchange, instance);
                }
            });
        }

        private Response<ServiceInstance> choose(Config config, ServerWebExchange exchange) {
            long beginTime = nanoTime();

            URI uri = exchange.getAttribute(GATEWAY_REQUEST_URL_ATTR);
            String serviceId = uri.getHost();
            ServiceInstance chosen = ruleAdapter.forOperator(config.getChoose().getLoadBalancerAlgorithm()).choose(config,
                    exchange, serviceId);

            // Add time metrics.
            metricsFacade
                    .getTimer(MetricsName.CANARY_LB_CHOOSE_TIME, MetricsTag.LB,
                            config.getChoose().getLoadBalancerAlgorithm().name())
                    .record(Duration.ofNanos(nanoTime() - beginTime));

            if (isNull(chosen)) {
                return new EmptyResponse();
            }
            return new DefaultResponse(chosen);
        }
    }

    public static final String NAME_CANARY_LOADBALANCER_FILTER = "CanaryLoadBalancer";

}