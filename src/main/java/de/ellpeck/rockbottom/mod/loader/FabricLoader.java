package de.ellpeck.rockbottom.mod.loader;

import de.ellpeck.rockbottom.mod.loader.api.EnvType;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * The public-facing FabricLoader instance.
 * @since 0.4.0
 */
public interface FabricLoader {

	<T> List<T> getEntrypoints(String key, Class<T> type);

	/**
	 * Gets the container for a given mod.
	 * @param id The ID of the mod.
	 * @return The mod container, if present.
	 */
	Optional<ModContainer> getModContainer(String id);

	/**
	 * Gets all mod containers.
	 * @return A collection of all loaded mod containers.
	 */
	Collection<ModContainer> getAllMods();

	/**
	 * Checks if a mod with a given ID is loaded.
	 * @param id The ID of the mod, as defined in fabric.mod.json.
	 * @return Whether or not the mod is present in this FabricLoader instance.
	 */
	boolean isModLoaded(String id);

	/**
	 * Checks if Fabric Loader is currently running in a "development"
	 * environment. Can be used for enabling debug mode or additional checks.
	 * Should not be used to make assumptions about f.e. mappings.
	 * @return Whether or not Loader is currently in a "development"
	 * environment.
	 */
	boolean isDevelopmentEnvironment();

	/**
	 * Get the current environment type.
	 * @return The current environment type.
	 */
	EnvType getEnvironmentType();

	/**
	 * Get the current game instance. Can represent a game client or
	 * server object. As such, the exact return is dependent on the
	 * current environment type.
	 * @return A client or server instance object.
	 */
	Object getGameInstance();

	/**
	 * Get the current game working directory.
	 * @return The directory.
	 */
	File getGameDirectory();

	/**
	 * Get the current directory for game configuration files.
	 * @return The directory.
	 */
	File getConfigDirectory();
}