package org.jvnet.hudson.plugins.exclusion;

import hudson.model.AbstractBuild;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Anthony Roux
 */
public class RessourcesMonitor implements Cloneable {

    private String ressource;
    private String jobName;
    public boolean build = false;

    @Override
    public RessourcesMonitor clone() {
        RessourcesMonitor rm = null;
        try {
            rm = (RessourcesMonitor) super.clone();
        } catch (CloneNotSupportedException ex) {
            Logger.getLogger(RessourcesMonitor.class.getName()).log(Level.SEVERE, null, ex);
        }
        return rm;
    }

 

    public boolean getBuild() {
        return build;
    }

    public void setBuild(boolean build) {
        this.build = build;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public RessourcesMonitor(String jobName, String ressource) {
        this.ressource = ressource;
        this.jobName = jobName;
        this.build = false;
    }
    
     public RessourcesMonitor(String jobName, String ressource, boolean build) {
        this.ressource = ressource;
        this.jobName = jobName;
        this.build = build;
    }

    public String getRessource() {
        return ressource;
    }

    public void setRessource(String ressource) {
        this.ressource = ressource;
    }
}
