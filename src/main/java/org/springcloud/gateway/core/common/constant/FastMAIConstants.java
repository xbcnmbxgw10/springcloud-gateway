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
package org.springcloud.gateway.core.common.constant;

/**
 * IAM server constants.
 * 
 * @author springcloudgateway <springcloudgateway@gmail.com>
 * @version v1.0.0
 * @date 2018年11月13日
 * @since
 */
public abstract class FastMAIConstants extends SCGBaseConstants {

    //
    // --- IAM Common Constants. ---
    //

    /**
     * Of the fast-CAS attribute for remember me authentication (CAS 3.4.10+)
     */
    public static final String KEY_REMEMBERME_NAME = "remembermeAttrName";
    /**
     * Authentication principal language attribute name.
     */
    public static final String KEY_LANG_NAME = "langAttrName";
    /**
     * Authenticating host attribute name.
     */
    public static final String KEY_AUTHC_HOST_NAME = "authcHostAttrName";
    /**
     * This key is generated when the authentication is successful and can be
     * used to encrypt and decrypt the transmission data of some sensitive api.
     */
    public static final String KEY_DATA_CIPHER_NAME = "dataCipherKey";
    /**
     * When authentication is successful, a key for the access token is
     * generated. It is used to enhance session based validation logic (the
     * original idea came from JWT). In fact, it is the signature of hmacha1
     * ("signkey", sessionid + UMID). Validation logic: the signature value
     * calculated by the server is equal to the signature value submitted by the
     * client, that is, the validation is passed.
     * 
     * @see {@link org.springcloudgatewaydevops.iam.common.config.AbstractIamProperties.ParamProperties#accessTokenName}
     * @see {@link org.springcloudgatewaydevops.iam.common.mgt.IamSubjectFactory#assertRequestSignTokenValidity}
     */
    public static final String KEY_ACCESSTOKEN_SIGN_NAME = "accessTokenSignAttrName";
    /**
     * Iam-server/Iam-client parent sessionId.
     */
    public static final String KEY_PARENT_SESSIONID_NAME = "parentSessionIdAttrName";
    /** authentication token save session key-name */
    public static final String KEY_AUTHC_TOKEN = "authcTokenAttrName";
    /** authentication accountInfo save session key-name */
    public static final String KEY_AUTHC_ACCOUNT_INFO = "authcAccountInfoAttrName";

    /**
     * IAM Server/client the JSON node key that response the session
     * information.
     */
    public static final String KEY_SESSIONINFO_NAME = "session";
    /**
     * IAM system service role parameter name.</br>
     * Can be used for user-client interception of unregistered state
     * processing.
     */
    public static final String KEY_SERVICE_ROLE = "serviceRole";

    /**
     * IAM Client-server interactive authentication CAS protocol URI
     */
    public static final String URI_AUTHENTICATOR = "/authenticator";

    public static final String CACHE_PREFIX_SCG_FASTCAS = CACHE_PREFIX_IAM + "fastcas:";

    /**
     * {@link IamSession} relation attributes cache name.
     */
    public static final String CACHE_PREFIX_SCG_SESSION_REFATTRS = CACHE_PREFIX_SCG_FASTCAS + "session:refattrs:";

    /**
     * IAM replay attacks signature cache name.
     */
    public static final String CACHE_PREFIX_SCG_REPLAY_SIGN = CACHE_PREFIX_SCG_FASTCAS + "security:replaysign:";

    /**
     * IAM XSRF endpoint base URI.
     */
    public static final String URI_XSRF_BASE = "/xsrf";

    /**
     * IAM XSRF token apply URI.
     */
    public static final String URI_XSRF_APPLY_TOKEN = "xtoken";

    /**
     * SNS authorized info stoage attribute key. </br>
     * {@link SocialAuthorizeInfo}
     */
    public static final String KEY_SNS_AUTHORIZED_INFO = "snsAuthzInfoAttrName";

    /**
     * Validating scan iteration batch size.
     */
    public static final int DEFAULT_SESSION_SCAN_BATCHS = 10_000;

    //
    // --- IAM Server Constants. ---
    //

