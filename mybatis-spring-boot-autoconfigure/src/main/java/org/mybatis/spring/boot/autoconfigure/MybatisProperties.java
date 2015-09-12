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
