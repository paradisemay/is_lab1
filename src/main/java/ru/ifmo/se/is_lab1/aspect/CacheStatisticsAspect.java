package ru.ifmo.se.is_lab1.aspect;

import jakarta.persistence.EntityManagerFactory;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class CacheStatisticsAspect {

    private static final Logger logger = LoggerFactory.getLogger(CacheStatisticsAspect.class);

    private final EntityManagerFactory entityManagerFactory;

    @Value("${app.cache.stats.enabled:false}")
    private boolean statsEnabled;

    public CacheStatisticsAspect(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    // We intercept calls to services to log stats after operations, or we could just schedule a logger.
    // The requirement says "ability to enable/disable logging of statistics ... using AOP".
    // I will interpret this as logging the state of the cache after service methods that might interact with it.
    // For simplicity, let's target the read/write methods in HumanBeingService.

    @Around("execution(* ru.ifmo.se.is_lab1.service.HumanBeingService.*(..))")
    public Object logCacheStats(ProceedingJoinPoint joinPoint) throws Throwable {
        Object result = joinPoint.proceed();

        if (statsEnabled) {
            logStats();
        }

        return result;
    }

    private void logStats() {
        SessionFactory sessionFactory = entityManagerFactory.unwrap(SessionFactory.class);
        Statistics statistics = sessionFactory.getStatistics();

        // Ensure statistics are enabled in Hibernate if we want to read them
        if (!statistics.isStatisticsEnabled()) {
             statistics.setStatisticsEnabled(true);
        }

        long hitCount = statistics.getSecondLevelCacheHitCount();
        long missCount = statistics.getSecondLevelCacheMissCount();
        long putCount = statistics.getSecondLevelCachePutCount();

        logger.info("L2 Cache Stats - Hits: {}, Misses: {}, Puts: {}", hitCount, missCount, putCount);

        // Detailed region stats could also be logged if needed
    }
}
