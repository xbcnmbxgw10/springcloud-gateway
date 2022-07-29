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
package org.springcloud.gateway.core.commons.security;

import static org.springcloud.gateway.core.collection.CollectionUtils2.safeList;
import static org.springcloud.gateway.core.lang.Assert2.notNullOf;
import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isBlank;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.support.HttpStatusHolder;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.server.ServerWebExchange;
import org.springcloud.gateway.core.commons.fault.IamGatewayFault;
import org.springcloud.gateway.core.commons.fault.IamGatewayFault.SafeFilterOrdered;
import org.springcloud.gateway.core.commons.microtag.GatewayMetricsFacade;
import org.springcloud.gateway.core.commons.microtag.GatewayMetricsFacade.MetricsName;
import org.springcloud.gateway.core.commons.microtag.GatewayMetricsFacade.MetricsTag;
<<<<<<< HEAD:src/main/java/org/springcloud/gateway/core/commons/security/RequestSecurityFilterFactory.java
import org.springcloud.gateway.core.commons.security.config.IPFSProperties;
import org.springcloud.gateway.core.commons.security.config.IPFSProperties.StrategyProperties;
=======
>>>>>>> 6ddba0b... FIX: optimization and suchs configurations:src/main/java/org/springcloud/gateway/core/commons/ipfs/SubnetFilterFactory.java
import org.springcloud.gateway.core.filter.CIDR;
import org.springcloud.gateway.core.bean.ConfigBeanUtils;

import io.netty.handler.ipfilter.IpFilterRuleType;
import io.netty.handler.ipfilter.IpSubnetFilterRule;
import io.netty.util.NetUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import reactor.core.publisher.Mono;

/**
 * {@link RequestSecurityFilterFactory}
 * 
 * @author springcloudgateway &lt;springcloudgateway@163.com, springcloudgateway@163.com&gt;
 * @version v1.0.0
 * @since v3.0.0
 * @see {@link io.netty.handler.ipfilter.IpSubnetFilterRule}
 */
public class RequestSecurityFilterFactory extends AbstractGatewayFilterFactory<RequestSecurityFilterFactory.Config> {

    private final IPFSProperties ipFilterConfig;
    private final GatewayMetricsFacade metricsFacade;

    public RequestSecurityFilterFactory(IPFSProperties ipListConfig, GatewayMetricsFacade metricsFacade) {
        super(RequestSecurityFilterFactory.Config.class);
        this.ipFilterConfig = notNullOf(ipListConfig, "ipListConfig");
        this.metricsFacade = notNullOf(metricsFacade, "metricsFacade");
    }

    @Override
    public String name() {
        return BEAN_NAME;
    }

    @Override
    public GatewayFilter apply(Config config) {
        applyDefaultToConfig(config);
        return new IpSubnetGatewayFilter(config.validate());
    }