    /**
     * URI login submission base path for processing all shiro authentication
     * filters submitted by login.
     */
    public static final String URI_AUTH_BASE = "/auth/";
    /**
     * IAM server base URI. You need to ensure synchronization with the
     * configuration in bootstrap.yml [spring.cloud.devops.iam.filter-chain]
     */
    public static final String URI_IAM_SERVER_BASE = "/_fastcas-server";
    /** IAM server validate API URI. */
    public static final String URI_IAM_SERVER_VALIDATE = "validate";
    /** IAM server logout API URI. */
    public static final String URI_IAM_SERVER_LOGOUT = "logout";
    /** IAM server secondary authentication validate API URI. */
    public static final String URI_IAM_SERVER_SECOND_VALIDATE = "secondaryValidate";
    /** IAM server seesions authentication validate API URI. */
    public static final String URI_IAM_SERVER_SESSION_VALIDATE = "sessionValidate";

    /**
     * Callback Processing and Path in third party social networks services
     */
    public static final String URI_IAM_SERVER_SNS_BASE = "/sns";
    /**
     * SNS connect URI
     */
    public static final String URI_IAM_SERVER_SNS_CONNECT = "connect";
    /**
     * SNS connect callback URI
     */
    public static final String URI_IAM_SERVER_SNS_CALLBACK = "callback";
    /**
     * The callback proxy URI is suitable for the qq and sina authorized login
     * pages of front-end window.open, and the callback proxy processing pages
     * (closing the child forms and passing callback information to the parent
     * forms) when the authorization is successful.
     */
    public static final String URI_IAM_SERVER_AFTER_CALLBACK_AGENT = "after_callback_agent";

    /**
     * WeChat public platform social services receive message URI.
     */
    public static final String URI_IAM_SERVER_WECHAT_MP_RECEIVE = "receive";

    /** Based URI with login authenticator controller. */
    public static final String URI_IAM_SERVER_LOGIN_BASE = "/login";
    /**
     * Pre-processing handshake, e.g, apply sessionKeyId, All clients are
     * unified, including PC/WEB/iOS/Andriod/WechatMp/WechatApplet
     */
    public static final String URI_IAM_SERVER_LOGIN_HANDSHAKE = "handshake";
    /**
     * Initialization before login checks whether authentication code is
     * enabled, etc.
     */
    public static final String URI_IAM_SERVER_LOGIN_CHECK = "check";
    /** URI for apply for locale. */
    public static final String URI_IAM_SERVER_LOGIN_APPLY_LOCALE = "applylocale";
    /**
     * Gets the error information stored in the current session
     */
    public static final String URI_IAM_SERVER_LOGIN_ERRREAD = "errread";
    /**
     * Gets used for page Jump mode, to read authenticated roles/permissions/...
     * info.
     */
    public static final String URI_IAM_SERVER_LOGIN_PERMITS = "permits";

    /** Based URI with verifier authenticator controller. */
    public static final String URI_IAM_SERVER_VERIFY_BASE = "/verify";
    /** URI for apply for CAPTCHA. */
    public static final String URI_IAM_SERVER_VERIFY_APPLY_CAPTCHA = "applycaptcha";
    /** URI for verify analyze for CAPTCHA. */
    public static final String URI_IAM_SERVER_VERIFY_ANALYSIS_CAPTCHA = "verifyanalysis";
    /** URI for apply for verify-code. */
    public static final String URI_IAM_SERVER_VERIFY_SMS_APPLY = "applysmsverify";

    /** Based URI with simple risk control controller. */
    public static final String URI_IAM_SERVER_RCM_BASE = "/rcm";
    /**
     * Before requesting authentication, the client needs to submit the device
     * fingerprint um, UA and other information to obtain the corresponding
     * token, so as to solve the risk control detection. Note: it is a simple
     * version of the implementation of risk control inspection. It is
     * recommended to use a more professional external RiskControlService in the
     * production environment.
     */
    public static final String URI_IAM_SERVER_RCM_UMTOKEN_APPLY = "applyumtoken";

    /**
     * Generic API v1 base URL.
     */
    public static final String URI_IAM_SERVER_API_V2_BASE = URI_IAM_SERVER_BASE + "/api/v2";
    /**
     * Generic API v1 sessions list query.
     */
    public static final String URI_IAM_SERVER_API_V2_SESSION = "/sessions";

