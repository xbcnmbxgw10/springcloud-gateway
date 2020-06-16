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

import static java.util.Objects.isNull;
import static org.springcloud.gateway.core.core.ReflectionUtils2.findField;
import static org.springcloud.gateway.core.core.ReflectionUtils2.getField;

import java.lang.reflect.Field;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.gateway.support.DelegatingServiceInstance;
import org.springcloud.gateway.core.commons.metrics.config.CanaryLoadBalancerProperties.ChooseProperties;
import org.springcloud.gateway.core.commons.metrics.stats.LoadBalancerStats.Stats;

/**
 * {@link LoadBalancerUtil}
 * 
 * @author springcloudgateway &lt;springcloudgateway@163.com, springcloudgateway@163.com&gt;
 * @version v1.0.0
 * @since v3.0.0
 */
public abstract class LoadBalancerUtil {

    public static boolean isAlive(CanaryLoadBalancerFilterFactory.Config config, Stats stats) {
        return isAlive(config.getChoose(), stats);
    }

    public static boolean isAlive(ChooseProperties chooseConfig, Stats stats) {
        return isNull(stats.getAlive()) ? chooseConfig.isNullPingToReachable() : stats.getAlive();
    }

    public static String getInstanceId(ServiceInstance instance) {
        if (instance instanceof DelegatingServiceInstance) {
            ServiceInstance _instance = getField(DELEGATE_FIELD, (DelegatingServiceInstance) instance, true);
            return _instance.getInstanceId();
        }
        return instance.getInstanceId();
    }

    public static final Field DELEGATE_FIELD = findField(DelegatingServiceInstance.class, "delegate", ServiceInstance.class);

}
