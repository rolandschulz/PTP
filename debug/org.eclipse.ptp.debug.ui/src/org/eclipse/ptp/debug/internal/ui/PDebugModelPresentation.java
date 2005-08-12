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
package org.eclipse.ptp.debug.internal.ui;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.debug.ui.IValueDetailListener;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.ptp.debug.core.breakpoints.IPAddressBreakpoint;
import org.eclipse.ptp.debug.core.breakpoints.IPBreakpoint;
import org.eclipse.ptp.debug.core.breakpoints.IPFunctionBreakpoint;
import org.eclipse.ptp.debug.core.breakpoints.IPLineBreakpoint;
import org.eclipse.ptp.debug.ui.PTPDebugUIPlugin;
import org.eclipse.ptp.debug.ui.UIDebugManager;
import org.eclipse.ptp.ui.model.IElementHandler;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;

/**
 * @author Clement chu
 *
 */
public class PDebugModelPresentation extends LabelProvider implements IDebugModelPresentation {
	private static PDebugModelPresentation instance = null;
	public final static String DISPLAY_FULL_PATHS = "DISPLAY_FULL_PATHS";
	
	protected UIDebugManager uiDebugManager = null;
	protected Map attributes = new HashMap(3);
	private OverlayImageCache imageCache = new OverlayImageCache();
	
	public PDebugModelPresentation() {
		uiDebugManager = PTPDebugUIPlugin.getDefault().getUIDebugManager();
	}
	
	public static PDebugModelPresentation getDefault() {
		if (instance == null)
			instance = new PDebugModelPresentation();
		return instance;
	}

	public String getEditorId(IEditorInput input, Object element) {
		if (input != null) {
			IEditorRegistry registry = PlatformUI.getWorkbench().getEditorRegistry();
			IEditorDescriptor descriptor = registry.getDefaultEditor(input.getName());
			if (descriptor != null)
				return descriptor.getId();
			/*
			 * FIXME
			id = (descriptor != null) ? descriptor.getId() : CUIPlugin.EDITOR_ID;
			*/
		}
		return null;
	}

	public IEditorInput getEditorInput(Object element) {
		if (element instanceof IMarker) {
			IResource resource = ((IMarker)element).getResource();
			if (resource instanceof IFile)
				return new FileEditorInput((IFile)resource);
		}
		if (element instanceof IFile) {
			return new FileEditorInput((IFile)element);
		}
		if (element instanceof IPBreakpoint) {
			IPBreakpoint pbk = (IPBreakpoint)element;
			IFile file = null;
			try {
				String handle = pbk.getSourceHandle();
				IPath path = new Path(handle);
				if (path.isValidPath(handle)) {
					IFile[] files = ResourcesPlugin.getWorkspace().getRoot().findFilesForLocation(path);
					if (files.length > 0)
						file = files[0];
					/*
					 * FIXME
					else {
						File fsFile = new File(handle);
						if (fsFile.isFile() && fsFile.exists()) {
							return new ExternalEditorInput(new LocalFileStorage(fsFile));
						}
					}
					*/
				}
			} catch (CoreException e) {}
			
			if (file == null)
				file = (IFile)pbk.getMarker().getResource().getAdapter(IFile.class);
			if (file != null)
				return new FileEditorInput(file);
		}
		/*
		 * FIXME
		if (element instanceof FileStorage || element instanceof LocalFileStorage) {
			return new ExternalEditorInput((IStorage)element);
		}
		*/
		return null;
	}

	public void computeDetail(IValue value, IValueDetailListener listener) {
		// TODO 
		System.out.println("PDebugModelPresentation - ComputeDetails");
	}

	public void setAttribute(String attribute, Object value) {
		if (value == null)
			return;
		getAttributes().put(attribute, value);
	}
	
	public Image getImage(Object element) {
		Image baseImage = getBaseImage(element);
		if (baseImage != null) {
			ImageDescriptor[] overlays = new ImageDescriptor[]{ null, null, null, null };
			/*
			if (element instanceof IPDebugElementStatus && !((IPDebugElementStatus)element).isOK()) {
				switch(((IPDebugElementStatus)element).getSeverity()) {
					case ICDebugElementStatus.WARNING:
						overlays[OverlayImageDescriptor.BOTTOM_LEFT] = CDebugImages.DESC_OVRS_WARNING;
						break;
					case ICDebugElementStatus.ERROR:
						overlays[OverlayImageDescriptor.BOTTOM_LEFT] = CDebugImages.DESC_OVRS_ERROR;
						break;
				}
			}
			if (element instanceof IWatchExpression && ((IWatchExpression)element).hasErrors())
				overlays[OverlayImageDescriptor.BOTTOM_LEFT] = PDebugImages.DESC_OVRS_ERROR;
			if (element instanceof IPVariable && ((IPVariable)element).isArgument())
				overlays[OverlayImageDescriptor.TOP_RIGHT] = PDebugImages.DESC_OVRS_ARGUMENT;
			if (element instanceof IPGlobalVariable && !(element instanceof IRegister))
				overlays[OverlayImageDescriptor.TOP_RIGHT] = PDebugImages.DESC_OVRS_GLOBAL;
			*/
			return getImageCache().getImageFor(new OverlayImageDescriptor(baseImage, overlays));
		}
		return null;
	}
	
