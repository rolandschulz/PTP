package org.eclipse.ptp.ui.preferences;

import org.eclipse.jface.dialogs.StatusDialog;
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.rmsystem.IResourceManager;
import org.eclipse.ptp.rmsystem.IResourceManagerConfiguration;
import org.eclipse.ptp.rmsystem.IResourceManagerFactory;
import org.eclipse.ptp.rmsystem.ResourceManagerConfiguration;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;

/**
 * Dialog to edit a resource manager.
 * 
 */
class ResourceManagerEditDialog extends StatusDialog {

	private final IResourceManager fOriginalResourceManager;
	private IResourceManagerFactory[] fResourceManagerFactories;
	private IResourceManager fNewResourceManager;

	private Text fNameText;
	private Text fDescriptionText;
	private Text fHostText;
	private Text fPortText;
	private Combo fRMCombo;
	private boolean fIsNameModifiable;

//	private StatusInfo fValidationStatus;

	/**
	 * Creates a new dialog.
	 *
	 * @param parent the shell parent of the dialog
	 * @param rm the resource manager to edit
	 * @param edit whether this is a new resource manager or an existing being edited
	 * @param factories the resource manager factories to use
	 */
	public ResourceManagerEditDialog(Shell parent, IResourceManager rm, boolean edit, IResourceManagerFactory[] factories) {
		super(parent);

		setShellStyle(getShellStyle() | SWT.MAX | SWT.RESIZE);

		setTitle(edit ? "Edit ResourceManager" : "New ResourceManager");

		fOriginalResourceManager = rm;

		fResourceManagerFactories = factories;
//		fValidationStatus= new StatusInfo();
	}

	/*
	 * @see org.eclipse.ui.texteditor.targets.StatusDialog#create()
	 */
	public void create() {
		super.create();
		// update initial OK button to be disabled for new targets
		boolean valid= fNameText == null || fNameText.getText().trim().length() != 0;
		if (!valid) {
//			StatusInfo status = new StatusInfo();
//			status.setError(TextEditorResourceManagerMessages.EditResourceManagerDialog_error_noname);
//			updateButtonsEnableState(status);
 		}
	}

	/*
	 * @see Dialog#createDialogArea(Composite)
	 */
	protected Control createDialogArea(Composite ancestor) {
		Composite parent= new Composite(ancestor, SWT.NONE);
		GridLayout layout= new GridLayout();
		layout.numColumns= 2;
		parent.setLayout(layout);
		parent.setLayoutData(new GridData(GridData.FILL_BOTH));

		ModifyListener listener= new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				doTextWidgetChanged(e.widget);
			}
		};

		createLabel(parent, "Name:");

		Composite composite= new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		layout= new GridLayout();
		layout.numColumns= 4;
		layout.marginWidth= 0;
		layout.marginHeight= 0;
		composite.setLayout(layout);

		// Name
		fNameText= createText(composite);
		fNameText.addModifyListener(listener);
		fNameText.addFocusListener(new FocusListener() {

			public void focusGained(FocusEvent e) {
			}

			public void focusLost(FocusEvent e) {
//				if (fSuppressError) {
//					fSuppressError= false;
//					updateButtons();
//				}
			}
		});

		// Resource Manager
		createLabel(composite, "Resource Manager");
		fRMCombo= new Combo(composite, SWT.READ_ONLY);

		for (int i= 0; i < fResourceManagerFactories.length; i++) {
			fRMCombo.add(fResourceManagerFactories[i].getName());
		}
		fRMCombo.addModifyListener(listener);
		
		// Description
		createLabel(parent, "Description");
		int descFlags= fIsNameModifiable ? SWT.BORDER : SWT.BORDER | SWT.READ_ONLY;
		fDescriptionText= new Text(parent, descFlags);
		fDescriptionText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		fDescriptionText.addModifyListener(listener);

		IResourceManagerConfiguration configuration = fOriginalResourceManager.getConfiguration();
		fDescriptionText.setText(configuration.getDescription());
		fNameText.setText(configuration.getName());
		fNameText.addModifyListener(listener);
		fHostText.setText(configuration.getHost());
		fPortText.setText(new Integer(configuration.getPort()).toString());
		fRMCombo.select(getIndex(configuration.getResourceManagerId()));

		applyDialogFont(parent);
		return composite;
	}

	private void doTextWidgetChanged(Widget w) {
//		if (w == fNameText) {
//			fSuppressError= false;
//			updateButtons();
//		} else if (w == fRMCombo) {
//			String contextId= getContextId();
//			fResourceManagerProcessor.setContextType(fContextTypeRegistry.getContextType(contextId));
//		} else if (w == fDescriptionText) {
//			// oh, nothing
//		}
	}

	private String getRMId() {
		if (fRMCombo != null && !fRMCombo.isDisposed()) {
			String name= fRMCombo.getText();
			for (int i= 0; i < fResourceManagerFactories.length; i++) {
				if (name.equals(fResourceManagerFactories[i].getName())) {
					return fResourceManagerFactories[i].getId();
				}
			}
		}

		return fOriginalResourceManager.getConfiguration().getResourceManagerId();
	}

	private static Label createLabel(Composite parent, String name) {
		Label label= new Label(parent, SWT.NULL);
		label.setText(name);
		label.setLayoutData(new GridData());

		return label;
	}

	private static Text createText(Composite parent) {
		Text text= new Text(parent, SWT.BORDER);
		text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		return text;
	}

	private static Button createCheckbox(Composite parent, String name) {
		Button button= new Button(parent, SWT.CHECK);
		button.setText(name);
		button.setLayoutData(new GridData());
		
		return button;
	}

	private int getIndex(String rmId) {

		if (rmId == null)
			return -1;

		for (int i= 0; i < fResourceManagerFactories.length; i++) {
			if (rmId.equals(fResourceManagerFactories[i].getId())) {
				return i;
			}
		}
		return -1;
	}

	private void updateButtons() {
//		StatusInfo status;
//
//		boolean valid= fNameText == null || fNameText.getText().trim().length() != 0;
//		if (!valid) {
//			status = new StatusInfo();
//			if (!fSuppressError) {
//				status.setError(TextEditorResourceManagerMessages.EditResourceManagerDialog_error_noname);
//			}
// 		} else {
// 			status= fValidationStatus;
// 		}
//		updateStatus(status);
	}

	protected void okPressed() {
		String name= fNameText == null ? fOriginalResourceManager.getConfiguration().getName() : fNameText.getText();
		IResourceManagerFactory factory = PTPCorePlugin.getDefault().getResourceManagerFactory(getRMId());
		IResourceManagerConfiguration config = new ResourceManagerConfiguration(name, fDescriptionText.getText(), getRMId(), fHostText.getText(), Integer.parseInt(fPortText.getText()));
		fNewResourceManager= factory.create(config);
		super.okPressed();
	}

	/**
	 * Returns the created target.
	 *
	 * @return the created target
	 */
	public IResourceManager getResourceManager() {
		return fNewResourceManager;
	}

}
