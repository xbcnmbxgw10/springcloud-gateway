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
package org.springcloud.gateway.core.commons.metrics.config;

import static org.springcloud.gateway.core.common.constant.GatewayIAMConstants.CONF_PREFIX_IAM_GATEWAY_LOADBANANER;

import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springcloud.gateway.core.commons.metrics.chooser.CanaryLoadBalancerChooser;
import org.springcloud.gateway.core.commons.metrics.chooser.DestinationCanaryHashLoadBalancerChooser;
import org.springcloud.gateway.core.commons.metrics.chooser.LeastConnCanaryLoadBalancerChooser;
import org.springcloud.gateway.core.commons.metrics.chooser.LeastTimeCanaryLoadBalancerChooser;
import org.springcloud.gateway.core.commons.metrics.chooser.RandomCanaryLoadBalancerChooser;
import org.springcloud.gateway.core.commons.metrics.chooser.RoundRobinCanaryLoadBalancerChooser;
import org.springcloud.gateway.core.commons.metrics.chooser.SourceHashCanaryLoadBalancerChooser;
import org.springcloud.gateway.core.commons.metrics.chooser.WeightLeastConnCanaryLoadBalancerChooser;
import org.springcloud.gateway.core.commons.metrics.chooser.WeightLeastTimeCanaryLoadBalancerChooser;
import org.springcloud.gateway.core.commons.metrics.chooser.WeightRandomCanaryLoadBalancerChooser;
import org.springcloud.gateway.core.commons.metrics.chooser.WeightRoundRobinCanaryLoadBalancerChooser;
import org.springcloud.gateway.core.commons.metrics.chooser.CanaryLoadBalancerChooser.LoadBalancerAlgorithm;
import org.springcloud.gateway.core.commons.metrics.metrics.CanaryLoadBalancerCollector;
import org.springcloud.gateway.core.commons.metrics.stats.DefaultLoadBalancerStats;
import org.springcloud.gateway.core.commons.metrics.stats.InMemoryLoadBalancerRegistry;
import org.springcloud.gateway.core.commons.metrics.stats.LoadBalancerRegistry;
import org.springcloud.gateway.core.commons.metrics.stats.LoadBalancerStats;
import org.springcloud.gateway.core.commons.metrics.stats.ReachableStrategy;
import org.springcloud.gateway.core.commons.metrics.stats.ReachableStrategy.DefaultLatestReachableStrategy;
import org.springcloud.gateway.core.commons.microtag.GatewayMetricsFacade;
import org.springcloud.gateway.core.commons.serv.CanaryLoadBalancerFilterFactory;
import org.springcloud.gateway.core.framework.operator.GenericOperatorAdapter;
import org.springcloud.gateway.core.web.matcher.SpelRequestMatcher;

import io.prometheus.client.Collector;
import io.prometheus.client.CollectorRegistry;

/**
 * {@link CanaryLoadbalanceAutoConfiguration}
 * 
 * @author springcloudgateway &lt;springcloudgateway@163.com, springcloudgateway@163.com&gt;
 * @version v1.0.0
 * @since v1.0.0
 */
public class CanaryLoadbalanceAutoConfiguration {

    @Bean
    @ConfigurationProperties(prefix = CONF_PREFIX_IAM_GATEWAY_LOADBANANER)
    public CanaryLoadBalancerProperties canaryLoadBalancerProperties() {
        return new CanaryLoadBalancerProperties();
    }

    // Load-balancer loadBalancerStats.

    @Bean
    @ConditionalOnMissingBean
    public LoadBalancerRegistry inMemoryLoadBalancerCache() {
        return new InMemoryLoadBalancerRegistry();
    }

    @Bean
    @ConditionalOnMissingBean
    public ReachableStrategy defaultReachableStrategy() {
        return new DefaultLatestReachableStrategy();
    }

