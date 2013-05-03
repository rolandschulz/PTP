/*******************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.services.ui.widgets;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ptp.internal.services.ui.messages.Messages;
import org.eclipse.ptp.services.core.IService;
import org.eclipse.ptp.services.core.IServiceCategory;
import org.eclipse.ptp.services.core.IServiceConfiguration;
import org.eclipse.ptp.services.core.IServiceProvider;
import org.eclipse.ptp.services.core.IServiceProviderDescriptor;
import org.eclipse.ptp.services.core.IServiceProviderWorkingCopy;
import org.eclipse.ptp.services.core.ServiceModelManager;
import org.eclipse.ptp.services.internal.core.ServiceConfiguration;
import org.eclipse.ptp.services.ui.IServiceProviderContributor;
import org.eclipse.ptp.services.ui.ServiceModelUIManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

/**
 * A widget for configuring the services providers for a given service
 * configuration.
 * 
 * Displays a table containing the service providers grouped by category on the
 * left part of the widget. When a provider is selected, the right hand part of
 * the widget will display the provider configuration along with a check box to
 * enable/disable the provider and a combo to select a different provider for
 * the service.
 * 
 * The configuration UI for the selected provider is supplied by the
 * providerContributor extension point.
 * 
 */
public class ServiceProviderConfigurationWidget extends Composite {

	// Keys for data attached to TreeItems that represent services
	private static final String SERVICE_KEY = "service"; // IService //$NON-NLS-1$
	private static final String DISABLED_KEY = "disabled"; // Boolean //$NON-NLS-1$
	private static final String PROVIDER_KEY = "provider"; // IServiceProvider //$NON-NLS-1$

	private IServiceConfiguration configuration;

	private final Tree servicesTree;
	private final Button enabledCheckbox;
	private final Label providerLabel;
	private final Combo providerCombo;
	private final Composite configurationComposite;
	private final Group providerComposite;
	private final StackLayout stackLayout;

	private final Image enabledIcon;
	private final Image disabledIcon;
	private final Image configIcon;

	private final ListenerList fSelectionListeners = new ListenerList();

	private static Comparator<IServiceCategory> CATEGORY_COMPARATOR = new Comparator<IServiceCategory>() {
		public int compare(IServiceCategory x, IServiceCategory y) {
			return x.getName().compareTo(y.getName());
		}
	};

	private static Comparator<IService> SERVICE_COMPARATOR = new Comparator<IService>() {
		public int compare(IService x, IService y) {
			return comparePriorities(x.getPriority(), y.getPriority(), x.getName(), y.getName());
		}
	};

	private static Comparator<IServiceProviderDescriptor> PROVIDER_COMPARATOR = new Comparator<IServiceProviderDescriptor>() {
		public int compare(IServiceProviderDescriptor x, IServiceProviderDescriptor y) {
			return comparePriorities(x.getPriority(), y.getPriority(), x.getName(), y.getName());
		}
	};

	private static int comparePriorities(Integer p1, Integer p2, String name1, String name2) {
		// sort by priority but fall back on sorting alphabetically
		if (p1 == null && p2 == null)
			return name1.compareTo(name2);
		if (p1 == null)
			return -1;
		if (p2 == null)
			return 1;
		if (p1.equals(p2)) {
			return name1.compareTo(name2);
		}
		return p1.compareTo(p2);
	}

	private static boolean filterOut(Set<String> serviceIds, Set<String> filterIds) {
		if (serviceIds.isEmpty() || filterIds.isEmpty())
			return false;

		for (String id : serviceIds) {
			if (filterIds.contains(id)) {
				return false;
			}
		}
		return true;
	}

