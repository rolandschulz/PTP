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
package org.eclipse.ptp.cell.debug.be.cdi.model.spu;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.core.model.ICDebugTarget;
import org.eclipse.cdt.debug.mi.core.MIException;
import org.eclipse.cdt.debug.mi.core.MISession;
import org.eclipse.cdt.debug.mi.core.cdi.model.Target;
import org.eclipse.cdt.debug.mi.core.command.CommandFactory;
import org.eclipse.cdt.debug.mi.core.output.MIInfo;
import org.eclipse.ptp.cell.debug.be.BEMessages;
import org.eclipse.ptp.cell.debug.be.cdi.command.CellMISPUDMAInfo;
import org.eclipse.ptp.cell.debug.be.cdi.command.CellMISPUDMAInfoInfo;
import org.eclipse.ptp.cell.debug.be.cdi.command.CellMISPUEventInfo;
import org.eclipse.ptp.cell.debug.be.cdi.command.CellMISPUEventInfoInfo;
import org.eclipse.ptp.cell.debug.be.cdi.command.CellMISPUMailboxInfo;
import org.eclipse.ptp.cell.debug.be.cdi.command.CellMISPUMailboxInfoInfo;
import org.eclipse.ptp.cell.debug.be.cdi.command.CellMISPUProxyDMAInfo;
import org.eclipse.ptp.cell.debug.be.cdi.command.CellMISPUProxyDMAInfoInfo;
import org.eclipse.ptp.cell.debug.be.cdi.command.CellMISPUSignalInfo;
import org.eclipse.ptp.cell.debug.be.cdi.command.CellMISPUSignalInfoInfo;
import org.eclipse.ptp.cell.debug.be.cdi.command.factories.StandardCellCommandFactory;
import org.eclipse.ptp.cell.debug.be.cdi.command.output.MISPUDMAElement;
import org.eclipse.ptp.cell.debug.be.cdi.command.output.MISPUDMAListTuple;
import org.eclipse.ptp.cell.debug.be.cdi.command.output.MISPUEvent;
import org.eclipse.ptp.cell.debug.be.cdi.command.output.MISPUMailbox;
import org.eclipse.ptp.cell.debug.be.cdi.command.output.MISPUProxyDMAElement;
import org.eclipse.ptp.cell.debug.be.cdi.command.output.MISPUSignal;


/**
 * @author Ricardo M. Matinata
 * @since 1.3
 *
 */
public class SPUEnhancementsProcessor {
	
	private int count = 0;
	
	public MISPUEvent[] processSPUEvent(ICDebugTarget cTarget){
		
		Target target = processCTarget(cTarget);
		if (target != null) {
			StandardCellCommandFactory cellFactory = processCommandFactory(target.getMISession().getCommandFactory());
			if (cellFactory != null) {
				CellMISPUEventInfo evinfo = cellFactory.createMISPUEventInfo();
				MISession miSession = target.getMISession(); 
				try {
					miSession.postCommand( evinfo );
					MIInfo info = evinfo.getMIInfo();
					if ( info != null ) {
						if ( info instanceof CellMISPUEventInfoInfo) {
							return ((CellMISPUEventInfoInfo)info).getEvents();
						}
					}
				} catch (MIException e) {
					List events = new ArrayList();
					events.add(new MISPUEvent(MISPUEvent.EVENT_MASK,BEMessages.getString("SPUEnhancementsProcessor.0"))); //$NON-NLS-1$
					events.add(new MISPUEvent(MISPUEvent.EVENT_STATUS,BEMessages.getString("SPUEnhancementsProcessor.1"))); //$NON-NLS-1$
					return (MISPUEvent[]) events.toArray(new MISPUEvent[] {});
				}
			}
			
		}
		List events = new ArrayList();
		events.add(new MISPUEvent(MISPUEvent.EVENT_MASK,BEMessages.getString("SPUEnhancementsProcessor.2"))); //$NON-NLS-1$
		events.add(new MISPUEvent(MISPUEvent.EVENT_STATUS,BEMessages.getString("SPUEnhancementsProcessor.3"))); //$NON-NLS-1$
		return (MISPUEvent[]) events.toArray(new MISPUEvent[] {});
	}
	
