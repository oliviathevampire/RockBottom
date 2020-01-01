package de.ellpeck.rockbottom.mod.loader.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Applied to declare that interface implementations are present only in the specified environment.
 * Use with caution, as Fabric-loader will completely remove interface implementations in a mismatched environment!
 * Implemented methods are not removed. To remove implemented methods, use {@link Environment}.
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
public @interface EnvironmentInterfaces {
	EnvironmentInterface[] value();
}