	private Image getBaseImage(Object element) {
		//TODO element can be DebugTarget, Thread
		if (element instanceof IMarker) {
			IBreakpoint bp = getBreakpoint((IMarker)element);
			if (bp != null && bp instanceof IPBreakpoint ) {
				return getBreakpointImage((IPBreakpoint)bp);
			}
		}
		if (element instanceof IPBreakpoint) {
			return getBreakpointImage((IPBreakpoint)element);
		}
		return super.getImage(element);
	}	

	protected Image getBreakpointImage(IPBreakpoint breakpoint) {
		try {
			if (breakpoint instanceof IPLineBreakpoint)
				return getLineBreakpointImage((IPLineBreakpoint)breakpoint);
			//TODO implement WatchBreakpoint
		} catch(CoreException e) {
		}
		return null;
	}
	
	protected Image getLineBreakpointImage(IPLineBreakpoint breakpoint) throws CoreException {
		String job_id = breakpoint.getJobId();
		String cur_job_id = uiDebugManager.getCurrentJobId();

		// Display nothing if the breakpoint is not in current job
		if (cur_job_id == null || !cur_job_id.equals(job_id))
			return new Image(null, 1, 1);
		
		String descriptor = null;
		IElementHandler setManager = uiDebugManager.getElementHandler(job_id);
		if (setManager == null) //no job running
			descriptor = breakpoint.isEnabled() ? PDebugImage.IMG_DEBUG_ONESET_EN : PDebugImage.IMG_DEBUG_ONESET_DI;
		else {//created job
			String cur_set_id = breakpoint.getCurSetId();
			String bpt_set_id = breakpoint.getSetId();
			
			if (bpt_set_id.equals(cur_set_id)) {
				descriptor = breakpoint.isEnabled() ? PDebugImage.IMG_DEBUG_ONESET_EN : PDebugImage.IMG_DEBUG_ONESET_DI;
			}
			else {
				if (setManager.getSet(bpt_set_id).isContainSets(cur_set_id))
					descriptor = breakpoint.isEnabled() ? PDebugImage.IMG_DEBUG_MULTISET_EN : PDebugImage.IMG_DEBUG_MULTISET_DI;
				else
					descriptor = breakpoint.isEnabled() ? PDebugImage.IMG_DEBUG_NOSET_EN : PDebugImage.IMG_DEBUG_NOSET_DI;
			}
		}
		return getImageCache().getImageFor(new OverlayImageDescriptor(PDebugImage.getImage(descriptor), computeBreakpointOverlays(breakpoint)));
	}

	public String getText(Object element) {
		String bt = getBaseText(element);
		if (bt == null)
			return null;
		StringBuffer baseText = new StringBuffer(bt);
		/*
		if (element instanceof IPDebugElementStatus && !((IPDebugElementStatus)element).isOK()) {
			baseText.append(getFormattedString(" <{0}>", ((IPDebugElementStatus)element).getMessage()));
		}
		if (element instanceof IAdaptable) {
			IEnableDisableTarget target = (IEnableDisableTarget)((IAdaptable)element).getAdapter(IEnableDisableTarget.class);
			if (target != null) {
				if (!target.isEnabled()) {
					baseText.append(' ');
					baseText.append(PDebugUIMessages.getString("PTPDebugModelPresentation.25"));
				}
			}
		}
		*/
		return baseText.toString();
	}

	private String getBaseText( Object element ) {
		boolean showQualified = isShowQualifiedNames();
		StringBuffer label = new StringBuffer();
		try {
			//TODO element can be IValue, IDebugTarget etc.
			if (element instanceof IMarker) {
				IBreakpoint breakpoint = getBreakpoint((IMarker)element);
				if (breakpoint != null) {
					return getBreakpointText(breakpoint, showQualified);
				}
				return null;
			}
			if (element instanceof IBreakpoint) {
				return getBreakpointText((IBreakpoint)element, showQualified);
			}
			if (element instanceof IDebugTarget)
				label.append(getTargetText((IDebugTarget)element, showQualified));
			else if ( element instanceof IThread )
				label.append(getThreadText((IThread)element, showQualified));
			if ( label.length() > 0 ) {
				return label.toString();
			}
		}
		catch (DebugException e) {
			return "Cannot find base text err: " + e.getMessage();
		}
		catch (CoreException e) {
			//TODO should log it
			System.out.println("GetBaseText Err: " + e.getMessage());
		}
		return null;
	}

