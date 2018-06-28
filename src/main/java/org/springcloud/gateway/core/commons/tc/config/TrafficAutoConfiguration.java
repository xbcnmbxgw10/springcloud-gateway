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
package org.springcloud.gateway.core.commons.tc.config;

import java.util.List;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.gateway.config.HttpClientCustomizer;
import org.springframework.cloud.gateway.filter.headers.HttpHeadersFilter;
import org.springframework.context.annotation.Bean;

import org.springcloud.gateway.core.common.constant.GatewayIAMConstants;
import org.springcloud.gateway.core.commons.microtag.GatewayMetricsFacade;
import org.springcloud.gateway.core.commons.tc.TReplicationFilterFactory;

/**
 * {@link TrafficAutoConfiguration}
 * 
 * @author springcloudgateway &lt;springcloudgateway@163.com, springcloudgateway@163.com&gt;
 * @version v1.0.0
 * @since v3.0.0
 */
public class TrafficAutoConfiguration {

    @Bean
    @ConfigurationProperties(prefix = GatewayIAMConstants.CONF_PREFIX_IAM_GATEWAY_TRAFFIC)
    public TrafficProperties trafficProperties() {
        return new TrafficProperties();
    }

    @Bean
    public TReplicationFilterFactory tReplicationFilterFactory(
            TrafficProperties trafficConfig,
            ObjectProvider<List<HttpHeadersFilter>> headersFilters,
            List<HttpClientCustomizer> customizers,
            GatewayMetricsFacade metricsFacade) {
        return new TReplicationFilterFactory(trafficConfig, headersFilters, customizers, metricsFacade);
    }

}
