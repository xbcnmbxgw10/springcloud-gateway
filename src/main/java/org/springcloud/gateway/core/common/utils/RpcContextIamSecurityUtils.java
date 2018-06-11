/*
 * Copyright (C) 2017 ~ 2025 the original author or authors.
 * <springcloudgateway@gmail.com> Technology CO.LTD.
 * All rights reserved.
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
 *
 * Reference to website: http://springcloud.gateway.com
 */
package org.springcloud.gateway.core.common.utils;

import static org.springcloud.gateway.core.common.constant.ServiceIAMConstants.KEY_IAM_RPC_PRINCIPAL;
import static org.springcloud.gateway.core.common.constant.ServiceIAMConstants.KEY_IAM_RPC_PRINCIPAL_ID;
import static org.springcloud.gateway.core.common.constant.ServiceIAMConstants.KEY_IAM_RPC_PRINCIPAL_USER;
import static org.springcloud.gateway.core.bridge.RpcContextHolderBridges.hasRpcContextHolderClass;
import static org.springcloud.gateway.core.bridge.RpcContextHolderBridges.invokeGet;
import static org.springcloud.gateway.core.bridge.RpcContextHolderBridges.invokeGetRef;

import org.springcloud.gateway.core.common.subject.IamPrincipal;
import org.springcloud.gateway.core.common.subject.SimpleIamPrincipal;
import org.springcloud.gateway.core.bridge.IamSecurityHolderBridges;

/**
 * {@link RpcContextIamSecurityUtils}
 *
 * @author springcloudgateway &lt;springcloudgateway@gmail.com&gt;
 * @version v1.0.0
 * @sine v1.0
 * @see {@link org.springcloud.gateway.core.web.interceptor.IamContextAutoConfiguration}
 */
public abstract class RpcContextIamSecurityUtils {

    public static IamPrincipal currentIamPrincipal() {
        if (hasRpcContextHolderClass()) { // Distributed(Cluster)
            // @see:org.springcloud.gateway.core.core.rpc.RpcContextIamSecurityAutoConfiguration.RpcContextSecurityHandlerInterceptor#preHandle
            // @see:org.springcloudgatewayinxcnm.integration.feign.core.context.internal.ProviderFeignContextInterceptor#preHandle
            return (IamPrincipal) invokeGetRef(false, KEY_IAM_RPC_PRINCIPAL, SimpleIamPrincipal.class);
        }
        // Standalone
        return (IamPrincipal) IamSecurityHolderBridges.invokeGetPrincipalInfo();
    }

    public static String currentIamPrincipalId() {
        if (hasRpcContextHolderClass()) { // Distributed(Cluster)
            // @see:org.springcloud.gateway.core.core.rpc.RpcContextIamSecurityAutoConfiguration.RpcContextSecurityHandlerInterceptor#preHandle
            // @see:org.springcloudgatewayinxcnm.integration.feign.core.context.internal.ProviderFeignContextInterceptor#preHandle
            return (String) invokeGet(false, KEY_IAM_RPC_PRINCIPAL_ID, String.class);
        }
        // Standalone
        return currentIamPrincipal().getPrincipalId();
    }

    public static String currentIamPrincipalName() {
        if (hasRpcContextHolderClass()) { // Distributed(Cluster)
            // @see:org.springcloud.gateway.core.core.rpc.RpcContextIamSecurityAutoConfiguration.RpcContextSecurityHandlerInterceptor#preHandle
            // @see:org.springcloudgatewayinxcnm.integration.feign.core.context.internal.ProviderFeignContextInterceptor#preHandle
            return (String) invokeGet(false, KEY_IAM_RPC_PRINCIPAL_USER, String.class);
        }
        // Standalone
        return currentIamPrincipal().getPrincipal();
    }

}
