package fr.goldor.ModcraftPerm.GUIs.PermInterface;


public class Perm<T extends IPermissions> implements IUserEditable {

    public T parent;
    public String permission;

    public Perm(T parentVar,String perm){
        parent = parentVar;
        permission = perm;
        initActions();
    }

    @Override
    public void initActions() {
        initAction(0,"Delete Permission",() -> {parent.delPermission(this);});
    }

    @Override
    public void initAction(int id, String actionDescription, Runnable action) {
        actionDesc.set(id,actionDescription);
        actions.set(id,action);
    }
}
