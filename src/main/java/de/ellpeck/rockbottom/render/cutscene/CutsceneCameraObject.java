package de.ellpeck.rockbottom.render.cutscene;

import de.ellpeck.rockbottom.api.RockBottomAPI;
import de.ellpeck.rockbottom.api.render.Camera;
import de.ellpeck.rockbottom.api.util.Util;

public abstract class CutsceneCameraObject implements Camera {
    protected double x;
    protected double y;
    protected float scale;
    protected double lastTickX;
    protected double lastTickY;
    protected float lastTickScale;

    public CutsceneCameraObject(double x, double y, float scale) {
        this.x = x;
        this.y = y;
        this.scale = scale;

        this.lastTickX = x;
        this.lastTickY = y;
        this.lastTickScale = scale;
    }

    abstract void update(int ticks);

    @Override
    public double getLerpedX() {
        return Util.lerp(this.lastTickX, this.x, RockBottomAPI.getGame().getTickDelta());
    }

    @Override
    public double getLerpedY() {
        return Util.lerp(this.lastTickY, this.y, RockBottomAPI.getGame().getTickDelta());
    }

    @Override
    public float getLerpedScale() {
        return (float) Util.lerp(this.lastTickScale / 100f, this.scale / 100f, RockBottomAPI.getGame().getTickDelta());
    }
}