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

import org.springcloud.gateway.core.constant.BaseConstants;

/**
 * Based IAM configuration constants.
 * 
 * @author springcloudgateway &lt;springcloudgateway@gmail.com&gt;
 * @version v1.0.0
 * @sine v1.0
 * @see
 */
public abstract class IAMConstants extends BaseConstants {

    public static final String CONF_PREFIX_IAM = "spring.iam";
    public static final String CONF_PREFIX_IAM_SECURITY_SNS = CONF_PREFIX_IAM + ".sns";
    public static final String CONF_PREFIX_IAM_SECURITY_CAPTCHA = CONF_PREFIX_IAM + ".captcha";

    public static final String CACHE_PREFIX_IAM = "iam";

    public static final String KEY_IAM_SUBJECT_USER = "subjectUserInfo";

}
