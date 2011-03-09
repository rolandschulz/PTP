/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.jaxb.ui.dialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.ptp.rm.jaxb.core.IJAXBResourceManagerConfiguration;
import org.eclipse.ptp.rm.jaxb.ui.IJAXBUINonNLSConstants;
import org.eclipse.ptp.rm.jaxb.ui.messages.Messages;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewPart;

public class ConfigurationChoiceDialog extends Dialog implements IJAXBUINonNLSConstants {

	private ConfigurationChoiceContainer container;
	private final IJAXBResourceManagerConfiguration config;
	private final IMemento memento;
	private String choice;
	private boolean preset;

	public ConfigurationChoiceDialog(IViewPart part, IJAXBResourceManagerConfiguration config) {
		super(part.getSite().getShell());
		this.config = config;
		this.memento = null;
		choice = ZEROSTR;
		preset = true;
	}

	public ConfigurationChoiceDialog(IViewPart part, IMemento memento) {
		super(part.getSite().getShell());
		this.config = null;
		this.memento = memento;
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
				String selected = getSelected();
				if (selected == null) {
					selected = ZEROSTR;
				}
				setChoice(selected);
				setPreset(choiceIsPreset());
			}
		};
		container.setConfig(config);
		container.setMemento(memento);
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
