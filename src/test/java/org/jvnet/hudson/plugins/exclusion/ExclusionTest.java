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
import static org.junit.Assert.assertTrue;
import hudson.Launcher;
import hudson.matrix.AxisList;
import hudson.matrix.MatrixBuild;
import hudson.matrix.MatrixProject;
import hudson.matrix.TextAxis;
import hudson.model.BuildListener;
import hudson.model.FreeStyleBuild;
import hudson.model.AbstractBuild;
import hudson.model.FreeStyleProject;
import hudson.model.queue.QueueTaskFuture;
import hudson.util.OneShotEvent;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.JenkinsRule.WebClient;
import org.jvnet.hudson.test.TestBuilder;

import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class ExclusionTest {

    @Rule public JenkinsRule j = new JenkinsRule();

    @Test
    public void acquireTheResource() throws Exception {
        BlockingBuilder blocker = new BlockingBuilder();

        FreeStyleProject p = j.createFreeStyleProject("job");
        p.getBuildWrappersList().add(defaultAlocatorForResources("job", "resource1", "resource2"));
        p.getBuildersList().add(new CriticalBlockStart());
        p.getBuildersList().add(blocker);
        p.getBuildersList().add(new CriticalBlockEnd());

        final QueueTaskFuture<FreeStyleBuild> future = p.scheduleBuild2(0);
        FreeStyleBuild b = future.waitForStart();
        Thread.sleep(1000);

        assertSame(b, IdAllocationManager.getOwnerBuild("RESOURCE1"));
        assertSame(b, IdAllocationManager.getOwnerBuild("RESOURCE2"));
        j.assertLogContains("Assigned RESOURCE1", b);
        j.assertLogContains("Assigned RESOURCE2", b);

        blocker.event.signal();
        future.get(10, TimeUnit.SECONDS);

        j.assertBuildStatusSuccess(b);
    }

    @Test
    public void blockBuildUntilOwnerCompletes() throws Exception {
        BlockingBuilder blocker = new BlockingBuilder();

        FreeStyleProject owning = j.createFreeStyleProject("a");
        owning.getBuildWrappersList().add(defaultAlocatorForResources("a", "RESOURCE"));
        owning.getBuildersList().add(new CriticalBlockStart());
        owning.getBuildersList().add(blocker);
        owning.getBuildersList().add(new CriticalBlockEnd());
        owning.setAssignedLabel(null);

        FreeStyleProject waiting = j.createFreeStyleProject("b");
        waiting.getBuildWrappersList().add(defaultAlocatorForResources("b", "RESOURCE"));
        waiting.getBuildersList().add(new CriticalBlockStart());
        waiting.getBuildersList().add(new BlockingBuilder()); // Block forever
        waiting.getBuildersList().add(new CriticalBlockEnd());
        waiting.setAssignedLabel(null);

        final QueueTaskFuture<FreeStyleBuild> owningFuture = owning.scheduleBuild2(0);
        FreeStyleBuild owningBuild = owningFuture.waitForStart();
        Thread.sleep(1000);
        assertSame(owningBuild, IdAllocationManager.getOwnerBuild("RESOURCE"));

        FreeStyleBuild waitingBuild = waiting.scheduleBuild2(0).waitForStart();
        Thread.sleep(1000);
        j.assertLogContains("Waiting ressource : RESOURCE currently use by : a #1", waitingBuild);

        WebClient wc = j.createWebClient();
        HtmlPage page = wc.goTo("administrationpanel");
        assertTrue(page.asText().contains("b\tRESOURCE"));

        blocker.event.signal();

        owningFuture.get();
        Thread.sleep(1000);

        assertSame(waitingBuild, IdAllocationManager.getOwnerBuild("RESOURCE"));
    }

    @Test
    public void interruptBuildWaitingToResource() throws Exception {
        BlockingBuilder blocker = new BlockingBuilder();

        FreeStyleProject waiting = j.createFreeStyleProject("job");
        waiting.getBuildWrappersList().add(defaultAlocatorForResources("job", "resource"));
        waiting.getBuildersList().add(new CriticalBlockStart());
        waiting.getBuildersList().add(blocker);
        waiting.getBuildersList().add(new CriticalBlockEnd());

        QueueTaskFuture<FreeStyleBuild> feature = waiting.scheduleBuild2(0);
        FreeStyleBuild build = feature.waitForStart();
        Thread.sleep(1000);

        build.getExecutor().interrupt();

        feature.get();

        assertNull("Resource should be available", IdAllocationManager.getOwnerBuild("RESOURCE"));

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

        FreeStyleProject waiting = j.createFreeStyleProject("job");
        waiting.getBuildWrappersList().add(defaultAlocatorForResources("jobs", "resource"));
        waiting.getBuildersList().add(new CriticalBlockStart());
        waiting.getBuildersList().add(blocker);

        final QueueTaskFuture<FreeStyleBuild> feature = waiting.scheduleBuild2(0);
        FreeStyleBuild build = feature.waitForStart();
        Thread.sleep(1000);

        assertSame(build, IdAllocationManager.getOwnerBuild("RESOURCE"));

        j.assertLogContains("Assigned RESOURCE", build);
        blocker.event.signal();
        feature.get();

        assertNull("Resource should be available", IdAllocationManager.getOwnerBuild("RESOURCE"));
    }

    @Test
    public void matrixProject() throws Exception {
        j.jenkins.setNumExecutors(5); // We have enough executors for all configurations

        MatrixProject p = j.createMatrixProject();
        p.setAxes(new AxisList(new TextAxis(
                "axis", "a", "b", "c", "d", "e"
        )));
        p.getBuildWrappersList().add(defaultAlocatorForResources("job", "resource"));
        p.getBuildersList().add(new CriticalBlockStart());
        p.getBuildersList().add(new TestBuilder() {
            @Override
            public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
                assertSame(build, IdAllocationManager.getOwnerBuild("RESOURCE"));
                return true;
            }
        });

        MatrixBuild build = j.buildAndAssertSuccess(p);
        for (char i = 'a'; i <= 'e'; i++) {
            j.assertBuildStatusSuccess(p.getItem("axis=" + i).getLastBuild());
        }

        assertNull(IdAllocationManager.getOwnerBuild("RESOURCE"));
    }

    private IdAllocator defaultAlocatorForResources(String jobName, String... resources) {
        DefaultIdType[] out = new DefaultIdType[resources.length];
        for (int i = 0; i < resources.length; i++) {
            out[i] = new DefaultIdType(resources[i]);
        }
        // TODO how can this even work?
        IdAllocator.DESCRIPTOR.setName(jobName);
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
