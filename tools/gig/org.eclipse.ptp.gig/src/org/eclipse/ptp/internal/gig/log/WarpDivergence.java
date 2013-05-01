/*******************************************************************************
 * Copyright (c) 2012 Brandon Gibson
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Brandon Gibson - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.ptp.internal.gig.log;

import java.util.List;

/*
 * Contains the data of how a warp diverged
 */
public class WarpDivergence {
	// threads in a warp may diverge into separate paths, this detects them
	private final List<int[]> sets;
	private final int warpNumber;

	/*
	 * each int[] in sets is required to be sorted
	 */
	public WarpDivergence(List<int[]> sets, int warp) {
		this.sets = sets;
		this.warpNumber = warp;
	}

	public List<int[]> getSets() {
		return sets;
	}

	/*
	 * Call this only from the first warp, currently will only return 32 or less (less if fewer than 32 threads even existed)
	 * NVIDIA may change warp sizes.
	 */
	public int getThreadsPerWarp() {
		int ret = 0;
		for (final int[] ia : sets) {
			ret = ret < ia[ia.length - 1] ? ia[ia.length - 1] : ret;
		}
		return ret + 1;
	}

	public int getWarpNumber() {
		return warpNumber;
	}

}
