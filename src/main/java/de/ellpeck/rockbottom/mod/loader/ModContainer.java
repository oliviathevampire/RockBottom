package de.ellpeck.rockbottom.mod.loader;

import de.ellpeck.rockbottom.mod.loader.metadata.ModMetadata;

import java.nio.file.Path;

public interface ModContainer {
	ModMetadata getMetadata();

	@Deprecated
	default Path getRoot() {
		return getRootPath();
	}

	Path getRootPath();

	/**
	 * Get an NIO reference to a file inside the JAR.
	 * Does not guarantee existence!
	 *
	 * @param file The location from root, using "/" as a separator.
	 * @return The Path to a given file.
	 */
	default Path getPath(String file) {
		Path root = getRootPath();
		return root.resolve(file.replace("/", root.getFileSystem().getSeparator()));
	}
}