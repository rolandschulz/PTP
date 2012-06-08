package org.eclipse.ptp.rdt.sync.ui.tests;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;

public class SyncUITasks {
	public enum WizardType {
		C,CPP,FORTRAN
	}

	public static SWTBot openNewProjectWizard(WizardType wizardType) {
		SWTWorkbenchBot bot = new SWTWorkbenchBot();
		bot.menu("File").menu("New").menu("Other...").click();
		SWTBotTree wizardTree = bot.activeShell().bot().tree();
		wizardTree.expandNode("Remote").getNode("Synchronized C/C++ Project").doubleClick();
		switch (wizardType) {
		case C:
		case CPP:
			wizardTree.expandNode("Remote").getNode("Synchronized C/C++ Project").doubleClick();
			break;
		case FORTRAN:
			wizardTree.expandNode("Remote").getNode("Synchronized Fortran Project").doubleClick();
			break;
		}
		return bot.activeShell().bot();
	}
	
	public static SWTBot openPropertiesPage(String projectName) {
		SWTWorkbenchBot bot = new SWTWorkbenchBot();
		SWTBotTree projectTree = bot.viewByTitle("Project Explorer").bot().tree();
		projectTree.select(projectName).contextMenu("Properties").click();
		SWTBotTree propertyTree = bot.activeShell().bot().tree();
		propertyTree.expandNode("C/C++ Build").getNode("Synchronize").select();
		return bot.activeShell().bot();
	}
}