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
 *    Wyatt Spear - current implementation
 *    Chris Navarro - JAXB custom widget implementation
 *    
 * Modified from ListSelectionDialog
 * Contributors:
 *     IBM Corporation - initial API and implementation
 * 	   Sebastian Davids <sdavids@gmx.de> - Fix for bug 90273 - [Dialogs] 
 * 			ListSelectionDialog dialog alignment
 ****************************************************************************/
package org.eclipse.ptp.etfw.tau.ui;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.ptp.etfw.IBuildLaunchUtils;
import org.eclipse.ptp.etfw.tau.papiselect.papic.EventTreeDialog;
import org.eclipse.ptp.etfw.tau.ui.messages.Messages;
import org.eclipse.ptp.internal.etfw.BuildLaunchUtils;
import org.eclipse.ptp.internal.etfw.RemoteBuildLaunchUtils;
import org.eclipse.ptp.internal.rm.jaxb.core.JAXBCoreConstants;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.rm.jaxb.control.ui.AbstractWidget;
import org.eclipse.ptp.rm.jaxb.control.ui.IWidgetDescriptor;
import org.eclipse.ptp.rm.jaxb.core.IVariableMap;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

/**
 * This class takes the primary function implemented by Wyatt Spear and makes it available for the TAU JAXB tool.
 * 
 * @see PapiListSelectionDialog
 * 
 * @author Chris Navarro
 * 
 */
public class PapiOptionDialog extends AbstractWidget {

	private static final String BIN = "bin"; //$NON-NLS-1$
	private static final String PAPI = "papi"; //$NON-NLS-1$
	private static final String PAPI_EVENT_CHOOSER = "papi_event_chooser"; //$NON-NLS-1$
	private static final String PAPIDIR = "PAPIDIR="; //$NON-NLS-1$
	private static final String UTILS = "utils"; //$NON-NLS-1$
	private static final String SHARE = "share"; //$NON-NLS-1$
	private static final String PAPI_XML_BIN = "papi_xml_event_info"; //$NON-NLS-1$
	private final IRemoteConnection remoteConnection;
	private final Button button;
	private final IBuildLaunchUtils blt;
	boolean refreshing = false;
	protected Map<String, String> varmap = new HashMap<String,String>();

	/**
	 * The list of all available options found among all available TAU makefiles
	 */
	protected LinkedHashSet<String> allopts = null;

	/**
	 * The list of all selected makefile options
	 */
	protected LinkedHashSet<String> selopts = null;
	/**
	 * The path to the TAU lib directory
	 */
	private IFileStore taulib = null;
	private String selection;
	private IVariableMap map;

