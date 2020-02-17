package org.springframework.data.couchbase.core;

public interface FluentCouchbaseOperations extends
  ExecutableUpsertByIdOperation,
  ExecutableInsertByIdOperation,
  ExecutableReplaceByIdOperation,
  ExecutableFindByIdOperation,
  ExecutableFindByQueryOperation,
  ExecutableFindByAnalyticsOperation,
  ExecutableExistsByIdOperation,
  ExecutableRemoveByIdOperation,
  ExecutableRemoveByQueryOperation {
}