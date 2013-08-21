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
package org.eclipse.ptp.etfw.tau;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.ptp.core.util.LaunchUtils;
import org.eclipse.ptp.etfw.IBuildLaunchUtils;
import org.eclipse.ptp.etfw.IToolLaunchConfigurationConstants;
import org.eclipse.ptp.etfw.tau.ITAULaunchConfigurationConstants;
import org.eclipse.ptp.etfw.tau.LaunchImages;
import org.eclipse.ptp.etfw.tau.messages.Messages;
import org.eclipse.ptp.etfw.tau.papiselect.PapiListSelectionDialog;
import org.eclipse.ptp.etfw.tau.papiselect.papic.EventTreeDialog;
import org.eclipse.ptp.etfw.tau.perfdmf.PerfDMFUIPlugin;
import org.eclipse.ptp.etfw.tau.perfdmf.views.PerfDMFView;
import org.eclipse.ptp.etfw.toolopts.ToolPane;
import org.eclipse.ptp.etfw.toolopts.ToolPaneListener;
import org.eclipse.ptp.etfw.toolopts.ToolsOptionsConstants;
import org.eclipse.ptp.etfw.ui.AbstractToolConfigurationTab;
import org.eclipse.ptp.internal.etfw.BuildLaunchUtils;
import org.eclipse.ptp.internal.etfw.RemoteBuildLaunchUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;

/**
 * Defines the tab of the performance-analysis launch configuration system where performance-analysis options are selected
 * 
 * @author wspear
 * 
 */
public class TAUAnalysisTab extends AbstractToolConfigurationTab {

	/**
	 * Encapsulates individual options for TAU makefile selection
	 * 
	 * @author wspear
	 * 
	 */
	protected class CheckItem {

		CheckItem() {
		}

		CheckItem(String cmd, String bText, String tText, String cString, boolean def) {
			defState = def;
			makeCmd = cmd;
			buttonText = bText;
			toolText = tText;
			confString = cString;
		}

		/**
		 * The check-button for this CheckItem
		 */
		protected Button unitCheck;

		/**
		 * The literal substring of the makefile's name associated with this CheckItem
		 */
		protected String makeCmd;

		/**
		 * The label on this CheckItem's checkbox
		 */
		protected String buttonText;

		/**
		 * The informational pop-up text about this selection
		 */
		protected String toolText;

		/**
		 * The name given in the configuration command for this option
		 */
		protected String confString;

		/**
		 * The default state of this CheckItem
		 */
		protected boolean defState;

	}

	/**
	 * Determines if the launch configuration associated with this tab has access to the PTP
	 */
	protected boolean noPTP = false;

	/**
	 * Sets weather or not it is possible to initiate a parallel launch from this tab
	 * 
	 * @param noPar
	 *            Availability of the PTP to this tab's launch configuration delegate
	 */
	public TAUAnalysisTab(boolean noPar) {
		noPTP = noPar;
	}

	/**
	 * Listens for action in the TAU build-options pane
	 * 
	 * @author wspear
	 * 
	 */
	protected class TauPaneListener extends ToolPaneListener {
		TauPaneListener(ToolPane tool) {
			super(tool);
		}

		@Override
		protected void localAction() {
			updateLaunchConfigurationDialog();
		}
	}

	/**
	 * Defines/contins the CheckItems available for narrowing the selection of TAU makefiles
	 */
	protected CheckItem checks[] = { new CheckItem("mpi", "MPI", "", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			ITAULaunchConfigurationConstants.MPI, true), new CheckItem("callpath", "Callpath Profiling", "", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			ITAULaunchConfigurationConstants.CALLPATH, false), new CheckItem("phase", "Phase Based Profiling", "", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			ITAULaunchConfigurationConstants.PHASE, false), new CheckItem("memory", "Memory Profiling", "", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			ITAULaunchConfigurationConstants.MEMORY, false), new CheckItem("opari", "OPARI", "", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			ITAULaunchConfigurationConstants.OPARI, false), new CheckItem("openmp", "OpenMP", "", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			ITAULaunchConfigurationConstants.OPENMP, false), new CheckItem("epilog", "Epilog", "", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			ITAULaunchConfigurationConstants.EPILOG, false), new CheckItem("vampirtrace", "VampirTrace", "", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			ITAULaunchConfigurationConstants.VAMPIRTRACE, false), new CheckItem("papi", "PAPI", "", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			ITAULaunchConfigurationConstants.PAPI, false),
			// Papi is entry 8 (needed for papi composite/MULTI)
			new CheckItem("perf", "Perflib", "", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					ITAULaunchConfigurationConstants.PERF, false), new CheckItem("trace", Messages.TAUAnalysisTab_31, "", //$NON-NLS-1$ //$NON-NLS-3$
					ITAULaunchConfigurationConstants.TRACE, false),
			new CheckItem("pdt", "PDT", "", ITAULaunchConfigurationConstants.PDT, false) }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

	/**
	 * The index of the mpi button in CheckItem list. Used to enable/disable tauinc.sh box
	 * 
	 * @author raportil
	 */
	protected int mpiIndex = 0;
	protected int callpathIndex = 1;
	protected Button runTauinc;

	/**
	 * The index of the papi button in CheckItem list. This requires special treatment
	 */
	protected int papiIndex = 8;
	protected int pdtIndex = 11;

	protected Button papiSelect;

	protected Button papiCountRadios[];

	protected Button pdtRadios[];

	protected Composite papiComp;

	protected Composite pdtComp;

	protected Composite mpiComp;

	protected Composite selComp;

	protected Label selectLabel;

	protected Button selectRadios[];

	protected Text compiler;

	// protected Button buildonlyCheck;

	// protected Button noParallelRun;

	protected Button nocleanCheck;

	protected Button keepprofsCheck;

	protected Button profSummaryCheck;

	protected Button portalCheck;

	protected Text tauSelectFile = null;

	protected Button browseSelfileButton = null;

	/**
	 * The list of all available makefiles
	 */
	protected LinkedHashSet<String> allmakefiles = null;

	/**
	 * The list of all selected makefiles
	 */
	protected LinkedHashSet<String> selmakefiles = null;

	/**
	 * The list of all available options found among all available TAU makefiles
	 */
	protected LinkedHashSet<String> allopts = null;

	/**
	 * The list of all selected makefile options
	 */
	protected LinkedHashSet<String> selopts = null;

	protected Combo makecombo = null;

	protected Combo dbCombo = null;

	/**
	 * The name of the selected makefile
	 */
	protected String selmakefile = null;

	/**
	 * The path to the TAU lib directory
	 */
	private IFileStore taulib = null;

	protected Map<String, String> archvarmap = null;

	protected Map<String, String> varmap = null;

	// TODO: This isn't generic. We need to get this pane explicitly Fixing: Moving individual panes into standard UI
	//protected final ToolPane tauOpts = Activator.getTool("TAU").getFirstBuilder(null).getGlobalCompiler().toolPanes[0];// toolPanes[0];//ToolMaker.makeTools(tauToolXML)[0].toolPanes[0]; //$NON-NLS-1$
	//protected final ToolPane tauEnv = Activator.getTool("TAU").getNthRunner(null, 2).global.toolPanes[0]; //$NON-NLS-1$

	// protected ToolPane custOpts=null;

	// private static File tauToolXML= null;
	// /**
	// * Initialize the file that defines the TAU compilation options pane
	// */
	// static{
	// try {