	public MISPUSignal[] processSPUSignal(ICDebugTarget cTarget){
		Target target = processCTarget(cTarget);
		if (target != null) {
			StandardCellCommandFactory cellFactory = processCommandFactory(target.getMISession().getCommandFactory());
			if (cellFactory != null) {
				CellMISPUSignalInfo evinfo = cellFactory.createMISPUSignalInfo();
				MISession miSession = target.getMISession(); 
				try {
					miSession.postCommand( evinfo );
					MIInfo info = evinfo.getMIInfo();
					if ( info != null ) {
						if ( info instanceof CellMISPUSignalInfoInfo) {
							return ((CellMISPUSignalInfoInfo)info).getSignals();
						}
					}
				} catch (MIException e) {
					List signals = new ArrayList();
					signals.add(new MISPUSignal(MISPUSignal.SIGNAL1_PENDING,BEMessages.getString("SPUEnhancementsProcessor.0"))); //$NON-NLS-1$
					signals.add(new MISPUSignal(MISPUSignal.SIGNAL1,BEMessages.getString("SPUEnhancementsProcessor.0"))); //$NON-NLS-1$
					signals.add(new MISPUSignal(MISPUSignal.SIGNAL1_TYPE,BEMessages.getString("SPUEnhancementsProcessor.0"))); //$NON-NLS-1$
					signals.add(new MISPUSignal(MISPUSignal.SIGNAL2_PENDING,BEMessages.getString("SPUEnhancementsProcessor.0"))); //$NON-NLS-1$
					signals.add(new MISPUSignal(MISPUSignal.SIGNAL2,BEMessages.getString("SPUEnhancementsProcessor.0"))); //$NON-NLS-1$
					signals.add(new MISPUSignal(MISPUSignal.SIGNAL2_TYPE,BEMessages.getString("SPUEnhancementsProcessor.0"))); //$NON-NLS-1$
					return (MISPUSignal[]) signals.toArray(new MISPUSignal[] {});
				}
			}
			
		}
		List signals = new ArrayList();
		signals.add(new MISPUSignal(MISPUSignal.SIGNAL1_PENDING,BEMessages.getString("SPUEnhancementsProcessor.2"))); //$NON-NLS-1$
		signals.add(new MISPUSignal(MISPUSignal.SIGNAL1,BEMessages.getString("SPUEnhancementsProcessor.2"))); //$NON-NLS-1$
		signals.add(new MISPUSignal(MISPUSignal.SIGNAL1_TYPE,BEMessages.getString("SPUEnhancementsProcessor.2"))); //$NON-NLS-1$
		signals.add(new MISPUSignal(MISPUSignal.SIGNAL2_PENDING,BEMessages.getString("SPUEnhancementsProcessor.2"))); //$NON-NLS-1$
		signals.add(new MISPUSignal(MISPUSignal.SIGNAL2,BEMessages.getString("SPUEnhancementsProcessor.2"))); //$NON-NLS-1$
		signals.add(new MISPUSignal(MISPUSignal.SIGNAL2_TYPE,BEMessages.getString("SPUEnhancementsProcessor.2"))); //$NON-NLS-1$
		return (MISPUSignal[]) signals.toArray(new MISPUSignal[] {});
	}
	
