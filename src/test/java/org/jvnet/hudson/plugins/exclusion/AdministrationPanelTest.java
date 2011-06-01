package org.jvnet.hudson.plugins.exclusion;

import com.gargoylesoftware.htmlunit.AjaxController;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.ScriptResult;
import com.gargoylesoftware.htmlunit.SgmlPage;
import com.gargoylesoftware.htmlunit.WebAssert;
import com.gargoylesoftware.htmlunit.WebRequestSettings;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.DomNodeList;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlButton;
import com.gargoylesoftware.htmlunit.html.HtmlCheckBoxInput;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlLink;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlTextArea;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;
import com.gargoylesoftware.htmlunit.javascript.host.Node;
import hudson.model.Descriptor;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.JobProperty;
import hudson.model.JobPropertyDescriptor;
import hudson.tasks.BuildWrapper;
import hudson.tasks.Publisher;
import hudson.util.DescribableList;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import org.junit.Before;
import org.junit.Test;
import org.jvnet.hudson.test.HudsonTestCase;
import org.w3c.dom.DocumentType;

/**
 * @author Anthony Roux
 */
public class AdministrationPanelTest extends HudsonTestCase {

    private AdministrationPanel adminPanel;
    private FreeStyleProject project;
  

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();

        project = createFreeStyleProject("firstJob");


        WebClient webClient = createWebClient();
        HtmlPage page = webClient.getPage(project, "configure");
        System.out.println(page.getDocumentURI());

        //Cocher la checkbox pour activer le plugin
        List<? extends Object> byXPath2 = page.getByXPath("//input[@id='cb12']");
        HtmlCheckBoxInput checkbox = (HtmlCheckBoxInput) byXPath2.get(0);
        checkbox.click();

        //Clicker sur le bouton Add pour ajouter une ressource
        List<? extends Object> bouttonAddXPath3 = page.getByXPath("//span[@id='yui-gen2']/span/button");
        HtmlButton button3 = (HtmlButton) bouttonAddXPath3.get(0);
        //MouseOver Obligatoire
        button3.mouseOver();
        Thread.sleep(3000);
        button3.click();

        Thread.sleep(3000);
     
         // Clicker sur "New resource" pour ajouter une textbox
        List<? extends Object> bouttonNewResource3 = page.getByXPath("//a[contains(text(),'New Resource')]");
        HtmlAnchor newResource = (HtmlAnchor) bouttonNewResource3.get(0);
        //on ajoute 3 ressources
        newResource.click();
        newResource.click();
        newResource.click();


        
         //Pour le textbox pour ressource 1
        List<? extends Object> textBoxResource1 = page.getByXPath("//tr[87]/td/div/div[1]/table/tbody/tr[1]/td[3]/input");
        HtmlTextInput nameResource1 = (HtmlTextInput) textBoxResource1.get(0);
        nameResource1.setValueAttribute("resource1");


        //Pour le textbox pour ressource 2
        List<? extends Object> textBoxResource2 = page.getByXPath("//td[@id='main-panel']/form/table/tbody/tr[87]/td/div/div[2]/table/tbody/tr[1]/td[3]/input");
        HtmlTextInput nameResource2 = (HtmlTextInput) textBoxResource2.get(0);
        nameResource2.setValueAttribute("resource2");

        //Pour le textbox pour ressource 3
        List<? extends Object> textBoxResource3 = page.getByXPath("//td[@id='main-panel']/form/table/tbody/tr[87]/td/div/div[3]/table/tbody/tr[1]/td[3]/input");
        HtmlTextInput nameResource3 = (HtmlTextInput) textBoxResource3.get(0);
        nameResource3.setValueAttribute("resource3");


