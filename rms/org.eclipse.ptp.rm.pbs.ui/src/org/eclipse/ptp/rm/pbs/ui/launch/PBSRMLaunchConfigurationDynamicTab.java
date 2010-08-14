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
 *     		                  - further modifications (04/30/2010)
 *                            - rewritten (05/11/2010)
 ******************************************************************************/
package org.eclipse.ptp.rm.pbs.ui.launch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jface.window.Window;
import org.eclipse.ptp.core.attributes.IAttribute;
import org.eclipse.ptp.core.attributes.IllegalValueException;
import org.eclipse.ptp.core.elements.IPQueue;
import org.eclipse.ptp.core.elements.IResourceManager;
import org.eclipse.ptp.launch.ui.extensions.RMLaunchValidation;
import org.eclipse.ptp.rm.pbs.core.rmsystem.IPBSResourceManagerConfiguration;
import org.eclipse.ptp.rm.pbs.core.rmsystem.PBSResourceManager;
import org.eclipse.ptp.rm.pbs.ui.PBSUIPlugin;
import org.eclipse.ptp.rm.pbs.ui.data.AttributePlaceholder;
import org.eclipse.ptp.rm.pbs.ui.data.PBSBatchScriptTemplate;
import org.eclipse.ptp.rm.pbs.ui.dialogs.ScrollingEditableMessageDialog;
import org.eclipse.ptp.rm.pbs.ui.managers.PBSBatchScriptTemplateManager;
import org.eclipse.ptp.rm.pbs.ui.messages.Messages;
import org.eclipse.ptp.rm.pbs.ui.utils.ConfigUtils;
import org.eclipse.ptp.rm.pbs.ui.utils.WidgetUtils;
import org.eclipse.ptp.rm.pbs.ui.wizards.PBSRMLaunchConfigurationDynamicTabWizardPage;
import org.eclipse.ptp.rm.ui.launch.BaseRMLaunchConfigurationDynamicTab;
import org.eclipse.ptp.rm.ui.launch.RMLaunchConfigurationDynamicTabDataSource;
import org.eclipse.ptp.rm.ui.launch.RMLaunchConfigurationDynamicTabWidgetListener;
import org.eclipse.ptp.rm.ui.utils.WidgetListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.progress.UIJob;

/**
 * Furnishes the options for configuring the PBS launch/submission. <br>
 * <br>
 * Both PBS Job Attributes as well as other extension properties can be set. <br>
 * <br>
 * The dynamic part of the control is built from the template selected.
 * 
 * @author arossi
 */
public class PBSRMLaunchConfigurationDynamicTab extends BaseRMLaunchConfigurationDynamicTab {
	/*
	 * (non-Javadoc) Provides communication between the template and the
	 * underlying store (configuration) on the one hand, and the template and
	 * the display widgets on the other. The extra fields are there to maintain
	 * the correct options for rebuilding the controls.
	 */
	private class PBSRMLaunchDataSource extends RMLaunchConfigurationDynamicTabDataSource {
		private String currentConfigName;
		private String currentRMId;
		private String currentTemplate;
		private String defaultTemplate;
		private String lastRMId;

		protected PBSRMLaunchDataSource(BaseRMLaunchConfigurationDynamicTab page) {
			super(page);
		}

		/*
		 * (non-Javadoc) Overridden to record changes in resource manager.
		 * 
		 * @see
		 * org.eclipse.ptp.rm.ui.launch.RMLaunchConfigurationDynamicTabDataSource
		 * #setResourceManager(org.eclipse.ptp.core.elements.IResourceManager)
		 */
		@Override
		public void setResourceManager(IResourceManager rm) {
			lastRMId = currentRMId;
			currentRMId = rm.getResourceManagerId();
			super.setResourceManager(rm);
		}

