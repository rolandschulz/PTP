/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.generic.ui;

import org.eclipse.ptp.core.attributes.EnumeratedAttribute;
import org.eclipse.ptp.core.attributes.StringAttribute;
import org.eclipse.ptp.core.elements.IPElement;
import org.eclipse.ptp.core.elements.IPJob;
import org.eclipse.ptp.core.elements.attributes.JobAttributes;
import org.eclipse.ptp.core.elements.attributes.ProcessAttributes;
import org.eclipse.ptp.core.elements.attributes.ProcessAttributes.State;
import org.eclipse.ptp.internal.ui.model.PProcessUI;
import org.eclipse.ptp.rm.ui.RMModelImages;
import org.eclipse.ptp.ui.IRuntimeModelPresentation;
import org.eclipse.ptp.ui.model.IElement;
import org.eclipse.swt.graphics.Image;

public class GenericRMRuntimeModelPresentation implements IRuntimeModelPresentation {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.ui.IRuntimeModelPresentation#getImage(java.lang.Object)
	 */
	public Image getImage(Object object) {
		if (object instanceof IElement) {
			IElement element = (IElement) object;
			IPElement pElement = element.getPElement();
			// FIXME PProcessUI goes away when we address UI scalability. See
			// Bug 311057
			if (pElement instanceof PProcessUI) {
				EnumeratedAttribute<State> status = pElement.getAttribute(ProcessAttributes.getStateAttributeDefinition());
				if (status != null) {
					if (element.isSelected()) {
						return RMModelImages.procSelImages.get(status.getValueAsString());
					}
					return RMModelImages.procImages.get(status.getValueAsString());
				}
			}
		} else if (object instanceof IPJob) {
			IPJob job = (IPJob) object;
			StringAttribute status = job.getAttribute(JobAttributes.getStatusAttributeDefinition());
			if (status != null) {
				if (job.isDebug()) {
					return RMModelImages.jobDebugImages.get(status.getValue());
				}
				return RMModelImages.jobImages.get(status.getValue());
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.ui.IRuntimeModelPresentation#getText(java.lang.Object)
	 */
	public String getText(Object object) {
		IPElement element = null;
		if (object instanceof IElement) {
			element = ((IElement) object).getPElement();
		} else if (object instanceof IPElement) {
			element = (IPElement) object;
		}
		if (element != null) {
			EnumeratedAttribute<State> state = element.getAttribute(ProcessAttributes.getStateAttributeDefinition());
			if (state != null) {
				return state.getValueAsString();
			}
		}
		return null;
	}

}
