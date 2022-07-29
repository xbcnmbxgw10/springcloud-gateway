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
package org.springcloud.gateway.core.web.matcher;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableList;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.StringUtils.contains;
import static org.apache.commons.lang3.StringUtils.containsIgnoreCase;
import static org.apache.commons.lang3.StringUtils.endsWith;
import static org.apache.commons.lang3.StringUtils.endsWithIgnoreCase;
import static org.apache.commons.lang3.StringUtils.equalsAny;
import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.startsWith;
import static org.apache.commons.lang3.StringUtils.trimToEmpty;
import static org.springcloud.gateway.core.collection.CollectionUtils2.safeList;
import static org.springcloud.gateway.core.lang.Assert2.hasText;
import static org.springcloud.gateway.core.lang.Assert2.hasTextOf;
import static org.springcloud.gateway.core.lang.Assert2.isTrue;
import static org.springcloud.gateway.core.lang.Assert2.notNull;
import static org.springcloud.gateway.core.lang.Assert2.notNullOf;
import static org.springframework.util.CollectionUtils.isEmpty;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;

import javax.annotation.Nullable;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;
import org.springcloud.gateway.core.bean2.BeanCopierUtils;
import org.springcloud.gateway.core.bean2.BeanMapConvert;
import org.springcloud.gateway.core.io.ByteStreamUtils;
import org.springcloud.gateway.core.kit.ProcessUtils;
import org.springcloud.gateway.core.math.Maths;
import org.springcloud.gateway.core.nlp.PingyUtils;
import org.springcloud.gateway.core.resource.ResourceUtils2;
import org.springcloud.gateway.core.socks.EasyExcelServices;
import org.springcloud.gateway.core.tools.JvmRuntimeTool;
import org.springcloud.gateway.core.utils.AopUtils2;
import org.springcloud.gateway.core.utils.PropertySources;
import org.springcloud.gateway.core.utils.expression.SpelExpressions;
import org.springcloud.gateway.core.utils.image.PixelUtils;
import org.springcloud.gateway.core.utils.web.WebUtils3;
import org.springcloud.gateway.core.validation.BindingResultUtils2;
import org.springcloud.gateway.core.view.Freemarkers;
import org.springcloud.gateway.core.web.WebUtils.WebRequestExtractor;
import org.springframework.expression.EvaluationException;
import org.springframework.util.AntPathMatcher;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.With;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;

/**
 * {@link SpelRequestMatcher}
 * 
 * @author springcloudgateway &lt;springcloudgateway@gmail.com&gt;
 * @version v1.0.0
 * @since v3.0.0
 */
@SuppressWarnings("deprecation")
@Getter
@Slf4j
public class SpelRequestMatcher {

    private @NotBlank static SpelExpressions SPEL;
    private @Nullable final List<MatchHttpRequestRule> ruleDefinitions;
    private @Nullable final Map<String, Supplier<Predicate<String>>> defaultExtraPredicateSupplierVariables;

    /**
     * Construction {@link SpelRequestMatcher}
     * 
     * @param ruleDefinitions
     *            The rule set definition to match.
     */
    public SpelRequestMatcher(List<MatchHttpRequestRule> ruleDefinitions) {
        this(ruleDefinitions, null);
    }

    /**
     * Construction {@link SpelRequestMatcher}
     * 
     * @param ruleDefinitions
     *            The rule set definition to match.
     * @param defaultExtraPredicateSupplierVariables
     *            The defaults extended predicate provider list. as such:
     *            support getting data in request.getAttributes in SPEL
     *            expressions.
     */
    public SpelRequestMatcher(List<MatchHttpRequestRule> ruleDefinitions,
            Map<String, Supplier<Predicate<String>>> defaultExtraPredicateSupplierVariables) {
        this.ruleDefinitions = isEmpty(ruleDefinitions) ? emptyList() : ruleDefinitions;
        this.ruleDefinitions.forEach(rule -> rule.validate()); // Validation
        this.defaultExtraPredicateSupplierVariables = isEmpty(defaultExtraPredicateSupplierVariables) ? emptyMap()
                : defaultExtraPredicateSupplierVariables;
    }

