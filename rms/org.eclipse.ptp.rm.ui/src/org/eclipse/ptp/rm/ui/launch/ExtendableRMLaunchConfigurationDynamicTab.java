package org.eclipse.ptp.rm.ui.launch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.ptp.core.attributes.IAttribute;
import org.eclipse.ptp.core.elements.IPQueue;
import org.eclipse.ptp.core.elements.IResourceManager;
import org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationContentsChangedListener;
import org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab;
import org.eclipse.ptp.launch.ui.extensions.RMLaunchValidation;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

public abstract class ExtendableRMLaunchConfigurationDynamicTab
		extends
		org.eclipse.ptp.launch.ui.extensions.AbstractRMLaunchConfigurationDynamicTab
		implements IRMLaunchConfigurationContentsChangedListener {

	private final List<AbstractRMLaunchConfigurationDynamicTab> tabControllers = new ArrayList<AbstractRMLaunchConfigurationDynamicTab>();
	private Composite control;

	protected void addDynamicTab(AbstractRMLaunchConfigurationDynamicTab tabController) {
		tabControllers.add(tabController);
		tabController.addContentsChangedListener(this);
	}

	public RMLaunchValidation canSave(Control control, IResourceManager rm, IPQueue queue) {
		for (AbstractRMLaunchConfigurationDynamicTab tabControl : tabControllers) {
			RMLaunchValidation validation = tabControl.canSave(control, rm, queue);
			if (! validation.isSuccess()) {
				return validation;
			}
		}
		return new RMLaunchValidation(true, null);
	}

	public RMLaunchValidation isValid(ILaunchConfiguration launchConfig, IResourceManager rm, IPQueue queue) {
		for (AbstractRMLaunchConfigurationDynamicTab tabControl : tabControllers) {
			RMLaunchValidation validation = tabControl.isValid(launchConfig, rm, queue);
			if (! validation.isSuccess()) {
				return validation;
			}
		}
		return new RMLaunchValidation(true, null);
	}

	public void createControl(Composite parent, IResourceManager rm, IPQueue queue)
			throws CoreException {
				control = new Composite(parent, SWT.NONE);
				GridLayout layout = new GridLayout();
				control.setLayout(layout);

				final TabFolder tabFolder = new TabFolder(control, SWT.NONE);
				tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

				for (AbstractRMLaunchConfigurationDynamicTab tabControl : tabControllers) {
					final TabItem simpleTabItem = new TabItem(tabFolder, SWT.NONE);
					tabControl.createControl(tabFolder, rm, queue);
					simpleTabItem.setText(tabControl.getText());
					simpleTabItem.setImage(tabControl.getImage());
					simpleTabItem.setControl(tabControl.getControl());
				}
			}

	public Control getControl() {
		return control;
	}

	public RMLaunchValidation initializeFrom(Control control, IResourceManager rm, IPQueue queue,
			ILaunchConfiguration configuration) {
				RMLaunchValidation resultValidation = new RMLaunchValidation(true, null);
				for (AbstractRMLaunchConfigurationDynamicTab tabControl : tabControllers) {
					RMLaunchValidation validation = tabControl.initializeFrom(control, rm, queue, configuration);
					if (! validation.isSuccess()) {
						resultValidation = validation;
					}
				}
				return resultValidation;
			}

	public RMLaunchValidation performApply(ILaunchConfigurationWorkingCopy configuration, IResourceManager rm, IPQueue queue) {
		RMLaunchValidation resultValidation = new RMLaunchValidation(true, null);
		for (AbstractRMLaunchConfigurationDynamicTab tabControl : tabControllers) {
			RMLaunchValidation validation = tabControl.performApply(configuration, rm, queue);
			if (! validation.isSuccess()) {
				resultValidation = validation;
			}
		}
		return resultValidation;
	}

	public RMLaunchValidation setDefaults(ILaunchConfigurationWorkingCopy configuration, IResourceManager rm, IPQueue queue) {
		RMLaunchValidation resultValidation = new RMLaunchValidation(true, null);
		for (AbstractRMLaunchConfigurationDynamicTab tabControl : tabControllers) {
			RMLaunchValidation validation = tabControl.setDefaults(configuration, rm, queue);
			if (! validation.isSuccess()) {
				resultValidation = validation;
			}
		}
		return resultValidation;
	}

	public IAttribute<?, ?, ?>[] getAttributes(IResourceManager rm, IPQueue queue, ILaunchConfiguration configuration,
			String mode) throws CoreException {
				List<IAttribute<?, ?, ?>> attributes = new ArrayList<IAttribute<?, ?, ?>>();
				for (AbstractRMLaunchConfigurationDynamicTab tabControl : tabControllers) {
					List<IAttribute<?, ?, ?>> attributesList = Arrays.asList(tabControl.getAttributes(rm, queue, configuration, mode));
					if (attributesList != null) {
						attributes.addAll(attributesList);
					}
				}
				return attributes.toArray(new IAttribute<?, ?, ?>[attributes.size()]);
			}

	public void updateControls() {
		for (AbstractRMLaunchConfigurationDynamicTab tabControl : tabControllers) {
			tabControl.updateControls();
		}
	}

	public void handleContentsChanged(IRMLaunchConfigurationDynamicTab factory) {
		fireContentsChanged();
	}
}
