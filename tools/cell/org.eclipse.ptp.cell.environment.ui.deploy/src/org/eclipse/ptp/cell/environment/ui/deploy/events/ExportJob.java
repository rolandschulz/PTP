package org.eclipse.ptp.cell.environment.ui.deploy.events;

import java.io.File;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ptp.cell.environment.ui.deploy.debug.Debug;
import org.eclipse.ptp.remotetools.core.IRemoteCopyTools;
import org.eclipse.ptp.remotetools.core.IRemoteFileTools;
import org.eclipse.ptp.remotetools.exception.CancelException;
import org.eclipse.ptp.remotetools.exception.RemoteConnectionException;
import org.eclipse.ptp.remotetools.exception.RemoteExecutionException;
import org.eclipse.ptp.remotetools.exception.RemoteOperationException;
import org.eclipse.ui.dialogs.IOverwriteQuery;


class ExportJob extends AbstractCellTargetJob {
	private final IProgressMonitor monitor;
	ExportJobWrapper wrapper;

	public ExportJob(ExportJobWrapper wrapper, IProgressMonitor monitor) {
		this.monitor = monitor;
		this.wrapper = wrapper;
	}

	public void run() {
		Debug.POLICY.enter(Debug.DEBUG_JOBS);
		try{					
			IRemoteFileTools fileTools = executionManager.getRemoteFileTools();
			IRemoteCopyTools copyTools = executionManager.getRemoteCopyTools();
			boolean hasDir = false;

			if(monitor.isCanceled())
				throw new InterruptedException(Messages.ExportJobWrapper_2);
			
			monitor.setTaskName(Messages.ExportJobWrapper_3);
			
			//check if directory exists
			monitor.subTask(Messages.ExportJobWrapper_4);
			hasDir = fileTools.hasDirectory(wrapper.remoteDir);
			
			if(monitor.isCanceled())
				throw new InterruptedException(Messages.ExportJobWrapper_5);					

			monitor.worked(1);					
			if(!hasDir){
				//if the directory does not exist, then ask user if they would like to create it
				boolean createDir = wrapper.exportPage.queryQuestion(Messages.ExportJobWrapper_6, Messages.ExportJobWrapper_7);						
				if(createDir){
					monitor.subTask(Messages.ExportJobWrapper_8);
					fileTools.createDirectory(wrapper.remoteDir);
				}
				else{
					throw new InvocationTargetException(null, 
						Messages.ExportJobWrapper_9);
				}
			}						
			
			if(monitor.isCanceled())
				throw new InterruptedException(Messages.ExportJobWrapper_10);			

			Object o;
			String localPath = ""; //$NON-NLS-1$
			String localFileName = ""; //$NON-NLS-1$
			String remotePath = wrapper.remoteDir;
			IFile iFile;
			File file;

			//copy over all selected files
			monitor.worked(1);
			monitor.setTaskName(Messages.ExportJobWrapper_11);
			for(int i = 0; i < wrapper.resourcesToCopy.size(); i++){
				
				if(monitor.isCanceled())
					throw new InterruptedException(Messages.ExportJobWrapper_12);
				
				o = wrapper.resourcesToCopy.get(i);
				if(o instanceof org.eclipse.core.resources.IFile){
					iFile = (IFile)o;
					Debug.POLICY.trace(Debug.DEBUG_JOBS, "IFile: {0}", iFile.getFullPath().toOSString()); //$NON-NLS-1$
					localPath = iFile.getLocation().toString();
					localFileName = iFile.getName();
					/*if user has selected to copy directory structure for all files, 
					  then remotePath must be modified to include the relative path of the local file */
					if(wrapper.createDirStructure){
						IPath path = iFile.getFullPath().removeLastSegments(1);
						remotePath = wrapper.remoteDir + path.toString();
					}
					else
						remotePath = wrapper.remoteDir;
				}
				else if(o instanceof java.io.File) {
					file = (File)o;
					Debug.POLICY.trace(Debug.DEBUG_JOBS, "File: {0}", file.getAbsolutePath()); //$NON-NLS-1$
					if(file.isDirectory()){
						copyDirectory(file, fileTools, monitor, ""); //$NON-NLS-1$
						continue;
					}
					localPath = file.toString();
					localFileName = file.getName();
					remotePath = wrapper.remoteDir;
				}
				else if(o instanceof org.eclipse.core.resources.IProject){
					IProject proj = (IProject)o;
					Debug.POLICY.trace(Debug.DEBUG_JOBS, "IProject: {0}", proj.getName()); //$NON-NLS-1$
					copyProject(proj, fileTools, monitor, ""); //$NON-NLS-1$
					continue;
				}
				else if(o instanceof org.eclipse.core.resources.IFolder){
					IFolder folder = (IFolder)o;
					Debug.POLICY.trace(Debug.DEBUG_JOBS, "Folder: {0}", folder.getName()); //$NON-NLS-1$
					copyFolder(folder, fileTools, monitor, Messages.ExportJobWrapper_13);
					continue;
				}					
				
				String tempPath = remotePath + File.separatorChar + localFileName;
				
				monitor.subTask(Messages.ExportJobWrapper_14 + localPath);
				if(wrapper.autoOverwrite){
					if(fileTools.hasDirectory(tempPath))
						fileTools.removeFile(tempPath);
					copyTools.uploadFileToDir(localPath, remotePath);
				}
				else{							
					if(fileTools.hasFile(tempPath) || fileTools.hasDirectory(tempPath)){
						if(!wrapper.noToAllOverwrite){
							String response = wrapper.exportPage.queryOverwrite(localFileName + Messages.ExportJobWrapper_15 + remotePath);
							if(response.equals(IOverwriteQuery.ALL)){
								wrapper.autoOverwrite = true;
								if(fileTools.hasDirectory(tempPath))
									fileTools.removeFile(tempPath);
								copyTools.uploadFileToDir(localPath, remotePath);
							}
							else
							if(response.equals(IOverwriteQuery.YES)){
								if(fileTools.hasDirectory(tempPath))
									fileTools.removeFile(tempPath);
								copyTools.uploadFileToDir(localPath, remotePath);
							}
							else
							if(response.equals(IOverwriteQuery.CANCEL)){
								throw new InterruptedException(Messages.ExportJobWrapper_16);
							}
							else
							if(response.equals(IOverwriteQuery.NO_ALL)){
								wrapper.noToAllOverwrite = true;
							}
						}
					}
					else
						copyTools.uploadFileToDir(localPath, remotePath);
				}
				monitor.worked(1);
			}
			monitor.setTaskName(Messages.ExportJobWrapper_17);
			monitor.subTask(Messages.ExportJobWrapper_18);
		
		} catch (RemoteConnectionException e) {
			Debug.POLICY.error(Debug.DEBUG_JOBS, e);
			exception = e;
			errorMessage = Messages.ExportJobWrapper_19;
			hadError = true;
		} catch (RemoteExecutionException e) {
			Debug.POLICY.error(Debug.DEBUG_JOBS, e);
			exception = e;
			errorMessage = Messages.ExportJobWrapper_20;
			hadError = true;				
		} catch (RemoteOperationException e) {
			Debug.POLICY.error(Debug.DEBUG_JOBS, e);
			exception = e;
			errorMessage = Messages.ExportJobWrapper_20;
			hadError = true;
		} catch (CancelException e) {
			Debug.POLICY.error(Debug.DEBUG_JOBS, e);
			exception = e;
			errorMessage = Messages.ExportJobWrapper_21;
			hadError = true;
		} catch (InterruptedException e){
			Debug.POLICY.error(Debug.DEBUG_JOBS, e);
			exception = e;
			hadError = true;
		} catch (InvocationTargetException e){
			Debug.POLICY.error(Debug.DEBUG_JOBS, e);
			exception = e;
			hadError = true;
		} catch (CoreException e){
			Debug.POLICY.error(Debug.DEBUG_JOBS, e);
			exception = e;
			errorMessage = Messages.ExportJobWrapper_22;
			hadError = true;
		}
		Debug.POLICY.exit(Debug.DEBUG_JOBS);
	}

