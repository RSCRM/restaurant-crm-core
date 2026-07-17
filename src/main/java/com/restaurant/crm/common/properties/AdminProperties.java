package com.restaurant.crm.common.properties;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "admin")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AdminProperties {
    String username;
    String password;
}
