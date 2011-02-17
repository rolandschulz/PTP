/*******************************************************************************
 * Copyright (c) 2010 University of Illinois 
 * All rights reserved. This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 *                  - modifications 05/11/2010
 *                  - modifications to use new converter class; non-nls
 *                    constants interface (09/14/2010); moved the
 *                    queue-name combo functionality into this class from
 *                    the launch tab (5.0) and added parameter to constructor
 ******************************************************************************/
package org.eclipse.ptp.rm.pbs.ui.wizards;

import java.util.Arrays;
import java.util.Map;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.ptp.core.attributes.BooleanAttribute;
import org.eclipse.ptp.core.attributes.IAttribute;
import org.eclipse.ptp.core.attributes.IntegerAttribute;
import org.eclipse.ptp.rm.pbs.core.ConfigUtils;
import org.eclipse.ptp.rm.pbs.core.IPBSNonNLSConstants;
import org.eclipse.ptp.rm.pbs.core.attributes.AttributePlaceholder;
import org.eclipse.ptp.rm.pbs.core.templates.PBSBaseAttributeToTemplateConverter;
import org.eclipse.ptp.rm.pbs.core.templates.PBSBatchScriptTemplate;
import org.eclipse.ptp.rm.pbs.ui.launch.PBSRMLaunchConfigurationDynamicTab;
import org.eclipse.ptp.rm.pbs.ui.messages.Messages;
import org.eclipse.ptp.rm.pbs.ui.utils.WidgetUtils;
import org.eclipse.ptp.rm.ui.launch.RMLaunchConfigurationDynamicTabWidgetListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

/**
 * Used to display the PBS Job Attributes for configuration on the basis of the
 * selected template.
 * 
 * @author arossi
 * 
 */
public class PBSRMLaunchConfigurationDynamicTabWizardPage extends WizardPage implements IPBSNonNLSConstants {
	private Map<String, String[]> constrained;
	private Composite container;
	private final RMLaunchConfigurationDynamicTabWidgetListener listener;
	private Map<String, AttributePlaceholder> pbsJobAttributes;
	private final Map<Control, AttributePlaceholder> valueWidgets;
	private final PBSRMLaunchConfigurationDynamicTab launchTab;

	public PBSRMLaunchConfigurationDynamicTabWizardPage(PBSRMLaunchConfigurationDynamicTab launchTab,
			Map<Control, AttributePlaceholder> valueWidgets, RMLaunchConfigurationDynamicTabWidgetListener listener,
			PBSBatchScriptTemplate template) {
		super(ZEROSTR);
		this.launchTab = launchTab;
		this.valueWidgets = valueWidgets;
		this.listener = listener;
		if (template != null) {
			pbsJobAttributes = template.getPbsJobAttributes();
			try {
				constrained = template.getConverter().getData().getConstrained();
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}
	}

	public void createControl(Composite parent) {
		container = WidgetUtils.createAnonymousNonFillingGroup(parent, 3);
		WidgetUtils.createLabel(container, Messages.DynamicTabWizardPage_ATTRIBUTE, SWT.LEFT, 1).setForeground(WidgetUtils.DKMG);
		WidgetUtils.createLabel(container, Messages.DynamicTabWizardPage_VALUE, SWT.LEFT, 1).setForeground(WidgetUtils.DKMG);
		WidgetUtils.createLabel(container, Messages.DynamicTabWizardPage_DESCRIPTION, SWT.LEFT, 1).setForeground(WidgetUtils.DKMG);
		if (pbsJobAttributes != null) {
			String[] attrNames = pbsJobAttributes.keySet().toArray(new String[0]);
			Arrays.sort(attrNames, PBSBaseAttributeToTemplateConverter.getSorter());
			for (String key : attrNames) {
				AttributePlaceholder ap = pbsJobAttributes.get(key);
				if (TAG_INTERNAL.equals(ap.getToolTip()))
					continue;
				IAttribute<?, ?, ?> attr = ap.getAttribute();
				String name = ap.getName();
				String descr = WidgetUtils.fitToLineLength(-1, attr.getDefinition().getDescription());
				Label l = WidgetUtils.createLabel(container, name, SWT.LEFT, 1);
				l.setToolTipText(WidgetUtils.fitToLineLength(40, ap.getToolTip()));
				valueWidgets.put(getValueWidget(container, ap.getDefaultString(), attr), ap);
				l = WidgetUtils.createLabel(container, descr, SWT.LEFT, 1);
				l.setForeground(WidgetUtils.DKBL);
			}
		}
		setControl(container);
	}

	/*
	 * Constructs the appropriate widget based on the type of attribute. Only
	 * four mappings implemented here: Integer : Spinner, Boolean : Button,
	 * Constrained : Combo, else : Text.
	 */
	private Control getValueWidget(Composite container, String defaultString, IAttribute<?, ?, ?> attr) {
		String value = attr.getValueAsString();
		Control c = null;
		if (attr instanceof BooleanAttribute) {
			Button b = WidgetUtils.createButton(container, ZEROSTR, null, SWT.CHECK, 1, false, listener);
			if (TRUE.equals(value))
				b.setSelection(true);
			c = b;
		} else if (attr instanceof IntegerAttribute) {
			/*
			 * For spinners, the default string should represent the minimum
			 * value
			 */
			IntegerAttribute iattr = (IntegerAttribute) attr;
			c = WidgetUtils.createSpinner(container, null, iattr.getDefinition().getMinValue(),
					iattr.getDefinition().getMaxValue(), Integer.parseInt(value), 1, false, listener);
		} else {
			String name = attr.getDefinition().getName();
			String[] constraints = constrained.get(name);
			if (constraints != null)
				c = WidgetUtils.createItemCombo(container, null, constraints, constraints[0], ZEROSTR, false, listener, 1);
			else if (name.equals(TAG_QUEUE)) {
				String[] items = ConfigUtils.getCurrentQueues(launchTab.getResourceManager());
				String first = items.length == 0 ? null : items[0];
				RMLaunchConfigurationDynamicTabWidgetListener l = launchTab.createDestinationComboListener();
				c = WidgetUtils.createItemCombo(container, null, items, first, ZEROSTR, false, l, 1);
				c.addMouseListener((MouseListener) l);
			} else
				c = WidgetUtils.createText(container, value, true, listener, null);
			c.setForeground(WidgetUtils.DKRD);
		}
		return c;
	}
}
