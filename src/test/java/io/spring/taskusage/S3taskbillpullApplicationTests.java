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

package io.spring.taskusage;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import io.spring.taskusage.configuration.S3Processor;
import io.spring.taskusage.configuration.TaskProcessorConfiguration;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;

import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.cloud.aws.core.io.s3.PathMatchingSimpleStorageResourcePatternResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.StreamUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class S3taskbillpullApplicationTests {

	private static final String FILE_NAME = "test.json";

	private String tempDir;

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	@Before
	public void tearDown() throws IOException{
		File file = folder.newFolder();
		this.tempDir = file.getAbsolutePath() + "/";
	}

	@Test
	public void testRepository() {
		ApplicationContextRunner applicationContextRunner = new ApplicationContextRunner()
				.withPropertyValues("io.spring.outputDirectory=" + this.tempDir)
				.withConfiguration(
						AutoConfigurations.of(TaskProcessorConfiguration.class,
								TestConfiguration.class));

		applicationContextRunner.run((context) -> {
			S3Processor s3Processor = context.getBean(S3Processor.class);
			s3Processor.processResources();
			FileSystemResource fileSystemResource = new FileSystemResource(tempDir + FILE_NAME);
			assertThat(fileSystemResource.exists()).isTrue();
			String result = StreamUtils.copyToString( fileSystemResource.getInputStream(), StandardCharsets.UTF_8);
			assertThat(result).isEqualTo("[{\"id\":\"1\",\"firstName\":\"jane\",\"lastName\":\"doe\",\"minutes\":\"500\",\"dataUsage\":\"1000\"}]");
		});
	}

	@Configuration
	public static class TestConfiguration {

		private Resource testfile = new ClassPathResource(FILE_NAME);

		@Bean
		public PathMatchingSimpleStorageResourcePatternResolver pathMatchingSimpleStorageResourcePatternResolver() throws IOException {
			PathMatchingSimpleStorageResourcePatternResolver pathMatchingResourceResolver = mock(PathMatchingSimpleStorageResourcePatternResolver.class);
			Resource[] resources = {this.testfile};
			when(pathMatchingResourceResolver.getResources(anyString())).thenReturn(resources);

			return pathMatchingResourceResolver;

		}
	}
}
