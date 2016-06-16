package com.azoft.injectorlib;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target(FIELD)
@Retention(RUNTIME)
public @interface InjectSavedState {

    /**
     * Optional TAG to save with
     *
     * @return name for store in bundle
     */
    String value() default "";
}
