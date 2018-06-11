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
package org.springcloud.gateway.core.annotation.condition;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Conditional;

/**
 * Check whether the current JVM process is started in the debugging mode of
 * jdwp, otherwise the condition is not tenable.
 * 
 * @author springcloudgateway <springcloudgateway@gmail.com>
 * @version v1.0.0
 * @since
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD })
@Documented
@Conditional(OnJdwpDebugCondition.class)
public @interface ConditionalOnJdwpDebug {

	/**
	 * The key-name of the enabled environment configuration attribute property.
	 * 
	 * @return
	 */
	String enableProperty();

	/**
	 * Must be consistent with:
	 * {@link org.springcloud.gateway.core.annotation.condition.ConditionalOnJdwpDebug#enableProperty}
	 */
	public static final String ENABLE_PROPERTY = "enableProperty";

}