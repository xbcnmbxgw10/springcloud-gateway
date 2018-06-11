package org.springcloud.gateway.core.commons.metrics.chooser;

import java.util.List;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.web.server.ServerWebExchange;
import org.springcloud.gateway.core.commons.metrics.stats.LoadBalancerStats;
import org.springcloud.gateway.core.commons.serv.CanaryLoadBalancerFilterFactory;

/**
 * Grayscale load balancer rule for weight-based least connections. </br>
 * This is a weighted concept more than the minimum number of connections, that
 * is, a weight value is added on the basis of the minimum number of
 * connections. When the number of connections is similar, the larger the weight
 * value is, the higher the priority is to assign requests.
 * 
 * @author springcloudgateway &lt;springcloudgateway@163.com, springcloudgateway@163.com&gt;
 * @version v1.0.0
 * @since v3.0.0
 */
public class WeightLeastConnCanaryLoadBalancerChooser extends LeastConnCanaryLoadBalancerChooser {

    @Override
    public LoadBalancerAlgorithm kind() {
        return LoadBalancerAlgorithm.WLC;
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