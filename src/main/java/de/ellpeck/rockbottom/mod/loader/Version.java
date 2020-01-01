package de.ellpeck.rockbottom.mod.loader;

import de.ellpeck.rockbottom.mod.util.version.VersionDeserializer;
import de.ellpeck.rockbottom.mod.util.version.VersionParsingException;

public interface Version {
	String getFriendlyString();

	static Version parse(String string) throws VersionParsingException {
		return VersionDeserializer.deserialize(string);
	}
}