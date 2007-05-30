/*******************************************************************************
 * Copyright (c) 2006,2007 IBM Corp. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corp. - initial implementation
 *******************************************************************************/

package org.eclipse.ptp.pldt.wizards.wizardPages;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.IManagedProject;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.ui.wizards.MBSCustomPageData;
import org.eclipse.cdt.managedbuilder.ui.wizards.MBSCustomPageManager;
import org.eclipse.cdt.ui.wizards.CProjectWizard;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.ptp.pldt.wizards.MpiWizardsPlugin;
import org.osgi.framework.Bundle;

/**
 * 
 * After the MPIProjectWizardPage runs, and we get MPI include path from the user,
 * we have this opportunity to use that information to modify the include paths etc.
 * in the project
 * 
 * @author Beth Tibbitts
 *
 */
public class MPIProjectRunnable implements Runnable {
	private static final boolean traceOn=false;

	/**
	 * Take the info from the MPI project wizard page and fix up the project include paths etc.
	 * 
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		String pageID = MPIProjectWizardPage.PAGE_ID;
		String propID=MPIProjectWizardPage.DO_MPI_INCLUDES;
		Object obj = MBSCustomPageManager.getPageProperty(pageID, propID);
		boolean doMpiIncludes=MPIProjectWizardPage.getDefaultUseMpiIncludes();
		if(obj!=null)
		    doMpiIncludes=Boolean.valueOf((String)obj);
		if(!doMpiIncludes) {
			if(traceOn)System.out.println("Do not save MPI info in this project.");
			return;
		}
		MBSCustomPageData pData = MBSCustomPageManager.getPageData(pageID);
 		CProjectWizard wiz=null;//cdt40  (cdt 3.1 was NewCProjectWizard)

 		try {
			IWizardPage wp = pData.getWizardPage();
			IWizard w = wp.getWizard();
			if(w instanceof CProjectWizard){
				wiz = (CProjectWizard)w;
			}
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
			Throwable reason = e.getCause();
		}
		IProject proj=wiz.getProject(true);

		if(traceOn)System.out.println("Project: " + proj.getName());

		propID = MPIProjectWizardPage.INCLUDE_PATH_PROP_ID;
		String newIncludePath = getNewPropValue(pageID, propID,"c:/mpich2/include");
		if(traceOn)System.out.println("Got prop: "+propID+"="+newIncludePath);
		
		propID = MPIProjectWizardPage.LIB_PROP_ID;
		String newLib=getNewPropValue(pageID,propID,"lib");
		
		propID=MPIProjectWizardPage.LIBRARY_SEARCH_PATH_PROP_ID;
		String newLibSearchPath=getNewPropValue(pageID,propID,"c:/mpich2/lib");
		
		propID=MPIProjectWizardPage.MPI_COMPILE_COMMAND_PROP_ID;
		String mpiCompileCommand=getNewPropValue(pageID,propID,"mpicc");
		
		propID=MPIProjectWizardPage.MPI_LINK_COMMAND_PROP_ID;
		String mpiLinkCommand=getNewPropValue(pageID,propID,"mpicc");
		
		propID=MPIProjectWizardPage.MPI_SAMPLE_FILE_PROP_ID;
		String mpiSampleFileInsert=getNewPropValue(pageID, propID, "false");
 
		IManagedBuildInfo info = null;
		try {
			info = ManagedBuildManager.getBuildInfo(proj);
			// note: assumed null if this is not a managed build project? will we get here in that case?
			if(traceOn)System.out.println("Build info: " + info);
		} catch (Exception e) {
			System.out.println("MPIProjectRunnable.run(), "+e.getMessage());
			e.printStackTrace();
			return;
		}
		Assert.isNotNull(info);
		
		IManagedProject mProj = info.getManagedProject();
		if(traceOn)showOptions(mProj);
		
		// add the include path & linker values to all the configurations
		IConfiguration[] configs = mProj.getConfigurations();

		for (int i = 0; i < configs.length; i++) {
			IConfiguration cf = configs[i];
			if(traceOn)System.out.println("Config " + i + ": " + cf.getName());
			addIncludePath(cf, newIncludePath);
			addLinkerOpt(cf,newLib,newLibSearchPath);
			//addLinkerOptOld(cf,newLib,newLibSearchPath);
			setCompileCommand(cf,mpiCompileCommand);
			setLinkCommand(cf,mpiLinkCommand);		
		}
		if(traceOn)System.out.println("Runnable, newIncludePath: "+newIncludePath);
		if(traceOn)System.out.println("   newLib: "+newLib+"  newLibSrchPth: "+newLibSearchPath);
		if(traceOn)System.out.println("   compileCmd: "+mpiCompileCommand);
		if(traceOn)System.out.println("   linkCmd: "+mpiLinkCommand);
		// ManagedBuildManger.saveBuildInfo(...) assures that the
		// values are persisted in the build model, otherwise they will
		// be lost when you shut down Eclipse.
		if(traceOn)System.out.println("ManagedBuildManager.saveBuildInfo...");
		
		if(mpiSampleFileInsert.equals("true")){
			 try {
				FileInputStream fis=null;
				Bundle bundle = Platform.getBundle(MpiWizardsPlugin.getPluginId());
				Path path = new Path("testMPI.c");
				URL fileURL = Platform.find(bundle, path);
				InputStream mpiFileStream = null;
				try {
					String fname="testMPI.c";
					mpiFileStream = fileURL.openStream();
					proj.getFile(fname).create(mpiFileStream,false,null);
					System.out.println("file "+fname+" created.");
				} catch (IOException e) {
					e.printStackTrace();
				}
			} catch (CoreException e) {
				System.out.println("Error creating testMPI.c");
			}
		}
		ManagedBuildManager.saveBuildInfo(proj, true);

	}
	public static String getResourceString(String key) {
		ResourceBundle bundle = MpiWizardsPlugin.getDefault().getResourceBundle();
		try {
			return (bundle != null) ? bundle.getString(key) : key;
		} catch (MissingResourceException e) {
			return key;
		}
	}

	/**
	 * Get a value that was possibly obtained from the user on the associated wizard page.
	 * @param pageID The pageID for our wizard page
	 * @param propID The propID of the value we want
	 * @param defaultVal The default value to use if the user didn't select anything,
	 * or if we haven't gotten around to LETTING the user select anything yet.
	 * @return
	 */
	private String getNewPropValue(String pageID, String propID, String defaultVal) {
		Object obj = MBSCustomPageManager.getPageProperty(pageID, propID);
		// if selection made on page, obj is non-null.
		String newValue = defaultVal;
		String msg=" ( used default value)";
		if (obj != null) {
			newValue = obj.toString();
			msg="";
		}
		if(traceOn)System.out.println("propID=" + propID + "  value=" + newValue+msg);
		return newValue;
	}


