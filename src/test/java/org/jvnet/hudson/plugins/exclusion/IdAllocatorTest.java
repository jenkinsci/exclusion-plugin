package org.jvnet.hudson.plugins.exclusion;

import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.DomNodeList;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlButton;
import com.gargoylesoftware.htmlunit.html.HtmlCheckBoxInput;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlTextArea;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import java.util.List;
import java.util.concurrent.Future;
import org.junit.Before;
import org.jvnet.hudson.test.HudsonTestCase;

/**
 * @author Anthony Roux
 */
public class IdAllocatorTest extends HudsonTestCase {

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
    }

    public void testJobWithoutEndStep() throws Exception {
        FreeStyleProject project2 = createFreeStyleProject("JobWithoutEndStep");
        WebClient webClient = createWebClient();
        HtmlPage page = webClient.getPage(project2, "configure");
        System.out.println(page.getDocumentURI());

        List<? extends Object> byXPath2 = page.getByXPath("//input[@id='cb12']");
        HtmlCheckBoxInput checkbox = (HtmlCheckBoxInput) byXPath2.get(0);
        checkbox.click();

        List<? extends Object> bouttonAddXPath3 = page.getByXPath("//span[@id='yui-gen2']/span/button");
        HtmlButton button3 = (HtmlButton) bouttonAddXPath3.get(0);
        button3.mouseOver();
        Thread.sleep(3000);
        button3.click();
        Thread.sleep(3000);

        List<? extends Object> bouttonNewResource3 = page.getByXPath("//a[contains(text(),'New Resource')]");
        HtmlAnchor newResource = (HtmlAnchor) bouttonNewResource3.get(0);
        newResource.click();
        newResource.click();

        List<? extends Object> textBoxResource1 = page.getByXPath("//tr[87]/td/div/div[1]/table/tbody/tr[1]/td[3]/input");
        HtmlTextInput nameResource1 = (HtmlTextInput) textBoxResource1.get(0);
        nameResource1.setValueAttribute("resource1");

        List<? extends Object> textBoxResource2 = page.getByXPath("//td[@id='main-panel']/form/table/tbody/tr[87]/td/div/div[2]/table/tbody/tr[1]/td[3]/input");
        HtmlTextInput nameResource2 = (HtmlTextInput) textBoxResource2.get(0);
        nameResource2.setValueAttribute("resourcePlus");

        List<? extends Object> bouttonAddBuildStepXpath = page.getByXPath("//span[@id='yui-gen4']/span/button");
        HtmlButton bouttonAddBuildStep = (HtmlButton) bouttonAddBuildStepXpath.get(0);
        //MouseOver Obligatoire
        bouttonAddBuildStep.mouseOver();
        Thread.sleep(3000);
        bouttonAddBuildStep.click();
        Thread.sleep(3000);

        List<? extends Object> bouttonNewStep1 = page.getByXPath("//a[contains(text(),'Critical block start')]");
        HtmlAnchor newStepSCB = (HtmlAnchor) bouttonNewStep1.get(0);
        newStepSCB.click();

        List<? extends Object> bouttonNewStep2 = page.getByXPath("//a[contains(text(),'Execute shell')] ");
        HtmlAnchor newStepShell = (HtmlAnchor) bouttonNewStep2.get(0);
        newStepShell.click();

        List<? extends Object> shellText = page.getByXPath("//textarea[@name='command']");
        HtmlTextArea commandShell = (HtmlTextArea) shellText.get(0);
        commandShell.setText("sleep 20");

        List<? extends Object> byXPath = page.getByXPath("//button[contains(.,'Save')]");
        HtmlButton buttonSave = (HtmlButton) byXPath.get(0);
        submit(buttonSave.getEnclosingForm());

        Future<FreeStyleBuild> scheduleBuild2 = project2.scheduleBuild2(0);
        Thread.sleep(3000);
        page = new WebClient().goTo("/administrationpanel");

        int count = 0;
        List<HtmlElement> elementsByIdAction = page.getElementsByIdAndOrName("action");
        for (int j = 0; j < elementsByIdAction.size(); j++) {
            DomNodeList<DomNode> childNodesAction = elementsByIdAction.get(j).getChildNodes();
            for (int i = 0; i < childNodesAction.size(); i++) {
                if (childNodesAction.get(i).toString().equals("Currently Use")) {
                    count++;
                }
            }
        }

        assertEquals(2, count);
        FreeStyleBuild get = scheduleBuild2.get();
        Result result = get.getResult();
        assertEquals(result, Result.SUCCESS);

        page = new WebClient().goTo("/administrationpanel");

        count = 0;
        elementsByIdAction = page.getElementsByIdAndOrName("action");
        for (int j = 0; j < elementsByIdAction.size(); j++) {
            DomNodeList<DomNode> childNodesAction = elementsByIdAction.get(j).getChildNodes();
            for (int i = 0; i < childNodesAction.size(); i++) {
                if (childNodesAction.get(i).toString().equals("Currently Use")) {
                    count++;
                }
            }
        }
        assertEquals(0, count);
    }

    public void testJobWithoutStartStep() throws Exception {
        FreeStyleProject project2 = createFreeStyleProject("JobWithoutStartStep");
        WebClient webClient = createWebClient();
        HtmlPage page = webClient.getPage(project2, "configure");
        System.out.println(page.getDocumentURI());

        List<? extends Object> byXPath2 = page.getByXPath("//input[@id='cb12']");
        HtmlCheckBoxInput checkbox = (HtmlCheckBoxInput) byXPath2.get(0);
        checkbox.click();

        List<? extends Object> bouttonAddXPath3 = page.getByXPath("//span[@id='yui-gen2']/span/button");
        HtmlButton button3 = (HtmlButton) bouttonAddXPath3.get(0);
        button3.mouseOver();
        Thread.sleep(3000);
        button3.click();
        Thread.sleep(3000);

        List<? extends Object> bouttonNewResource3 = page.getByXPath("//a[contains(text(),'New Resource')]");
        HtmlAnchor newResource = (HtmlAnchor) bouttonNewResource3.get(0);
        newResource.click();
        newResource.click();

        List<? extends Object> textBoxResource1 = page.getByXPath("//tr[87]/td/div/div[1]/table/tbody/tr[1]/td[3]/input");
        HtmlTextInput nameResource1 = (HtmlTextInput) textBoxResource1.get(0);
        nameResource1.setValueAttribute("resource1");

        List<? extends Object> textBoxResource2 = page.getByXPath("//td[@id='main-panel']/form/table/tbody/tr[87]/td/div/div[2]/table/tbody/tr[1]/td[3]/input");
        HtmlTextInput nameResource2 = (HtmlTextInput) textBoxResource2.get(0);
        nameResource2.setValueAttribute("resourcePlus");

        List<? extends Object> bouttonAddBuildStepXpath = page.getByXPath("//span[@id='yui-gen4']/span/button");
        HtmlButton bouttonAddBuildStep = (HtmlButton) bouttonAddBuildStepXpath.get(0);
        //MouseOver Obligatoire
        bouttonAddBuildStep.mouseOver();
        Thread.sleep(3000);
        bouttonAddBuildStep.click();
        Thread.sleep(3000);



        List<? extends Object> bouttonNewStep2 = page.getByXPath("//a[contains(text(),'Execute shell')] ");
        HtmlAnchor newStepShell = (HtmlAnchor) bouttonNewStep2.get(0);
        newStepShell.click();

        List<? extends Object> shellText = page.getByXPath("//textarea[@name='command']");
        HtmlTextArea commandShell = (HtmlTextArea) shellText.get(0);
        commandShell.setText("sleep 20");

        List<? extends Object> bouttonNewStep1 = page.getByXPath("//a[contains(text(),'Critical block end')]");
        HtmlAnchor newStepSCE = (HtmlAnchor) bouttonNewStep1.get(0);
        newStepSCE.click();

        List<? extends Object> byXPath = page.getByXPath("//button[contains(.,'Save')]");
        HtmlButton buttonSave = (HtmlButton) byXPath.get(0);
        submit(buttonSave.getEnclosingForm());

        Future<FreeStyleBuild> scheduleBuild2 = project2.scheduleBuild2(0);
        Thread.sleep(3000);
        page = new WebClient().goTo("/administrationpanel");

        int count = 0;
        List<HtmlElement> elementsByIdAction = page.getElementsByIdAndOrName("action");
        for (int j = 0; j < elementsByIdAction.size(); j++) {
            DomNodeList<DomNode> childNodesAction = elementsByIdAction.get(j).getChildNodes();
            for (int i = 0; i < childNodesAction.size(); i++) {
                if (childNodesAction.get(i).toString().equals("Currently Use")) {
                    count++;
                }
            }
        }

        assertEquals(0, count);
        FreeStyleBuild get = scheduleBuild2.get();
        Result result = get.getResult();
        assertEquals(result, Result.SUCCESS);
    }

    public void testJobWithoutPluginActivateWithStartAndEndStep() throws Exception {

        FreeStyleProject project2 = createFreeStyleProject("JobWithoutPluginActivateWithStartAndEndStep");
        WebClient webClient = createWebClient();
        HtmlPage page = webClient.getPage(project2, "configure");

        List<? extends Object> bouttonAddBuildStepXpath = page.getByXPath("//span[@id='yui-gen4']/span/button");
        HtmlButton bouttonAddBuildStep = (HtmlButton) bouttonAddBuildStepXpath.get(0);
        //MouseOver Obligatoire
        bouttonAddBuildStep.mouseOver();
        Thread.sleep(3000);
        bouttonAddBuildStep.click();
        Thread.sleep(3000);

        List<? extends Object> bouttonNewStep1 = page.getByXPath("//a[contains(text(),'Critical block start')]");
        HtmlAnchor newStepSCB = (HtmlAnchor) bouttonNewStep1.get(0);
        newStepSCB.click();

        List<? extends Object> bouttonNewStep2 = page.getByXPath("//a[contains(text(),'Execute shell')] ");
        HtmlAnchor newStepShell = (HtmlAnchor) bouttonNewStep2.get(0);
        newStepShell.click();

        List<? extends Object> shellText = page.getByXPath("//textarea[@name='command']");
        HtmlTextArea commandShell = (HtmlTextArea) shellText.get(0);
        commandShell.setText("sleep 20");

        List<? extends Object> bouttonNewStep3 = page.getByXPath("//a[contains(text(),'Critical block end')]");
        HtmlAnchor newStepSCE = (HtmlAnchor) bouttonNewStep3.get(0);
        newStepSCE.click();

        List<? extends Object> byXPath = page.getByXPath("//button[contains(.,'Save')]");
        HtmlButton buttonSave = (HtmlButton) byXPath.get(0);
        submit(buttonSave.getEnclosingForm());

        Future<FreeStyleBuild> scheduleBuild2 = project2.scheduleBuild2(0);
        Thread.sleep(3000);
        page = new WebClient().goTo("/administrationpanel");

        int count = 0;
        List<HtmlElement> elementsByIdAction = page.getElementsByIdAndOrName("action");
        for (int j = 0; j < elementsByIdAction.size(); j++) {
            DomNodeList<DomNode> childNodesAction = elementsByIdAction.get(j).getChildNodes();
            for (int i = 0; i < childNodesAction.size(); i++) {
                if (childNodesAction.get(i).toString().equals("Currently Use")) {
                    count++;
                }
            }
        }

        assertEquals(0, count);
        FreeStyleBuild get = scheduleBuild2.get();
        Result result = get.getResult();
        assertEquals(result, Result.SUCCESS);
    }

    public void testJobWithoutResourceWithStartAndEndStep() throws Exception {
        FreeStyleProject project2 = createFreeStyleProject("fastJob");
        WebClient webClient = createWebClient();
        HtmlPage page = webClient.getPage(project2, "configure");

        List<? extends Object> byXPath2 = page.getByXPath("//input[@id='cb12']");
        HtmlCheckBoxInput checkbox = (HtmlCheckBoxInput) byXPath2.get(0);
        checkbox.click();

        List<? extends Object> bouttonAddXPath3 = page.getByXPath("//span[@id='yui-gen2']/span/button");
        HtmlButton button3 = (HtmlButton) bouttonAddXPath3.get(0);
        button3.mouseOver();
        Thread.sleep(3000);
        button3.click();
        Thread.sleep(3000);

        List<? extends Object> bouttonAddBuildStepXpath = page.getByXPath("//span[@id='yui-gen4']/span/button");
        HtmlButton bouttonAddBuildStep = (HtmlButton) bouttonAddBuildStepXpath.get(0);
        //MouseOver Obligatoire
        bouttonAddBuildStep.mouseOver();
        Thread.sleep(3000);
        bouttonAddBuildStep.click();
        Thread.sleep(3000);

        List<? extends Object> bouttonNewStep1 = page.getByXPath("//a[contains(text(),'Critical block start')]");
        HtmlAnchor newStepSCB = (HtmlAnchor) bouttonNewStep1.get(0);
        newStepSCB.click();

        List<? extends Object> bouttonNewStep2 = page.getByXPath("//a[contains(text(),'Execute shell')] ");
        HtmlAnchor newStepShell = (HtmlAnchor) bouttonNewStep2.get(0);
        newStepShell.click();

        List<? extends Object> shellText = page.getByXPath("//textarea[@name='command']");
        HtmlTextArea commandShell = (HtmlTextArea) shellText.get(0);
        commandShell.setText("sleep 20");

        List<? extends Object> bouttonNewStep3 = page.getByXPath("//a[contains(text(),'Critical block end')]");
        HtmlAnchor newStepSCE = (HtmlAnchor) bouttonNewStep3.get(0);
        newStepSCE.click();

        List<? extends Object> byXPath = page.getByXPath("//button[contains(.,'Save')]");
        HtmlButton buttonSave = (HtmlButton) byXPath.get(0);
        submit(buttonSave.getEnclosingForm());

        Future<FreeStyleBuild> scheduleBuild2 = project2.scheduleBuild2(0);
        Thread.sleep(3000);
        page = new WebClient().goTo("/administrationpanel");

        int count = 0;
        List<HtmlElement> elementsByIdAction = page.getElementsByIdAndOrName("action");
        for (int j = 0; j < elementsByIdAction.size(); j++) {
            DomNodeList<DomNode> childNodesAction = elementsByIdAction.get(j).getChildNodes();
            for (int i = 0; i < childNodesAction.size(); i++) {
                if (childNodesAction.get(i).toString().equals("Currently Use")) {
                    count++;
                }
            }
        }

        assertEquals(0, count);
        FreeStyleBuild get = scheduleBuild2.get();
        Result result = get.getResult();
        assertEquals(result, Result.SUCCESS);
    }

    public void testJobWithResourceWithoutStartAndEndStep() throws Exception {
        FreeStyleProject project2 = createFreeStyleProject("fastJob");
        WebClient webClient = createWebClient();
        HtmlPage page = webClient.getPage(project2, "configure");

        List<? extends Object> byXPath2 = page.getByXPath("//input[@id='cb12']");
        HtmlCheckBoxInput checkbox = (HtmlCheckBoxInput) byXPath2.get(0);
        checkbox.click();

        List<? extends Object> bouttonAddXPath3 = page.getByXPath("//span[@id='yui-gen2']/span/button");
        HtmlButton button3 = (HtmlButton) bouttonAddXPath3.get(0);
        button3.mouseOver();
        Thread.sleep(3000);
        button3.click();
        Thread.sleep(3000);

        List<? extends Object> bouttonNewResource3 = page.getByXPath("//a[contains(text(),'New Resource')]");
        HtmlAnchor newResource = (HtmlAnchor) bouttonNewResource3.get(0);
        newResource.click();
        newResource.click();

        List<? extends Object> textBoxResource1 = page.getByXPath("//tr[87]/td/div/div[1]/table/tbody/tr[1]/td[3]/input");
        HtmlTextInput nameResource1 = (HtmlTextInput) textBoxResource1.get(0);
        nameResource1.setValueAttribute("resource1");

        List<? extends Object> textBoxResource2 = page.getByXPath("//td[@id='main-panel']/form/table/tbody/tr[87]/td/div/div[2]/table/tbody/tr[1]/td[3]/input");
        HtmlTextInput nameResource2 = (HtmlTextInput) textBoxResource2.get(0);
        nameResource2.setValueAttribute("resourcePlus");

        List<? extends Object> bouttonAddBuildStepXpath = page.getByXPath("//span[@id='yui-gen4']/span/button");
        HtmlButton bouttonAddBuildStep = (HtmlButton) bouttonAddBuildStepXpath.get(0);
        //MouseOver Obligatoire
        bouttonAddBuildStep.mouseOver();
        Thread.sleep(3000);
        bouttonAddBuildStep.click();
        Thread.sleep(3000);

        List<? extends Object> bouttonNewStep2 = page.getByXPath("//a[contains(text(),'Execute shell')] ");
        HtmlAnchor newStepShell = (HtmlAnchor) bouttonNewStep2.get(0);
        newStepShell.click();

        List<? extends Object> shellText = page.getByXPath("//textarea[@name='command']");
        HtmlTextArea commandShell = (HtmlTextArea) shellText.get(0);
        commandShell.setText("sleep 20");

        List<? extends Object> byXPath = page.getByXPath("//button[contains(.,'Save')]");
        HtmlButton buttonSave = (HtmlButton) byXPath.get(0);
        submit(buttonSave.getEnclosingForm());

        Future<FreeStyleBuild> scheduleBuild2 = project2.scheduleBuild2(0);
        Thread.sleep(3000);
        page = new WebClient().goTo("/administrationpanel");

        int count = 0;
        List<HtmlElement> elementsByIdAction = page.getElementsByIdAndOrName("action");
        for (int j = 0; j < elementsByIdAction.size(); j++) {
            DomNodeList<DomNode> childNodesAction = elementsByIdAction.get(j).getChildNodes();
            for (int i = 0; i < childNodesAction.size(); i++) {
                if (childNodesAction.get(i).toString().equals("Currently Use")) {
                    count++;
                }
            }
        }

        assertEquals(0, count);
        FreeStyleBuild get = scheduleBuild2.get();
        Result result = get.getResult();
        assertEquals(result, Result.SUCCESS);
    }
}
