/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation 
 *     Albert L. Rossi (NCSA) - full implementation (bug 310188)
 ******************************************************************************/
package org.eclipse.ptp.rm.pbs.ui.launch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.ptp.core.attributes.IAttribute;
import org.eclipse.ptp.core.attributes.IAttributeDefinition;
import org.eclipse.ptp.core.attributes.IllegalValueException;
import org.eclipse.ptp.core.elements.IPQueue;
import org.eclipse.ptp.core.elements.IResourceManager;
import org.eclipse.ptp.launch.ui.extensions.RMLaunchValidation;
import org.eclipse.ptp.rm.pbs.core.PBSJobAttributes;
import org.eclipse.ptp.rm.pbs.ui.AttributePlaceholder;
import org.eclipse.ptp.rm.pbs.ui.messages.Messages;
import org.eclipse.ptp.rm.pbs.ui.utils.WidgetUtils;
import org.eclipse.ptp.rm.pbs.ui.wizards.DynamicTabWizardPage;
import org.eclipse.ptp.rm.ui.launch.BaseRMLaunchConfigurationDynamicTab;
import org.eclipse.ptp.rm.ui.launch.RMLaunchConfigurationDynamicTabDataSource;
import org.eclipse.ptp.rm.ui.launch.RMLaunchConfigurationDynamicTabWidgetListener;
import org.eclipse.ptp.utils.ui.Activator;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;

/**
 * This tab provides fields for setting the user-determined subset of attribute
 * values. This subset must be configured using the PBS Resource Manager
 * preference page.
 * 
 * @author Albert L. Rossi, NCSA University of Illinois
 */
public class PBSRMLaunchConfigurationDynamicTab extends BaseRMLaunchConfigurationDynamicTab {
	/*
	 * (non-Javadoc) Accesses the parent's map of widgets to attribute
	 * placeholders for the various update functions.
	 */
	class PBSRMLaunchDataSource extends RMLaunchConfigurationDynamicTabDataSource {
		private boolean save = false;

		protected PBSRMLaunchDataSource(BaseRMLaunchConfigurationDynamicTab page) {
			super(page);
		}

		/*
		 * (non-Javadoc) Note that only three types are defined: int, string and
		 * boolean. These are mapped to Spinner, Text and (check) Button
		 * widgets.
		 */
		@Override
		protected void copyFromFields() throws ValidationException {
			toggleSave();
			for (Iterator<Entry<Control, AttributePlaceholder>> i = valueWidgets.entrySet().iterator(); i.hasNext();) {
				Entry<Control, AttributePlaceholder> e = i.next();
				Control c = e.getKey();
				AttributePlaceholder ap = e.getValue();
				Object value = null;
				if (c instanceof Text) {
					value = extractText((Text) c);
				} else if (c instanceof Spinner) {
					value = ((Spinner) c).getSelection();
				} else if (c instanceof Button) {
					value = ((Button) c).getSelection();
				}
				if (value != null)
					try {
						ap.getAttribute().setValueAsString(value.toString());
					} catch (IllegalValueException t) {
						throw new ValidationException(t.toString());
					}
			}
			toggleSave();
		}

		/*
		 * (non-Javadoc) Note that only three types are defined: int, string and
		 * boolean. These are mapped to Spinner, Text and (check) Button
		 * widgets.
		 */
		@Override
		protected void copyToFields() {
			for (Iterator<Entry<Control, AttributePlaceholder>> i = valueWidgets.entrySet().iterator(); i.hasNext();) {
				Entry<Control, AttributePlaceholder> e = i.next();
				Control c = e.getKey();
				AttributePlaceholder ap = e.getValue();
				Object value = ap.getAttribute().getValue();
				if (value != null) {
					if (c instanceof Text) {
						applyText((Text) c, (String) value);
					} else if (c instanceof Spinner) {
						((Spinner) c).setSelection((Integer) value);
					} else if (c instanceof Button) {
						((Button) c).setSelection((Boolean) value);
					}
				}
			}
		}

