package com.junbaor.event.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;

abstract public class AnnotationUtils {

    @Nullable
    public static <A extends Annotation> A getAnnotation(@NotNull Class<?> clazz,
                                                         @NotNull Class<A> annotationClass) {
        Class<?> classForUse = clazz;
        while (classForUse != null) {
            A annotation = classForUse.getAnnotation(annotationClass);
            if (annotation != null) {
                return annotation;
            }
            classForUse = classForUse.getSuperclass();
        }
        return null;
    }

}
