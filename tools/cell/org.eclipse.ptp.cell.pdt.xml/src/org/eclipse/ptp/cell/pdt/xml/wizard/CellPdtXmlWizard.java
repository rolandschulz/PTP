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

import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.ptp.cell.pdt.xml.core.AbstractPdtXmlGenerator;
import org.eclipse.ptp.cell.pdt.xml.core.CellPdtXmlGenerator;
import org.eclipse.ptp.cell.pdt.xml.core.eventgroup.EventGroupForest;
import org.eclipse.ptp.cell.pdt.xml.debug.Debug;
import org.eclipse.ptp.cell.pdt.xml.wizard.ui.PdtWizardGroupsPositionAndColorPage;
import org.eclipse.ptp.cell.pdt.xml.wizard.ui.PdtWizardSelectEventsPage;



/**
 * Generates a PDT wizard specific for Cell B.E. target architecture
 * 
 * @author Richard Maciel
 *
 */
public class CellPdtXmlWizard extends AbstractPdtXmlWizard {

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.cell.pdt.xml.ui.PdtXmlWizard#isCellArchitecture()
	 */
	@Override
	public boolean isCellArchitecture() {
		return true;
	}

	@Override
	protected AbstractPdtXmlGenerator createArchSpecificGenerator(
			IPath absPath, EventGroupForest eventGroupForest2) {
		
		Debug.read();
		Debug.POLICY.pass(Debug.DEBUG_XML_WIZARD);
		
		PdtWizardSelectEventsPage selEventsPage = (PdtWizardSelectEventsPage)getPage(PdtWizardSelectEventsPage.class.getName());
		
		return new CellPdtXmlGenerator(absPath, selectedEventGroupForest, 
				selEventsPage.getPpeEnableProfile(), selEventsPage.getSpeEnableProfile());
	}

	@Override
	public void refresh() {
		IWizardPage [] pages = getPages();
		for(int i=0; i < pages.length; i++) {
			if(pages[i] instanceof PdtWizardSelectEventsPage) {
				((PdtWizardSelectEventsPage)pages[i]).refresh();
			} else if(pages[i] instanceof PdtWizardGroupsPositionAndColorPage) {
				((PdtWizardGroupsPositionAndColorPage)pages[i]).refresh();
			}
		}
		
	}

}
