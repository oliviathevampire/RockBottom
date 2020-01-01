package de.ellpeck.rockbottom.mod.loader.metadata;

import de.ellpeck.rockbottom.mod.loader.Version;

public interface ModDependency {
	String getModId();
	boolean matches(Version version);
}