/*******************************************************************************
 * Copyright (c) 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.debug.ui.testplugin;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.zip.ZipFile;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.cdt.core.ICDescriptor;
import org.eclipse.cdt.core.ICDescriptorOperation;
import org.eclipse.cdt.core.IBinaryParser.IBinaryObject;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.IArchive;
import org.eclipse.cdt.core.model.IArchiveContainer;
import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.cdt.core.model.IBinaryContainer;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.dialogs.IOverwriteQuery;
import org.eclipse.ui.wizards.datatransfer.ImportOperation;
import org.eclipse.ui.wizards.datatransfer.ZipFileStructureProvider;

/**
 * @author Clement chu
 */
public class PTPProjectHelper {
	/**
	 * Get File from working directory
	 * @param path
	 * @return
	 */
	public static File getFileInPlugin(IPath path) {
		try {
			URL installURL = new URL(PTPDebugTestPlugin.getDefault().getBundle().getEntry("/"), path.toOSString());
			URL localURL = FileLocator.toFileURL(installURL);
			return new File(localURL.getFile());
		} catch (IOException e) {
			return null;
		}
	}
	/**
	 * Create CProject with imported zip file
	 * @param projectName
	 * @param zipFile
	 * @return
	 * @throws CoreException
	 * @throws InvocationTargetException
	 * @throws IOException
	 */
	public static ICProject createCProjectWithImport(String projectName, IPath zipFile) throws CoreException, InvocationTargetException, IOException {
		IWorkspaceRoot root= ResourcesPlugin.getWorkspace().getRoot();
		IProject project= root.getProject(projectName);
		if (!project.exists()) {
			project.create(null);
		} else {
			project.refreshLocal(IResource.DEPTH_INFINITE, null);
		}
		
		if (!project.isOpen()) {
			project.open(null);
		}
		File zip = getFileInPlugin(zipFile);
		importFilesFromZip(new ZipFile(zip), project.getFullPath(),null);
		
		if (!project.hasNature(CProjectNature.C_NATURE_ID)) {
			addNatureToProject(project, CProjectNature.C_NATURE_ID, null);
		}
		ICProject cproject = CCorePlugin.getDefault().getCoreModel().create(project);
		//Try to guess at the correct binary parser.. elf or pe at this point..
		ICDescriptorOperation op = new ICDescriptorOperation() {
			public void execute(ICDescriptor descriptor, IProgressMonitor monitor) throws CoreException {
				descriptor.remove(CCorePlugin.BINARY_PARSER_UNIQ_ID);
				String os = System.getProperty("os.name");
				boolean isMac=(os.toLowerCase().indexOf("mac")!=-1);
				boolean isPe=(os.toLowerCase().indexOf("windows")!=-1);
				descriptor.create(CCorePlugin.BINARY_PARSER_UNIQ_ID, isMac?"org.eclipse.cdt.core.MachO":(isPe?"org.eclipse.cdt.core.PE":"org.eclipse.cdt.core.ELF"));
			}
		};
		CCorePlugin.getDefault().getCDescriptorManager().runDescriptorOperation(project, op, null);
		return cproject;	
	}
	/**
	 * Create CProject with given project name
	 * @param projectName
	 * @return
	 * @throws CoreException
	 */
	public static ICProject createCProject(String projectName) throws CoreException {
		IWorkspaceRoot root= ResourcesPlugin.getWorkspace().getRoot();
		IProject project= root.getProject(projectName);
		if (!project.exists()) {
			project.create(null);
		} else {
			project.refreshLocal(IResource.DEPTH_INFINITE, null);
		}
		if (!project.isOpen()) {
			project.open(null);
		}
		if (!project.hasNature(CProjectNature.C_NATURE_ID)) {
			addNatureToProject(project, CProjectNature.C_NATURE_ID, null);
		}
		ICProject cproject = CCorePlugin.getDefault().getCoreModel().create(project);
		return cproject;
	}
	/**
	 * Delete given CProject
	 * @param cproject
	 * @throws CoreException
	 */
	public static void delete(ICProject cproject) throws CoreException {
		cproject.getProject().delete(true, true, null);
	}
	/**
	 * Attempts to find an archive with the given name in the workspace
	 * @param testProject
	 * @param name
	 * @return
	 * @throws CModelException
	 */
	public static IArchive findArchive(ICProject testProject,String name) throws CModelException {
		int x;
		IArchive[] myArchives;
		IArchiveContainer archCont;
		archCont=testProject.getArchiveContainer();
		myArchives=archCont.getArchives();
		if (myArchives.length<1) 
			return(null);
		for (x=0;x<myArchives.length;x++) {
			if (myArchives[x].getElementName().equals(name))
					return(myArchives[x]);
		}
		return(null);
	}
	/**
	 * Find binary object file
	 * @param testProject
	 * @param name
	 * @return IBinaryObject
	 * @throws CoreException
	 */
	public static IBinaryObject findBinaryObject(ICProject testProject, String name) throws CoreException {
		IBinary binary = findBinary(testProject, name);
		if (binary == null) {
			return null;        
		}
		return (IBinaryObject)binary.getAdapter(IBinaryObject.class);
	}
	/**
	 * Attempts to find a binary with the given name in the workspace
	 * @param testProject
	 * @param name
	 * @return
	 * @throws CModelException
	 */
	public static IBinary findBinary(ICProject testProject, String name) throws CModelException {
		IBinaryContainer binCont;
		int x;
		IBinary[] myBinaries;
		binCont=testProject.getBinaryContainer();
		myBinaries=binCont.getBinaries();
		if (myBinaries.length<1) 
			return(null);
		for (x=0;x<myBinaries.length;x++) {
			if (myBinaries[x].getElementName().equals(name))
					return(myBinaries[x]);
				
		}
		return(null);
	}
	/**
	 * Attempts to find an object with the given name in the workspace
	 * @param testProject
	 * @param name
	 * @return
	 * @throws CModelException
	 */
	public static IBinary findObject(ICProject testProject,String name) throws CModelException {
		int x;
		ICElement[] myElements;
		myElements=testProject.getChildren();
		if (myElements.length<1) 
			return(null);
		for (x=0;x<myElements.length;x++) {
			if (myElements[x].getElementName().equals(name))
				if (myElements[x] instanceof IBinary) {
					 return((IBinary) myElements[x]);
				}
		}
		return(null);
	}
	/**
	 * Attempts to find a TranslationUnit with the given name in the workspace
	 * @param testProject
	 * @param name
	 * @return
	 * @throws CModelException
	 */
	public static ITranslationUnit findTranslationUnit(ICProject testProject,String name) throws CModelException {
		int x;
		ICElement[] myElements;
		myElements=testProject.getChildren();
		if (myElements.length<1) 
			return(null);
		for (x=0;x<myElements.length;x++) {
			if (myElements[x].getElementName().equals(name))
				if (myElements[x] instanceof ITranslationUnit) {
					return((ITranslationUnit) myElements[x]);
				}
		}
		return(null);
	}	
	/**
	 * Attempts to find an element with the given name in the workspace
	 */
	public static ICElement findElement(ICProject testProject,String name) throws CModelException {
		int x;
		ICElement[] myElements;
		myElements=testProject.getChildren();
		if (myElements.length<1) 
			return(null);
		for (x=0;x<myElements.length;x++) {
			if (myElements[x].getElementName().equals(name))
				return myElements[x];
		}
		return(null);
	}
	/**
	 * Add project nature
	 * @param proj
	 * @param natureId
	 * @param monitor
	 * @throws CoreException
	 */
	private static void addNatureToProject(IProject proj, String natureId, IProgressMonitor monitor) throws CoreException {
		IProjectDescription description = proj.getDescription();
		String[] prevNatures= description.getNatureIds();
		String[] newNatures= new String[prevNatures.length + 1];
		System.arraycopy(prevNatures, 0, newNatures, 0, prevNatures.length);
		newNatures[prevNatures.length]= natureId;
		description.setNatureIds(newNatures);
		proj.setDescription(description, monitor);
	}
	/**
	 * Import zip file
	 * @param srcZipFile
	 * @param destPath
	 * @param monitor
	 * @throws InvocationTargetException
	 */
	private static void importFilesFromZip(ZipFile srcZipFile, IPath destPath, IProgressMonitor monitor) throws InvocationTargetException {		
		ZipFileStructureProvider structureProvider=	new ZipFileStructureProvider(srcZipFile);
		try {
			ImportOperation op= new ImportOperation(destPath, structureProvider.getRoot(), structureProvider, new ImportOverwriteQuery());
			op.run(monitor);
		} catch (InterruptedException e) {
			// should not happen
		}
	}
	/**
	 * inner class
	 * @author clement
	 *
	 */
	private static class ImportOverwriteQuery implements IOverwriteQuery {
		public String queryOverwrite(String file) {
			return ALL;
		}	
	}
}