	protected List<IOption> getOptionsByType(int type){
		type=IOption.INCLUDE_PATH; // sample
		return null;
	}
	/**
	 * Add an include path for C compiling to the existing include paths of the given
	 * Configuration
	 * 
	 * @param cf
	 *            the configuration of the project (e.g. Release, Debug, etc.)
	 * @param newIncludePath
	 *            include path to add
	 */
	private void addIncludePath(IConfiguration cf, String newIncludePath) {
		// note: could be > 1 path in 'newIncludePath'
		String ext = "c";
		ITool cfTool = cf.getToolFromInputExtension(ext);
		// do we need to also handle c++ case as well?

		// String optID = "gnu.c.compiler.option.include.paths";
		// IOption option = cfTool.getOptionById(optID);
		// run thru ALL options and check type for each
		int desiredType=IOption.INCLUDE_PATH;
		IOption[] allOptions=cfTool.getOptions();
		for (int i = 0; i < allOptions.length; i++) {
			IOption option = allOptions[i];
			try {
				int optionType = option.getValueType();
				if (desiredType == optionType) {
					String[] includePaths = option.getIncludePaths();
					String[] newIncludePaths = add(includePaths, newIncludePath);
					if(traceOn)System.out.println("add "+newIncludePath+" to existing includePaths: "+includePaths);
					ManagedBuildManager.setOption(cf, cfTool, option, newIncludePaths);
				    }
				} catch (BuildException e) {
					System.out.println("MPIProjectRunnable, problem getting include paths: "
									+ e.getMessage());
					e.printStackTrace();
				} 
			}
		}

	/**
	 * Add a new linker option
	 * @param cf the Configuration to which we want to add to linker options
	 * @param libName the lib name (e.g. "lib")
	 * @param libPath the library search path name (e.g. "c:/mypath/lib")
	 * 
	 */
	private void addLinkerOpt(IConfiguration cf, String libName, String libPath) {
		String ext = "o";
		ITool cfTool = cf.getToolFromInputExtension(ext);
		IOption[] allOptions = cfTool.getOptions();
		for (int i = 0; i < allOptions.length; i++) {
			IOption option = allOptions[i];
			try {
				int optionType = option.getValueType();
				switch (optionType) {
				case IOption.LIBRARIES:
					addOptionValue(cf, cfTool, option, libName);
					break;
				case IOption.LIBRARY_PATHS:
					addOptionValue(cf, cfTool, option, libPath);
				default:
					break;
				}
			} catch (BuildException e) {
				System.out
						.println("MPIProjectRunnable, problem getting include paths: "
								+ e.getMessage());
				e.printStackTrace();
			}
		}
	}

	
	private void setCompileCommand(IConfiguration cf, String buildCmd) {
		if(traceOn)System.out.println("compile cmd: "+buildCmd);
		ITool compiler = cf.getToolFromInputExtension("c");
		compiler.setToolCommand(buildCmd);
		
	}
	private void setLinkCommand(IConfiguration cf, String buildCmd) {
		if(traceOn)System.out.println("link cmd: "+buildCmd);
		ITool linker=cf.getToolFromInputExtension("o");
		linker.setToolCommand(buildCmd);
		
	}
	
