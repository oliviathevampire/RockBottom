package de.ellpeck.rockbottom.mod.util;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemAlreadyExistsException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.*;

public final class FileSystemUtil {
    public static class FileSystemDelegate implements AutoCloseable {
        private final FileSystem fileSystem;
        private final boolean owner;

        public FileSystemDelegate(FileSystem fileSystem, boolean owner) {
            this.fileSystem = fileSystem;
            this.owner = owner;
        }

        public FileSystem get() {
            return fileSystem;
        }

        @Override
        public void close() throws IOException {
            if (owner) {
                fileSystem.close();
            }
        }
    }

    private FileSystemUtil() {

    }

    private static final Map<String, String> jfsArgsCreate = new HashMap<>();
    private static final Map<String, String> jfsArgsEmpty = new HashMap<>();

    static {
        jfsArgsCreate.put("create", "true");
    }

	public static FileSystemDelegate getJarFileSystem(File file, boolean create) throws IOException {
    	return getJarFileSystem(file.toURI(), create);
	}

	public static FileSystemDelegate getJarFileSystem(Path path, boolean create) throws IOException {
		return getJarFileSystem(path.toUri(), create);
	}

	public static FileSystemDelegate getJarFileSystem(URI uri, boolean create) throws IOException {
        URI jarUri;
        try {
            jarUri = new URI("jar:" + uri.getScheme(), uri.getHost(), uri.getPath(), uri.getFragment());
        } catch (URISyntaxException e) {
            throw new IOException(e);
        }

        try {
            return new FileSystemDelegate(FileSystems.newFileSystem(jarUri, create ? jfsArgsCreate : jfsArgsEmpty), true);
        } catch (FileSystemAlreadyExistsException e) {
            return new FileSystemDelegate(FileSystems.getFileSystem(jarUri), false);
        }
    }
}