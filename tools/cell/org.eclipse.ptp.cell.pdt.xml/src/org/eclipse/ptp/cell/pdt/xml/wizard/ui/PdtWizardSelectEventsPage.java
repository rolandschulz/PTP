/******************************************************************************
 * Copyright (c) 2007 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *

*****************************************************************************/
package org.eclipse.ptp.cell.pdt.xml.wizard.ui;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.ptp.cell.pdt.xml.core.eventgroup.AbstractEventElement;
import org.eclipse.ptp.cell.pdt.xml.core.eventgroup.Event;
import org.eclipse.ptp.cell.pdt.xml.core.eventgroup.EventGroup;
import org.eclipse.ptp.cell.pdt.xml.core.eventgroup.EventGroupForest;
import org.eclipse.ptp.cell.pdt.xml.core.eventgroup.EventSubgroup;
import org.eclipse.ptp.cell.pdt.xml.debug.Debug;
import org.eclipse.ptp.utils.ui.swt.Frame;
import org.eclipse.ptp.utils.ui.swt.FrameMold;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;


/**
 * Page containing a list of events for the user select.
 * 
 * @author Richard Maciel
 *
 */
public class PdtWizardSelectEventsPage extends WizardPage {

	protected Frame generalGroupOptions;
	protected Button x86EnableProfile;
	protected Button ppeEnableProfile;
	protected Button speEnableProfile;
	
	
	protected CheckboxTreeViewer eventStatusTreeViewer;
	protected Group eventDescriptionGroup;
	protected Label eventDescriptionLabel;
	//protected TextGroup eventDescription;
	
	protected Boolean isCellArchictecture;
	private EventGroupForest eventGroupForest;
	
