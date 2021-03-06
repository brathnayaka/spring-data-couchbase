/*
 * Copyright 2017-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.data.couchbase.repository.config;

import java.util.Collection;

import org.junit.Test;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.core.env.Environment;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.type.StandardAnnotationMetadata;
import org.springframework.data.couchbase.core.mapping.Document;
import org.springframework.data.couchbase.repository.ReactiveCouchbaseRepository;
import org.springframework.data.repository.config.AnnotationRepositoryConfigurationSource;
import org.springframework.data.repository.config.RepositoryConfiguration;
import org.springframework.data.repository.config.RepositoryConfigurationSource;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.data.repository.reactive.RxJava2CrudRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

/**
 * Unit tests for {@link ReactiveCouchbaseRepositoryConfigurationExtension}.
 *
 * @author Mark Paluch
 */
public class ReactiveCouchbaseRepositoryConfigurationExtensionUnitTests {

	StandardAnnotationMetadata metadata = new StandardAnnotationMetadata(Config.class, true);
	ResourceLoader loader = new PathMatchingResourcePatternResolver();
	Environment environment = new StandardEnvironment();
	BeanDefinitionRegistry registry = new DefaultListableBeanFactory();

	RepositoryConfigurationSource configurationSource = new AnnotationRepositoryConfigurationSource(metadata,
			EnableReactiveCouchbaseRepositories.class, loader, environment, registry);

	@Test // DATACOUCH-350
	public void isStrictMatchIfDomainTypeIsAnnotatedWithDocument() {

		ReactiveCouchbaseRepositoryConfigurationExtension extension = new ReactiveCouchbaseRepositoryConfigurationExtension();
		assertHasRepo(SampleRepository.class, extension.getRepositoryConfigurations(configurationSource, loader, true));
	}

	@Test // DATACOUCH-350
	public void isStrictMatchIfRepositoryExtendsStoreSpecificBase() {

		ReactiveCouchbaseRepositoryConfigurationExtension extension = new ReactiveCouchbaseRepositoryConfigurationExtension();
		assertHasRepo(StoreRepository.class, extension.getRepositoryConfigurations(configurationSource, loader, true));
	}

	@Test // DATACOUCH-350
	public void isNotStrictMatchIfDomainTypeIsNotAnnotatedWithDocument() {

		ReactiveCouchbaseRepositoryConfigurationExtension extension = new ReactiveCouchbaseRepositoryConfigurationExtension();
		assertDoesNotHaveRepo(UnannotatedRepository.class,
				extension.getRepositoryConfigurations(configurationSource, loader, true));
	}

	private static void assertHasRepo(Class<?> repositoryInterface,
			Collection<RepositoryConfiguration<RepositoryConfigurationSource>> configs) {

		for (RepositoryConfiguration<?> config : configs) {
			if (config.getRepositoryInterface().equals(repositoryInterface.getName())) {
				return;
			}
		}

		fail("Expected to find config for repository interface "
				.concat(repositoryInterface.getName()).concat(" but got ")
				.concat(configs.toString()));
	}

	private static void assertDoesNotHaveRepo(Class<?> repositoryInterface,
			Collection<RepositoryConfiguration<RepositoryConfigurationSource>> configs) {

		for (RepositoryConfiguration<?> config : configs) {
			if (config.getRepositoryInterface().equals(repositoryInterface.getName())) {
				fail("Expected not to find config for repository interface "
						.concat(repositoryInterface.getName()));
			}
		}
	}

	@EnableReactiveCouchbaseRepositories(considerNestedRepositories = true)
	static class Config {

	}

	@Document
	static class Sample {}

	static class Store {}

	interface SampleRepository extends ReactiveCrudRepository<Sample, Long> {}

	interface UnannotatedRepository extends RxJava2CrudRepository<Store, Long> {}

	interface StoreRepository extends ReactiveCouchbaseRepository<Store, Long> {}
}
