/**
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *
 */
package org.eclipse.ptp.remotetools.environment.ui.views;


import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ptp.remotetools.environment.EnvironmentPlugin;
import org.eclipse.ptp.remotetools.environment.control.ITargetControl;
import org.eclipse.ptp.remotetools.environment.core.ITargetElement;
import org.eclipse.ptp.remotetools.environment.core.ITargetElementStatus;
import org.eclipse.ptp.remotetools.environment.core.ITargetEventListener;
import org.eclipse.ptp.remotetools.environment.core.TargetElement;
import org.eclipse.ptp.remotetools.environment.core.TargetEnvironmentManager;
import org.eclipse.ptp.remotetools.environment.core.TargetTypeElement;
import org.eclipse.ptp.remotetools.environment.extension.INode;
import org.eclipse.ptp.remotetools.environment.ui.UIEnvironmentPlugin;
import org.eclipse.ptp.remotetools.environment.wizard.EnvironmentWizard;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.part.ViewPart;



/**
 *
 * The view uses a label provider to define how model
 * objects should be presented in the view. Each
 * view can present the same model objects using
 * different labels and icons, if needed. Alternatively,
 * a single label provider can be shared between views
 * in order to ensure that objects of the same type are
 * presented in the same way everywhere.
 * <p>
 * 
 * @author Ricardo M. Matinata
 * @since 1.1
 */

public class RemoteToolsEnvironmentView extends ViewPart implements ISelectionChangedListener,ITargetEventListener {
	
	private TreeViewer viewer;
	private Action startAction;
	private Action stopAction;
	private Action resumeAction;
	private Action pauseAction;
	private Action createAction;
	private Action editAction;
	private Action removeAction;
	private Action doubleClickAction;
	
	private List<Action> workloadControllers = new ArrayList<Action>();
	// Set the table column property names
	private final String ENVIRONMENT_CONTROL_NAME		= "Target Environment";
	private final String ENVIRONMENT_CONTROL_STATUS		= "Status";


	// Set column names
	private String[] columnNames = new String[] { 
			ENVIRONMENT_CONTROL_NAME, 
			ENVIRONMENT_CONTROL_STATUS
			};
	
	// Model
	TargetEnvironmentManager model = EnvironmentPlugin.getDefault().getTargetsManager();
	ITargetElement currentElement = null;

	/*
	 * The content provider class is responsible for
	 * providing objects to the view. It can wrap
	 * existing objects in adapters or simply return
	 * objects as-is. These objects may be sensitive
	 * to the current input of the view, or ignore
	 * it and always show the same content 
	 * (like Task List, for example).
	 */
	
	class ViewContentProvider implements ITreeContentProvider {
	      private final Object[] EMPTY = new Object[] {};
	      
		  /**
		   * Gets the children for a TargetEnvironmentTypeElement or TargetEnvironmentConfigElement
		   * 
		   * @param arg0
		   *            the TargetEnvironmentTypeElement or TargetEnvironmentConfigElement
		   * @return Object[]
		   */
		  public Object[] getChildren(Object arg0) {
		    if (arg0 instanceof TargetTypeElement) {
		    	return ((TargetTypeElement) arg0).getElements().toArray();	
		    }else if (arg0 instanceof ITargetElement){
		        // gets the children of ITargetElement
		    	return EnvironmentPlugin.getDefault().getChildrenProviderManager().getChildren((ITargetElement)arg0);		    	
		    }else if (arg0 instanceof INode) {
		        return ((INode) arg0).getChildren();
		    }
		    return EMPTY;
		  }

		  /**
		   * Gets the parent TargetEnvironmentTypeElement for a TargetEnvironmentConfigElement
		   * 
		   * @param arg0
		   *            the TargetEnvironmentConfigElement
		   * @return Object
		   */
		  public Object getParent(Object arg0) {
			if (arg0 instanceof ITargetElement) {
				return ((ITargetElement) arg0).getType();
			}else if (arg0 instanceof INode) {
			    return ((INode) arg0).getParent();
			}
			return null;
		  }

