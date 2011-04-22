/**********************************************************************
 * Copyright (c) 2007 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.pldt.mpi.analysis.analysis;

import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.ptp.pldt.mpi.analysis.cdt.graphs.impl.Block;

/**
 * Barrier Control Flow Graph Block
 * 
 */
public class BarrierCFGBlock extends Block {
	protected BarrierExpression BE_;

	public BarrierCFGBlock(IASTNode content, IASTStatement parent, int type) {
		super(content, parent, type);
		BE_ = null;
	}

	public BarrierExpression getBE() {
		return BE_;
	}

	public void setBE(BarrierExpression be) {
		BE_ = be;
	}
}
