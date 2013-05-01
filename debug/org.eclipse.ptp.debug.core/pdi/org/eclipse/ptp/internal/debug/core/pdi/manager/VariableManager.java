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
package org.eclipse.ptp.internal.debug.core.pdi.manager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.ptp.debug.core.TaskSet;
import org.eclipse.ptp.debug.core.pdi.IPDISession;
import org.eclipse.ptp.debug.core.pdi.PDIException;
import org.eclipse.ptp.debug.core.pdi.event.IPDIEvent;
import org.eclipse.ptp.debug.core.pdi.manager.IPDIVariableManager;
import org.eclipse.ptp.debug.core.pdi.model.IPDIArgument;
import org.eclipse.ptp.debug.core.pdi.model.IPDIArgumentDescriptor;
import org.eclipse.ptp.debug.core.pdi.model.IPDIGlobalVariable;
import org.eclipse.ptp.debug.core.pdi.model.IPDIGlobalVariableDescriptor;
import org.eclipse.ptp.debug.core.pdi.model.IPDILocalVariable;
import org.eclipse.ptp.debug.core.pdi.model.IPDILocalVariableDescriptor;
import org.eclipse.ptp.debug.core.pdi.model.IPDIRegister;
import org.eclipse.ptp.debug.core.pdi.model.IPDIRegisterDescriptor;
import org.eclipse.ptp.debug.core.pdi.model.IPDIStackFrame;
import org.eclipse.ptp.debug.core.pdi.model.IPDITarget;
import org.eclipse.ptp.debug.core.pdi.model.IPDIThread;
import org.eclipse.ptp.debug.core.pdi.model.IPDIThreadStorage;
import org.eclipse.ptp.debug.core.pdi.model.IPDIThreadStorageDescriptor;
import org.eclipse.ptp.debug.core.pdi.model.IPDIVariable;
import org.eclipse.ptp.debug.core.pdi.model.IPDIVariableDescriptor;
import org.eclipse.ptp.debug.core.pdi.request.IPDIListArgumentsRequest;
import org.eclipse.ptp.debug.core.pdi.request.IPDIListLocalVariablesRequest;
import org.eclipse.ptp.internal.debug.core.pdi.messages.Messages;

/**
 * @author clement
 * 
 */
public class VariableManager extends AbstractPDIManager implements IPDIVariableManager {
	static final IPDIVariable[] EMPTY_VARIABLES = {};
	int MAX_STACK_DEPTH = IPDIThread.STACKFRAME_DEFAULT_DEPTH;
	Map<TaskSet, List<IPDIVariable>> variablesMap;

