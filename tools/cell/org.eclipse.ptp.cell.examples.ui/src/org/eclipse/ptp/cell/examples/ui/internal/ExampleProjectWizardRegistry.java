/******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *****************************************************************************/
package org.eclipse.ptp.cell.examples.ui.internal;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.ptp.utils.core.extensionpoints.IProcessMemberVisitor;
import org.eclipse.ptp.utils.core.extensionpoints.ProcessExtensions;

/**
 * @author laggarcia
 * @since 1.1.1
 * 
 */
public class ExampleProjectWizardRegistry {

	public static final String EXT_EXAMPLE_PROJECT_ID = "org.eclipse.ptp.cell.examples.ui.exampleProjectCreationWizard"; //$NON-NLS-1$

	private static final String ID = "id"; //$NON-NLS-1$

	private static final ExampleProjectWizardRegistry instance = null;

	private final Map projectsWizards = new HashMap();

	/**
	 * 
	 */
	private ExampleProjectWizardRegistry() {

		ProcessExtensions.process(EXT_EXAMPLE_PROJECT_ID,
				new IProcessMemberVisitor() {

					public Object process(IExtension extension,
							IConfigurationElement member) {
						List projectsWizardsSetupList;
						// Create an entry in the HashMap with the Id of the
						// wizard that this extension is intended for associated
						// and a new ArrayList.
						projectsWizards.put(member.getAttribute(ID),
								(projectsWizardsSetupList = new ArrayList()));

						IConfigurationElement[] projectsSetup = member
								.getChildren();
						for (int i = 0; i < projectsSetup.length; i++) {
							projectsWizardsSetupList
									.add(new ProjectWizardDefinition(
											projectsSetup[i]));
						}

						return projectsWizards;
					}

				});

	}

	public static synchronized ExampleProjectWizardRegistry getExampleProjectRegistry() {
		if (instance == null) {
			return new ExampleProjectWizardRegistry();
		}
		return instance;
	}

	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}

	public List getProjectWizardDefinitionList(String wizardId) {
		return (List) projectsWizards.get(wizardId);
	}

}
