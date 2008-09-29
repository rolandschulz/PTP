/**
 * 
 */
package org.eclipse.ptp.cell.pdt.xml.wizard.ui;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.ptp.cell.pdt.xml.core.eventgroup.EventGroup;
import org.eclipse.ptp.cell.pdt.xml.core.eventgroup.EventGroupForest;
import org.eclipse.ptp.cell.pdt.xml.debug.Debug;
import org.eclipse.ptp.cell.pdt.xml.wizard.AbstractPdtXmlWizard;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;


/**
 * Let the user select which event groups he wants to include into the configuration file.
 * 
 * @author Richard Maciel
 *
 */
public class PdtWizardAddRemoveEventsPage extends WizardPage {

	EventGroupForest selectedEventGroupForest, availableEventGroupForest;
	
	protected ListViewer availableEventGroups, selectedEventGroups;
	protected Button select, deselect, selectAll, deselectAll;
	
	/**
	 * @param selectedEventGroupForest 
	 * @param pageName
	 */
	public PdtWizardAddRemoveEventsPage(EventGroupForest availableEventGroupForest, EventGroupForest selectedEventGroupForest) {
		super(PdtWizardAddRemoveEventsPage.class.getName());
		setTitle(Messages.PdtWizardAddRemoveEventsPage_WizardPage_Title);
		setDescription(Messages.PdtWizardAddRemoveEventsPage_WizardPage_Description);
		
		this.selectedEventGroupForest = selectedEventGroupForest;
		this.availableEventGroupForest = availableEventGroupForest;
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
		layout.numColumns = 3;
		layout.horizontalSpacing = 3;
		layout.makeColumnsEqualWidth = true;
		//layout.marginWidth = 5;
 		composite.setLayout(layout);
 		composite.setFont(font);
 		
 		// Create layout factory to generate layouts for the groups
		GridLayoutFactory compLayoutFactory = GridLayoutFactory.swtDefaults();
		compLayoutFactory.numColumns(1);
		
		// Create layout data factory for Lists and lists' container
		GridDataFactory gdFactoryLists = GridDataFactory.swtDefaults();
 		gdFactoryLists.grab(true, true);
 		gdFactoryLists.align(SWT.FILL, SWT.FILL);

 		// Create two group frames to contain the lists
		Group availEvtGrpComposite = new Group(composite, SWT.BORDER);
		availEvtGrpComposite.setText(Messages.PdtWizardAddRemoveEventsPage_Group_AvailableEventGroups);
		availEvtGrpComposite.setLayoutData(gdFactoryLists.create());
		availEvtGrpComposite.setLayout(compLayoutFactory.create());
 		
 		GridDataFactory gdFactoryButtons = gdFactoryLists.copy();
 		gdFactoryButtons.grab(false, false);
 		gdFactoryButtons.align(SWT.CENTER, SWT.CENTER);
 		
 		
 		availableEventGroups = new ListViewer(availEvtGrpComposite);
 		availableEventGroups.getControl().setLayoutData(gdFactoryLists.create());
 		availableEventGroups.setLabelProvider(new LabelProvider());
 		availableEventGroups.setContentProvider(new PdtEventGroupListContentProvider());
 		
 		availableEventGroups.setInput(availableEventGroupForest);
 		
 		Composite buttonHolder = new Composite(composite, SWT.NONE);
 		buttonHolder.setLayoutData(gdFactoryButtons.create());
 		FillLayout btnHldLay = new FillLayout(SWT.VERTICAL);
 		buttonHolder.setLayout(btnHldLay);
 
 		ButtonSelectionHandler btnHandle = new ButtonSelectionHandler();
 		
 		select = new Button(buttonHolder, SWT.PUSH);
 		select.setText(Messages.PdtWizardAddRemoveEventsPage_Button_Add); 
 		select.addSelectionListener(btnHandle);
 		deselect = new Button(buttonHolder, SWT.PUSH);
 		deselect.setText(Messages.PdtWizardAddRemoveEventsPage_Button_Remove); 
 		deselect.addSelectionListener(btnHandle);
 		selectAll = new Button(buttonHolder, SWT.PUSH);
 		selectAll.setText(Messages.PdtWizardAddRemoveEventsPage_Button_AddAll); 
 		selectAll.addSelectionListener(btnHandle);
 		deselectAll = new Button(buttonHolder, SWT.PUSH);
 		deselectAll.setText(Messages.PdtWizardAddRemoveEventsPage_Button_RemoveAll); 
 		deselectAll.addSelectionListener(btnHandle);
 		
 		Group selEvtGrpComposite = new Group(composite, SWT.BORDER);
		selEvtGrpComposite.setText(Messages.PdtWizardAddRemoveEventsPage_Group_SelectedEventGroups);
		selEvtGrpComposite.setLayoutData(gdFactoryLists.create());
		selEvtGrpComposite.setLayout(compLayoutFactory.create());
 		
 		selectedEventGroups = new ListViewer(selEvtGrpComposite);
 		selectedEventGroups.getControl().setLayoutData(gdFactoryLists.create());
 		selectedEventGroups.setLabelProvider(new LabelProvider());
 		selectedEventGroups.setContentProvider(new PdtEventGroupListContentProvider());
 		
 		selectedEventGroups.setInput(selectedEventGroupForest);
 		
 		setControl(composite);
	}
	