		/*
		 * (non-Javadoc) Note that only three types are defined: int, string and
		 * boolean.
		 */
		@Override
		protected void copyToStorage() {
			ILaunchConfigurationWorkingCopy config = getConfigurationWorkingCopy();
			for (Iterator<AttributePlaceholder> i = valueWidgets.values().iterator(); i.hasNext();) {
				AttributePlaceholder ap = i.next();
				IAttribute<?, ?, ?> attr = ap.getAttribute();
				String id = attr.getDefinition().getId();
				Object value = attr.getValue();
				if (value instanceof Boolean)
					config.setAttribute(id, (Boolean) value);
				else if (value instanceof Integer)
					config.setAttribute(id, (Integer) value);
				else if (value instanceof String)
					config.setAttribute(id, (String) value);
			}
		}

		/*
		 * (non-Javadoc) NOP for the moment.
		 */
		@Override
		protected void loadDefault() {
		}

		/*
		 * (non-Javadoc) Note that only three types are defined: int, string and
		 * boolean.
		 */
		@Override
		protected void loadFromStorage() {
			ILaunchConfiguration config = getConfigurationWorkingCopy();
			if (config == null) {
				config = getConfiguration();
			}
			if (config == null)
				return;
			for (Iterator<Entry<Control, AttributePlaceholder>> i = valueWidgets.entrySet().iterator(); i.hasNext();) {
				Entry<Control, AttributePlaceholder> e = i.next();
				Control c = e.getKey();
				AttributePlaceholder ap = e.getValue();
				IAttribute<?, ?, ?> attr = ap.getAttribute();
				String id = attr.getDefinition().getId();
				Object value = null;
				try {
					if (c instanceof Text) {
						value = config.getAttribute(id, EMPTY_STRING);
					} else if (c instanceof Button) {
						value = config.getAttribute(id, false);
					} else if (c instanceof Spinner) {
						value = config.getAttribute(id, 1);
					}
					if (value != null) {
						attr.setValueAsString(value.toString());
					}
				} catch (Throwable t) {
					t.printStackTrace();
				}
			}
		}

		/*
		 * (non-Javadoc) If this is called during a save, we check that none of
		 * the widget Text values is null; else we validate the attribute
		 * values.
		 */
		@Override
		protected void validateLocal() throws ValidationException {
			if (!save()) {
				for (Iterator<AttributePlaceholder> i = valueWidgets.values().iterator(); i.hasNext();) {
					AttributePlaceholder ap = i.next();
					Object value = ap.getAttribute().getValue();
					if (null == value || "".equals(value.toString().trim()))
						throw new ValidationException(ap.getName() + ": VALUE NOT SET");
				}
			} else {
				for (Iterator<Control> i = valueWidgets.keySet().iterator(); i.hasNext();) {
					Control c = i.next();
					if (c instanceof Text) {
						String value = ((Text) c).getText().trim();
						if (null == value || "".equals(value)) {
							AttributePlaceholder ap = valueWidgets.get(c);
							throw new ValidationException(ap.getName() + ": VALUE NOT SET");
						}
					}
				}
			}
		}

		private synchronized boolean save() {
			return save;
		}

		private synchronized void toggleSave() {
			save = !save;
		}
	}

	/*
	 * (non-Javadoc) No specific sub-functionality for the moment, but we define
	 * the PBS-specific class anyway.
	 */
	class PBSRMLaunchWidgetListener extends RMLaunchConfigurationDynamicTabWidgetListener {
		public PBSRMLaunchWidgetListener(BaseRMLaunchConfigurationDynamicTab dynamicTab) {
			super(dynamicTab);
		}
	}

	private Composite control;

	/*
	 * //////////////////////////////////////////////////////////////////////////
	 * Fields
	 */
	private PBSRMLaunchDataSource dataSource;
	private PBSRMLaunchWidgetListener listener;
	private Map<Control, AttributePlaceholder> valueWidgets;
	private DynamicTabWizardPage wizardPage;

	/*
	 * (non-Javadoc) The control is populated by a wizard page with adds widgets
	 * on the basis of the selected attributes.
	 * 
	 * @see
	 * org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab#
	 * createControl(org.eclipse.swt.widgets.Composite,
	 * org.eclipse.ptp.core.elements.IResourceManager,
	 * org.eclipse.ptp.core.elements.IPQueue)
	 */
	public void createControl(Composite parent, IResourceManager rm, IPQueue queue) throws CoreException {
		control = WidgetUtils.createContainer(parent, null, false, 110);
		valueWidgets = new HashMap<Control, AttributePlaceholder>();
		wizardPage = new DynamicTabWizardPage(valueWidgets, getListener());
		wizardPage.createControl(control);
	}

