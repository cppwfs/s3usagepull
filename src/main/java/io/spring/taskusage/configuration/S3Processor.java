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
import java.nio.charset.StandardCharsets;
import java.sql.Types;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.aws.core.io.s3.PathMatchingSimpleStorageResourcePatternResolver;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.util.StreamUtils;

public class S3Processor {


	@Value("${io.spring.inputBucket:s3://cellsample/sampledata/*.*}")
	private String inputBucket;

	private final NamedParameterJdbcTemplate jdbcTemplate;

	private PathMatchingSimpleStorageResourcePatternResolver resourcePatternResolver;

	private static final String CREATE_USAGE = "INSERT into "
			+ "BILL_USAGE(id, first_name, last_name, minutes, data_usage ) values (:id, :firstName, :lastName, :minutes, :dataUsage)";


	public S3Processor(PathMatchingSimpleStorageResourcePatternResolver resourcePatternResolver, DataSource dataSource) {
		this.resourcePatternResolver = resourcePatternResolver;
		this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
	}

	public void processResources() throws IOException {
		Resource[] resources = this.resourcePatternResolver.getResources(this.inputBucket);
		for (Resource resource : resources) {
			processResource(resource);
		}
	}

	public void processResource(Resource resource) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		CollectionType javaType = mapper.getTypeFactory().constructCollectionType(List.class, Usage.class);
		List<Usage> usageList = mapper.readValue(StreamUtils.copyToString( resource.getInputStream(), StandardCharsets.UTF_8), javaType);
		for(Usage usage : usageList) {
			insertArgument(usage);
		}
	}
	private void insertArgument(Usage usage) {
		final MapSqlParameterSource queryParameters = new MapSqlParameterSource()
				.addValue("id", usage.getId(), Types.BIGINT)
				.addValue("firstName", usage.getFirstName(), Types.VARCHAR)
				.addValue("lastName", usage.getLastName(), Types.VARCHAR)
				.addValue("minutes", usage.getMinutes(), Types.BIGINT)
				.addValue("dataUsage", usage.getDataUsage(), Types.BIGINT);
		this.jdbcTemplate.update(CREATE_USAGE, queryParameters);
	}
}
