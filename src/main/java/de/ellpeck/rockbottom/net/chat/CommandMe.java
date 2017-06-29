package de.ellpeck.rockbottom.net.chat;

import de.ellpeck.rockbottom.api.IGameInstance;
import de.ellpeck.rockbottom.api.assets.font.FormattingCode;
import de.ellpeck.rockbottom.api.net.chat.Command;
import de.ellpeck.rockbottom.api.net.chat.IChatLog;
import de.ellpeck.rockbottom.api.net.chat.ICommandSender;

public class CommandMe extends Command{

    public CommandMe(){
        super("me", "/me <message>", 0);
    }

    @Override
    public String execute(String[] args, ICommandSender sender, String playerName, IGameInstance game, IChatLog chat){
        if(args.length <= 0){
            return FormattingCode.RED+"Message is missing!";
        }
        else{
            String sentence = "";

            for(String s : args){
                sentence += " "+s;
            }

            chat.broadcastMessage(sender.getChatColorFormat()+playerName+sentence);

            return null;
        }
    }
}