	/**
	 * Add a value to a multi-valued tool option<br>
	 * (For example, add a path to include paths, or a lib to libraries list)
	 * @param cf the Configuration
	 * @param tool the tool in which to update the option
	 * @param option the option to update 
	 * @param value the new value to add to the list of existing values in the option
	 */
	private void addOptionValue(IConfiguration cf, ITool tool, IOption option, String value) {
		int stopHere=0;
		try {
			int type = option.getValueType();
			String[] valueList = null;
			switch (type) {
			case IOption.INCLUDE_PATH:
				valueList = option.getIncludePaths();
				valueList = add(valueList,value);
				break;
			case IOption.LIBRARIES:
				valueList = option.getLibraries();
				valueList=addNotPath(valueList, value);
				break;
			case IOption.STRING_LIST:// this is type for library search path cdt 3.1.2
				valueList = option.getStringListValue();
				valueList=addNotPath(valueList,value);
				break;
			case IOption.LIBRARY_PATHS:// this is type for library search path cdt 4.0 as on 5/11/07
				valueList=option.getBasicStringListValue();
				valueList=addNotPath(valueList,value);
				break;
			
			default:
				System.out.println("MPIProjectWizard runnable postprocessing (MPIProjectRunnable), can't get type of option for " + option.getName());
				return;

			}

			// add the new one to the list of old ones
			//valueList = add(valueList, value);
			// update the option in the managed builder options
			ManagedBuildManager.setOption(cf, tool, option, valueList);

		} catch (BuildException e) {
			System.out.println("MPIProjectRunnable.addOptionValue(), "+e.getMessage());
			System.out.println(e.getMessage());
			e.printStackTrace();
		}

	}

