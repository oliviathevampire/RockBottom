package de.ellpeck.rockbottom.render.cutscene;

import de.ellpeck.rockbottom.api.IGameInstance;
import de.ellpeck.rockbottom.api.IRenderer;
import de.ellpeck.rockbottom.api.util.Colors;

public abstract class Transition {
    protected final int start;
    protected final int end;

    public Transition(int start, int end) {
        this.start = start;
        this.end = end;
    }

    public boolean playing(int ticks) {
        return ticks >= start && ticks <= end;
    }

    public abstract void render(IGameInstance game, IRenderer renderer, int ticks);

    public static class DipTo extends Transition {
        private final int r, g, b;
        public DipTo(int start, int end, int r, int g, int b) {
            super(start, end);
            this.r = r;
            this.g = g;
            this.b = b;
        }

        @Override
        public void render(IGameInstance game, IRenderer renderer, int ticks) {
            if (playing(ticks)) {
                int duration = end - start;
                int innerTicks = ticks - start;
                int half = duration / 2;
                float a = 1;
                if (innerTicks < half) {
                    a = (float) innerTicks / half;
                } else if (innerTicks > half) {
                    a = 1 - (float) innerTicks / half;
                }
                renderer.addFilledRect(0, 0, game.getWidth(), game.getHeight(), Colors.rgb(r, g, b, a));
            }
        }

        public boolean isFirstHalf(int ticks) {
            int duration = end - start;
            int innerTicks = ticks - start;
            int half = duration / 2;
            return innerTicks < half;
        }
    }

}