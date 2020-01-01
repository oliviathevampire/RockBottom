package de.ellpeck.rockbottom.mod;

import de.ellpeck.rockbottom.mod.loader.metadata.ModMetadata;
import de.ellpeck.rockbottom.mod.metadata.LoaderModMetadata;
import de.ellpeck.rockbottom.mod.util.FileSystemUtil;
import de.ellpeck.rockbottom.mod.util.UrlConversionException;
import de.ellpeck.rockbottom.mod.util.UrlUtil;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

public class ModContainer implements de.ellpeck.rockbottom.mod.loader.ModContainer {
	private final LoaderModMetadata info;
	private final URL originUrl;
	private Path root;

	public ModContainer(LoaderModMetadata info, URL originUrl) {
		this.info = info;
		this.originUrl = originUrl;
	}

	void setupRootPath() {
		if (root != null) {
			throw new RuntimeException("Not allowed to setup mod root path twice!");
		}

		try {
			Path holder = UrlUtil.asPath(originUrl).toAbsolutePath();
			if (Files.isDirectory(holder)) {
				root = holder.toAbsolutePath();
			} else /* JAR */ {
				FileSystemUtil.FileSystemDelegate delegate = FileSystemUtil.getJarFileSystem(holder, false);
				if (delegate.get() == null) {
					throw new RuntimeException("Could not open JAR file " + holder.getFileName() + " for NIO reading!");
				}

				root = delegate.get().getRootDirectories().iterator().next();

				// We never close here. It's fine. getJarFileSystem() will handle it gracefully, and so should mods
			}
		} catch (IOException | UrlConversionException e) {
			throw new RuntimeException("Failed to find root directory for mod '" + info.getId() + "'!", e);
		}
	}

	@Override
	public ModMetadata getMetadata() {
		return info;
	}

	@Override
	public Path getRootPath() {
		if (root == null) {
			throw new RuntimeException("Accessed mod root before primary loader!");
		}
		return root;
	}

	public LoaderModMetadata getInfo() {
		return info;
	}

	public URL getOriginUrl() {
		return originUrl;
	}
}