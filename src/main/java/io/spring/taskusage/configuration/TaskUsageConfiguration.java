/*
 * Copyright 2019 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *          https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package io.spring.taskusage.configuration;

import java.io.IOException;

import com.amazonaws.services.s3.AmazonS3;

import org.springframework.boot.CommandLineRunner;
import org.springframework.cloud.aws.core.io.s3.PathMatchingSimpleStorageResourcePatternResolver;
import org.springframework.cloud.task.configuration.EnableTask;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.util.StreamUtils;

@EnableTask
@Configuration
public class TaskUsageConfiguration {

	@Bean
	CommandLineRunner commandLineRunner(PathMatchingSimpleStorageResourcePatternResolver resourcePatternResolver) {
		return args -> {
			Resource[] resources = resourcePatternResolver.getResources("s3://cellsample/sampledata/*.*");
			for (Resource resource : resources) {
				processResource(resource);
			}

		};
	}

	@Bean
	PathMatchingSimpleStorageResourcePatternResolver resourcePatternResolver(AmazonS3 amazonS3, ResourcePatternResolver resourcePatternResolver) {
		return new PathMatchingSimpleStorageResourcePatternResolver(amazonS3, resourcePatternResolver);
	}

	private void processResource(Resource resource) throws IOException {
		FileSystemResource fileSystemResource = new FileSystemResource("/Users/glennrenfro/tmp/" + resource.getFilename());
		fileSystemResource.getFile().createNewFile();
		StreamUtils.copy(resource.getInputStream(), fileSystemResource.getOutputStream());
	}

}
