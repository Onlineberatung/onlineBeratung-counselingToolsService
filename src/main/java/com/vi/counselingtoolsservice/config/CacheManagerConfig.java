package com.vi.counselingtoolsservice.config;

import net.sf.ehcache.config.CacheConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CacheManagerConfig {

  public static final String TOKEN_CACHE = "TOKEN_CACHE";

  @Bean
  public CacheManager cacheManager() {
    return new EhCacheCacheManager(ehCacheManager());
  }

  @Bean(destroyMethod = "shutdown")
  public net.sf.ehcache.CacheManager ehCacheManager() {
    var config = new net.sf.ehcache.config.Configuration();
    config.addCache(buildTokenCache());
    return net.sf.ehcache.CacheManager.newInstance(config);
  }

  private CacheConfiguration buildTokenCache() {
    var tokenCacheConfig = new CacheConfiguration();
    tokenCacheConfig.setName(TOKEN_CACHE);
    tokenCacheConfig.setEternal(false);
    tokenCacheConfig.setTimeToLiveSeconds(30);
    tokenCacheConfig.setTimeToIdleSeconds(30);
    tokenCacheConfig.setMaxBytesLocalHeap(1000000L);
    return tokenCacheConfig;
  }

}
