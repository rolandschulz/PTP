/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cldt.debug.mi.core.cdi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.eclipse.cldt.debug.core.cdi.CDIException;
import org.eclipse.cldt.debug.core.cdi.model.ICDIRegisterDescriptor;
import org.eclipse.cldt.debug.core.cdi.model.ICDIRegisterGroup;
import org.eclipse.cldt.debug.mi.core.IMIConstants;
import org.eclipse.cldt.debug.mi.core.MIException;
import org.eclipse.cldt.debug.mi.core.MIPlugin;
import org.eclipse.cldt.debug.mi.core.MISession;
import org.eclipse.cldt.debug.mi.core.cdi.model.Register;
import org.eclipse.cldt.debug.mi.core.cdi.model.RegisterDescriptor;
import org.eclipse.cldt.debug.mi.core.cdi.model.RegisterGroup;
import org.eclipse.cldt.debug.mi.core.cdi.model.Target;
import org.eclipse.cldt.debug.mi.core.cdi.model.VariableDescriptor;
import org.eclipse.cldt.debug.mi.core.command.CommandFactory;
import org.eclipse.cldt.debug.mi.core.command.MIDataListChangedRegisters;
import org.eclipse.cldt.debug.mi.core.command.MIDataListRegisterNames;
import org.eclipse.cldt.debug.mi.core.command.MIVarCreate;
import org.eclipse.cldt.debug.mi.core.command.MIVarDelete;
import org.eclipse.cldt.debug.mi.core.command.MIVarUpdate;
import org.eclipse.cldt.debug.mi.core.event.MIEvent;
import org.eclipse.cldt.debug.mi.core.event.MIRegisterChangedEvent;
import org.eclipse.cldt.debug.mi.core.event.MIVarChangedEvent;
import org.eclipse.cldt.debug.mi.core.output.MIDataListChangedRegistersInfo;
import org.eclipse.cldt.debug.mi.core.output.MIDataListRegisterNamesInfo;
import org.eclipse.cldt.debug.mi.core.output.MIVar;
import org.eclipse.cldt.debug.mi.core.output.MIVarChange;
import org.eclipse.cldt.debug.mi.core.output.MIVarCreateInfo;
import org.eclipse.cldt.debug.mi.core.output.MIVarUpdateInfo;

/**
 */
public class RegisterManager extends Manager {

	Map regsMap;
	MIVarChange[] noChanges = new MIVarChange[0];

	public RegisterManager(Session session) {
		super(session, true);
		regsMap = new Hashtable();
		setAutoUpdate( MIPlugin.getDefault().getPluginPreferences().getBoolean( IMIConstants.PREF_REGISTERS_AUTO_REFRESH ) );
	}

	synchronized List getRegistersList(Target target) {
		List regsList = (List)regsMap.get(target);
		if (regsList == null) {
			regsList = Collections.synchronizedList(new ArrayList());
			regsMap.put(target, regsList);
		}
		return regsList;
	}

	public ICDIRegisterGroup[] getRegisterGroups(Target target) throws CDIException {
		RegisterGroup group = new RegisterGroup(target, "Main"); //$NON-NLS-1$
		return new ICDIRegisterGroup[] { group };
	}

	public ICDIRegisterDescriptor[] getRegisterDescriptors(RegisterGroup group) throws CDIException {
		Target target = (Target)group.getTarget();
		return getRegisterDescriptors(target);
	}
	public ICDIRegisterDescriptor[] getRegisterDescriptors(Target target) throws CDIException {
		MISession mi = target.getMISession();
		CommandFactory factory = mi.getCommandFactory();
		MIDataListRegisterNames registers = factory.createMIDataListRegisterNames();
		try {
			mi.postCommand(registers);
			MIDataListRegisterNamesInfo info =
				registers.getMIDataListRegisterNamesInfo();
			if (info == null) {
				throw new CDIException(CdiResources.getString("cdi.Common.No_answer")); //$NON-NLS-1$
			}
			String[] names = info.getRegisterNames();
			List regsList = new ArrayList(names.length);
			for (int i = 0; i < names.length; i++) {
				if (names[i].length() > 0) {
					regsList.add(new RegisterDescriptor(target, null, null, names[i], null, i, 0));
				}
			}
			return (ICDIRegisterDescriptor[])regsList.toArray(new ICDIRegisterDescriptor[0]);
		} catch (MIException e) {
			throw new MI2CDIException(e);
		} finally {
		}
	}

	public Register createRegister(RegisterDescriptor regDesc) throws CDIException {
		Register reg = findRegister(regDesc);
		if (reg == null) {
			try {
				String name = regDesc.getQualifiedName(); //$NON-NLS-1$
				Target target = (Target)regDesc.getTarget();
				MISession mi = target.getMISession();
				CommandFactory factory = mi.getCommandFactory();
				MIVarCreate var = factory.createMIVarCreate(name);
				mi.postCommand(var);
				MIVarCreateInfo info = var.getMIVarCreateInfo();
				if (info == null) {
					throw new CDIException(CdiResources.getString("cdi.Common.No_answer")); //$NON-NLS-1$
				}
				reg = new Register(regDesc, info.getMIVar());
				List regList = getRegistersList(target);
				regList.add(reg);
			} catch (MIException e) {
				throw new MI2CDIException(e);
			}
		}
		return reg;
		//throw new CDIException(CdiResources.getString("cdi.RegisterManager.Wrong_register_type")); //$NON-NLS-1$
	}