	private void copyProject(IProject project, IRemoteFileTools tools, IProgressMonitor monitor, String remPath) throws CoreException, InterruptedException, RemoteExecutionException, RemoteConnectionException, CancelException, InvocationTargetException, RemoteOperationException {
		Debug.POLICY.enter(Debug.DEBUG_JOBS, project.getName(), remPath);
		IResource[] members = project.members();
		IResource temp;
		
		for(int i = 0; i < members.length; i++){
			if(monitor.isCanceled())
				throw new InterruptedException(Messages.ExportJobWrapper_23);
			
			temp = members[i];
			if(temp instanceof IProject) {
				IProject proj = (IProject) temp;
				Debug.POLICY.trace(Debug.DEBUG_JOBS, "IProject: {0}", proj.getName()); //$NON-NLS-1$
				copyProject(project, tools, monitor, remPath + File.separatorChar + project.getName());
			} else 
			if(temp instanceof IFolder) {
				IFolder folder = (IFolder) temp;
				Debug.POLICY.trace(Debug.DEBUG_JOBS, "IFolder: {0}", folder.getFullPath().toOSString()); //$NON-NLS-1$
				copyFolder(folder, tools, monitor, remPath + File.separatorChar + project.getName());
			} else 
			if(temp instanceof IFile) {
				IFile file = (IFile)temp;
				Debug.POLICY.trace(Debug.DEBUG_JOBS, "IFile: {0}", file.getFullPath().toOSString()); //$NON-NLS-1$
				copyIFile(file, tools, monitor, remPath + File.separatorChar + project.getName());
			} else {
				throw new InvocationTargetException(null, Messages.ExportJobWrapper_24 + temp.getLocation().toString() +
						Messages.ExportJobWrapper_25);
			}
		}
		Debug.POLICY.exit(Debug.DEBUG_JOBS);
	}

