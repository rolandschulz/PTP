package org.eclipse.ptp.internal.rdt.sync.cdt.ui;

import org.eclipse.cdt.managedbuilder.language.settings.providers.AbstractBuiltinSpecsDetector;
import org.eclipse.cdt.ui.language.settings.providers.AbstractLanguageSettingProviderOptionPage;
import org.eclipse.cdt.utils.ui.controls.ControlFactory;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.ptp.internal.rdt.sync.cdt.core.SyncXMLFileSettingsProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * Options page for {@link SyncXMLFileSettingsProvider}
 */
public class SyncXMLFileSettingsProviderOptions extends AbstractLanguageSettingProviderOptionPage {
	Composite parent;
	Text XMLFileNameTextBox;

	@Override
	public void createControl(Composite comp) {
		parent = comp;
		SyncXMLFileSettingsProvider provider = (SyncXMLFileSettingsProvider) getProvider();
		Composite composite = createCompositeForPageArea(parent);

		Label label = new Label(composite, SWT.NONE);
		GridData gd = new GridData();
		gd.horizontalSpan = 2;
		label.setLayoutData(gd);
		label.setEnabled(parent.isEnabled());
		label.setText("XML file location:"); //$NON-NLS-1$

		XMLFileNameTextBox = new Text(composite, SWT.BORDER);
		gd = new GridData(GridData.FILL, GridData.CENTER, true, false);
		XMLFileNameTextBox.setLayoutData(gd);
		XMLFileNameTextBox.setEnabled(parent.isEnabled());

		String XMLFileName = provider.getXMLFile();
		XMLFileNameTextBox.setText(XMLFileName !=null ? XMLFileName : ""); //$NON-NLS-1$

		createBrowseButton(composite);

		setControl(composite);
	}

	/**
	 * Create composite for the page.
	 * Copy of org.eclipse.cdt.managedbuilder.internal.ui.language.settings.providers.BuiltinSpecsDetectorOptionPage#createCompositeForPageArea
	 */
	private Composite createCompositeForPageArea(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginWidth = 1;
		layout.marginHeight = 1;
		layout.marginRight = 1;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		Dialog.applyDialogFont(composite);

		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		composite.setLayoutData(gd);
		return composite;
	}

	/**
	 * Create "Browse" button.
	 * Mostly copied from org.eclipse.cdt.managedbuilder.internal.ui.language.settings.providers.BuiltinSpecsDetectorOptionPage#createBrowsButton
	 */
	private void createBrowseButton(Composite composite) {
		Button button = ControlFactory.createPushButton(composite, "Browse"); //$NON-NLS-1$
		button.setEnabled(parent.isEnabled());
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent evt) {
				FileDialog dialog = new FileDialog(getShell(), SWT.NONE);
				dialog.setText("Choose file"); //$NON-NLS-1$
				String fileName = XMLFileNameTextBox.getText();
				// taking chance that the first word is a compiler path
				int space = fileName.indexOf(' ');
				if (space > 0) {
					fileName = fileName.substring(0, space);
				}
				IPath folder = new Path(fileName).removeLastSegments(1);
				dialog.setFilterPath(folder.toOSString());
				String chosenFile = dialog.open();
				if (chosenFile != null) {
					XMLFileNameTextBox.insert(chosenFile);
				}
			}
		});
	}
}