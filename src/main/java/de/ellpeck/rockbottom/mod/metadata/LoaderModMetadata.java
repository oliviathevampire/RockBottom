package de.ellpeck.rockbottom.mod.metadata;

import de.ellpeck.rockbottom.mod.loader.api.EnvType;
import de.ellpeck.rockbottom.mod.loader.metadata.ModMetadata;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Internal variant of the ModMetadata interface.
 */
public interface LoaderModMetadata extends ModMetadata {
	int getSchemaVersion();
	default String getOldStyleLanguageAdapter() {
		return "net.fabricmc.loader.language.JavaLanguageAdapter";
	}
	Map<String, String> getLanguageAdapterDefinitions();
	Collection<NestedJarEntry> getJars();
	Collection<String> getMixinConfigs(EnvType type);
	boolean loadsInEnvironment(EnvType type);

	Collection<String> getOldInitializers();
	List<EntrypointMetadata> getEntrypoints(String type);
	Collection<String> getEntrypointKeys();

	void emitFormatWarnings(Logger logger);
}