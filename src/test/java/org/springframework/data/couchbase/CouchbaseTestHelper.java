/*
 * Copyright 2019 Couchbase, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.data.couchbase;

import org.rnorth.ducttape.unreliables.Unreliables;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public class CouchbaseTestHelper {
  private CouchbaseTestHelper() {
    throw new AssertionError("not instantiable");
  }

  /**
   * Returns a repository instance for the given interface.
   * <p>
   * Retry because concurrent index creation throws exception.
   * See https://issues.couchbase.com/browse/MB-32238
   */
  public static <T> T getRepositoryWithRetry(RepositoryFactorySupport factory, Class<T> repositoryInterface) {
    return retryUntilSuccess(() -> factory.getRepository(repositoryInterface));
  }

  private static <T> T retryUntilSuccess(final Callable<T> lambda) {
    return Unreliables.retryUntilSuccess(10, TimeUnit.SECONDS, lambda);
  }
}