	protected boolean isShowQualifiedNames() {
		Boolean showQualified = (Boolean)getAttributes().get(DISPLAY_FULL_PATHS);
		showQualified = showQualified == null ? Boolean.FALSE : showQualified;
		return showQualified.booleanValue();
	}

	private Map getAttributes() {
		return attributes;
	}

	private OverlayImageCache getImageCache() {
		return imageCache;
	}

	private boolean isEmpty(String string) {
		return (string == null || string.trim().length() == 0);
	}

	protected IBreakpoint getBreakpoint(IMarker marker) {
		return DebugPlugin.getDefault().getBreakpointManager().getBreakpoint(marker);
	}

	protected String getBreakpointText(IBreakpoint breakpoint, boolean qualified) throws CoreException {
		if (breakpoint instanceof IPLineBreakpoint) {
			return getLineBreakpointText((IPLineBreakpoint)breakpoint, qualified);
		}
		return "";
	}

	protected String getLineBreakpointText(IPLineBreakpoint breakpoint, boolean qualified) throws CoreException {
		StringBuffer label = new StringBuffer();
		appendSourceName(breakpoint, label, qualified);
		appendLineNumber(breakpoint, label);
		appendStatus(breakpoint, label);
		return label.toString();
	}
	protected StringBuffer appendSourceName(IPBreakpoint breakpoint, StringBuffer label, boolean qualified) throws CoreException {
		String handle = breakpoint.getSourceHandle();
		if (!isEmpty( handle)) {
			IPath path = new Path(handle);
			if (path.isValidPath(handle)) {
				label.append(qualified ? path.toOSString() : path.lastSegment());
			}
		}
		return label;
	}
	protected StringBuffer appendLineNumber(IPLineBreakpoint breakpoint, StringBuffer label) throws CoreException {
		int lineNumber = breakpoint.getLineNumber();
		if (lineNumber > 0) {
			label.append(" ");
			label.append("[Line: " + lineNumber + "]");
		}
		return label;
	}
	protected StringBuffer appendStatus(IPBreakpoint breakpoint, StringBuffer label) throws CoreException {
		String job_id = breakpoint.getJobId();
		String jobName = job_id.length()==0?"N/A":uiDebugManager.getName(job_id);
		label.append(" ");
		label.append("<Job: " + jobName + " - Set: " + breakpoint.getSetId() + ">");
		return label;
	}

	private ImageDescriptor[] computeBreakpointOverlays(IPBreakpoint breakpoint) {
		ImageDescriptor[] overlays = new ImageDescriptor[]{ null, null, null, null };
		try {
			if (breakpoint.isConditional()) {
				overlays[OverlayImageDescriptor.TOP_LEFT] = (breakpoint.isEnabled()) ? PDebugImage.ID_IMG_DEBUG_OVER_BPT_COND_EN : PDebugImage.ID_IMG_DEBUG_OVER_BPT_COND_DI;
			}
			if (breakpoint.isInstalled()) {
				overlays[OverlayImageDescriptor.BOTTOM_LEFT] = (breakpoint.isEnabled()) ? PDebugImage.ID_IMG_DEBUG_OVER_BPT_INST_EN : PDebugImage.ID_IMG_DEBUG_OVER_BPT_INST_DI;
			}
			if (breakpoint instanceof IPAddressBreakpoint) {
				overlays[OverlayImageDescriptor.TOP_RIGHT] = (breakpoint.isEnabled()) ? PDebugImage.ID_IMG_DEBUG_OVER_BPT_ADDR_EN : PDebugImage.ID_IMG_DEBUG_OVER_BPT_ADDR_DI;
			}
			if (breakpoint instanceof IPFunctionBreakpoint) {
				overlays[OverlayImageDescriptor.TOP_RIGHT] = (breakpoint.isEnabled()) ? PDebugImage.ID_IMG_DEBUG_OVER_BPT_FUNC_EN : PDebugImage.ID_IMG_DEBUG_OVER_BPT_FUNC_DI;
			}
		} catch(CoreException e) {
			//TODO log it
			System.out.println("computerBreakpointOverlays err: " + e.getMessage());
		}
		return overlays;
	}

	protected String getTargetText(IDebugTarget target, boolean qualified) throws DebugException {
		return "TO DO LATER";
	}
	protected String getThreadText(IThread thread, boolean qualified) throws DebugException {
		return "TO DO LATER";
	}
	
	public void dispose() {
		getImageCache().disposeAll();
		attributes.clear();
		super.dispose();
	}	
}
