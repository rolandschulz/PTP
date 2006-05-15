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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ILabelProvider;
//import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
//import org.eclipse.swt.graphics.Image;
import org.eclipse.ptp.tau.TauPlugin;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.cdt.launch.internal.ui.LaunchUIPlugin;
import org.eclipse.cdt.managedbuilder.core.*;
//import org.eclipse.cdt.managedbuilder.envvar.IBuildEnvironmentVariable;
//import org.eclipse.cdt.managedbuilder.envvar.IConfigurationEnvironmentVariableSupplier;
import org.eclipse.cdt.core.model.ICProject;
//import org.eclipse.debug.core.ILaunchConfiguration;
//import org.eclipse.debug.ui.DebugUITools;
//import org.eclipse.debug.ui.IDebugModelPresentation;

public class AddTAUConfiguration implements IObjectActionDelegate {

	/**
	 * Constructor for Action1.
	 */
	public AddTAUConfiguration() {
		super();
	}
	/*The selected object*/
	IStructuredSelection selection;
	/**
	 * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {}

	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		try{
		if(selection==null)return;
		//Get the project object from the selection
		ICProject project = (ICProject) selection.getFirstElement();
		if (project == null){
			System.out.println("No project!");
			return;
		}
		//Error out if this is not a managed build project
		if(!ManagedBuildManager.canGetBuildInfo(project.getResource()))
		{	
			Shell shell = new Shell();
			MessageDialog.openError(shell,
					"Taucdt Plug-in",
					"The TAU CDT Plugin only supports managed-make projects at this time");
			return;
		}
		//Make sure it has 'info'
		IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(project.getResource());
		if (info == null||!info.isValid()){
			System.out.println("No info!");
			return;
		}
		//Get the managed project object
		IManagedProject managedProj = info.getManagedProject();
		if (managedProj == null){
			System.out.println("No managed project!");
			return;
		}
		//Make a list of the configurations already within the project
		IConfiguration[] configs = info.getManagedProject().getConfigurations();
		List configlist = Collections.EMPTY_LIST;
		configlist = new ArrayList(configs.length);
		for(int i=0;i<configs.length;i++){
			configlist.add(configs[i]);
		}
		//Ask the user which configuration to copy and modify for TAU
		IConfiguration selectedconf=chooseBuildConfiguration(configlist);
		if(selectedconf==null)
		{System.out.println("No Conf Selected");return;}
		if(selectedconf.getName()==null)
		{System.out.println("Uh Oh!");return;}
		
		//Build the tau compiler run string based on the TAU preferences
		IPreferenceStore pstore = TauPlugin.getDefault().getPreferenceStore();
		String tbpath = pstore.getString("TAUCDTArchPath")+File.separator+"bin";
		String tlpath = pstore.getString("TAUCDTArchPath")+File.separator+"lib";
		
		String tmakepath = pstore.getString("makeCombo");
		if((tmakepath==null)||tmakepath.equals("Specify Makefile Manually")||tmakepath.equals(""))
			tmakepath=pstore.getString("TAUCDTMakefile");
		else
			tmakepath=tlpath+File.separator+tmakepath;
			
		String tauoptchunk="";
		String tauopts = pstore.getString("TAUCDTOpts");
		if(!tauopts.equals("")){
			tauoptchunk=" -tau_options='"+tauopts+"'";
		}
		//Make the new configuration name, and if there is already a configuration with that name, remove it.
		String newname = selectedconf.getName()+("("+tmakepath.substring(tmakepath.lastIndexOf(".")+1)+")");
		IConfiguration[] confs = managedProj.getConfigurations();
		for(int i =0; i<confs.length;i++)
		{
			if(confs[i].getName().equals(newname))
			{
				managedProj.removeConfiguration(confs[i].getId());
			}
		}
		//Make a copy of the selected configuration(Clone works, basic create does not) and rename it.
		IConfiguration newConfig = managedProj.createConfigurationClone(selectedconf, selectedconf.getId()+"."+ManagedBuildManager.getRandomNumber());
		if (newConfig == null){
			System.out.println("No config!");
			return;
		}
		newConfig.setName(newname);
		IToolChain chain = newConfig.getToolChain();
		ITool[] tools = chain.getTools();
		//Replace the compiler/linker commands with the correct tau compiler scripts and arguments.
		for(int i =0;i<tools.length;i++){
			String toolid=tools[i].getId();
			if(toolid.indexOf(".c.")>=0)
			{
				tools[i].setToolCommand(tbpath+File.separator+"tau_cc.sh"+" -tau_makefile="+tmakepath+tauoptchunk);
			}
			if(toolid.indexOf(".cpp.")>=0)
			{
				tools[i].setToolCommand(tbpath+File.separator+"tau_cxx.sh"+" -tau_makefile="+tmakepath+tauoptchunk);
			}
			if(toolid.indexOf(".fortran.")>=0)
			{
				tools[i].setToolCommand(tbpath+File.separator+"tau_f90.sh"+" -tau_makefile="+tmakepath+tauoptchunk);
			}
		}
		ManagedBuildManager.saveBuildInfo(project.getProject(),true);
		ManagedBuildManager.setDefaultConfiguration(project.getProject(),newConfig);
		}catch (Exception e){e.printStackTrace();}
	}
	
	private class IConfLP extends LabelProvider{
		public String getText(Object element) {
			// TODO Auto-generated method stub
			return ((IConfiguration)element).toString();
		}
	}
	//Ask the user to select a build configuration from the selected project.
	protected IConfiguration chooseBuildConfiguration(List configList) {
		ILabelProvider labelProvider=new IConfLP();
		ElementListSelectionDialog dialog = new ElementListSelectionDialog(getShell(), labelProvider);
		dialog.setElements(configList.toArray());
		dialog.setTitle("Build Configuarion Selection");
		dialog.setMessage("Select a build configuration for TAU");
		dialog.setMultipleSelection(false);
		int result = dialog.open();
		labelProvider.dispose();
		if (result == Window.OK) {
			return (IConfiguration) dialog.getFirstResult();
		}
		return null;
	}
	protected Shell getShell() {
		return LaunchUIPlugin.getActiveWorkbenchShell();
	}
	
	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		if (selection instanceof IStructuredSelection)
			this.selection = (IStructuredSelection) selection;
		else
		{	//if the selection is invalid, stop
			this.selection = null;
			System.out.println("Invalid Selection");
		}
	}
}


