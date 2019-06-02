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
package org.springcloud.gateway.core.commons.entrypoint.config;

import static org.springcloud.gateway.core.common.constant.GatewayMAIConstants.CONF_PREFIX_SCG_GATEWAY_LOGGING;

import org.springcloud.gateway.core.commons.entrypoint.RequestGlobalFilter;
import org.springcloud.gateway.core.commons.entrypoint.ResponseGlobalFilter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * {@link LoggingMessageAutoConfiguration}
 * 
 * @author springcloudgateway &lt;springcloudgateway@163.com, springcloudgateway@163.com&gt;
 * @version v1.0.0
 * @since v3.0.0
 */
public class LoggingAutoConfiguration {

    @Bean
    @ConfigurationProperties(prefix = CONF_PREFIX_SCG_GATEWAY_LOGGING)
    public LoggingProperties loggingProperties() {
        return new LoggingProperties();
    }

    @Bean
    public RequestGlobalFilter requestGlobalFilter(LoggingProperties loggingConfig) {
        return new RequestGlobalFilter(loggingConfig);
    }

    @Bean
    public ResponseGlobalFilter responseGlobalFilter(LoggingProperties loggingConfig) {
        return new ResponseGlobalFilter(loggingConfig);
    }

}
