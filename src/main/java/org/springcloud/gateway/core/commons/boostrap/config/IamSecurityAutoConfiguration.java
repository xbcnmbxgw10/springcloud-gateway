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
package org.springcloud.gateway.core.commons.boostrap.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.StringRedisTemplate;

import org.springcloud.gateway.core.common.constant.GatewayMAIConstants;
<<<<<<< HEAD
import org.springcloud.gateway.core.commons.boostrap.util.SimpleRequestFactory;
import org.springcloud.gateway.core.commons.event.DefaultRedisSignAuthingEventRecoder;
=======
import org.springcloud.gateway.core.commons.boostrap.sign.SimpleSignAuthingFilterFactory;
import org.springcloud.gateway.core.commons.bootstrap.sign.event.DefaultRedisSignAuthingEventRecoder;
>>>>>>> 6ddba0b... FIX: optimization and suchs configurations
import org.springcloud.gateway.core.commons.microtag.GatewayMetricsFacade;
import org.springcloud.gateway.core.eventbus.EventBusSupport;

/**
 * {@link IamSecurityAutoConfiguration}
 * 
 * @author springcloudgateway &lt;springcloudgateway@163.com, springcloudgateway@163.com&gt;
 * @version v1.0.0
 * @since v3.0.0
 */
public class IamSecurityAutoConfiguration {

    @Bean
    @ConfigurationProperties(prefix = GatewayMAIConstants.CONF_PREFIX_SCG_GATEWAY_SECURITY)
    public IamSecurityProperties iamSecurityProperties() {
        return new IamSecurityProperties();
    }

    // Simple signature authorizer configuration.

    @Bean(name = BEAN_SIMPLE_SIGN_EVENTBUS, destroyMethod = "close")
    public EventBusSupport simpleSignAuthingEventBusSupport(IamSecurityProperties authingConfig) {
        return new EventBusSupport(authingConfig.getSimpleSign().getEventRecorder().getPublishEventBusThreads());
    }

    @Bean
    public SimpleRequestFactory simpleRequestFactory(
            IamSecurityProperties authingConfig,
            StringRedisTemplate stringTemplate,
            GatewayMetricsFacade metricsFacade,
            @Qualifier(BEAN_SIMPLE_SIGN_EVENTBUS) EventBusSupport eventBus) {
        return new SimpleRequestFactory(authingConfig, stringTemplate, metricsFacade, eventBus);
    }

    // Simple signature authorizer event recorder

    @Bean
    public DefaultRedisSignAuthingEventRecoder defaultRedisSignAuthingEventRecoder(
            @Qualifier(BEAN_SIMPLE_SIGN_EVENTBUS) EventBusSupport eventBus) {
        DefaultRedisSignAuthingEventRecoder recorder = new DefaultRedisSignAuthingEventRecoder();
        eventBus.register(recorder);
        return recorder;
    }

    // Oauth2 authorizer configuration.

    // @Bean
    // public TokenRelayRefreshFilter
    // tokenRelayRefreshGatewayFilter(
    // ServerOAuth2AuthorizedClientRepository authorizedClientRepository,
    // ReactiveClientRegistrationRepository clientRegistrationRepository) {
    // return new
    // TokenRelayRefreshFilterFactory(authorizedClientRepository,
    // clientRegistrationRepository);
    // }

    public static final String BEAN_SIMPLE_SIGN_EVENTBUS = "simpleSignAuthingEventBusSupport";

}
