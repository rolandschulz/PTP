/*******************************************************************************
 * Copyright (c) 2005, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     QNX Software System
 *     Sergey Prigogin (Google) - https://bugs.eclipse.org/bugs/show_bug.cgi?id=13221
 *     Ed Swartz (Nokia)
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
/* -- ST-Origin --
 * Source folder: org.eclipse.cdt.ui/src
 * Class: org.eclipse.cdt.internal.ui.editor.OpenIncludeAction
 * Version: 1.37
 */
package org.eclipse.ptp.internal.rdt.ui.editor;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.IInclude;
import org.eclipse.cdt.core.parser.ExtendedScannerInfo;
import org.eclipse.cdt.core.parser.IExtendedScannerInfo;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.IScannerInfoProvider;
import org.eclipse.cdt.internal.core.resources.ResourceLookup;
import org.eclipse.cdt.internal.ui.CPluginImages;
import org.eclipse.cdt.internal.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.cdt.internal.ui.util.EditorUtility;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.utils.EFSExtensionManager;
import org.eclipse.cdt.utils.PathUtil;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.ptp.rdt.core.serviceproviders.IIndexServiceProvider;
import org.eclipse.ptp.rdt.core.services.IRDTServiceConstants;
import org.eclipse.ptp.rdt.ui.UIPlugin;
import org.eclipse.ptp.services.core.IService;
import org.eclipse.ptp.services.core.IServiceConfiguration;
import org.eclipse.ptp.services.core.IServiceModelManager;
import org.eclipse.ptp.services.core.IServiceProvider;
import org.eclipse.ptp.services.core.ServiceModelManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;

/**
 * Action to open an include file from the Outline view
 * @author vkong
 *
 */
