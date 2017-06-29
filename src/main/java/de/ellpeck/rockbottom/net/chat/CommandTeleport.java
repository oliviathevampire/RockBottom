package de.ellpeck.rockbottom.net.chat;

import de.ellpeck.rockbottom.api.IGameInstance;
import de.ellpeck.rockbottom.api.assets.font.FormattingCode;
import de.ellpeck.rockbottom.api.entity.player.AbstractEntityPlayer;
import de.ellpeck.rockbottom.api.net.chat.Command;
import de.ellpeck.rockbottom.api.net.chat.IChatLog;
import de.ellpeck.rockbottom.api.net.chat.ICommandSender;

import java.util.Arrays;

public class CommandTeleport extends Command{

    public CommandTeleport(){
        super("teleport", "/teleport <x> <y>", 5);
    }

    @Override
    public String execute(String[] args, ICommandSender sender, String playerName, IGameInstance game, IChatLog chat){
        try{
            if(sender instanceof AbstractEntityPlayer){
                int x = Integer.parseInt(args[0]);
                int y = Integer.parseInt(args[1]);
                ((AbstractEntityPlayer)sender).setPos(x, y);

                return FormattingCode.GREEN+"Teleported to "+x+", "+y+"!";
            }
            else{
                return FormattingCode.RED+"Only players can execute this command!";
            }
        }
        catch(Exception e){
            return FormattingCode.RED+"Error formatting number for command args "+Arrays.toString(args)+"!";
        }
    }
}
