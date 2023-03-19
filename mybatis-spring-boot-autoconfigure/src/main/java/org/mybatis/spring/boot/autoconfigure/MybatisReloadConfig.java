/*
 *    Copyright 2015-2022 the original author or authors.
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

import io.methvin.watcher.DirectoryWatcher;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.builder.xml.XMLMapperEntityResolver;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.parsing.XNode;
import org.apache.ibatis.parsing.XPathParser;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.support.ApplicationObjectSupport;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * mybatis xml file auto reload config.
 * if <b>mybatis.xml-reload</b> is true, will enable this configuration.
 *
 * @author wayn
 */
@Component
@ConditionalOnProperty(value = "mybatis.xml-reload", matchIfMissing = true)
public class MybatisReloadConfig extends ApplicationObjectSupport implements InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(MybatisReloadConfig.class);
    public static final PathMatchingResourcePatternResolver pathMatchingResourcePatternResolver = new PathMatchingResourcePatternResolver();


    private final MybatisProperties properties;

    public MybatisReloadConfig(MybatisProperties properties) {
        this.properties = properties;
    }

    @Override
    public void afterPropertiesSet() throws IOException {
        Map<String, SqlSessionFactory> beansOfType = getApplicationContext().getBeansOfType(SqlSessionFactory.class);
        String[] locationPatterns = properties.getMapperLocations();
        List<Resource> mapperLocations = Arrays.stream(properties.resolveMapperLocations()).collect(Collectors.toList());

        // xml auto reload begin.
        // Modify the mapperLocation file and the listening directory to its own resources directory instead of the target directory,
        // so that the modified xml load can take effect immediately
        List<Path> rootPaths = new ArrayList<>();
        for (String locationPattern : locationPatterns) {
            String[] split = locationPattern.split("/");
            String rootDir = split[0];
            Path rootPath = Path.of(pathMatchingResourcePatternResolver.getResources(rootDir)[0].getFile().getAbsolutePath());
            if (!rootDir.contains("classpath:")) {
                String compilerDir = rootDir.split(":", 2)[1];
                File mapperDir = new File("src/main/resources/" + compilerDir);
                if (mapperDir.exists()) {
                    rootPath = Path.of(mapperDir.getAbsolutePath());
                    File[] files = mapperDir.listFiles();
                    mapperLocations.addAll(Arrays.stream(files != null ? files : new File[0]).map(FileSystemResource::new).toList());
                }
            }
            rootPaths.add(rootPath);
        }

        // Directory monitor, modifying files in the directory will trigger the MODIFY event.
        DirectoryWatcher watcher = DirectoryWatcher.builder()
                .paths(rootPaths) // or use paths(directoriesToWatch)
                .listener(event -> {
                    switch (event.eventType()) {
                        case CREATE: /* file created */
                            break;
                        case MODIFY: /* file modified */
                            Path modifyPath = event.path();
                            String absolutePath = modifyPath.toFile().getAbsolutePath();
                            LOGGER.debug("mybatis xml file has changed:" + modifyPath);
                            for (SqlSessionFactory sqlSessionFactory : beansOfType.values()) {
                                try {
                                    // Obtain the loadedResources, resultMaps, sqlFragmentsMaps, mappedStatementMaps properties in the Configuration object through reflection
                                    Configuration targetConfiguration = sqlSessionFactory.getConfiguration();
                                    Class<? extends Configuration> aClass = targetConfiguration.getClass();
                                    Set<String> loadedResources = (Set<String>) getFieldValue(targetConfiguration, aClass, "loadedResources");
                                    loadedResources.clear();

                                    Map<String, ResultMap> resultMaps = (Map<String, ResultMap>) getFieldValue(targetConfiguration, aClass, "resultMaps");
                                    Map<String, XNode> sqlFragmentsMaps = (Map<String, XNode>) getFieldValue(targetConfiguration, aClass, "sqlFragments");
                                    Map<String, MappedStatement> mappedStatementMaps = (Map<String, MappedStatement>) getFieldValue(targetConfiguration, aClass, "mappedStatements");

                                    for (Resource mapperLocation : mapperLocations) {
                                        if (!absolutePath.equals(mapperLocation.getFile().getAbsolutePath())) {
                                            continue;
                                        }
                                        XPathParser parser = new XPathParser(mapperLocation.getInputStream(), true, targetConfiguration.getVariables(), new XMLMapperEntityResolver());
                                        XNode mapperXnode = parser.evalNode("/mapper");
                                        List<XNode> resultMapNodes = mapperXnode.evalNodes("/mapper/resultMap");
                                        String namespace = mapperXnode.getStringAttribute("namespace");

                                        // clear loadedResources, resultMaps, sqlFragmentsMaps, mappedStatementMaps begin
                                        for (XNode xNode : resultMapNodes) {
                                            String id = xNode.getStringAttribute("id", xNode.getValueBasedIdentifier());
                                            resultMaps.remove(namespace + "." + id);
                                        }

                                        List<XNode> sqlNodes = mapperXnode.evalNodes("/mapper/sql");
                                        for (XNode sqlNode : sqlNodes) {
                                            String id = sqlNode.getStringAttribute("id", sqlNode.getValueBasedIdentifier());
                                            sqlFragmentsMaps.remove(namespace + "." + id);
                                        }

                                        List<XNode> msNodes = mapperXnode.evalNodes("select|insert|update|delete");
                                        for (XNode msNode : msNodes) {
                                            String id = msNode.getStringAttribute("id", msNode.getValueBasedIdentifier());
                                            mappedStatementMaps.remove(namespace + "." + id);
                                        }
                                        // clear loadedResources, resultMaps, sqlFragmentsMaps, mappedStatementMaps end.
                                        try {
                                            // Finally, reload the xml file
                                            XMLMapperBuilder xmlMapperBuilder = new XMLMapperBuilder(mapperLocation.getInputStream(),
                                                    targetConfiguration, mapperLocation.toString(), targetConfiguration.getSqlFragments());
                                            xmlMapperBuilder.parse();
                                        } catch (Exception e) {
                                            LOGGER.error(e.getMessage(), e);
                                        }
                                        LOGGER.debug("Parsed mapper file: '" + mapperLocation + "'");
                                    }
                                } catch (Exception e) {
                                    throw new RuntimeException(e);
                                }
                            }
                            break;
                        case DELETE: /* file deleted */
                            break;
                    }
                })
                .build();

        watcher.watchAsync(new ScheduledThreadPoolExecutor(1,
                new BasicThreadFactory.Builder().namingPattern("xml-reload-%d").daemon(true).build()));
        // xml auto reload end.
    }

    /**
     * Use reflection to get the field value.
     */
    private static Object getFieldValue(Configuration targetConfiguration, Class<? extends Configuration> aClass,
                                        String filed) throws NoSuchFieldException, IllegalAccessException {
        Field resultMapsField = aClass.getDeclaredField(filed);
        resultMapsField.setAccessible(true);
        return resultMapsField.get(targetConfiguration);
    }
}
