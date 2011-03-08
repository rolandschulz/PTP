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
 *                            - modified (10/01/2010) to use non-nls interface; 
 *                              moved the queue-name combo functionality into 
 *                              the launch tab (5.0)
 *                            - fixed load to get config, not working copy
 *                            - eliminated static map (not necessary) (11/12/2010)
 ******************************************************************************/
package org.eclipse.ptp.rm.pbs.ui.launch;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ptp.core.attributes.IAttribute;
import org.eclipse.ptp.core.attributes.IllegalValueException;
import org.eclipse.ptp.core.elements.IPQueue;
import org.eclipse.ptp.launch.ui.extensions.RMLaunchValidation;
import org.eclipse.ptp.rm.pbs.core.ConfigUtils;
import org.eclipse.ptp.rm.pbs.core.IPBSNonNLSConstants;
import org.eclipse.ptp.rm.pbs.core.attributes.AttributePlaceholder;
import org.eclipse.ptp.rm.pbs.core.rmsystem.PBSResourceManager;
import org.eclipse.ptp.rm.pbs.core.templates.PBSBatchScriptTemplate;
import org.eclipse.ptp.rm.pbs.ui.PBSUIPlugin;
import org.eclipse.ptp.rm.pbs.ui.dialogs.ScrollingEditableMessageDialog;
import org.eclipse.ptp.rm.pbs.ui.messages.Messages;
import org.eclipse.ptp.rm.pbs.ui.utils.WidgetUtils;
import org.eclipse.ptp.rm.pbs.ui.wizards.PBSBatchScriptTemplateWizard;
import org.eclipse.ptp.rm.pbs.ui.wizards.PBSRMLaunchConfigurationDynamicTabWizardPage;
import org.eclipse.ptp.rm.ui.launch.BaseRMLaunchConfigurationDynamicTab;
import org.eclipse.ptp.rm.ui.launch.RMLaunchConfigurationDynamicTabDataSource;
import org.eclipse.ptp.rm.ui.launch.RMLaunchConfigurationDynamicTabWidgetListener;
import org.eclipse.ptp.rm.ui.utils.WidgetListener;
import org.eclipse.ptp.rmsystem.IResourceManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
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
public class PBSRMLaunchConfigurationDynamicTab extends BaseRMLaunchConfigurationDynamicTab implements IPBSNonNLSConstants {

	private class DestinationComboListener extends RMLaunchConfigurationDynamicTabWidgetListener implements MouseListener {
		public DestinationComboListener(BaseRMLaunchConfigurationDynamicTab dynamicTab) {
			super(dynamicTab);
		}

		@Override
		public void modifyText(ModifyEvent e) {
			if (!templateChangeListener.isEnabled()) {
				return;
			}
			super.modifyText(e);
		}

		public void mouseDoubleClick(MouseEvent e) {
			// unused
		}

		public void mouseDown(MouseEvent e) {
			Combo c = (Combo) e.getSource();
			if (c.getItemCount() != 0) {
				return;
			}
			disable();
			String text = c.getText();
			c.setItems(ConfigUtils.getCurrentQueues(getResourceManager()));
			c.setText(text);
			enable();
		}

		public void mouseUp(MouseEvent e) {
			// usused
		}
	}

	/*
	 * (non-Javadoc) Provides communication between the template and the
	 * underlying store (configuration) on the one hand, and the template and
	 * the display widgets on the other. The extra fields are there to maintain
	 * the correct options for rebuilding the controls.
	 */
	private class PBSRMLaunchDataSource extends RMLaunchConfigurationDynamicTabDataSource {
		protected PBSRMLaunchDataSource(BaseRMLaunchConfigurationDynamicTab page) {
			super(page);
		}

		/*
		 * (non-Javadoc) Overridden to record changes in resource manager.
		 * 
		 * @see
		 * org.eclipse.ptp.rm.ui.launch.RMLaunchConfigurationDynamicTabDataSource
		 * #setResourceManager(org.eclipse.ptp.core.elements.IPResourceManager)
		 */
		@Override
		public void setResourceManager(IResourceManager rm) {
			super.setResourceManager(rm);
		}

