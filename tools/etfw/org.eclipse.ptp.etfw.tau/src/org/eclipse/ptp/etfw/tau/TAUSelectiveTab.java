package org.eclipse.ptp.etfw.tau;

import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.internal.ui.SWTFactory;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ptp.etfw.tau.messages.Messages;
import org.eclipse.ptp.etfw.toolopts.IToolUITab;
import org.eclipse.ptp.etfw.toolopts.ToolPaneListener;
import org.eclipse.ptp.etfw.toolopts.ToolsOptionsConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class TAUSelectiveTab implements IToolUITab {

	/**
	 * Listen for activity in the TAU makefile combo-box, CheckItem widgets or other options
	 * 
	 * @author wspear
	 * 
	 */
	protected class WidgetListener extends SelectionAdapter implements ModifyListener, IPropertyChangeListener {
		public void modifyText(ModifyEvent evt) {
			final Object source = evt.getSource();
			if (source == tauSelectFile) {
				if (selectRadios[2].getSelection()) {
					// selectOpt.setArg(tauSelectFile.getText());
					selectFieldVal = tauSelectFile.getText();
				}
			}
			paneListener.widgetSelected(null);
		}

		public void propertyChange(PropertyChangeEvent event) {
			paneListener.widgetSelected(null);
		}

		@Override
		public void widgetSelected(SelectionEvent e) {

			final Object source = e.getSource();

			if (source == browseSelfileButton) {
				handleSelfileBrowseButtonSelected();
			}

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
			paneListener.widgetSelected(null);

		}
	}

	private final static String UNIX_SLASH = "/";

	/**
	 * Produces a new GridLayout based on provided arguments
	 * 
	 * @param columns
	 * @param isEqual
	 * @param mh
	 * @param mw
	 * @return
	 */
	protected static GridLayout createGridLayout(int columns, boolean isEqual, int mh,
			int mw) {
		final GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = columns;
		gridLayout.makeColumnsEqualWidth = isEqual;
		gridLayout.marginHeight = mh;
		gridLayout.marginWidth = mw;
		return gridLayout;
	}

	protected Composite selComp;

	protected Label selectLabel;

	protected Button selectRadios[];

	protected Text tauSelectFile = null;
	protected Button browseSelfileButton = null;
	// ToolOption selectOpt = null;
	private static final String selFileValConf = "Tau Compiler.performance.options.configuration_id_-OPTTAUSELECTFILE_ARGUMENT_SAVED";
	private static final String selFileButtonConf = "Tau Compiler.performance.options.configuration_id_-OPTTAUSELECTFILE_BUTTON_STATE";

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

	String selectFieldVal = null;

	boolean selectFieldChecked = false;

	protected WidgetListener listener = new WidgetListener();

	private ToolPaneListener paneListener;

	Composite fComp;

	public TAUSelectiveTab() {
	}

	/**
	 * Creates and returns a new push button with the given
	 * label and/or image.
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
	 * Creates and returns a new radio button with the given
	 * label and/or image.
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
		return "TAU_SELECTIVE_SELECTION_PANE";
	}

	public String getConfigVarID() {
		return "TAU_SELECTIVE_SELECTION_PANE_VARS";
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
		return "Selective Instrumentation";
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

	/**
	 * Launches a file selection dialog to select a selective instrumentation file
	 * 
	 */
	protected void handleSelfileBrowseButtonSelected() {
		final FileDialog dialog = new FileDialog(getShell());
		dialog.setText(Messages.TAUAnalysisTab_SelectTauSelInstFile);

		final String correctPath = getFieldContent(tauSelectFile.getText());
		if (correctPath != null) {
			// IFileStore path = blt.getFile(correctPath);//new File(correctPath);
			// if (path.fetchInfo().exists()) {
			// dialog.setFilterPath(!path.fetchInfo().isDirectory() ? correctPath : path
			// .getParent());
			// } //TODO: Support a starting path for selective instrumentation files on the remote and/or local machine.
		}

		final String selectedPath = dialog.open();
		if (selectedPath != null) {
			tauSelectFile.setText(selectedPath);
		}
	}

	public void initializePane(ILaunchConfiguration configuration)
			throws CoreException {
		final int selected = configuration.getAttribute(ITAULaunchConfigurationConstants.SELECT, 0);

		selectRadios[selected].setSelection(true);

		tauSelectFile.setText(configuration.getAttribute(selFileValConf, "")); //$NON-NLS-1$  //configuration.getAttribute(ITAULaunchConfigurationConstants.SELECT_FILE, "")

		if (!selectRadios[2].getSelection()) {
			selComp.setEnabled(false);
			tauSelectFile.setEnabled(false);
			tauSelectFile.setEnabled(false);
		}

		selectFieldVal = configuration.getAttribute(selFileValConf, "");
		selectFieldChecked = configuration.getAttribute(selFileButtonConf, false);

	}

	public boolean isEmbedded() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isVirtual() {
		// TODO Auto-generated method stub
		return false;
	}

	public void makeToolPane(Composite comp) {
		makeToolPane(comp, null);// TODO: This will fail.

	}

	public void makeToolPane(Composite selinstComp, ToolPaneListener paneListener) {

		fComp = selinstComp;
		this.paneListener = paneListener;

		/*
		 * 
		 * Selective Instrumentation
		 */
		// TabItem selinstTab = new TabItem(tabParent, SWT.NULL);
		// selinstTab.setText(Messages.TAUAnalysisTab_SelectiveInstrumentation);

		// Composite selinstComp = new Composite(tabParent, SWT.NONE);
		// selinstTab.setControl(selinstComp);

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
		for (final Button selectRadio : selectRadios) {
			selectRadio.addSelectionListener(listener);
		}

	}

	public void OptUpdate() {
		// TODO Auto-generated method stub

	}

	public void performApply(ILaunchConfigurationWorkingCopy configuration) {

		int selected = 0;
		for (int i = 0; i < selectRadios.length; i++) {
			if (selectRadios[i].getSelection()) {
				selected = i;
				break;
			}
		}

		if (selected == 3) {
			configuration.setAttribute(ITAULaunchConfigurationConstants.TAU_REDUCE, true);
		} else {
			configuration.setAttribute(ITAULaunchConfigurationConstants.TAU_REDUCE, false);
		}
		configuration.setAttribute(ITAULaunchConfigurationConstants.SELECT, selected);

		configuration.setAttribute(selFileValConf, selectFieldVal);
		configuration.setAttribute(selFileButtonConf, selectFieldChecked);
	}

	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(ITAULaunchConfigurationConstants.SELECT, 0);
		configuration.setAttribute(ITAULaunchConfigurationConstants.SELECT_FILE, ""); //$NON-NLS-1$

	}

	public void updateOptDisplay() {
		// TODO Auto-generated method stub

	}

	public boolean updateOptField(Object source) {
		// TODO Auto-generated method stub
		return false;
	}

}
