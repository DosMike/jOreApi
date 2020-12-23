package de.dosmike.spongepowered.oreapi.routes;

import de.dosmike.spongepowered.oreapi.OreApiV2;
import de.dosmike.spongepowered.oreapi.netobject.OreMemberList;
import de.dosmike.spongepowered.oreapi.netobject.OrePermission;
import de.dosmike.spongepowered.oreapi.netobject.OreProjectReference;
import de.dosmike.spongepowered.oreapi.netobject.OreRole;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class Members extends AbstractRoute {

    OreProjectReference project;

    public Members(OreApiV2 api, OreProjectReference reference) {
        super(api);
        this.project = reference.toReference();
    }

    // allows for convenience methods to one-call change members on remotes
    private Map<String, OreRole> memberRoles = null;

    /**
     * Retrieve the current list of members for this project with additional role information.
     * You will only see accepted roles unless you have {@link OrePermission#Manage_Subject_Members}
     * To update member roles call {@link OreMemberList#forPosting()} to get the mapping.
     */
    public CompletableFuture<OreMemberList> get() {
        return enqueue(NetTasks.getMembers(cm(), project)).thenApply(members -> {
            memberRoles = members.forPosting();
            return members;
        });
    }

    /**
     * Updates the member list for this project. Keep in mind that you have to send the whole list.
     * If you need to create the list, use {@link OreMemberList#forPosting()}
     * Keep in mind that a changed role is more like an invite and has to be accepted by the other party.
     *
     * @param roles a username -&gt; role mapping
     */
    public CompletableFuture<Void> set(Map<String, OreRole> roles) {
        return enqueue(NetTasks.setMembers(cm(), project, roles));
    }


}