    /**
     * IAM client/server authentication session stored cache name.
     */
    public static final String CACHE_PREFIX_SCG_SESSION = CACHE_PREFIX_IAM + "session:id:";
    /**
     * IAM client authentication authorization information storage cache name.
     */
    public static final String CACHE_PREFIX_SCG_TICKET_CLIENT = CACHE_PREFIX_IAM + "ticket:c:";
    /**
     * IAM server authentication authorization information storage cache name.
     */
    public static final String CACHE_PREFIX_SCG_TICKET_SERVER = CACHE_PREFIX_IAM + "ticket:s:";

    /**
     * Login authentication related processing cache name.
     */
    public static final String CACHE_PREFIX_SCG_SNSAUTH = CACHE_PREFIX_IAM + "snsauth:";
    /**
     * IAM server matching CAPTCHA verification failure counter cache name.
     */
    public static final String CACHE_PREFIX_SCG_FAILFAST_COUNTER_CAPTCHA = CACHE_PREFIX_IAM + "counter:captcha:";
    /**
     * IAM server matching SMS verification failure counter cache name.
     */
    public static final String CACHE_PREFIX_SCG_FAILFAST_COUNTER_SMS = CACHE_PREFIX_IAM + "counter:sms:";
    /**
     * IAM server matching verification failure counter cache name.
     */
    public static final String CACHE_PREFIX_SCG_FAILFAST_COUNTER_MATCH = CACHE_PREFIX_IAM + "counter:credentials:";
    /**
     * Security verifier for jigsaw captcha image cache name.
     */
    public static final String CACHE_PREFIX_SCG_VERIFY_JIGSAW_IMG = CACHE_PREFIX_IAM + "verify:jigsaw:img";
    /**
     * Cryptographic service cache name.
     */
    public static final String CACHE_PREFIX_SCG_CRYPTO = CACHE_PREFIX_IAM + "crypto:keypairs";
    /** Simple risk control handler umidToken cache key. */
    public static final String CACHE_PREFIX_SCG_RCM_UMIDTOKEN = CACHE_PREFIX_IAM + "rcm:umidtoken:";

    /**
     * Login failure overrun, lock cache name.
     */
    public static final String LOCK_PREFIX_IAM_CREDENTIALS_MATCH = CACHE_PREFIX_IAM + "lock:credentials:";

    /**
     * The public key index by logged-in users
     */
    public static final String KEY_SECRET_INFO = "applySecretInfo";

    /**
     * Limiter login failure prefix based on user-name.
     */
    public static final String KEY_FAIL_LIMIT_UID_PREFIX = "uid_";
    /**
     * Limiter login failure prefix based on remote IP.
     */
    public static final String KEY_FAIL_LIMIT_RIP_PREFIX = "rip_";
    /**
     * Used for record all accounts that have failed to log in in this session.
     */
    public static final String KEY_FAIL_PRINCIPAL_FACTORS = "failPrincipalFactors";
    /**
     * Error information for saving iam-related operations to sessions.
     */
    public static final String KEY_ERR_SESSION_SAVED = "errorTipsInfo";
    /**
     * IAM system service role: iam-web.</br>
     * Can be used for user-client interception of unregistered state
     * processing.
     */
    public static final String KEY_SERVICE_ROLE_VALUE_IAMSERVER = "IamWithCasAppServer";

    /**
     * Delegate message source bean name.
     */
    public static final String BEAN_SESSION_RESOURCE_MSG_BUNDLER = "iamSessionResourceMessageBundler";

    //
    // --- IAM Client Constants. ---
    //

    /**
     * IAM system service role: iam-client.</br>
     * Can be used for user-client interception of unregistered state
     * processing.
     */
    public static final String KEY_SERVICE_ROLE_VALUE_IAMCLIENT = "IamWithCasAppClient";

    /**
     * Only one node of the cluster is required to run the client session
     * validity verification.
     */
    public static final String LOCK_PREFIX_IAM_SESSION_VALIDTING = CACHE_PREFIX_IAM + "c:validating:";

    /** Fast-CAS client base URI. */
    public static final String URI_IAM_CLIENT_BASE = "/_fastcas-client";
    /** Fast-CAS client logout URI. */
    public static final String URI_IAM_CLIENT_LOGOUT = "logout";

}