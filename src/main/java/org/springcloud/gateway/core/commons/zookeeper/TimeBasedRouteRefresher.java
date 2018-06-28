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
package org.springcloud.gateway.core.commons.zookeeper;

import static org.springcloud.gateway.core.lang.Assert2.isTrue;
import static java.lang.String.format;
import static java.util.Objects.nonNull;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.util.concurrent.ScheduledFuture;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;

import org.springcloud.gateway.core.task.RunnerProperties;
import org.springcloud.gateway.core.commons.zookeeper.config.RouteProperties;
import org.springcloud.gateway.core.task.ApplicationTaskRunner;

/**
 * Gateway configuration refresh scheduler coordinator.
 *
 * @author springcloudgateway <springcloudgateway@163.com>
 * @version v1.0.0
 * @since v1.0
 */
public class TimeBasedRouteRefresher extends ApplicationTaskRunner<RunnerProperties> implements ApplicationRunner, DisposableBean {

    private @Autowired RouteProperties config;
    private @Autowired IRouteCacheRefresher refresher;

    /** Refreshing scheduled future */
    protected ScheduledFuture<?> future;

    public TimeBasedRouteRefresher() {
        super(new RunnerProperties().withConcurrency(1));
    }

    @Override
    public void destroy() throws Exception {
        super.close();
    }

    /**
     * Starting configuration refresher schedule.
     */
    @Override
    public void run() {
        refreshRouters();
    }

    /**
     * Restarting configuration refresher schedule.
     */
    public void restartRefresher() {
        isTrue(future.cancel(false), CurrentlyInRefreshingException.class,
                "Updating refreshDelayMs failed, because refreshing is currently in progressing");
        refreshRouters();
    }

    public IRouteCacheRefresher getRefresher() {
        return refresher;
    }

    /**
     * Creating or refreshing routers.
     * 
     * @throws IllegalStateException
     */
    private void refreshRouters() throws IllegalStateException {
        if (nonNull(future) && !future.isDone()) {
            throw new IllegalStateException(format("Cannot refresh routes, because already refreshing task. %s", future));
        }
        this.future = getWorker().scheduleWithFixedDelay(() -> {
            try {
                log.debug("Refreshing routes ...");
                refresher.refreshRoutes();
            } catch (Exception e) {
                log.error("Failed to refreshing routes.", e);
            }
        }, 1_000L, config.getRefreshDelayMs(), MILLISECONDS);
    }

}