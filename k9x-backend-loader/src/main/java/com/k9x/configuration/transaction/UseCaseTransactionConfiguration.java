package com.k9x.configuration.transaction;

import com.k9x.application.shared.TransactionalUseCase;
import org.springframework.aop.Advisor;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.aop.support.StaticMethodMatcherPointcut;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;
import org.springframework.transaction.TransactionManager;
import org.springframework.transaction.interceptor.MatchAlwaysTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;

import java.lang.reflect.Method;

/**
 * Declares the transactional boundary of the application: every service case that implements
 * {@link TransactionalUseCase} runs inside a single database transaction, so all its persistence
 * writes (however many, wherever they sit in the method) commit or roll back together.
 *
 * <p>The boundary lives here, at the composition root, rather than as a Spring annotation inside the
 * application layer, so that {@code k9x-backend-application} stays framework-free (plain Java over the
 * domain). Which use cases are transactional is decided explicitly by the marker interface — no
 * matching on class names or packages. The advisor proxies each marked bean; the transaction-aware
 * jOOQ {@code DSLContext} then binds every adapter call in the method to the same connection, which is
 * why the adapters themselves no longer open their own transactions.
 *
 * <p>Read-only service cases simply do not implement the marker, so they are never wrapped and never
 * hold a connection open for a transaction. The daily snapshot batch also opts out on purpose: it
 * commits one event at a time (skipping failures), so its adapter ({@code SaveObdxSnapshotJooqAdapter})
 * keeps its own per-event {@code dsl.transaction}.
 *
 * <p>The advisor is marked {@link BeanDefinition#ROLE_INFRASTRUCTURE} so it is picked up by the
 * {@code InfrastructureAdvisorAutoProxyCreator} that Spring's transaction management already registers
 * — no AspectJ weaver or extra AOP starter is required.
 */
@Configuration
public class UseCaseTransactionConfiguration {

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public Advisor useCaseTransactionAdvisor(TransactionManager transactionManager) {
        StaticMethodMatcherPointcut pointcut = new StaticMethodMatcherPointcut() {
            @Override
            public boolean matches(Method method, Class<?> targetClass) {
                return TransactionalUseCase.class.isAssignableFrom(targetClass);
            }
        };
        TransactionInterceptor interceptor =
                new TransactionInterceptor(transactionManager, new MatchAlwaysTransactionAttributeSource());
        return new DefaultPointcutAdvisor(pointcut, interceptor);
    }
}