    private void applyDefaultToConfig(Config config) {
        try {
            ConfigBeanUtils.configureWithDefault(new Config(), config, ipFilterConfig.getDefaultStrategy());
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Creates InetSocketAddress instance. Numeric IP addresses will be detected
     * and resolved without doing reverse DNS lookups.
     *
     * @param hostname
     *            ip-address or hostname
     * @param port
     *            port number
     * @param resolve
     *            when true, resolve given hostname at instance creation time
     * @return InetSocketAddress for given parameters
     */
    public static InetSocketAddress createInetSocketAddress(String hostname, int port, boolean resolve) {
        InetAddress inetAddressForIpString = null;
        byte[] ipAddressBytes = NetUtil.createByteArrayFromIpAddressString(hostname);
        if (ipAddressBytes != null) {
            try {
                if (ipAddressBytes.length == 4) {
                    inetAddressForIpString = Inet4Address.getByAddress(ipAddressBytes);
                } else {
                    inetAddressForIpString = Inet6Address.getByAddress(null, ipAddressBytes, -1);
                }
            } catch (UnknownHostException e) {
                throw new RuntimeException(e); // Should never happen
            }
        }
        if (inetAddressForIpString != null) {
            return new InetSocketAddress(inetAddressForIpString, port);
        } else {
            return resolve ? new InetSocketAddress(hostname, port) : InetSocketAddress.createUnresolved(hostname, port);
        }
    }

    @Getter
    @Setter
    @Validated
    @ToString
    public static class Config extends StrategyProperties {
        public Config validate() {
            // Validate CIDRs.
            safeList(getIPSubnets()).forEach(sn -> sn.validate());
            return this;
        }
    }

    @AllArgsConstructor
    class IpSubnetGatewayFilter implements GatewayFilter, Ordered {
        private final Config config;

        @Override
        public int getOrder() {
            return SafeFilterOrdered.ORDER_IPFILTER;
        }

        @Override
        public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
            // Add metrics of total.
            metricsFacade.counter(exchange, MetricsName.IPFILTER_TOTAL, 1, MetricsTag.ROUTE_ID,
                    IamGatewayFault.getRouteId(exchange));

            if (isAllowed(config, exchange)) {
                return chain.filter(exchange);
            }

            // Add metrics of hits total.
            metricsFacade.counter(exchange, MetricsName.IPFILTER_HITS_TOTAL, 1, MetricsTag.ROUTE_ID,
                    IamGatewayFault.getRouteId(exchange));

            // Response of reject.
            ServerWebExchangeUtils.setResponseStatus(exchange, HttpStatusHolder.parse(config.getStatusCode()));
            return exchange.getResponse().setComplete();
        }

        private boolean isAllowed(Config config, ServerWebExchange exchange) {
            // Determine remote client address.
            // Note:This method does not send network resolutions
            InetSocketAddress remoteAddress = createInetSocketAddress(getClientAddress(config, exchange), 0, false);

            // The local address is allowed to pass by default.
            InetAddress address = remoteAddress.getAddress();
            if (config.isAnyLocalAddressAllowed()
                    && (address.isAnyLocalAddress() || address.isLinkLocalAddress() || address.isLoopbackAddress())) {
                return true;
            }

            // Check if it is allowed to pass.
            //
            // matching white-list
            boolean isAccept = safeList(config.getIPSubnets()).stream()
                    .filter(s -> s.isAllow())
                    .flatMap(s -> safeList(s.getCidrs()).stream())
                    .anyMatch(cidr -> buildRule(cidr, IpFilterRuleType.ACCEPT).matches(remoteAddress));

            // matching blacklist
            boolean isReject = safeList(config.getIPSubnets()).stream()
                    .filter(s -> !s.isAllow())
                    .flatMap(s -> safeList(s.getCidrs()).stream())
                    .anyMatch(cidr -> buildRule(cidr, IpFilterRuleType.REJECT).matches(remoteAddress));

            // If none of the conditions are met, allow access.
            boolean allowed = isAccept && !isReject;
            if (!isAccept && isReject) {
                allowed = false;
            } else if (isAccept && isReject) {
                // Whether to use the blacklist first in case of conflict?
                allowed = !config.isPreferRejectOnCidrConflict();
            } else if (!isAccept && !isReject) {
                allowed = config.isAcceptNotMatchCidr();
            }
            return allowed;
        }

        private String getClientAddress(Config config, ServerWebExchange exchange) {
            HttpHeaders headers = exchange.getRequest().getHeaders();
            String host = null;
            for (String header : config.getForwardHeaderNames()) {
                host = headers.getFirst(header);
                if (!isBlank(host) && !"Unknown".equalsIgnoreCase(host)) {
                    return host;
                }
            }
            // Fall-back
            return exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();
        }

        private IpSubnetFilterRule buildRule(String cidr, IpFilterRuleType ruleType) {
            try {
                CIDR _cidr = CIDR.newCIDR(cidr);
                return new IpSubnetFilterRule(_cidr.getBaseAddress(), _cidr.getMask(), ruleType);
            } catch (UnknownHostException e) {
                throw new IllegalArgumentException(format("Failed to parse cidr for '%s'", cidr));
            }
        }

    }

    public static final String BEAN_NAME = "IpFilter";

}
