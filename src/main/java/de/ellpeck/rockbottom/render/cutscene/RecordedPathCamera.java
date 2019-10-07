package de.ellpeck.rockbottom.render.cutscene;

import de.ellpeck.rockbottom.api.RockBottomAPI;

public class RecordedPathCamera extends CutsceneCameraObject {
    private final Cutscene cutscene;
    private final RecordedPath path;

    public RecordedPathCamera(Cutscene cutscene, RecordedPath path) {
        super(path.getX(0), path.getY(0), path.getScale(0));
        this.cutscene = cutscene;
        this.path = path;
    }

    @Override
    public void update(int ticks) {
        lastTickX = x;
        lastTickY = y;
        lastTickScale = scale;

        x = path.getX(ticks);
        y = path.getY(ticks);
        scale = path.getScale(ticks);

        if (scale != lastTickScale) {
            RockBottomAPI.getGame().getRenderer().recalculateWorldScale();
        }
    }

    @Override
    public double getLerpedX() {
        if (!cutscene.hideGui()) {
            return RockBottomAPI.getGame().getPlayer().getLerpedX();
        }
        return super.getLerpedX();
    }

    @Override
    public double getLerpedY() {
        if (!cutscene.hideGui()) {
            return RockBottomAPI.getGame().getPlayer().getLerpedY();
        }
        return super.getLerpedY();
    }

    @Override
    public float getLerpedScale() {
        if (!cutscene.hideGui()) {
            return RockBottomAPI.getGame().getSettings().renderScale;
        }
        return super.getLerpedScale();
    }

}