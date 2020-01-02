package de.ellpeck.rockbottom.mod.game;

import de.ellpeck.rockbottom.init.AbstractGame;
import de.ellpeck.rockbottom.mod.loader.api.EnvType;
import de.ellpeck.rockbottom.mod.metadata.BuiltinModMetadata;
import de.ellpeck.rockbottom.mod.util.Arguments;

import java.io.File;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class RockBottomGameProvider implements GameProvider {

    private Arguments arguments;

    @Override
    public String getGameId() {
        return AbstractGame.ID;
    }

    @Override
    public String getGameName() {
        return AbstractGame.NAME;
    }

    @Override
    public String getRawGameVersion() {
        return AbstractGame.VERSION;
    }

    @Override
    public String getNormalizedGameVersion() {
        return AbstractGame.VERSION;
    }

    @Override
    public Collection<GameProvider.BuiltinMod> getBuiltinMods() {
        return Collections.singletonList(
                new GameProvider.BuiltinMod(null, new BuiltinModMetadata.Builder(getGameId(), getNormalizedGameVersion())
                        .setName(getGameName())
                        .build())
        );
    }

    @Override
    public String getEntrypoint() {
        return "de.ellpeck.rockbottom.Main";
    }

    @Override
    public Path getLaunchDirectory() {
        return new File(".", "rockbottom").toPath();
    }

    @Override
    public boolean isObfuscated() {
        return false;
    }

    @Override
    public boolean requiresUrlClassLoader() {
        return false;
    }

    @Override
    public List<Path> getGameContextJars() {
        return null;
    }

    @Override
    public boolean locateGame(EnvType envType, ClassLoader loader) {
        return true;
    }

    @Override
    public void acceptArguments(String... argStrings) {
        this.arguments = new Arguments();
        arguments.parse(argStrings);
    }

    @Override
    public void launch(ClassLoader loader) {
        try {
            Class<?> c = loader.loadClass("de.ellpeck.rockbottom.Main");
            Method m = c.getMethod("main", String[].class);
            m.invoke(null, (Object) arguments.toArray());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
