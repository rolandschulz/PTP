/******************************************************************************
 * Copyright 2004-2010 The Board of Trustees of the University of Illinois. 
 * All rights reserved.
 * 
 * Contributors:  
 *   Shawn Hampton: original design and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.jaxb.ui.dialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.ptp.rm.jaxb.core.IJAXBNonNLSConstants;
import org.eclipse.ptp.rm.jaxb.core.rm.IJAXBResourceManagerConfiguration;
import org.eclipse.ptp.rm.jaxb.core.xml.JAXBUtils;
import org.eclipse.ptp.rm.jaxb.ui.messages.Messages;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

public class ConfigurationChoiceDialog extends Dialog implements IJAXBNonNLSConstants {

	private ConfigurationChoiceContainer container;
	private final IJAXBResourceManagerConfiguration config;
	private String choice;
	private boolean preset;

	public ConfigurationChoiceDialog(Shell parentShell, IJAXBResourceManagerConfiguration config) {
		super(parentShell);
		this.config = config;
		choice = ZEROSTR;
		preset = true;
	}

	public String getChoice() {
		return choice;
	}

	public boolean isPreset() {
		return preset;
	}

	@Override
	public int open() {
		super.open();
		return getReturnCode();
	}

	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText(Messages.ResourceManagerEditor_title);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);
		container = new ConfigurationChoiceContainer(composite) {
			@Override
			protected void onUpdate() {
				String selected = container.getSelected();
				if (ZEROSTR.equals(selected)) {
					return;
				}
				try {
					JAXBUtils.validate(selected);
				} catch (Throwable t) {
					t.printStackTrace();
					return;
				}
				setChoice(selected);
				setPreset(container.choiceIsPreset());
			}
		};
		container.setConfig(config);
		container.setAvailableConfigurations();
		applyDialogFont(composite);
		return composite;
	}

	private void setChoice(String choice) {
		this.choice = choice;
	}

	private void setPreset(boolean preset) {
		this.preset = preset;
	}
}
