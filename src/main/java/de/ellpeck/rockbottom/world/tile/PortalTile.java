package de.ellpeck.rockbottom.world.tile;

import de.ellpeck.rockbottom.api.render.tile.ITileRenderer;
import de.ellpeck.rockbottom.api.tile.Tile;
import de.ellpeck.rockbottom.api.util.reg.ResourceName;
import de.ellpeck.rockbottom.render.tile.PortalTileRenderer;

public class PortalTile extends Tile {

    public PortalTile(ResourceName name) {
        super(name);
    }

    @Override
    public ITileRenderer getRenderer() {
        return new PortalTileRenderer<PortalTile>(name);
    }

}