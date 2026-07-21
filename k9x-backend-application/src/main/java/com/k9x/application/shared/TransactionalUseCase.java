package com.k9x.application.shared;

/**
 * Marks a service case as a transactional unit of work: every persistence write it performs (however
 * many, wherever they sit in the method) commits or rolls back together.
 *
 * <p>It is a plain marker — no methods, no framework dependency — so the application layer stays free
 * of Spring. The transactional boundary is wired at the composition root: the loader advises every
 * bean implementing this interface with a database transaction (see
 * {@code com.k9x.configuration.transaction.UseCaseTransactionConfiguration}). Read-only service cases
 * do not implement it, so they never hold a connection open for a transaction.
 */
public interface TransactionalUseCase {
}
