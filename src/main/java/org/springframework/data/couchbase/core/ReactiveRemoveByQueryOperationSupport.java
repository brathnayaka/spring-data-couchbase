/*
 * Copyright 2012-2020 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.data.couchbase.core;

import com.couchbase.client.java.query.QueryOptions;
import com.couchbase.client.java.query.QueryScanConsistency;
import com.couchbase.client.java.query.ReactiveQueryResult;
import org.springframework.data.couchbase.core.query.Query;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Optional;

public class ReactiveRemoveByQueryOperationSupport implements ReactiveRemoveByQueryOperation {

  private static final Query ALL_QUERY = new Query();

  private final ReactiveCouchbaseTemplate template;

  public ReactiveRemoveByQueryOperationSupport(final ReactiveCouchbaseTemplate template) {
    this.template = template;
  }

  @Override
  public <T> ReactiveRemoveByQuery<T> removeByQuery(Class<T> domainType) {
    return new ReactiveRemoveByQuerySupport<>(template, domainType, ALL_QUERY, QueryScanConsistency.NOT_BOUNDED);
  }

  static class ReactiveRemoveByQuerySupport<T> implements ReactiveRemoveByQuery<T> {

    private final ReactiveCouchbaseTemplate template;
    private final Class<T> domainType;
    private final Query query;
    private final QueryScanConsistency scanConsistency;


    ReactiveRemoveByQuerySupport(final ReactiveCouchbaseTemplate template, final Class<T> domainType, final Query query,
                                   final QueryScanConsistency scanConsistency) {
      this.template = template;
      this.domainType = domainType;
      this.query = query;
      this.scanConsistency = scanConsistency;
    }

    @Override
    public Flux<RemoveResult> all() {
      return Flux.defer(() -> {
        String bucket = "`" + template.getBucketName() + "`";

        String typeKey = template.getConverter().getTypeKey();
        String typeValue = template.support().getJavaNameForEntity(domainType);
        String where = " WHERE `" + typeKey + "` = \"" + typeValue + "\"";

        String returning = " RETURNING meta().*";
        String statement = "DELETE FROM " + bucket + " " + where + returning;

        return template
          .getCouchbaseClientFactory()
          .getCluster()
          .reactive()
          .query(statement, buildQueryOptions())
          .onErrorMap(throwable -> {
            if (throwable instanceof RuntimeException) {
              return template.potentiallyConvertRuntimeException((RuntimeException) throwable);
            } else {
              return throwable;
            }
          })
          .flatMapMany(ReactiveQueryResult::rowsAsObject)
          .map(row -> new RemoveResult(row.getString("id"), row.getLong("cas"), Optional.empty()));
      });
    }

    private QueryOptions buildQueryOptions() {
      final QueryOptions options = QueryOptions.queryOptions();
      if (scanConsistency != null) {
        options.scanConsistency(scanConsistency);
      }
      return options;
    }

    @Override
    public TerminatingRemoveByQuery<T> matching(final Query query) {
      return new ReactiveRemoveByQuerySupport<>(template, domainType, query, scanConsistency);
    }

    @Override
    public RemoveByQueryWithQuery<T> consistentWith(final QueryScanConsistency scanConsistency) {
      return new ReactiveRemoveByQuerySupport<>(template, domainType, query, scanConsistency);
    }

  }

}
