package org.jvnet.hudson.plugins.exclusion;

import hudson.Extension;
import hudson.model.AbstractBuild;
import hudson.model.Descriptor;
import hudson.model.Hudson;
import hudson.model.Project;
import hudson.model.RootAction;
import hudson.tasks.BuildWrapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.export.ExportedBean;

/**
 *
 * @author Anthony Roux
 */
@ExportedBean
@Extension
public class AdministrationPanel implements RootAction {

    //Lien vers la list ressource de IdAllocator
    private List<RessourcesMonitor> listRessources;
    //Copie de la list de IdAllocator en local
    private List<RessourcesMonitor> list;

    public List<RessourcesMonitor> getList() {
        return list;
    }

    public AdministrationPanel() {
        super();
        listRessources = IdAllocator.getListRessources();
    }

    //Appellé à chaque chargement de la page d'administration
    public void load() {

    /* Pour le cas on l'on décoche le plugin */
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

                //On regarde si le descripteur est bien "org.jvnet.hudson.plugins.exclusion.IdAllocator$DescriptorImpl"
                if (buildWrappers.get(key).getDescriptor().toString().split("@")[0].equals("org.jvnet.hudson.plugins.exclusion.IdAllocator$DescriptorImpl")) {
                    //Pas de doublons
                    if (!allExclusionJobs.contains(p.getName())) {
                        allExclusionJobs.add(p.getName());
                    }
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
            list.add(new RessourcesMonitor(rm.getJobName(), rm.getRessource(), rm.getBuild()));
        }
    }

    public void doFreeResource(StaplerRequest res, StaplerResponse rsp, @QueryParameter("resourceName") String resourceName) throws IOException, InterruptedException {

        //Pour chaque ressource
        //
        for (RessourcesMonitor rm : list) {
            //On veut prendre que les ressources qui sont donné par l'utilisateur
            // Et en cours d'utilisation
            if (rm.getRessource().equals(resourceName) && rm.getBuild()) {

                //On récupere l'id à libérer
                DefaultIdType p = new DefaultIdType(resourceName);
                Id i = p.allocate(false, rm.getAbsBuild(), CriticalBlockStart.pam, rm.getLauncher(), rm.getListener());

                //On veut le faire seulement pour le cas où :
                // le job est celui qui utilise la ressource actuellement
                // donc on recupere le nom du job qui utilise la ressource et on cherche dans la liste
                AbstractBuild get = IdAllocationManager.ids.get(resourceName);
                if (get != null) {
                    if (get.getProject().getName().equals(rm.getJobName())) {
                        //Libere la ressource
                        i.cleanUp();
                        //Passe la ressource à "false" pour dire qu'elle n'est plus utilisé (pour l'affichage)
                        IdAllocator.updateBuild(rm.getJobName(), resourceName, false);
                    }
                }
            }
        }
        //On redirige sur la page du panel d'administration
        rsp.sendRedirect(res.getContextPath() + getUrlName());
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