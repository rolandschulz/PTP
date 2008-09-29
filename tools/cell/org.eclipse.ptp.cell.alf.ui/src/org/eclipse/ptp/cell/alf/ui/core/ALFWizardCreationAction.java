/******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *
 *****************************************************************************/

package org.eclipse.ptp.cell.alf.ui.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ptp.cell.alf.ui.Messages;
import org.eclipse.ptp.cell.alf.ui.debug.Debug;
import org.eclipse.ptp.cell.alf.ui.wizard.ALFWizard;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.ICDescriptor;
import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.IManagedOptionValueHandler;
import org.eclipse.cdt.managedbuilder.core.IManagedProject;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.IProjectType;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.core.ManagedBuilderCorePlugin;
import org.eclipse.cdt.managedbuilder.core.ManagedCProjectNature;
import org.eclipse.cdt.managedbuilder.internal.core.ManagedBuildInfo;


/**
 * This class implements the back-end actions that are to be performed after the user has input all of the necessary parameters
 * 
 * @author Sean Curry
 * @since 3.0.0
 */
public class ALFWizardCreationAction implements IRunnableWithProgress {

	private static String PATH_SEPERATOR = "/"; //$NON-NLS-1$
	private static String SPACE = " "; //$NON-NLS-1$
	
	private ALFWizard wizard;
	
	private IPath projectLocation;
	private String projectName;
	private int stackSize;
	private int expAccelNum;
	private int partitionMethod;
	private ArrayList buffers;
	private boolean is64bit;
	
	public ALFWizardCreationAction(ALFWizard wizard, IPath projectLocation, String projectName, int stackSize, int expAccelNum, int partitionMethod, ArrayList buffers, boolean is64bit){
		this.wizard = wizard;
		this.projectLocation = projectLocation;
		this.projectName = projectName;
		this.stackSize = stackSize;
		this.expAccelNum = expAccelNum;
		this.partitionMethod = partitionMethod;
		this.buffers = buffers;
		this.is64bit = is64bit;
	}
	
	public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		Debug.read();
		Debug.POLICY.enter(Debug.DEBUG_CREATE_ACTION);
		boolean wasCanceled = false;
		
		File tmpDir = null;
		
		File xmlParameterFile = null;
		
		IProject ppuProject = null;
		IProject spuProject = null;
		IProject libraryProject = null;
		
