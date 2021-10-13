package org.jvnet.hudson.plugins.exclusion;

import hudson.DescriptorExtensionList;
import hudson.model.Descriptor;

import jenkins.model.Jenkins;

/**
 * @author Kohsuke Kawaguchi
 * @author Anthony Roux
 */
public abstract class IdTypeDescriptor extends Descriptor<IdType> {

    protected IdTypeDescriptor(Class<? extends IdType> clazz) {
        super(clazz);
    }

    public IdTypeDescriptor() {
        super();
    }

    public static DescriptorExtensionList<IdType,IdTypeDescriptor> all() {
        return Jenkins.get().getDescriptorList(IdType.class);
    }
}