		/*
		 * (non-Javadoc) Widgets-to-Model (attribute).
		 */
		@Override
		protected void copyFromFields() throws ValidationException {
			PBSBatchScriptTemplate template = templateManager.getCurrent();
			if (template == null)
				return;

			AttributePlaceholder ap = null;
			Object value = null;

			for (Iterator<Entry<Control, AttributePlaceholder>> i = valueWidgets.entrySet().iterator(); i.hasNext();) {
				Entry<Control, AttributePlaceholder> e = i.next();
				Control c = e.getKey();
				ap = e.getValue();
				value = null;
				if (c instanceof Text)
					value = ((Text) c).getText();
				else if (c instanceof Combo)
					value = ((Combo) c).getText();
				else if (c instanceof Spinner)
					value = ((Spinner) c).getSelection();
				else if (c instanceof Button)
					value = ((Button) c).getSelection();
				try {
					ap.getAttribute().setValueAsString(value.toString());
				} catch (IllegalValueException t) {
					throw new ValidationException(t.toString());
				}
			}

			if (templateChangeListener.isEnabled() && mpiCommand != null) {
				value = mpiCommand.getText().trim();
				try {
					template.setMPIAttributes((String) value);
				} catch (IllegalValueException t) {
					throw new ValidationException(t.getMessage() + ": " + t.getCause()); //$NON-NLS-1$
				}
			}
		}

		/*
		 * (non-Javadoc) Model-to-widget (valueWidgets, combo box).
		 */
		@Override
		protected void copyToFields() {
			PBSBatchScriptTemplate template = templateManager.getCurrent();
			if (template == null)
				return;
			AttributePlaceholder ap = null;
			IAttribute<?, ?, ?> attr = null;
			Object value = null;
			for (Iterator<Entry<Control, AttributePlaceholder>> i = valueWidgets.entrySet().iterator(); i.hasNext();) {
				Entry<Control, AttributePlaceholder> e = i.next();
				ap = e.getValue();
				attr = ap.getAttribute();
				if (attr != null)
					value = attr.getValue();
				Control c = e.getKey();
				if (value != null)
					if (c instanceof Text)
						applyText((Text) c, (String) value);
					else if (c instanceof Combo)
						applyText((Combo) c, (String) value);
					else if (c instanceof Spinner)
						((Spinner) c).setSelection((Integer) value);
					else if (c instanceof Button)
						((Button) c).setSelection((Boolean) value);
			}

			ap = template.getMpiCommand();
			if (ap != null) {
				attr = ap.getAttribute();
				if (attr != null)
					value = attr.getValue();
				if (value != null) {
					String[] items = mpiCommand.getItems();
					for (int i = 0; i < items.length; i++)
						if (items[i].equals(value)) {
							mpiCommand.select(i);
							break;
						}
				}
			}

			String[] items = templates.getItems();
			for (int i = 0; i < items.length; i++)
				if (items[i].equals(currentTemplate)) {
					templates.select(i);
					break;
				}
		}

		/*
		 * (non-Javadoc) Attribute values to configuration. We also maintain a
		 * map in memory of the most current configurations, in order to enable
		 * preservation of values when switching between templates and/or
		 * resource managers within the ResourceTab object.
		 */
		@Override
		protected void copyToStorage() {
			ILaunchConfigurationWorkingCopy config = getConfigurationWorkingCopy();
			if (config == null)
				return;
			config.setAttribute(TAG_CURRENT_TEMPLATE, currentTemplate);
			PBSBatchScriptTemplate template = templateManager.getCurrent();
			if (template != null)
				template.saveValues(config);
		}

		/*
		 * (non-Javadoc)
		 */
		@Override
		protected void loadDefault() {
			// UNUSED
		}

		/*
		 * (non-Javadoc) Configuration to model (attributes).
		 */
		@Override
		protected void loadFromStorage() {
			ILaunchConfiguration config = getConfiguration();
			if (config != null) {
				PBSBatchScriptTemplate template = templateManager.getCurrent();
				if (template == null)
					return;
				template.setConfiguration(config);
				try {
					template.configure();
				} catch (Throwable t) {
					t.printStackTrace();
				}
			}
		}