		try{
			
			String date = getDate();

			monitor.beginTask(Messages.ALFWizardCreationAction_mainTask, 9);
			monitor.setTaskName(Messages.ALFWizardCreationAction_taskName);
			monitor.subTask(Messages.ALFWizardCreationAction_subTask1);

			/* **************************** *
			 * Make the temporary directory *
			 * **************************** */
			String tmpDirPath = Messages.ALFWizardCreationAction_tmpDirLocation + date;
			tmpDir = new File(tmpDirPath);
			Debug.POLICY.trace(Debug.DEBUG_CREATE_ACTION, "Create tmp dir: ''{0}''", tmpDir.toString()); //$NON-NLS-1$
			if(!tmpDir.exists())
				tmpDir.mkdir();
			

			/* ***************************** *
			 * Create the parameter XML file *
			 * ***************************** */
			Debug.POLICY.trace(Debug.DEBUG_CREATE_ACTION, "Create the parameter XML file."); //$NON-NLS-1$
			Document xmlParameterFileDocument = createXMLParameterDocument(tmpDir.getAbsolutePath(), date);
			xmlParameterFile = createXMLParameterFile(xmlParameterFileDocument, tmpDir);
	        
	        if(monitor.isCanceled())
	        	throw new InterruptedException(Messages.ALFWizardCreationAction_canceled);

	        /* ****************************************************************** *
	         * Call the code generator and pass it the parameter description file *
	         * ****************************************************************** */
			Debug.POLICY.trace(Debug.DEBUG_CREATE_ACTION, "Call the code generator and pass it the parameter description file."); //$NON-NLS-1$
	        monitor.worked(1);
	        monitor.subTask(Messages.ALFWizardCreationAction_subTask2);
	        
	        // make sure the path to the code generator executable exists. 
	        // prompt the user for the correct path if it does not exist.
	        String codeGenLocation = Messages.ALFWizardCreationAction_codeGeneratorCommand;
	        File codeGenFile = new File(Messages.ALFWizardCreationAction_codeGeneratorCommand);
	        
	        if(!codeGenFile.exists()){
	        	codeGenLocation = wizard.queryPathLocation(Messages.ALFWizardCreationAction_queryTitle, Messages.ALFWizardCreationAction_queryCodeGenPath, 
	        			Messages.ALFWizardCreationAction_codeGeneratorCommand);
	        }
	        if(codeGenLocation == null)
	        	throw new InvocationTargetException(new Exception(), Messages.ALFWizardCreationAction_errorCallingCodeGenMsg);
	        
			Process codeGen = Runtime.getRuntime().exec(codeGenLocation + SPACE + Messages.ALFWizardCreationAction_codeGeneratorOptions + 
														SPACE + xmlParameterFile.getAbsolutePath());
			codeGen.waitFor();
			
			if(monitor.isCanceled())
	        	throw new InterruptedException(Messages.ALFWizardCreationAction_canceled);
			
			monitor.worked(1);
			
		    /* ************************************************************************* *
		     * Create the host (PPU), PPU Shared Library, and accelerator (SPU) projects *
			 * ************************************************************************* */
			Debug.POLICY.trace(Debug.DEBUG_CREATE_ACTION, "Create the host (PPU), PPU Shared Library, and accelerator (SPU) projects."); //$NON-NLS-1$

			// Create the PPU executable project
			Debug.POLICY.trace(Debug.DEBUG_CREATE_ACTION, "Create the PPU executable project."); //$NON-NLS-1$
			monitor.subTask(Messages.ALFWizardCreationAction_subTask3);
			ppuProject = createProject("ppu_" + projectName, "cell.managedbuild.target.cell.ppu.exe"); //$NON-NLS-1$//$NON-NLS-2$
			monitor.worked(1);
			
			if(monitor.isCanceled())
	        	throw new InterruptedException(Messages.ALFWizardCreationAction_canceled);
			
			// Create the PPU shared library project
			Debug.POLICY.trace(Debug.DEBUG_CREATE_ACTION, "Create the PPU shared library project."); //$NON-NLS-1$
			monitor.subTask(Messages.ALFWizardCreationAction_subTask4);
			libraryProject = createProject("lib" + projectName, "cell.managedbuild.target.cell.ppu.so"); //$NON-NLS-1$//$NON-NLS-2$
			monitor.worked(1);
			
			if(monitor.isCanceled())
	        	throw new InterruptedException(Messages.ALFWizardCreationAction_canceled);
			
			// Create the SPU executable project
			Debug.POLICY.trace(Debug.DEBUG_CREATE_ACTION, "Create the SPU executable project."); //$NON-NLS-1$
			monitor.subTask(Messages.ALFWizardCreationAction_subTask5);
			spuProject = createProject("spu_" + projectName, "cell.managedbuild.target.cell.spu.exe"); //$NON-NLS-1$//$NON-NLS-2$
			monitor.worked(1);
			
			if(monitor.isCanceled())
	        	throw new InterruptedException(Messages.ALFWizardCreationAction_canceled);

			
			/* **************************************************** *
			 * Import the source and header files into the projects *
			 * **************************************************** */
			Debug.POLICY.trace(Debug.DEBUG_CREATE_ACTION, "Import the source and header files into the projects."); //$NON-NLS-1$
			monitor.subTask(Messages.ALFWizardCreationAction_subTask6);
			importFiles(monitor, tmpDirPath, ppuProject, spuProject);
			monitor.worked(1);
			

			/* ********************** *
			 * Configure the projects *
			 * ********************** */	
			Debug.POLICY.trace(Debug.DEBUG_CREATE_ACTION, "Configure the projects."); //$NON-NLS-1$

			// Configure SPU executable project
			Debug.POLICY.trace(Debug.DEBUG_CREATE_ACTION, "Configure SPU executable project."); //$NON-NLS-1$
			monitor.subTask(Messages.ALFWizardCreationAction_subTask7);
			configureSPUProject(spuProject, ppuProject.getName());
			try{ spuProject.refreshLocal(2, null); } catch(CoreException e){ 
				Debug.POLICY.error(Debug.DEBUG_CREATE_ACTION_MORE, e);
				/* Do nothing */ 
			}
			if(monitor.isCanceled())
				throw new InterruptedException(Messages.ALFWizardCreationAction_canceled);
			monitor.worked(1);				

			// Configure PPU shared library project
			Debug.POLICY.trace(Debug.DEBUG_CREATE_ACTION, "Configure PPU shared library project."); //$NON-NLS-1$
			monitor.subTask(Messages.ALFWizardCreationAction_subTask8);
			try{ // Add the SPU project to the list of referenced projects				
				IProjectDescription projectDescription = libraryProject.getDescription();
				projectDescription.setReferencedProjects(new IProject[]{spuProject});
				libraryProject.setDescription(projectDescription, null);				
			} catch (Exception e){ 
				Debug.POLICY.error(Debug.DEBUG_CREATE_ACTION_MORE, e);
				Debug.POLICY.logError(e);
			}
			configureSharedLibraryProject32bit(libraryProject, spuProject.getName());
			configureSharedLibraryProject64bit(libraryProject, spuProject.getName());
			
			try{ libraryProject.refreshLocal(2, null); } catch(CoreException e){ 
				Debug.POLICY.error(Debug.DEBUG_CREATE_ACTION_MORE, e);
				Debug.POLICY.logError(e);
			}
			monitor.worked(1);
			if(monitor.isCanceled())
				throw new InterruptedException(Messages.ALFWizardCreationAction_canceled);				

			// Configure the PPU executable project
			Debug.POLICY.trace(Debug.DEBUG_CREATE_ACTION, "Configure the PPU executable project."); //$NON-NLS-1$
			monitor.subTask(Messages.ALFWizardCreationAction_subTask9);
			try{ // Add the shared library project to the list of referenced projects
				IProjectDescription projectDescription = ppuProject.getDescription();
				projectDescription.setReferencedProjects(new IProject[]{libraryProject});
				ppuProject.setDescription(projectDescription, null);
			} catch (Exception e){
				Debug.POLICY.error(Debug.DEBUG_CREATE_ACTION_MORE, e);
				/* Do nothing */
			}
			configurePPUProject32bit(ppuProject, libraryProject.getName(),
					spuProject.getName());
			configurePPUProject64bit(ppuProject, libraryProject.getName(),
					spuProject.getName());
			try {
				ppuProject.refreshLocal(2, null);
			} catch (CoreException e) {
				Debug.POLICY.error(Debug.DEBUG_CREATE_ACTION_MORE, e);
				Debug.POLICY.logError(e);
			}
			monitor.worked(1);
			if(monitor.isCanceled())
				throw new InterruptedException(Messages.ALFWizardCreationAction_canceled);

			// Build the projects
			//try{ spuProject.build(IncrementalProjectBuilder.FULL_BUILD, null); } catch(CoreException e){ /* Do nothing */ }
			//try{ libraryProject.build(IncrementalProjectBuilder.FULL_BUILD, null); } catch(CoreException e){ /* Do nothing */ }
			//try{ ppuProject.build(IncrementalProjectBuilder.FULL_BUILD, null); } catch(CoreException e){ /* Do nothing */ }	

		} catch (FactoryConfigurationError e){
			Debug.POLICY.error(Debug.DEBUG_CREATE_ACTION, e);
			throw new InvocationTargetException(e, Messages.ALFWizardCreationAction_errorCreatingParamFileMsg);
		} catch (ParserConfigurationException e){
			Debug.POLICY.error(Debug.DEBUG_CREATE_ACTION, e);
			throw new InvocationTargetException(e, Messages.ALFWizardCreationAction_errorCreatingParamFileMsg);
		} catch (TransformerException e){
			Debug.POLICY.error(Debug.DEBUG_CREATE_ACTION, e);
			throw new InvocationTargetException(e, Messages.ALFWizardCreationAction_errorCreatingParamFileMsg);
		} catch (IOException e) {
			Debug.POLICY.error(Debug.DEBUG_CREATE_ACTION, e);
			throw new InvocationTargetException(e, Messages.ALFWizardCreationAction_errorCallingCodeGenMsg);
	    } catch (InterruptedException e) {
	    	wasCanceled = true;
	    	throw e;
	    }
		finally{
			Debug.POLICY.trace(Debug.DEBUG_CREATE_ACTION, "Removed tmp dir: ''{0}''", tmpDir.toString()); //$NON-NLS-1$
			if (xmlParameterFile != null) {
				xmlParameterFile.delete();
			}
			if (tmpDir != null) {
				deleteDir(tmpDir);
			}
			if (wasCanceled) {
				if (ppuProject != null) {
					try{
						ppuProject.delete(true, true, null);
					} catch (CoreException e){
						Debug.POLICY.error(Debug.DEBUG_CREATE_ACTION, e);
						Debug.POLICY.logError(e);
					}
				}
				if (spuProject != null) {
					try{
						spuProject.delete(true, true, null);
					} catch (CoreException e){
						Debug.POLICY.error(Debug.DEBUG_CREATE_ACTION, e);
						Debug.POLICY.logError(e);
					}
				}
				if (libraryProject != null) {
					try{
						libraryProject.delete(true, true, null);
					} catch (CoreException e){
						Debug.POLICY.error(Debug.DEBUG_CREATE_ACTION, e);
						Debug.POLICY.logError(e);
					}
				}
			}
		}
		Debug.POLICY.exit(Debug.DEBUG_CREATE_ACTION);
	}

	/**
	 * Configures the build properties of the host (PPU) project's 32bit GNU configuration to include the necessary libraries, look-up paths, and embed-SPU input.
	 * 
	 * @param ppuProject the PPU project to configure
	 * @param spuProjectName the name of the SPU project, so the embed SPU input can be correctly specified
	 * @return true if the options were successfully saved in the configuration, else false
	 */
	private boolean configurePPUProject32bit(IProject ppuProject, String libProjectName, String spuProjectName){
		Debug.POLICY.enter(Debug.DEBUG_CREATE_ACTION_MORE, ppuProject, libProjectName, spuProjectName);
		boolean saveBuildStatus = false;
//		try{
			// Get the 32bit GNU Debug configuration 
			IConfiguration[] configs = ManagedBuildManager.getBuildInfo(ppuProject).getManagedProject().getConfigurations();
			IConfiguration configuration = null;
			for(int i = 0; i < configs.length; i++){
				if(configs[i].getId().startsWith("cell.managedbuild.config.cell.ppu.gnu32.exe.debug")){ //$NON-NLS-1$
					configuration = configs[i];
					break;
				}
			}
			
			// Get the necessary toolchain to add the options to
			ManagedBuildManager.performValueHandlerEvent(configuration, IManagedOptionValueHandler.EVENT_OPEN, true);
			IToolChain toolChain = configuration.getToolChain();

			/* *************************************************************** *
			 * Add the necessary libraries needed by this PPU project's linker *
			 * *************************************************************** */
			ITool linkerTool = toolChain.getToolsBySuperClassId("cell.managedbuild.tool.cell.ppu.gnu32.c.linker")[0]; //$NON-NLS-1$
			
			IOption libraryOption = linkerTool.getOptionBySuperClassId("cell.ppu.gnu.c.linker.option.libs"); //$NON-NLS-1$
			IOption librarySearchPathOption = linkerTool.getOptionBySuperClassId("gnu.c.link.option.paths"); //$NON-NLS-1$
		
			ManagedBuildManager.setOption(configuration, linkerTool, libraryOption, new String[]{"dl", "pthread", "spe2", "alf"}); //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$//$NON-NLS-4$
			ManagedBuildManager.setOption(configuration, linkerTool, librarySearchPathOption, new String[]{"/opt/cell/sysroot/usr/lib"}); //$NON-NLS-1$


			/* ********************************************************************* *
			 * Add the necessary include paths needed by this PPU project's compiler *
			 * ********************************************************************* */
			ITool compilerTool = toolChain.getToolsBySuperClassId("cell.managedbuild.tool.cell.ppu.gnu32.c.compiler.debug")[0]; //$NON-NLS-1$
			
			IOption includePathOption = compilerTool.getOptionBySuperClassId("gnu.c.compiler.option.include.paths"); //$NON-NLS-1$
			
			ManagedBuildManager.setOption(configuration, compilerTool, includePathOption, new String[]{"/opt/cell/sysroot/usr/include", "/opt/cell/sysroot/opt/cell/sdk/usr/include"}); //$NON-NLS-1$ //$NON-NLS-2$

			/* ************************************* *
			 * Save the configuration's new settings *
			 * ************************************* */
			// Write out the build model info
			saveBuildStatus = ManagedBuildManager.saveBuildInfo(ppuProject, true);
			IManagedBuildInfo bi = ManagedBuildManager.getBuildInfo(ppuProject);
			if(bi != null & bi instanceof ManagedBuildInfo)
				((ManagedBuildInfo)bi).initializePathEntries();
			if(!is64bit){
				ManagedBuildManager.setDefaultConfiguration(ppuProject, configuration);
				ManagedBuildManager.setSelectedConfiguration(ppuProject, configuration);
			}
			ManagedBuildManager.performValueHandlerEvent(configuration, IManagedOptionValueHandler.EVENT_APPLY, true);
			if(!saveBuildStatus) {
//				throw new Exception();
				Debug.POLICY.error(Debug.DEBUG_CREATE_ACTION_MORE, "Failed: {0}.", "configurePPUProject32bit"); //$NON-NLS-1$ //$NON-NLS-2$
				wizard.logWarningMessage(Messages.ALFWizardCreationAction_queryTitle, Messages.ALFWizardCreationAction_errorConfiguringPpuProject);
			}
			
//		} catch(Exception e){
//			Debug.POLICY.error(Debug.DEBUG_CREATE_ACTION_MORE, e);
//			wizard.logWarningMessage(Messages.ALFWizardCreationAction_queryTitle, Messages.ALFWizardCreationAction_errorConfiguringPpuProject);
//		}
		
		Debug.POLICY.exit(Debug.DEBUG_CREATE_ACTION_MORE, saveBuildStatus);
		return saveBuildStatus;
	}
	
	/**
	 * Configures the PPU projects 64bit GNU configuration. 
	 * 
	 * @param ppuProject the PPU project to configure
	 * @param spuProjectName the name of the SPU project
	 * @return true if the options were successfully saved in the configuration, else false
	 */
	private boolean configurePPUProject64bit(IProject ppuProject, String libProjectName, String spuProjectName){
		Debug.POLICY.enter(Debug.DEBUG_CREATE_ACTION_MORE, ppuProject, libProjectName, spuProjectName);
		boolean saveBuildStatus = false;
//		try{
			// Get the 64bit GNU Debug configuration 	
			IConfiguration[] configs = ManagedBuildManager.getBuildInfo(ppuProject).getManagedProject().getConfigurations();
			
			IConfiguration configuration = null;
			for(int i = 0; i < configs.length; i++){
				if(configs[i].getId().startsWith("cell.managedbuild.config.cell.ppu.gnu64.exe.debug")){ //$NON-NLS-1$
					configuration = configs[i];
					break;
				}
			}

			ManagedBuildManager.performValueHandlerEvent(configuration, IManagedOptionValueHandler.EVENT_OPEN, true);
			IToolChain toolChain = configuration.getToolChain();
			
			/* *************************************************************** *
			 * Add the necessary libraries needed by this PPU project's linker *
			 * *************************************************************** */
			ITool linkerTool = toolChain.getToolsBySuperClassId("cell.managedbuild.tool.cell.ppu.gnu64.c.linker")[0]; //$NON-NLS-1$
			
			IOption libraryOption = linkerTool.getOptionBySuperClassId("cell.ppu.gnu.c.linker.option.libs"); //$NON-NLS-1$
			IOption librarySearchPathOption = linkerTool.getOptionBySuperClassId("gnu.c.link.option.paths"); //$NON-NLS-1$

			ManagedBuildManager.setOption(configuration, linkerTool, libraryOption, new String[]{"dl", "pthread", "spe2", "alf"}); //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$//$NON-NLS-4$
			ManagedBuildManager.setOption(configuration, linkerTool, librarySearchPathOption, new String[]{"/opt/cell/sysroot/usr/lib64"}); //$NON-NLS-1$

			
			/* ********************************************************************* *
			 * Add the necessary include paths needed by this PPU project's compiler *
			 * ********************************************************************* */
			ITool compilerTool = toolChain.getToolsBySuperClassId("cell.managedbuild.tool.cell.ppu.gnu64.c.compiler.debug")[0]; //$NON-NLS-1$
			
			IOption includePathOption = compilerTool.getOptionBySuperClassId("gnu.c.compiler.option.include.paths"); //$NON-NLS-1$
			
			ManagedBuildManager.setOption(configuration, compilerTool, includePathOption, new String[]{"/opt/cell/sysroot/usr/include", "/opt/cell/sysroot/opt/cell/sdk/usr/include"}); //$NON-NLS-1$ //$NON-NLS-2$
			
			
			/* *************************************** *
			 * Add the defined symbol (-D) "__64BIT__"
			 * *************************************** */
			IOption definedSymbolOption = compilerTool.getOptionBySuperClassId("gnu.c.compiler.option.preprocessor.def.symbols"); //$NON-NLS-1$
			
			ManagedBuildManager.setOption(configuration, compilerTool, definedSymbolOption, new String[]{"__64BIT__"}); //$NON-NLS-1$


			/* ************************************* *
			 * Save the configuration's new settings *
			 * ************************************* */
			// Write out the build model info
			saveBuildStatus = ManagedBuildManager.saveBuildInfo(ppuProject, true);
			IManagedBuildInfo bi = ManagedBuildManager.getBuildInfo(ppuProject);
			if(bi != null & bi instanceof ManagedBuildInfo)
				((ManagedBuildInfo)bi).initializePathEntries();
			if(is64bit){
				ManagedBuildManager.setDefaultConfiguration(ppuProject, configuration);
				ManagedBuildManager.setSelectedConfiguration(ppuProject, configuration);
			}
			ManagedBuildManager.performValueHandlerEvent(configuration, IManagedOptionValueHandler.EVENT_APPLY, true);
			if(!saveBuildStatus) {
//				throw new Exception();
				Debug.POLICY.error(Debug.DEBUG_CREATE_ACTION_MORE, "Failed: {0}.", "configurePPUProject64bit"); //$NON-NLS-1$ //$NON-NLS-2$
				wizard.logWarningMessage(Messages.ALFWizardCreationAction_queryTitle, Messages.ALFWizardCreationAction_errorConfiguringPpuProject);
			}
			
//		} catch(Exception e){
//			Debug.POLICY.error(Debug.DEBUG_CREATE_ACTION_MORE, e);
//			wizard.logWarningMessage(Messages.ALFWizardCreationAction_queryTitle, Messages.ALFWizardCreationAction_errorConfiguringPpuProject);
//		}

		Debug.POLICY.exit(Debug.DEBUG_CREATE_ACTION_MORE, saveBuildStatus);
		return saveBuildStatus;
	}
	
	private boolean configureSharedLibraryProject32bit(IProject libProject, String spuProjectName){
		Debug.POLICY.enter(Debug.DEBUG_CREATE_ACTION_MORE, libProject, spuProjectName);
		boolean saveBuildStatus = false;
//		try{
			// Get the 32bit GNU Debug configuration
			IConfiguration[] configs = ManagedBuildManager.getBuildInfo(libProject).getManagedProject().getConfigurations();
			
			IConfiguration configuration = null;
			for(int i = 0; i < configs.length; i++){
				if(configs[i].getId().startsWith("cell.managedbuild.config.cell.ppu.gnu32.so.debug")){ //$NON-NLS-1$
					configuration = configs[i];
					break;
				}
			}
			
			ManagedBuildManager.performValueHandlerEvent(configuration, IManagedOptionValueHandler.EVENT_OPEN, true);
			IToolChain toolChain = configuration.getToolChain();

			/* ********************************************************************** *
			 * Add the SPU project's executable as an Embed SPU input to this project *
			 * ********************************************************************** */
			ITool embedSpuTool = toolChain.getToolsBySuperClassId("cell.managedbuild.tool.cell.ppu.gnu32.embedspu")[0]; //$NON-NLS-1$
			
			IOption embedSpuInputOption = embedSpuTool.getOptionBySuperClassId("cell.ppu.gnu.embedspu.option.inputs"); //$NON-NLS-1$
			
			ManagedBuildManager.setOption(configuration, embedSpuTool, embedSpuInputOption, 
					new String[]{"\"${workspace_loc:/" + spuProjectName + "/spu-gnu-debug/" + spuProjectName + "}\""});  //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$


			/* ************************************* *
			 * Save the configuration's new settings *
			 * ************************************* */
			// Write out the build model info
			saveBuildStatus = ManagedBuildManager.saveBuildInfo(libProject, true);
			IManagedBuildInfo bi = ManagedBuildManager.getBuildInfo(libProject);
			if(bi != null & bi instanceof ManagedBuildInfo)
				((ManagedBuildInfo)bi).initializePathEntries();
			if(!is64bit){
				ManagedBuildManager.setDefaultConfiguration(libProject, configuration);
				ManagedBuildManager.setSelectedConfiguration(libProject, configuration);
			}
			ManagedBuildManager.performValueHandlerEvent(configuration, IManagedOptionValueHandler.EVENT_APPLY, true);
			if(!saveBuildStatus) {
//				throw new Exception();
				Debug.POLICY.error(Debug.DEBUG_CREATE_ACTION_MORE, "Failed: {0}.", "configureSharedLibraryProject32bit"); //$NON-NLS-1$ //$NON-NLS-2$
				wizard.logWarningMessage(Messages.ALFWizardCreationAction_queryTitle, Messages.ALFWizardCreationAction_errorConfiguringSharedLibraryProject);
			}
			
//		} catch(Exception e){
//			Debug.POLICY.error(Debug.DEBUG_CREATE_ACTION_MORE, e);
//			wizard.logWarningMessage(Messages.ALFWizardCreationAction_queryTitle, Messages.ALFWizardCreationAction_errorConfiguringSharedLibraryProject);
//		}
		Debug.POLICY.exit(Debug.DEBUG_CREATE_ACTION_MORE, saveBuildStatus);
		return saveBuildStatus;
	}
	
	private boolean configureSharedLibraryProject64bit(IProject libProject, String spuProjectName){
		Debug.POLICY.enter(Debug.DEBUG_CREATE_ACTION_MORE, libProject, spuProjectName);

		boolean saveBuildStatus = false;
//		try{
			// Get the 64bit GNU Debug configuration
			IConfiguration[] configs = ManagedBuildManager.getBuildInfo(libProject).getManagedProject().getConfigurations();
			
			IConfiguration configuration = null;
			for(int i = 0; i < configs.length; i++){
				if(configs[i].getId().startsWith("cell.managedbuild.config.cell.ppu.gnu64.so.debug")){ //$NON-NLS-1$
					configuration = configs[i];
					break;
				}
			}

			ManagedBuildManager.performValueHandlerEvent(configuration, IManagedOptionValueHandler.EVENT_OPEN, true);
			IToolChain toolChain = configuration.getToolChain();

			/* ********************************************************************** *
			 * Add the SPU project's executable as an Embed SPU input to this project *
			 * ********************************************************************** */
			ITool embedSpuTool = toolChain.getToolsBySuperClassId("cell.managedbuild.tool.cell.ppu.gnu64.embedspu")[0]; //$NON-NLS-1$
			
			IOption embedSpuInputOption = embedSpuTool.getOptionBySuperClassId("cell.ppu.gnu.embedspu.option.inputs"); //$NON-NLS-1$
			
			ManagedBuildManager.setOption(configuration, embedSpuTool, embedSpuInputOption, 
					new String[]{"\"${workspace_loc:/" + spuProjectName + "/spu-gnu-debug/" + spuProjectName + "}\""});  //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$


			/* ************************************* *
			 * Save the configuration's new settings *
			 * ************************************* */
			// Write out the build model info
			saveBuildStatus = ManagedBuildManager.saveBuildInfo(libProject, true);
			IManagedBuildInfo bi = ManagedBuildManager.getBuildInfo(libProject);
			if(bi != null & bi instanceof ManagedBuildInfo)
				((ManagedBuildInfo)bi).initializePathEntries();
			if(is64bit){
				ManagedBuildManager.setDefaultConfiguration(libProject, configuration);
				ManagedBuildManager.setSelectedConfiguration(libProject, configuration);
			}
			ManagedBuildManager.performValueHandlerEvent(configuration, IManagedOptionValueHandler.EVENT_APPLY, true);
			if(!saveBuildStatus) {
//				throw new Exception();
				Debug.POLICY.error(Debug.DEBUG_CREATE_ACTION_MORE, "Failed: {0}.", "configureSharedLibraryProject64bit"); //$NON-NLS-1$ //$NON-NLS-2$
				wizard.logWarningMessage(Messages.ALFWizardCreationAction_queryTitle, Messages.ALFWizardCreationAction_errorConfiguringSharedLibraryProject);
			}
			
//		} catch(Exception e){
//			Debug.POLICY.error(Debug.DEBUG_CREATE_ACTION_MORE, e);
//			wizard.logWarningMessage(Messages.ALFWizardCreationAction_queryTitle, Messages.ALFWizardCreationAction_errorConfiguringSharedLibraryProject);
//		}
		Debug.POLICY.exit(Debug.DEBUG_CREATE_ACTION_MORE, saveBuildStatus);
		return saveBuildStatus;
	}
	
	/**
	 * Configures the build properties of the accelerator (SPU) project to include the alf libraries and library look-up paths.
	 * @param spuProject the SPU project to be configured
	 * @param ppuProjectName the name of the PPU project
	 */
	private boolean configureSPUProject(IProject spuProject, String ppuProjectName){
		Debug.POLICY.enter(Debug.DEBUG_CREATE_ACTION_MORE, spuProject, ppuProjectName);
		boolean saveBuildStatus = false;
//		try{
			// Get the configuration and toolchain necessary to add the options cell.managedbuild.config.cell.spu.gnu.exe.debug
			IConfiguration[] configs = ManagedBuildManager.getBuildInfo(spuProject).getManagedProject().getConfigurations();
			IConfiguration configuration = null;
			for(int i = 0; i < configs.length; i++){
				if(configs[i].getId().startsWith("cell.managedbuild.config.cell.spu.gnu.exe.debug")){ //$NON-NLS-1$
					configuration = configs[i];
					break;
				}
			}
			
			ManagedBuildManager.performValueHandlerEvent(configuration, IManagedOptionValueHandler.EVENT_OPEN, true);
			IToolChain toolChain = configuration.getToolChain();

			/* ***************************************************************** *
			 * Add the necessary alf library needed by this SPU project's linker *
			 * ***************************************************************** */
			ITool linkerTool = toolChain.getToolsBySuperClassId("cell.managedbuild.tool.cell.spu.gnu.c.linker")[0]; //$NON-NLS-1$
			
			IOption libraryOption = linkerTool.getOptionBySuperClassId("cell.gnu.c.linker.option.libs"); //$NON-NLS-1$
			IOption librarySearchPathOption = linkerTool.getOptionBySuperClassId("gnu.c.link.option.paths"); //$NON-NLS-1$
			
			ManagedBuildManager.setOption(configuration, linkerTool, libraryOption, new String[]{"alf"}); //$NON-NLS-1$
			ManagedBuildManager.setOption(configuration, linkerTool, librarySearchPathOption, new String[]{"/opt/cell/sysroot/usr/spu/lib"}); //$NON-NLS-1$


			/* ********************************************************************* *
			 * Add the necessary include paths needed by this SPU project's compiler *
			 * ********************************************************************* */
			ITool compilerTool = toolChain.getToolsBySuperClassId("cell.managedbuild.tool.cell.spu.gnu.c.compiler.debug")[0]; //$NON-NLS-1$
			
			IOption includePathOption = compilerTool.getOptionBySuperClassId("gnu.c.compiler.option.include.paths"); //$NON-NLS-1$
			
			ManagedBuildManager.setOption(configuration, compilerTool, includePathOption, 
					new String[]{"/opt/cell/sysroot/usr/spu/include", "\"${workspace_loc:/" + ppuProjectName + "}\""});  //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$


			/* ************************************* *
			 * Save the configuration's new settings *
			 * ************************************* */
			// Write out the build model info
			saveBuildStatus = ManagedBuildManager.saveBuildInfo(spuProject, true);
			IManagedBuildInfo bi = ManagedBuildManager.getBuildInfo(spuProject);
			if(bi != null & bi instanceof ManagedBuildInfo){
				((ManagedBuildInfo)bi).initializePathEntries();
			}
			ManagedBuildManager.performValueHandlerEvent(configuration, IManagedOptionValueHandler.EVENT_APPLY, true);
			if(!saveBuildStatus) {
//				throw new Exception();
				Debug.POLICY.error(Debug.DEBUG_CREATE_ACTION_MORE, "Failed: {0}.", "configureSPUProject"); //$NON-NLS-1$ //$NON-NLS-2$
				wizard.logWarningMessage(Messages.ALFWizardCreationAction_queryTitle, Messages.ALFWizardCreationAction_errorConfiguringSpuProject);
			}
			
//		} catch (Exception e){
//			Debug.POLICY.error(Debug.DEBUG_CREATE_ACTION_MORE, e);
//			wizard.logWarningMessage(Messages.ALFWizardCreationAction_queryTitle, Messages.ALFWizardCreationAction_errorConfiguringSpuProject);
//		}
		Debug.POLICY.exit(Debug.DEBUG_CREATE_ACTION_MORE, saveBuildStatus);
		return saveBuildStatus;
	}

	private void cloneProjectConfigurations(IProject project) {
		IConfiguration[] configs = ManagedBuildManager.getBuildInfo(project)
				.getManagedProject().getConfigurations();

		for (int i = 0; i < configs.length; i++) {
			ManagedBuildManager.performValueHandlerEvent(configs[i],
					IManagedOptionValueHandler.EVENT_OPEN, true);
			IToolChain tempToolChain = configs[i].getToolChain();
			cloneTools(tempToolChain, configs[i]);
			ManagedBuildManager.performValueHandlerEvent(configs[i],
					IManagedOptionValueHandler.EVENT_APPLY, true);
		}
	}
	
	private void cloneTools(IToolChain toolChain, IConfiguration configuration){ 
		Debug.POLICY.enter(Debug.DEBUG_CREATE_ACTION_MORE, toolChain);
		ITool[] tools = toolChain.getTools();
		
		for(int i = 0; i < tools.length; i++){
			ITool newTool = toolChain.createTool(tools[i], ManagedBuildManager.calculateChildId(tools[i].getId(), null), tools[i].getName(), false);

			IOption[] options = tools[i].getOptions();
			for(int j = 0; j < options.length; j++){
				newTool.createOption(options[j], ManagedBuildManager.calculateChildId(options[j].getId(), null), options[j].getName(), false);
			}
		}
		toolChain.setDirty(true);
		Debug.POLICY.exit(Debug.DEBUG_CREATE_ACTION_MORE);
	}
	
	private IProject createProject(String name, String projectTypeID) throws InvocationTargetException {
		Debug.POLICY.enter(Debug.DEBUG_CREATE_ACTION_MORE, name, projectTypeID);
		IProject newProject = null;
		try {
			IWorkspace workspace = ResourcesPlugin.getWorkspace();
			
			IProject newProjectHandle = workspace.getRoot().getProject(name);
			
			IProjectDescription description = workspace.newProjectDescription(newProjectHandle.getName());
			if(Platform.getLocation().equals(projectLocation))
				description.setLocation(null);
			else
				description.setLocation(projectLocation);
		
			newProject = CCorePlugin.getDefault().createCProject(description, newProjectHandle, null, ManagedBuilderCorePlugin.MANAGED_MAKE_PROJECT_ID);

			ManagedCProjectNature.addManagedNature(newProject, null);
			ManagedCProjectNature.addManagedBuilder(newProject,null);

			IManagedProject newManagedProject = null;
			IManagedBuildInfo info = null;
			
			info = ManagedBuildManager.createBuildInfo(newProject);
			
			IProjectType parent = ManagedBuildManager.getExtensionProjectType(projectTypeID);
			newManagedProject = ManagedBuildManager.createManagedProject(newProject, parent);

			if (newManagedProject != null) {
				IConfiguration [] selectedConfigs = parent.getConfigurations();
				for (int i = 0; i < selectedConfigs.length; i++) {
					IConfiguration config = selectedConfigs[i];
					int id = ManagedBuildManager.getRandomNumber();
					IConfiguration newConfig = newManagedProject.createConfigurationClone(config, config.getId() + "." + id); //$NON-NLS-1$
					newConfig.setArtifactName(newManagedProject.getDefaultArtifactName());
				}
				// Now add the first supported config in the list as the default
				IConfiguration defaultCfg = null;
				IConfiguration[] newConfigs = newManagedProject.getConfigurations();
				for(int i = 0; i < newConfigs.length; i++) {
					if(newConfigs[i].isSupported()){
						defaultCfg = newConfigs[i];
						break;
					}
				}

				if(defaultCfg == null && newConfigs.length > 0)
					defaultCfg = newConfigs[0];
				
				if(defaultCfg != null) {
					ManagedBuildManager.setDefaultConfiguration(newProject, defaultCfg);
					ManagedBuildManager.setSelectedConfiguration(newProject, defaultCfg);
				}
				ManagedBuildManager.setNewProjectVersion(newProject);
				
				ICDescriptor desc = null;
				try {
					desc = CCorePlugin.getDefault().getCProjectDescription(newProject, true);
					desc.create(CCorePlugin.BUILD_SCANNER_INFO_UNIQ_ID, ManagedBuildManager.INTERFACE_IDENTITY);
				} catch (CoreException e) {
					Debug.POLICY.error(Debug.DEBUG_CREATE_ACTION_MORE, e);
//					Debug.POLICY.logError(e);
					throw e;
				}
				
				if (info != null) {
					info.setValid(true);
					ManagedBuildManager.saveBuildInfo(newProject, true);
				}
			}
			
			IStatus initResult = ManagedBuildManager.initBuildInfoContainer(newProject);
			if (initResult.getCode() != IStatus.OK) {
				// At this point, I can live with a failure
//				ManagedBuilderUIPlugin.log(initResult);
				Debug.POLICY.logStatus(initResult);
			}
	
			cloneProjectConfigurations(newProject);

		} catch (OperationCanceledException e) {
			Debug.POLICY.error(Debug.DEBUG_CREATE_ACTION_MORE, e);
			throw new InvocationTargetException(e, Messages.ALFWizardCreationAction_errorCreatingProject);
		} catch (CoreException e) {
			Debug.POLICY.error(Debug.DEBUG_CREATE_ACTION_MORE, e);
			throw new InvocationTargetException(e, Messages.ALFWizardCreationAction_errorCreatingProject);
		} catch (BuildException e) {
			Debug.POLICY.error(Debug.DEBUG_CREATE_ACTION_MORE, e);
			throw new InvocationTargetException(e, Messages.ALFWizardCreationAction_errorCreatingProject);
		}		
		
		Debug.POLICY.exit(Debug.DEBUG_CREATE_ACTION_MORE, newProject);
		return newProject;
	}

	private Document createXMLParameterDocument(String outputDir, String date) throws InvocationTargetException, ParserConfigurationException {
		Debug.POLICY.enter(Debug.DEBUG_CREATE_ACTION_MORE, outputDir, date);
		
		String tempString;
			
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder parser = factory.newDocumentBuilder();

		// create blank DOM Document
	    Document doc = parser.newDocument();
			
	    // create the elements for <root>, <global>
	    Element alfRoot = doc.createElement(Messages.ALFWizardCreationAction_tag_alf);
	    Element global = doc.createElement(Messages.ALFWizardCreationAction_tag_global);
	        
	    /* create the element children of <global> */
	        
	    // <ALF_VERSION>
	    Element alfVersion = doc.createElement(Messages.ALFWizardCreationAction_tag_ALF_VERSION);
	    Text alfVersionText = doc.createTextNode(Messages.ALFWizardCreationAction_alfVersion);
	    alfVersion.appendChild(alfVersionText);
	    // <DATE_TIME>	        
	    Element dateTime = doc.createElement(Messages.ALFWizardCreationAction_tag_DATE_TIME);
	    Text dateTimeText = doc.createTextNode(date);
	    dateTime.appendChild(dateTimeText);
	    // <TEMPLATE_DIR>
	    Element templateDir = doc.createElement(Messages.ALFWizardCreationAction_tag_TEMPLATE_DIR);
	    //verify that the template directory exists and/or can be read
	    String tDirPath = Messages.ALFWizardCreationAction_templateDir;
	    File tDir = new File(tDirPath);
	    if(!tDir.exists()){
	    	tDirPath = wizard.queryPathLocation(Messages.ALFWizardCreationAction_queryTitle, Messages.ALFWizardCreationAction_queryTemplateDirMessage, 
	    			Messages.ALFWizardCreationAction_templateDir);
	    }
	    if(tDirPath == null){
	    	throw new InvocationTargetException(new Exception(), Messages.ALFWizardCreationAction_errorTemplateDir);
	    }
	    Text templateDirText = doc.createTextNode(tDirPath);
	    templateDir.appendChild(templateDirText);
	    // <TARGET_DIR>
	    Element targetDir = doc.createElement(Messages.ALFWizardCreationAction_tag_TARGET_DIR);
	    Text targetDirText = doc.createTextNode(outputDir);
	    targetDir.appendChild(targetDirText);
	    // <PROJECT_NAME>
	    Element projectName = doc.createElement(Messages.ALFWizardCreationAction_tag_PROJECT_NAME);
	    Text projectNameText = doc.createTextNode(this.projectName);
	    projectName.appendChild(projectNameText);
	    // <STACK_SIZE>
	    Element stackSize = doc.createElement(Messages.ALFWizardCreationAction_tag_STACK_SIZE);
	    Text stackSizeText = doc.createTextNode(this.stackSize + ""); //$NON-NLS-1$
	    stackSize.appendChild(stackSizeText);
	    // <PARTITION_METHOD>
	    Element partitionMethod = doc.createElement(Messages.ALFWizardCreationAction_tag_PARTITION_METHOD);
	    Text partitionMethodText = doc.createTextNode(this.partitionMethod + ""); //$NON-NLS-1$
	    partitionMethod.appendChild(partitionMethodText);
	    // <EXP_ACCEL_NUM>
	    Element expAccelNum = doc.createElement(Messages.ALFWizardCreationAction_tag_EXP_ACCEL_NUM);
	    Text expAccelNumText = doc.createTextNode(this.expAccelNum + ""); //$NON-NLS-1$
	    expAccelNum.appendChild(expAccelNumText);
	    // <BUFFER_NUMBER>
	    Element bufferNumber = doc.createElement(Messages.ALFWizardCreationAction_tag_BUFFER_NUMBER);
	    Text bufferNumberText = doc.createTextNode(this.buffers.size() + ""); //$NON-NLS-1$
	    bufferNumber.appendChild(bufferNumberText);

	    // append <alf> to document, and <global> to <alf>
	    doc.appendChild(alfRoot);
	    alfRoot.appendChild(global);

	    // append all of the children of <global> to it
	    global.appendChild(alfVersion);
	    global.appendChild(dateTime);
	    global.appendChild(templateDir);
	    global.appendChild(targetDir);
	    global.appendChild(projectName);
	    global.appendChild(stackSize);
	    global.appendChild(partitionMethod);
	    global.appendChild(expAccelNum);
	    global.appendChild(bufferNumber);

	    // create and append <buffer> tags for each buffer
	    for(int i = 0; i < buffers.size(); i++){
	    	ALFBuffer buf = (ALFBuffer) buffers.get(i);

	    	// <buffer>
	    	Element buffer = doc.createElement(Messages.ALFWizardCreationAction_tag_buffer);
	    	alfRoot.appendChild(buffer);

	    	/* create the element children of <buffer> */

	    	// <VARIABLE_NAME>
	    	Element variableName = doc.createElement(Messages.ALFWizardCreationAction_tag_VARIABLE_NAME);
	    	Text variableNameText = doc.createTextNode(buf.getName());
	    	variableName.appendChild(variableNameText);

	    	// <ELEMENT_TYPE>
	    	Element elementType = doc.createElement(Messages.ALFWizardCreationAction_tag_ELEMENT_TYPE);
	    	Text elementTypeText = doc.createTextNode(buf.getElementType());
	    	elementType.appendChild(elementTypeText);

	    	// <ELEMENT_UNIT>
	    	Element elementUnit = doc.createElement(Messages.ALFWizardCreationAction_tag_ELEMENT_UNIT);
	    	tempString = null;
	    	switch(buf.getElementUnit()){ // elementUnit is stored as a int, so assign the string based on the int value
	    	case ALFConstants.ALF_DATA_BYTE: tempString = Messages.ALFWizardCreationAction_elementUnitByte; break;

	    	case ALFConstants.ALF_DATA_INT16: tempString = Messages.ALFWizardCreationAction_elementUnitInt16; break;

	    	case ALFConstants.ALF_DATA_INT32: tempString = Messages.ALFWizardCreationAction_elementUnitInt32; break;

	    	case ALFConstants.ALF_DATA_INT64: tempString = Messages.ALFWizardCreationAction_elementUnitInt64; break;

	    	case ALFConstants.ALF_DATA_FLOAT: tempString = Messages.ALFWizardCreationAction_elementUnitFloat; break;

	    	case ALFConstants.ALF_DATA_DOUBLE: tempString = Messages.ALFWizardCreationAction_elementUnitDouble; break;

	    	case ALFConstants.ALF_DATA_ADDR32: tempString = Messages.ALFWizardCreationAction_elementUnitAddr32; break;

	    	case ALFConstants.ALF_DATA_ADDR64: tempString = Messages.ALFWizardCreationAction_elementUnitAddr64; break;

	    	case ALFConstants.ALF_DATA_ELEMENT_TYPE: tempString = buf.getElementType(); break;

	    	default: tempString = null;
	    	}
	    	Text elementUnitText = doc.createTextNode(tempString);
	    	elementUnit.appendChild(elementUnitText);

	    	// <BUFFER_TYPE>
	    	Element bufferType = doc.createElement(Messages.ALFWizardCreationAction_tag_BUFFER_TYPE);
	    	tempString = null;
	    	switch(buf.getBufferType()){
	    	case ALFConstants.ALF_BUFFER_INPUT: tempString = Messages.ALFWizardCreationAction_bufferTypeInput; break;

	    	case ALFConstants.ALF_BUFFER_OUTPUT: tempString = Messages.ALFWizardCreationAction_bufferTypeOutput; break;

	    	default: tempString = null;
	    	}
	    	Text bufferTypeText = doc.createTextNode(tempString);
	    	bufferType.appendChild(bufferTypeText);

	    	// <NUM_DIMENSION>
	    	Element numDimension = doc.createElement(Messages.ALFWizardCreationAction_tag_NUM_DIMENSION);
	    	int actualNumDim = buf.getNumDimensions() + 1;
	    	Text numDimensionText = doc.createTextNode(actualNumDim + ""); //$NON-NLS-1$
	    	numDimension.appendChild(numDimensionText);

	    	// <DIMENSION_SIZE_X>
	    	Element dimensionSizeX = doc.createElement(Messages.ALFWizardCreationAction_tag_DIMENSION_SIZE_X);
	    	Text dimensionSizeXText = doc.createTextNode(buf.getDimensionSizeX() + ""); //$NON-NLS-1$
	    	dimensionSizeX.appendChild(dimensionSizeXText);

	    	// <DIMENSION_SIZE_Y>
	    	Element dimensionSizeY = doc.createElement(Messages.ALFWizardCreationAction_tag_DIMENSION_SIZE_Y);
	    	Text dimensionSizeYText = doc.createTextNode(buf.getDimensionSizeY() + ""); //$NON-NLS-1$
	    	dimensionSizeY.appendChild(dimensionSizeYText);

	    	// <DIMENSION_SIZE_Z>
	    	Element dimensionSizeZ = doc.createElement(Messages.ALFWizardCreationAction_tag_DIMENSION_SIZE_Z);
	    	Text dimensionSizeZText = doc.createTextNode(buf.getDimensionSizeZ() + ""); //$NON-NLS-1$
	    	dimensionSizeZ.appendChild(dimensionSizeZText);

	    	// <DISTRIBUTION_MODEL_X>
	    	Element distributionModelX = doc.createElement(Messages.ALFWizardCreationAction_tag_DISTRIBUTION_MODEL_X);
	    	tempString = null;
	    	switch(buf.getDistributionModelX()){
	    	case ALFConstants.DIST_MODEL_STAR: tempString = Messages.ALFWizardCreationAction_distributionModelStar; break;

	    	case ALFConstants.DIST_MODEL_BLOCK: tempString = Messages.ALFWizardCreationAction_distributionModelBlock; break;

	    	case ALFConstants.DIST_MODEL_CYCLIC: tempString = Messages.ALFWizardCreationAction_distributionModelCyclic; break;

	    	default: tempString = null;
	    	}
	    	Text distributionModelXText = doc.createTextNode(tempString);
	    	distributionModelX.appendChild(distributionModelXText);

	    	// <DISTRIBUTION_MODEL_Y>
	    	Element distributionModelY = doc.createElement(Messages.ALFWizardCreationAction_tag_DISTRIBUTION_MODEL_Y);
	    	tempString = null;
	    	switch(buf.getDistributionModelY()){
	    	case ALFConstants.DIST_MODEL_STAR: tempString = Messages.ALFWizardCreationAction_distributionModelStar; break;

	    	case ALFConstants.DIST_MODEL_BLOCK: tempString = Messages.ALFWizardCreationAction_distributionModelBlock; break;

	    	case ALFConstants.DIST_MODEL_CYCLIC: tempString = Messages.ALFWizardCreationAction_distributionModelCyclic; break;

	    	default: tempString = null;
	    	}
	    	Text distributionModelYText = doc.createTextNode(tempString);
	    	distributionModelY.appendChild(distributionModelYText);

	    	// <DISTRIBUTION_MODEL_Z>
	    	Element distributionModelZ = doc.createElement(Messages.ALFWizardCreationAction_tag_DISTRIBUTION_MODEL_Z);
	    	tempString = null;
	    	switch(buf.getDistributionModelZ()){
	    	case ALFConstants.DIST_MODEL_STAR: tempString = Messages.ALFWizardCreationAction_distributionModelStar; break;

	    	case ALFConstants.DIST_MODEL_BLOCK: tempString = Messages.ALFWizardCreationAction_distributionModelBlock; break;

	    	case ALFConstants.DIST_MODEL_CYCLIC: tempString = Messages.ALFWizardCreationAction_distributionModelCyclic; break;

	    	default: tempString = null;
	    	}
	    	Text distributionModelZText = doc.createTextNode(tempString);
	    	distributionModelZ.appendChild(distributionModelZText);

	    	// <DISTRIBUTION_SIZE_X>
	    	Element distributionSizeX = doc.createElement(Messages.ALFWizardCreationAction_tag_DISTRIBUTION_SIZE_X);
	    	Text distributionSizeXText = doc.createTextNode(buf.getDistributionSizeX() + ""); //$NON-NLS-1$
	    	distributionSizeX.appendChild(distributionSizeXText);

	    	// <DISTRIBUTION_SIZE_Y>
	    	Element distributionSizeY = doc.createElement(Messages.ALFWizardCreationAction_tag_DISTRIBUTION_SIZE_Y);
	    	Text distributionSizeYText = doc.createTextNode(buf.getDistributionSizeY() + ""); //$NON-NLS-1$
	    	distributionSizeY.appendChild(distributionSizeYText);

	    	// <DISTRIBUTION_SIZE_Z>
	    	Element distributionSizeZ = doc.createElement(Messages.ALFWizardCreationAction_tag_DISTRIBUTION_SIZE_Z);
	    	Text distributionSizeZText = doc.createTextNode(buf.getDistributionSizeZ() + ""); //$NON-NLS-1$
	    	distributionSizeZ.appendChild(distributionSizeZText);

	    	// append the dimension-independent children to its <buffer> element
	    	buffer.appendChild(variableName);
	    	buffer.appendChild(elementType);
	    	buffer.appendChild(elementUnit);
	    	buffer.appendChild(bufferType);
	    	buffer.appendChild(numDimension);

	    	// append the dimension-dependent children to its <buffer> element depending on the number of dimensions
	    	int numDim = buf.getNumDimensions();
	    	switch(numDim){
	    		case ALFConstants.ONE_DIMENSIONAL: 
	    			buffer.appendChild(dimensionSizeX);
	    			buffer.appendChild(distributionModelX);
	    			buffer.appendChild(distributionSizeX);
	    			break;
	    			
	    		case ALFConstants.TWO_DIMENSIONAL:
	    			buffer.appendChild(dimensionSizeX);
	    			buffer.appendChild(dimensionSizeY);
	    			buffer.appendChild(distributionModelX);
	    			buffer.appendChild(distributionSizeX);
	    			buffer.appendChild(distributionModelY);
	    			buffer.appendChild(distributionSizeY);
	    			break;
	    			
	    		case ALFConstants.THREE_DIMENSIONAL:
	    			buffer.appendChild(dimensionSizeX);
	    			buffer.appendChild(dimensionSizeY);
	    			buffer.appendChild(dimensionSizeZ);
	    			buffer.appendChild(distributionModelX);
	    			buffer.appendChild(distributionSizeX);
	    			buffer.appendChild(distributionModelY);
	    			buffer.appendChild(distributionSizeY);
	    			buffer.appendChild(distributionModelZ);
	    			buffer.appendChild(distributionSizeZ);
	    			break;
	    			
	    		default: throw new InvocationTargetException(new Exception(), Messages.ALFWizardCreationAction_errorCreatingParamFileMsg);
	    	}
	    }
	    
		Debug.POLICY.exit(Debug.DEBUG_CREATE_ACTION_MORE, doc);
	    return doc;
	}
	
	private static void copyFile(String src, String dst) throws InvocationTargetException {
		Debug.POLICY.enter(Debug.DEBUG_CREATE_ACTION_MORE, src, dst);
		try {
			// Create channel on the source
			FileChannel srcChannel;
			srcChannel = new FileInputStream(src).getChannel();

			// Create channel on the destination
	        FileChannel dstChannel = new FileOutputStream(dst).getChannel();
	    
	        // Copy file contents from source to destination
	        dstChannel.transferFrom(srcChannel, 0, srcChannel.size());
	        
	        // Close the channels
	        srcChannel.close();
	        dstChannel.close();
		} catch (FileNotFoundException e) {
			Debug.POLICY.error(Debug.DEBUG_CREATE_ACTION_MORE, e);
			throw new InvocationTargetException(e, Messages.ALFWizardCreationAction_errorSourceFilesNotFound);
		} catch (IOException e) {
			Debug.POLICY.error(Debug.DEBUG_CREATE_ACTION_MORE, e);
			throw new InvocationTargetException(e, Messages.ALFWizardCreationAction_errorCopyingFiles);
		}
		Debug.POLICY.exit(Debug.DEBUG_CREATE_ACTION_MORE);
	}
	
	private File createXMLParameterFile(Document doc, File tmpDir) throws InvocationTargetException, TransformerException{
		Debug.POLICY.enter(Debug.DEBUG_CREATE_ACTION_MORE, doc, tmpDir);
		try{
			
			File xmlFile = new File(tmpDir, Messages.ALFWizardCreationAction_xmlParamFileName);
			if(!xmlFile.exists())
				xmlFile.createNewFile();
		
			TransformerFactory tranFactory = TransformerFactory.newInstance();
			Transformer aTransformer = tranFactory.newTransformer();

			Source src = new DOMSource(doc);
			Result dest = new StreamResult(xmlFile.getPath());
			aTransformer.transform(src, dest);
			
			Debug.POLICY.exit(Debug.DEBUG_CREATE_ACTION_MORE, xmlFile);
			return xmlFile;
		
		} catch (IOException e){
			Debug.POLICY.error(Debug.DEBUG_CREATE_ACTION_MORE, e);
			throw new InvocationTargetException(e, Messages.ALFWizardCreationAction_errorCreatingParamFileMsg);
		}
	}

	private void deleteDir(File file){
		Debug.POLICY.pass(Debug.DEBUG_CREATE_ACTION_MORE, file);
		if(file == null)
			return;
		if(file.isFile())
			file.delete();
		if(file.isDirectory()){
			File[] files = file.listFiles();
			for(int i = 0; i < files.length; i++)
				deleteDir(files[i]);
			file.delete();
		}
	}
	
	/**
	 * Returns the current date in a format which is not obtainable using the Calendar.getTime()
	 * @return string representation of the current date and time, in the formate: YYYY-MM-DD_HH:MM:SS
	 */
	private String getDate(){
		Calendar cal = Calendar.getInstance(TimeZone.getDefault());
		String month = (cal.get(Calendar.MONTH) + 1) + ""; //$NON-NLS-1$
		if(month.length() == 1){ month = "0" + month; } //$NON-NLS-1$
		String day = cal.get(Calendar.DAY_OF_MONTH) + ""; //$NON-NLS-1$
		if(day.length() == 1){ day = "0" +  day; } //$NON-NLS-1$
		String hour = cal.get(Calendar.HOUR_OF_DAY) + ""; //$NON-NLS-1$
		if(hour.length() == 1){ hour = "0" + hour; } //$NON-NLS-1$
		String minute = cal.get(Calendar.MINUTE) + ""; //$NON-NLS-1$
		if(minute.length() == 1){ minute = "0" + minute; } //$NON-NLS-1$
		String second = cal.get(Calendar.SECOND) + ""; //$NON-NLS-1$
		if(second.length() == 1){ second = "0" + second; } //$NON-NLS-1$
		
		return cal.get(Calendar.YEAR) + "-" + month + "-" + day + "_" + hour + ":" + minute + ":" + second; //$NON-NLS-1$ //$NON-NLS-2$//$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
	}
	
	private void importFiles(IProgressMonitor monitor, String tmpDirPath, IProject ppuProject, IProject spuProject) throws InvocationTargetException, InterruptedException{
		Debug.POLICY.enter(Debug.DEBUG_CREATE_ACTION_MORE, tmpDirPath, ppuProject, spuProject);
		try{
			/* **************************************************** *
			 * Import the source and header files into the projects *
			 * **************************************************** */
			// Get IFile references to the generated .c source code files
			String ppuFilePath = tmpDirPath + PATH_SEPERATOR + "host" + PATH_SEPERATOR +  //$NON-NLS-1$
			"ppu_" + projectName + ".c";  //$NON-NLS-1$//$NON-NLS-2$
			File ppuFile = new File(ppuFilePath);
			String spuFilePath = tmpDirPath + PATH_SEPERATOR + "accel" + PATH_SEPERATOR +  //$NON-NLS-1$
			"spu_" + projectName + ".c";  //$NON-NLS-1$//$NON-NLS-2$
			File spuFile = new File(spuFilePath);

			if(!ppuFile.exists() || !spuFile.exists())
				throw new InvocationTargetException(new Exception(), Messages.ALFWizardCreationAction_errorSourceFilesNotFound);

			File newPPUFile = new File(ppuProject.getLocation().toString() + PATH_SEPERATOR + "ppu_" + projectName + ".c"); //$NON-NLS-1$//$NON-NLS-2$
			newPPUFile.createNewFile();
			copyFile(ppuFile.getAbsolutePath(), newPPUFile.getAbsolutePath());

			if(monitor.isCanceled())
				throw new InterruptedException(Messages.ALFWizardCreationAction_canceled);

			File newSPUFile = new File(spuProject.getLocation().toString() + PATH_SEPERATOR + "spu_" + projectName + ".c"); //$NON-NLS-1$//$NON-NLS-2$
			newSPUFile.createNewFile();
			copyFile(spuFile.getAbsolutePath(), newSPUFile.getAbsolutePath());

			if(monitor.isCanceled())
				throw new InterruptedException(Messages.ALFWizardCreationAction_canceled);

			// Import the header file "common.h" into host project
			File headerFile = new File(tmpDirPath + PATH_SEPERATOR + "common.h"); //$NON-NLS-1$

			if(!headerFile.exists())
				throw new InvocationTargetException(new Exception(), Messages.ALFWizardCreationAction_errorHeaderFileNotFound);

			File newHeaderFile = new File(ppuProject.getLocation().toString() + PATH_SEPERATOR + "common.h"); //$NON-NLS-1$
			newHeaderFile.createNewFile();
			copyFile(headerFile.getAbsolutePath(), newHeaderFile.getAbsolutePath());

			if(monitor.isCanceled())
				throw new InterruptedException(Messages.ALFWizardCreationAction_canceled);			

			// Import the host header file into host project
			File hostHeaderFile = new File(tmpDirPath + PATH_SEPERATOR + "host" + PATH_SEPERATOR +  //$NON-NLS-1$
					"ppu_" + projectName + ".h"); //$NON-NLS-1$ //$NON-NLS-2$

			if(!hostHeaderFile.exists())
				throw new InvocationTargetException(new Exception(), Messages.ALFWizardCreationAction_errorHeaderFileNotFound);

			File newHostHeaderFile = new File(ppuProject.getLocation().toString() + PATH_SEPERATOR + "ppu_" + projectName + ".h"); //$NON-NLS-1$ //$NON-NLS-2$
			newHostHeaderFile.createNewFile();
			copyFile(hostHeaderFile.getAbsolutePath(), newHostHeaderFile.getAbsolutePath());

			if(monitor.isCanceled())
				throw new InterruptedException(Messages.ALFWizardCreationAction_canceled);
			
		} catch(IOException e){
			Debug.POLICY.error(Debug.DEBUG_CREATE_ACTION_MORE, e);
			throw new InvocationTargetException(e, Messages.ALFWizardCreationAction_errorCopyingFiles);
		}
		Debug.POLICY.exit(Debug.DEBUG_CREATE_ACTION_MORE);
	}
}
