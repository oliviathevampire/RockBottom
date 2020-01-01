package de.ellpeck.rockbottom.render.cutscene;

import de.ellpeck.rockbottom.api.IGameInstance;
import de.ellpeck.rockbottom.api.RockBottomAPI;
import de.ellpeck.rockbottom.api.assets.font.FormattingCode;
import de.ellpeck.rockbottom.api.entity.player.AbstractEntityPlayer;
import de.ellpeck.rockbottom.api.net.chat.component.ChatComponentText;
import de.ellpeck.rockbottom.api.render.Camera;

public class CutsceneManager {
    private static CutsceneManager INSTANCE = new CutsceneManager();

    private Cutscene cutscene;
    private RecordedPath latestPath;
    private PathRecorder recorder;

    public static CutsceneManager getInstance() {
        return INSTANCE;
    }

    public void update() {
        if (cutscene == null) {
            if (recorder == null) {
                return;
            }
            recorder.update(0);
            return;
        } else if (cutscene.finished()) {
            cutscene = null;
            RockBottomAPI.getGame().getRenderer().recalculateWorldScale();
            return;
        }
        cutscene.update();
    }

    public void render() {
        if (isPlaying()) {
            cutscene.render(RockBottomAPI.getGame(), RockBottomAPI.getGame().getRenderer());
        }
    }

    /**
     * Start a new cutscene
     * @return True if the cutscene started successfully
     */
    public boolean startRecordedCutscene() {
        if (isPlaying() || isRecording()) {
            RockBottomAPI.logger().warn("Tried to start a cutscene while the camera was already occupied!");
            return false;
        }
        this.cutscene = new Cutscene(latestPath);
        return true;
    }

    public boolean enableRecordingMode() {
        if (isPlaying()) {
            RockBottomAPI.logger().warn("Tried to start a path recording while the camera was already occupied!");
            return false;
        }
        AbstractEntityPlayer player;
        if (RockBottomAPI.getGame() != null && (player = RockBottomAPI.getGame().getPlayer()) != null) {
            this.recorder = new PathRecorder((float) player.getX(), (float) player.getY(), RockBottomAPI.getGame().getSettings().renderScale * 100f);
            return true;
        }
        RockBottomAPI.logger().warn("Tried to start a path recording when not in-game!");
        return false;
    }

    public boolean disableRecordingMode() {
        if (isPlaying()) {
            RockBottomAPI.logger().warn("Tried to disable recording mode while a cutscene was playing!");
            return false;
        } else if (recorder == null) {
            RockBottomAPI.logger().warn("Tried to disable recording mode without enabling it first!");
            return false;
        }
        IGameInstance game = RockBottomAPI.getGame();
        if (game == null) {
            RockBottomAPI.logger().warn("Tried to disable recording mode when not in-game!");
            return false;
        }
        RecordedPath path = recorder.saveRecording();
        if (path != null) {
            game.getChatLog().displayMessage(new ChatComponentText(FormattingCode.GREEN + "Recording Saved!"));
            this.latestPath = path;
        } else {
            game.getChatLog().displayMessage(new ChatComponentText(FormattingCode.RED + "Failed to save recording!"));
        }
        recorder = null;
        game.getRenderer().recalculateWorldScale();
        return true;
    }

    public Camera getCamera() {
        AbstractEntityPlayer player;
        if (RockBottomAPI.getGame() != null && (player = RockBottomAPI.getGame().getPlayer()) != null) {
            if (cutscene != null) {
                return cutscene.getCamera();
            } else if (recorder != null) {
                return recorder;
            } else {
                return player;
            }
        }
        return new IdleCamera(30, 0, 100);
    }

    public Cutscene getActiveCutscene() {
        return cutscene;
    }

    public PathRecorder getActiveRecorder() {
        return recorder;
    }

    public boolean hideGui() {
        return (isPlaying() && cutscene.hideGui()) || (isRecording() && recorder.isRecording());
    }

    public boolean isPlaying() {
        return RockBottomAPI.getGame() != null && RockBottomAPI.getGame().getPlayer() != null && cutscene != null;
    }

    public boolean isRecording() {
        return RockBottomAPI.getGame() != null && RockBottomAPI.getGame().getPlayer() != null && recorder != null;
    }

}