	public VariableManager(IPDISession session) {
		super(session, true);
		variablesMap = new Hashtable<TaskSet, List<IPDIVariable>>();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.manager.IPDIVariableManager#createArgument(org.eclipse.ptp.debug.core.pdi.model.
	 * IPDIArgumentDescriptor)
	 */
	public IPDIArgument createArgument(IPDIArgumentDescriptor argDesc) throws PDIException {
		IPDIVariable variable = findVariable(argDesc);
		IPDIArgument argument = null;
		if (variable != null && variable instanceof IPDIArgument) {
			argument = (IPDIArgument) variable;
		}
		if (argument == null) {
			IPDIStackFrame stack = argDesc.getStackFrame();
			IPDITarget target = stack.getTarget();
			IPDIThread currentThread = target.getCurrentThread();
			IPDIStackFrame currentFrame = currentThread.getCurrentStackFrame();
			target.lockTarget();
			try {
				target.setCurrentThread(stack.getThread(), false);
				stack.getThread().setCurrentStackFrame(stack, false);
				argument = session.getModelFactory().newArgument(session, argDesc, argDesc.getId());
				List<IPDIVariable> variablesList = getVariablesList(argDesc.getTasks());
				variablesList.add(argument);
			} finally {
				target.setCurrentThread(currentThread, false);
				currentThread.setCurrentStackFrame(currentFrame, false);
				target.releaseTarget();
			}
		}
		return argument;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.manager.IPDIVariableManager#createGlobalVariable(org.eclipse.ptp.debug.core.pdi.model.
	 * IPDIGlobalVariableDescriptor)
	 */
	public IPDIGlobalVariable createGlobalVariable(IPDIGlobalVariableDescriptor varDesc) throws PDIException {
		IPDIVariable variable = findVariable(varDesc);
		IPDIGlobalVariable global = null;
		if (variable instanceof IPDIGlobalVariable) {
			global = (IPDIGlobalVariable) variable;
		}
		if (global == null) {
			global = session.getModelFactory().newGlobalVariable(session, varDesc, varDesc.getId());
			List<IPDIVariable> variablesList = getVariablesList(varDesc.getTasks());
			variablesList.add(global);
		}
		return global;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.manager.IPDIVariableManager#createLocalVariable(org.eclipse.ptp.debug.core.pdi.model.
	 * IPDILocalVariableDescriptor)
	 */
	public IPDILocalVariable createLocalVariable(IPDILocalVariableDescriptor varDesc) throws PDIException {
		IPDIVariable variable = findVariable(varDesc);
		IPDILocalVariable local = null;
		if (variable instanceof IPDILocalVariable) {
			local = (IPDILocalVariable) variable;
		}
		if (local == null) {
			IPDIStackFrame stack = varDesc.getStackFrame();
			IPDITarget target = stack.getTarget();
			IPDIThread currentThread = target.getCurrentThread();
			IPDIStackFrame currentFrame = currentThread.getCurrentStackFrame();
			target.lockTarget();
			try {
				target.setCurrentThread(stack.getThread(), false);
				stack.getThread().setCurrentStackFrame(stack, false);
				local = session.getModelFactory().newLocalVariable(session, varDesc, varDesc.getId());
				List<IPDIVariable> variablesList = getVariablesList(varDesc.getTasks());
				variablesList.add(local);
			} finally {
				target.setCurrentThread(currentThread, false);
				currentThread.setCurrentStackFrame(currentFrame, false);
				target.releaseTarget();
			}
		}
		return local;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.manager.IPDIVariableManager#createThreadStorage(org.eclipse.ptp.debug.core.pdi.model.
	 * IPDIThreadStorageDescriptor)
	 */
	public IPDIThreadStorage createThreadStorage(IPDIThreadStorageDescriptor desc) throws PDIException {
		throw new PDIException(desc.getTasks(), Messages.VariableManager_0);
	}

	/**
	 * @param varDesc
	 * @return
	 * @throws PDIException
	 */
	public IPDIVariable createVariable(IPDIVariableDescriptor varDesc) throws PDIException {
		if (varDesc instanceof IPDIArgumentDescriptor) {
			return createArgument((IPDIArgumentDescriptor) varDesc);
		} else if (varDesc instanceof IPDILocalVariableDescriptor) {
			return createLocalVariable((IPDILocalVariableDescriptor) varDesc);
		} else if (varDesc instanceof IPDIGlobalVariableDescriptor) {
			return createGlobalVariable((IPDIGlobalVariableDescriptor) varDesc);
		} else if (varDesc instanceof IPDIRegisterDescriptor) {
			return session.getRegisterManager().createRegister((IPDIRegisterDescriptor) varDesc);
		} else if (varDesc instanceof IPDIThreadStorageDescriptor) {
			return createThreadStorage((IPDIThreadStorageDescriptor) varDesc);
		}
		throw new PDIException(varDesc.getTasks(), Messages.VariableManager_0);
	}