	public PapiOptionDialog(Composite parent, IWidgetDescriptor wd) {
		super(parent, wd);

		this.remoteConnection = wd.getRemoteConnection();
		
		if(remoteConnection!=null){
			blt = new RemoteBuildLaunchUtils(remoteConnection);
		}
		else{
			blt = new BuildLaunchUtils();
		}
		setLayout(new GridLayout(1, false));
		setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));

		this.button = new Button(this, SWT.PUSH | SWT.TOP);
		button.setText(Messages.PapiOptionDialog_SelectPapiCounters);
		button.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				handlePapiSelect();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});

		findTauDirectory();
	}

	/**
	 * Collects the list of TAU makefiles available at the specified TAU installation (asking the user to specify one if necessary)
	 * Adds the list of all available makefiles to allmakefiles and all available makefiles options to allopts
	 * 
	 */
	private void findTauDirectory() {
		String binpath = blt.getToolPath("tau"); //$NON-NLS-1$
		IFileStore bindir = null;
		if (binpath == null || binpath.length() == 0) {
			binpath = blt.checkToolEnvPath("pprof"); //$NON-NLS-1$
			if (binpath != null && binpath.length() > 0) {
				bindir = blt.getFile(binpath);
			}
		} else {
			bindir = blt.getFile(binpath);
		}

		if (bindir == null || !bindir.fetchInfo().exists()) {
			return;
		}

		taulib = bindir.getParent().getChild("lib"); //$NON-NLS-1$
	}

	@Override
	public void setEnabled(boolean enabled) {
		button.setEnabled(enabled);
	}

	public Button getButton() {
		return button;
	}

	/**
	 * Handles launching of the PAPI counter selection dialog. Places values returned by the dialog in the launch environment
	 * variables list
	 * 
	 */

	protected void handlePapiSelect() {
		Object[] selected = null;
		try {

			IFileStore pdir = getPapiLoc();

			if (pdir == null || !pdir.fetchInfo().exists() || !pdir.fetchInfo().isDirectory()) {
				return;
			}
			IFileStore pcxi = pdir.getChild(PAPI_XML_BIN);// new File(papiBin+File.separator+"papi_xml_event_info");

			if (pcxi.fetchInfo().exists())// papiCountRadios[2].getSelection())
			{
				EventTreeDialog treeD = new EventTreeDialog(getShell(), pdir, blt);

				if (treeD.open() == Window.OK) {
					selected = treeD.getCommands().toArray();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		if ((selected != null) && (selected.length > 0)) {
			LinkedHashSet<Object> selset = new LinkedHashSet<Object>(Arrays.asList(selected));

			String pn = "PAPI_NATIVE_"; //$NON-NLS-1$
			String pPre = "PAPI_"; //$NON-NLS-1$
			varmap = new HashMap<String, String>(selset.size());
			varmap.put("COUNTER1", "GET_TIME_OF_DAY"); //$NON-NLS-1$ //$NON-NLS-2$
			//String agg = "time";
			Iterator<Object> varit = selset.iterator();
			int counter = 2;
			while (varit.hasNext()) {
				String varTxt = (String) varit.next();
				if (varTxt.indexOf(pPre) != 0) {
					varTxt = pn + varTxt;
				}
				varmap.put("COUNTER" + counter, varTxt); //$NON-NLS-1$
				//agg+=":"+varTxt;
				counter++;
			}

		} else {
			varmap = null;
		}
	}

	/**
	 * Finds the PAPI utilities' location
	 * 
	 * @return The string representation of the location of the PAPI utilities located in the selected makefile, or the empty string
	 *         if they are not found
	 * @throws FileNotFoundException
	 *             if the location is in the makefile but not valid
	 */
	private IFileStore getPapiLoc() throws FileNotFoundException {

		if (taulib == null) {
			return null;
		}

		if (map.get(ITauConstants.TAU_MAKEFILE_TAB_ID) != null) {
			String selItem = map.get(ITauConstants.TAU_MAKEFILE_TAB_ID).getValue().toString();
			this.setSelection(selItem);
		}

		String selItem = this.selection;
		String[] selections = selItem.split(JAXBCoreConstants.REMOTE_PATH_SEP);
		selItem = selections[selections.length - 1];

		IFileStore papimakefile = blt.getFile(taulib.toURI().getPath());
		papimakefile = papimakefile.getChild(selItem);

		// TODO If this is invalid, we should throw a warning and stop here
		if (!papimakefile.fetchInfo().exists()) {
			System.out.println(Messages.PapiOptionDialog_InvalidPapiMakefile);
		}

		String papiline = ""; //$NON-NLS-1$
		boolean found = false;
		try {
			BufferedReader readmake = new BufferedReader(new InputStreamReader(papimakefile.openInputStream(EFS.NONE, null)));
			papiline = readmake.readLine();
			while (papiline != null) {
				if (papiline.indexOf(PAPIDIR) == 0) {
					found = true;
					break;
				}
				papiline = readmake.readLine();
			}
			readmake.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (CoreException e) {
			e.printStackTrace();
		}
		IFileStore papibin = null;
		if (found && papiline != null) {
			papiline = papiline.substring(papiline.indexOf(JAXBCoreConstants.EQ) + 1);
			IFileStore papihome = blt.getFile(papiline);

			papibin = papihome.getChild(BIN).getChild(PAPI_EVENT_CHOOSER);
			if (!papibin.fetchInfo().exists()) {
				papibin = papihome.getChild(SHARE).getChild(PAPI).getChild(UTILS).getChild(PAPI_EVENT_CHOOSER);
			}

			if (!papibin.fetchInfo().exists()) {
				throw new FileNotFoundException(Messages.PapiOptionDialog_PapiUtilsNotFound);
			}

		} else {
			MessageDialog.openError(this.getShell(), Messages.PapiOptionDialog_PapiDirNotFound,
					Messages.PapiOptionDialog_NoPapiDirInMakefile);
		}

		if (papibin != null) {
			return papibin.getParent();
		} else {
			return null;
		}
	}

	public void setSelection(String selection) {
		this.selection = selection;
	}

	public void setVariableMap(IVariableMap lcMap) {
		this.map = lcMap;
	}
	
	public Map<String, String> getVariableMap(){
		return varmap;
	}
}
