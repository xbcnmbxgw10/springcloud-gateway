package org.springcloud.gateway.core.common.bean;

import org.springcloud.gateway.core.bean.BaseBean;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * {@link RealmBean}
 * 
 * @author springcloudgateway &lt;springcloudgateway@gmail.com&gt; * @version
 *         2022-03-30 v3.0.0
 * @since v3.0.0
 */
@Getter
@Setter
@ToString
@SuperBuilder
@NoArgsConstructor
public class RealmBean extends BaseBean {
    private static final long serialVersionUID = -9160052379887868918L;
    private String name;
    private String displayName;
    private Integer enable;
    private String deployFrontendUri;
    private Integer userRegistrationEnabled;
    private Integer forgotPasswordEnabled;
    private Integer rememberMeEnabled;
    private Integer emailLoginEnabled;
    private Integer smsLoginEnabled;
    private Integer editUsernameEnabled;
    private String securityDefenseJson;
    private String jwksJson;
}