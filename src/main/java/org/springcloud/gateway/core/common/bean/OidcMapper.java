package org.springcloud.gateway.core.common.bean;

import org.springcloud.gateway.core.bean.BaseBean;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * {@link OidcClient}
 * 
 * @author springcloudgateway &lt;springcloudgateway@gmail.com&gt;
 * @version v1.0.0
 * @since v3.0.0
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class OidcMapper extends BaseBean {
    private static final long serialVersionUID = -343559094853261256L;

    private Long id;
    private String name;
    private String mapperType;
    private Integer addToIdToken;
    private Integer addToAccessToken;
    private Integer addUserinfo;
    private String valueJson;

    @Getter
    @Setter
    @SuperBuilder
    @NoArgsConstructor
    public static class BaseProtocolMapper {
        private Boolean isAddToAccessToken;
        private Boolean isAddToIdToken;
        private Boolean isAddToUserinfo;
    }

    @Getter
    @Setter
    @SuperBuilder
    @NoArgsConstructor
    public static class ClaimParameterToken extends BaseProtocolMapper {
    }

    @Getter
    @Setter
    @SuperBuilder
    @NoArgsConstructor
    public static class HardcodeRole extends BaseProtocolMapper {
        private String role;
    }

    @Getter
    @Setter
    @SuperBuilder
    @NoArgsConstructor
    public static class HardcodeClaim extends BaseProtocolMapper {
        private String claimJsonType;
        private String claimValue;
    }

    @Getter
    @Setter
    @SuperBuilder
    @NoArgsConstructor
    public static class UserAttribute extends BaseProtocolMapper {
        private String userAttribute;
        private String tokenClaimName;
        private String claimJsonType;
    }

    @Getter
    @Setter
    @SuperBuilder
    @NoArgsConstructor
    public static class UserAddress extends BaseProtocolMapper {
        private String street_address;
        private String locality;
        private String region;
        private String postal_code;
        private String country;
        private String formatted;
    }

}