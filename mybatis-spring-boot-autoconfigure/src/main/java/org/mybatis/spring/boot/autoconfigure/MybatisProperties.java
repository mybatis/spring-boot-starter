/**
 *    Copyright 2015-2016 the original author or authors.
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ExecutorType;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

/**
 * Configuration properties for Mybatis.
 *
 * @author Eddú Meléndez
 * @author Kazuki Shimizu
 */
@ConfigurationProperties(prefix = MybatisProperties.MYBATIS_PREFIX)
public class MybatisProperties {

  public static final String MYBATIS_PREFIX = "mybatis";

  /**
   * Config file path.
   */
  private String configLocation;

  /**
   * Location of mybatis mapper files.
   */
  private String[] mapperLocations;

  /**
   * Package to scan domain objects.
   */
  private String typeAliasesPackage;

  /**
   * Package to scan handlers.
   */
  private String typeHandlersPackage;

  /**
   * Check the config file exists.
   */
  private boolean checkConfigLocation = false;

  /**
   * Execution mode for {@link org.mybatis.spring.SqlSessionTemplate}.
   */
  private ExecutorType executorType;

  /**
   * A Configuration object for customize default settings. If {@link #configLocation}
   * is specified, this property is not used.
   */
  @NestedConfigurationProperty
  private Configuration configuration;

  /**
   * @since 1.1.0
   * @return
   */
  public String getConfigLocation() {
    return this.configLocation;
  }

  /**
   * @since 1.1.0
   * @return
   */
  public void setConfigLocation(String configLocation) {
    this.configLocation = configLocation;
  }

  @Deprecated
  public String getConfig() {
    return this.configLocation;
  }

  @Deprecated
  public void setConfig(String config) {
    this.configLocation = config;
  }

  public String[] getMapperLocations() {
    return this.mapperLocations;
  }

  public void setMapperLocations(String[] mapperLocations) {
    this.mapperLocations = mapperLocations;
  }

  public String getTypeHandlersPackage() {
    return this.typeHandlersPackage;
  }

  public void setTypeHandlersPackage(String typeHandlersPackage) {
    this.typeHandlersPackage = typeHandlersPackage;
  }

  public String getTypeAliasesPackage() {
    return this.typeAliasesPackage;
  }

  public void setTypeAliasesPackage(String typeAliasesPackage) {
    this.typeAliasesPackage = typeAliasesPackage;
  }

  public boolean isCheckConfigLocation() {
    return this.checkConfigLocation;
  }

  public void setCheckConfigLocation(boolean checkConfigLocation) {
    this.checkConfigLocation = checkConfigLocation;
  }

  public ExecutorType getExecutorType() {
    return this.executorType;
  }

  public void setExecutorType(ExecutorType executorType) {
    this.executorType = executorType;
  }

  public Configuration getConfiguration() {
    return configuration;
  }

  public void setConfiguration(Configuration configuration) {
    this.configuration = configuration;
  }

  public Resource[] resolveMapperLocations() {
    ResourcePatternResolver resourceResolver = new PathMatchingResourcePatternResolver();
    List<Resource> resources = new ArrayList<Resource>();
    if (this.mapperLocations != null) {
      for (String mapperLocation : this.mapperLocations) {
        try {
          Resource[] mappers = resourceResolver.getResources(mapperLocation);
          resources.addAll(Arrays.asList(mappers));
        } catch (IOException e) {
          // ignore
        }
      }
    }
    return resources.toArray(new Resource[resources.size()]);
  }
}
