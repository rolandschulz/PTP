/*******************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation 
 *     Albert L. Rossi (NCSA) - full implementation (bug 310188)
 ******************************************************************************/
package org.eclipse.ptp.rm.pbs.ui.wizards;

import java.util.Iterator;
import java.util.Map;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.ptp.core.attributes.BooleanAttribute;
import org.eclipse.ptp.core.attributes.IAttribute;
import org.eclipse.ptp.core.attributes.IntegerAttribute;
import org.eclipse.ptp.rm.pbs.ui.AttributePlaceholder;
import org.eclipse.ptp.rm.pbs.ui.messages.Messages;
import org.eclipse.ptp.rm.pbs.ui.preferences.PBSPreferencePage;
import org.eclipse.ptp.rm.pbs.ui.utils.WidgetUtils;
import org.eclipse.ptp.rm.ui.launch.RMLaunchConfigurationDynamicTabWidgetListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

/*
 *  Used to display the (dynamically chosen) attributes in the PBS Launch Tab.
 */
public class DynamicTabWizardPage extends WizardPage {
	private static final Color DKBL = Display.getDefault().getSystemColor(SWT.COLOR_DARK_BLUE);
	private static final Color DKMG = Display.getDefault().getSystemColor(SWT.COLOR_DARK_MAGENTA);
	private static final Color DKRD = Display.getDefault().getSystemColor(SWT.COLOR_DARK_RED);

	private final RMLaunchConfigurationDynamicTabWidgetListener listener;
	private final Map<Control, AttributePlaceholder> valueWidgets;

	public DynamicTabWizardPage(Map<Control, AttributePlaceholder> valueWidgets,
			RMLaunchConfigurationDynamicTabWidgetListener listener) {
		super(""); //$NON-NLS-1$
		this.valueWidgets = valueWidgets;
		this.listener = listener;
	}

	public void createControl(Composite parent) {
		Composite container = WidgetUtils.createComposite(parent, 6, 110, 200);
		WidgetUtils.createLabel(container, Messages.DynamicTabWizardPage_ATTRIBUTE, SWT.LEFT, 1).setForeground(DKMG);
		WidgetUtils.createLabel(container, Messages.DynamicTabWizardPage_DESCRIPTION, SWT.LEFT, 2).setForeground(DKMG);
		WidgetUtils.createLabel(container, Messages.DynamicTabWizardPage_TOOLTIP, SWT.LEFT, 2).setForeground(DKMG);
		WidgetUtils.createLabel(container, Messages.DynamicTabWizardPage_VALUE, SWT.LEFT, 1).setForeground(DKMG);
		for (Iterator<AttributePlaceholder> i = PBSPreferencePage.getSelectedAttributes().values().iterator(); i.hasNext();) {
			AttributePlaceholder ap = i.next();
			if (!ap.getChecked())
				continue;
			IAttribute<?, ?, ?> attr = ap.getAttribute();
			String name = ap.getName();
			String descr = attr.getDefinition().getDescription();
			String toolTip = ap.getToolTip();
			WidgetUtils.createLabel(container, name, SWT.LEFT, 1);
			WidgetUtils.createLabel(container, descr, SWT.LEFT, 2).setForeground(DKBL);
			WidgetUtils.createLabel(container, toolTip, SWT.LEFT, 2).setForeground(DKBL);
			valueWidgets.put(getValueWidget(container, attr), ap);
		}
		setControl(container);
	}

	/*
	 * Constructs the appropriate widget based on the type of attribute. Only
	 * three mappings implemented here: Integer : Spinner, Boolean : Button,
	 * else : Text.
	 */
	private Control getValueWidget(Composite container, IAttribute<?, ?, ?> attr) {
		String name = attr.getDefinition().getName();
		String value = attr.getValueAsString();
		Control c = null;
		if (attr instanceof BooleanAttribute) {
			Button b = WidgetUtils.createButton(container, name, null, SWT.CHECK, 1, listener);
			if ("true".equals(value)) //$NON-NLS-1$
				b.setSelection(true);
			c = b;
		} else if (attr instanceof IntegerAttribute) {
			c = WidgetUtils.createSpinner(container, null, 1, Integer.MAX_VALUE, Integer.parseInt(value), 1, listener);
		} else {
			c = WidgetUtils.createText(container, value, listener, null);
		}
		c.setForeground(DKRD);
		return c;
	}
}