    @Bean
    public LoadBalancerStats defaultLoadBalancerStats(CanaryLoadBalancerProperties loadBalancerConfig) {
        return new DefaultLoadBalancerStats(loadBalancerConfig);
    }

    // Load-balancer metrics.

    /**
     * @see {@link org.springframework.boot.actuate.autoconfigure.metrics.export.prometheus.PrometheusMetricsExportAutoConfiguration}
     */
    public Collector canaryLoadBalancerCollector(CollectorRegistry registry) {
        CanaryLoadBalancerCollector collector = new CanaryLoadBalancerCollector();
        registry.register(collector);
        return collector;
    }

    // Load-balancer rules.

    @Bean(BEAN_CANARY_LB_REQUEST_MATCHER)
    public SpelRequestMatcher canaryLoadBalancerSpelRequestMatcher(CanaryLoadBalancerProperties loadBalancerConfig) {
        return new SpelRequestMatcher(loadBalancerConfig.getCanaryMatchRuleDefinitions());
    }

    @Bean
    public CanaryLoadBalancerChooser destinationHashCanaryLoadBalancerRule() {
        return new DestinationCanaryHashLoadBalancerChooser();
    }

    @Bean
    public CanaryLoadBalancerChooser leastConnCanaryLoadBalancerRule() {
        return new LeastConnCanaryLoadBalancerChooser();
    }

    @Bean
    public CanaryLoadBalancerChooser leastTimeCanaryLoadBalancerRule() {
        return new LeastTimeCanaryLoadBalancerChooser();
    }

    @Bean
    public CanaryLoadBalancerChooser randomCanaryLoadBalancerRule() {
        return new RandomCanaryLoadBalancerChooser();
    }

    @Bean
    public CanaryLoadBalancerChooser roundRobinCanaryLoadBalancerRule() {
        return new RoundRobinCanaryLoadBalancerChooser();
    }

    @Bean
    public CanaryLoadBalancerChooser sourceHashCanaryLoadBalancerRule() {
        return new SourceHashCanaryLoadBalancerChooser();
    }

    @Bean
    public CanaryLoadBalancerChooser weightRandomCanaryLoadBalancerRule() {
        return new WeightRandomCanaryLoadBalancerChooser();
    }

    @Bean
    public CanaryLoadBalancerChooser weightRoundRobinCanaryLoadBalancerRule() {
        return new WeightRoundRobinCanaryLoadBalancerChooser();
    }

    @Bean
    public CanaryLoadBalancerChooser weightLeastConnCanaryLoadBalancerRule() {
        return new WeightLeastConnCanaryLoadBalancerChooser();
    }

    @Bean
    public CanaryLoadBalancerChooser weightLeastTimeCanaryLoadBalancerRule() {
        return new WeightLeastTimeCanaryLoadBalancerChooser();
    }

    @Bean
    public GenericOperatorAdapter<LoadBalancerAlgorithm, CanaryLoadBalancerChooser> compositeCanaryLoadBalancerAdapter(
            List<CanaryLoadBalancerChooser> rules) {
        return new GenericOperatorAdapter<LoadBalancerAlgorithm, CanaryLoadBalancerChooser>(rules) {
        };
    }

    // Load-balancer filters.

    @Bean
    public CanaryLoadBalancerFilterFactory canaryLoadBalancerFilterFactory(
            CanaryLoadBalancerProperties loadBalancerConfig,
            GenericOperatorAdapter<LoadBalancerAlgorithm, CanaryLoadBalancerChooser> ruleAdapter,
            LoadBalancerStats loadBalancerStats,
            GatewayMetricsFacade metricsFacade) {
        return new CanaryLoadBalancerFilterFactory(loadBalancerConfig, ruleAdapter, loadBalancerStats, metricsFacade);
    }

    public static final String BEAN_CANARY_LB_REQUEST_MATCHER = "canaryLoadBalancerSpelRequestMatcher";
    public static final String BEAN_CANARY_LB_STATS = "defaultCanaryLoadBalancerStats";

}
