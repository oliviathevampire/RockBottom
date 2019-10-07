package de.ellpeck.rockbottom.world.tile;

import de.ellpeck.rockbottom.api.entity.player.AbstractEntityPlayer;
import de.ellpeck.rockbottom.api.item.ItemTile;
import de.ellpeck.rockbottom.api.render.item.DefaultItemRenderer;
import de.ellpeck.rockbottom.api.render.item.IItemRenderer;
import de.ellpeck.rockbottom.api.tile.BasicTile;
import de.ellpeck.rockbottom.api.util.reg.ResourceName;
import de.ellpeck.rockbottom.api.world.IWorld;
import de.ellpeck.rockbottom.api.world.layer.TileLayer;

public class OreMaterialTile extends BasicTile {

    public OreMaterialTile(ResourceName name) {
        super(name);
    }

    @Override
    protected ItemTile createItemTile() {
        return new ItemTile(this.getName()) {
            @Override
            protected IItemRenderer createRenderer(ResourceName name) {
                return new DefaultItemRenderer(name);
            }
        };
    }

    @Override
    public boolean canPlaceInLayer(TileLayer layer) {
        return false;
    }

    @Override
    public boolean canPlace(IWorld world, int x, int y, TileLayer layer, AbstractEntityPlayer player) {
        return false;
    }
}
