package de.ellpeck.rockbottom.world.tile;

import de.ellpeck.rockbottom.api.GameContent;
import de.ellpeck.rockbottom.api.StaticTileProps;
import de.ellpeck.rockbottom.api.entity.Entity;
import de.ellpeck.rockbottom.api.item.ItemInstance;
import de.ellpeck.rockbottom.api.render.tile.ITileRenderer;
import de.ellpeck.rockbottom.api.util.reg.ResourceName;
import de.ellpeck.rockbottom.api.world.IWorld;
import de.ellpeck.rockbottom.api.world.layer.TileLayer;
import de.ellpeck.rockbottom.render.tile.OreTileRenderer;

import java.util.List;

public class TinTile extends OreMaterialTile {

    public TinTile() {
        super(ResourceName.intern("tin"));
        this.addProps(StaticTileProps.HAS_CANISTER);
    }

    @Override
    protected ITileRenderer createRenderer(ResourceName name) {
        return new OreTileRenderer<TinTile>(name, true);
    }

    @Override
    public List<ItemInstance> getDrops(IWorld world, int x, int y, TileLayer layer, Entity destroyer) {
        List<ItemInstance> drops = super.getDrops(world, x, y, layer, destroyer);
        if (world.getState(layer, x, y).get(StaticTileProps.HAS_CANISTER)) {
            drops.add(new ItemInstance(GameContent.ITEM_BRONZE_CANISTER));
        }
        return drops;
    }
}