	// URL testURL=Activator.getDefault().getBundle().getEntry("toolxml"+File.separator+"tau_tool.xml");
	// tauToolXML = new File(new URI(FileLocator.toFileURL(testURL).toString().replaceAll(" ", "%20")));

	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// }
	private final static String UNIX_SLASH = "/"; //$NON-NLS-1$

	private static final String selFileValConf = "Tau Compiler.performance.options.configuration_id_-OPTTAUSELECTFILE_ARGUMENT_SAVED";
	private static final String selFileButtonConf = "Tau Compiler.performance.options.configuration_id_-OPTTAUSELECTFILE_BUTTON_STATE";
	String selectFieldVal = null;
	boolean selectFieldChecked = false;

	/**
	 * Listen for activity in the TAU makefile combo-box, CheckItem widgets or other options
	 * 
	 * @author wspear
	 * 
	 */
	protected class WidgetListener extends SelectionAdapter implements ModifyListener, IPropertyChangeListener {
		@Override
		public void widgetSelected(SelectionEvent e) {

			Object source = e.getSource();
			/*
			 * Reinitialize the selected makefile
			 */
			if (source == makecombo) {
				selmakefile = makecombo.getItem(makecombo.getSelectionIndex());
				updateComboDerivedOptions(selmakefile);
				updateLaunchConfigurationDialog();
			}
			// TODO: Find a way to make this work with independently defined panes
			// else if (source == pdtRadios[0] || source == pdtRadios[1]) {
			// if (pdtRadios[0].getSelection()) {
			// pdtOpt.setSelected(true);
			// compOpt.setSelected(false);
			// } else {
			// pdtOpt.setSelected(false);
			// compOpt.setSelected(true);
			// }
			// tauOpts.OptUpdate();
			// tauEnv.OptUpdate();
			// }
			// else
			// if(source==buildonlyCheck){
			// updateLaunchConfigurationDialog();
			// }
			// else
			// if(source==noParallelRun){
			// if(noParallelRun.getSelection()&&selmakefile.indexOf("-mpi")>0){
			// buildonlyCheck.setSelection(true);
			// }
			// }
			else if (source == browseSelfileButton) {
				handleSelfileBrowseButtonSelected();
			} else if (source == papiSelect) {
				handlePapiSelect();
			}

			/*
			 * else if (source.equals(selectRadios[0]) || source.equals(selectRadios[3])) {
			 * if (selectRadios[0].getSelection() || selectRadios[3].getSelection()) {
			 * selectOpt.setSelected(false);
			 * selectOpt.setArg(""); //$NON-NLS-1$
			 * selectOpt.setEnabled(false);
			 * }
			 * } else if (source.equals(selectRadios[1])) {
			 * if (selectRadios[1].getSelection()) {
			 * selectOpt.setSelected(true);
			 * selectOpt.setArg(ToolsOptionsConstants.PROJECT_ROOT + UNIX_SLASH + "tau.selective"); //$NON-NLS-1$
			 * selectOpt.setEnabled(false);
			 * }
			 * } else if (source.equals(selectRadios[2])) {
			 * if (!selectRadios[2].getSelection()) {
			 * selComp.setEnabled(false);
			 * tauSelectFile.setEnabled(false);
			 * tauSelectFile.setEnabled(false);
			 * } else {
			 * selComp.setEnabled(true);
			 * tauSelectFile.setEnabled(true);
			 * tauSelectFile.setEnabled(true);
			 * selectOpt.setSelected(true);
			 * selectOpt.setArg(tauSelectFile.getText());
			 * }
			 * selectOpt.setEnabled(false);
			 * }
			 */

			else if (source.equals(selectRadios[0]) || source.equals(selectRadios[3])) {
				if (selectRadios[0].getSelection() || selectRadios[3].getSelection()) {
					// selectOpt.setSelected(false);
					selectFieldChecked = false;
					//selectOpt.setArg(""); //$NON-NLS-1$
					// selectOpt.setEnabled(false);
				}
			} else if (source.equals(selectRadios[1])) {
				if (selectRadios[1].getSelection()) {
					// selectOpt.setSelected(true);
					//selectOpt.setArg(ToolsOptionsConstants.PROJECT_ROOT + UNIX_SLASH + "tau.selective"); //$NON-NLS-1$
					selectFieldChecked = true;
					selectFieldVal = ToolsOptionsConstants.PROJECT_ROOT + UNIX_SLASH + "tau.selective";
					// selectOpt.setEnabled(false);
				}
			} else if (source.equals(selectRadios[2])) {
				if (!selectRadios[2].getSelection()) {
					selComp.setEnabled(false);
					tauSelectFile.setEnabled(false);
					tauSelectFile.setEnabled(false);
				} else {
					selComp.setEnabled(true);
					tauSelectFile.setEnabled(true);
					tauSelectFile.setEnabled(true);
					// selectOpt.setSelected(true);
					// selectOpt.setArg(tauSelectFile.getText());
					selectFieldChecked = true;
					selectFieldVal = tauSelectFile.getText();
				}
				// selectOpt.setEnabled(false);
			}

			/*
			 * If not one of the above options, then one of the makefile selection options has been trippedIterate through until we
			 * find which one, then check or uncheck it as necessary and reinitializethe combo box and the remaining available
			 * checkboxes
			 */
			else {
				for (CheckItem check : checks) {
					if (source == check.unitCheck) {
						if (((Button) source).getSelection()) {
							selopts.add(check.makeCmd);
						} else {
							selopts.remove(check.makeCmd);
						}
						initMakeCombo();
						reinitMakeChecks();
					}
				}
			}

			updateLaunchConfigurationDialog();
		}

		public void propertyChange(PropertyChangeEvent event) {
			updateLaunchConfigurationDialog();
		}

		public void modifyText(ModifyEvent evt) {
			Object source = evt.getSource();
			if (source == tauSelectFile) {
				if (selectRadios[2].getSelection()) {
					// selectOpt.setArg(tauSelectFile.getText());
					selectFieldVal = tauSelectFile.getText();
				}
			}
			updateLaunchConfigurationDialog();
		}
	}

	protected WidgetListener listener = new WidgetListener();
	private IBuildLaunchUtils blt;

	/*
	 * Plugins of this nature require a default constructor
	 */
	public TAUAnalysisTab() {
		// getLaunchConfiguration();
		// blt = new BuildLaunchUtils();
	}

	/**
	 * Disables exactly the TAU makefile selection checkboxes that are presently excluded by the selected TAU makefile selection
	 * checkboxes For example, if option A is selected, only options found in makefiles that include option A will remain enabled.
	 * 
	 */
	private void reinitMakeChecks() {

		LinkedHashSet<String> goodopts = new LinkedHashSet<String>(allopts.size());
		String holdmake = null;
		String check = null;

		if ((selopts == null) || (selopts.size() == 0)) {
			goodopts.addAll(allopts);
		} else {

			Iterator<String> makes = allmakefiles.iterator();
			boolean allgood = true;
			while (makes.hasNext()) {
				holdmake = makes.next();

				Iterator<String> opts = selopts.iterator();

				while (opts.hasNext()) {
					check = opts.next();

					if (holdmake.indexOf("-" + check) <= 0) { //$NON-NLS-1$
						allgood = false;
						break;
					}
				}
				if (allgood == true) {
					goodopts.addAll(Arrays.asList(holdmake.split("-"))); //$NON-NLS-1$
				} else {
					allgood = true;
				}
			}
		}

		for (int i = 0; i < checks.length; i++) {
			if (!goodopts.contains(checks[i].makeCmd)) {
				checks[i].unitCheck.setEnabled(false);
			} else {
				checks[i].unitCheck.setEnabled(true);
			}
		}
	}

