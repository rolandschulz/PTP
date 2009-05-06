/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.rdt.core.remotemake;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.CCProjectNature;
import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.cdt.make.core.scannerconfig.IScannerConfigBuilderInfo2;
import org.eclipse.cdt.make.internal.core.scannerconfig.gnu.GCCScannerConfigUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ptp.rdt.core.RDTLog;


/**
 * Gets the command to run the compiler from the scanner discovery extension point
 * and prepares it to run.
 * 
 * @author Mike Kucera
 */
public class RemoteSpecsRunSIProvider extends RemoteRunSIProvider {

	public static final String SPECS_FILE_PATH_VAR = "${specs_file_path}";   //$NON-NLS-1$
	public static final String SPECS_FOLDER_NAME   = ".specs"; //$NON-NLS-1$
	
	
	@Override
	protected List<String> getCommand(IProject project, String providerId, IScannerConfigBuilderInfo2 buildInfo) {
		// get the command that is provided in the extension point
		String gcc  = buildInfo.getProviderRunCommand(providerId);
		String args = buildInfo.getProviderRunArguments(providerId);
		String specsFileName = getSpecsFileName(project);
		
		if(gcc == null || args == null || specsFileName == null)
			return null;
		
		IFile specsFile;
		try {
			specsFile = createSpecsFile(project, specsFileName);
		} catch (CoreException e) {
			RDTLog.logError(e);
			return null;
		}
		
		String specsFilePath = specsFile.getLocationURI().getRawPath();
		args = args.replace(SPECS_FILE_PATH_VAR, specsFilePath);
		
		List<String> command = new ArrayList<String>();
		command.add(gcc);
		for(String arg : args.split(" ")) //$NON-NLS-1$
			command.add(arg);
		
		return command;
	}
	
	
	/**
	 * Create an empty "specs" file in a hidden directory in the project.
	 * Use EFS (via resources) for this for simplicity.
	 */
	private static IFile createSpecsFile(IProject project, String specsFileName) throws CoreException  {
		IFolder specsFolder = project.getFolder(SPECS_FOLDER_NAME);
		if(!specsFolder.exists()) {
			specsFolder.create(IResource.HIDDEN, true, null); // should not fire resource event
		}
		
		IFile specsFile = specsFolder.getFile(specsFileName);
		if(!specsFile.exists()) {
			InputStream is = new ByteArrayInputStream("\n".getBytes()); //$NON-NLS-1$
			specsFile.create(is, true, null);
		}
		
		return specsFile;
	}
	
	
	private static String getSpecsFileName(IProject project) {
		try {
			if(project.hasNature(CCProjectNature.CC_NATURE_ID))
	            return GCCScannerConfigUtil.CPP_SPECS_FILE;
	        else if(project.hasNature(CProjectNature.C_NATURE_ID))
	            return GCCScannerConfigUtil.C_SPECS_FILE;
		} catch(CoreException e) { }
		
		return null;
	}
}
