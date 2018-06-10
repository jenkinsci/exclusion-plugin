package org.jvnet.hudson.plugins.exclusion;

import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.*;
import hudson.model.listeners.ItemListener;
import hudson.tasks.BuildWrapper;
import jenkins.tasks.SimpleBuildWrapper;
import net.sf.json.JSONObject;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.stapler.StaplerRequest;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Kohsuke Kawaguchi
 * @author Anthony Roux
 */
public class IdAllocator extends SimpleBuildWrapper {

	//Resources currently configured in the job
    private IdType[] ids = null;
    private static List<ResourcesMonitor> listResources = new ArrayList<ResourcesMonitor>();
    private static String jName = "unknow";

    public IdAllocator(IdType[] ids) {
        this.ids = ids;
    }

    @Override
    public void setUp(Context context, Run<?, ?> run, FilePath filePath, Launcher launcher, TaskListener taskListener, EnvVars envVars) throws IOException, InterruptedException {
        final List<String> allocated = new ArrayList<String>();
        final List<Id> alloc = new ArrayList<Id>();
        final String buildName = run.getParent().getName();
        final Computer cur = Executor.currentExecutor().getOwner();
        final IdAllocationManager pam = IdAllocationManager.getManager(cur);

        for (IdType pt : ids) {
            allocated.add(pt.name);
            Id p = pt.allocate(false, run, pam, launcher, taskListener);
            alloc.add(p);
        }

        int i = 0;
        for (String p : allocated) {
            context.env("variableEnv" + buildName + i, p);
            context.env(p, p);
            i++;
        }

        context.setDisposer(new Disposer() {
            private static final long serialVersionUID = 1L;

            @Override
            public void tearDown(Run<?, ?> run, FilePath filePath, Launcher launcher, TaskListener taskListener) throws IOException, InterruptedException {
                for (Id p : alloc) {
                    Run<?, ?> get = IdAllocationManager.getOwnerBuild(p.type.name);
                    if (get != null) {
                        if (get.getParent().getName().equals(run.getParent().getName())) {
                            p.cleanUp();
                        }
                    }
                }
            }
        });


    }


    public IdType[] getIds() {
        return ids;
    }

    public static List<ResourcesMonitor> getListResources() {
        return listResources;
    }

    /**
     * This method update Job name
     * @param oldProjectName : Old project name
	 * @param newProjectName : New project name
     */
    private static void updateList(String oldProjectName, String newProjectName) {
        for (int i = listResources.size() - 1; i >= 0; i--) {
            if (listResources.get(i).getJobName().equals(oldProjectName)) {
                String resource = listResources.get(i).getResource();
                listResources.remove(i);
                listResources.add(new ResourcesMonitor(newProjectName, resource));
            }
        }
    }

    /**
     * This method removes all the resources of a project (Job)
     * @param ProjectName : Project name
     */
    /*package*/ static void deleteList(String ProjectName) {
        for (int i = listResources.size() - 1; i >= 0; i--) {
            if (listResources.get(i).getJobName().equals(ProjectName)) {
                listResources.remove(i);
            }
        }
    }

    /**
	 * This method changes the state of a resource (in use or not)
     * @param ProjectName : Project name
     * @param resourceName : Resource name
     * @param build : resource state (true = in use)
     */
    /*package*/ static void updateBuild(String ProjectName, String resourceName, boolean build) {
        for (int i = listResources.size() - 1; i >= 0; i--) {
            if (listResources.get(i).getJobName().equals(ProjectName) && listResources.get(i).getResource().equals(resourceName)) {
                ResourcesMonitor rmGet = listResources.get(i);
                listResources.remove(i);
                rmGet.setBuild(build);
                listResources.add(rmGet);
            }
        }
    }

    /**
     * Update allocations metadata in case Item is deleted or renamed.
     */
    @Restricted(NoExternalUse.class)
    @Extension
    public static final class RenameListener extends ItemListener {

        @Override
        public void onRenamed(Item item, String oldName, String newName) {
            IdAllocator.updateList(oldName, newName);
        }

        @Override
        public void onDeleted(Item item) {
            IdAllocator.deleteList(item.getName());
        }
    }

    @Override
    public Descriptor<BuildWrapper> getDescriptor() {
        String projectName;
	//A way to get the current project name
        String[] threadName = Executor.currentThread().getName().split(" ");

        if (threadName[0].equals("Loading") && threadName[1].equals("job")) {
            projectName = "";
            for (int i = 2; i < threadName.length - 1; i++) {
                projectName += threadName[i] + " ";
            }
            projectName += threadName[threadName.length - 1];
        } else {
            projectName = jName;
        }

        if (!projectName.equals("unknow")) {
            try {
		//Encoding for spaces
                projectName = URLDecoder.decode(projectName, "UTF-8");
            } catch (UnsupportedEncodingException ex) {
            }
			//Remove all resources
            for (int i = listResources.size() - 1; i >= 0; i--) {
                if (listResources.get(i).getJobName().equals(projectName)) {
                    listResources.remove(i);
                }
            }

            //Add all object for the current job
            for (IdType pt : getIds()) {
                listResources.add(new ResourcesMonitor(projectName, pt.name));
            }
        }
        jName = "unknow";


        //// will be good if i can get job name ...
       /* if (!jName.equals("unknow")) {
        for (int i = listResources.size() - 1; i >= 0; i--) {
        if (listResources.get(i).getJobName().equals(jName)) {
        listResources.remove(i);
        }
        }

        //Add all object for the current job
        for (IdType pt : ids) {
        System.out.println("jname " + jName + " / resource :" + pt.name);
        listResources.add(new ResourcesMonitor(jName, pt.name));
        }
        }*/
        return DESCRIPTOR;
    }

    @Extension
    public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

    public static final class DescriptorImpl extends Descriptor<BuildWrapper> {

        @Override
        public String getDisplayName() {
            return "Add resource to manage exclusion";
        }

        public List<IdTypeDescriptor> getIdTypes() {
            return IdTypeDescriptor.all();
        }

        @Override
        public BuildWrapper newInstance(StaplerRequest req, JSONObject formData) throws FormException {
            List<IdType> ids = Descriptor.newInstancesFromHeteroList(
                    req, formData, "ids", IdTypeDescriptor.all());
            // In some cases you can not get the job name as previously, so we let Newinstance do it
            String[] split = req.getReferer().split("/");
            for (int i = 0; i < split.length; i++) {
                if (split[i].equals("job")) {
                    setName(split[i + 1]);
                }
            }
            return new IdAllocator(ids.toArray(new IdType[ids.size()]));
        }

        // TODO introduced to keep things working in unittest too. jName has to die as soon as we have decent coverage.
        /*package*/ void setName(String name) {
            jName = name;
        }
    }
}
