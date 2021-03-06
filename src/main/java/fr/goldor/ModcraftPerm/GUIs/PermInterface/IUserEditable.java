package fr.goldor.ModcraftPerm.GUIs.PermInterface;

import java.util.ArrayList;

public interface IUserEditable {
    ArrayList<String> actionDesc = new ArrayList<>();
    ArrayList<Runnable> actions = new ArrayList<>();

    void initAction(int id,String actionDescription,Runnable action);
    void initActions();
}
