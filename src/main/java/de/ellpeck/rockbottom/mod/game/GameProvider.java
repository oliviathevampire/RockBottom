package de.ellpeck.rockbottom.mod.game;

import de.ellpeck.rockbottom.mod.loader.api.EnvType;
import de.ellpeck.rockbottom.mod.loader.metadata.ModMetadata;

import java.net.URL;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

public interface GameProvider {
	String getGameId();
	String getGameName();
	String getRawGameVersion();
	String getNormalizedGameVersion();
	Collection<BuiltinMod> getBuiltinMods();

	String getEntrypoint();
	Path getLaunchDirectory();
	boolean isObfuscated();
	boolean requiresUrlClassLoader();
	List<Path> getGameContextJars();

	boolean locateGame(EnvType envType, ClassLoader loader);
	void acceptArguments(String... arguments);
//	EntrypointTransformer getEntrypointTransformer();
	void launch(ClassLoader loader);

	default boolean canOpenErrorGui() {
		return true;
	}

	public static class BuiltinMod {
		public BuiltinMod(URL url, ModMetadata metadata) {
			this.url = url;
			this.metadata = metadata;
		}

		public final URL url;
		public final ModMetadata metadata;
	}
}