		/*
		 * (non-Javadoc) Widgets-to-Model (attribute).
		 */
		@Override
		protected void copyFromFields() throws ValidationException {
			if (dynamicControl == null || dynamicControl.isDisposed()) {
				return;
			}
			PBSBatchScriptTemplate template = pbsRM.getTemplateManager().getCurrent();
			if (template == null) {
				return;
			}

			AttributePlaceholder ap = null;
			Object value = null;

			for (Iterator<Entry<Control, AttributePlaceholder>> i = valueWidgets.entrySet().iterator(); i.hasNext();) {
				Entry<Control, AttributePlaceholder> e = i.next();
				Control c = e.getKey();
				ap = e.getValue();
				value = null;
				if (c instanceof Text) {
					value = ((Text) c).getText();
				} else if (c instanceof Combo) {
					value = ((Combo) c).getText();
				} else if (c instanceof Spinner) {
					value = ((Spinner) c).getSelection();
				} else if (c instanceof Button) {
					value = ((Button) c).getSelection();
				}
				if (value != null) {
					try {
						ap.getAttribute().setValueAsString(value.toString());
					} catch (IllegalValueException t) {
						throw new ValidationException(t.toString());
					}
				}
			}

			if (templateChangeListener.isEnabled() && mpiCommand != null) {
				value = WidgetUtils.getSelected(mpiCommand).trim();
				try {
					template.setMPIAttributes((String) value);
				} catch (Throwable t) {
					throw new ValidationException(t.getMessage() + CO + SP + t.getCause());
				}
			}
		}

		/*
		 * (non-Javadoc) Model-to-widget (valueWidgets, combo box).
		 */
		@Override
		protected void copyToFields() {
			if (dynamicControl == null || dynamicControl.isDisposed()) {
				return;
			}
			PBSBatchScriptTemplate template = pbsRM.getTemplateManager().getCurrent();
			if (template == null) {
				return;
			}
			AttributePlaceholder ap = null;
			IAttribute<?, ?, ?> attr = null;
			Object value = null;
			for (Iterator<Entry<Control, AttributePlaceholder>> i = valueWidgets.entrySet().iterator(); i.hasNext();) {
				Entry<Control, AttributePlaceholder> e = i.next();
				ap = e.getValue();
				attr = ap.getAttribute();
				if (attr != null) {
					value = attr.getValue();
				}
				Control c = e.getKey();
				if (value != null) {
					if (c instanceof Text) {
						applyText((Text) c, (String) value);
					} else if (c instanceof Combo) {
						applyText((Combo) c, (String) value);
					} else if (c instanceof Spinner) {
						((Spinner) c).setSelection((Integer) value);
					} else if (c instanceof Button) {
						((Button) c).setSelection((Boolean) value);
					}
				}
			}

			ap = template.getMpiCommand();
			if (ap != null) {
				attr = ap.getAttribute();
				if (attr != null) {
					value = attr.getValue();
				}
				if (value != null) {
					WidgetUtils.select(mpiCommand, (String) value);
				}
			}

			WidgetUtils.select(templates, template.getName());
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
			if (config == null) {
				return;
			}
			PBSBatchScriptTemplate template = pbsRM.getTemplateManager().getCurrent();
			if (template != null) {
				template.saveValues(config);
			}
		}

		/*
		 * (non-Javadoc)
		 */
		@Override
		protected void loadDefault() {
		}

