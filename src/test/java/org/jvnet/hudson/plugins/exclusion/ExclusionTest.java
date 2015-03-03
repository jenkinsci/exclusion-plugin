/*
 * The MIT License
 *
 * Copyright (c) 2015 Red Hat, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.jvnet.hudson.plugins.exclusion;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.FreeStyleBuild;
import hudson.model.Result;
import hudson.model.AbstractBuild;
import hudson.model.FreeStyleProject;
import hudson.model.queue.QueueTaskFuture;
import hudson.util.OneShotEvent;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import jenkins.model.CauseOfInterruption;

import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.TestBuilder;

public class ExclusionTest {

    @Rule public JenkinsRule j = new JenkinsRule();

    @Test
    public void acquireTheResource() throws Exception {
        BlockingBuilder blocker = new BlockingBuilder();

        FreeStyleProject p = j.createFreeStyleProject();
        p.getBuildWrappersList().add(defaultAlocatorForResources("resource1", "resource2"));
        p.getBuildersList().add(new CriticalBlockStart());
        p.getBuildersList().add(blocker);
        p.getBuildersList().add(new CriticalBlockEnd());

        final QueueTaskFuture<FreeStyleBuild> future = p.scheduleBuild2(0);
        FreeStyleBuild b = future.waitForStart();
        Thread.sleep(1000);

        assertSame(b, IdAllocationManager.ids.get("RESOURCE1"));
        assertSame(b, IdAllocationManager.ids.get("RESOURCE2"));
        j.assertLogContains("Assigned RESOURCE1", b);
        j.assertLogContains("Assigned RESOURCE2", b);

        blocker.event.signal();
        future.get(10, TimeUnit.SECONDS);

        j.assertBuildStatusSuccess(b);
    }

    @Test
    public void blockBuildUntilOwnerCompletes() throws Exception {
        BlockingBuilder blocker = new BlockingBuilder();

        FreeStyleProject owning = j.createFreeStyleProject();
        owning.getBuildWrappersList().add(defaultAlocatorForResources("resource"));
        owning.getBuildersList().add(new CriticalBlockStart());
        owning.getBuildersList().add(blocker);
        owning.getBuildersList().add(new CriticalBlockEnd());

        FreeStyleProject waiting = j.createFreeStyleProject();
        waiting.getBuildWrappersList().add(defaultAlocatorForResources("resource"));
        waiting.getBuildersList().add(new CriticalBlockStart());
        waiting.getBuildersList().add(new BlockingBuilder()); // Block forever
        waiting.getBuildersList().add(new CriticalBlockEnd());

        final QueueTaskFuture<FreeStyleBuild> owningFuture = owning.scheduleBuild2(0);
        FreeStyleBuild owningBuild = owningFuture.waitForStart();
        Thread.sleep(1000);
        assertSame(owningBuild, IdAllocationManager.ids.get("RESOURCE"));

        FreeStyleBuild waitingBuild = waiting.scheduleBuild2(0).waitForStart();
        Thread.sleep(1000);
        j.assertLogContains("Waiting ressource : RESOURCE currently use by : test0 #1", waitingBuild);

        blocker.event.signal();

        owningFuture.get();
        Thread.sleep(1000);

        assertSame(waitingBuild, IdAllocationManager.ids.get("RESOURCE"));
    }

    @Test
    public void interruptBuildWaitingToResource() throws Exception {
        BlockingBuilder blocker = new BlockingBuilder();

        FreeStyleProject waiting = j.createFreeStyleProject();
        waiting.getBuildWrappersList().add(defaultAlocatorForResources("resource"));
        waiting.getBuildersList().add(new CriticalBlockStart());
        waiting.getBuildersList().add(blocker);
        waiting.getBuildersList().add(new CriticalBlockEnd());

        QueueTaskFuture<FreeStyleBuild> feature = waiting.scheduleBuild2(0);
        FreeStyleBuild build = feature.waitForStart();
        Thread.sleep(1000);

        build.getExecutor().interrupt();

        feature.get();

        assertNull("Resource should be available", IdAllocationManager.ids.get("RESOURCE"));

        // Should be available for further builds
        feature = waiting.scheduleBuild2(0);
        feature.waitForStart();
        Thread.sleep(1000);
        blocker.event.signal();
        j.assertBuildStatusSuccess(feature);
    }

    @Test
    public void ommitEndStep() throws Exception {
        BlockingBuilder blocker = new BlockingBuilder();

        FreeStyleProject waiting = j.createFreeStyleProject();
        waiting.getBuildWrappersList().add(defaultAlocatorForResources("resource"));
        waiting.getBuildersList().add(new CriticalBlockStart());
        waiting.getBuildersList().add(blocker);

        final QueueTaskFuture<FreeStyleBuild> feature = waiting.scheduleBuild2(0);
        FreeStyleBuild build = feature.waitForStart();
        Thread.sleep(1000);

        assertSame(build, IdAllocationManager.ids.get("RESOURCE"));

        j.assertLogContains("Assigned RESOURCE", build);
        blocker.event.signal();
        feature.get();

        assertNull("Resource should be available", IdAllocationManager.ids.get("RESOURCE"));
    }

    private IdAllocator defaultAlocatorForResources(String... resources) {
        DefaultIdType[] out = new DefaultIdType[resources.length];
        for (int i = 0; i < resources.length; i++) {
            out[i] = new DefaultIdType(resources[i]);
        }
        return new IdAllocator(out);
    }

    private static final class BlockingBuilder extends TestBuilder {
        public final OneShotEvent event = new OneShotEvent();

        @Override
        public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
            try {
                event.block();
            } catch (InterruptedException ex) {
                ex.printStackTrace(listener.getLogger());
                // Allowed on teardown
            }
            return true;
        }
    }
}
