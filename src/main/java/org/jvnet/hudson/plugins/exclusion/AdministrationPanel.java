package org.jvnet.hudson.plugins.exclusion;

import hudson.Extension;
import hudson.model.Project;
import hudson.model.RootAction;
import hudson.model.Run;
import jenkins.model.Jenkins;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerProxy;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.export.ExportedBean;
import org.kohsuke.stapler.interceptor.RequirePOST;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

/**
 * Administration page model object.
 *
 * @author Anthony Roux
 */
@ExportedBean
@Extension
public class AdministrationPanel implements RootAction, StaplerProxy {

    // Link to the IdAllocator resources list
    private List<RessourcesMonitor> listRessources;

    public AdministrationPanel() {
        super();
        listRessources = IdAllocator.getListRessources();
    }

    public Object getTarget() {
        Jenkins.getInstance().checkPermission(Jenkins.ADMINISTER);
        return this;
    }

    @Deprecated
    public void load() {
        getList();
    }

    @Restricted(NoExternalUse.class) // Exported for view
    // TODO why does this update stuff?
    public List<RessourcesMonitor> getList() {

        for (Project<?, ?> p : Jenkins.getInstance().getProjects()) {
            if (p.getBuildWrappersList().get(IdAllocator.class) == null) {
                IdAllocator.deleteList(p.getName());
            }
        }

        // Set all builds to false (build = currently used)
        for (RessourcesMonitor rm : listRessources) {
            rm.setBuild(false);
        }

        // For each resource Job, set build to true if a resource is used
        for (Entry<String, Run<?, ?>> allocation: IdAllocationManager.getAllocations().entrySet()) {
            IdAllocator.updateBuild(allocation.getValue().getParent().getName(), allocation.getKey(), true);
        }

        ArrayList<RessourcesMonitor> list = new ArrayList<RessourcesMonitor>(listRessources.size());
		for (RessourcesMonitor rm : listRessources) {
            list.add(new RessourcesMonitor(rm.getJobName(), rm.getRessource(), rm.getBuild()));
        }

        return list;
    }

	//Called when we click on "release resource" button
    @RequirePOST
    @Restricted(NoExternalUse.class) // Exported for view
    public void doFreeResource(StaplerRequest res, StaplerResponse rsp, @QueryParameter("resourceName") String resourceName) throws IOException, InterruptedException {
        for (RessourcesMonitor rm : getList()) {
            // Check if the resource is the one chosen by the user
            if (rm.getRessource().equals(resourceName) && rm.getBuild()) {

                // Get the Id by resource name
                DefaultIdType p = new DefaultIdType(resourceName);
                // "null" for params not used
                // Only used to get the Id
                Id i = p.allocate(false, null, CriticalBlockStart.pam, null, null);

                // Cleanup only if the job is currently using the resource
                // So we get the name of the job that uses the resource and we look in the list
                Run<?, ?> get = IdAllocationManager.getOwnerBuild(resourceName);
                if (get != null) {
                    if (get.getParent().getName().equals(rm.getJobName())) {
                        // Release resource
                        i.cleanUp();
                    }
                }
            }
        }
        // Redirects to the administration panel
        rsp.sendRedirect(res.getContextPath() + getUrlName());
    }

    public String getIconFileName() {
        return "/plugin/Exclusion/icons/exclusion.png";
    }

    public String getDisplayName() {
        return "Exclusion administration";
    }

    public String getUrlName() {
        return "/administrationpanel";
    }
}
