/****************************************************************************
 *			Tuning and Analysis Utilities
 *			http://www.cs.uoregon.edu/research/paracomp/tau
 ****************************************************************************
 * Copyright (c) 1997-2006
 *    Department of Computer and Information Science, University of Oregon
 *    Advanced Computing Laboratory, Los Alamos National Laboratory
 *    Research Center Juelich, ZAM Germany	
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Wyatt Spear - initial API and implementation
 ****************************************************************************/
package org.eclipse.ptp.etfw;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.IFileSystem;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ptp.etfw.toolopts.BuildTool;
import org.eclipse.ptp.etfw.toolopts.ExecTool;
import org.eclipse.ptp.etfw.toolopts.ExternalTool;
import org.eclipse.ptp.etfw.toolopts.ExternalToolProcess;
import org.eclipse.ptp.etfw.toolopts.IToolUITab;
import org.eclipse.ptp.etfw.toolopts.PostProcTool;
import org.eclipse.ptp.etfw.toolopts.ToolApp;
import org.eclipse.ptp.etfw.ui.AbstractToolConfigurationTab;
import org.eclipse.ptp.internal.etfw.BuildLaunchUtils;
import org.eclipse.ptp.internal.etfw.messages.Messages;
import org.eclipse.ptp.internal.etfw.toolopts.ToolMaker;

/**
 * Utilities for accessing ETFW tools
 * @author wspear
 * @since 7.0
 *
 */
public class ETFWUtils {

	private static ExternalToolProcess[] tools = null;

	private static ArrayList<IFileStore> workflowList = null;

	private static ArrayList<AbstractToolConfigurationTab> perfConfTabs = null;

	private static ArrayList<IToolUITab> toolUITabs = null;

	private static ArrayList<AbstractToolDataManager> perfConfManagers = null;

	private static ArrayList<IFileStore> getInternalXMLWorkflows() {
		if (workflowList != null) {
			return workflowList;
		}

		workflowList = new ArrayList<IFileStore>();

		final IExtensionRegistry registry = Platform.getExtensionRegistry();
		final IExtensionPoint extensionPoint = registry.getExtensionPoint("org.eclipse.ptp.etfw.workflows"); //$NON-NLS-1$
		final IExtension[] extensions = extensionPoint.getExtensions();

		for (final IExtension ext : extensions) {
			final IConfigurationElement[] elements = ext.getConfigurationElements();

			IFileStore ifs = null;

			for (final IConfigurationElement ce : elements) {
				try {
					final String plugspace = ext.getNamespaceIdentifier();
					final String aGetter = ce.getAttribute("XMLFile"); //$NON-NLS-1$

					final URI iuri = new URI(FileLocator.toFileURL((Platform.getBundle(plugspace).getEntry(aGetter))).toString()
							.replaceAll(" ", "%20")); //$NON-NLS-1$ //$NON-NLS-2$
					ifs = EFS.getLocalFileSystem().getStore(iuri);
					workflowList.add(ifs);
				} catch (final Exception e) {
					e.printStackTrace();
				}
			}
		}
		return workflowList;
	}

	/**
	 * Initializes the list of user defined performance data managers if necessary and returns the list.
	 * @return
	 */
	public static ArrayList<AbstractToolDataManager> getPerfConfManagers() {
		if (perfConfManagers != null) {
			return perfConfManagers;
		}

		perfConfManagers = new ArrayList<AbstractToolDataManager>();

		final IExtensionRegistry registry = Platform.getExtensionRegistry();
		final IExtensionPoint extensionPoint = registry.getExtensionPoint("org.eclipse.ptp.etfw.dataManagers"); //$NON-NLS-1$
		final IExtension[] extensions = extensionPoint.getExtensions();

		for (final IExtension ext : extensions) {
			final IConfigurationElement[] elements = ext.getConfigurationElements();

			for (final IConfigurationElement ce : elements) {
				try {
					final AbstractToolDataManager aGetter = (AbstractToolDataManager) ce.createExecutableExtension("class"); //$NON-NLS-1$
					// aGetter.setId(ce.getAttribute("id"));
					perfConfManagers.add(aGetter);
				} catch (final CoreException e) {
					e.printStackTrace();
					// PTPCorePlugin.log(e);
				}
			}
		}

		return perfConfManagers;
	}

