package fr.goldor.ModcraftPerm.utils;

import fr.goldor.ModcraftPerm.ModcraftPerm;

import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.StringTextComponent;

import java.util.ArrayList;
import java.util.List;

public class FuncUtils {


    public static void sendMessageToPlayer(CommandSource source, String s){

        String finalMessage = String.format("%s[ModcraftPerm] %s",References.MainColor,s);
        source.sendFeedback(new StringTextComponent(finalMessage),false);

    }

    public static void sendMessageInServer(String s) {

        String finalMessage = String.format("%s[ModcraftPerm] %s",References.MainColor,s);
        List<ServerPlayerEntity> players = ModcraftPerm.GetDedicatedServer().getPlayerList().getPlayers();
        for (ServerPlayerEntity player: players) {
            player.sendMessage(new StringTextComponent(finalMessage),null);
        }

    }


    public static ArrayList<PlayerEntity> pseudoSearch(String s, String pseudos[]){

        ArrayList<String> pseudoSearched = new ArrayList();

        for(int i=0;i<pseudos.length;i++){

            boolean test = pseudos[i].toLowerCase().contains(s.toLowerCase());

            if(test == true){
                pseudoSearched.add(pseudos[i]);
            }
        }

        ArrayList<PlayerEntity> playerList = new ArrayList<PlayerEntity>();

        for (String pseudo: pseudoSearched) {
            playerList.add(ModcraftPerm.GetDedicatedServer().getPlayerList().getPlayerByUsername(pseudo));
        }


        return playerList;
    }

    public static PlayerEntity getOnlyOnePlayer(CommandSource source,String name){
        ArrayList<PlayerEntity> playersSearch = FuncUtils.pseudoSearch(name,ModcraftPerm.GetDedicatedServer().getOnlinePlayerNames());
        if(playersSearch.size() > 1){

            String playerNameList = "";
            for (PlayerEntity player : playersSearch) {
                playerNameList += ","+player.getName().getString();
            }

            FuncUtils.sendMessageToPlayer(source,String.format("%sMultiple player found with this name %s%s",References.ErrorColor,References.PseudoColor,playerNameList));
        }
        else if(playersSearch.size() == 1){
            return playersSearch.get(0);
        }
        else {
            FuncUtils.sendMessageToPlayer(source,String.format("%sNo player found with this name",References.ErrorColor));
        }
        return null;
    }

}