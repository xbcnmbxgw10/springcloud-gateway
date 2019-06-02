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
package org.springcloud.gateway.core.common.i18n;

import static org.springcloud.gateway.core.common.constant.FastMAIConstants.KEY_LANG_NAME;

import java.util.Locale;
import java.util.Objects;

import org.springcloud.gateway.core.bridge.IamSecurityHolderBridges;
import org.springcloud.gateway.core.i18n.AbstractResourceMessageBundler;

/**
 * Session delegate resource bundle message source.
 *
 * @author springcloudgateway <springcloudgateway@gmail.com>
 * @version v1.0.0
 * @since
 */
public class SessionResourceMessageBundler extends AbstractResourceMessageBundler {

    public SessionResourceMessageBundler(Class<?> withClassPath) {
        super(withClassPath);
    }

    @Override
    protected Locale getSessionLocale() {
        Object loc = IamSecurityHolderBridges.invokeGetBindValue(KEY_LANG_NAME);
        Locale locale = null;
        if (loc instanceof Locale) {
            locale = (Locale) loc;
        } else if (loc instanceof String) {
            locale = new Locale((String) loc);
        }
        return Objects.isNull(locale) ? Locale.SIMPLIFIED_CHINESE : locale;
    }

}