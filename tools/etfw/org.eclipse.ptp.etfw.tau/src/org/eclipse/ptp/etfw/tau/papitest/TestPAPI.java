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
package org.eclipse.ptp.etfw.tau.papitest;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.ptp.etfw.IBuildLaunchUtils;
import org.eclipse.ptp.etfw.tau.Activator;
import org.eclipse.ptp.etfw.tau.messages.Messages;
import org.eclipse.ptp.etfw.tau.papiselect.PapiListSelectionDialog;
import org.eclipse.ptp.etfw.tau.papiselect.papic.EventTreeDialog;
import org.eclipse.ptp.internal.etfw.BuildLaunchUtils;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.internal.Workbench;

/**
 * Our sample action implements workbench action delegate. The action proxy will
 * be created by the workbench and shown in the UI. When the user tries to use
 * the action, this delegate will be created and execution will be delegated to
 * it.
 * 
 * @see IWorkbenchWindowActionDelegate
 */
@SuppressWarnings("restriction")
public class TestPAPI {
	private IWorkbenchWindow window;

	protected static final String papiLocationSelectionVar = "ID.of.PAPI.bin.directory.location"; //$NON-NLS-1$

	protected static final String papiCounterTypeVar = "ID.of.PAPI.counter.type.selected"; //$NON-NLS-1$

	/**
	 * The constructor.
	 */
	public TestPAPI() {
		window = Workbench.getInstance().getActiveWorkbenchWindow();
	}

	/**
	 * We can use this method to dispose of any system resources we previously
	 * allocated.
	 * 
	 * @see IWorkbenchWindowActionDelegate#dispose
	 */
	public void dispose() {
	}

	/**
	 * We will cache window object in order to be able to provide parent shell
	 * for the message dialog.
	 * 
	 * @see IWorkbenchWindowActionDelegate#init
	 */
	public void init(IWorkbenchWindow window) {
		this.window = window;
	}

	/**
	 * The action has been activated. The argument of the method represents the
	 * 'real' action sitting in the workbench UI.
	 * 
	 * @see IWorkbenchWindowActionDelegate#run
	 */
	public void run() {
		// try {
		final LabelProvider papilab = new LabelProvider();
		final ArrayContentProvider paprov = new ArrayContentProvider();

		final PAPISplash splash = new PAPISplash(window.getShell());
		splash.open();

		final int papiCountType = Activator.getDefault().getPreferenceStore().getInt(papiCounterTypeVar);
		final String papiLoc = Activator.getDefault().getPreferenceStore().getString(papiLocationSelectionVar);

		// System.out.println(papiLoc+" "+papiCountType);
		final IBuildLaunchUtils blt = new BuildLaunchUtils();
		final IFileStore pdir = blt.getFile(papiLoc);// new File(papiLoc);
		if (!pdir.fetchInfo().exists() || !pdir.fetchInfo().isDirectory()) {// || !pdir..canRead()) {
			return;
		}
		final IFileStore pcxi = pdir.getChild("papi_xml_event_info");// new File(papiLoc + File.separator + "papi_xml_event_info"); //$NON-NLS-1$

		if (pcxi.fetchInfo().exists())// papiCountType==2)
		{
			final EventTreeDialog treeD = new EventTreeDialog(window.getShell(), pdir, blt);

			if (treeD.open() == Window.OK) {
				showCounters(treeD.getCommands().toArray());
			}
			return;
		}

		final PapiListSelectionDialog papidialog = new PapiListSelectionDialog(window.getShell(), pdir, blt, paprov, papilab,
				Messages.TestPAPI_SelectPapiCounters, papiCountType);
		papidialog.setTitle(Messages.TestPAPI_PapiCounters);
		papidialog.setHelpAvailable(false);

		/*
		 * The disabled code here shows how to initialize the selector with
		 * previously selected counters
		 */
		// if ((varmap != null) && (varmap.size() > 0)) {
		// papidialog.setInitialSelections(varmap.values().toArray());
		// }

		if (papidialog.open() == Window.OK) {
			final Object[] selected = papidialog.getResult();
			showCounters(selected);

		}
		// } catch (Exception e) {
		// e.printStackTrace();
		// }

	}

	/**
	 * Selection in the workbench has been changed. We can change the state of
	 * the 'real' action here if we want, but this can only happen after the
	 * delegate has been created.
	 * 
	 * @see IWorkbenchWindowActionDelegate#selectionChanged
	 */
	public void selectionChanged(IAction action, ISelection selection) {
	}

	private void showCounters(Object[] selected) {
		if ((selected != null) && (selected.length > 0)) {
			String counters = ""; //$NON-NLS-1$
			for (final Object element : selected) {
				counters += element + "\n"; //$NON-NLS-1$
			}

			MessageDialog.openInformation(window.getShell(), Messages.TestPAPI_SelectedPapiCounters, counters);

			// LinkedHashSet selset = new LinkedHashSet(Arrays
			// .asList(selected));
			//
			// varmap = new HashMap(selset.size());
			// varmap.put("COUNTER1", "GET_TIME_OF_DAY");
			// Iterator varit = selset.iterator();
			// int counter = 2;
			// while (varit.hasNext()) {
			// varmap.put("COUNTER" + counter, varit.next());
			// counter++;
			// }
			//
			// } else {
			// varmap = null;
		}
	}
}