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

package org.eclipse.ptp.tau.popup.actions;
import java.io.File;
//import java.io.FileNotFoundException;
import java.io.IOException;
//import java.io.RandomAccessFile;
import java.lang.reflect.InvocationTargetException;
//import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.cdt.core.model.ICProject;
//import org.eclipse.cdt.debug.core.CDebugCorePlugin;
//import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
//import org.eclipse.cdt.debug.core.ICDebugConfiguration;
import org.eclipse.cdt.launch.AbstractCLaunchDelegate;
import org.eclipse.cdt.launch.internal.ui.LaunchMessages;
import org.eclipse.cdt.launch.internal.ui.LaunchUIPlugin;
import org.eclipse.cdt.ui.CElementLabelProvider;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.debug.ui.ILaunchShortcut;
//import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.dialogs.TwoPaneElementSelector;
//import org.eclipse.ptp.launch.ui.*;
//import org.eclipse.ptp.launch.*;
import org.eclipse.ptp.core.IPTPLaunchConfigurationConstants;
import org.eclipse.ptp.debug.core.IPDebugConfiguration;
import org.eclipse.ptp.debug.core.PTPDebugCorePlugin;
import org.eclipse.ptp.tau.TauPlugin;
//import edu.uoregon.tau.taujava.TaujavaPlugin;
//import org.apache.derby.*;

	public class TAUApplicationLaunchShortcut implements ILaunchShortcut {
		//Location of paraprof program
		public File paradir=null;
		//Profile/trace output flags (for directory creation)
		public boolean prof=true;
		public boolean trac=false;
		//Map for env variables
		Map appMap=null;
		public ILaunchConfigurationWorkingCopy wc = null;
		//Profile and trace output directories
		File predir=null;
		File tredir=null;
		
		public void launch(IEditorPart editor, String mode) {
			searchAndLaunch(new Object[] { editor.getEditorInput()}, mode);
		}

		public void launch(ISelection selection, String mode) {
			if (selection instanceof IStructuredSelection) {
				searchAndLaunch(((IStructuredSelection) selection).toArray(), mode);
			}
		}

		public void launch(IBinary bin, String mode) {
			try {/*Only this version of launch is used presently*/
				ILaunchConfiguration configuration=null;

				String programCPU = bin.getCPU();

				//Define the debugger for the launch. Prompt the user if there is more then 1 debugger.
				IPDebugConfiguration debugConfig = null;
				IPDebugConfiguration[] debugConfigs = PTPDebugCorePlugin.getDefault().getDebugConfigurations();
				List debugList = new ArrayList(debugConfigs.length);
				String os = Platform.getOS();
				for (int i = 0; i < debugConfigs.length; i++) {
					String platform = debugConfigs[i].getPlatform();
					if (debugConfigs[i].supportsMode(IPTPLaunchConfigurationConstants.DEBUGGER_MODE_RUN)) {
						if (platform.equals("*") || platform.equals(os)) { //$NON-NLS-1$
							if (debugConfigs[i].supportsCPU(programCPU)) 
								debugList.add(debugConfigs[i]);
						}
					}
				}
				debugConfigs = (IPDebugConfiguration[]) debugList.toArray(new IPDebugConfiguration[0]);
				if (debugConfigs.length == 1) {//Use the only debugger listed
					debugConfig = debugConfigs[0];
				} else if (debugConfigs.length > 1) {//Ask which debugger to use
					debugConfig = chooseDebugConfig(debugConfigs, mode);
				}
				if (debugConfig != null) {//Create the configuration to run
					configuration = createConfiguration(bin, debugConfig);
				}

				if (configuration != null) {//Run the configuration if possible
					DebugUITools.saveAndBuildBeforeLaunch();
					configuration.launch(mode, null);
				}
				else
				{return;}//No valid configuration
				
				//If indicated, launch paraprof on profile output.
				IPreferenceStore pstore = TauPlugin.getDefault().getPreferenceStore();
				boolean para = pstore.getBoolean("runParaProf");
				if(para&&prof)
				{
					try {
						String bpath=pstore.getString("TAUCDTArchPath");
						String paracmd=bpath+File.separator+"bin"+File.separator+"paraprof ";
						Runtime.getRuntime().exec(paracmd+paradir.getPath());
					} catch (IOException e) {
						// TO DO Auto-generated catch block
						e.printStackTrace();
					}
				}
				//Change the env variables to the 'repeat' directories, to avoid overwriting initial output.
				if(prof){
					appMap.put("PROFILEDIR",predir.getPath());
				}
				if(trac){
					appMap.put("TRACEDIR",tredir.getPath());
				}
				wc.setAttribute(ILaunchManager.ATTR_ENVIRONMENT_VARIABLES,appMap);
				configuration = wc.doSave();
				
			} catch (CoreException e) {
				e.printStackTrace();
				LaunchUIPlugin.errorDialog(LaunchMessages.getString("CApplicationLaunchShortcut.LaunchFailed"), e.getStatus());  //$NON-NLS-1$
			}
		}

		/**
		 * Locate a configuration to relaunch for the given type.  If one cannot be found, create one.
		 * 
		 * @return a re-useable config or <code>null</code> if none
		 */
		/*This is not used presently*/
		protected ILaunchConfiguration findLaunchConfiguration(IBinary bin, String mode) {
			ILaunchConfiguration configuration = null;
			ILaunchConfigurationType configType = getCLaunchConfigType();
			List candidateConfigs = Collections.EMPTY_LIST;
			try {
				ILaunchConfiguration[] configs = DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurations(configType);
				candidateConfigs = new ArrayList(configs.length);
				
				//AbstractParallelLaunchConfigurationDelegate APLCD = null;
				//ParallelLaunchConfigurationDelegate test2=null;
				for (int i = 0; i < configs.length; i++) {
					
					ILaunchConfiguration config = configs[i];
					IPath programPath = AbstractCLaunchDelegate.getProgramPath(config);
					String projectName = AbstractCLaunchDelegate.getProjectName(config);
					IPath name = bin.getResource().getProjectRelativePath();
					if (programPath != null && programPath.equals(name)) {
						if (projectName != null && projectName.equals(bin.getCProject().getProject().getName())) {
							candidateConfigs.add(config);
						}
					}
				}
			} catch (CoreException e) {
				LaunchUIPlugin.log(e);
			}

			// If there are no existing configs associated with the IBinary, create one.
			// If there is exactly one config associated with the IBinary, return it.
			// Otherwise, if there is more than one config associated with the IBinary, prompt the
			// user to choose one.
			int candidateCount = candidateConfigs.size();
			if (candidateCount < 1) {
				String programCPU = bin.getCPU();

				// Prompt the user if more then 1 debugger.
				IPDebugConfiguration debugConfig = null;
				IPDebugConfiguration[] debugConfigs = PTPDebugCorePlugin.getDefault().getDebugConfigurations();
				List debugList = new ArrayList(debugConfigs.length);
				String os = Platform.getOS();
				for (int i = 0; i < debugConfigs.length; i++) {
					String platform = debugConfigs[i].getPlatform();
					if (debugConfigs[i].supportsMode(IPTPLaunchConfigurationConstants.DEBUGGER_MODE_RUN)) {
						if (platform.equals("*") || platform.equals(os)) { //$NON-NLS-1$
							if (debugConfigs[i].supportsCPU(programCPU)) 
								debugList.add(debugConfigs[i]);
						}
					}
				}
				debugConfigs = (IPDebugConfiguration[]) debugList.toArray(new IPDebugConfiguration[0]);
				if (debugConfigs.length == 1) {
					debugConfig = debugConfigs[0];
				} else if (debugConfigs.length > 1) {
					debugConfig = chooseDebugConfig(debugConfigs, mode);
				}
				if (debugConfig != null) {
					configuration = createConfiguration(bin, debugConfig);
					debugConfig = debugConfigs[0];
				}
			} else if (candidateCount == 1) {
				configuration = (ILaunchConfiguration) candidateConfigs.get(0);
			} else {
				// Prompt the user to choose a config.  A null result means the user
				// cancelled the dialog, in which case this method returns null,
				// since cancelling the dialog should also cancel launching anything.
				configuration = chooseConfiguration(candidateConfigs, mode);
			}
			return configuration;
		}

		/**
		 * Method createConfiguration.
		 * @param bin
		 * @return ILaunchConfiguration
		 */
		private ILaunchConfiguration createConfiguration(IBinary bin, IPDebugConfiguration debugConfig) {
			ILaunchConfiguration config = null;
			try {
				String projectName = bin.getResource().getProjectRelativePath().toString();
				int opentau=projectName.indexOf("(tau");
				String maketype=null;
				if(opentau==-1){maketype="(TAU)";}else
				maketype=projectName.substring(opentau);

				ILaunchConfigurationType configType = getCLaunchConfigType();
				wc=configType.newInstance(null, getLaunchManager().generateUniqueLaunchConfigurationNameFrom(bin.getElementName())+maketype+"(TAU)");
				wc.setAttribute(IPTPLaunchConfigurationConstants.ATTR_APPLICATION_NAME, projectName);
				wc.setAttribute(IPTPLaunchConfigurationConstants.ATTR_PROJECT_NAME, bin.getCProject().getElementName());
				wc.setAttribute(IPTPLaunchConfigurationConstants.ATTR_DEBUGGER_START_MODE, true);
				wc.setAttribute(IPTPLaunchConfigurationConstants.ATTR_DEBUGGER_START_MODE,IPTPLaunchConfigurationConstants.DEBUGGER_MODE_RUN);
				wc.setAttribute(IPTPLaunchConfigurationConstants.ATTR_DEBUGGER_ID, debugConfig.getID());
				
				IPreferenceStore pstore = TauPlugin.getDefault().getPreferenceStore();
				String numproc=String.valueOf(pstore.getInt("numProc"));
				wc.setAttribute(IPTPLaunchConfigurationConstants.NUMBER_OF_PROCESSES, numproc);
				/*
				if(IPTPLaunchConfigurationConstants.DEF_NUMBER_OF_PROCESSES.equals("0"))
				{
					wc.setAttribute(IPTPLaunchConfigurationConstants.NUMBER_OF_PROCESSES, numproc);
				}
				else
					wc.setAttribute(IPTPLaunchConfigurationConstants.NUMBER_OF_PROCESSES, IPTPLaunchConfigurationConstants.DEF_NUMBER_OF_PROCESSES);*/
		        wc.setAttribute(IPTPLaunchConfigurationConstants.PROCESSES_PER_NODE, IPTPLaunchConfigurationConstants.DEF_PROCESSES_PER_NODE);
		        wc.setAttribute(IPTPLaunchConfigurationConstants.FIRST_NODE_NUMBER, IPTPLaunchConfigurationConstants.DEF_FIRST_NODE_NUMBER);

				appMap=new HashMap();

				String builddir;

				if(!pstore.getBoolean("defaultOutputRoot"))
				{
					IWorkspace workspace=ResourcesPlugin.getWorkspace();
					IWorkspaceRoot root = workspace.getRoot();
					IResource resource = root.findMember(bin.getCProject().getPath());
					File tauout = resource.getLocation().toFile();
					builddir = tauout.getPath();
				}
				else
				{
					builddir=pstore.getString("outputRoot");
				}
				
				
				String makefile = maketype;
				
				//Are profile and trace, respectively, used?
				if(makefile.indexOf("-trace")!=-1)
				{
					trac=true;
					if(!(makefile.indexOf("-profile")!=-1)){
						prof=false;
					}
				}
				
				builddir+=File.separator+"TAU_Output"+File.separator+projectName+File.separator;

				Calendar timer = Calendar.getInstance();
				SimpleDateFormat timestamp = new SimpleDateFormat("yyyy_MMMMM_dd(hh_mm_ssaaa)");
				Date time = timer.getTime();
				String ftime = timestamp.format(time);

				//appMap.put("LD_LIBRARY_PATH",lpath);
				if(prof){
				File pdir=new File(builddir+"profile"+File.separator+ftime);
				paradir=pdir;
				pdir.mkdirs();
				predir=new File(pdir.getPath()+File.separator+"Re-Run");
				predir.mkdirs();
				appMap.put("PROFILEDIR",pdir.getPath());
				//System.out.println(pdir.getPath());
				}
				
				if(trac){
				File tdir=new File(builddir+"trace"+File.separator+ftime);
				tdir.mkdirs();
				tredir=new File(tdir.getPath()+File.separator+"Re-Run");
				tredir.mkdirs();
				appMap.put("TRACEDIR",tdir.getPath());
				//System.out.println(tdir.getPath());
				}
				wc.setAttribute(ILaunchManager.ATTR_ENVIRONMENT_VARIABLES,appMap);
				config = wc.doSave();
			} catch (CoreException ce) {
				LaunchUIPlugin.log(ce);
			}
			return config;
		}

		/**
		 * Method getCLaunchConfigType.
		 * @return ILaunchConfigurationType
		 */
		protected ILaunchConfigurationType getCLaunchConfigType() {
			return getLaunchManager().getLaunchConfigurationType(IPTPLaunchConfigurationConstants.PTP_LAUNCHCONFIGURETYPE_ID);//ICDTLaunchConfigurationConstants.ID_LAUNCH_C_APP
		}

		protected ILaunchManager getLaunchManager() {
			return DebugPlugin.getDefault().getLaunchManager();
		}

		/**
		 * Convenience method to get the window that owns this action's Shell.
		 */
		protected Shell getShell() {
			return LaunchUIPlugin.getActiveWorkbenchShell();
		}

		/**
		 * Method chooseDebugConfig.
		 * @param debugConfigs
		 * @param mode
		 * @return ICDebugConfiguration
		 */
		private IPDebugConfiguration chooseDebugConfig(IPDebugConfiguration[] debugConfigs, String mode) {
			ILabelProvider provider = new LabelProvider() {
				/**
				 * The <code>LabelProvider</code> implementation of this 
				 * <code>ILabelProvider</code> method returns the element's <code>toString</code>
				 * string. Subclasses may override.
				 */
				public String getText(Object element) {
					if (element == null) {
						return ""; //$NON-NLS-1$
					} else if (element instanceof IPDebugConfiguration) {
						return ((IPDebugConfiguration) element).getName();
					}
					return element.toString();
				}
			};
			ElementListSelectionDialog dialog = new ElementListSelectionDialog(getShell(), provider);
			dialog.setElements(debugConfigs);
			dialog.setTitle(getDebugConfigDialogTitleString(debugConfigs, mode)); 
			dialog.setMessage(getDebugConfigDialogMessageString(debugConfigs, mode)); 
			dialog.setMultipleSelection(false);
			int result = dialog.open();
			provider.dispose();
			if (result == Window.OK) {
				return (IPDebugConfiguration) dialog.getFirstResult();
			}
			return null;
		}

		protected String getDebugConfigDialogTitleString(IPDebugConfiguration [] configList, String mode) {
			return LaunchMessages.getString("CApplicationLaunchShortcut.LaunchDebugConfigSelection");  //$NON-NLS-1$
		}

		protected String getDebugConfigDialogMessageString(IPDebugConfiguration [] configList, String mode) {
			if (mode.equals(ILaunchManager.DEBUG_MODE)) {
				return LaunchMessages.getString("CApplicationLaunchShortcut.ChooseConfigToDebug");  //$NON-NLS-1$
			} else if (mode.equals(ILaunchManager.RUN_MODE)) {
				return LaunchMessages.getString("CApplicationLaunchShortcut.ChooseConfigToRun");  //$NON-NLS-1$
			}
			return LaunchMessages.getString("CApplicationLaunchShortcut.Invalid_launch_mode_1"); //$NON-NLS-1$
		}

		/**
		 * Show a selection dialog that allows the user to choose one of the specified
		 * launch configurations.  Return the chosen config, or <code>null</code> if the
		 * user cancelled the dialog.
		 */
		protected ILaunchConfiguration chooseConfiguration(List configList, String mode) {
			IDebugModelPresentation labelProvider = DebugUITools.newDebugModelPresentation();
			ElementListSelectionDialog dialog = new ElementListSelectionDialog(getShell(), labelProvider);
			dialog.setElements(configList.toArray());
			dialog.setTitle(getLaunchSelectionDialogTitleString(configList, mode)); 
			dialog.setMessage(getLaunchSelectionDialogMessageString(configList, mode)); 
			dialog.setMultipleSelection(false);
			int result = dialog.open();
			labelProvider.dispose();
			if (result == Window.OK) {
				return (ILaunchConfiguration) dialog.getFirstResult();
			}
			return null;
		}

		protected String getLaunchSelectionDialogTitleString(List configList, String mode) {
			return LaunchMessages.getString("CApplicationLaunchShortcut.LaunchConfigSelection");  //$NON-NLS-1$
		}

		protected String getLaunchSelectionDialogMessageString(List binList, String mode) {
			if (mode.equals(ILaunchManager.DEBUG_MODE)) {
				return LaunchMessages.getString("CApplicationLaunchShortcut.ChooseLaunchConfigToDebug");  //$NON-NLS-1$
			} else if (mode.equals(ILaunchManager.RUN_MODE)) {
				return LaunchMessages.getString("CApplicationLaunchShortcut.ChooseLaunchConfigToRun");  //$NON-NLS-1$
			}
			return LaunchMessages.getString("CApplicationLaunchShortcut.Invalid_launch_mode_2"); //$NON-NLS-1$
		}

		/**
		 * Prompts the user to select a  binary
		 * 
		 * @return the selected binary or <code>null</code> if none.
		 */
		protected IBinary chooseBinary(List binList, String mode) {
			ILabelProvider programLabelProvider = new CElementLabelProvider() {
				public String getText(Object element) {
					if (element instanceof IBinary) {
						IBinary bin = (IBinary)element;
						StringBuffer name = new StringBuffer();
						name.append(bin.getPath().lastSegment());
						return name.toString();
					}
					return super.getText(element);
				}
			};

			ILabelProvider qualifierLabelProvider = new CElementLabelProvider() {
				public String getText(Object element) {
					if (element instanceof IBinary) {
						IBinary bin = (IBinary)element;
						StringBuffer name = new StringBuffer();
						name.append(bin.getCPU() + (bin.isLittleEndian() ? "le" : "be")); //$NON-NLS-1$ //$NON-NLS-2$
						name.append(" - "); //$NON-NLS-1$
						name.append(bin.getPath().toString());
						return name.toString();
					}
					return super.getText(element);
				}
			};

			TwoPaneElementSelector dialog = new TwoPaneElementSelector(getShell(), programLabelProvider, qualifierLabelProvider);
			dialog.setElements(binList.toArray());
			dialog.setTitle(getBinarySelectionDialogTitleString(binList, mode)); //$NON-NLS-1$
			dialog.setMessage(getBinarySelectionDialogMessageString(binList, mode)); //$NON-NLS-1$
			dialog.setUpperListLabel(LaunchMessages.getString("Launch.common.BinariesColon")); //$NON-NLS-1$
			dialog.setLowerListLabel(LaunchMessages.getString("Launch.common.QualifierColon")); //$NON-NLS-1$
			dialog.setMultipleSelection(false);
			if (dialog.open() == Window.OK) {
				return (IBinary) dialog.getFirstResult();
			}

			return null;
		}

		protected String getBinarySelectionDialogTitleString(List binList, String mode) {
			return LaunchMessages.getString("CApplicationLaunchShortcut.CLocalApplication");  //$NON-NLS-1$
		}

		protected String getBinarySelectionDialogMessageString(List binList, String mode) {
			if (mode.equals(ILaunchManager.DEBUG_MODE)) {
				return LaunchMessages.getString("CApplicationLaunchShortcut.ChooseLocalAppToDebug");  //$NON-NLS-1$
			} else if (mode.equals(ILaunchManager.RUN_MODE)) {
				return LaunchMessages.getString("CApplicationLaunchShortcut.ChooseLocalAppToRun");  //$NON-NLS-1$
			}
			return LaunchMessages.getString("CApplicationLaunchShortcut.Invalid_launch_mode_3"); //$NON-NLS-1$
		}

		/**
		 * Method searchAndLaunch.
		 * @param objects
		 * @param mode
		 */
		/*Not used in this version*/
		private void searchAndLaunch(final Object[] elements, String mode) {
			if (elements != null && elements.length > 0) {
				IBinary bin = null;
				if (elements.length == 1 && elements[0] instanceof IBinary) {
					bin = (IBinary)elements[0];
				} else {
					final List results = new ArrayList();
					ProgressMonitorDialog dialog = new ProgressMonitorDialog(getShell());
					IRunnableWithProgress runnable = new IRunnableWithProgress() {
						public void run(IProgressMonitor pm) throws InterruptedException {
							int nElements = elements.length;
							pm.beginTask("Looking for executables", nElements); //$NON-NLS-1$
							try {
								IProgressMonitor sub = new SubProgressMonitor(pm, 1);
								for (int i = 0; i < nElements; i++) {
									if (elements[i] instanceof IAdaptable) {
										IResource r = (IResource) ((IAdaptable) elements[i]).getAdapter(IResource.class);
										if (r != null) {
											ICProject cproject = CoreModel.getDefault().create(r.getProject());
											if (cproject != null) {
												try {
													IBinary[] bins = cproject.getBinaryContainer().getBinaries();

													for (int j = 0; j < bins.length; j++) {
														if (bins[j].isExecutable()) {
															results.add(bins[j]);
														}
													}
												} catch (CModelException e) {
												}
											}
										}
									}
									if (pm.isCanceled()) {
										throw new InterruptedException();
									}
									sub.done();
								}
							} finally {
								pm.done();
							}
						}
					};
					try {
						dialog.run(true, true, runnable);
					} catch (InterruptedException e) {
						return;
					} catch (InvocationTargetException e) {
						MessageDialog.openError(getShell(), LaunchMessages.getString("CApplicationLaunchShortcut.Application_Launcher"), 
								e.getMessage()); //$NON-NLS-1$
						return;
					}
					int count = results.size();
					if (count == 0) {
						MessageDialog.openError(getShell(), LaunchMessages.getString("CApplicationLaunchShortcut.Application_Launcher"), 
								LaunchMessages.getString("CApplicationLaunchShortcut.Launch_failed_no_binaries")); //$NON-NLS-1$ //$NON-NLS-2$
					} else if (count > 1) {
						bin = chooseBinary(results, mode);
					} else {
						bin = (IBinary)results.get(0);
					}
				}
				if (bin != null) {
					launch(bin, mode);
				}
			} else {
				MessageDialog.openError(getShell(), LaunchMessages.getString("CApplicationLaunchShortcut.Application_Launcher"), 
						LaunchMessages.getString("CApplicationLaunchShortcut.Launch_failed_no_project_selected")); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
	}