		/*
		 * (non-Javadoc) Checks the consistency of string values, particularly
		 * empty strings where a default string is defined.
		 */
		@Override
		protected void validateLocal() throws ValidationException {
			for (Iterator<Control> i = valueWidgets.keySet().iterator(); i.hasNext();) {
				Control c = i.next();
				if (c instanceof Text) {
					Text t = (Text) c;
					String value = t.getText().trim();
					AttributePlaceholder ap = valueWidgets.get(c);
					String defaultString = ap.getDefaultString();
					/*
					 * maybe restore default
					 */
					if (ConfigUtils.EMPTY_STRING.equals(value) && !ConfigUtils.EMPTY_STRING.equals(defaultString))
						t.setText(defaultString);
				} else if (c instanceof Combo) {
					Combo cmb = (Combo) c;
					String value = cmb.getText();
					AttributePlaceholder ap = valueWidgets.get(c);
					if (value.indexOf("?") >= 0) //$NON-NLS-1$ 
						throw new ValidationException(ap.getName() + ": " + Messages.PBSRMLaunchDataSource_ValueNotSet); //$NON-NLS-1$ 
				}
			}
		}

		private void setCurrentConfiguration() {
			ILaunchConfigurationWorkingCopy config = getConfigurationWorkingCopy();
			if (config == null)
				return;
			put(config);
		}

		/*
		 * Determines whether the template should be the default or not. Because
		 * of the way RMs are identified, it becomes complex to try to capture
		 * changes to their template settings which would not span across
		 * instances of the ResourceTab, so we have just left the mapping of a
		 * (default) template to the RM fixed. The use of an RM with a different
		 * template only lasts for the duration of the Resource Tab's
		 * persistence in memory; on being reopened, the default template is
		 * restored (but with the last saved attribute values).
		 */
		private void setCurrentTemplate(String oldRM) {
			try {
				ILaunchConfiguration c = get(currentConfigName);
				if (c == null)
					currentTemplate = defaultTemplate;
				else
					currentTemplate = c.getAttribute(TAG_CURRENT_TEMPLATE, defaultTemplate);
			} catch (CoreException ce) {
				ce.printStackTrace();
			}
		}
	}

	/*
	 * (non-Javadoc) General purpose listener for all widgets except the
	 * template choice. Overridden methods support opening of dialog for editing
	 * template.
	 */
	private class PBSRMLaunchWidgetListener extends RMLaunchConfigurationDynamicTabWidgetListener {
		public PBSRMLaunchWidgetListener(BaseRMLaunchConfigurationDynamicTab dynamicTab) {
			super(dynamicTab);
		}

		/*
		 * (non-Javadoc) Overridden to stop proliferation of events when the
		 * control is being rebuilt.
		 * 
		 * @see org.eclipse.ptp.rm.ui.launch.
		 * RMLaunchConfigurationDynamicTabWidgetListener
		 * #modifyText(org.eclipse.swt.events.ModifyEvent)
		 */
		@Override
		public void modifyText(ModifyEvent e) {
			Object o = e.getSource();
			if (!templateChangeListener.isEnabled())
				if (valueWidgets.containsKey(o))
					return;
			super.modifyText(e);
		}

		/*
		 * (non-Javadoc) Overridden to provide for opening of editors.
		 * 
		 * @see org.eclipse.ptp.rm.ui.launch.
		 * RMLaunchConfigurationDynamicTabWidgetListener
		 * #widgetSelected(org.eclipse.swt.events.SelectionEvent)
		 */
		@Override
		public void widgetDefaultSelected(SelectionEvent e) {
			Object o = e.getSource();
			if (o == editPrepended || o == editPostpended || o == viewScript)
				widgetSelected(e);
			else
				super.widgetDefaultSelected(e);
		}