	public void destroyRegister(Register reg) {
		Target target = (Target)reg.getTarget();
		List regList = (List)regsMap.get(target);
		if (regList != null) {
			if (regList.remove(reg)) {
				MISession miSession = target.getMISession();
				try {
					removeMIVar(miSession, reg.getMIVar());
				} catch (CDIException e) {
					//
				}
			}
		}
	}

	/**
	 * Tell gdb to remove the underlying var-object also.
	 */
	void removeMIVar(MISession miSession, MIVar miVar) throws CDIException {
		CommandFactory factory = miSession.getCommandFactory();
		MIVarDelete var = factory.createMIVarDelete(miVar.getVarName());
		try {
			miSession.postCommand(var);
			var.getMIInfo();
		} catch (MIException e) {
			throw new MI2CDIException(e);
		}
	}

	/**
	 * Use by the eventManager to find the Register;
	 */
	public Register getRegister(MISession miSession, String varName) {
		Target target = ((Session)getSession()).getTarget(miSession);
		Register[] regs = getRegisters(target);
		for (int i = 0; i < regs.length; i++) {
			if (regs[i].getMIVar().getVarName().equals(varName)) {
				return regs[i];
			}
			try {
				Register r = (Register)regs[i].getChild(varName);
				if (r != null) {
					return r;
				}
			} catch (ClassCastException e) {
			}
		}
		return null;
	}

	/**
	 * Use by the eventManager to find the Register;
	 */
	public Register getRegister(MISession miSession, int regno) {
		Target target = ((Session)getSession()).getTarget(miSession);
		return getRegister(target, regno);
	}
	public Register getRegister(Target target, int regno) {
		Register[] regs = getRegisters(target);
		for (int i = 0; i < regs.length; i++) {
			if (regs[i].getPosition() == regno) {
				return regs[i];
			}
		}
		return null;
	}

	public void update(Target target) throws CDIException {
		MISession mi = target.getMISession();
		CommandFactory factory = mi.getCommandFactory();
		MIDataListChangedRegisters changed = factory.createMIDataListChangedRegisters();
		try {
			mi.postCommand(changed);
			MIDataListChangedRegistersInfo info =
				changed.getMIDataListChangedRegistersInfo();
			if (info == null) {
				throw new CDIException(CdiResources.getString("cdi.Common.No_answer")); //$NON-NLS-1$
			}
			int[] regnos = info.getRegisterNumbers();
			List eventList = new ArrayList(regnos.length);
			// Now that we know the registers changed
			// call -var-update to update the value in gdb.
			// And send the notification.
			for (int i = 0 ; i < regnos.length; i++) {
				Register reg = getRegister(target, regnos[i]);
				if (reg != null) {
					String varName = reg.getMIVar().getVarName();
					MIVarChange[] changes = noChanges;
					MIVarUpdate update = factory.createMIVarUpdate(varName);
					try {
						mi.postCommand(update);
						MIVarUpdateInfo updateInfo = update.getMIVarUpdateInfo();
						if (updateInfo == null) {
							throw new CDIException(CdiResources.getString("cdi.Common.No_answer")); //$NON-NLS-1$
						}
						changes = updateInfo.getMIVarChanges();
					} catch (MIException e) {
						//throw new MI2CDIException(e);
						//eventList.add(new MIVarDeletedEvent(varName));
					}
					if (changes.length != 0) {
						for (int j = 0 ; j < changes.length; j++) {
							String n = changes[j].getVarName();
							if (changes[j].isInScope()) {
								eventList.add(new MIVarChangedEvent(mi, n));
							}
						}
					} else {
						// Fall back to the register number.
						eventList.add(new MIRegisterChangedEvent(mi, update.getToken(), reg.getName(), regnos[i]));
					}
				}
			}
			MIEvent[] events = (MIEvent[])eventList.toArray(new MIEvent[0]);
			mi.fireEvents(events);
		} catch (MIException e) {
			throw new MI2CDIException(e);
		}
	}

	private Register[] getRegisters(Target target) {
		List regsList = (List)regsMap.get(target);
		if (regsList != null) {
			return (Register[]) regsList.toArray(new Register[regsList.size()]);
		}
		return new Register[0];
	}

	/**
	 * Return the Element with this thread/stackframe, and with this name.
	 * null is return if the element is not in the cache.
	 */
	private Register findRegister(RegisterDescriptor rd) throws CDIException {
		Target target = (Target)rd.getTarget();
		String name = rd.getName();
		int position = rd.getPosition();
		Register[] regs = getRegisters(target);
		for (int i = 0; i < regs.length; i++) {
			if (regs[i].getName().equals(name)
				&& regs[i].getCastingArrayStart() == rd.getCastingArrayStart()
				&& regs[i].getCastingArrayEnd() == rd.getCastingArrayEnd()
				&& VariableDescriptor.equalsCasting(regs[i], rd)) {
				// check threads
				if (regs[i].getPosition() == position) {
					return regs[i];
				}
			}
		}
		return null;
	}


}
