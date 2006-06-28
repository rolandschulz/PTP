/*******************************************************************************
 * Copyright (c) 2005 The Regents of the University of California. 
 * This material was produced under U.S. Government contract W-7405-ENG-36 
 * for Los Alamos National Laboratory, which is operated by the University 
 * of California for the U.S. Department of Energy. The U.S. Government has 
 * rights to use, reproduce, and distribute this software. NEITHER THE 
 * GOVERNMENT NOR THE UNIVERSITY MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR 
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified 
 * to produce derivative works, such modified software should be clearly marked, 
 * so as not to confuse it with the version available from LANL.
 * 
 * Additionally, this program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * LA-CC 04-115
 *******************************************************************************/
package org.eclipse.ptp.simulation.ui.preferences;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourceAttributes;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ptp.ui.PTPUIPlugin;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.IDocumentProviderExtension;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * @author Clement chu
 * 
 */
public abstract class SimulationProjectCreation implements IRunnableContext {
	private final String TEMPLATE_FOLDER = "template";
	private String projectName = "";
	private String fileName = "";
	
	/** Constructor
	 * @param projectName
	 * @param fileName
	 */
	public SimulationProjectCreation(String projectName, String fileName) {
		this.projectName = projectName;
		this.fileName = fileName;
	}
	
