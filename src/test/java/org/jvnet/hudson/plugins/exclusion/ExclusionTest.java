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
public class ExclusionTest extends HudsonTestCase {

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
        commandShell.setText("sleep 30");

        //Rajoute step EndBlock
        List<? extends Object> bouttonNewStep3 = page.getByXPath("//a[contains(text(),'Critical block end')]");
        HtmlAnchor newStepSCE = (HtmlAnchor) bouttonNewStep3.get(0);
        newStepSCE.click();


        //Submit la page (Save)
        List<? extends Object> byXPath = page.getByXPath("//button[contains(.,'Save')]");
        HtmlButton buttonSave = (HtmlButton) byXPath.get(0);
        submit(buttonSave.getEnclosingForm());
    }

    public void testTwoJobsAtSameTime() throws Exception {
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

        List<? extends Object> textBoxResource1 = page.getByXPath("//tr[87]/td/div/div[1]/table/tbody/tr[1]/td[3]/input");
        HtmlTextInput nameResource1 = (HtmlTextInput) textBoxResource1.get(0);
        nameResource1.setValueAttribute("resource1");

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

        //Rajoute step commande shell qui sleep 60sec
        List<? extends Object> bouttonNewStep2 = page.getByXPath("//a[contains(text(),'Execute shell')] ");
        HtmlAnchor newStepShell = (HtmlAnchor) bouttonNewStep2.get(0);
        newStepShell.click();

        // Ecrire le la commande shell
        List<? extends Object> shellText = page.getByXPath("//textarea[@name='command']");
        HtmlTextArea commandShell = (HtmlTextArea) shellText.get(0);
        commandShell.setText("sleep 15");

        List<? extends Object> bouttonNewStep3 = page.getByXPath("//a[contains(text(),'Critical block end')]");
        HtmlAnchor newStepSCE = (HtmlAnchor) bouttonNewStep3.get(0);
        newStepSCE.click();

        List<? extends Object> byXPath = page.getByXPath("//button[contains(.,'Save')]");
        HtmlButton buttonSave = (HtmlButton) byXPath.get(0);
        submit(buttonSave.getEnclosingForm());


        Thread.sleep(3000);
        //Lance le projet 1
        Future<FreeStyleBuild> scheduleBuild2 = project.scheduleBuild2(0);
        Thread.sleep(3000);
        //Lancer le projet 2
        Future<FreeStyleBuild> scheduleBuild21 = project2.scheduleBuild2(0);

        page = new WebClient().goTo("/administrationpanel");

        int count = 0;
        List<HtmlElement> elementsByIdAction = page.getElementsByIdAndOrName("action");
        for (int j = 0; j < elementsByIdAction.size(); j++) {
            DomNodeList<DomNode> childNodesAction = elementsByIdAction.get(j).getChildNodes();
            for (int i = 0; i < childNodesAction.size(); i++) {
                if (childNodesAction.get(i).toString().equals("Currently Used")) {
                    count++;
                }
            }
        }
        //Les 3 ressources du premier projet + ressourceplus qui n'est pas en commun entre les 2 projets
        assertEquals(3, count);
        FreeStyleBuild get = scheduleBuild2.get();
        Result result = get.getResult();
        assertEquals(result, Result.SUCCESS);


        page = new WebClient().goTo("/administrationpanel");

        //Quand il fini ça lance le 2eme on verifie
        count = 0;
        elementsByIdAction = page.getElementsByIdAndOrName("action");
        for (int j = 0; j < elementsByIdAction.size(); j++) {
            DomNodeList<DomNode> childNodesAction = elementsByIdAction.get(j).getChildNodes();
            for (int i = 0; i < childNodesAction.size(); i++) {
                if (childNodesAction.get(i).toString().equals("Currently Used")) {
                    count++;
                }
            }
        }
        //Les deux du second projet
        assertEquals(1, count);

        FreeStyleBuild get2 = scheduleBuild21.get();
        Result result2 = get2.getResult();
        assertEquals(result2, Result.SUCCESS);

        page = new WebClient().goTo("/administrationpanel");
        //Quand tout est fini on verifie que les ressources sont libérées
        count = 0;
        elementsByIdAction = page.getElementsByIdAndOrName("action");
        for (int j = 0; j < elementsByIdAction.size(); j++) {
            DomNodeList<DomNode> childNodesAction = elementsByIdAction.get(j).getChildNodes();
            for (int i = 0; i < childNodesAction.size(); i++) {
                if (childNodesAction.get(i).toString().equals("Currently Used")) {
                    count++;
                }
            }
        }
        assertEquals(0, count);
    }
}
