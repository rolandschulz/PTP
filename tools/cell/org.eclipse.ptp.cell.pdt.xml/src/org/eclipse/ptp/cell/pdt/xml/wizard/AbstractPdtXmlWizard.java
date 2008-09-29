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
package org.eclipse.ptp.cell.pdt.xml.wizard;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ptp.cell.pdt.xml.Activator;
import org.eclipse.ptp.cell.pdt.xml.core.AbstractPdtXmlGenerator;
import org.eclipse.ptp.cell.pdt.xml.core.ConfigGroupParserException;
import org.eclipse.ptp.cell.pdt.xml.core.ConfigurationFileGenerationException;
import org.eclipse.ptp.cell.pdt.xml.core.PdtEventForestFactory;
import org.eclipse.ptp.cell.pdt.xml.core.PdtXmlBean;
import org.eclipse.ptp.cell.pdt.xml.core.eventgroup.EventGroup;
import org.eclipse.ptp.cell.pdt.xml.core.eventgroup.EventGroupForest;
import org.eclipse.ptp.cell.pdt.xml.debug.Debug;
import org.eclipse.ptp.cell.pdt.xml.wizard.ui.PdtWizardAddRemoveEventsPage;
import org.eclipse.ptp.cell.pdt.xml.wizard.ui.PdtWizardConfigurationFilePage;
import org.eclipse.ptp.cell.pdt.xml.wizard.ui.PdtWizardGroupsPositionAndColorPage;
import org.eclipse.ptp.cell.pdt.xml.wizard.ui.PdtWizardSelectEventsPage;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;


/**
 * @author Richard Maciel
 *
 */
public abstract class AbstractPdtXmlWizard extends Wizard implements INewWizard {

	protected IDialogSettings dialogSettings;
	protected EventGroupForest selectedEventGroupForest, availableEventGroupForest;
	
