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
package org.springcloud.gateway.core.commons.config;

import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializationContext;

/**
 * {@link ReactiveByteArrayRedisTemplate}
 * 
 * @author springcloudgateway &lt;springcloudgateway@163.com, springcloudgateway@163.com&gt;
 * @version v1.0.0
 * @since v3.0.0
 * @see {@link org.springframework.data.redis.core.ReactiveStringRedisTemplate}
 */
public class ReactiveByteArrayRedisTemplate extends ReactiveRedisTemplate<byte[], byte[]> {

    public ReactiveByteArrayRedisTemplate(ReactiveRedisConnectionFactory connectionFactory) {
        this(connectionFactory, RedisSerializationContext.byteArray());
    }

    public ReactiveByteArrayRedisTemplate(ReactiveRedisConnectionFactory connectionFactory,
            RedisSerializationContext<byte[], byte[]> serializationContext) {
        super(connectionFactory, serializationContext);
    }

    public ReactiveByteArrayRedisTemplate(ReactiveRedisConnectionFactory connectionFactory,
            RedisSerializationContext<byte[], byte[]> serializationContext, boolean exposeConnection) {
        super(connectionFactory, serializationContext, exposeConnection);
    }
}
