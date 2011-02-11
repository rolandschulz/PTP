/**********************************************************************
 * Copyright (c) 2007,2010 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.pldt.lapi.views;

import org.eclipse.ptp.pldt.common.views.SimpleTableMarkerView;
import org.eclipse.ptp.pldt.lapi.Activator;
import org.eclipse.ptp.pldt.lapi.IDs;
import org.eclipse.ptp.pldt.lapi.messages.Messages;

/**
 * A LAPI artifact view based on SimpleTableView.<br>
 * Note that the ID must be unique.
 * 
 */
public class LapiArtifactView extends SimpleTableMarkerView
{
	public LapiArtifactView()
	{
		// if you need to read icon images, etc. from the plug-in, be sure to
		// pass
		// in an actual Plugin class instance for first arg
		super(Activator.getDefault(), Messages.LapiArtifactView_lapi_artifact_column_title,
				Messages.LapiArtifactView_lapi_artifacts_plural, Messages.LapiArtifactView_construct_column_title, IDs.MARKER_ID);
	}
}