	/**
	 * Initializes the makefile selection checkboxes. If there are no options or makefiles available, disables all makefile options
	 * Otherwise, selectively enables only those makefile options that are available in the available makefiles
	 * 
	 */
	private void initMakeChecks() {
		if ((allmakefiles == null) || (allmakefiles.size() == 0) || (allopts == null) || (allopts.size() == 0)) {
			for (CheckItem check : checks) {
				check.unitCheck.setEnabled(false);
			}
			return;
		}

		for (int i = 0; i < checks.length; i++) {
			if (!allopts.contains(checks[i].makeCmd)) {
				checks[i].unitCheck.setEnabled(false);
			}
		}
	}

	/**
	 * Collects the list of TAU makefiles available at the specified TAU installation (asking the user to specify one if necessary)
	 * Adds the list of all available makefiles to allmakefiles and all available makefiles options to allopts
	 * 
	 */
	private void initMakefiles() {
		// IPreferenceStore pstore = Activator.getDefault().getPreferenceStore();

		String binpath = blt.getToolPath("tau");//pstore.getString(ITAULaunchConfigurationConstants.TAU_ARCH_PATH); //$NON-NLS-1$
		IFileStore bindir = null;
		if (binpath == null || binpath.length() == 0) {
			binpath = blt.checkToolEnvPath("pprof"); //$NON-NLS-1$
			if (binpath != null && binpath.length() > 0) {
				bindir = blt.getFile(binpath);
				// archpath=pprof.getParent().toURI().getPath();
			}
		} else {
			bindir = blt.getFile(binpath);
		}

		List<IFileStore> mfiles = testTAUEnv(bindir);

		/*
		 * if ((mfiles == null) || (mfiles.length == 0)) { String checkArch = BuildLaunchUtils.checkToolEnvPath("tau_merge"); if
		 * (checkArch != null) { checkArch=checkArch.substring(0, checkArch.lastIndexOf(File.separator)); mfiles =
		 * testTAUEnv(checkArch); } else checkArch = archpath; Shell
		 * ourshell=PlatformUI.getWorkbench().getDisplay().getActiveShell(); while ((mfiles == null || mfiles.length == 0) &&
		 * checkArch != null) { checkArch =
		 * BuildLaunchUtils.askToolPath(checkArch,"Select TAU Arch Directory","You must select a valid TAU architecture " +
		 * "directory.  Such a directory should be created " + "when you configure and install TAU.  It must " +
		 * "contain least one valid stub makefile configured " + "with the Program Database Toolkit (pdt)",ourshell); mfiles =
		 * testTAUEnv(checkArch); }
		 * 
		 * if (checkArch == null) { checkArch = "/"; mfiles = testTAUEnv(checkArch); }
		 * 
		 * pstore.setValue(ITAULaunchConfigurationConstants.TAU_ARCH_PATH, checkArch); }
		 */

		allmakefiles = new LinkedHashSet<String>();
		allopts = new LinkedHashSet<String>();
		String name = null;
		if (mfiles == null) {
			return;
		}
		for (int i = 0; i < mfiles.size(); i++) {
			name = mfiles.get(i).getName();
			allmakefiles.add(name);
			allopts.addAll(Arrays.asList(name.split("-"))); //$NON-NLS-1$
		}
		allopts.remove("Makefile.tau"); //$NON-NLS-1$
	}

	private static final String TAU_MAKEFILE_PREFIX = "Makefile.tau"; //$NON-NLS-1$

	/**
	 * Given a directory (presumably a tau arch directory) this looks in the lib subdirectory and returns a list of all
	 * Makefile.tau... files with -pdt
	 * */
	private List<IFileStore> testTAUEnv(IFileStore bindir) {

		// class makefilter implements FilenameFilter {
		// public boolean accept(File dir, String name) {
		//				if ((name.indexOf("Makefile.tau") != 0)) { //$NON-NLS-1$
		// return false;
		// }
		//
		// return true;
		// }
		// }

		if (bindir == null || !bindir.fetchInfo().exists()) {
			return null;
		}

		// int lastSlash=binpath.lastIndexOf(File.separator);
		// if(lastSlash<0)
		// return null;
		// IFileStore taumain=blt.getFile(binpath);
		// IFileStore taumain=bindir.getParent();
		//tlpath = binpath.substring(0,lastSlash) + File.separator + "lib"; //$NON-NLS-1$
		taulib = bindir.getParent().getChild("lib"); // blt.getFile(tlpath);// new File(tlpath); //$NON-NLS-1$
		// IFileStore[] mfiles = null;
		IFileStore[] mfiles = null;
		ArrayList<IFileStore> tmfiles = null;
		// makefilter mfilter = new makefilter();
		if (taulib.fetchInfo().exists()) {
			try {
				mfiles = taulib.childStores(EFS.NONE, null);
			} catch (CoreException e) {
				e.printStackTrace();
			}
			tmfiles = new ArrayList<IFileStore>();
			for (IFileStore mfile : mfiles) {
				IFileInfo finf = mfile.fetchInfo();
				if (!finf.isDirectory() && finf.getName().startsWith(TAU_MAKEFILE_PREFIX)) {
					tmfiles.add(mfile);
				}
			}

			// mfiles = taulib.listFiles(mfilter);
		}

		return tmfiles;
	}

