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

import static org.springcloud.gateway.core.collection.CollectionUtils2.safeList;
import static org.springcloud.gateway.core.lang.DateUtils2.formatDate;
import static java.util.stream.Collectors.toList;
import static org.quartz.TriggerUtils.computeFireTimes;
import org.quartz.CronExpression;
import org.quartz.impl.triggers.CronTriggerImpl;

import java.text.ParseException;
import java.util.List;

/**
 * Quartz task cron expression utility.
 * 
 * @author springcloudgateway
 * @version v1.0.0
 * @since
 */
public abstract class QuartzCronUtils {

    /**
     * Check the expression is Valid
     */
    public static boolean isValidExpression(String expression) {
        return CronExpression.isValidExpression(expression);
    }

    /**
     * Get the expression next numTimes -- run time
     */
    public static List<String> getNextExecTime(String expression, Integer numTimes) {
        CronTriggerImpl cronTriggerImpl = new CronTriggerImpl();
        try {
            cronTriggerImpl.setCronExpression(expression);
        } catch (ParseException e) {
            throw new IllegalArgumentException(e);
        }
        return safeList(computeFireTimes(cronTriggerImpl, null, numTimes)).stream()
                .map(d -> formatDate(d, "yyyy-MM-dd HH:mm:ss"))
                .collect(toList());
    }

}