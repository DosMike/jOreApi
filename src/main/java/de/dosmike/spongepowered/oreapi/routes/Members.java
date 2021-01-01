package de.dosmike.spongepowered.oreapi.routes;

import de.dosmike.spongepowered.oreapi.OreApiV2;
import de.dosmike.spongepowered.oreapi.netobject.OreMemberList;
import de.dosmike.spongepowered.oreapi.netobject.OrePermission;
import de.dosmike.spongepowered.oreapi.netobject.OreProjectReference;
import de.dosmike.spongepowered.oreapi.netobject.OreRole;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Shared Route for Project and Organization members
 *
 * @see Project
 * @see Organization
 */
public abstract class Members extends AbstractRoute {

    private Members(OreApiV2 api) {
        super(api);
    }

    // allows for convenience methods to one-call change members on remotes
    protected Map<String, OreRole> memberRoles = null;

    /**
     * Retrieve the current list of members for this object with additional role information.
     * You will only see accepted roles unless you have {@link OrePermission#Manage_Subject_Members}
     * To update member roles call {@link OreMemberList#forPosting()} to get the mapping.
     */
    public abstract CompletableFuture<OreMemberList> get();

    /**
     * Updates the member list for this object. Keep in mind that you have to send the whole list.
     * If you need to create the list, use {@link OreMemberList#forPosting()}<br>
     * Keep in mind that a changed role is more like an invite and has to be accepted by the other party.
     *
     * @param roles a username -&gt; role mapping
     */
    public abstract CompletableFuture<Void> set(Map<String, OreRole> roles);

    /**
     * Route for project members
     */
    public static class Project extends Members {

        OreProjectReference project;

        Project(OreApiV2 api, OreProjectReference reference) {
            super(api);
            this.project = reference.toReference();
        }

        public CompletableFuture<OreMemberList> get() {
            return enqueue(NetTasks.getProjectMembers(cm(), project)).thenApply(members -> {
                memberRoles = members.forPosting();
                return members;
            });
        }

        public CompletableFuture<Void> set(Map<String, OreRole> roles) {
            return enqueue(NetTasks.setProjectMembers(cm(), project, roles));
        }

    }

    /**
     * Route for organization members
     */
    public static class Organization extends Members {

        String organization;

        Organization(OreApiV2 api, String organization) {
            super(api);
            this.organization = organization;
        }

        public CompletableFuture<OreMemberList> get() {
            return enqueue(NetTasks.getOrganizationMembers(cm(), organization)).thenApply(members -> {
                memberRoles = members.forPosting();
                return members;
            });
        }

        public CompletableFuture<Void> set(Map<String, OreRole> roles) {
            return enqueue(NetTasks.setOrganizationMembers(cm(), organization, roles));
        }


    }

}
