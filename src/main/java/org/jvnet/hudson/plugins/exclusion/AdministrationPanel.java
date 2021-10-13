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
import java.util.Map;

/**
 * Administration page model object.
 *
 * @author Anthony Roux
 * @author Oleksandr Kulychok
 */
@ExportedBean
@Extension
public class AdministrationPanel implements RootAction, StaplerProxy {

    // Link to the IdAllocator resources list
    private List<ResourcesMonitor> listResources;

    public AdministrationPanel() {
        super();
        listResources = IdAllocator.getListResources();
    }

    public Object getTarget() {
        Jenkins.get().checkPermission(Jenkins.ADMINISTER);
        return this;
    }

    @Deprecated
    public void load() {
        getList();
    }


    // List of declared resources over all Jobs (Oleksandr Kulychok: leave as is for now)
    @Restricted(NoExternalUse.class) // Exported for view
    // TODO why does this update stuff?
    public List<ResourcesMonitor> getList() {

        for (Project<?, ?> p : Jenkins.get().getProjects()) {
            if (p.getBuildWrappersList().get(IdAllocator.class) == null) {
                IdAllocator.deleteList(p.getName());
            }
        }

        // Set all builds to false (build = currently used)
        for (ResourcesMonitor rm : listResources) {
            rm.setBuild(false);
        }

        // For each resource Job, set build to true if a resource is used
        for (Map.Entry<String, Run<?, ?>> allocation : IdAllocationManager.getAllocations().entrySet()) {
            IdAllocator.updateBuild(allocation.getValue().getParent().getName(), allocation.getKey(), true);
        }

        ArrayList<ResourcesMonitor> list = new ArrayList<>(listResources.size());
        for (ResourcesMonitor rm : listResources) {
            list.add(new ResourcesMonitor(rm.getJobName(), rm.getResource(), rm.getBuild()));
        }

        return list;
    }


    @Restricted(NoExternalUse.class) // Exported for view
    public List<AllocatedResource> getAllocatedResources() {
        Map<String, Run<?, ?>> allocations = IdAllocationManager.getAllocations();

        List<AllocatedResource> result = new ArrayList<>();
        for (String id : allocations.keySet()) {
            Run<?, ?> run = allocations.get(id);
            AllocatedResource allocatedResource = new AllocatedResource();
            allocatedResource.resourceId = id;
            allocatedResource.runId = run.getExternalizableId(); // it is unique, see javadoc
            allocatedResource.runUrl = run.getUrl();
            result.add(allocatedResource);
        }

        return result;

    }


    public class AllocatedResource {
        public String resourceId;
        public String runUrl;  // url to 'build', but lets use jenkins core naming in java code
        public String runId;
    }


    //Called when we click on "release " link
    @RequirePOST
    @Restricted(NoExternalUse.class) // Exported for view
    public void doFreeResource(StaplerRequest res, StaplerResponse rsp,
                               @QueryParameter("resourceId") String resourceId,
                               @QueryParameter("runId") String runId
    ) throws IOException, InterruptedException {

        for (AllocatedResource resource : getAllocatedResources()) {

            // Check if the resource is the one chosen by the user
            if (resource.resourceId.equals(resourceId)) {
                // Check if the resource belong to the same run which user selected (he can has deprecated page)
                Run<?, ?> run = IdAllocationManager.getOwnerBuild(resourceId);
                if (run != null && run.getExternalizableId().equals(runId)) {
                    // Get the Id by resource name
                    DefaultIdType p = new DefaultIdType(resourceId);
                    // "null" for params not used, Only used to get the Id. TODO: should be refactored
                    Id id = p.allocate(false, null, CriticalBlockStart.pam, null, null);
                    // Release resource
                    id.cleanUp();
                }
            }
        }
        // Redirects  to the administration panel (refresh it)
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
