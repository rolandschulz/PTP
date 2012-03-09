/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.rdt.core.tests;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.Properties;

import org.eclipse.cdt.core.CCProjectNature;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.internal.core.pdom.indexer.IndexerPreferences;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.ptp.internal.rdt.core.index.RemoteFastIndexer;
import org.eclipse.ptp.rdt.core.resources.RemoteNature;
import org.osgi.framework.Bundle;

@SuppressWarnings("restriction")
public class RemoteTestProject {

	private final IProject project;

	public RemoteTestProject(String projectName, URI location) throws CoreException {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IProject projectHandle = workspace.getRoot().getProject(projectName);

		IProjectDescription projectDescription = workspace.newProjectDescription(projectHandle.getName());
		projectDescription.setLocationURI(location);

		project = CCorePlugin.getDefault().createCDTProject(projectDescription, projectHandle, null);

		if (!project.isOpen()) {
			project.open(null);
		}

		CProjectNature.addCNature(project, new NullProgressMonitor());
		CCProjectNature.addCCNature(project, new NullProgressMonitor());
		RemoteNature.addRemoteNature(project, new NullProgressMonitor());

		IndexerPreferences.set(project, IndexerPreferences.KEY_INDEX_ALL_FILES, "true");
		IndexerPreferences.set(project, IndexerPreferences.KEY_INDEXER_ID, RemoteFastIndexer.ID);

		Properties properties = new Properties();
		//		properties.put(IndexerPreferences.KEY_FILES_TO_PARSE_UP_FRONT, ""); //$NON-NLS-1$
		IndexerPreferences.setProperties(project, IndexerPreferences.SCOPE_PROJECT_PRIVATE, properties);

		CCorePlugin.getDefault().mapCProjectOwner(project, "project.id.whatever", false);
	}

	public IProject getProject() {
		return project;
	}

	public ICProject getCProject() {
		return CCorePlugin.getDefault().getCoreModel().create(project);
	}

	public void createFile(String path, String contents) throws CoreException {
		IFile file = project.getFile(new Path(path));
		InputStream inStream = new ByteArrayInputStream(contents.getBytes());
		file.create(inStream, true, null);
	}

	public void delete() throws CoreException {
		project.delete(true, true, null);
	}

	@Override
	public String toString() {
		return project.toString() + "@" + project.getLocationURI();
	}

	public String getName() {
		return project.getName();
	}

	// private final static IOverwriteQuery OVERWRITE_QUERY= new IOverwriteQuery() {
	// public String queryOverwrite(String file) {
	// return ALL;
	// }
	// };

	/**
	 * Copies all the files located in the source folder to the root of the remote project.
	 * 
	 * @throws InterruptedException
	 * @throws InvocationTargetException
	 */
	public void importProjectFixture(Bundle bundle, String sourceFolder) throws Exception {
		// IPath destination = project.getFullPath();
		// File source = new File(sourceFolder);
		//
		// ImportOperation importOp = new ImportOperation(destination, source,
		// FileSystemStructureProvider.INSTANCE, OVERWRITE_QUERY);
		// importOp.setCreateContainerStructure(false);
		// importOp.run(new NullProgressMonitor());
		CProjectHelper.importSourcesFromPlugin(getCProject(), bundle, sourceFolder);
	}

}