public class OpenIncludeAction extends
		org.eclipse.cdt.internal.ui.editor.OpenIncludeAction {

	private static final String PREFIX= "OpenIncludeAction."; //$NON-NLS-1$
	
	private static final String DIALOG_TITLE= PREFIX + "dialog.title"; //$NON-NLS-1$
	private static final String DIALOG_MESSAGE= PREFIX + "dialog.message"; //$NON-NLS-1$
	
	private ISelectionProvider fSelectionProvider;
	
	public OpenIncludeAction(ISelectionProvider provider) {
		super(provider);
		setDescription(CUIPlugin.getResourceString(PREFIX + "description")); //$NON-NLS-1$
		setToolTipText(CUIPlugin.getResourceString(PREFIX + "tooltip")); //$NON-NLS-1$
		
		CPluginImages.setImageDescriptors(this, CPluginImages.T_LCL, CPluginImages.IMG_MENU_OPEN_INCLUDE);
		
		fSelectionProvider= provider;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.ui.editor.OpenIncludeAction#run()
	 */
	@Override
	public void run() {
		IInclude include= getIncludeStatement(fSelectionProvider.getSelection());
		if (include == null) {
			return;
		}
		try {
			ArrayList<URI> filesFound = new ArrayList<URI>(4);
			IResource res = include.getUnderlyingResource(); //the resource that contains this include
			String fullFileName= include.getFullFileName(); //the full path, if there is one
			IProject project = include.getCProject().getProject();
			
			if (fullFileName != null && fullFileName.length()>0) {
				// Bug 379298 - Open include file from Outline view throws errors for remote project
				if (!isLocalServiceConfiguration(project)) { //files are on remote server
					//get remote location information
					URI locationURI = include.getLocationURI(); //the location of the innermost file enclosing this include
					if (EFSExtensionManager.getDefault().isVirtual(locationURI)) {  //this is pointing to another underlying URI
						// get the underlying URI
						locationURI = EFSExtensionManager.getDefault().getLinkedURI(locationURI);
					}
					
					//attempt to get the path of the include file
					URI includeURI = replacePath(locationURI, fullFileName);

					IFileStore fileStore = EFS.getStore(includeURI);
					String scheme = fileStore.getFileSystem().getScheme();
					//since files are on remote server - the include file should not be found locally
					if (!scheme.equals(EFS.getLocalFileSystem().getScheme()) && !fileStore.fetchInfo().isDirectory() && fileStore.fetchInfo().exists()) {
						filesFound.add(includeURI);
					} else {
						URI projectURI = include.getCProject().getProject().getLocationURI();						
						//get the full project path on the server
						String projectPath = EFSExtensionManager.getDefault().getPathFromURI(projectURI);
						
						if (!fullFileName.startsWith(projectPath)) {
							//this include is not in the project - check if the EFS store 
							//represented by locationURI is linked to another URI and try again
							
							//make sure the locationURI refers to the server location
							URI linkedURI = EFSExtensionManager.getDefault().getLinkedURI(locationURI);
							includeURI = replacePath(linkedURI, fullFileName);
							fileStore = EFS.getStore(includeURI);
							if (!fileStore.fetchInfo().isDirectory() && fileStore.fetchInfo().exists()) { //find the file on server
								filesFound.add(includeURI);
							}
						} // since this include is in the project, the code below will try to find it within the project
					}
				}
				else {
					IPath fullPath= new Path(fullFileName);
					if (fullPath.isAbsolute() && fullPath.toFile().exists()) { //local
						filesFound.add(fullPath.toFile().toURI());
					}
				}
			}			

			if (filesFound.isEmpty() && res != null) {
				boolean isSystemInclude = include.isStandard();
				IProject proj = res.getProject();
				String includeName = include.getElementName();
				URI locationURI = include.getLocationURI(); //the location of the innermost file enclosing this include
				if (EFSExtensionManager.getDefault().isVirtual(locationURI)) {  //this is pointing to another underlying URI
					// get the underlying URI
					locationURI = EFSExtensionManager.getDefault().getLinkedURI(locationURI);
				}
				
				// Search in the scannerInfo information
				IScannerInfoProvider provider =  CCorePlugin.getDefault().getScannerInfoProvider(proj);
				if (provider != null) {
					IScannerInfo info = provider.getScannerInformation(res);
					// XXXX this should fall back to project by itself
					if (info == null) {
						info = provider.getScannerInformation(proj);
					}
					if (info != null) {
						IExtendedScannerInfo scanInfo = new ExtendedScannerInfo(info);
						
						if (!isSystemInclude) {
							IPath pathURI = new Path(locationURI.toString());
							// search in current directory
							if (pathURI != null) {
								String currentDir= pathURI.removeLastSegments(1).toOSString();
								findFile(new String[] { currentDir }, includeName, filesFound);
							}
							if (filesFound.isEmpty()) {
								// search in "..." include directories
								String[] localIncludePaths = scanInfo.getLocalIncludePath(); 
								String[] includePaths = new String[localIncludePaths.length];
								for (int i = 0; i < localIncludePaths.length; i++) {
									includePaths[i] = locationURI.getScheme() + "://" + locationURI.getHost() + new String(localIncludePaths[i]); //$NON-NLS-1$
								}
								findFile(includePaths, includeName, filesFound);
							}
						}
	
						if (filesFound.isEmpty()) {
							// search in <...> include directories
							String[] includePaths = scanInfo.getIncludePaths(); //these include paths do not have host information
							String[] newIncludePaths = new String[includePaths.length];
							for (int i = 0; i < includePaths.length; i++) {
								newIncludePaths[i] = locationURI.getScheme() + "://" + locationURI.getHost() + new String(includePaths[i]); //$NON-NLS-1$
							}
							findFile(newIncludePaths, includeName, filesFound);
						}
					}
					
					if (filesFound.isEmpty()) {
						// Fall back and search the project
						IResource resource = include.getCProject().getProject().findMember(include.getElementName());
						if (!isSystemInclude && resource != null && resource instanceof IFile)
							filesFound.add(resource.getLocationURI());
					}
				}
			}
			Object fileToOpen;
			int nElementsFound= filesFound.size();
			if (nElementsFound == 0) {
				noElementsFound();
				fileToOpen= null;
			} else if (nElementsFound == 1) {
				fileToOpen= filesFound.get(0);
			} else {
				fileToOpen= chooseFile(filesFound);
			}
			
			if (fileToOpen != null)
				EditorUtility.openInEditor((URI)fileToOpen, include.getCProject());					
			
		} catch (CModelException e) {
			UIPlugin.log(e.getStatus());
		} catch (CoreException e) {
			UIPlugin.log(e.getStatus());
		}
	}

	private void noElementsFound() {
		MessageBox errorMsg = new MessageBox(CUIPlugin.getActiveWorkbenchShell(), SWT.ICON_ERROR | SWT.OK);
		errorMsg.setText(CUIPlugin.getResourceString("OpenIncludeAction.error")); //$NON-NLS-1$
		errorMsg.setMessage (CUIPlugin.getResourceString("OpenIncludeAction.error.description")); //$NON-NLS-1$
		errorMsg.open();
	}
	
	private boolean isInProject(IPath path) {
		return getWorkspaceRoot().getFileForLocation(path) != null;		
	}
	
	/**
	 * Returns the path as is, if it points to a workspace resource. If the path
	 * does not point to a workspace resource, but there are linked workspace
	 * resources pointing to it, returns the paths of these resources.
	 * Otherwise, returns the path as is. 
	 */
	private IPath[] resolveIncludeLink(IPath path) {
		if (!isInProject(path)) {
			IFile[] files = ResourceLookup.findFilesForLocation(path);
			if (files.length > 0) {
				IPath[] paths = new IPath[files.length];
				for (int i = 0; i < files.length; i++) {
					paths[i] = files[i].getFullPath(); 
				}
				return paths;
			}
		}
		
		return new IPath[] { path };
	}

	private IWorkspaceRoot getWorkspaceRoot() {
		return ResourcesPlugin.getWorkspace().getRoot();
	}
	
	private void findFile(String[] includePaths, String name, ArrayList<URI> list)
			throws CoreException {
		// in case it is an absolute path and it's local
		IPath includeFile= new Path(name);		
		if (includeFile.isAbsolute()) {
			includeFile = PathUtil.getCanonicalPathWindows(includeFile);
			if (includeFile.toFile().exists()) {
				list.add(includeFile.toFile().toURI());
				return;
			}
		}
		HashSet<IPath> foundSet = new HashSet<IPath>();
		for (String includePath : includePaths) {
			IPath path = PathUtil.getCanonicalPathWindows(new Path(includePath).append(includeFile));
			//local case:
			File file = path.toFile();
			if (file.exists()) {
				IPath[] paths = resolveIncludeLink(path);
				for (IPath p : paths) {
					if (foundSet.add(p)) {
						list.add(p.toFile().toURI());
					}
				}
			} else { //remote case include paths:
				try {
					URI uri = new URI(path.toPortableString());
					IFileStore fileStore = EFS.getStore(uri);
					if (!fileStore.fetchInfo().isDirectory() && fileStore.fetchInfo().exists()) {
						list.add(uri);
					}
				} catch (URISyntaxException e) {
					UIPlugin.log(e);
				}
			}
		}
	}

	private Object chooseFile(ArrayList<URI> filesFound) {
		ILabelProvider renderer= new LabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof URI) {
					URI uri= (URI)element;
					String pathString = EFSExtensionManager.getDefault().getPathFromURI(uri);
					IPath path = new Path(pathString);
					return path.lastSegment() + " - "  + path.toString(); //$NON-NLS-1$
				}
				return super.getText(element);
			}
		};
		
		ElementListSelectionDialog dialog= new ElementListSelectionDialog(CUIPlugin.getActiveWorkbenchShell(), renderer, false, false);
		dialog.setTitle(CUIPlugin.getResourceString(DIALOG_TITLE));
		dialog.setMessage(CUIPlugin.getResourceString(DIALOG_MESSAGE));
		dialog.setElements(filesFound);
		
		if (dialog.open() == Window.OK) {
			return (IFile)((IPath) dialog.getSelectedElement()).toFile();
		}
		return null;
	}

	private static IInclude getIncludeStatement(ISelection sel) {
		if (!sel.isEmpty() && sel instanceof IStructuredSelection) {
			List<?> list= ((IStructuredSelection)sel).toList();
			if (list.size() == 1) {
				Object element= list.get(0);
				if (element instanceof IInclude) {
					return (IInclude)element;
				}
			}
		}
		return null;
	}
	
	/**
	 * Replaces the path portion of the given URI.
	 */
	private URI replacePath(URI u, String path) {
		try {
			//Bug 332798: handle remote tools connection
			return new URI(u.getScheme(), u.getAuthority(),
							path, //replaced!
							u.getQuery(),
							u.getFragment());
		} catch (URISyntaxException e) {
			UIPlugin.log(e);
			return null;
		}
	}
	
	private static boolean isLocalServiceConfiguration (IProject project) {
		IServiceModelManager smm = ServiceModelManager.getInstance();
		
		if(smm.isConfigured(project)) {
			IServiceConfiguration serviceConfig = smm.getActiveConfiguration(project);
			IService indexingService = smm.getService(IRDTServiceConstants.SERVICE_C_INDEX);
			IServiceProvider serviceProvider = serviceConfig.getServiceProvider(indexingService);
	
			if (serviceProvider instanceof IIndexServiceProvider) {
				return !((IIndexServiceProvider)serviceProvider).isRemote();
			}
		}
		return false;
	}

}
