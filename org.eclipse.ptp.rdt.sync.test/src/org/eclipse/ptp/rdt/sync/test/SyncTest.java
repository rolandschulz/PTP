package org.eclipse.ptp.rdt.sync.test;

import static org.junit.Assert.assertTrue;

import org.eclipse.cdt.core.CCProjectNature;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ptp.rdt.sync.core.BuildConfigurationManager;
import org.eclipse.ptp.rdt.sync.core.resources.RemoteSyncNature;
import org.junit.Test;

public class SyncTest {
	private final String projectName = "AnAutoTest"; //$NON-NLS-1$
	private final String remoteHost = "ejd@roland"; //$NON-NLS-1$
	private final String remoteDir = "/home/ejd/doTest"; //$NON-NLS-1$
	private IProject project;

	@Test
	public void test() throws OperationCanceledException, CoreException {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IProject projectHandle = workspace.getRoot().getProject(projectName);
		IProjectDescription projectDescription = workspace.newProjectDescription(projectName);

		projectDescription.setLocationURI(null);
		project = CCorePlugin.getDefault().createCDTProject(projectDescription, projectHandle, null);
		if (!project.isOpen()) {
			project.open(null);
		}

		CProjectNature.addCNature(project, new NullProgressMonitor());
		CCProjectNature.addCCNature(project, new NullProgressMonitor());
		RemoteSyncNature.addNature(project, new NullProgressMonitor());
		BuildConfigurationManager.getInstance().createLocalConfiguration(project);
		assertTrue(false);
	}
}
