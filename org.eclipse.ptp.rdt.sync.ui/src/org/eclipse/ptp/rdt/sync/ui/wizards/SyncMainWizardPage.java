/*******************************************************************************
 * Copyright (c) 2007, 2009 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Intel Corporation - initial API and implementation
 *     IBM Corporation
 *******************************************************************************/
package org.eclipse.ptp.rdt.sync.ui.wizards;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedMap;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.osgi.util.TextProcessor;
import org.eclipse.ptp.rdt.sync.ui.ISynchronizeParticipant;
import org.eclipse.ptp.rdt.sync.ui.ISynchronizeParticipantDescriptor;
import org.eclipse.ptp.rdt.sync.ui.SynchronizeParticipantRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.ide.IIDEHelpContextIds;
import org.eclipse.ui.internal.ide.dialogs.ProjectContentsLocationArea.IErrorMessageReporter;
import org.eclipse.cdt.ui.CDTSharedImages;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.newui.CDTPrefUtil;
import org.eclipse.cdt.ui.newui.PageLayout;
import org.eclipse.cdt.ui.wizards.CDTMainWizardPage;
import org.eclipse.cdt.ui.wizards.CNewWizard;
import org.eclipse.cdt.ui.wizards.CWizardHandler;
import org.eclipse.cdt.ui.wizards.EntryDescriptor;
import org.eclipse.cdt.ui.wizards.IWizardItemsListListener;
import org.eclipse.cdt.ui.wizards.IWizardWithMemory;
import org.eclipse.cdt.internal.ui.newui.Messages;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.ui.wizards.MBSWizardHandler;

	public class SyncMainWizardPage extends CDTMainWizardPage implements IWizardItemsListListener {
		public static final String REMOTE_SYNC_WIZARD_PAGE_ID = "org.eclipse.ptp.rdt.sync.ui.remoteSyncWizardPage"; //$NON-NLS-1$
		public static final String SERVICE_PROVIDER_PROPERTY = "org.eclipse.ptp.rdt.sync.ui.remoteSyncWizardPage.serviceProvider"; //$NON-NLS-1$
		private static String RDT_PROJECT_TYPE = "org.eclipse.ptp.rdt"; //$NON-NLS-1$
		private static final Image IMG_CATEGORY = CDTSharedImages.getImage(CDTSharedImages.IMG_OBJS_SEARCHFOLDER);
		private static final Image IMG_ITEM = CDTSharedImages.getImage(CDTSharedImages.IMG_OBJS_VARIABLE);

		public static final String PAGE_ID = "org.eclipse.cdt.managedbuilder.ui.wizard.NewModelProjectWizardPage"; //$NON-NLS-1$

		private static final String EXTENSION_POINT_ID = "org.eclipse.cdt.ui.CDTWizard"; //$NON-NLS-1$
		private static final String ELEMENT_NAME = "wizard"; //$NON-NLS-1$
		private static final String CLASS_NAME = "class"; //$NON-NLS-1$
		public static final String DESC = "EntryDescriptor"; //$NON-NLS-1$
	    private static final int SIZING_TEXT_FIELD_WIDTH = 250;

	    // widgets
	    private Text projectNameField;
	    private Text projectLocationField;
	    private Button browseButton;
		private Composite fProviderArea;
		private StackLayout fProviderStack;
	    private Tree localTree;
	    private Composite localToolChain;
	    private Composite remoteToolChain;
	    private Table remoteToolChainTable;
	    private Button show_sup;
	    private Label projectLocalOptionsLabel;
	    private Label projectRemoteOptionsLabel;
   
		private Label categorySelectedForLocalLabel;
		private Label categorySelectedForRemoteLabel;

		private SortedMap<String, IToolChain> toolChainMap;
		
		private boolean useDefaultLocalDirectory = true;
		private ISynchronizeParticipant fSelectedParticipant = null;

	    /**
	     * Creates a new project creation wizard page.
	     *
	     * @param pageName the name of this page
	     */
	    public SyncMainWizardPage(String pageName) {
	        super(pageName);
	        setPageComplete(false);
	    }

	    /** (non-Javadoc)
	     * Method declared on IDialogPage.
	     */
	    @Override
		public void createControl(Composite parent) {
	        Composite composite = new Composite(parent, SWT.NULL);

	        initializeDialogUnits(parent);
	        PlatformUI.getWorkbench().getHelpSystem().setHelp(composite, IIDEHelpContextIds.NEW_PROJECT_WIZARD_PAGE);

	        composite.setLayout(new GridLayout());
	        composite.setLayoutData(new GridData(GridData.FILL_BOTH));

	        createProjectBasicInfoGroup(composite);
	        setControl(composite);
	        
	        createProjectRemoteInfoGroup(composite);

	    	createProjectDetailedInfoGroup((Composite)getControl()); 
			this.switchTo(this.updateData(localTree, localToolChain, show_sup.getSelection(), SyncMainWizardPage.this, getWizard()), getDescriptor(localTree));

			setPageComplete(false);
	        setErrorMessage(null);
	        setMessage(null);
	        Dialog.applyDialogFont(composite);
	    }
	    
	    private void createProjectDetailedInfoGroup(Composite parent) {
	        Composite c = new Composite(parent, SWT.NONE);
	        c.setLayoutData(new GridData(GridData.FILL_BOTH));
	    	c.setLayout(new GridLayout(2, true));
	    	
	        Label left_label = new Label(c, SWT.NONE);
	        left_label.setText("Project Type"); //$NON-NLS-1$
	        left_label.setFont(parent.getFont());
	        left_label.setLayoutData(new GridData(GridData.BEGINNING));
	        
	        projectLocalOptionsLabel = new Label(c, SWT.NONE);
	        projectLocalOptionsLabel.setFont(parent.getFont());
	        projectLocalOptionsLabel.setLayoutData(new GridData(GridData.BEGINNING));
	        projectLocalOptionsLabel.setText("Local Toolchain"); //$NON-NLS-1$
	    	
	        localTree = new Tree(c, SWT.SINGLE | SWT.BORDER);
	        localTree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 3));
	        localTree.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					TreeItem[] tis = localTree.getSelection();
					if (tis == null || tis.length == 0) return;
					switchTo((CWizardHandler)tis[0].getData(), (EntryDescriptor)tis[0].getData(DESC));
					setPageComplete(validatePage());
				}});
	        localTree.getAccessible().addAccessibleListener(
					 new AccessibleAdapter() {                       
		                 @Override
						public void getName(AccessibleEvent e) {
		                	 for (int i = 0; i < localTree.getItemCount(); i++) {
		                		 if (localTree.getItem(i).getText().equals(e.result))
		                			 return;
		                	 }
	                         e.result = Messages.CMainWizardPage_0; 
		                 }
		             }
				 );

	        localToolChain = new Composite(c, SWT.NONE);
	        localToolChain.setLayoutData(new GridData(GridData.FILL_BOTH));
	        localToolChain.setLayout(new PageLayout());
	        
	        projectRemoteOptionsLabel = new Label(c, SWT.NONE);
	        projectRemoteOptionsLabel.setFont(parent.getFont());
	        projectRemoteOptionsLabel.setLayoutData(new GridData(GridData.BEGINNING));
	        projectRemoteOptionsLabel.setText("Remote Toolchain"); //$NON-NLS-1$

	        remoteToolChain = new Composite(c, SWT.NONE);
	        remoteToolChain.setLayoutData(new GridData(GridData.FILL_BOTH));
	        remoteToolChain.setLayout(new PageLayout());
	        remoteToolChainTable = new Table(remoteToolChain, SWT.MULTI | SWT.V_SCROLL | SWT.BORDER);
	        remoteToolChainTable.setVisible(true);


	        show_sup = new Button(c, SWT.CHECK);
	        show_sup.setText(Messages.CMainWizardPage_1); 
	        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
	        gd.horizontalSpan = 2;
	        show_sup.setLayoutData(gd);
	        show_sup.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					if (h_selected != null)
						h_selected.setSupportedOnly(show_sup.getSelection());
					switchTo(updateData(localTree, localToolChain, show_sup.getSelection(), SyncMainWizardPage.this, getWizard()), getDescriptor(localTree));
				}} );

	        // restore settings from preferences
			show_sup.setSelection(!CDTPrefUtil.getBool(CDTPrefUtil.KEY_NOSUPP));
	    }
	    
	    @Override
		public IWizardPage getNextPage() {
			return (h_selected == null) ? null : h_selected.getSpecificPage();
	    }		

	    /**
	     * Returns the current project location path as entered by 
	     * the user, or its anticipated initial value.
	     * Note that if the default has been returned the path
	     * in a project description used to create a project
	     * should not be set.
	     *
	     * TODO: Implement this method
	     * @return the project location path or its anticipated initial value.
	     */
	    @Override
	    public IPath getLocationPath() {
	        return new Path(""); //$NON-NLS-1$
	    }
	    
	    // TODO: Implement this method
	    @Override
	    public URI getLocationURI() {
	    	return null;
	    }

	    @Override
	    public URI getProjectLocation() {
	    	return useDefaults() ? null : getLocationURI();
	    }

	    @Override
		protected boolean validatePage() {
			if (!validateProject()) {
				return false;
			}
    		setMessage(null);

	        if (getProjectName().indexOf('#') >= 0) {
	            setErrorMessage(Messages.CDTMainWizardPage_0);	             
	            return false;
	        }
	        
	        boolean bad = true; // should we treat existing project as error
	        
	        IProject handle = getProjectHandle();
	        if (handle.exists()) {
	        	if (getWizard() instanceof IWizardWithMemory) {
	        		IWizardWithMemory w = (IWizardWithMemory)getWizard();
	        		if (w.getLastProjectName() != null && w.getLastProjectName().equals(getProjectName()))
	        			bad = false;
	        	}
	        	if (bad) { 
	        		setErrorMessage(Messages.CMainWizardPage_10); 
	        	    return false;
	        	}
	        }

	        if (bad) { // skip this check if project already created 
	        	try {
	        		IFileStore fs;
		        	URI p = getProjectLocation();
		        	if (p == null) {
		        		fs = EFS.getStore(ResourcesPlugin.getWorkspace().getRoot().getLocationURI());
		        		fs = fs.getChild(getProjectName());
		        	} else
		        		fs = EFS.getStore(p);
	        		IFileInfo f = fs.fetchInfo();
		        	if (f.exists()) {
		        		if (f.isDirectory()) {
		        			setMessage(Messages.CMainWizardPage_7, IMessageProvider.WARNING); 
		        		} else {
		        			setErrorMessage(Messages.CMainWizardPage_6); 
		        			return false;
		        		}
		        	}
	        	} catch (CoreException e) {
	        		CUIPlugin.log(e.getStatus());
	        	}
	        }
	        
	        if (!useDefaults()) {
	            IStatus locationStatus = ResourcesPlugin.getWorkspace().validateProjectLocationURI(handle,
	            		getLocationURI());
	            if (!locationStatus.isOK()) {
	                setErrorMessage(locationStatus.getMessage());
	                return false;
	            }
	        }

	        if (localTree.getItemCount() == 0) {
	        	setErrorMessage(Messages.CMainWizardPage_3); 
	        	return false;
	        }
	        
	        // it is not an error, but we cannot continue
	        if (h_selected == null) {
	            setErrorMessage(null);
		        return false;	        	
	        }

	        String s = h_selected.getErrorMessage(); 
			if (s != null) {
        		setErrorMessage(s);
        		return false;
	        }
	        
            setErrorMessage(null);
	        return true;
	    }

	    /**
	     * 
	     * @param tree
	     * @param right
	     * @param show_sup
	     * @param ls
	     * @param wizard
	     * @return : selected Wizard Handler.
	     */
		public CWizardHandler updateData(Tree tree, Composite right, boolean show_sup, IWizardItemsListListener ls, IWizard wizard) {
			// remember selected item
			TreeItem[] selection = tree.getSelection();
			TreeItem selectedItem = selection.length>0 ? selection[0] : null; 
			String savedLabel = selectedItem!=null ? selectedItem.getText() : null;
			String savedParentLabel = getParentText(selectedItem);
			
			tree.removeAll();
			IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(EXTENSION_POINT_ID);
			if (extensionPoint == null) return null;
			IExtension[] extensions = extensionPoint.getExtensions();
			if (extensions == null) return null;
			
			List<EntryDescriptor> items = new ArrayList<EntryDescriptor>();
			for (int i = 0; i < extensions.length; ++i)	{
				IConfigurationElement[] elements = extensions[i].getConfigurationElements();
				for (IConfigurationElement element : elements) {
					if (element.getName().equals(ELEMENT_NAME)) {
						CNewWizard w = null;
						try {
							w = (CNewWizard) element.createExecutableExtension(CLASS_NAME);
						} catch (CoreException e) {
							System.out.println(Messages.CMainWizardPage_5 + e.getLocalizedMessage()); 
							return null; 
						}
						if (w == null) return null;
						w.setDependentControl(right, ls);
						for (EntryDescriptor ed : w.createItems(show_sup, wizard)) {
							items.add(ed);
						}
					}
				}
			}
			// If there is a EntryDescriptor which is default for category, make sure it 
			// is in the front of the list.
			for (int i = 0; i < items.size(); ++i)
			{
				EntryDescriptor ed = items.get(i);
				if (ed.isDefaultForCategory())
				{
					items.remove(i);
					items.add(0, ed);
					break;
				}				
			}
			
			// bug # 211935 : allow items filtering.
			if (ls != null) // NULL means call from prefs
				items = ls.filterItems(items);
			addItemsToTree(tree, items);
			
			if (tree.getItemCount() > 0) {
				TreeItem target = null;
				// try to search item which was selected before
				if (savedLabel!=null) {
					target = findItem(tree, savedLabel, savedParentLabel);
				}
				if (target == null) {
					target = tree.getItem(0);
					if (target.getItemCount() != 0)
						target = target.getItem(0);
				}
				tree.setSelection(target);
				return (CWizardHandler)target.getData();
			}
			return null;
		}

		private static String getParentText(TreeItem item) {
			if (item==null || item.getParentItem()==null)
				return ""; //$NON-NLS-1$
			return item.getParentItem().getText();
		}

		private static TreeItem findItem(Tree tree, String label, String parentLabel) {
			for (TreeItem item : tree.getItems()) {
				TreeItem foundItem = findTreeItem(item, label, parentLabel);
				if (foundItem!=null)
					return foundItem;
			}
			return null;
		}

		private static TreeItem findTreeItem(TreeItem item, String label, String parentLabel) {
			if (item.getText().equals(label) && getParentText(item).equals(parentLabel))
				return item;
			
			for (TreeItem child : item.getItems()) {
				TreeItem foundItem = findTreeItem(child, label, parentLabel);
				if (foundItem!=null)
					return foundItem;
			}
			return null;
		}

		private static void addItemsToTree(Tree tree, List<EntryDescriptor> items) {
		//  Sorting is disabled because of users requests	
		//	Collections.sort(items, CDTListComparator.getInstance());
			
			ArrayList<TreeItem> placedTreeItemsList = new ArrayList<TreeItem>(items.size());
			ArrayList<EntryDescriptor> placedEntryDescriptorsList = new ArrayList<EntryDescriptor>(items.size());
			for (EntryDescriptor wd : items) {
				if (wd.getParentId() == null) {
					wd.setPath(wd.getId());
					TreeItem ti = new TreeItem(tree, SWT.NONE);
					ti.setText(TextProcessor.process(wd.getName()));
					ti.setData(wd.getHandler());
					ti.setData(DESC, wd);
					ti.setImage(calcImage(wd));
					placedTreeItemsList.add(ti);
					placedEntryDescriptorsList.add(wd);
				}
			}
			while(true) {
				boolean found = false;
				Iterator<EntryDescriptor> it2 = items.iterator();
				while (it2.hasNext()) {
					EntryDescriptor wd1 = it2.next();
					if (wd1.getParentId() == null) continue;
					for (int i = 0; i< placedEntryDescriptorsList.size(); i++) {
						EntryDescriptor wd2 = placedEntryDescriptorsList.get(i);
						if (wd2.getId().equals(wd1.getParentId())) {
							found = true;
							wd1.setParentId(null);
							CWizardHandler h = wd2.getHandler();
							/* If neither wd1 itself, nor its parent (wd2) have a handler
							 * associated with them, and the item is not a category,
							 * then skip it. If it's category, then it's possible that
							 * children will have a handler associated with them.
							 */
							if (h == null && wd1.getHandler() == null && !wd1.isCategory())
								break;

							wd1.setPath(wd2.getPath() + "/" + wd1.getId()); //$NON-NLS-1$
							wd1.setParent(wd2);
							if (h != null) {
								if (wd1.getHandler() == null && !wd1.isCategory())
									wd1.setHandler((CWizardHandler)h.clone());
								if (!h.isApplicable(wd1))
									break;
							}
							
							TreeItem p = placedTreeItemsList.get(i);
							TreeItem ti = new TreeItem(p, SWT.NONE);
							ti.setText(wd1.getName());
							ti.setData(wd1.getHandler());
							ti.setData(DESC, wd1);
							ti.setImage(calcImage(wd1));
							placedTreeItemsList.add(ti);
							placedEntryDescriptorsList.add(wd1);
							break;
						}
					}
				}
				// repeat iterations until all items are placed.
				if (!found) break;
			}
			// orphan elements (with not-existing parentId) are ignored
		}

		private static Image calcImage(EntryDescriptor ed) {
			if (ed.getImage() != null) return ed.getImage();
			if (ed.isCategory()) return IMG_CATEGORY;
			return IMG_ITEM;
		}

		private void switchTo(CWizardHandler h, EntryDescriptor ed) {
			if (h == null) 
				h = ed.getHandler();
			if (ed.isCategory())
				h = null;
			try {
				if (h != null) 
					h.initialize(ed);
			} catch (CoreException e) { 
				h = null;
			}
			if (h_selected != null) 
				h_selected.handleUnSelection();
			h_selected = h;
			if (h == null) {
				if (ed.isCategory()) {
					if (categorySelectedForLocalLabel == null) {
						categorySelectedForLocalLabel = new Label(localToolChain, SWT.WRAP);
						categorySelectedForLocalLabel.setText(Messages.CDTMainWizardPage_1);  
						localToolChain.layout();
					}

					if (categorySelectedForRemoteLabel == null) {
						categorySelectedForRemoteLabel = new Label(remoteToolChain, SWT.WRAP);
						categorySelectedForRemoteLabel.setText(Messages.CDTMainWizardPage_1);  
						remoteToolChain.layout();
					}
				}
				return;
			}
			if (categorySelectedForLocalLabel != null)
				categorySelectedForLocalLabel.setVisible(false);
			if (categorySelectedForRemoteLabel != null)
				categorySelectedForRemoteLabel.setVisible(false);
			h_selected.handleSelection();
			h_selected.setSupportedOnly(show_sup.getSelection());
			
			toolChainMap = ((MBSWizardHandler) h_selected).getToolChains();
			remoteToolChainTable.removeAll();
			for (String toolChainName : toolChainMap.keySet()) {
				TableItem ti = new TableItem(remoteToolChainTable, SWT.NONE);
				ti.setText(toolChainName);
			}
			remoteToolChainTable.redraw();
		}
		
		public static EntryDescriptor getDescriptor(Tree _tree) {
			TreeItem[] sel = _tree.getSelection();
			if (sel == null || sel.length == 0) 
				return null;
			return (EntryDescriptor)sel[0].getData(DESC);
		}
		
		@Override
		public void toolChainListChanged(int count) {
			setPageComplete(validatePage());
			getWizard().getContainer().updateButtons();
		}

		@Override
		public boolean isCurrent() { return isCurrentPage(); }

		@Override
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public List filterItems(List items) {
            /*
             * Remove RDT project types as these will not work with synchronized
             * projects
             */
            Iterator iterator = items.iterator();

            List<EntryDescriptor> filteredList = new LinkedList<EntryDescriptor>();

            while (iterator.hasNext()) {
                    EntryDescriptor ed = (EntryDescriptor) iterator.next();
                    if (!ed.getId().startsWith(RDT_PROJECT_TYPE)) {
                            filteredList.add(ed);
                    }
            }

            return filteredList;
		}
		
		// Functions copied from WizardNewProjectCreationPage

	    /**
		 * Get an error reporter for the receiver.
		 * TODO: Use this - put an error reporter on the page!
		 * @return IErrorMessageReporter
		 */
		private IErrorMessageReporter getErrorReporter() {
			return new IErrorMessageReporter(){
				/* (non-Javadoc)
				 * @see org.eclipse.ui.internal.ide.dialogs.ProjectContentsLocationArea.IErrorMessageReporter#reportError(java.lang.String)
				 */
				public void reportError(String errorMessage, boolean infoOnly) {
					if (infoOnly) {
						setMessage(errorMessage, IStatus.INFO);
						setErrorMessage(null);
					}
					else
						setErrorMessage(errorMessage);
					boolean valid = errorMessage == null;
					if(valid) {
						valid = validatePage();
					}
					
					setPageComplete(valid);
				}
			};
		}

	    /**
	     * Creates the project name specification controls.
	     *
	     * @param parent the parent composite
	     */
		private final void createProjectBasicInfoGroup(Composite parent) {
			Composite projectGroup = new Composite(parent, SWT.NONE);
			GridLayout layout = new GridLayout();
			layout.numColumns = 3;
			projectGroup.setLayout(layout);
			projectGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

			// new project name label
			Label projectNameLabel = new Label(projectGroup, SWT.NONE);
			projectNameLabel.setText("Project name:"); //$NON-NLS-1$
			projectNameLabel.setFont(parent.getFont());

			// new project name entry field
			projectNameField = new Text(projectGroup, SWT.BORDER);
			GridData nameData = new GridData(GridData.FILL_HORIZONTAL);
			nameData.horizontalSpan = 2;
			nameData.widthHint = SIZING_TEXT_FIELD_WIDTH;
			projectNameField.setLayoutData(nameData);
			projectNameField.setFont(parent.getFont());

			projectNameField.addListener(SWT.Modify, new Listener() {
				public void handleEvent(Event e) {
					// Do not disable default location because of this event!
					boolean udld_value = useDefaultLocalDirectory;
					setProjectLocationString();
					useDefaultLocalDirectory = udld_value;
					setPageComplete(validatePage());         
				}
			});

			// new project location label
			Label projectLocationLabel = new Label(projectGroup, SWT.NONE);
			projectLocationLabel.setText("Local directory:"); //$NON-NLS-1$
			projectLocationLabel.setFont(parent.getFont());

			// new project location entry field
			projectLocationField = new Text(projectGroup, SWT.BORDER);
			GridData locationData = new GridData(GridData.FILL_HORIZONTAL);
			locationData.widthHint = SIZING_TEXT_FIELD_WIDTH;
			projectLocationField.setLayoutData(locationData);
			projectLocationField.setFont(parent.getFont());
			this.setProjectLocationString();
			projectLocationField.addListener(SWT.Modify, new Listener() {
				public void handleEvent(Event e) {
					useDefaultLocalDirectory = false;
					setPageComplete(validatePage());        
				}
			});

			// Browse button
			browseButton = new Button(projectGroup, SWT.PUSH);
			browseButton.setText("Browse"); //$NON-NLS-1$
			browseButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					DirectoryDialog dirDialog = new DirectoryDialog(projectLocationField.getShell());
					dirDialog.setText("Select project local directory"); //$NON-NLS-1$
					String selectedDir = dirDialog.open();
					projectLocationField.setText(selectedDir);
				}
			});
		}

		private final void createProjectRemoteInfoGroup(Composite parent) {
			Composite projectGroup = new Composite(parent, SWT.NONE);
			GridLayout layout = new GridLayout();
			layout.numColumns = 3;
			projectGroup.setLayout(layout);
			GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
			projectGroup.setLayoutData(gd);

			// Label for "Provider:"
			Label remoteLocationLabel = new Label(projectGroup, SWT.LEFT);
			remoteLocationLabel.setText("Remote Location"); //$NON-NLS-1$

			fProviderArea = new Group(projectGroup, SWT.SHADOW_ETCHED_IN);
			fProviderStack = new StackLayout();
			fProviderArea.setLayout(fProviderStack);
			GridData providerAreaData = new GridData(SWT.FILL, SWT.FILL, true, true);
			providerAreaData.horizontalSpan = 3;
			fProviderArea.setLayoutData(providerAreaData);

			// For now, assume only one provider, to reduce the number of GUI elements.
			// TODO: Add error handling if there are no providers
			ISynchronizeParticipantDescriptor[] providers = SynchronizeParticipantRegistry.getDescriptors();
			fSelectedParticipant = providers[0].getParticipant();
			
			Composite comp = new Composite(fProviderArea, SWT.NONE);
			comp.setLayout(new GridLayout(1, false));
			comp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			fSelectedParticipant.createConfigurationArea(comp, getWizard().getContainer());
			fProviderStack.topControl = comp;
		}

		private void setProjectLocationString() {
			if (useDefaultLocalDirectory) {
				projectLocationField.setText(Platform.getLocation().toOSString() + File.separator + projectNameField.getText());
			}
		}

		/**
		 * Creates a project resource handle for the current project name field
		 * value. The project handle is created relative to the workspace root.
		 * <p>
		 * This method does not create the project resource; this is the
		 * responsibility of <code>IProject::create</code> invoked by the new
		 * project resource wizard.
		 * </p>
		 * 
		 * @return the new project resource handle
		 */
		@Override
		public IProject getProjectHandle() {
			return ResourcesPlugin.getWorkspace().getRoot().getProject(
					getProjectName());
		}

		/**
		 * Returns the current project name as entered by the user, or its anticipated
		 * initial value.
		 *
		 * @return the project name, its anticipated initial value, or <code>null</code>
		 *   if no project name is known
		 */
		@Override
		public String getProjectName() {
			return getProjectNameFieldValue();
		}

		/**
		 * Returns the value of the project name field
		 * with leading and trailing spaces removed.
		 * 
		 * @return the project name in the field
		 */
		private String getProjectNameFieldValue() {
			if (projectNameField == null) {
				return ""; //$NON-NLS-1$
			}

			return projectNameField.getText().trim();
		}

		/**
		 * Returns whether this page's controls currently all contain valid 
		 * values.
		 *
		 * @return <code>true</code> if all controls are valid, and
		 *   <code>false</code> if at least one is invalid
		 */
		private boolean validateProject() {
			IWorkspace workspace = IDEWorkbenchPlugin.getPluginWorkspace();

			String projectFieldContents = getProjectNameFieldValue();
			if (projectFieldContents.equals("")) { //$NON-NLS-1$
				setErrorMessage(null);
				setMessage(IDEWorkbenchMessages.WizardNewProjectCreationPage_projectNameEmpty);
				return false;
			}

			IStatus nameStatus = workspace.validateName(projectFieldContents,
					IResource.PROJECT);
			if (!nameStatus.isOK()) {
				setErrorMessage(nameStatus.getMessage());
				return false;
			}

			IProject handle = getProjectHandle();
			if (handle.exists()) {
				setErrorMessage(IDEWorkbenchMessages.WizardNewProjectCreationPage_projectExistsMessage);
				return false;
			}

			setErrorMessage(null);
			setMessage(null);
			return true;
		}

		/**
		 * Sets the initial project name that this page will use when
		 * created. The name is ignored if the createControl(Composite)
		 * method has already been called. Leading and trailing spaces
		 * in the name are ignored.
		 * Providing the name of an existing project will not necessarily 
		 * cause the wizard to warn the user.  Callers of this method 
		 * should first check if the project name passed already exists 
		 * in the workspace.
		 * 
		 * TODO: Implement this method
		 * @param name initial project name for this page
		 * 
		 * @see IWorkspace#validateName(String, int)
		 * 
		 */
		@Override
		public void setInitialProjectName(String name) {
			// Empty for now
		}

		/*
		 * see @DialogPage.setVisible(boolean)
		 */
		@Override
		public void setVisible(boolean visible) {
			this.getControl().setVisible(visible);
			if (visible) {
				projectNameField.setFocus();
			}
		}

		// No defaults yet
		@Override
		public boolean useDefaults() {
			return false;
		}
		
		/**
		 * Get the synchronize participant, which contains remote information
		 * @return participant
		 */
		public ISynchronizeParticipant getSynchronizeParticipant() {
			return fSelectedParticipant;
		}
		
		/**
		 * Get the selected remote tool chain
		 * @return tool chain or null if either none selected or name does not map to a value (such as "Other Toolchain")
		 */
		public IToolChain getRemoteToolChain() {
			 TableItem[] selectedToolChains  = remoteToolChainTable.getSelection();
			 if (selectedToolChains.length < 1) {
				 return null;
			 } else {
				 String toolChainName = selectedToolChains[0].getText();
				 return toolChainMap.get(toolChainName);
			 }
		}

		//		/*
		//		 * (non-Javadoc)
		//		 * 
		//		 * @see org.eclipse.jface.dialogs.IDialogPage#getErrorMessage()
		//		 */
		//		public String getErrorMessage() {
		//			if (fSelectedProvider==null)
		//				return Messages.ConvertToSyncProjectWizardPage_0; 
		//			else 
		//				return fSelectedProvider.getParticipant().getErrorMessage();
		//		}
		//
		//		/*
		//		 * (non-Javadoc)
		//		 * 
		//		 * @see
		//		 * org.eclipse.cdt.managedbuilder.ui.wizards.MBSCustomPage#isCustomPageComplete
		//		 * ()
		//		 */
		//		@Override
		//		protected boolean isCustomPageComplete() {
		//			return fbVisited  && getErrorMessage()==null && fSelectedProvider.getParticipant().isConfigComplete();
		//		}
		//
		//		private void update() {
		//			getWizard().getContainer().updateMessage();
		//			getWizard().getContainer().updateButtons();
		//		}
}