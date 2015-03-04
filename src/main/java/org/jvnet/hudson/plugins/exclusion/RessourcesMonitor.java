package org.jvnet.hudson.plugins.exclusion;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Anthony Roux
 */
public class RessourcesMonitor implements Cloneable {

    private String ressource;
    private String jobName;
    private boolean build = false;

    public RessourcesMonitor(String jobName, String ressource) {
        this(jobName, ressource, false);
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

    public String getRessource() {
        return ressource;
    }
}
