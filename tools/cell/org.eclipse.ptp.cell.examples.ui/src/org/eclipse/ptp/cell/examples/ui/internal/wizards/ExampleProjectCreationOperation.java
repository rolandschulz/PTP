/******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *****************************************************************************/
package org.eclipse.ptp.cell.examples.ui.internal.wizards;

import java.util.Iterator;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;

import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ptp.cell.examples.ui.internal.ExampleMessages;
import org.eclipse.ptp.cell.examples.ui.internal.ExampleUIPlugin;
import org.eclipse.ptp.cell.examples.ui.internal.ProjectWizardDefinition;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.IOverwriteQuery;
import org.eclipse.ui.wizards.datatransfer.ImportOperation;
import org.eclipse.ui.wizards.datatransfer.ZipFileStructureProvider;
import org.osgi.framework.Bundle;


/**
 * @author laggarcia
 * @since 1.1.1
 */
public class ExampleProjectCreationOperation implements IRunnableWithProgress {

	private class ImportOverwriteQuery implements IOverwriteQuery {

		private int openDialog(final String file) {
			final int[] result = { IDialogConstants.CANCEL_ID };
			wizardShell.getDisplay().syncExec(new Runnable() {
				public void run() {
					String title = ExampleMessages.overwriteQueryTitle;
					String msg = ExampleMessages.bind(
							ExampleMessages.overwriteQueryMessage, file);
					String[] options = { IDialogConstants.YES_LABEL,
							IDialogConstants.NO_LABEL,
							IDialogConstants.YES_TO_ALL_LABEL,
							IDialogConstants.CANCEL_LABEL };
					MessageDialog dialog = new MessageDialog(wizardShell,
							title, null, msg, MessageDialog.QUESTION, options,
							0);
					result[0] = dialog.open();
				}
			});
			return result[0];
		}

		public String queryOverwrite(String file) {
			String[] returnCodes = { YES, NO, ALL, CANCEL };
			int returnVal = openDialog(file);
			return returnVal < 0 ? CANCEL : returnCodes[returnVal];
		}
	}

	private Shell wizardShell;

	private IWizardPage[] wizardPages;

	private ProjectWizardDefinition projectWizardDefinition;

	private IResource fileToOpen;

	/**
	 * 
	 */
	public ExampleProjectCreationOperation(Wizard wizard) {
		this.wizardShell = wizard.getShell();
		this.wizardPages = wizard.getPages();
	}

	public void run(IProgressMonitor monitor) throws InvocationTargetException,
			InterruptedException {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		try {
			monitor.beginTask(ExampleMessages.creatingExampleProjects,
					wizardPages.length);
			for (int i = 0; i < wizardPages.length; i++) {
				createProject(
						(ExampleProjectCreationWizardPage) wizardPages[i],
						new SubProgressMonitor(monitor, 1));
			}
		} finally {
			monitor.done();
		}

	}

	private void createProject(ExampleProjectCreationWizardPage wizardPage,
			IProgressMonitor monitor) throws InvocationTargetException,
			InterruptedException {
		projectWizardDefinition = wizardPage.getProjectWizardDefinition();

		monitor.beginTask(ExampleMessages.configuringProject, 1);

		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IProject project = getNewProjectOpened(root, wizardPage
				.getProjectName(), monitor);
		doImport(project.getFullPath(), projectWizardDefinition.getSourceZip(),
				projectWizardDefinition.getZipInternalRelativePath(),
				new SubProgressMonitor(monitor, 1));

		String fileToOpenPath = projectWizardDefinition.getFileToOpen();
		if (fileToOpenPath != null && fileToOpenPath.length() > 0) {
			fileToOpen = project.findMember(new Path(fileToOpenPath));
		}

	}

	private void doImport(IPath destiny, String sourceZip,
			String zipRelativeDir, IProgressMonitor monitor)
			throws InvocationTargetException, InterruptedException {

		try {
			// get the zip file from plugin
			Bundle bundle = Platform.getBundle(projectWizardDefinition
					.getContributingBundle());
			URL url = new URL(bundle.getEntry("/"), sourceZip); //$NON-NLS-1$
			ZipFile zipFile = new ZipFile(FileLocator.toFileURL(url).getFile());

			// find the entry point if it exists
			ZipFileStructureProvider structureProvider = new ZipFileStructureProvider(
					zipFile);
			IPath zipRelativePath = new Path(zipRelativeDir);
			int zipInternalPathSegments = zipRelativePath.segmentCount();
			ZipEntry entry = structureProvider.getRoot();
			if (zipInternalPathSegments != 0) {
				// The zipRelativePath is NOT the root of the zip file
				for (int i = 1; i <= zipInternalPathSegments; i++) {
					Iterator children = structureProvider.getChildren(entry)
							.iterator();
					while (children.hasNext()) {
						entry = (ZipEntry) children.next();
						String segments = zipRelativePath.uptoSegment(i)
								.toString();
						if (entry.getName().equals(segments)) {
							break;
						}
					}
				}
			}
			// if the doesn't exist, an exception will be thrown somewhere in
			// the processing of the zip file
			if (!entry.getName().equals(zipRelativeDir)) {
				Status status = new Status(
						IStatus.ERROR,
						ExampleUIPlugin.ID,
						IStatus.ERROR,
						zipRelativeDir
								+ " doesn't exist in the example source zip file.", //$NON-NLS-1$
						new Exception());
				throw new InvocationTargetException(new CoreException(status));
			}

			// import files from zip
			ImportOperation op = new ImportOperation(destiny, entry,
					structureProvider, new ImportOverwriteQuery());
			op.setCreateContainerStructure(false);
			op.run(monitor);
		} catch (IOException e) {
			Status status = new Status(IStatus.ERROR, ExampleUIPlugin.ID,
					IStatus.ERROR, sourceZip + "/" + zipRelativeDir + ": " //$NON-NLS-1$ //$NON-NLS-2$
							+ e.getMessage(), e);
			throw new InvocationTargetException(new CoreException(status));
		}

	}

	/**
	 * Check if the project exists in the Workspace. If it doesn't exist, create
	 * it. After that, check if the project is opened. If it is not, open it.
	 * Return the newly opened project.
	 */
	private IProject getNewProjectOpened(IWorkspaceRoot root,
			String projectName, IProgressMonitor monitor)
			throws InvocationTargetException {
		try {
			IProject project = root.getProject(projectName);
			if (!project.exists()) {
				project.create(null);
			}
			if (!project.isOpen()) {
				project.open(null);
			}
			return project;
		} catch (CoreException e) {
			throw new InvocationTargetException(e);
		}
	}

	public IResource getFileToOpen() {
		return fileToOpen;
	}

}
