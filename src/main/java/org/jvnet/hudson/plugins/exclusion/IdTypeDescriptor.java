package org.jvnet.hudson.plugins.exclusion;

import hudson.DescriptorExtensionList;
import hudson.model.Descriptor;

import jenkins.model.Jenkins;

import org.kohsuke.stapler.StaplerRequest;

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

    @Override
    public final IdType newInstance(StaplerRequest req) throws FormException {
        throw new UnsupportedOperationException();
    }

    public static DescriptorExtensionList<IdType,IdTypeDescriptor> all() {
        return Jenkins.getInstance().<IdType,IdTypeDescriptor>getDescriptorList(IdType.class);
    }
}
