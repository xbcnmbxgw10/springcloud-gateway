/**
 * Check whether the current JVM process is started in the debugging mode of
 * jdwp, otherwise the condition is not tenable. {@link ConditionalOnJdwpDebug}
 * 
 * @author springcloudgateway <springcloudgateway@gmail.com>
 * @version v1.0.0
 * @since
 */
package org.springcloud.gateway.core.annotation.condition;

import static org.springcloud.gateway.core.actualtion.JvmRuntimeTool.isJvmInDebugging;
import static org.springcloud.gateway.core.annotation.condition.ConditionalOnJdwpDebug.ENABLE_PROPERTY;
import static org.springcloud.gateway.core.lang.Assert2.isTrue;
import static java.lang.String.format;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.type.AnnotatedTypeMetadata;

@Order(Ordered.HIGHEST_PRECEDENCE + 50)
class OnJdwpDebugCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        Object enablePropertyName = metadata.getAnnotationAttributes(ConditionalOnJdwpDebug.class.getName()).get(ENABLE_PROPERTY);
        isTrue(nonNull(enablePropertyName) && isNotBlank(enablePropertyName.toString()),
                format("%s.%s It shouldn't be empty", ConditionalOnJdwpDebug.class.getSimpleName(), ENABLE_PROPERTY));

        // Obtain environment enable property value.
        Boolean enable = context.getEnvironment().getProperty(enablePropertyName.toString(), Boolean.class);
        return isNull(enable) ? isJvmInDebugging : enable;
    }

}