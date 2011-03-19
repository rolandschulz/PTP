/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.pbs.core.rmsystem;

import org.eclipse.ptp.rm.pbs.core.templates.PBSBatchScriptTemplateManager;
import org.eclipse.ptp.rmsystem.AbstractResourceManagerConfiguration;
import org.eclipse.ptp.rtsystem.AbstractRuntimeResourceManager;
import org.eclipse.ptp.rtsystem.AbstractRuntimeResourceManagerControl;
import org.eclipse.ptp.rtsystem.AbstractRuntimeResourceManagerMonitor;
import org.eclipse.ptp.rtsystem.IRuntimeSystem;

public class PBSResourceManager extends AbstractRuntimeResourceManager {
	private PBSBatchScriptTemplateManager fTemplateManager = null;

	/**
	 * @since 5.0
	 */
	public PBSResourceManager(AbstractResourceManagerConfiguration config, AbstractRuntimeResourceManagerControl control,
			AbstractRuntimeResourceManagerMonitor monitor) {
		super(config, control, monitor);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rtsystem.AbstractRuntimeResourceManager#getRuntimeSystem
	 * ()
	 */
	@Override
	public IRuntimeSystem getRuntimeSystem() {
		return super.getRuntimeSystem();
	}

	/**
	 * @since 5.0
	 */
	public PBSBatchScriptTemplateManager getTemplateManager() {
		if (fTemplateManager == null) {
			try {
				fTemplateManager = new PBSBatchScriptTemplateManager(this);
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
		return fTemplateManager;
	}

}