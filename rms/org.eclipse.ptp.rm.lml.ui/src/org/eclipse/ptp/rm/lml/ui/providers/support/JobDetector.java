/**
 * Copyright (c) 2012 Forschungszentrum Juelich GmbH
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * 		Carsten Karbach, FZ Juelich
 */
package org.eclipse.ptp.rm.lml.ui.providers.support;

import java.util.Set;

import org.eclipse.swt.graphics.Point;

/**
 * Classes implementing this interface are usually painting jobs.
 * This interface provides a function for detecting the positions of
 * the painted jobs. The job positions are detected relative to the ScrolledComposite
 * surrounding the top-level NodedisplayComp.
 * 
 * All kinds of classes have to implement this interface: UsagebarPainter, RectanglePaintListener,
 * NodedisplayComp instances. They are all involved in painting job rectangles on the screen.
 * 
 * @author karbach
 * 
 */
public interface JobDetector {

	/**
	 * Triggers the detection of all rectangle positions for the job with the passed ID
	 * painted by this component or its children.
	 * 
	 * @param points
	 *            this set is filled with all positions, where rectangles are painted associated with the passed job id
	 * @param jobId
	 *            the id for which job positions are searched
	 */
	public void detectJobPositions(Set<Point> points, String jobId);

}