		/*
		 * (non-Javadoc) Overridden to provide for opening of editors.
		 * 
		 * @see org.eclipse.ptp.rm.ui.launch.
		 * RMLaunchConfigurationDynamicTabWidgetListener
		 * #widgetSelected(org.eclipse.swt.events.SelectionEvent)
		 */
		@Override
		public void widgetSelected(SelectionEvent e) {
			PBSBatchScriptTemplate template = templateManager.getCurrent();
			if (template == null)
				return;
			AttributePlaceholder ap = null;
			Object o = e.getSource();
			String title = ConfigUtils.EMPTY_STRING;
			if (o == editPrepended) {
				title = Messages.PBSBatchScriptTemplateEditPrepend_title;
				ap = template.getPrependedBashCommands();
			} else if (o == editPostpended) {
				title = Messages.PBSBatchScriptTemplateEditPostpend_title;
				ap = template.getPostpendedBashCommands();
			}

			try {
				if (ap != null)
					openEditor(ap, title);
				else if (o == viewScript) {
					openReadOnly(template.realize());
					super.widgetSelected(e);
				}
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}

		/*
		 * Editor for adding or editing user-provided script commands.
		 */
		private void openEditor(AttributePlaceholder ap, String title) {
			try {
				IAttribute<?, ?, ?> attr = ap.getAttribute();
				String attrval = attr.getValueAsString();
				ScrollingEditableMessageDialog dialog = new ScrollingEditableMessageDialog(control.getShell(), title, attrval);
				if (dialog.open() == Window.CANCEL)
					return;
				attr.setValueAsString(dialog.getValue());
			} catch (Throwable t) {
				WidgetUtils.errorMessage(control.getShell(), t, Messages.PBSBatchScriptTemplateEditError_message,
						Messages.PBSBatchScriptTemplateEditError_title, false);
			}
		}

		/*
		 * For viewing the script realized from the current template with the
		 * provided values.
		 */
		private void openReadOnly(String script) {
			try {
				new ScrollingEditableMessageDialog(control.getShell(), Messages.PBSBatchScriptDislay, script, true).open();
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}
	}

	/*
	 * Separate listener for template combo box.
	 */
	private class TemplateChangeListener extends WidgetListener {

		/*
		 * Updates the internal current template field and calls
		 * fireTemplateChange.
		 */
		@Override
		protected void doModifyText(ModifyEvent e) {
			dataSource.currentTemplate = templates.getItem(templates.getSelectionIndex());
			fireTemplateChange();
		}
	}

	/*
	 * The ResourceTab reconstructs a new LaunchTab object every time a
	 * different resource manager is selected; in order to be able to maintain
	 * the proper attribute values between resource manager choices within any
	 * given ResourceTab, we need a static map.
	 * 
	 * This map more properly belongs to the ResourceTab instance, but for the
	 * moment the static map will probably not cause semantic issues.
	 */
	private static final Map<String, ILaunchConfiguration> configurations = new HashMap<String, ILaunchConfiguration>();

	private static final String[] mpiChoices = Messages.MPICommands.split(","); //$NON-NLS-1$ 

	private static final String TAG_CURRENT_TEMPLATE = Messages.PBSRMLaunchConfigCurrentTemplate;

	private Composite childControl;
	private Composite control;
	private PBSRMLaunchDataSource dataSource;
	private Button editPostpended;
	private Button editPrepended;
	private PBSRMLaunchWidgetListener listener;
	private Combo mpiCommand;
	private TemplateChangeListener templateChangeListener;
	private PBSBatchScriptTemplateManager templateManager;
	private Combo templates;
	private Map<Control, AttributePlaceholder> valueWidgets;
	private Button viewScript;

	/**
	 * Creates the templateManager and templateChangeListener.
	 * 
	 * @param resourceManager
	 */
	public PBSRMLaunchConfigurationDynamicTab(IResourceManager resourceManager) {
		super();
		try {
			templateChangeListener = new TemplateChangeListener();
			templateManager = new PBSBatchScriptTemplateManager();
			valueWidgets = new HashMap<Control, AttributePlaceholder>();
			templateManager.loadTemplate(null, null);
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	/**
	 * The control has two dynamic panels: <br>
	 * <br>
	 * The first panel allows for choosing the template and inspecting the
	 * generated script.<br>
	 * <br>
	 * The second is populated by MPI command configuration and buttons for
	 * opening editors for modifying the areas of the batch script surrounding
	 * the actual application execution command.<br>
	 * <br>
	 * The last panel is populated by a wizard page which adds widgets on the
	 * basis of the PBS Job Attributes present in the template.
	 */
	public void createControl(Composite parent, IResourceManager rm, IPQueue queue) throws CoreException {
		control = WidgetUtils.createComposite(parent, 2);
		populateControl();
	}

	/**
	 * We send only the realized script as attribute.<br>
	 */
	public IAttribute<?, ?, ?>[] getAttributes(IResourceManager rm, IPQueue queue, ILaunchConfiguration configuration, String mode)
			throws CoreException {
		List<IAttribute<?, ?, ?>> attrs = new ArrayList<IAttribute<?, ?, ?>>();

		String current = configuration.getAttribute(TAG_CURRENT_TEMPLATE, ConfigUtils.EMPTY_STRING);

		PBSBatchScriptTemplate template = templateManager.loadTemplate(current, configuration);
		try {
			template.configure();
			attrs.add(templateManager.getCurrent().createScriptAttribute());
		} catch (IllegalValueException t) {
			IStatus status = new Status(Status.ERROR, PBSUIPlugin.getUniqueIdentifier(), "getAttributes", t); //$NON-NLS-1$
			throw new CoreException(status);
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
		return Messages.PBSConfigurationWizardPage_title;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * If a new resource manager has been selected within this Resource tab, the
	 * controls need to be rebuilt, so template change is fired.
	 * 
	 * @see
	 * org.eclipse.ptp.rm.ui.launch.BaseRMLaunchConfigurationDynamicTab#performApply
	 * (org.eclipse.debug.core.ILaunchConfigurationWorkingCopy,
	 * org.eclipse.ptp.core.elements.IResourceManager,
	 * org.eclipse.ptp.core.elements.IPQueue)
	 */
	@Override
	public RMLaunchValidation performApply(ILaunchConfigurationWorkingCopy configuration, IResourceManager rm, IPQueue queue) {
		// should not be null
		dataSource.currentConfigName = configuration.getName();
		String oldRM = dataSource.lastRMId;
		RMLaunchValidation rmv = super.performApply(configuration, rm, queue);
		if (templateChangeListener.isEnabled())
			if (oldRM == null) {
				PBSResourceManager pbsRM = (PBSResourceManager) rm;
				IPBSResourceManagerConfiguration rmConfig = (IPBSResourceManagerConfiguration) pbsRM.getConfiguration();
				dataSource.defaultTemplate = rmConfig.getDefaultTemplateName();
				dataSource.setCurrentTemplate(oldRM);
				fireTemplateChange();
			}
		dataSource.setCurrentConfiguration();
		return rmv;
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
	 * (non-Javadoc) Unused; all updates handled by the dataSource.
	 * 
	 * @see org.eclipse.ptp.rm.ui.launch.BaseRMLaunchConfigurationDynamicTab#
	 * updateControls()
	 */
	@Override
	public void updateControls() {
		// NOT USED
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.eclipse.ptp.rm.ui.launch.BaseRMLaunchConfigurationDynamicTab#
	 * createDataSource()
	 */
	@Override
	protected synchronized RMLaunchConfigurationDynamicTabDataSource createDataSource() {
		if (dataSource == null)
			dataSource = new PBSRMLaunchDataSource(this);
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
		if (listener == null)
			listener = new PBSRMLaunchWidgetListener(this);
		return listener;
	}

	/*
	 * Constructs lower part of control, containing combo selection for MPI
	 * command and buttons for editing optional sections, if these are present
	 * in the template.
	 */
	private void createOptionalGroup(Composite parent, PBSBatchScriptTemplate template) {
		if (template == null)
			return;
		boolean[] nonNull = new boolean[] { null != template.getMpiCommand(), null != template.getPrependedBashCommands(),
				null != template.getPostpendedBashCommands() };

		if (!nonNull[0] && !nonNull[1] && !nonNull[2])
			return;

		Group options = WidgetUtils.createFillingGroup(parent, Messages.PBSRMLaunchConfigGroup2_title, 3, 1, false);
		options.setForeground(WidgetUtils.DKMG);

		if (nonNull[0])
			mpiCommand = WidgetUtils.createItemCombo(options, Messages.PBSBatchScriptTemplateMPICommand, mpiChoices, mpiChoices[0],
					null, true, listener, 2);
		if (nonNull[1])
			editPrepended = WidgetUtils.createButton(options, Messages.PBSBatchScriptTemplateEditPrepend_title, null, SWT.PUSH, 1,
					false, listener);
		if (nonNull[2])
			editPostpended = WidgetUtils.createButton(options, Messages.PBSBatchScriptTemplateEditPostpend_title, null, SWT.PUSH,
					1, false, listener);
	}

	/*
	 * Constructs upper part of control, containing combo selection for changing
	 * template and button for viewing the script.
	 */
	private void createSelectionGroup(Composite parent) {
		Group selection = WidgetUtils.createFillingGroup(parent, Messages.PBSRMLaunchConfigGroup1_title, 3, 3, true);
		selection.setForeground(WidgetUtils.DKMG);
		templates = WidgetUtils.createItemCombo(selection, null, templateManager.findAvailableTemplates(), null, null, true,
				templateChangeListener, 2);
		((GridData) templates.getLayoutData()).widthHint = 200;
		viewScript = WidgetUtils.createButton(selection, Messages.PBSRMLaunchConfigViewScript_title, null, SWT.PUSH, 1, true,
				listener);
	}

	/*
	 * Saves the current template, loads a new one and reconfigures the dynamic
	 * widgets on the basis of its placeholders.
	 */
	private void fireTemplateChange() {
		new UIJob("template change") {//$NON-NLS-1$ 
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				try {
					templateChangeListener.disable();
					dataSource.copyFromFields();
					dataSource.copyToStorage();
					ILaunchConfiguration c = get(dataSource.currentConfigName);
					templateManager.loadTemplate(dataSource.currentTemplate, c);
					populateControl();
					dataSource.loadFromStorage();
					dataSource.copyToFields();
				} catch (Throwable t) {
					t.printStackTrace();
				} finally {
					templateChangeListener.enable();
				}
				return Status.OK_STATUS;
			}
		}.schedule();
	}

	/*
	 * Nests child control which can be disposed when rebuild is called for.
	 */
	private void populateControl() {
		if (childControl != null) {
			childControl.dispose();
			valueWidgets.clear();
		}
		childControl = WidgetUtils.createComposite(control, 1);
		createSelectionGroup(childControl);
		PBSBatchScriptTemplate template = templateManager.getCurrent();
		if (template != null) {
			createOptionalGroup(childControl, template);
			PBSRMLaunchConfigurationDynamicTabWizardPage wizardPage = new PBSRMLaunchConfigurationDynamicTabWizardPage(
					valueWidgets, getListener(), template);
			wizardPage.createControl(childControl);
		}
	}

	/*
	 * For accessing the most recent configuration in memory.
	 */
	private static synchronized ILaunchConfiguration get(String name) {
		return configurations.get(name);
	}

	/*
	 * For storing the most recent configuration in memory.
	 */
	private static synchronized void put(ILaunchConfiguration configuration) {
		configurations.put(configuration.getName(), configuration);
	}
}