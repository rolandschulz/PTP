/*******************************************************************************
 * Copyright (c) 2005 The Regents of the University of California. 
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
package org.eclipse.ptp.ui;

import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.ptp.core.IPElement;
import org.eclipse.ptp.core.IPNode;
import org.eclipse.ptp.core.IPProcess;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

/**
 *
 */
public class ParallelElementLabelProvider extends LabelProvider implements IColorProvider, ParallelElementStatus {   
	private Display display = Display.getCurrent();        
    public Color[] statusColors = {
    	//node
    	display.getSystemColor(SWT.COLOR_BLUE),			//excl
    	display.getSystemColor(SWT.COLOR_CYAN),			//shared
    	display.getSystemColor(SWT.COLOR_DARK_BLUE),	//excl
    	display.getSystemColor(SWT.COLOR_DARK_CYAN),	//shared
		display.getSystemColor(SWT.COLOR_DARK_GRAY),	//down
		display.getSystemColor(SWT.COLOR_RED),			//error
		display.getSystemColor(SWT.COLOR_DARK_GREEN),	//exited
		display.getSystemColor(SWT.COLOR_GREEN),		//running
		display.getSystemColor(SWT.COLOR_DARK_RED),		//unknown
		display.getSystemColor(SWT.COLOR_GRAY),			//up
		//process
		display.getSystemColor(SWT.COLOR_RED),			//error
		display.getSystemColor(SWT.COLOR_DARK_GREEN),	//exited
		display.getSystemColor(SWT.COLOR_DARK_YELLOW),	//exited signal
		display.getSystemColor(SWT.COLOR_GREEN),		//running
		display.getSystemColor(SWT.COLOR_BLUE),			//starting
		display.getSystemColor(SWT.COLOR_DARK_BLUE)		//stopped		
    };
    
	private Image[] parallelImages = {
		ParallelImages.getImage(ParallelImages.IMG_NODE_USER_ALLOC_EXCL),		//excl
		ParallelImages.getImage(ParallelImages.IMG_NODE_USER_ALLOC_SHARED),		//shared
		ParallelImages.getImage(ParallelImages.IMG_NODE_OTHER_ALLOC_EXCL),		//excl
		ParallelImages.getImage(ParallelImages.IMG_NODE_OTHER_ALLOC_SHARED),	//shared
		ParallelImages.getImage(ParallelImages.IMG_NODE_DOWN),					//node down
		ParallelImages.getImage(ParallelImages.IMG_NODE_ERROR),					//node error
		ParallelImages.getImage(ParallelImages.IMG_NODE_EXITED),				//node exited
		ParallelImages.getImage(ParallelImages.IMG_NODE_RUNNING),				//node running
		ParallelImages.getImage(ParallelImages.IMG_NODE_UNKNOWN),				//node unknown
		ParallelImages.getImage(ParallelImages.IMG_NODE_UP),					//node up
		ParallelImages.getImage(ParallelImages.IMG_PROC_ERROR),					//proc error
		ParallelImages.getImage(ParallelImages.IMG_PROC_EXITED),				//proc exited
		ParallelImages.getImage(ParallelImages.IMG_PROC_EXITED_SIGNAL),			//proc exited signal
		ParallelImages.getImage(ParallelImages.IMG_PROC_RUNNING),				//proc running
		ParallelImages.getImage(ParallelImages.IMG_PROC_STARTING),				//proc starting
		ParallelImages.getImage(ParallelImages.IMG_PROC_STOPPED),				//stopped
	};
       	
	/**
	 * @see ILabelProvider#getImage
	 */
	public Image getImage(Object element) {
		int statusIndex = getParallelStatusIndex(element);
		if (statusIndex < 0 || statusIndex >= parallelImages.length)
			return null;
		
		return parallelImages[statusIndex];
	}
	
	/**
	 * @see ILabelProvider#getText
	 */
	public String getText(Object element) {
		if (element instanceof IPElement) {
		    return ((IPElement)element).getElementName();
		}
		return "";
	}
	
	public Color getForeground(Object element) {
		return null;
		/*
		int statusIndex = getParallelStatusIndex(element);
		if (statusIndex < 0 || statusIndex >= statusColors.length)
			return null;
		
		return statusColors[statusIndex];
		*/
	}
		
	public int getParallelStatusIndex(Object element) {
	    if (element instanceof IPNode) {
	        IPNode node = (IPNode)element;
	        
	        if(node.hasChildren()) {
	        	if (!node.isAllStop())
	        		return NODE_RUNNING;
	        	else
	        		return NODE_EXITED;
	        
	        }
	        else {
		        String nodeState = node.getState();

	        	if (nodeState != null && nodeState.equals("up")) {
	                String user = node.getUser();
	                if (node.isCurrentUser()) {
	                    String mode = node.getMode();
	                    if (mode != null && mode.equals("0100"))
	                        return NODE_USER_ALLOC_EXCL;
	                    else if (mode != null && (mode.equals("0110") || mode.equals("0111") || mode.equals("0101")))
	                        return NODE_USER_ALLOC_SHARED;
	                }
	                else if (user != null && !user.equals("root")) {
	                    String mode = node.getMode();
	                    if (mode != null && mode.equals("0100"))
	                        return NODE_OTHER_ALLOC_EXCL;
	                    else if (mode != null && (mode.equals("0110") || mode.equals("0111") || mode.equals("0101")))
	                        return NODE_OTHER_ALLOC_SHARED;
	                }
	                return NODE_UP;
		        }
	       	  	else if(nodeState != null && nodeState.equals("down")) {
	       	  		return NODE_DOWN;
	       	  	}
	       	  	else if(nodeState != null && nodeState.equals("error")) {
	       	  		return NODE_ERROR;
	       	  	}
	        }
	    }
	    else {
	        if (element instanceof IPProcess) {
		        IPProcess process = (IPProcess)element;
		        String state = process.getStatus();
		        if (state == null)
		            return PROC_ERROR;
		        else if (state.equals(IPProcess.STARTING))
		        	return PROC_STARTING;
		        else if (state.equals(IPProcess.RUNNING))
		        	return PROC_RUNNING;
	        	else if(state.equals(IPProcess.EXITED))
	        		return PROC_EXITED;
	        	else if(state.equals(IPProcess.EXITED_SIGNALLED))
	        		return PROC_EXITED_SIGNAL;
	        	else if(state.equals(IPProcess.STOPPED))
	        		return PROC_STOPPED;
	        	else if(state.equals(IPProcess.ERROR))
	        		return PROC_ERROR;
	        	else
	        		return PROC_ERROR;
	        }
	    }
	    return -1;
	}

	/**
	 * @see IColorProvider#getBackground
	 */
	public Color getBackground(Object element) {
	    return null;
	}	
	
	/**
	 * @see IBaseLabelProvider#dispose()
	 */
	public void dispose() {
		statusColors = null;
		parallelImages = null;
		display = null;
	}	
}
