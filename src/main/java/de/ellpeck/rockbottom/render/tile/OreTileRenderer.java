package de.ellpeck.rockbottom.render.tile;

import de.ellpeck.rockbottom.api.IGameInstance;
import de.ellpeck.rockbottom.api.IRenderer;
import de.ellpeck.rockbottom.api.StaticTileProps;
import de.ellpeck.rockbottom.api.assets.IAssetManager;
import de.ellpeck.rockbottom.api.render.tile.DefaultTileRenderer;
import de.ellpeck.rockbottom.api.tile.Tile;
import de.ellpeck.rockbottom.api.tile.state.TileState;
import de.ellpeck.rockbottom.api.util.reg.ResourceName;
import de.ellpeck.rockbottom.api.world.IWorld;
import de.ellpeck.rockbottom.api.world.layer.TileLayer;

public class OreTileRenderer<T extends Tile> extends DefaultTileRenderer<T> {

    private final ResourceName canister;
    private boolean hasCanister;

    public OreTileRenderer(ResourceName texture, boolean hasCanister) {
        super(texture);
        this.hasCanister = hasCanister;
        if(hasCanister) {
            this.canister = this.texture.addSuffix(".canister");
        } else {
            this.canister = null;
        }
    }

    @Override
    public void render(IGameInstance game, IAssetManager manager, IRenderer g, IWorld world, T tile, TileState state, int x, int y, TileLayer layer, float renderX, float renderY, float scale, int[] light) {
        if (state.get(StaticTileProps.HAS_CANISTER) && this.hasCanister) {
            manager.getTexture(this.canister).draw(renderX, renderY, scale, scale, light);
        } else {
            super.render(game, manager, g, world, tile, state, x, y, layer, renderX, renderY, scale, light);
        }
    }
}