	/**
	 * Print the options for a project.<br>
	 * e.g. for Debug and Release configurations, get all the tools (compiler, linker etc.) then for each tool, 
	 * get all the options and print them.
	 * 
	 * IResource resource= ... ;// can be any resource, I use project.
     * IManagedBuildInfo mbo = ManagedBuildManager.getBuildInfo(resource);
     * IManagedProject mp=mbo.getManagedProject();
	 * <br>
	 * This helps in figuring out what they are, and what you want to change.
	 * Some sample output is at the bottom of this file.
	 * 
	 * @param proj the (managed) project for which print all this stuff.
	 */
	private void showOptions(IManagedProject proj) {
		if(traceOn)System.out.println("Managed Project: "+proj.getName());
		if(traceOn)System.out.println("Path.SEPARATOR="+Path.SEPARATOR);
		if(traceOn)System.out.println("Path.DEVICE_SEPARATOR="+Path.DEVICE_SEPARATOR);
		IConfiguration[] configs = proj.getConfigurations();
		try {
			for (int i = 0; i < configs.length; i++) {
				IConfiguration cf = configs[i];
				ITool[] allTools = cf.getTools();

				int numTools = allTools.length;
				System.out.println("Config " + i + ": " + cf.getName()+ " has "+numTools+" tools.");

				for (int k = 0; k < allTools.length; k++) {
					ITool tool = allTools[k];
					System.out.println("  Tool " + k + ": " + tool.getName());
					//boolean rc=tool.setToolCommand("foo");
					String toolCmd=tool.getToolCommand();
					String toolID=tool.getId();//"cdt.managedbuild.tool.gnu.c.compiler.cygwin.exe.debug.244391908"
					                           //"cdt.managedbuild.tool.gnu.c.linker.cygwin.exe.debug.107078755"
					System.out.println("  cmd="+toolCmd+"  toolID="+toolID);
					
					
					IOption[] options = tool.getOptions();

					for (int j = 0; j < options.length; j++) {
						IOption opt = options[j];
						String foundOptionID = opt.getId();
						System.out.println("    option " + j + " " + opt.getName() + " id="
								+ foundOptionID);
						if(opt.getValueType()==IOption.INCLUDE_PATH) {
							showIncludePaths(opt);
						}
					}
				}

				System.out.println("Config " + i + ": " + cf.getName()
						+ "======= End of ALL tools ");
				
				// another way to access Tool
				String ext = "c";
				ITool cfTool = cf.getToolFromInputExtension(ext);
				System.out.println("Tool by ext: " + ext + " is: " + cfTool.getName());
				
				// Look for include path when we know the option ID
				String optID = "gnu.c.compiler.option.include.paths";
				IOption option = cfTool.getOptionById(optID);
				System.out.println("Option " + optID + " is " + option.getName());

				IOption[] options = cfTool.getOptions();
				for (int j = 0; j < options.length; j++) {
					IOption opt = options[j];
					String foundOptionID = opt.getId();
					System.out.println("  option " + j + " " + opt.getName() + " id="
							+ foundOptionID);
					if(opt.getValueType()==IOption.INCLUDE_PATH) {
						showIncludePaths(opt);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * Display the list of include paths in an option which is presumed
	 * to be the include paths option.
	 * @param opt
	 * @throws BuildException
	 */
	private void showIncludePaths(IOption opt) throws BuildException {
		Assert.isTrue(opt.getValueType() == IOption.INCLUDE_PATH);
		// if the option is a list of include paths, display them.
		String[] includePaths = opt.getIncludePaths();
		for (int index = 0; index < includePaths.length; index++) {
			String path = includePaths[index];
			System.out.println("   include path " + index + ": " + path);
		}

	}

	/**
	 * Add one or more paths to the list of paths
	 * 
	 * @param includePaths the existing list of paths to add to
	 * @param newPath the new path to add; may be >1 directory, with path delimiter
	 * @return the merged list
	 */
	private String[] add(String[] includePaths, String newPath) {
		String pathSep=java.io.File.pathSeparator;  // semicolon for windows, colon for Mac/Linux
		List<String> newPathList = new ArrayList<String>();
		String path;
		for (int i = 0; i < includePaths.length; i++) {
			path = includePaths[i];
			newPathList.add(path);
		}
		String[] newPathArray=newPath.split(pathSep);
		for (int i = 0; i < newPathArray.length; i++) {
			path = newPathArray[i];
			newPathList.add(path);
		}
		
		String[] newArray=(String[])newPathList.toArray(new String[0]);
		return newArray;
		
		/*

		*/
	}

	/**
	 * Add a single string to an array of strings
	 * @param strList
	 * @param newStr
	 * @return
	 */
	private String[] addNotPath(String[] strList, String newStr) {
		int len = strList.length;
		String newList[] = new String[len + 1];
		System.arraycopy(strList, 0, newList, 0, len);
		newList[len] = newStr;
		return newList;
	}

}
/**
 * 
  Some sample tool data from showOptions()  on Windows XP with cygwin
  Tool 1: GCC C Compiler<p>

    option 0 Do not search system directories (-nostdinc) id=gnu.c.compiler.option.preprocessor.nostdinc<br>
    option 1 Preprocess only (-E) id=gnu.c.compiler.option.preprocessor.preprocess<br>
    option 2 Defined symbols (-D) id=gnu.c.compiler.option.preprocessor.def.symbols<br>
    option 3 Undefined symbols (-U) id=gnu.c.compiler.option.preprocessor.undef.symbol<br>
    option 4 Include paths (-I) id=gnu.c.compiler.option.include.paths<br>
    option 5 Optimization Level id=gnu.c.compiler.cygwin.exe.release.option.optimization.level<br>
    option 6 Other optimization flags id=gnu.c.compiler.option.optimization.flags<br>
    option 7 Debug Level id=gnu.c.compiler.cygwin.exe.release.option.debugging.level<br>
    option 8 Other debugging flags id=gnu.c.compiler.option.debugging.other<br>
    option 9 Generate gprof information (-pg) id=gnu.c.compiler.option.debugging.gprof<br>
    option 10 Generate prof information (-p) id=gnu.c.compiler.option.debugging.prof<br>
    option 11 Check syntax only (-fsyntax-only) id=gnu.c.compiler.option.warnings.syntax<br>
    option 12 Pedantic (-pedantic) id=gnu.c.compiler.option.warnings.pedantic<br>
    option 13 Pedantic warnings as errors (-pedantic-errors) id=gnu.c.compiler.option.warnings.pedantic.error<br>
    option 14 Inhibit all warnings (-w) id=gnu.c.compiler.option.warnings.nowarn<br>
    option 15 All warnings (-Wall) id=gnu.c.compiler.option.warnings.allwarn<br>
    option 16 Warnings as errors (-Werror) id=gnu.c.compiler.option.warnings.toerrors<br>
    option 17 Other flags id=gnu.c.compiler.option.misc.other<br>
    option 18 Verbose (-v) id=gnu.c.compiler.option.misc.verbose<br>
    option 19 Support ANSI programs (-ansi) id=gnu.c.compiler.option.misc.ansi<br>
 * 
 * Tool 2: GCC C Linker <br>
 * option 5 Libraries (-l) id=gnu.c.link.option.libs<br> 
 * option 6 Library search path (-L)  id=gnu.c.link.option.paths<br>
 * 
 */

