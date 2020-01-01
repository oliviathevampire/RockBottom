package de.ellpeck.rockbottom.mod.loader.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Applied to declare that the element is present only in the specified environment.
 * Use with caution, as Fabric-loader will completely remove annotated elements in a mismatched environment!
 * This annotation should be inherited when overriding a method.
 */
@Retention(RetentionPolicy.CLASS)
@Target({ ElementType.TYPE, ElementType.METHOD, ElementType.FIELD, ElementType.CONSTRUCTOR })
public @interface Environment {
	EnvType value();
}