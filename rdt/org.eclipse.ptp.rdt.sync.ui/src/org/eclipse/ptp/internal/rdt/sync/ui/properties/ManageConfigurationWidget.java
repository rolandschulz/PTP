/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.internal.rdt.sync.ui.properties;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ptp.internal.rdt.sync.ui.SynchronizePropertiesRegistry;
import org.eclipse.ptp.internal.rdt.sync.ui.messages.Messages;
import org.eclipse.ptp.internal.rdt.sync.ui.wizards.AddSyncConfigWizard;
import org.eclipse.ptp.rdt.sync.core.SyncConfig;
import org.eclipse.ptp.rdt.sync.core.SyncConfigManager;
import org.eclipse.ptp.rdt.sync.ui.ISynchronizeProperties;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Tree;

/**
 * @author greg
 * 
 */
public class ManageConfigurationWidget extends Composite {
	private static class FontStyler extends Styler {
		private final String fFont;

		public FontStyler(String font) {
			fFont = font;
		}

		@Override
		public void applyStyles(TextStyle textStyle) {
			FontRegistry registry = new FontRegistry();
			textStyle.font = registry.getBold(fFont);
		}
	}

	private class SyncConfigContentProvider implements ITreeContentProvider {
		@Override
		public void dispose() {
			// Nothing
		}

		@Override
		public Object[] getChildren(Object parentElement) {
			if (parentElement instanceof SyncConfig) {
				SyncConfig config = (SyncConfig) parentElement;
				String[] children = new String[] { Messages.ManageConfigurationWidget_Connection_name + config.getConnectionName(),
						Messages.ManageConfigurationWidget_Project_location + config.getLocation(),
						Messages.ManageConfigurationWidget_Sync_provider + config.getSyncService().getName() };
				return children;
			}
			return null;
		}

		@Override
		public Object[] getElements(Object inputElement) {
			return fConfigs.toArray();
		}

		@Override
		public Object getParent(Object element) {
			return null;
		}

		@Override
		public boolean hasChildren(Object element) {
			return (element instanceof SyncConfig);
		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			// Nothing
		}

	}

	private class SyncConfigLabelProvider extends StyledCellLabelProvider {
		@Override
		public void update(ViewerCell cell) {
			Object element = cell.getElement();
			StyledString styledString = new StyledString();
			if (element instanceof SyncConfig) {
				SyncConfig config = (SyncConfig) element;
				styledString.append(config.getName());
				if (SyncConfigManager.isActive(getProject(), config)) {
					styledString.setStyle(0, styledString.length(), new FontStyler(Display.getCurrent().getSystemFont()
							.getFontData()[0].getName()));
				}
			} else if (element instanceof String) {
				String str = (String) element;
				int pos = str.indexOf(":"); //$NON-NLS-1$
				if (pos >= 0) {
					styledString = new StyledString();
					styledString.append(str.substring(0, pos + 1), StyledString.DECORATIONS_STYLER);
					styledString.append(str.substring(pos + 1, str.length()));
				}
			}
			cell.setText(styledString.getString());
			cell.setStyleRanges(styledString.getStyleRanges());

			super.update(cell);
		}
	}

	private IProject fProject;

	private final Button fAddButton;
	private final TreeViewer fTreeViewer;
	private final Composite fUserDefinedRegion;

	private final Set<SyncConfig> fConfigs = new TreeSet<SyncConfig>();
	private final Set<SyncConfig> fAddedConfigs = new HashSet<SyncConfig>();
	private final Set<SyncConfig> fRemovedConfigs = new HashSet<SyncConfig>();

