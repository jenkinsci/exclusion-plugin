package org.jvnet.hudson.plugins.exclusion;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.Descriptor;
import hudson.model.AbstractBuild;
import hudson.model.Computer;
import hudson.model.Executor;
import hudson.tasks.BuildWrapper;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.StaplerRequest;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * first author Kohsuke Kawaguchi
 * fork by Anthony Roux
 */
public class IdAllocator extends BuildWrapper {

    private IdType[] ids = null;
    public static List<RessourcesMonitor> listRessources = new ArrayList<RessourcesMonitor>();
    private static String jName = "unknow";

    public IdAllocator(IdType[] ids) {
        this.ids = ids;
    }

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

            @Override
            public boolean tearDown(AbstractBuild abstractBuild, BuildListener buildListener) throws IOException, InterruptedException {
                for (Id p : alloc) {
                    AbstractBuild get = IdAllocationManager.ids.get(p.type.name);
                    if (get != null) {
                        if (get.getProject().getName().equals(abstractBuild.getProject().getName())) {
                            p.cleanUp();
                        }
                    }
                }
                return true;
            }

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

    public static void setListRessources(List<RessourcesMonitor> list) {
        listRessources = list;
    }

    public static void updateList(String oldProjecName, String newProjectName) {
        for (int i = listRessources.size() - 1; i >= 0; i--) {
            if (listRessources.get(i).getJobName().equals(oldProjecName)) {
                String ressource = listRessources.get(i).getRessource();
                listRessources.remove(i);
                listRessources.add(new RessourcesMonitor(newProjectName, ressource));
            }
        }
    }

    /**
     * Cette methode permet de supprimer toutes les ressources d'un projet (Job)
     * @param ProjectName : Nom du projet
     */
    public static void deleteList(String ProjectName) {
        for (int i = listRessources.size() - 1; i >= 0; i--) {
            if (listRessources.get(i).getJobName().equals(ProjectName)) {
                listRessources.remove(i);
            }
        }
    }

    /**
     * Cette méthode change l'état d'une ressource (en cours d'utilisation ou non)
     * @param ProjectName : Nom du projet
     * @param resourceName : Resource à modifie
     * @param build : etat de la ressource (true = en cours d'utilisation)
     */
    public static void updateBuild(String ProjectName, String resourceName, boolean build) {
        for (int i = listRessources.size() - 1; i >= 0; i--) {
            if (listRessources.get(i).getJobName().equals(ProjectName) && listRessources.get(i).getRessource().equals(resourceName)) {
                RessourcesMonitor rmGet = listRessources.get(i);
                listRessources.remove(i);
                rmGet.setBuild(build);
                listRessources.add(rmGet);
            }
        }
    }

    @Override
    public Descriptor<BuildWrapper> getDescriptor() {
        String projectName = "unknow";
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
                projectName = URLDecoder.decode(projectName, "UTF-8");
            } catch (UnsupportedEncodingException ex) {
            }
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

        public String getDisplayName() {
            return "Add resource to manage exclusion";

        }

        @Override
        public String getHelpFile() {
            return "/plugin/exclusion/help.html";
        }

        public List<IdTypeDescriptor> getIdTypes() {
            return IdTypeDescriptor.LIST;

        }

        @Override
        public BuildWrapper newInstance(StaplerRequest req, JSONObject formData) throws FormException {
            List<IdType> ids = Descriptor.newInstancesFromHeteroList(
                    req, formData, "ids", IdTypeDescriptor.LIST);
            String[] split = req.getReferer().split("/");
            for (int i = 0; i < split.length; i++) {
                if (split[i].equals("job")) {
                    jName = split[i + 1];
                }
            }
            IdAllocator portAlloc = new IdAllocator(ids.toArray(new IdType[ids.size()]));
            return portAlloc;
        }
    }
}