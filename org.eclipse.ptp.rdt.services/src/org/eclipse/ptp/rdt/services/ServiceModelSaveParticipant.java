/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - Initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.rdt.services;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.resources.ISaveContext;
import org.eclipse.core.resources.ISaveParticipant;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ptp.rdt.services.core.ServiceModelManager;

/**
 * Persists the service model configuration to the workspace
 * metadata.
 */
public class ServiceModelSaveParticipant implements ISaveParticipant {
	public void doneSaving(ISaveContext context) {
		int saveNumber = context.getPreviousSaveNumber();
		IPath path = Activator.getServiceModelStateFilePath(saveNumber);
		File file = path.toFile();
		file.delete();
	}

	public void prepareToSave(ISaveContext context) throws CoreException {
	}

	public void rollback(ISaveContext context) {
		int saveNumber = context.getSaveNumber();
		IPath path = Activator.getServiceModelStateFilePath(saveNumber);
		File file = path.toFile();
		file.delete();
	}

	public void saving(ISaveContext context) throws CoreException {
		switch (context.getKind()) {
		case ISaveContext.FULL_SAVE:
			int saveNumber = context.getSaveNumber();
			IPath path = Activator.getServiceModelStateFilePath(saveNumber);
			File file = path.toFile();
			ServiceModelManager manager = ServiceModelManager.getInstance();
			try {
					manager.saveModelConfiguration(file);
				} catch (IOException e) {
					throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage()));
				}
			context.map(Activator.getServiceModelStateFilePath(), path);
			context.needSaveNumber();
			break;
		case ISaveContext.PROJECT_SAVE:
			break;
		case ISaveContext.SNAPSHOT:
			break;
		}
	}
}
