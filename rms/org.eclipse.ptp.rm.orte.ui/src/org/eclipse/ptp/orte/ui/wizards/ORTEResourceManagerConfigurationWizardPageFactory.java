/*******************************************************************************
 * Copyright (c) 2006 The Regents of the University of California. 
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
package org.eclipse.ptp.orte.ui.wizards;

import org.eclipse.ptp.orte.core.rmsystem.ORTEResourceManagerFactory;
import org.eclipse.ptp.ui.wizards.RMConfigurationWizard;
import org.eclipse.ptp.ui.wizards.RMConfigurationWizardPage;
import org.eclipse.ptp.ui.wizards.RMConfigurationWizardPageFactory;

public class ORTEResourceManagerConfigurationWizardPageFactory extends
		RMConfigurationWizardPageFactory {

	public ORTEResourceManagerConfigurationWizardPageFactory() {
		// no-op
	}

	public RMConfigurationWizardPage[] getPages(RMConfigurationWizard wizard) {
		return new RMConfigurationWizardPage[]{new ORTEResourceManagerConfigurationWizardPage(wizard)};
	}

	public Class<ORTEResourceManagerFactory> getRMFactoryClass() {
		return ORTEResourceManagerFactory.class;
	}
}
