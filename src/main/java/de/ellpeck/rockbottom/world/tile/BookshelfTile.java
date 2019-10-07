package de.ellpeck.rockbottom.world.tile;

import de.ellpeck.rockbottom.api.render.tile.ITileRenderer;
import de.ellpeck.rockbottom.api.render.tile.MultiMetaTileRenderer;
import de.ellpeck.rockbottom.api.tile.MultiMetaTile;
import de.ellpeck.rockbottom.api.tile.state.TileState;
import de.ellpeck.rockbottom.api.util.BoundBox;
import de.ellpeck.rockbottom.api.util.reg.ResourceName;
import de.ellpeck.rockbottom.api.world.IWorld;
import de.ellpeck.rockbottom.api.world.layer.TileLayer;

public class BookshelfTile extends MultiMetaTile {

    public BookshelfTile() {
        super(ResourceName.intern("bookshelf"));
        this.addSubTile(ResourceName.intern("bookshelf_1"));
        this.addSubTile(ResourceName.intern("bookshelf_2"));
        this.addSubTile(ResourceName.intern("bookshelf_big_1"));
        this.addSubTile(ResourceName.intern("bookshelf_big_2"));
    }

    @Override
    public boolean isPlatform() {
        return true;
    }

    @Override
    public boolean canPlaceInLayer(TileLayer layer) {
        return layer == TileLayer.MAIN;
    }

    @Override
    public boolean isFullTile() {
        return false;
    }

    @Override
    public BoundBox getBoundBox(IWorld world, TileState state, int x, int y, TileLayer layer) {
        return null;
    }

    @Override
    protected ITileRenderer createRenderer(ResourceName name) {
        return new MultiMetaTileRenderer<>(name, this);
    }

    @Override
    protected boolean[][] makeStructure() {
        return new boolean[][]{
                {true, true}
        };
    }

    @Override
    public int getWidth() {
        return 2;
    }

    @Override
    public int getHeight() {
        return 1;
    }

    @Override
    public int getMainX() {
        return 0;
    }

    @Override
    public int getMainY() {
        return 0;
    }
}
