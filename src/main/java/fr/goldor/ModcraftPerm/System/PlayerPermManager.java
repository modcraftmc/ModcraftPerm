package fr.goldor.ModcraftPerm.System;

import fr.goldor.ModcraftPerm.DB.DataBaseConnection;
import fr.goldor.ModcraftPerm.ModcraftPerm;
import fr.goldor.ModcraftPerm.utils.References;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PlayerPermManager {

    private static DataBaseConnection dataBaseConnection;
    private static Map<String,String> groupBufferMap = new HashMap<String, String>();;
    private static Map<String,String> specificCommandsBufferMap = new HashMap<String, String>();;


    public static void Connect(DataBaseConnection newDataBaseConnection){
            dataBaseConnection = newDataBaseConnection;
    }

    public static boolean PlayerHasPerm(String playerUUID,String permSyntax){
        String[] permSearched = permSyntax.split("\\.");
        ArrayList<String> permSelection = ListAllRegisteredCommands(playerUUID);

        boolean masterPerm = false;
        for(int i = 0;i<permSearched.length;i++){

            if(masterPerm){
                continue;
            }

            ArrayList<String> permDeselection = new ArrayList<>();
            for(String perm: permSelection){

                String[] permComparator = perm.split("\\.");
                if(i >= permComparator.length){
                    permDeselection.add(perm);
                }
                else if(permComparator[i].contentEquals("*")){

                    if(i+1 == permComparator.length){
                        masterPerm = true;
                    }

                }
                else if(permSearched[i].contentEquals(permComparator[i])){

                }
                else{
                    permDeselection.add(perm);
                }
            }

            for (String perm: permDeselection) {
                permSelection.remove(perm);
            }
        }

        if(!permSelection.isEmpty()){
            return true;
        }


        return false;
    }

    public static String GetPlayerNameByUUID(String playerUUID){
        ResultSet result = dataBaseConnection.QuerySQLCommand(String.format("SELECT playerName FROM `player_perm` WHERE playerUUID = '%s'",playerUUID));

        try {
            if(result.next()){
                return result.getString(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String AddGroup(String playerUUID,String groupName){
        return AddGroup(playerUUID,"",groupName);
    }

    public static String AddGroup(String playerUUID,String playerName,String groupName){

        if(!CommandGroupManager.groupExist(groupName)) return String.format("%sGroup : %s%s%s doesn't exist ! Please create group before adding to a player.", References.ErrorColor,References.MainColor,groupName,References.ErrorColor);

        ResultSet result = dataBaseConnection.QuerySQLCommand(String.format("SELECT `playerGroupsName` FROM `player_perm` WHERE `playerUUID` = '%s'",playerUUID));
        String[] groups = null;
        String finalGroups;

        try {
            if(result.next()){
                String stringResult = result.getString(1);
                if(stringResult != null) groups = stringResult.split("-");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if(groups != null){

            ArrayList<String> newPlayers = new ArrayList<String>();
            for (int i = 0;i < groups.length;i++) {

                if(groups[i].contentEquals(groupName)) return String.format("%s%s%s already contain this group !",References.PseudoColor,playerName,References.ErrorColor);

                newPlayers.add(groups[i]);
                
            }

            newPlayers.add(groupName);
            finalGroups = String.join("-",newPlayers);

        }
        else {
            finalGroups = groupName;
        }

        groupBufferMap.replace(playerUUID,finalGroups);
        dataBaseConnection.ExecuteSQLCommand(String.format("UPDATE `player_perm` SET `playerGroupsName` = '%s' WHERE `playerUUID` = '%s'",finalGroups,playerUUID));
        CommandGroupManager.RegisterPlayer(playerUUID, groupName);
        return String.format("%sGroup : %s%s%s successfully added to player : %s%s",References.SuccessfulColor,References.MainColor,groupName,References.SuccessfulColor,References.PseudoColor,playerName);
    }

    public static String RemoveGroup(String playerUUID,String groupName){
        return RemoveGroup(playerUUID,"",groupName);
    }

    public static String RemoveGroup(String playerUUID,String playerName,String groupName){

        if(!CommandGroupManager.groupExist(groupName)) return String.format("%sGroup : %s%s%s doesn't exist !",References.ErrorColor,References.MainColor,groupName,References.ErrorColor);

        ResultSet result = dataBaseConnection.QuerySQLCommand(String.format("SELECT `playerGroupsName` FROM `player_perm` WHERE `playerUUID` = '%s'",playerUUID));
        String[] groups = null;
        String finalGroups = "";
        boolean remove = false;

        try {
            if(result.next()){
                String stringResult = result.getString(1);
                if(stringResult != null) groups = stringResult.split("-");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if(groups != null){

            ArrayList<String> newPlayers = new ArrayList<String>();
            for (int i = 0;i < groups.length;i++) {

                if(groups[i].contentEquals(groupName)){
                    remove = true;
                }
                else {
                    newPlayers.add(groups[i]);
                }
            }

            finalGroups = String.join("-",newPlayers);
        }

        if(remove){
            groupBufferMap.replace(playerUUID,finalGroups);
            dataBaseConnection.ExecuteSQLCommand(String.format("UPDATE `player_perm` SET `playerGroupsName` = '%s' WHERE `playerUUID` = '%s'",finalGroups,playerUUID));

            CommandGroupManager.UnregisterPlayer(playerUUID, groupName);
            return String.format("%sGroup %s%s%s successfully removed to player : %s%s",References.SuccessfulColor,References.MainColor,groupName,References.SuccessfulColor,References.PseudoColor,playerName);
        }else{
            return String.format("%sGroup : %s%s%s not found for player : %s%s",References.ErrorColor,References.MainColor,groupName,References.ErrorColor,References.PseudoColor,playerName);
        }
    }

    public static String RegisterCommand(String playerUUID,String command){
        return RegisterCommand(playerUUID,"",command);
    }

    public static String RegisterCommand(String playerUUID,String playerName,String command){
        String[] currentPerms = ListSpecificRegisteredCommands(playerUUID,true);
        ArrayList<String> newPerms = new ArrayList<String>();

        String finalPerms = "";
        for (String s: currentPerms) {
            if(command.contentEquals(s)){
                return String.format("%sPermission : %s%s%s is already in player : %s%s",References.ErrorColor,References.MainColor,command,References.ErrorColor,References.PseudoColor,playerName);
            }
            if(!s.contentEquals("")) newPerms.add(s);
        }

        newPerms.add(command);
        finalPerms = String.join("/",newPerms);

        specificCommandsBufferMap.replace(playerUUID,finalPerms);
        dataBaseConnection.ExecuteSQLCommand(String.format("UPDATE `player_perm` SET `playerPerm` = '%s' WHERE `playerUUID` = '%s'",finalPerms,playerUUID));
        return String.format("%sPermission : %s%s%s successfully added to player : %s%s",References.SuccessfulColor,References.MainColor,command,References.SuccessfulColor,References.PseudoColor,playerName);
    }

    public static String UnregisterCommand(String playerUUID,String command){
        return UnregisterCommand(playerUUID,"",command);
    }

    public static String UnregisterCommand(String playerUUID,String playerName,String command){
        boolean remove = false;
        String[] currentPerms = ListSpecificRegisteredCommands(playerUUID,true);
        ArrayList<String> newPerms = new ArrayList<String>();

        for (String perm: currentPerms) {
            if(perm.contentEquals(command)){
                remove = true;
            }
            else{
                newPerms.add(perm);
            }
        }

        String finalPerms = "";
        if(!newPerms.isEmpty()) finalPerms = String.join("/",newPerms);

        if(remove){
            specificCommandsBufferMap.replace(playerUUID,finalPerms);
            dataBaseConnection.ExecuteSQLCommand(String.format("UPDATE `player_perm` SET `playerPerm` = '%s' WHERE `playerUUID` = '%s'",finalPerms,playerUUID));
            return String.format("%sPermission : %s%s%s successfully removed from player : %s%s",References.SuccessfulColor,References.MainColor,command,References.SuccessfulColor,References.PseudoColor,playerName);
        }
        else{
            return String.format("%sPermission : %s%s%s do not exist in player : %s%s",References.ErrorColor,References.MainColor,command,References.ErrorColor,References.PseudoColor,playerName);
        }
    }

    public static String[] ListRegisteredGroupName(String playerUUID,boolean saveInBuffer){

        if(groupBufferMap.containsKey(playerUUID)){
            if(groupBufferMap.get(playerUUID) == null){
                return new String[0];
            }
            return groupBufferMap.get(playerUUID).split("-");
        }

        //get registered groups by the db
        ResultSet result = dataBaseConnection.QuerySQLCommand(String.format("SELECT playerGroupsName FROM `player_perm` WHERE playerUUID = '%s'",playerUUID));

        try {

            if(result.next()){
                if(saveInBuffer) groupBufferMap.put(playerUUID,result.getString(1));

                if(result.getString(1) == null){
                    return new String[0];
                }

                return result.getString(1).split("-");
            }

            return new String[0];
        }
        catch (SQLException e) {

            ModcraftPerm.GetLogger().warn(String.format("No groups founded for player with UUID : %s !",playerUUID));
            e.printStackTrace();

            return new String[0];
        }
    }

    public static String[] ListSpecificRegisteredCommands(String playerUUID,boolean saveInBuffer){

        if(specificCommandsBufferMap.containsKey(playerUUID)){
            if(specificCommandsBufferMap.get(playerUUID) == null){
                return new String[0];
            }
            return specificCommandsBufferMap.get(playerUUID).split("/");
        }

        //get registered commands by the db
        ResultSet result = dataBaseConnection.QuerySQLCommand(String.format("SELECT playerPerm FROM `player_perm` WHERE playerUUID = '%s'",playerUUID));

        try {

            if(result.next()){

                if(saveInBuffer) specificCommandsBufferMap.put(playerUUID,result.getString(1));

                if(result.getString(1) == null){
                    return new String[0];
                }

                return result.getString(1).split("/");
            }

            return new String[0];
        }
        catch (SQLException e) {

            ModcraftPerm.GetLogger().warn(String.format("No permissions founded for player with UUID : %s !",playerUUID));
            e.printStackTrace();

            return new String[0];
        }
    }

    public static ArrayList<String> ListAllRegisteredCommands(String playerUUID){
        ArrayList<String> AllRegisteredCommands = new ArrayList<String>();

        String[] groupsName = ListRegisteredGroupName(playerUUID,true);
        for (String group : groupsName) {

            String[] perms = CommandGroupManager.ListRegisteredCommand(group,true);
            for (String perm : perms) {
                AllRegisteredCommands.add(perm);
            }

        }

        String[] specificRegisteredCommand = ListSpecificRegisteredCommands(playerUUID,true);
        if(specificRegisteredCommand.length > 0) {
            for (String command : specificRegisteredCommand) {
                AllRegisteredCommands.add(command);
            }
        }

        return AllRegisteredCommands;
    }

    public static void CopyPlayerPerm(String basePlayerUUID,String playerUUID){

    }

    public static void DeleteBuffers(){
        specificCommandsBufferMap.clear();
        groupBufferMap.clear();
    }
}