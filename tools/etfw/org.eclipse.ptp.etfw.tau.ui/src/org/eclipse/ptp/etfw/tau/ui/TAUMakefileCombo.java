/*******************************************************************************
 * Copyright (c) 2012 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 *   
 * Contributors: 
 * 		Chris Navarro (Illinois/NCSA) - Design and implementation
 *******************************************************************************/
package org.eclipse.ptp.etfw.tau.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ptp.etfw.tau.ui.messages.Messages;
import org.eclipse.ptp.internal.etfw.RemoteBuildLaunchUtils;
import org.eclipse.ptp.internal.rm.jaxb.core.JAXBCoreConstants;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.rm.jaxb.control.ui.AbstractWidget;
import org.eclipse.ptp.rm.jaxb.control.ui.IWidgetDescriptor;
import org.eclipse.ptp.rm.jaxb.core.IVariableMap;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

/**
 * This class is based on the work by Wyatt Spear. It creates a special UI widget for use with the JAXB ETFw workflows and parses
 * the available TAU Makefiles so the user can select only available TAU Makefiles on the target machine.
 * 
 * @see TAUMakefileTab
 * 
 * @author Chris Navarro
 * 
 */
public class TAUMakefileCombo extends AbstractWidget {

	private static final String LIB_FOLDER = "lib"; //$NON-NLS-1$
	private static final String PPROF = "pprof"; //$NON-NLS-1$
	private static final String TAU = "tau"; //$NON-NLS-1$
	private static final String TRUE_SELECTION = "true"; //$NON-NLS-1$
	private static final String MPI_OPTION = "use_mpi"; //$NON-NLS-1$
	private static final String CALLPATH_OPTION = "use_callpath_profiling"; //$NON-NLS-1$
	private static final String OPARI_OPTION = "use_opari"; //$NON-NLS-1$
	private static final String OpenMP_OPTION = "use_openmp"; //$NON-NLS-1$
	private static final String PAPI_OPTION = "use_papi_library"; //$NON-NLS-1$
	private static final String TRACE_OPTION = "use_tau_tracing"; //$NON-NLS-1$
	private static final String PDT_OPTION = "use_tau_with_PDT"; //$NON-NLS-1$

	private final Map<String, String> translateBoolean;
	private final IRemoteConnection remoteConnection;
	private final Combo combo;
	private final RemoteBuildLaunchUtils blt;
	private IVariableMap map;
	private String selectedMakefile;

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
	private LinkedHashSet<String> allmakefiles = null;;
	boolean refreshing = false;

