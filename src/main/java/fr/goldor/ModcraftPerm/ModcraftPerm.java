package fr.goldor.ModcraftPerm;

import com.google.gson.JsonObject;

import com.mojang.brigadier.exceptions.CommandSyntaxException;

import fr.goldor.ModcraftPerm.Commands.ModcraftPermCommandDispatcher;
import fr.goldor.ModcraftPerm.DB.DataBaseConnection;
import fr.goldor.ModcraftPerm.System.CommandGroupManager;
import fr.goldor.ModcraftPerm.System.PlayerPermManager;
import fr.goldor.ModcraftPerm.utils.FuncUtils;
import fr.goldor.ModcraftPerm.utils.JsonManager;
import fr.goldor.ModcraftPerm.utils.References;

import net.minecraft.command.CommandSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.network.FMLNetworkConstants;

import net.minecraftforge.fml.network.event.EventNetworkChannel;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(References.MOD_ID)
public class ModcraftPerm
{
    private static final Logger LOGGER = LogManager.getLogger();
    private static MinecraftServer dedicatedServer;
    private static DataBaseConnection dataBaseConnection;

    public static Map<CommandSource,String> nextAuthorizedCommands = new HashMap<CommandSource, String>();

    public static Logger GetLogger(){
        return LOGGER;
    }
    public static MinecraftServer GetDedicatedServer(){
        return dedicatedServer;
    }
    public static DataBaseConnection GetDataBaseConnection(){
        return dataBaseConnection;
    }

