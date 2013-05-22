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
 *    Roland Grunberg - added support for initialization data
 ****************************************************************************/
package org.eclipse.ptp.internal.etfw.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ptp.etfw.IToolLaunchConfigurationConstants;
import org.eclipse.ptp.internal.etfw.messages.Messages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

public class ParametricParameterTab extends AbstractLaunchConfigurationTab implements IToolLaunchConfigurationConstants, IExecutableExtension {

	/**
	 * Listen for activity in the options widgets
	 * 
	 * @author wspear
	 * 
	 */
	protected class WidgetListener extends SelectionAdapter implements ModifyListener, IPropertyChangeListener {
		public void modifyText(ModifyEvent evt) {
			updateLaunchConfigurationDialog();
		}

		public void propertyChange(PropertyChangeEvent event) {
			updateLaunchConfigurationDialog();
		}

		@Override
		public void widgetSelected(SelectionEvent e) {

			updateLaunchConfigurationDialog();
		}
	}

	private static boolean checkNoParTableCounts(Table t, boolean all) {
		final TableItem[] tis = t.getItems();
		int n = -1;
		for (final TableItem ti : tis) {
			if (ti.getChecked() || all) {
				if (n == -1) {
					n = getComArgs(ti.getText(1)).size();
				}
				if (getComArgs(ti.getText(0)).size() > 1 || getComArgs(ti.getText(1)).size() != n) {
					return false;
				}
			}
		}

		return true;
	}

	private static boolean checkTableCounts(Table t, int n, boolean all) {

		final TableItem[] tis = t.getItems();

		for (final TableItem ti : tis) {
			if (ti.getChecked() || all) {
				if (getComArgs(ti.getText(0)).size() > 1 || getComArgs(ti.getText(1)).size() != n) {
					return false;
				}
			}
		}

		return true;
	}

	/**
	 * Returns a new GridLayout
	 * 
	 * @param columns
	 *            Number of columns
	 * @param isEqual
	 * @param mh
	 * @param mw
	 * @return
	 */
	protected static GridLayout createGridLayout(int columns, boolean isEqual, int mh, int mw) {
		final GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = columns;
		gridLayout.makeColumnsEqualWidth = isEqual;
		gridLayout.marginHeight = mh;
		gridLayout.marginWidth = mw;
		return gridLayout;
	}

	/**
	 * Given a string of comma separated strings, returns an array of the
	 * strings
	 * 
	 * @param combined
	 *            The string to be tokenized by commas
	 * @return
	 */
	static List<String> getComArgs(String combined) {
		final StringTokenizer st = new StringTokenizer(combined, ","); //$NON-NLS-1$
		final List<String> numProcesses = new ArrayList<String>();
		while (st.hasMoreTokens()) {
			numProcesses.add(st.nextToken().trim());
		}
		return numProcesses;
	}

	/**
	 * Given two numeric values seperated by a bar, returns a list of all
	 * numeric values in the range.
	 * 
	 * @param combined
	 * @return
	 */
	static List<String> getRangeArgs(String combined) {
		final List<String> numProcesses = new ArrayList<String>();

		final StringTokenizer st = new StringTokenizer(combined, "-"); //$NON-NLS-1$

		if (st.countTokens() == 2) {
			final String from = st.nextToken().trim();
			final String to = st.nextToken().trim();
			final int fromi = Integer.parseInt(from);
			final int toi = Integer.parseInt(to);
			for (int i = fromi; i <= toi; i++) {
				numProcesses.add(i + ""); //$NON-NLS-1$
			}
		}

		return numProcesses;
	}

	private static List<String> getTableChecks(Table table) {
		final List<String> l = new ArrayList<String>();
		final TableItem[] tiA = table.getItems();

		for (final TableItem it : tiA) {
			if (it.getChecked()) {
				l.add("1"); //$NON-NLS-1$
			} else {
				l.add("0"); //$NON-NLS-1$
			}
		}

		return l;
	}

	private static List<String> getTableList(Table table, int index) {
		final List<String> l = new ArrayList<String>();
		final TableItem[] tiA = table.getItems();

		for (final TableItem it : tiA) {
			l.add(it.getText(index));
		}

		return l;
	}