	/*
	 * //////////////////////////////////////////////////////////////////////////
	 * Public superclass methods
	 */

	/*
	 * (non-Javadoc) A copy of the attributes, with their values, to be handed
	 * of to the launch method, is constructed from the configuration.
	 * 
	 * @see
	 * org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab#
	 * getAttributes(org.eclipse.ptp.core.elements.IResourceManager,
	 * org.eclipse.ptp.core.elements.IPQueue,
	 * org.eclipse.debug.core.ILaunchConfiguration, java.lang.String)
	 */
	public IAttribute<?, ?, ?>[] getAttributes(IResourceManager rm, IPQueue queue, ILaunchConfiguration configuration, String mode)
			throws CoreException {
		List<IAttribute<?, ?, ?>> attrs = new ArrayList<IAttribute<?, ?, ?>>();
		Map<?, ?> configAttr = configuration.getAttributes();
		Map<String, IAttributeDefinition<?, ?, ?>> defs = PBSJobAttributes.getAttributeDefinitionMap();
		try {
			for (Iterator<?> i = configAttr.entrySet().iterator(); i.hasNext();) {
				Entry<?, ?> e = (Entry<?, ?>) i.next();
				Object value = e.getValue();
				IAttributeDefinition<?, ?, ?> def = defs.get(e.getKey());
				if (def != null && value != null)
					attrs.add(defs.get(e.getKey()).create(value.toString()));
			}
		} catch (IllegalValueException e) {
			throw new CoreException(new Status(Status.WARNING, Activator.PLUGIN_ID, "getAttributes", e));
		}
		return attrs.toArray(new IAttribute<?, ?, ?>[attrs.size()]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab#
	 * getControl()
	 */
	public Control getControl() {
		return control;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.ui.launch.BaseRMLaunchConfigurationDynamicTab#getImage
	 * ()
	 */
	@Override
	public Image getImage() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.ui.launch.BaseRMLaunchConfigurationDynamicTab#getText
	 * ()
	 */
	@Override
	public String getText() {
		return Messages.BasicPBSConfigurationWizardPage_title;
	}

	/*
	 * (non-Javadoc) overridden to do validation only on save actions.
	 * 
	 * @see
	 * org.eclipse.ptp.rm.ui.launch.BaseRMLaunchConfigurationDynamicTab#performApply
	 * (org.eclipse.debug.core.ILaunchConfigurationWorkingCopy,
	 * org.eclipse.ptp.core.elements.IResourceManager,
	 * org.eclipse.ptp.core.elements.IPQueue)
	 */
	@Override
	public RMLaunchValidation performApply(ILaunchConfigurationWorkingCopy configuration, IResourceManager rm, IPQueue queue) {
		dataSource.toggleSave();
		RMLaunchValidation validation = super.performApply(configuration, rm, queue);
		dataSource.toggleSave();
		return validation;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab#
	 * setDefaults(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy,
	 * org.eclipse.ptp.core.elements.IResourceManager,
	 * org.eclipse.ptp.core.elements.IPQueue)
	 */
	public RMLaunchValidation setDefaults(ILaunchConfigurationWorkingCopy configuration, IResourceManager rm, IPQueue queue) {
		return new RMLaunchValidation(true, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.eclipse.ptp.rm.ui.launch.BaseRMLaunchConfigurationDynamicTab#
	 * updateControls()
	 */
	@Override
	public void updateControls() {
	}

	/*
	 * //////////////////////////////////////////////////////////////////////////
	 * Protected Superclass Methods
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.eclipse.ptp.rm.ui.launch.BaseRMLaunchConfigurationDynamicTab#
	 * createDataSource()
	 */
	@Override
	protected synchronized RMLaunchConfigurationDynamicTabDataSource createDataSource() {
		if (dataSource == null) {
			dataSource = new PBSRMLaunchDataSource(this);
		}
		return dataSource;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.eclipse.ptp.rm.ui.launch.BaseRMLaunchConfigurationDynamicTab#
	 * createListener()
	 */
	@Override
	protected synchronized RMLaunchConfigurationDynamicTabWidgetListener createListener() {
		if (listener == null) {
			listener = new PBSRMLaunchWidgetListener(this);
		}
		return listener;
	}
}