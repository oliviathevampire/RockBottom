package de.ellpeck.rockbottom.mod.discovery;

import de.ellpeck.rockbottom.mod.util.UrlConversionException;
import de.ellpeck.rockbottom.mod.util.UrlUtil;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

public class DirectoryModCandidateFinder implements ModCandidateFinder {
	private final Path path;

	public DirectoryModCandidateFinder(Path path) {
		this.path = path;
	}

	@Override
	public void findCandidates(Consumer<URL> urlProposer) {
		if (!Files.exists(path)) {
			try {
				Files.createDirectory(path);
			} catch (IOException e) {
				throw new RuntimeException("Could not create directory " + path, e);
			}
		}

		if (!Files.isDirectory(path)) {
			throw new RuntimeException(path + " is not a directory!");
		}

		try {
			Files.walk(path, 1).forEach((modPath) -> {
				if (!Files.isDirectory(modPath) && modPath.toString().endsWith(".jar")) {
					try {
						urlProposer.accept(UrlUtil.asUrl(modPath));
					} catch (UrlConversionException e) {
						throw new RuntimeException("Failed to convert URL for mod '" + modPath + "'!", e);
					}
				}
			});
		} catch (IOException e) {
			throw new RuntimeException("Exception while searching for mods in '" + path + "'!", e);
		}
	}
}