		/*
		 * (non-Javadoc) Configuration to model (attributes).
		 */
		@Override
		protected void loadFromStorage() {
			ILaunchConfiguration config = getConfiguration();
			if (config != null) {
				PBSBatchScriptTemplate template = pbsRM.getTemplateManager().getCurrent();
				if (template == null) {
					return;
				}
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
			if (dynamicControl == null || dynamicControl.isDisposed()) {
				return;
			}
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
					if (ZEROSTR.equals(value) && !ZEROSTR.equals(defaultString)) {
						t.setText(defaultString);
					}
				} else if (c instanceof Combo) {
					Combo cmb = (Combo) c;
					String value = cmb.getText();
					AttributePlaceholder ap = valueWidgets.get(c);
					if (value.indexOf(QM) >= 0) {
						throw new ValidationException(ap.getName() + CO + SP + Messages.PBSRMLaunchDataSource_ValueNotSet);
					}
				}
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
			if (valueWidgets.containsKey(o)) {
				if (!templateChangeListener.isEnabled()) {
					return;
				}
			}
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
			if (o == editPrepended || o == editPostpended || o == viewScript || o == editTemplates) {
				widgetSelected(e);
			} else {
				super.widgetDefaultSelected(e);
			}
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
			PBSBatchScriptTemplate template = pbsRM.getTemplateManager().getCurrent();
			if (template == null) {
				return;
			}
			AttributePlaceholder ap = null;
			Object o = e.getSource();
			String title = ZEROSTR;
			if (o == editPrepended) {
				title = Messages.PBSBatchScriptTemplateEditPrepend_title;
				ap = template.getPrependedBashCommands();
			} else if (o == editPostpended) {
				title = Messages.PBSBatchScriptTemplateEditPostpend_title;
				ap = template.getPostpendedBashCommands();
			}

			try {
				if (ap != null) {
					openEditor(ap, title);
				} else if (o == viewScript) {
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
				if (dialog.open() == Window.CANCEL) {
					return;
				}
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
				new ScrollingEditableMessageDialog(control.getShell(), Messages.PBSBatchScriptDisplay, script, true).open();
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}
	}

	/*
	 * Separate listener for template combo box and button.
	 */
	private class TemplateChangeListener extends WidgetListener {
		@Override
		protected void doModifyText(ModifyEvent e) {
			fireTemplateChange(WidgetUtils.getSelected(templates));
		}

		@Override
		protected void doWidgetSelected(SelectionEvent e) {
			Object o = e.getSource();
			if (o == editTemplates) {
				handleEditTemplates();
			}
		}
	}

	private final Map<Control, AttributePlaceholder> valueWidgets;

	private PBSRMLaunchDataSource dataSource;
	private ScrolledComposite parent;
	private Composite dynamicControl;
	private Composite control;
	private Button editPostpended;
	private Button editPrepended;
	private Button viewScript;
	private Button editTemplates;
	private Combo mpiCommand;
	private Combo templates;
	private final TemplateChangeListener templateChangeListener;
	private PBSRMLaunchWidgetListener listener;
	private DestinationComboListener destComboListener;
	private PBSResourceManager pbsRM;

	/**
	 * Creates the templateManager and templateChangeListener.
	 * 
	 * @param resourceManager
	 */
	public PBSRMLaunchConfigurationDynamicTab(IResourceManager rm, ILaunchConfigurationDialog dialog) {
		super(dialog);
		setResourceManager(rm);
		templateChangeListener = new TemplateChangeListener();
		valueWidgets = new HashMap<Control, AttributePlaceholder>();
	}

	/**
	 * The control has two panels: <br>
	 * <br>
	 * The first panel allows for choosing the template and inspecting the
	 * generated script.<br>
	 * <br>
	 * The second is dynamically populated, and has two sub-panels. The first
	 * has MPI command configuration and buttons for opening editors for
	 * modifying the areas of the batch script surrounding the actual
	 * application execution command. The second sub-panel is a wizard page
	 * which adds widgets on the basis of the PBS Job Attributes present in the
	 * template.
	 */
	public void createControl(Composite parent, IResourceManager rm, IPQueue queue) throws CoreException {
		control = WidgetUtils.createComposite(parent, 1);
		setResourceManager(rm);
		if (parent instanceof ScrolledComposite) {
			this.parent = (ScrolledComposite) parent;
		}
		createSelectionGroup(control);
		rmNotRunningWarning();
	}

	public synchronized RMLaunchConfigurationDynamicTabWidgetListener createDestinationComboListener() {
		if (destComboListener == null) {
			destComboListener = new DestinationComboListener(this);
		}
		return destComboListener;
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

	public synchronized PBSResourceManager getResourceManager() {
		return pbsRM;
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
	 * @see
	 * org.eclipse.ptp.rm.ui.launch.BaseRMLaunchConfigurationDynamicTab#performApply
	 * (org.eclipse.debug.core.ILaunchConfigurationWorkingCopy,
	 * org.eclipse.ptp.core.elements.IPResourceManager,
	 * org.eclipse.ptp.core.elements.IPQueue)
	 */
	@Override
	public RMLaunchValidation performApply(ILaunchConfigurationWorkingCopy configuration, IResourceManager rm, IPQueue queue) {
		setResourceManager(rm);
		RMLaunchValidation rmv = super.performApply(configuration, getResourceManager(), queue);
		return rmv;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab#
	 * setDefaults(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy,
	 * org.eclipse.ptp.core.elements.IPResourceManager,
	 * org.eclipse.ptp.core.elements.IPQueue)
	 */
	public RMLaunchValidation setDefaults(ILaunchConfigurationWorkingCopy configuration, IResourceManager rm, IPQueue queue) {
		setResourceManager(rm);
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
	}

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

	/*
	 * Nests child control which can be disposed when rebuild is called for.
	 */
	private void buildDynamicPart(ILaunchConfiguration lconfig) {
		if (dynamicControl != null) {
			dynamicControl.dispose();
			valueWidgets.clear();
		}
		if (control.isDisposed()) {
			return;
		}
		dynamicControl = WidgetUtils.createComposite(control, 1);
		PBSBatchScriptTemplate template = pbsRM.getTemplateManager().getCurrent();
		if (template == null && lconfig != null) {
			pbsRM.getTemplateManager().loadTemplate(pbsRM.getTemplateManager().getCurrentTemplateName(), lconfig);
			template = pbsRM.getTemplateManager().getCurrent();
		}
		if (template != null) {
			createOptionalGroup(dynamicControl, template);
			PBSRMLaunchConfigurationDynamicTabWizardPage wizardPage = new PBSRMLaunchConfigurationDynamicTabWizardPage(this,
					valueWidgets, getListener(), template);
			wizardPage.createControl(dynamicControl);
		}
		/*
		 * We need to repeat this here (the ResourcesTab does it when it
		 * initially builds the control).
		 */
		if (parent != null) {
			parent.setMinSize(control.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		}
	}

	/*
	 * Constructs lower part of control, containing combo selection for MPI
	 * command and buttons for editing optional sections, if these are present
	 * in the template.
	 */
	private void createOptionalGroup(Composite parent, PBSBatchScriptTemplate template) {
		if (template == null) {
			return;
		}
		boolean[] nonNull = new boolean[] { null != template.getMpiCommand(), null != template.getPrependedBashCommands(),
				null != template.getPostpendedBashCommands() };

		if (!nonNull[0] && !nonNull[1] && !nonNull[2]) {
			return;
		}

		Group options = WidgetUtils.createFillingGroup(parent, Messages.PBSRMLaunchConfigGroup2_title, 3, 1, false);
		options.setForeground(WidgetUtils.DKMG);

		if (nonNull[0]) {
			mpiCommand = WidgetUtils.createItemCombo(options, Messages.PBSBatchScriptTemplateMPICommand, MPICMDS, MPICMDS[0], null,
					true, listener, 2);
		}
		if (nonNull[1]) {
			editPrepended = WidgetUtils.createButton(options, Messages.PBSBatchScriptTemplateEditPrepend_title, null, SWT.PUSH, 1,
					false, listener);
		}
		if (nonNull[2]) {
			editPostpended = WidgetUtils.createButton(options, Messages.PBSBatchScriptTemplateEditPostpend_title, null, SWT.PUSH,
					1, false, listener);
		}
	}

	/*
	 * Constructs upper part of control, containing combo selection for changing
	 * template and button for viewing the script. Calls set template to
	 * establish the dynamic components.
	 */
	private void createSelectionGroup(Composite parent) {
		Composite composite = WidgetUtils.createComposite(parent, 1);
		Group selection = WidgetUtils.createFillingGroup(composite, Messages.PBSRMLaunchConfigGroup1_title, 5, 1, false);
		selection.setForeground(WidgetUtils.DKMG);
		templates = WidgetUtils.createItemCombo(selection, null, pbsRM.getTemplateManager().findAvailableTemplates(), null, null,
				true, templateChangeListener, 2);
		((GridData) templates.getLayoutData()).widthHint = 200;
		editTemplates = WidgetUtils.createButton(selection, Messages.PBSRMLaunchConfigEditTemplates_title, null, SWT.PUSH, 1, true,
				templateChangeListener);
		viewScript = WidgetUtils.createButton(selection, Messages.PBSRMLaunchConfigViewScript_title, null, SWT.PUSH, 1, true,
				listener);
		Label l = WidgetUtils.createLabel(selection, rmNotRunningWarning(), SWT.LEFT, 1);
		l.setForeground(WidgetUtils.DKRD);

		WidgetUtils.select(templates, pbsRM.getTemplateManager().getCurrentTemplateName());
	}

	/*
	 * Saves the current template, loads a new one and reconfigures the dynamic
	 * widgets on the basis of its placeholders.
	 */
	private void fireTemplateChange(final String name) {
		new UIJob(TEMPLATE_CHANGE) {
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				try {
					dataSource.copyFromFields();
					templateChangeListener.disable();
					dataSource.copyToStorage();
					ILaunchConfiguration c = dataSource.getConfiguration();
					pbsRM.getTemplateManager().loadTemplate(name, c);
					buildDynamicPart(c);
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
	 * First checks for the base template configuration. It then brings up
	 * wizard for editing or deleting templates.
	 */
	private void handleEditTemplates() {
		new UIJob(EDIT_TEMPLATES) {
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				try {
					try {
						Shell shell = PBSUIPlugin.getActiveWorkbenchShell();
						if (getResourceManager() == null || !getResourceManager().getState().equals(IResourceManager.STARTED_STATE)) {
							MessageDialog dialog = new MessageDialog(shell, Messages.PBSAttributeTemplateManager_requestStartTitle,
									null, Messages.PBSAttributeTemplateManager_requestStartMessage, MessageDialog.QUESTION,
									new String[] { Messages.PBSAttributeTemplateManager_requestStartContinue,
											Messages.PBSAttributeTemplateManager_requestStartCancel }, 1);
							if (MessageDialog.CANCEL == dialog.open()) {
								return Status.OK_STATUS;
							}
						}
						if (!pbsRM.getTemplateManager().handleBaseTemplates()) {
							new MessageDialog(shell, Messages.PBSAttributeTemplateManager_requestInitializeTitle, null,
									Messages.PBSAttributeTemplateManager_requestInitializeMessage, MessageDialog.WARNING,
									new String[] { Messages.PBSAttributeTemplateManager_requestStartCancel }, 0).open();
							return Status.OK_STATUS;
						}
					} catch (Throwable t) {
						t.printStackTrace();
					}
					PBSBatchScriptTemplateWizard templateWizard = new PBSBatchScriptTemplateWizard(pbsRM);
					if (Window.CANCEL != new WizardDialog(control.getShell(), templateWizard).open()) {
						repopulateTemplates(templateWizard.getSelectedTemplate());
					}
				} catch (Throwable t) {
					t.printStackTrace();
				}
				return Status.OK_STATUS;
			}
		}.schedule();
	}

	/*
	 * Called when the template wizard is closed with OK button. Updates the
	 * template list from the template manager's list of available templates.
	 */
	private void repopulateTemplates(final String oldTemplate) {
		new UIJob(REPOPULATE_TEMPLATES) {
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				templateChangeListener.disable();
				String[] tempNames = pbsRM.getTemplateManager().findAvailableTemplates();
				templates.setItems(tempNames);
				int i = 0;
				for (; i < tempNames.length; i++) {
					if (tempNames[i].equals(oldTemplate)) {
						templates.select(i);
						break;
					}
				}
				templateChangeListener.enable();
				if (tempNames.length > 0 && i == tempNames.length) {
					templates.select(0);
				} else {
					fireTemplateChange(WidgetUtils.getSelected(templates));
				}
				return Status.OK_STATUS;
			}
		}.schedule();
	}

	/*
	 * Displays warning about template configurations being out of date if the
	 * RM is not running.
	 */
	private String rmNotRunningWarning() {
		IResourceManager rm = getResourceManager();
		StringBuffer text = new StringBuffer();
		if (rm != null) {
			text.append(Messages.PBSAttributeTemplateManager_rmState);
			String state = rm.getState();
			text.append(state);
			if (!IResourceManager.STARTED_STATE.equals(state)) {
				text.append(Messages.PBSAttributeTemplateManager_rmNotStartedMessage);
			}
		}
		return text.toString();
	}

	/*
	 * For consistency.
	 */
	private synchronized void setResourceManager(IResourceManager resourceManager) {
		try {
			pbsRM = (PBSResourceManager) resourceManager;
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
}