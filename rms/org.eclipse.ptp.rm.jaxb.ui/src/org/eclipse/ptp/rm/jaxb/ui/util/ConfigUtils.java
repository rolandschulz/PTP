package org.eclipse.ptp.rm.jaxb.ui.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.window.Window;
import org.eclipse.ptp.rm.jaxb.core.IJAXBNonNLSConstants;
import org.eclipse.ptp.rm.jaxb.ui.messages.Messages;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.model.WorkbenchLabelProvider;

public class ConfigUtils implements IJAXBNonNLSConstants {

	private ConfigUtils() {
	}

	/**
	 * Open a dialog that allows the user to choose a project.
	 * 
	 * @return selected project
	 */
	public static File chooseLocalProject(Shell shell) {
		IProject[] projects = getLocalProjects();
		WorkbenchLabelProvider labelProvider = new WorkbenchLabelProvider();
		ElementListSelectionDialog dialog = new ElementListSelectionDialog(shell, labelProvider);
		dialog.setTitle(Messages.JAXBRMConfigurationSelectionWizardPage_Project_Selection_Title);
		dialog.setMessage(Messages.JAXBRMConfigurationSelectionWizardPage_Project_Selection_Message);
		dialog.setElements(projects);
		if (dialog.open() == Window.OK) {
			IProject project = (IProject) dialog.getFirstResult();
			return new File(project.getLocationURI());
		}
		return null;
	}

	public static File getUserHome() {
		return new File(System.getProperty(JAVA_USER_HOME));
	}

	public static IWorkspaceRoot getWorkspaceRoot() {
		return ResourcesPlugin.getWorkspace().getRoot();
	}

	private static IProject[] getLocalProjects() {
		IProject[] all = getWorkspaceRoot().getProjects();
		List<IProject> local = new ArrayList<IProject>();
		for (IProject p : all) {
			if (FILE_SCHEME.equals(p.getLocationURI().getScheme())) {
				local.add(p);
			}
		}
		return local.toArray(new IProject[0]);
	}

}