    /**
     * Finds a collection of all rule names that match the expectations of a
     * SPEL expression based on request information.
     * 
     * @param extractor
     *            Wrapper for extracting request parameters
     * @param expression
     *            SPEL expression used to match the request
     * @return Returns true if the request satisfies the matching SPEL
     *         expression, otherwise returns false.
     */
    public List<MatchHttpRequestRule> find(@NotNull WebRequestExtractor extractor, @NotBlank String expression) {
        return find(extractor, expression, null);
    }

    /**
     * Finds a collection of all rule names that match the expectations of a
     * SPEL expression based on request information.
     * 
     * @param extractor
     *            Wrapper for extracting request parameters
     * @param expression
     *            SPEL expression used to match the request
     * @param extraPredicateSupplierVariables
     *            The temporary extended predicate provider list. as such:
     *            support getting data in request.getAttributes in SPEL
     *            expressions.
     * @return Returns true if the request satisfies the matching SPEL
     *         expression, otherwise returns false.
     */
    public List<MatchHttpRequestRule> find(
            @NotNull WebRequestExtractor extractor,
            @NotBlank String expression,
            @Nullable Map<String, Supplier<Predicate<String>>> extraPredicateSupplierVariables) {
        notNullOf(extractor, "extractor");
        hasTextOf(expression, "expression");

        // Add '$' prefix to build-in rules variables.
        Map<String, Object> model = safeList(ruleDefinitions).stream().collect(toMap(r -> "$".concat(r.getName()), r -> r));

        // Add '$' prefix to build-in request variables.
        model.put("$".concat(SPEL_KEYWORDS_REQUEST), extractor);

        // Add '$' prefix to default build-in extension predicate variables
        // supplier.
        defaultExtraPredicateSupplierVariables.forEach((varName, supplier) -> {
            if (nonNull(model.putIfAbsent("$".concat(varName), supplier))) {
                throw new IllegalArgumentException(
                        format("Already exists for add built-in supplier variable name '%s'.", varName));
            }
        });

        // Add '$' prefix to temporary build-in extension predicate variables
        // supplier.
        if (nonNull(extraPredicateSupplierVariables)) {
            extraPredicateSupplierVariables.forEach((varName, supplier) -> {
                if (nonNull(model.putIfAbsent("$".concat(varName), supplier))) {
                    throw new IllegalArgumentException(
                            format("Already exists for add temporary built-in supplier variable name '%s'.", varName));
                }
            });
        }

        // do request matching.
        List<MatchHttpRequestRule> result = ruleDefinitions.stream().filter(e -> {
            model.put("$".concat(SPEL_KEYWORDS_RULE), e);
            return SPEL.resolve(expression, model);
        }).collect(toList());

        return unmodifiableList(result);
    }

    /**
     * The whether the match according to the request information satisfies the
     * SPEL expression.
     * 
     * @param extractor
     *            Wrapper for extracting request parameters
     * @param expression
     *            SPEL expression used to match the request
     * @return Returns true if the request satisfies the matching SPEL
     *         expression, otherwise returns false.
     * @return
     */
    public boolean matches(@NotNull WebRequestExtractor extractor, @NotBlank String expression) {
        return matches(extractor, expression, null);
    }

    /**
     * The whether the match according to the request information satisfies the
     * SPEL expression.
     * 
     * @param extractor
     *            Wrapper for extracting request parameters
     * @param expression
     *            SPEL expression used to match the request
     * @param extraPredicateSupplierVariables
     *            The temporary extended predicate provider list. as such:
     *            support getting data in request.getAttributes in SPEL
     *            expressions.
     * @return Returns true if the request satisfies the matching SPEL
     *         expression, otherwise returns false.
     */
    public boolean matches(
            @NotNull WebRequestExtractor extractor,
            @NotBlank String expression,
            @Nullable Map<String, Supplier<Predicate<String>>> extraPredicateSupplierVariables) {
        notNullOf(extractor, "extractor");
        hasTextOf(expression, "expression");

        // Add '$' prefix to build-in request and rules variables.
        Map<String, Object> model = ruleDefinitions.stream().collect(toMap(r -> "$".concat(r.getName()), r -> r));
        model.put("$".concat(SPEL_KEYWORDS_REQUEST), extractor);
        model.put("$".concat(SPEL_KEYWORDS_RULES), ruleDefinitions.stream().collect(toMap(r -> r.getName(), r -> r)));

        // Add '$' prefix to default build-in extension predicate variables
        // supplier.
        defaultExtraPredicateSupplierVariables.forEach((varName, supplier) -> {
            if (nonNull(model.putIfAbsent("$".concat(varName), supplier))) {
                throw new IllegalArgumentException(
                        format("Already exists for add built-in supplier variable name '%s'.", varName));
            }
        });

        // Add '$' prefix to temporary build-in extension predicate variables
        // supplier.
        if (nonNull(extraPredicateSupplierVariables)) {
            extraPredicateSupplierVariables.forEach((varName, supplier) -> {
                if (nonNull(model.putIfAbsent("$".concat(varName), supplier))) {
                    throw new IllegalArgumentException(
                            format("The already exists for add temporary built-in supplier variable name '%s'.", varName));
                }
            });
        }

        try {
            return SPEL.resolve(expression, model);
        } catch (EvaluationException e) {
            String errmsg = format("Cannot evaluate expression: '%s'", expression);
            log.error(errmsg, e);
            throw new EvaluationException(errmsg, e);
        }
    }

