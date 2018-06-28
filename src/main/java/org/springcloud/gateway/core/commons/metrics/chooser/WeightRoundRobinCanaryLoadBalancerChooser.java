package org.springcloud.gateway.core.commons.metrics.chooser;

import java.util.List;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.web.server.ServerWebExchange;
import org.springcloud.gateway.core.commons.metrics.stats.LoadBalancerStats;
import org.springcloud.gateway.core.commons.serv.CanaryLoadBalancerFilterFactory;

/**
 * Grayscale load balancer rule for weight-based round-Robin.
 * 
 * @author springcloudgateway &lt;springcloudgateway@163.com, springcloudgateway@163.com&gt;
 * @version v1.0.0
 * @since v3.0.0
 * @see {@link org.springframework.cloud.loadbalancer.core.RoundRobinLoadBalancer}
 */
public class WeightRoundRobinCanaryLoadBalancerChooser extends RoundRobinCanaryLoadBalancerChooser {

    @Override
    public LoadBalancerAlgorithm kind() {
        return LoadBalancerAlgorithm.WRR;
    }

    @Override
    protected ServiceInstance doChooseInstance(
            CanaryLoadBalancerFilterFactory.Config config,
            ServerWebExchange exchange,
            LoadBalancerStats stats,
            String serviceId,
            List<ServiceInstance> candidateInstances) {

        // TODO

        return super.doChooseInstance(config, exchange, stats, serviceId, candidateInstances);
    }

}