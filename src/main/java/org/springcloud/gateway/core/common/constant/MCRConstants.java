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
package org.springcloud.gateway.core.common.constant;

/**
 * IAM for event bus constants.
 * 
 * @author springcloudgateway &lt;springcloudgateway@gmail.com&gt;
 * @version v1.0.0
 * @since v3.0.0
 */
public abstract class MCRConstants extends SCGBaseConstants {

    public static final String CONF_PREFIX_SCG_RCM = CONF_PREFIX_SCG + ".rcm";
    public static final String CONF_PREFIX_SCG_RCM_EVENTBUS = CONF_PREFIX_SCG_RCM + ".eventbus";
    public static final String CONF_PREFIX_SCG_RCM_EVENTBUS_KAFKA = CONF_PREFIX_SCG_RCM_EVENTBUS + ".kafka";
    public static final String CONF_PREFIX_SCG_RCM_EVENTBUS_PULSAR = CONF_PREFIX_SCG_RCM_EVENTBUS + ".pulsar";
    public static final String CONF_PREFIX_SCG_RCM_EVENTBUS_ROCKETMQ = CONF_PREFIX_SCG_RCM_EVENTBUS + ".rocketmq";

    public static final String DEF_IAM_RCM_EVENTBUS_TOPIC = "iam_event";

}