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
package org.eclipse.ptp.debug.internal.core.pdi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.ptp.core.util.BitList;
import org.eclipse.ptp.debug.core.pdi.IPDIVariableManager;
import org.eclipse.ptp.debug.core.pdi.PDIException;
import org.eclipse.ptp.debug.core.pdi.event.IPDIEvent;
import org.eclipse.ptp.debug.core.pdi.model.IPDIArgumentDescriptor;
import org.eclipse.ptp.debug.core.pdi.model.IPDILocalVariableDescriptor;
import org.eclipse.ptp.debug.core.pdi.model.IPDIStackFrame;
import org.eclipse.ptp.debug.core.pdi.model.IPDIThread;
import org.eclipse.ptp.debug.core.pdi.model.IPDIThreadStorageDescriptor;
import org.eclipse.ptp.debug.internal.core.pdi.event.ChangedEvent;
import org.eclipse.ptp.debug.internal.core.pdi.event.DestroyedEvent;
import org.eclipse.ptp.debug.internal.core.pdi.event.VariableInfo;
import org.eclipse.ptp.debug.internal.core.pdi.model.Argument;
import org.eclipse.ptp.debug.internal.core.pdi.model.ArgumentDescriptor;
import org.eclipse.ptp.debug.internal.core.pdi.model.GlobalVariable;
import org.eclipse.ptp.debug.internal.core.pdi.model.GlobalVariableDescriptor;
import org.eclipse.ptp.debug.internal.core.pdi.model.LocalVariable;
import org.eclipse.ptp.debug.internal.core.pdi.model.LocalVariableDescriptor;
import org.eclipse.ptp.debug.internal.core.pdi.model.Register;
import org.eclipse.ptp.debug.internal.core.pdi.model.RegisterDescriptor;
import org.eclipse.ptp.debug.internal.core.pdi.model.StackFrame;
import org.eclipse.ptp.debug.internal.core.pdi.model.Target;
import org.eclipse.ptp.debug.internal.core.pdi.model.Thread;
import org.eclipse.ptp.debug.internal.core.pdi.model.ThreadStorage;
import org.eclipse.ptp.debug.internal.core.pdi.model.ThreadStorageDescriptor;
import org.eclipse.ptp.debug.internal.core.pdi.model.Variable;
import org.eclipse.ptp.debug.internal.core.pdi.model.VariableDescriptor;
import org.eclipse.ptp.debug.internal.core.pdi.request.DeleteVariableRequest;
import org.eclipse.ptp.debug.internal.core.pdi.request.ListArgumentsRequest;
import org.eclipse.ptp.debug.internal.core.pdi.request.ListLocalVariablesRequest;

/**
 * @author clement
 *
 */
public class VariableManager extends Manager implements IPDIVariableManager {
	static final Variable[] EMPTY_VARIABLES = {};
	int MAX_STACK_DEPTH = Thread.STACKFRAME_DEFAULT_DEPTH;
	Map<BitList, List<Variable>> variablesMap;

