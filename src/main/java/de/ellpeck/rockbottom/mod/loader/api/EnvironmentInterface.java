package de.ellpeck.rockbottom.mod.loader.api;

import java.lang.annotation.*;

/**
 * Applied to declare that an interface implementation is present only in the specified environment.
 * Use with caution, as Fabric-loader will completely remove interface implementations in a mismatched environment!
 * Implemented methods are not removed. To remove implemented methods, use {@link Environment}.
 */
@Retention(RetentionPolicy.CLASS)
@Repeatable(EnvironmentInterfaces.class)
@Target(ElementType.TYPE)
public @interface EnvironmentInterface {
	EnvType value();
	Class<?> itf();
}