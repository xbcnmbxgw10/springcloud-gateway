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
package org.springcloud.gateway.core.commons.zookeeper.repository;

import static org.springcloud.gateway.core.common.constant.GatewayMAIConstants.CACHE_PREFIX_SCG_GWTEWAY_ROUTES;
import static org.springcloud.gateway.core.modelseri.JacksonUtils.parseJSON;
import static org.springcloud.gateway.core.modelseri.JacksonUtils.toJSONString;
import static java.util.stream.Collectors.toList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.data.redis.core.StringRedisTemplate;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Redis routes information persistence class This class contains route
 * persistence and route refresh of distributed cluster.
 *
 * @author springcloudgateway<springcloudgateway@163.com>
 * @version v1.0.0
 * @since
 */
public class RedisRouteDefinitionRepository extends AbstractRouteRepository {

    private @Autowired StringRedisTemplate stringTemplate;

    @Override
    protected Flux<RouteDefinition> loadPermanentRouteDefinitions() {
        return Flux.fromIterable(stringTemplate.opsForHash()
                .values(CACHE_PREFIX_SCG_GWTEWAY_ROUTES)
                .stream()
                .map(routeDefinition -> parseJSON(routeDefinition.toString(), RouteDefinition.class))
                .collect(toList()));
    }

    @Override
    public Mono<Void> save(Mono<RouteDefinition> route) {
        return route.flatMap(routeDefinition -> {
            stringTemplate.opsForHash().put(CACHE_PREFIX_SCG_GWTEWAY_ROUTES, routeDefinition.getId(),
                    toJSONString(routeDefinition));
            return Mono.empty();
        });
    }

    @Override
    public Mono<Void> delete(Mono<String> routeId) {
        return routeId.flatMap(id -> {
            stringTemplate.opsForHash().delete(CACHE_PREFIX_SCG_GWTEWAY_ROUTES, id);
            return Mono.empty();
        });
    }

}