package org.eclipse.ptp.rdt.sync.ui.tests;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(SWTBotJunit4ClassRunner.class)
public class HelloWorldTest {
	private static SWTWorkbenchBot bot;
	private String projectName = "SWTAutoProject5";
	private String connectionName = "pmemd";
	private String remoteDirectory = "/home/ejd/SWTAutoProject5";
	private String projectType = "Makefile project";
	private String projectSubType = "Hello World C++ Makefile Project";
	private String[] remoteToolchains = {"Linux GCC"};
	private String[] localToolchains = {"Linux GCC"};

	@Test
	public void makeHelloWorldSyncProject() throws InterruptedException {
		SWTBot wizardBot = openNewProjectWizard();
		wizardBot.text(0).setText(projectName);
		wizardBot.comboBox(0).setSelection(connectionName);
		wizardBot.text(2).setText(remoteDirectory);
		wizardBot.tree().expandNode(projectType).getNode(projectSubType).select();
		for (String s : remoteToolchains) {
			wizardBot.table(0).select(s);
		}
		for (String s : localToolchains) {
			wizardBot.table(1).select(s);
		}
		wizardBot.button("Finish").click();
		Thread.sleep(60000);
	}

	@BeforeClass
	public static void setup() throws InterruptedException {
		bot = new SWTWorkbenchBot();
	}
	
	@AfterClass
	public static void quit() throws InterruptedException {
		bot.captureScreenshot("screenshot.png");
	}
	
	private SWTBot openNewProjectWizard() {
		bot.menu("File").menu("New").menu("Other...").click();
		SWTBotTree wizardTree = bot.activeShell().bot().tree();
		wizardTree.expandNode("Remote").getNode("Synchronized C/C++ Project").doubleClick();
		return bot.activeShell().bot();
	}
}