	/**
	 * Given the name of a user defined performance data manager, returns the associatd AbstractToolDataManager object, or null if it is not found
	 * @param name of the performance data manager
	 * @return selected performance data manager
	 */
	public static AbstractToolDataManager getPerfDataManager(String name) {
		if (name == null) {
			return null;
		}
		AbstractToolDataManager check = null;
		final Iterator<AbstractToolDataManager> perfit = perfConfManagers.iterator();
		while (perfit.hasNext()) {
			check = perfit.next();
			if (check.getName().equals(name)) {
				return (check);
			}
		}
		return null;
	}

	/**
	 * Returns the list of user defined performance tool configuration tab objects. Initializes the list first if necessary.
	 * @return
	 */
	public static ArrayList<AbstractToolConfigurationTab> getPerfTabs() {
		if (perfConfTabs != null) {
			return perfConfTabs;
		}

		perfConfTabs = new ArrayList<AbstractToolConfigurationTab>();

		final IExtensionRegistry registry = Platform.getExtensionRegistry();
		final IExtensionPoint extensionPoint = registry.getExtensionPoint("org.eclipse.ptp.etfw.configurationTabs"); //$NON-NLS-1$
		final IExtension[] extensions = extensionPoint.getExtensions();

		for (final IExtension ext : extensions) {
			final IConfigurationElement[] elements = ext.getConfigurationElements();

			for (final IConfigurationElement ce : elements) {
				try {
					final AbstractToolConfigurationTab aGetter = (AbstractToolConfigurationTab) ce
							.createExecutableExtension("class"); //$NON-NLS-1$
					// aGetter.setId(ce.getAttribute("id"));
					perfConfTabs.add(aGetter);
				} catch (final CoreException e) {
					e.printStackTrace();
					// PTPCorePlugin.log(e);
				}
			}
		}

		return perfConfTabs;
	}

	/**
	 * Returns the performance tool at index dex in the performance tools array
	 * 
	 * @param dex
	 * @return
	 */
	public static ExternalToolProcess getTool(int dex) {
		if (dex < tools.length) {
			return tools[dex];
		} else {
			return null;
		}
	}

	/**
	 * Returns the performance tool with the given name from the performance tools array, or null if not found
	 * 
	 * @param toolName
	 * @return
	 */
	public static ExternalToolProcess getTool(String toolName) {
		for (final ExternalToolProcess tool : tools) {
			if (tool.toolName.equals(toolName)) {
				return tool;
			}
		}
		return null;
	}

	/**
	 * Returns an array of all of the non-virtual tool panes defined in available tool definition xml files
	 * Panes are ordered by tool, and within each tool by compilation, execution and analysis step
	 * 
	 * @return
	 * @since 7.0
	 */
	public static IToolUITab[] getToolPanes() {
		final ArrayList<IToolUITab> paneList = new ArrayList<IToolUITab>();
		IToolUITab[] panes = null;

		if (tools.length <= 0) {
			return null;
		}

		for (final ExternalToolProcess tool : tools) {
			for (int j = 0; j < tool.externalTools.size(); j++) {
				final ExternalTool t = tool.externalTools.get(j);
				if (t instanceof BuildTool) {
					final BuildTool bt = (BuildTool) t;
					insertPanes(bt.getAllCompilerPanes(), paneList);

				} else if (t instanceof ExecTool) {
					final ExecTool et = (ExecTool) t;
					for (final ToolApp execUtil : et.execUtils) {
						insertPanes(execUtil.toolPanes, paneList);
					}
				} else if (t instanceof PostProcTool) {
					final PostProcTool pt = (PostProcTool) t;
					for (final ToolApp analysisCommand : pt.analysisCommands) {
						insertPanes(analysisCommand.toolPanes, paneList);
					}
				}
				if (t.global != null) {
					insertPanes(t.global.toolPanes, paneList);
				}
			}
		}

		final ArrayList<IToolUITab> uitList = getToolUITabs();
		if (uitList != null && uitList.size() > 0) {// TODO: Improve ordering of panes
			for (int i = 0; i < uitList.size(); i++) {
				paneList.add(uitList.get(i));
			}
		}

		panes = new IToolUITab[paneList.size()];
		paneList.toArray(panes);

		return panes;
	}

	/**
	 * Returns an array of the ETFw tool objects defined in plugins or loaded from xml by the user
	 * @return
	 */
	public static ExternalToolProcess[] getTools() {
		return tools;
	}

