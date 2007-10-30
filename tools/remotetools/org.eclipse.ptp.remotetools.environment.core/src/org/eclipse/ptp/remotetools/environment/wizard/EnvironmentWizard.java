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
package org.eclipse.ptp.remotetools.environment.wizard;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ptp.remotetools.environment.EnvironmentPlugin;
import org.eclipse.ptp.remotetools.environment.core.ITargetElement;
import org.eclipse.ptp.remotetools.environment.core.TargetElement;
import org.eclipse.ptp.remotetools.environment.core.TargetTypeElement;


/**
 * 
 * @author Ricardo M. Matinata
 * @since 1.1
 */
public class EnvironmentWizard extends Wizard {

	TargetTypeElement typeElement;
	ITargetElement targetElement;
	String originalKey = ""; //$NON-NLS-1$
	boolean cancel = false;
	/**
	 * 
	 */
	public EnvironmentWizard(TargetTypeElement element) {
		super();
		this.typeElement = element;
	}
	
	public EnvironmentWizard(ITargetElement element) {
		super();
		this.typeElement = element.getType();
		this.targetElement = element;
	}
	
	/**
	 * 
	 * @see org.eclipse.jface.wizard.IWizard#addPages()
	 */
	public void addPages() {
		super.addPages();
		this.setWindowTitle(DialogMessages.getString("EnvironmentWizard.0")); //$NON-NLS-1$
		//Map attributes = null;
		if (targetElement != null) {
			//attributes =  new HashMap(targetElement.getAttributes());
			originalKey = targetElement.getName();
		}
		//AbstractEnvironmentDialogPage page = typeElement.getExtension().dialogPageFactory(attributes,originalKey);
		AbstractEnvironmentDialogPage page;
		if(targetElement != null) {
			page = typeElement.getExtension().dialogPageFactory(targetElement);
		} else {
			page = typeElement.getExtension().dialogPageFactory();
		}
		addPage(page);
	}

	/**
	 * 
	 * @see org.eclipse.jface.wizard.Wizard#performFinish()
	 */
	public boolean performFinish() {
		if (!cancel) {
			AbstractEnvironmentDialogPage page = ((AbstractEnvironmentDialogPage)getStartingPage());
			Map attributes = page.getAttributes();
			
			if (attributes == null)
				return false;
			
			if (targetElement == null) {
				String id = EnvironmentPlugin.getDefault().getEnvironmentUniqueID();
				typeElement.addElement(new TargetElement(typeElement,page.getName(),attributes, id));
			} else {
				targetElement.setAttributes(attributes);
				targetElement.setName(page.getName());
			}
		}
		return true;
	}
	
	public boolean performCancel() {
		this.cancel = true;
        return true;
    }
	
	/** 
	 * 
	 * @see org.eclipse.jface.wizard.IWizard#canFinish()
	 */
	public boolean canFinish() {
		
		AbstractEnvironmentDialogPage page = ((AbstractEnvironmentDialogPage)getStartingPage());
		page.setErrorMessage(null);
		if (!page.getName().equals(originalKey)) {
			if (page.getName() != null) {
				if (!(EnvironmentPlugin.getDefault().getTargetsManager().selectControl(
						page.getName() ) == null)) {
					page.setErrorMessage(DialogMessages.getString("EnvironmentWizard.1")); //$NON-NLS-1$
					return false;
				} 
			} else {
				page.setErrorMessage(DialogMessages.getString("EnvironmentWizard.2")); //$NON-NLS-1$
				return false;
			}
		}
		
		return page.canFinish();
	}

}