		  /**
		   * Gets whether this team or player has children
		   * 
		   * @param arg0
		   *            the TargetEnvironmentTypeElement or TargetEnvironmentConfigElement
		   * @return boolean
		   */
		  public boolean hasChildren(Object arg0) {
		    return getChildren(arg0).length > 0;
		  }

		  /**
		   * Gets the elements for the table
		   * 
		   * @param arg0
		   *            the model
		   * @return Object[]
		   */
		  public Object[] getElements(Object arg0) {
			if (arg0 instanceof Object[]) {
				return (Object[]) arg0;
			}
			return getChildren(arg0);
		}

		  /**
			 * Disposes any resources
			 */
		  public void dispose() {
		  }

		  /**
		   * Called when the input changes
		   * 
		   * @param arg0
		   *            the parent viewer
		   * @param arg1
		   *            the old input
		   * @param arg2
		   *            the new input
		   */
		  public void inputChanged(Viewer arg0, Object arg1, Object arg2) {
		    // Nothing to do
		  }
		}
	
	class ViewLabelProvider extends LabelProvider implements ITableLabelProvider {
		private Image viewIcon;

		public String getColumnText(Object obj, int index) {
			String result = "";
			
	        switch (index) {
	            case 0:
	                if (obj instanceof INode) {
	                    result = ((INode) obj).getDisplayText();
	                }else {
	                    result = obj.toString();
	                }
	                break;
	            case 1 :
	            	if (TargetElement.class.isAssignableFrom(obj.getClass())) {
	            		int status = ((ITargetElement)obj).getStatus();
	            		
	            		switch (status) {
							case ITargetElementStatus.STARTED:
								result = "Started";
								break;
							case ITargetElementStatus.STOPPED:
								result = "Stopped";
								break;
							case ITargetElementStatus.RESUMED:
								result = "Running";
								break;
							case ITargetElementStatus.PAUSED:
								result = "Paused";
								break;
	            		}
	            			
	            	}
	                break;
	            }
	        return result;
		}
		
		public Image getColumnImage(Object obj, int index) {
			if (index == 0 ) {
			   if (obj instanceof INode) {
			       return ((INode) obj).getIcon();
			   }else {
			       return getImage(obj);
			   }
			}
			return null;
		}
		
		public Image getImage(Object obj) {
			if (TargetTypeElement.class.isAssignableFrom(obj.getClass())) {
                URL url = UIEnvironmentPlugin.getDefault().getBundle().getEntry("/icons/connect_create.gif");
                ImageDescriptor imageMonitorDescriptor = ImageDescriptor.createFromURL(url);
                viewIcon = imageMonitorDescriptor.createImage();
			} else if (TargetElement.class.isAssignableFrom(obj.getClass())) {
			    URL url = UIEnvironmentPlugin.getDefault().getBundle().getEntry("/icons/monitor_obj.gif");
                ImageDescriptor imageMonitorDescriptor = ImageDescriptor.createFromURL(url);
                viewIcon = imageMonitorDescriptor.createImage();
			}
			return viewIcon;
		}
		
		public void dispose() {
			super.dispose();
			if (viewIcon != null) {
				viewIcon.dispose();
				viewIcon = null;
			}
		}
	}
	class NameSorter extends ViewerSorter {
	}

	/**
	 * The constructor.
	 */
	public RemoteToolsEnvironmentView() {
	}

	/**
	 * This is a callback that will allow us
	 * to create the viewer and initialize it.
	 */
	public void createPartControl(Composite parent) {
		
		viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		viewer.setContentProvider(new ViewContentProvider());
		viewer.setLabelProvider(new ViewLabelProvider());
		viewer.setSorter(new NameSorter());
		defineTable(viewer);
		viewer.setColumnProperties(columnNames);
		// viewer.setInput(getViewSite());
		viewer.setInput(model.getTypeElements().toArray());
		viewer.refresh();
		getSite().setSelectionProvider(viewer);
		viewer.addSelectionChangedListener(this);
		workloadControllers = getWorkloadControllers();
		for (Action controller : workloadControllers) {
			if(controller instanceof ISelectionChangedListener) {
				viewer.addSelectionChangedListener((ISelectionChangedListener) controller);
			}
		}
		model.addModelEventListener(this);
		makeActions();
		hookContextMenu();
		hookDoubleClickAction();
		contributeToActionBars();
		
		// Pack the window
	    parent.pack();

	    // Scroll to top
	    viewer.reveal(viewer.getTree().getItem(0));
	}

