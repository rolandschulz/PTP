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
package org.eclipse.ptp.perf.tau;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.ptp.perf.Activator;
import org.eclipse.ptp.perf.IPerformanceLaunchConfigurationConstants;
import org.eclipse.ptp.perf.internal.BuildLaunchUtils;
import org.eclipse.ptp.perf.tau.papiselect.PapiListSelectionDialog;
import org.eclipse.ptp.perf.tau.papiselect.papic.EventTreeDialog;
import org.eclipse.ptp.perf.tau.perfdmf.views.PerfDMFView;
import org.eclipse.ptp.perf.toolopts.ToolPane;
import org.eclipse.ptp.perf.toolopts.ToolPaneListener;
import org.eclipse.ptp.perf.ui.AbstractPerformanceConfigurationTab;
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
 * @author wspear
 *
 */
public class TAUAnalysisTab extends AbstractPerformanceConfigurationTab {

	/**
	 * Encapsulates individual options for TAU makefile selection
	 * @author wspear
	 *
	 */
	protected class CheckItem {

		CheckItem() {
		}

		CheckItem(String cmd, String bText, String tText, String cString,
				boolean def) {
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
	protected boolean noPTP=false;

	/**
	 * Sets weather or not it is possible to initiate a parallel launch from this tab
	 * @param noPar Availability of the PTP to this tab's launch configuration delegate
	 */
	public TAUAnalysisTab(boolean noPar) {
		noPTP=noPar;
	}

	/**
	 * Listens for action in the TAU build-options pane
	 * @author wspear
	 *
	 */
	protected class TauPaneListener extends ToolPaneListener{
		TauPaneListener(ToolPane tool) {
			super(tool);
		}
		protected void localAction(){
			updateLaunchConfigurationDialog();
		}
	}

	/**
	 * Defines/contins the CheckItems available for narrowing the selection of TAU makefiles 
	 */
	protected CheckItem checks[] = {
			new CheckItem("mpi", "MPI", "",
					ITAULaunchConfigurationConstants.MPI, true),
					new CheckItem("callpath", "Callpath Profiling", "",
							ITAULaunchConfigurationConstants.CALLPATH, false),
							new CheckItem("phase", "Phase Based Profiling", "",
									ITAULaunchConfigurationConstants.PHASE, false),
									new CheckItem("memory", "Memory Profiling", "",
											ITAULaunchConfigurationConstants.MEMORY, false),
											new CheckItem("opari", "OPARI", "",
													ITAULaunchConfigurationConstants.OPARI, false),
													new CheckItem("openmp", "OpenMP", "",
															ITAULaunchConfigurationConstants.OPENMP, false),
															new CheckItem("epilog", "Epilog", "",
																	ITAULaunchConfigurationConstants.EPILOG, false),
																	new CheckItem("vampirtrace", "VampirTrace", "",
																			ITAULaunchConfigurationConstants.VAMPIRTRACE, false),
																			new CheckItem("papi", "PAPI", "",
																					ITAULaunchConfigurationConstants.PAPI, false),
																					// Papi is entry 8 (needed for papi composite/MULTI)
																					new CheckItem("perf", "Perflib", "",
																							ITAULaunchConfigurationConstants.PERF, false),
																							new CheckItem("trace", "Trace", "",
																									ITAULaunchConfigurationConstants.TRACE, false), };

	/**
	 * The index of the mpi button in CheckItem list. Used to enable/disable tauinc.sh box
	 * @author raportil
	 */
	protected int mpiIndex = 0;
	protected int callpathIndex=1;
	protected Button runTauinc;

	/**
	 * The index of the papi button in CheckItem list.  This requires special treatment
	 */
	protected int papiIndex = 8;

	protected Button papiSelect;

	protected Button papiCountRadios[];

	protected Composite papiComp;

	protected Composite selComp;

	protected Label selectLabel;

	protected Button selectRadios[];

	protected Text compiler;

	//protected Button buildonlyCheck;

	//protected Button noParallelRun;

	protected Button nocleanCheck;

	protected Button keepprofsCheck;

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

	protected Combo dbCombo=null;

	/**
	 * The name of the selected makefile
	 */
	protected String selmakefile = null;

	/**
	 * The path to the TAU lib directory
	 */
	private String tlpath = null;

	protected Map<String, Object> archvarmap = null;

	protected Map<String, Object> varmap = null;

	//TODO:  This isn't generic.  We need to get this pane explicitly
	protected final ToolPane tauOpts = Activator.getTool("TAU").getGlobalCompiler().toolPanes[0];// toolPanes[0];//ToolMaker.makeTools(tauToolXML)[0].toolPanes[0];

//	protected ToolPane custOpts=null;

//	private static File tauToolXML= null;
//	/**
//	* Initialize the file that defines the TAU compilation options pane
//	*/
//	static{
//	try {

//	URL testURL=Activator.getDefault().getBundle().getEntry("toolxml"+File.separator+"tau_tool.xml");
//	tauToolXML = new File(new URI(FileLocator.toFileURL(testURL).toString().replaceAll(" ", "%20")));

//	} catch (Exception e) {
//	e.printStackTrace();
//	} 
//	}

	/**
	 * Listen for activity in the TAU makefile combo-box, CheckItem widgets or other options
	 * @author wspear
	 *
	 */
	protected class WidgetListener extends SelectionAdapter implements
	ModifyListener, IPropertyChangeListener {
		public void widgetSelected(SelectionEvent e) {

			Object source = e.getSource();
			/*
			 * Reinitialize the selected makefile
			 */
			if (source == makecombo) {
				selmakefile = makecombo.getItem(makecombo.getSelectionIndex());
				if ((selmakefile.indexOf("-papi") > 0)
						&& (selmakefile.indexOf("-multiplecounters") > 0)) {
					papiSelect.setEnabled(true);
				} else {
					papiSelect.setEnabled(false);
				}

//				if(noParallelRun.getSelection())
//				{
//				buildonlyCheck.setSelection(selmakefile.indexOf("-mpi")>0);
//				}
				updateLaunchConfigurationDialog();
			}
//			else
//			if(source==buildonlyCheck){
//			updateLaunchConfigurationDialog();
//			}
//			else
//			if(source==noParallelRun){
//			if(noParallelRun.getSelection()&&selmakefile.indexOf("-mpi")>0){
//			buildonlyCheck.setSelection(true);
//			}
//			}
			else
				if (source == browseSelfileButton) {
					handleSelfileBrowseButtonSelected();
				} else if (source == papiSelect) {
					handlePapiSelect();
				} else if (source.equals(selectRadios[2])) {
					if (!selectRadios[2].getSelection()) {
						selComp.setEnabled(false);
						tauSelectFile.setEnabled(false);
						tauSelectFile.setEnabled(false);
					} else {
						selComp.setEnabled(true);
						tauSelectFile.setEnabled(true);
						tauSelectFile.setEnabled(true);
					}
				}
			/*
			 *If not one of the above options, then one of the makefile selection options has been tripped
			 *Iterate through until we find which one, then check or uncheck it as necessary and reinitialize
			 *the combo box and the remaining available checkboxes 
			 */
				else {
					for (int i = 0; i < checks.length; i++) {
						if (source == checks[i].unitCheck) {
							if (((Button) source).getSelection()) {
								selopts.add(checks[i].makeCmd);
							} else {
								selopts.remove(checks[i].makeCmd);
							}
							initMakeCombo();
							reinitMakeChecks();
						}
					}
				}

			/*
			 *If MPI box was checked enable tauinc box. Else, uncheck/disable tauinc box
			 *@author raportil
			 */
			if (source == checks[mpiIndex].unitCheck||source==checks[callpathIndex].unitCheck) {
				if (checks[mpiIndex].unitCheck.getSelection()&&checks[callpathIndex].unitCheck.getSelection()) {
					runTauinc.setEnabled(true);
				} else {
					runTauinc.setSelection(false);
					runTauinc.setEnabled(false);
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
			}
			updateLaunchConfigurationDialog();
		}
	}

	protected WidgetListener listener = new WidgetListener();

	/*
	 * Plugins of this nature require a default constructor
	 */
	public TAUAnalysisTab(){
	}

	/**
	 * Disables exactly the TAU makefile selection checkboxes that are presently excluded by the selected TAU makefile selection checkboxes
	 * For example, if option A is selected, only options found in makefiles that include option A will remain enabled.
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

					if (holdmake.indexOf("-" + check) <= 0) {
						allgood = false;
						break;
					}
				}
				if (allgood == true) {
					goodopts.addAll(Arrays.asList(holdmake.split("-")));
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
	 * Initializes the makefile selection checkboxes.
	 * If there are no options or makefiles available, disables all makefile options
	 * Otherwise, selectively enables only those makefile options that are available in the available makefiles
	 *
	 */
	private void initMakeChecks() {
		if ((allmakefiles == null) || (allmakefiles.size() == 0)
				|| (allopts == null) || (allopts.size() == 0)) {
			for (int i = 0; i < checks.length; i++) {
				checks[i].unitCheck.setEnabled(false);
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
		//IPreferenceStore pstore = Activator.getDefault().getPreferenceStore();
		String archpath = BuildLaunchUtils.getToolPath("tau");//pstore.getString(ITAULaunchConfigurationConstants.TAU_ARCH_PATH);

		File[] mfiles = testTAUEnv(archpath);

		/*
		if ((mfiles == null) || (mfiles.length == 0)) {
			String checkArch = BuildLaunchUtils.checkToolEnvPath("tau_merge");
			if (checkArch != null) {
				checkArch=checkArch.substring(0, checkArch.lastIndexOf(File.separator));
				mfiles = testTAUEnv(checkArch);
			} else
				checkArch = archpath;
			Shell ourshell=PlatformUI.getWorkbench().getDisplay().getActiveShell();
			while ((mfiles == null || mfiles.length == 0) && checkArch != null) {
				checkArch = BuildLaunchUtils.askToolPath(checkArch,"Select TAU Arch Directory","You must select a valid TAU architecture "
						+ "directory.  Such a directory should be created "
						+ "when you configure and install TAU.  It must "
						+ "contain least one valid stub makefile configured "
						+ "with the Program Database Toolkit (pdt)",ourshell);
				mfiles = testTAUEnv(checkArch);
			}

			if (checkArch == null) {
				checkArch = "/";
				mfiles = testTAUEnv(checkArch);
			}

			pstore.setValue(ITAULaunchConfigurationConstants.TAU_ARCH_PATH, checkArch);
		}*/

		allmakefiles = new LinkedHashSet<String>();
		allopts = new LinkedHashSet<String>();
		String name = null;
		if(mfiles==null)return;
		for (int i = 0; i < mfiles.length; i++) {
			name = mfiles[i].getName();
			allmakefiles.add(name);
			allopts.addAll(Arrays.asList(name.split("-")));
		}
		allopts.remove("Makefile.tau");
	}

	/**
	 * Given a directory (presumably a tau arch directory) this looks in the lib
	 * subdirectory and returns a list of all Makefile.tau... files with -pdt
	 * */
	private File[] testTAUEnv(String binpath) {

		class makefilter implements FilenameFilter {
			public boolean accept(File dir, String name) {
				if ((name.indexOf("Makefile.tau") != 0)
						|| (name.indexOf("-pdt") <= 0)) {
					return false;
				}
				/*Only include papi makefiles built with multiplecounters*/
//				if (name.indexOf("-multiplecounters") <= 0
//						&& (name.indexOf("-papi") > 0)) {
//					return false;
//				}

				return true;
			}
		}
		tlpath = binpath.substring(0,binpath.lastIndexOf(File.separator)) + File.separator + "lib";
		File taulib = new File(tlpath);
		File[] mfiles = null;
		makefilter mfilter = new makefilter();
		if (taulib.exists()) {
			mfiles = taulib.listFiles(mfilter);
		}

		return mfiles;
	}

	/**
	 * (re)populates the makefile combo box with those makefiles that match the
	 * selected options
	 */
	private void initMakeCombo() {
		try {
			String adding = "";

			makecombo.removeAll();
			/*Put the list of valid makefiles in selmakefiles*/
			selectMakefiles();
			String select = "";

			/*If there are valid makefiles, put each one in the fresh combobox*/
			if ((selmakefiles != null) && (selmakefiles.size() > 0)) {
				Iterator<String> i = selmakefiles.iterator();
				while (i.hasNext()) {
					adding = i.next();
					/*
					 * We want to select the minimal (shortest) makefile by default
					 */
					if ((select.length() > adding.length())
							|| select.equals("")) {
						select = adding;
					}
					makecombo.add(adding);
				}
				/* 
				 * If the currently selected makefile is still present, keep it.  Otherwise use the shortest
				 */
				if ((selmakefile != null)
						&& (makecombo.indexOf(selmakefile) >= 0)) {
					makecombo.select(makecombo.indexOf(selmakefile));
				} else {
					makecombo.select(makecombo.indexOf(select));
				}
			}
			/*
			 *  If there are no valid makefiles, make it known
			 */
			else {
				makecombo.add("No Valid Makefiles!");
				makecombo.select(0);
			}
			String checkforpapi = makecombo.getItem(makecombo
					.getSelectionIndex());
			/*
			 * If the new makefile has the right options, activate the papi selector
			 */

			if ((checkforpapi.indexOf("-papi") > 0)
					&& (checkforpapi.indexOf("-multiplecounters") > 0)) {
				papiSelect.setEnabled(true);
			} else {
				papiSelect.setEnabled(false);
			}
			makecombo.pack();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Populates the set selmakefiles with those makefiles in allmakefiles that
	 * contain every option in selopts
	 */
	private void selectMakefiles() {
		selmakefiles = new LinkedHashSet<String>();
		Iterator<String> allit = allmakefiles.iterator();
		String curmake = "";
		String curopt = "";
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
				if (curmake.indexOf("-" + curopt) <= 0) {
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
		 * Analysis Options:  TAU Makefile options and PAPI counter selection
		 * 
		 * */
		TabItem anaTab = new TabItem(tabParent, SWT.NULL);
		anaTab.setText("Analysis Options");

		ScrolledComposite scrollAna = new ScrolledComposite(tabParent,SWT.V_SCROLL);

		Composite anaComp = new Composite(scrollAna, SWT.NONE);
		anaTab.setControl(scrollAna);

		anaComp.setLayout(createGridLayout(1, false, 0, 0));
		anaComp.setLayoutData(spanGridData(GridData.FILL_HORIZONTAL, 5));


		/*
		 * The actual controls of AnaComp
		 * */
		createVerticalSpacer(anaComp, 2);

		for (int i = 0; i < checks.length; i++) {
			/*Papi is a special case*/
			if (i != papiIndex) {
				checks[i].unitCheck = createCheckButton(anaComp,
						checks[i].buttonText);
				checks[i].unitCheck.setToolTipText(checks[i].toolText);
				checks[i].unitCheck.addSelectionListener(listener);

				/*
				 * Put tauinc box below MPI box
				 * @author raportil
				 */
				if (i == mpiIndex) {
					runTauinc = createCheckButton(anaComp, "Generate MPI include list");
					runTauinc.addSelectionListener(listener);
				}

			} else {
				papiComp = new Composite(anaComp, SWT.NONE);
				papiComp.setLayout(createGridLayout(5, false, 0, 0));
				papiComp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
				checks[i].unitCheck = createCheckButton(papiComp,
						checks[i].buttonText);
				checks[i].unitCheck.setToolTipText(checks[i].toolText);
				checks[i].unitCheck.addSelectionListener(listener);
				papiSelect = createPushButton(papiComp, "Select PAPI Counters",
						null);
				papiSelect
				.setToolTipText("Set PAPI COUNTER environment variables");
				papiSelect.addSelectionListener(listener);
				papiCountRadios = new Button[2];
				papiCountRadios[0] = createRadioButton(papiComp,
				"Preset Counters");
				papiCountRadios[1] = createRadioButton(papiComp,
				"Native Counters");
//				papiCountRadios[2] = createRadioButton(papiComp,
//				"PAPI-C");
			}
		}
		/*
		 * Composite comComp=new Composite(parallelComp,SWT.NONE);
		 * comComp.setLayout(createGridLayout(2, false, 0, 0));
		 * comComp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL)); Label
		 * compLab = new Label(comComp, 0); compLab.setText("Using Compiler:");
		 * compiler = new Text(comComp, SWT.BORDER | SWT.SINGLE);
		 * compiler.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		 * compiler.addModifyListener(listener);
		 */

		Composite makeComp = new Composite(anaComp, SWT.NONE);
		makeComp.setLayout(createGridLayout(2, false, 0, 0));
		makeComp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		Label makeLab = new Label(makeComp, 0);
		makeLab.setText("Select Makefile:");
		makecombo = new Combo(makeComp, SWT.DROP_DOWN | SWT.READ_ONLY
				| SWT.BORDER);
		makecombo.addSelectionListener(listener);

		anaComp.pack();
		int anaCompHeight=anaComp.computeSize(SWT.DEFAULT, SWT.DEFAULT).y;

		scrollAna.setContent(anaComp);
		scrollAna.setMinSize(400, anaCompHeight);
		scrollAna.setExpandHorizontal(true);
		scrollAna.setExpandVertical(true);

		/*
		 * 
		 * TAU Compiler:  TAU Compiler options
		 * 
		 * */

//		tauOpts.encloseOpts="\'";
//		tauOpts.prependOpts="-tau_options=";
//		tauOpts.separateOpts=" ";

		TabItem optTab = new TabItem(tabParent, SWT.NULL);
		optTab.setText(tauOpts.toolName.trim());

		ScrolledComposite scrollOpt = new ScrolledComposite(tabParent,
				SWT.V_SCROLL);

		Composite optComp = new Composite(scrollOpt, SWT.NONE);

		optTab.setControl(scrollOpt);

		/*
		 * The actual controls of optComp
		 * */

		tauOpts.makeToolPane(optComp, new TauPaneListener(tauOpts));

		optComp.pack();
		int optCompHeight=optComp.computeSize(SWT.DEFAULT, SWT.DEFAULT).y;
		scrollOpt.setContent(optComp);
		scrollOpt.setMinSize(400, optCompHeight);
		scrollOpt.setExpandHorizontal(true);
		scrollOpt.setExpandVertical(true);

		/*
		 * 
		 * Selective Instrumentation
		 * 
		 * */
		TabItem selinstTab = new TabItem(tabParent, SWT.NULL);
		selinstTab.setText("Selective Instrumentation");

		Composite selinstComp = new Composite(tabParent, SWT.NONE);
		selinstTab.setControl(selinstComp);

		selinstComp.setLayout(createGridLayout(1, false, 0, 0));
		selinstComp.setLayoutData(spanGridData(GridData.FILL_HORIZONTAL, 5));

		/*
		 * The actual controls of selinstTab
		 * */
		createVerticalSpacer(selinstComp, 1);

		selectRadios = new Button[4];

		selectRadios[0] = createRadioButton(selinstComp, "None");
		selectRadios[0].setToolTipText("Do not use selective instrumentation.");
		selectRadios[1] = createRadioButton(selinstComp, "Internal");
		selectRadios[1]
		             .setToolTipText("Use the selective instrumentation file generated"
		            		 + " by selective instrumentation commands in the workspace.");
		selectRadios[2] = createRadioButton(selinstComp, "User Defined");
		selectRadios[2]
		             .setToolTipText("Specify a pre-existing selective instrumentation "
		            		 + "file.");

		selComp = new Composite(selinstComp, SWT.NONE);
		selComp.setLayout(createGridLayout(2, false, 0, 0));
		selComp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		tauSelectFile = new Text(selComp, SWT.BORDER | SWT.SINGLE);
		tauSelectFile.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		tauSelectFile.addModifyListener(listener);
		browseSelfileButton = createPushButton(selComp, "Browse", null);
		browseSelfileButton.addSelectionListener(listener);

		selectRadios[3] = createRadioButton(selinstComp, "Automatic");
		selectRadios[3].setEnabled(false);
		for (int i = 0; i < selectRadios.length; i++) {
			selectRadios[i].addSelectionListener(listener);
		}

		/*
		 * 
		 * Data Collection: Storage and management of output data
		 * 
		 * */
		TabItem dataTab = new TabItem(tabParent, SWT.NULL);
		dataTab.setText("Data Collection");

		Composite dataComp = new Composite(tabParent, SWT.NONE);
		dataTab.setControl(dataComp);

		dataComp.setLayout(createGridLayout(1, false, 0, 0));
		dataComp.setLayoutData(spanGridData(GridData.FILL_HORIZONTAL, 5));

		/*
		 * The actual controls of dataTab
		 * */
		createVerticalSpacer(dataComp, 1);

//		buildonlyCheck = createCheckButton(dataComp,
//		"Build the instrumented executable but do not launch it");
//		buildonlyCheck.addSelectionListener(listener);
//		noParallelRun=createCheckButton(dataComp,"Auto-select the above for MPI-based makefiles");
//		noParallelRun.addSelectionListener(listener);
//		nocleanCheck = createCheckButton(dataComp,
//		"Keep instrumented executable");
//		nocleanCheck.addSelectionListener(listener);

		Composite dbComp = new Composite(dataComp, SWT.NONE);
		dbComp.setLayout(createGridLayout(2, false, 0, 0));
		dbComp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label dbLab = new Label(dbComp, 0);
		dbLab.setText("Select Database:");

		dbCombo = new Combo(dbComp, SWT.DROP_DOWN | SWT.READ_ONLY| SWT.BORDER);
		dbCombo.addSelectionListener(listener);

		keepprofsCheck = createCheckButton(dataComp, "Keep profiles");
		keepprofsCheck.addSelectionListener(listener);

		portalCheck = createCheckButton(dataComp,
		"Upload profile data to TAU Portal");
		portalCheck.addSelectionListener(listener);
	}

	public void updateComboFromSelection() {
		System.out.println("change startup");
	}

	/**
	 * Defaults are empty.
	 * 
	 * @see ILaunchConfigurationTab#setDefaults(ILaunchConfigurationWorkingCopy)
	 */
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(ITAULaunchConfigurationConstants.MPI,
				!noPTP);
		configuration.setAttribute(ITAULaunchConfigurationConstants.TAUINC,
				ITAULaunchConfigurationConstants.TAUINC_DEF);
		configuration.setAttribute(ITAULaunchConfigurationConstants.CALLPATH,
				ITAULaunchConfigurationConstants.CALLPATH_DEF);
		configuration.setAttribute(ITAULaunchConfigurationConstants.MEMORY,
				ITAULaunchConfigurationConstants.MEMORY_DEF);
		configuration.setAttribute(ITAULaunchConfigurationConstants.PAPI,
				ITAULaunchConfigurationConstants.PAPI_DEF);
		configuration.setAttribute(ITAULaunchConfigurationConstants.PERF,
				ITAULaunchConfigurationConstants.PERF_DEF);
		configuration.setAttribute(ITAULaunchConfigurationConstants.TRACE,
				ITAULaunchConfigurationConstants.TRACE_DEF);
		configuration.setAttribute(ITAULaunchConfigurationConstants.PHASE,
				ITAULaunchConfigurationConstants.PHASE_DEF);
		configuration.setAttribute(ITAULaunchConfigurationConstants.COMPILER,
				ITAULaunchConfigurationConstants.COMPILER_DEF);

		configuration.setAttribute(ITAULaunchConfigurationConstants.EPILOG,
				ITAULaunchConfigurationConstants.EPILOG_DEF);
		configuration.setAttribute(
				ITAULaunchConfigurationConstants.VAMPIRTRACE,
				ITAULaunchConfigurationConstants.VAMPIRTRACE_DEF);

//		configuration.setAttribute(IPerformanceLaunchConfigurationConstants.NOCLEAN,
//		IPerformanceLaunchConfigurationConstants.NOCLEAN_DEF);
		configuration.setAttribute(ITAULaunchConfigurationConstants.KEEPPROFS,
				ITAULaunchConfigurationConstants.KEEPPROFS_DEF);

		configuration.setAttribute(ITAULaunchConfigurationConstants.SELECT, 0);
		configuration.setAttribute(
				ITAULaunchConfigurationConstants.SELECT_FILE, "");

		configuration.setAttribute(ITAULaunchConfigurationConstants.ENVVARS,
				(Map<String, Object>) null);

		configuration.setAttribute(ITAULaunchConfigurationConstants.TAU_MAKEFILE,
		"");

		tauOpts.setDefaults(configuration);
	}

	/**
	 * @see ILaunchConfigurationTab#initializeFrom(ILaunchConfiguration)
	 */
	public void initializeFrom(ILaunchConfiguration configuration) {
		try {

			selopts = new LinkedHashSet<String>();

			initMakefiles();
			initMakeChecks();

			for (int i = 0; i < checks.length; i++) {
				checks[i].unitCheck.setSelection(configuration.getAttribute(
						checks[i].confString, checks[i].defState));
				if (checks[i].unitCheck.getSelection()
						&& checks[i].unitCheck.getEnabled()) {
					selopts.add(checks[i].makeCmd);
				}
			}

			/*
			 *If MPI box checked and enabled, enable tauinc box. Else, disable tauinc box
			 *@author raportil
			 */
			if (checks[mpiIndex].unitCheck.getSelection()&& checks[callpathIndex].unitCheck.getSelection()&& checks[mpiIndex].unitCheck.getEnabled()) {
				runTauinc.setEnabled(true);
			} else {
				runTauinc.setEnabled(false);
			}
			runTauinc.setSelection(configuration.getAttribute(ITAULaunchConfigurationConstants.TAUINC, false));

			tauOpts.OptUpdate();

			tauOpts.initializePane(configuration);

			int pcr=configuration.getAttribute(
					ITAULaunchConfigurationConstants.PAPISELECT, 0);
			if(pcr>1)pcr=0;
			papiCountRadios[pcr]
					.setSelection(true);

			selmakefile = configuration.getAttribute(
					ITAULaunchConfigurationConstants.TAU_MAKENAME, (String) null);

			initMakeCombo();
			reinitMakeChecks();
			int selected = configuration.getAttribute(
					ITAULaunchConfigurationConstants.SELECT, 0);

			selectRadios[selected].setSelection(true);

			tauSelectFile.setText(configuration.getAttribute(
					ITAULaunchConfigurationConstants.SELECT_FILE, ""));

			if (!selectRadios[2].getSelection()) {
				selComp.setEnabled(false);
				tauSelectFile.setEnabled(false);
				tauSelectFile.setEnabled(false);
			}

//			buildonlyCheck.setSelection(configuration.getAttribute(
//			IPerformanceLaunchConfigurationConstants.BUILDONLY, false));
//			noParallelRun.setSelection(configuration.getAttribute(
//			ITAULaunchConfigurationConstants.NOPARRUN, noPTP));

//			if(noParallelRun.getSelection()&&selmakefile.indexOf("-mpi")>0)
//			{
//			buildonlyCheck.setSelection(true);
//			}

//			nocleanCheck.setSelection(configuration.getAttribute(
//			IPerformanceLaunchConfigurationConstants.NOCLEAN, false));

			initDBCombo(configuration.getAttribute(ITAULaunchConfigurationConstants.PERFDMF_DB, (String)null));

			keepprofsCheck.setSelection(configuration.getAttribute(
					ITAULaunchConfigurationConstants.KEEPPROFS, false));

			portalCheck.setSelection(configuration.getAttribute(
					ITAULaunchConfigurationConstants.PORTAL, false));

			varmap = archvarmap = configuration.getAttribute(
					ITAULaunchConfigurationConstants.ENVVARS, (Map<String,Object>) null);

			Activator.getDefault().getPluginPreferences().setDefault("TAUCheckForAutoOptions",true);

		} catch (CoreException e) {
			setErrorMessage("Core Exception while initializing Analysis tab: "
					+ e.getMessage());
		}
	}

	private void initDBCombo(String selected)
	{
		String[] dbs = PerfDMFView.getDatabaseNames();

		dbCombo.clearSelection();
		dbCombo.removeAll();
		if(dbs==null||dbs.length<1)
		{
			dbCombo.add(ITAULaunchConfigurationConstants.NODB);
			dbCombo.select(0);
			return;
		}

		for(int i=0;i<dbs.length;i++)
		{
			dbCombo.add(dbs[i]);
			//System.out.println(dbs[i]);
		}

		if(selected==null||dbCombo.indexOf(selected)<0)
			dbCombo.select(0);
		else
			dbCombo.select(dbCombo.indexOf(selected));
	}

	/**
	 * @see ILaunchConfigurationTab#performApply(ILaunchConfigurationWorkingCopy)
	 */
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		for (int i = 0; i < checks.length; i++) {
			configuration.setAttribute(checks[i].confString,
					checks[i].unitCheck.getSelection());
		}

		configuration.setAttribute(ITAULaunchConfigurationConstants.TAUINC,runTauinc.getSelection());

		tauOpts.performApply(configuration);

		configuration.setAttribute(tauOpts.configID, tauOpts.getOptionString());

//		configuration.setAttribute(IPerformanceLaunchConfigurationConstants.BUILDONLY,
//		buildonlyCheck.getSelection());
//		configuration.setAttribute(ITAULaunchConfigurationConstants.NOPARRUN,
//		noParallelRun.getSelection());
//		configuration.setAttribute(IPerformanceLaunchConfigurationConstants.NOCLEAN,
//		nocleanCheck.getSelection());
		configuration.setAttribute(ITAULaunchConfigurationConstants.KEEPPROFS,
				keepprofsCheck.getSelection());
		configuration.setAttribute(ITAULaunchConfigurationConstants.PORTAL,
				portalCheck.getSelection());

		/*
		 * If varmap is null and avm is not... or vm is not null and avm is null... or neither are null and they're equal
		 */
		if (((varmap == null) && (archvarmap != null))
				|| ((varmap != null) && (archvarmap == null))
				|| ((varmap != null) && (archvarmap != null) && !varmap.equals(archvarmap))) {
			Map<String, Object> envvars = null;

			try {
				envvars = configuration.getAttribute(
						ILaunchManager.ATTR_ENVIRONMENT_VARIABLES, (Map<String, Object>) null);
			} catch (CoreException e) {
				e.printStackTrace();
			}

			if ((envvars != null) && (envvars.size() > 0)
					&& (archvarmap != null) && (archvarmap.size() > 0)) {
				Iterator<String> archit = archvarmap.keySet().iterator();
				while (archit.hasNext()) {
					envvars.remove(archit.next());
				}
			}

			if ((varmap != null) && (varmap.size() > 0)) {
				if (envvars == null) {
					envvars = new HashMap<String, Object>();
				}
				envvars.putAll(varmap);
			}

			configuration.setAttribute(
					ILaunchManager.ATTR_ENVIRONMENT_VARIABLES, envvars);
			configuration.setAttribute(
					ITAULaunchConfigurationConstants.ENVVARS, varmap);
		}
		if (papiCountRadios[0].getSelection()) {
			configuration.setAttribute(ITAULaunchConfigurationConstants.PAPISELECT,
					0);
		} else if (papiCountRadios[1].getSelection()){
			configuration.setAttribute(ITAULaunchConfigurationConstants.PAPISELECT,
					1);
		}
//		else if (papiCountRadios[2].getSelection()){
//			configuration.setAttribute(ITAULaunchConfigurationConstants.PAPISELECT,
//					2);
//		}

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

			String selcommand="";
			String selpath="";
			if(selected==1)
			{
				//configuration.getLocation().toOSString();
				selpath=IPerformanceLaunchConfigurationConstants.PROJECT_LOCATION+File.separator+"tau.selective";
				selcommand="-optTauSelectFile="+selpath;
			}
			else
				if(selected==2)
				{
					selpath=tauSelectFile.getText();
					configuration.setAttribute(ITAULaunchConfigurationConstants.SELECT_FILE, selpath);
					if(!selpath.equals(""))
						selcommand="-optTauSelectFile="+selpath;
				}
			configuration.setAttribute(ITAULaunchConfigurationConstants.SELECT_COMMAND,selcommand);
			configuration.setAttribute(ITAULaunchConfigurationConstants.SELECT,selected);
		}
		String tauMakeName=makecombo.getItem(makecombo.getSelectionIndex());
		configuration.setAttribute(ITAULaunchConfigurationConstants.TAU_MAKENAME, tauMakeName);
		configuration.setAttribute(ITAULaunchConfigurationConstants.TAU_MAKEFILE,"-tau_makefile="+tlpath+File.separator+makecombo.getItem(makecombo.getSelectionIndex()));

		configuration.setAttribute(ITAULaunchConfigurationConstants.PERFDMF_DB, dbCombo.getItem(dbCombo.getSelectionIndex()));

		configuration.setAttribute(IPerformanceLaunchConfigurationConstants.TOOLCONFNAME+"TAU", "_"+tauMakeName.substring(tauMakeName.lastIndexOf(".")+1));
		//.setDefault("TAUCheckForAutoOptions",true);


//		if(noParallelRun.getSelection()&&makecombo.getItem(makecombo.getSelectionIndex()).indexOf("-mpi")>0)
//		{
//		configuration.setAttribute(IPerformanceLaunchConfigurationConstants.BUILDONLY,
//		true);
//		}

	}

	protected String getFieldContent(IntegerFieldEditor editorField) {
		return getFieldContent(editorField.getStringValue());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#isValid
	 * (org.eclipse.debug.core.ILaunchConfiguration)
	 */
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
		dialog.setText("Select TAU Selective Instrumentation File");

		String correctPath = getFieldContent(tauSelectFile.getText());
		if (correctPath != null) {
			File path = new File(correctPath);
			if (path.exists()) {
				dialog.setFilterPath(path.isFile() ? correctPath : path
						.getParent());
			}
		}

		String selectedPath = dialog.open();
		if (selectedPath != null) {
			tauSelectFile.setText(selectedPath);
		}
	}

	/**
	 * Finds the PAPI utilities' location
	 * @return The string representation of the location of the PAPI utilities located in the selected makefile, or the empty string if they are not found
	 * @throws FileNotFoundException if the location is in the makefile but not valid
	 */
	private String getPapiLoc() throws FileNotFoundException {

		String papimake = tlpath + File.separator
		+ makecombo.getItem(makecombo.getSelectionIndex());

		File papimakefile = new File(papimake);
		if (!papimakefile.canRead()) {
			System.out.println("INVALID MAKEFILE FOR PAPI");
		}

		String papiline = "";
		boolean found = false;
		try {
			BufferedReader readmake = new BufferedReader(new FileReader(
					papimakefile));
			papiline = readmake.readLine();
			while (papiline != null) {
				if (papiline.indexOf("PAPIDIR=") == 0) {
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
		}

		if (found) {
			papiline = papiline.substring(papiline.indexOf("=") + 1);
			File papibin = new File(papiline + File.separator + "bin"
					+ File.separator + "papi_event_chooser");
			if (!papibin.canRead()) {
				papibin = new File(papiline + File.separator + "share"
						+ File.separator + "papi" + File.separator + "utils"
						+ File.separator + "papi_event_chooser");
			}

			if (!papibin.canRead()) {
				throw new FileNotFoundException(
				"Could not locate papi utilities");
			}

			papiline = papibin.getParentFile().toString();
		} else {
			System.out.println("No PAPIDIR in Makefile!");
		}

		return papiline;
	}

	/**
	 * Handles launching of the PAPI counter selection dialog.  Places values returned by the dialog in the launch environment variables list
	 *
	 */
	protected void handlePapiSelect() {
		Object[] selected=null;
		try {

			String papiBin=getPapiLoc();
			
			File pdir=new File(papiBin);
			if(!pdir.isDirectory()||!pdir.canRead()){
				return;
			}
			File pcxi=new File(papiBin+File.separator+"papi_xml_event_info");
			

			if(pcxi.exists())//papiCountRadios[2].getSelection())
			{
				//System.out.println(papiBin);
				final String pTool="papi_xml_event_info";
				File pDir=new File(papiBin);
				String[]files=pDir.list(new FilenameFilter(){
					
					public boolean accept(File fi, String name) {
						if(name.equals(pTool))
							return true;
						return false;
					}
				});

				if(files.length<1){
					MessageDialog.openWarning(getShell(),pTool+" Not Found","Please select a different counter selection tool, or select a TAU configuration associated with PAPI version 3.6 or higher.");
					return;
				}

				EventTreeDialog treeD=new EventTreeDialog(getShell(),papiBin);
//				if ((varmap != null) && (varmap.size() > 0)) {
//				treeD.setInitialSelections(varmap.values().toArray());
//				}

				if(treeD.open()==Window.OK)
				{
					selected=treeD.getCommands().toArray();
				}
			}

			else
			{
				LabelProvider papilab = new LabelProvider();
				ArrayContentProvider paprov = new ArrayContentProvider();

				int papiCountType = PapiListSelectionDialog.PRESET;
				if (papiCountRadios[1].getSelection()) {
					papiCountType = PapiListSelectionDialog.NATIVE;
				}
				PapiListSelectionDialog papidialog = new PapiListSelectionDialog(
						getShell(), papiBin, paprov, papilab,
						"Select the PAPI counters to use with TAU", papiCountType);
				papidialog.setTitle("PAPI Counters");
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
			LinkedHashSet<Object> selset = new LinkedHashSet<Object>(Arrays
					.asList(selected));

			String pn="PAPI_NATIVE_";
			String pPre="PAPI_";
			varmap = new HashMap<String, Object>(selset.size());
			varmap.put("COUNTER1", "GET_TIME_OF_DAY");
			Iterator<Object> varit = selset.iterator();
			int counter = 2;
			while (varit.hasNext()) {
				String varTxt=(String) varit.next();
				if(varTxt.indexOf(pPre)!=0)
				{
					varTxt=pn+varTxt;
				}
				varmap.put("COUNTER" + counter, varTxt);
				counter++;
			}

		} else {
			varmap = null;
		}
	}

	/**
	 * @see ILaunchConfigurationTab#getName()
	 */
	public String getName() {
		return "TAU";
	}

	/**
	 * @see ILaunchConfigurationTab#setLaunchConfigurationDialog
	 * (ILaunchConfigurationDialog)
	 */
	public void setLaunchConfigurationDialog(ILaunchConfigurationDialog dialog) {
		super.setLaunchConfigurationDialog(dialog);
	}

	/**
	 * @see ILaunchConfigurationTab#getImage()
	 */
	public Image getImage() {
		return LaunchImages.getImage("org.eclipse.ptp.perf.tau.core.tauLogo.gif");
	}


}