        //Click sur le bouton d'ajout d'un step de build
        List<? extends Object> bouttonAddBuildStepXpath = page.getByXPath("//span[@id='yui-gen4']/span/button");
        HtmlButton bouttonAddBuildStep = (HtmlButton) bouttonAddBuildStepXpath.get(0);
        //MouseOver Obligatoire
        bouttonAddBuildStep.mouseOver();
        Thread.sleep(3000);
        bouttonAddBuildStep.click();
        Thread.sleep(3000);

        //Rajoute step StartBlock 
        List<? extends Object> bouttonNewStep1 = page.getByXPath("//a[contains(text(),'Critical block start')]");
        HtmlAnchor newStepSCB = (HtmlAnchor) bouttonNewStep1.get(0);
        newStepSCB.click();

        //Rajoute step commande shell qui sleep 60sec
        List<? extends Object> bouttonNewStep2 = page.getByXPath("//a[contains(text(),'Execute shell')] ");
        HtmlAnchor newStepShell = (HtmlAnchor) bouttonNewStep2.get(0);
        newStepShell.click();

        // Ecrire le la commande shell
        List<? extends Object> shellText = page.getByXPath("//textarea[@name='command']");
        HtmlTextArea commandShell = (HtmlTextArea) shellText.get(0);
        commandShell.setText("sleep 60");

        //Rajoute step EndBlock
        List<? extends Object> bouttonNewStep3 = page.getByXPath("//a[contains(text(),'Critical block end')]");
        HtmlAnchor newStepSCE = (HtmlAnchor) bouttonNewStep3.get(0);
        newStepSCE.click();


