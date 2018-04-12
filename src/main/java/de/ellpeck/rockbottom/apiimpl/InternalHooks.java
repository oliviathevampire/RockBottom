package de.ellpeck.rockbottom.apiimpl;

import com.google.common.collect.Table;
import de.ellpeck.rockbottom.api.*;
import de.ellpeck.rockbottom.api.assets.IAssetManager;
import de.ellpeck.rockbottom.api.assets.font.FontProp;
import de.ellpeck.rockbottom.api.assets.font.FormattingCode;
import de.ellpeck.rockbottom.api.assets.font.IFont;
import de.ellpeck.rockbottom.api.data.settings.Settings;
import de.ellpeck.rockbottom.api.effect.ActiveEffect;
import de.ellpeck.rockbottom.api.effect.IEffect;
import de.ellpeck.rockbottom.api.entity.AbstractEntityItem;
import de.ellpeck.rockbottom.api.entity.Entity;
import de.ellpeck.rockbottom.api.entity.MovableWorldObject;
import de.ellpeck.rockbottom.api.entity.player.AbstractEntityPlayer;
import de.ellpeck.rockbottom.api.entity.player.statistics.ItemStatistic;
import de.ellpeck.rockbottom.api.event.EventResult;
import de.ellpeck.rockbottom.api.event.impl.PlaceTileEvent;
import de.ellpeck.rockbottom.api.event.impl.WorldObjectCollisionEvent;
import de.ellpeck.rockbottom.api.gui.AbstractStatGui;
import de.ellpeck.rockbottom.api.gui.GuiContainer;
import de.ellpeck.rockbottom.api.gui.component.*;
import de.ellpeck.rockbottom.api.gui.container.ContainerSlot;
import de.ellpeck.rockbottom.api.gui.container.ItemContainer;
import de.ellpeck.rockbottom.api.internal.IInternalHooks;
import de.ellpeck.rockbottom.api.item.Item;
import de.ellpeck.rockbottom.api.item.ItemInstance;
import de.ellpeck.rockbottom.api.render.item.IItemRenderer;
import de.ellpeck.rockbottom.api.tile.Tile;
import de.ellpeck.rockbottom.api.tile.TileLiquid;
import de.ellpeck.rockbottom.api.tile.state.IStateHandler;
import de.ellpeck.rockbottom.api.tile.state.TileProp;
import de.ellpeck.rockbottom.api.tile.state.TileState;
import de.ellpeck.rockbottom.api.util.BoundBox;
import de.ellpeck.rockbottom.api.util.Colors;
import de.ellpeck.rockbottom.api.util.Counter;
import de.ellpeck.rockbottom.api.util.Util;
import de.ellpeck.rockbottom.api.util.reg.ResourceName;
import de.ellpeck.rockbottom.api.world.IWorld;
import de.ellpeck.rockbottom.api.world.layer.TileLayer;
import de.ellpeck.rockbottom.net.packet.toclient.PacketEntityUpdate;
import de.ellpeck.rockbottom.net.packet.toserver.PacketSetOrPickHolding;
import de.ellpeck.rockbottom.net.packet.toserver.PacketShiftClick;
import de.ellpeck.rockbottom.world.entity.EntityItem;
import de.ellpeck.rockbottom.world.entity.player.InteractionManager;
import de.ellpeck.rockbottom.world.entity.player.statistics.StatisticList;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.util.*;
import java.util.List;

public class InternalHooks implements IInternalHooks{

    @Override
    public void doDefaultEntityUpdate(Entity entity, List<ActiveEffect> effects){
        if(!entity.isDead()){
            entity.applyMotion();

            entity.canClimb = false;
            entity.isClimbing = false;

            entity.move();

            if(entity.onGround || entity.isClimbing){
                if(entity.onGround){
                    entity.motionY = 0;
                }

                if(entity.isFalling){
                    double dist = entity.fallStartY-entity.y;
                    if(dist > 0){
                        entity.onGroundHit(dist);
                    }

                    entity.isFalling = false;
                    entity.fallStartY = 0;
                }
            }
            else if(entity.motionY < 0){
                if(!entity.isFalling){
                    entity.isFalling = true;
                    entity.fallStartY = entity.y;
                }
            }

            if(entity.collidedHor){
                entity.motionX = 0;
            }
        }
        else{
            entity.motionX = 0;
            entity.motionY = 0;
        }

        entity.ticksExisted++;

        for(int i = effects.size()-1; i >= 0; i--){
            ActiveEffect active = effects.get(i);

            IEffect effect = active.getEffect();
            if(!effect.isInstant(entity)){
                effect.updateLasting(active, entity);
            }

            active.removeTime(1);
            if(active.getTime() <= 0){
                effects.remove(i);
                effect.onRemovedOrEnded(active, entity, true);

                i--;
            }
        }

        if(entity.world.isServer()){
            if(entity.doesSync()){
                if(entity.ticksExisted%entity.getSyncFrequency() == 0){
                    if(entity.lastX != entity.x || entity.lastY != entity.y){
                        RockBottomAPI.getNet().sendToAllPlayersWithLoadedPosExcept(entity.world, new PacketEntityUpdate(entity.getUniqueId(), entity.x, entity.y, entity.motionX, entity.motionY, entity.facing), entity.x, entity.y, entity);

                        entity.lastX = entity.x;
                        entity.lastY = entity.y;
                    }
                }
            }
        }
    }

