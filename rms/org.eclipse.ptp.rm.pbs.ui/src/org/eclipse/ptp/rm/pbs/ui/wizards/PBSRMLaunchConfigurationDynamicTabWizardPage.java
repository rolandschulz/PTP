/*******************************************************************************
 * Copyright (c) 2010 University of Illinois 
 * All rights reserved. This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 *                  - modifications 05/11/2010
 ******************************************************************************/
package org.eclipse.ptp.rm.pbs.ui.wizards;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.ptp.core.attributes.BooleanAttribute;
import org.eclipse.ptp.core.attributes.IAttribute;
import org.eclipse.ptp.core.attributes.IntegerAttribute;
import org.eclipse.ptp.rm.pbs.ui.PBSUIPlugin;
import org.eclipse.ptp.rm.pbs.ui.data.AttributePlaceholder;
import org.eclipse.ptp.rm.pbs.ui.data.PBSBatchScriptTemplate;
import org.eclipse.ptp.rm.pbs.ui.messages.Messages;
import org.eclipse.ptp.rm.pbs.ui.utils.ConfigUtils;
import org.eclipse.ptp.rm.pbs.ui.utils.WidgetUtils;
import org.eclipse.ptp.rm.ui.launch.RMLaunchConfigurationDynamicTabWidgetListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.osgi.framework.Bundle;

/**
 * Used to display the PBS Job Attributes for configuration on the basis of the
 * selected template.
 * 
 * @author arossi
 * 
 */
public class PBSRMLaunchConfigurationDynamicTabWizardPage extends WizardPage {
	private Properties constrained;
	private Composite container;
	private final RMLaunchConfigurationDynamicTabWidgetListener listener;
	private Map<String, AttributePlaceholder> pbsJobAttributes;
	private final Map<Control, AttributePlaceholder> valueWidgets;

	public PBSRMLaunchConfigurationDynamicTabWizardPage(
			Map<Control, AttributePlaceholder> valueWidgets,
			RMLaunchConfigurationDynamicTabWidgetListener listener,
			PBSBatchScriptTemplate template) {
		super(ConfigUtils.EMPTY_STRING);
		this.valueWidgets = valueWidgets;
		this.listener = listener;
		if (template != null)
			pbsJobAttributes = template.getPbsJobAttributes();
		loadConstraints();
	}

	public void createControl(Composite parent) { // FIXME make me scroll!
		container = WidgetUtils.createAnonymousNonFillingGroup(parent, 3);
		WidgetUtils.createLabel(container,
				Messages.DynamicTabWizardPage_ATTRIBUTE, SWT.LEFT, 1)
				.setForeground(WidgetUtils.DKMG);
		WidgetUtils.createLabel(container, Messages.DynamicTabWizardPage_VALUE,
				SWT.LEFT, 1).setForeground(WidgetUtils.DKMG);
		WidgetUtils.createLabel(container,
				Messages.DynamicTabWizardPage_DESCRIPTION, SWT.LEFT, 1)
				.setForeground(WidgetUtils.DKMG);
		if (pbsJobAttributes != null)
			for (Iterator<AttributePlaceholder> i = pbsJobAttributes.values()
					.iterator(); i.hasNext();) {
				AttributePlaceholder ap = i.next();
				if (Messages.PBSAttributeInternalExtension.equals(ap
						.getToolTip()))
					continue;
				IAttribute<?, ?, ?> attr = ap.getAttribute();
				String name = ap.getName();
				String descr = attr.getDefinition().getDescription();
				Label l = WidgetUtils.createLabel(container, name, SWT.LEFT, 1);
				l.setToolTipText(ap.getToolTip());
				valueWidgets.put(
						getValueWidget(container, ap.getDefaultString(), attr),
						ap);
				l = WidgetUtils.createLabel(container, descr, SWT.LEFT, 1);
				l.setForeground(WidgetUtils.DKBL);
			}
		setControl(container);
	}

	/*
	 * Constructs the appropriate widget based on the type of attribute. Only
	 * four mappings implemented here: Integer : Spinner, Boolean : Button,
	 * Constrained : Combo, else : Text.
	 */
	private Control getValueWidget(Composite container, String defaultString,
			IAttribute<?, ?, ?> attr) {
		String value = attr.getValueAsString();
		Control c = null;
		if (attr instanceof BooleanAttribute) {
			Button b = WidgetUtils.createButton(container,
					ConfigUtils.EMPTY_STRING, null, SWT.CHECK, 1, false,
					listener);
			if ("true".equals(value))
				b.setSelection(true);
			c = b;
		} else if (attr instanceof IntegerAttribute)
			/*
			 * For spinners, the default string should represent the minimum
			 * value
			 */
			c = WidgetUtils.createSpinner(container, null,
					Integer.parseInt(defaultString), Integer.MAX_VALUE,
					Integer.parseInt(value), 1, false, listener);
		else {
			String name = attr.getDefinition().getId();
			String constraints = constrained.getProperty(name);
			if (constraints != null) {
				String[] items = constraints.split(",");
				c = WidgetUtils.createItemCombo(container, null, items,
						items[0], ConfigUtils.EMPTY_STRING, false, listener, 1);
			} else
				c = WidgetUtils.createText(container, value, true, listener,
						null);
			c.setForeground(WidgetUtils.DKRD);
		}
		return c;
	}

	/*
	 * Constructs the mapping of attribute names to constrained string values.
	 */
	private void loadConstraints() {
		Bundle bundle = PBSUIPlugin.getDefault().getBundle();
		URL url = FileLocator.find(bundle, new Path(
				Messages.DynamicTabWizardPage_constraints), null);
		if (url == null)
			return;
		InputStream s = null;
		constrained = new Properties();
		try {
			s = url.openStream();
			constrained.load(s);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				s.close();
			} catch (IOException e) {
			}
		}
	}
}
