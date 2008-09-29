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
import org.eclipse.ptp.cell.pdt.xml.core.AbstractPdtXmlGenerator;
import org.eclipse.ptp.cell.pdt.xml.core.eventgroup.EventGroupForest;



/**
 * Generates a PDT wizard specific for x86 target architecture
 * 
 * @author Richard Maciel
 *
 */
public class X86PdtXmlWizard extends AbstractPdtXmlWizard {

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.cell.pdt.xml.ui.PdtXmlWizard#isCellArchitecture()
	 */
	@Override
	public boolean isCellArchitecture() {
		return false;
	}

	@Override
	protected AbstractPdtXmlGenerator createArchSpecificGenerator(
			IPath absPath, EventGroupForest eventGroupForest2) {
		// TODO Fill it when creating support for x86
		return null;
	}

	@Override
	public void refresh() {
		
	}

}
