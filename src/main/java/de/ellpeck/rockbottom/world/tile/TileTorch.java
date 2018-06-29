package de.ellpeck.rockbottom.world.tile;

import de.ellpeck.rockbottom.api.StaticTileProps;
import de.ellpeck.rockbottom.api.entity.player.AbstractEntityPlayer;
import de.ellpeck.rockbottom.api.item.ItemInstance;
import de.ellpeck.rockbottom.api.render.tile.ITileRenderer;
import de.ellpeck.rockbottom.api.tile.TileBasic;
import de.ellpeck.rockbottom.api.tile.state.TileState;
import de.ellpeck.rockbottom.api.util.BoundBox;
import de.ellpeck.rockbottom.api.util.Util;
import de.ellpeck.rockbottom.api.util.reg.ResourceName;
import de.ellpeck.rockbottom.api.world.IWorld;
import de.ellpeck.rockbottom.api.world.layer.TileLayer;
import de.ellpeck.rockbottom.render.tile.TileTorchRenderer;

public class TileTorch extends TileBasic {

    public TileTorch(ResourceName name) {
        super(name);
        this.addProps(StaticTileProps.TORCH_FACING, StaticTileProps.TORCH_TIMER);
    }

    public double getTurnOffChance() {
        return 0.95;
    }

    public int getMaxLight() {
        return 25;
    }

    @Override
    public void updateRandomly(IWorld world, int x, int y, TileLayer layer) {
        if (Util.RANDOM.nextDouble() >= this.getTurnOffChance()) {
            TileState state = world.getState(layer, x, y);
            if (state.get(StaticTileProps.TORCH_TIMER) < 9) {
                world.setState(layer, x, y, state.cycleProp(StaticTileProps.TORCH_TIMER));
            }
        }
    }

    @Override
    public boolean onInteractWith(IWorld world, int x, int y, TileLayer layer, double mouseX, double mouseY, AbstractEntityPlayer player) {
        TileState state = world.getState(layer, x, y);
        if (state.get(StaticTileProps.TORCH_TIMER) > 0) {
            if (!world.isClient()) {
                world.setState(layer, x, y, state.prop(StaticTileProps.TORCH_TIMER, 0));
            }
            return true;
        }
        return false;
    }

    @Override
    public int getLight(IWorld world, int x, int y, TileLayer layer) {
        int timer = world.getState(layer, x, y).get(StaticTileProps.TORCH_TIMER);
        float onPercentage = 1F - timer / 10F;
        return Util.ceil(this.getMaxLight() * onPercentage);
    }

    @Override
    public BoundBox getBoundBox(IWorld world, int x, int y, TileLayer layer) {
        return null;
    }

    @Override
    public boolean canPlaceInLayer(TileLayer layer) {
        return layer == TileLayer.MAIN;
    }

    @Override
    public boolean canPlace(IWorld world, int x, int y, TileLayer layer, AbstractEntityPlayer player) {
        return this.getTorchState(world, x, y, 0) != null;
    }

    @Override
    public TileState getPlacementState(IWorld world, int x, int y, TileLayer layer, ItemInstance instance, AbstractEntityPlayer placer) {
        return this.getTorchState(world, x, y, 0);
    }

    @Override
    public void onChangeAround(IWorld world, int x, int y, TileLayer layer, int changedX, int changedY, TileLayer changedLayer) {
        if (!world.isClient()) {
            TileState state = this.getTorchState(world, x, y, world.getState(layer, x, y).get(StaticTileProps.TORCH_TIMER));

            if (state == null) {
                world.destroyTile(x, y, layer, null, this.forceDrop);
            } else if (state != world.getState(x, y)) {
                world.setState(x, y, state);
            }
        }
    }

    @Override
    public boolean canStay(IWorld world, int x, int y, TileLayer layer, int changedX, int changedY, TileLayer changedLayer) {
        return this.getTorchState(world, x, y, 0) != null;
    }

    private TileState getTorchState(IWorld world, int x, int y, int timer) {
        int meta;

        if (world.getState(x, y - 1).getTile().hasSolidSurface(world, x, y - 1, TileLayer.MAIN)) {
            meta = 0;
        } else if (world.getState(x + 1, y).getTile().isFullTile()) {
            meta = 1;
        } else if (world.getState(x - 1, y).getTile().isFullTile()) {
            meta = 2;
        } else if (world.getState(TileLayer.BACKGROUND, x, y).getTile().isFullTile()) {
            meta = 3;
        } else {
            return null;
        }

        return this.getDefState().prop(StaticTileProps.TORCH_FACING, meta).prop(StaticTileProps.TORCH_TIMER, timer);
    }

    @Override
    public boolean isFullTile() {
        return false;
    }

    @Override
    protected ITileRenderer createRenderer(ResourceName name) {
        return new TileTorchRenderer(name);
    }
}