    /**
     * @see {@link org.springframework.web.reactive.function.server.RequestPredicates}
     */
    @With
    @Getter
    @Setter
    @SuperBuilder
    @AllArgsConstructor
    public static class MatchHttpRequestRule implements Predicate<WebRequestExtractor> {

        /**
         * The name of the matching rule (required).
         */
        private @NotBlank String name;

        /**
         * This option is to specify the way to combine the matching result of
         * the header with the matching result of the query parameter. By
         * default, or is used. </br>
         * It does not affect the use of this merge result with schema, host,
         * port, etc. and the final merge method.
         */
        // private boolean orMatchHeaderQuery;

        /**
         * (Optional)The used to match the current request HTTP method. </br>
         * for example: POST
         */
        private @Nullable String method;

        /**
         * (Optional) The used to match the current request HTTP schema. </br>
         * for example: https://
         */
        private @Nullable String scheme;

        /**
         * (Optional) The used to match the current request HTTP host. </br>
         * for example: example.com
         */
        private @Nullable String host;

        /**
         * (Optional)The used to match the current request HTTP port. </br>
         * for example: 443
         */
        private @Nullable Integer port;

        /**
         * (Optional)The used to match the current request HTTP ant path
         * pattern. </br>
         * for example: /foo/bar/list
         */
        private @Nullable String path;

        /**
         * (Optional) The name-value used to match the current request HTTP
         * header.
         */
        private @Nullable MatchProperty header;

        /**
         * (Optional) The name-value used to match the current request HTTP
         * cookie.
         */
        private @Nullable MatchProperty cookie;

        /**
         * (Optional) The name-value used to match the current request HTTP
         * query parameter. </br>
         * for example: __iam_gateway_log
         */
        private @Nullable MatchProperty query;

        //
        // Temporary fields.
        //
        @Getter(AccessLevel.NONE)
        @Setter(AccessLevel.NONE)
        private AntPathMatcher pathMatcher;

        public MatchHttpRequestRule() {
            // this.orMatchHeaderQuery = true;
            this.pathMatcher = new AntPathMatcher("/");
        }

        @Override
        public boolean test(WebRequestExtractor extractor) {
            return doMatchRequest(extractor);
        }

        private boolean doMatchRequest(@NotNull WebRequestExtractor extractor) {
            notNullOf(extractor, "extractor");

            // Match HTTP method
            boolean flagMethod = isBlank(getMethod());
            if (!flagMethod && equalsIgnoreCase(extractor.getMethod(), getMethod())) {
                flagMethod = true;
            }
            // Match HTTP schema
            boolean flagSchema = isBlank(getScheme());
            if (!flagSchema && equalsIgnoreCase(extractor.getScheme(), getScheme())) {
                flagSchema = true;
            }
            // Match HTTP host
            boolean flagHost = isBlank(getHost());
            if (!flagHost && equalsIgnoreCase(extractor.getHost(), getHost())) {
                flagHost = true;
            }
            // Match HTTP port
            boolean flagPort = (isNull(getPort()) || getPort() <= 0);
            if (!flagPort && equalsIgnoreCase(extractor.getPort() + "", getPort() + "")) {
                flagPort = true;
            }
            // Match HTTP path
            boolean flagPath = isBlank(getPath());
            if (!flagPath && pathMatcher.matchStart(getPath(), extractor.getPath())) {
                flagPath = true;
            }
            // Match HTTP headers.
            boolean flagHeader = isNull(getHeader());
            if (!flagHeader && getHeader().getSymbol().getFunction().apply(
                    trimToEmpty(extractor.getHeaderValue(getHeader().getKey())), getHeader().getValue())) {
                flagHeader = true;
            }
            // Match HTTP cookies.
            boolean flagCookie = isNull(getCookie());
            if (!flagCookie && getCookie().getSymbol().getFunction().apply(
                    trimToEmpty(extractor.getCookieValue(getCookie().getKey())), getCookie().getValue())) {
                flagCookie = true;
            }
            // Match HTTP query parameter.
            boolean flagQuery = isNull(getQuery());
            if (!flagQuery && getQuery().getSymbol().getFunction().apply(
                    trimToEmpty(extractor.getQueryValue(getQuery().getKey())), getQuery().getValue())) {
                flagQuery = true;
            }
            // if (isOrMatchHeaderQuery()) {
            // return flagMethod && flagSchema && flagHost && flagPort &&
            // flagPath && (flagHeader || flagQuery);
            // }
            return flagMethod && flagSchema && flagHost && flagPort && flagPath && flagHeader && flagCookie && flagQuery;
        }