    @Override
    public void doWorldObjectMovement(MovableWorldObject object){
        if(object.motionX != 0 || object.motionY != 0){
            BoundBox ownBox = object.getBoundingBox();
            BoundBox tempBox = ownBox.copy().add(object.x, object.y);
            BoundBox tempBoxMotion = tempBox.copy().add(object.motionX, object.motionY);

            List<BoundBox> boxes = new ArrayList<>();

            for(int x = Util.floor(tempBoxMotion.getMinX()); x < Util.ceil(tempBoxMotion.getMaxX()); x++){
                for(int y = Util.floor(tempBoxMotion.getMinY()); y < Util.ceil(tempBoxMotion.getMaxY()); y++){
                    if(object.world.isPosLoaded(x, y)){
                        for(TileLayer layer : TileLayer.getAllLayers()){
                            TileState state = object.world.getState(layer, x, y);
                            List<BoundBox> tileBoxes = state.getTile().getBoundBoxes(object.world, x, y, layer, object, tempBox, tempBoxMotion);

                            if(layer.canCollide(object) && object.canCollideWithTile(state, x, y, layer)){
                                object.onTileCollision(x, y, layer, state, tempBox, tempBoxMotion, tileBoxes);
                                boxes.addAll(tileBoxes);
                            }

                            object.onTileIntersection(x, y, layer, state, tempBox, tempBoxMotion, tileBoxes);
                        }
                    }
                }
            }

            List<Entity> entities = object.world.getEntities(tempBoxMotion);
            for(Entity entity : entities){
                BoundBox entityTempBox = entity.getBoundingBox().copy().add(entity.x, entity.y);
                BoundBox entityTempBoxMotion = entityTempBox.copy().add(entity.motionX, entity.motionY);

                if(entity.canCollideWith(object, tempBox, tempBoxMotion)){
                    object.onEntityCollision(entity, tempBox, tempBoxMotion, entityTempBox, entityTempBoxMotion);
                    boxes.add(entityTempBox);
                }

                object.onEntityIntersection(entity, tempBox, tempBoxMotion, entityTempBox, entityTempBoxMotion);
            }

            RockBottomAPI.getEventHandler().fireEvent(new WorldObjectCollisionEvent(object, tempBoxMotion, boxes));

            double motionY = object.motionY;
            if(motionY != 0){
                if(!boxes.isEmpty()){
                    for(BoundBox box : boxes){
                        if(motionY != 0){
                            if(!box.isEmpty()){
                                if(tempBox.getMaxX() > box.getMinX() && tempBox.getMinX() < box.getMaxX()){
                                    if(motionY > 0 && tempBox.getMaxY() <= box.getMinY()){
                                        double diff = box.getMinY()-tempBox.getMaxY();
                                        if(diff < motionY){
                                            motionY = diff;
                                        }
                                    }
                                    else if(motionY < 0 && tempBox.getMinY() >= box.getMaxY()){
                                        double diff = box.getMaxY()-tempBox.getMinY();
                                        if(diff > motionY){
                                            motionY = diff;
                                        }
                                    }
                                }
                            }
                        }
                        else{
                            break;
                        }
                    }
                }

                object.y += motionY;
            }

            double motionX = object.motionX;
            if(motionX != 0){
                if(!boxes.isEmpty()){
                    tempBox.set(ownBox).add(object.x, object.y);
                    for(BoundBox box : boxes){
                        if(motionX != 0){
                            if(!box.isEmpty()){
                                if(tempBox.getMaxY() > box.getMinY() && tempBox.getMinY() < box.getMaxY()){
                                    if(motionX > 0 && tempBox.getMaxX() <= box.getMinX()){
                                        double diff = box.getMinX()-tempBox.getMaxX();
                                        if(diff < motionX){
                                            motionX = Math.round(diff);
                                        }
                                    }
                                    else if(motionX < 0 && tempBox.getMinX() >= box.getMaxX()){
                                        double diff = box.getMaxX()-tempBox.getMinX();
                                        if(diff > motionX){
                                            motionX = Math.round(diff);
                                        }
                                    }
                                }
                            }
                        }
                        else{
                            break;
                        }
                    }
                }

                object.x += motionX;
            }

            object.collidedHor = motionX != object.motionX;
            object.collidedVert = motionY != object.motionY;
            object.onGround = object.collidedVert && object.motionY < 0;
        }
    }

    @Override
    public boolean doDefaultSlotMovement(IGameInstance game, int button, float x, float y, GuiContainer gui, ComponentSlot slot){
        boolean isSecond = Settings.KEY_GUI_ACTION_2.isKey(button);
        if(isSecond || Settings.KEY_GUI_ACTION_1.isKey(button)){
            ItemContainer container = gui.getContainer();
            return setOrPickUpHolding(gui.player, container, container.getIdForSlot(slot.slot), isSecond);
        }
        return false;
    }

