/**********************************************************************
 * Copyright (c) 2007, 2010, 2011 IBM Corporation and University of Illinois.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Jeff Overbey (Illinois) - adaptation to OpenACC
 *******************************************************************************/
package org.eclipse.ptp.pldt.openacc.internal.views;

import org.eclipse.ptp.pldt.common.views.SimpleTableMarkerView;
import org.eclipse.ptp.pldt.openacc.internal.Activator;
import org.eclipse.ptp.pldt.openacc.internal.IDs;
import org.eclipse.ptp.pldt.openacc.internal.messages.Messages;

/**
 * The OpenACC Artifacts view.
 * <p>
 * Note that the ID must be unique.
 * 
 * @author Beth Tibbitts (IBM)
 * @author Jeff Overbey (Illinois)
 */
public class OpenACCArtifactView extends SimpleTableMarkerView
{
	/** Constructor. Invoked dynamically due to class reference in plugin.xml. */
	public OpenACCArtifactView()
	{
		super(Activator.getDefault(),
				Messages.OpenACCArtifactView_openacc_artifact_column_title,
				Messages.OpenACCArtifactView_openacc_artifacts_plural,
				Messages.OpenACCArtifactView_construct_column_title,
				IDs.MARKER_ID);
	}
}