	public ServiceProviderConfigurationWidget(Composite parent, int style) {
		super(parent, style);

		GridLayout bodyLayout = new GridLayout(2, false);
		bodyLayout.marginHeight = 0;
		bodyLayout.marginWidth = 0;
		setLayout(bodyLayout);

		Label label = new Label(this, SWT.NONE);
		GridData labelData = new GridData(SWT.FILL, SWT.TOP, true, false);
		labelData.horizontalSpan = 2;
		label.setLayoutData(labelData);
		label.setText(Messages.ServiceProviderConfigurationWidget_0);

		servicesTree = new Tree(this, SWT.BORDER | SWT.SINGLE);
		GridData servicesTreeData = new GridData(SWT.FILL, SWT.FILL, false, true);
		servicesTreeData.widthHint = 150;
		servicesTree.setLayoutData(servicesTreeData);
		servicesTree.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				TreeItem[] items = servicesTree.getSelection();
				TreeItem item = null;
				if (items.length > 0) {
					item = items[0];
				}
				displayService(item);
				notifySelection(e);
			}
		});

		providerComposite = new Group(this, SWT.SHADOW_ETCHED_IN);
		providerComposite.setLayout(new GridLayout(1, false));
		providerComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		Composite selectionComposite = new Composite(providerComposite, SWT.NONE);
		selectionComposite.setLayout(new GridLayout(1, false));
		selectionComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		enabledCheckbox = new Button(selectionComposite, SWT.CHECK);
		enabledCheckbox.setText(Messages.ServiceProviderConfigurationWidget_1);
		enabledCheckbox.setLayoutData(new GridData());
		enabledCheckbox.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				boolean checked = enabledCheckbox.getSelection();
				changeServiceState(!checked);
			}
		});
		enabledCheckbox.setEnabled(false);

		providerLabel = new Label(selectionComposite, SWT.NONE);
		providerLabel.setText(Messages.ServiceProviderConfigurationWidget_2);
		providerLabel.setLayoutData(new GridData());
		providerLabel.setEnabled(false);

		providerCombo = new Combo(selectionComposite, SWT.DROP_DOWN | SWT.READ_ONLY);
		GridData providerComboData = new GridData(SWT.FILL, SWT.FILL, true, true);
		providerComboData.widthHint = 200;
		providerCombo.setLayoutData(providerComboData);
		providerCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				IServiceProviderDescriptor[] descriptors = (IServiceProviderDescriptor[]) providerCombo.getData();
				selectProvider(descriptors[providerCombo.getSelectionIndex()]);
			}
		});
		providerCombo.setEnabled(false);

		Label separator = new Label(selectionComposite, SWT.SEPARATOR | SWT.SHADOW_OUT | SWT.HORIZONTAL);
		separator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		configurationComposite = new Composite(providerComposite, SWT.NONE);
		GridData configurationCompositeData = new GridData(GridData.FILL_BOTH);
		configurationCompositeData.horizontalSpan = 2;
		configurationComposite.setLayoutData(configurationCompositeData);
		stackLayout = new StackLayout();
		stackLayout.marginHeight = 0;
		stackLayout.marginWidth = 0;
		configurationComposite.setLayout(stackLayout);

		// TODO make this better using an ImageRegistry
		enabledIcon = new Image(getDisplay(), getClass().getResourceAsStream("/icons/etool16/service.gif")); //$NON-NLS-1$
		disabledIcon = new Image(getDisplay(), getClass().getResourceAsStream("/icons/etool16/service-disabled.gif")); //$NON-NLS-1$
		configIcon = new Image(getDisplay(), getClass().getResourceAsStream("/icons/etool16/service-category.gif")); //$NON-NLS-1$

		addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				enabledIcon.dispose();
				disabledIcon.dispose();
				configIcon.dispose();
			}
		});
	}

	/**
	 * Adds the listener to the collection of listeners who will be notified
	 * when the users selects a service configuration </p>
	 * 
	 * @param listener
	 *            the listener that will be notified of the selection
	 */
	public void addSelectionListener(SelectionListener listener) {
		fSelectionListeners.add(listener);
	}

	public void applyChangesToConfiguration() {
		if (configuration == null) {
			return;
		}

		for (TreeItem categoryTreeItem : servicesTree.getItems()) {
			for (TreeItem serviceTreeItem : categoryTreeItem.getItems()) {
				IService service = (IService) serviceTreeItem.getData(SERVICE_KEY);
				boolean disabled = Boolean.TRUE.equals(serviceTreeItem.getData(DISABLED_KEY));

				if (disabled) {
					configuration.disable(service);
				} else {
					IServiceProviderWorkingCopy serviceProvider = (IServiceProviderWorkingCopy) serviceTreeItem
							.getData(PROVIDER_KEY);
					if (serviceProvider != null) {
						if (serviceProvider.isDirty()) {
							serviceProvider.save();
						}
						IServiceProvider current = configuration.getServiceProvider(service);
						if (current == null || !current.getId().equals(serviceProvider.getId())) {
							configuration.setServiceProvider(service, serviceProvider.getOriginal());
						}
					}
				}
			}
		}
	}

	/**
	 * Returns the service configuration object that is being displayed by this
	 * widget. In order for the changes made by the user to be reflected in the
	 * configuration the applyChangesToConfiguration() method must be called
	 * first.
	 */
	public IServiceConfiguration getServiceConfiguration() {
		return configuration;
	}

	/**
	 * Removes the listener from the collection of listeners who will be
	 * notified when a service configuration is selected by the user.
	 * 
	 * @param listener
	 *            the listener which will no longer be notified
	 */
	public void removeSelectionListener(SelectionListener listener) {
		fSelectionListeners.remove(listener);
	}

	/**
	 * Causes the tree to display all the services that are available in the
	 * system. Services that are not part of the given service configuration
	 * will be shown as disabled. Services that are part of the given
	 * configuration will show as enabled.
	 * 
	 * Any changes made by the user will only be applied to the given
	 * configuration when the applyChangesToConfiguration() method is called.
	 */
	public void setServiceConfiguration(IServiceConfiguration conf) {
		setServiceConfiguration(conf, null);
	}

	/**
	 * Causes the tree to display all the services that are available in the
	 * system. Services that are not part of the given service configuration
	 * will be shown as disabled. Services that are part of the given
	 * configuration will show as enabled.
	 * 
	 * Any changes made by the user will only be applied to the given
	 * configuration when the applyChangesToConfiguration() method is called.
	 * 
	 * Additionally the services tree will be filtered to exclude services that
	 * do not apply to the given set of project nature IDs. This is useful when
	 * the widget is used as part of a project properties page as only the
	 * services that apply to the project will be shown.
	 */
	public void setServiceConfiguration(IServiceConfiguration configuration, Set<String> natureIds) {
		this.configuration = configuration;
		createTreeContent(natureIds);
		displayService(null);
	}

	private TreeItem createTreeCategory(Tree parent, IServiceCategory category) {
		TreeItem item = new TreeItem(servicesTree, SWT.NONE);
		item.setText(category == null ? Messages.ServiceProviderConfigurationWidget_3 : category.getName());
		item.setImage(configIcon);
		return item;
	}

	private void createTreeContent(Set<String> filterNatureIds) {
		servicesTree.removeAll();
		if (configuration == null) {
			return;
		}

		if (filterNatureIds == null) {
			filterNatureIds = Collections.emptySet();
		}

		SortedSet<IService> defaultCategoryServices = new TreeSet<IService>(SERVICE_COMPARATOR);
		SortedMap<IServiceCategory, SortedSet<IService>> categoryServices = new TreeMap<IServiceCategory, SortedSet<IService>>(
				CATEGORY_COMPARATOR);

		for (IService service : ServiceModelManager.getInstance().getServices()) {
			if (filterOut(service.getNatures(), filterNatureIds)) {
				continue;
			}

			IServiceCategory category = service.getCategory();
			if (category == null) {
				defaultCategoryServices.add(service);
			} else {
				SortedSet<IService> services = categoryServices.get(category);
				if (services == null) {
					services = new TreeSet<IService>(SERVICE_COMPARATOR);
					categoryServices.put(category, services);
				}
				services.add(service);
			}
		}

		for (Map.Entry<IServiceCategory, SortedSet<IService>> entry : categoryServices.entrySet()) {
			TreeItem parent = createTreeCategory(servicesTree, entry.getKey());
			for (IService service : entry.getValue()) {
				createTreeService(parent, service);
			}
			parent.setExpanded(true);
		}

		if (!defaultCategoryServices.isEmpty()) {
			TreeItem parent = createTreeCategory(servicesTree, null);
			for (IService service : defaultCategoryServices) {
				createTreeService(parent, service);
			}
			parent.setExpanded(true);
		}
	}

	private void createTreeService(TreeItem parent, IService service) {
		boolean disabled = configuration.isDisabled(service);
		TreeItem child = new TreeItem(parent, SWT.NONE);
		child.setText(service.getName());
		child.setData(SERVICE_KEY, service);
		child.setData(DISABLED_KEY, disabled);
		child.setImage(disabled ? disabledIcon : enabledIcon);
	}

	private void displayService(TreeItem serviceTreeItem) {
		// Each tree item represents a service
		if (serviceTreeItem != null) {
			IService service = (IService) serviceTreeItem.getData(SERVICE_KEY);
			IServiceProvider provider = (IServiceProvider) serviceTreeItem.getData(PROVIDER_KEY);

			// clear everything out
			providerCombo.removeAll();
			enabledCheckbox.setEnabled(false);
			providerLabel.setEnabled(false);
			stackLayout.topControl = null;
			configurationComposite.layout();

			// if the user selected a category node then nothing else needed
			if (service == null) {
				return;
			}

			// get the service provider that has been selected
			if (provider == null && !configuration.isDisabled(service)) {
				provider = configuration.getServiceProvider(service);
			}

			boolean disabled = Boolean.TRUE.equals(serviceTreeItem.getData(DISABLED_KEY));

			// populate the provider combo
			Set<IServiceProviderDescriptor> providers = service.getProviders();
			// it's possible there are no providers
			if (providers.size() != 0) {
				IServiceProviderDescriptor[] descriptors = providers.toArray(new IServiceProviderDescriptor[0]);
				Arrays.sort(descriptors, PROVIDER_COMPARATOR);

				int selection = 0;
				for (int i = 0; i < descriptors.length; i++) {
					providerCombo.add(descriptors[i].getName());
					if (provider != null && provider.getId().equals(descriptors[i].getId())) {
						selection = i;
					}
				}
				providerCombo.setData(descriptors);
				providerCombo.select(selection);
				if (!disabled) {
					selectProvider(descriptors[selection]);
				}
			}

			// set the enabled/disabled state appropriately
			providerCombo.setEnabled(!disabled);
			providerLabel.setEnabled(!disabled);
			enabledCheckbox.setSelection(!disabled);
			enabledCheckbox.setEnabled(true);
		} else {
			providerCombo.removeAll();
			enabledCheckbox.setEnabled(false);
			providerLabel.setEnabled(false);
			stackLayout.topControl = null;
			configurationComposite.layout();
		}
	}

	/**
	 * Returns the active provider if there is one or returns one of the former
	 * providers if possible.
	 */
	private IServiceProvider getExistingProvider(String providerId, IService service) {
		IServiceProvider setProvider = configuration.getServiceProvider(service);
		if (setProvider != null && providerId.equals(setProvider.getId())) {
			return setProvider;
		}

		if (configuration instanceof ServiceConfiguration) {
			for (IServiceProvider formerProvider : ((ServiceConfiguration) configuration).getFormerServiceProviders(service)) {
				if (providerId.equals(formerProvider.getId())) {
					return formerProvider;
				}
			}
		}
		return null;
	}

	/**
	 * Notify all listeners of the selection.
	 * 
	 * @param e
	 *            event that was generated by the selection
	 */
	private void notifySelection(SelectionEvent e) {
		Event newEvent = new Event();
		newEvent.item = e.item;
		newEvent.x = e.x;
		newEvent.y = e.y;
		newEvent.width = e.width;
		newEvent.height = e.height;
		newEvent.detail = e.detail;
		newEvent.stateMask = e.stateMask;
		newEvent.text = e.text;
		newEvent.doit = e.doit;
		newEvent.widget = this;
		SelectionEvent event = new SelectionEvent(newEvent);
		for (Object listener : fSelectionListeners.getListeners()) {
			((SelectionListener) listener).widgetSelected(event);
		}
	}

	private void selectProvider(final IServiceProviderDescriptor descriptor) {
		TreeItem serviceTreeItem = servicesTree.getSelection()[0];
		IServiceProviderWorkingCopy newProvider = (IServiceProviderWorkingCopy) serviceTreeItem.getData(PROVIDER_KEY);
		if (newProvider == null || !newProvider.getId().equals(descriptor.getId())) {
			IService service = (IService) serviceTreeItem.getData(SERVICE_KEY);
			IServiceProvider existingProvider = getExistingProvider(descriptor.getId(), service);
			if (existingProvider == null) {
				existingProvider = ServiceModelManager.getInstance().getServiceProvider(descriptor);
			}
			newProvider = existingProvider.copy();
			serviceTreeItem.setData(PROVIDER_KEY, newProvider);
		}

		Composite comp = new Composite(configurationComposite, SWT.NONE);
		GridLayout layout = new GridLayout(1, false);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		comp.setLayout(layout);

		ServiceModelUIManager uim = ServiceModelUIManager.getInstance();
		final IServiceProviderContributor contributor = uim.getServiceProviderContributor(descriptor);

		if (contributor != null) {
			contributor.configureServiceProvider(newProvider, comp);

			/*
			 * If no service provider configuration UI is provided, see if there
			 * is a wizard and use that.
			 */
			if (comp.getChildren().length == 0 && contributor != null) {
				final IWizard wizard = contributor.getWizard(newProvider, null);
				if (wizard != null) {
					Button button = new Button(comp, SWT.PUSH);
					button.setText(Messages.ServiceProviderConfigurationWidget_4);
					button.addSelectionListener(new SelectionAdapter() {

						/*
						 * (non-Javadoc)
						 * 
						 * @see
						 * org.eclipse.swt.events.SelectionAdapter#widgetSelected
						 * (org.eclipse.swt.events.SelectionEvent)
						 */
						@Override
						public void widgetSelected(SelectionEvent e) {
							WizardDialog dialog = new WizardDialog(getShell(), wizard);
							dialog.open();
						}
					});
				}
			}
		}

		stackLayout.topControl = comp;
		configurationComposite.layout();
	}

	protected void changeServiceState(boolean disabled) {
		TreeItem serviceTreeItem = servicesTree.getSelection()[0];
		serviceTreeItem.setData(DISABLED_KEY, disabled);
		serviceTreeItem.setImage(disabled ? disabledIcon : enabledIcon);
		displayService(serviceTreeItem);
	}
}
