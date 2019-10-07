package de.ellpeck.rockbottom.world.tile;

import de.ellpeck.rockbottom.api.RockBottomAPI;
import de.ellpeck.rockbottom.api.assets.font.FormattingCode;
import de.ellpeck.rockbottom.api.entity.Entity;
import de.ellpeck.rockbottom.api.entity.player.AbstractEntityPlayer;
import de.ellpeck.rockbottom.api.net.chat.component.ChatComponentText;
import de.ellpeck.rockbottom.api.tile.Tile;
import de.ellpeck.rockbottom.api.tile.BasicTile;
import de.ellpeck.rockbottom.api.tile.state.TileState;
import de.ellpeck.rockbottom.api.util.BoundBox;
import de.ellpeck.rockbottom.api.util.Util;
import de.ellpeck.rockbottom.api.util.reg.ResourceName;
import de.ellpeck.rockbottom.api.world.IWorld;
import de.ellpeck.rockbottom.api.world.layer.TileLayer;

import java.util.List;

public class BedTile extends BasicTile {
    private final BoundBox box = new BoundBox(0.0D, 0.0D, 1.0D, 0.4166666666666667D);

    public BedTile(ResourceName name) {
        super(name);
    }

    public boolean onInteractWith(IWorld world, int x, int y, TileLayer layer, double mouseX, double mouseY, AbstractEntityPlayer player) {
        if (world.getTotalTime() >= 3000 && world.getTotalTime() <= 9000) {
            if (!RockBottomAPI.getNet().isServer()) {
                RockBottomAPI.getGame().getChatLog().displayMessage(new ChatComponentText(FormattingCode.RED + "You can only sleep at night!"));
            }
        } else {
            world.setTotalTime(3000);
        }

        return true;
    }

    @Override
    public void onCollideWithEntity(IWorld world, int x, int y, TileLayer layer, TileState state, BoundBox entityBox, BoundBox entityBoxMotion, List<BoundBox> tileBoxes, Entity entity) {
        if (x == Util.floor(entity.getX()) && entity.getY() - (double)Util.floor(entity.getY()) < 0.48D && entity.motionY < -0.15D) {
            entity.fallStartY = 0;
            entity.motionY = Math.abs(entity.motionY);
        }

        entity.fallStartY = 0;
    }

    public boolean canPlace(IWorld world, int x, int y, TileLayer layer, AbstractEntityPlayer player) {
        return world.getState(x, y - 1).getTile().isFullTile() && super.canPlace(world, x, y, layer, player);
    }

    public BoundBox getBoundBox(IWorld world, int x, int y) {
        return this.box;
    }

    public boolean isFullTile() {
        return false;
    }

    public boolean canPlaceInLayer(TileLayer layer) {
        return layer == TileLayer.MAIN && super.canPlaceInLayer(layer);
    }

    @Override
    public Tile setHardness(float hardness) {
        return this.setHardness(10.0F);
    }
}