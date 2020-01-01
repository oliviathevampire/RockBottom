package de.ellpeck.rockbottom.assets.loader;

import com.google.common.base.Charsets;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.ellpeck.rockbottom.api.RockBottomAPI;
import de.ellpeck.rockbottom.api.assets.IAssetLoader;
import de.ellpeck.rockbottom.api.assets.IAssetManager;
import de.ellpeck.rockbottom.api.assets.IShaderProgram;
import de.ellpeck.rockbottom.api.content.pack.ContentPack;
import de.ellpeck.rockbottom.api.mod.IMod;
import de.ellpeck.rockbottom.api.util.reg.ResourceName;
import de.ellpeck.rockbottom.assets.shader.Shader;
import de.ellpeck.rockbottom.assets.shader.ShaderProgram;
import de.ellpeck.rockbottom.content.ContentManager;
import org.lwjgl.opengl.GL20;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

public class ShaderLoader implements IAssetLoader<IShaderProgram> {

    private final Set<ResourceName> disabled = new HashSet<>();

    @Override
    public ResourceName getAssetIdentifier() {
        return IShaderProgram.ID;
    }

    @Override
    public void loadAsset(IAssetManager manager, ResourceName resourceName, String path, JsonElement element, String elementName, IMod loadingMod, ContentPack pack) throws Exception {
        if (!this.disabled.contains(resourceName)) {
            if (manager.hasAsset(IShaderProgram.ID, resourceName)) {
                RockBottomAPI.logger().info("Shader " + resourceName + " already exists, not adding shader for mod " + loadingMod.getDisplayName() + " with content pack " + pack.getName());
            } else {
                JsonObject object = element.getAsJsonObject();
                String vertexPath = object.get("vertex").getAsString();
                String fragmentPath = object.get("fragment").getAsString();

                Shader vertex = this.loadShader(path + vertexPath, GL20.GL_VERTEX_SHADER);
                Shader fragment = this.loadShader(path + fragmentPath, GL20.GL_FRAGMENT_SHADER);

                ShaderProgram shader = new ShaderProgram(vertex, fragment);
                manager.addAsset(this, resourceName, shader);
                RockBottomAPI.logger().info("Loaded shader " + resourceName + " for mod " + loadingMod.getDisplayName());
            }
        } else {
            RockBottomAPI.logger().info("Shader " + resourceName + " will not be loaded for mod " + loadingMod.getDisplayName() + " with content pack " + pack.getName() + " because it was disabled by another content pack!");
        }
    }

    @Override
    public void disableAsset(IAssetManager manager, ResourceName resourceName) {
        this.disabled.add(resourceName);
    }

    private Shader loadShader(String path, int type) throws Exception {
        String shader = "";

        BufferedReader reader = new BufferedReader(new InputStreamReader(ContentManager.getResourceAsStream(path), Charsets.UTF_8));
        while (true) {
            String line = reader.readLine();
            if (line != null) {
                shader += line + '\n';
            } else {
                break;
            }
        }
        reader.close();

        return new Shader(type, shader);
    }
}
