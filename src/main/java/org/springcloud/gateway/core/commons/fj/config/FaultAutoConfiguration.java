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
package org.springcloud.gateway.core.commons.fj.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;

import org.springcloud.gateway.core.common.constant.GatewayMAIConstants;
import org.springcloud.gateway.core.commons.fj.InectorFilterFactory;
import org.springcloud.gateway.core.commons.microtag.GatewayMetricsFacade;

/**
 * {@link FaultAutoConfiguration}
 * 
 * @author springcloudgateway &lt;springcloudgateway@163.com, springcloudgateway@163.com&gt;
 * @version v1.0.0
 * @since v3.0.0
 */
public class FaultAutoConfiguration {

    @Bean
    @ConfigurationProperties(prefix = GatewayMAIConstants.CONF_PREFIX_SCG_GATEWAY_FAULT)
    public FaultProperties faultProperties() {
        return new FaultProperties();
    }

    @Bean
    public InectorFilterFactory inectorFilterFactory(
            FaultProperties faultConfig,
            GatewayMetricsFacade metricsFacade) {
        return new InectorFilterFactory(faultConfig, metricsFacade);
    }

}
