package com.banking.audit;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class AuditAspect {

    private final AuditService auditService;

    @Around("@annotation(auditable)")
    public Object audit(ProceedingJoinPoint joinPoint, Auditable auditable) throws Throwable {

        String ip   = resolveClientIp();
        String args = Arrays.toString(joinPoint.getArgs());

        Object result;
        try {
            result = joinPoint.proceed();
        } catch (Throwable t) {
            auditService.record(
                    auditable.action() + "_FAILED",
                    auditable.entityType(),
                    "N/A",
                    "Error: " + t.getMessage() + " | args: " + args,
                    ip);
            throw t;
        }

        auditService.record(
                auditable.action(),
                auditable.entityType(),
                resolveEntityId(result),
                "args: " + args,
                ip);

        return result;
    }

    private String resolveEntityId(Object result) {
        if (result == null) return "N/A";
        try {
            return result.getClass().getMethod("getId").invoke(result).toString();
        } catch (Exception e) {
            return result.toString();
        }
    }

    private String resolveClientIp() {
        try {
            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest req = attrs.getRequest();
                String forwarded = req.getHeader("X-Forwarded-For");
                return (forwarded != null) ? forwarded.split(",")[0].trim() : req.getRemoteAddr();
            }
        } catch (Exception ignored) {}
        return "UNKNOWN";
    }
}