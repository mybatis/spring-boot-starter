/*
 *    Copyright 2015-2024 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.mybatis.spring.boot.autoconfigure;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.ibatis.reflection.TypeParameterResolver;
import org.mybatis.spring.mapper.MapperFactoryBean;
import org.mybatis.spring.mapper.MapperScannerConfigurer;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.aot.BeanFactoryInitializationAotContribution;
import org.springframework.beans.factory.aot.BeanFactoryInitializationAotProcessor;
import org.springframework.beans.factory.aot.BeanRegistrationExcludeFilter;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.RegisteredBean;
import org.springframework.util.ReflectionUtils;

/**
 * @since 3.0.4
 */
class MyBatisBeanFactoryInitializationAotProcessor
    implements BeanFactoryInitializationAotProcessor, BeanRegistrationExcludeFilter {

  private static final Set<Class<?>> EXCLUDE_CLASSES = new HashSet<>();

  static {
    EXCLUDE_CLASSES.add(MapperScannerConfigurer.class);
  }

  @Override
  public BeanFactoryInitializationAotContribution processAheadOfTime(ConfigurableListableBeanFactory beanFactory) {
    String[] beanNames = beanFactory.getBeanNamesForType(MapperFactoryBean.class);
    if (beanNames.length == 0) {
      return null;
    }
    return (context, code) -> {
      RuntimeHints hints = context.getRuntimeHints();
      for (String beanName : beanNames) {
        BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanName.substring(1));
        PropertyValue mapperInterface = beanDefinition.getPropertyValues().getPropertyValue("mapperInterface");
        if (mapperInterface != null && mapperInterface.getValue() != null) {
          Class<?> mapperInterfaceType = (Class<?>) mapperInterface.getValue();
          if (mapperInterfaceType != null) {
            registerReflectionTypeIfNecessary(mapperInterfaceType, hints);
            hints.proxies().registerJdkProxy(mapperInterfaceType);
            registerMapperRelationships(mapperInterfaceType, hints);
          }
        }
      }
    };
  }

  @Override
  public boolean isExcludedFromAotProcessing(RegisteredBean registeredBean) {
    return EXCLUDE_CLASSES.contains(registeredBean.getBeanClass());
  }

  private void registerMapperRelationships(Class<?> mapperInterfaceType, RuntimeHints hints) {
    Method[] methods = ReflectionUtils.getAllDeclaredMethods(mapperInterfaceType);
    for (Method method : methods) {
      if (method.getDeclaringClass() != Object.class) {
        ReflectionUtils.makeAccessible(method);
        Class<?> returnType = MyBatisMapperTypeUtils.resolveReturnClass(mapperInterfaceType, method);
        registerReflectionTypeIfNecessary(returnType, hints);
        MyBatisMapperTypeUtils.resolveParameterClasses(mapperInterfaceType, method)
            .forEach(x -> registerReflectionTypeIfNecessary(x, hints));
      }
    }
  }

  static class MyBatisMapperTypeUtils {
    private MyBatisMapperTypeUtils() {
      // NOP
    }

    static Class<?> resolveReturnClass(Class<?> mapperInterface, Method method) {
      Type resolvedReturnType = TypeParameterResolver.resolveReturnType(method, mapperInterface);
      return typeToClass(resolvedReturnType, method.getReturnType());
    }

    static Set<Class<?>> resolveParameterClasses(Class<?> mapperInterface, Method method) {
      return Stream.of(TypeParameterResolver.resolveParamTypes(method, mapperInterface))
          .map(x -> typeToClass(x, x instanceof Class ? (Class<?>) x : Object.class)).collect(Collectors.toSet());
    }

    private static Class<?> typeToClass(Type src, Class<?> fallback) {
      Class<?> result = null;
      if (src instanceof Class<?>) {
        if (((Class<?>) src).isArray()) {
          result = ((Class<?>) src).getComponentType();
        } else {
          result = (Class<?>) src;
        }
      } else if (src instanceof ParameterizedType parameterizedType) {
        int index = (parameterizedType.getRawType() instanceof Class
            && Map.class.isAssignableFrom((Class<?>) parameterizedType.getRawType())
            && parameterizedType.getActualTypeArguments().length > 1) ? 1 : 0;
        Type actualType = parameterizedType.getActualTypeArguments()[index];
        result = typeToClass(actualType, fallback);
      }
      if (result == null) {
        result = fallback;
      }
      return result;
    }

  }

  private void registerReflectionTypeIfNecessary(Class<?> type, RuntimeHints hints) {
    if (!type.isPrimitive() && !type.getName().startsWith("java")) {
      hints.reflection().registerType(type, MemberCategory.values());
    }
  }

}
