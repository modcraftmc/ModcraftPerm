package fr.goldor.ModcraftPerm.GUIs.PermInterface;

import java.util.Collection;
import java.util.List;

public class Group implements IPermissions, IUserEditable {
    public String name;

    public Group(String nameVar, Collection<? extends String> col){
        name = nameVar;
        col.forEach(s -> permissions.add(new Perm<>(this,s)));
    }

    @Override
    public void addPermission(Perm perm) {
        //todo: send packet to server to change database permissions
        permissions.add(perm);
    }

    @Override
    public void addPermissions(Collection<? extends Perm> col) {
        //todo: send packet to server to change database permissions
        permissions.addAll(col);
    }

    @Override
    public void delPermission(Perm perm) {
        //todo: send packet to server to change database permissions
        permissions.remove(perm);
    }

    @Override
    public void delPermissions(Collection<? extends Perm> col) {
        //todo: send packet to server to change database permissions
        permissions.removeAll(col);
    }



    @Override
    public void initActions() {
        initAction(0,"Delete Group",() -> {RootElements.delGroup(this);});
        initAction(1,"Add Permission",() -> {}); //todo: make new interface to get string
    }

    @Override
    public void initAction(int id, String actionDescription, Runnable action) {
        actionDesc.set(id,actionDescription);
        actions.set(id,action);
    }
}