	/**
	 * Returns a list of the tool UI tab objects defined by available tool workflows. Initializes the list if necessary.
	 * @since 7.0
	 */
	public static ArrayList<IToolUITab> getToolUITabs() {
		if (toolUITabs == null) {
			toolUITabs = new ArrayList<IToolUITab>();

			final IExtensionRegistry registry = Platform.getExtensionRegistry();
			final IExtensionPoint extensionPoint = registry.getExtensionPoint("org.eclipse.ptp.etfw.toolUITabs"); //$NON-NLS-1$
			final IExtension[] extensions = extensionPoint.getExtensions();

			for (final IExtension ext : extensions) {
				final IConfigurationElement[] elements = ext.getConfigurationElements();

				for (final IConfigurationElement ce : elements) {
					try {
						final IToolUITab aGetter = (IToolUITab) ce.createExecutableExtension("class"); //$NON-NLS-1$
						toolUITabs.add(aGetter);
					} catch (final CoreException e) {
						e.printStackTrace();
					}
				}
			}
		}
		return toolUITabs;
	}

	/**
	 * Inserts the tool panes located in the array panes into the List paneList
	 * 
	 * @param panes
	 * @param paneList
	 */
	private static void insertPanes(IToolUITab[] panes, List<IToolUITab> paneList) {
		if (panes != null && panes.length > 0) {
			for (int k = 0; k < panes.length; k++) {
				if (!panes[k].isVirtual()) {
					paneList.add(panes[k]);
				}
			}
		}
	}

	/**
	 * Inserts the tool panes located in the list panes into the List paneList
	 * 
	 * @param panes
	 * @param paneList
	 */
	private static void insertPanes(List<IToolUITab> panes, List<IToolUITab> paneList) {
		if (panes != null && panes.size() > 0) {
			for (int k = 0; k < panes.size(); k++) {
				if (!panes.get(k).isVirtual()) {
					paneList.add(panes.get(k));
				}
			}
		}
	}

	/**
	 * Reinitializes the performance tool data structures from the given XML definition file(s).
	 * 
	 */
	public static void refreshTools(IPreferenceStore store) {
		getInternalXMLWorkflows();
		final ArrayList<ExternalToolProcess> theTools = new ArrayList<ExternalToolProcess>();// null;

		final IFileSystem loc = EFS.getLocalFileSystem();

		final String fiList = store.getString(IToolLaunchConfigurationConstants.XMLLOCID);
		final String[] fiLocs = fiList.split(",,,"); //$NON-NLS-1$

		final List<IFileStore> files = new ArrayList<IFileStore>();
		IFileStore fi = null;
		for (final String fiLoc : fiLocs) {
			try {
				fi = loc.getStore(new URI(fiLoc));
			} catch (final URISyntaxException e) {
				// TODO Auto-generated catch block

				fi = EFS.getLocalFileSystem().getStore(new Path(fiLoc));
			}
			final IFileInfo finf = fi.fetchInfo();

			if (finf.exists() && !finf.isDirectory()) {
				files.add(fi);
			}
		}

		if (files.size() == 0) {
			final String epath = BuildLaunchUtils.checkLocalToolEnvPath("eclipse"); //$NON-NLS-1$
			if (epath != null) {
				IFileStore toolxml = loc.getStore(new Path(epath));
				IFileInfo finf = toolxml.fetchInfo();
				if (finf.exists()) {
					toolxml = toolxml.getChild("tool.xml"); //$NON-NLS-1$
					finf = toolxml.fetchInfo();
					if (finf.exists()) {
						files.add(toolxml);
						store.setValue(IToolLaunchConfigurationConstants.XMLLOCID, toolxml.toURI().toString());
					}
				}
			}
		}
		for (int i = 0; i < workflowList.size(); i++) {
			tools = ToolMaker.makeTools(workflowList.get(i));
			if (tools != null) {
				for (final ExternalToolProcess tool : tools) {
					theTools.add(tool);
				}
			}
		}

		for (int i = 0; i < files.size(); i++) {
			try {
				tools = ToolMaker.makeTools(files.get(i)); // ExternalToolProcess.getSample();//new ExternalToolProcess[1];;
			} catch (final Exception e) {
				tools = null;
				e.printStackTrace();
				System.out.println(Messages.Activator_ProblemReading + files.get(i).toString());
			}
			if (tools != null) {
				for (final ExternalToolProcess tool : tools) {
					theTools.add(tool);
				}
			}
			tools = null;
		}

		tools = theTools.toArray(new ExternalToolProcess[theTools.size()]);

		for (final ExternalToolProcess tool : tools) {
			BuildLaunchUtils.verifyLocalEnvToolPath(tool);
		}

	}
}