	private void defineTable(TreeViewer viewer) {
		Tree t = viewer.getTree();
		t.setHeaderVisible(true);
		
		// 1st column with image/checkboxes - NOTE: The SWT.CENTER has no effect!!
		TreeColumn column = new TreeColumn(t, SWT.LEFT, 0);		
		column.setText(ENVIRONMENT_CONTROL_NAME);
		column.setWidth(650);
		
		// 2nd column with task Description
		column = new TreeColumn(t, SWT.RIGHT, 1);
		column.setText(ENVIRONMENT_CONTROL_STATUS);
		column.setWidth(50);
		
		viewer.expandAll();
		
		// Pack the columns
		/*
	    for (int i = 0, n = t.getColumnCount(); i < n; i++) {
	      t.getColumn(i).pack();
	    }
	    */
	    // Turn on the header and the lines
	    t.setHeaderVisible(true);
	    t.setLinesVisible(true);
		
		
	}
	
	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				ISelection selection = viewer.getSelection();
				Object obj = ((IStructuredSelection)selection).getFirstElement();
				if (obj != null) {
					if (TargetElement.class.isAssignableFrom(obj.getClass())) {
						// Edit
						// Remove						
						createAction.setEnabled(false);
						
						ITargetElement temp = (ITargetElement)obj;			
						if(temp.getStatus() == ITargetElementStatus.STOPPED){
							editAction.setEnabled(true);
							removeAction.setEnabled(true);
						} else{
							editAction.setEnabled(false);
							removeAction.setEnabled(false);
						}
						RemoteToolsEnvironmentView.this.fillContextMenu(manager);
					} else if (TargetTypeElement.class.isAssignableFrom(obj.getClass())) {
						//Create
						createAction.setEnabled(true);
						editAction.setEnabled(false);
						removeAction.setEnabled(false);
						RemoteToolsEnvironmentView.this.fillContextMenu(manager);
					}
				}
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {
		//manager.add(action1);
		//manager.add(new Separator());
		//manager.add(action2);
	}

	private void fillContextMenu(IMenuManager manager) {
		manager.add(createAction);
		manager.add(editAction);
		manager.add(removeAction);
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}
	
	private void fillLocalToolBar(IToolBarManager manager) {
		// Other plug-ins can contribute there actions here
		for (Action controller : workloadControllers) {
			manager.add(controller);
		}
		
		manager.add(startAction);
		manager.add(stopAction);
		manager.add(resumeAction);
		manager.add(pauseAction);
	}
	
