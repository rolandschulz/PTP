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
package org.eclipse.ptp.cell.managedbuilder.makegen.gnu;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.internal.core.model.Util;
import org.eclipse.cdt.managedbuilder.core.IBuilder;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.macros.IBuildMacroProvider;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ptp.cell.managedbuilder.core.ManagedBuilderCorePlugin;
import org.eclipse.ptp.cell.managedbuilder.debug.Debug;
import org.eclipse.ptp.cell.managedbuilder.internal.core.ManagedMakeMessages;
import org.eclipse.ptp.cell.managedbuilder.makegen.IManagedBuilderMakefileGenerator;


/**
 * This is a specialized makefile generator that takes advantage of the
 * extensions present in Gnu Make.
 * 
 * @author laggarcia
 * @since 1.1.0
 */
public class GnuMakefileGenerator extends
		org.eclipse.cdt.managedbuilder.makegen.gnu.GnuMakefileGenerator
		implements IManagedBuilderMakefileGenerator {

	protected static final String MESSAGE_COPY_FILE = ManagedMakeMessages.copyFile;

	protected static final String MESSAGE_CUSTOM_MAKEFILE_INCLUDES = ManagedMakeMessages.customMakefileIncludes;

	protected static final String MESSAGE_MAKEFILE_INCLUDES = ManagedMakeMessages.makefileIncludes;

	protected static final String MESSAGE_TOPMAKEFILE_ERROR = ManagedMakeMessages.topMakefileError;

	protected static final String MESSAGE_FILE_DOESNT_EXIST = ManagedMakeMessages.fileDoesntExist;

	protected static final String DEFAULT_ANNOUNCEMENT_PREFIX = "Tool.default.announcement"; //$NON-NLS-1$

	protected final String SPU_EMBED_TOOL_ID_MATCHING = "embedspu"; //$NON-NLS-1$

	protected final String EMPTY_STRING = new String();

	protected final String PHONY = ".PHONY"; //$NON-NLS-1$

	protected final String LINUX = "linux"; //$NON-NLS-1$

	protected static final String COPY_MACRO = "CP"; //$NON-NLS-1$

	protected static final String LINUX_COPY_COMMAND = "cp"; //$NON-NLS-1$

	private IProject project;

	private IProgressMonitor monitor;

	private String[] spuExecutables;

	private boolean[] spuExecutablesExist;
	
	private String[] spuObjTargets;

	private String embedSPUToolPrimaryInputExtension;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.managedbuilder.makegen.IManagedBuilderMakefileGenerator#initialize()
	 * 
	 * @param project @param info @param monitor
	 */
	public void initialize(int buildKind, IConfiguration cfg, IBuilder builder,
			IProgressMonitor monitor) {
		Debug.read();
		Debug.POLICY.enter(Debug.DEBUG_MAKEFILE, cfg.getId(), builder.getId());

		this.project = cfg.getOwner().getProject();
		this.monitor = monitor;

		Debug.POLICY.trace(Debug.DEBUG_MAKEFILE, "Project: {0}", project.getName()); //$NON-NLS-1$
		
		super.initialize(buildKind, cfg, builder, monitor);

		ITool[] buildTools = cfg.getFilteredTools();

		// Getting the PPU GNU Embed SPU Tool
		// Usually CDT's makefile generator gets the tool from the extension of
		// the input file.
		// However, in our case, we don't yet have the files in our project as
		// we are going to copy (import) them.
		// The solution was to test the tools ids and find the one that matches
		// with a substring contained in the Embed SPU tool id.
		for (int i = 0; i < buildTools.length; i++) {
			if (buildTools[i].getId().indexOf(SPU_EMBED_TOOL_ID_MATCHING) > -1) {
				ITool embedSPUTool = buildTools[i];

				// Retrieving the primary input extension of the Embed SPU Tool
				// for later use.
				// It was defined just one primary input extension for Embed
				// SPU, so let's use it directly.
				// Usually CDT's makefile generator gets the input extension
				// from the file extension.
				// However, in our case, we don't yet have the files in our
				// project as we are going to copy them.
				// We would be able to use the original files, but they actually
				// don't need to have any defined extension as they are
				// probably, but not necessarily, executables.
				// During the copy we will set the files extensions to the
				// primary input file extension defined in the Embed SPU Tool.
				embedSPUToolPrimaryInputExtension = embedSPUTool
						.getPrimaryInputExtensions()[0];

				Debug.POLICY.trace(Debug.DEBUG_MAKEFILE, "embedded spu tool: {0}; extension: {1}", embedSPUTool.getId(), embedSPUToolPrimaryInputExtension); //$NON-NLS-1$

				// You can define an InputType as being defined by an option.
				// However, CDT's makefile generator justs pay attention to this
				// when you are processing a target tool or a tool with
				// multipleOfTypes InputType or when you are dealing with
				// Secondary Inputs.
				// As we want to use this feature for a primary InputType of
				// Embed SPU, let's do the resolve work.
				IOption option = embedSPUTool
						.getOptionBySuperClassId(embedSPUTool
								.getPrimaryInputType().getOptionId());
				try {
					// The Embed SPU Inputs option was defined as being of the
					// type STRING_LIST, so we will not test the other types.
					if (option.getValueType() == IOption.STRING_LIST) {

						String spuObj;
						spuExecutables = option.getStringListValue();
						spuExecutablesExist = new boolean[spuExecutables.length];
						spuObjTargets = new String[spuExecutables.length];
						for (int j = 0; j < spuExecutables.length; j++) {
							// Resolve macro references
							spuObj = spuExecutables[j];
							spuExecutables[j] = ManagedBuildManager
									.getBuildMacroProvider()
									.resolveValueToMakefileFormat(
											spuExecutables[j],
											EMPTY_STRING,
											WHITESPACE,
											IBuildMacroProvider.CONTEXT_CONFIGURATION,
											cfg);
							spuExecutables[j] = spuExecutables[j].replaceAll("\"", //$NON-NLS-1$
									EMPTY_STRING);
							File spuObjFile = new File(spuExecutables[j]);
							if (!(spuExecutablesExist[j] = spuObjFile.exists())) {
								// The files doesn't exist!
								spuExecutables[j] = spuObj;
								spuObjTargets[j] = null;
							} else {
								// The file exists!
								spuObjTargets[j] = spuExecutables[j]
										.substring(spuExecutables[j]
												.lastIndexOf('/') + 1)
										+ DOT
										+ embedSPUToolPrimaryInputExtension;
							}
							Debug.POLICY.trace(Debug.DEBUG_MAKEFILE, "Executable: ''{0}''; Target: ''{1}''; Exist: ''{2}''", //$NON-NLS-1$
									spuExecutables[j], spuObjTargets[j], spuExecutablesExist[j]);
						}
					}
					// In the case someone changes the option value type.
					else {
						// TODO handle this exception
						Debug.POLICY.error(Debug.DEBUG_MAKEFILE, "Invalid sputiming option: {0}", option.getId()); //$NON-NLS-1$
						spuExecutables = null;						
					}
				} catch (Exception e) {
					Debug.POLICY.error(Debug.DEBUG_MAKEFILE, e);
					Debug.POLICY.logError(e);
					spuExecutables = null;
				}
				break;
			}
		}
		Debug.POLICY.exit(Debug.DEBUG_MAKEFILE);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.managedbuilder.makegen.IManagedBuilderMakefileGenerator#generateMakefiles(org.eclipse.core.resources.IResourceDelta)
	 */
	public MultiStatus generateMakefiles(IResourceDelta delta)
			throws CoreException {
		Debug.read();
		Debug.POLICY.enter(Debug.DEBUG_MAKEFILE);

		createSPUObjsDummyFiles();
		checkCancel();

		MultiStatus status = super.generateMakefiles(delta);

		populateImportSPUObjsMakefile();
		checkCancel();

		Debug.POLICY.exit(Debug.DEBUG_MAKEFILE, status.toString());
		return status;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.managedbuilder.makegen.IManagedBuilderMakefileGenerator#regenerateMakefiles()
	 */
	public MultiStatus regenerateMakefiles() throws CoreException {
		Debug.read();
		Debug.POLICY.enter(Debug.DEBUG_MAKEFILE);

		createSPUObjsDummyFiles();
		checkCancel();

		MultiStatus status = super.regenerateMakefiles();

		populateImportSPUObjsMakefile();
		checkCancel();

		Debug.POLICY.exit(Debug.DEBUG_MAKEFILE, status.toString());
		return status;
	}

	/*
	 * (non-Javadoc) Create the entire contents of the makefile.
	 * 
	 * @param fileHandle The file to place the contents in. @param rebuild FLag
	 * signalling that the user is doing a full rebuild @throws CoreException
	 */
	protected void populateTopMakefile(IFile fileHandle, boolean rebuild)
			throws CoreException {
		Debug.read();
		Debug.POLICY.enter(Debug.DEBUG_MAKEFILE, fileHandle.getFullPath().toOSString());

		StringBuffer buffer = new StringBuffer();

		// Add the header
		buffer.append(addCustomTopHeader());

		// Add custom macros and inclues for Cell
		buffer.append(addMacros());

		super.populateTopMakefile(fileHandle, rebuild);

		// As we don't have access to the private methods used in
		// super.populateTopMakefile(IFile, boolean)
		// we called it and now we will read the contents of the file generated
		// by it and add a prologue
		// and an epilogue.
		try {
			buffer.append(Util.getContent(fileHandle));
		} catch (IOException ioe) {
			outputCommentLine(buffer);
			buffer.append(COMMENT_SYMBOL + WHITESPACE
					+ MESSAGE_TOPMAKEFILE_ERROR + NEWLINE);
			outputCommentLine(buffer);
			Debug.POLICY.error(Debug.DEBUG_MAKEFILE, ioe);
			Debug.POLICY.logStatus(new Status(IStatus.INFO, ManagedBuilderCorePlugin.getDefault().getBundle().getSymbolicName(), 0, fileHandle.getFullPath().toOSString(), ioe));
		}

		Util.save(buffer, fileHandle);
		Debug.POLICY.exit(Debug.DEBUG_MAKEFILE);
	}

	protected StringBuffer addCustomTopHeader() {
		Debug.read();
		Debug.POLICY.pass(Debug.DEBUG_MAKEFILE);
		return addDefaultHeader();
	}

	protected StringBuffer addTopHeader() {
		Debug.read();
		Debug.POLICY.pass(Debug.DEBUG_MAKEFILE);
		// Overrides the addTopHeader so that super.populateTopMakefile(IFile,
		// boolean) doesn't print any header
		// The header for our topMakefile will be defined by addTopHeader
		return new StringBuffer();
	}

	/*
	 * (non-javadoc)
	 */
	protected StringBuffer addMacros() {
		Debug.read();
		Debug.POLICY.pass(Debug.DEBUG_MAKEFILE);
		
		StringBuffer buffer = new StringBuffer();

		buffer.append(COMMENT_SYMBOL + WHITESPACE
				+ MESSAGE_CUSTOM_MAKEFILE_INCLUDES + NEWLINE);

		// Add the custom includes for Cell

		// Include the makefile that will copy the imported SPU files
		buffer.append("-include " + IMPORT_SPU_OBJS_MAKEFILE_NAME + NEWLINE //$NON-NLS-1$
				+ NEWLINE);

		buffer.append(COMMENT_SYMBOL + WHITESPACE + MESSAGE_MAKEFILE_INCLUDES
				+ NEWLINE);

		return (buffer.append(NEWLINE));
	}

	/**
	 * The makefile generator generates a file that will copy all the spu object
	 * files that are going to be embeded in the ppu application.
	 * 
	 * The rule will have the following format:
	 * 
	 * <pre>
	 *  &lt;relative_path&gt;/&lt;filename&gt;.spuo: &lt;filepath&gt;
	 *  	@echo 'Copying file: $&lt;'
	 *  	@echo 'Invoking: $(CP)'
	 *  	@echo '$(CP) $&lt; $@'
	 *  	@$(CP) $&lt; $@
	 *  	@echo 'Finished building: $@'
	 *  	@echo ' '
	 * </pre>
	 * 
	 * @param fileHandle
	 *            The file that should be populated with the output
	 * @throws CoreException
	 */
	protected void populateImportSPUObjsMakefile() throws CoreException {
		Debug.read();
		Debug.POLICY.enter(Debug.DEBUG_MAKEFILE);
		
		if (spuExecutables == null) {
			Debug.POLICY.exit(Debug.DEBUG_MAKEFILE);
			return;
		}

		// When no file is set, the spuExecutables will be set with
		// EMPTY_STRING_ARRAY = new String[0];
		// Test this.
		if (spuExecutables.length == 0) {
			Debug.POLICY.exit(Debug.DEBUG_MAKEFILE);
			return;
		}

		IPath importSPUObjsMakefilePath = getTopBuildDir().append(
				IMPORT_SPU_OBJS_MAKEFILE_NAME);
		IFile importSPUObjsMakefileHandle = createFile(importSPUObjsMakefilePath);

		StringBuffer buffer = new StringBuffer();

		buffer.append(addImportSPUObjsMakefileHeader());

		// Check the OS to define the right copy command
		if (Platform.getOS().equals(LINUX)) {
			// For now, just linux is supported
			buffer.append(COPY_MACRO + WHITESPACE + ":=" + WHITESPACE //$NON-NLS-1$
					+ LINUX_COPY_COMMAND + NEWLINE + NEWLINE);
		}

		// Create the rules for copying each of the imported spu objects
		for (int i = 0; i < spuExecutables.length; i++) {
			if (spuExecutablesExist[i]) {
				Debug.POLICY.trace(Debug.DEBUG_MAKEFILE, "Rule for: {0}->{1}",  spuExecutables[i],  spuObjTargets[i]); //$NON-NLS-1$
				buffer.append(ROOT + SEPARATOR + SPU_OBJS_DIR_NAME + SEPARATOR
						+ spuObjTargets[i] + COLON + WHITESPACE
						+ spuExecutables[i] + WHITESPACE + NEWLINE);
				buffer
						.append(TAB
								+ AT
								+ escapedEcho(MESSAGE_COPY_FILE + WHITESPACE
										+ IN_MACRO));
				buffer
						.append(TAB
								+ AT
								+ escapedEcho(org.eclipse.cdt.managedbuilder.internal.core.ManagedMakeMessages
										.getResourceString(DEFAULT_ANNOUNCEMENT_PREFIX)
										+ WHITESPACE + DOLLAR_SYMBOL + "(" //$NON-NLS-1$
										+ COPY_MACRO + ")")); //$NON-NLS-1$
				// Building the copy command
				String command = new String(DOLLAR_SYMBOL
						+ "(" + COPY_MACRO + ")" //$NON-NLS-1$ //$NON-NLS-2$
						+ WHITESPACE + IN_MACRO + WHITESPACE + OUT_MACRO);
				buffer.append(TAB + AT + escapedEcho(command));
				buffer.append(TAB + AT + command + NEWLINE);
				// Echo finished message
				buffer.append(TAB
						+ AT
						+ escapedEcho(MESSAGE_FINISH_FILE + WHITESPACE
								+ OUT_MACRO));
				buffer.append(TAB + AT + ECHO_BLANK_LINE + NEWLINE + NEWLINE);
			} else {
				Debug.POLICY.trace(Debug.DEBUG_MAKEFILE, "Ignored: {0}",  spuExecutables[i]); //$NON-NLS-1$
			}
		}

		// Forcing the execution of these rules
		buffer.append(PHONY + COLON);
		for (int i = 0; i < spuExecutables.length; i++) {
			if (spuExecutablesExist[i]) {
				buffer.append(WHITESPACE + ROOT + SEPARATOR + SPU_OBJS_DIR_NAME
						+ SEPARATOR + spuObjTargets[i]);
			}
		}
		buffer.append(NEWLINE);

		// Save the buffer that was populated with the import files commands.
		Util.save(buffer, importSPUObjsMakefileHandle);
		
		Debug.POLICY.exit(Debug.DEBUG_MAKEFILE);
	}

	protected StringBuffer addImportSPUObjsMakefileHeader() {
		Debug.read();
		Debug.POLICY.pass(Debug.DEBUG_MAKEFILE);
		return addDefaultHeader();
	}

	/*
	 * This is a hack to make the makefile generator understands that it has to
	 * create a makefile to process the imported embed spu files.
	 * 
	 * The best to do in this case should be to use symbolic links to the real
	 * files pointed in the Inputs Option in the Embed SPU Tool or to use the
	 * Inputs Option as the Input defined in the option attribute in the
	 * InputType description of the Embed SPU Tool. However, java.io.File does
	 * not support symbolic links, and although Eclipes allows symbolic links in
	 * the workspace, they are not valid in the file system, and because of
	 * that, they are useless for make invocation. In the other side, option
	 * attribute in the InputType could be used, but it would be necessary to
	 * rewrite lots of code due to the fact that
	 * org.eclipse.cdt.managedbuilder.makegen.gnu.GnuMakefileGenerator doesn't
	 * create rules automaticaly to inputs described this way, just for inputs
	 * that exists in the workspace. The decision was to reuse the most we can.
	 * 
	 */
	protected void createSPUObjsDummyFiles() throws CoreException {
		Debug.read();
		Debug.POLICY.enter(Debug.DEBUG_MAKEFILE);

		//Remove the SPU_OBJS_DIR_NAME so that we remove any no more used files that can be processed by Embed SPU tool
		removeDirectory(SPU_OBJS_DIR_NAME);

		if (spuExecutables == null) {
			Debug.POLICY.exit(Debug.DEBUG_MAKEFILE);
			return;
		}

		// When no file is set, the spuExecutables will be set with EMPTY_STRING_ARRAY
		// = new String[0];
		// Test this.
		if (spuExecutables.length == 0) {
			Debug.POLICY.exit(Debug.DEBUG_MAKEFILE);
			return;
		}

		// Create spu_objs "source" directory to be the default directory to
		// receive the imported embed spu object files.
		// TODO: Mark the spu_objs dir as a source dir
		IPath spuObjsSourceDir = createDirectory(SPU_OBJS_DIR_NAME);

		// The rule creation engine just adds a rule if a file that matches a
		// rule exists.
		// In this case, however, the imported object files with embed spu code
		// will exist just in build time.
		// We have to create dumy spuo files so that the rule is correctly
		// created.
		// Before creating the file, we have to change the file extension of the file
		// referenced in the Inputs option of the Embed SPU Tool.
		for (int i = 0; i < spuExecutables.length; i++) {
			if (!spuExecutablesExist[i]) {
				Debug.POLICY.trace(Debug.DEBUG_MAKEFILE, "SPU executable does not exist: {0}",  spuExecutables[i]); //$NON-NLS-1$
				throw new CoreException(new Status(IStatus.ERROR,
						ManagedBuilderCorePlugin.getUniqueIdentifier(),
						FILE_DOESNT_EXIST, NLS.bind(MESSAGE_FILE_DOESNT_EXIST,
								spuExecutables[i]), null));
			}
			else {
				Debug.POLICY.trace(Debug.DEBUG_MAKEFILE, "Create dummy file for: {0}->{1}",  spuExecutables[i],  spuObjTargets[i]); //$NON-NLS-1$
				createFile(spuObjsSourceDir.append(spuObjTargets[i]));
			}
		}
		Debug.POLICY.exit(Debug.DEBUG_MAKEFILE);
	}

	/**
	 * Return or create the folder in the project directory.
	 * If the folder is created, set the derived bit to true so the
	 * CM system ignores the contents. If the resource exists,
	 * respect the existing derived setting.
	 * 
	 * @param string @return IPath
	 */
	private IPath createDirectory(String dirName) throws CoreException {
		// Create or get the handle for the directory
		IFolder folder = project.getFolder(dirName);
		if (!folder.exists()) {
			try {
				folder.create(true, true, new SubProgressMonitor(monitor, 1));
			} catch (CoreException e) {
				// If the folder already existed locally, just refresh to get
				// contents
				if (e.getStatus().getCode() == IResourceStatus.PATH_OCCUPIED)
					folder.refreshLocal(IResource.DEPTH_ZERO, null);
				else
					throw e;
			}

			if (!folder.isDerived()) {
				folder.setDerived(true);
			}
		}

		return folder.getFullPath();
	}

	private void removeDirectory(String dirName) throws CoreException {
		// Create or get the handle for the directory
		IFolder folder = project.getFolder(dirName);
		if (folder.exists()) {
			folder.delete(true, new SubProgressMonitor(monitor, 1));
		}

	}

	/**
	 * Create the file if it doesn't exist. If the resource is created,
	 * set the derived bit to true so the CM system ignores the contents.
	 * If the resource exists, respect the existing derived setting.
	 * 
	 * @param filePath @return IFile
	 */
	private IFile createFile(IPath filePath) throws CoreException {
		// Create or get the handle for the file
		IWorkspaceRoot root = CCorePlugin.getWorkspace().getRoot();
		// try to get the file if filePath is an absolute path
		IFile file = root.getFileForLocation(filePath);
		if (file == null) {
			// if filePath is not absolute, get the relative path
			file = root.getFile(filePath);
		}
		if (!file.exists()) {
			try {
				file.create(new ByteArrayInputStream(new byte[0]), false,
						new SubProgressMonitor(monitor, 1));
			} catch (CoreException e) {
				// If the file already existed locally, just refresh to get
				// contents
				if (e.getStatus().getCode() == IResourceStatus.PATH_OCCUPIED)
					file.refreshLocal(IResource.DEPTH_ZERO, null);
				else
					throw e;
			}

			// Make sure the new file is marked as derived
			if (!file.isDerived()) {
				file.setDerived(true);
			}
		}

		return file;
	}

}