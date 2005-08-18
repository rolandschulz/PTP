/*******************************************************************************
 * Copyright (c) 2003, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.makegen.gnu;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;	// Note: We use LinkedHashMap instead of HashMap 
								// only to keep the generation of makefiles constant
								// for our test set.  Make itself doesn't care 
								// about the order.
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.internal.core.model.Util;
import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IInputType;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.IManagedCommandLineGenerator;
import org.eclipse.cdt.managedbuilder.core.IManagedCommandLineInfo;
import org.eclipse.cdt.managedbuilder.core.IManagedOutputNameProvider;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.IOutputType;
import org.eclipse.cdt.managedbuilder.core.IResourceConfiguration;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.core.ManagedBuilderCorePlugin;
import org.eclipse.cdt.managedbuilder.internal.core.ManagedMakeMessages;
import org.eclipse.cdt.managedbuilder.internal.macros.FileContextData;
import org.eclipse.cdt.managedbuilder.internal.macros.MacroResolver;
import org.eclipse.cdt.managedbuilder.macros.BuildMacroException;
import org.eclipse.cdt.managedbuilder.macros.IBuildMacroProvider;
import org.eclipse.cdt.managedbuilder.makegen.IManagedBuilderMakefileGenerator;
import org.eclipse.cdt.managedbuilder.makegen.IManagedDependencyGenerator;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.resources.IResourceProxyVisitor;
import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;

/**
 * This is a specialized makefile generator that takes advantage of the 
 * extensions present in Gnu Make.
 * 
 * @since 1.2
 */

public class GnuMakefileGenerator implements IManagedBuilderMakefileGenerator {

	/**
	 * This class walks the delta supplied by the build system to determine
	 * what resources have been changed. The logic is very simple. If a 
	 * buildable resource (non-header) has been added or removed, the directories 
	 * in which they are located are "dirty" so the makefile fragments for them 
	 * have to be regenerated.
	 * <p>
	 * The actual dependencies are recalculated as a result of the build step 
	 * itself. We are relying on make to do the right things when confronted 
	 * with a dependency on a moved header file. That said, make will treat 
	 * the missing header file in a dependency rule as a target it has to build 
	 * unless told otherwise. These dummy targets are added to the makefile 
	 * to avoid a missing target error. 
	 */
	public class ResourceDeltaVisitor implements IResourceDeltaVisitor {
		private GnuMakefileGenerator generator;
		private IManagedBuildInfo info;

		/**
		 * The constructor 
		 */
		public ResourceDeltaVisitor(GnuMakefileGenerator generator, IManagedBuildInfo info) {
			this.generator = generator;
			this.info = info;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.core.resources.IResourceDeltaVisitor#visit(org.eclipse.core.resources.IResourceDelta)
		 */
		public boolean visit(IResourceDelta delta) throws CoreException {
			// Should the visitor keep iterating in current directory 
			boolean keepLooking = false;
			IResource resource = delta.getResource();
			
			// What kind of resource change has occurred
			if (resource.getType() == IResource.FILE) {
				String ext = resource.getFileExtension();
				switch (delta.getKind()) {
					case IResourceDelta.ADDED:
						if (!generator.isGeneratedResource(resource)) {
							// This is a source file so just add its container
							if (info.buildsFileType(ext)) {
								generator.appendModifiedSubdirectory(resource);
							}
						}
						break;
					case IResourceDelta.REMOVED:
						// we get this notification if a resource is moved too
						if (!generator.isGeneratedResource(resource)) {
							// This is a source file so just add its container
							if (info.buildsFileType(ext)) {
								generator.appendDeletedFile(resource);
								generator.appendModifiedSubdirectory(resource);
							}
						}
						break;
					default:
						keepLooking = true;
						break;
				}
			} 
			if (resource.getType() == IResource.FOLDER) {
				// I only care about delete event
				switch (delta.getKind()) {
					case IResourceDelta.REMOVED:
						if (!generator.isGeneratedResource(resource)) {
							generator.appendDeletedSubdirectory((IContainer)resource);
						}
					default:
						break;
				}
			}
			if (resource.getType() == IResource.PROJECT) {
				// If there is a zero-length delta, something the project depends on has changed so just call make
				IResourceDelta[] children = delta.getAffectedChildren();
				if (children != null && children.length > 0) {
					keepLooking = true;
				}
			} else {
				// If the resource is part of the generated directory structure don't recurse
				if (!generator.isGeneratedResource(resource)) {
					keepLooking = true;
				}
			}

			return keepLooking;
		}
	}	


	
	/**
	 * This class is used to recursively walk the project and determine which
	 * modules contribute buildable source files. 
	 */
	protected class ResourceProxyVisitor implements IResourceProxyVisitor {
		private GnuMakefileGenerator generator;
		private IManagedBuildInfo info;

		/**
		 * Constructs a new resource proxy visitor to quickly visit project
		 * resources.
		 */
		public ResourceProxyVisitor(GnuMakefileGenerator generator, IManagedBuildInfo info) {
			this.generator = generator;
			this.info = info;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.core.resources.IResourceProxyVisitor#visit(org.eclipse.core.resources.IResourceProxy)
		 */
		public boolean visit(IResourceProxy proxy) throws CoreException {
			// No point in proceeding, is there 
			if (generator == null) {
				return false;
			}
			
			// Is this a resource we should even consider
			if (proxy.getType() == IResource.FILE) {
				// If this resource has a Resource Configuration and is not excluded or
				// if it has a file extension that one of the tools builds, add the sudirectory to the list
				IResource resource = proxy.requestResource();
				boolean willBuild = false; 
				IResourceConfiguration resConfig = config.getResourceConfiguration(resource.getFullPath().toString());
				if (resConfig != null) willBuild = true;
				if (!willBuild) {
					String ext = resource.getFileExtension();				
					if (info.buildsFileType(ext) &&
							// If this file resource is a generated resource, then it is uninteresting
							!generator.isGeneratedResource(resource)) {
						willBuild = true;
					}
				}
				if (willBuild) {
					if ((resConfig == null) || (!(resConfig.isExcluded()))) {
						generator.appendBuildSubdirectory(resource);
					}
				}				
				return false;
			}

			// Recurse into subdirectories
			return true;
		}

	}

	// String constants for makefile contents and messages
	private static final String COMMENT = "MakefileGenerator.comment";	//$NON-NLS-1$
	//private static final String AUTO_DEP = COMMENT + ".autodeps";	//$NON-NLS-1$
	//private static final String MESSAGE = "ManagedMakeBuilder.message";	//$NON-NLS-1$
	//private static final String BUILD_ERROR = MESSAGE + ".error";	//$NON-NLS-1$
	
	//private static final String DEP_INCL = COMMENT + ".module.dep.includes";	//$NON-NLS-1$
	private static final String HEADER = COMMENT + ".header"; //$NON-NLS-1$
	
	protected static final String MESSAGE_FINISH_BUILD = ManagedMakeMessages.getResourceString("MakefileGenerator.message.finish.build");	//$NON-NLS-1$
	protected static final String MESSAGE_FINISH_FILE = ManagedMakeMessages.getResourceString("MakefileGenerator.message.finish.file");	//$NON-NLS-1$
	protected static final String MESSAGE_START_BUILD = ManagedMakeMessages.getResourceString("MakefileGenerator.message.start.build");	//$NON-NLS-1$
	protected static final String MESSAGE_START_FILE = ManagedMakeMessages.getResourceString("MakefileGenerator.message.start.file");	//$NON-NLS-1$
	protected static final String MESSAGE_NO_TARGET_TOOL = ManagedMakeMessages.getResourceString("MakefileGenerator.message.no.target");	//$NON-NLS-1$
	//private static final String MOD_INCL = COMMENT + ".module.make.includes";	//$NON-NLS-1$	
	private static final String MOD_LIST = COMMENT + ".module.list";	//$NON-NLS-1$	
	private static final String MOD_VARS = COMMENT + ".module.variables";	//$NON-NLS-1$	
	private static final String MOD_RULES = COMMENT + ".build.rule";	//$NON-NLS-1$	
	private static final String BUILD_TOP = COMMENT + ".build.toprules";	//$NON-NLS-1$	
	private static final String ALL_TARGET = COMMENT + ".build.alltarget";	//$NON-NLS-1$	
	private static final String MAINBUILD_TARGET = COMMENT + ".build.mainbuildtarget";	//$NON-NLS-1$
	private static final String BUILD_TARGETS = COMMENT + ".build.toptargets";	//$NON-NLS-1$	
	private static final String SRC_LISTS = COMMENT + ".source.list";	//$NON-NLS-1$
	
	private static final String EMPTY_STRING = new String();
	private static final String[] EMPTY_STRING_ARRAY = new String[0];

	private static final String OBJS_MACRO = "OBJS";	//$NON-NLS-1$
	private static final String DEPS_MACRO = "DEPS";	//$NON-NLS-1$
	private static final String MACRO_ADDITION_ADDPREFIX_HEADER = "${addprefix ";	//$NON-NLS-1$
	private static final String MACRO_ADDITION_ADDPREFIX_SUFFIX = "," + WHITESPACE + LINEBREAK;	//$NON-NLS-1$
	private static final String MACRO_ADDITION_PREFIX_SUFFIX = "+=" + WHITESPACE + LINEBREAK;	//$NON-NLS-1$
	private static final String PREBUILD = "pre-build"; //$NON-NLS-1$ 
	private static final String MAINBUILD = "main-build"; //$NON-NLS-1$ 
	private static final String POSTBUILD = "post-build"; //$NON-NLS-1$ 
	private static final String SECONDARY_OUTPUTS = "secondary-outputs"; //$NON-NLS-1$ 
	
	// Enumerations
	public static final int 
			PROJECT_RELATIVE = 1,
			PROJECT_SUBDIR_RELATIVE = 2,
			ABSOLUTE = 3;
	
	// Local variables needed by generator
	private String buildTargetName;
	private String buildTargetExt;
	private IConfiguration config;
	private ITool[] buildTools;
	private boolean[] buildToolsUsed;
	private ManagedBuildGnuToolInfo[] gnuToolInfos;
	private Vector deletedFileList;
	private Vector deletedDirList;
	private Vector dependencyMakefiles;
	private IManagedBuildInfo info;
	private Vector invalidDirList;
	private Vector modifiedList;
	private IProgressMonitor monitor;
	private IProject project;
	private IResource[] projectResources;
	private Vector ruleList;
	private Vector subdirList;
	private IPath topBuildDir;
	private Set outputExtensionsSet;
	// Maps of macro names (String) to values (List)
    private HashMap buildSrcVars = new HashMap();
	private HashMap buildOutVars = new HashMap();
	private LinkedHashMap topBuildOutVars = new LinkedHashMap();

