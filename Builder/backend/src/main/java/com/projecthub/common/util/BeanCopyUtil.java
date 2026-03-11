package com.projecthub.common.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;

/** Bean 复制工具类 */
@Slf4j
public class BeanCopyUtil {

  /**
   * 复制单个对象
   *
   * @param source 源对象
   * @param target 目标对象类
   * @param <T> 目标类型
   * @return 复制后的对象
   */
  public static <T> T copyProperties(Object source, Class<T> target) {
    if (source == null) {
      return null;
    }
    try {
      T instance = target.getDeclaredConstructor().newInstance();
      BeanUtils.copyProperties(source, instance);
      return instance;
    } catch (Exception e) {
      log.error("Bean 复制失败", e);
      throw new RuntimeException("Bean 复制失败", e);
    }
  }

  /**
   * 复制列表
   *
   * @param sources 源对象列表
   * @param target 目标对象类
   * @param <T> 目标类型
   * @return 复制后的对象列表
   */
  public static <T> List<T> copyProperties(List<?> sources, Class<T> target) {
    if (sources == null || sources.isEmpty()) {
      return new ArrayList<>();
    }
    List<T> targets = new ArrayList<>(sources.size());
    for (Object source : sources) {
      targets.add(copyProperties(source, target));
    }
    return targets;
  }

  /**
   * 复制单个对象 (使用 Supplier)
   *
   * @param source 源对象
   * @param supplier 目标对象构造器
   * @param <T> 目标类型
   * @return 复制后的对象
   */
  public static <T> T copyProperties(Object source, Supplier<T> supplier) {
    if (source == null) {
      return null;
    }
    T instance = supplier.get();
    BeanUtils.copyProperties(source, instance);
    return instance;
  }

  /**
   * 复制列表 (使用 Supplier)
   *
   * @param sources 源对象列表
   * @param supplier 目标对象构造器
   * @param <T> 目标类型
   * @return 复制后的对象列表
   */
  public static <T, R> List<R> copyProperties(List<T> sources, Supplier<R> supplier) {
    if (sources == null || sources.isEmpty()) {
      return new ArrayList<>();
    }
    List<R> targets = new ArrayList<>(sources.size());
    for (T source : sources) {
      R instance = supplier.get();
      BeanUtils.copyProperties(source, instance);
      targets.add(instance);
    }
    return targets;
  }
}
