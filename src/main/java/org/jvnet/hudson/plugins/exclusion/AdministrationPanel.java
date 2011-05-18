package org.jvnet.hudson.plugins.exclusion;

import groovy.ui.SystemOutputInterceptor;
import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.model.Hudson;
import hudson.model.Items;
import hudson.model.Job;
import hudson.model.JobProperty;
import hudson.model.ParameterDefinition;
import hudson.model.ParameterValue;
import hudson.model.ParametersDefinitionProperty;
import hudson.model.Project;
import hudson.model.RootAction;
import hudson.model.Run;
import hudson.model.TopLevelItem;
import hudson.tasks.BuildWrapper;
import java.io.IOException;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

/**
 *
 * @author Anthony Roux
 */
@ExportedBean
@Extension
public class AdministrationPanel implements RootAction {

    private List<RessourcesMonitor> listRessources;
    private List<RessourcesMonitor> list;

    public List<RessourcesMonitor> getList() {
        return list;
    }

    public AdministrationPanel() {
        listRessources = IdAllocator.getListRessources();
    }

    public void load() {

      
        //List de tous les jobs
        List<String> allJobsName = new ArrayList<String>();

        //List de tous les jobs qui utilisent le plugin (avec une ressource au moins)
        List<String> allExclusionJobs = new ArrayList<String>();

        //On parcourt tous les projets
        for (Project<?, ?> p : Hudson.getInstance().getProjects()) {
           
            //On remplie la liste des noms de tous les projets
            allJobsName.add(p.getName());
            //On veut récuperer tous les composants BuildWrappers
            Map<Descriptor<BuildWrapper>, BuildWrapper> buildWrappers = p.getBuildWrappers();
            //Pour chacun d'entre eux 
            for (Iterator i = buildWrappers.keySet().iterator(); i.hasNext();) {
                Descriptor<BuildWrapper> key = (Descriptor<BuildWrapper>) i.next();
                //IdAllocator.jName = p.getName();
                // System.out.println("key = " + key.getDisplayName() + " value = " + buildWrappers.get(key).getDescriptor());
                //On regarde si le descripteur est bien "org.jvnet.hudson.plugins.exclusion.IdAllocator$DescriptorImpl"
                if (buildWrappers.get(key).getDescriptor().toString().split("@")[0].equals("org.jvnet.hudson.plugins.exclusion.IdAllocator$DescriptorImpl")) {
                    //Pas de doublons
                    if(!allExclusionJobs.contains(p.getName()))
                        allExclusionJobs.add(p.getName());
                }
            }
        }

            //On delete chaque job qui est dans la list global et qui n'est pas dans la liste des exclusions
            for (String jobName : allJobsName) {
                if (!allExclusionJobs.contains(jobName)) {
                    IdAllocator.deleteList(jobName);
                }
            }
        

        list = new ArrayList<RessourcesMonitor>();
        for (RessourcesMonitor rm : listRessources) {
            //System.out.println("build = " + rm.getBuild());
            list.add(new RessourcesMonitor(rm.getJobName(), rm.getRessource(),rm.getBuild()));
        }
    }
    
    public void jetest(){
        System.out.println("SALUTUTUTFJRZJR");
    }

    public String getIconFileName() {
        return "/plugin/exclusion/icons/exclusion.png";
    }

    public String getDisplayName() {
        return "Exclusion administration";
    }

    public String getUrlName() {
        return "/administrationpanel";
    }
}