	public GnuMakefileGenerator() {
		super();
	}

	
	/*************************************************************************
	 *   IManagedBuilderMakefileGenerator   M E T H O D S
	 ************************************************************************/
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.makegen.IManagedBuilderMakefileGenerator#initialize()
	 *
	 * @param project
	 * @param info
	 * @param monitor
	 */
	public void initialize(IProject project, IManagedBuildInfo info, IProgressMonitor monitor) {
		// Save the project so we can get path and member information
		this.project = project;
		try {
			projectResources = project.members();
		} catch (CoreException e) {
			projectResources = null;
		}
		// Save the monitor reference for reporting back to the user
		this.monitor = monitor;
		// Get the build info for the project
		this.info = info;
		// Get the name of the build target
		buildTargetName = info.getBuildArtifactName();
		// Get its extension
		buildTargetExt = info.getBuildArtifactExtension();
		
		try{
			//try to resolve the build macros in the target extension
			buildTargetExt = ManagedBuildManager.getBuildMacroProvider().resolveValueToMakefileFormat(
					buildTargetExt,
					"", //$NON-NLS-1$
					" ", //$NON-NLS-1$
					IBuildMacroProvider.CONTEXT_CONFIGURATION,
					info.getDefaultConfiguration());
		} catch (BuildMacroException e){
		}
		
		try{
			//try to resolve the build macros in the target name
			String resolved = ManagedBuildManager.getBuildMacroProvider().resolveValueToMakefileFormat(
					buildTargetName,
					"", //$NON-NLS-1$
					" ", //$NON-NLS-1$
					IBuildMacroProvider.CONTEXT_CONFIGURATION,
					info.getDefaultConfiguration());
			if((resolved = resolved.trim()).length() > 0)
				buildTargetName = resolved;
		} catch (BuildMacroException e){
		}

		
		if (buildTargetExt == null) {
			buildTargetExt = new String();
		}
		// Cache the build tools
		config = info.getDefaultConfiguration();
		buildTools = config.getFilteredTools();
		buildToolsUsed = new boolean[buildTools.length];
		for (int i=0; i<buildTools.length; i++) buildToolsUsed[i] = false;
		// Initialize the tool info array
		gnuToolInfos = new ManagedBuildGnuToolInfo[buildTools.length];
		//set the top build dir path
		topBuildDir = project.getFolder(info.getConfigurationName()).getFullPath();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.makegen.IManagedBuilderMakefileGenerator#generateDependencies()
	 */
	public void generateDependencies() throws CoreException {
		// This is a hack for the pre-3.x GCC compilers
		IWorkspaceRoot root = CCorePlugin.getWorkspace().getRoot();
		Iterator subDirs = getSubdirList().listIterator();
		while(subDirs.hasNext()) {
			// The builder creates a subdir with same name as source in the build location
			IContainer subDir = (IContainer)subDirs.next();
			IPath projectRelativePath = subDir.getProjectRelativePath();
			IPath buildRelativePath = topBuildDir.append(projectRelativePath);
			IFolder buildFolder = root.getFolder(buildRelativePath);
			if (buildFolder == null) continue;

			// Find all of the dep files in the generated subdirectories
			IResource[] files = buildFolder.members();
			for (int index = 0; index < files.length; ++index){
				IResource file = files[index];
				if (DEP_EXT.equals(file.getFileExtension())) {
					IFile depFile = root.getFile(file.getFullPath());
					if (depFile == null) continue;
					try {
						updateMonitor(ManagedMakeMessages.getFormattedString("GnuMakefileGenerator.message.postproc.dep.file", depFile.getName()));	//$NON-NLS-1$
						populateDummyTargets(depFile, false);
					} catch (CoreException e) {
						throw e;
					} catch (IOException e) {
						// Keep trying
						continue;
					}
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.makegen.IManagedBuilderMakefileGenerator#generateMakefiles(org.eclipse.core.resources.IResourceDelta)
	 */
	public MultiStatus generateMakefiles(IResourceDelta delta) throws CoreException {
		/*
		 * Let's do a sanity check right now. 
		 * 
		 * 1. This is an incremental build, so if the top-level directory is not 
		 * there, then a rebuild is needed.
		 */
		IFolder folder = project.getFolder(info.getConfigurationName());
		if (!folder.exists()) {
			return regenerateMakefiles();
		}
		
		// Return value
		MultiStatus status;		

		// Visit the resources in the delta and compile a list of subdirectories to regenerate
		updateMonitor(ManagedMakeMessages.getFormattedString("MakefileGenerator.message.calc.delta", project.getName()));	//$NON-NLS-1$
		ResourceDeltaVisitor visitor = new ResourceDeltaVisitor(this, info);
		delta.accept(visitor);
		checkCancel();
		
		// Get all the subdirectories participating in the build
		updateMonitor(ManagedMakeMessages.getFormattedString("MakefileGenerator.message.finding.sources", project.getName()));	//$NON-NLS-1$
		ResourceProxyVisitor resourceVisitor = new ResourceProxyVisitor(this, info);
		project.accept(resourceVisitor, IResource.NONE);
		checkCancel();
		
		// Make sure there is something to build
		if (getSubdirList().isEmpty()) {
			String info = ManagedMakeMessages.getFormattedString("MakefileGenerator.warning.no.source", project.getName());	//$NON-NLS-1$ 
			updateMonitor(info);
			status = new MultiStatus(
					ManagedBuilderCorePlugin.getUniqueIdentifier(),
					IStatus.INFO,
					new String(),
					null);
			status.add(new Status (
					IStatus.INFO,
					ManagedBuilderCorePlugin.getUniqueIdentifier(),
					NO_SOURCE_FOLDERS,
					info,
					null));
			return status;
		} 

		// Make sure the build directory is available
		topBuildDir = createDirectory(info.getConfigurationName());
		checkCancel();

		// Make sure that there is a makefile containing all the folders participating
		IPath srcsFilePath = topBuildDir.append(SRCSFILE_NAME);
		IFile srcsFileHandle = createFile(srcsFilePath);
		buildSrcVars.clear();
		buildOutVars.clear();
		topBuildOutVars.clear();
		populateSourcesMakefile(srcsFileHandle);
		checkCancel();
		
		// Regenerate any fragments that are missing for the exisiting directories NOT modified
		Iterator iter = getSubdirList().listIterator();
		while (iter.hasNext()) {
			IContainer subdirectory = (IContainer)iter.next();
			if (!getModifiedList().contains(subdirectory)) {
				// Make sure the directory exists (it may have been deleted)
				if (!subdirectory.exists()) {
					appendDeletedSubdirectory(subdirectory);
					continue;
				}
				// Make sure a fragment makefile exists
				IPath fragmentPath = getBuildWorkingDir().append(subdirectory.getProjectRelativePath()).append(MODFILE_NAME);
				IFile makeFragment = project.getFile(fragmentPath);
				if (!makeFragment.exists()) {
					// If one or both are missing, then add it to the list to be generated
					getModifiedList().add(subdirectory);
				}
			}
		}

		// Delete the old dependency files for any deleted resources
		iter = getDeletedFileList().listIterator();
		while (iter.hasNext()) {
			IResource deletedFile = (IResource)iter.next();
			deleteDepFile(deletedFile);
			deleteBuildTarget(deletedFile);
		}
		
		// Regenerate any fragments for modified directories
		iter = getModifiedList().listIterator();
		while (iter.hasNext()) {
			IContainer subDir = (IContainer) iter.next();
			// Make sure the directory exists (it may have been deleted)
			if (!subDir.exists()) {
				appendDeletedSubdirectory(subDir);
				continue;
			}
			//populateFragmentMakefile(subDir);    //  See below
			checkCancel();
		}
		
		// Recreate all module makefiles
		// NOTE WELL: For now, always recreate all of the fragment makefile.  This is necessary
		//     in order to re-populate the buildVariable lists.  In the future, the list could 
		//     possibly segmented by subdir so that all fragments didn't need to be
		//     regenerated
		iter = getSubdirList().listIterator();
		while (iter.hasNext()) {
			IContainer subDir = (IContainer)iter.next();
			try {
				populateFragmentMakefile(subDir);
			} catch (CoreException e) {
				// Probably should ask user if they want to continue
				checkCancel();
				continue;
			}
			checkCancel();
		}
	
		// Calculate the inputs and outputs of the Tools to be generated in the main makefile
		calculateToolInputsOutputs();
		checkCancel();

		// Re-create the top-level makefile
		IPath makefilePath = topBuildDir.append(MAKEFILE_NAME);
		IFile makefileHandle = createFile(makefilePath);
		populateTopMakefile(makefileHandle, false);
		checkCancel();
		
		// Remove deleted folders from generated build directory
		iter = getDeletedDirList().listIterator();
		while (iter.hasNext()) {
			IContainer subDir = (IContainer) iter.next();
			removeGeneratedDirectory(subDir);
			checkCancel();
		}

		// How did we do
		if (!getInvalidDirList().isEmpty()) {
			status = new MultiStatus (
					ManagedBuilderCorePlugin.getUniqueIdentifier(),
					IStatus.WARNING,
					new String(),
					null);
			// Add a new status for each of the bad folders
			iter = getInvalidDirList().iterator();
			while (iter.hasNext()) {
				status.add(new Status (
						IStatus.WARNING,
						ManagedBuilderCorePlugin.getUniqueIdentifier(),
						SPACES_IN_PATH,
						((IContainer)iter.next()).getFullPath().toString(),
						null));
			}
		} else {
			status = new MultiStatus(
					ManagedBuilderCorePlugin.getUniqueIdentifier(),
					IStatus.OK,
					new String(),
					null);
		}

		return status;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.makegen.IManagedBuilderMakefileGenerator#getBuildWorkingDir()
	 */
	public IPath getBuildWorkingDir() {
		if (topBuildDir != null) {
			return topBuildDir.removeFirstSegments(1);
		} 
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.makegen.IManagedBuilderMakefileGenerator#getMakefileName()
	 */
	public String getMakefileName() {
		return new String(MAKEFILE_NAME);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.makegen.IManagedBuilderMakefileGenerator#isGeneratedResource(org.eclipse.core.resources.IResource)
	 */
	public boolean isGeneratedResource(IResource resource) {
		// Is this a generated directory ...
		IPath path = resource.getProjectRelativePath();
		String[] configNames = info.getConfigurationNames();
		for (int i = 0; i < configNames.length; i++) {
			String name = configNames[i];
			IPath root = new Path(name);
			// It is if it is a root of the resource pathname
			if (root.isPrefixOf(path)) return true;
		}
		
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.makegen.IManagedBuilderMakefileGenerator#regenerateDependencies()
	 */
	public void regenerateDependencies(boolean force) throws CoreException {
		// A hack for the pre-3.x GCC compilers is to put dummy targets for deps
		IWorkspaceRoot root = CCorePlugin.getWorkspace().getRoot();

		Iterator iter = getDependencyMakefiles().listIterator();
		while (iter.hasNext()) {
			// The path to search for the dependency makefile
			IPath relDepFilePath = topBuildDir.append(new Path((String)iter.next()));
			IFile depFile = root.getFile(relDepFilePath);
			if (depFile == null || !depFile.isAccessible()) continue;
			try {
				updateMonitor(ManagedMakeMessages.getFormattedString("GnuMakefileGenerator.message.postproc.dep.file", depFile.getName()));	//$NON-NLS-1$
				populateDummyTargets(depFile, true);
			} catch (CoreException e) {
				throw e;
			} catch (IOException e) {
				// This looks like a problem reading or writing the file
				continue;
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.makegen.IManagedBuilderMakefileGenerator#regenerateMakefiles()
	 */
	public MultiStatus regenerateMakefiles() throws CoreException {
		MultiStatus status;
		// Visit the resources in the project
		ResourceProxyVisitor visitor = new ResourceProxyVisitor(this, info);
		project.accept(visitor, IResource.NONE);
		
		// See if the user has cancelled the build
		checkCancel();

		// Populate the makefile if any buildable source files have been found in the project
		if (getSubdirList().isEmpty()) {
			String info = ManagedMakeMessages.getFormattedString("MakefileGenerator.warning.no.source", project.getName()); //$NON-NLS-1$ 
			updateMonitor(info);	
			status = new MultiStatus(
					ManagedBuilderCorePlugin.getUniqueIdentifier(),
					IStatus.INFO,
					new String(),
					null);
			status.add(new Status (
					IStatus.INFO,
					ManagedBuilderCorePlugin.getUniqueIdentifier(),
					NO_SOURCE_FOLDERS,
					info,
					null));
			return status;
		} 

		// Create the top-level directory for the build output
		topBuildDir = createDirectory(info.getConfigurationName());
		checkCancel();
		
		// Get the list of subdirectories
		IPath srcsFilePath = topBuildDir.append(SRCSFILE_NAME);
		IFile srcsFileHandle = createFile(srcsFilePath);
		buildSrcVars.clear();
		buildOutVars.clear();
		topBuildOutVars.clear();
		populateSourcesMakefile(srcsFileHandle);
		checkCancel();
		
		// Now populate the module makefiles
		Iterator iter = getSubdirList().listIterator();
		while (iter.hasNext()) {
			IContainer subDir = (IContainer)iter.next();
			try {
				populateFragmentMakefile(subDir);
			} catch (CoreException e) {
				// Probably should ask user if they want to continue
				checkCancel();
				continue;
			}
			checkCancel();
		}

		// Calculate the inputs and outputs of the Tools to be generated in the main makefile
		calculateToolInputsOutputs();
		checkCancel();

		// Create the top-level makefile
		IPath makefilePath = topBuildDir.append(MAKEFILE_NAME);
		IFile makefileHandle = createFile(makefilePath);
		populateTopMakefile(makefileHandle, true);
		checkCancel();
		
		// Now finish up by adding all the object files
		IPath objFilePath = topBuildDir.append(OBJECTS_MAKFILE);
		IFile objsFileHandle = createFile(objFilePath);
		populateObjectsMakefile(objsFileHandle);
		checkCancel();

		// How did we do
		if (!getInvalidDirList().isEmpty()) {
			status = new MultiStatus (
					ManagedBuilderCorePlugin.getUniqueIdentifier(),
					IStatus.WARNING,
					new String(),
					null);
			// Add a new status for each of the bad folders
			iter = getInvalidDirList().iterator();
			while (iter.hasNext()) {
				status.add(new Status (
						IStatus.WARNING,
						ManagedBuilderCorePlugin.getUniqueIdentifier(),
						SPACES_IN_PATH,
						((IContainer)iter.next()).getFullPath().toString(),
						null));
			}
		} else {
			status = new MultiStatus(
					ManagedBuilderCorePlugin.getUniqueIdentifier(),
					IStatus.OK,
					new String(),
					null);
		}
		return status;
	}

	
	/*************************************************************************
	 *   M A K E F I L E S   P O P U L A T I O N   M E T H O D S 
	 ************************************************************************/

	protected void populateDummyTargets(IFile makefile, boolean force) throws CoreException, IOException {
		if (makefile == null || !makefile.exists()) return;
		
		// Found that bad boy, so let's get its contents
		InputStream contentStream = makefile.getContents(false);
		Reader in = new InputStreamReader(contentStream);
		StringBuffer inBuffer = null;
		int chunkSize = contentStream.available();
		inBuffer = new StringBuffer(chunkSize);
		char[] readBuffer = new char[chunkSize];
		int n = in.read(readBuffer);
		while (n > 0) {
			inBuffer.append(readBuffer);
			n = in.read(readBuffer);
		}
		contentStream.close();  
		
		// The rest of this operation is equally expensive, so 
		// if we are doing an incremental build, only update the 
		// files that do not have a comment
		if (inBuffer == null) return;
		String inBufferString = inBuffer.toString();
		if (!force && inBufferString.startsWith(COMMENT_SYMBOL)) {
				return;
		}

		// Reconstruct the buffer tokens into useful chunks of dependency information 
		Vector bufferTokens = new Vector(Arrays.asList(inBufferString.split("\\s")));	//$NON-NLS-1$
		Vector deps = new Vector(bufferTokens.size());
		Iterator tokenIter = bufferTokens.iterator();
		while (tokenIter.hasNext()) {
			String token = (String)tokenIter.next();
			if (token.lastIndexOf("\\") == token.length() - 1  && token.length() > 1) {	//$NON-NLS-1$
				// This is escaped so keep adding to the token until we find the end
				while (tokenIter.hasNext()) {
					String nextToken = (String)tokenIter.next();
					token += WHITESPACE + nextToken;
					if (!nextToken.endsWith("\\")) {	//$NON-NLS-1$
						break;
					}
				}
			}
			deps.add(token);
		}
		deps.trimToSize();
		
		// Now find the header file dependencies and make dummy targets for them
		boolean save = false;
		StringBuffer outBuffer = null;
		
		// If we are doing an incremental build, only update the files that do not have a comment
		String firstLine;
		try {
			firstLine = (String) deps.get(0);
		} catch (ArrayIndexOutOfBoundsException e) {
			// This makes no sense so bail
			return;
		}

		// Put the generated comments in the output buffer
		if (!firstLine.startsWith(COMMENT_SYMBOL)) {
			outBuffer = addDefaultHeader();
		} else {
			outBuffer = new StringBuffer();
		}
		
		// Some echo implementations misbehave and put the -n and newline in the output
		if (firstLine.startsWith("-n")) { //$NON-NLS-1$
			
			// Now let's parse:
			// Win32 outputs -n '<path>/<file>.d <path>/'
			// POSIX outputs -n <path>/<file>.d <path>/
			// Get the dep file name
			String secondLine;
			try {
				secondLine = (String) deps.get(1);
			} catch (ArrayIndexOutOfBoundsException e) {
				secondLine = new String();
			}
			if (secondLine.startsWith("'")) { //$NON-NLS-1$
				// This is the Win32 implementation of echo (MinGW without MSYS)
				outBuffer.append(secondLine.substring(1) + WHITESPACE);
			} else {
				outBuffer.append(secondLine + WHITESPACE);
			}
			
			// The relative path to the build goal comes next
			String thirdLine;
			try {
				thirdLine = (String) deps.get(2);
			} catch (ArrayIndexOutOfBoundsException e) {
				thirdLine = new String();
			}
			int lastIndex = thirdLine.lastIndexOf("'"); //$NON-NLS-1$
			if (lastIndex != -1) {
				if (lastIndex == 0) {
					outBuffer.append(WHITESPACE);
				} else {
					outBuffer.append(thirdLine.substring(0, lastIndex - 1));
				}
			} else {
				outBuffer.append(thirdLine);
			}
			
			// followed by the actual dependencies
			String fourthLine;
			try {
				fourthLine = (String) deps.get(3);
			} catch (ArrayIndexOutOfBoundsException e) {
				fourthLine = new String();
			}
			outBuffer.append(fourthLine + WHITESPACE);
			
			// Now do the rest
			try {
				Iterator iter = deps.listIterator(4);
				while (iter.hasNext()) {
					String nextElement = (String)iter.next();
					if (nextElement.endsWith("\\")) { //$NON-NLS-1$
						outBuffer.append(nextElement + NEWLINE + WHITESPACE);
					} else {
						outBuffer.append(nextElement + WHITESPACE);
					}
				}
			} catch (IndexOutOfBoundsException e) {					
			}

		} else {
			outBuffer.append(inBuffer);
		}
		
		outBuffer.append(NEWLINE);
		save = true;
		
		// Dummy targets to add to the makefile
		Iterator dummyIter = deps.iterator();
		while (dummyIter.hasNext()) {
			String dummy = (String)dummyIter.next();
			IPath dep = new Path(dummy);
			String extension = dep.getFileExtension();
			if (info.isHeaderFile(extension)) {
				/*
				 * The formatting here is 
				 * <dummy_target>:
				 */
				outBuffer.append(dummy + COLON + NEWLINE + NEWLINE);
			}
		}
		
		// Write them out to the makefile
		if (save) {
			Util.save(outBuffer, makefile);
		}		
	}
	
	/* (non-javadoc)
	 * This method generates a "fragment" make file (subdir.mk).
	 * One of these is generated for each project directory/subdirectory
	 * that contains source files.
	 * 
	 * @param module
	 * @throws CoreException
	 */
	protected void populateFragmentMakefile(IContainer module) throws CoreException {
		// Calculate the new directory relative to the build output
		IPath moduleRelativePath = module.getProjectRelativePath();
		IPath buildRoot = getBuildWorkingDir();
		if (buildRoot == null) {
			return;
		}
		
		IPath moduleOutputPath = buildRoot.append(moduleRelativePath);
		updateMonitor(ManagedMakeMessages.getFormattedString("MakefileGenerator.message.gen.source.makefile", moduleOutputPath.toString()));	//$NON-NLS-1$

		// Now create the directory
		IPath moduleOutputDir = createDirectory(moduleOutputPath.toString());
		
		// Create a module makefile
		IFile modMakefile = createFile(moduleOutputDir.append(MODFILE_NAME));
		StringBuffer makeBuf = new StringBuffer();
		makeBuf.append(addFragmentMakefileHeader());
		makeBuf.append(addSources(module));

		// Save the files
		Util.save(makeBuf, modMakefile);
	}

	/* (non-Javadoc)
	 * The makefile generator generates a Macro for each type of output, other than final artifact,
	 * created by the build.    
	 * 
	 * @param fileHandle The file that should be populated with the output 
	 * @throws CoreException
	 */
	protected void populateObjectsMakefile(IFile fileHandle) throws CoreException {
		
		// Master list of "object" dependencies, i.e. dependencies between input files and output files.
		StringBuffer macroBuffer = new StringBuffer();
		List valueList;
		macroBuffer.append(addDefaultHeader());

		// Map of macro names (String) to its definition (List of Strings)
		HashMap outputMacros = new HashMap();

		// Add the predefined LIBS, USER_OBJS, & DEPS macros
		
		// Add the libraries this project depends on
		valueList = new ArrayList();
		String[] libs = info.getLibsForConfiguration(buildTargetExt);
		for (int i = 0; i < libs.length; i++) {
			String string = libs[i];
			valueList.add(string);
		}
		outputMacros.put("LIBS", valueList);	//$NON-NLS-1$
		
		// Add the extra user-specified objects
		valueList = new ArrayList();
		String[] userObjs = info.getUserObjectsForConfiguration(buildTargetExt);
		for (int i = 0; i < userObjs.length; i++) {
			String string = userObjs[i];
			valueList.add(string);
		}
		outputMacros.put("USER_OBJS", valueList);	//$NON-NLS-1$
		
		//  Write every macro to the file
		Iterator iterator = outputMacros.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry entry = (Map.Entry)iterator.next();
 			macroBuffer.append((String)entry.getKey() + " :=");	//$NON-NLS-1$
			valueList = (List)entry.getValue();
			Iterator valueIter = valueList.iterator();
			while (valueIter.hasNext()) {
	 			macroBuffer.append(WHITESPACE + (String)valueIter.next());
			}
			if (iterator.hasNext()) macroBuffer.append(NEWLINE + NEWLINE);
		}
 
 		// For now, just save the buffer that was populated when the rules were created
		Util.save(macroBuffer, fileHandle);

	}

	/* (non-Javadoc)
	 * @param fileHandle
	 * @throws CoreException
	 */
	protected void populateSourcesMakefile(IFile fileHandle) throws CoreException {
		// Add the comment
		StringBuffer buffer = addDefaultHeader();

		// Determine the set of macros
 		HashSet handledInputExtensions = new HashSet();
		String buildMacro;
		for (int i=0; i<buildTools.length; i++) {
			// Add the known sources macros
 			String[] extensionsList = buildTools[i].getAllInputExtensions();
 			for (int j=0; j<extensionsList.length; j++) {
 				// create a macro of the form "EXTENSION_SRCS :="
 				String extensionName = extensionsList[j];
				if(//!getOutputExtensions().contains(extensionName) && 
				   !handledInputExtensions.contains(extensionName)) {
 					handledInputExtensions.add(extensionName);
 					buildMacro = getSourceMacroName(extensionName).toString();
					if (!buildSrcVars.containsKey(buildMacro)) {
						buildSrcVars.put(buildMacro, new ArrayList());
					}
 				}
 			}
			// Add the specified output build variables
			IOutputType[] outTypes = buildTools[i].getOutputTypes();
			if (outTypes != null && outTypes.length > 0) {
				for (int j=0; j<outTypes.length; j++) {
					buildMacro = outTypes[j].getBuildVariable();
					if (!buildOutVars.containsKey(buildMacro)) {
						buildOutVars.put(buildMacro, new ArrayList());
					}
				}
			} else {
				// For support of pre-CDT 3.0 integrations.
				buildMacro = OBJS_MACRO;	//$NON-NLS-1$
				if (!buildOutVars.containsKey(buildMacro)) {
					buildOutVars.put(buildMacro, new ArrayList());
				}
			}
			//  Always add DEPS...
			buildMacro = DEPS_MACRO;	//$NON-NLS-1$
			if (!buildOutVars.containsKey(buildMacro)) {
				buildOutVars.put(buildMacro, new ArrayList());
			}
		}
		
		// Add the macros to the makefile
		Iterator iterator = buildSrcVars.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry entry = (Map.Entry)iterator.next();
			String macroName = (String)entry.getKey();
			buffer.append(macroName + WHITESPACE + ":=" + WHITESPACE + NEWLINE);	//$NON-NLS-1$
		}
		iterator = buildOutVars.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry entry = (Map.Entry)iterator.next();
			String macroName = (String)entry.getKey();
			buffer.append(macroName + WHITESPACE + ":=" + WHITESPACE + NEWLINE);	//$NON-NLS-1$
		}
 		
		// Add a list of subdirectories to the makefile
		buffer.append(NEWLINE + addSubdirectories());
		
		// Save the file
		Util.save(buffer, fileHandle);
	}

	/*  (non-Javadoc)
	 * Create the entire contents of the makefile.
	 * 
	 * @param fileHandle The file to place the contents in.
	 * @param rebuild FLag signalling that the user is doing a full rebuild
	 * @throws CoreException
	 */
	protected void populateTopMakefile(IFile fileHandle, boolean rebuild) throws CoreException {
		StringBuffer buffer = new StringBuffer();
		
		// Add the header
		buffer.append(addTopHeader());
		
		// Add the macro definitions
		buffer.append(addMacros());

		// List to collect needed build output variables
		List outputVarsAdditionsList = new ArrayList();

		// Determine target rules
		StringBuffer targetRules = addTargets(outputVarsAdditionsList, rebuild);
		
		// Add outputMacros that were added to by the target rules
		buffer.append(writeTopAdditionMacros(outputVarsAdditionsList, getTopBuildOutputVars()));
		
		// Add target rules
		buffer.append(targetRules);

		// Save the file
		Util.save(buffer, fileHandle);
	}

	
	/*************************************************************************
	 *   M A I N (makefile)   M A K E F I L E   M E T H O D S 
	 ************************************************************************/

	/* (non-Javadoc)
	 * Answers a <code>StringBuffer</code> containing the comment(s) 
	 * for the top-level makefile.
	 */
	protected StringBuffer addTopHeader() {
		return addDefaultHeader();
	}
	
	/* (non-javadoc)
	 */
	private StringBuffer addMacros() {
		StringBuffer buffer = new StringBuffer();
		
		// Add the ROOT macro
		buffer.append("ROOT := .." + NEWLINE); //$NON-NLS-1$
		buffer.append(NEWLINE);
		
		// include makefile.init supplementary makefile
		buffer.append("-include $(ROOT)" + SEPARATOR + MAKEFILE_INIT + NEWLINE); //$NON-NLS-1$
		buffer.append(NEWLINE);

		// Get the clean command from the build model
		buffer.append("RM := "); //$NON-NLS-1$
		buffer.append(info.getCleanCommand() + NEWLINE);
		buffer.append(NEWLINE);
		
		// Now add the source providers
		buffer.append(COMMENT_SYMBOL + WHITESPACE + ManagedMakeMessages.getResourceString(SRC_LISTS) + NEWLINE);
		buffer.append("-include sources.mk" + NEWLINE); //$NON-NLS-1$
		buffer.append("-include $(SUBDIRS:%=%/subdir.mk)" + NEWLINE); //$NON-NLS-1$
		buffer.append("-include objects.mk" + NEWLINE); //$NON-NLS-1$
		// Include DEPS makefiles if non-empty
		buffer.append("ifneq ($(strip $(" + DEPS_MACRO + ")),)" + NEWLINE); //$NON-NLS-1$ //$NON-NLS-2$
		buffer.append("-include $(" + DEPS_MACRO + ")" + NEWLINE); //$NON-NLS-1$ //$NON-NLS-2$
		buffer.append("endif" + NEWLINE + NEWLINE); //$NON-NLS-1$
		// Include makefile.defs supplemental makefile
		buffer.append("-include $(ROOT)" + SEPARATOR + MAKEFILE_DEFS + NEWLINE); //$NON-NLS-1$
		
		
		return (buffer.append(NEWLINE));
	}

	/* (non-javadoc)
	 * Answers a <code>StringBuffer</code> containing all of the required targets to
	 * properly build the project.
	 * 
	 * @param outputVarsAdditionsList  list to add needed build output variables to
	 * @param rebuild
	 * @return StringBuffer
	 */
	private StringBuffer addTargets(List outputVarsAdditionsList, boolean rebuild) {
		StringBuffer buffer = new StringBuffer();

		IConfiguration config = info.getDefaultConfiguration();

		// Assemble the information needed to generate the targets
		String prebuildStep = info.getPrebuildStep();
		try{
			//try to resolve the build macros in the prebuild step
			prebuildStep = ManagedBuildManager.getBuildMacroProvider().resolveValueToMakefileFormat(
					prebuildStep,
					EMPTY_STRING,
					WHITESPACE,
					IBuildMacroProvider.CONTEXT_CONFIGURATION,
					config);
		} catch (BuildMacroException e){
		}
		prebuildStep = prebuildStep.trim(); // Remove leading and trailing whitespace (and control characters)

		String postbuildStep = info.getPostbuildStep();
		try{
			//try to resolve the build macros in the postbuild step
			postbuildStep = ManagedBuildManager.getBuildMacroProvider().resolveValueToMakefileFormat(
					postbuildStep,
					EMPTY_STRING,
					WHITESPACE,
					IBuildMacroProvider.CONTEXT_CONFIGURATION,
					config);
				
		} catch (BuildMacroException e){
		}
		postbuildStep = postbuildStep.trim(); // Remove leading and trailing whitespace (and control characters)
		String preannouncebuildStep = info.getPreannouncebuildStep();
		String postannouncebuildStep = info.getPostannouncebuildStep();
		String targets = rebuild ? "clean all" : "all"; //$NON-NLS-1$ //$NON-NLS-2$

		ITool targetTool = config.getTargetTool();
		if (targetTool == null) {
			targetTool = info.getToolFromOutputExtension(buildTargetExt);
		}

		// Get all the projects the build target depends on
		IProject[] refdProjects = null;
		try {
			refdProjects = project.getReferencedProjects();
		} catch (CoreException e) {
			// There are 2 exceptions; the project does not exist or it is not open
			// and neither conditions apply if we are building for it ....
		}
		
        // If a prebuild step exists, redefine the all target to be
		// all: {pre-build} main-build
		// and then reset the "traditional" all target to main-build
		// This will allow something meaningful to happen if the generated
		// makefile is
		// extracted and run standalone via "make all"
		// 
		String defaultTarget = "all:"; //$NON-NLS-1$
		if (prebuildStep.length() > 0) {

			// Add the comment for the "All" target
			buffer.append(COMMENT_SYMBOL + WHITESPACE + ManagedMakeMessages.getResourceString(ALL_TARGET) + NEWLINE);

			buffer.append(defaultTarget + WHITESPACE);
			buffer.append(PREBUILD + WHITESPACE);

			// Reset defaultTarget for now and for subsequent use, below
			defaultTarget = MAINBUILD;
			buffer.append(defaultTarget);

			// Update the defaultTarget, main-build, by adding a colon, which is
			// needed below
			defaultTarget = defaultTarget.concat(COLON);
			buffer.append(NEWLINE + NEWLINE);

			// Add the comment for the "main-build" target
			buffer.append(COMMENT_SYMBOL + WHITESPACE + ManagedMakeMessages.getResourceString(MAINBUILD_TARGET) + NEWLINE);					
		}
		else
			// Add the comment for the "All" target
			buffer.append(COMMENT_SYMBOL + WHITESPACE + ManagedMakeMessages.getResourceString(ALL_TARGET) + NEWLINE);

		// Write out the all target first in case someone just runs make
		// all: <target_name> or mainbuild: <target_name>
               
		String outputPrefix = EMPTY_STRING;
		if (targetTool != null) { 		
			outputPrefix = targetTool.getOutputPrefix();
		}
		buffer.append(defaultTarget + WHITESPACE + outputPrefix + buildTargetName);
		if (buildTargetExt.length() > 0) {
			buffer.append(DOT + buildTargetExt);
		}
		
		// Add the Secondary Outputs to the all target, if any
		IOutputType[] secondaryOutputs = config.getToolChain().getSecondaryOutputs();
		if (secondaryOutputs.length > 0) {
			buffer.append(WHITESPACE + SECONDARY_OUTPUTS);
		}

		buffer.append(NEWLINE + NEWLINE);

		/*
		 * The build target may depend on other projects in the workspace. These
		 * are captured in the deps target: deps: <cd <Proj_Dep_1/build_dir>;
		 * $(MAKE) [clean all | all]>
		 */
		Vector managedProjectOutputs = new Vector(refdProjects.length);
		if (refdProjects.length > 0) {
			boolean addDeps = true;
			if (refdProjects != null) {
				for (int i = 0; i < refdProjects.length; i++) {
					IProject dep = refdProjects[i];
					if (!dep.exists()) continue;
					if (addDeps) {
						buffer.append("dependents:" + NEWLINE); //$NON-NLS-1$						
						addDeps = false;
					}
					String buildDir = dep.getLocation().toString();
					String depTargets = targets;
					if (ManagedBuildManager.manages(dep)) {
						// Add the current configuration to the makefile path
						IManagedBuildInfo depInfo = ManagedBuildManager.getBuildInfo(dep);
						buildDir += SEPARATOR + depInfo.getConfigurationName();
					
						// Extract the build artifact to add to the dependency list
						String depTarget = depInfo.getBuildArtifactName();
						String depExt = depInfo.getBuildArtifactExtension();
						
						try{
							//try to resolve the build macros in the artifact extension
							depExt = ManagedBuildManager.getBuildMacroProvider().resolveValueToMakefileFormat(
									depExt,
									"", //$NON-NLS-1$
									" ", //$NON-NLS-1$
									IBuildMacroProvider.CONTEXT_CONFIGURATION,
									info.getDefaultConfiguration());
						} catch (BuildMacroException e){
						}
						
						try{
							//try to resolve the build macros in the artifact name
							String resolved = ManagedBuildManager.getBuildMacroProvider().resolveValueToMakefileFormat(
									depTarget,
									"", //$NON-NLS-1$
									" ", //$NON-NLS-1$
									IBuildMacroProvider.CONTEXT_CONFIGURATION,
									info.getDefaultConfiguration());
							if((resolved = resolved.trim()).length() > 0)
								depTarget = resolved;
						} catch (BuildMacroException e){
						}

						String depPrefix = depInfo.getOutputPrefix(depExt);
						if (depInfo.needsRebuild()) {
							depTargets = "clean all"; //$NON-NLS-1$
						}
						String dependency = buildDir + SEPARATOR + depPrefix + depTarget;
						if (depExt.length() > 0) {
							dependency += DOT + depExt;
						}
						dependency = escapeWhitespaces(dependency);
						managedProjectOutputs.add(dependency);
					}
					buffer.append(TAB + "-cd" + WHITESPACE + escapeWhitespaces(buildDir) + WHITESPACE + LOGICAL_AND + WHITESPACE + "$(MAKE) " + depTargets + NEWLINE); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
			buffer.append(NEWLINE);
		}
		
		// Add the targets tool rules
		buffer.append(addTargetsRules(targetTool, 
				outputVarsAdditionsList, managedProjectOutputs, (postbuildStep.length() > 0)));

		// Add the prebuild step target, if specified
		if (prebuildStep.length() > 0) {
			buffer.append(PREBUILD + COLON + NEWLINE);
			if (preannouncebuildStep.length() > 0) {
				buffer.append(TAB + DASH + AT + ECHO + WHITESPACE
						+ SINGLE_QUOTE + preannouncebuildStep + SINGLE_QUOTE
						+ NEWLINE);
			}
			buffer.append(TAB + DASH + prebuildStep + NEWLINE);
			buffer.append(TAB + DASH + AT + ECHO + WHITESPACE + SINGLE_QUOTE
					+ WHITESPACE + SINGLE_QUOTE + NEWLINE + NEWLINE);
		}

		// Add the postbuild step, if specified
		if (postbuildStep.length() > 0) {
			buffer.append(POSTBUILD + COLON + NEWLINE);
			if (postannouncebuildStep.length() > 0) {
				buffer.append(TAB + DASH + AT + ECHO + WHITESPACE
						+ SINGLE_QUOTE + postannouncebuildStep + SINGLE_QUOTE
						+ NEWLINE);
			}
			buffer.append(TAB + DASH + postbuildStep + NEWLINE);
			buffer.append(TAB + DASH + AT + ECHO + WHITESPACE + SINGLE_QUOTE
					+ WHITESPACE + SINGLE_QUOTE + NEWLINE + NEWLINE);
		} 

		// Add the Secondary Outputs target, if needed
		if (secondaryOutputs.length > 0) {
			buffer.append(SECONDARY_OUTPUTS + COLON);
			Vector outs2 = calculateSecondaryOutputs(secondaryOutputs);
			for (int i=0; i<outs2.size(); i++) {
				buffer.append(WHITESPACE + "$(" + (String)outs2.get(i) + ")"); //$NON-NLS-1$ //$NON-NLS-2$
			}
			buffer.append(NEWLINE + NEWLINE); 
		}

		// Add all the needed dummy and phony targets
		buffer.append(".PHONY: all clean dependents" + NEWLINE); //$NON-NLS-1$
		buffer.append(".SECONDARY:"); //$NON-NLS-1$
		if (prebuildStep.length() > 0) {
			buffer.append(WHITESPACE + MAINBUILD + WHITESPACE + PREBUILD);
		}
		if (postbuildStep.length() > 0) {
			buffer.append(WHITESPACE + POSTBUILD);
		}
		buffer.append(NEWLINE); 
		Iterator refIter = managedProjectOutputs.listIterator();
		while(refIter.hasNext()) {
			buffer.append((String)refIter.next() + COLON + NEWLINE);
		}
		buffer.append(NEWLINE);
		
		// Include makefile.targets supplemental makefile
		buffer.append("-include $(ROOT)" + SEPARATOR + MAKEFILE_TARGETS + NEWLINE); //$NON-NLS-1$

		return buffer;
 	}

	/* (non-javadoc)
	 * Returns the targets rules.  The targets make file (top makefile) contains:
	 *  1  the rule for the final target tool
	 *  2  the rules for all of the tools that use multipleOfType in their primary input type
	 *  3  the rules for all tools that use the output of #2 tools
	 * 
	 * @param outputVarsAdditionsList  list to add needed build output variables to
	 * @param managedProjectOutputs  Other projects in the workspace that this project depends upon
	 * @return StringBuffer
	 */
	private StringBuffer addTargetsRules(ITool targetTool,
			List outputVarsAdditionsList, Vector managedProjectOutputs, boolean postbuildStep) {
		StringBuffer buffer = new StringBuffer();
		// Add the comment
		buffer.append(COMMENT_SYMBOL + WHITESPACE + ManagedMakeMessages.getResourceString(BUILD_TOP) + NEWLINE);

		//  Get the target tool and generate the rule
		if (targetTool != null) { 		
			if (addRuleForTool(targetTool, buffer, true, buildTargetName, buildTargetExt, 
					outputVarsAdditionsList, managedProjectOutputs, postbuildStep)) {
				//  Mark the target tool as processed
				for (int i=0; i<buildTools.length; i++) {
					if (targetTool == buildTools[i]) {
						buildToolsUsed[i] = true;
					}
				}
			}
		} else {
			buffer.append(TAB + AT + ECHO + WHITESPACE + SINGLE_QUOTE + MESSAGE_NO_TARGET_TOOL + WHITESPACE + OUT_MACRO + SINGLE_QUOTE + NEWLINE);
		}

		//  Generate the rules for all Tools that specify InputType.multipleOfType, and any Tools that
		//  consume the output of those tools.  This does not apply to pre-3.0 integrations, since
		//  the only "multipleOfType" tool is the "target" tool
		for (int i=0; i<buildTools.length; i++) {
			ITool tool = buildTools[i];
			IInputType type = tool.getPrimaryInputType();
			if (type != null && type.getMultipleOfType()) {
				if (!buildToolsUsed[i]) {
					addRuleForTool(tool, buffer, false, null, null, outputVarsAdditionsList, null, false);
					//  Mark the target tool as processed
					buildToolsUsed[i] = true;
					// Look for tools that consume the output
					generateRulesForConsumers(tool, outputVarsAdditionsList, buffer);
				}
			}
		}
			
		// Add the comment
		buffer.append(COMMENT_SYMBOL + WHITESPACE + ManagedMakeMessages.getResourceString(BUILD_TARGETS) + NEWLINE);

		// Always add a clean target
		buffer.append("clean:" + NEWLINE); //$NON-NLS-1$
		buffer.append(TAB + "-$(RM)" + WHITESPACE); //$NON-NLS-1$
		Iterator iterator = buildOutVars.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry entry = (Map.Entry)iterator.next();
			String macroName = (String)entry.getKey();
			buffer.append("$(" + macroName + ")");	//$NON-NLS-1$	//$NON-NLS-2$
		}
		String outputPrefix = EMPTY_STRING;
		if (targetTool != null) { 		
			outputPrefix = targetTool.getOutputPrefix();
		}
		buffer.append(WHITESPACE + outputPrefix + buildTargetName); //$NON-NLS-1$
		if (buildTargetExt.length() > 0) {
			buffer.append(DOT + buildTargetExt);
		}
		buffer.append(NEWLINE);
		buffer.append(TAB + DASH + AT + ECHO + WHITESPACE + SINGLE_QUOTE
				+ WHITESPACE + SINGLE_QUOTE + NEWLINE + NEWLINE);
			
		return buffer;
	}
	
	/* (non-Javadoc)
	 * Create the rule
	 * 
	 * @param tool
	 * @param buffer  Buffer to add makefile rules to
	 * @param bTargetTool True if this is the target tool
	 * @param targetName  If this is the "targetTool", the target file name, else <code>null</code>
	 * @param targetName  If this is the "targetTool", the target file extension, else <code>null</code>
	 * @param outputVarsAdditionsList  list to add needed build output variables to
	 * @param managedProjectOutputs  Other projects in the workspace that this project depends upon
	 * @param bPostBuildStep  Emit post-build step invocation
	 */
	protected boolean addRuleForTool(ITool tool, StringBuffer buffer, boolean bTargetTool, String targetName, String targetExt, 
			List outputVarsAdditionsList, Vector managedProjectOutputs, boolean bEmitPostBuildStepCall) {
		
		//  Get the tool's inputs and outputs
		Vector inputs = new Vector();
		Vector dependencies = new Vector();
		Vector outputs = new Vector();
		Vector enumeratedPrimaryOutputs = new Vector();
		Vector enumeratedSecondaryOutputs = new Vector();
		Vector outputVariables = new Vector();
		String outputPrefix = EMPTY_STRING;

		if (!getToolInputsOutputs(tool, inputs, dependencies, outputs, 
				enumeratedPrimaryOutputs, enumeratedSecondaryOutputs,
				outputVariables, bTargetTool, managedProjectOutputs)) {
			return false;
		}

		//  If we have no primary output, make all of the secondary outputs the primary output
		if (enumeratedPrimaryOutputs.size() == 0) {
			enumeratedPrimaryOutputs = enumeratedSecondaryOutputs;
			enumeratedSecondaryOutputs.clear();
		}
		
		//  Add the output variables for this tool to our list
		outputVarsAdditionsList.addAll(outputVariables);
		
		//  Create the build rule 
		String buildRule = EMPTY_STRING;
		String outflag = tool.getOutputFlag();
		
		String primaryOutputs = EMPTY_STRING;
		boolean first = true;
		for (int i=0; i<enumeratedPrimaryOutputs.size(); i++) {
			String output = (String)enumeratedPrimaryOutputs.get(i);
			if (!first) primaryOutputs += WHITESPACE;
			first = false;
			primaryOutputs += output;
		}

		buildRule += (primaryOutputs + COLON + WHITESPACE);
		
		first = true;
		for (int i=0; i<dependencies.size(); i++) {
			String input = (String)dependencies.get(i);
			if (!first) buildRule += WHITESPACE;
			first = false;
			buildRule += input;
		}
				
		// We can't have duplicates in a makefile
		if (getRuleList().contains(buildRule)) {
			return true;
		}
		else {
			getRuleList().add(buildRule);
		}
		buffer.append(buildRule + NEWLINE);
		if (bTargetTool) {
			buffer.append(TAB + AT + ECHO + WHITESPACE + SINGLE_QUOTE + MESSAGE_START_BUILD + WHITESPACE + OUT_MACRO + SINGLE_QUOTE + NEWLINE);
		}
		buffer.append(TAB + AT + ECHO + WHITESPACE + SINGLE_QUOTE + tool.getAnnouncement() + SINGLE_QUOTE + NEWLINE);
		
		// Get the command line for this tool invocation
		String[] flags;
		try { 
			flags = tool.getToolCommandFlags(null,null);
		} catch( BuildException ex ) {
			// TODO  report error
			flags = EMPTY_STRING_ARRAY;
		}
		String command = tool.getToolCommand();
		try{
			//try to resolve the build macros in the tool command
			String resolvedCommand = ManagedBuildManager.getBuildMacroProvider().resolveValueToMakefileFormat(command,
					EMPTY_STRING,
					WHITESPACE,
					IBuildMacroProvider.CONTEXT_FILE,
					new FileContextData(null,null,null,info.getDefaultConfiguration().getToolChain()));
			if((resolvedCommand = resolvedCommand.trim()).length() > 0)
				command = resolvedCommand;
				
		} catch (BuildMacroException e){
		}
		String[] cmdInputs = (String[])inputs.toArray(new String[inputs.size()]);
		IManagedCommandLineGenerator gen = tool.getCommandLineGenerator();
		IManagedCommandLineInfo cmdLInfo = gen.generateCommandLineInfo( tool, command, 
				flags, outflag, outputPrefix, primaryOutputs, cmdInputs, tool.getCommandLinePattern() );
		// The command to build
		String buildCmd = null;
		if( cmdLInfo == null ) {
			String toolFlags;
			try { 
				toolFlags = tool.getToolCommandFlagsString(null,null);
			} catch( BuildException ex ) {
				// TODO report error
				toolFlags = EMPTY_STRING;
			}
			buildCmd = command + WHITESPACE + toolFlags + WHITESPACE + outflag + WHITESPACE + outputPrefix + primaryOutputs + WHITESPACE + IN_MACRO;
		}
		else buildCmd = cmdLInfo.getCommandLine();
		buffer.append(TAB + AT + ECHO + WHITESPACE + buildCmd + NEWLINE);
		buffer.append(TAB + AT + buildCmd);

		// TODO
		// NOTE WELL:  Dependency file generation is not handled for this type of Tool
		 
		// Echo finished message
		buffer.append(NEWLINE);
		if (bTargetTool) {
			buffer.append(TAB + AT + ECHO + WHITESPACE + SINGLE_QUOTE + MESSAGE_FINISH_BUILD + WHITESPACE + OUT_MACRO + SINGLE_QUOTE + NEWLINE);
		} else {
			buffer.append(TAB + AT + ECHO + WHITESPACE + SINGLE_QUOTE + MESSAGE_FINISH_FILE + WHITESPACE + OUT_MACRO + SINGLE_QUOTE + NEWLINE);
		}
		buffer.append(TAB + AT + ECHO + WHITESPACE + SINGLE_QUOTE + WHITESPACE + SINGLE_QUOTE + NEWLINE);				
		
		// If there is a post build step, then add a recursive invocation of MAKE to invoke it after the main build
	    // Note that $(MAKE) will instantiate in the recusive invocation to the make command that was used to invoke
	    // the makefile originally 
	    if (bEmitPostBuildStepCall) {
	        buffer.append(TAB + MAKE + WHITESPACE + NO_PRINT_DIR + WHITESPACE + POSTBUILD + NEWLINE + NEWLINE);       
	    }
	    else {
			// Just emit a blank line
			buffer.append(NEWLINE);
	    }
		
		// If we have secondary outputs, output dependency rules without commands
		if (enumeratedSecondaryOutputs.size() > 0) {
			String primaryOutput = (String)enumeratedPrimaryOutputs.get(0);
			for (int i=0; i<enumeratedSecondaryOutputs.size(); i++) {
				String output = (String)enumeratedSecondaryOutputs.get(0);
				buffer.append(output + COLON + WHITESPACE + primaryOutput + NEWLINE);
			}
			buffer.append(NEWLINE);
		}
		return true;
	}

	/* (non-Javadoc)
	 * @param outputVarsAdditionsList  list to add needed build output variables to
	 * @param buffer  buffer to add rules to
	 */
	 private void generateRulesForConsumers(ITool generatingTool, List outputVarsAdditionsList, StringBuffer buffer) {
		//  Generate a build rule for any tool that consumes the output of this tool
		IOutputType[] outTypes = generatingTool.getOutputTypes();
		for (int i=0; i<outTypes.length; i++) {
			String[] outExts = outTypes[i].getOutputExtensions(generatingTool);
			String outVariable = outTypes[i].getBuildVariable();
			if (outExts != null) {
				for (int j=0; j<outExts.length; j++) {
					for (int k=0; k<buildTools.length; k++) {
						ITool tool = buildTools[k];
						if (!buildToolsUsed[k]) {
							// Also has to match build variables if specified
							IInputType inType = tool.getInputType(outExts[j]);
							if (inType != null) {
								String inVariable = inType.getBuildVariable();
								if ((outVariable == null && inVariable == null) ||
								    (outVariable != null && inVariable != null &&
									 outVariable.equals(inVariable))) {
									if (addRuleForTool(buildTools[k], buffer, false, null, null, 
											outputVarsAdditionsList, null, false)) {
										buildToolsUsed[k] = true;
										// Look for tools that consume the output
										generateRulesForConsumers(buildTools[k], outputVarsAdditionsList, buffer);
									}
								}
							}
						}
					}
				}
			}
		}
	}

	protected boolean getToolInputsOutputs(ITool tool, 
			Vector inputs, Vector dependencies, Vector outputs, 
			Vector enumeratedPrimaryOutputs, Vector enumeratedSecondaryOutputs, 
			Vector outputVariables,	boolean bTargetTool, Vector managedProjectOutputs) {
		
		//  Get the information regarding the tool's inputs and outputs from the objects
		//  created by calculateToolInputsOutputs
		IManagedBuildGnuToolInfo toolInfo = null;
		for (int i=0; i<buildTools.length; i++) {
			if (tool == buildTools[i]) {
				toolInfo = gnuToolInfos[i];
				break;
			}
		}
		if (toolInfo == null) return false;
		
		//  Populate the output Vectors
		inputs.addAll(toolInfo.getCommandInputs());
		outputs.addAll(toolInfo.getCommandOutputs());
		enumeratedPrimaryOutputs.addAll(toolInfo.getEnumeratedPrimaryOutputs());
		enumeratedSecondaryOutputs.addAll(toolInfo.getEnumeratedSecondaryOutputs());
		outputVariables.addAll(toolInfo.getOutputVariables());
		dependencies.addAll(toolInfo.getCommandDependencies());
		
		if (bTargetTool && managedProjectOutputs != null) {
			Iterator refIter = managedProjectOutputs.listIterator();
			while (refIter.hasNext()) {
				dependencies.add((String)refIter.next());
			}
		}
		return true;
	}

	/* (non-Javadoc)
	 * @param fileHandle
	 * @throws CoreException
	 */
	protected Vector calculateSecondaryOutputs(IOutputType[] secondaryOutputs) {
		Vector buildVars = new Vector();
		for (int i=0; i<buildTools.length; i++) {
			// Add the specified output build variables
			IOutputType[] outTypes = buildTools[i].getOutputTypes();
			if (outTypes != null && outTypes.length > 0) {
				for (int j=0; j<outTypes.length; j++) {
					IOutputType outType = outTypes[j];
					//  Is this one of the secondary outputs?
					//  Look for an outputType with this ID, or one with a superclass with this id
					thisType: 
					for (int k = 0; k < secondaryOutputs.length; k++) {
						IOutputType matchType = outType;
						do {
							if (matchType.getId().equals(secondaryOutputs[k].getId())) {
								buildVars.add(outType.getBuildVariable());
								break thisType;
							}		
							matchType = matchType.getSuperClass();
						} while (matchType != null);
					}
				}
			}
		}
		return buildVars;
	}
	
	protected boolean isSecondaryOutputVar(IOutputType[] secondaryOutputs, String varName) {
		for (int i=0; i<buildTools.length; i++) {
			// Add the specified output build variables
			IOutputType[] outTypes = buildTools[i].getOutputTypes();
			if (outTypes != null && outTypes.length > 0) {
				for (int j=0; j<outTypes.length; j++) {
					IOutputType outType = outTypes[j];
					//  Is this one of the secondary outputs?
					//  Look for an outputType with this ID, or one with a superclass with this id
					thisType: 
					for (int k = 0; k < secondaryOutputs.length; k++) {
						IOutputType matchType = outType;
						do {
							if (matchType.getId().equals(secondaryOutputs[k].getId())) {
								if (outType.getBuildVariable().equals(varName)) {
									return true;
								}
							}		
							matchType = matchType.getSuperClass();
						} while (matchType != null);
					}
				}
			}
		}
		return false;
	}
	
	/*************************************************************************
	 *   S O U R C E S (sources.mk)   M A K E F I L E   M E T H O D S 
	 ************************************************************************/
	
	/* (non-javadoc)
	 * @return
	 */
	private StringBuffer addSubdirectories() {
		StringBuffer buffer = new StringBuffer();
		// Add the comment
		buffer.append(COMMENT_SYMBOL + WHITESPACE + ManagedMakeMessages.getResourceString(MOD_LIST) + NEWLINE);
		
		buffer.append("SUBDIRS := " + LINEBREAK); //$NON-NLS-1$
		
		// Get all the module names
		Iterator iter = getSubdirList().listIterator();
		while (iter.hasNext()) {
			IContainer container = (IContainer) iter.next();
			updateMonitor(ManagedMakeMessages.getFormattedString("MakefileGenerator.message.adding.source.folder", container.getFullPath().toString()));	//$NON-NLS-1$
			// Check the special case where the module is the project root
			if (container.getFullPath() == project.getFullPath()) {
				buffer.append(DOT + WHITESPACE + LINEBREAK);
			} else {
				IPath path = container.getProjectRelativePath();
				buffer.append(path.toString() +  WHITESPACE + LINEBREAK);
			}
		}

		buffer.append(NEWLINE);
		return buffer;
	}

	
	/*************************************************************************
	 *   F R A G M E N T (subdir.mk)   M A K E F I L E   M E T H O D S 
	 ************************************************************************/
	
	/* (non-Javadoc)
	 * Returns a <code>StringBuffer</code> containing the comment(s) 
	 * for a fragment makefile (subdir.mk).
	 */
	protected StringBuffer addFragmentMakefileHeader() {
		return addDefaultHeader();
	}

	/* (non-javadoc)
	 * Returns a <code>StringBuffer</code> containing makefile text for all of the sources 
	 * contributed by a container (project directory/subdirectory) to the fragement makefile
	 * 
	 * @param module  project resource directory/subdirectory
	 * @return StringBuffer  generated text for the fragement makefile
	 */
	protected StringBuffer addSources(IContainer module) throws CoreException {
		// Calculate the new directory relative to the build output
		IPath moduleRelativePath = module.getProjectRelativePath();
		String relativePath = moduleRelativePath.toString();
		relativePath += relativePath.length() == 0 ? "" : SEPARATOR;  //$NON-NLS-1$
 		relativePath = escapeWhitespaces(relativePath);
 		
 		// For build macros in the configuration, create a map which will map them  
		// to a string which holds its list of sources.
  		LinkedHashMap buildVarToRuleStringMap = new LinkedHashMap();
 		
 		// Add statements that add the source files in this folder, 
		// and generated source files, and generated dependency files 
		// to the build macros
		Iterator iterator = buildSrcVars.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry entry = (Map.Entry)iterator.next();
			String macroName = (String)entry.getKey();
			addMacroAdditionPrefix(buildVarToRuleStringMap, macroName, null, false);
		}
		iterator = buildOutVars.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry entry = (Map.Entry)iterator.next();
			String macroName = (String)entry.getKey();
			addMacroAdditionPrefix(buildVarToRuleStringMap, macroName, "./" + relativePath, false);	  //$NON-NLS-1$
		}
		// Create an entry for the DEPS macro
		addMacroAdditionPrefix(buildVarToRuleStringMap, DEPS_MACRO, "./" + relativePath, true);	  //$NON-NLS-1$
 		
 		// String buffers
 		StringBuffer buffer = new StringBuffer();	// Return buffer
 		StringBuffer ruleBuffer = new StringBuffer(COMMENT_SYMBOL + WHITESPACE + ManagedMakeMessages.getResourceString(MOD_RULES) + NEWLINE);

		// Visit the resources in this folder and add each one to a sources macro, and generate a build rule, if appropriate
		IResource[] resources = module.members();

		IResourceConfiguration resConfig; 
		IFolder folder = project.getFolder(info.getConfigurationName());
		
		for (int i = 0; i < resources.length; i++) {
			IResource resource = resources[i];
			if (resource.getType() == IResource.FILE) {
				// Check whether this resource is excluded from build
				resConfig = config.getResourceConfiguration(resource.getFullPath().toString());
				if( (resConfig != null) && (resConfig.isExcluded()) )
					continue;
				addFragmentMakefileEntriesForSource(buildVarToRuleStringMap, ruleBuffer, 
						folder, relativePath, resource, resource.getLocation(), resConfig, null, false);
			}
		}
							
		// Write out the macro addition entries to the buffer
		buffer.append(writeAdditionMacros(buildVarToRuleStringMap));
		return buffer.append(ruleBuffer + NEWLINE);
	}

	/* (non-Javadoc
	 * Adds the entries for a particular source file to the fragment makefile
	 * 
	 * @param buildVarToRuleStringMap  map of build variable names to the list of files assigned to the variable
	 * @param ruleBuffer  buffer to add generated nmakefile text to
	 * @param folder  the top level build output directory
	 * @param relativePath  build output directory relative path of the current output directory
	 * @param resource  the source file for this invocation of the tool - this may be null for a generated output
	 * @param sourceLocation  the full path of the source
	 * @param resConfig  the IResourceConfiguration associated with this file or null
	 * @param varName  the build variable to add this invocation's outputs to
	 *                   if <code>null</code>, use the file extension to find the name
	 * @param generatedSource  if <code>true</code>, this file was generated by another tool in the tool-chain
	 */
	protected void addFragmentMakefileEntriesForSource (LinkedHashMap buildVarToRuleStringMap, StringBuffer ruleBuffer, 
			IFolder folder, String relativePath, IResource resource, IPath sourceLocation, IResourceConfiguration resConfig, 
			String varName, boolean generatedSource) {
		//  Determine which tool, if any, builds files with this extension
		String ext = sourceLocation.getFileExtension();
		ITool tool = null;

		//  Use the tool from the resource configuration if there is one
		if (resConfig != null) {
			ITool[] tools = resConfig.getToolsToInvoke();
			if (tools != null && tools.length > 0) {
				tool = tools[0];
			}
		}
		for (int j=0; j<buildTools.length; j++) {
			if (buildTools[j].buildsFileType(ext)) {
				if (tool == null) {
					tool = buildTools[j];
				}
				// look for the extension in the map
				if (varName == null) {
					varName = getSourceMacroName(ext).toString();
					//  Add the resource to the list of all resources associated with a variable.
					List varList = (List)buildSrcVars.get(varName);
					//  Since we don't know how these files will be used, we store them using a "location"
					//  path rather than a relative path
					varList.add(sourceLocation);
				} else {
					//  Add the resource to the list of all resources associated with a variable.
					List varList = (List)buildOutVars.get(varName);
					if (varList != null) {
						//  Since we don't know how these files will be used, we store them using a "location"
						//  path rather than a relative path
						varList.add(sourceLocation);
					}
				}
				if (!buildVarToRuleStringMap.containsKey(varName)) {
					//  TODO - is this an error?
				} else {
					//  Add the resource name to the makefile line that adds resources to the build variable
					addMacroAdditionFile(buildVarToRuleStringMap, varName, relativePath, sourceLocation, generatedSource);
				}
				break;
			}
		}
				
		if (tool != null) {
			//  Generate the rule to build this source file
			IInputType primaryInputType = tool.getPrimaryInputType();
			IInputType inputType = tool.getInputType(ext);
			if ((primaryInputType != null && !primaryInputType.getMultipleOfType()) ||
				(inputType == null && !(tool == info.getToolFromOutputExtension(buildTargetExt))))	{
				
				// Try to add the rule for the file
				StringBuffer generatedDepFile = new StringBuffer();
				Vector generatedOutputs = new Vector();
				addRuleForSource(relativePath, ruleBuffer, resource, sourceLocation, resConfig, generatedSource, generatedDepFile, generatedOutputs);
				
				// If the rule generates a dependency file, add the file to the DEPS variable
				if (generatedDepFile.length() > 0) {
					addMacroAdditionFile(buildVarToRuleStringMap, DEPS_MACRO, generatedDepFile.toString());
				}
			
				// If the generated outputs of this tool are input to another tool, 
				// 1. add the output to the appropriate macro 
				// 2. If the tool does not have multipleOfType input, generate the rule.
				
				IOutputType outType = tool.getPrimaryOutputType();
				String buildVariable = null;
				if (outType != null) {
					if (tool.getCustomBuildStep()) {
						// TODO: This is somewhat of a hack since a custom build step
						//       tool does not currently define a build variable
						if (generatedOutputs.size() > 0) {
							IPath firstOutput = (IPath)generatedOutputs.get(0);
							String firstExt = firstOutput.getFileExtension();
							for (int j=0; j<buildTools.length; j++) {
								if (buildTools[j].buildsFileType(firstExt)) {
									String bV = buildTools[j].getPrimaryInputType().getBuildVariable();
									if (bV.length() > 0) {
										buildVariable = bV; 
										break;
									}
								}
							}
						}
					} else {
						buildVariable = outType.getBuildVariable();
					}
				} else {
					// For support of pre-CDT 3.0 integrations.
					buildVariable = OBJS_MACRO;
				}
				
				for (int k=0; k<generatedOutputs.size(); k++) {
					IPath generatedOutput;
					if (((IPath)generatedOutputs.get(k)).isAbsolute()) {
						// TODO:  Should we use relative paths when possible (e.g., see MbsMacroSupplier.calculateRelPath)
						generatedOutput = (IPath)generatedOutputs.get(k);
					} else {
						generatedOutput = project.getLocation().append(getBuildWorkingDir()).append((IPath)generatedOutputs.get(k));
					}
					//  If this file has an absolute path, then the generateOutputResource will not be correct
					//  because the file is not under the project.  We use this resource in the calls to the dependency generator
					IResource generateOutputResource = project.getFile(generatedOutput);
					addFragmentMakefileEntriesForSource(buildVarToRuleStringMap, ruleBuffer, 
							folder, relativePath, generateOutputResource, generatedOutput, null, buildVariable, true);
				}						
			}
		} else { 
			//  If this generated output is identified as a secondary output, add the file to the build variable
			if (varName != null) {
				IOutputType[] secondaryOutputs = config.getToolChain().getSecondaryOutputs();
				if (secondaryOutputs.length > 0) {
					if (isSecondaryOutputVar(secondaryOutputs, varName)) {
						addMacroAdditionFile(buildVarToRuleStringMap, varName, relativePath, sourceLocation, generatedSource); 
					}
				}
			}
		}
	}
	
	/* (non-Javadoc)
	 * Create a rule for this source file.  We create a pattern rule if possible.
	 * 
	 * This is an example of a pattern rule:
	 * 
	 * <relative_path>/%.<outputExtension>: $(ROOT)/<relative_path>/%.<inputExtension>
	 * 		@echo 'Building file: $<'
	 * 		@echo 'Invoking tool xxx'
	 * 		@echo <tool> <flags> <output_flag><output_prefix>$@ $<
	 * 		@<tool> <flags> <output_flag><output_prefix>$@ $< && \
	 * 		echo -n $(@:%.o=%.d) ' <relative_path>/' >> $(@:%.o=%.d) && \
	 * 		<tool> -P -MM -MG <flags> $< >> $(@:%.o=%.d)
	 * 		@echo 'Finished building: $<'
	 * 		@echo ' '
	 * 
	 * Note that the macros all come from the build model and are 
	 * resolved to a real command before writing to the module
	 * makefile, so a real command might look something like:
	 * source1/%.o: $(ROOT)/source1/%.cpp
	 * 		@echo 'Building file: $<'
	 * 		@echo 'Invoking tool xxx'
	 * 		@echo g++ -g -O2 -c -I/cygdrive/c/eclipse/workspace/Project/headers -o$@ $<
	 * 		@ g++ -g -O2 -c -I/cygdrive/c/eclipse/workspace/Project/headers -o$@ $< && \
	 * 		echo -n $(@:%.o=%.d) ' source1/' >> $(@:%.o=%.d) && \
	 * 		g++ -P -MM -MG -g -O2 -c -I/cygdrive/c/eclipse/workspace/Project/headers $< >> $(@:%.o=%.d)
	 * 		@echo 'Finished building: $<'
	 * 		@echo ' '
	 * 
	 * @param relativePath  build output directory relative path of the current output directory
	 * @param buffer  buffer to populate with the build rule
	 * @param resource  the source file for this invocation of the tool
	 * @param generatedSource  <code>true</code> if the resource is a generated output
	 * @param sourceLocation  the full path of the source
	 * @param resConfig  the IResourceConfiguration associated with this file or null
	 * @param generatedDepFile  passed in as an empty string; append the dependency file name
	 *                          to it if one is generated by the rule
	 * @param enumeratedOutputs  vector of the filenames that are the output of this rule
	 */
	protected void addRuleForSource(String relativePath, StringBuffer buffer, IResource resource, 
			IPath sourceLocation, IResourceConfiguration resConfig, 
			boolean generatedSource, StringBuffer generatedDepFile, Vector enumeratedOutputs) {
		
		String fileName = sourceLocation.removeFileExtension().lastSegment();
		String inputExtension = sourceLocation.getFileExtension();
		String outputExtension = info.getOutputExtension(inputExtension);

		ITool tool;
		if( resConfig != null) {
			ITool[] tools = resConfig.getToolsToInvoke();
			tool = tools[0];
		} else {
			tool = info.getToolFromInputExtension(inputExtension);
		}

		//  Get the dependency generator associated with this tool and file extension
		IManagedDependencyGenerator depGen = tool.getDependencyGeneratorForExtension(inputExtension);
		boolean doDepGen = (depGen != null && depGen.getCalculatorType() == IManagedDependencyGenerator.TYPE_COMMAND);

		// If the tool creates a dependency file, add it to the list
		if (doDepGen) {
			String depFile =  relativePath + fileName + DOT + DEP_EXT;
			getDependencyMakefiles().add(depFile);
			generatedDepFile.append(fileName + DOT + DEP_EXT);
		}
		
		// Figure out the output paths		
		String OptDotExt = EMPTY_STRING;
		if (outputExtension != null && outputExtension.length() > 0)
	        OptDotExt = DOT + outputExtension; 

		Vector ruleOutputs = new Vector();
		Vector enumeratedPrimaryOutputs = new Vector();
		Vector enumeratedSecondaryOutputs = new Vector();
		calculateOutputsForSource(tool, relativePath, resource, sourceLocation, ruleOutputs, enumeratedPrimaryOutputs, enumeratedSecondaryOutputs);
		enumeratedOutputs.addAll(enumeratedPrimaryOutputs);
		enumeratedOutputs.addAll(enumeratedSecondaryOutputs);
		String primaryOutputName = null;
		if (enumeratedPrimaryOutputs.size() > 0) {
			primaryOutputName = ((IPath)enumeratedPrimaryOutputs.get(0)).toString();
		} else {
			primaryOutputName = relativePath + fileName + OptDotExt;
		}
		String otherPrimaryOutputs = EMPTY_STRING;
		for (int i=1; i<enumeratedPrimaryOutputs.size(); i++) {		// Starting a 1 is intentional
			otherPrimaryOutputs += WHITESPACE + ((IPath)enumeratedPrimaryOutputs.get(i)).toString();
		}
		
		// Figure out the path to the input resource		
		/*
		 * fix for PR 70491
		 * We need to check if the current resource is LINKED, because
		 * the default CDT doesn't handle this properly.  If it IS linked,
		 * then we must get the actual location of the resource, rather
		 * than the relative path.
		 */
		String projectLocation = project.getLocation().toString();

		// Optput file location needed for the file-specific build macros
		IPath outputLocation = Path.fromOSString(primaryOutputName);
		if (!outputLocation.isAbsolute()) {
			outputLocation = project.getLocation().append(getBuildWorkingDir()).append(primaryOutputName);
		}
		
		// A separate rule is needed for the resource in case the explicit file-specific macros
		// are referenced
		boolean fileExplicitMacrosReferenced = MacroResolver.getReferencedExplitFileMacros(tool).length > 0 ||
			MacroResolver.getReferencedExplitFileMacros(tool.getToolCommand(),IBuildMacroProvider.CONTEXT_FILE, 
					new FileContextData(sourceLocation, outputLocation, null, config.getToolChain())).length > 0;
		//get and resolve command
		String cmd = tool.getToolCommand();
		try{
			String resolvedCommand = ManagedBuildManager.getBuildMacroProvider().resolveValueToMakefileFormat(cmd,
					EMPTY_STRING,
					WHITESPACE,
					IBuildMacroProvider.CONTEXT_FILE,
					new FileContextData(sourceLocation, outputLocation, null, info.getDefaultConfiguration().getToolChain()));
			if((resolvedCommand = resolvedCommand.trim()).length() > 0)
				cmd = resolvedCommand;
				
		} catch (BuildMacroException e){
		}

		String defaultOutputName = EMPTY_STRING;
		String primaryDependencyName = EMPTY_STRING;
		String home = (generatedSource)? DOT : ROOT;
		String resourcePath = null;
		boolean patternRule = true;
		//boolean isItLinked = false;
		//if (resource.isLinked()) {   NOTE: we don't use this since children of linked resources return false
		if(!sourceLocation.toString().startsWith(projectLocation)) {
			// it IS linked, so use the actual location
			//isItLinked = true;
			resourcePath = sourceLocation.toString();
			// Need a hardcoded rule, not a pattern rule, as a linked file
			// can reside in any path
			defaultOutputName = relativePath + fileName + OptDotExt;
			primaryDependencyName = resourcePath;
			patternRule = false;
		} else {
			// use the relative path (not really needed to store per se but in the future someone may want this)
			resourcePath = relativePath; 
			// The rule and command to add to the makefile
			if( resConfig != null || fileExplicitMacrosReferenced) {
				// Need a hardcoded rule, not a pattern rule
				defaultOutputName = resourcePath + fileName + OptDotExt;
				primaryDependencyName = home + SEPARATOR + resourcePath + fileName + DOT + inputExtension;
				patternRule = false;
			} else {
				defaultOutputName = relativePath + WILDCARD + OptDotExt;
				primaryDependencyName = home + SEPARATOR + resourcePath + WILDCARD + DOT + inputExtension;
			}
		} // end fix for PR 70491
		
		//  If we still think that we are using a pattern rule, make sure that at least one of the rule
		//  outputs contains a %.
		if (patternRule) {
			patternRule = false;
			for (int i=0; i<ruleOutputs.size(); i++) {
				String ruleOutput = ((IPath)ruleOutputs.get(i)).toString();
				if (ruleOutput.indexOf('%') >= 0) {		//$NON-NLS-1$
					patternRule = true;
					break;
				}
			}
			if (!patternRule) {
				//  Need to reset the primary dependency to be a filename rather than a pattern
				primaryDependencyName = home + SEPARATOR + resourcePath + fileName + DOT + inputExtension;
			}
		}

		//  Begin building the rule for this source file
		String buildRule = EMPTY_STRING;
		if (patternRule) {
			if (ruleOutputs.size() == 0) {
				buildRule = defaultOutputName;
			} else {
				boolean first = true;
				for (int i=0; i<ruleOutputs.size(); i++) {
					String ruleOutput = ((IPath)ruleOutputs.get(i)).toString();
					if (ruleOutput.indexOf('%') >= 0) {		//$NON-NLS-1$
						if (first) {
							first = false;
						} else {
							buildRule += WHITESPACE;
						}
						buildRule += ruleOutput;
					}
				}
			}
		} else {
			buildRule += primaryOutputName;
		}
		
		buildRule += COLON + WHITESPACE + primaryDependencyName;

		// Other additional inputs
		// Get any additional dependencies specified for the tool in other InputType elements and AdditionalInput elements
		IPath[] addlDepPaths = tool.getAdditionalDependencies();
		for (int i=0; i<addlDepPaths.length; i++) {			
			// Translate the path from project relative to
			// build directory relative
			IPath addlPath = addlDepPaths[i];
			if (!(addlPath.toString().startsWith("$("))) {		//$NON-NLS-1$
				if (!addlPath.isAbsolute()) {
					IPath tempPath = project.getLocation().append(addlPath);
					if (tempPath != null) {
						addlPath = calculateRelativePath(getTopBuildDir(), tempPath);
					}
				}
			}
			buildRule += WHITESPACE + addlPath.toString();
		}
				
		// No duplicates in a makefile.  If we already have this rule, return
		if (getRuleList().contains(buildRule)) {
			//  TODO:  Should we assert that this is a pattern rule?
			return;
		}
		else {
			getRuleList().add(buildRule);
		}
		
		// Echo starting message
		buffer.append(buildRule + NEWLINE);
		buffer.append(TAB + AT + ECHO + WHITESPACE + SINGLE_QUOTE + MESSAGE_START_FILE + WHITESPACE + IN_MACRO + SINGLE_QUOTE + NEWLINE);
		 
		//  Generate the command line
		IManagedCommandLineInfo cmdLInfo = null;
		Vector inputs = new Vector(); 
		inputs.add(IN_MACRO);
		String outflag = null;
		String outputPrefix = null;
		
		if( resConfig != null || fileExplicitMacrosReferenced) {
			buffer.append(TAB + AT + ECHO + WHITESPACE + SINGLE_QUOTE + tool.getAnnouncement() + SINGLE_QUOTE + NEWLINE);
			outflag = tool.getOutputFlag();
			outputPrefix = tool.getOutputPrefix();
			String[] flags = null;
			try { 
				flags = tool.getToolCommandFlags(sourceLocation, outputLocation);
			} catch( BuildException ex ) {
				// TODO add some routines to catch this
				flags = EMPTY_STRING_ARRAY;
			}
			// Other additional inputs
			// Get any additional dependencies specified for the tool in other InputType elements and AdditionalInput elements
			IPath[] addlInputPaths = tool.getAdditionalResources();
			for (int i=0; i<addlInputPaths.length; i++) {
				// Translate the path from project relative to
				// build directory relative
				IPath addlPath = addlInputPaths[i];
				if (!(addlPath.toString().startsWith("$("))) {		//$NON-NLS-1$
					if (!addlPath.isAbsolute()) {
						IPath tempPath = project.getLocation().append(addlPath);
						if (tempPath != null) {
							addlPath = calculateRelativePath(getTopBuildDir(), tempPath);
						}
					}
				}
				inputs.add(addlPath.toString());
			}
			// Call the command line generator
			IManagedCommandLineGenerator cmdLGen = tool.getCommandLineGenerator();
			cmdLInfo = cmdLGen.generateCommandLineInfo( tool, cmd, flags, outflag, outputPrefix,
					OUT_MACRO + otherPrimaryOutputs, (String[])inputs.toArray(new String[inputs.size()]), tool.getCommandLinePattern() );
	
			String buildCmd = cmdLInfo.getCommandLine();
			buffer.append(TAB + AT + ECHO + WHITESPACE + buildCmd + NEWLINE);
			buffer.append(TAB + AT + buildCmd);
		} else {
			buffer.append(TAB + AT + ECHO + WHITESPACE + SINGLE_QUOTE + tool.getAnnouncement() + SINGLE_QUOTE + NEWLINE);
			String buildFlags = EMPTY_STRING;
			try {
				buildFlags = tool.getToolCommandFlagsString(sourceLocation, outputLocation);
			} catch (BuildException e) {
			}
			outflag = info.getOutputFlag(outputExtension);
			outputPrefix = info.getOutputPrefix(outputExtension);
			String[] flags = buildFlags.split( "\\s" ); //$NON-NLS-1$
			// Other additional inputs
			// Get any additional dependencies specified for the tool in other InputType elements and AdditionalInput elements
			IPath[] addlInputPaths = tool.getAdditionalResources();
			for (int i=0; i<addlInputPaths.length; i++) {
				// Translate the path from project relative to
				// build directory relative
				IPath addlPath = addlInputPaths[i];
				if (!(addlPath.toString().startsWith("$("))) {		//$NON-NLS-1$
					if (!addlPath.isAbsolute()) {
						IPath tempPath = project.getLocation().append(addlPath);
						if (tempPath != null) {
							addlPath = calculateRelativePath(getTopBuildDir(), tempPath);
						}
					}
				}
				inputs.add(addlPath.toString());
			}
			// Call the command line generator
			cmdLInfo = info.generateToolCommandLineInfo( inputExtension, flags, outflag, outputPrefix, 
					OUT_MACRO + otherPrimaryOutputs, (String[])inputs.toArray(new String[inputs.size()]), sourceLocation, outputLocation );
			// The command to build
			String buildCmd = null;
			if( cmdLInfo == null ) buildCmd = cmd + WHITESPACE + buildFlags + WHITESPACE + 
					outflag + WHITESPACE + outputPrefix + OUT_MACRO + otherPrimaryOutputs + WHITESPACE + IN_MACRO;
			else buildCmd = cmdLInfo.getCommandLine();
			buffer.append(TAB + AT + ECHO + WHITESPACE + buildCmd + NEWLINE);
			buffer.append(TAB + AT + buildCmd);
		}
		
		// Determine if there are any dependencies to calculate
		if (doDepGen && depGen.getCalculatorType() == IManagedDependencyGenerator.TYPE_COMMAND) {
			buffer.append(WHITESPACE + LOGICAL_AND + WHITESPACE + LINEBREAK);
			// Get the dependency rule out of the generator
			String depCmd = depGen.getDependencyCommand(resource, info);
			buffer.append(depCmd);
		}
		
		// Echo finished message
		buffer.append(NEWLINE);
		buffer.append(TAB + AT + ECHO + WHITESPACE + SINGLE_QUOTE + MESSAGE_FINISH_FILE + WHITESPACE + IN_MACRO + SINGLE_QUOTE + NEWLINE);
		buffer.append(TAB + AT + ECHO + WHITESPACE + SINGLE_QUOTE + WHITESPACE + SINGLE_QUOTE + NEWLINE + NEWLINE);


		//  Add separate dependency lines per file if necessary
		boolean addedDepLines = false; 
		if (depGen != null && depGen.getCalculatorType() != IManagedDependencyGenerator.TYPE_COMMAND) { 
			Vector addlDepsVector = calculateDependenciesForSource(depGen, tool, relativePath, resource);
			for (int i=0; i<addlDepsVector.size(); i++) {
				buffer.append(primaryOutputName + COLON + WHITESPACE + addlDepsVector.get(i).toString() + NEWLINE);
				addedDepLines = true;
			}
		}

		// Add any additional outputs here using dependency lines
		for (int i=1; i<enumeratedPrimaryOutputs.size(); i++) {		// Starting a 1 is intentional
			buffer.append(((IPath)enumeratedPrimaryOutputs.get(i)).toString() + COLON + WHITESPACE + primaryOutputName + NEWLINE);
			addedDepLines = true;
		}
		for (int i=0; i<enumeratedSecondaryOutputs.size(); i++) {
			buffer.append(((IPath)enumeratedSecondaryOutputs.get(i)).toString() + COLON + WHITESPACE + primaryOutputName + NEWLINE);
			addedDepLines = true;
		}
		if (addedDepLines) {
			buffer.append(NEWLINE);			
		}

	}

	/* (non-Javadoc)
	 * Returns the output <code>IPath</code>s for this invocation of the tool with the specified source file
	 /*
	 * The priorities for determining the names of the outputs of a tool are:
	 *  1.  If the tool is the build target and primary output, use artifact name & extension -
	 *      This case does not apply here...
	 *  2.  If an option is specified, use the value of the option
	 *  3.  If a nameProvider is specified, call it
	 *  4.  If outputNames is specified, use it
	 *  5.  Use the name pattern to generate a transformation macro 
	 *      so that the source names can be transformed into the target names 
	 *      using the built-in string substitution functions of <code>make</code>.
	 *      
	 *  @param tool
	 *  @param relativePath  build output directory relative path of the current output directory
	 *  @param resource
	 *  @param ruleOutputs  Vector of rule IPaths that are relative to the build directory 
	 *  @param enumeratedPrimaryOutputs  Vector of IPaths of primary outputs 
	 *                            that are relative to the build directory 
	 *  @param enumeratedSecondaryOutputs  Vector of IPaths of secondary outputs 
	 *                            that are relative to the build directory 
	 */  
	protected void calculateOutputsForSource(ITool tool, String relativePath, IResource resource, 
			IPath sourceLocation, Vector ruleOutputs, Vector enumeratedPrimaryOutputs, Vector enumeratedSecondaryOutputs) {
		String inExt = sourceLocation.getFileExtension();
		String outExt = tool.getOutputExtension(inExt);
		
		IOutputType[] outTypes = tool.getOutputTypes();
		if (outTypes != null && outTypes.length > 0) {
			for (int i=0; i<outTypes.length; i++) {
				IOutputType type = outTypes[i];
				boolean primaryOutput = (type == tool.getPrimaryOutputType());
				//if (primaryOutput && ignorePrimary) continue;
				String outputPrefix = type.getOutputPrefix();
				boolean multOfType = type.getMultipleOfType();
				IOption option = tool.getOptionBySuperClassId(type.getOptionId());
				IManagedOutputNameProvider nameProvider = type.getNameProvider();
				String[] outputNames = type.getOutputNames();

				//  1.  If the tool is the build target and this is the primary output, 
				//      use artifact name & extension
				//      Not appropriate here...
				//  2.  If an option is specified, use the value of the option
				if (option != null) {
					try {
						List outputList = new ArrayList();
						int optType = option.getValueType();
						if (optType == IOption.STRING) {
							outputList.add(outputPrefix + option.getStringValue());
						} else if (
								optType == IOption.STRING_LIST ||
								optType == IOption.LIBRARIES ||
								optType == IOption.OBJECTS) {
							outputList = (List)option.getValue();
							// Add outputPrefix to each if necessary
							if (outputPrefix.length() > 0) {
								for (int j=0; j<outputList.size(); j++) {
									outputList.set(j, outputPrefix + outputList.get(j));
								}
							}
						}
						for (int j=0; j<outputList.size(); j++) {
							IPath outPath = Path.fromOSString((String)outputList.get(j));
							//  If only a file name is specified, add the relative path of this output directory
							if (outPath.segmentCount() == 1) {
								outPath = Path.fromOSString(relativePath + (String)outputList.get(j)); 
							}
							if (primaryOutput) {
								ruleOutputs.add(j, outPath);
								enumeratedPrimaryOutputs.add(j, resolvePercent(outPath, sourceLocation));
							} else {
								ruleOutputs.add(outPath);
								enumeratedSecondaryOutputs.add(resolvePercent(outPath, sourceLocation));
							}
						}
					} catch( BuildException ex ) {}
				} else 
				//  3.  If a nameProvider is specified, call it
				if (nameProvider != null) {
					IPath[] inPaths = new IPath[1];
					inPaths[0] = sourceLocation;
					IPath[] outPaths = nameProvider.getOutputNames(tool, inPaths);
					for (int j=0; j<outPaths.length; j++) {
						IPath outPath = outPaths[j];
						//  If only a file name is specified, add the relative path of this output directory
						if (outPath.segmentCount() == 1) {
							outPath = Path.fromOSString(relativePath + outPath.toString()); 
						}
						if (primaryOutput) {
							ruleOutputs.add(j, outPath);
							enumeratedPrimaryOutputs.add(j, resolvePercent(outPath, sourceLocation));
						} else {
							ruleOutputs.add(outPath);
							enumeratedSecondaryOutputs.add(resolvePercent(outPath, sourceLocation));
						}
					}
				} else
				//  4.  If outputNames is specified, use it
				if (outputNames != null) {
					for (int j = 0; j < outputNames.length; j++) {
						IPath outPath = Path.fromOSString(outputNames[j]);
						//  If only a file name is specified, add the relative path of this output directory
						if (outPath.segmentCount() == 1) {
							outPath = Path.fromOSString(relativePath + outPath.toString()); 
						}
						if (primaryOutput) {
							ruleOutputs.add(j, outPath);
							enumeratedPrimaryOutputs.add(j, resolvePercent(outPath, sourceLocation));
						} else {
							ruleOutputs.add(outPath);
							enumeratedSecondaryOutputs.add(resolvePercent(outPath, sourceLocation));
						}
					}
				} else {
				//  5.  Use the name pattern to generate a transformation macro 
				//      so that the source names can be transformed into the target names 
				//      using the built-in string substitution functions of <code>make</code>.
					if (multOfType) {
						// This case is not handled - a nameProvider or outputNames must be specified
						// TODO - report error
					} else {
						String namePattern = type.getNamePattern();
						IPath namePatternPath = null; 
						if (namePattern == null || namePattern.length() == 0) {
							namePattern = relativePath + outputPrefix + IManagedBuilderMakefileGenerator.WILDCARD;
							if (outExt != null && outExt.length() > 0) {
								namePattern += DOT + outExt;
							}
							namePatternPath = Path.fromOSString(namePattern);
						}
						else {
							if (outputPrefix.length() > 0) {
								namePattern = outputPrefix + namePattern;
							}
							namePatternPath = Path.fromOSString(namePattern);
							//  If only a file name is specified, add the relative path of this output directory
							if (namePatternPath.segmentCount() == 1) {
								namePatternPath = Path.fromOSString(relativePath + namePatternPath.toString()); 
							}
						}
					
						if (primaryOutput) {
							ruleOutputs.add(0, namePatternPath);
							enumeratedPrimaryOutputs.add(0, resolvePercent(namePatternPath, sourceLocation));
						} else {
							ruleOutputs.add(namePatternPath);
							enumeratedSecondaryOutputs.add(resolvePercent(namePatternPath, sourceLocation));
						}
					}
				}					
			}
		} else {
			// For support of pre-CDT 3.0 integrations.
			// NOTE WELL:  This only supports the case of a single "target tool"
			//     that consumes exactly all of the object files, $OBJS, produced
			//     by other tools in the build and produces a single output.
			//     In this case, the output file name is the input file name with
			//     the output extension.

			//if (!ignorePrimary) {
				String outPrefix = tool.getOutputPrefix();
				IPath outPath = Path.fromOSString(relativePath + outPrefix + WILDCARD);
				outPath = outPath.addFileExtension(outExt);
				ruleOutputs.add(0, outPath);
				enumeratedPrimaryOutputs.add(0, resolvePercent(outPath, sourceLocation));
			//}
		}
	}
	
	/* (non-Javadoc)
	 * If the path contains a %, returns the path resolved using the resource name 
	 *
	 */
	protected IPath resolvePercent(IPath outPath, IPath sourceLocation) {
		//  Get the input file name
		String fileName = sourceLocation.removeFileExtension().lastSegment();
		//  Replace the % with the file name
		String outName = outPath.toOSString().replaceAll("%", fileName); //$NON-NLS-1$ 
		return Path.fromOSString(outName);
	}
	
	/* (non-Javadoc)
	 * Returns the dependency <code>IPath</code>s for this invocation of the tool with the specified source file
	 *      
	 *  @param depGen  the dependency calculator
	 *  @param tool  tool used to build the source file
	 *  @param relativePath  build output directory relative path of the current output directory
	 *  @param resource  source file to scan for dependencies
	 *  @return Vector of IPaths that are relative to the build directory 
	 */  
	protected Vector calculateDependenciesForSource(IManagedDependencyGenerator depGen, ITool tool, String relativePath, IResource resource) {
		Vector deps = new Vector();
		int type = depGen.getCalculatorType();
		
		switch (type) {

		case IManagedDependencyGenerator.TYPE_INDEXER:
		case IManagedDependencyGenerator.TYPE_EXTERNAL:
			IResource[] res = depGen.findDependencies(resource, project);
			for (int i=0; i<res.length; i++) {
				IPath dep = res[i].getProjectRelativePath();
				deps.add(dep);
			}
			break;
			
		case IManagedDependencyGenerator.TYPE_NODEPS:
		default:
			break;
		}
		return deps;
	}

	
	/*************************************************************************
	 *   M A K E F I L E   G E N E R A T I O N   C O M M O N   M E T H O D S 
	 ************************************************************************/
	
	/* (non-Javadoc)
	 * Generates a source macro name from a file extension 
	 */
	public StringBuffer getSourceMacroName(String extensionName) {
		StringBuffer macroName = new StringBuffer();
		
		// We need to handle case sensitivity in file extensions (e.g. .c vs .C), so if the
		// extension was already upper case, tack on an "UPPER_" to the macro name.
		// In theory this means there could be a conflict if you had for example,
		// extensions .c_upper, and .C, but realistically speaking the chances of this are
		// practically nil so it doesn't seem worth the hassle of generating a truly
		// unique name.
		if(extensionName.equals(extensionName.toUpperCase())) {
			macroName.append(extensionName.toUpperCase() + "_UPPER");	//$NON-NLS-1$
		} else {
			// lower case... no need for "UPPER_"
			macroName.append(extensionName.toUpperCase());
		}
		macroName.append("_SRCS");	//$NON-NLS-1$
		return macroName;
	}
	
	/* (non-Javadoc)
	 * Answers all of the output extensions that the target 
	 * of the build has tools defined to work on.
	 * 
	 * @return a <code>Set</code> containing all of the output extensions 
	 */
	public Set getOutputExtensions() {
		if (outputExtensionsSet == null) {
			// The set of output extensions which will be produced by this tool.
			// It is presumed that this set is not very large (likely < 10) so
			// a HashSet should provide good performance.
			outputExtensionsSet = new HashSet();
			
			// For each tool for the target, lookup the kinds of sources it outputs
			// and add that to our list of output extensions.
			for (int i=0; i<buildTools.length; i++) {
				ITool tool = buildTools[i];
				String[] outputs = tool.getAllOutputExtensions();
				if (outputs != null) {
					outputExtensionsSet.addAll(Arrays.asList(outputs));
				}
			}
		}
 		return outputExtensionsSet;
	}
	
	/* (non-Javadoc)
	 * Outputs a comment formatted as follows:
	 * ##### ....... #####
	 * # <Comment message>
	 * ##### ....... ##### 
	 */
	protected StringBuffer addDefaultHeader() {
		StringBuffer buffer = new StringBuffer();
		outputCommentLine(buffer);
		buffer.append(COMMENT_SYMBOL + WHITESPACE + ManagedMakeMessages.getResourceString(HEADER) + NEWLINE);
		outputCommentLine(buffer);
		buffer.append(NEWLINE);
		return buffer;
	}

	/* (non-Javadoc)
	 * Put COLS_PER_LINE comment charaters in the argument.
	 */
	protected void outputCommentLine(StringBuffer buffer) {
		for (int i = 0; i < COLS_PER_LINE; i++) {
			buffer.append(COMMENT_SYMBOL);
		}
		buffer.append(NEWLINE);		
	}

	/* (non-Javadoc)
	 * Answers the argument with all whitespaces replaced with an escape sequence.
	 * 
	 * @param path
	 */
	protected String escapeWhitespaces(String path) {
		// Escape the spaces in the path/filename if it has any
		String[] segments = path.split("\\s"); //$NON-NLS-1$
		if (segments.length > 1) {
			StringBuffer escapedPath = new StringBuffer();
			for (int index = 0; index < segments.length; ++index) {
				escapedPath.append(segments[index]);
				if (index + 1 < segments.length) {
					escapedPath.append("\\ "); //$NON-NLS-1$
				}
			}
			return escapedPath.toString().trim();
		} else {
			return path;
		}
	}

	/* (non-Javadoc)
	 * Adds a macro addition prefix to a map of macro names to entries.
	 * Entry prefixes look like:
	 * 	C_SRCS += \
	 * 	${addprefix $(ROOT)/, \
	 */
	protected void addMacroAdditionPrefix(LinkedHashMap map, String macroName, String relativePath, boolean addPrefix) {
		// there is no entry in the map, so create a buffer for this macro
		StringBuffer tempBuffer = new StringBuffer();
		tempBuffer.append(macroName + WHITESPACE + MACRO_ADDITION_PREFIX_SUFFIX);
		if (addPrefix) {
			tempBuffer.append(MACRO_ADDITION_ADDPREFIX_HEADER + relativePath + MACRO_ADDITION_ADDPREFIX_SUFFIX);
		}
		
		// have to store the buffer in String form as StringBuffer is not a sublcass of Object
		map.put(macroName, tempBuffer.toString());
	}

	/* (non-Javadoc)
	 * Adds a file to an entry in a map of macro names to entries.
	 * File additions look like:
	 * 	example.c, \
	 */
	protected void addMacroAdditionFile(HashMap map, String macroName, String filename) {
		StringBuffer buffer = new StringBuffer();
		buffer.append(map.get(macroName));
		buffer.append(filename + WHITESPACE + LINEBREAK);
		// re-insert string in the map
		map.put(macroName, buffer.toString());
	}

	/* (non-Javadoc)
	 * Adds a file to an entry in a map of macro names to entries.
	 * File additions look like:
	 * 	example.c, \
	 */
	protected void addMacroAdditionFile(HashMap map, String macroName, 
			String relativePath, IPath sourceLocation, boolean generatedSource) {
		//  Add the source file path to the makefile line that adds source files to the build variable
		String srcName;
		IPath projectLocation = project.getLocation();
		IPath dirLocation = projectLocation;
		if (generatedSource) {
			dirLocation = dirLocation.append(getBuildWorkingDir());
		}
		if (dirLocation.isPrefixOf(sourceLocation)) {
			IPath srcPath = sourceLocation.removeFirstSegments(dirLocation.segmentCount()).setDevice(null);
			if (generatedSource) {
				srcName = "./" + srcPath.toString();     //$NON-NLS-1$
			} else {
				srcName = "$(ROOT)/" + srcPath.toString();     //$NON-NLS-1$
			}
		} else {
			if (generatedSource && !sourceLocation.isAbsolute()) {
				srcName = "./" + relativePath + sourceLocation.lastSegment().toString();	//$NON-NLS-1$
			} else {
				// TODO:  Should we use relative paths when possible (e.g., see MbsMacroSupplier.calculateRelPath)
				srcName = sourceLocation.toString();
			}
		}
		addMacroAdditionFile(map, macroName, srcName); 
	}

	/* (non-Javadoc)
	 * Adds file(s) to an entry in a map of macro names to entries.
	 * File additions look like:
	 * 	example.c, \
	 */
	public void addMacroAdditionFiles(HashMap map, String macroName, Vector filenames) {
		StringBuffer buffer = new StringBuffer();
		buffer.append(map.get(macroName));
		for (int i=0; i<filenames.size(); i++) {
			String filename = (String)filenames.get(i);
			if (filename.length() > 0) {
				buffer.append(filename + WHITESPACE + LINEBREAK);
			}
		}
		// re-insert string in the map
		map.put(macroName, buffer.toString());
	}

	/* (non-Javadoc)
	 * Write all macro addition entries in a map to the buffer
	 */
	protected StringBuffer writeAdditionMacros(LinkedHashMap map) {
		StringBuffer buffer = new StringBuffer();
		// Add the comment
		buffer.append(COMMENT_SYMBOL + WHITESPACE + ManagedMakeMessages.getResourceString(MOD_VARS) + NEWLINE);
		
 		Collection bufferCollection = map.values();
 		Iterator collectionIterator = bufferCollection.iterator();
 		while(collectionIterator.hasNext())
 		{
			String macroString = collectionIterator.next().toString();
			// Check if we added any files to the rule
			// Currently, we do this by comparing the end of the rule buffer to MACRO_ADDITION_PREFIX_SUFFIX
			if (!(macroString.endsWith(MACRO_ADDITION_PREFIX_SUFFIX)) && 
				!(macroString.endsWith(MACRO_ADDITION_ADDPREFIX_SUFFIX))) {
				StringBuffer currentBuffer = new StringBuffer();
				// Close off the rule
				if (macroString.indexOf(MACRO_ADDITION_ADDPREFIX_HEADER) >= 0) {
					currentBuffer.append(macroString + "}" + NEWLINE); //$NON-NLS-1$
				} else {
					// Remove the final "/"
					if (macroString.endsWith(LINEBREAK)) {
						macroString = macroString.substring(0, (macroString.length() - 2)) + NEWLINE;
					}
					currentBuffer.append(macroString);
				}
				currentBuffer.append(NEWLINE);
 			
	 			// append the contents of the buffer to the master buffer for the whole file
	 			buffer.append(currentBuffer);
			}
 		}		
		return buffer.append(NEWLINE);
	}

	/* (non-Javadoc)
	 * Write all macro addition entries in a map to the buffer
	 */
	protected StringBuffer writeTopAdditionMacros(List varList, HashMap varMap) {
		StringBuffer buffer = new StringBuffer();
		// Add the comment
		buffer.append(COMMENT_SYMBOL + WHITESPACE + ManagedMakeMessages.getResourceString(MOD_VARS) + NEWLINE);
		
		for (int i=0; i<varList.size(); i++) {
			String addition = (String)varMap.get((String)varList.get(i));
			StringBuffer currentBuffer = new StringBuffer();
			currentBuffer.append(addition);
			currentBuffer.append(NEWLINE);
			
 			// append the contents of the buffer to the master buffer for the whole file
 			buffer.append(currentBuffer);
		}
		return buffer.append(NEWLINE);
	}

	/* (non-Javadoc)
	 * Calculates the inputs and outputs for tools that will be generated in the top makefile. 
	 * This information is used by the top level makefile generation methods.
	 */
	protected void calculateToolInputsOutputs() {

		//  We are "done" when the information for all tools has been calculated,
		//  or we are not making any progress
		boolean done = false;
		boolean lastChance = false;
		int[] doneState = new int[buildTools.length];
		
		// Identify the target tool 
		ITool targetTool = config.getTargetTool();
		if (targetTool == null) {
			targetTool = info.getToolFromOutputExtension(buildTargetExt);
		}
		
		//  Initialize the tool info array and the done state
		for (int i=0; i<buildTools.length; i++) {
			if ((buildTools[i] == targetTool)) {
				String ext = info.getBuildArtifactExtension();
				//try to resolve the build macros in the artifact extension
				try{
					ext = ManagedBuildManager.getBuildMacroProvider().resolveValueToMakefileFormat(
							ext,
							"", //$NON-NLS-1$
							" ", //$NON-NLS-1$
							IBuildMacroProvider.CONTEXT_CONFIGURATION,
							info.getDefaultConfiguration());
				} catch (BuildMacroException e){
				}
				
				String name = info.getBuildArtifactName();
				//try to resolve the build macros in the artifact name
				try{
					String resolved = ManagedBuildManager.getBuildMacroProvider().resolveValueToMakefileFormat(
							name,
							"", //$NON-NLS-1$
							" ", //$NON-NLS-1$
							IBuildMacroProvider.CONTEXT_CONFIGURATION,
							info.getDefaultConfiguration());
					if((resolved = resolved.trim()).length() > 0)
						name = resolved;
				} catch (BuildMacroException e){
				}

				gnuToolInfos[i] = new ManagedBuildGnuToolInfo(project, buildTools[i], true, 
						name, ext); 
			} else {
				gnuToolInfos[i] = new ManagedBuildGnuToolInfo(project, buildTools[i], false, null, null);				
			}
			doneState[i] = 0;
		}
		
		//  Initialize the build output variable to file additions map
		LinkedHashMap map = getTopBuildOutputVars();
		Iterator iterator = buildOutVars.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry entry = (Map.Entry)iterator.next();
			String macroName = (String)entry.getKey();
			addMacroAdditionPrefix(map, macroName, "", false);	//$NON-NLS-1$
		}

		// Set of input extensions for which macros have been created so far
		HashSet handledDepsInputExtensions = new HashSet();
		HashSet handledOutsInputExtensions = new HashSet();
		
		while (!done) {
			int[] testState = new int[doneState.length];
			for (int i=0; i<testState.length; i++) testState[i] = 0;
			
			//  Calculate inputs
			for (int i=0; i<gnuToolInfos.length; i++) {
				if (gnuToolInfos[i].areInputsCalculated()) {
					testState[i]++;
				} else {
					if (gnuToolInfos[i].calculateInputs(this, projectResources, lastChance)) {
						testState[i]++;
					}
				}
			}
			//  Calculate dependencies
			for (int i=0; i<gnuToolInfos.length; i++) {
				if (gnuToolInfos[i].areDependenciesCalculated()) {
					testState[i]++;
				} else {
					if (gnuToolInfos[i].calculateDependencies(this, handledDepsInputExtensions, lastChance)) {
						testState[i]++;
					}
				}
			}
			//  Calculate outputs
			for (int i=0; i<gnuToolInfos.length; i++) {
				if (gnuToolInfos[i].areOutputsCalculated()) {
					testState[i]++;
				} else {
					if (gnuToolInfos[i].calculateOutputs(this, handledOutsInputExtensions, lastChance)) {
						testState[i]++;
					}
				}
			}
			//  Are all calculated?  If so, done.
			done = true;
			for (int i=0; i<testState.length; i++) {
				if (testState[i] != 3) {
					done = false;
					break;
				}
			}
			
			//  Test our "done" state vs. the previous "done" state.
			//  If we have made no progress, give it a "last chance" and then quit
			if (!done) {
				done = true;
				for (int i=0; i<testState.length; i++) {
					if (testState[i] != doneState[i]) {
						done = false;
						break;
					}
				}				
			}
			if (done) {
				if (!lastChance) {
					lastChance = true;
					done = false;
				}
			} 
			if (!done) {
				doneState = testState;
			}
		}
	}

	/* (non-javadoc)
	 * Returns the (String) list of files associated with the build variable
	 * 
	 * @param variable  the variable name
	 * @param locationType  the format in which we want the filenames returned
	 * @param directory  project relative directory path used with locationType == DIRECTORY_RELATIVE 
	 * @param getAll  only return the list if all tools that are going to contrubute to this
	 *                variable have done so.
	 * @return List
	 */
	public List getBuildVariableList(String variable, int locationType, IPath directory, boolean getAll) {
		boolean done = true;
		for (int i=0; i<gnuToolInfos.length; i++) {
			if (!gnuToolInfos[i].areOutputVariablesCalculated()) {
				done = false;
			} 
		}
		if (!done && getAll) return null;
		List list = (List)buildSrcVars.get(variable);
		if (list == null) {
			list = (List)buildOutVars.get(variable);
		}

		List fileList = null;
		if (list != null) {
			//  Convert the items in the list to the location-type wanted by the caller,
			//  and to a string list
			IPath dirLocation = null;
			if (locationType != ABSOLUTE) {
				dirLocation = project.getLocation();
				if (locationType == PROJECT_SUBDIR_RELATIVE) {
					dirLocation = dirLocation.append(directory);
				}
			}
			for (int i=0; i<list.size(); i++) {
				IPath path = (IPath)list.get(i);
				if (locationType != ABSOLUTE) {
					if (dirLocation.isPrefixOf(path)) {
						path = path.removeFirstSegments(dirLocation.segmentCount()).setDevice(null);
					}
				}
				fileList.add(path.toString());
			}
		}
		
		return fileList;
	}

	/* (non-javadoc)
	 * Returns the list of files associated with the build variable in the top makefile
	 * 
	 * @param variable  the variable name
	 * @return List
	 */
	public List getTopBuildVariableList(String variable) {
		return (List)topBuildOutVars.get(variable);
	}
	
	/* (non-javadoc)
	 * Returns the map of build variables to list of files
	 * 
	 * @return HashMap
	 */
	public HashMap getBuildOutputVars() {
		return buildOutVars;
	}
	
	/* (non-javadoc)
	 * Returns the map of build variables used in the top makefile to list of files
	 * 
	 * @return HashMap
	 */
	public LinkedHashMap getTopBuildOutputVars() {
		return topBuildOutVars;
	}
	
	/* (non-javadoc)
	 * Returns the list of known build rules. This keeps me from generating duplicate
	 * rules for known file extensions.
	 * 
	 * @return List
	 */
	protected Vector getRuleList() {
		if (ruleList == null) {
			ruleList = new Vector();
		}
		return ruleList;
	}
	
	/*************************************************************************
	 *   R E S O U R C E   V I S I T O R   M E T H O D S 
	 ************************************************************************/
	
	/**
	 * Adds the container of the argument to the list of folders in the project that
	 * contribute source files to the build. The resource visitor has already established 
	 * that the build model knows how to build the files. It has also checked that
	 * the resource is not generated as part of the build.
	 *  
	 * @param resource
	 */
	protected void appendBuildSubdirectory(IResource resource) {
		IContainer container = resource.getParent();
		// If the path contains a space relative to the project, reject it from the build
		if (resource.getProjectRelativePath().toString().indexOf(" ") != -1) {	//$NON-NLS-1$
			// Only add the container once
			if (!getInvalidDirList().contains(container)) {
				getInvalidDirList().add(container);
			}
		} else {
			// Only add the container once
			if (!getSubdirList().contains(container)) {
				getSubdirList().add(container);		
			}
		}
	}

	/**
	 * Adds the container of the argument to a list of subdirectories that are to be
	 * deleted. As a result, the directories that are generated for the output 
	 * should be removed as well.
	 * 
	 * @param container
	 */
	protected void appendDeletedSubdirectory(IContainer container) {
		// No point in adding a folder if the parent is already there
		IContainer parent = container.getParent();
		if (!getDeletedDirList().contains(container) && 
				!getDeletedDirList().contains(parent)) {
			getDeletedDirList().add(container);
		}
	}

	/**
	 * If a file is removed from a source folder (either because of a delete 
	 * or move action on the part of the user), the makefilegenerator has to
	 * remove the dependency makefile along with the old build goal 
	 *  
	 * @param resource
	 */
	protected void appendDeletedFile(IResource resource) {
		// Cache this for now
		getDeletedFileList().add(resource);
	}
	
	/**
	 * Adds the container of the argument to a list of subdirectories that are part 
	 * of an incremental rebuild of the project. The makefile fragments for these 
	 * directories will be regenerated as a result of the build.
	 * 
	 * @param resource
	 */
	protected void appendModifiedSubdirectory(IResource resource) {
		IContainer container = resource.getParent();
		// If the path contains a space relative to the project, reject it from the build
		if (resource.getProjectRelativePath().toString().indexOf(" ") != -1) {	//$NON-NLS-1$
			// Only add the container once
			if (!getInvalidDirList().contains(container)) {
				getInvalidDirList().add(container);
			}
		} else {
			if (!getModifiedList().contains(container)) {
				getModifiedList().add(container);
			}
		}
	}

	
	/*************************************************************************
	 *   O T H E R   M E T H O D S 
	 ************************************************************************/
	
	/* (non-Javadoc)
	 * @param message
	 */
	protected void cancel(String message) {
		if (monitor != null && !monitor.isCanceled()) {
			throw new OperationCanceledException(message);
		}
	}

	/* (non-Javadoc)
	 * Check whether the build has been cancelled. Cancellation requests 
	 * propagated to the caller by throwing <code>OperationCanceledException</code>.
	 * 
	 * @see org.eclipse.core.runtime.OperationCanceledException#OperationCanceledException()
	 */
	protected void checkCancel() {
		if (monitor != null && monitor.isCanceled()) {
			throw new OperationCanceledException();
		}
	}

	/* (non-Javadoc)
	 * Return or create the folder needed for the build output. If we are
	 * creating the folder, set the derived bit to true so the CM system 
	 * ignores the contents. If the resource exists, respect the existing 
	 * derived setting. 
	 * 
	 * @param string
	 * @return IPath
	 */
	private IPath createDirectory(String dirName) throws CoreException {
		// Create or get the handle for the build directory 
		IFolder folder = project.getFolder(dirName);
		if (!folder.exists()) {
			// Make sure that parent folders exist
			IPath parentPath = (new Path(dirName)).removeLastSegments(1);
			// Assume that the parent exists if the path is empty
			if (!parentPath.isEmpty()) {
				IFolder parent = project.getFolder(parentPath);
				if (!parent.exists()) {
					createDirectory(parentPath.toString());
				}
			}
			
			// Now make the requested folder
			try {
				folder.create(true, true, null);
			}
			catch (CoreException e) {
				if (e.getStatus().getCode() == IResourceStatus.PATH_OCCUPIED)
					folder.refreshLocal(IResource.DEPTH_ZERO, null);
				else
					throw e;
			}

			// Make sure the folder is marked as derived so it is not added to CM
			if (!folder.isDerived()) {
				folder.setDerived(true);
			}
		}
	
		return folder.getFullPath();
	}

	/* (non-Javadoc)
	 * Return or create the makefile needed for the build. If we are creating 
	 * the resource, set the derived bit to true so the CM system ignores 
	 * the contents. If the resource exists, respect the existing derived 
	 * setting.
	 *  
	 * @param makefilePath
	 * @return IFile
	 */
	private IFile createFile(IPath makefilePath) throws CoreException {
		// Create or get the handle for the makefile
		IWorkspaceRoot root = CCorePlugin.getWorkspace().getRoot();
		IFile newFile = root.getFileForLocation(makefilePath);
		if (newFile == null) {
			newFile = root.getFile(makefilePath);
		}
		// Create the file if it does not exist
		ByteArrayInputStream contents = new ByteArrayInputStream(new byte[0]);
		try {
			newFile.create(contents, false, new SubProgressMonitor(monitor, 1));
			// Make sure the new file is marked as derived
			if (!newFile.isDerived()) {
				newFile.setDerived(true);
			}

		}
		catch (CoreException e) {
			// If the file already existed locally, just refresh to get contents
			if (e.getStatus().getCode() == IResourceStatus.PATH_OCCUPIED)
				newFile.refreshLocal(IResource.DEPTH_ZERO, null);
			else
				throw e;
		}
		
		return newFile;
	}

	/**
	 * @param deletedFile
	 */
	private void deleteBuildTarget(IResource deletedFile) {
		// Get the project relative path of the file
		String fileName = getFileName(deletedFile);
		String srcExtension = deletedFile.getFileExtension();
		String targetExtension = info.getOutputExtension(srcExtension);
		if (targetExtension != "") //$NON-NLS-1$
			fileName += DOT + targetExtension;
		IPath projectRelativePath = deletedFile.getProjectRelativePath().removeLastSegments(1);
		IPath targetFilePath = getBuildWorkingDir().append(projectRelativePath).append(fileName);
		IResource depFile = project.findMember(targetFilePath);
		if (depFile != null && depFile.exists()) {
			try {
				depFile.delete(true, new SubProgressMonitor(monitor, 1));
			} catch (CoreException e) {
				// This had better be allowed during a build
				
			}
		}		
	}
	
	/**
	 * @param deletedFile
	 */
	private void deleteDepFile(IResource deletedFile) {
		// Get the project relative path of the file
		String fileName = getFileName(deletedFile);
		fileName += DOT + DEP_EXT;
		IPath projectRelativePath = deletedFile.getProjectRelativePath().removeLastSegments(1);
		IPath depFilePath = getBuildWorkingDir().append(projectRelativePath).append(fileName);
		IResource depFile = project.findMember(depFilePath);
		if (depFile != null && depFile.exists()) {
			try {
				depFile.delete(true, new SubProgressMonitor(monitor, 1));
			} catch (CoreException e) {
				// This had better be allowed during a build
				
			}
		}
	}

	/**
	 * @return Returns the deletedDirList.
	 */
	private Vector getDeletedDirList() {
		if (deletedDirList == null) {
			deletedDirList = new Vector();
		}
		return deletedDirList;
	}
	
	/* (non-Javadoc)
	 * @return
	 */
	private Vector getDeletedFileList() {
		if (deletedFileList == null) {
			deletedFileList = new Vector();
		}
		return deletedFileList;
	}

	/* (non-Javadoc)
	 * @return
	 */
	private Vector getDependencyMakefiles() {
		if (dependencyMakefiles == null) {
			dependencyMakefiles = new Vector();
		}
		return dependencyMakefiles;
	}
	
	/* (non-Javadoc)
	 * Strips off the file extension from the argument and returns 
	 * the name component in a <code>String</code>
	 * 
	 * @param file
	 * @return
	 */
	private String getFileName(IResource file) {
		String answer = new String();
		String lastSegment = file.getName();
		int extensionSeparator = lastSegment.lastIndexOf(DOT);
		if (extensionSeparator != -1) {
			answer = lastSegment.substring(0, extensionSeparator);
		}
		return answer;
	}
	
	/* (non-Javadoc)
	 * Answers a Vector containing a list of directories that are invalid for the 
	 * build for some reason. At the moment, the only reason a directory would 
	 * not be considered for the build is if it contains a space in the relative 
	 * path from the project root.
	 * 
	 * @return a a list of directories that are invalid for the build
	 */
	private Vector getInvalidDirList() {
		if (invalidDirList == null) {
			invalidDirList = new Vector();
		}
		return invalidDirList;
	}
	
	/* (non-javadoc)
	 * 
	 * @return Vector
	 */
	private Vector getModifiedList() {
		if (modifiedList == null) {
			modifiedList = new Vector();
		}
		return modifiedList;
	}

	/* (non-javadoc)
	 * Answers the list of subdirectories contributing source code to the build
	 * 
	 * @return List
	 */
	private Vector getSubdirList() {
		if (subdirList == null) {
			subdirList = new Vector();
		}
		return subdirList;
	}

	/* (non-Javadoc)
	 * @param subDir
	 */
	private void removeGeneratedDirectory(IContainer subDir) {
		try {
			// The source directory isn't empty
			if (subDir.exists() && subDir.members().length > 0) return;
		} catch (CoreException e) {
			// The resource doesn't exist so we should delete the output folder
		}
		
		// Figure out what the generated directory name is and delete it
		IPath moduleRelativePath = subDir.getProjectRelativePath();
		IPath buildRoot = getBuildWorkingDir();
		if (buildRoot == null) {
			return;
		}
		IPath moduleOutputPath = buildRoot.append(moduleRelativePath);
		IFolder folder = project.getFolder(moduleOutputPath);
		if (folder.exists()) {
			try {
				folder.delete(true, new SubProgressMonitor(monitor, 1));
			} catch (CoreException e) {
				// TODO Log this
			}
		}		
	}

	/* (non-Javadoc)
	 * @param msg
	 */
	protected void updateMonitor(String msg) {
		if (monitor!= null && !monitor.isCanceled()) {
			monitor.subTask(msg);
			monitor.worked(1);
		}
	}

	/**
	 * Return the configuration's top build directory 
	 */
	public IPath getTopBuildDir() {
		return project.getLocation().append(getBuildWorkingDir());
	}

	/**
	 * Calculate a relative path given the full path to a folder and a file 
	 */
	public IPath calculateRelativePath(IPath container, IPath contents){
		IPath path = contents;
		if(container.isPrefixOf(contents)){
			path = contents.setDevice(null).removeFirstSegments(container.segmentCount());
		} else {
			String file = null;
			container = container.addTrailingSeparator();
			if(!contents.hasTrailingSeparator()){
				file = contents.lastSegment();
				contents = contents.removeLastSegments(1);
				contents = contents.addTrailingSeparator();
			}
			
			IPath prefix = contents;
			for(;prefix.segmentCount() > 0 && !prefix.isPrefixOf(container);prefix = prefix.removeLastSegments(1)){
			}
			if(prefix.segmentCount() > 0){
				int diff = container.segmentCount() - prefix.segmentCount();
				StringBuffer buff = new StringBuffer();
				while(diff-- > 0)
					buff.append("../");	//$NON-NLS-1$
				path = new Path(buff.toString()).append(contents.removeFirstSegments(prefix.segmentCount()));
				if(file != null)
					path = path.append(file);
			}
		}
		return path;
	}

}
