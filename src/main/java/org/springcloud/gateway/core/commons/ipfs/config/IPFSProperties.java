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
package org.springcloud.gateway.core.commons.ipfs.config;

import static java.util.Arrays.asList;
import static java.util.Objects.nonNull;

import java.net.UnknownHostException;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springcloud.gateway.core.filter.CIDR;
import org.springcloud.gateway.core.web.WebUtils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.With;
import lombok.experimental.SuperBuilder;

/**
 * {@link IPFSProperties}
 * 
 * @author springcloudgateway &lt;springcloudgateway@163.com, springcloudgateway@163.com&gt;
 * @version v1.0.0
 * @since v3.0.0
 */
@Getter
@Setter
@Validated
@ToString
public class IPFSProperties {

    /**
     * The default IP filter strategy configuration.
     */
    private StrategyProperties defaultStrategy = new StrategyProperties();

    @Getter
    @Setter
    @Validated
    @ToString
    public static class StrategyProperties {

        /**
         * When the white-list (allow) and the CIDRs of the blacklist (deny)
         * conflict, whether the blacklist (deny) has a higher priority.
         */
        private boolean preferRejectOnCidrConflict = true;

        /**
         * Whether to accept the request when neither the white-list nor the
         * blacklist CIDRs match.
         */
        private boolean acceptNotMatchCidr = true;

        /**
         * The allow all local addresses to pass.
         */
        private boolean anyLocalAddressAllowed = true;

        /**
         * The HttpStatus returned when the IpFilter blacklist is met, the
         * default is FORBIDDEN.
         */
        private String statusCode = HttpStatus.FORBIDDEN.name();

        /**
         * The according to the list of header names of the request header
         * current limiter, it can usually be used to obtain the actual IP after
         * being forwarded by the proxy to limit the current, or it can be
         * flexibly used for other purposes.
         */
        private List<String> forwardHeaderNames = asList(WebUtils.HEADER_REAL_IP);

        /**
         * The IP sub-net restriction strategy configuration.
         */
        private List<IPSubnet> iPSubnets;
    }

    @Getter
    @Setter
    @Validated
    @ToString
    @With
    @SuperBuilder
    @AllArgsConstructor
    public static class IPSubnet {

        /**
         * The allow or deny IP sub-net segments.
         */
        private boolean allow;

        /**
         * The matching sub-net CIDRs
         */
        private List<String> cidrs;

        public IPSubnet() {
            this.allow = false;
        }

        public IPSubnet validate() {
            if (nonNull(cidrs)) {
                cidrs.forEach(cidr -> {
                    try {
                        CIDR.newCIDR(cidr);
                    } catch (UnknownHostException e) {
                        throw new IllegalArgumentException(e);
                    }
                });
            }
            return this;
        }
    }

}
