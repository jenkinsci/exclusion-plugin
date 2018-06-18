package org.jvnet.hudson.plugins.exclusion;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Anthony Roux
 */
public class ResourcesMonitor implements Cloneable {

    private String resource;
    private String jobName;
    private boolean build = false;

    public ResourcesMonitor(String jobName, String resource) {
        this(jobName, resource, false);
    }

    public ResourcesMonitor(String jobName, String resource, boolean build) {
        this.resource = resource;
        // For spaces, delete %20 from name
        try {
            this.jobName = URLDecoder.decode(jobName, "UTF-8");
        } catch (UnsupportedEncodingException ex) {

        }
        this.build = build;
    }

    @Override
    public ResourcesMonitor clone() {
        ResourcesMonitor rm = null;
        try {
            rm = (ResourcesMonitor) super.clone();
        } catch (CloneNotSupportedException ex) {
            Logger.getLogger(ResourcesMonitor.class.getName()).log(Level.SEVERE, null, ex);
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

    public String getResource() {
        return resource;
    }
}
