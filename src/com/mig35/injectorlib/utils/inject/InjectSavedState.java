package com.mig35.injectorlib.utils.inject;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Date: 28.03.13
 * Time: 12:26
 *
 * @author MiG35
 */
@Target(FIELD)
@Retention(RUNTIME)
public @interface InjectSavedState {

	/**
	 * Optional TAG to save with
	 */
	String value() default "";

}