	private static void setTableList(Table ta, List<String> data0, List<String> data1, List<String> checkList) {

		if (data0 == null || data1 == null) {
			return;
		}
		ta.removeAll();
		for (int i = 0; i < data0.size(); i++) {
			final TableItem ti = new TableItem(ta, SWT.NONE);
			ti.setText(0, data0.get(i));
			ti.setText(1, data1.get(i));
			if (checkList.get(i).equals("1")) { //$NON-NLS-1$
				ti.setChecked(true);
			}
		}
	}

	protected static GridData spanGridData(int style, int space) {
		GridData gd = null;
		if (style == -1) {
			gd = new GridData();
		} else {
			gd = new GridData(style);
		}
		gd.horizontalSpan = space;
		return gd;
	}

	private Button useParam;

	private Text processors;

	private Text optLevels;

	// private Table cmpTab;
	private Table argTab;

	private Table varTab;

	private Button allCom;

	// private Text trial;
	private Text script;

	private Button scriptBrowse;

	private boolean parallel = false;

	private final WidgetListener wl = new WidgetListener();

	private static final String weakError = Messages.ParametricParameterTab_25;

	private static final String noParError = Messages.ParametricParameterTab_26;

	public ParametricParameterTab() {
	}

	public ParametricParameterTab(boolean parallel) {
		this.parallel = parallel;
	}

