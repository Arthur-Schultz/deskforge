package com.deskforge.demo;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "deskforge.demo")
public record DemoProperties(boolean publicDemo, List<String> sharedAccounts) {

    public DemoProperties {
        sharedAccounts = sharedAccounts == null ? List.of() : List.copyOf(sharedAccounts);
    }
}