	public VariableManager(Session session) {
		super(session, true);
		variablesMap = new Hashtable<BitList, List<Variable>>();
	}
	public void shutdown() {
		variablesMap.clear();
	}
	private synchronized List<Variable> getVariablesList(BitList tasks) {
		List<Variable> variablesList = (List<Variable>) variablesMap.get(tasks);
		if (variablesList == null) {
			variablesList = Collections.synchronizedList(new ArrayList<Variable>());
			variablesMap.put(tasks, variablesList);
		}
		return variablesList;
	}
	public Variable getVariableByName(BitList tasks, String varname) {
		Variable[] vars = getVariables(tasks);
		for (Variable var : vars) {
			if (var.getName().equals(varname))
				return var;
		}
		return null;
	}
	public Variable getVariable(BitList tasks, String varid) {
		Variable[] vars = getVariables(tasks);
		for (Variable var : vars) {
			if (var.getVarId().equals(varid)) {
				return var;
			}
			Variable v = var.getChild(varid);
			if (v != null) {
				return v;
			}
		}
		return null;
	}
	Variable findVariable(VariableDescriptor v) throws PDIException {
		IPDIStackFrame vstack = v.getStackFrame();
		IPDIThread vthread = v.getThread();
		int position = v.getPosition();
		int depth = v.getStackDepth();
		Variable[] vars = getVariables(v.getTasks());
		for (int i = 0; i < vars.length; i++) {
			//TODO compare FULL NAME
			if (vars[i].getFullName().equals(v.getFullName())
					&& vars[i].getName().equals(v.getName())
					&& vars[i].getCastingArrayStart() == v.getCastingArrayStart()
					&& vars[i].getCastingArrayEnd() == v.getCastingArrayEnd()
					&& VariableDescriptor.equalsCasting(vars[i], v)) {
				// check threads
				IPDIThread thread = vars[i].getThread();
				if ((vthread == null && thread == null) || (vthread != null && thread != null && thread.equals(vthread))) {
					// check stackframes
					IPDIStackFrame frame = vars[i].getStackFrame();
					if (vstack == null && frame == null) {
						return vars[i];
					} else if (frame != null && vstack != null && frame.equals(vstack)) {
						if (vars[i].getPosition() == position) {
							if (vars[i].getStackDepth() == depth) {
								return vars[i];
							}
						}
					}
				}
			}
		}
		return null;
	}
	Variable[] getVariables(BitList tasks) {
		List<Variable> variableList = (List<Variable>)variablesMap.get(tasks);
		if (variableList != null) {
			return (Variable[]) variableList.toArray(new Variable[variableList.size()]);
		}
		return new Variable[0];
	}
	void removeVar(BitList tasks, String varid) throws PDIException {
		session.getEventRequestManager().addEventRequest(new DeleteVariableRequest(session, tasks, varid));
	}
	public Variable removeVariableFromList(BitList tasks, String varId) {
		List<Variable> varList = getVariablesList(tasks);
		synchronized (varList) {
			for (Iterator<Variable> iterator = varList.iterator(); iterator.hasNext();) {
				Variable variable = (Variable)iterator.next();
				if (variable.getVarId().equals(varId)) {
					iterator.remove();
					return variable;
				}
			}
		}
		return null;
	}
	public VariableDescriptor getVariableDescriptorAsArray(VariableDescriptor varDesc, int start, int length) throws PDIException {
		Thread thread = (Thread)varDesc.getThread();
		StackFrame frame = (StackFrame)varDesc.getStackFrame();
		String name = varDesc.getName();
		String fullName = varDesc.getFullName();
		int pos = varDesc.getPosition();
		int depth = varDesc.getStackDepth();
		VariableDescriptor vo = null;

		if (varDesc instanceof ArgumentDescriptor || varDesc instanceof Argument) {
			vo = new ArgumentDescriptor(session, varDesc.getTasks(), thread, frame, name, fullName, pos, depth);
		} else if (varDesc instanceof LocalVariableDescriptor || varDesc instanceof LocalVariable) {
			vo = new LocalVariableDescriptor(session, varDesc.getTasks(), thread, frame, name, fullName, pos, depth);
		} else if (varDesc instanceof GlobalVariableDescriptor || varDesc instanceof GlobalVariable) {
			vo = new GlobalVariableDescriptor(session, varDesc.getTasks(), thread, frame, name, fullName, pos, depth);
		} else if (varDesc instanceof RegisterDescriptor || varDesc instanceof Register) {
			vo = new RegisterDescriptor(session, varDesc.getTasks(), thread, frame, name, fullName, pos, depth);
		} else if (varDesc instanceof ThreadStorageDescriptor || varDesc instanceof ThreadStorage) {
			vo = new ThreadStorageDescriptor(session, varDesc.getTasks(), thread, frame, name, fullName, pos, depth);
		} else {
			throw new PDIException(varDesc.getTasks(), PDIResources.getString("pdi.VariableManager.Unknown_variable_object"));			
		}

		vo.setCastingArrayStart(varDesc.getCastingArrayStart() + start);
		vo.setCastingArrayEnd(length);
		return vo;
	}
	public VariableDescriptor getVariableDescriptorAsType(VariableDescriptor varDesc, String type) throws PDIException {
		throw new PDIException(varDesc.getTasks(), "Not implemented getVariableDescriptorAsType() yet.");
	}
	public Variable createVariable(VariableDescriptor varDesc) throws PDIException {
		if (varDesc instanceof ArgumentDescriptor) {
			return createArgument((ArgumentDescriptor)varDesc);
		} else if (varDesc instanceof LocalVariableDescriptor) {
			return createLocalVariable((LocalVariableDescriptor)varDesc);
		} else if (varDesc instanceof GlobalVariableDescriptor) {
			return createGlobalVariable((GlobalVariableDescriptor)varDesc);
		} else if (varDesc instanceof RegisterDescriptor) {
			return session.getRegisterManager().createRegister((RegisterDescriptor)varDesc);
		} else if (varDesc instanceof ThreadStorageDescriptor) {
			return createThreadStorage((ThreadStorageDescriptor)varDesc);
		}
		throw new PDIException(varDesc.getTasks(), PDIResources.getString("pdi.VariableManager.Unknown_variable_object"));			
	}
	public Argument createArgument(ArgumentDescriptor argDesc) throws PDIException {
		Variable variable = findVariable(argDesc);
		Argument argument = null;
		if (variable != null && variable instanceof Argument) {
			argument = (Argument) variable;
		}
		if (argument == null) {
			StackFrame stack = (StackFrame)argDesc.getStackFrame();
			Target target = (Target)stack.getTarget();
			Thread currentThread = (Thread)target.getCurrentThread();
			StackFrame currentFrame = currentThread.getCurrentStackFrame();
			target.lockTarget();
			try {
				target.setCurrentThread(stack.getThread(), false);
				((Thread)stack.getThread()).setCurrentStackFrame(stack, false);
				argument = new Argument(session, argDesc, argDesc.getVarId());
				List<Variable> variablesList = getVariablesList(argDesc.getTasks());
				variablesList.add(argument);
			} finally {
				target.setCurrentThread(currentThread, false);
				currentThread.setCurrentStackFrame(currentFrame, false);
				target.releaseTarget();
			}
		}
		return argument;
	}
	public IPDIArgumentDescriptor[] getArgumentDescriptors(StackFrame frame) throws PDIException {
		List<ArgumentDescriptor> argObjects = new ArrayList<ArgumentDescriptor>();
		Target target = (Target)frame.getTarget();
		Thread currentThread = (Thread)target.getCurrentThread();
		StackFrame currentFrame = currentThread.getCurrentStackFrame();
		target.lockTarget();
		try {
			target.setCurrentThread(frame.getThread(), false);
			((Thread)frame.getThread()).setCurrentStackFrame(frame, false);
						
			int depth = frame.getThread().getStackFrameCount();
			int level = frame.getLevel();
			int diff = depth - level;
			
			ListArgumentsRequest request = new ListArgumentsRequest(session, target.getTasks(), diff, diff);
			session.getEventRequestManager().addEventRequest(request);
			String[] args = request.getArguments(target.getTasks());
			for (int i = 0; i < args.length; i++) {
				argObjects.add(new ArgumentDescriptor(session, target.getTasks(), null, frame, args[i], null, args.length - i, level));
			}
		} finally {
			target.setCurrentThread(currentThread, false);
			currentThread.setCurrentStackFrame(currentFrame, false);
			target.releaseTarget();
		}
		return (IPDIArgumentDescriptor[]) argObjects.toArray(new IPDIArgumentDescriptor[0]);
	}
	public GlobalVariableDescriptor getGlobalVariableDescriptor(BitList tasks, String filename, String function, String name) throws PDIException {
		if (filename == null) {
			filename = new String();
		}
		if (function == null) {
			function = new String();
		}
		if (name == null) {
			name = new String();
		}
		StringBuffer buffer = new StringBuffer();
		if (filename.length() > 0) {
			buffer.append('\'').append(filename).append('\'').append("::");
		}
		if (function.length() > 0) {
			buffer.append(function).append("::");
		}
		buffer.append(name);
		return new GlobalVariableDescriptor(session, tasks, null, null, buffer.toString(), null, 0, 0);
	}
	public GlobalVariable createGlobalVariable(GlobalVariableDescriptor varDesc) throws PDIException {
		Variable variable = findVariable(varDesc);
		GlobalVariable global = null;
		if (variable instanceof GlobalVariable) {
			global = (GlobalVariable)variable;
		}
		if (global == null) {
			global = new GlobalVariable(session, varDesc, varDesc.getVarId());
			List<Variable> variablesList = getVariablesList(varDesc.getTasks());
			variablesList.add(global);
		}
		return global;
	}
	public IPDILocalVariableDescriptor[] getLocalVariableDescriptors(StackFrame frame) throws PDIException {
		List<LocalVariableDescriptor> varObjects = new ArrayList<LocalVariableDescriptor>();
		Target target = (Target)frame.getTarget();
		Thread currentThread = (Thread)target.getCurrentThread();
		StackFrame currentFrame = currentThread.getCurrentStackFrame();
		target.lockTarget();
		try {
			target.setCurrentThread(frame.getThread(), false);
			((Thread)frame.getThread()).setCurrentStackFrame(frame, false);
			int level = frame.getLevel();
			
			ListLocalVariablesRequest request = new ListLocalVariablesRequest(target.getTasks());
			session.getEventRequestManager().addEventRequest(request);
			String[] vars = request.getLocalVariables(target.getTasks());
			for (int i=0; i<vars.length; i++) {
				varObjects.add(new LocalVariableDescriptor(session, target.getTasks(), null, frame, vars[i], null, vars.length - i, level));
			}
		} finally {
			target.setCurrentThread(currentThread, false);
			currentThread.setCurrentStackFrame(currentFrame, false);
			target.releaseTarget();
		}
		return (IPDILocalVariableDescriptor[]) varObjects.toArray(new IPDILocalVariableDescriptor[0]);
	}
	public LocalVariable createLocalVariable(LocalVariableDescriptor varDesc) throws PDIException {
		Variable variable = findVariable(varDesc);
		LocalVariable local = null;
		if (variable instanceof LocalVariable) {
			local = (LocalVariable)variable;
		}
		if (local == null) {
			StackFrame stack = (StackFrame)varDesc.getStackFrame();
			Target target = (Target)stack.getTarget();
			Thread currentThread = (Thread)target.getCurrentThread();
			StackFrame currentFrame = currentThread.getCurrentStackFrame();
			target.lockTarget();
			try {
				target.setCurrentThread(stack.getThread(), false);
				((Thread)stack.getThread()).setCurrentStackFrame(stack, false);
				local = new LocalVariable(session, varDesc, varDesc.getVarId());
				List<Variable> variablesList = getVariablesList(varDesc.getTasks());
				variablesList.add(local);
			} finally {
				target.setCurrentThread(currentThread, false);
				currentThread.setCurrentStackFrame(currentFrame, false);
				target.releaseTarget();
			}
		}
		return local;
	}
	public IPDIThreadStorageDescriptor[] getThreadStorageDescriptors(Thread thread) throws PDIException {
		return new IPDIThreadStorageDescriptor[0];
	}
	public ThreadStorage createThreadStorage(ThreadStorageDescriptor desc) throws PDIException {
		throw new PDIException(desc.getTasks(), PDIResources.getString("pdi.VariableManager.Unknown_variable_object"));
	}
	public void destroyVariable(Variable variable) throws PDIException {
		BitList qTasks = variable.getTasks();
		List<Variable> varList = getVariablesList(qTasks);
		if (varList.contains(variable)) {
			removeVar(qTasks, variable.getVarId());
		}
		IPDIEvent event = new DestroyedEvent(new VariableInfo(session, qTasks, variable.getVarId(), variable));
		session.getEventManager().fireEvents(new IPDIEvent[] { event });
	}
	public void destroyAllVariables(BitList qTasks) throws PDIException {
		List<IPDIEvent> eventList = new ArrayList<IPDIEvent>();
		Variable[] variables = getVariables(qTasks);
		for (int i = 0; i < variables.length; ++i) {
			removeVar(qTasks, variables[i].getVarId());
			eventList.add(new DestroyedEvent(new VariableInfo(session, qTasks, variables[i].getVarId(), variables[i])));
		}
		session.getEventManager().fireEvents((IPDIEvent[]) eventList.toArray(new IPDIEvent[0]));
	}
	public void update(BitList qTasks) throws PDIException {
		update(qTasks, new String[0]);
	}
	public void update(BitList qTasks, String[] vars) throws PDIException {
		List<IPDIEvent> eventList = new ArrayList<IPDIEvent>();
		for (int i = 0; i < vars.length; i++) {
			Variable variable = getVariable(qTasks, vars[i]);
			if (variable != null) {
				variable.setUpdated(true);
				eventList.add(new ChangedEvent(new VariableInfo(session, qTasks, variable.getVarId(), variable)));
			}
		}
		session.getEventManager().fireEvents((IPDIEvent[]) eventList.toArray(new IPDIEvent[0]));
	}
}
