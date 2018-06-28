package org.springcloud.gateway.core.commons.zookeeper.config;

import org.springcloud.gateway.core.commons.zookeeper.Https2HttpGlobalFilter;

import lombok.Getter;
import lombok.Setter;

/**
 * {@link IamSecurityProperties}
 *
 * @author springcloudgateway<springcloudgateway@163.com>
 * @version v1.0.0
 * @since
 */
@Getter
@Setter
public class RouteProperties {

    /**
     * Enabled to https to http forward filter. {@link Https2HttpGlobalFilter}
     */
    private boolean forwaredHttpsToHttp = true;

    private Long refreshDelayMs = 30_000L;

}
