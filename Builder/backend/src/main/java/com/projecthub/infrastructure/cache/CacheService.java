package com.projecthub.infrastructure.cache;

import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/** 缓存服务 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CacheService {

  private final RedisTemplate<String, Object> redisTemplate;

  /** 缓存用户信息 */
  private static final String USER_CACHE_PREFIX = "user:";

  /** 缓存项目信息 */
  private static final String PROJECT_CACHE_PREFIX = "project:";

  /** 缓存任务信息 */
  private static final String TASK_CACHE_PREFIX = "task:";

  /** 缓存用户信息 */
  public void cacheUser(Long userId, Object userInfo, long ttl, TimeUnit unit) {
    String key = USER_CACHE_PREFIX + userId;
    redisTemplate.opsForValue().set(key, userInfo, ttl, unit);
    log.debug("缓存用户信息：userId={}, ttl={}", userId, ttl);
  }

  /** 获取缓存的用户信息 */
  public Object getCachedUser(Long userId) {
    String key = USER_CACHE_PREFIX + userId;
    return redisTemplate.opsForValue().get(key);
  }

  /** 删除缓存的用户信息 */
  public void evictUserCache(Long userId) {
    String key = USER_CACHE_PREFIX + userId;
    redisTemplate.delete(key);
    log.debug("删除用户缓存：userId={}", userId);
  }

  /** 缓存项目信息 */
  public void cacheProject(Long projectId, Object projectInfo, long ttl, TimeUnit unit) {
    String key = PROJECT_CACHE_PREFIX + projectId;
    redisTemplate.opsForValue().set(key, projectInfo, ttl, unit);
    log.debug("缓存项目信息：projectId={}, ttl={}", projectId, ttl);
  }

  /** 获取缓存的项目信息 */
  public Object getCachedProject(Long projectId) {
    String key = PROJECT_CACHE_PREFIX + projectId;
    return redisTemplate.opsForValue().get(key);
  }

  /** 删除缓存的项目信息 */
  public void evictProjectCache(Long projectId) {
    String key = PROJECT_CACHE_PREFIX + projectId;
    redisTemplate.delete(key);
    log.debug("删除项目缓存：projectId={}", projectId);
  }

  /** 缓存任务信息 */
  public void cacheTask(Long taskId, Object taskInfo, long ttl, TimeUnit unit) {
    String key = TASK_CACHE_PREFIX + taskId;
    redisTemplate.opsForValue().set(key, taskInfo, ttl, unit);
    log.debug("缓存任务信息：taskId={}, ttl={}", taskId, ttl);
  }

  /** 获取缓存的任务信息 */
  public Object getCachedTask(Long taskId) {
    String key = TASK_CACHE_PREFIX + taskId;
    return redisTemplate.opsForValue().get(key);
  }

  /** 删除缓存的任务信息 */
  public void evictTaskCache(Long taskId) {
    String key = TASK_CACHE_PREFIX + taskId;
    redisTemplate.delete(key);
    log.debug("删除任务缓存：taskId={}", taskId);
  }

  /** 清除项目相关缓存（包括项目和任务） */
  public void evictProjectRelatedCache(Long projectId) {
    // 清除项目缓存
    evictProjectCache(projectId);
    // TODO: 清除项目下所有任务的缓存
    log.info("清除项目相关缓存：projectId={}", projectId);
  }
}
