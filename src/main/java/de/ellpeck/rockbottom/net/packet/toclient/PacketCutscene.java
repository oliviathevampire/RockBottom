package de.ellpeck.rockbottom.net.packet.toclient;

import de.ellpeck.rockbottom.api.IGameInstance;
import de.ellpeck.rockbottom.api.RockBottomAPI;
import de.ellpeck.rockbottom.api.assets.font.FormattingCode;
import de.ellpeck.rockbottom.api.net.chat.component.ChatComponentText;
import de.ellpeck.rockbottom.api.net.packet.IPacket;
import de.ellpeck.rockbottom.render.cutscene.CutsceneManager;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

public class PacketCutscene implements IPacket {

    private CutsceneSubcommand command;
    private int duration;

    public PacketCutscene(CutsceneSubcommand command, int duration) {
        this.command = command;
        this.duration = duration;
    }

    public PacketCutscene(CutsceneSubcommand command) {
        this.command = command;
        if (command == CutsceneSubcommand.PLAY) {
            this.duration = 100;
        }
    }

    @Override
    public void toBuffer(ByteBuf buf) {
        buf.writeInt(this.command.id);
        if (command == CutsceneSubcommand.PLAY) {
            buf.writeInt(this.duration);
        }
    }

    @Override
    public void fromBuffer(ByteBuf buf) {
        this.command = CutsceneSubcommand.fromID(buf.readInt());
        if (command == CutsceneSubcommand.PLAY) {
            this.duration = buf.readInt();
        }
    }

    @Override
    public void handle(IGameInstance game, ChannelHandlerContext context) {
        System.out.println("hello");
        if (command != null) {
            switch(command) {
                case PLAY:
                    CutsceneManager.getInstance().startRecordedCutscene();
                    break;
                case RECORD:
                    if (CutsceneManager.getInstance().enableRecordingMode()) {
                        game.getChatLog().displayMessage(new ChatComponentText(FormattingCode.PURPLE + "Press JUMP to start recording."));
                    } else {
                        game.getChatLog().displayMessage(new ChatComponentText(FormattingCode.RED + "Failed to switch to recording mode."));
                    }
                    break;

            }
        }
    }

    public enum CutsceneSubcommand {
        PLAY(0), RECORD(1);

        public final int id;
        CutsceneSubcommand(int id) {
            this.id = id;
        }

        public static CutsceneSubcommand fromID(int id) {
            for (CutsceneSubcommand command : CutsceneSubcommand.values()) {
                if (command.id == id) {
                    return command;
                }
            }
            RockBottomAPI.logger().warn("Tried to deserialize invalid cutscene subcommand id: " + id);
            return null;
        }
    }
}