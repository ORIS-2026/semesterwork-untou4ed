package ru.itis.wishlist.logging.aspect;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.itis.wishlist.logging.WishlistLoggingProperties;

import java.util.Arrays;

@Aspect
@RequiredArgsConstructor
public class WishlistLoggingAspect {

    private final WishlistLoggingProperties properties;

    @Pointcut("within(@org.springframework.stereotype.Service *)")
    public void service() {}

    @Pointcut("within(@org.springframework.stereotype.Repository *)")
    public void repository() {}

    @Pointcut("within(@org.springframework.web.bind.annotation.RestController *)" +
              " || within(@org.springframework.stereotype.Controller *)")
    public void controller() {}

    @Pointcut("within(@org.springframework.cloud.openfeign.FeignClient *)")
    public void feignClient() {}

    @Around("service() || repository() || controller() || feignClient()")
    public Object logMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        Logger log = LoggerFactory.getLogger(joinPoint.getTarget().getClass());
        String method = ((MethodSignature) joinPoint.getSignature()).getMethod().getName();

        if (properties.isLogArgs()) {
            log.info("→ {}({})", method, formatArgs(joinPoint.getArgs()));
        } else {
            log.info("→ {}()", method);
        }

        long start = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - start;

            if (duration >= properties.getSlowThresholdMs()) {
                log.warn("← {} [SLOW: {}ms]", method, duration);
            } else if (properties.isLogReturn()) {
                log.info("← {} return=[{}] [{}ms]", method, result, duration);
            } else {
                log.info("← {} [{}ms]", method, duration);
            }

            return result;
        } catch (Throwable ex) {
            long duration = System.currentTimeMillis() - start;
            log.error("✗ {} threw {}('{}') [{}ms]",
                    method, ex.getClass().getSimpleName(), ex.getMessage(), duration);
            throw ex;
        }
    }

    private String formatArgs(Object[] args) {
        if (args == null || args.length == 0) return "";
        return Arrays.stream(args)
                .map(arg -> arg == null ? "null" : arg.toString())
                .reduce((a, b) -> a + ", " + b)
                .orElse("");
    }
}