	private void copyFolder(IFolder folder, IRemoteFileTools tools, IProgressMonitor monitor, String remPath) throws CoreException, InterruptedException, RemoteExecutionException, RemoteConnectionException, CancelException, InvocationTargetException, RemoteOperationException {
		Debug.POLICY.enter(Debug.DEBUG_JOBS, folder.getFullPath().toOSString(), remPath);
		IResource[] members = folder.members();
		IResource temp;
		
		for(int i = 0; i < members.length; i++){
			if(monitor.isCanceled())
				throw new InterruptedException(Messages.ExportJobWrapper_26);
			
			temp = members[i];
			if(temp instanceof IProject) {
				IProject proj = (IProject) temp;
				Debug.POLICY.trace(Debug.DEBUG_JOBS, "IProject: {0}", proj.getName()); //$NON-NLS-1$
				copyProject((IProject)temp, tools, monitor, remPath + File.separatorChar + folder.getName());
			} else
			if(temp instanceof IFolder) {
				IFolder folder2 = (IFolder) temp;
				Debug.POLICY.trace(Debug.DEBUG_JOBS, "IFolder: {0}", folder2.getFullPath().toOSString()); //$NON-NLS-1$
				copyFolder((IFolder)temp, tools, monitor, remPath + File.separatorChar + folder.getName());
			} else
			if(temp instanceof IFile) {
				IFile file = (IFile)temp;
				Debug.POLICY.trace(Debug.DEBUG_JOBS, "IFile: {0}", file.getFullPath().toOSString()); //$NON-NLS-1$
				copyIFile((IFile)temp, tools, monitor, remPath + File.separatorChar + folder.getName());
			} else {
				throw new InvocationTargetException(null, Messages.ExportJobWrapper_27 + temp.getLocation().toString() +
					Messages.ExportJobWrapper_28);
			}
		}
		Debug.POLICY.exit(Debug.DEBUG_JOBS);
	}

