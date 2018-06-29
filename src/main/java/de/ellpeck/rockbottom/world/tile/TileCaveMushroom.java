package de.ellpeck.rockbottom.world.tile;

import de.ellpeck.rockbottom.api.render.tile.ITileRenderer;
import de.ellpeck.rockbottom.api.tile.TileMeta;
import de.ellpeck.rockbottom.api.util.BoundBox;
import de.ellpeck.rockbottom.api.util.reg.ResourceName;
import de.ellpeck.rockbottom.api.world.IWorld;
import de.ellpeck.rockbottom.api.world.layer.TileLayer;
import de.ellpeck.rockbottom.render.tile.TileCaveMushroomRenderer;

public class TileCaveMushroom extends TileMeta {

    public TileCaveMushroom() {
        super(ResourceName.intern("cave_mushroom"), false);
        for (int i = 1; i <= 7; i++) {
            this.addSubTile(this.name.addSuffix("." + i));
        }
    }

    @Override
    protected ITileRenderer createRenderer(ResourceName name) {
        return new TileCaveMushroomRenderer();
    }

    @Override
    public boolean isFullTile() {
        return false;
    }

    @Override
    public BoundBox getBoundBox(IWorld world, int x, int y, TileLayer layer) {
        return null;
    }

    @Override
    public boolean canStay(IWorld world, int x, int y, TileLayer layer, int changedX, int changedY, TileLayer changedLayer) {
        return world.getState(layer, x, y - 1).getTile().hasSolidSurface(world, x, y, layer);
    }
}
