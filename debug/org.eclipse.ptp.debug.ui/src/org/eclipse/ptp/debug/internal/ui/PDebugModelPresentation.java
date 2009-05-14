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

import java.math.BigInteger;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IDisconnect;
import org.eclipse.debug.core.model.IExpression;
import org.eclipse.debug.core.model.IRegister;
import org.eclipse.debug.core.model.IRegisterGroup;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.ITerminate;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.core.model.IWatchExpression;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugEditorPresentation;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.IValueDetailListener;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.ptp.debug.core.model.IEnableDisableTarget;
import org.eclipse.ptp.debug.core.model.IPAddressBreakpoint;
import org.eclipse.ptp.debug.core.model.IPBreakpoint;
import org.eclipse.ptp.debug.core.model.IPDebugElement;
import org.eclipse.ptp.debug.core.model.IPDebugElementStatus;
import org.eclipse.ptp.debug.core.model.IPDebugTarget;
import org.eclipse.ptp.debug.core.model.IPDummyStackFrame;
import org.eclipse.ptp.debug.core.model.IPFunctionBreakpoint;
import org.eclipse.ptp.debug.core.model.IPGlobalVariable;
import org.eclipse.ptp.debug.core.model.IPLineBreakpoint;
import org.eclipse.ptp.debug.core.model.IPModule;
import org.eclipse.ptp.debug.core.model.IPSignal;
import org.eclipse.ptp.debug.core.model.IPStackFrame;
import org.eclipse.ptp.debug.core.model.IPThread;
import org.eclipse.ptp.debug.core.model.IPValue;
import org.eclipse.ptp.debug.core.model.IPVariable;
import org.eclipse.ptp.debug.core.model.IPWatchpoint;
import org.eclipse.ptp.debug.core.model.PDebugElementState;
import org.eclipse.ptp.debug.core.pdi.event.IPDIBreakpointInfo;
import org.eclipse.ptp.debug.core.pdi.event.IPDIExitInfo;
import org.eclipse.ptp.debug.core.pdi.event.IPDISharedLibraryInfo;
import org.eclipse.ptp.debug.core.pdi.event.IPDISignalInfo;
import org.eclipse.ptp.debug.core.pdi.event.IPDIWatchpointScopeInfo;
import org.eclipse.ptp.debug.core.pdi.event.IPDIWatchpointTriggerInfo;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIF;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIFType;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIFTypeArray;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIFTypeChar;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIFTypePointer;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIFTypeReference;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIFTypeStruct;
import org.eclipse.ptp.debug.internal.core.sourcelookup.PSourceNotFoundElement;
import org.eclipse.ptp.debug.ui.PTPDebugUIPlugin;
import org.eclipse.ptp.debug.ui.UIDebugManager;
import org.eclipse.ptp.ui.model.IElementHandler;
import org.eclipse.ptp.ui.model.IElementSet;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;

/**
 * @author Clement chu
 * 
 */
public class PDebugModelPresentation extends LabelProvider implements IDebugModelPresentation, IDebugEditorPresentation {
	private static PDebugModelPresentation instance = null;
	public final static String DISPLAY_FULL_PATHS = "DISPLAY_FULL_PATHS";
	private static final String DUMMY_STACKFRAME_LABEL = "...";
	protected Map<String, Object> attributes = new HashMap<String, Object>(3);
	private OverlayImageCache imageCache = new OverlayImageCache();
	protected UIDebugManager uiDebugManager = null;

	/**
	 * Constructor
	 */
	public PDebugModelPresentation() {
		// make sure using the one created by start up
		if (instance == null)
			instance = this;
	}
	public static PDebugModelPresentation getDefault() {
		if (instance == null)
			instance = new PDebugModelPresentation();
		return instance;
	}

