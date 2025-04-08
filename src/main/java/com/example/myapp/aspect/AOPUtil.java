package com.example.myapp.aspect;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.CodeSignature;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

@UtilityClass
@Slf4j
public class AOPUtil {

    private final ExpressionParser expressionParser = new SpelExpressionParser();

    public String getKeyVal(JoinPoint joinPoint, String name, String key) {
        return resolveKey(joinPoint, name, key);
    }

    public Method getMethod(JoinPoint joinPoint) {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        return methodSignature.getMethod();
    }

    public Type getMethodActualReturnType(Method method) {
        return ((ParameterizedType) method.getGenericReturnType()).getActualTypeArguments()[0];
    }

    public TypeReference getTypeReference(Method method) {
        return new TypeReference<>() {
            @Override
            public Type getType() {
                return getMethodActualReturnType(method);
            }
        };
    }

    private String[] getParamNames(JoinPoint joinPoint) {
        CodeSignature codeSignature = (CodeSignature) joinPoint.getSignature();
        return codeSignature.getParameterNames();
    }

    private String resolveKey(JoinPoint joinPoint, String name, String key) {
        if (StringUtils.hasText(key)) {
            if (key.contains("#") || key.contains("'")) {
                String[] parameterNames = getParamNames(joinPoint);
                Object[] args = joinPoint.getArgs();
                StandardEvaluationContext context = new StandardEvaluationContext();
                for (int i = 0; i < parameterNames.length; i++) {
                    context.setVariable(parameterNames[i], args[i]);
                }
                return name + "_" + expressionParser.parseExpression(key).getValue(context);
            }
            return name + "_" + key;
        } else {
            throw new RuntimeException("Redis annotation missing key");
        }
    }
}
