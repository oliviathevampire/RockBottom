package de.ellpeck.rockbottom.render.cutscene;

import de.ellpeck.rockbottom.api.IGameInstance;
import de.ellpeck.rockbottom.api.IRenderer;
import de.ellpeck.rockbottom.api.RockBottomAPI;
import de.ellpeck.rockbottom.api.gui.Gui;

public class Cutscene {
    private RecordedPath path;
    private RecordedPathCamera camera;
    private Transition.DipTo startTransition;
    private Transition.DipTo endTransition;

    private int ticks = 0;
    private boolean hidGuiLastTick = false;

    public Cutscene(RecordedPath path) {
        this.path = path;
        this.camera = new RecordedPathCamera(this, path);
        this.startTransition = new Transition.DipTo(0, 80, 0, 0, 0);
        this.endTransition = new Transition.DipTo(path.getLength() - 80, path.getLength(), 0, 0, 0);
    }

    public RecordedPathCamera getCamera() {
        return camera;
    }

    public void update() {
        if (finished()) {
            return;
        }

        Gui gui = RockBottomAPI.getGame().getGuiManager().getGui();
        if (gui == null || !gui.doesPauseGame()) {
            ticks++;
            boolean hideGui = hideGui();
            if (hideGui) {
                camera.update(ticks);
            }

            if (hidGuiLastTick != hideGui) {
                RockBottomAPI.getGame().getRenderer().recalculateWorldScale();
            }

            hidGuiLastTick = hideGui;
        }
    }

    public void render(IGameInstance game, IRenderer renderer) {
        if (startTransition.playing(ticks)) {
            startTransition.render(game, renderer, ticks);
        } else if (endTransition.playing(ticks)) {
            endTransition.render(game, renderer, ticks);
        }
    }

    public boolean hideGui() {
        return !startTransition.isFirstHalf(ticks) && endTransition.isFirstHalf(ticks);
    }

    public boolean finished() {
        return ticks >= path.getLength();
    }
}