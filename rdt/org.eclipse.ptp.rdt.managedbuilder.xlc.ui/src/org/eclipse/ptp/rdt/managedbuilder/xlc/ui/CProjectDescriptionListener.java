/*******************************************************************************
 * Copyright (c) 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.rdt.managedbuilder.xlc.ui;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.CIncludePathEntry;
import org.eclipse.cdt.core.settings.model.CMacroEntry;
import org.eclipse.cdt.core.settings.model.CProjectDescriptionEvent;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSetting;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescriptionListener;
import org.eclipse.cdt.core.settings.model.ICProjectDescriptionManager;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IInputType;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.internal.core.ManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.internal.core.ManagedProject;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ptp.rdt.core.activator.Activator;
import org.eclipse.ptp.rdt.managedbuilder.xlc.ui.messages.Messages;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.internal.Workbench;

/**
 * @author vkong
 * @since 3.4
 * 
 */
public class CProjectDescriptionListener implements ICProjectDescriptionListener {

	private static CProjectDescriptionListener instance = new CProjectDescriptionListener();
	protected boolean fOldInputType = false;

	protected CProjectDescriptionListener() {
	}

	public static void startListening() {
		CCorePlugin.getDefault().getProjectDescriptionManager().addCProjectDescriptionListener(instance, CProjectDescriptionEvent.LOADED);
	}

