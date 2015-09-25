/*
 *    Copyright 2010-2015 the original author or authors.
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

import org.apache.ibatis.session.ExecutorType;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for Mybatis.
 *
 * @author Eddú Meléndez
 */
@ConfigurationProperties(prefix = MybatisProperties.MYBATIS_PREFIX)
public class MybatisProperties {

	public static final String MYBATIS_PREFIX = "mybatis";

	/**
	 * Config file path.
	 */
	private String config;

	/**
	 * Check the config file exists.
	 */
	private boolean checkConfigLocation = false;

	/**
	 * Execution mode.
	 */
	private ExecutorType executorType = ExecutorType.SIMPLE;

	public String getConfig() {
		return this.config;
	}

	public void setConfig(String config) {
		this.config = config;
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
}
