package de.ellpeck.rockbottom.mod.metadata;

import com.google.gson.*;
import de.ellpeck.rockbottom.mod.loader.Version;
import de.ellpeck.rockbottom.mod.util.version.VersionDeserializer;

import java.io.InputStream;
import java.io.InputStreamReader;

public class ModMetadataParser {
	public static final int LATEST_VERSION = 1;

	private static final Gson GSON_V1 = new GsonBuilder()
		.registerTypeAdapter(Version.class, new VersionDeserializer())
		.registerTypeAdapter(ModMetadataV1.JarEntry.class, new ModMetadataV1.JarEntry.Deserializer())
		.registerTypeAdapter(ModMetadataV1.IconEntry.class, new ModMetadataV1.IconEntry.Deserializer())
		.registerTypeAdapter(ModMetadataV1.LicenseEntry.class, new ModMetadataV1.LicenseEntry.Deserializer())
		.registerTypeAdapter(ModMetadataV1.Person.class, new ModMetadataV1.Person.Deserializer())
		.registerTypeAdapter(ModMetadataV1.DependencyContainer.class, new ModMetadataV1.DependencyContainer.Deserializer())
		.registerTypeAdapter(ModMetadataV1.MixinEntry.class, new ModMetadataV1.MixinEntry.Deserializer())
		.registerTypeAdapter(ModMetadataV1.EntrypointContainer.class, new ModMetadataV1.EntrypointContainer.Deserializer())
		.registerTypeAdapter(ModMetadataV1.Environment.class, new ModMetadataV1.Environment.Deserializer())
		.create();

	private static final JsonParser JSON_PARSER = new JsonParser();

	private static LoaderModMetadata getMod(JsonObject object) {
		return GSON_V1.fromJson(object, ModMetadataV1.class);
	}

	public static LoaderModMetadata[] getMods(InputStream in) {
		JsonElement el = JSON_PARSER.parse(new InputStreamReader(in));
		if (el.isJsonObject()) {
			LoaderModMetadata metadata = getMod(el.getAsJsonObject());
			if (metadata != null) {
				return new LoaderModMetadata[] { metadata };
			}
		}

		return new LoaderModMetadata[0];
	}
}