	public void createControl(Composite comp) {

		final ScrolledComposite sparent = new ScrolledComposite(comp, SWT.V_SCROLL);
		setControl(sparent);

		final Composite parent = new Composite(sparent, SWT.NONE);

		// FillLayout fl = new FillLayout();
		// fl.type=SWT.VERTICAL;

		// parent.setLayout(createGridLayout(2, false, 0, 0));
		// parent.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		parent.setLayout(createGridLayout(3, false, 0, 0));
		parent.setLayoutData(spanGridData(GridData.FILL_HORIZONTAL, 5));
		createVerticalSpacer(parent, 3);

		useParam = new Button(parent, SWT.CHECK);
		useParam.setText(Messages.ParametricParameterTab_EnableParametric);
		useParam.addSelectionListener(wl);

		final GridData fill3 = new GridData(GridData.FILL_HORIZONTAL);
		fill3.horizontalSpan = 3;

		Label lab;

		lab = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
		lab.setLayoutData(fill3);

		lab = new Label(parent, SWT.NONE);
		lab.setText(Messages.ParametricParameterTab_OptLevels);
		lab.setToolTipText(Messages.ParametricParameterTab_BlankForDefaultCompWarn);

		optLevels = new Text(parent, SWT.BORDER);
		optLevels.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		optLevels.addModifyListener(wl);

		lab = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
		lab.setLayoutData(fill3);

		String allComText = Messages.ParametricParameterTab_CheckedOneRunPerCombo;

		if (parallel) {
			lab = new Label(parent, SWT.NONE);
			lab.setText(Messages.ParametricParameterTab_MPIProcesses);

			processors = new Text(parent, SWT.BORDER);
			processors.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			processors.addModifyListener(wl);

			lab = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
			lab.setLayoutData(fill3);

			allComText = Messages.ParametricParameterTab_CheckToRunEachCombo;
		}

		// cmpTab=makeArgTable(parent,"Flags","Variables");//TODO: Make a
		// compiler table
		allCom = new Button(parent, SWT.CHECK);
		allCom.setText(Messages.ParametricParameterTab_RunJobsForAllCombos);
		allCom.setToolTipText(allComText);
		allCom.addSelectionListener(wl);
		// simWeak.setLayoutData(fill3);

		lab = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
		lab.setLayoutData(fill3);

		argTab = makeArgTable(parent, Messages.ParametricParameterTab_Name, Messages.ParametricParameterTab_Values,
				Messages.ParametricParameterTab_AppArgs);

		lab = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
		lab.setLayoutData(fill3);

		varTab = makeArgTable(parent, Messages.ParametricParameterTab_Name, Messages.ParametricParameterTab_Values,
				Messages.ParametricParameterTab_EnvVars);

		lab = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
		lab.setLayoutData(fill3);

		// lab=new Label(parent,SWT.NONE);
		// lab.setText("Trial ID");
		// trial=new Text(parent,SWT.BORDER);
		// trial.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		// new Label(parent,SWT.NONE);

		lab = new Label(parent, SWT.NONE);
		lab.setText(Messages.ParametricParameterTab_AnalysisApp);
		script = new Text(parent, SWT.BORDER);
		script.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		script.addModifyListener(wl);
		scriptBrowse = new Button(parent, SWT.NONE);
		scriptBrowse.setText(Messages.ParametricParameterTab_Browse);
		scriptBrowse.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				final FileDialog fd = new FileDialog(parent.getShell());
				fd.setText(Messages.ParametricParameterTab_SelectPerfExScript);
				final String s = fd.open();
				if (s != null) {
					script.setText(s);
				}
			}

		});

		final int thisHeight = parent.computeSize(SWT.DEFAULT, SWT.DEFAULT).y;
		sparent.setMinSize(400, thisHeight);
		sparent.setExpandHorizontal(true);
		sparent.setExpandVertical(true);
		sparent.setContent(parent);
	}

	public String getName() {
		return Messages.ParametricParameterTab_ParametricStudy;
	}

	@SuppressWarnings("unchecked")
	public void initializeFrom(ILaunchConfiguration configuration) {

		try {
			if (parallel) {
				processors.setText(configuration.getAttribute(PARA_NUM_PROCESSORS, "1")); //$NON-NLS-1$

			}
			allCom.setSelection(configuration.getAttribute(PARA_ALL_COMBO, false));
			optLevels.setText(configuration.getAttribute(PARA_OPT_LEVELS, "")); //$NON-NLS-1$
			setTableList(argTab, configuration.getAttribute(PARA_ARG_NAMES, (List<String>) null),
					configuration.getAttribute(PARA_ARG_VALUES, (List<String>) null),
					configuration.getAttribute(PARA_ARG_BOOLS, (List<String>) null));
			setTableList(varTab, configuration.getAttribute(PARA_VAR_NAMES, (List<String>) null),
					configuration.getAttribute(PARA_VAR_VALUES, (List<String>) null),
					configuration.getAttribute(PARA_VAR_BOOLS, (List<String>) null));

			useParam.setSelection(configuration.getAttribute(PARA_USE_PARAMETRIC, false));

			script.setText(configuration.getAttribute(PARA_PERF_SCRIPT, "")); //$NON-NLS-1$

		} catch (final CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	public boolean isValid(ILaunchConfiguration config) {

		setErrorMessage(null);
		setMessage(null);

		super.isValid(config);

		boolean ok = true;
		final boolean all = allCom.getSelection();
		if (parallel) {
			final int numProcArgs = getComArgs(processors.getText()).size();

			ok = checkTableCounts(argTab, numProcArgs, !all);
			ok &= checkTableCounts(varTab, numProcArgs, !all);

			if (!ok) {
				setErrorMessage(weakError);
			}
		} else {
			ok = checkNoParTableCounts(argTab, !all);
			ok &= checkNoParTableCounts(varTab, !all);
			if (!ok) {
				setErrorMessage(noParError);
			}
		}

		return ok;
	}

	private Table makeArgTable(Composite suParent, String c1, String c2, String title) {

		final Group parent = new Group(suParent, SWT.NONE);
		parent.setLayout(createGridLayout(3, false, 0, 0));
		parent.setLayoutData(spanGridData(GridData.FILL_HORIZONTAL, 5));
		parent.setText(title);

		final Table argTab = new Table(parent, SWT.BORDER | SWT.V_SCROLL | SWT.MULTI | SWT.CHECK);// SWT.FULL_SELECTION|
		argTab.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				if (e.detail == SWT.CHECK) {
					updateLaunchConfigurationDialog();
				}

			}

		});

		final GridData tableGD = new GridData(GridData.FILL_HORIZONTAL);
		tableGD.heightHint = 80;
		tableGD.horizontalSpan = 3;
		argTab.setLayoutData(tableGD);

		final GridData span2 = new GridData(GridData.FILL_HORIZONTAL);
		span2.horizontalSpan = 2;

		TableColumn tc = new TableColumn(argTab, SWT.NONE);
		tc.setText(c1);
		tc.setWidth(80);

		tc = new TableColumn(argTab, SWT.NONE);
		tc.setText(c2);
		tc.setWidth(80);

		argTab.setHeaderVisible(true);

		final TableEditor te = new TableEditor(argTab);
		te.horizontalAlignment = SWT.LEFT;
		te.grabHorizontal = true;
		te.minimumWidth = 50;

		// TODO: Eventually enable line-editing
		// argTab.addSelectionListener(new SelectionAdapter(){
		// public void widgetSelected(SelectionEvent e){
		// final int selCol=1;//e.x;//TODO: This x coord may not work
		//
		// Control oldE=te.getEditor();
		// if(oldE!=null){
		// oldE.dispose();
		// }
		// TableItem ti=(TableItem)e.item;
		//
		//
		// if(ti==null){
		// return;
		// }
		//
		// Text iEd=new Text(argTab,SWT.NONE);
		// iEd.setText(ti.getText(selCol));
		// iEd.addModifyListener(new ModifyListener(){
		// public void modifyText(ModifyEvent me){
		// Text text=(Text)te.getEditor();
		// te.getItem().setText(selCol,text.getText());
		// }
		// });
		// iEd.selectAll();
		// iEd.setFocus();
		// te.setEditor(iEd,ti,selCol);
		// }
		// });

		Label lab = new Label(parent, SWT.NONE);
		lab.setText(c1);
		final Text flags = new Text(parent, SWT.BORDER);
		flags.setLayoutData(span2);

		lab = new Label(parent, SWT.NONE);
		lab.setText(c2);
		final Text vars = new Text(parent, SWT.BORDER);
		vars.setLayoutData(span2);

		final Button addVar = new Button(parent, SWT.None);
		addVar.setText(Messages.ParametricParameterTab_Add);
		addVar.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {

				final TableItem it = new TableItem(argTab, SWT.NONE);

				it.setText(0, flags.getText());
				it.setText(1, vars.getText());
				updateLaunchConfigurationDialog();
			}

		});

		final Button removeVar = new Button(parent, SWT.None);
		removeVar.setText(Messages.ParametricParameterTab_Remove);
		removeVar.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				if (argTab.getSelectionIndices().length > 0) {
					argTab.remove(argTab.getSelectionIndices());
					updateLaunchConfigurationDialog();
				}
			}

		});
		new Label(parent, SWT.NULL);

		return argTab;
	}

	public void performApply(ILaunchConfigurationWorkingCopy configuration) {

		if (parallel) {
			configuration.setAttribute(PARA_NUM_PROCESSORS, processors.getText());

		}
		configuration.setAttribute(PARA_ALL_COMBO, allCom.getSelection());
		// else
		// {
		// configuration.setAttribute(PARA_NUM_PROCESSORS, "not_parallel");
		// }
		configuration.setAttribute(PARA_OPT_LEVELS, optLevels.getText());
		configuration.setAttribute(PARA_ARG_NAMES, getTableList(argTab, 0));
		configuration.setAttribute(PARA_ARG_VALUES, getTableList(argTab, 1));
		configuration.setAttribute(PARA_ARG_BOOLS, getTableChecks(argTab));

		configuration.setAttribute(PARA_VAR_NAMES, getTableList(varTab, 0));
		configuration.setAttribute(PARA_VAR_VALUES, getTableList(varTab, 1));
		configuration.setAttribute(PARA_VAR_BOOLS, getTableChecks(varTab));

		configuration.setAttribute(PARA_USE_PARAMETRIC, useParam.getSelection());

		configuration.setAttribute(PARA_PERF_SCRIPT, script.getText());
	}

	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {

	}

	public void setInitializationData(IConfigurationElement config,
			String propertyName, Object data) throws CoreException {
		if (data != null) {
			Map<String, String> parameters = (Map<String, String>) data;
			parallel = Boolean.valueOf(parameters.get("parallel")); //$NON-NLS-1$
		}
	}

}