        //Submit la page (Save)
        List<? extends Object> byXPath = page.getByXPath("//button[contains(.,'Save')]");
        HtmlButton buttonSave = (HtmlButton) byXPath.get(0);
        submit(buttonSave.getEnclosingForm());
    }

    @Test
    public void testGetIconFileName() {
        adminPanel = new AdministrationPanel();
        assertEquals("/plugin/exclusion/icons/exclusion.png", adminPanel.getIconFileName());
    }

    @Test
    public void testGetDisplayName() {
        adminPanel = new AdministrationPanel();
        assertEquals("Exclusion administration", adminPanel.getDisplayName());
    }

    @Test
    public void testGetUrlName() {
        adminPanel = new AdministrationPanel();
        assertEquals("/administrationpanel", adminPanel.getUrlName());
    }

    /**
     * 
     * @throws Exception 
     */
    @Test
    public void testFormElementsPresent() throws Exception {


        HtmlPage page = new WebClient().goTo("/administrationpanel");

        HtmlElement elem = page.getElementById("jobName");

        DomNodeList<DomNode> childNodes = elem.getChildNodes();
        for (int i = 0; i < childNodes.size(); i++) {
            assertEquals("firstJob", childNodes.get(i).toString());
        }

        List<String> listResources = new ArrayList<String>();
        listResources.add("RESOURCE1");
        listResources.add("RESOURCE2");
        listResources.add("RESOURCE3");

        List<String> listResourcesFalse = new ArrayList<String>();
        listResourcesFalse.add("FAIL1");
        listResourcesFalse.add("FAIL2");
        listResourcesFalse.add("FAIL3");

        int count = 0;
        //Toutes les resources du tableau
        List<HtmlElement> elementsById = page.getElementsByIdAndOrName("resource");
        for (int j = 0; j < elementsById.size(); j++) {
            DomNodeList<DomNode> childNodesResource = elementsById.get(j).getChildNodes();
            for (int i = 0; i < childNodesResource.size(); i++) {
                count++;
                //On verifie que chaque ressource qu'on trouve dans le tableau est bien parmi la liste attendu
                assertTrue(listResources.contains(childNodesResource.get(i).toString()));
                // La liste ne contient pas les ressources
                assertFalse(listResourcesFalse.contains(childNodesResource.get(i).toString()));
            }
        }

        //On verifie qu'on a le meme nombre ressource entre attendu / obtenu
        assertEquals(listResources.size(), count);

        // On verifie qu'on a pas d'action
        List<HtmlElement> elementsByIdAction = page.getElementsByIdAndOrName("action");
        for (int j = 0; j < elementsByIdAction.size(); j++) {
            DomNodeList<DomNode> childNodesAction = elementsByIdAction.get(j).getChildNodes();
            for (int i = 0; i < childNodesAction.size(); i++) {
                assertNull(childNodesAction.get(i));
            }
        }
    }

    @Test
    public void testFormElementsPresentWhenStartJob() throws Exception {
        //Lance le projet
        Future<FreeStyleBuild> scheduleBuild2 = project.scheduleBuild2(0);
        Thread.sleep(3000);
        HtmlPage page = new WebClient().goTo("/administrationpanel");
        
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
        assertEquals(3, count);
        scheduleBuild2.cancel(true);
    }

    @Test
    public void testFormElementsPresentWhenStopJob() throws Exception {
        //Lance le projet
        Future<FreeStyleBuild> scheduleBuild2 = project.scheduleBuild2(0);
        Thread.sleep(3000);
        HtmlPage page = new WebClient().goTo("/administrationpanel");

        //List<? extends Object> byXPath = page.getByXPath("//table[@id='executors']/tbody[2]/tr[2]/td[3]/a/img");
        //  System.out.println(byXPath.get(0));
        scheduleBuild2.cancel(true);
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
        scheduleBuild2.cancel(true);
    }

    @Test
    public void testStopJob() throws Exception {
        //////////////////////////// Deuxieme Job /////////////////////////////////

        FreeStyleProject  project2 = createFreeStyleProject("fastJob");
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

        List<? extends Object> bouttonNewStep1 = page.getByXPath("//a[contains(text(),'Critical block start')]");
        HtmlAnchor newStepSCB = (HtmlAnchor) bouttonNewStep1.get(0);
        newStepSCB.click();

        List<? extends Object> bouttonNewStep3 = page.getByXPath("//a[contains(text(),'Critical block end')]");
        HtmlAnchor newStepSCE = (HtmlAnchor) bouttonNewStep3.get(0);
        newStepSCE.click();

        List<? extends Object> byXPath = page.getByXPath("//button[contains(.,'Save')]");
        HtmlButton buttonSave = (HtmlButton) byXPath.get(0);
        submit(buttonSave.getEnclosingForm());

        Thread.sleep(3000);
        //Lance le projet 1
        Future<FreeStyleBuild> scheduleBuild2 = project.scheduleBuild2(0);
        //Lancer le projet 2
        Future<FreeStyleBuild> scheduleBuild21 = project2.scheduleBuild2(0);

        Thread.sleep(8000);
        page = new WebClient().goTo("/administrationpanel");

        
        //On verifie qu'il n'y a le bon nombre de currently use (3 car que le projet 1)
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
        assertEquals(3, count);
        scheduleBuild21.cancel(true);
        scheduleBuild2.cancel(true);
    }

    @Test
    /**
     * Test de verification du boutton pour liberer une ressource
     */
    public void testReleaseResource() throws Exception {
        //Lance le projet
        Future<FreeStyleBuild> scheduleBuild2 = project.scheduleBuild2(0);
        Thread.sleep(7000);
        HtmlPage page = new WebClient().goTo("/administrationpanel");

        // click sur le boutton release
        List<? extends Object> byXPath = page.getByXPath("//span[@id='yui-gen0']/span/button");
        HtmlButton bouttonRelease = (HtmlButton) byXPath.get(0);
       // bouttonRelease.mouseOver();
       // Thread.sleep(3000);
        //bouttonRelease.click();
        submit(bouttonRelease.getEnclosingForm());

        Thread.sleep(5000);

        page = new WebClient().goTo("/administrationpanel");
         //On verifie qu'il n'y a le bon nombre de currently use (3 car que le projet 1)
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
        scheduleBuild2.cancel(true);
    }
}