    public static boolean setOrPickUpHolding(AbstractEntityPlayer player, ItemContainer container, int slotId, boolean half){
        if(player.world.isClient()){
            RockBottomAPI.getNet().sendToServer(new PacketSetOrPickHolding(player.getUniqueId(), slotId, half));
        }

        ContainerSlot slot = container.getSlot(slotId);
        ItemInstance slotInst = slot.get();

        if(half){
            if(container.holdingInst == null){
                if(slotInst != null){
                    int halfAmount = Util.ceil(slot.get().getAmount()/2D);
                    if(slot.canRemove(halfAmount)){
                        container.holdingInst = slotInst.copy().setAmount(halfAmount);
                        slot.set(slotInst.removeAmount(halfAmount).nullIfEmpty());
                        return true;
                    }
                }
            }
            else{
                boolean should = false;

                if(slotInst != null){
                    if(slotInst.isEffectivelyEqual(container.holdingInst) && slotInst.getAmount() < slotInst.getMaxAmount()){
                        should = true;

                        ItemInstance newInst = slotInst.addAmount(1);
                        if(slot.canPlace(newInst)){
                            slot.set(newInst);
                        }
                    }
                }
                else{
                    should = true;

                    ItemInstance newInst = container.holdingInst.copy().setAmount(1);
                    if(slot.canPlace(newInst)){
                        slot.set(newInst);
                    }
                }

                if(should){
                    container.holdingInst = container.holdingInst.removeAmount(1).nullIfEmpty();
                    return true;
                }
            }
        }
        else{
            if(container.holdingInst == null){
                if(slotInst != null && slot.canRemove(slotInst.getAmount())){
                    container.holdingInst = slotInst;
                    slot.set(null);
                    return true;
                }
            }
            else{
                int removeAmount = 0;

                if(slotInst != null){
                    if(slotInst.isEffectivelyEqual(container.holdingInst)){
                        int possibleAdd = Math.min(slotInst.getMaxAmount()-slotInst.getAmount(), container.holdingInst.getAmount());
                        if(possibleAdd > 0){
                            ItemInstance newInst = slotInst.copy().addAmount(possibleAdd);
                            if(slot.canPlace(newInst)){
                                removeAmount = possibleAdd;
                                slot.set(newInst);
                            }
                        }
                    }
                    else if(slot.canRemove(slotInst.getAmount()) && slot.canPlace(container.holdingInst)){
                        ItemInstance slotCopy = slotInst.copy();
                        slot.set(container.holdingInst);
                        container.holdingInst = slotCopy;
                        return true;
                    }
                }
                else if(slot.canPlace(container.holdingInst)){
                    removeAmount = container.holdingInst.getAmount();
                    slot.set(container.holdingInst.copy());
                }

                if(removeAmount > 0){
                    container.holdingInst = container.holdingInst.removeAmount(removeAmount).nullIfEmpty();
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean doDefaultShiftClicking(IGameInstance game, int button, GuiContainer gui, ComponentSlot slot){
        if(Settings.KEY_GUI_ACTION_1.isKey(button)){
            ItemContainer container = gui.getContainer();
            ItemInstance remaining = slot.slot.get();

            if(remaining != null){
                boolean modified = false;
                for(GuiContainer.ShiftClickBehavior behavior : gui.shiftClickBehaviors){
                    if(behavior.slots.contains(slot.componentId)){
                        for(int slotInto : behavior.slotsInto){
                            GuiComponent comp = gui.getComponents().get(slotInto);
                            if(comp instanceof ComponentSlot){
                                ComponentSlot intoSlot = (ComponentSlot)comp;
                                if(behavior.condition == null || behavior.condition.apply(slot.slot, intoSlot.slot)){
                                    int result = shiftClick(gui.player, container, container.getIdForSlot(slot.slot), container.getIdForSlot(intoSlot.slot));

                                    if(result == 1){
                                        return true;
                                    }
                                    else if(result == 2){
                                        modified = true;
                                    }
                                }
                            }
                        }
                    }
                }
                return modified;
            }
        }
        return false;
    }

    public static int shiftClick(AbstractEntityPlayer player, ItemContainer container, int from, int into){
        if(player.world.isClient()){
            RockBottomAPI.getNet().sendToServer(new PacketShiftClick(player.getUniqueId(), from, into));
        }

        ContainerSlot slotFrom = container.getSlot(from);
        ContainerSlot slotInto = container.getSlot(into);

        ItemInstance fromInst = slotFrom.get();
        ItemInstance intoInst = slotInto.get();

        if(intoInst == null){
            if(slotInto.canPlace(fromInst) && slotFrom.canRemove(fromInst.getAmount())){
                slotInto.set(fromInst);
                slotFrom.set(null);
                return 1;
            }
        }
        else if(intoInst.isEffectivelyEqual(fromInst)){
            int possible = Math.min(intoInst.getMaxAmount()-intoInst.getAmount(), fromInst.getAmount());
            if(possible > 0 && slotFrom.canRemove(possible)){
                ItemInstance newInto = intoInst.copy().addAmount(possible);
                if(slotInto.canPlace(newInto)){
                    int remaining = fromInst.getAmount()-possible;
                    slotInto.set(newInto);
                    if(remaining <= 0){
                        slotFrom.set(null);
                        return 1;
                    }
                    else{
                        slotFrom.set(fromInst.copy().setAmount(remaining));
                        return 2;
                    }
                }
            }
        }
        return 0;
    }

    @Override
    public boolean placeTile(int x, int y, TileLayer layer, AbstractEntityPlayer player, ItemInstance selected, Tile tile, boolean removeItem, boolean simulate){
        if(layer != TileLayer.MAIN || player.world.getEntities(new BoundBox(x, y, x+1, y+1), entity -> !(entity instanceof AbstractEntityItem)).isEmpty()){
            if(layer.canTileBeInLayer(player.world, x, y, tile)){
                Tile tileThere = player.world.getState(layer, x, y).getTile();
                if(tileThere != tile && tileThere.canReplace(player.world, x, y, layer)){
                    if(InteractionManager.defaultTilePlacementCheck(player.world, x, y, layer, tile) && tile.canPlace(player.world, x, y, layer, player)){
                        if(!simulate){
                            PlaceTileEvent event = new PlaceTileEvent(player, selected, removeItem, layer, x, y);
                            if(RockBottomAPI.getEventHandler().fireEvent(event) != EventResult.CANCELLED){
                                selected = event.instance;
                                layer = event.layer;
                                removeItem = event.removeItem;
                                x = event.x;
                                y = event.y;

                                tile.doPlace(player.world, x, y, layer, selected, player);

                                if(!player.world.isClient()){
                                    Item item = tile.getItem();
                                    if(item != null){
                                        player.getStatistics().getOrInit(StatisticList.TILES_PLACED, ItemStatistic.class).update(item);
                                    }

                                    if(removeItem){
                                        player.getInv().set(player.getSelectedSlot(), selected.removeAmount(1).nullIfEmpty());
                                    }

                                    ResourceName sound = tile.getPlaceSound(player.world, x, y, layer, player, player.world.getState(layer, x, y));
                                    if(sound != null){
                                        player.world.playSound(sound, x+0.5, y+0.5, layer.index(), 1F, 1F);
                                    }
                                }
                            }
                        }
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public void doDefaultLiquidBehavior(IWorld world, int x, int y, TileLayer layer, TileLiquid tile){
        TileState ourState = world.getState(layer, x, y);
        int ourLevel = ourState.get(tile.level)+1;
        if(!world.isPosLoaded(x, y-1)){
            return;
        }
        // Check down
        if(world.getState(x, y-1).getTile().canLiquidSpreadInto(world, x, y-1, tile)){
            TileState beneathState = world.getState(layer, x, y-1);
            if(beneathState.getTile() == tile){
                // Liquid beneath us
                int otherLevel = beneathState.get(tile.level)+1;
                int remaining = ourLevel-tile.getLevels()+otherLevel;
                if(remaining < tile.getLevels() && tile.getLevels()-otherLevel != 0){ // If more liquid can fit beneath us
                    if(remaining > 0){
                        // Transfer one unit to the liquid beneath us
                        world.setState(layer, x, y, ourState.prop(tile.level, remaining-1));
                        world.setState(layer, x, y-1, beneathState.prop(tile.level, tile.getLevels()-1));
                        return;
                    }
                    else{
                        // Transfer our last unit to the liquid beneath us and remove this liquid
                        world.setState(layer, x, y, GameContent.TILE_AIR.getDefState());
                        world.setState(layer, x, y-1, beneathState.prop(tile.level, otherLevel+ourLevel-1));
                        return;
                    }
                }
                else{
                    if(!world.isPosLoaded(x+1, y-1) || !world.isPosLoaded(x-1, y-1)){
                        return;
                    }
                    TileState leftState = world.getState(layer, x-1, y-1);
                    TileState rightState = world.getState(layer, x+1, y-1);
                    // Try to balance to the sides
                    if(Util.RANDOM.nextBoolean()){
                        if(this.transferDown(world, layer, ourState, leftState, x, y, -1, tile) || this.transferDown(world, layer, ourState, rightState, x, y, 1, tile)){
                            return;
                        }
                    }
                    else{
                        if(this.transferDown(world, layer, ourState, rightState, x, y, 1, tile) || this.transferDown(world, layer, ourState, leftState, x, y, -1, tile)){
                            return;
                        }
                    }
                }
            }
            else if(beneathState.getTile().isAir()){
                // Nothing beneath us move down
                world.setState(layer, x, y-1, ourState);
                world.setState(layer, x, y, GameContent.TILE_AIR.getDefState());
                return;
            }
            // Fall through to the balancing and spreading logic
        }

        // Balance and spread
        if(!world.isPosLoaded(x-1, y) || !world.isPosLoaded(x+1, y)){
            return;
        }
        TileState leftState = world.getState(layer, x-1, y);
        TileState rightState = world.getState(layer, x+1, y);
        boolean oneToSpare;
        if(Util.RANDOM.nextBoolean()){
            // Left first
            oneToSpare = this.balanceAndSpread(world, layer, leftState, rightState, ourState, ourLevel, x, y, -1, false, tile);
            ourState = world.getState(layer, x, y);
            ourLevel = ourState.get(tile.level)+1;
            // Right second
            boolean found = this.balanceAndSpread(world, layer, rightState, leftState, ourState, ourLevel, x, y, 1, oneToSpare, tile);
            if(found != oneToSpare){ // We need to check left again
                this.balanceAndSpread(world, layer, leftState, rightState, ourState, ourLevel, x, y, -1, true, tile);
            }
        }
        else{
            // Right first
            oneToSpare = this.balanceAndSpread(world, layer, rightState, leftState, ourState, ourLevel, x, y, 1, false, tile);
            ourState = world.getState(layer, x, y);
            ourLevel = ourState.get(tile.level)+1;
            // Left second
            boolean found = this.balanceAndSpread(world, layer, leftState, rightState, ourState, ourLevel, x, y, -1, oneToSpare, tile);
            if(found != oneToSpare){ // We need to check right again
                this.balanceAndSpread(world, layer, rightState, leftState, ourState, ourLevel, x, y, 1, true, tile);
            }
        }

    }

    @Override
    public String getKeyOrMouseName(int key){
        switch(key){
            case GLFW.GLFW_MOUSE_BUTTON_1:
                return this.localizeKey("mouse.left");
            case GLFW.GLFW_MOUSE_BUTTON_2:
                return this.localizeKey("mouse.right");
            case GLFW.GLFW_MOUSE_BUTTON_3:
                return this.localizeKey("mouse.3");
            case GLFW.GLFW_MOUSE_BUTTON_4:
                return this.localizeKey("mouse.4");
            case GLFW.GLFW_MOUSE_BUTTON_5:
                return this.localizeKey("mouse.5");
            case GLFW.GLFW_MOUSE_BUTTON_6:
                return this.localizeKey("mouse.6");
            case GLFW.GLFW_MOUSE_BUTTON_7:
                return this.localizeKey("mouse.7");
            case GLFW.GLFW_MOUSE_BUTTON_8:
                return this.localizeKey("mouse.8");

            case GLFW.GLFW_KEY_A:
                return "A";
            case GLFW.GLFW_KEY_B:
                return "B";
            case GLFW.GLFW_KEY_C:
                return "C";
            case GLFW.GLFW_KEY_D:
                return "D";
            case GLFW.GLFW_KEY_E:
                return "E";
            case GLFW.GLFW_KEY_F:
                return "F";
            case GLFW.GLFW_KEY_G:
                return "G";
            case GLFW.GLFW_KEY_H:
                return "H";
            case GLFW.GLFW_KEY_I:
                return "I";
            case GLFW.GLFW_KEY_J:
                return "J";
            case GLFW.GLFW_KEY_K:
                return "K";
            case GLFW.GLFW_KEY_L:
                return "L";
            case GLFW.GLFW_KEY_M:
                return "M";
            case GLFW.GLFW_KEY_N:
                return "N";
            case GLFW.GLFW_KEY_O:
                return "O";
            case GLFW.GLFW_KEY_P:
                return "P";
            case GLFW.GLFW_KEY_Q:
                return "Q";
            case GLFW.GLFW_KEY_R:
                return "R";
            case GLFW.GLFW_KEY_S:
                return "S";
            case GLFW.GLFW_KEY_T:
                return "T";
            case GLFW.GLFW_KEY_U:
                return "U";
            case GLFW.GLFW_KEY_V:
                return "V";
            case GLFW.GLFW_KEY_W:
                return "W";
            case GLFW.GLFW_KEY_X:
                return "X";
            case GLFW.GLFW_KEY_Y:
                return "Y";
            case GLFW.GLFW_KEY_Z:
                return "Z";
            case GLFW.GLFW_KEY_1:
                return "1";
            case GLFW.GLFW_KEY_2:
                return "2";
            case GLFW.GLFW_KEY_3:
                return "3";
            case GLFW.GLFW_KEY_4:
                return "4";
            case GLFW.GLFW_KEY_5:
                return "5";
            case GLFW.GLFW_KEY_6:
                return "6";
            case GLFW.GLFW_KEY_7:
                return "7";
            case GLFW.GLFW_KEY_8:
                return "8";
            case GLFW.GLFW_KEY_9:
                return "9";
            case GLFW.GLFW_KEY_0:
                return "0";
            case GLFW.GLFW_KEY_SPACE:
                return this.localizeKey("space");
            case GLFW.GLFW_KEY_MINUS:
                return this.localizeKey("minus");
            case GLFW.GLFW_KEY_EQUAL:
                return this.localizeKey("equals");
            case GLFW.GLFW_KEY_LEFT_BRACKET:
                return this.localizeKey("left_bracket");
            case GLFW.GLFW_KEY_RIGHT_BRACKET:
                return this.localizeKey("right_bracket");
            case GLFW.GLFW_KEY_BACKSLASH:
                return this.localizeKey("backslash");
            case GLFW.GLFW_KEY_SEMICOLON:
                return this.localizeKey("semicolon");
            case GLFW.GLFW_KEY_APOSTROPHE:
                return this.localizeKey("apostrophe");
            case GLFW.GLFW_KEY_GRAVE_ACCENT:
                return this.localizeKey("grave");
            case GLFW.GLFW_KEY_COMMA:
                return this.localizeKey("comma");
            case GLFW.GLFW_KEY_PERIOD:
                return this.localizeKey("period");
            case GLFW.GLFW_KEY_SLASH:
                return this.localizeKey("slash");
            case GLFW.GLFW_KEY_WORLD_1:
                return this.localizeKey("world_1");
            case GLFW.GLFW_KEY_WORLD_2:
                return this.localizeKey("world_2");

            case GLFW.GLFW_KEY_ESCAPE:
                return this.localizeKey("escape");
            case GLFW.GLFW_KEY_F1:
                return "F1";
            case GLFW.GLFW_KEY_F2:
                return "F2";
            case GLFW.GLFW_KEY_F3:
                return "F3";
            case GLFW.GLFW_KEY_F4:
                return "F4";
            case GLFW.GLFW_KEY_F5:
                return "F5";
            case GLFW.GLFW_KEY_F6:
                return "F6";
            case GLFW.GLFW_KEY_F7:
                return "F7";
            case GLFW.GLFW_KEY_F8:
                return "F8";
            case GLFW.GLFW_KEY_F9:
                return "F9";
            case GLFW.GLFW_KEY_F10:
                return "F10";
            case GLFW.GLFW_KEY_F11:
                return "F11";
            case GLFW.GLFW_KEY_F12:
                return "F12";
            case GLFW.GLFW_KEY_F13:
                return "F13";
            case GLFW.GLFW_KEY_F14:
                return "F14";
            case GLFW.GLFW_KEY_F15:
                return "F15";
            case GLFW.GLFW_KEY_F16:
                return "F16";
            case GLFW.GLFW_KEY_F17:
                return "F17";
            case GLFW.GLFW_KEY_F18:
                return "F18";
            case GLFW.GLFW_KEY_F19:
                return "F19";
            case GLFW.GLFW_KEY_F20:
                return "F20";
            case GLFW.GLFW_KEY_F21:
                return "F21";
            case GLFW.GLFW_KEY_F22:
                return "F22";
            case GLFW.GLFW_KEY_F23:
                return "F23";
            case GLFW.GLFW_KEY_F24:
                return "F24";
            case GLFW.GLFW_KEY_F25:
                return "F25";
            case GLFW.GLFW_KEY_UP:
                return this.localizeKey("up");
            case GLFW.GLFW_KEY_DOWN:
                return this.localizeKey("down");
            case GLFW.GLFW_KEY_LEFT:
                return this.localizeKey("left");
            case GLFW.GLFW_KEY_RIGHT:
                return this.localizeKey("right");
            case GLFW.GLFW_KEY_LEFT_SHIFT:
                return this.localizeKey("left_shift");
            case GLFW.GLFW_KEY_RIGHT_SHIFT:
                return this.localizeKey("right_shift");
            case GLFW.GLFW_KEY_LEFT_CONTROL:
                return this.localizeKey("left_control");
            case GLFW.GLFW_KEY_RIGHT_CONTROL:
                return this.localizeKey("right_control");
            case GLFW.GLFW_KEY_LEFT_ALT:
                return this.localizeKey("left_alt");
            case GLFW.GLFW_KEY_RIGHT_ALT:
                return this.localizeKey("right_alt");
            case GLFW.GLFW_KEY_TAB:
                return this.localizeKey("tab");
            case GLFW.GLFW_KEY_ENTER:
                return this.localizeKey("enter");
            case GLFW.GLFW_KEY_BACKSPACE:
                return this.localizeKey("backspace");
            case GLFW.GLFW_KEY_INSERT:
                return this.localizeKey("insert");
            case GLFW.GLFW_KEY_DELETE:
                return this.localizeKey("delete");
            case GLFW.GLFW_KEY_PAGE_UP:
                return this.localizeKey("page_up");
            case GLFW.GLFW_KEY_PAGE_DOWN:
                return this.localizeKey("page_down");
            case GLFW.GLFW_KEY_HOME:
                return this.localizeKey("home");
            case GLFW.GLFW_KEY_END:
                return this.localizeKey("end");
            case GLFW.GLFW_KEY_KP_0:
                return this.localizeKey("keypad.0");
            case GLFW.GLFW_KEY_KP_1:
                return this.localizeKey("keypad.1");
            case GLFW.GLFW_KEY_KP_2:
                return this.localizeKey("keypad.2");
            case GLFW.GLFW_KEY_KP_3:
                return this.localizeKey("keypad.3");
            case GLFW.GLFW_KEY_KP_4:
                return this.localizeKey("keypad.4");
            case GLFW.GLFW_KEY_KP_5:
                return this.localizeKey("keypad.5");
            case GLFW.GLFW_KEY_KP_6:
                return this.localizeKey("keypad.6");
            case GLFW.GLFW_KEY_KP_7:
                return this.localizeKey("keypad.7");
            case GLFW.GLFW_KEY_KP_8:
                return this.localizeKey("keypad.8");
            case GLFW.GLFW_KEY_KP_9:
                return this.localizeKey("keypad.9");
            case GLFW.GLFW_KEY_KP_DIVIDE:
                return this.localizeKey("keypad.divide");
            case GLFW.GLFW_KEY_KP_MULTIPLY:
                return this.localizeKey("keypad.multiply");
            case GLFW.GLFW_KEY_KP_SUBTRACT:
                return this.localizeKey("keypad.subtract");
            case GLFW.GLFW_KEY_KP_ADD:
                return this.localizeKey("keypad.add");
            case GLFW.GLFW_KEY_KP_DECIMAL:
                return this.localizeKey("keypad.decimal");
            case GLFW.GLFW_KEY_KP_EQUAL:
                return this.localizeKey("keypad.equals");
            case GLFW.GLFW_KEY_KP_ENTER:
                return this.localizeKey("keypad.enter");
            case GLFW.GLFW_KEY_PRINT_SCREEN:
                return this.localizeKey("print");
            case GLFW.GLFW_KEY_NUM_LOCK:
                return this.localizeKey("num_lock");
            case GLFW.GLFW_KEY_CAPS_LOCK:
                return this.localizeKey("caps_lock");
            case GLFW.GLFW_KEY_SCROLL_LOCK:
                return this.localizeKey("scroll_lock");
            case GLFW.GLFW_KEY_PAUSE:
                return this.localizeKey("pause");
            case GLFW.GLFW_KEY_LEFT_SUPER:
                return this.localizeKey("left_super");
            case GLFW.GLFW_KEY_RIGHT_SUPER:
                return this.localizeKey("right_super");
            case GLFW.GLFW_KEY_MENU:
                return this.localizeKey("menu");

            default:
                return this.localizeKey("unknown");
        }
    }

    private String localizeKey(String name){
        return RockBottomAPI.getGame().getAssetManager().localize(ResourceName.intern("key_name."+name));
    }

    // Direction: 1 = right, -1 = left
    private boolean balanceAndSpread(IWorld world, TileLayer layer, TileState otherState, TileState oppositeState, TileState ourState, int ourLevel, int x, int y, int direction, boolean oneToSpare, TileLiquid tile){
        if(world.getState(x+direction, y).getTile().canLiquidSpreadInto(world, x+direction, y, tile)){
            if(otherState.getTile() == tile){
                // Balance with left
                int otherLevel = otherState.get(tile.level)+1;
                if(otherLevel > ourLevel){
                    if(otherLevel-ourLevel > 1){
                        this.transfer(world, layer, ourLevel, otherState, ourState, x+direction, x, y, tile);
                    }
                    else{
                        // Remember for balancing
                        return true;
                    }
                }
                else if(otherLevel < ourLevel){
                    if(otherLevel-ourLevel < -1){
                        this.transfer(world, layer, otherLevel, ourState, otherState, x, x+direction, y, tile);
                    }
                    else if(oneToSpare){ // If we have one to spare we can transfer it here
                        this.transfer(world, layer, otherLevel, oppositeState, otherState, x-direction, x+direction, y, tile);
                    }
                    else{ // Check if we have one to spare further away
                        if(!world.isPosLoaded(x-direction*2, y)){
                            return false; // Maybe this should be an exception being thrown to completely cancel the update. I am not doing this however because exceptions are really bad for performance
                        }
                        TileState farState = world.getState(layer, x-direction*2, y);
                        if(farState.getTile() == tile){
                            if(farState.get(tile.level)-otherLevel > 0){
                                this.transfer(world, layer, otherLevel, farState, otherState, x-direction*2, x+direction, y, tile);
                            }
                        }
                    }
                }
            }
            else if(otherState.getTile().isAir()){
                if(ourLevel > 1){
                    // Spread
                    this.spread(world, layer, ourLevel, ourState, x, y, direction, tile);
                }
            }
        }
        return oneToSpare;
    }

    // Direction: 1 = right, -1 = left
    private void spread(IWorld world, TileLayer layer, int ourLevel, TileState ourState, int x, int y, int direction, TileLiquid tile){
        world.setState(layer, x+direction, y, tile.getDefState()); // Place one unit
        world.setState(layer, x, y, ourState.prop(tile.level, ourLevel-2)); // Decrease our level
    }

    // Direction: 1 = right, -1 = left
    private void transfer(IWorld world, TileLayer layer, int secondLevel, TileState firstState, TileState secondState, int x1, int x2, int y, TileLiquid tile){
        world.setState(layer, x1, y, firstState.prop(tile.level, firstState.get(tile.level)-1)); // Decrease first by one
        world.setState(layer, x2, y, secondState.prop(tile.level, secondLevel)); // Increase second by one
        world.scheduleUpdate(x2, y, layer, tile.getFlowSpeed());
    }

    private boolean transferDown(IWorld world, TileLayer layer, TileState firstState, TileState secondState, int x, int y, int direction, TileLiquid tile){
        boolean empty = false;
        if(secondState.getTile() == tile){
            int secondLevel = secondState.get(tile.level)+1;
            if(secondLevel < tile.getLevels()){
                if(firstState.get(tile.level) == 0){
                    world.setState(layer, x, y, GameContent.TILE_AIR.getDefState());
                    empty = true;
                }
                else{
                    world.setState(layer, x, y, firstState.prop(tile.level, firstState.get(tile.level)-1)); // Decrease first by one
                }
                world.setState(layer, x+direction, y-1, secondState.prop(tile.level, secondLevel)); // Increase second by one
            }
        }
        return empty;
    }

    @Override
    public boolean doInputFieldKeyPress(IGameInstance game, int button, ComponentInputField field){
        if(field.isSelected()){
            if(button == GLFW.GLFW_KEY_BACKSPACE){
                if(!field.getText().isEmpty()){
                    field.setText(field.getText().substring(0, field.getText().length()-1));
                }
                return true;
            }
            else if(button == GLFW.GLFW_KEY_ESCAPE){
                if(field.selectable){
                    field.setSelected(false);
                    return true;
                }
            }
            else{
                IInputHandler input = game.getInput();
                if(input.isKeyDown(GLFW.GLFW_KEY_LEFT_CONTROL) || input.isKeyDown(GLFW.GLFW_KEY_RIGHT_CONTROL)){
                    if(button == GLFW.GLFW_KEY_V){
                        try{
                            String data = (String)Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);

                            int space = this.getTextSpace(game, field);
                            if(space < data.length()){
                                data = data.substring(0, space);
                            }

                            field.setText(field.getText()+data);

                            return true;
                        }
                        catch(Exception ignored){
                        }
                        return false;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public boolean doInputFieldCharInput(IGameInstance game, char[] characters, ComponentInputField field){
        boolean did = false;
        if(field.isSelected()){
            for(char character : characters){
                if(character >= 32 && character <= 254){
                    if(this.getTextSpace(game, field) > 0){
                        field.setText(field.getText()+character);
                        did = true;
                    }
                }
            }
        }
        return did;
    }

    @Override
    public void doInputFieldRender(IGameInstance game, IAssetManager manager, IRenderer g, int x, int y, ComponentInputField field){
        if(field.renderBox){
            g.addFilledRect(x, y, field.getWidth(), field.getHeight(), field.isMouseOverPrioritized(game) ? GuiComponent.getElementColor() : GuiComponent.getUnselectedElementColor());
            g.addEmptyRect(x, y, field.getWidth(), field.getHeight(), GuiComponent.getElementOutlineColor());
        }

        IFont font = manager.getFont();

        String text = field.getDisplayText();
        if(field.isCensored()){
            char[] chars = new char[text.length()];
            Arrays.fill(chars, '*');
            text = new String(chars);
        }

        String display = text+(field.isSelected() ? ((game.getTotalTicks()/15)%2 == 0 ? "|" : " ") : "");
        font.drawCutOffString(x+3, y+field.getHeight()/2F-font.getHeight(0.35F)/2F, display, 0.35F, field.getWidth()-6, true, false);

        if(field.displaxMaxLength){
            int space = this.getTextSpace(game, field);
            FormattingCode format = space <= 0 ? FormattingCode.RED : (space <= field.maxLength/8 ? FormattingCode.ORANGE : (space <= field.maxLength/4 ? FormattingCode.YELLOW : FormattingCode.NONE));
            font.drawStringFromRight(x+field.getWidth()-1, y+field.getHeight()-font.getHeight(0.2F), format.toString()+(field.maxLength-space)+'/'+field.maxLength, 0.2F);
        }
    }

    private int getTextSpace(IGameInstance game, ComponentInputField field){
        return field.maxLength-game.getAssetManager().getFont().removeFormatting(field.getText()).length();
    }

    @Override
    public void doTileStateInit(TileState thisState, ResourceName name, Tile tile, Map<String, Comparable> properties, Table<String, Comparable, TileState> subStates){
        RockBottomAPI.TILE_STATE_REGISTRY.register(name, thisState);

        for(TileProp prop : tile.getProps()){
            String propName = prop.getName();
            for(int i = 0; i < prop.getVariants(); i++){
                Comparable value = prop.getValue(i);
                if(!properties.get(propName).equals(value)){
                    Map<String, Comparable> subProps = new TreeMap<>(properties);
                    subProps.put(propName, value);

                    ResourceName subName = generateTileStateName(tile, subProps);
                    if(tile.hasState(subName, subProps)){
                        TileState state = RockBottomAPI.TILE_STATE_REGISTRY.get(subName);

                        if(state == null){
                            state = new TileState(subName, tile, subProps);
                        }

                        subStates.put(propName, value, state);
                    }
                }
            }
        }
    }

    public static ResourceName generateTileStateName(Tile tile, Map<String, Comparable> properties){
        StringBuilder suffix = new StringBuilder();

        if(!properties.isEmpty()){
            suffix.append(';');

            Iterator<Map.Entry<String, Comparable>> iterator = properties.entrySet().iterator();
            while(iterator.hasNext()){
                Map.Entry<String, Comparable> entry = iterator.next();

                String append = entry.getKey()+'@'+entry.getValue();
                if(iterator.hasNext()){
                    append += ",";
                }

                suffix.append(append);
            }
        }

        return tile.getName().addSuffix(suffix.toString());
    }

    @Override
    public IStateHandler makeStateHandler(Tile tile){
        return new StateHandler(tile);
    }

    @Override
    public FormattingCode getFormattingCode(String s, int index, Map<Character, FormattingCode> defaults){
        if(s.length() > index+1 && s.charAt(index) == '&'){
            char formatChar = s.charAt(index+1);

            if(formatChar == '('){
                int closingIndex = s.indexOf(")", index+2);
                if(closingIndex > index+2){
                    String code = s.substring(index+2, closingIndex);
                    String[] colors = code.split(",");

                    if(colors.length == 3){
                        try{
                            return new FormattingCode(' ', Colors.rgb(Float.parseFloat(colors[0]), Float.parseFloat(colors[1]), Float.parseFloat(colors[2])), FontProp.NONE, code.length()+3, "&("+code+')');
                        }
                        catch(Exception ignored){
                        }
                    }
                }
            }
            else if(formatChar == 'r'){
                return new FormattingCode('r', Colors.rainbow((Util.getTimeMillis()/10)%256));
            }
            else{
                FormattingCode def = defaults.get(formatChar);
                if(def != null){
                    return def;
                }
            }
        }
        return FormattingCode.NONE;
    }

    @Override
    public AbstractEntityItem makeItem(IWorld world, ItemInstance inst, double x, double y, double motionX, double motionY){
        return new EntityItem(world, inst);
    }

    @Override
    public List<ComponentStatistic> makeItemStatComponents(IGameInstance game, ItemStatistic.Stat stat, Map<Item, Counter> statMap, AbstractStatGui gui, ComponentMenu menu, ResourceName textureLocation){
        return Collections.singletonList(new ComponentStatistic(gui, () -> game.getAssetManager().localize(stat.getInitializer().getName().addPrefix("stat.")), () -> String.valueOf(stat.getTotal()), stat.getTotal(), () -> {
            List<ComponentStatistic> list = new ArrayList<>();

            for(Map.Entry<Item, Counter> entry : statMap.entrySet()){
                Item item = entry.getKey();
                Counter value = entry.getValue();

                ItemInstance instance = new ItemInstance(item);
                String statName = game.getAssetManager().localize(stat.getInitializer().getName().addPrefix("stat.").addSuffix("_per_tile"));

                list.add(new ComponentStatistic(gui, () -> {
                    instance.setMeta((game.getTotalTicks()/Constants.TARGET_TPS)%(item.getHighestPossibleMeta()+1));
                    return String.format(statName, instance.getDisplayName());
                }, value :: toString, value.get(), null){
                    @Override
                    public void renderStatGraphic(IGameInstance game, IAssetManager manager, IRenderer g, int x, int y){
                        IItemRenderer renderer = item.getRenderer();
                        if(renderer != null){
                            renderer.render(game, manager, g, item, instance, x+1, y+1, 12F, Colors.WHITE);
                        }
                    }
                });
            }

            game.getGuiManager().openGui(gui.makeSubGui(list));

            return true;
        }){
            @Override
            public void renderStatGraphic(IGameInstance game, IAssetManager manager, IRenderer g, int x, int y){
                manager.getTexture(textureLocation).draw(x+1, y+1, 12F, 12F);
            }
        });
    }
}
