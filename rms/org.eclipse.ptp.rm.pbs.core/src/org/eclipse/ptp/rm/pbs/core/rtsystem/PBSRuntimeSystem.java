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

package org.eclipse.ptp.rm.pbs.core.rtsystem;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.ptp.core.attributes.IAttribute;
import org.eclipse.ptp.rm.core.rtsystem.AbstractRemoteProxyRuntimeSystem;
import org.eclipse.ptp.rm.pbs.core.Activator;
import org.eclipse.ptp.rm.pbs.core.IPBSNonNLSConstants;
import org.eclipse.ptp.rm.pbs.core.rmsystem.PBSResourceManager;
import org.eclipse.ptp.rm.pbs.core.rmsystem.PBSResourceManagerConfiguration;
import org.eclipse.ptp.rm.pbs.core.templates.PBSBatchScriptTemplate;

/**
 * @since 4.0
 */
public class PBSRuntimeSystem extends AbstractRemoteProxyRuntimeSystem {
	private final PBSResourceManager fRM;

	/**
	 * @since 5.0
	 */
	public PBSRuntimeSystem(PBSResourceManager rm, PBSProxyRuntimeClient proxy) {
		super(proxy);
		fRM = rm;
	}

	/**
	 * Sends only the realized script as attribute.<br>
	 */
	@Override
	public List<IAttribute<?, ?, ?>> getAttributes(ILaunchConfiguration configuration, String mode) throws CoreException {
		List<IAttribute<?, ?, ?>> attrs = super.getAttributes(configuration, mode);

		PBSResourceManagerConfiguration rmConfig = (PBSResourceManagerConfiguration) fRM.getConfiguration();
		String current = rmConfig.getCurrentTemplateName();
		PBSBatchScriptTemplate template = fRM.getTemplateManager().loadTemplate(current, configuration);
		try {
			template.configure();
			attrs.add(template.createScriptAttribute());
		} catch (Throwable t) {
			IStatus status = new Status(Status.ERROR, Activator.getUniqueIdentifier(), IPBSNonNLSConstants.GET_ATTRIBUTES, t);
			throw new CoreException(status);
		}
		System.out.println(attrs);
		return attrs;
	}
}
