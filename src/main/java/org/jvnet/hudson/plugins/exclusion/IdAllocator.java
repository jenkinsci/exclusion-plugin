package org.jvnet.hudson.plugins.exclusion;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.model.Computer;
import hudson.model.Descriptor;
import hudson.model.Executor;
import hudson.model.Item;
import hudson.model.listeners.ItemListener;
import hudson.tasks.BuildWrapper;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONObject;

import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.stapler.StaplerRequest;

/**
 * @author Kohsuke Kawaguchi
 * @author Anthony Roux
 */
public class IdAllocator extends BuildWrapper {

	//Resources currently configured in the job
    private IdType[] ids = null;
    private static List<RessourcesMonitor> listRessources = new ArrayList<RessourcesMonitor>();
    private static String jName = "unknow";

    public IdAllocator(IdType[] ids) {
        this.ids = ids;
    }

	//Called when a job which use Exclusion plugin is started
    @Override
    public Environment setUp(AbstractBuild build, Launcher launcher, BuildListener listener) throws IOException, InterruptedException {
        final List<String> allocated = new ArrayList<String>();
        final List<Id> alloc = new ArrayList<Id>();
        final String buildName = build.getProject().getName();
        final Computer cur = Executor.currentExecutor().getOwner();
        final IdAllocationManager pam = IdAllocationManager.getManager(cur);
        for (IdType pt : ids) {
            allocated.add(pt.name);
            Id p = pt.allocate(false, build, pam, launcher, listener);
            alloc.add(p);
        }


        return new Environment() {

			//Called when a job finished
			// Released resource
            @Override
            public boolean tearDown(AbstractBuild abstractBuild, BuildListener buildListener) throws IOException, InterruptedException {
                for (Id p : alloc) {
                    AbstractBuild<?, ?> get = IdAllocationManager.getOwnerBuild(p.type.name);
                    if (get != null) {
                        if (get.getProject().getName().equals(abstractBuild.getProject().getName())) {
                            p.cleanUp();
                        }
                    }
                }
                return true;
            }

			//Called when a job started
			// Add environmental variables for each resource
            @Override
            public void buildEnvVars(Map<String, String> env) {
                int i = 0;
                for (String p : allocated) {
                    env.put("variableEnv" + buildName + i, p);
                    env.put(p, p);
                    i++;
                }
            }
        };
    }

    public IdType[] getIds() {
        return ids;
    }

    public void setIds(IdType[] ids) {
        this.ids = ids;
    }

    public static List<RessourcesMonitor> getListRessources() {
        return listRessources;
    }

    /**
     * This method update Job name
     * @param oldProjecName : Old project name
	 * @param newProjectName : New project name
     */
    private static void updateList(String oldProjecName, String newProjectName) {
        for (int i = listRessources.size() - 1; i >= 0; i--) {
            if (listRessources.get(i).getJobName().equals(oldProjecName)) {
                String ressource = listRessources.get(i).getRessource();
                listRessources.remove(i);
                listRessources.add(new RessourcesMonitor(newProjectName, ressource));
            }
        }
    }

    /**
     * This method removes all the resources of a project (Job)
     * @param ProjectName : Project name
     */
    /*package*/ static void deleteList(String ProjectName) {
        for (int i = listRessources.size() - 1; i >= 0; i--) {
            if (listRessources.get(i).getJobName().equals(ProjectName)) {
                listRessources.remove(i);
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
        for (int i = listRessources.size() - 1; i >= 0; i--) {
            if (listRessources.get(i).getJobName().equals(ProjectName) && listRessources.get(i).getRessource().equals(resourceName)) {
                RessourcesMonitor rmGet = listRessources.get(i);
                listRessources.remove(i);
                rmGet.setBuild(build);
                listRessources.add(rmGet);
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
        String projectName = "unknow";
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
            for (int i = listRessources.size() - 1; i >= 0; i--) {
                if (listRessources.get(i).getJobName().equals(projectName)) {
                    listRessources.remove(i);
                }
            }

            //Add all object for the current job
            for (IdType pt : getIds()) {
                listRessources.add(new RessourcesMonitor(projectName, pt.name));
            }
        }
        jName = "unknow";


        //// will be good if i can get job name ...
       /* if (!jName.equals("unknow")) {
        for (int i = listRessources.size() - 1; i >= 0; i--) {
        if (listRessources.get(i).getJobName().equals(jName)) {
        listRessources.remove(i);
        }
        }
        
        //Add all object for the current job
        for (IdType pt : ids) {
        System.out.println("jname " + jName + " / ressource :" + pt.name);
        listRessources.add(new RessourcesMonitor(jName, pt.name));
        }
        }*/
        return DESCRIPTOR;
    }
	
    @Extension
    public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

    public static final class DescriptorImpl extends Descriptor<BuildWrapper> {

        DescriptorImpl() {
            super(IdAllocator.class);
            load();
        }

        @Override
        public String getDisplayName() {
            return "Add resource to manage exclusion";

        }

        @Override
        public String getHelpFile() {
            return "/plugin/Exclusion/help.html";
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
            IdAllocator portAlloc = new IdAllocator(ids.toArray(new IdType[ids.size()]));
            return portAlloc;
        }

        // TODO introduced to keep things working in unittest too. jName has to die as soon as we have decent coverage.
        /*package*/ void setName(String name) {
            jName = name;
        }
    }
}
