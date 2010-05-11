/*******************************************************************************
 * Copyright (c) 2008, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.internal.rdt.core.serviceproviders;

import java.util.Map;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.ptp.internal.rdt.core.RemoteIndexerInfoProviderFactory;
import org.eclipse.ptp.internal.rdt.core.model.ModelAdapter;
import org.eclipse.ptp.internal.rdt.core.model.TranslationUnit;
import org.eclipse.ptp.internal.rdt.core.model.WorkingCopy;
import org.eclipse.ptp.internal.rdt.core.subsystems.ICIndexSubsystem;
import org.eclipse.ptp.rdt.core.RDTLog;
import org.eclipse.rse.core.subsystems.IConnectorService;
import org.eclipse.rse.core.subsystems.ISubSystem;

public class AbstractRemoteService {

	protected IConnectorService fConnectorService;
	protected ICIndexSubsystem fIndexSubsystem;

	public AbstractRemoteService(ICIndexSubsystem subSystem) {
		fIndexSubsystem = subSystem;
	}

	public AbstractRemoteService(IConnectorService connectorService) {
		fConnectorService = connectorService;
	}

	protected ICIndexSubsystem getSubSystem() {
		if (fIndexSubsystem == null) {

			if (fConnectorService != null) {

				ISubSystem[] subSystems = fConnectorService.getSubSystems();

				for (int k = 0; k < subSystems.length; k++) {
					if (subSystems[k] instanceof ICIndexSubsystem)

						fIndexSubsystem = (ICIndexSubsystem) subSystems[k];
				}
			}
		}
		
		return fIndexSubsystem;
	}

	protected ITranslationUnit adaptWorkingCopy(IWorkingCopy workingCopy) throws CModelException {
		ITranslationUnit unit;
		
		if (workingCopy.isConsistent()) {
			unit = ModelAdapter.adaptElement(null, workingCopy, 0, true);
		} else {
			String contents = new String (workingCopy.getContents());
			unit = new WorkingCopy(null, workingCopy, contents);
		}
		
		if (unit instanceof TranslationUnit) {
			// can't trust getting a resource from adapted C elements
			IResource resource = workingCopy.getResource(); //might be null if it is a remote TU
			ICProject project = workingCopy.getCProject();
			IProject rproject = project.getProject();
			IResource infoResource = resource != null ? resource : rproject;
			
			IScannerInfo scannerInfo = RemoteIndexerInfoProviderFactory.getScannerInfo(infoResource);
			
			Map<String,String> langaugeProperties = null;
			try {
				String languageId = unit.getLanguage().getId();
				langaugeProperties = RemoteIndexerInfoProviderFactory.getLanguageProperties(languageId, rproject);
			} catch(Exception e) {
				RDTLog.logError(e);
			}
			
			((TranslationUnit) unit).setASTContext(scannerInfo, langaugeProperties);
		}
		return unit;
	}

}