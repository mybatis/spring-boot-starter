/**
 *    Copyright 2015-2019 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.mybatis.spring.boot.autoconfigure;

import org.apache.ibatis.scripting.LanguageDriver;
import org.mybatis.scripting.freemarker.FreeMarkerLanguageDriver;
import org.mybatis.scripting.thymeleaf.ThymeleafLanguageDriver;
import org.mybatis.scripting.thymeleaf.ThymeleafLanguageDriverConfig;
import org.mybatis.scripting.velocity.Driver;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * {@link EnableAutoConfiguration Auto-Configuration} for MyBatis's scripting language drivers.
 *
 * @author Kazuki Shimizu
 * @since 2.1.0
 */
@Configuration
@ConditionalOnClass(LanguageDriver.class)
public class MybatisLanguageDriverAutoConfiguration {

  @Configuration
  @ConditionalOnClass(FreeMarkerLanguageDriver.class)
  static class FreeMarkerConfiguration {
    @Bean
    @ConditionalOnMissingBean
    FreeMarkerLanguageDriver freeMarkerLanguageDriver() {
      return new FreeMarkerLanguageDriver();
    }
  }

  @Configuration
  @ConditionalOnClass(Driver.class)
  static class VelocityConfiguration {
    @Bean
    @ConditionalOnMissingBean
    Driver velocityLanguageDriver() {
      return new Driver();
    }
  }

  @Configuration
  @ConditionalOnClass(ThymeleafLanguageDriver.class)
  static class ThymeleafConfiguration {
    @Bean
    @ConditionalOnMissingBean
    ThymeleafLanguageDriver thymeleafLanguageDriver(ObjectProvider<ThymeleafLanguageDriverConfig> configProvider) {
      return new ThymeleafLanguageDriver(configProvider.getIfAvailable(ThymeleafLanguageDriverConfig::newInstance));
    }
  }

}
