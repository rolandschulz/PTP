/*******************************************************************************
 * Copyright (c) 2009 School of Computer Science,
 * National University of Defense Technology, P.R.China.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jie Jiang,National University of Defense Technology
 ******************************************************************************/
package org.eclipse.ptp.rm.slurm.ui;

import org.eclipse.ptp.core.attributes.StringAttribute;
import org.eclipse.ptp.core.elements.IPElement;
import org.eclipse.ptp.core.elements.IPJob;
import org.eclipse.ptp.core.elements.IPNode;
import org.eclipse.ptp.core.elements.attributes.JobAttributes;
import org.eclipse.ptp.core.elements.attributes.NodeAttributes;
import org.eclipse.ptp.internal.ui.model.PProcessUI;
import org.eclipse.ptp.ui.IRuntimeModelPresentation;
import org.eclipse.ptp.ui.model.IElement;
import org.eclipse.swt.graphics.Image;

public class SLURMRuntimeModelPresentation implements IRuntimeModelPresentation {
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.IRuntimeModelPresentation#getImage(java.lang.Object)
	 */
	public Image getImage(Object object) {
		if (object instanceof IElement) {
			IElement element = (IElement)object;
			IPElement pElement = element.getPElement();
			if (pElement instanceof PProcessUI) {
				PProcessUI process = (PProcessUI) pElement;
				//StringAttribute status = pElement.getJob.getAttribute(ProcessAttributes.getStatusAttributeDefinition());
				StringAttribute status = process.getJob().getAttribute(JobAttributes.getStatusAttributeDefinition());
				if (status != null) {
					if (element.isSelected()) {
						return SLURMModelImages.procSelImages.get(status.getValue());
					}
					return SLURMModelImages.procImages.get(status.getValue());
				}
			} else if (pElement instanceof IPNode) {
				StringAttribute status = pElement.getAttribute(NodeAttributes.getStatusAttributeDefinition());
				if (status != null) {
					return SLURMModelImages.nodeImages.get(status.getValue());
				}
			}
		} else if (object instanceof IPJob) {
			IPJob job = (IPJob) object;
			StringAttribute status = job.getAttribute(JobAttributes.getStatusAttributeDefinition());
			if (status != null) {
				if (job.isDebug()) {
					return SLURMModelImages.jobDebugImages.get(status.getValue());
				}
				return SLURMModelImages.jobImages.get(status.getValue());
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.IRuntimeModelPresentation#getText(java.lang.Object)
	 */
	public String getText(Object object) {
/*		
		IPElement element = null;
		if (object instanceof IElement) {
			element = ((IElement)object).getPElement();
		} else if (object instanceof IPElement) {
			element = (IPElement)object;
		}
		if (element != null) {
			StringAttribute status = element.getAttribute(ProcessAttributes.getStatusAttributeDefinition());
			if (status != null) {
				return status.getValue();
			}
		}
*/		
		return null;
	}

}
