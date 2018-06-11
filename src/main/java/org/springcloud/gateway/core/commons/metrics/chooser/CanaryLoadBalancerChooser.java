package org.springcloud.gateway.core.commons.metrics.chooser;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.web.server.ServerWebExchange;
import org.springcloud.gateway.core.commons.serv.CanaryLoadBalancerFilterFactory;
import org.springcloud.gateway.core.framework.operator.Operator;

/**
 * {@link CanaryLoadBalancerChooser}
 * 
 * @author springcloudgateway &lt;springcloudgateway@163.com, springcloudgateway@163.com&gt;
 * @version v1.0.0
 * @since v3.0.0
 */
public interface CanaryLoadBalancerChooser extends Operator<CanaryLoadBalancerChooser.LoadBalancerAlgorithm> {

    ServiceInstance choose(CanaryLoadBalancerFilterFactory.Config config, ServerWebExchange exchange, String serviceId);

    /**
     * see:https://www.cnblogs.com/pengpengboshi/p/13278440.html
     */
    public static enum LoadBalancerAlgorithm {
        R, RR, WR, WRR, DH, SH, LC, LT, WLC, WLT
    }

}
