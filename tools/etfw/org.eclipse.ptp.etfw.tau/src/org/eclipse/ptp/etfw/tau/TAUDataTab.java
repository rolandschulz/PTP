package org.eclipse.ptp.etfw.tau;

import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.internal.ui.SWTFactory;
import org.eclipse.ptp.etfw.tau.messages.Messages;
import org.eclipse.ptp.etfw.tau.perfdmf.PerfDMFUIPlugin;
import org.eclipse.ptp.etfw.tau.perfdmf.views.PerfDMFView;
import org.eclipse.ptp.etfw.toolopts.IToolUITab;
import org.eclipse.ptp.etfw.toolopts.ToolPaneListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class TAUDataTab implements IToolUITab {

	/**
	 * Produces a new GridLayout based on provided arguments
	 * 
	 * @param columns
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
	 * Creates a new GridData based on provided style and space arguments
	 * 
	 * @param style
	 * @param space
	 * @return
	 */
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

	protected Combo dbCombo = null;

	protected Button nocleanCheck;

	protected Button keepprofsCheck;

	protected Button profSummaryCheck;

	protected Button portalCheck;

	private ToolPaneListener paneListener;

	Composite fComp;

	public TAUDataTab() {
	}

	/**
	 * Creates and returns a new check button with the given label.
	 * 
	 * @param parent
	 *            the parent composite
	 * @param label
	 *            the button label
	 * @return a new check button
	 * @since 3.0
	 */
	protected Button createCheckButton(Composite parent, String label) {
		return SWTFactory.createCheckButton(parent, label, null, false, 1);
	}

	/**
	 * Creates and returns a new push button with the given label and/or image.
	 * 
	 * @param parent
	 *            parent control
	 * @param label
	 *            button label or <code>null</code>
	 * @param image
	 *            image of <code>null</code>
	 * 
	 * @return a new push button
	 */
	protected Button createPushButton(Composite parent, String label, Image image) {
		return SWTFactory.createPushButton(parent, label, image);
	}

	/**
	 * Creates and returns a new radio button with the given label and/or image.
	 * 
	 * @param parent
	 *            parent control
	 * @param label
	 *            button label or <code>null</code>
	 * 
	 * @return a new radio button
	 */
	protected Button createRadioButton(Composite parent, String label) {
		return SWTFactory.createRadioButton(parent, label);
	}

	/**
	 * Creates vertical space in the parent <code>Composite</code>
	 * 
	 * @param comp
	 *            the parent to add the vertical space to
	 * @param colSpan
	 *            the number of line of vertical space to add
	 */
	protected void createVerticalSpacer(Composite comp, int colSpan) {
		SWTFactory.createVerticalSpacer(comp, colSpan);
	}

	public String getArgument(ILaunchConfiguration configuration) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getConfigID() {
		// TODO Auto-generated method stub
		return "TAU_DATA_MANAGEMENT_PANE";
	}

	public String getConfigVarID() {
		// TODO Auto-generated method stub
		return "TAU_DATA_MANAGEMENT_PANE_VARS";
	}

	public Map<String, String> getEnvVars(ILaunchConfiguration configuration) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Treats empty strings as null
	 * 
	 * @param text
	 * @return Contents of text, or null if text is the empty string
	 */
	protected String getFieldContent(String text) {
		if ((text.trim().length() == 0) || text.equals("")) {
			return null;
		}

		return text;
	}

	public String getName() {
		return "Data Collection";
	}

	public String getOptionString() {
		// TODO Auto-generated method stub
		return "";
	}

	/**
	 * Returns the shell this tab is contained in, or <code>null</code>.
	 * 
	 * @return the shell this tab is contained in, or <code>null</code>
	 */
	protected Shell getShell() {
		// Control control = getControl();
		if (fComp != null) {
			return fComp.getShell();
		}
		return null;
	}

	public String getToolName() {
		return "TAU";
	}

	public Map<String, String> getVarMap() {
		// TODO Auto-generated method stub
		return null;
	}

	private void initDBCombo(String selected) {
		String[] dbs = null;
		try {
			dbs = PerfDMFUIPlugin.getPerfDMFView().getDatabaseNames();
		} catch (final java.lang.NoClassDefFoundError e) {
			System.out.println(Messages.TAUAnalysisTab_WarnTauJarsNotFound);
		}

		dbCombo.clearSelection();
		dbCombo.removeAll();
		if (dbs == null || dbs.length < 1) {
			dbCombo.add(ITAULaunchConfigurationConstants.NODB);
			dbCombo.select(0);
			return;
		}

		for (final String db : dbs) {
			dbCombo.add(db);
			// System.out.println(dbs[i]);
		}

		if (selected == null || dbCombo.indexOf(selected) < 0) {
			dbCombo.select(0);
		} else {
			dbCombo.select(dbCombo.indexOf(selected));
		}
	}

	public void initializePane(ILaunchConfiguration configuration) throws CoreException {
		initDBCombo(configuration.getAttribute(ITAULaunchConfigurationConstants.PERFDMF_DB, (String) null));

		keepprofsCheck.setSelection(configuration.getAttribute(ITAULaunchConfigurationConstants.KEEPPROFS, false));

		profSummaryCheck.setSelection(configuration.getAttribute(ITAULaunchConfigurationConstants.PROFSUMMARY, false));

		portalCheck.setSelection(configuration.getAttribute(ITAULaunchConfigurationConstants.PORTAL, false));

	}

	public boolean isEmbedded() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isVirtual() {
		return false;
	}

	public void makeToolPane(Composite comp) {
		makeToolPane(comp, null);

	}

	public void makeToolPane(Composite dataComp, ToolPaneListener listener) {
		fComp = dataComp;
		/*
		 * 
		 * Data Collection: Storage and management of output data
		 */

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

		final Composite dbComp = new Composite(dataComp, SWT.NONE);
		dbComp.setLayout(createGridLayout(2, false, 0, 0));
		dbComp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		final Label dbLab = new Label(dbComp, 0);
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

	public void OptUpdate() {
		// TODO Auto-generated method stub

	}

	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(ITAULaunchConfigurationConstants.KEEPPROFS, keepprofsCheck.getSelection());

		configuration.setAttribute(ITAULaunchConfigurationConstants.PROFSUMMARY, profSummaryCheck.getSelection());

		configuration.setAttribute(ITAULaunchConfigurationConstants.PORTAL, portalCheck.getSelection());

		final int idex = dbCombo.getSelectionIndex();
		if (idex >= 0) {
			configuration.setAttribute(ITAULaunchConfigurationConstants.PERFDMF_DB, dbCombo.getItem(idex));
			configuration.setAttribute(ITAULaunchConfigurationConstants.PERFDMF_DB_NAME,
					PerfDMFView.extractDatabaseName(dbCombo.getItem(idex)));
		}

	}

	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(ITAULaunchConfigurationConstants.KEEPPROFS, ITAULaunchConfigurationConstants.KEEPPROFS_DEF);

	}

	public void updateOptDisplay() {
		// TODO Auto-generated method stub

	}

	public boolean updateOptField(Object source) {
		// TODO Auto-generated method stub
		return false;
	}
}