	public ManageConfigurationWidget(final Composite parent, int style) {
		super(parent, style);

		setLayout(new GridLayout(1, false));
		Composite main = new Composite(this, SWT.NONE);
		main.setLayout(new GridLayout(2, false));
		main.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		fTreeViewer = new TreeViewer(main, SWT.FULL_SELECTION | SWT.MULTI | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		fTreeViewer.setContentProvider(new SyncConfigContentProvider());
		fTreeViewer.setLabelProvider(new SyncConfigLabelProvider());
		fTreeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				ISelection selection = event.getSelection();
				if (selection instanceof IStructuredSelection) {
					ISynchronizeProperties prop = SynchronizePropertiesRegistry.getSynchronizePropertiesForProject(getProject());
					if (prop != null) {
						prop.disposePropertiesConfigurationArea();
						IStructuredSelection sel = (IStructuredSelection) selection;
						if (sel.size() == 1) {
							Object obj = sel.getFirstElement();
							if (obj instanceof SyncConfig) {
								prop.createPropertiesConfigurationArea(fUserDefinedRegion, (SyncConfig) obj);
								fUserDefinedRegion.layout();
							}
						}
					}
				}
			}
		});
		Tree tree = fTreeViewer.getTree();
		tree.setLinesVisible(false);
		tree.setHeaderVisible(false);
		tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 3));

		fAddButton = new Button(main, SWT.NONE);
		fAddButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		fAddButton.setText(Messages.ManageConfigurationWidget_Add);
		fAddButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				AddSyncConfigWizard wizard = new AddSyncConfigWizard(getProject());
				WizardDialog dialog = new WizardDialog(getShell(), wizard);
				if (dialog.open() == Window.OK) {
					SyncConfig config = wizard.getSyncConfig();
					fAddedConfigs.add(config);
					fConfigs.add(config);
					fTreeViewer.refresh();
					ISynchronizeProperties prop = SynchronizePropertiesRegistry.getSynchronizePropertiesForProject(getProject());
					if (prop != null) {
						prop.addConfiguration(config);
					}
				}
			}
		});

		Button removeButton = new Button(main, SWT.NONE);
		removeButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		removeButton.setText(Messages.ManageConfigurationWidget_Remove);
		removeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection ss = (IStructuredSelection) fTreeViewer.getSelection();
				if (ss.size() == fConfigs.size()) {
					MessageDialog.openError(getShell(), Messages.ManageConfigurationWidget_Remove_Configuration,
							Messages.ManageConfigurationWidget_Must_be_at_least_one);
				} else if (!ss.isEmpty()) {
					for (Iterator<?> iterator = ss.iterator(); iterator.hasNext();) {
						Object el = iterator.next();
						if (el instanceof SyncConfig) {
							SyncConfig config = (SyncConfig) el;
							if (SyncConfigManager.isActive(getProject(), config)) {
								MessageDialog.openError(getShell(), Messages.ManageConfigurationWidget_Remove_Configuration,
										Messages.ManageConfigurationWidget_Cannot_remove_active);
							} else {
								if (fAddedConfigs.contains(config)) {
									fAddedConfigs.remove(config);
								} else {
									fRemovedConfigs.add(config);
								}
								fConfigs.remove(config);
							}
						}
					}
					fTreeViewer.refresh();
				}
			}
		});

		Button setActiveButton = new Button(main, SWT.NONE);
		setActiveButton.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false, 1, 1));
		setActiveButton.setText(Messages.ManageConfigurationWidget_Set_Active);
		setActiveButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection ss = (IStructuredSelection) fTreeViewer.getSelection();
				if (ss.size() > 1) {
					MessageDialog.openError(getShell(), Messages.ManageConfigurationWidget_Set_Active_Configuration,
							Messages.ManageConfigurationWidget_Only_one_configuration_active);
				} else if (!ss.isEmpty()) {
					for (Iterator<?> iterator = ss.iterator(); iterator.hasNext();) {
						Object el = iterator.next();
						if (el instanceof SyncConfig) {
							SyncConfig config = (SyncConfig) el;
							if (!SyncConfigManager.isActive(getProject(), config)) {
								SyncConfigManager.setActive(getProject(), config);
							}
						}
					}
					fTreeViewer.refresh();
				}
			}
		});

		Label rule = new Label(this, SWT.HORIZONTAL | SWT.SEPARATOR);
		rule.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

		fUserDefinedRegion = new Composite(this, SWT.NONE);
		fUserDefinedRegion.setLayout(new GridLayout(1, false));
		fUserDefinedRegion.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
	}

	public void commit() {
		if (!fAddedConfigs.isEmpty() || !fRemovedConfigs.isEmpty()) {
			SyncConfigManager.updateConfigs(getProject(), fAddedConfigs.toArray(new SyncConfig[0]),
					fRemovedConfigs.toArray(new SyncConfig[0]));
		}
	}

	public IProject getProject() {
		return fProject;
	}

	public void setDefaults() {
		for (SyncConfig config : fAddedConfigs) {
			fConfigs.remove(config);
		}
		for (SyncConfig config : fRemovedConfigs) {
			fConfigs.add(config);
		}
		fAddedConfigs.clear();
		fRemovedConfigs.clear();
		fTreeViewer.refresh();
	}

	public void setProject(IProject project) {
		fProject = project;
		SyncConfig[] configs = SyncConfigManager.getConfigs(project);
		fConfigs.addAll(Arrays.asList(configs));
		fTreeViewer.setInput(fConfigs);

		/*
		 * Select the active configuration. This ensures that the properties configuration area is layed out and the widget is
		 * properly sized.
		 */
		for (SyncConfig config : fConfigs) {
			if (SyncConfigManager.isActive(project, config)) {
				fTreeViewer.setSelection(new StructuredSelection(config));
				break;
			}
		}
	}
}