	/** Get project name
	 * @return
	 */
	public String getProjectName() {
		return projectName;
	}
	/** Get file name
	 * @return
	 */
	public String getFileName() {
		return fileName;
	}
	/** Set project name
	 * @param projectName
	 */
	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}
	/** Set file name
	 * @param fileName
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	/** Get full file name including extension
	 * @return
	 */
	public String getFullFileName() {
		return getFileName() + getFileExtension();
	}
	/** Get file extension
	 * @return
	 */
	public abstract String getFileExtension();
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.operation.IRunnableContext#run(boolean, boolean, org.eclipse.jface.operation.IRunnableWithProgress)
	 */
	public void run(boolean fork, boolean cancelable, IRunnableWithProgress runnable) throws InvocationTargetException, InterruptedException {
		new ProgressMonitorDialog(PTPUIPlugin.getActiveWorkbenchShell()).run(fork, cancelable, runnable);
	}	
		
	/** Create simulator project
	 * @throws CoreException
	 */
	public void createSimulatorProject() throws CoreException {
		IRunnableWithProgress runnable = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				if (monitor == null)
					monitor = new NullProgressMonitor();
				monitor.beginTask("Creating simulator project", 10);
				try {
					createSimulatorProject(projectName, fileName, monitor);
				} catch (CoreException e) {
					throw new InvocationTargetException(e);
				} finally {
					monitor.subTask("Done..");
					monitor.done();
				}
			}
		};
		try {
			PlatformUI.getWorkbench().getProgressService().runInUI(this, runnable, null);
		} catch (InterruptedException e) {
			throw new CoreException(Status.CANCEL_STATUS);
		} catch (InvocationTargetException e1) {
			Throwable innerException = e1.getTargetException();
			if (innerException instanceof CoreException)
				throw (CoreException)innerException;

			throw new CoreException(new Status(IStatus.ERROR, PTPUIPlugin.getUniqueIdentifier(), 0, "Internal error", innerException));
		}
	}
	/** Is simulator project existed
	 * @return
	 */
	public boolean isSimulatorProjectExisted() {
		IResource project = ResourcesPlugin.getWorkspace().getRoot().findMember(getProjectName());
		if (project == null || !project.exists() || project.getType() != IResource.PROJECT) {
			return false;
		}
		IResource file = project.getProject().findMember(getFullFileName());
		if (file == null || !file.exists() || file.getType() != IResource.FILE) {
			return false;
		}
		return true;
	}
	/** Create simulator project
	 * @param projectName
	 * @param fileName
	 * @param monitor
	 * @throws CoreException
	 */
	private void createSimulatorProject(String projectName, String fileName, IProgressMonitor monitor) throws CoreException {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IProject project = workspace.getRoot().getProject(getProjectName());
		if (!project.exists()) {
			//create project
			IProjectDescription description = workspace.newProjectDescription(project.getName());
			description.setLocation(project.getLocation());
			createProject(description, project, new SubProgressMonitor(monitor, 5));
		}
		IFile file = workspace.getRoot().getFile(project.getFullPath().append(getFullFileName()));
		if (!file.exists() || !file.isReadOnly()) {
			createFile(file, getFileContent(), new SubProgressMonitor(monitor, 3));
			setReadyOnly(file);
		}
		openEditor(file);
	}
	
	/** Get file content
	 * @return
	 */
	protected InputStream getFileContent() {
		String templateFile = getTemplateFile();
		if (templateFile == null)
			return null;
		try {
			File file = getTemplateFilePath(templateFile).toFile();
			if (file.exists())
				return file.toURL().openStream();
		} catch (IOException e) {
			return null;
		}
		return null;
	}
	/** Get template file path
	 * @param templateFile
	 * @return
	 */
	private IPath getTemplateFilePath(String templateFile) {
		String pluginPath =  PTPUIPlugin.getDefault().getPluginPath();
		if (pluginPath == null)
			return null;
		
		return new Path(pluginPath).append(TEMPLATE_FOLDER).append(templateFile);
	}
	/** Get template file
	 * @return
	 */
	protected abstract String getTemplateFile();
	/** Create project
	 * @param description
	 * @param newProject
	 * @param monitor
	 * @throws CoreException
	 */
	protected abstract void createProject(IProjectDescription description, IProject newProject, IProgressMonitor monitor) throws CoreException;
	
	/** Create file
	 * @param fileHandle
	 * @param contents
	 * @param monitor
	 * @throws CoreException
	 */
	protected void createFile(IFile fileHandle, InputStream contents, IProgressMonitor monitor) throws CoreException {
		if (contents == null)
			contents = new ByteArrayInputStream(new byte[0]);
		try {
			fileHandle.create(contents, false, monitor);
		} catch (CoreException e) {
			// If the file already existed locally, just refresh to get contents
			if (e.getStatus().getCode() == IResourceStatus.PATH_OCCUPIED) {
				//if file is not read only, then overwrite the content
				if (!fileHandle.isReadOnly())
					fileHandle.setContents(contents, false, false, monitor);
				
				fileHandle.refreshLocal(IResource.DEPTH_ZERO, null);
			}
			else
				throw e;
		}
		
		if (monitor.isCanceled())
			throw new OperationCanceledException();
	}
	
	/** Set file to read only
	 * @param resource
	 * @throws CoreException
	 */
	protected void setReadyOnly(IResource resource) throws CoreException {
		ResourceAttributes attributes = resource.getResourceAttributes();
		if (attributes != null) {
			attributes.setReadOnly(true);
			resource.setResourceAttributes(attributes);
		}
	}
	
	/** Get editor ID
	 * @return
	 */
	protected abstract String getEditorID();
	
	/** Open editor
	 * @param file
	 * @throws CoreException
	 */
	protected void openEditor(IFile file) throws CoreException {
		IEditorPart editorPart = getEditorPart(file);
		if (editorPart == null)
			throw new CoreException(Status.CANCEL_STATUS);
		ITextEditor textEditor = getTextEditor(editorPart);
		if (textEditor == null)
			throw new CoreException(Status.CANCEL_STATUS);
		
		IDocumentProvider docProvider = textEditor.getDocumentProvider();
		if (docProvider instanceof IDocumentProviderExtension) {
			IDocumentProviderExtension extension = (IDocumentProviderExtension)docProvider;
			extension.validateState(editorPart.getEditorInput(), null);
		}
	}
	/** Get editor part
	 * @param file
	 * @return
	 */
	protected IEditorPart getEditorPart(final IFile file) {
		IWorkbenchPage page = PTPUIPlugin.getActiveWorkbenchWindow().getActivePage();
		IEditorPart editorPart = page.getActiveEditor();
		if (editorPart != null) {
			IEditorInput editorInput = editorPart.getEditorInput();
			if (editorInput instanceof IFileEditorInput) {
				if (((IFileEditorInput) editorInput).getFile().equals(file)) {
					page.bringToTop(editorPart);
					return editorPart;
				}
			}
		}
		IEditorReference[] refs = page.getEditorReferences();
		for (int i = 0; i < refs.length; i++) {
			IEditorPart refEditor = refs[i].getEditor(false);
			IEditorInput editorInput = refEditor.getEditorInput();
			if (editorInput instanceof IFileEditorInput) {
				if (((IFileEditorInput) editorInput).getFile().equals(file)) {
					page.bringToTop(refEditor);
					return refEditor;
				}
			}
		}
		try {
			return page.openEditor(new FileEditorInput(file), getEditorID(), false);
		} catch (PartInitException e) {
			PTPUIPlugin.log(e);
		}
		return null;
	}
	/** Get text editor
	 * @param editorPart
	 * @return
	 */
	protected ITextEditor getTextEditor(IEditorPart editorPart) {
		if (editorPart instanceof ITextEditor)
			return (ITextEditor) editorPart;
		return (ITextEditor) editorPart.getAdapter(ITextEditor.class);
	}
	/** Get file
	 * @param editorInput
	 * @return
	 */
	protected IFile getFile(IEditorInput editorInput) {
		if (editorInput instanceof IFileEditorInput)
			return ((IFileEditorInput) editorInput).getFile();
		return null;
	}
}
