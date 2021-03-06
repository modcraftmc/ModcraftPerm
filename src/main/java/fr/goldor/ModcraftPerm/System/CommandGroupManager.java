package fr.goldor.ModcraftPerm.System;

import fr.goldor.ModcraftPerm.DB.DataBaseConnection;
import fr.goldor.ModcraftPerm.ModcraftPerm;
import fr.goldor.ModcraftPerm.utils.References;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CommandGroupManager {

    private static DataBaseConnection dataBaseConnection;
    private static Map<String,String> commandsBufferMap = new HashMap<String, String>();

    public static void Connect(DataBaseConnection newDataBaseConnection){
        dataBaseConnection = newDataBaseConnection;
    }

    public static String AddGroup(String groupName){
        if(groupExist(groupName)){
            return String.format("%sGroup : %s%s%s already exist !", References.ErrorColor,References.MainColor,groupName,References.ErrorColor);
        }
        else{
            dataBaseConnection.ExecuteSQLCommand(String.format("INSERT INTO `group_perm` (`groupID`, `groupName`, `groupPerm`, `groupPlayersID`) VALUES (NULL, '%s', NULL, NULL)",groupName));
            return String.format("%sGroup : %s%s%s has been created.",References.SuccessfulColor,References.MainColor,groupName,References.SuccessfulColor);
        }
    }

    public static String RemoveGroup(String groupName){
        if(groupExist(groupName)){
            String[] playersWithGroup = ListRegisteredPlayer(groupName);
            for (String playerUUID: playersWithGroup) {
                PlayerPermManager.RemoveGroup(playerUUID,groupName);
            }

            commandsBufferMap.remove(groupName);
            dataBaseConnection.ExecuteSQLCommand(String.format("DELETE FROM `group_perm` WHERE `groupName` = '%s'",groupName));
            return String.format("%sGroup : %s%s%s has been removed",References.SuccessfulColor,References.MainColor,groupName,References.SuccessfulColor);
        }
        else{
            return String.format("%sGroup : %s%s%s didn't exist !",References.ErrorColor,References.MainColor,groupName,References.ErrorColor);
        }
    }

    public static void RegisterPlayer(String playerUUID,String groupName){
        ResultSet result = dataBaseConnection.QuerySQLCommand(String.format("SELECT `groupPlayersID` FROM `group_perm` WHERE `groupName` = '%s'",groupName));
        String[] players = null;
        int playerID = -1;
        String finalPlayers;

        try {
            if(result.next()){
                String stringResult = result.getString(1);
                if(stringResult != null) players = stringResult.split("-");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        result = dataBaseConnection.QuerySQLCommand(String.format("SELECT `playerID` FROM `player_perm` WHERE `playerUUID` = '%s'",playerUUID));
        try {
            if(result.next()){
                playerID = result.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            ModcraftPerm.GetLogger().error(String.format("No player ID found in data base for playerUUID : %s",playerUUID));
            return;
        }
        if(playerID == -1){
            ModcraftPerm.GetLogger().error(String.format("No player ID found in data base for playerUUID : %s",playerUUID));
            return;
        }


        if(players != null){

            ArrayList<String> newPlayers = new ArrayList<String>();
            for (int i = 0;i < players.length;i++) {
                newPlayers.add(players[i]);
            }

            newPlayers.add(String.valueOf(playerID));
            finalPlayers = String.join("-",newPlayers);

        }
        else {
            finalPlayers = String.valueOf(playerID);
        }

        dataBaseConnection.ExecuteSQLCommand(String.format("UPDATE `group_perm` SET `groupPlayersID` ='%s' WHERE `groupName` = '%s'",finalPlayers,groupName));
    }

    public static void UnregisterPlayer(String playerUUID,String groupName){
        ResultSet result = dataBaseConnection.QuerySQLCommand(String.format("SELECT `groupPlayersID` FROM `group_perm` WHERE `groupName` = '%s'",groupName));
        String[] players = null;
        int playerID = -1;
        String finalPlayers;

        try {
            if(result.next()){
                String stringResult = result.getString(1);
                if(stringResult != null) players = stringResult.split("-");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        result = dataBaseConnection.QuerySQLCommand(String.format("SELECT `playerID` FROM `player_perm` WHERE `playerUUID` = '%s'",playerUUID));
        try {
            if(result.next()){
                playerID = result.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            ModcraftPerm.GetLogger().error(String.format("No player ID found in data base for playerUUID : %s",playerUUID));
            return;
        }
        if(playerID == -1){
            ModcraftPerm.GetLogger().error(String.format("No player ID found in data base for playerUUID : %s",playerUUID));
            return;
        }

        if(players != null){

            ArrayList<String> newPlayers = new ArrayList<String>();
            for (int i = 0;i < players.length;i++) {
                if(!players[i].contentEquals(String.valueOf(playerID))) newPlayers.add(players[i]);
            }

            if(!newPlayers.isEmpty()) finalPlayers = String.join("-",newPlayers);else finalPlayers = "";

        }
        else {
            finalPlayers = "";
        }

        dataBaseConnection.ExecuteSQLCommand(String.format("UPDATE `group_perm` SET `groupPlayersID` ='%s' WHERE `groupName` = '%s'",finalPlayers,groupName));
    }

    public static String RegisterCommand(String command,String groupName){
        if(!CommandGroupManager.groupExist(groupName)) return String.format("%sGroup : %s%s%s doesn't exist ! Please create group before adding permissions.",References.ErrorColor,References.MainColor,groupName,References.ErrorColor);

        String[] currentPerms = ListRegisteredCommand(groupName,true);
        ArrayList<String> newPerms = new ArrayList<String>();

        String finalPerms = "";
        for (String s: currentPerms) {
            if(command.contentEquals(s)){
                return String.format("%sPermission : %s%s%s is already in %s%s",References.ErrorColor,References.MainColor,command,References.ErrorColor,References.MainColor,groupName);
            }
            if(!s.contentEquals("")) newPerms.add(s);
        }

        newPerms.add(command);
        finalPerms = String.join("/",newPerms);

        commandsBufferMap.replace(groupName,finalPerms);
        dataBaseConnection.ExecuteSQLCommand(String.format("UPDATE `group_perm` SET `groupPerm` = '%s' WHERE `groupName` = '%s'",finalPerms,groupName));
        return String.format("%sPermission : %s%s%s successfully added to %s%s",References.SuccessfulColor,References.MainColor,command,References.SuccessfulColor,References.MainColor,groupName);
    }

    public static String UnregisterCommand(String command,String groupName){
        if(!CommandGroupManager.groupExist(groupName)) return String.format("%sGroup : %s%s%s doesn't exist ! Please create group before removing permissions.",References.ErrorColor,References.MainColor,groupName,References.ErrorColor);

        boolean remove = false;
        String[] currentPerms = ListRegisteredCommand(groupName,true);
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
            commandsBufferMap.replace(groupName,finalPerms);
            dataBaseConnection.ExecuteSQLCommand(String.format("UPDATE `group_perm` SET `groupPerm` = '%s' WHERE `groupName` = '%s'",finalPerms,groupName));
            return String.format("%sPermission : %s%s%s successfully removed from group : %s%s",References.SuccessfulColor,References.MainColor,command,References.SuccessfulColor,References.MainColor,groupName);
        }
        else{
            return String.format("%sPermission : %s%s%s do not exist in group : %s%s",References.ErrorColor, References.MainColor,command,References.ErrorColor,References.MainColor,groupName);
        }
    }

    public static String[] ListGroups(){
        ArrayList<String> groups = new ArrayList<String>();
        ResultSet result = dataBaseConnection.QuerySQLCommand("SELECT `groupName` FROM `group_perm`");

        try {

            while(result.next()){
                groups.add(result.getString(1));
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return new String[0];
        }

        return groups.toArray(new String[0]);
    }

    public static String[] ListRegisteredCommand(String groupName,boolean saveInBuffer){

        //see if perm already save in buffer
        if(commandsBufferMap.containsKey(groupName)){
            if(commandsBufferMap.get(groupName) == null){
                return new String[0];
            }
                return commandsBufferMap.get(groupName).split("/");
        }

        //get registered commands by the db
        ResultSet result = dataBaseConnection.QuerySQLCommand(String.format("SELECT groupPerm FROM `group_perm` WHERE groupName = '%s'",groupName));
        try {
            if(result.next()){

                if(saveInBuffer) commandsBufferMap.put(groupName,result.getString(1));

                if(result.getString(1) == null){
                    return new String[0];
                }

                return result.getString(1).split("/");
            }

            return new String[0];
        }
        catch (SQLException e) {
            ModcraftPerm.GetLogger().warn(String.format("No permissions founded for group with name : %s does exist ?",groupName));
            e.printStackTrace();

            return new String[0];
        }
    }

    public static String[] ListRegisteredPlayer(String groupName){
        ResultSet result = dataBaseConnection.QuerySQLCommand(String.format("SELECT groupPlayersID FROM `group_perm` WHERE groupName = '%s'",groupName));
        try {
            if(result.next()){
                if(result.getString(1) == null){

                    return new String[0];
                }

                String[] playersID = result.getString(1).split("-");
                ArrayList<String> playersName = new ArrayList<String>();

                result = dataBaseConnection.QuerySQLCommand(String.format("SELECT playerUUID FROM `player_perm` WHERE playerID = '%s'",String.join(" OR ",playersID)));

                while(result.next()){
                    playersName.add(result.getString(1));
                }

                if(playersName.size() > 0){

                    return playersName.toArray(new String[0]);
                }

                return new String[0];
            }
        } catch (SQLException e) {
            ModcraftPerm.GetLogger().warn("No players founded for group with name : "+groupName+" does exist ?");
            e.printStackTrace();
        }

        return new String[0];
    }

    public static String[] ListRegisteredGroup(String command){
        ResultSet result = dataBaseConnection.QuerySQLCommand("SELECT groupName FROM `group_perm`");
        ArrayList<String> groups = new ArrayList<String>();
        ArrayList<String> groupsWithCommand = new ArrayList<String>();

        try {
            while(result.next()){
                groups.add(result.getString(2));
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        for (String group: groups) {
            String[] perms = ListRegisteredCommand(group,false);
            for (String perm: perms) {
                if(perm.contentEquals(command)) groupsWithCommand.add(group);
            }
        }
        String[] list = groupsWithCommand.toArray(new String[0]);

        return list;
    }

    public static boolean groupExist(String groupName){

        //see if perm already save in buffer
        if(commandsBufferMap.containsKey(groupName)){
            return true;
        }

        ResultSet result = dataBaseConnection.QuerySQLCommand(String.format("SELECT `groupName` FROM `group_perm` WHERE `groupName` = '%s'",groupName));
        try {

            if(result.next()){
                if(result.getString(1).contentEquals(groupName)) return true;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public static void DeleteBuffers(){
        commandsBufferMap.clear();
    }
}