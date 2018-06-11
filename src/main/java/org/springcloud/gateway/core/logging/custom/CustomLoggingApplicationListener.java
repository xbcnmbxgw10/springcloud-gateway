/*
 * Copyright 2017 ~ 2025 the original author or authors. <springcloudgateway@gmail.com>
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
package org.springcloud.gateway.core.logging.custom;

import static org.springframework.boot.logging.LoggingSystem.SYSTEM_PROPERTY;

import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.context.logging.LoggingApplicationListener;
import org.springframework.context.ApplicationEvent;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import org.springcloud.gateway.core.logging.custom.logback.LogbackLoggingSystem;

/**
 * Enhanced logging system application listener.
 * 
 * @author springcloudgateway <springcloudgateway@gmail.com>
 * @version v1.0.0
 * @since
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE)
public class CustomLoggingApplicationListener extends LoggingApplicationListener {

    /**
     * Automatic setting uses the enhanced spring log system. Refer to the
     * source code: </br>
     * {@link org.springframework.boot.logging.LoggingApplicationListener#onApplicationStartingEvent(ApplicationStartingEvent)}
     * </br>
     * {@link org.springframework.boot.logging.LoggingSystem#get(ClassLoader)}
     * </br>
     * {@link org.springframework.boot.logging.LoggingApplicationListener#onApplicationPreparedEvent(ApplicationPreparedEvent)}
     * </br>
     */
    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        // Force priority use custom logging system
        System.setProperty(SYSTEM_PROPERTY, LogbackLoggingSystem.class.getName());
        super.onApplicationEvent(event);
    }

}