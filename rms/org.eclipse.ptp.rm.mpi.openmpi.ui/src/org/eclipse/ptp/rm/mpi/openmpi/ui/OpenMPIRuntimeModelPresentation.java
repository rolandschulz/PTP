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
package org.eclipse.ptp.rm.mpi.openmpi.ui;

import org.eclipse.ptp.core.attributes.StringAttribute;
import org.eclipse.ptp.core.elements.IPElement;
import org.eclipse.ptp.core.elements.IPJob;
import org.eclipse.ptp.core.elements.IPProcess;
import org.eclipse.ptp.core.elements.attributes.ProcessAttributes;
import org.eclipse.ptp.rm.ui.RMModelImages;
import org.eclipse.ptp.ui.IRuntimeModelPresentation;
import org.eclipse.ptp.ui.model.IElement;
import org.eclipse.swt.graphics.Image;

public class OpenMPIRuntimeModelPresentation implements IRuntimeModelPresentation {

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.IRuntimeModelPresentation#getImage(java.lang.Object)
	 */
	public Image getImage(Object object) {
		if (object instanceof IElement) {
			IElement element = (IElement)object;
			IPElement pElement = element.getPElement();
			if (pElement instanceof IPProcess) {
				StringAttribute status = pElement.getAttribute(ProcessAttributes.getStatusAttributeDefinition());
				if (status != null) {
					if (element.isSelected()) {
						return RMModelImages.procSelImages.get(status.getValue());
					}
					return RMModelImages.procImages.get(status.getValue());
				}
			} else if (pElement instanceof IPJob) {
				StringAttribute status = pElement.getAttribute(ProcessAttributes.getStatusAttributeDefinition());
				if (status != null) {
					if (((IPJob)pElement).isDebug()) {
						return RMModelImages.jobDebugImages.get(status.getValue());
					}
					return RMModelImages.jobImages.get(status.getValue());
				}
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.IRuntimeModelPresentation#getText(java.lang.Object)
	 */
	public String getText(Object object) {
		return null;
	}

}
