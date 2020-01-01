package de.ellpeck.rockbottom.mod.util.version;

import de.ellpeck.rockbottom.mod.loader.Version;

public class StringVersion implements Version {
	private final String version;

	public StringVersion(String version) {
		this.version = version;
	}

	@Override
	public String getFriendlyString() {
		return version;
	}

	@Override
	public String toString() {
		return version;
	}
}