	/**
	 * Pass all items in the EventGroup list parameter from the available to
	 * the selected EventGroup
	 * @param eventGroups
	 */
	protected void selectEventGroups(List eventGroupList) {
		if(eventGroupList.size() > 0) {
			Set<EventGroup> availEvtGrpSet = availableEventGroupForest.getVisibleGroups();
			Set<EventGroup> selEvtGrpSet = selectedEventGroupForest.getVisibleGroups();
			
			availEvtGrpSet.removeAll(eventGroupList);
			selEvtGrpSet.addAll(eventGroupList);
			
			// Update the List control
			Object [] evtGrpArray = eventGroupList.toArray();
			availableEventGroups.remove(evtGrpArray);
			selectedEventGroups.add(evtGrpArray);
		}
	}
	
	/**
	 * Pass all items in the EventGroup array parameter from the selected to the
	 * available EventGroup
	 * @param eventGroups
	 */
	protected void removeEventGroups(List eventGroupList) {
		if(eventGroupList.size() > 0) {
			Set<EventGroup> availEvtGrpSet = availableEventGroupForest.getVisibleGroups();
			Set<EventGroup> selEvtGrpSet = selectedEventGroupForest.getVisibleGroups();
			
			selEvtGrpSet.removeAll(eventGroupList);
			availEvtGrpSet.addAll(eventGroupList);
			
			// Update the List control
			Object [] evtGrpArray = eventGroupList.toArray();
			selectedEventGroups.remove(evtGrpArray);
			availableEventGroups.add(evtGrpArray);
		}
	}
	
	/**
	 * Find which button was pressed and act properly
	 * 
	 * @param btn
	 * @param sel
	 */
	protected void selectOrRemove(Button btn, ISelection availableSel, ISelection selectedSel) {
		if(btn.equals(select)) {
			List availSelList = ((StructuredSelection)availableSel).toList();
			selectEventGroups(availSelList);
		} else if(btn.equals(selectAll)) {
			List availAllList = new LinkedList(availableEventGroupForest.getVisibleGroups());
			selectEventGroups(availAllList);
		} else if(btn.equals(deselect)) {
			List selSelList = ((StructuredSelection)selectedSel).toList();
			removeEventGroups(selSelList);
		} else if(btn.equals(deselectAll)) {
			List selAllList = new LinkedList(selectedEventGroupForest.getVisibleGroups());
			removeEventGroups(selAllList);
		}
		
		// Inform the wizard to update the other pages.
		((AbstractPdtXmlWizard)this.getWizard()).refresh();
		/*((PdtWizardSelectEventsPage)this.getNextPage()).refresh();
		((PdtWizardGroupsPositionAndColorPage)this.)*/
	}
	
	/**
	 * Low-level handler for buttons. Take care of swt errors and
	 * calls the method to effectively handle the selection
	 * @author Richard Maciel
	 *
	 */
	protected class ButtonSelectionHandler extends SelectionAdapter {
		@Override
		public void widgetSelected(SelectionEvent e) {
			try {
				selectOrRemove((Button)e.widget, availableEventGroups.getSelection(),
						selectedEventGroups.getSelection());		
			} catch(Exception exception) {
				Debug.POLICY.logError(exception);
			}
		}
	}
	
	
	
	public class PdtEventGroupListContentProvider implements IContentProvider, IStructuredContentProvider {

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

		public Object[] getElements(Object inputElement) {
			// Filter the GENERAL item
			List<EventGroup> eventGroupList = new LinkedList<EventGroup>();
			for (EventGroup eventGroup : ((EventGroupForest)inputElement).getVisibleGroups()) {
				if(!eventGroup.getName().equals(EventGroup.GENERAL_GROUP)) {
					eventGroupList.add(eventGroup);
				}
			}
			
			
			return eventGroupList.toArray();
			
			/*EventGroupForest forest = (EventGroupForest)inputElement;
			Set<EventGroup> groupSet = forest.getGroups();
			//EventGroup [] groupArr = (EventGroup [])groupSet.toArray();
			Object [] groupArr = groupSet.toArray();
			
			return groupArr;*/
		}
		
		public void addElement(Object element) {
			
		}
		
	}
	
}
