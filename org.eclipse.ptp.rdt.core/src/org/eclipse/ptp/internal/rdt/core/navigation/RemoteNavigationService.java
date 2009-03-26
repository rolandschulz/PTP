/*******************************************************************************
 * Copyright (c) 2008, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM - Initial API and implementation
 *     Mike Kucera (IBM)
 *******************************************************************************/
package org.eclipse.ptp.internal.rdt.core.navigation;

import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ptp.internal.rdt.core.RemoteIndexerInfoProviderFactory;
import org.eclipse.ptp.internal.rdt.core.model.Scope;
import org.eclipse.ptp.internal.rdt.core.model.TranslationUnit;
import org.eclipse.ptp.internal.rdt.core.serviceproviders.AbstractRemoteService;
import org.eclipse.ptp.internal.rdt.core.subsystems.ICIndexSubsystem;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.subsystems.IConnectorService;


public class RemoteNavigationService extends AbstractRemoteService implements INavigationService {

	public RemoteNavigationService(IHost host, IConnectorService connectorService) {
		super(host, connectorService);
	}

	
	public OpenDeclarationResult openDeclaration(Scope scope, ITranslationUnit unit, String selectedText, int selectionStart, int selectionLength, IProgressMonitor monitor) {
		// go to the subsystem
		ICIndexSubsystem subsystem = getSubSystem();
		
		if(unit instanceof TranslationUnit) {
			IScannerInfo scannerInfo = null;
			if(unit.getResource() == null) {
				// external translation unit... get scanner info from the context?
				scannerInfo = RemoteIndexerInfoProviderFactory.getScannerInfo(unit.getCProject().getProject());
			}
			else {
				scannerInfo = RemoteIndexerInfoProviderFactory.getScannerInfo(unit.getResource());
			}
			((TranslationUnit)unit).setASTContext(scannerInfo);
		}
		
		return subsystem.openDeclaration(scope, unit, selectedText, selectionStart, selectionLength, monitor);
	}
	
}
