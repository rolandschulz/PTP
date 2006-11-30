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

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IDisconnect;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.ITerminate;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.core.model.IWatchExpression;
import org.eclipse.debug.ui.IDebugEditorPresentation;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.debug.ui.IValueDetailListener;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.ptp.debug.core.PDebugUtils;
import org.eclipse.ptp.debug.core.cdi.IPCDIBreakpointHit;
import org.eclipse.ptp.debug.core.cdi.IPCDIExitInfo;
import org.eclipse.ptp.debug.core.cdi.IPCDISharedLibraryEvent;
import org.eclipse.ptp.debug.core.cdi.IPCDISignalExitInfo;
import org.eclipse.ptp.debug.core.cdi.IPCDISignalReceived;
import org.eclipse.ptp.debug.core.cdi.IPCDIWatchpointScope;
import org.eclipse.ptp.debug.core.cdi.IPCDIWatchpointTrigger;
import org.eclipse.ptp.debug.core.cdi.model.IPCDISignal;
import org.eclipse.ptp.debug.core.model.IEnableDisableTarget;
import org.eclipse.ptp.debug.core.model.IPAddressBreakpoint;
import org.eclipse.ptp.debug.core.model.IPBreakpoint;
import org.eclipse.ptp.debug.core.model.IPDebugElement;
import org.eclipse.ptp.debug.core.model.IPDebugElementStatus;
import org.eclipse.ptp.debug.core.model.IPDebugTarget;
import org.eclipse.ptp.debug.core.model.IPDummyStackFrame;
import org.eclipse.ptp.debug.core.model.IPFunctionBreakpoint;
import org.eclipse.ptp.debug.core.model.IPLineBreakpoint;
import org.eclipse.ptp.debug.core.model.IPSignal;
import org.eclipse.ptp.debug.core.model.IPStackFrame;
import org.eclipse.ptp.debug.core.model.IPType;
import org.eclipse.ptp.debug.core.model.IPValue;
import org.eclipse.ptp.debug.core.model.IPVariable;
import org.eclipse.ptp.debug.core.model.PDebugElementState;
import org.eclipse.ptp.debug.ui.PTPDebugUIPlugin;
import org.eclipse.ptp.ui.model.IElementHandler;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;

/**
 * @author Clement chu
 * 
 */
public class PDebugModelPresentation extends LabelProvider implements IDebugModelPresentation, IDebugEditorPresentation {
	private static PDebugModelPresentation instance = null;
	public final static String DISPLAY_FULL_PATHS = "DISPLAY_FULL_PATHS";
	private static final String DUMMY_STACKFRAME_LABEL = "...";
	protected UIDebugManager uiDebugManager = null;
	protected Map attributes = new HashMap(3);
	private OverlayImageCache imageCache = new OverlayImageCache();

