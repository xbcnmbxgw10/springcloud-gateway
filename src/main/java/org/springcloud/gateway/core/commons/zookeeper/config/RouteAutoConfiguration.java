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
package org.springcloud.gateway.core.commons.zookeeper.config;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.gateway.route.RouteDefinitionRepository;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;

import org.springcloud.gateway.core.common.constant.GatewayMAIConstants;
import org.springcloud.gateway.core.commons.boostrap.config.IamSecurityAutoConfiguration;
import org.springcloud.gateway.core.commons.zookeeper.Https2HttpGlobalFilter;
import org.springcloud.gateway.core.commons.zookeeper.RefreshRouteApplicationListener;
import org.springcloud.gateway.core.commons.zookeeper.TimeBasedRouteRefresher;
import org.springcloud.gateway.core.commons.zookeeper.repository.RedisRouteDefinitionRepository;

/**
 * {@link IamSecurityAutoConfiguration}
 * 
 * @author springcloudgateway &lt;springcloudgateway@163.com, springcloudgateway@163.com&gt;
 * @version v1.0.0
 * @since v1.0.0
 */
public class RouteAutoConfiguration {

    @Bean
    @ConfigurationProperties(prefix = GatewayMAIConstants.CONF_PREFIX_SCG_GATEWAY_ROUTE)
    public RouteProperties routeProperties() {
        return new RouteProperties();
    }

    @Bean
    public Https2HttpGlobalFilter https2HttpGlobalFilter(RouteProperties config) {
        return new Https2HttpGlobalFilter(config);
    }

    @Bean
    public RouteDefinitionRepository redisRouteDefinitionRepository() {
        return new RedisRouteDefinitionRepository();
    }

    // @Bean
    // public RouteDefinitionRepository nacosRouteDefinitionRepository() {
    // return new NacosRouteDefinitionRepository();
    // }

    @Bean
    public RefreshRouteApplicationListener refreshRouteApplicationListener() {
        return new RefreshRouteApplicationListener();
    }

    @Bean
    public TimeBasedRouteRefresher timeBasedRouteRefresher() {
        return new TimeBasedRouteRefresher();
    }

    // @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // 官方demo
                .route(p -> p.path("/get").filters(f -> f.addRequestHeader("Hello", "World")).uri("http://httpbin.org:80"))
                // 自定义
                .route(p -> p
                        // 日期时间断言
                        /*
                         * .between(ZonedDateTime.of(2020, 7, 8, 9, 0, 0, 0,
                         * ZoneId.systemDefault()), ZonedDateTime.of(2020, 7, 8,
                         * 14, 0, 0, 0, ZoneId.systemDefault())).and()
                         */
                        // .before(ZonedDateTime.of(2020,7,8,9,0,0,0,
                        // ZoneId.systemDefault()))
                        .after(ZonedDateTime.of(2020, 7, 8, 14, 0, 0, 0, ZoneId.systemDefault()))
                        .and()

                        // cookie断言-- 名称匹配且值符合正则表达式
                        .cookie("token", "[a-zA-Z]+")
                        .and()

                        // head断言
                        .header("session")
                        .and()

                        // host断言??
                        // .host("**.somehost.org").and()
                        // .host("**.springcloud.gateway.debug").and()

                        // method断言
                        // .method(HttpMethod.GET, HttpMethod.POST).and()

                        // query断言
                        .query("name")
                        .and()

                        // remote address 断言
                        .remoteAddr("10.0.0.113", "127.0.0.1", "localhost")
                        .and()

                        // 路径
                        .path("/**")
                        // .path("/gateway-example/test/hello")

                        // 过滤
                        .filters(f -> f.addRequestHeader("token", "World").addRequestParameter("age", "18").addResponseHeader(
                                "myResponseheader", "myResponseheader"))

                        // 目标
                        .uri("http://localhost:14086"))
                .build();
    }

}
