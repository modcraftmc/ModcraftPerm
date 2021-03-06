package fr.goldor.ModcraftPerm.GUIs.PermInterface;

import net.minecraft.client.gui.screen.inventory.FurnaceScreen;

import java.util.ArrayList;
import java.util.Arrays;

public class RootElements {
    public static ArrayList<Group> groups = new ArrayList<>();
    public static ArrayList<Group> players = new ArrayList<>();

    public static void delGroup(Group group){
        //todo: send packet to server to change database permissions
        groups.remove(group);
    }

    public static void addGroup(String name,String[] perms){
        //todo: send packet to server to change database permissions
        groups.add(new Group(name, Arrays.asList(perms)));
        FurnaceScreen
    }
}