	public TAUMakefileCombo(Composite parent, IWidgetDescriptor wd) {
		super(parent, wd);
		// This translates UI selections to strings for parsing the makefile list
		translateBoolean = new HashMap<String, String>();
		translateBoolean.put(MPI_OPTION, "mpi"); //$NON-NLS-1$
		translateBoolean.put(CALLPATH_OPTION, "callpath"); //$NON-NLS-1$
		translateBoolean.put(OPARI_OPTION, "opari"); //$NON-NLS-1$
		translateBoolean.put(OpenMP_OPTION, "openmp"); //$NON-NLS-1$
		translateBoolean.put(PAPI_OPTION, "papi"); //$NON-NLS-1$
		translateBoolean.put(TRACE_OPTION, "trace"); //$NON-NLS-1$
		translateBoolean.put(PDT_OPTION, "pdt"); //$NON-NLS-1$

		this.remoteConnection = wd.getRemoteConnection();
		blt = new RemoteBuildLaunchUtils(remoteConnection);

		setLayout(new GridLayout(1, false));
		combo = new Combo(this, SWT.READ_ONLY);
		combo.setItems(new String[] { Messages.TAUMakefileCombo_BuildingMakefileList });
		combo.select(0);
		combo.setEnabled(false);

		if (allmakefiles == null) {
			Job job = new Job(Messages.TAUMakefileCombo_UpdatingMakefileList) {
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					refreshing = true;
					initMakefiles();
					Display.getDefault().asyncExec(new Runnable() {
						@Override
						public void run() {
							// TODO handle case where user closes dialog before this finishes, leads to widget disposed error
							updateMakefileCombo();
							if(combo!=null&&!combo.isDisposed())
								combo.getParent().layout();
							refreshing = false;
						}
					});

					return Status.OK_STATUS;
				}
			};
			job.setUser(true);
			job.schedule();
		}
	}

	@Override
	public void setEnabled(boolean enabled) {
		if (allmakefiles == null && enabled && !refreshing) {
			refreshing = true;
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					initMakefiles();
					String[] items = allmakefiles.toArray(new String[0]);
					combo.setItems(items);
					getParent().layout(true);
					refreshing = false;
				}
			});

		}

		if (!refreshing) {
			updateMakefileCombo();
		}
	}

	public void setSelectedMakefile(String makefile){
		this.selectedMakefile=makefile;
	}
	public String getSelectedMakefile(){
		return selectedMakefile;
	}
	
	private void updateMakefileCombo() {
		if(combo==null||combo.isDisposed()){
			return;
		}
		List<String> options = populateOptions();
		int preDex=-1;
		List<String> makefiles = new ArrayList<String>();
		makefiles.add(JAXBCoreConstants.ZEROSTR);
		int i = 1;
		for (String name : allmakefiles) {
			int optionTypes = 0;
			for (String option : options) {
				if (name.contains(option)) {
					optionTypes++;
				}
			}

			if (optionTypes == options.size()) {
				makefiles.add(name);
				if(selectedMakefile!=null&&selectedMakefile.endsWith(name)){
					preDex=i;
				}
				i++;
			}
		}

		String[] items = makefiles.toArray(new String[0]);
		combo.setItems(items);
		combo.setEnabled(true);
		if (items.length > 1) {
			if(preDex>0)
				combo.select(preDex);
			else
				combo.select(1);
			combo.notifyListeners(SWT.Selection, null);
		}
		getParent().layout(true);
	}

	private List<String> populateOptions() {
		List<String> options = new ArrayList<String>();

		addOption(MPI_OPTION, options);
		addOption(CALLPATH_OPTION, options);
		addOption(OPARI_OPTION, options);
		addOption(OpenMP_OPTION, options);
		addOption(PAPI_OPTION, options);
		addOption(TRACE_OPTION, options);
		addOption(PDT_OPTION, options);

		return options;
	}

	private void addOption(String variable, List<String> options) {
		if (map != null) {
			// Find the selected options and add them so we can filter the makefile selection
			if (map.get(variable) != null) {
				String option = map.get(variable).getValue().toString();
				if (option.equals(TRUE_SELECTION)) {
					options.add(translateBoolean.get(variable));
				}
			}
		}
	}

	/**
	 * Collects the list of TAU makefiles available at the specified TAU installation (asking the user to specify one if necessary)
	 * Adds the list of all available makefiles to allmakefiles and all available makefiles options to allopts
	 * 
	 */
	private void initMakefiles() {
		allmakefiles = new LinkedHashSet<String>();
		String binpath = blt.getToolPath(TAU);
		IFileStore bindir = null;
		if (binpath == null || binpath.length() == 0) {
			binpath = blt.checkToolEnvPath(PPROF);
			if (binpath != null && binpath.length() > 0) {
				bindir = blt.getFile(binpath);
			}
		} else {
			bindir = blt.getFile(binpath);
		}

		List<IFileStore> mfiles = testTAUEnv(bindir);

		allopts = new LinkedHashSet<String>();
		String name = null;
		if (mfiles == null) {
			return;
		}
		for (int i = 0; i < mfiles.size(); i++) {
			name = mfiles.get(i).getName();
			allmakefiles.add(name);
			allopts.addAll(Arrays.asList(name.split(JAXBCoreConstants.HYPH)));
		}
		allopts.remove(ITauConstants.TAU_MAKEFILE_PREFIX);
	}

	/**
	 * Given a directory (presumably a tau arch directory) this looks in the lib subdirectory and returns a list of all
	 * Makefile.tau... files with -pdt
	 * */
	private List<IFileStore> testTAUEnv(IFileStore bindir) {
		if (bindir == null || !bindir.fetchInfo().exists()) {
			return null;
		}

		taulib = bindir.getParent().getChild(LIB_FOLDER);
		IFileStore[] mfiles = null;
		ArrayList<IFileStore> tmfiles = null;
		if (taulib.fetchInfo().exists()) {
			try {
				mfiles = taulib.childStores(EFS.NONE, null);
				tmfiles = new ArrayList<IFileStore>();
				for (int i = 0; i < mfiles.length; i++) {
					IFileInfo finf = mfiles[i].fetchInfo();
					if (!finf.isDirectory() && finf.getName().startsWith(ITauConstants.TAU_MAKEFILE_PREFIX)) {
						tmfiles.add(mfiles[i]);
					}
				}
			} catch (CoreException e) {
				e.printStackTrace();
			}

		}

		return tmfiles;
	}

	public Combo getCombo() {
		return this.combo;
	}

	public String getSelection() {
		String selection = this.combo.getItem(combo.getSelectionIndex());
		String makefilePath = taulib.toURI().getPath() + JAXBCoreConstants.REMOTE_PATH_SEP + selection;
		return makefilePath;
	}

	public void setVariableMap(IVariableMap map) {
		this.map = map;
	}
}
