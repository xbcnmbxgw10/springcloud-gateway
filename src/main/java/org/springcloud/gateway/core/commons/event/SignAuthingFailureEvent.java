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
package org.springcloud.gateway.core.commons.event;

import org.springcloud.gateway.core.commons.boostrap.util.SimpleRequestFactory.AppIdExtractor;
import org.springcloud.gateway.core.commons.boostrap.util.SimpleRequestFactory.SignAlgorithm;
import org.springcloud.gateway.core.commons.boostrap.util.SimpleRequestFactory.SignHashingMode;

import lombok.Getter;

/**
 * {@link SignAuthingFailureEvent}
 * 
 * @author springcloudgateway &lt;springcloudgateway@163.com, springcloudgateway@163.com&gt;
 * @version v1.0.0
 * @since v3.0.0
 */
@Getter
public class SignAuthingFailureEvent extends BaseSignAuthingFailureEvent {
    private static final long serialVersionUID = -7291654693102770442L;

    private final String cause;

    public SignAuthingFailureEvent(String appId, AppIdExtractor extractor, SignAlgorithm algorithm, SignHashingMode mode,
            String routeId, String requsetPath, String cause) {
        super(appId, extractor, algorithm, mode, routeId, requsetPath);
        this.cause = cause;
    }

}