	/**
	 * 
	 */
	public AbstractPdtXmlWizard() {
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#performFinish()
	 */
	@Override
	public boolean performFinish() {
		// Wizard pages must be completed
		PdtWizardConfigurationFilePage confFilePage = (PdtWizardConfigurationFilePage)getPage(PdtWizardConfigurationFilePage.class.getName());
		PdtWizardSelectEventsPage selEventsPage = (PdtWizardSelectEventsPage)getPage(PdtWizardSelectEventsPage.class.getName());
		PdtWizardGroupsPositionAndColorPage setPosAndColorPage = (PdtWizardGroupsPositionAndColorPage)getPage(PdtWizardGroupsPositionAndColorPage.class.getName());
		
		boolean complete = confFilePage.isPageComplete() &&
		 					selEventsPage.isPageComplete() &&
		 					setPosAndColorPage.isPageComplete();
		 
		if(complete) {
			generateXmlConfigurationFile(confFilePage.getFilePath(), selectedEventGroupForest);
		}
		 
		return complete;
	}
	
	
	
	/**
	 * Create a xml file and fill it with PDT XML data. 
	 * 
	 */
	private void generateXmlConfigurationFile(IPath newFilePath, EventGroupForest eventGroupForest) {
		IPath workspacePath = Platform.getLocation();
		IPath absPath = workspacePath.append(newFilePath);
		
		// Check if file extension is XML. If it is not, append xml extension
		String fileExtension = absPath.getFileExtension();
		
		if(fileExtension == null || !fileExtension.toLowerCase().matches("xml")) { //$NON-NLS-1$
			absPath = absPath.addFileExtension("xml"); //$NON-NLS-1$
		}
		//File newFile = new File(absPath.toOSString());
		
		Debug.read();
		Debug.POLICY.pass(Debug.DEBUG_XML_WIZARD);
		
		try {
			//newFile.createNewFile();
			AbstractPdtXmlGenerator xmlGen = createArchSpecificGenerator(absPath, eventGroupForest); //new CellPdtXmlGenerator(absPath, eventGroupForest);
			
			// Get data from the forest and put it into the XML file.
			xmlGen.generatePdtXmlFile();
			
			
			// Refresh file resource to update the workspace
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			IFile file = root.getFileForLocation(absPath);
			file.refreshLocal(1, new NullProgressMonitor());
		} catch (ConfigurationFileGenerationException e) {
			Debug.POLICY.error(Debug.DEBUG_XML_WIZARD, e);
			// Create an error message for the user
			Debug.POLICY.logError(e, Messages.AbstractPdtXmlWizard_GenerateXmlConfigFile_Dialog_Message_CouldNotCreateFile);
			/*Status errorStatus = new Status(Status.ERROR, Activator.getDefault().getBundle().toString(), 
					Messages.AbstractPdtXmlWizard_GenerateXmlConfigFile_Dialog_Message_CouldNotCreateFile);
			ErrorDialog confFileError = new ErrorDialog(this.getShell(), 
					Messages.AbstractPdtXmlWizard_GenerateXmlConfigFile_Dialog_Title_CouldNotCreateFile, "", errorStatus, Status.ERROR); //$NON-NLS-2$ //$NON-NLS-1$ //$NON-NLS-1$
			confFileError.open();*/
		} catch (CoreException e1) {
			Debug.POLICY.error(Debug.DEBUG_XML_WIZARD, e1);
			// Create an error message for the user
			Debug.POLICY.logError(e1, Messages.AbstractPdtXmlWizard_GenerateXmlConfigFile_Dialog_Message_ProblemRefreshingWorkspace);
		}
	}

	abstract protected AbstractPdtXmlGenerator createArchSpecificGenerator(IPath absPath,
			EventGroupForest eventGroupForest2);

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWizard#init(org.eclipse.ui.IWorkbench, org.eclipse.jface.viewers.IStructuredSelection)
	 */
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		//System.out.println(selection.getFirstElement().getClass().getName());
		//IFile received = (IFile)selection.getFirstElement();

		// Get the directory where the selected resource is (or returns itself if the directory is selected)
		//IFile selFile = (IFile)selection.getFirstElement();
		//System.out.println(selFile.getFullPath());
		
		Debug.read();
		Debug.POLICY.pass(Debug.DEBUG_XML_WIZARD);
		
		// Generate both EventGroupForest objects
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
			IPath eventGroupDirPath = new Path(store.getString(PdtXmlBean.ATTR_EVENT_GROUP_DIR));
 		PdtEventForestFactory factory = new PdtEventForestFactory(eventGroupDirPath);
 		selectedEventGroupForest = factory.createEmptyEventGroupForest();
 		try {
 			//selectedEventGroupForest = factory.createEventGroupForest();
 			availableEventGroupForest = factory.createEventGroupForest(); 
		} catch (ConfigGroupParserException e) {
			Debug.POLICY.error(Debug.DEBUG_XML_WIZARD, e);
			// Create an error message for the user
			Debug.POLICY.logError(e, Messages.AbstractPdtXmlWizard_Init_Dialog_Message_CouldNotParserGroupDefinitionFile);
			/*Status errorStatus = new Status(Status.ERROR, Activator.getDefault().getBundle().toString(), 
					Messages.AbstractPdtXmlWizard_Init_Dialog_Message_CouldNotParserGroupDefinitionFile);
			ErrorDialog confParserError = new ErrorDialog(this.getShell(), 
					Messages.AbstractPdtXmlWizard_Init_Dialog_Title_CouldNotParserGroupDefinitionFile, "", errorStatus, Status.ERROR); //$NON-NLS-2$ //$NON-NLS-1$ //$NON-NLS-1$
			confParserError.open();*/
		}
		
		// Already transfer all the invisible groups to the selectedEventGroupForest
		
		List<EventGroup> invisibleList = new LinkedList<EventGroup>(availableEventGroupForest.getInvisibleGroups());
		selectedEventGroupForest.getInvisibleGroups().addAll(invisibleList);
		availableEventGroupForest.getInvisibleGroups().removeAll(invisibleList);
		/*EventGroup genEvtGrp = null;
		for (EventGroup eventGroup : availableEventGroupForest.getInvisibleGroups()) {
			if(eventGroup.getName().equals(EventGroup.GENERAL_GROUP)) {
				genEvtGrp = eventGroup;
			}
		}
		availableEventGroupForest.getVisibleGroups().remove(genEvtGrp);
		selectedEventGroupForest.getVisibleGroups().add(genEvtGrp);*/
		
	}

	/**
	 * Signalize if the architecture is cell b.e.
	 * This method is used to determine if cell-related events will be displayed
	 * on the {@link PdtWizardSelectEventsPage} 
	 * 
	 * @return 
	 */
	abstract public boolean isCellArchitecture();
	
	/**
	 * Update wizard pages
	 */
	abstract public void refresh();
	
	@Override
	public void addPages() {
		PdtWizardConfigurationFilePage pdtFileConfiguration = new PdtWizardConfigurationFilePage();
		addPage(pdtFileConfiguration);
		
		PdtWizardAddRemoveEventsPage addRemoveEvents = new PdtWizardAddRemoveEventsPage(availableEventGroupForest, selectedEventGroupForest);
		addPage(addRemoveEvents);
		
		PdtWizardSelectEventsPage selectEvents = new PdtWizardSelectEventsPage(isCellArchitecture(), selectedEventGroupForest);
		addPage(selectEvents);
		
		PdtWizardGroupsPositionAndColorPage setPosAndColor = new PdtWizardGroupsPositionAndColorPage(selectedEventGroupForest);
		addPage(setPosAndColor);
	}
}
