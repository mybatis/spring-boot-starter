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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mybatis.spring.mapper.MapperFactoryBean;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.support.MergedBeanDefinitionPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.ResolvableType;
import org.springframework.util.ClassUtils;

/**
 * The {@code BeanDefinitionPostProcessor} for customizing a {@code MapperFactoryBean}.
 *
 * @author Stéphane Nicoll
 * @author Kazuki Shimizu
 * @author xuxiaowei
 *
 * @since 3.0.4
 */
@Configuration
class MyBatisMapperFactoryBeanPostProcessor implements MergedBeanDefinitionPostProcessor, BeanFactoryAware {

  private static final Log LOG = LogFactory.getLog(MyBatisMapperFactoryBeanPostProcessor.class);

  private static final String MAPPER_FACTORY_BEAN = MapperFactoryBean.class.getName();

  private ConfigurableBeanFactory beanFactory;

  @Override
  public void setBeanFactory(BeanFactory beanFactory) {
    this.beanFactory = (ConfigurableBeanFactory) beanFactory;
  }

  @Override
  public void postProcessMergedBeanDefinition(RootBeanDefinition beanDefinition, Class<?> beanType, String beanName) {
    if (ClassUtils.isPresent(MAPPER_FACTORY_BEAN, this.beanFactory.getBeanClassLoader())) {
      resolveMapperFactoryBeanTypeIfNecessary(beanDefinition);
    }
  }

  private void resolveMapperFactoryBeanTypeIfNecessary(RootBeanDefinition beanDefinition) {
    if (!beanDefinition.hasBeanClass() || !MapperFactoryBean.class.isAssignableFrom(beanDefinition.getBeanClass())) {
      return;
    }
    if (beanDefinition.getResolvableType().hasUnresolvableGenerics()) {
      Class<?> mapperInterface = getMapperInterface(beanDefinition);
      if (mapperInterface != null) {
        // Exposes a generic type information to context for prevent early initializing
        ConstructorArgumentValues constructorArgumentValues = new ConstructorArgumentValues();
        constructorArgumentValues.addGenericArgumentValue(mapperInterface);
        beanDefinition.setConstructorArgumentValues(constructorArgumentValues);
        beanDefinition
            .setTargetType(ResolvableType.forClassWithGenerics(beanDefinition.getBeanClass(), mapperInterface));
      }
    }
  }

  private Class<?> getMapperInterface(RootBeanDefinition beanDefinition) {
    try {
      return (Class<?>) beanDefinition.getPropertyValues().get("mapperInterface");
    } catch (Exception e) {
      LOG.debug("Fail getting mapper interface type.", e);
      return null;
    }
  }

}