	public static void stopListening() {
		CCorePlugin.getDefault().getProjectDescriptionManager().removeCProjectDescriptionListener(instance);
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.cdt.core.settings.model.ICProjectDescriptionListener#handleEvent
	 * (org.eclipse.cdt.core.settings.model.CProjectDescriptionEvent)
	 */
	public void handleEvent(final CProjectDescriptionEvent event) {
		ICProjectDescription old= event.getOldCProjectDescription();
		ICProjectDescription act= event.getNewCProjectDescription();
		if (act != null) {
			if (completedProjectCreation(old, act)) {
				final IProject project = event.getProject();
				final ICProjectDescriptionManager mngr = CoreModel.getDefault().getProjectDescriptionManager();
				
				Job convertToolChainJob = new Job(Messages.getString("CProjectDescriptionListener_jobName")) { //$NON-NLS-1$
					
					ICLanguageSettingEntry[] fCPPIncludePathEntries = null;
					ICLanguageSettingEntry[] fCPPMacroEntries = null;
					
					protected IStatus run(IProgressMonitor monitor) {
						ManagedBuildInfo info = (ManagedBuildInfo) ManagedBuildManager .getBuildInfo(project);
						
						if (info == null)
							return Status.OK_STATUS;
						
						ManagedProject mProj = (ManagedProject) info.getManagedProject();

						ICProjectDescription des = mngr.getProjectDescription(project, true /* writable */);
						ICConfigurationDescription[] configurations = des.getConfigurations();
						// ICConfigurationDescription activeConfiguration = des.getActiveConfiguration();

						if (info != null) {
							IConfiguration config = info.getDefaultConfiguration();
							ICConfigurationDescription configDes = des.getActiveConfiguration();

							IToolChain toolChain = config.getToolChain();
							for (ITool tool : toolChain.getTools()) {
								//find the C++ compiler
								if (tool.getId().indexOf("org.eclipse.ptp.rdt.managedbuild.tool.xlc.cpp.compiler.exe.debug") != -1) { //$NON-NLS-1$
									final ITool cppCompiler = tool;
									for (final IInputType inputType : cppCompiler.getInputTypes()) {

										// search for the obsolete input type
										if (inputType.getId().indexOf("org.eclipse.ptp.rdt.managedbuilder.xlc.ui.cpp.c.compiler.input") != -1) { //$NON-NLS-1$

											Workbench.getInstance().getDisplay().syncExec(new Runnable() {
												
												public void run() {
													
													Shell shell = new Shell();
													String title = Messages.getString("CProjectDescriptionListener_dialogTitle"); //$NON-NLS-1$
													String question = getConversionDialogString();
													boolean continueConversion = MessageDialog.openQuestion(shell, title, question);  
													
													if (continueConversion) {
														//remove it
														cppCompiler.removeInputType(inputType);
														fOldInputType = true;
													}
												}
											
											});
											
											if (fOldInputType) {
												try {
													//save changes
													ManagedBuildManager.saveBuildInfo(project, true);
													mngr.setProjectDescription(project, des, true /* force */, monitor);
												} catch (CoreException e) {
													Activator.log(e);
												}
											}
											break;

										}

									}
								}
							}

							if (fOldInputType) {
								
								// get the include paths and symbols settings from the C++ compiler for C++ source files
								if (configDes.getRootFolderDescription() != null) {
									ICLanguageSetting[] languageSettings = configDes.getRootFolderDescription().getLanguageSettings();
									for (ICLanguageSetting langSetting : configDes.getRootFolderDescription().getLanguageSettings()) {

										if (langSetting.getId().indexOf("org.eclipse.ptp.rdt.managedbuilder.xlc.ui.cpp.compiler.input") != -1) { //$NON-NLS-1$
											fCPPIncludePathEntries = langSetting.getSettingEntries(ICSettingEntry.INCLUDE_PATH);
											fCPPMacroEntries = langSetting.getSettingEntries(ICSettingEntry.MACRO);
											break;
										}
									}

									// update include paths and symbols settings of C source files for the C compiler
									for (ICLanguageSetting langSetting : configDes.getRootFolderDescription().getLanguageSettings()) {
										
										if (langSetting.getId().indexOf("org.eclipse.ptp.rdt.managedbuilder.xlc.ui.c.compiler.input") != -1) { //$NON-NLS-1$
											updateIncludeSettings(langSetting);
											updateMacroSettings(langSetting);

											try {
												//save the changes
												ManagedBuildManager.saveBuildInfo(project, true);
												mngr.setProjectDescription(project, des, true /* force */, monitor);
											} catch (CoreException e) {
												Activator.log(e);
											}

											break;
										}
									}
								}
							}
						}

						return Status.OK_STATUS;
					}
					
					private boolean updateMacroSettings(ICLanguageSetting langSetting) {
						ICLanguageSettingEntry[] entries = langSetting.getSettingEntries(ICSettingEntry.MACRO);
						List<ICLanguageSettingEntry> newEntries = new LinkedList<ICLanguageSettingEntry>();
						for(ICLanguageSettingEntry entry : entries) {
							newEntries.add(entry);
						}
						
						boolean entriesChanged = false;
																	
						// look for settings
						for (ICLanguageSettingEntry cppMacroEntry : fCPPMacroEntries) {
							String symbol = ((CMacroEntry)cppMacroEntry).getName();
							boolean symbolFound = false;
							
							for (ICLanguageSettingEntry entry : entries) {
								if (((CMacroEntry) entry).getName().equals(symbol)) {
									symbolFound = true; // it's already there, so don't set it
									break;
								}
							}
							
							// if we didn't find the symbol, add it
							if(!symbolFound) {
								entriesChanged = true;
								CMacroEntry newEntry = new CMacroEntry(symbol, cppMacroEntry.getValue(), cppMacroEntry.getFlags());
								newEntries.add(newEntry);
							}
						}
							
						// if we changed the entries, then set the new ones
						if(entriesChanged) {
							langSetting.setSettingEntries(ICSettingEntry.MACRO, newEntries.toArray(new ICLanguageSettingEntry[0]));
						}
						
						return entriesChanged;		
					}

					private boolean updateIncludeSettings(ICLanguageSetting langSetting) {
						ICLanguageSettingEntry[] entries = langSetting.getSettingEntries(ICSettingEntry.INCLUDE_PATH);
						List<ICLanguageSettingEntry> newEntries = new LinkedList<ICLanguageSettingEntry>();
						for(ICLanguageSettingEntry entry : entries) {
							newEntries.add(entry);
						}
						
						boolean entriesChanged = false;
																	
						// look for settings					
						for (ICLanguageSettingEntry cppIncludePathEntry : fCPPIncludePathEntries) {
							IPath path = ((CIncludePathEntry)cppIncludePathEntry).getLocation();

							boolean pathFound = false;
							
							for (ICLanguageSettingEntry entry : entries) {
								if (((CIncludePathEntry) entry).getLocation().equals(path)) {
									pathFound = true; // it's already there, so don't set it
									break;
								}
							}
							
							// if we didn't find the path, add it
							if(!pathFound) {
								entriesChanged = true;
								CIncludePathEntry newEntry = new CIncludePathEntry(path, cppIncludePathEntry.getFlags());
								newEntries.add(newEntry);
							}
						}
							
						// if we changed the entries, then set the new ones
						if(entriesChanged) {
							langSetting.setSettingEntries(ICSettingEntry.INCLUDE_PATH, newEntries.toArray(new ICLanguageSettingEntry[0]));
						}
						
						return entriesChanged;
					}
				};
				
				convertToolChainJob.setRule(ResourcesPlugin.getWorkspace().getRoot());
				convertToolChainJob.schedule();
			}
		}
	}

	private boolean completedProjectCreation(ICProjectDescription old, ICProjectDescription act) {
		return (old == null || old.isCdtProjectCreating()) && !act.isCdtProjectCreating();
	}
	
	protected String getConversionDialogString() {
		return Messages.getString("CProjectDescriptionListener_dialogQuestion"); //$NON-NLS-1$
	}
}
