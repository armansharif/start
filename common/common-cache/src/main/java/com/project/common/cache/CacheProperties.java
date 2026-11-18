package com.project.common.cache;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Cache configuration.
 *
 * <p>Caching can be switched off entirely (tests, single-node debugging) and each cache
 * can have its own TTL, because a permission lookup and a catalogue lookup do not age at
 * the same speed.</p>
 */
@ConfigurationProperties(prefix = "app.cache")
public class CacheProperties {

    private boolean enabled = true;

    /** Default time to live for caches without an explicit entry. */
    private Duration defaultTtl = Duration.ofMinutes(10);

    /** Prefix of every key; keeps environments sharing a Redis instance apart. */
    private String namespace = "backend-platform";

    /** Per cache TTL overrides, keyed by cache name. */
    private Map<String, Duration> ttls = new LinkedHashMap<>();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Duration getDefaultTtl() {
        return defaultTtl;
    }

    public void setDefaultTtl(Duration defaultTtl) {
        this.defaultTtl = defaultTtl;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public Map<String, Duration> getTtls() {
        return ttls;
    }

    public void setTtls(Map<String, Duration> ttls) {
        this.ttls = ttls;
    }

    public Duration ttlFor(String cacheName) {
        return ttls.getOrDefault(cacheName, defaultTtl);
    }
}
