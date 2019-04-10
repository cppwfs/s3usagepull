/*
 * Copyright 2017 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package io.spring.taskusage.configuration;

import com.amazonaws.services.s3.AmazonS3;

import org.springframework.boot.CommandLineRunner;
import org.springframework.cloud.aws.core.io.s3.PathMatchingSimpleStorageResourcePatternResolver;
import org.springframework.cloud.task.configuration.EnableTask;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.ResourcePatternResolver;

@Configuration
@EnableTask
public class TaskConfiguration {

	@Bean
	PathMatchingSimpleStorageResourcePatternResolver resourcePatternResolver(AmazonS3 amazonS3, ResourcePatternResolver resourcePatternResolver) {
		return new PathMatchingSimpleStorageResourcePatternResolver(amazonS3, resourcePatternResolver);
	}

	@Bean
	CommandLineRunner commandLineRunner(S3Processor s3Processor) {
		return args -> {
			s3Processor.processResources();
		};
	}
}
