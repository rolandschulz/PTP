/*******************************************************************************
 * Copyright (c) 2006,2010 IBM Corp. 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corp. - initial implementation
 *******************************************************************************/

package org.eclipse.ptp.pldt.wizards.wizardPages;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.cdt.core.templateengine.TemplateCore;
import org.eclipse.cdt.core.templateengine.process.ProcessArgument;
import org.eclipse.cdt.core.templateengine.process.ProcessFailureException;
import org.eclipse.cdt.core.templateengine.process.ProcessRunner;
import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.IManagedProject;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ptp.pldt.wizards.MpiWizardsPlugin;
import org.eclipse.ptp.pldt.wizards.messages.Messages;

/**
 * 
 * After the MPIProjectWizardPage runs, and we get MPI include path from the user,
 * 
 * we have this opportunity to use that information to modify the include paths, build cmd, etc.
 * in the project
 * 
 * <p>
 * This handles plain C projects; MPIProjectProcessCPP extends this to handle C++ projects
 * 
 * @author Beth Tibbitts
 * 
 */
public class MPIProjectProcess extends ProcessRunner {
	private static final boolean traceOn = false;
	private boolean wizTraceOn = MPIProjectWizardPage.wizardTraceOn;

	protected Map<String, String> valueStore;

	// this gets called after the "Finish" button on the wizard is pressed.
	@Override
	public void process(TemplateCore template, ProcessArgument[] args,
			String processId, IProgressMonitor monitor)
			throws ProcessFailureException {
		if (wizTraceOn)
			System.out.println("MPIProjectProcess().run()..."); //$NON-NLS-1$

		valueStore = template.getValueStore();
		String pageID = MPIProjectWizardPage.PAGE_ID;

		Object obj = getNewPropValue(pageID, MPIProjectWizardPage.DO_MPI_INCLUDES, null);
		// use the default value if nothing was set in the pageData by the user
		boolean setMPIprojectInfo = MPIProjectWizardPage.getDefaultUseMpiIncludes();
		if (obj != null)
			setMPIprojectInfo = Boolean.valueOf((String) obj);
		if (!setMPIprojectInfo) {
			if (traceOn)
				System.out.println("Do not save MPI info in this project."); //$NON-NLS-1$
			return;
		}

		// this process must be executed after a separate process which creates the project
		IProject proj = ResourcesPlugin.getWorkspace().getRoot().getProject(valueStore.get("projectName")); //$NON-NLS-1$
		if (!proj.exists()) {
			System.out.println(Messages.MPIProjectProcess_proj_does_not_exist_quit);
			return;
		}

		if (traceOn)
			System.out.println(Messages.MPIProjectProcess_project + proj.getName());

		// Collect the values that the user entered on the wizard page
		String propID = MPIProjectWizardPage.INCLUDE_PATH_PROP_ID;
		String newIncludePath = getNewPropValue(pageID, propID, ""); //$NON-NLS-1$
		if (traceOn)
			System.out.println("Got prop value for new include path: " + propID + "=" + newIncludePath); //$NON-NLS-1$ //$NON-NLS-2$

		propID = MPIProjectWizardPage.LIB_PROP_ID;
		String newLib = getNewPropValue(pageID, propID, "lib"); //$NON-NLS-1$

		propID = MPIProjectWizardPage.LIBRARY_SEARCH_PATH_PROP_ID;
		String newLibSearchPath = getNewPropValue(pageID, propID, ""); //$NON-NLS-1$

		propID = MPIProjectWizardPage.MPI_COMPILE_COMMAND_PROP_ID;
		String mpiCompileCommand = getNewPropValue(pageID, propID, "mpicc"); //$NON-NLS-1$

		propID = MPIProjectWizardPage.MPI_LINK_COMMAND_PROP_ID;
		String mpiLinkCommand = getNewPropValue(pageID, propID, "mpicc"); //$NON-NLS-1$

		IManagedBuildInfo info = null;
		try {
			info = ManagedBuildManager.getBuildInfo(proj);
			// note: assumed null if this is not a managed build project? will we get here in that case?
			if (traceOn)
				System.out.println("Build info: " + info); //$NON-NLS-1$
		} catch (Exception e) {
			System.out.println("MPIProjectProcess.run(), " + e.getMessage()); //$NON-NLS-1$
			e.printStackTrace();
			return;
		}
		assert (info != null);

		IManagedProject mProj = info.getManagedProject();
		if (traceOn)
			showOptions(mProj);
		/*
		 * // should this logically be in the wizard page logic instead?
		 * // so that the user sees it before the page where they can hit 'advanced' button and get project properties?
		 * IPreferenceStore ps = MpiPlugin.getDefault().getPreferenceStore();
		 * REMIND_MPI_INCLUDE_PATH = ps.getBoolean(REMIND_MPI_INCLUDE_PATH_KEY);
		 * if (REMIND_MPI_INCLUDE_PATH) {
		 * 
		 * String title = "MPI include path";
		 * String msg =
		 * "If your project shows errors and can't recognize MPI symbols, make sure your MPI header file is in the include path for the project. "
		 * ;
		 * msg +=
		 * " Since mpicc may do that for you for the build, Eclipse may not know about it.  You may want to add it to Project Properties, C/C++ General, Paths and Symbols, "
		 * ;
		 * msg += " so that the editor and indexer can find MPI symbols.";
		 * String toggleMsg = "Remind me about this again";
		 * MessageDialogWithToggle
		 * .openInformation(null, title, msg, toggleMsg, REMIND_MPI_INCLUDE_PATH, ps, REMIND_MPI_INCLUDE_PATH_KEY);
		 * REMIND_MPI_INCLUDE_PATH = ps.getBoolean(REMIND_MPI_INCLUDE_PATH_KEY);
		 * System.out.println("Remind include path is: " + REMIND_MPI_INCLUDE_PATH);
		 * }
		 */
		// add the include path, linker, build cmd, etc. values to all the configurations
		IConfiguration[] configs = mProj.getConfigurations();
		for (int i = 0; i < configs.length; i++) {
			IConfiguration cf = configs[i];
			if (traceOn)
				System.out.println("Config " + i + ": " + cf.getName()); //$NON-NLS-1$ //$NON-NLS-2$
			// mpicc should not need include or link lib info - bug 343761 - don't set it
			// (if no mpi preferences are set)

			// if an include path is available, set it
			boolean setIncludeLink = newIncludePath.length() != 0;
			if (traceOn)
				System.out.println("MPP.process(), setIncludeLink=" + setIncludeLink);
			if (setIncludeLink) {
				addIncludePath(cf, newIncludePath);
			}
			// if a lib search path is available, set it and the lib name
			if (newLibSearchPath.length() != 0) {
				addLinkerOpt(cf, newLib, newLibSearchPath); // add this back, if we are doing include?
			}

			// note: set the build commands regardless of include/linker info
			setCompileCommand(cf, mpiCompileCommand);
			setLinkCommand(cf, mpiLinkCommand);
		}
		if (traceOn)
			System.out.println("MPIProjectProcess, newIncludePath: " + newIncludePath); //$NON-NLS-1$
		if (traceOn)
			System.out.println("   newLib: " + newLib + "  newLibSrchPth: " + newLibSearchPath); //$NON-NLS-1$ //$NON-NLS-2$
		if (traceOn)
			System.out.println("   compileCmd: " + mpiCompileCommand); //$NON-NLS-1$
		if (traceOn)
			System.out.println("   linkCmd: " + mpiLinkCommand); //$NON-NLS-1$
		// ManagedBuildManger.saveBuildInfo(...) assures that the
		// values are persisted in the build model, otherwise they will
		// be lost when you shut down Eclipse.
		if (traceOn)
			System.out.println("ManagedBuildManager.saveBuildInfo..."); //$NON-NLS-1$

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
	 * 
	 * @param pageID
	 *            The pageID for our wizard page
	 * @param propID
	 *            The propID of the value we want
	 * @param defaultVal
	 *            The default value to use if the user didn't select anything,
	 *            or if we haven't gotten around to LETTING the user select anything yet.
	 * @return
	 */
	protected String getNewPropValue(String pageID, String propID, String defaultVal) {
		Object obj = valueStore.get(pageID + MPIProjectWizardPage.DOT + propID);
		// if selection made on page, obj is non-null.
		String newValue = defaultVal;
		String msg = " ( used default value)"; //$NON-NLS-1$
		if (obj != null) {
			newValue = obj.toString();
			msg = ""; //$NON-NLS-1$
		}
		if (traceOn)
			System.out.println("propID=" + propID + "  value=" + newValue + msg); //$NON-NLS-1$ //$NON-NLS-2$
		return newValue;
	}

	/**
	 * Add an include path for C compiling to the existing include paths of the given
	 * Configuration. XLC is handled as a special case to find its include path option;
	 * for other tools, we assume there is a single option for include paths, so we take
	 * the first one.
	 * 
	 * @param cf
	 *            the configuration of the project (e.g. Release, Debug, etc.)
	 * @param newIncludePath
	 *            include path(s) to add. If more than one, separate by
	 *            java.io.File.pathSeparator, which is semicolon for Windows, colon for Mac/Linux
	 */
	protected void addIncludePath(IConfiguration cf, String newIncludePath) {
		// note: could be > 1 path in 'newIncludePath'
		String ext = "c"; //$NON-NLS-1$
		ITool cfTool = cf.getToolFromInputExtension(ext);

		// String id = cfTool.getId(); // "cdt.managedbuild.tool.xlc.c.compiler.exe.debug.1423270745"
		String name = cfTool.getName();// "XL C Compiler"
		IOption option = null;
		if (name.startsWith("XL C")) { // special case for XL C compiler //$NON-NLS-1$
			option = cfTool.getOptionById("xlc.c.compiler.option.include.paths"); //$NON-NLS-1$
		} else { // otherwise we assume there is only one include path option.
			// FIXME we want the other include path option, the one from C/C++ General -> Paths and Symbols

			option = getFirstOptionByType(cf, cfTool, IOption.INCLUDE_PATH);

		}
		if (option != null) {
			String[] includePaths = null;
			try {
				includePaths = option.getIncludePaths();
			} catch (BuildException e) {
				e.printStackTrace();
			}
			String[] newIncludePaths = add(includePaths, newIncludePath);
			if (traceOn)
				System.out.println("        add " + newIncludePath + " to existing includePaths: " //$NON-NLS-1$ //$NON-NLS-2$
						+ unroll(includePaths));
			ManagedBuildManager.setOption(cf, cfTool, option, newIncludePaths);
		}
		else {
			System.out.println("MPIProjectProcess, no option for include paths found."); //$NON-NLS-1$
		}
	}

	/**
	 * Add a new linker option. Assumes that there is ONE instance of an option
	 * of type <code>IOption.LIBRARY_PATHS</code> and <code>IOption.LIBRARIES</code>.
	 * This method adds the libName and libPath to these two options.
	 * 
	 * @param cf
	 *            the Configuration to which we want to add to linker options
	 * @param libName
	 *            the lib name (e.g. "lib")
	 * @param libPath
	 *            the library search path name (e.g. "c:/mypath/lib")
	 * 
	 */
	protected void addLinkerOpt(IConfiguration cf, String libName, String libPath) {
		String ext = "o"; //$NON-NLS-1$
		ITool cfTool = cf.getToolFromInputExtension(ext);
		if (cfTool == null) {
			MessageDialog.openWarning(null, Messages.MPIProjectProcess_unable_to_set_linker_opt,
					Messages.MPIProjectProcess_unable_to_set_linker_opt_toolchain_invalid_q);
			return;
		}
		IOption lpOpt = getFirstOptionByType(cf, cfTool, IOption.LIBRARY_PATHS);
		addOptionValue(cf, cfTool, lpOpt, libPath);

		IOption libOpt = getFirstOptionByType(cf, cfTool, IOption.LIBRARIES);
		addOptionValue(cf, cfTool, libOpt, libName);
	}

	protected void setCompileCommand(IConfiguration cf, String buildCmd) {
		if (traceOn)
			System.out.println("compile cmd: " + buildCmd); //$NON-NLS-1$
		ITool compiler = cf.getToolFromInputExtension("c"); //$NON-NLS-1$
		compiler.setToolCommand(buildCmd);
	}

	protected void setLinkCommand(IConfiguration cf, String buildCmd) {
		if (traceOn)
			System.out.println("link cmd: " + buildCmd); //$NON-NLS-1$
		ITool linker = cf.getToolFromInputExtension("o"); //$NON-NLS-1$
		if (linker == null) {
			MessageDialog.openWarning(null, Messages.MPIProjectProcess_unable_to_set_link_command,
					Messages.MPIProjectProcess_unable_to_set_link_command_toolchain_invalid_q);
			return;
		}
		linker.setToolCommand(buildCmd);

	}

	/**
	 * Add a value to a multi-valued tool option<br>
	 * (For example, add a path to include paths, or a lib to libraries list)
	 * 
	 * @param cf
	 *            the Configuration
	 * @param tool
	 *            the tool in which to update the option
	 * @param option
	 *            the option to update
	 * @param value
	 *            the new value to add to the list of existing values in the option
	 */
	protected void addOptionValue(IConfiguration cf, ITool tool, IOption option, String value) {
		try {
			int type = option.getValueType();
			String[] valueList = null;
			switch (type) {
			case IOption.INCLUDE_PATH:
				valueList = option.getIncludePaths();
				valueList = add(valueList, value);
				break;
			case IOption.LIBRARIES:
				valueList = option.getLibraries();
				valueList = addNotPath(valueList, value);
				break;
			case IOption.LIBRARY_PATHS:// this is type for library search path cdt 4.0
				valueList = option.getBasicStringListValue();
				valueList = addNotPath(valueList, value);
				break;

			default:
				System.out
						.println("MPIProjectWizard Process postprocessing (MPIProjectProcess), can't get type of option for " + option.getName()); //$NON-NLS-1$
				return;
			}
			// update the option in the managed builder options
			ManagedBuildManager.setOption(cf, tool, option, valueList);

		} catch (BuildException e) {
			System.out.println("MPIProjectProcess.addOptionValue(), " + e.getMessage()); //$NON-NLS-1$
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
	 * IManagedProject mp=mbo.getManagedProject(); <br>
	 * This helps in figuring out what they are, and what you want to change.
	 * Some sample output is at the bottom of this file.
	 * 
	 * @param proj
	 *            the (managed) project for which print all this stuff.
	 */
	protected void showOptions(IManagedProject proj) {
		if (traceOn)
			System.out.println("Managed Project: " + proj.getName()); //$NON-NLS-1$
		if (traceOn)
			System.out.println("Path.SEPARATOR=" + Path.SEPARATOR); //$NON-NLS-1$
		if (traceOn)
			System.out.println("Path.DEVICE_SEPARATOR=" + Path.DEVICE_SEPARATOR); //$NON-NLS-1$
		IConfiguration[] configs = proj.getConfigurations();
		try {
			for (int i = 0; i < configs.length; i++) {
				IConfiguration cf = configs[i];
				ITool[] allTools = cf.getTools();

				int numTools = allTools.length;
				System.out.println("Config " + i + ": " + cf.getName() + " has " + numTools + " tools."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

				for (int k = 0; k < allTools.length; k++) {
					ITool tool = allTools[k];
					System.out.println("  Tool " + k + ": " + tool.getName()); //$NON-NLS-1$ //$NON-NLS-2$
					// boolean rc=tool.setToolCommand("foo");
					String toolCmd = tool.getToolCommand();
					String toolID = tool.getId();
					System.out.println("  cmd=" + toolCmd + "  toolID=" + toolID); //$NON-NLS-1$ //$NON-NLS-2$

					IOption[] options = tool.getOptions();

					for (int j = 0; j < options.length; j++) {
						IOption opt = options[j];
						String foundOptionID = opt.getId();
						int type = opt.getValueType();
						System.out.println("    option " + j + " " + opt.getName() + " id=" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
								+ foundOptionID + "  type=" + showType(type)); //$NON-NLS-1$
						if (opt.getValueType() == IOption.INCLUDE_PATH) {
							showIncludePaths(opt);
						}
					}
				}

				System.out.println("Config " + i + ": " + cf.getName() //$NON-NLS-1$ //$NON-NLS-2$
						+ "======= End of ALL tools "); //$NON-NLS-1$

				// another way to access Tool
				String ext = "c"; //$NON-NLS-1$
				ITool cfTool = cf.getToolFromInputExtension(ext);
				System.out.println("Tool by ext: " + ext + " is: " + cfTool.getName()); //$NON-NLS-1$ //$NON-NLS-2$

				// Look for include path
				IOption option = getFirstOptionByType(cf, cfTool, IOption.INCLUDE_PATH);
				if (option != null) {
					String oname = option.getName();
					System.out.println("Option " + option.getId() + " is " + oname); //$NON-NLS-1$ //$NON-NLS-2$
					showIncludePaths(option);
				}
				else
					System.out.println("No include path option found. "); //$NON-NLS-1$

			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	protected String showType(int type) {
		if (type == IOption.INCLUDE_PATH)
			return type + " (IOption.INCLUDE_PATH)"; //$NON-NLS-1$
		if (type == IOption.LIBRARY_PATHS)
			return type + " (IOption.LIBRARY_PATHS)"; //$NON-NLS-1$
		if (type == IOption.LIBRARIES)
			return type + " (IOption.LIBRARIES)"; //$NON-NLS-1$
		return type + " "; //$NON-NLS-1$
	}

	/**
	 * Display the list of include paths in an option which is presumed
	 * to be the include paths option.
	 * 
	 * @param opt
	 * @throws BuildException
	 */
	protected void showIncludePaths(IOption opt) throws BuildException {
		assert opt.getValueType() == IOption.INCLUDE_PATH;
		// if the option is a list of include paths, display them.
		String[] includePaths = opt.getIncludePaths();
		for (int index = 0; index < includePaths.length; index++) {
			String path = includePaths[index];
			System.out.println("   include path " + index + ": " + path); //$NON-NLS-1$ //$NON-NLS-2$
		}

	}

	/**
	 * Add one or more paths to the list of paths
	 * 
	 * @param existingPaths
	 *            the existing list of paths to add to
	 * @param newPath
	 *            the new path to add; may be >1 directory, with path delimiter java.io.File.pathSeparator
	 *            (usually semicolon or colon)
	 * @return the merged list
	 */
	protected String[] add(String[] existingPaths, String newPath) {
		String pathSep = java.io.File.pathSeparator; // semicolon for windows, colon for Mac/Linux
		List<String> newPathList = new ArrayList<String>();
		String path;
		for (int i = 0; i < existingPaths.length; i++) {
			path = existingPaths[i];
			newPathList.add(path);
		}
		String[] newPathArray = newPath.split(pathSep);
		for (int i = 0; i < newPathArray.length; i++) {
			path = newPathArray[i];
			newPathList.add(path);
		}

		String[] newArray = (String[]) newPathList.toArray(new String[0]);
		return newArray;
	}

	/**
	 * Add a single string to an array of strings
	 * 
	 * @param strList
	 * @param newStr
	 * @return
	 */
	protected String[] addNotPath(String[] strList, String newStr) {
		int len = strList.length;
		String newList[] = new String[len + 1];
		System.arraycopy(strList, 0, newList, 0, len);
		newList[len] = newStr;
		return newList;
	}

	/**
	 * Simple string representation of items in a string array
	 * 
	 * @param list
	 * @return
	 */
	protected String unroll(String[] list) {
		StringBuffer result = new StringBuffer();
		// list = new String[]{"one","two"};
		// System.out.println(list);
		for (int i = 0; i < list.length; i++) {
			String string = list[i];
			result.append(string);
			result.append(", "); //$NON-NLS-1$
		}
		if (result.length() > 2) {
			result.delete(result.length() - 2, result.length() - 1);
		}
		return result.toString();
	}

	/**
	 * Get the options of a specific type.<br>
	 * We really need an ITool.getOptionsByType(int) so we don't have to do
	 * this.
	 * 
	 * @param cf
	 *            the IConfiguration in which to search
	 * @param cfTool
	 *            the ITool whose options are being searched
	 * @param optionType
	 *            the option type we are looking for, e.g. IOption.SOMETHING
	 * @return
	 */
	protected List<IOption> getOptionsByType(IConfiguration cf, ITool cfTool, int optionType) {

		// run thru ALL options and check type for each, returning the ones that match
		IOption[] allOptions = cfTool.getOptions();
		List<IOption> foundOptions = new ArrayList<IOption>();

		for (int i = 0; i < allOptions.length; i++) {
			IOption option = allOptions[i];

			int oType = 0;
			try {
				oType = option.getValueType();
			} catch (BuildException e) {
				e.printStackTrace();
				continue;
			}
			if (optionType == oType) {
				// add it to the list
				foundOptions.add(option);
			}

		}
		// IOption[] ret= foundOptions.toArray(new IOption[foundOptions.size()]);
		return foundOptions;
	}

	/**
	 * Returns the <i>first</i> option of the specified type from the tool
	 * 
	 * @param cf
	 * @param cfTool
	 * @param optionType
	 * @return
	 */
	protected IOption getFirstOptionByType(IConfiguration cf, ITool cfTool, int optionType) {
		List<IOption> allOptions = getOptionsByType(cf, cfTool, optionType);
		if (allOptions.size() > 0) {
			return allOptions.get(0);
		}
		return null;
	}
}

/**
 * 
 * Some sample tool data from showOptions() on Windows XP with cygwin Tool 1:
 * GCC C Compiler
 * <p>
 * 
 * option 0 Do not search system directories (-nostdinc) id=gnu.c.compiler.option.preprocessor.nostdinc<br>
 * option 1 Preprocess only (-E) id=gnu.c.compiler.option.preprocessor.preprocess<br>
 * option 2 Defined symbols (-D) id=gnu.c.compiler.option.preprocessor.def.symbols<br>
 * option 3 Undefined symbols (-U) id=gnu.c.compiler.option.preprocessor.undef.symbol<br>
 * option 4 Include paths (-I) id=gnu.c.compiler.option.include.paths<br>
 * option 5 Optimization Level id=gnu.c.compiler.cygwin.exe.release.option.optimization.level<br>
 * option 6 Other optimization flags id=gnu.c.compiler.option.optimization.flags<br>
 * option 7 Debug Level id=gnu.c.compiler.cygwin.exe.release.option.debugging.level<br>
 * option 8 Other debugging flags id=gnu.c.compiler.option.debugging.other<br>
 * option 9 Generate gprof information (-pg) id=gnu.c.compiler.option.debugging.gprof<br>
 * option 10 Generate prof information (-p) id=gnu.c.compiler.option.debugging.prof<br>
 * option 11 Check syntax only (-fsyntax-only) id=gnu.c.compiler.option.warnings.syntax<br>
 * option 12 Pedantic (-pedantic) id=gnu.c.compiler.option.warnings.pedantic<br>
 * option 13 Pedantic warnings as errors (-pedantic-errors) id=gnu.c.compiler.option.warnings.pedantic.error<br>
 * option 14 Inhibit all warnings (-w) id=gnu.c.compiler.option.warnings.nowarn<br>
 * option 15 All warnings (-Wall) id=gnu.c.compiler.option.warnings.allwarn<br>
 * option 16 Warnings as errors (-Werror) id=gnu.c.compiler.option.warnings.toerrors<br>
 * option 17 Other flags id=gnu.c.compiler.option.misc.other<br>
 * option 18 Verbose (-v) id=gnu.c.compiler.option.misc.verbose<br>
 * option 19 Support ANSI programs (-ansi) id=gnu.c.compiler.option.misc.ansi<br>
 * 
 * Tool 2: GCC C Linker <br>
 * option 5 Libraries (-l) id=gnu.c.link.option.libs<br>
 * option 6 Library search path (-L) id=gnu.c.link.option.paths<br>
 * 
 */

