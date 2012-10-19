package org.jvnet.hudson.plugins.exclusion;

import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.Computer;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * author Anthony Roux
 */
public class RessourcesMonitor implements Cloneable {

    private String ressource;
    private String jobName;
    public boolean build = false;
    private AbstractBuild<?, ?> absBuild = null;
    private Launcher launcher = null;

    public AbstractBuild<?, ?> getAbsBuild() {
        return absBuild;
    }

    public void setAbsBuild(AbstractBuild<?, ?> absBuild) {
        this.absBuild = absBuild;
    }

    public Launcher getLauncher() {
        return launcher;
    }

    public void setLauncher(Launcher launcher) {
        this.launcher = launcher;
    }

    public BuildListener getListener() {
        return listener;
    }

    public void setListener(BuildListener listener) {
        this.listener = listener;
    }
    private BuildListener listener = null;

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
        try {
			// For spaces, delete %20 from name
             this.jobName = URLDecoder.decode(jobName, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
          
        }
        this.build = false;
    }

    public RessourcesMonitor(String jobName, String ressource, boolean build) {
        this.ressource = ressource;
        // For spaces, delete %20 from name
        try {
            this.jobName = URLDecoder.decode(jobName, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
           
        }
        this.build = build;
    }

    public String getRessource() {
        return ressource;
    }

    public void setRessource(String ressource) {
        this.ressource = ressource;
    }
}