	private List<Action> getWorkloadControllers() {

		List<Action> actions = new ArrayList<Action>();
		
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IExtensionPoint extensionPoint = registry
				.getExtensionPoint("org.eclipse.ptp.remotetools.environment.ui.workloadController");
		IExtension[] extensions = extensionPoint.getExtensions();

		try {
			for (int i = 0; i < extensions.length; i++) {
				IExtension extension = extensions[i];
				IConfigurationElement[] elements = extension
						.getConfigurationElements();
				for (int j = 0; j < elements.length; j++) {
				    IConfigurationElement element = elements[j];
	                if ("controllerDelegate".equals(element.getName())) {
	                    actions.add((Action) element.createExecutableExtension("class"));
	                }
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return actions.isEmpty() ? Collections.EMPTY_LIST : actions;
	}
	
	public void refresh() {
		final Display display = viewer.getControl().getDisplay();
		display.asyncExec(new Runnable() {
		      public void run () {
		          if (!viewer.getControl().isDisposed())
		        	  viewer.refresh(true);
		      }
		});
	}

	private void makeActions() {
		
		URL url = UIEnvironmentPlugin.getDefault().getBundle().getEntry("/icons/run_exc.gif");
	    ImageDescriptor imageRun = ImageDescriptor.createFromURL(url);
	    url = UIEnvironmentPlugin.getDefault().getBundle().getEntry("/icons/terminatedlaunch.gif");
	    ImageDescriptor imageStop = ImageDescriptor.createFromURL(url);
	    url = UIEnvironmentPlugin.getDefault().getBundle().getEntry("/icons/suspend.gif");
	    ImageDescriptor imageSuspend = ImageDescriptor.createFromURL(url);
	    url = UIEnvironmentPlugin.getDefault().getBundle().getEntry("/icons/resume.gif");
	    ImageDescriptor imageResume = ImageDescriptor.createFromURL(url);
	    
	    IWorkbenchPartSite site =  this.getSite();
		final Shell shell = site.getShell();
		
		final IJobChangeListener ijob = new IJobChangeListener() {

			public void aboutToRun(IJobChangeEvent event) {
				
				
			}

			public void awake(IJobChangeEvent event) {
				
				
			}

			public void done(IJobChangeEvent event) {
				
				refresh();
				
			}

			public void running(IJobChangeEvent event) {
				
				
			}

			public void scheduled(IJobChangeEvent event) {
				
				
			}

			public void sleeping(IJobChangeEvent event) {
				
				
			}
			
		};
		
		startAction = new Action() {
			public void run() {
				ISelection selection = viewer.getSelection();
				final Object obj = ((IStructuredSelection)selection).getFirstElement();
				if (obj != null) {
					if (TargetElement.class.isAssignableFrom(obj.getClass())) {
						Job job = new Job("Start the  Environment") {
							protected IStatus run(IProgressMonitor monitor) {
								
								startAction.setEnabled(false);
								IStatus status = null;
								ITargetElement element = null;
								ITargetControl control = null;
								
								try {
									element = (ITargetElement)obj;
									control = element.getControl();
									if ( control.create(monitor) )
										status = Status.OK_STATUS;
								} catch (CoreException e) {
									status = e.getStatus();
									startAction.setEnabled(true);
								}
								
								return status;
							}
						};
						job.setUser(true);
						job.schedule();
						job.addJobChangeListener(ijob);
						return ;
					}
				}
				showMessage("Please Select a Target Configuration first",viewer);
			}
		};
		startAction.setText("Create");
		startAction.setToolTipText("Start the Environment");
		startAction.setImageDescriptor(imageRun);
		
		stopAction = new Action() {
			public void run() {
				
				ISelection selection = viewer.getSelection();
				final Object obj = ((IStructuredSelection)selection).getFirstElement();
				if (obj != null) {
					if (TargetElement.class.isAssignableFrom(obj.getClass())) {
						Job job = new Job("Stop the  Environment") {
							protected IStatus run(IProgressMonitor monitor) {
								
								stopAction.setEnabled(false);
								IStatus status = null;
								
								try {
									if ( ((ITargetElement)obj).getControl().kill(monitor) )
										status = Status.OK_STATUS;
								} catch (CoreException e) {
									status = e.getStatus();
									stopAction.setEnabled(true);
								}
								
								return status;
							}
						};
						job.setUser(true);
						job.schedule();
						job.addJobChangeListener(ijob);
						return ;
					}
				}
				showMessage("Please Select a Target Configuration first",viewer);
			}
		};
		stopAction.setText("Kill");
		stopAction.setToolTipText("Kill the Environment");
		stopAction.setImageDescriptor(imageStop);
		
		//resume
		resumeAction = new Action() {
			public void run() {
				
				ISelection selection = viewer.getSelection();
				final Object obj = ((IStructuredSelection)selection).getFirstElement();
				
				if (obj != null) {
					if (TargetElement.class.isAssignableFrom(obj.getClass())) {
						Job job = new Job("Resume the  Environment") {
							protected IStatus run(IProgressMonitor monitor) {
								
								resumeAction.setEnabled(false);
								IStatus status = null;
								
								try {
									if ( ((ITargetElement)obj).getControl().resume(monitor) ) {
										status = Status.OK_STATUS;
										pauseAction.setEnabled(true);
									}
								} catch (CoreException e) {
									status = e.getStatus();
									resumeAction.setEnabled(true);
								}
								
								return status;
							}
						};
						job.setUser(true);
						job.schedule();
						job.addJobChangeListener(ijob);
						return ;
					}
				}
				showMessage("Please Select a Target Configuration first",viewer);
			}
		};
		resumeAction.setText("Resume");
		resumeAction.setToolTipText("Resume");
		resumeAction.setImageDescriptor(imageResume);
		
		//stop
		pauseAction = new Action() {
			public void run() {
				
				ISelection selection = viewer.getSelection();
				final Object obj = ((IStructuredSelection)selection).getFirstElement();
				
				if (obj != null) {
					if (TargetElement.class.isAssignableFrom(obj.getClass())) {
						Job job = new Job("Pause the  Environment") {
							protected IStatus run(IProgressMonitor monitor) {
								
								pauseAction.setEnabled(false);
								IStatus status = null;
								
								try {
									if ( ((ITargetElement)obj).getControl().stop(monitor) ) {
										status = Status.OK_STATUS;
										resumeAction.setEnabled(true);
									}
								} catch (CoreException e) {
									status = e.getStatus();
									pauseAction.setEnabled(true);
								}
								
								return status;
							}
						};
						job.setUser(true);
						job.schedule();
						job.addJobChangeListener(ijob);
						return ;
					}
				}
				showMessage("Please Select a Target Configuration first",viewer);
			}
		};
		pauseAction.setText("Pause");
		pauseAction.setToolTipText("Pause");
		pauseAction.setImageDescriptor(imageSuspend);
		
		// Create
		createAction = new Action() {
			public void run() {
				ISelection selection = viewer.getSelection();
				Object obj = ((IStructuredSelection)selection).getFirstElement();
				
				if (TargetTypeElement.class.isAssignableFrom(obj.getClass())) {
					EnvironmentWizard wizard = new EnvironmentWizard((TargetTypeElement)obj);
					WizardDialog dialog = new WizardDialog(shell, wizard);
					dialog.create();
					dialog.setBlockOnOpen(true);
					dialog.open();
					refresh();
				}
				
			}
		};
		createAction.setText("Create");
		createAction.setToolTipText("Creates a new configuration");
		
		// Edit
		editAction = new Action() {
			public void run() {
				ISelection selection = viewer.getSelection();
				Object obj = ((IStructuredSelection)selection).getFirstElement();
				
				if (TargetElement.class.isAssignableFrom(obj.getClass())) {
					EnvironmentWizard wizard = new EnvironmentWizard((ITargetElement)obj);
					WizardDialog dialog = new WizardDialog(shell, wizard);
					dialog.create();
					dialog.setBlockOnOpen(true);
					dialog.open();
					refresh();
				}
			}
		};
		editAction.setText("Edit");
		editAction.setToolTipText("Edits configuration");
		
		// Remove
		removeAction = new Action() {
			public void run() {
				ISelection selection = viewer.getSelection();
				Object obj = ((IStructuredSelection)selection).getFirstElement();
				
				if (TargetElement.class.isAssignableFrom(obj.getClass())) {
					ITargetElement confElement = (ITargetElement)obj;
					if (confElement.getStatus() != ITargetElementStatus.STARTED) {
						if (showConfirm("Do you really want to remove ?",viewer)) {
							confElement.getType().removeElement(confElement);
							refresh();
						}
					} else {
						showMessage("Please stop this instance first !",viewer);
					}
				}
			}
		};
		removeAction.setText("Remove");
		removeAction.setToolTipText("Removes configuration");
		
		doubleClickAction = new Action() {
			public void run() {
				ISelection selection = viewer.getSelection();
				Object obj = ((IStructuredSelection)selection).getFirstElement();
				//TargetEnvironmentTypeElement
				//TargetEnvironmentConfigElement, type TargetEnvironmentTypeElement
				
				if (TargetTypeElement.class.isAssignableFrom(obj.getClass())){
					if(viewer.getExpandedState(obj)){
						viewer.setExpandedState(obj, false);
					}
					else
						viewer.setExpandedState(obj,true);
					
				}
				
				if (TargetElement.class.isAssignableFrom(obj.getClass())){
					ITargetElement ele = (ITargetElement) obj;
					try {
						ITargetControl control = ele.getControl();
						if(control.query() == ITargetElementStatus.STOPPED){
							editAction.run();
						}
					} catch (CoreException e) {
						e.printStackTrace();
					}					
				}

                if (INode.class.isAssignableFrom(obj.getClass())) {
                    INode node = (INode) obj;
                    UIEnvironmentPlugin.getDefault().getDoubleClickHandlerManager().doubleClickExecute(node);
                }

               //showMessage("Double-click detected on "+obj.toString(),viewer);
			}
		};		
		
		stopAction.setEnabled(false);
		resumeAction.setEnabled(false);
		pauseAction.setEnabled(false);
		createAction.setEnabled(false);
		editAction.setEnabled(false);
		removeAction.setEnabled(false);
		
	}
	
	private void hookDoubleClickAction() {
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				doubleClickAction.run();
			}
		});
	}
	
