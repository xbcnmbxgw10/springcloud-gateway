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
package org.springcloud.gateway.core.bean.model;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

/**
 * A general pull-down box data transmission model for Web
 * 
 * @author &lt;springcloudgateway@gmail.com&gt;
 * @author vjay
 * @version v1.0.0
 * @see
 */
@Getter
@Setter
public class SelectionModel implements Serializable {
	private static final long serialVersionUID = -3412929918195969714L;

	/**
	 * Drop down box display value
	 */
	private String label;

	/**
	 * Drop down box background value.
	 */
	private String value;

	@Override
	public String toString() {
		return label + ":" + value;
	}

}