	/**
	 * (re)populates the makefile combo box with those makefiles that match the selected options
	 */
	private void initMakeCombo() {
		try {
			String adding = ""; //$NON-NLS-1$

			makecombo.removeAll();
			/* Put the list of valid makefiles in selmakefiles */
			selectMakefiles();
			String select = ""; //$NON-NLS-1$

			/* If there are valid makefiles, put each one in the fresh combobox */
			if ((selmakefiles != null) && (selmakefiles.size() > 0)) {
				Iterator<String> i = selmakefiles.iterator();
				while (i.hasNext()) {
					adding = i.next();
					/*
					 * We want to select the minimal (shortest) makefile by default
					 */
					if ((select.length() > adding.length()) || select.equals("")) { //$NON-NLS-1$
						select = adding;
					}
					makecombo.add(adding);
				}
				/*
				 * If the currently selected makefile is still present, keep it. Otherwise use the shortest
				 */
				if ((selmakefile != null) && (makecombo.indexOf(selmakefile) >= 0)) {
					makecombo.select(makecombo.indexOf(selmakefile));
				} else {
					makecombo.select(makecombo.indexOf(select));
				}
			}
			/*
			 * If there are no valid makefiles, make it known
			 */
			else {
				makecombo.add(Messages.TAUAnalysisTab_NoValidMakefiles);
				makecombo.select(0);
			}
			String makeStub = makecombo.getItem(makecombo.getSelectionIndex());

			updateComboDerivedOptions(makeStub);

			//
			// /*
			// * If the new makefile has no PDT, only compiler instrumentation is available
			// */
			// if(checkMakeStub.indexOf("-pdt")>=0){
			// if(!pdtRadios[0].getEnabled()){
			// pdtRadios[0].setEnabled(true);
			// pdtRadios[0].setSelection(true);
			// }
			//
			//
			// //this.pdtRadios[1].setEnabled(true);
			// }else{
			// pdtRadios[0].setEnabled(false);
			// pdtRadios[1].setSelection(true);
			// }

			makecombo.pack();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void updateComboDerivedOptions(String makeStub) {
		/*
		 * If the new makefile has the right options, activate the papi selector
		 */

		if (makeStub.indexOf("-papi") > 0) { //$NON-NLS-1$
			papiSelect.setEnabled(true);
		} else {
			papiSelect.setEnabled(false);
		}

		/*
		 * If the new makefile has no PDT, only compiler instrumentation is available
		 */
		if (makeStub.indexOf("-pdt") >= 0) { //$NON-NLS-1$
			if (!pdtRadios[0].getEnabled()) {
				pdtRadios[0].setEnabled(true);
				// pdtRadios[0].setSelection(true);
				// pdtOpt.setSelected(true);
				// pdtRadios[1].setSelection(false);
				// compOpt.setSelected(false);

				// tauOpts.OptUpdate();
				// tauEnv.OptUpdate();
			}

			// this.pdtRadios[1].setEnabled(true);
		} else {
			pdtRadios[0].setEnabled(false);
			pdtRadios[0].setSelection(false);
			// pdtOpt.setSelected(false);
			pdtRadios[1].setSelection(true);
			// compOpt.setSelected(true);

			// tauOpts.OptUpdate();
			// tauEnv.OptUpdate();
		}

		/*
		 * If MPI box was checked enable tauinc box. Else, uncheck/disable tauinc box
		 * 
		 * @author raportil
		 */
		if (makeStub.indexOf("-callpath") >= 0 && makeStub.indexOf("-mpi") >= 0) { //$NON-NLS-1$ //$NON-NLS-2$
			runTauinc.setEnabled(true);
		} else {
			runTauinc.setSelection(false);
			runTauinc.setEnabled(false);

		}

	}

	/**
	 * Populates the set selmakefiles with those makefiles in allmakefiles that contain every option in selopts
	 */
	private void selectMakefiles() {
		selmakefiles = new LinkedHashSet<String>();
		Iterator<String> allit = allmakefiles.iterator();
		String curmake = ""; //$NON-NLS-1$
		String curopt = ""; //$NON-NLS-1$
		// Look at each makefile individually
		while (allit.hasNext()) {
			Iterator<String> nameit = selopts.iterator();
			// Assume the makefile meets the desired criteria
			boolean hasall = true;
			curmake = allit.next();
			// Look at each option in the selected options
			while (nameit.hasNext()) {
				curopt = nameit.next();
				// If the makefile is missing a required option, mark it a
				// failure and keep checking
				if (curmake.indexOf("-" + curopt) <= 0) { //$NON-NLS-1$
					hasall = false;
					break;
				}
			}
			// If the makefile wasn't rejected, add it to the list of selectable
			// makefiles
			if (hasall) {
				selmakefiles.add(curmake);
			} else {
				hasall = true;
			}
		}
	}

	/**
	 * Generates the UI for the analyis tab, consisting of sub-tabs which may be dynamically generated
	 * 
	 * @see ILaunchConfigurationTab#createControl(Composite)
	 */
	public void createControl(Composite parent) {

		Composite comp = new Composite(parent, SWT.NONE);
		setControl(comp);
		FillLayout topLayout = new FillLayout();
		comp.setLayout(topLayout);
		TabFolder tabParent = new TabFolder(comp, SWT.BORDER);

		/*
		 * 
		 * Analysis Options: TAU Makefile options and PAPI counter selection
		 */
		TabItem anaTab = new TabItem(tabParent, SWT.NULL);
		anaTab.setText(Messages.TAUAnalysisTab_AnalysisOptions);

		ScrolledComposite scrollAna = new ScrolledComposite(tabParent, SWT.V_SCROLL);

		Composite anaComp = new Composite(scrollAna, SWT.NONE);
		anaTab.setControl(scrollAna);

		anaComp.setLayout(createGridLayout(1, false, 0, 0));
		anaComp.setLayoutData(spanGridData(GridData.FILL_HORIZONTAL, 5));

		/*
		 * The actual controls of AnaComp
		 */
		createVerticalSpacer(anaComp, 2);

		for (int i = 0; i < checks.length; i++) {
			/* Papi is a special case */
			if (i == papiIndex) {
				papiComp = new Composite(anaComp, SWT.NONE);
				papiComp.setLayout(createGridLayout(5, false, 0, 0));
				papiComp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
				checks[i].unitCheck = createCheckButton(papiComp, checks[i].buttonText);
				checks[i].unitCheck.setToolTipText(checks[i].toolText);
				checks[i].unitCheck.addSelectionListener(listener);
				papiSelect = createPushButton(papiComp, Messages.TAUAnalysisTab_SelectPapiCounters, null);
				papiSelect.setToolTipText(Messages.TAUAnalysisTab_SetPapiEnvVar);
				papiSelect.addSelectionListener(listener);
				papiCountRadios = new Button[2];
				papiCountRadios[0] = createRadioButton(papiComp, Messages.TAUAnalysisTab_PresetCounters);
				papiCountRadios[1] = createRadioButton(papiComp, Messages.TAUAnalysisTab_NativeCounters);
				// papiCountRadios[2] = createRadioButton(papiComp,
				// "PAPI-C");
			} else if (i == pdtIndex) {
				pdtComp = new Composite(anaComp, SWT.NONE);
				pdtComp.setLayout(createGridLayout(5, false, 0, 0));
				pdtComp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
				checks[i].unitCheck = createCheckButton(pdtComp, checks[i].buttonText);
				checks[i].unitCheck.setToolTipText(checks[i].toolText);
				checks[i].unitCheck.addSelectionListener(listener);
				pdtRadios = new Button[2];
				pdtRadios[0] = createRadioButton(pdtComp, Messages.TAUAnalysisTab_PDTInstrumentation);
				pdtRadios[1] = createRadioButton(pdtComp, Messages.TAUAnalysisTab_CompilerInstrumentation);
				pdtRadios[0].addSelectionListener(listener);
				pdtRadios[1].addSelectionListener(listener);
			} else if (i == mpiIndex) {
				mpiComp = new Composite(anaComp, SWT.NONE);
				mpiComp.setLayout(createGridLayout(5, false, 0, 0));
				mpiComp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
				checks[i].unitCheck = createCheckButton(mpiComp, checks[i].buttonText);
				checks[i].unitCheck.setToolTipText(checks[i].toolText);
				checks[i].unitCheck.addSelectionListener(listener);

				/*
				 * Put tauinc box below MPI box
				 * 
				 * @author raportil
				 */

				runTauinc = createCheckButton(mpiComp, Messages.TAUAnalysisTab_GenerateMPIIncludeList);
				runTauinc.addSelectionListener(listener);
			} else {
				checks[i].unitCheck = createCheckButton(anaComp, checks[i].buttonText);
				checks[i].unitCheck.setToolTipText(checks[i].toolText);
				checks[i].unitCheck.addSelectionListener(listener);
			}
		}
		/*
		 * Composite comComp=new Composite(parallelComp,SWT.NONE); comComp.setLayout(createGridLayout(2, false, 0, 0));
		 * comComp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL)); Label compLab = new Label(comComp, 0);
		 * compLab.setText("Using Compiler:"); compiler = new Text(comComp, SWT.BORDER | SWT.SINGLE); compiler.setLayoutData(new
		 * GridData(GridData.FILL_HORIZONTAL)); compiler.addModifyListener(listener);
		 */

		Composite makeComp = new Composite(anaComp, SWT.NONE);
		makeComp.setLayout(createGridLayout(2, false, 0, 0));
		makeComp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		Label makeLab = new Label(makeComp, 0);
		makeLab.setText(Messages.TAUAnalysisTab_SelectMakefile);
		makecombo = new Combo(makeComp, SWT.DROP_DOWN | SWT.READ_ONLY | SWT.BORDER);
		makecombo.addSelectionListener(listener);

		anaComp.pack();
		int anaCompHeight = anaComp.computeSize(SWT.DEFAULT, SWT.DEFAULT).y;

		scrollAna.setContent(anaComp);
		scrollAna.setMinSize(400, anaCompHeight);
		scrollAna.setExpandHorizontal(true);
		scrollAna.setExpandVertical(true);

		/*
		 * 
		 * TAU Compiler: TAU Compiler options
		 */

		// tauOpts.encloseOpts="\'";
		// tauOpts.prependOpts="-tau_options=";
		// tauOpts.separateOpts=" ";

		// TabItem envTab = new TabItem(tabParent, SWT.NULL);
		// envTab.setText(tauEnv.paneName.trim());

		// ScrolledComposite scrollEnv = new ScrolledComposite(tabParent, SWT.V_SCROLL);

		// Composite envComp = new Composite(scrollEnv, SWT.NONE);

		// envTab.setControl(scrollEnv);

		/*
		 * The actual controls of optComp
		 */

		// tauEnv.makeToolPane(envComp, new TauPaneListener(tauEnv));

		// envComp.pack();
		// int envCompHeight = envComp.computeSize(SWT.DEFAULT, SWT.DEFAULT).y;
		// scrollEnv.setContent(envComp);
		// scrollEnv.setMinSize(400, envCompHeight);
		// scrollEnv.setExpandHorizontal(true);
		// scrollEnv.setExpandVertical(true);

		/*
		 * 
		 * TAU Compiler: TAU Compiler options
		 */

		// tauOpts.encloseOpts="\'";
		// tauOpts.prependOpts="-tau_options=";
		// tauOpts.separateOpts=" ";

		// TabItem optTab = new TabItem(tabParent, SWT.NULL);
		// optTab.setText(tauOpts.paneName.trim());

		// ScrolledComposite scrollOpt = new ScrolledComposite(tabParent, SWT.V_SCROLL);
		//
		// Composite optComp = new Composite(scrollOpt, SWT.NONE);
		//
		// optTab.setControl(scrollOpt);

		/*
		 * The actual controls of optComp
		 */

		// tauOpts.makeToolPane(optComp, new TauPaneListener(tauOpts));
		//
		// optComp.pack();
		// int optCompHeight = optComp.computeSize(SWT.DEFAULT, SWT.DEFAULT).y;
		// scrollOpt.setContent(optComp);
		// scrollOpt.setMinSize(400, optCompHeight);
		// scrollOpt.setExpandHorizontal(true);
		// scrollOpt.setExpandVertical(true);

		/*
		 * 
		 * Selective Instrumentation
		 */
		TabItem selinstTab = new TabItem(tabParent, SWT.NULL);
		selinstTab.setText(Messages.TAUAnalysisTab_SelectiveInstrumentation);

		Composite selinstComp = new Composite(tabParent, SWT.NONE);
		selinstTab.setControl(selinstComp);

		selinstComp.setLayout(createGridLayout(1, false, 0, 0));
		selinstComp.setLayoutData(spanGridData(GridData.FILL_HORIZONTAL, 5));

		/*
		 * The actual controls of selinstTab
		 */
		createVerticalSpacer(selinstComp, 1);

		selectRadios = new Button[4];

		selectRadios[0] = createRadioButton(selinstComp, Messages.TAUAnalysisTab_None);
		selectRadios[0].setToolTipText(Messages.TAUAnalysisTab_NoSelectiveInstrumentation);
		selectRadios[1] = createRadioButton(selinstComp, Messages.TAUAnalysisTab_Internal);
		selectRadios[1].setToolTipText(Messages.TAUAnalysisTab_UseGeneratedSelInstFile
				+ Messages.TAUAnalysisTab_ByWorkspaceCommands);
		selectRadios[2] = createRadioButton(selinstComp, Messages.TAUAnalysisTab_UserDefined);
		selectRadios[2].setToolTipText(Messages.TAUAnalysisTab_SpecPreExistingSelInst + Messages.TAUAnalysisTab_File);

		selComp = new Composite(selinstComp, SWT.NONE);
		selComp.setLayout(createGridLayout(2, false, 0, 0));
		selComp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		tauSelectFile = new Text(selComp, SWT.BORDER | SWT.SINGLE);
		tauSelectFile.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		tauSelectFile.addModifyListener(listener);
		browseSelfileButton = createPushButton(selComp, Messages.TAUAnalysisTab_Browse, null);
		browseSelfileButton.addSelectionListener(listener);

		selectRadios[3] = createRadioButton(selinstComp, Messages.TAUAnalysisTab_Automatic);
		// selectRadios[3].setEnabled(false);
		for (Button selectRadio : selectRadios) {
			selectRadio.addSelectionListener(listener);
		}

		/*
		 * 
		 * Data Collection: Storage and management of output data
		 */
		TabItem dataTab = new TabItem(tabParent, SWT.NULL);
		dataTab.setText(Messages.TAUAnalysisTab_DataCollection);

		Composite dataComp = new Composite(tabParent, SWT.NONE);
		dataTab.setControl(dataComp);

		dataComp.setLayout(createGridLayout(1, false, 0, 0));
		dataComp.setLayoutData(spanGridData(GridData.FILL_HORIZONTAL, 5));

		/*
		 * The actual controls of dataTab
		 */
		createVerticalSpacer(dataComp, 1);

		// buildonlyCheck = createCheckButton(dataComp,
		// "Build the instrumented executable but do not launch it");
		// buildonlyCheck.addSelectionListener(listener);
		// noParallelRun=createCheckButton(dataComp,"Auto-select the above for MPI-based makefiles");
		// noParallelRun.addSelectionListener(listener);
		// nocleanCheck = createCheckButton(dataComp,
		// "Keep instrumented executable");
		// nocleanCheck.addSelectionListener(listener);

		Composite dbComp = new Composite(dataComp, SWT.NONE);
		dbComp.setLayout(createGridLayout(2, false, 0, 0));
		dbComp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label dbLab = new Label(dbComp, 0);
		dbLab.setText(Messages.TAUAnalysisTab_SelectDatabase);

		dbCombo = new Combo(dbComp, SWT.DROP_DOWN | SWT.READ_ONLY | SWT.BORDER);
		dbCombo.addSelectionListener(listener);

		keepprofsCheck = createCheckButton(dataComp, Messages.TAUAnalysisTab_KeepProfiles);
		keepprofsCheck.addSelectionListener(listener);

		profSummaryCheck = createCheckButton(dataComp, Messages.TAUAnalysisTab_ProfileSummary);
		profSummaryCheck.addSelectionListener(listener);

		portalCheck = createCheckButton(dataComp, Messages.TAUAnalysisTab_UploadDataToTauPortal);
		portalCheck.addSelectionListener(listener);
	}

	public void updateComboFromSelection() {
		System.out.println("change startup"); //$NON-NLS-1$
	}

	/**
	 * Defaults are empty.
	 * 
	 * @see ILaunchConfigurationTab#setDefaults(ILaunchConfigurationWorkingCopy)
	 */
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(ITAULaunchConfigurationConstants.MPI, !noPTP);
		configuration.setAttribute(ITAULaunchConfigurationConstants.TAUINC, ITAULaunchConfigurationConstants.TAUINC_DEF);
		configuration.setAttribute(ITAULaunchConfigurationConstants.CALLPATH, ITAULaunchConfigurationConstants.CALLPATH_DEF);
		configuration.setAttribute(ITAULaunchConfigurationConstants.MEMORY, ITAULaunchConfigurationConstants.MEMORY_DEF);
		configuration.setAttribute(ITAULaunchConfigurationConstants.PAPI, ITAULaunchConfigurationConstants.PAPI_DEF);
		configuration.setAttribute(ITAULaunchConfigurationConstants.PERF, ITAULaunchConfigurationConstants.PERF_DEF);
		configuration.setAttribute(ITAULaunchConfigurationConstants.TRACE, ITAULaunchConfigurationConstants.TRACE_DEF);
		configuration.setAttribute(ITAULaunchConfigurationConstants.PHASE, ITAULaunchConfigurationConstants.PHASE_DEF);
		configuration.setAttribute(ITAULaunchConfigurationConstants.COMPILER, ITAULaunchConfigurationConstants.COMPILER_DEF);

		configuration.setAttribute(ITAULaunchConfigurationConstants.EPILOG, ITAULaunchConfigurationConstants.EPILOG_DEF);
		configuration.setAttribute(ITAULaunchConfigurationConstants.VAMPIRTRACE, ITAULaunchConfigurationConstants.VAMPIRTRACE_DEF);

		// configuration.setAttribute(IToolLaunchConfigurationConstants.NOCLEAN,
		// IToolLaunchConfigurationConstants.NOCLEAN_DEF);
		configuration.setAttribute(ITAULaunchConfigurationConstants.KEEPPROFS, ITAULaunchConfigurationConstants.KEEPPROFS_DEF);

		configuration.setAttribute(ITAULaunchConfigurationConstants.SELECT, 0);
		configuration.setAttribute(ITAULaunchConfigurationConstants.SELECT_FILE, ""); //$NON-NLS-1$

		configuration.setAttribute(ITAULaunchConfigurationConstants.ENVVARS, (Map<String, String>) null);

		configuration.setAttribute(ITAULaunchConfigurationConstants.TAU_MAKEFILE, ""); //$NON-NLS-1$

		// tauOpts.setDefaults(configuration);
		// tauEnv.setDefaults(configuration);
	}

	// ToolOption pdtOpt = null;// tauOpts.getOption("-optPDTInst");
	// ToolOption compOpt = null;// tauOpts.getOption("-optCompInst");
	// ToolOption selectOpt = null;

	/**
	 * @see ILaunchConfigurationTab#initializeFrom(ILaunchConfiguration)
	 */
	@SuppressWarnings("unchecked")
	public void initializeFrom(ILaunchConfiguration configuration) {
		try {

			if (LaunchUtils.getRemoteServicesId(configuration) != null) {
				blt = new RemoteBuildLaunchUtils(configuration);
			} else {
				blt = new BuildLaunchUtils();
			}

			selopts = new LinkedHashSet<String>();

			initMakefiles();
			initMakeChecks();

			for (CheckItem check : checks) {
				check.unitCheck.setSelection(configuration.getAttribute(check.confString, check.defState));
				if (check.unitCheck.getSelection() && check.unitCheck.getEnabled()) {
					selopts.add(check.makeCmd);
				}
			}

			/*
			 * If MPI box checked and enabled, enable tauinc box. Else, disable tauinc box
			 * 
			 * @author raportil
			 */
			if (checks[mpiIndex].unitCheck.getSelection() && checks[callpathIndex].unitCheck.getSelection()
					&& checks[mpiIndex].unitCheck.getEnabled()) {
				runTauinc.setEnabled(true);
			} else {
				runTauinc.setEnabled(false);
			}
			runTauinc.setSelection(configuration.getAttribute(ITAULaunchConfigurationConstants.TAUINC, false));

			// tauOpts.OptUpdate();
			//
			// tauOpts.initializePane(configuration);
			//
			// tauEnv.OptUpdate();
			// tauEnv.initializePane(configuration);

			int pcr = configuration.getAttribute(ITAULaunchConfigurationConstants.PAPISELECT, 0);
			if (pcr > 1) {
				pcr = 0;
			}
			papiCountRadios[pcr].setSelection(true);

			int pdtSel = configuration.getAttribute(ITAULaunchConfigurationConstants.PDTSELECT, 0);
			if (pdtSel > 1) {
				pdtSel = 0;
			}
			pdtRadios[pdtSel].setSelection(true);
			// TODO: Find a way to make this logic work
			//			pdtOpt = tauOpts.getOption("-optPDTInst"); //$NON-NLS-1$
			//			compOpt = tauOpts.getOption("-optCompInst"); //$NON-NLS-1$
			// if (pdtOpt != null && compOpt != null) {
			// pdtOpt.setEnabled(false);
			// compOpt.setEnabled(false);
			// if (pdtSel == 0) {
			// pdtOpt.setSelected(true);
			// compOpt.setSelected(false);
			// } else {
			// pdtOpt.setSelected(false);
			// compOpt.setSelected(true);
			// }
			// }

			selmakefile = configuration.getAttribute(ITAULaunchConfigurationConstants.TAU_MAKENAME, (String) null);

			initMakeCombo();
			reinitMakeChecks();

			int selected = configuration.getAttribute(ITAULaunchConfigurationConstants.SELECT, 0);

			selectRadios[selected].setSelection(true);

			tauSelectFile.setText(configuration.getAttribute(ITAULaunchConfigurationConstants.SELECT_FILE, "")); //$NON-NLS-1$

			if (!selectRadios[2].getSelection()) {
				selComp.setEnabled(false);
				tauSelectFile.setEnabled(false);
				tauSelectFile.setEnabled(false);
			}
			// TODO: Find a way to make this cross-pane logic work

			selectFieldVal = configuration.getAttribute(selFileValConf, "");
			selectFieldChecked = configuration.getAttribute(selFileButtonConf, false);

			//			selectOpt = new ToolOption();// tauOpts.getOption("-optTauSelectFile"); //$NON-NLS-1$
			// if (selectOpt != null) {
			// selectOpt.setEnabled(false);
			// // selectOpt.setArg(configuration.getAttribute(ITAULaunchConfigurationConstants.INTERNAL_SELECTIVE_FILE, ""));
			// }

			// buildonlyCheck.setSelection(configuration.getAttribute(
			// IToolLaunchConfigurationConstants.BUILDONLY, false));
			// noParallelRun.setSelection(configuration.getAttribute(
			// ITAULaunchConfigurationConstants.NOPARRUN, noPTP));

			// if(noParallelRun.getSelection()&&selmakefile.indexOf("-mpi")>0)
			// {
			// buildonlyCheck.setSelection(true);
			// }

			// nocleanCheck.setSelection(configuration.getAttribute(
			// IToolLaunchConfigurationConstants.NOCLEAN, false));

			initDBCombo(configuration.getAttribute(ITAULaunchConfigurationConstants.PERFDMF_DB, (String) null));

			keepprofsCheck.setSelection(configuration.getAttribute(ITAULaunchConfigurationConstants.KEEPPROFS, false));

			profSummaryCheck.setSelection(configuration.getAttribute(ITAULaunchConfigurationConstants.PROFSUMMARY, false));

			portalCheck.setSelection(configuration.getAttribute(ITAULaunchConfigurationConstants.PORTAL, false));

			varmap = archvarmap = configuration.getAttribute(ITAULaunchConfigurationConstants.ENVVARS, (Map<String, String>) null);

			//Activator.getDefault().getPluginPreferences().setDefault(ITAULaunchConfigurationConstants.TAU_CHECK_AUTO_OPT,true); //$NON-NLS-1$

		} catch (CoreException e) {
			setErrorMessage(Messages.TAUAnalysisTab_CoreExceptionInitAnalysisTab + e.getMessage());
		}
	}

	private void initDBCombo(String selected) {
		String[] dbs = null;
		try {
			dbs = PerfDMFUIPlugin.getPerfDMFView().getDatabaseNames();
		} catch (java.lang.NoClassDefFoundError e) {
			System.out.println(Messages.TAUAnalysisTab_WarnTauJarsNotFound);
		}

		dbCombo.clearSelection();
		dbCombo.removeAll();
		if (dbs == null || dbs.length < 1) {
			dbCombo.add(ITAULaunchConfigurationConstants.NODB);
			dbCombo.select(0);
			return;
		}

		for (String db : dbs) {
			dbCombo.add(db);
			// System.out.println(dbs[i]);
		}

		if (selected == null || dbCombo.indexOf(selected) < 0) {
			dbCombo.select(0);
		} else {
			dbCombo.select(dbCombo.indexOf(selected));
		}
	}

	/**
	 * @see ILaunchConfigurationTab#performApply(ILaunchConfigurationWorkingCopy)
	 */
	@SuppressWarnings("unchecked")
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		for (CheckItem check : checks) {
			configuration.setAttribute(check.confString, check.unitCheck.getSelection());
		}

		configuration.setAttribute(ITAULaunchConfigurationConstants.TAUINC, runTauinc.getSelection());

		// tauOpts.performApply(configuration);

		// configuration.setAttribute(tauOpts.configID, tauOpts.getOptionString());
		// configuration.setAttribute(tauOpts.configVarID, tauOpts.getVarMap());
		//
		// tauEnv.performApply(configuration);
		// configuration.setAttribute(tauEnv.configID, tauEnv.getOptionString());
		// configuration.setAttribute(tauEnv.configVarID, tauEnv.getVarMap());

		// configuration.setAttribute(IToolLaunchConfigurationConstants.BUILDONLY,
		// buildonlyCheck.getSelection());
		// configuration.setAttribute(ITAULaunchConfigurationConstants.NOPARRUN,
		// noParallelRun.getSelection());
		// configuration.setAttribute(IToolLaunchConfigurationConstants.NOCLEAN,
		// nocleanCheck.getSelection());
		configuration.setAttribute(ITAULaunchConfigurationConstants.KEEPPROFS, keepprofsCheck.getSelection());

		configuration.setAttribute(ITAULaunchConfigurationConstants.PROFSUMMARY, profSummaryCheck.getSelection());

		configuration.setAttribute(ITAULaunchConfigurationConstants.PORTAL, portalCheck.getSelection());

		/*
		 * If varmap is null and avm is not... or vm is not null and avm is null... or neither are null and they're equal
		 */
		if (((varmap == null) && (archvarmap != null)) || ((varmap != null) && (archvarmap == null))
				|| ((varmap != null) && (archvarmap != null) && !varmap.equals(archvarmap))) {
			Map<String, String> envvars = null;

			try {
				envvars = configuration.getAttribute(ILaunchManager.ATTR_ENVIRONMENT_VARIABLES, (Map<String, String>) null);
			} catch (CoreException e) {
				e.printStackTrace();
			}

			if ((envvars != null) && (envvars.size() > 0) && (archvarmap != null) && (archvarmap.size() > 0)) {
				Iterator<String> archit = archvarmap.keySet().iterator();
				while (archit.hasNext()) {
					envvars.remove(archit.next());
				}
			}

			if ((varmap != null) && (varmap.size() > 0)) {
				if (envvars == null) {
					envvars = new HashMap<String, String>();
				}
				envvars.putAll(varmap);
			}

			configuration.setAttribute(ILaunchManager.ATTR_ENVIRONMENT_VARIABLES, envvars);
			configuration.setAttribute(ITAULaunchConfigurationConstants.ENVVARS, varmap);
		}
		if (papiCountRadios[0].getSelection()) {
			configuration.setAttribute(ITAULaunchConfigurationConstants.PAPISELECT, 0);
		} else if (papiCountRadios[1].getSelection()) {
			configuration.setAttribute(ITAULaunchConfigurationConstants.PAPISELECT, 1);
		}

		if (pdtRadios[0].getSelection()) {
			configuration.setAttribute(ITAULaunchConfigurationConstants.PDTSELECT, 0);
		} else {
			configuration.setAttribute(ITAULaunchConfigurationConstants.PDTSELECT, 1);
		}

		// else if (papiCountRadios[2].getSelection()){
		// configuration.setAttribute(ITAULaunchConfigurationConstants.PAPISELECT,
		// 2);
		// }

		/**
		 * Selective instrumentation file specification
		 */
		{
			int selected = 0;
			for (int i = 0; i < selectRadios.length; i++) {
				if (selectRadios[i].getSelection()) {
					selected = i;
					break;
				}
			}
			//
			// String selcommand="";
			// String selpath="";
			// if(selected==1)
			// {
			// //configuration.getLocation().toOSString();
			// selpath=ToolsOptionsConstants.PROJECT_LOCATION+File.separator+"tau.selective";
			// selcommand="-optTauSelectFile="+selpath;
			// configuration.setAttribute(ITAULaunchConfigurationConstants.INTERNAL_SELECTIVE_FILE, selpath);
			// }
			// else
			// if(selected==2)
			// {
			// selpath=tauSelectFile.getText();
			// configuration.setAttribute(ITAULaunchConfigurationConstants.SELECT_FILE, selpath);
			// if(!selpath.equals(""))
			// {
			// selcommand="-optTauSelectFile="+selpath;
			// }
			// configuration.setAttribute(ITAULaunchConfigurationConstants.INTERNAL_SELECTIVE_FILE, selpath);
			// }

			if (selected == 3) {
				configuration.setAttribute(ITAULaunchConfigurationConstants.TAU_REDUCE, true);
			} else {
				configuration.setAttribute(ITAULaunchConfigurationConstants.TAU_REDUCE, false);
			}
			// configuration.setAttribute(ITAULaunchConfigurationConstants.SELECT_COMMAND,selcommand);
			configuration.setAttribute(ITAULaunchConfigurationConstants.SELECT, selected);

			configuration.setAttribute(selFileValConf, selectFieldVal);
			configuration.setAttribute(selFileButtonConf, selectFieldChecked);
		}
		int seldex = makecombo.getSelectionIndex();
		if (seldex < 0 || taulib == null) {
			return;
		}
		String tauMakeName = makecombo.getItem(seldex);
		configuration.setAttribute(ITAULaunchConfigurationConstants.TAU_MAKENAME, tauMakeName);
		configuration.setAttribute(ITAULaunchConfigurationConstants.TAU_MAKEFILE,
				"-tau_makefile=" + taulib.toURI().getPath() + UNIX_SLASH + makecombo.getItem(makecombo.getSelectionIndex())); //$NON-NLS-1$

		configuration.setAttribute(ITAULaunchConfigurationConstants.PERFDMF_DB, dbCombo.getItem(dbCombo.getSelectionIndex()));
		configuration.setAttribute(ITAULaunchConfigurationConstants.PERFDMF_DB_NAME,
				PerfDMFView.extractDatabaseName(dbCombo.getItem(dbCombo.getSelectionIndex())));

		configuration
				.setAttribute(
						IToolLaunchConfigurationConstants.TOOLCONFNAME + "TAU", "_" + tauMakeName.substring(tauMakeName.lastIndexOf(".") + 1)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		// if(noParallelRun.getSelection()&&makecombo.getItem(makecombo.getSelectionIndex()).indexOf("-mpi")>0)
		// {
		// configuration.setAttribute(IToolLaunchConfigurationConstants.BUILDONLY,
		// true);
		// }

	}

	protected String getFieldContent(IntegerFieldEditor editorField) {
		return getFieldContent(editorField.getStringValue());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#isValid (org.eclipse.debug.core.ILaunchConfiguration)
	 */
	@Override
	public boolean isValid(ILaunchConfiguration configuration) {
		setErrorMessage(null);
		setMessage(null);

		return true;
	}

	/**
	 * Launches a file selection dialog to select a selective instrumentation file
	 * 
	 */
	protected void handleSelfileBrowseButtonSelected() {
		FileDialog dialog = new FileDialog(getShell());
		dialog.setText(Messages.TAUAnalysisTab_SelectTauSelInstFile);

		String correctPath = getFieldContent(tauSelectFile.getText());
		if (correctPath != null) {
			// IFileStore path = blt.getFile(correctPath);//new File(correctPath);
			// if (path.fetchInfo().exists()) {
			// dialog.setFilterPath(!path.fetchInfo().isDirectory() ? correctPath : path
			// .getParent());
			// } //TODO: Support a starting path for selective instrumentation files on the remote and/or local machine.
		}

		String selectedPath = dialog.open();
		if (selectedPath != null) {
			tauSelectFile.setText(selectedPath);
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

		if (makecombo == null || taulib == null) {
			return null;
		}

		int selDex = makecombo.getSelectionIndex();

		if (selDex == -1) {
			return null;
		}

		String selItem = makecombo.getItem(selDex);

		if (selItem == null) {
			return null;
		}

		// String papimake = tlpath + File.separator+ selItem;

		IFileStore papimakefile = blt.getFile(taulib.toURI().getPath());// new File(papimake);
		papimakefile = papimakefile.getChild(selItem);

		if (!papimakefile.fetchInfo().exists()) {
			System.out.println(Messages.TAUAnalysisTab_InvalidPAPIMakefile);
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
		if (found) {
			papiline = papiline.substring(papiline.indexOf(EQ) + 1);
			IFileStore papihome = blt.getFile(papiline); // new File(papiline + File.separator + BIN + File.separator +
															// PAPI_EVENT_CHOOSER);
			papibin = papihome.getChild(BIN).getChild(PAPI_EVENT_CHOOSER);
			if (!papibin.fetchInfo().exists()) {
				papibin = papihome.getChild(SHARE).getChild(PAPI).getChild(UTILS).getChild(PAPI_EVENT_CHOOSER);// new File(papiline
																												// + File.separator
																												// + SHARE +
																												// File.separator +
																												// PAPI +
																												// File.separator +
																												// UTILS +
																												// File.separator +
																												// PAPI_EVENT_CHOOSER);
			}

			if (!papibin.fetchInfo().exists()) {
				throw new FileNotFoundException(Messages.TAUAnalysisTab_CouldNotLocatePapiUtils);
			}

			// papiline = papibin.getParent().toURI().getPath();//.fetchInfo().getParentFile().toString();
		} else {
			System.out.println(Messages.TAUAnalysisTab_NoPapiDirInMakefile);
		}

		return papibin.getParent();
	}

	private static final String BIN = "bin"; //$NON-NLS-1$
	private static final String EQ = "="; //$NON-NLS-1$
	private static final String PAPI = "papi"; //$NON-NLS-1$
	private static final String PAPI_EVENT_CHOOSER = "papi_event_chooser"; //$NON-NLS-1$
	private static final String PAPIDIR = "PAPIDIR="; //$NON-NLS-1$
	private static final String UTILS = "utils"; //$NON-NLS-1$
	private static final String SHARE = "share"; //$NON-NLS-1$
	private static final String PAPI_XML_BIN = "papi_xml_event_info"; //$NON-NLS-1$

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
				// System.out.println(papiBin);
				//final String pTool="papi_xml_event_info"; //$NON-NLS-1$
				// File pDir=new File(papiBin);
				// String[]files=pDir.list(new FilenameFilter(){
				//
				// public boolean accept(File fi, String name) {
				// if(name.equals(pTool))
				// return true;
				// return false;
				// }
				// });

				// if(files.length<1){
				// MessageDialog.openWarning(getShell(),PAPI_XML_BIN+Messages.TAUAnalysisTab_NotFound,Messages.TAUAnalysisTab_PleaseSelectDiffCounterTool);
				// return;
				// }

				EventTreeDialog treeD = new EventTreeDialog(getShell(), pdir, blt);
				// if ((varmap != null) && (varmap.size() > 0)) {
				// treeD.setInitialSelections(varmap.values().toArray());
				// }

				if (treeD.open() == Window.OK) {
					selected = treeD.getCommands().toArray();
				}
			}

			else {
				LabelProvider papilab = new LabelProvider();
				ArrayContentProvider paprov = new ArrayContentProvider();

				int papiCountType = PapiListSelectionDialog.PRESET;
				if (papiCountRadios[1].getSelection()) {
					papiCountType = PapiListSelectionDialog.NATIVE;
				}
				PapiListSelectionDialog papidialog = new PapiListSelectionDialog(getShell(), pdir, blt, paprov, papilab,
						Messages.TAUAnalysisTab_SelectPapiCountersForTau, papiCountType);
				papidialog.setTitle(Messages.TAUAnalysisTab_PapiCounters);
				papidialog.setHelpAvailable(false);
				if ((varmap != null) && (varmap.size() > 0)) {
					papidialog.setInitialSelections(varmap.values().toArray());
				}

				if (papidialog.open() == Window.OK) {
					selected = papidialog.getResult();
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
			Iterator<Object> varit = selset.iterator();
			int counter = 2;
			while (varit.hasNext()) {
				String varTxt = (String) varit.next();
				if (varTxt.indexOf(pPre) != 0) {
					varTxt = pn + varTxt;
				}
				varmap.put("COUNTER" + counter, varTxt); //$NON-NLS-1$
				counter++;
			}

		} else {
			varmap = null;
		}
	}

	/**
	 * @see ILaunchConfigurationTab#getName()
	 */
	@Override
	public String getName() {
		return "TAU"; //$NON-NLS-1$
	}

	/**
	 * @see ILaunchConfigurationTab#setLaunchConfigurationDialog (ILaunchConfigurationDialog)
	 */
	@Override
	public void setLaunchConfigurationDialog(ILaunchConfigurationDialog dialog) {
		super.setLaunchConfigurationDialog(dialog);
	}

	/**
	 * @see ILaunchConfigurationTab#getImage()
	 */
	@Override
	public Image getImage() {
		return LaunchImages.getImage("org.eclipse.ptp.etfw.tau.core.tauLogo.gif"); //$NON-NLS-1$
	}

}
