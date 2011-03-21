/*******************************************************************************
 * Copyright (c) 2010 Poznan Supercomputing and Networking Center
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jan Konczak (PSNC) - initial implementation
 ******************************************************************************/

package org.eclipse.ptp.rm.smoa.ui.launch;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.ptp.core.attributes.ArrayAttribute;
import org.eclipse.ptp.core.attributes.ArrayAttributeDefinition;
import org.eclipse.ptp.core.attributes.IAttribute;
import org.eclipse.ptp.core.attributes.IAttributeDefinition;
import org.eclipse.ptp.core.attributes.IllegalValueException;
import org.eclipse.ptp.core.attributes.IntegerAttribute;
import org.eclipse.ptp.core.elements.IPMachine;
import org.eclipse.ptp.core.elements.IPNode;
import org.eclipse.ptp.core.elements.IPQueue;
import org.eclipse.ptp.core.elements.IPResourceManager;
import org.eclipse.ptp.core.elements.attributes.ElementAttributes;
import org.eclipse.ptp.core.elements.attributes.JobAttributes;
import org.eclipse.ptp.launch.ui.extensions.AbstractRMLaunchConfigurationDynamicTab;
import org.eclipse.ptp.launch.ui.extensions.RMLaunchValidation;
import org.eclipse.ptp.rm.smoa.core.SMOAConfiguration;
import org.eclipse.ptp.rm.smoa.core.attrib.SMOAJobAttributes;
import org.eclipse.ptp.rm.smoa.core.attrib.SMOANodeAttributes;
import org.eclipse.ptp.rm.smoa.core.attrib.StringMapAttribute;
import org.eclipse.ptp.rm.smoa.core.attrib.StringMapAttributeDefinition;
import org.eclipse.ptp.rm.smoa.core.rmsystem.SMOAResourceManager;
import org.eclipse.ptp.rm.smoa.ui.SMOAUIPlugin;
import org.eclipse.ptp.rmsystem.IResourceManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;

/**
 * This GUI element is drawn within the Launch dialog, under Resources tab.
 * 
 * The configuration is saved outside this class.
 */
public class SMOARMLaunchConfigurationDynamicTab extends AbstractRMLaunchConfigurationDynamicTab {

	/** The regular expression for filtering application names for combo box */
	public static final String APP_REGEX = Messages.SMOARMLaunchConfigurationDynamicTab_APP_REGEX;

	// Main GUI composite
	private Composite control = null;

	// Controls for user:
	private Text jobName = null;
	private Text jobDescription = null;
	private Text jobNativeSpecification = null;
	private List machinesAll = null;
	private List machinesPreferred = null;
	private Spinner minCPUs = null;
	private Spinner maxCPUs = null;
	private Button cboxMin = null;
	private Button cboxMax = null;
	private Combo application = null;
	private Button cboxMake = null;
	private Text queue = null;
	private Text customMake = null;
	private Button cboxCustomMake = null;

	public SMOARMLaunchConfigurationDynamicTab(IResourceManager rm, ILaunchConfigurationDialog dialog) {
		super(dialog);
	}

	public RMLaunchValidation canSave(Control control, IResourceManager rm, IPQueue queue) {
		return new RMLaunchValidation(true, null);
	}

