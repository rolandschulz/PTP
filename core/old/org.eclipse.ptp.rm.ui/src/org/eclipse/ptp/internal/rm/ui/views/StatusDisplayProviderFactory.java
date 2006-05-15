/*******************************************************************************
 * Copyright (c) 2006 The Regents of the University of California. 
 * This material was produced under U.S. Government contract W-7405-ENG-36 
 * for Los Alamos National Laboratory, which is operated by the University 
 * of California for the U.S. Department of Energy. The U.S. Government has 
 * rights to use, reproduce, and distribute this software. NEITHER THE 
 * GOVERNMENT NOR THE UNIVERSITY MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR 
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified 
 * to produce derivative works, such modified software should be clearly marked, 
 * so as not to confuse it with the version available from LANL.
 * 
 * Additionally, this program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * LA-CC 04-115
 *******************************************************************************/
package org.eclipse.ptp.internal.rm.ui.views;

import org.eclipse.ptp.rm.core.RMJobStatus;
import org.eclipse.ptp.rm.core.RMStatus;
import org.eclipse.swt.graphics.Image;

public class StatusDisplayProviderFactory {

	private static class RMStatusDP implements IStatusDisplayProvider {
		private final RMStatus status;

		public RMStatusDP(RMStatus status) {
			this.status = status;
		}

		public Image getImage() {
			// TODO Auto-generated method stub
			return null;
		}

		public String getText() {
			return status.toString();
		}
	}

	private static class RMJobStatusDP implements IStatusDisplayProvider {
		private final RMJobStatus status;

		public RMJobStatusDP(RMJobStatus status) {
			this.status = status;
		}

		public Image getImage() {
			// TODO Auto-generated method stub
			return null;
		}

		public String getText() {
			return status.toString();
		}
	}

	public static IStatusDisplayProvider create(RMStatus status) {
		return new RMStatusDP(status);
	}

	public static IStatusDisplayProvider create(RMJobStatus status) {
		return new RMJobStatusDP(status);
	}

}
