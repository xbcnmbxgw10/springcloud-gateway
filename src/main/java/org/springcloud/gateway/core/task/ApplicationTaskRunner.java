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
package org.springcloud.gateway.core.task;

import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

import org.springcloud.gateway.core.task.GenericTaskRunner;
import org.springcloud.gateway.core.task.RunnerProperties;
import org.springcloud.gateway.core.task.SafeScheduledTaskPoolExecutor;

/**
 * Application generic local scheduler & task runner.
 * 
 * @author springcloudgateway <springcloudgateway@gmail.com>
 * @version v1.0.0
 * @since
 * @see {@link org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler}
 * @see <a href=
 *      "http://www.doc88.com/p-3922316178617.html">ScheduledThreadPoolExecutor
 *      Retry task OOM resolution</a>
 */
public abstract class ApplicationTaskRunner<C extends RunnerProperties> extends GenericTaskRunner<C>
        implements ApplicationRunner, DisposableBean {

    private final AtomicBoolean started = new AtomicBoolean(false);

    public ApplicationTaskRunner() {
        super();
    }

    public ApplicationTaskRunner(C config) {
        super(config);
    }

    @Override
    public void destroy() throws Exception {
        super.close();
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (started.compareAndSet(false, true)) {
            super.start();
            onApplicationStarted(args, getWorker());
        }
    }

    protected void onApplicationStarted(ApplicationArguments args, SafeScheduledTaskPoolExecutor worker) throws Exception {
    }

}