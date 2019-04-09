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

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import io.spring.taskusage.configuration.S3Processor;
import io.spring.taskusage.configuration.TaskProcessorConfiguration;
import io.spring.taskusage.configuration.Usage;
import javax.sql.DataSource;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.jdbc.EmbeddedDataSourceConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.cloud.aws.core.io.s3.PathMatchingSimpleStorageResourcePatternResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class S3taskbillpullApplicationTests {

	private static final String FILE_NAME = "test.json";

	@Test
	public void testRepository() {
		ApplicationContextRunner applicationContextRunner = new ApplicationContextRunner()
				.withConfiguration(
						AutoConfigurations.of(EmbeddedDataSourceConfiguration.class, TaskProcessorConfiguration.class,
								TestConfiguration.class));
		applicationContextRunner.run((context) -> {
			DataSource dataSource = context.getBean(DataSource.class);
			S3Processor s3Processor = context.getBean(S3Processor.class);
			initializeDatabase(dataSource);
			s3Processor.processResources();
			List<Usage> usages = getResultsFromDB(dataSource);
			assertThat(usages.size()).isEqualTo(1);
			assertThat(usages.get(0).getId()).isEqualTo(1);
			assertThat(usages.get(0).getFirstName()).isEqualTo("jane");
		});
	}

	public void initializeDatabase(DataSource dataSource) {
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		jdbcTemplate.execute("CREATE TABLE bill_usage ( id int, first_name varchar(50), last_name varchar(50), minutes int, data_usage int)");
	}

	public List<Usage> getResultsFromDB(DataSource dataSource) {
			NamedParameterJdbcTemplate jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
			return jdbcTemplate.query("select ID, FIRST_NAME, LAST_NAME, MINUTES, DATA_USAGE FROM BILL_USAGE", new UsageRowMapper());
	}


	@Configuration
	public static class TestConfiguration {

		@Bean
		public PathMatchingSimpleStorageResourcePatternResolver pathMatchingSimpleStorageResourcePatternResolver() throws IOException {
			PathMatchingSimpleStorageResourcePatternResolver pathMatchingResourceResolver = mock(PathMatchingSimpleStorageResourcePatternResolver.class);
			Resource[] resources = {new ClassPathResource(FILE_NAME)};
			when(pathMatchingResourceResolver.getResources(anyString())).thenReturn(resources);

			return pathMatchingResourceResolver;

		}
	}

	private final class UsageRowMapper implements RowMapper<Usage> {
		@Override
		public Usage mapRow(ResultSet rs, int rowNum) throws SQLException {
			return new Usage(rs.getLong("id"),
					rs.getString("FIRST_NAME"), rs.getString("LAST_NAME"),
					rs.getLong("MINUTES"), rs.getLong("DATA_USAGE"));
		}

	}
}
