/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.managedbuilder.core.makegen;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.IManagedOutputNameProvider;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.makegen.IManagedDependencyGenerator;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.photran.internal.cdtinterface.core.FortranLanguage;

/**
 *  This class implements the Dependency Manager and Output Name Provider interfaces
 *  @author Unknown
 *  @author Timofey Yuvashev 2009
 */
public class DefaultFortranDependencyCalculator implements IManagedDependencyGenerator,
														   IManagedOutputNameProvider
{
	public static final String MODULE_EXTENSION = "o";	//$NON-NLS-1$
	
	/*
	 * Return a list of the names of all modules used by a file
	 */
	private String[] findUsedModuleNames(File file) {
		ArrayList names = new ArrayList();
		try {
		/*
			InputStream in = new BufferedInputStream(new FileInputStream(file));
			ILexer lexer = FortranProcessor.createLexerFor(in, file.getName());
			for (Token thisToken = lexer.yylex(), lastToken = null;
			     thisToken.getTerminal() != Terminal.END_OF_INPUT;
			     lastToken = thisToken, thisToken = lexer.yylex())
			{
				if (lastToken != null
						      && lastToken.getTerminal() == Terminal.T_USE
					          && thisToken.getTerminal() == Terminal.T_IDENT)
				{
					names.add(thisToken.getText());
				}
			}
		*/
			InputStream in = new BufferedInputStream(new FileInputStream(file));
			Reader r = new BufferedReader(new InputStreamReader(in));
			StreamTokenizer st = new StreamTokenizer(r);
			st.commentChar('!');
			st.eolIsSignificant(false);
			st.slashSlashComments(false);
			st.slashStarComments(false);
			st.wordChars('_', '_');
			
			int token;
			while ((token = st.nextToken()) != StreamTokenizer.TT_EOF) {
				if (st.ttype == StreamTokenizer.TT_WORD) {
					if (st.sval.equalsIgnoreCase("use")) {
						token = st.nextToken();
						if (st.ttype == StreamTokenizer.TT_WORD) {
							names.add(st.sval);
						} else {
							st.pushBack();
						}
					}
					/**
					 * This should be moved to separate include file list
					 */
					/*
					else if (st.sval.equalsIgnoreCase("include")) {
						token = st.nextToken();
						if (st.ttype == '\'' || st.ttype == '"') {
							names.add(st.sval);
						} else {
							st.pushBack();
						}
					}
					*/
				}
			}
		}
		catch (Exception e) {
			return new String[0];
		}
		return (String[]) names.toArray(new String[names.size()]);
	}
	
	/*
	 * Return a list of the names of all modules defined in a file
	 */
	private String[] findModuleNames(File file) {
		ArrayList names = new ArrayList();
		try {
			/*
			InputStream in = new BufferedInputStream(new FileInputStream(file));
			ILexer lexer = FortranProcessor.createLexerFor(in, file.getName());
			for (Token thisToken = lexer.yylex(), lastToken = null, tokenBeforeLast = null;
			     thisToken.getTerminal() != Terminal.END_OF_INPUT;
			     tokenBeforeLast = lastToken, lastToken = thisToken, thisToken = lexer.yylex())
			{
				if (lastToken != null
				    && lastToken.getTerminal() == Terminal.T_MODULE
				    && thisToken.getTerminal() == Terminal.T_IDENT)
				{
					if (tokenBeforeLast != null && tokenBeforeLast.getTerminal() == Terminal.T_END) {
						continue;
					}
					names.add(thisToken.getText());
				}
			}
			*/
			InputStream in = new BufferedInputStream(new FileInputStream(file));
			Reader r = new BufferedReader(new InputStreamReader(in));
			StreamTokenizer st = new StreamTokenizer(r);
			st.commentChar('!');
			st.eolIsSignificant(false);
			st.slashSlashComments(false);
			st.slashStarComments(false);
			st.wordChars('_', '_');
			
			int token;
			while ((token = st.nextToken()) != StreamTokenizer.TT_EOF) {
				if (st.ttype == StreamTokenizer.TT_WORD) {
					if (st.sval.equalsIgnoreCase("module")) {
						token = st.nextToken();
						if (st.ttype == StreamTokenizer.TT_WORD) {
							names.add(st.sval);
						} else {
							st.pushBack();
						}
					}
				}
			}
		}
		catch (Exception e) {
			return new String[0];
		}
		return (String[]) names.toArray(new String[names.size()]);
	}

	/*
	 * Returns true if the resource is a Fortran source file
	 */
	private boolean isFortranFile(IProject project, File file, Collection fortranContentTypes) {
		try {
			IContentType ct = CCorePlugin.getContentType(project, file.getCanonicalPath());
			if (ct != null) {
				return fortranContentTypes.contains(ct.toString());
			}
		} catch (Exception e) {
		}
		return false;
	}
	
	/*
	 * Given a set of the module names used by a source file, and a set of resources to search, determine
	 * if any of the source files implements the module names.
	 */
	private IResource[] FindModulesInResources(IProject project, 
											   Collection contentTypes, 
											   IResource resource, 
											   IResource[] resourcesToSearch, 
											   String topBuildDir, 
											   String[] usedNames) 
	{
		IResource[] res = null;
		try {
			res = project.members();
		} catch (CoreException e1) {
			throw new Error("No files found in the given project");
		}
		
		ArrayList modRes = new ArrayList();
		for (int ir = 0; ir < resourcesToSearch.length; ir++) {
			if (resourcesToSearch[ir].equals(resource)) continue;
			if (resourcesToSearch[ir].getType() == IResource.FILE) {
				File projectFile = resourcesToSearch[ir].getLocation().toFile();
				if (!isFortranFile(project, projectFile, contentTypes)) continue;
				String[] modules = findModuleNames(projectFile);
				if (modules != null) {
					for (int iu = 0; iu < usedNames.length; iu++) {
						boolean foundDependency = false;
						for (int im = 0; im < modules.length; im++) {
							if (usedNames[iu].equalsIgnoreCase(modules[im])) {
								//  Get the path to the module file that will be created by the build.  By default, ifort appears
								//  to generate .mod files in the directory from which the compiler is run.  For MBS, this
								//  is the top-level build directory.  
								//  TODO: Support the /module:path option and use that in determining the path of the module file 
								IPath modName = getModulePath(topBuildDir, modules[im], res, project);
								modRes.add(project.getFile(modName));
								foundDependency = true;
								break;
							}
						}
						if (foundDependency) break;
					}
				}
			} else if (resourcesToSearch[ir].getType() == IResource.FOLDER) {
				try {
					IResource[] modFound = FindModulesInResources(project, contentTypes, resource, ((IFolder)resourcesToSearch[ir]).members(), 
							topBuildDir, usedNames);
					if (modFound != null) {
						for (int i=0; i<modFound.length; i++) {
							modRes.add(modFound[i]);
						}
					}
				} catch(Exception e) {}
			}
		}		
		return (IResource[]) modRes.toArray(new IResource[modRes.size()]);
	}
	
	/*
	 * Finds the relative path to the file whose name matches of the given module. If no such file
	 * is found, the "Debug" folder is erased and an error is thrown.
	 * 
	 * @topBuildDir - name of directory (possibly relative path) in which make/object files will be created
	 * @moduleName  - name of the module that we are comparing files against
	 * @project		- project we are currently compiling. Used to remove the Debug directory in case of failure
	 */
	private IPath getModulePath(String topBuildDir, String moduleName, IResource[] resources, IProject project)
	{
		String fileNameContainingModule;
		fileNameContainingModule = getFileNameContainingModule(moduleName, resources, topBuildDir);
		
		//If we can't find any files with that module, remove Debug folder
		if(fileNameContainingModule == null || fileNameContainingModule == "")
		{
			removeDir(new File(project.getLocation().toString() + Path.SEPARATOR + topBuildDir));
			try 
			{
				project.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
			} 
			catch (CoreException e) 
			{
				throw new Error("Could not update the project");
			}
			throw new Error("Could not find a file to match the module name: "+ moduleName);
		}
		
		IPath p = Path.fromOSString("./"+topBuildDir + Path.SEPARATOR + fileNameContainingModule + "." + MODULE_EXTENSION);
		return p;
	}
	
	/*
	 * This method operates under that assumption that 
	 * 1. There will be no 2 files that are named the same, even if they are in separate folder
	 * 2. There is only 1 module per file
	 * 3. There are no files that differ in their names only by upper or lower case letters (i.e. file1.f90 and fILe1.f90)
	 * 
	 * This method finds the relative path to the filename that matches the name of the given module. It 
	 * searches the list of given @resources to compare the names of contained files. It returns a string which
	 * is the relative path to the file supposedly containing the given module, with that file's extension chopped off.
	 * 
	 * @moduleName - name of the module that we are comparing files against
	 * @resources  - list of resources that we look through (could be files and folders)
	 */
	private String getFileNameContainingModule(String moduleName, IResource[] resources, String buildDirName)
	{
		ArrayList possibleMatchingFiles = new ArrayList();
		if(resources == null || resources.length < 1 || 
		   moduleName == null || moduleName == "")
		{
			return null;
		}
		
		for(int i = 0; i < resources.length; i++)
		{
			if(resources[i] instanceof IFile)
			{
				IFile f = (IFile)resources[i];
				
				//Gets rid of the file extension
				String fileName = f.getName().replaceFirst("\\..+", "");
				
				//If a file name matches a module name exactly -- return the relative path to that file
				if(fileName == moduleName)
					return f.getProjectRelativePath().toString().replaceFirst("\\..+", "");
				
				//Otherwise, check if the two names have different cases
				else if(fileName.equalsIgnoreCase(moduleName))
				{
					//And if they do, keep it
					possibleMatchingFiles.add(f.getProjectRelativePath().toString().replaceFirst("\\..+", "")); 
				}
			}
			//If its a folder, recurse
			else if(resources[i] instanceof IContainer)
			{
				IContainer folder = (IContainer)resources[i];
				IResource[] subResource = null;
				
				//Skip build folder, because it contains files with the same
				// names as the one we are looking for (created .o files)
				if(folder.getName().equalsIgnoreCase(buildDirName))
					continue;
					
				try 
				{
					subResource = folder.members();
				} 
				catch (CoreException e) 
				{
					throw new Error("Could not open a container to explore its files");
				}
				
				String name = getFileNameContainingModule(moduleName, subResource, buildDirName);
				if(name != null)
					return name;
			}			
		}
		if(possibleMatchingFiles.size() == 1)
			return (String) possibleMatchingFiles.get(0);
		
		return null;
			
	}
	
	private void removeDir(File f)
	{	
		if(f != null && f.exists())
		{
			File[] files = f.listFiles();
			for(int i = 0; i < files.length; i++)
			{
				if(files[i].isDirectory())
					removeDir(files[i]);
				else
					files[i].delete();
			}
		}
		f.delete();
	}
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.makegen.IManagedBuilderDependencyCalculator#findDependencies(org.eclipse.core.resources.IResource)
	 */
	public IResource[] findDependencies(IResource resource, IProject project) {
		ArrayList dependencies = new ArrayList();
		Collection fortranContentTypes = new FortranLanguage().getRegisteredContentTypeIds();

		//  TODO:  This method should be passed the ITool and the relative path of the top build directory
		//         For now we'll figure this out from the project.
		IManagedBuildInfo mngInfo = ManagedBuildManager.getBuildInfo(project);
		IConfiguration config = mngInfo.getDefaultConfiguration();

		File file = resource.getLocation().toFile();
		try {
			if (!isFortranFile(project, file, fortranContentTypes)) {
				return new IResource[0];
			}
			
			// add dependency on self
			dependencies.add(resource);
	
			//  Get the names of the modules USE'd by the source file
			String[] usedNames = findUsedModuleNames(file);
			if (usedNames.length != 0) {
				//  Search the project files for a Fortran source that creates the module.  If we find one, then compiling this
				//  source file is dependent upon first compiling the found source file.
				IResource[] resources = project.members();	
				IResource[] modRes = FindModulesInResources(project, fortranContentTypes, resource, resources, config.getName(), usedNames);
				if (modRes != null) {
					for (int i=0; i<modRes.length; i++) {
						dependencies.add(modRes[i]);
					}
				}
			}
		}
		catch (Exception e)
		{
			return new IResource[0];
		}
		
		return (IResource[]) dependencies.toArray(new IResource[dependencies.size()]);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.makegen.IManagedBuilderDependencyCalculator#getCalculatorType()
	 */
	public int getCalculatorType() {
		return TYPE_EXTERNAL;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.makegen.IManagedBuilderDependencyCalculator#getDependencyCommand()
	 */
	public String getDependencyCommand(IResource resource, IManagedBuildInfo info) {
		/* 
		 * The type of this IManagedDependencyGenerator is TYPE_EXTERNAL,
		 * so implement findDependencies() rather than getDependencyCommand().
		 * */
		return null;
	}

	/*
	 *  (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IManagedOutputNameProvider#getOutputNames(org.eclipse.cdt.managedbuilder.core.ITool, org.eclipse.core.runtime.IPath[])
	 */
	public IPath[] getOutputNames(ITool tool, IPath[] primaryInputNames) {
		//  TODO:  This method should be passed the relative path of the top build directory?
		ArrayList outs = new ArrayList();
		if (primaryInputNames.length > 0) {
			// Get the names of modules created by this source file
			String[] modules = findModuleNames(primaryInputNames[0].toFile());
			// Add any generated modules
			if (modules != null) {
				for (int i = 0; i < modules.length; i++) {
					//  Return the path to the module file that will be created by the build.  By default, ifort appears
					//  to generate .mod files in the directory from which the compiler is run.  For MBS, this
					//  is the top-level build directory.  
					//  TODO: Support the /module:path option and use that in determining the path of the module file
					//  TODO: The nameProvider documentation should note that the returned path is relative to the top-level 
					//        build directory.  HOWEVER, if only a file name is returned, MBS will automatically add on the
					//        directory path relative to the top-level build directory.  The relative path comes from the source
					//        file location.  In order to specify that this output file is always in the top-level build 
					//        directory, regardless of the source file directory structure, return "./path".
					IPath modName = Path.fromOSString("." + Path.SEPARATOR + modules[i] + "." + MODULE_EXTENSION);
					outs.add(modName);				
				}
			}
		}
		return (IPath[]) outs.toArray(new IPath[outs.size()]);
	}
}
