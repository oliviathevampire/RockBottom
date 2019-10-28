/*
package de.ellpeck.rockbottom.gui.menu.background;

import de.ellpeck.rockbottom.api.GameContent;
import de.ellpeck.rockbottom.api.RockBottomAPI;
import de.ellpeck.rockbottom.api.gui.IMainMenuTheme;
import de.ellpeck.rockbottom.api.tile.state.TileState;
import de.ellpeck.rockbottom.api.util.Util;
import de.ellpeck.rockbottom.api.world.gen.INoiseGen;
import de.ellpeck.rockbottom.render.WorldRenderer;

public class WhiteDesertTheme implements IMainMenuTheme {

    private final INoiseGen noiseGen = RockBottomAPI.getApiHandler().makeSimplexNoise(Util.RANDOM.nextLong());

    @Override
    public TileState getState(int x, int y, TileState[][] grid) {
        int height = Util.ceil(this.noiseGen.make2dNoise(x / 10D, 0D) * 3D) + 1;

        TileState state;
        if (y <= height - 3) {
            state = GameContent.TILE_WHITE_SANDSTONE.getDefState();
        } else if (y <= height) {
            state = GameContent.TILE_WHITE_SAND.getDefState();
        } else {
            state = GameContent.TILE_AIR.getDefState();
        }

        if (state.getTile().isAir()) {
            if (Util.RANDOM.nextFloat() >= 0.75F) {
                if (grid[x][y - 1].getTile().isFullTile()) {
                    return GameContent.TILE_PEBBLES.getDefState();
                }
            }
        }
        return state;
    }

    @Override
    public int getBackgroundColor() {
        return WorldRenderer.SKY_COLORS[WorldRenderer.SKY_COLORS.length - 1];
    }
}
*/
