package org.springcloud.gateway.core.commons.metrics.chooser;

import java.util.List;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.web.server.ServerWebExchange;
import org.springcloud.gateway.core.commons.metrics.stats.LoadBalancerStats;
import org.springcloud.gateway.core.commons.serv.CanaryLoadBalancerFilterFactory;

/**
 * Grayscale load balancer rule for weight-based least response time.
 * 
 * @author springcloudgateway &lt;springcloudgateway@163.com, springcloudgateway@163.com&gt;
 * @version v1.0.0
 * @since v3.0.0
 * @see {@link com.netflix.loadbalancer.WeightedResponseTimeRule}
 */
public class WeightLeastTimeCanaryLoadBalancerChooser extends LeastConnCanaryLoadBalancerChooser {

    @Override
    public LoadBalancerAlgorithm kind() {
        return LoadBalancerAlgorithm.WLT;
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