	public MISPUMailbox[] processSPUMailbox(ICDebugTarget cTarget){
		Target target = processCTarget(cTarget);
		if (target != null) {
			StandardCellCommandFactory cellFactory = processCommandFactory(target.getMISession().getCommandFactory());
			if (cellFactory != null) {
				CellMISPUMailboxInfo evinfo = cellFactory.createMISPUMailboxInfo();
				MISession miSession = target.getMISession(); 
				try {
					miSession.postCommand( evinfo );
					MIInfo info = evinfo.getMIInfo();
					if ( info != null ) {
						if ( info instanceof CellMISPUMailboxInfoInfo) {
							return ((CellMISPUMailboxInfoInfo)info).getMailboxes();
						}
					}
				} catch (MIException e) {
					List mboxs = new ArrayList();
					mboxs.add(new MISPUMailbox(MISPUMailbox.MBOX,BEMessages.getString("SPUEnhancementsProcessor.0"),Integer.toString(0))); //$NON-NLS-1$
					mboxs.add(new MISPUMailbox(MISPUMailbox.IBOX,BEMessages.getString("SPUEnhancementsProcessor.0"),Integer.toString(0))); //$NON-NLS-1$
					mboxs.add(new MISPUMailbox(MISPUMailbox.WBOX,BEMessages.getString("SPUEnhancementsProcessor.0"),Integer.toString(0))); //$NON-NLS-1$
					return (MISPUMailbox[]) mboxs.toArray(new MISPUMailbox[] {});
				}
			}
			
		}
		List mboxs = new ArrayList();
		mboxs.add(new MISPUMailbox(MISPUMailbox.MBOX,BEMessages.getString("SPUEnhancementsProcessor.2"),Integer.toString(0))); //$NON-NLS-1$
		mboxs.add(new MISPUMailbox(MISPUMailbox.IBOX,BEMessages.getString("SPUEnhancementsProcessor.2"),Integer.toString(0))); //$NON-NLS-1$
		mboxs.add(new MISPUMailbox(MISPUMailbox.WBOX,BEMessages.getString("SPUEnhancementsProcessor.2"),Integer.toString(0))); //$NON-NLS-1$
		return (MISPUMailbox[]) mboxs.toArray(new MISPUMailbox[] {});
	}
	