        public MatchHttpRequestRule validate() {
            // Validation for name.
            hasTextOf(getName(), "rule name is required");
            isTrue(!equalsAny(getName(), SPEL_KEYWORDS_REQUEST, SPEL_KEYWORDS_RULES, SPEL_KEYWORDS_RULE),
                    "Invalid rule name '%s', Cannot conflict with built-in keywords", getName());
            // Validation for header.
            if (nonNull(getHeader())) {
                getHeader().validate();
            }
            // Validation for query.
            if (nonNull(getQuery())) {
                getQuery().validate();
            }
            return this;
        }

        @Override
        public String toString() {
            return "MatchHttpRequestRule [name=" + name + ", method=" + method + ", scheme=" + scheme + ", host=" + host
                    + ", port=" + port + ", path=" + path + ", header=" + header + ", cookie=" + cookie + ", query=" + query
                    + ", pathMatcher=" + pathMatcher + "]";
        }
    }

    @With
    @Getter
    @Setter
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MatchProperty {

        /**
         * For example, the matching mode used to match the current request
         * parameter value prefix,suffix,include etc.
         */
        private @NotNull MatchSymbol symbol = MatchSymbol.EQ;

        /**
         * Object key.
         */
        private @NotBlank String key;

        /**
         * Object value.
         */
        private @NotBlank String value;

        public MatchProperty validate() {
            notNull(symbol, "matchMode is required");
            hasText(key, "key is required");
            hasText(value, "value is required");
            return this;
        }
    }

    @Getter
    @AllArgsConstructor
    public static enum MatchSymbol {
        EQ((v1, v2) -> StringUtils.equals(v1, v2)),

        IGNORECASE_EQ((v1, v2) -> equalsIgnoreCase(v1, v2)),

        PREFIX((v1, v2) -> startsWith(v1, v2)),

        IGNORECASE_PREFIX((v1, v2) -> endsWithIgnoreCase(v1, v2)),

        SUFFIX((v1, v2) -> endsWith(v1, v2)),

        IGNORECASE_SUFFIX((v1, v2) -> equalsIgnoreCase(v1, v2)),

        INCLUDE((v1, v2) -> contains(v1, v2)),

        IGNORECASE_INCLUDE((v1, v2) -> containsIgnoreCase(v1, v2));

        private final BiFunction<String, String, Boolean> function;
    }

    public static final String SPEL_KEYWORDS_REQUEST = "request";
    public static final String SPEL_KEYWORDS_RULES = "rules";
    public static final String SPEL_KEYWORDS_RULE = "rule";
    public static final List<Class<?>> SPEL_KEYWORDS_CLASS_BUILDIN = asList(Maths.class, PingyUtils.class, AopUtils2.class,
            PropertySources.class, ProcessUtils.class, BeanCopierUtils.class, BeanMapConvert.class, WebUtils3.class,
            PixelUtils.class, Freemarkers.class, BindingResultUtils2.class, ResourceUtils2.class, EasyExcelServices.class,
            JvmRuntimeTool.class, ByteStreamUtils.class);
    static {
        SPEL = SpelExpressions.create(SPEL_KEYWORDS_CLASS_BUILDIN.toArray(new Class[0]));
    }

}