	private void copyIFile(IFile file, IRemoteFileTools fileTools, IProgressMonitor monitor, String remPath)	throws RemoteOperationException, RemoteConnectionException, CancelException, InterruptedException{
		Debug.POLICY.enter(Debug.DEBUG_JOBS, file.getFullPath().toOSString(), remPath);

		String localPath = file.getLocation().toString();
		String localFileName = file.getName();
		String remotePath = wrapper.remoteDir + remPath;
		String tempPath = remotePath + File.separatorChar + localFileName;
		IRemoteCopyTools copyTools = fileTools.getRemoteCopyTools();
		
		if(monitor.isCanceled())
			throw new InterruptedException(Messages.ExportJobWrapper_29);
		
		monitor.subTask(Messages.ExportJobWrapper_30 + localPath);
		if(wrapper.autoOverwrite){
			if(fileTools.hasDirectory(tempPath))
				fileTools.removeFile(tempPath);
			copyTools.uploadFileToDir(localPath, remotePath);
		}
		else{							
			if(fileTools.hasFile(tempPath) || fileTools.hasDirectory(tempPath)){
				if(!wrapper.noToAllOverwrite){
					String response = wrapper.exportPage.queryOverwrite(localFileName + Messages.ExportJobWrapper_31 + remotePath);
					if(response.equals(IOverwriteQuery.ALL)){
						wrapper.autoOverwrite = true;
						if(fileTools.hasDirectory(tempPath))
							fileTools.removeFile(tempPath);
						copyTools.uploadFileToDir(localPath, remotePath);
					}
					else
					if(response.equals(IOverwriteQuery.YES)){
						if(fileTools.hasDirectory(tempPath))
							fileTools.removeFile(tempPath);
						copyTools.uploadFileToDir(localPath, remotePath);
					}
					else
					if(response.equals(IOverwriteQuery.CANCEL)){
						throw new InterruptedException(Messages.ExportJobWrapper_32);
					}
					else
					if(response.equals(IOverwriteQuery.NO_ALL)){
						wrapper.noToAllOverwrite = true;
					}
				}
			}
			else
				copyTools.uploadFileToDir(localPath, remotePath);
		}
		monitor.worked(1);
		
		Debug.POLICY.exit(Debug.DEBUG_JOBS);
	}

	private void copyDirectory(File file, IRemoteFileTools fileTools, IProgressMonitor monitor, String remPath) throws RemoteExecutionException, RemoteConnectionException, CancelException, InterruptedException, RemoteOperationException {
		Debug.POLICY.enter(Debug.DEBUG_JOBS, file.toString(), remPath);

		if(monitor.isCanceled())
			throw new InterruptedException(Messages.ExportJobWrapper_33);
		
		if(file.isDirectory()){
			File[] files = file.listFiles();
			for(int i = 0; i < files.length; ++i){
				if(monitor.isCanceled())
					throw new InterruptedException(Messages.ExportJobWrapper_34);
				copyDirectory(files[i], fileTools, monitor, remPath + File.separatorChar + file.getName());
			}
			return;
		}			
		
		String localPath = file.toString();
		String localFileName = file.getName();
		String remotePath = wrapper.remoteDir + remPath;
		String tempPath = remotePath + File.separatorChar + localFileName;
		IRemoteCopyTools copyTools = fileTools.getRemoteCopyTools();
		
		if(monitor.isCanceled())
			throw new InterruptedException(Messages.ExportJobWrapper_35);
		
		monitor.subTask(Messages.ExportJobWrapper_36 + file.toString());				
		if(wrapper.autoOverwrite){
			if(fileTools.hasDirectory(tempPath))
				fileTools.removeFile(tempPath);
			copyTools.uploadFileToDir(localPath, remotePath);
		}
		else{							
			if(fileTools.hasFile(tempPath) || fileTools.hasDirectory(tempPath)){
				if(!wrapper.noToAllOverwrite){
					String response = wrapper.exportPage.queryOverwrite(localFileName + Messages.ExportJobWrapper_37 + remotePath);
					if(response.equals(IOverwriteQuery.ALL)){
						wrapper.autoOverwrite = true;
						if(fileTools.hasDirectory(tempPath))
							fileTools.removeFile(tempPath);
						copyTools.uploadFileToDir(localPath, remotePath);
					}
					else
					if(response.equals(IOverwriteQuery.YES)){
						if(fileTools.hasDirectory(tempPath))
							fileTools.removeFile(tempPath);
						copyTools.uploadFileToDir(localPath, remotePath);
					}
					else
					if(response.equals(IOverwriteQuery.CANCEL)){
						throw new InterruptedException(Messages.ExportJobWrapper_38);
					}
					else
					if(response.equals(IOverwriteQuery.NO_ALL)){
						wrapper.noToAllOverwrite = true;
					}
				}
			}
			else
				copyTools.uploadFileToDir(localPath, remotePath);
		}
		monitor.worked(1);	
		Debug.POLICY.exit(Debug.DEBUG_JOBS);
	}
}