	public Object[] processSPUDMA(ICDebugTarget cTarget){
		Target target = processCTarget(cTarget);
		if (target != null) {
			StandardCellCommandFactory cellFactory = processCommandFactory(target.getMISession().getCommandFactory());
			if (cellFactory != null) {
				CellMISPUDMAInfo evinfo = cellFactory.createMISPUDMAInfo();
				MISession miSession = target.getMISession(); 
				try {
					miSession.postCommand( evinfo );
					MIInfo info = evinfo.getMIInfo();
					if ( info != null ) {
						if ( info instanceof CellMISPUDMAInfoInfo) {
							Object[] cmds = ((CellMISPUDMAInfoInfo)info).getDMAcmds();
							Object[] cmdlist = ((CellMISPUDMAInfoInfo)info).getDMAcmdlist();
							return new Object[] {cmds,cmdlist};
						}
					}
				} catch (MIException e) {
					count++;
					List aList = new ArrayList();
					List bList = new ArrayList();
					aList.add(new MISPUDMAListTuple(BEMessages.getString("SPUEnhancementsProcessor.0"),BEMessages.getString("SPUEnhancementsProcessor.0"),BEMessages.getString("SPUEnhancementsProcessor.0"),BEMessages.getString("SPUEnhancementsProcessor.0"),BEMessages.getString("SPUEnhancementsProcessor.0"),BEMessages.getString("SPUEnhancementsProcessor.0"),BEMessages.getString("SPUEnhancementsProcessor.0"),BEMessages.getString("SPUEnhancementsProcessor.0"),BEMessages.getString("SPUEnhancementsProcessor.0"),BEMessages.getString("SPUEnhancementsProcessor.0"))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$ //$NON-NLS-10$
					bList.add(new MISPUDMAElement(MISPUDMAElement.DMA_INFO_TYPE,BEMessages.getString("SPUEnhancementsProcessor.0"))); //$NON-NLS-1$
					bList.add(new MISPUDMAElement(MISPUDMAElement.DMA_INFO_MASK,BEMessages.getString("SPUEnhancementsProcessor.0"))); //$NON-NLS-1$
					bList.add(new MISPUDMAElement(MISPUDMAElement.DMA_INFO_STATUS,BEMessages.getString("SPUEnhancementsProcessor.0"))); //$NON-NLS-1$
					bList.add(new MISPUDMAElement(MISPUDMAElement.DMA_INFO_STALLNNOTIFY,BEMessages.getString("SPUEnhancementsProcessor.0"))); //$NON-NLS-1$
					bList.add(new MISPUDMAElement(MISPUDMAElement.DMA_INFO_ATOMICCMDST,BEMessages.getString("SPUEnhancementsProcessor.0"))); //$NON-NLS-1$
					Object[] cmdlist = aList.toArray(new MISPUDMAListTuple[] {});
					Object[] cmds = bList.toArray(new MISPUDMAElement[] {});
					return new Object[] {cmds,cmdlist};
				}
			}
			
		}
		List aList = new ArrayList();
		List bList = new ArrayList();
		aList.add(new MISPUDMAListTuple(BEMessages.getString("SPUEnhancementsProcessor.2"),BEMessages.getString("SPUEnhancementsProcessor.2"),BEMessages.getString("SPUEnhancementsProcessor.2"),BEMessages.getString("SPUEnhancementsProcessor.2"),BEMessages.getString("SPUEnhancementsProcessor.2"),BEMessages.getString("SPUEnhancementsProcessor.2"),BEMessages.getString("SPUEnhancementsProcessor.2"),BEMessages.getString("SPUEnhancementsProcessor.2"),BEMessages.getString("SPUEnhancementsProcessor.2"),BEMessages.getString("SPUEnhancementsProcessor.2"))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$ //$NON-NLS-10$
		bList.add(new MISPUDMAElement(MISPUDMAElement.DMA_INFO_TYPE,BEMessages.getString("SPUEnhancementsProcessor.2"))); //$NON-NLS-1$
		bList.add(new MISPUDMAElement(MISPUDMAElement.DMA_INFO_MASK,BEMessages.getString("SPUEnhancementsProcessor.2"))); //$NON-NLS-1$
		bList.add(new MISPUDMAElement(MISPUDMAElement.DMA_INFO_STATUS,BEMessages.getString("SPUEnhancementsProcessor.2"))); //$NON-NLS-1$
		bList.add(new MISPUDMAElement(MISPUDMAElement.DMA_INFO_STALLNNOTIFY,BEMessages.getString("SPUEnhancementsProcessor.2"))); //$NON-NLS-1$
		bList.add(new MISPUDMAElement(MISPUDMAElement.DMA_INFO_ATOMICCMDST,BEMessages.getString("SPUEnhancementsProcessor.2"))); //$NON-NLS-1$
		Object[] cmdlist = aList.toArray(new MISPUDMAListTuple[] {});
		Object[] cmds = bList.toArray(new MISPUDMAElement[] {});
		return new Object[] {cmds,cmdlist};
	}
	
	public Object[] processSPUProxyDMA(ICDebugTarget cTarget){
		Target target = processCTarget(cTarget);
		if (target != null) {
			StandardCellCommandFactory cellFactory = processCommandFactory(target.getMISession().getCommandFactory());
			if (cellFactory != null) {
				CellMISPUProxyDMAInfo evinfo = cellFactory.createMISPUProxyDMAInfo();
				MISession miSession = target.getMISession(); 
				try {
					miSession.postCommand( evinfo );
					MIInfo info = evinfo.getMIInfo();
					if ( info != null ) {
						if ( info instanceof CellMISPUProxyDMAInfoInfo) {
							Object[] cmds = ((CellMISPUProxyDMAInfoInfo)info).getProxyDMAcmds();
							Object[] cmdlist = ((CellMISPUProxyDMAInfoInfo)info).getProxyDMAcmdlist();
							return new Object[] {cmds,cmdlist};
						}
					}
				} catch (MIException e) {
					count++;
					List aList = new ArrayList();
					List bList = new ArrayList();
					aList.add(new MISPUDMAListTuple(BEMessages.getString("SPUEnhancementsProcessor.0"),BEMessages.getString("SPUEnhancementsProcessor.0"),BEMessages.getString("SPUEnhancementsProcessor.0"),BEMessages.getString("SPUEnhancementsProcessor.0"),BEMessages.getString("SPUEnhancementsProcessor.0"),BEMessages.getString("SPUEnhancementsProcessor.0"),BEMessages.getString("SPUEnhancementsProcessor.0"),BEMessages.getString("SPUEnhancementsProcessor.0"),BEMessages.getString("SPUEnhancementsProcessor.0"),BEMessages.getString("SPUEnhancementsProcessor.0"))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$ //$NON-NLS-10$
					bList.add(new MISPUProxyDMAElement(MISPUProxyDMAElement.PROXYDMA_INFO_TYPE,BEMessages.getString("SPUEnhancementsProcessor.0"))); //$NON-NLS-1$
					bList.add(new MISPUProxyDMAElement(MISPUProxyDMAElement.PROXYDMA_INFO_MASK,BEMessages.getString("SPUEnhancementsProcessor.0"))); //$NON-NLS-1$
					bList.add(new MISPUProxyDMAElement(MISPUProxyDMAElement.PROXYDMA_INFO_STATUS,BEMessages.getString("SPUEnhancementsProcessor.0"))); //$NON-NLS-1$
					Object[] cmdlist = aList.toArray(new MISPUDMAListTuple[] {});
					Object[] cmds = bList.toArray(new MISPUProxyDMAElement[] {});
					return new Object[] {cmds,cmdlist};
				}
			}
			
		}
		List aList = new ArrayList();
		List bList = new ArrayList();
		aList.add(new MISPUDMAListTuple(BEMessages.getString("SPUEnhancementsProcessor.2"),BEMessages.getString("SPUEnhancementsProcessor.2"),BEMessages.getString("SPUEnhancementsProcessor.2"),BEMessages.getString("SPUEnhancementsProcessor.2"),BEMessages.getString("SPUEnhancementsProcessor.2"),BEMessages.getString("SPUEnhancementsProcessor.2"),BEMessages.getString("SPUEnhancementsProcessor.2"),BEMessages.getString("SPUEnhancementsProcessor.2"),BEMessages.getString("SPUEnhancementsProcessor.2"),BEMessages.getString("SPUEnhancementsProcessor.2"))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$ //$NON-NLS-10$
		bList.add(new MISPUProxyDMAElement(MISPUProxyDMAElement.PROXYDMA_INFO_TYPE,BEMessages.getString("SPUEnhancementsProcessor.2"))); //$NON-NLS-1$
		bList.add(new MISPUProxyDMAElement(MISPUProxyDMAElement.PROXYDMA_INFO_MASK,BEMessages.getString("SPUEnhancementsProcessor.2"))); //$NON-NLS-1$
		bList.add(new MISPUProxyDMAElement(MISPUProxyDMAElement.PROXYDMA_INFO_STATUS,BEMessages.getString("SPUEnhancementsProcessor.2"))); //$NON-NLS-1$
		Object[] cmdlist = aList.toArray(new MISPUDMAListTuple[] {});
		Object[] cmds = bList.toArray(new MISPUProxyDMAElement[] {});
		return new Object[] {cmds,cmdlist};
	}
	
	private Target processCTarget(ICDebugTarget cTarget) {
		
		Target target = null;
		if (cTarget != null) {
			ICDITarget cdiTarget = (ICDITarget) cTarget.getAdapter(ICDITarget.class);
			if (cdiTarget != null) {
				if (cdiTarget instanceof Target) {
					target = (Target) cdiTarget;
				}
			}
		}
		
		return target;
	}
	
	private StandardCellCommandFactory processCommandFactory(CommandFactory factory) {
		
		StandardCellCommandFactory cellFactory = null;
		if (factory != null) {
			if (factory instanceof StandardCellCommandFactory) {
				cellFactory = (StandardCellCommandFactory) factory;
			}
		}
		
		return cellFactory;
	}

}