	/**
	 * @param qTasks
	 * @throws PDIException
	 */
	public void destroyAllVariables(TaskSet qTasks) throws PDIException {
		List<IPDIEvent> eventList = new ArrayList<IPDIEvent>();
		IPDIVariable[] variables = getVariables(qTasks);
		for (IPDIVariable variable : variables) {
			removeVar(qTasks, variable.getId());
			eventList.add(session.getEventFactory().newDestroyedEvent(
					session.getEventFactory().newVariableInfo(session, qTasks, variable.getId(), variable)));
		}
		session.getEventManager().fireEvents(eventList.toArray(new IPDIEvent[0]));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.manager.IPDIVariableManager#destroyVariable(org.eclipse.ptp.debug.core.pdi.model.IPDIVariable)
	 */
	public void destroyVariable(IPDIVariable variable) throws PDIException {
		TaskSet qTasks = variable.getTasks();
		List<IPDIVariable> varList = getVariablesList(qTasks);
		if (varList.contains(variable)) {
			removeVar(qTasks, variable.getId());
		}
		IPDIEvent event = session.getEventFactory().newDestroyedEvent(
				session.getEventFactory().newVariableInfo(session, qTasks, variable.getId(), variable));
		session.getEventManager().fireEvents(new IPDIEvent[] { event });
	}

	/**
	 * XXX: code change that could result in different behavior
	 * 
	 * @param v
	 * @return
	 * @throws PDIException
	 */
	public IPDIVariable findVariable(IPDIVariableDescriptor v) throws PDIException {
		IPDIVariable[] vars = getVariables(v.getTasks());
		for (IPDIVariable var : vars) {
			// TODO compare FULL NAME
			if (var.getFullName().equals(v.getFullName()) && var.equalDescriptors(v)) {
				return var;
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.manager.IPDIVariableManager#getArgumentDescriptors(org.eclipse.ptp.debug.core.pdi.model.
	 * IPDIStackFrame)
	 */
	public IPDIArgumentDescriptor[] getArgumentDescriptors(IPDIStackFrame frame) throws PDIException {
		List<IPDIArgumentDescriptor> argObjects = new ArrayList<IPDIArgumentDescriptor>();
		IPDITarget target = frame.getTarget();
		IPDIThread currentThread = target.getCurrentThread();
		IPDIStackFrame currentFrame = currentThread.getCurrentStackFrame();
		target.lockTarget();
		try {
			target.setCurrentThread(frame.getThread(), false);
			frame.getThread().setCurrentStackFrame(frame, false);

			int depth = frame.getThread().getStackFrameCount();
			int level = frame.getLevel();
			int diff = depth - level;

			IPDIListArgumentsRequest request = session.getRequestFactory().getListArgumentsRequest(target.getTasks(), diff, diff);
			session.getEventRequestManager().addEventRequest(request);
			String[] args = request.getArguments(target.getTasks());
			for (int i = 0; i < args.length; i++) {
				argObjects.add(session.getModelFactory().newArgumentDescriptor(session, target.getTasks(), null, frame, args[i],
						null, args.length - i, level));
			}
		} finally {
			target.setCurrentThread(currentThread, false);
			currentThread.setCurrentStackFrame(currentFrame, false);
			target.releaseTarget();
		}
		return argObjects.toArray(new IPDIArgumentDescriptor[0]);
	}

	/**
	 * @param tasks
	 * @param filename
	 * @param function
	 * @param name
	 * @return
	 * @throws PDIException
	 */
	public IPDIGlobalVariableDescriptor getGlobalVariableDescriptor(TaskSet tasks, String filename, String function, String name)
			throws PDIException {
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
			buffer.append('\'').append(filename).append('\'').append("::"); //$NON-NLS-1$
		}
		if (function.length() > 0) {
			buffer.append(function).append("::"); //$NON-NLS-1$
		}
		buffer.append(name);
		return session.getModelFactory().newGlobalVariableDescriptor(session, tasks, null, null, buffer.toString(), null, 0, 0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.manager.IPDIVariableManager#getLocalVariableDescriptors(org.eclipse.ptp.debug.core.pdi.model
	 * .IPDIStackFrame)
	 */
	public IPDILocalVariableDescriptor[] getLocalVariableDescriptors(IPDIStackFrame frame) throws PDIException {
		List<IPDILocalVariableDescriptor> varObjects = new ArrayList<IPDILocalVariableDescriptor>();
		IPDITarget target = frame.getTarget();
		IPDIThread currentThread = target.getCurrentThread();
		IPDIStackFrame currentFrame = currentThread.getCurrentStackFrame();
		target.lockTarget();
		try {
			target.setCurrentThread(frame.getThread(), false);
			frame.getThread().setCurrentStackFrame(frame, false);
			int level = frame.getLevel();

			IPDIListLocalVariablesRequest request = session.getRequestFactory().getListLocalVariablesRequest(target.getTasks());
			session.getEventRequestManager().addEventRequest(request);
			String[] vars = request.getLocalVariables(target.getTasks());
			for (int i = 0; i < vars.length; i++) {
				varObjects.add(session.getModelFactory().newLocalVariableDescriptor(session, target.getTasks(), null, frame,
						vars[i], null, vars.length - i, level));
			}
		} finally {
			target.setCurrentThread(currentThread, false);
			currentThread.setCurrentStackFrame(currentFrame, false);
			target.releaseTarget();
		}
		return varObjects.toArray(new IPDILocalVariableDescriptor[0]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.manager.IPDIVariableManager#getThreadStorageDescriptors(org.eclipse.ptp.debug.core.pdi.model
	 * .IPDIThread)
	 */
	public IPDIThreadStorageDescriptor[] getThreadStorageDescriptors(IPDIThread thread) throws PDIException {
		return new IPDIThreadStorageDescriptor[0];
	}

	/**
	 * @param tasks
	 * @param varId
	 * @return
	 */
	public IPDIVariable getVariable(TaskSet tasks, String varid) {
		IPDIVariable[] vars = getVariables(tasks);
		for (IPDIVariable var : vars) {
			if (var.getId().equals(varid)) {
				return var;
			}
			IPDIVariable v = var.getChild(varid);
			if (v != null) {
				return v;
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.manager.IPDIVariableManager#getVariableByName(org.eclipse.ptp.core.util.TaskSet,
	 * java.lang.String)
	 */
	public IPDIVariable getVariableByName(TaskSet tasks, String varname) {
		IPDIVariable[] vars = getVariables(tasks);
		for (IPDIVariable var : vars) {
			if (var.getName().equals(varname)) {
				return var;
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.manager.IPDIVariableManager#getVariableDescriptorAsArray(org.eclipse.ptp.debug.core.pdi.model
	 * .IPDIVariableDescriptor, int, int)
	 */
	public IPDIVariableDescriptor getVariableDescriptorAsArray(IPDIVariableDescriptor varDesc, int start, int length)
			throws PDIException {
		IPDIThread thread = varDesc.getThread();
		IPDIStackFrame frame = varDesc.getStackFrame();
		String name = varDesc.getName();
		String fullName = varDesc.getFullName();
		int pos = varDesc.getPosition();
		int depth = varDesc.getStackDepth();
		IPDIVariableDescriptor vo = null;

		if (varDesc instanceof IPDIArgumentDescriptor || varDesc instanceof IPDIArgument) {
			vo = session.getModelFactory().newArgumentDescriptor(session, varDesc.getTasks(), thread, frame, name, fullName, pos,
					depth);
		} else if (varDesc instanceof IPDILocalVariableDescriptor || varDesc instanceof IPDILocalVariable) {
			vo = session.getModelFactory().newLocalVariableDescriptor(session, varDesc.getTasks(), thread, frame, name, fullName,
					pos, depth);
		} else if (varDesc instanceof IPDIGlobalVariableDescriptor || varDesc instanceof IPDIGlobalVariable) {
			vo = session.getModelFactory().newGlobalVariableDescriptor(session, varDesc.getTasks(), thread, frame, name, fullName,
					pos, depth);
		} else if (varDesc instanceof IPDIRegisterDescriptor || varDesc instanceof IPDIRegister) {
			vo = session.getModelFactory().newRegisterDescriptor(session, varDesc.getTasks(), thread, frame, name, fullName, pos,
					depth);
		} else if (varDesc instanceof IPDIThreadStorageDescriptor || varDesc instanceof IPDIThreadStorage) {
			vo = session.getModelFactory().newThreadStorageDescriptor(session, varDesc.getTasks(), thread, frame, name, fullName,
					pos, depth);
		} else {
			throw new PDIException(varDesc.getTasks(), Messages.VariableManager_0);
		}

		vo.setCastingArrayStart(varDesc.getCastingArrayStart() + start);
		vo.setCastingArrayEnd(length);
		return vo;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.manager.IPDIVariableManager#getVariableDescriptorAsType(org.eclipse.ptp.debug.core.pdi.model
	 * .IPDIVariableDescriptor, java.lang.String)
	 */
	public IPDIVariableDescriptor getVariableDescriptorAsType(IPDIVariableDescriptor varDesc, String type) throws PDIException {
		throw new PDIException(varDesc.getTasks(), Messages.VariableManager_1);
	}

	/**
	 * @param tasks
	 * @return
	 */
	public IPDIVariable[] getVariables(TaskSet tasks) {
		List<IPDIVariable> variableList = variablesMap.get(tasks);
		if (variableList != null) {
			return variableList.toArray(new IPDIVariable[variableList.size()]);
		}
		return new IPDIVariable[0];
	}

	/**
	 * @param tasks
	 * @param varId
	 * @throws PDIException
	 */
	public void removeVar(TaskSet tasks, String varid) throws PDIException {
		session.getEventRequestManager().addEventRequest(
				session.getRequestFactory().getDeletePartialExpressionRequest(tasks, varid));
	}

	/**
	 * @param tasks
	 * @param varId
	 * @return
	 */
	public IPDIVariable removeVariableFromList(TaskSet tasks, String varId) {
		List<IPDIVariable> varList = getVariablesList(tasks);
		synchronized (varList) {
			for (Iterator<IPDIVariable> iterator = varList.iterator(); iterator.hasNext();) {
				IPDIVariable variable = iterator.next();
				if (variable.getId().equals(varId)) {
					iterator.remove();
					return variable;
				}
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.internal.debug.core.pdi.AbstractPDIManager#shutdown()
	 */
	@Override
	public void shutdown() {
		variablesMap.clear();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.internal.debug.core.pdi.AbstractPDIManager#update(org.eclipse.ptp.core.util.TaskSet)
	 */
	@Override
	public void update(TaskSet qTasks) throws PDIException {
		update(qTasks, new String[0]);
	}

	/**
	 * @param qTasks
	 * @param vars
	 * @throws PDIException
	 */
	public void update(TaskSet qTasks, String[] vars) throws PDIException {
		List<IPDIEvent> eventList = new ArrayList<IPDIEvent>();
		for (String var : vars) {
			IPDIVariable variable = getVariable(qTasks, var);
			if (variable != null) {
				eventList.add(session.getEventFactory().newChangedEvent(
						session.getEventFactory().newVariableInfo(session, qTasks, variable.getId(), variable)));
			}
		}
		session.getEventManager().fireEvents(eventList.toArray(new IPDIEvent[0]));
	}

	/**
	 * @param tasks
	 * @return
	 */
	private synchronized List<IPDIVariable> getVariablesList(TaskSet tasks) {
		List<IPDIVariable> variablesList = variablesMap.get(tasks);
		if (variablesList == null) {
			variablesList = Collections.synchronizedList(new ArrayList<IPDIVariable>());
			variablesMap.put(tasks, variablesList);
		}
		return variablesList;
	}
}