    public ModcraftPerm() {

        ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.DISPLAYTEST, () -> Pair.of(() -> FMLNetworkConstants.IGNORESERVERONLY, (a, b) -> true));

        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);

        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(ModcraftPermCommandDispatcher.class);
    }

    private void setup(final FMLCommonSetupEvent event) {

    }

    @SubscribeEvent
    public void onServerStarting(FMLServerStartingEvent event) {

        ModcraftPermCommandDispatcher.Register(event.getServer().getCommandManager().getDispatcher());
        dedicatedServer = event.getServer();
        dataBaseConnection = new DataBaseConnection();

        LoadConfig();

        if(dataBaseConnection.isConnected){
            if(!CommandGroupManager.groupExist(References.DefaultGroupName)){
                CommandGroupManager.AddGroup(References.DefaultGroupName);
            }
        }
    }

    @SubscribeEvent
    public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event){

    if(dataBaseConnection.isConnected) {
        try {

            ResultSet result = dataBaseConnection.QuerySQLCommand("SELECT `playerName` FROM `player_perm` WHERE `playerUUID` = '"+event.getPlayer().getUniqueID().toString()+"'");

            if(result.next()){

                if(!result.getString(1).contentEquals(event.getPlayer().getDisplayName().getString())){
                    dataBaseConnection.ExecuteSQLCommand("UPDATE `player_perm` SET `playerName`= '"+event.getPlayer().getDisplayName().getString()+"' WHERE `playerID` = '"+result.getString(1)+"'");
                    LOGGER.warn("player : "+event.getPlayer().getDisplayName().getString()+" is successfully connected to the Data Base but with a new name !");
                }
                else{
                    LOGGER.info("player : "+event.getPlayer().getDisplayName().getString()+" is successfully connected to the Data Base !");
                }

            }
            else{
                LOGGER.warn("player : "+event.getPlayer().getDisplayName().getString()+" isn't in the Data Base !");
                LOGGER.info("Creating a new profile...");

                dataBaseConnection.ExecuteSQLCommand("INSERT INTO `player_perm`(`playerID`, `playerName`, `playerUUID`, `playerGroupsName`, `playerPerm`) VALUES (null,'"+event.getPlayer().getDisplayName().getString()+"','"+event.getPlayer().getUniqueID().toString()+"',null,null)");
                PlayerPermManager.AddGroup(event.getPlayer().getUniqueID().toString(),event.getPlayer().getDisplayName().getString(),References.DefaultGroupName);

                LOGGER.info("profile of player : "+event.getPlayer().getDisplayName().getString()+" created !");
            }

        } catch (SQLException e) { e.printStackTrace(); }
    }
    else{
        LOGGER.error("No Data Base to connect refusing connection if the client isn't in the ignore Data Base permission list");
            /*if(false){ //todo: check the ignore Data Base permission list

            }
            else{*/
        dedicatedServer.getPlayerList().getPlayerByUUID(event.getPlayer().getUniqueID()).connection.disconnect(new StringTextComponent("You've been disconnected by the permission manager because of the impossibility to connect to the Data Base : \nplease contact a server operator if this error still appear after retry"));
        //}
    }

}

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void OnCommand(CommandEvent event){

        CommandSource source = event.getParseResults().getContext().getSource();

        if(source.hasPermissionLevel(4)){ //if is op don't check perm
            return;
        }

        String CommandString = event.getParseResults().getReader().getString();

        //check if this command is send by the mod
        if(nextAuthorizedCommands.containsKey(source)){

            if(nextAuthorizedCommands.get(source).contentEquals(CommandString)){
                nextAuthorizedCommands.remove(source);

                return;
            }
            else {
                event.setCanceled(true);
            }

        }
        else {
            event.setCanceled(true);
        }

        Thread permissionChecker = new Thread(new Runnable() { // make it threaded to avoid lost time
            @Override
            public void run() {
                //translate to permissions syntax
                String permSyntax = "";
                try { // if command is unknown this will fail
                    permSyntax += event.getParseResults().getContext().getNodes().get(0).getNode().getUsageText();
                } catch (Exception e) {

                    FuncUtils.sendMessageToPlayer(source,String.format("%sUnknown command !",References.ErrorColor));

                    return;
                }

                for (int i = 1;i <event.getParseResults().getContext().getNodes().size();i++) {

                    permSyntax += ".";
                    if(event.getParseResults().getContext().getNodes().get(i).getNode().getUsageText().startsWith("<")) {
                        permSyntax += "argument";
                    }
                    else{
                        permSyntax += event.getParseResults().getContext().getNodes().get(i).getNode().getUsageText();
                    }

                }

                //check if user can run this command
                //([a-z]+|\\*)(\\.([a-z]|\\*)+)*#regex de mes commands
                try {
                    if(PlayerPermManager.PlayerHasPerm(event.getParseResults().getContext().getSource().asPlayer().getUniqueID().toString(),permSyntax)){
                        nextAuthorizedCommands.put(event.getParseResults().getContext().getSource(),event.getParseResults().getReader().getString());
                        ModcraftPerm.GetDedicatedServer().getCommandManager().handleCommand(source,CommandString);
                    }
                    else{
                        FuncUtils.sendMessageToPlayer(source,String.format("%sYou don't have the permission to execute this command !",References.ErrorColor));
                    }
                } catch (CommandSyntaxException e) { e.printStackTrace(); }
            }
        });

        permissionChecker.start();
    }

    /*@SubscribeEvent
    public void onPlayerPacket(){
        
    }*/

    public static void DeleteBuffers(){
        CommandGroupManager.DeleteBuffers();
        PlayerPermManager.DeleteBuffers();
    }

    public static void LoadAllBuffers(){
        Thread thread = new Thread(() -> {

            DeleteBuffers();
            for(int i = 0; i < ModcraftPerm.GetDedicatedServer().getPlayerList().getPlayers().size();i++){
                PlayerPermManager.ListAllRegisteredCommands(ModcraftPerm.GetDedicatedServer().getPlayerList().getPlayers().get(i).getUniqueID().toString()); //list player's perm store these in the buffer
            }

        });
        thread.start();
    }

    public static void LoadConfig(){

        if(JsonManager.FileExist(FMLPaths.CONFIGDIR.get().toString(),References.DataBaseInformationFileName)){

            ArrayList<JsonObject> jsonObjects = JsonManager.ReadJsonObjects(FMLPaths.CONFIGDIR.get().toString(),References.DataBaseInformationFileName);

            String DBURL = "";
            String USER = "";
            String PASSWORD = "";

            try{
                DBURL = jsonObjects.get(0).get("DataBaseURL").getAsString();
                USER = jsonObjects.get(0).get("User").getAsString();
                PASSWORD = jsonObjects.get(0).get("Password").getAsString();
            }catch (Exception e){
                e.printStackTrace();

                File file = new File(FMLPaths.CONFIGDIR.get().toString()+"\\"+References.DataBaseInformationFileName+".json");
                if(file.delete()){
                    LoadConfig();
                }
                else {
                    LOGGER.error("Can't delete config file : "+FMLPaths.CONFIGDIR.get().toString()+"\\"+References.DataBaseInformationFileName+".json");
                }

                return;
            }


            if(DBURL != "" && USER != "" && PASSWORD != ""){

                LOGGER.info("Connection to the DataBase ...");

                dataBaseConnection.Connect(DBURL,USER,PASSWORD);

                CommandGroupManager.Connect(dataBaseConnection);
                PlayerPermManager.Connect(dataBaseConnection);

            }else{
                LOGGER.error("Invalid value for connection to the DataBase in file : "+FMLPaths.CONFIGDIR.get().toString()+"\\"+References.DataBaseInformationFileName+".json");
            }

        }else {
            File file = new File(FMLPaths.CONFIGDIR.get().toString()+"\\"+References.DataBaseInformationFileName+".json");

            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            JsonObject DBInfo = new JsonObject();

            DBInfo.addProperty("DataBaseURL","");
            DBInfo.addProperty("User","");
            DBInfo.addProperty("Password","");

            JsonManager.WriteJsonObject(DBInfo,FMLPaths.CONFIGDIR.get().toString(),References.DataBaseInformationFileName);

            LOGGER.warn("please set Database information in file : "+FMLPaths.CONFIGDIR.get().toString()+"\\"+References.DataBaseInformationFileName+".json then execute command 'mperm reload-config' or restart the server");
        }

    }

}