	public PdtWizardSelectEventsPage(Boolean isCell, EventGroupForest eventGroupForest) {
		super(PdtWizardSelectEventsPage.class.getName());
		setTitle(Messages.PdtWizardSelectEventsPage_Title);
		setDescription(Messages.PdtWizardSelectEventsPage_Description);
		isCellArchictecture = isCell;
		
		this.eventGroupForest = eventGroupForest;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		initializeDialogUnits(parent);
		
		Font font = parent.getFont();
		
		// create the composite to hold this wizard page's widgets
		Composite composite = new Composite(parent, SWT.NONE);
		
		//create desired layout for this wizard page
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
 		composite.setLayout(layout);
 		composite.setFont(font);

 		//composite.setLayoutData(gd);
 		
 		// Create general group options frame and associated controls
 		FrameMold genOptMold = new FrameMold(Messages.PdtWizardSelectEventsPage_Frame_Title);
 		genOptMold.addOption(FrameMold.HAS_FRAME);
 		generalGroupOptions = new Frame(composite, genOptMold);
 		generalGroupOptions.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1));
 		
 		// Generate grid data for buttons
 		GridData butGridData = GridDataFactory.fillDefaults().create();
 		butGridData.horizontalAlignment = SWT.CENTER;
 		
 		if(isCellArchictecture) {
 			// Generate two buttons. One to PPE profile and one to SPE profile
 			ppeEnableProfile = new Button(generalGroupOptions.getTopUserReservedComposite(), SWT.CHECK);
 			ppeEnableProfile.setLayoutData(butGridData);
 			ppeEnableProfile.setText(Messages.PdtWizardSelectEventsPage_CreateControl_Checkbox_PpeProfile);
 			
 			speEnableProfile = new Button(generalGroupOptions.getTopUserReservedComposite(), SWT.CHECK);
 			speEnableProfile.setLayoutData(GridDataFactory.copyData(butGridData));
 			speEnableProfile.setText(Messages.PdtWizardSelectEventsPage_CreateControl_Checkbox_SpeProfile);
 		} else {
 			// Generate only one button for the x86 profile
 			x86EnableProfile = new Button(generalGroupOptions.getTopUserReservedComposite(), SWT.CHECK);
 			x86EnableProfile.setLayoutData(butGridData);
 			x86EnableProfile.setText(Messages.PdtWizardSelectEventsPage_CreateControl_Checkbox_X86Profile);
 		}

 		// Create tree selection viewer
 		
 		GridData gd = GridDataFactory.fillDefaults().create();
 		gd.grabExcessHorizontalSpace = true;
 		gd.horizontalSpan = 1;
 		gd.grabExcessVerticalSpace = true;
 		
 		eventStatusTreeViewer = new CheckboxTreeViewer(composite);
 		//eventStatusTreeViewer.getTree().addFocusListener(new TreeViewFocusListener());
 		eventStatusTreeViewer.setContentProvider(new PdtEventTreeContentProvider());
 		eventStatusTreeViewer.setLabelProvider(new LabelProvider());
 		eventStatusTreeViewer.getControl().setLayoutData(gd);
 		
 		// Set group and a label inside it
 		GridData descLayDat = GridDataFactory.copyData(gd);
 		eventDescriptionGroup = new Group(composite, SWT.BORDER);
 		eventDescriptionGroup.setLayoutData(descLayDat);
 		eventDescriptionGroup.setText(Messages.PdtWizardSelectEventsPage_CreateControl_Group_Description);
 		GridLayout descGroupLay = new GridLayout(1, true);
 		eventDescriptionGroup.setLayout(descGroupLay);
 		
 		GridData descLblLay = GridDataFactory.copyData(gd);
 		eventDescriptionLabel = new Label(eventDescriptionGroup, SWT.WRAP);
 		eventDescriptionLabel.setLayoutData(descLblLay);
 		
		eventStatusTreeViewer.addSelectionChangedListener(new TreeSelectionEvent());
		
		eventStatusTreeViewer.addCheckStateListener(new TreeCheckedStateListener());
 		
 		
		// Initialize
		initializeControls();
		
		//eventStatusTreeViewer.setGrayed(root.getGroups().toArray()[1], false);
 		//eventStatusTreeViewer.
 		//eventStatusTreeViewer.
 		
 		
 		// At the end of the method
 		setControl(composite);
 		
	}
	
	private void initializeControls() {
		// Fill the tree control
		fillEventTree();
	}

	/*@Override
	public boolean isPageComplete() {
		boolean pageComplete = super.isPageComplete();
		
		// This page must be the actual to be complete.
		pageComplete &= isCurrentPage();
		
		return pageComplete;
	}*/
	
	protected void fillEventTree() {
 		// Get path to fill the tree control with the events
 		// This path comes from the properties
 		if(isCellArchictecture) {
			eventStatusTreeViewer.setInput(eventGroupForest);
 		} else {
 			eventDescriptionGroup.setEnabled(false);
 		}
	}
	
	public EventGroupForest getEventGroups() {
		return eventGroupForest;
	}
	
	protected class TreeSelectionEvent implements ISelectionChangedListener {
		/**
		 * Display information about the selected tree element on the side 
		 * label
		 */
		public void selectionChanged(SelectionChangedEvent event) {
			Debug.read();
			try {
				if(event.getSelection() instanceof IStructuredSelection) {
					IStructuredSelection sel = (IStructuredSelection)event.getSelection();
					AbstractEventElement eventElem = (AbstractEventElement)sel.getFirstElement();
					
					if(eventElem == null) {
						return;
					}
					
					if(eventElem instanceof EventGroup) {
						//EventGroup eventGroup = (EventGroup)eventElem;
						eventDescriptionLabel.setText(Messages.PdtWizardSelectEventsPage_SelectionChanged_Tree_EventGroupItem);
						
					} else if(eventElem instanceof EventSubgroup) {
						//EventSubgroup eventSubgroup = (EventSubgroup)eventElem;
						eventDescriptionLabel.setText(Messages.PdtWizardSelectEventsPage_SelectionChanged_Tree_EventSubgroupItem);
					} else {
						// Event
						Event pdtEvent = (Event)eventElem;
						eventDescriptionLabel.setText(pdtEvent.getDescription());
						//eventDescription.set
					}
					
					//AbstractEventElement selElem = (AbstractEventElement) 
					//eventDescription.setText(((AbstractEventElement)((IStructuredSelection)event.getSelection()).getFirstElement()).getName());
				}
			} catch (Exception e) {
				Debug.POLICY.logError(e);
			}
			//System.out.println(event.getSource().getClass().toString());	
		}
	}
	
	protected class TreeCheckedStateListener implements ICheckStateListener {
		/**
		 * Enables/disables the event element associated with the tree node that
		 * was checked/unchecked.
		 */
		public void checkStateChanged(CheckStateChangedEvent event) {
			Debug.read();
			try {
			// Set element valid attribute to the value of the check control
			AbstractEventElement elem = (AbstractEventElement)event.getElement();
			elem.setActive(event.getChecked());
			
			//Make sure that children of this elem becomes setted and gray
			setChildGrayChecked(elem, event.getChecked());
			
			} catch (Exception e) {
				Debug.POLICY.logError(e);
			}
		}
	}

	public void setChildGrayChecked(AbstractEventElement elem, boolean state) {
		if(elem instanceof EventGroup) {
			EventGroup eventGroup = (EventGroup)elem;
			
			for (EventSubgroup subgroup : eventGroup.getSubgroups()) {
				//eventStatusTreeViewer.setChecked(subgroup, state);
				if(state) {
					eventStatusTreeViewer.setGrayChecked(subgroup, true);
					//eventStatusTreeViewer.setChecked(subgroup, true);
				} else {
					//eventStatusTreeViewer.setGrayChecked(subgroup, subgroup.getActive());
					eventStatusTreeViewer.setGrayed(subgroup, false);
					eventStatusTreeViewer.setChecked(subgroup, subgroup.getActive());
				}
				setChildGrayChecked(subgroup, state);
			}
		} else if(elem instanceof EventSubgroup) {
			EventSubgroup eventSubgroup = (EventSubgroup)elem;
			
			for (Event event : eventSubgroup.getEvents()) {
				if(state) {
					//eventStatusTreeViewer.setGrayChecked(event, true);
					eventStatusTreeViewer.setGrayChecked(event, true);
				} else {
					eventStatusTreeViewer.setGrayed(event, false);
					eventStatusTreeViewer.setChecked(event, event.getActive());
				}
			}
			
		}
	}
	
	/*protected class TreeViewFocusListener implements FocusListener {

		public void focusGained(FocusEvent e) {
//			eventStatusTreeViewer.update(eventGroupForest, null);
			eventStatusTreeViewer.refresh();
			
		}

		public void focusLost(FocusEvent e) {
			// TODO Auto-generated method stub
			
		}

		
		//eventStatusTreeViewer.update(eventGroupForest, null);
			
		
		
	}*/
	
	public boolean getX86EnableProfile() {
		return x86EnableProfile.getSelection();
	}

	public boolean getPpeEnableProfile() {
		return ppeEnableProfile.getSelection();
	}

	public boolean getSpeEnableProfile() {
		return speEnableProfile.getSelection();
	}
	
	/**
	 * Refresh wizard page data 
	 */
	public void refresh() {
		eventStatusTreeViewer.refresh();
	}
}
