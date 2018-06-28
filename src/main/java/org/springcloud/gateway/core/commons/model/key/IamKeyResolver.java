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
package org.springcloud.gateway.core.commons.model.key;

import org.springframework.web.server.ServerWebExchange;
import org.springcloud.gateway.core.commons.model.config.IamRequestLimiterProperties;
import org.springcloud.gateway.core.commons.model.key.HeaderIamKeyResolver.HeaderKeyResolverStrategy;
import org.springcloud.gateway.core.commons.model.key.HostIamKeyResolver.HostKeyResolverStrategy;
import org.springcloud.gateway.core.commons.model.key.IamKeyResolver.KeyResolverStrategy;
import org.springcloud.gateway.core.commons.model.key.IntervalIamKeyResolver.IntervalKeyResolverStrategy;
import org.springcloud.gateway.core.commons.model.key.IpRangeIamKeyResolver.IpRangeKeyResolverStrategy;
import org.springcloud.gateway.core.commons.model.key.PathIamKeyResolver.PathKeyResolverStrategy;
import org.springcloud.gateway.core.commons.model.key.PrincipalIamKeyResolver.PrincipalKeyResolverStrategy;
import org.springcloud.gateway.core.framework.operator.Operator;

import lombok.AllArgsConstructor;
import lombok.Getter;
import reactor.core.publisher.Mono;

/**
 * {@link IamKeyResolver}
 * 
 * @author springcloudgateway &lt;springcloudgateway@163.com, springcloudgateway@163.com&gt;
 * @version v1.0.0
 * @since v3.0.0
 */
public interface IamKeyResolver<C extends KeyResolverStrategy> extends Operator<IamKeyResolver.KeyResolverProvider> {

    Mono<String> resolve(C strategy, ServerWebExchange exchange);

    @Getter
    @AllArgsConstructor
    public static enum KeyResolverProvider {
        Host(HostKeyResolverStrategy.class),

        IpRange(IpRangeKeyResolverStrategy.class),

        Principal(PrincipalKeyResolverStrategy.class),

        Path(PathKeyResolverStrategy.class),

        Header(HeaderKeyResolverStrategy.class),

        Interval(IntervalKeyResolverStrategy.class);

        private final Class<? extends KeyResolverStrategy> strategyClass;
    }

    // @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include =
    // JsonTypeInfo.As.PROPERTY, property = "privoder")
    // @JsonSubTypes({ @Type(value = HostKeyResolverStrategy.class,name="Host"),
    // @Type(value = PrincipalKeyResolverStrategy.class, name = "Principal"),
    // @Type(value = PathKeyResolverStrategy.class, name = "Path"),
    // @Type(value = HeaderKeyResolverStrategy.class, name = "Header"),
    // @Type(value = IntervalKeyResolverStrategy.class, name = "Interval"),
    // @Type(value = IpRangeKeyResolverStrategy.class, name = "IpRange")})
    public static abstract class KeyResolverStrategy {
        public void applyDefaultIfNecessary(IamRequestLimiterProperties config) {
        }
    }

}