	/**
	 * Get UIDebugManager
	 * @return
	 */
	private UIDebugManager getUIDebugManager() {
		if (uiDebugManager == null) {
			uiDebugManager = PTPDebugUIPlugin.getUIDebugManager();
		}
		return uiDebugManager;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.debug.ui.IDebugModelPresentation#setAttribute(java.lang.String, java.lang.Object)
	 */
	public void setAttribute(String attribute, Object value) {
		if (value == null)
			return;
		getAttributes().put(attribute, value);
	}
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.debug.ui.IDebugModelPresentation#computeDetail(org.eclipse.debug.core.model.IValue, org.eclipse.debug.ui.IValueDetailListener)
	 */
	public void computeDetail(IValue value, IValueDetailListener listener) {
		PValueDetailProvider.getDefault().computeDetail(value, listener);
	}
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.debug.ui.ISourcePresentation#getEditorInput(java.lang.Object)
	 */
	public IEditorInput getEditorInput(Object element) {
		return PDebugUIUtils.getEditorInput(element);
	}
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.debug.ui.ISourcePresentation#getEditorId(org.eclipse.ui.IEditorInput, java.lang.Object)
	 */
	public String getEditorId(IEditorInput input, Object element) {
		return PDebugUIUtils.getEditorId(input, element);
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ILabelProvider#getImage(java.lang.Object)
	 */
	public Image getImage(Object element) {
		Image baseImage = getBaseImage(element);
		if (baseImage != null) {
			ImageDescriptor[] overlays = new ImageDescriptor[] { null, null, null, null };
			if (element instanceof IPDebugElementStatus && !((IPDebugElementStatus)element).isOK()) {
				switch(((IPDebugElementStatus)element).getSeverity()) { 
					case IPDebugElementStatus.WARNING: 
						overlays[OverlayImageDescriptor.BOTTOM_LEFT] = PDebugImage.getDescriptor(PDebugImage.IMG_DEBUG_OVRS_WARNING); 
						break;
					case IPDebugElementStatus.ERROR: 
						overlays[OverlayImageDescriptor.BOTTOM_LEFT] = PDebugImage.getDescriptor(PDebugImage.IMG_DEBUG_OVRS_ERROR);
						break;
				}
			}
			if (element instanceof IWatchExpression && ((IWatchExpression)element).hasErrors())
				overlays[OverlayImageDescriptor.BOTTOM_LEFT] = PDebugImage.getDescriptor(PDebugImage.IMG_DEBUG_OVRS_ERROR); 
			if (element instanceof IPVariable && ((IPVariable)element).isArgument()) 
				overlays[OverlayImageDescriptor.TOP_RIGHT] = PDebugImage.getDescriptor(PDebugImage.IMG_DEBUG_OVRS_ARGUMENT); 
			if (element instanceof IPGlobalVariable && !(element instanceof IRegister)) 
				overlays[OverlayImageDescriptor.TOP_RIGHT] = PDebugImage.getDescriptor(PDebugImage.IMG_DEBUG_OVRS_GLOBAL);

			return getImageCache().getImageFor(new OverlayImageDescriptor(baseImage, overlays));
		}
		return null;
	}

	/**
	 * Get base image
	 * @param element
	 * @return
	 */
	private Image getBaseImage(Object element) {
		if (element instanceof IPDebugTarget) {
			IPDebugTarget target = (IPDebugTarget)element;
			if (target.isPostMortem()) {
				return DebugUITools.getImage(IDebugUIConstants.IMG_OBJS_DEBUG_TARGET_TERMINATED);
			}
			if (target.isTerminated() || target.isDisconnected()) {
				return DebugUITools.getImage(IDebugUIConstants.IMG_OBJS_DEBUG_TARGET_TERMINATED);
			}
			return DebugUITools.getImage(IDebugUIConstants.IMG_OBJS_DEBUG_TARGET);
		}
		if (element instanceof IPThread) {
			IPThread thread = (IPThread)element;
			IPDebugTarget target = (IPDebugTarget)thread.getDebugTarget();
			if (target.isPostMortem()) {
				return DebugUITools.getImage(IDebugUIConstants.IMG_OBJS_THREAD_TERMINATED);
			}
			if (thread.isSuspended()) {
				return DebugUITools.getImage(IDebugUIConstants.IMG_OBJS_THREAD_SUSPENDED);
			}
			else if (thread.isTerminated()) {
				return DebugUITools.getImage(IDebugUIConstants.IMG_OBJS_THREAD_TERMINATED);
			}
			else {
				return DebugUITools.getImage(IDebugUIConstants.IMG_OBJS_THREAD_RUNNING);
			}
		}
		if (element instanceof IMarker) {
			IBreakpoint bp = getBreakpoint((IMarker) element);
			if (bp != null && bp instanceof IPBreakpoint) {
				return getBreakpointImage((IPBreakpoint) bp);
			}
		}
		if (element instanceof IPBreakpoint) {
			return getBreakpointImage((IPBreakpoint) element);
		}
		if (element instanceof IRegisterGroup) {
			return getRegisterGroupImage((IRegisterGroup)element);
		}
		if (element instanceof IExpression) {
			return getExpressionImage((IExpression)element);
		}
		if (element instanceof IRegister) {
			return getRegisterImage((IRegister)element);
		}
		if (element instanceof IVariable) {
			return getVariableImage((IVariable)element);
		}
		if (element instanceof IPModule) {
			return getModuleImage((IPModule)element);
		}
		if (element instanceof IPSignal) { 
			return getSignalImage((IPSignal)element);
		}
		return super.getImage(element);
	}
	protected Image getSignalImage(IPSignal signal) {
		return PDebugImage.getImage(PDebugImage.IMG_DEBUG_SIGNAL);
	}
	protected Image getRegisterGroupImage(IRegisterGroup element) {
		IEnableDisableTarget target = (IEnableDisableTarget)element.getAdapter(IEnableDisableTarget.class);
		if (target != null && !target.isEnabled())
			return PDebugImage.getImage(PDebugImage.IMG_DEBUG_REGISTER_GROUP_DISABLED);
		return PDebugImage.getImage(PDebugImage.IMG_DEBUG_REGISTER_GROUP);
	}
	protected Image getBreakpointImage(IPBreakpoint breakpoint) {
		try {
			if (breakpoint instanceof IPLineBreakpoint)
				return getLineBreakpointImage((IPLineBreakpoint) breakpoint);
			if (breakpoint instanceof IPWatchpoint) {
				return getWatchpointImage((IPWatchpoint)breakpoint);
			}
		} catch (CoreException e) {
			PTPDebugUIPlugin.log(e);
		}
		return null;
	}
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
				IElementSet set = (IElementSet)setManager.getElementByID(bpt_set_id);
				if (set == null)
					descriptor = breakpoint.isEnabled() ? PDebugImage.IMG_DEBUG_BPTCURSET_EN : PDebugImage.IMG_DEBUG_BPTCURSET_DI;
				else {
					if (set.containsMatchSet(cur_set_id))
						descriptor = breakpoint.isEnabled() ? PDebugImage.IMG_DEBUG_BPTMULTISET_EN : PDebugImage.IMG_DEBUG_BPTMULTISET_DI;
					else
						descriptor = breakpoint.isEnabled() ? PDebugImage.IMG_DEBUG_BPTNOSET_EN : PDebugImage.IMG_DEBUG_BPTNOSET_DI;
				}
			}
		}
		return getImageCache().getImageFor(new OverlayImageDescriptor(PDebugImage.getImage(descriptor), computeBreakpointOverlays(breakpoint)));
	}
	protected Image getWatchpointImage(IPWatchpoint watchpoint) throws CoreException {
		String descriptor = null;
		if (watchpoint.isEnabled()) {
			if (watchpoint.isReadType() && !watchpoint.isWriteType())
				descriptor = PDebugImage.IMG_DEBUG_READ_WATCHPOINT_ENABLED;
			else if (!watchpoint.isReadType() && watchpoint.isWriteType())
				descriptor = PDebugImage.IMG_DEBUG_WRITE_WATCHPOINT_ENABLED;
			else
				descriptor = PDebugImage.IMG_DEBUG_WATCHPOINT_ENABLED;
		}
		else {
			if (watchpoint.isReadType() && !watchpoint.isWriteType())
				descriptor = PDebugImage.IMG_DEBUG_READ_WATCHPOINT_DISABLED;
			else if (!watchpoint.isReadType() && watchpoint.isWriteType())
				descriptor = PDebugImage.IMG_DEBUG_WRITE_WATCHPOINT_DISABLED;
			else
				descriptor = PDebugImage.IMG_DEBUG_WATCHPOINT_DISABLED;
		}
		return getImageCache().getImageFor(new OverlayImageDescriptor(PDebugImage.getImage(descriptor), computeBreakpointOverlays(watchpoint)));
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
	 */
	public String getText(Object element) {
		String bt = getBaseText(element);
		if (bt == null)
			return null;
		StringBuffer baseText = new StringBuffer(bt);
		if (element instanceof IPDebugElementStatus && !((IPDebugElementStatus) element).isOK()) {
			baseText.append(MessageFormat.format(" <{0}>", new Object[] { ((IPDebugElementStatus)element).getMessage() }));
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
	private String getBaseText(Object element) {
		boolean showQualified = isShowQualifiedNames();
		StringBuffer label = new StringBuffer();
		try {
			if (element instanceof IPModule) { 
				label.append(getModuleText((IPModule)element, showQualified)); 
				return label.toString();
			}
			if (element instanceof IPSignal) {
				label.append(getSignalText((IPSignal) element));
				return label.toString();
			}
			if (element instanceof IRegisterGroup) { 
				label.append(((IRegisterGroup)element).getName());
				return label.toString();
			}
			if (element instanceof IWatchExpression) {
				return getWatchExpressionText((IWatchExpression) element);
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
			if (element instanceof PSourceNotFoundElement) {
				return getBaseText(((PSourceNotFoundElement)element).getElement());
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
			return MessageFormat.format(PDebugUIMessages.getString("PTPDebugModelPresentation.2"), new Object[] { e.getMessage() });
		} catch (CoreException e) {
			PTPDebugUIPlugin.log(e);
		}
		return null;
	}
	protected String getModuleText(IPModule module, boolean qualified) {
		StringBuffer sb = new StringBuffer();
		IPath path = module.getImageName();
		if (!path.isEmpty()) {
			sb.append(path.lastSegment());
		}
		else {
			sb.append(PDebugUIMessages.getString("PTPDebugModelPresentation.unknown_1"));		
		}
		return sb.toString();
	}
	protected String getRegisterGroupText(IRegisterGroup group) {
		String name = PDebugUIMessages.getString("PTPDebugModelPresentation.not_available_1");
		try {
			name = group.getName();
		}
		catch(DebugException e) {
			PTPDebugUIPlugin.log(e.getStatus());
		}
		return name;
	}
	protected boolean isShowQualifiedNames() {
		Boolean showQualified = (Boolean) getAttributes().get(DISPLAY_FULL_PATHS);
		showQualified = (showQualified == null) ? Boolean.FALSE : showQualified;
		return showQualified.booleanValue();
	}
	private Map<String, Object> getAttributes() {
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
	private ImageDescriptor[] computeBreakpointOverlays(IPBreakpoint breakpoint) {
		ImageDescriptor[] overlays = new ImageDescriptor[] { null, null, null, null };
		try {
			if (breakpoint.isGlobal()) {
				overlays[OverlayImageDescriptor.TOP_LEFT] = (breakpoint.isEnabled()) ? PDebugImage.getDescriptor(PDebugImage.IMG_DEBUG_OVER_BPT_GLOB_EN) : PDebugImage.getDescriptor(PDebugImage.IMG_DEBUG_OVER_BPT_GLOB_DI);
			}
			if (breakpoint.isConditional()) {
				overlays[OverlayImageDescriptor.BOTTOM_LEFT] = (breakpoint.isEnabled()) ? PDebugImage.getDescriptor(PDebugImage.IMG_DEBUG_OVER_BPT_COND_EN) : PDebugImage.getDescriptor(PDebugImage.IMG_DEBUG_OVER_BPT_COND_DI);
			}
			if (breakpoint.isInstalled()) {
				overlays[OverlayImageDescriptor.BOTTOM_LEFT] = (breakpoint.isEnabled()) ? PDebugImage.getDescriptor(PDebugImage.IMG_DEBUG_OVER_BPT_INST_EN) : PDebugImage.getDescriptor(PDebugImage.IMG_DEBUG_OVER_BPT_INST_DI);
			}
			if (breakpoint instanceof IPAddressBreakpoint) {
				overlays[OverlayImageDescriptor.TOP_RIGHT] = (breakpoint.isEnabled()) ? PDebugImage.getDescriptor(PDebugImage.IMG_DEBUG_OVER_BPT_ADDR_EN) : PDebugImage.getDescriptor(PDebugImage.IMG_DEBUG_OVER_BPT_ADDR_DI);
			}
			if (breakpoint instanceof IPFunctionBreakpoint) {
				overlays[OverlayImageDescriptor.BOTTOM_RIGHT] = (breakpoint.isEnabled()) ? PDebugImage.getDescriptor(PDebugImage.IMG_DEBUG_OVER_BPT_FUNC_EN) : PDebugImage.getDescriptor(PDebugImage.IMG_DEBUG_OVER_BPT_FUNC_DI);
			}
		} catch (CoreException e) {
			PTPDebugUIPlugin.log(e);
		}
		return overlays;
	}
	protected Image getVariableImage(IVariable element) {
		if (element instanceof IPVariable) {
			IAIF aif = null;
			try {
				aif = ((IPVariable)element).getAIF();
			}
			catch(DebugException e) {
				// use default image
			}
			if (aif == null)
				return PDebugImage.getImage((((IPVariable)element).isEnabled()) ? PDebugImage.IMG_DEBUG_VARIABLE_SIMPLE : PDebugImage.IMG_DEBUG_VARIABLE_SIMPLE_DISABLED);
			IAIFType type = aif.getType();
			if (type instanceof IAIFTypePointer || type instanceof IAIFTypeReference)
				return PDebugImage.getImage((((IPVariable)element).isEnabled()) ? PDebugImage.IMG_DEBUG_VARIABLE_POINTER : PDebugImage.IMG_DEBUG_VARIABLE_POINTER_DISABLED);
			else if (type instanceof IAIFTypeArray || type instanceof IAIFTypeStruct)
				return PDebugImage.getImage((((IPVariable)element).isEnabled()) ? PDebugImage.IMG_DEBUG_VARIABLE_AGGREGATE : PDebugImage.IMG_DEBUG_VARIABLE_AGGREGATE_DISABLED);
			else
				return PDebugImage.getImage((((IPVariable)element).isEnabled()) ? PDebugImage.IMG_DEBUG_VARIABLE_SIMPLE : PDebugImage.IMG_DEBUG_VARIABLE_SIMPLE_DISABLED);
		}
		return null;
	}
	protected Image getRegisterImage(IRegister element) {
		return ((element instanceof IPVariable && ((IPVariable)element).isEnabled())) ? PDebugImage.getImage(PDebugImage.IMG_DEBUG_REGISTER) : PDebugImage.getImage(PDebugImage.IMG_DEBUG_REGISTER_DISABLED);
	}
	protected Image getExpressionImage(IExpression element) {
		return DebugUITools.getImage(IDebugUIConstants.IMG_OBJS_EXPRESSION);
	}
	protected Image getModuleImage(IPModule element) {
		switch(element.getType()) {
			case IPModule.EXECUTABLE:
				if (element.areSymbolsLoaded()) {
					return PDebugImage.getImage(PDebugImage.IMG_DEBUG_EXECUTABLE_WITH_SYMBOLS);
				}
				return PDebugImage.getImage(PDebugImage.IMG_DEBUG_EXECUTABLE);
			case IPModule.SHARED_LIBRARY:
				if ( element.areSymbolsLoaded() ) {
					return PDebugImage.getImage(PDebugImage.IMG_DEBUG_SHARED_LIBRARY_WITH_SYMBOLS);
				}
				return PDebugImage.getImage(PDebugImage.IMG_DEBUG_SHARED_LIBRARY);
		}
		return null;
	}
	protected String getVariableText(IVariable var) throws DebugException {
		StringBuffer label = new StringBuffer();
		if (var instanceof IPVariable) {
			IAIF aif = null;
			try {
				aif = ((IPVariable) var).getAIF();
			} catch (DebugException e) {
				// don't display type
			}
			if (aif != null && isShowVariableTypeNames()) {
				String typeName = getVariableTypeName(aif);
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
	protected String getValueText(IValue value) {
		StringBuffer label = new StringBuffer();
		if (value instanceof IPDebugElementStatus && !((IPDebugElementStatus) value).isOK()) {
			label.append(MessageFormat.format(PDebugUIMessages.getString("PTPDebugModelPresentation.error1"), new Object[] { ((IPDebugElementStatus)value).getMessage() }));
		} else if (value instanceof IPValue) {
			IAIF aif = null;
			try {
				aif = ((IPValue) value).getAIF();
			} catch (DebugException e) {
				// don't display type
			}
			try {
				String valueString = value.getValueString();
				if (valueString != null) {
					valueString = valueString.trim();
					if (aif != null && aif instanceof IAIFTypeChar) {
						if (valueString.length() == 0)
							valueString = ".";
						label.append(valueString);
					} else if (aif == null || (!(aif.getType() instanceof IAIFTypeArray) && !(aif.getType() instanceof IAIFTypeStruct))) {
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
	protected String getSignalText(IPSignal signal) {
		StringBuffer sb = new StringBuffer(PDebugUIMessages.getString("PTPDebugModelPresentation.signal"));
		try {
			String name = signal.getName();
			sb.append(" \'").append(name).append('\'');
		} catch (DebugException e) {
		}
		return sb.toString();
	}
	protected String getWatchExpressionText(IWatchExpression expression) {
		StringBuffer result = new StringBuffer();
		result.append('"').append(expression.getExpressionText()).append('"');
		if (expression.isPending()) {
			result.append(" = ").append("...");
		} else {
			IValue value = expression.getValue();
			if (value instanceof IPValue) {
				IAIF aif = null;
				try {
					aif = ((IPValue)value).getAIF();
				} catch (DebugException e1) {
				}
				if (aif != null && isShowVariableTypeNames()) {
					String typeName = getVariableTypeName(aif);
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
	protected String getTargetText(IDebugTarget target, boolean qualified) throws DebugException {
		IPDebugTarget t = (IPDebugTarget) target.getAdapter(IPDebugTarget.class);
		if (t != null) {
			if (!t.isPostMortem()) {
				PDebugElementState state = t.getState();
				if (state.equals(PDebugElementState.EXITED)) {
					Object info = t.getCurrentStateInfo();
					String label = PDebugUIMessages.getString("PTPDebugModelPresentation.target1");
					String reason = "";
					if (info != null && info instanceof IPDISignalInfo) {
						IPDISignalInfo sigInfo = (IPDISignalInfo) info;
						reason = ' ' + MessageFormat.format(PDebugUIMessages.getString("PTPDebugModelPresentation.target2"), new Object[] { sigInfo.getName(), sigInfo.getDescription() });
					} else if (info != null && info instanceof IPDIExitInfo) {
						reason = ' ' + MessageFormat.format(PDebugUIMessages.getString("PTPDebugModelPresentation.target3"), new Object[] { new Integer(((IPDIExitInfo) info).getCode()) });
					}
					return MessageFormat.format(label, new Object[] { target.getName(), reason });
				} else if (state.equals(PDebugElementState.SUSPENDED)) {
					return MessageFormat.format(PDebugUIMessages.getString("PTPDebugModelPresentation.target4"), new Object[] { target.getName() });
				}
			}
		}
		return target.getName();
	}
	protected String getThreadText(IThread thread, boolean qualified) throws DebugException {
		IPDebugTarget target = (IPDebugTarget) thread.getDebugTarget().getAdapter(IPDebugTarget.class);
		if (target.isPostMortem()) {
			return MessageFormat.format(PDebugUIMessages.getString("PTPDebugModelPresentation.thread"), new Object[] { thread.getName() });
		}
		if (thread.isTerminated()) {
			return MessageFormat.format(PDebugUIMessages.getString("PTPDebugModelPresentation.thread2"), new Object[] { thread.getName() });
		}
		if (thread.isStepping()) {
			return MessageFormat.format(PDebugUIMessages.getString("PTPDebugModelPresentation.thread3"), new Object[] { thread.getName() });
		}
		if (!thread.isSuspended()) {
			return MessageFormat.format(PDebugUIMessages.getString("PTPDebugModelPresentation.thread4"), new Object[] { thread.getName() });
		}
		if (thread.isSuspended()) {
			String reason = "";
			IPDebugElement element = (IPDebugElement) thread.getAdapter(IPDebugElement.class);
			if (element != null) {
				Object info = element.getCurrentStateInfo();
				if (info != null && info instanceof IPDISignalInfo) {
					reason = MessageFormat.format(PDebugUIMessages.getString("PTPDebugModelPresentation.thread5"), new Object[] { ((IPDISignalInfo) info).getName(), ((IPDISignalInfo) info).getDescription() });
				} else if (info != null && info instanceof IPDIWatchpointTriggerInfo) {
					reason = MessageFormat.format(PDebugUIMessages.getString("PTPDebugModelPresentation.thread6"), new Object[] { ((IPDIWatchpointTriggerInfo) info).getOldValue(), ((IPDIWatchpointTriggerInfo) info).getNewValue() });
				} else if (info != null && info instanceof IPDIWatchpointScopeInfo) {
					reason = PDebugUIMessages.getString("PTPDebugModelPresentation.thread7");
				} else if (info != null && info instanceof IPDIBreakpointInfo) {
					reason = PDebugUIMessages.getString("PTPDebugModelPresentation.thread8");
				} else if (info != null && info instanceof IPDISharedLibraryInfo) {
					reason = PDebugUIMessages.getString("PTPDebugModelPresentation.thread9");
				}
			}
			return MessageFormat.format(PDebugUIMessages.getString("PTPDebugModelPresentation.thread10"), new Object[] { thread.getName(), reason });
		}
		return MessageFormat.format(PDebugUIMessages.getString("PTPDebugModelPresentation.thread11"), new Object[] { thread.getName() });
	}
	protected String getStackFrameText(IStackFrame f, boolean qualified) throws DebugException {
		if (f instanceof IPStackFrame) {
			IPStackFrame frame = (IPStackFrame) f;
			StringBuffer label = new StringBuffer();
			label.append(frame.getLevel());
			label.append(' ');
			String function = frame.getFunction();
			if (isEmpty(function)) {
				label.append(PDebugUIMessages.getString("PTPDebugModelPresentation.frame2"));
			} else {
				function = function.trim();
				if (function.length() > 0) {
					label.append(function);
					label.append("() ");
					if (frame.getFile() != null) {
						IPath path = new Path(frame.getFile());
						if (!path.isEmpty()) {
							//label.append(PDebugUIMessages.getString("PTPDebugModelPresentation.frame1"));
							//label.append(' ');
							label.append((qualified ? path.toOSString() : path.lastSegment()));
							label.append(':');
							if (frame.getFrameLineNumber() != 0)
								label.append(frame.getFrameLineNumber());
						}
					}
				}
			}
			BigInteger address = frame.getAddress();
			if (address != null) {
				label.append(' ');
				label.append(address.toString(16));
			}
			return label.toString();
		}
		return (f.getAdapter(IPDummyStackFrame.class) != null) ? getDummyStackFrameLabel(f) : f.getName();
	}	
	private String getDummyStackFrameLabel(IStackFrame stackFrame) {
		return DUMMY_STACKFRAME_LABEL;
	}
	protected boolean isShowVariableTypeNames() {
		Boolean show = (Boolean) getAttributes().get(DISPLAY_VARIABLE_TYPE_NAMES);
		show = show == null ? Boolean.FALSE : show;
		return show.booleanValue();
	}
	public void dispose() {
		getImageCache().disposeAll();
		attributes.clear();
		super.dispose();
	}

	
	protected String getBreakpointText(IBreakpoint breakpoint, boolean qualified) throws CoreException {
		if (breakpoint instanceof IPLineBreakpoint) {
			return getLineBreakpointText((IPLineBreakpoint) breakpoint, qualified);
		}
		return "";
	}
	protected String getLineBreakpointText(IPLineBreakpoint breakpoint, boolean qualified) throws CoreException {
		StringBuffer label = new StringBuffer();
		appendSourceName(breakpoint, label, qualified);
		appendLineNumber(breakpoint, label);
		appendBreakpointStatus(breakpoint, label);
		return label.toString();
	}
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
	protected StringBuffer appendLineNumber(IPLineBreakpoint breakpoint, StringBuffer label) throws CoreException {
		int lineNumber = breakpoint.getLineNumber();
		if (lineNumber > 0) {
			label.append(" ");
			label.append(MessageFormat.format(PDebugUIMessages.getString("PTPDebugModelPresentation.line1"), new Object[] { Integer.toString(lineNumber) }));
		}
		return label;
	}
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
	private String getVariableTypeName(IAIF aif) {
		StringBuffer result = new StringBuffer();
		if (aif != null) {
			IAIFType type = aif.getType();
			if (type != null) {
				result.append(type.toString().trim());
				if (type instanceof IAIFTypeArray) {
					int[] dims = ((IAIFTypeArray)type).getDimensionDetails();
					for (int i = 0; i < dims.length; ++i) {
						result.append('[');
						result.append(dims[i]);
						result.append(']');
					}
				}
			}
		}
		return result.toString();
	}
	public boolean addAnnotations(IEditorPart editorPart, IStackFrame frame) {
		try {
			if (frame instanceof IPStackFrame) {
				PAnnotationManager.getDefault().addAnnotation(editorPart, (IPStackFrame)frame);
				return true;
			}
		}
		catch (CoreException e) {
			return false;
		}
		return false;
	}
	public void removeAnnotations(IEditorPart editorPart, IThread thread) {
		
	}
}
