/*
This code is not from Michael Schleichardt.
It was copied on 12-1-2011 from http://www.playframework.org/community/snippets/19 .
This code can be modified.
 */

package validation;

import java.lang.annotation.Target;
import java.lang.annotation.Retention;
import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;

import net.sf.oval.configuration.annotation.Constraint;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Constraint(checkWith = UniqueCheck.class)
public @interface Unique {
  String message() default UniqueCheck.message;
}