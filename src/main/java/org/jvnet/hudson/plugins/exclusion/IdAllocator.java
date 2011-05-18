package org.jvnet.hudson.plugins.exclusion;

import hudson.EnvVars;
import hudson.Extension;
import hudson.Launcher;
import hudson.matrix.MatrixConfiguration;
import hudson.model.BuildListener;
import hudson.model.Descriptor;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Computer;
import hudson.model.Describable;
import hudson.model.Executor;
import hudson.model.Hudson;
import hudson.model.Job;
import hudson.model.JobProperty;
import hudson.model.ParameterDefinition;
import hudson.model.ParametersDefinitionProperty;
import hudson.model.Project;
import hudson.model.Run.RunnerAbortedException;
import hudson.model.listeners.ItemListener;
import hudson.tasks.BuildWrapper;
import hudson.util.CopyOnWriteList;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.StaplerRequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.kohsuke.stapler.RequestImpl;
import sun.security.jca.GetInstance;

public class IdAllocator extends BuildWrapper {

    public IdType[] ids = null;
    public static  List<RessourcesMonitor> listRessources = new ArrayList<RessourcesMonitor>();
    public static String jName = "unknow";

    public static List<RessourcesMonitor> getListRessources() {
        return listRessources;
    }

    public static void setListRessources(List<RessourcesMonitor> list) {
        listRessources = list;
    }
    
    
    public static boolean isActivated = false;

    public IdAllocator(IdType[] ids) {
        this.ids = ids;
        isActivated = true;
        System.out.println("constructeur");
    }

    @Override
    public Environment setUp(AbstractBuild build, Launcher launcher, BuildListener listener) throws IOException, InterruptedException {
        System.out.println("setup");
        System.out.println(jName + " xxxxxxxx ");
        CriticalBlockStart.pa = this;
        isActivated = true;
        final List<String> allocated = new ArrayList<String>();
        final String buildName = build.toString();
        for (IdType pt : ids) {
            allocated.add(pt.name);
        }


        return new Environment() {
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

    public static void updateList(String oldProjecName, String newProjectName) {
        for (int i = listRessources.size() - 1; i >= 0; i--) {
            if (listRessources.get(i).getJobName().equals(oldProjecName)) {
                String ressource = listRessources.get(i).getRessource();
                listRessources.remove(i);
                listRessources.add(new RessourcesMonitor(newProjectName, ressource));
            }
        }
    }

    public static void deleteList(String ProjectName) {
        for (int i = listRessources.size() - 1; i >= 0; i--) {
            if (listRessources.get(i).getJobName().equals(ProjectName)) {
                listRessources.remove(i);
            }
        }
    }

    @Override
    public Descriptor<BuildWrapper> getDescriptor() {
        //  System.out.println("getdscriptor");

        String projectName = "unknow";
        System.out.println(jName + " ---- ");
        String[] threadName = Executor.currentThread().getName().split("\\\\");
        if (threadName.length > 1) {
            for (int i = 0; i < threadName.length; i++) {
                if (threadName[i].equals("jobs")) {
                    projectName = threadName[i + 1];
                }
            }
        } else {
            projectName = jName;
        }

        if (!projectName.equals("unknow")) {
            for (int i = listRessources.size() - 1; i >= 0; i--) {
                if (listRessources.get(i).getJobName().equals(projectName)) {
                    listRessources.remove(i);
                }
            }

            //Add all object for the current job

            for (IdType pt : ids) {
                listRessources.add(new RessourcesMonitor(projectName, pt.name));
            }
        }
        jName = "unknow";


        ////Version top si on arrive a get le nom du job dans le job sinon marche pas
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
            System.out.println("descri");
            load();
        }

        public String getDisplayName() {
            System.out.println("getdisplay");
            return "Add ressource to manage exclusion";

        }

        @Override
        public String getHelpFile() {
            System.out.println("helpfile");
            return "/plugin/port-allocator/help.html";
        }

        public List<IdTypeDescriptor> getIdTypes() {
            System.out.println("gitidtypes");
            return IdTypeDescriptor.LIST;

        }

        @Override
        public BuildWrapper newInstance(StaplerRequest req, JSONObject formData) throws FormException {
            System.out.println("new instance");
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