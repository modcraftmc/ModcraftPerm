package fr.goldor.ModcraftPerm.GUIs.PermInterface;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public interface IPermissions {
    ArrayList<Perm> permissions = new ArrayList<>();
    void addPermission(Perm perm);
    void addPermissions(Collection<? extends Perm> col);
    void delPermission(Perm perm);
    void delPermissions(Collection<? extends Perm> col);
}
