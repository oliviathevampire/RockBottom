package de.ellpeck.rockbottom.render.cutscene;

import de.ellpeck.rockbottom.api.render.Camera;

public class IdleCamera implements Camera {
    private final double x, y;
    private final float scale;

    public IdleCamera(double x, double y, float scale) {
        this.x = x;
        this.y = y;
        this.scale = scale / 100f;
    }

    @Override
    public double getLerpedX() {
        return x;
    }

    @Override
    public double getLerpedY() {
        return y;
    }

    @Override
    public float getLerpedScale() {
        return scale;
    }
}