	/** Constructor
	 * 
	 */
	public PDebugModelPresentation() {
		// make sure using the one created by start up
		if (instance == null)
			instance = this;
	} 
	/** Get instance
	 * @return
	 */
	public static PDebugModelPresentation getDefault() {
		if (instance == null)
			instance = new PDebugModelPresentation();
		return instance;
	}
	/** Get UIDebugManager
	 * @return
	 */
	private UIDebugManager getUIDebugManager() {
		if (uiDebugManager == null) {
			uiDebugManager = PTPDebugUIPlugin.getUIDebugManager();
		}
		return uiDebugManager;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ISourcePresentation#getEditorId(org.eclipse.ui.IEditorInput, java.lang.Object)
	 */
	public String getEditorId(IEditorInput input, Object element) {
		if (input != null) {
			IEditorRegistry registry = PlatformUI.getWorkbench().getEditorRegistry();
			IEditorDescriptor descriptor = registry.getDefaultEditor(input.getName());
			if (descriptor != null)
				return descriptor.getId();
			// TODO return CEditor id hardcode, CUIPlugin.EDITOR_ID
			return (descriptor != null) ? descriptor.getId() : "org.eclipse.cdt.ui.editor.CEditor";
		}
		return null;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ISourcePresentation#getEditorInput(java.lang.Object)
	 */
	public IEditorInput getEditorInput(Object element) {
		if (element instanceof IMarker) {
			IResource resource = ((IMarker) element).getResource();
			if (resource instanceof IFile)
				return new FileEditorInput((IFile) resource);
		}
		if (element instanceof IFile) {
			return new FileEditorInput((IFile) element);
		}
		if (element instanceof IPBreakpoint) {
			IPBreakpoint pbk = (IPBreakpoint) element;
			IFile file = null;
			try {
				String handle = pbk.getSourceHandle();
				IPath path = new Path(handle);
				if (path.isValidPath(handle)) {
					IFile[] files = ResourcesPlugin.getWorkspace().getRoot().findFilesForLocation(path);
					if (files.length > 0)
						file = files[0];
					/*
					 * FIXME else { File fsFile = new File(handle); if (fsFile.isFile() && fsFile.exists()) { return new ExternalEditorInput(new LocalFileStorage(fsFile)); } }
					 */
				}
			} catch (CoreException e) {
			}
			if (file == null)
				file = (IFile) pbk.getMarker().getResource().getAdapter(IFile.class);
			if (file != null)
				return new FileEditorInput(file);
		}
		/*
		 * FIXME if (element instanceof FileStorage || element instanceof LocalFileStorage) { return new ExternalEditorInput((IStorage)element); }
		 */
		return null;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IDebugModelPresentation#computeDetail(org.eclipse.debug.core.model.IValue, org.eclipse.debug.ui.IValueDetailListener)
	 */
	public void computeDetail(IValue value, IValueDetailListener listener) {
		PValueDetailProvider.getDefault().computeDetail(value, listener);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IDebugModelPresentation#setAttribute(java.lang.String, java.lang.Object)
	 */
	public void setAttribute(String attribute, Object value) {
		if (value == null)
			return;
		getAttributes().put(attribute, value);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ILabelProvider#getImage(java.lang.Object)
	 */
	public Image getImage(Object element) {
		Image baseImage = getBaseImage(element);
		if (baseImage != null) {
			ImageDescriptor[] overlays = new ImageDescriptor[] { null, null, null, null };
			/*
			 * if (element instanceof IPDebugElementStatus && !((IPDebugElementStatus)element).isOK()) { switch(((IPDebugElementStatus)element).getSeverity()) { case IPDebugElementStatus.WARNING:
			 * overlays[OverlayImageDescriptor.BOTTOM_LEFT] = CDebugImages.DESC_OVRS_WARNING; break; case IPDebugElementStatus.ERROR: overlays[OverlayImageDescriptor.BOTTOM_LEFT] =
			 * CDebugImages.DESC_OVRS_ERROR; break; } } if (element instanceof IWatchExpression && ((IWatchExpression)element).hasErrors()) overlays[OverlayImageDescriptor.BOTTOM_LEFT] =
			 * PDebugImages.DESC_OVRS_ERROR; if (element instanceof IPVariable && ((IPVariable)element).isArgument()) overlays[OverlayImageDescriptor.TOP_RIGHT] = PDebugImages.DESC_OVRS_ARGUMENT; if
			 * (element instanceof IPGlobalVariable && !(element instanceof IRegister)) overlays[OverlayImageDescriptor.TOP_RIGHT] = PDebugImages.DESC_OVRS_GLOBAL;
			 */
			return getImageCache().getImageFor(new OverlayImageDescriptor(baseImage, overlays));
		}
		return null;
	}
	/** Get base image
	 * @param element
	 * @return
	 */
	private Image getBaseImage(Object element) {
		// TODO element can be DebugTarget, Thread
		if (element instanceof IMarker) {
			IBreakpoint bp = getBreakpoint((IMarker) element);
			if (bp != null && bp instanceof IPBreakpoint) {
				return getBreakpointImage((IPBreakpoint) bp);
			}
		}
		if (element instanceof IPBreakpoint) {
			return getBreakpointImage((IPBreakpoint) element);
		}
		/*
		 * TODO
		if (element instanceof IPSignal) {
			return getSignalImage((IPSignal)element);
		}
		*/
		return super.getImage(element);
	}
	/*
	 * TODO
	protected Image getSignalImage(IPSignal signal) {
		return PTPDebugUIPlugin.getImageDescriptorRegistry().get(PTPDebugImages.DESC_OBJS_SIGNAL);
	}
	*/	
	/** Get breakpoint image
	 * @param breakpoint
	 * @return
	 */
	protected Image getBreakpointImage(IPBreakpoint breakpoint) {
		try {
			if (breakpoint instanceof IPLineBreakpoint)
				return getLineBreakpointImage((IPLineBreakpoint) breakpoint);
			// TODO implement WatchBreakpoint
		} catch (CoreException e) {
			PTPDebugUIPlugin.log(e);
		}
		return null;
	}
	/** Get line breakpoint image
	 * @param breakpoint
	 * @return
	 * @throws CoreException
	 */
	protected Image getLineBreakpointImage(IPLineBreakpoint breakpoint) throws CoreException {
		String job_id = breakpoint.getJobId();
		String cur_job_id = getUIDebugManager().getCurrentJobId();
		// Display nothing if the breakpoint is not in current job
		if (!job_id.equals(IPBreakpoint.GLOBAL) && !job_id.equals(cur_job_id))
			return new Image(null, 1, 1);
		String descriptor = null;
		IElementHandler setManager = getUIDebugManager().getElementHandler(job_id);
		if (setManager == null) // no job running
			descriptor = breakpoint.isEnabled() ? PDebugImage.IMG_DEBUG_BPTCURSET_EN : PDebugImage.IMG_DEBUG_BPTCURSET_DI;
		else { // created job
			String cur_set_id = getUIDebugManager().getCurrentSetId();
			String bpt_set_id = breakpoint.getSetId();
			if (bpt_set_id.equals(cur_set_id)) {
				descriptor = breakpoint.isEnabled() ? PDebugImage.IMG_DEBUG_BPTCURSET_EN : PDebugImage.IMG_DEBUG_BPTCURSET_DI;
			} else {
				if (setManager.getSet(bpt_set_id).isContainSets(cur_set_id))
					descriptor = breakpoint.isEnabled() ? PDebugImage.IMG_DEBUG_BPTMULTISET_EN : PDebugImage.IMG_DEBUG_BPTMULTISET_DI;
				else
					descriptor = breakpoint.isEnabled() ? PDebugImage.IMG_DEBUG_BPTNOSET_EN : PDebugImage.IMG_DEBUG_BPTNOSET_DI;
			}
		}
		return getImageCache().getImageFor(new OverlayImageDescriptor(PDebugImage.getImage(descriptor), computeBreakpointOverlays(breakpoint)));
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
	 */
	public String getText(Object element) {
		String bt = getBaseText(element);
		if (bt == null)
			return null;
		StringBuffer baseText = new StringBuffer(bt);
		if (element instanceof IPDebugElementStatus && !((IPDebugElementStatus) element).isOK()) {
			baseText.append(getFormattedString(" <{0}>", ((IPDebugElementStatus) element).getMessage()));
		}
		if (element instanceof IAdaptable) {
			IEnableDisableTarget target = (IEnableDisableTarget) ((IAdaptable) element).getAdapter(IEnableDisableTarget.class);
			if (target != null) {
				if (!target.isEnabled()) {
					baseText.append(' ');
					baseText.append(PDebugUIMessages.getString("PTPDebugModelPresentation.disabled1"));
				}
			}
		}
		return baseText.toString();
	}

	/** Get watch expression text on Expression View
	 * @param expression
	 * @return
	 */
	protected String getWatchExpressionText(IWatchExpression expression) {
		StringBuffer result = new StringBuffer();
		result.append('"').append(expression.getExpressionText()).append('"');
		if (expression.isPending()) {
			result.append(" = ").append("...");
		}
		else {
			IValue value = expression.getValue();
			if (value instanceof IPValue) {
				IPType type = null;
				try {
					type = ((IPValue)value).getType();
				}
				catch(DebugException e1) {
				}
				if (type != null && isShowVariableTypeNames()) {
					String typeName = getVariableTypeName(type);
					if (!isEmpty(typeName)) {
						result.insert(0, typeName + ' ');
					}
				}
				if (expression.isEnabled()) {
					String valueString = getValueText(value);
					if (valueString.length() > 0) {
						result.append(" = ").append(valueString);
					}
				}
			}
		}
		if (!expression.isEnabled()) {
			result.append(' ');
			result.append(PDebugUIMessages.getString("PTPDebugModelPresentation.disabled1"));
		}
		return result.toString();
	}
	
	/** Get base text
	 * @param element
	 * @return
	 */
	private String getBaseText(Object element) {
		boolean showQualified = isShowQualifiedNames();
		StringBuffer label = new StringBuffer();
		try {
			/*
			 * if (element instanceof ICModule) { label.append(getModuleText((ICModule)element, showQualified)); 
			 * return label.toString(); }
			 * if (element instanceof IRegisterGroup) { label.append(((IRegisterGroup)element).getName()); return label.toString(); } 
			 */
			if (element instanceof IPSignal) {
				label.append(getSignalText((IPSignal)element)); 
				return label.toString(); 
			} 
			if ( element instanceof IWatchExpression ) {
				return getWatchExpressionText((IWatchExpression)element);
			}
			if (element instanceof IVariable) {
				label.append(getVariableText((IVariable) element));
				return label.toString();
			}
			if (element instanceof IValue) {
				label.append(getValueText((IValue) element));
				return label.toString();
			}
			if (element instanceof IStackFrame) {
				label.append(getStackFrameText((IStackFrame) element, showQualified));
				return label.toString();
			}
			if (element instanceof IMarker) {
				IBreakpoint breakpoint = getBreakpoint((IMarker) element);
				if (breakpoint != null) {
					return getBreakpointText(breakpoint, showQualified);
				}
				return null;
			}
			if (element instanceof IBreakpoint) {
				return getBreakpointText((IBreakpoint) element, showQualified);
			}
			if (element instanceof IDebugTarget)
				label.append(getTargetText((IDebugTarget) element, showQualified));
			else if (element instanceof IThread)
				label.append(getThreadText((IThread) element, showQualified));
			if (label.length() > 0) {
				return label.toString();
			}
			if (element instanceof ITerminate) {
				if (((ITerminate) element).isTerminated()) {
					label.insert(0, PDebugUIMessages.getString("PTPDebugModelPresentation.terminated1"));
					return label.toString();
				}
			}
			if (element instanceof IDisconnect) {
				if (((IDisconnect) element).isDisconnected()) {
					label.insert(0, PDebugUIMessages.getString("PTPDebugModelPresentation.disconnected1"));
					return label.toString();
				}
			}
			if (label.length() > 0) {
				return label.toString();
			}
		} catch (DebugException e) {
			PTPDebugUIPlugin.log(e);
		} catch (CoreException e) {
			PTPDebugUIPlugin.log(e);
		}
		return null;
	}
	/** Is show qualified names in the Breakpoint view for each breakpoints
	 * @return
	 */
	protected boolean isShowQualifiedNames() {
		Boolean showQualified = (Boolean) getAttributes().get(DISPLAY_FULL_PATHS);
		showQualified = (showQualified == null) ? Boolean.FALSE : showQualified;
		return showQualified.booleanValue();
	}
	/** Is show variable type names in the breakpoint view for each breakpoints
	 * @return
	 */
	protected boolean isShowVariableTypeNames() {
		Boolean show = (Boolean) getAttributes().get(DISPLAY_VARIABLE_TYPE_NAMES);
		show = show == null ? Boolean.FALSE : show;
		return show.booleanValue();
	}
	/** Get attributes
	 * @return
	 */
	private Map getAttributes() {
		return attributes;
	}
	protected String getSignalText(IPSignal signal) {
		StringBuffer sb = new StringBuffer(PDebugUIMessages.getString("PTPDebugModelPresentation.signal"));
		try {
			String name = signal.getName();
			sb.append( " \'" ).append( name ).append( '\'' );
		}
		catch( DebugException e ) {
		}
		return sb.toString();
	}	
	/** Get variable type name
	 * @param type
	 * @return
	 */
	private String getVariableTypeName(IPType type) {
		StringBuffer result = new StringBuffer();
		String typeName = type.getName();
		if (typeName != null)
			typeName = typeName.trim();
		/*
		if (type.isArray() && typeName != null) {
			int index = typeName.indexOf('[');
			if (index != -1)
				typeName = typeName.substring(0, index).trim();
		}
		*/
		if (typeName != null && typeName.length() > 0) {
			result.append(typeName);
			if (type.isArray()) {
				int[] dims = type.getArrayDimensions();
				for (int i = 0; i < dims.length; ++i) {
					result.append('[');
					result.append(dims[i]);
					result.append(']');
				}
			}
		}
		return result.toString();
	}
	/** Get variable text
	 * @param var
	 * @return
	 * @throws DebugException
	 */
	protected String getVariableText(IVariable var) throws DebugException {
		StringBuffer label = new StringBuffer();
		if (var instanceof IPVariable) {
			IPType type = null;
			try {
				type = ((IPVariable) var).getType();
			} catch (DebugException e) {
				// don't display type
			}
			if (type != null && isShowVariableTypeNames()) {
				String typeName = getVariableTypeName(type);
				if (typeName != null && typeName.length() > 0) {
					label.append(typeName).append(' ');
				}
			}
			String name = var.getName();
			if (name != null)
				label.append(name.trim());
			String valueString = getValueText(var.getValue());
			if (!isEmpty(valueString)) {
				label.append(" = ");
				label.append(valueString);
			}
		}
		return label.toString();
	}
	/** Get value text
	 * @param value
	 * @return
	 */
	protected String getValueText(IValue value) {
		StringBuffer label = new StringBuffer();
		if (value instanceof IPDebugElementStatus && !((IPDebugElementStatus) value).isOK()) {
			label.append(getFormattedString(PDebugUIMessages.getString("PTPDebugModelPresentation.error1"), ((IPDebugElementStatus) value).getMessage()));
		} else if (value instanceof IPValue) {
			IPType type = null;
			try {
				type = ((IPValue) value).getType();
			} catch (DebugException e) {
			}
			try {
				String valueString = value.getValueString();
				if (valueString != null) {
					valueString = valueString.trim();
					if (type != null && type.isCharacter()) {
						if (valueString.length() == 0)
							valueString = ".";
						label.append(valueString);
					} else if (type == null || (!type.isArray() && !type.isStructure())) {
						if (valueString.length() > 0) {
							label.append(valueString);
						}
					}
				}
			} catch (DebugException e1) {
			}
		}
		return label.toString();
	}
	/** Get image cache
	 * @return
	 */
	private OverlayImageCache getImageCache() {
		return imageCache;
	}
	/** Is string empty
	 * @param string
	 * @return
	 */
	private boolean isEmpty(String string) {
		return (string == null || string.trim().length() == 0);
	}
	/** Get breakpoint from marker
	 * @param marker
	 * @return
	 */
	protected IBreakpoint getBreakpoint(IMarker marker) {
		return DebugPlugin.getDefault().getBreakpointManager().getBreakpoint(marker);
	}
	/** Get content of breakpoint
	 * @param breakpoint
	 * @param qualified
	 * @return
	 * @throws CoreException
	 */
	protected String getBreakpointText(IBreakpoint breakpoint, boolean qualified) throws CoreException {
		if (breakpoint instanceof IPLineBreakpoint) {
			return getLineBreakpointText((IPLineBreakpoint) breakpoint, qualified);
		}
		return "";
	}
	/** Get line of breakpoint
	 * @param breakpoint
	 * @param qualified
	 * @return
	 * @throws CoreException
	 */
	protected String getLineBreakpointText(IPLineBreakpoint breakpoint, boolean qualified) throws CoreException {
		StringBuffer label = new StringBuffer();
		appendSourceName(breakpoint, label, qualified);
		appendLineNumber(breakpoint, label);
		appendBreakpointStatus(breakpoint, label);
		return label.toString();
	}
	/** Append source name into breakpoint
	 * @param breakpoint
	 * @param label
	 * @param qualified
	 * @return
	 * @throws CoreException
	 */
	protected StringBuffer appendSourceName(IPBreakpoint breakpoint, StringBuffer label, boolean qualified) throws CoreException {
		String handle = breakpoint.getSourceHandle();
		if (!isEmpty(handle)) {
			IPath path = new Path(handle);
			if (path.isValidPath(handle)) {
				label.append(qualified ? path.toOSString() : path.lastSegment());
			}
		}
		return label;
	}
	/** Append line number into breakpoint
	 * @param breakpoint
	 * @param label
	 * @return
	 * @throws CoreException
	 */
	protected StringBuffer appendLineNumber(IPLineBreakpoint breakpoint, StringBuffer label) throws CoreException {
		int lineNumber = breakpoint.getLineNumber();
		if (lineNumber > 0) {
			label.append(" ");
			label.append(MessageFormat.format(PDebugUIMessages.getString("PTPDebugModelPresentation.line1"), new String[] { Integer.toString(lineNumber) }));
		}
		return label;
	}
	/** Append status into breakpoint
	 * @param breakpoint
	 * @param label
	 * @return
	 * @throws CoreException
	 */
	protected StringBuffer appendBreakpointStatus(IPBreakpoint breakpoint, StringBuffer label) throws CoreException {
		label.append(" ");
		label.append("{");
		label.append(breakpoint.getJobName());
		label.append(":");
		label.append(breakpoint.getSetId());
		label.append("}");
		// label.append(MessageFormat.format(PDebugUIMessages.getString("PTPDebugModelPresentation.details1"), new String[] { jobName, breakpoint.getSetId() }));
		return label;
	}
	/** Get image descriptor and compute breakpoint overlays
	 * @param breakpoint
	 * @return
	 */
	private ImageDescriptor[] computeBreakpointOverlays(IPBreakpoint breakpoint) {
		ImageDescriptor[] overlays = new ImageDescriptor[] { null, null, null, null };
		try {
			if (breakpoint.isGlobal()) {
				overlays[OverlayImageDescriptor.TOP_LEFT] = (breakpoint.isEnabled()) ? PDebugImage.ID_IMG_DEBUG_OVER_BPT_GLOB_EN : PDebugImage.ID_IMG_DEBUG_OVER_BPT_GLOB_DI;
			}
			if (breakpoint.isConditional()) {
				overlays[OverlayImageDescriptor.BOTTOM_LEFT] = (breakpoint.isEnabled()) ? PDebugImage.ID_IMG_DEBUG_OVER_BPT_COND_EN : PDebugImage.ID_IMG_DEBUG_OVER_BPT_COND_DI;
			}
			if (breakpoint.isInstalled()) {
				overlays[OverlayImageDescriptor.BOTTOM_LEFT] = (breakpoint.isEnabled()) ? PDebugImage.ID_IMG_DEBUG_OVER_BPT_INST_EN : PDebugImage.ID_IMG_DEBUG_OVER_BPT_INST_DI;
			}
			if (breakpoint instanceof IPAddressBreakpoint) {
				overlays[OverlayImageDescriptor.TOP_RIGHT] = (breakpoint.isEnabled()) ? PDebugImage.ID_IMG_DEBUG_OVER_BPT_ADDR_EN : PDebugImage.ID_IMG_DEBUG_OVER_BPT_ADDR_DI;
			}
			if (breakpoint instanceof IPFunctionBreakpoint) {
				overlays[OverlayImageDescriptor.BOTTOM_RIGHT] = (breakpoint.isEnabled()) ? PDebugImage.ID_IMG_DEBUG_OVER_BPT_FUNC_EN : PDebugImage.ID_IMG_DEBUG_OVER_BPT_FUNC_DI;
			}
		} catch (CoreException e) {
			PTPDebugUIPlugin.log(e);
		}
		return overlays;
	}
	/** Get debug target text
	 * @param target
	 * @param qualified
	 * @return
	 * @throws DebugException
	 */
	protected String getTargetText(IDebugTarget target, boolean qualified) throws DebugException {
		IPDebugTarget t = (IPDebugTarget) target.getAdapter(IPDebugTarget.class);
		if (t != null) {
			if (!t.isPostMortem()) {
				// FIXME used PDebugElementState
				PDebugElementState state = t.getState();
				if (state.equals(PDebugElementState.EXITED)) {
					Object info = t.getCurrentStateInfo();
					String label = PDebugUIMessages.getString("PTPDebugModelPresentation.target1");
					String reason = "";
					if (info != null && info instanceof IPCDISignalExitInfo) {
						IPCDISignalExitInfo sigInfo = (IPCDISignalExitInfo) info;
						reason = ' ' + MessageFormat.format(PDebugUIMessages.getString("PTPDebugModelPresentation.target2"), new String[] { sigInfo.getName(), sigInfo.getDescription() });
					} else if (info != null && info instanceof IPCDIExitInfo) {
						reason = ' ' + MessageFormat.format(PDebugUIMessages.getString("PTPDebugModelPresentation.target3"), new Integer[] { new Integer(((IPCDIExitInfo) info).getCode()) });
					}
					return MessageFormat.format(label, new String[] { target.getName(), reason });
				} else if (state.equals(PDebugElementState.SUSPENDED)) {
					return MessageFormat.format(PDebugUIMessages.getString("PTPDebugModelPresentation.target4"), new String[] { target.getName() });
				}
			}
		}
		return target.getName();
	}
	/** Get debug thread text
	 * @param thread
	 * @param qualified
	 * @return
	 * @throws DebugException
	 */
	protected String getThreadText(IThread thread, boolean qualified) throws DebugException {
		IPDebugTarget target = (IPDebugTarget) thread.getDebugTarget().getAdapter(IPDebugTarget.class);
		if (target.isPostMortem()) {
			return getFormattedString(PDebugUIMessages.getString("PTPDebugModelPresentation.thread"), thread.getName());
		}
		if (thread.isTerminated()) {
			return getFormattedString(PDebugUIMessages.getString("PTPDebugModelPresentation.thread2"), thread.getName());
		}
		if (thread.isStepping()) {
			return getFormattedString(PDebugUIMessages.getString("PTPDebugModelPresentation.thread3"), thread.getName());
		}
		if (!thread.isSuspended()) {
			return getFormattedString(PDebugUIMessages.getString("PTPDebugModelPresentation.thread4"), thread.getName());
		}
		if (thread.isSuspended()) {
			String reason = "";
			IPDebugElement element = (IPDebugElement) thread.getAdapter(IPDebugElement.class);
			if (element != null) {
				Object info = element.getCurrentStateInfo();
				if (info != null && info instanceof IPCDISignalReceived) {
					IPCDISignal signal = ((IPCDISignalReceived) info).getSignal();
					reason = MessageFormat.format(PDebugUIMessages.getString("PTPDebugModelPresentation.thread5"), new String[] { signal.getName(), signal.getDescription() });
				} else if (info != null && info instanceof IPCDIWatchpointTrigger) {
					reason = MessageFormat.format(PDebugUIMessages.getString("PTPDebugModelPresentation.thread6"), new String[] { ((IPCDIWatchpointTrigger) info).getOldValue(), ((IPCDIWatchpointTrigger) info).getNewValue() });
				} else if (info != null && info instanceof IPCDIWatchpointScope) {
					reason = PDebugUIMessages.getString("PTPDebugModelPresentation.thread7");
				} else if (info != null && info instanceof IPCDIBreakpointHit) {
					reason = PDebugUIMessages.getString("PTPDebugModelPresentation.thread8");
				} else if (info != null && info instanceof IPCDISharedLibraryEvent) {
					reason = PDebugUIMessages.getString("PTPDebugModelPresentation.thread9");
				}
			}
			return MessageFormat.format(PDebugUIMessages.getString("PTPDebugModelPresentation.thread10"), new String[] { thread.getName(), reason });
		}
		return MessageFormat.format(PDebugUIMessages.getString("PTPDebugModelPresentation.thread11"), new String[] { thread.getName() });
	}
	/** Get debug stack frame text
	 * @param f
	 * @param qualified
	 * @return
	 * @throws DebugException
	 */
	protected String getStackFrameText(IStackFrame f, boolean qualified) throws DebugException {
		if (f instanceof IPStackFrame) {
			IPStackFrame frame = (IPStackFrame) f;

			StringBuffer label = new StringBuffer();
			label.append(frame.getLevel());
			label.append(' ');
			String function = frame.getFunction();
			if (isEmpty(function)) {
				label.append(PDebugUIMessages.getString("PTPDebugModelPresentation.frame2"));
			}
			else {
				function = function.trim();
				if (function.length() > 0) {
					label.append(function);
					label.append("() ");
					if (frame.getFile() != null) {
						IPath path = new Path(frame.getFile());
						if (!path.isEmpty()) {
							label.append(PDebugUIMessages.getString("PTPDebugModelPresentation.frame1"));
							label.append(' ');
							label.append((qualified ? path.toOSString() : path.lastSegment()));
							label.append(':');
							if (frame.getFrameLineNumber() != 0)
								label.append(frame.getFrameLineNumber());
						}
					}
				}
			}
			IAddress address = frame.getAddress();
			if (address != null) {
				label.append(' ');
				label.append(address.toHexAddressString());
			}
			return label.toString();
		}
		return (f.getAdapter(IPDummyStackFrame.class) != null)?getDummyStackFrameLabel(f):f.getName();
	}
	private String getDummyStackFrameLabel(IStackFrame stackFrame) {
		return DUMMY_STACKFRAME_LABEL;
	}	
	/** Get formatted text
	 * @param key
	 * @param arg
	 * @return
	 */
	public static String getFormattedString(String key, String arg) {
		return getFormattedString(key, new String[] { arg });
	}
	/** Get formatted text
	 * @param string
	 * @param args
	 * @return
	 */
	public static String getFormattedString(String string, String[] args) {
		return MessageFormat.format(string, args);
	}
	public void dispose() {
		getImageCache().disposeAll();
		attributes.clear();
		super.dispose();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IDebugEditorPresentation#addAnnotations(org.eclipse.ui.IEditorPart, org.eclipse.debug.core.model.IStackFrame)
	 */
	public boolean addAnnotations(IEditorPart editorPart, IStackFrame stackFrame) {
		try {
			//PAnnotationManager.getDefault().focusAnnotation(editorPart, stackFrame);
			PAnnotationManager.getDefault().addAnnotation(editorPart, stackFrame);
			return true;
		} catch (CoreException e) {
			PTPDebugUIPlugin.log(e);
			return false;
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IDebugEditorPresentation#removeAnnotations(org.eclipse.ui.IEditorPart, org.eclipse.debug.core.model.IThread)
	 */
	public void removeAnnotations(IEditorPart editorPart, IThread thread) {
		//PDebugUtils.println("-------------PDebugModePresentation - removeAnnotations");
		/*
		try{
			PAnnotationManager.getDefault().removeAnnotation(editorPart, thread);
		} catch (CoreException e) {
			PTPDebugUIPlugin.log(e);
		}
		*/
	}
}
