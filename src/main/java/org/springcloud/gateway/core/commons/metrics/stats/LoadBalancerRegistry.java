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
package org.springcloud.gateway.core.commons.metrics.stats;

import java.util.Map;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.springcloud.gateway.core.commons.metrics.stats.LoadBalancerStats.InstanceStatus;
import org.springcloud.gateway.core.commons.metrics.stats.LoadBalancerStats.RouteServiceStatus;
import org.springcloud.gateway.core.commons.serv.CanaryLoadBalancerFilterFactory;

/**
 * {@link LoadBalancerRegistry}
 * 
 * @author springcloudgateway &lt;springcloudgateway@163.com, springcloudgateway@163.com&gt;
 * @version v1.0.0
 * @since v3.0.0
 */
public interface LoadBalancerRegistry {

    void register(
            @NotBlank String routeId,
            @NotNull CanaryLoadBalancerFilterFactory.Config config,
            @NotNull InstanceStatus instance);

    void update(@NotBlank String routeId, @NotNull RouteServiceStatus routeService, boolean safeCheck);

    @NotNull
    Map<String, RouteServiceStatus> getAllRouteServices();

    @NotNull
    RouteServiceStatus getRouteService(@NotBlank String routeId, boolean required);

}
