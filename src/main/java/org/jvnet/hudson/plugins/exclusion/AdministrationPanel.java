package org.jvnet.hudson.plugins.exclusion;

import hudson.Extension;
import hudson.model.RootAction;
import hudson.model.AbstractBuild;
import hudson.model.Descriptor;
import hudson.model.Hudson;
import hudson.model.Project;
import hudson.tasks.BuildWrapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import jenkins.model.Jenkins;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerProxy;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.export.ExportedBean;

/**
 *
 * @author Anthony Roux
 */
@ExportedBean
@Extension
public class AdministrationPanel implements RootAction, StaplerProxy {

    // Link to the IdAllocator resources list
    private List<RessourcesMonitor> listRessources;
	// Local copy of the IdAllocator list
    private List<RessourcesMonitor> list;

    public List<RessourcesMonitor> getList() {
        return list;
    }

    public AdministrationPanel() {
        super();
        listRessources = IdAllocator.getListRessources();
    }

    public Object getTarget() {
        Jenkins.getInstance().checkPermission(Jenkins.ADMINISTER);
        return this;
    }

    //Called for each page load of administration
    public void load() {

		/* In case plugin is uncheck */
         // List all jobs
        List<String> allJobsName = new ArrayList<String>();

        // List all the jobs that use the plugin (with at least one resource)
        List<String> allExclusionJobs = new ArrayList<String>();

        // Check all projects
        for (Project<?, ?> p : Hudson.getInstance().getProjects()) {

            // Add all jobs names to the list
            allJobsName.add(p.getName());
            // We want to retrieve all components BuildWrappers
            Map<Descriptor<BuildWrapper>, BuildWrapper> buildWrappers = p.getBuildWrappers();
            // For each of them
            for (Iterator i = buildWrappers.keySet().iterator(); i.hasNext();) {
                Descriptor<BuildWrapper> key = (Descriptor<BuildWrapper>) i.next();

                // We check if the descriptor is "org.jvnet.hudson.plugins.exclusion.IdAllocator $ DescriptorImpl"
                if (buildWrappers.get(key).getDescriptor().toString().split("@")[0].equals("org.jvnet.hudson.plugins.exclusion.IdAllocator$DescriptorImpl")) {
                    // No duplicates
                    if (!allExclusionJobs.contains(p.getName())) {
                        allExclusionJobs.add(p.getName());
                    }
                }
            }
        }

		// We delete each job that is in the global list and not in the list of exclusions
        for (String jobName : allJobsName) {
            if (!allExclusionJobs.contains(jobName)) {
                IdAllocator.deleteList(jobName);
            }
        }

        // Set all builds to false (build = currently used)
        for (RessourcesMonitor rm : listRessources) {
            rm.setBuild(false);
        }

        // For each resource Job, set build to true if a resource is used
        for (Iterator i = IdAllocationManager.ids.keySet().iterator(); i.hasNext();) {
            String resource = (String) i.next();
            IdAllocator.updateBuild(IdAllocationManager.ids.get(resource).getProject().getName(), resource, true);
        }

        list = new ArrayList<RessourcesMonitor>();
		// Local copy of the list
        for (RessourcesMonitor rm : listRessources) {
            list.add(new RessourcesMonitor(rm.getJobName(), rm.getRessource(), rm.getBuild()));
        }


    }

	//Called when we click on "release resource" button
    public void doFreeResource(StaplerRequest res, StaplerResponse rsp, @QueryParameter("resourceName") String resourceName) throws IOException, InterruptedException {

        // For each resource
        for (RessourcesMonitor rm : list) {
            // Check if the resource is the one chosen by the user
            if (rm.getRessource().equals(resourceName) && rm.getBuild()) {

                // Get the Id by resource name
                DefaultIdType p = new DefaultIdType(resourceName);
                // "null" for params not used
                // Only used to get the Id
                Id i = p.allocate(false, null, CriticalBlockStart.pam, null, null);

                // Cleanup only if the job is currently using the resource
                // So we get the name of the job that uses the resource and we look in the list
                AbstractBuild get = IdAllocationManager.ids.get(resourceName);
                if (get != null) {
                    if (get.getProject().getName().equals(rm.getJobName())) {
                        // Release resource
                        i.cleanUp();
                    }
                }
            }
        }
        // Redirectsto the administration panel
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