	public void createControl(Composite parent, IResourceManager rm, IPQueue ipqueue) throws CoreException {
		control = new Composite(parent, SWT.NONE);
		control.setLayout(new GridLayout(1, false));

		Label label;
		Group group;

		// Job name

		final Composite nameComposite = new Composite(control, SWT.NONE);
		nameComposite.setLayout(new GridLayout(2, false));
		nameComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		label = new Label(nameComposite, SWT.NONE);
		label.setText(Messages.SMOARMLaunchConfigurationDynamicTab_Name);
		label.setLayoutData(new GridData());

		jobName = new Text(nameComposite, SWT.SINGLE | SWT.BORDER);
		jobName.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		label = new Label(nameComposite, SWT.NONE);
		label.setText(Messages.SMOARMLaunchConfigurationDynamicTab_Description);
		label.setLayoutData(new GridData());

		jobDescription = new Text(nameComposite, SWT.MULTI | SWT.BORDER);
		GridData gridData = new GridData(GridData.FILL_BOTH);
		gridData.minimumHeight = jobDescription.getLineHeight();
		jobDescription.setLayoutData(gridData);

		// App & make

		final Composite appComposite = new Composite(control, SWT.NONE);
		appComposite.setLayout(new GridLayout(4, false));
		appComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		label = new Label(appComposite, SWT.NONE);
		label.setText(Messages.SMOARMLaunchConfigurationDynamicTab_WrapperScript);
		label.setLayoutData(new GridData());

		application = new Combo(appComposite, SWT.BORDER | SWT.READ_ONLY);
		application.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		label = new Label(appComposite, SWT.NONE);
		label.setText("     "); /* Now I know where the name "spacer" comes from *///$NON-NLS-1$
		label.setLayoutData(new GridData());

		cboxMake = new Button(appComposite, SWT.CHECK | SWT.BORDER);
		cboxMake.setLayoutData(new GridData());

		cboxMake.setText(Messages.SMOARMLaunchConfigurationDynamicTab_RunMake);

		// Queue and make app

		final Composite queue_makenameComposite = new Composite(control, SWT.NONE);
		queue_makenameComposite.setLayout(new GridLayout(2, false));
		queue_makenameComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		cboxCustomMake = new Button(queue_makenameComposite, SWT.CHECK | SWT.BORDER);
		cboxCustomMake.setText(Messages.SMOARMLaunchConfigurationDynamicTab_CustomMakeCommand);
		cboxCustomMake.setLayoutData(new GridData());
		cboxCustomMake.setEnabled(false);

		customMake = new Text(queue_makenameComposite, SWT.SINGLE | SWT.BORDER);
		customMake.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		customMake.setEnabled(false);

		label = new Label(queue_makenameComposite, SWT.NONE);
		label.setText(Messages.SMOARMLaunchConfigurationDynamicTab_QueueName);
		label.setLayoutData(new GridData());

		queue = new Text(queue_makenameComposite, SWT.SINGLE | SWT.BORDER);
		queue.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		// CPU count

		group = new Group(control, SWT.NONE);
		group.setText(Messages.SMOARMLaunchConfigurationDynamicTab_CpuCountBounds);
		group.setLayout(new GridLayout(6, false));
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		label = new Label(group, SWT.NONE);
		label.setText(Messages.SMOARMLaunchConfigurationDynamicTab_MinCpuCount);
		label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.HORIZONTAL_ALIGN_END));

		minCPUs = new Spinner(group, SWT.SINGLE | SWT.BORDER);
		minCPUs.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));

		cboxMin = new Button(group, SWT.CHECK);
		cboxMin.setSelection(true);
		cboxMin.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));

		label = new Label(group, SWT.NONE);
		label.setText(Messages.SMOARMLaunchConfigurationDynamicTab_MaxCpuCount);
		label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.HORIZONTAL_ALIGN_END));

		maxCPUs = new Spinner(group, SWT.SINGLE | SWT.BORDER);
		maxCPUs.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));

		cboxMax = new Button(group, SWT.CHECK);
		cboxMax.setSelection(true);
		cboxMax.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));

		// Machines

		group = new Group(control, SWT.NONE);
		group.setText(Messages.SMOARMLaunchConfigurationDynamicTab_PreferredMachines);
		group.setLayout(new GridLayout(2, true));
		group.setLayoutData(new GridData(GridData.FILL_BOTH));

		label = new Label(group, SWT.NONE);
		label.setText(Messages.SMOARMLaunchConfigurationDynamicTab_AllMachines);
		label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		label = new Label(group, SWT.NONE);
		label.setText(Messages.SMOARMLaunchConfigurationDynamicTab_PreferredMachines);
		label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		machinesAll = new List(group, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL);
		gridData = new GridData(GridData.FILL_BOTH);
		gridData.minimumHeight = (int) (2.25 * machinesAll.getItemHeight());
		machinesAll.setLayoutData(gridData);

		machinesPreferred = new List(group, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL);
		machinesPreferred.setLayoutData(new GridData(GridData.FILL_BOTH));

		// Descriptions

		final Composite descsComposite = new Composite(control, SWT.NONE);
		descsComposite.setLayout(new GridLayout(2, false));
		descsComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		label = new Label(descsComposite, SWT.NONE);
		label.setText(Messages.SMOARMLaunchConfigurationDynamicTab_NativeSpec);
		label.setLayoutData(new GridData());

		jobNativeSpecification = new Text(descsComposite, SWT.MULTI | SWT.BORDER);
		gridData = new GridData(GridData.FILL_BOTH);
		gridData.minimumHeight = jobNativeSpecification.getLineHeight();
		jobNativeSpecification.setLayoutData(gridData);

	}

	/**
	 * Passes all attributes from ILaunchConfiguration to the proper launch
	 * filtering only those returned by SMOAJobAttributes.getLaunchAttributes()
	 * method
	 */
	@SuppressWarnings("unchecked")
	public IAttribute<?, ?, ?>[] getAttributes(IResourceManager rm, IPQueue queue, ILaunchConfiguration configuration, String mode)
			throws CoreException {
		final Map<String, IAttribute<?, ?, ?>> launchAttributes = new HashMap<String, IAttribute<?, ?, ?>>();

		final Map<?, ?> configurationAttributes = configuration.getAttributes();

		final Map<String, IAttributeDefinition<?, ?, ?>> possibleAttributesMap = SMOAJobAttributes.getLaunchAttributes();
		try {
			for (final Object name : configurationAttributes.entrySet()) {
				final Entry<?, ?> entry = (Entry<?, ?>) name;

				final Object value = entry.getValue();
				final IAttributeDefinition<?, ?, ?> definition = possibleAttributesMap.get(entry.getKey());

				if (definition != null && value != null) {
					if (definition instanceof ArrayAttributeDefinition<?>) {
						// ArrayAttributeDefinition
						assert value instanceof java.util.List<?>;
						final java.util.List<String> list = (java.util.List<String>) value;
						final ArrayAttributeDefinition<String> arrayDef = (ArrayAttributeDefinition<String>) definition;

						final ArrayAttribute<String> attribute = arrayDef.create(list.toArray(new String[list.size()]));
						launchAttributes.put(entry.getKey().toString(), attribute);
					} else if (definition instanceof StringMapAttributeDefinition) {
						// StringMapAttributeDefinition
						if (value instanceof Map<?, ?>) {
							final Map<String, String> map = (Map<String, String>) entry.getValue();
							final StringMapAttribute attribute = ((StringMapAttributeDefinition) definition).create(map);
							launchAttributes.put(entry.getKey().toString(), attribute);
						} else {
							throw new IllegalValueException("Bad format of some map attr"); //$NON-NLS-1$
						}
					} else {
						// Others
						launchAttributes.put(entry.getKey().toString(), definition.create(value.toString()));
					}
				}
			}
		} catch (final IllegalValueException e) {
			throw new CoreException(new Status(IStatus.WARNING, SMOAUIPlugin.PLUGIN_ID, "getAttributes", e)); //$NON-NLS-1$
		}

		return launchAttributes.values().toArray(new IAttribute<?, ?, ?>[launchAttributes.size()]);
	}

	public Control getControl() {
		return control;
	}

	public RMLaunchValidation initializeFrom(Control control, IResourceManager rm, IPQueue ipqueue,
			ILaunchConfiguration configuration) {
		boolean success = false;
		String message = null;

		success = true;

		// We get machine names and sum of processor counts
		int maxCpuCount = 0;

		final IPResourceManager rmp = (IPResourceManager) rm.getAdapter(IPResourceManager.class);
		final IPMachine[] machines = rmp.getMachines();
		if (machines != null && machines.length > 0) {
			final IPMachine machine = machines[0];
			for (final IPNode node : machine.getNodes()) {
				machinesAll.add(node.getName());
				final IntegerAttribute cpuCountAttribute = node.getAttribute(SMOANodeAttributes.getCpuCountDef());
				if (cpuCountAttribute != null) {
					final Integer cpusOnNode = cpuCountAttribute.getValue();
					if (cpusOnNode != null) {
						maxCpuCount += cpusOnNode;
					}
				}
			}
		}

		final java.util.List<String> apps = new Vector<String>();
		if (rm instanceof SMOAResourceManager && ((SMOAResourceManager) rm).getConfiguration() instanceof SMOAConfiguration) {
			final SMOAConfiguration conf = (SMOAConfiguration) ((SMOAResourceManager) rm).getConfiguration();
			for (final String app : conf.getAvailableAppList()) {
				if (app.matches(APP_REGEX)) {
					apps.add(app);
				}
			}
		}
		apps.add(SMOAJobAttributes.NO_WRAPPER_SCRIPT);
		Collections.sort(apps);
		application.setItems(apps.toArray(new String[apps.size()]));
		application.select(application.indexOf(SMOAJobAttributes.NO_WRAPPER_SCRIPT));

		if (maxCpuCount == 0) {
			maxCpuCount = 1;
		}

		minCPUs.setMinimum(1);
		maxCPUs.setMinimum(1);
		minCPUs.setMaximum(maxCpuCount);
		maxCPUs.setMaximum(maxCpuCount);

		minCPUs.setSelection(1);
		maxCPUs.setSelection(maxCpuCount);

		minCPUs.setEnabled(false);
		maxCPUs.setEnabled(false);

		cboxMin.setSelection(false);
		cboxMax.setSelection(false);

		try {
			final Map<?, ?> confAttributes = configuration.getAttributes();

			final String nameEntry = (String) confAttributes.get(ElementAttributes.getNameAttributeDefinition().getId());
			if (nameEntry != null) {
				jobName.setText(nameEntry);
			}

			final String appEntry = (String) confAttributes.get(SMOAJobAttributes.getAppNameDef().getId());
			if (appEntry != null) {
				if (application.indexOf(appEntry) != -1) {
					application.select(application.indexOf(appEntry));
				}
			}

			final Boolean b = (Boolean) confAttributes.get(SMOAJobAttributes.getMakeDef().getId());
			if (b != null) {
				cboxMake.setSelection(b);
				if (b) {
					cboxCustomMake.setEnabled(true);
				}
			}

			final Boolean c = (Boolean) confAttributes.get(SMOAJobAttributes.getIfCustomMakeDef().getId());
			if (c != null) {
				cboxCustomMake.setSelection(c);
			}
			{
				if (b != null && b && c) {
					customMake.setEnabled(true);
				}
			}

			final String makeCmdEntry = (String) confAttributes.get(SMOAJobAttributes.getCustomMakeCommandDef().getId());
			if (makeCmdEntry != null) {
				customMake.setText(makeCmdEntry);
			}

			final String queueEntry = (String) confAttributes.get(SMOAJobAttributes.getQueueNameDef().getId());
			if (queueEntry != null) {
				queue.setText(queueEntry);
			}

			final Integer minEntry = (Integer) confAttributes.get(SMOAJobAttributes.getMinCpuDef().getId());
			if (minEntry != null) {
				minCPUs.setSelection(minEntry);
				cboxMin.setSelection(true);
				minCPUs.setEnabled(true);
			}

			final Integer maxEntry = (Integer) confAttributes.get(SMOAJobAttributes.getMaxCpuDef().getId());
			if (maxEntry != null) {
				maxCPUs.setSelection(maxEntry);
				cboxMax.setSelection(true);
				maxCPUs.setEnabled(true);
			}

			final String descEntry = (String) confAttributes.get(SMOAJobAttributes.getDescDef().getId());
			if (descEntry != null) {
				jobDescription.setText(descEntry);
			}

			final String nativeEntry = (String) confAttributes.get(SMOAJobAttributes.getNativeSpecDef().getId());
			if (nativeEntry != null) {
				jobNativeSpecification.setText(nativeEntry);
			}

			@SuppressWarnings("unchecked")
			final java.util.List<String> prefEntry = (java.util.List<String>) confAttributes.get(SMOAJobAttributes
					.getPrefferedDef().getId());
			if (prefEntry != null) {
				for (final String string : prefEntry) {
					machinesPreferred.add(string);
				}
			}

		} catch (final CoreException e) {
			e.printStackTrace();
			success = false;
			message = e.getMessage();
		}

		prepareListeners(control, rm, ipqueue, configuration);

		return new RMLaunchValidation(success, message);
	}

	public RMLaunchValidation isValid(ILaunchConfiguration launchConfig, IResourceManager rm, IPQueue queue) {

		final boolean isCustomCommandValid = !(customMake.isEnabled() && customMake.getText().isEmpty());

		return new RMLaunchValidation(isCustomCommandValid, isCustomCommandValid ? null
				: Messages.SMOARMLaunchConfigurationDynamicTab_CustomMakeIsEmptyError);
	}

	/**
	 * Called by each context change, tab change and by initialization (after
	 * initializeForm).
	 */
	public RMLaunchValidation performApply(ILaunchConfigurationWorkingCopy configuration, IResourceManager rm, IPQueue ipqueue) {

		configuration.setAttribute(ElementAttributes.getNameAttributeDefinition().getId(), jobName.getText());

		configuration.setAttribute(SMOAJobAttributes.getDescDef().getId(), jobDescription.getText());

		configuration.setAttribute(SMOAJobAttributes.getQueueNameDef().getId(), queue.getText());

		configuration.setAttribute(SMOAJobAttributes.getAppNameDef().getId(), application.getItem(application.getSelectionIndex()));

		configuration.setAttribute(SMOAJobAttributes.getMakeDef().getId(), cboxMake.getSelection());

		configuration.setAttribute(SMOAJobAttributes.getIfCustomMakeDef().getId(), cboxCustomMake.getSelection());

		configuration.setAttribute(SMOAJobAttributes.getCustomMakeCommandDef().getId(), customMake.getText());

		if (cboxMin.getSelection()) {
			configuration.setAttribute(SMOAJobAttributes.getMinCpuDef().getId(), minCPUs.getSelection());
		} else {
			configuration.removeAttribute(SMOAJobAttributes.getMinCpuDef().getId());
		}

		if (cboxMax.getSelection()) {
			configuration.setAttribute(SMOAJobAttributes.getMaxCpuDef().getId(), maxCPUs.getSelection());
		} else {
			configuration.removeAttribute(SMOAJobAttributes.getMaxCpuDef().getId());
		}

		configuration.setAttribute(SMOAJobAttributes.getNativeSpecDef().getId(), jobNativeSpecification.getText());
		final Vector<String> pref = new Vector<String>();
		for (final String s : machinesPreferred.getItems()) {
			pref.add(s);
		}
		configuration.setAttribute(SMOAJobAttributes.getPrefferedDef().getId(), pref);

		return new RMLaunchValidation(true, null);
	}

	private void prepareListeners(Control c, final IResourceManager rm, IPQueue ipqueue, final ILaunchConfiguration configuration) {
		jobName.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent arg0) {
				fireContentsChanged();
			}
		});

		jobDescription.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent arg0) {
				fireContentsChanged();
			}
		});

		jobNativeSpecification.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent arg0) {
				fireContentsChanged();
			}
		});

		minCPUs.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent arg0) {
				if (minCPUs.getSelection() > maxCPUs.getSelection()) {
					final int swap = maxCPUs.getSelection();
					maxCPUs.setSelection(minCPUs.getSelection());
					minCPUs.setSelection(swap);
				}
				fireContentsChanged();
			}
		});

		maxCPUs.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent arg0) {
				if (minCPUs.getSelection() > maxCPUs.getSelection()) {
					final int swap = maxCPUs.getSelection();
					maxCPUs.setSelection(minCPUs.getSelection());
					minCPUs.setSelection(swap);
				}
				fireContentsChanged();
			}
		});

		machinesAll.addMouseListener(new MouseListener() {
			public void mouseDoubleClick(MouseEvent arg0) {
				for (final String s : machinesAll.getSelection()) {
					if (machinesPreferred.indexOf(s) == -1) {
						machinesPreferred.add(s);
					}
				}
				fireContentsChanged();
			}

			public void mouseDown(MouseEvent arg0) {
			}

			public void mouseUp(MouseEvent arg0) {
			}
		});

		machinesPreferred.addMouseListener(new MouseListener() {
			public void mouseDoubleClick(MouseEvent arg0) {
				machinesPreferred.remove(machinesPreferred.getSelectionIndices());
				fireContentsChanged();
			}

			public void mouseDown(MouseEvent arg0) {
			}

			public void mouseUp(MouseEvent arg0) {
			}
		});

		cboxMin.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}

			public void widgetSelected(SelectionEvent arg0) {
				minCPUs.setEnabled(cboxMin.getSelection());
				fireContentsChanged();
			}
		});

		cboxMax.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}

			public void widgetSelected(SelectionEvent arg0) {
				maxCPUs.setEnabled(cboxMax.getSelection());
				fireContentsChanged();
			}
		});

		queue.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent arg0) {
				fireContentsChanged();
			}
		});

		cboxMake.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent arg0) {

			}

			public void widgetSelected(SelectionEvent arg0) {
				cboxCustomMake.setEnabled(cboxMake.getSelection());
				customMake.setEnabled(cboxMake.getSelection() && cboxCustomMake.getSelection());
				fireContentsChanged();
			}
		});

		cboxCustomMake.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent arg0) {
			}

			public void widgetSelected(SelectionEvent arg0) {
				customMake.setEnabled(cboxMake.getSelection() && cboxCustomMake.getSelection());
				fireContentsChanged();
			}
		});

		customMake.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent arg0) {
				fireContentsChanged();
			}
		});

		application.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent arg0) {
				widgetSelected(arg0);
			}

			public void widgetSelected(SelectionEvent arg0) {
				fireContentsChanged();
			}
		});
	}

	/**
	 * Called only by creating a new Launch
	 */
	public RMLaunchValidation setDefaults(ILaunchConfigurationWorkingCopy configuration, IResourceManager rm, IPQueue queue) {

		configuration.setAttribute(ElementAttributes.getNameAttributeDefinition().getId(), ""); //$NON-NLS-1$

		configuration.removeAttribute(JobAttributes.getWorkingDirectoryAttributeDefinition().getId());

		return new RMLaunchValidation(true, null);
	}
}
