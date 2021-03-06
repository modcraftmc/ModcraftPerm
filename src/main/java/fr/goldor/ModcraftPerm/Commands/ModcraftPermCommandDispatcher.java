package fr.goldor.ModcraftPerm.Commands;

import fr.goldor.ModcraftPerm.GUIs.TestGuiScreen;
import fr.goldor.ModcraftPerm.ModcraftPerm;
import fr.goldor.ModcraftPerm.System.CommandGroupManager;
import fr.goldor.ModcraftPerm.System.PlayerPermManager;
import fr.goldor.ModcraftPerm.System.PrettyListing;
import fr.goldor.ModcraftPerm.utils.FuncUtils;
import fr.goldor.ModcraftPerm.utils.References;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ModcraftPermCommandDispatcher {

    public static void Register(CommandDispatcher<CommandSource> dispatcher){
        dispatcher.register(Commands.literal("mperm")
                .then(Commands.literal("delete-buffers")
                    .executes(context -> deleteBuffers(context.getSource())))
                .then(Commands.literal("load-buffers")
                    .executes(context -> loadBuffers(context.getSource())))
                .then(Commands.literal("reload-config")
                    .executes(context -> reloadConfig()))
                .then(Commands.literal("interface")
                        .executes(context -> test(context.getSource())))
                .then(Commands.literal("list")
                    .then(Commands.literal("group")
                        .executes(context -> listGroups(context.getSource()))
                        .then(Commands.argument("groupName", StringArgumentType.string())
                            .then(Commands.literal("perms")
                                .executes(context -> listGroupPerms(context.getSource(),StringArgumentType.getString(context,"groupName"))))
                            .then(Commands.literal("players")
                                .executes(context -> listGroupPlayers(context.getSource(),StringArgumentType.getString(context,"groupName"))))))
                    .then(Commands.argument("playerName", StringArgumentType.string())
                        .then(Commands.literal("perms")
                            .executes(context -> listPlayerPerms(context.getSource(),StringArgumentType.getString(context,"playerName"))))
                        .then(Commands.literal("groups")
                            .executes(context -> listPlayerGroups(context.getSource(),StringArgumentType.getString(context,"playerName"))))))
                .then(Commands.literal("add")
                    .then(Commands.literal("group")
                        .then(Commands.argument("groupName", StringArgumentType.string())
                            .executes(context -> addGroup(context.getSource(),StringArgumentType.getString(context,"groupName")))
                            .then(Commands.literal("toPlayer")
                                .then(Commands.argument("player",StringArgumentType.string())
                                    .executes(context -> addGroupToPlayer(context.getSource(),StringArgumentType.getString(context,"groupName"),StringArgumentType.getString(context,"player")))))))
                    .then(Commands.literal("perm")
                        .then(Commands.argument("permArgument",StringArgumentType.string())
                            .then(Commands.literal("toPlayer")
                                .then(Commands.argument("player",StringArgumentType.string())
                                    .executes(context -> addPermToPlayer(context.getSource(), StringArgumentType.getString(context,"permArgument"), StringArgumentType.getString(context,"player")))))
                            .then(Commands.literal("toGroup")
                                .then(Commands.argument("group",StringArgumentType.string())
                                    .executes(context -> addPermToGroup(context.getSource(), StringArgumentType.getString(context,"permArgument"), StringArgumentType.getString(context,"group"))))))))
                .then(Commands.literal("del")
                    .then(Commands.literal("group")
                        .then(Commands.argument("groupName", StringArgumentType.string())
                            .executes(context -> delGroup(context.getSource(),StringArgumentType.getString(context,"groupName")))
                            .then(Commands.literal("toPlayer")
                                .then(Commands.argument("player",StringArgumentType.string())
                                    .executes(context -> delGroupToPlayer(context.getSource(),StringArgumentType.getString(context,"groupName"),StringArgumentType.getString(context,"player")))))))
                    .then(Commands.literal("perm")
                        .then(Commands.argument("permArgument",StringArgumentType.string())
                            .then(Commands.literal("toPlayer")
                                .then(Commands.argument("player",StringArgumentType.string())
                                    .executes(context -> delPermToPlayer(context.getSource(), StringArgumentType.getString(context,"permArgument"), StringArgumentType.getString(context,"player")))))
                                .then(Commands.literal("toGroup")
                                    .then(Commands.argument("group",StringArgumentType.string())
                                        .executes(context -> delPermToGroup(context.getSource(), StringArgumentType.getString(context,"permArgument"), StringArgumentType.getString(context,"group"))))))))
        );
    }

    private static int reloadConfig(){
        ModcraftPerm.LoadConfig();
        return 1;
    }

    private static int deleteBuffers(CommandSource source){
        ModcraftPerm.DeleteBuffers();
        FuncUtils.sendMessageToPlayer(source, String.format("%sBuffers has been deleted.",References.SuccessfulColor));
        return 1;
    }

    private static int loadBuffers(CommandSource source){
        ModcraftPerm.LoadAllBuffers();
        FuncUtils.sendMessageToPlayer(source,String.format("%sBuffers has been loaded.",References.SuccessfulColor));
        return 1;
    }

    private static int listGroups(CommandSource source){
        Thread thread = new Thread(() -> {
            PrettyListing listing = new PrettyListing();
            String list = String.format("Here are the groups : %s%s",References.GroupColor,listing.list(CommandGroupManager.ListGroups()));
            FuncUtils.sendMessageToPlayer(source,list);
        });
        thread.start();
        return 1;
    }

    private static int listGroupPerms(CommandSource source,String groupName){
        Thread thread = new Thread(() -> {
            if (!CommandGroupManager.groupExist(groupName)){
                FuncUtils.sendMessageToPlayer(source,String.format("%sGroup : %s%s%s doesn't exist !",References.ErrorColor,References.GroupColor,groupName,References.ErrorColor));
                return;
            }

            PrettyListing listing = new PrettyListing();
            String list = listing.listPermission(CommandGroupManager.ListRegisteredCommand(groupName,false),"\\.");
            FuncUtils.sendMessageToPlayer(source,String.format("Here a list of %s%s's%s permissions %s%s",References.GroupColor,groupName,References.MainColor,References.PermColor,list));
        });
        thread.start();
        return 1;
    }

    private static int listGroupPlayers(CommandSource source,String groupName){
        Thread thread = new Thread(() -> {
            if(!CommandGroupManager.groupExist(groupName)){
                FuncUtils.sendMessageToPlayer(source,String.format("%sGroup : %s%s%s doesn't exist !",References.ErrorColor,References.GroupColor,groupName,References.ErrorColor));
                return;
            }

            PrettyListing listing = new PrettyListing();

            String[] playersUUID = CommandGroupManager.ListRegisteredPlayer(groupName);
            String[] playerName = new String[playersUUID.length];
            for (int i = 0; i < playersUUID.length; i++) {
                playerName[i] = PlayerPermManager.GetPlayerNameByUUID(playersUUID[i]);
            }

            String list = String.format("Here are the players assign to the group : %s%s%s%s",References.GroupColor,groupName,References.PseudoColor,listing.list(playerName));

            FuncUtils.sendMessageToPlayer(source,list);
        });
        thread.start();

        return 1;
    }

    private static int listPlayerPerms(CommandSource source,String playerName){
        Thread thread = new Thread(() -> {
            PlayerEntity player = FuncUtils.getOnlyOnePlayer(source,playerName);
            if(player != null) {

                PrettyListing listing = new PrettyListing();
                String list = listing.listPermission(PlayerPermManager.ListSpecificRegisteredCommands(player.getUniqueID().toString(),false), "\\.");
                FuncUtils.sendMessageToPlayer(source, String.format("Here a list of %s%s's%s permissions %s%s", References.PseudoColor, player.getDisplayName().getString(), References.MainColor, References.PermColor, list));

            }
        });
        thread.start();
        return 1;
    }

    private static int listPlayerGroups(CommandSource source,String playerName){
        Thread thread = new Thread(() -> {
            PlayerEntity player = FuncUtils.getOnlyOnePlayer(source,playerName);
            if(player != null) {
                PrettyListing listing = new PrettyListing();
                String[] playerGroups = PlayerPermManager.ListRegisteredGroupName(player.getUniqueID().toString(),false);

                String list = String.format("Here are the groups assign to the player : %s%s%s%s",References.PseudoColor,player.getDisplayName().getString(),References.GroupColor,listing.list(playerGroups));

                FuncUtils.sendMessageToPlayer(source,list);
            }
        });
        thread.start();

        return 1;
    }

    public static int addGroup(CommandSource source,String groupName){
        Thread thread = new Thread(() -> FuncUtils.sendMessageToPlayer(source,CommandGroupManager.AddGroup(groupName)));
        thread.start();

        return 1;
    }

    public static int delGroup(CommandSource source,String groupName){
        Thread thread = new Thread(() -> FuncUtils.sendMessageToPlayer(source,CommandGroupManager.RemoveGroup(groupName)));
        thread.start();


        return 1;
    }

    public static int addGroupToPlayer(CommandSource source, String groupName, String playerName){
        Thread thread = new Thread(() -> {

            PlayerEntity player = FuncUtils.getOnlyOnePlayer(source,playerName);
            if(player != null){
                FuncUtils.sendMessageToPlayer(source,PlayerPermManager.AddGroup(player.getUniqueID().toString(),player.getDisplayName().getString(),groupName));
            }

        });
        thread.start();

        return 1;
    }

    public static int addPermToPlayer(CommandSource source, String perm, String playerName){
        Thread thread = new Thread(() -> {

            if(perm.matches("([a-z]+|\\*)(\\.([a-z]|\\*)+)*")) {
                PlayerEntity player = FuncUtils.getOnlyOnePlayer(source,playerName);
                if(player != null){
                    FuncUtils.sendMessageToPlayer(source,PlayerPermManager.RegisterCommand(player.getUniqueID().toString(),player.getDisplayName().getString(),perm));
                }
            }
            else {
                FuncUtils.sendMessageToPlayer(source,String.format("%sPermission : %s%s%s do not match the syntax rule",References.ErrorColor,References.MainColor,perm,References.ErrorColor));
            }

        });
        thread.start();

        return 1;
    }

    public static int addPermToGroup(CommandSource source, String perm, String groupName){
        Thread thread = new Thread(() -> {

            if(perm.matches("([a-z]+|\\*)(\\.([a-z]|\\*)+)*")) {
                FuncUtils.sendMessageToPlayer(source, CommandGroupManager.RegisterCommand(perm, groupName));
            }
            else {
                FuncUtils.sendMessageToPlayer(source,String.format("%sPermission : %s%s%s do not match the syntax rule",References.ErrorColor,References.MainColor,perm,References.ErrorColor));
            }

        });
        thread.start();

        return 1;
    }

    public static int delGroupToPlayer(CommandSource source,String groupName,String playerName){
        Thread thread = new Thread(() -> {

            PlayerEntity player = FuncUtils.getOnlyOnePlayer(source,playerName);
            if(player != null){
                FuncUtils.sendMessageToPlayer(source,PlayerPermManager.RemoveGroup(player.getUniqueID().toString(),player.getDisplayName().getString(),groupName));
            }

        });
        thread.start();

        return 1;
    }

    public static int delPermToPlayer(CommandSource source, String perm, String playerName){
        Thread thread = new Thread(() -> {

            if(perm.matches("([a-z]+|\\*)(\\.([a-z]|\\*)+)*")) {
                PlayerEntity player = FuncUtils.getOnlyOnePlayer(source,playerName);
                if(player != null){
                    FuncUtils.sendMessageToPlayer(source,PlayerPermManager.UnregisterCommand(player.getUniqueID().toString(),player.getDisplayName().getString(),perm));
                }
            }
            else {
                FuncUtils.sendMessageToPlayer(source,String.format("%sPermission : %s%s%s do not match the syntax rule",References.ErrorColor,References.MainColor,perm,References.ErrorColor));
            }

        });
        thread.start();


        return 1;
    }

    public static int delPermToGroup(CommandSource source, String perm, String groupName){
        Thread thread = new Thread(() -> {

            if(perm.matches("([a-z]+|\\*)(\\.([a-z]|\\*)+)*")) {
                FuncUtils.sendMessageToPlayer(source, CommandGroupManager.UnregisterCommand(perm, groupName));
            }
            else {
                FuncUtils.sendMessageToPlayer(source,String.format("%sPermission : %s%s%s do not match the syntax rule",References.ErrorColor,References.MainColor,perm,References.ErrorColor));
            }

        });
        thread.start();

        return 1;
    }

    @OnlyIn(Dist.CLIENT)
    public static int test(CommandSource source){
        Minecraft.getInstance().displayGuiScreen(new TestGuiScreen());
        return 1;
    }

}