package de.ellpeck.rockbottom.world.tile;

import de.ellpeck.rockbottom.api.render.tile.ITileRenderer;
import de.ellpeck.rockbottom.api.tile.TallPlantTile;
import de.ellpeck.rockbottom.api.util.reg.ResourceName;
import de.ellpeck.rockbottom.render.tile.TileCornRenderer;

public class CornTile extends TallPlantTile {

    public CornTile() {
        super(ResourceName.intern("corn"));
    }

    @Override
    protected ITileRenderer createRenderer(ResourceName name) {
        return new TileCornRenderer(name);
    }
}