	private void showMessage(String message,TreeViewer viewer) {
		MessageDialog.openInformation(
			viewer.getControl().getShell(),
			"Environments",
			message);
	}

	private boolean showConfirm(String message,TreeViewer viewer) {
		return MessageDialog.openConfirm(
			viewer.getControl().getShell(),
			"Environments",
			message);
	}
	
	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}

	public void selectionChanged(SelectionChangedEvent event) {
		ISelection selection = event.getSelection();
		Object obj = ((IStructuredSelection)selection).getFirstElement();
				
		if (obj != null) {
			if (TargetElement.class.isAssignableFrom(obj.getClass())) {
				currentElement = (ITargetElement) obj;
				switch (currentElement.getStatus()) {
					case ITargetElementStatus.STARTED:
						targetStarted(currentElement);
						break;
					case ITargetElementStatus.STOPPED:
						targetStopped(currentElement);
						break;
					case ITargetElementStatus.RESUMED:
						targetResumed(currentElement);
						break;
					case ITargetElementStatus.PAUSED:
						targetPaused(currentElement);
						break;
				}
			} else {
				currentElement = null;
				startAction.setEnabled(false); // Create
				stopAction.setEnabled(false); // Kill
				resumeAction.setEnabled(false); // Resume
				pauseAction.setEnabled(false); // Pause
			}
		}
	}

	public void targetStarted(ITargetElement event) {
		
		if (event != null && currentElement != null) {
				if (event.equals(currentElement)) {
					startAction.setEnabled(false); // Create
					stopAction.setEnabled(true); // Kill
					resumeAction.setEnabled(true); // Resume
					pauseAction.setEnabled(false); // Pause
					editAction.setEnabled(false);
					removeAction.setEnabled(false);
				}
		}
		this.refresh();
		
	}

	public void targetStopped(ITargetElement event) {
		
		if (event != null && currentElement != null) {
			if (event.equals(currentElement)) {
					startAction.setEnabled(true); // Create
					stopAction.setEnabled(false); // Kill
					resumeAction.setEnabled(false); // Resume
					pauseAction.setEnabled(false); // Pause
					editAction.setEnabled(true);
					removeAction.setEnabled(true);
			}
		}
		this.refresh();
		
	}

	public void targetPaused(ITargetElement event) {
		
		if (event != null && currentElement != null) {
			if (event.equals(currentElement)) {
					startAction.setEnabled(false); // Create
					stopAction.setEnabled(true); // Kill
					resumeAction.setEnabled(true); // Resume
					pauseAction.setEnabled(false); // Pause
				
			}
		}
		this.refresh();
		
	}

	public void targetResumed(ITargetElement event) {
		
		if (event != null && currentElement != null) {
			if (event.equals(currentElement)) {
					startAction.setEnabled(false); // Create
					stopAction.setEnabled(true); // Kill
					resumeAction.setEnabled(false); // Resume
					pauseAction.setEnabled(true); // Pause
				
			}
		}
		this.refresh();
		
	}

	public void handleStateChangeEvent(int event, ITargetElement from) {
		
		switch (event) {
			case ITargetElementStatus.STARTED:
				targetStarted(from);
				break;
			case ITargetElementStatus.STOPPED:
				targetStopped(from);
				break;
			case ITargetElementStatus.RESUMED:
				targetResumed(from);
				break;
			case ITargetElementStatus.PAUSED:
				targetPaused(from);
				break;
		}
		
	}
	
	public void dispose() {
		super.dispose();
		model.removeModelEventListener(this);
	}
}


