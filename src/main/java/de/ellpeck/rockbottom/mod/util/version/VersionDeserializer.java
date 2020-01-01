package de.ellpeck.rockbottom.mod.util.version;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import de.ellpeck.rockbottom.mod.loader.SemanticVersion;
import de.ellpeck.rockbottom.mod.loader.Version;

import java.lang.reflect.Type;

public class VersionDeserializer implements JsonDeserializer<Version> {
	public static SemanticVersion deserializeSemantic(String s) throws VersionParsingException {
		if (s == null || s.isEmpty()) {
			throw new VersionParsingException("Version must be a non-empty string!");
		}

		return new SemanticVersionImpl(s, false);
	}

	public static Version deserialize(String s) throws VersionParsingException {
		if (s == null || s.isEmpty()) {
			throw new VersionParsingException("Version must be a non-empty string!");
		}

		Version version;

		try {
			version = new SemanticVersionImpl(s, false);
		} catch (VersionParsingException e) {
			version = new StringVersion(s);
		}

		return version;
	}

	@Override
	public Version deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
		if (!json.isJsonPrimitive()) {
			throw new JsonParseException("Version must be a non-empty string!");
		}

		String s = json.getAsString();
		try {
			return deserialize(s);
		} catch (VersionParsingException e) {
			throw new JsonParseException(e);
		}
	}
}