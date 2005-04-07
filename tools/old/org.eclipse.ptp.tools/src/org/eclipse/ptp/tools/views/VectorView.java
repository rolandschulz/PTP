/*******************************************************************************
 * Copyright (c) 2005 The Regents of the University of California. 
 * This material was produced under U.S. Government contract W-7405-ENG-36 
 * for Los Alamos National Laboratory, which is operated by the University 
 * of California for the U.S. Department of Energy. The U.S. Government has 
 * rights to use, reproduce, and distribute this software. NEITHER THE 
 * GOVERNMENT NOR THE UNIVERSITY MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR 
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified 
 * to produce derivative works, such modified software should be clearly marked, 
 * so as not to confuse it with the version available from LANL.
 * 
 * Additionally, this program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * LA-CC 04-115
 *******************************************************************************/
package org.eclipse.ptp.tools.views;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.model.ICDIStackFrame;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.core.cdi.model.ICDIThread;
import org.eclipse.cdt.debug.internal.core.model.CVariable;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.ui.AbstractDebugView;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.action.*;
import org.eclipse.ui.*;

/**
 * Basic 2D vector field visualization plugin.
 * 
 * matt sottile / matt@lanl.gov
 */

public class VectorView extends AbstractDebugView implements ISelectionListener {
	private VectorViewDropDownAction fXDropDownAction;
	private VectorViewDropDownAction fYDropDownAction;
	private String[] varList = null;
	private IVariable fXVariable;
	private IVariable fYVariable;
	private boolean continuous = false;
	
	class VectorViewContentProvider implements IContentProvider {
		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
		 */
		public void dispose() {
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
		 */
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}		
	}
	
	class VectorViewDropDownAction extends Action implements IMenuCreator {
		private Menu fMenu;
		private String fVar;
		private Action[] fVarActions;
		
	    public VectorViewDropDownAction(String name) {
	        fMenu = null;
	        fVar = null;
	        setMenuCreator(this);
	        setText(name);
	    }
	    
	    public boolean isSelected(String var) {
	    		return fVar != null && fVar.equals(var);
	    }
	    
		/* (non-Javadoc)
		 * @see org.eclipse.jface.action.IMenuCreator#dispose()
		 */
		public void dispose() {
			if (fMenu != null) {
				fMenu.dispose();
				fMenu = null;
			}
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.action.IMenuCreator#getMenu(org.eclipse.swt.widgets.Control)
		 */
		public Menu getMenu(Menu parent) {
			return null;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.action.IMenuCreator#getMenu(org.eclipse.swt.widgets.Menu)
		 */
		public Menu getMenu(Control parent) {
	        if (fMenu != null) {
	            fMenu.dispose();
	        }
	        fMenu= new Menu(parent);
	        fVarActions = new Action[varList.length];
	        for (int v = 0; v < varList.length; v++) {
	        		fVarActions[v] = new Action(varList[v], IAction.AS_CHECK_BOX) {
	        			public void run() {
	        				fVar = this.getText();
						updateChecked(fVar, fVarActions);
					}
				};
				ActionContributionItem item = new ActionContributionItem(fVarActions[v]);
				item.fill(fMenu, -1);
	        }
	        updateChecked(fVar, fVarActions);

	        return fMenu;
		}
		
	}
	/**
	 * The constructor.
	 */
	public VectorView() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.AbstractDebugView#createViewer(org.eclipse.swt.widgets.Composite)
	 */
	protected Viewer createViewer(Composite parent) {
		VectorViewer viewer = new VectorViewer(parent);
		getSite().getWorkbenchWindow().getSelectionService().addSelectionListener(IDebugUIConstants.ID_DEBUG_VIEW, this);
		IToolBarManager toolBar = getViewSite().getActionBars().getToolBarManager();
		fXDropDownAction = new VectorViewDropDownAction("X");
		fXDropDownAction.setEnabled(false);
		toolBar.add(fXDropDownAction);
		fYDropDownAction = new VectorViewDropDownAction("Y");
		fYDropDownAction.setEnabled(false);
		toolBar.add(fYDropDownAction);
	    IMenuManager menuMgr = getViewSite().getActionBars().getMenuManager();
	    menuMgr.add(new Action("Run continuously", IAction.AS_CHECK_BOX) {
	    		public void run() {
	    			continuous = this.isChecked();
	    		}
	    });
		return viewer;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.AbstractDebugView#createActions()
	 */
	protected void createActions() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.AbstractDebugView#getHelpContextId()
	 */
	protected String getHelpContextId() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.AbstractDebugView#fillContextMenu(org.eclipse.jface.action.IMenuManager)
	 */
	protected void fillContextMenu(IMenuManager menu) {
		menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.AbstractDebugView#configureToolBar(org.eclipse.jface.action.IToolBarManager)
	 */
	protected void configureToolBar(IToolBarManager tbm) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPart#dispose()
	 */
	public void dispose() {
		getSite().getWorkbenchWindow().getSelectionService().removeSelectionListener(IDebugUIConstants.ID_DEBUG_VIEW, this);
		super.dispose();
	}
	
	private void updateChecked(String var, Action[] actions) {
		for (int i = 0; i < actions.length; i++) {
			IAction a = actions[i];
			a.setChecked(a.getText().equals(var));
		}
	}
	
	public IVariable getXVariable() {
		return fXVariable;
	}

	public IVariable getYVariable() {
		return fYVariable;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.ISelectionListener#selectionChanged(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		IVariable valueX = null;
		IVariable valueY = null;
		IAdaptable adaptable = DebugUITools.getDebugContext();
		if (adaptable != null) {
			IDebugElement element = (IDebugElement) adaptable.getAdapter(IDebugElement.class);
			if (element != null) {
				IDebugTarget target = element.getDebugTarget();
				if (target != null) {
					try {
						IThread[] threads = target.getThreads();
						for (int i = 0; i < threads.length; i++) {
							if (!threads[i].isSuspended())
								continue;
							IStackFrame[] stacks = threads[i].getStackFrames();
							for (int j = 0; j < stacks.length; j++) {
								IStackFrame stack = stacks[j];
								if (!stack.isSuspended())
									continue;
								IVariable[] vars = stack.getVariables();
								varList = new String[vars.length];
								for (int k = 0; k < vars.length; k++) {
									IVariable var = vars[k];
									String name = var.getName();
									if (name != null) {
										varList[k] = name;
										if (fXDropDownAction.isSelected(varList[k])) {
											valueX = var;
										}
										if (fYDropDownAction.isSelected(varList[k])) {
											valueY = var;
										}
									} else
										System.out.println("getName() returns null!");
								}
							}
						}
					} catch (DebugException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					if (varList != null) {
						fXDropDownAction.setEnabled(true);
						fYDropDownAction.setEnabled(true);
					}
					
					fXVariable = valueX;
					fYVariable = valueY;
					
					getViewer().setInput(this);
					
					if (continuous && valueX != null && valueY != null && target.isSuspended()) {
						if (target.canResume()) {
							try {
								target.resume();
							} catch (DebugException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
						} else {
							System.out.println("Can't resume for some reason...");
						}
					}
				}
			}
		}
	}
}