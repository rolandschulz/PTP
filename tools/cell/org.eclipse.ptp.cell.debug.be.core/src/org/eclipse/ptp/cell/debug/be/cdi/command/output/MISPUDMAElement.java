/**
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *
 */
package org.eclipse.ptp.cell.debug.be.cdi.command.output;

import org.eclipse.cdt.debug.mi.core.output.MIConst;
import org.eclipse.cdt.debug.mi.core.output.MIResult;
import org.eclipse.cdt.debug.mi.core.output.MIValue;

/**
 * 
 * @author Ricardo M. Matinata
 * @since 1.3
 *
 */
public class MISPUDMAElement extends MISPUTupleElement {

	final public static String DMA_INFO_TYPE = "dma_info_type"; //$NON-NLS-1$
	
	final public static String DMA_INFO_MASK = "dma_info_mask"; //$NON-NLS-1$
	
	final public static String DMA_INFO_STATUS = "dma_info_status"; //$NON-NLS-1$
	
	final public static String DMA_INFO_STALLNNOTIFY = "dma_info_stall_and_notify"; //$NON-NLS-1$
	
	final public static String DMA_INFO_ATOMICCMDST = "dma_info_atomic_command_status"; //$NON-NLS-1$

	public MISPUDMAElement(String name, String value) {
		super(name,value);
	}
	
	public MISPUDMAElement(MIResult result) {
		super(result);
	}

	protected void parseResult(MIResult result) {
		
		String var;
		MIValue value = null;
		
		// event_mask
		var = result.getVariable();
		value = result.getMIValue();
		if (var.equals(DMA_INFO_TYPE)) {
			// dma_info_type
			setName(DMA_INFO_TYPE);
			if (value != null && value instanceof MIConst) {
				setValue(((MIConst)value).getCString());
			} else {
				setValue(EMPTY);
			}
		} else if (var.equals(DMA_INFO_MASK)) {
			// dma_info_mask
			setName(DMA_INFO_MASK);
			if (value != null && value instanceof MIConst) {
				setValue(((MIConst)value).getCString());
			} else {
				setValue(EMPTY);
			}
		} else if (var.equals(DMA_INFO_STATUS)) {
			// dma_info_status
			setName(DMA_INFO_STATUS);
			if (value != null && value instanceof MIConst) {
				setValue(((MIConst)value).getCString());
			} else {
				setValue(EMPTY);
			}
		} else if (var.equals(DMA_INFO_STALLNNOTIFY)) {
			// dma_info_stall_and_notify
			setName(DMA_INFO_STALLNNOTIFY);
			if (value != null && value instanceof MIConst) {
				setValue(((MIConst)value).getCString());
			} else {
				setValue(EMPTY);
			}
		} else if (var.equals(DMA_INFO_ATOMICCMDST)) {
			// dma_info_atomic_command_status
			setName(DMA_INFO_ATOMICCMDST);
			if (value != null && value instanceof MIConst) {
				setValue(((MIConst)value).getCString());
			} else {
				setValue(EMPTY);
			}
		}

	}

}