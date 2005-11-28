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
package org.eclipse.ptp.debug.external.cdi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.event.ICDIEvent;
import org.eclipse.cdt.debug.core.cdi.model.ICDIArgument;
import org.eclipse.cdt.debug.core.cdi.model.ICDIArgumentDescriptor;
import org.eclipse.cdt.debug.core.cdi.model.ICDIGlobalVariable;
import org.eclipse.cdt.debug.core.cdi.model.ICDILocalVariable;
import org.eclipse.cdt.debug.core.cdi.model.ICDILocalVariableDescriptor;
import org.eclipse.cdt.debug.core.cdi.model.ICDIStackFrame;
import org.eclipse.cdt.debug.core.cdi.model.ICDIThread;
import org.eclipse.cdt.debug.core.cdi.model.ICDIThreadStorageDescriptor;
import org.eclipse.cdt.debug.core.cdi.model.ICDIVariable;
import org.eclipse.ptp.core.util.BitList;
import org.eclipse.ptp.debug.core.aif.IAIF;
import org.eclipse.ptp.debug.external.cdi.model.StackFrame;
import org.eclipse.ptp.debug.external.cdi.model.Target;
import org.eclipse.ptp.debug.external.cdi.model.Thread;
import org.eclipse.ptp.debug.external.cdi.model.variable.Argument;
import org.eclipse.ptp.debug.external.cdi.model.variable.ArgumentDescriptor;
import org.eclipse.ptp.debug.external.cdi.model.variable.GlobalVariable;
import org.eclipse.ptp.debug.external.cdi.model.variable.GlobalVariableDescriptor;
import org.eclipse.ptp.debug.external.cdi.model.variable.LocalVariable;
import org.eclipse.ptp.debug.external.cdi.model.variable.LocalVariableDescriptor;
import org.eclipse.ptp.debug.external.cdi.model.variable.ThreadStorage;
import org.eclipse.ptp.debug.external.cdi.model.variable.ThreadStorageDescriptor;
import org.eclipse.ptp.debug.external.cdi.model.variable.Variable;
import org.eclipse.ptp.debug.external.cdi.model.variable.VariableDescriptor;
import org.eclipse.ptp.debug.external.commands.GetAIFCommand;
import org.eclipse.ptp.debug.external.commands.ListArgumentsCommand;
import org.eclipse.ptp.debug.external.commands.ListGlobalVariablesCommand;
import org.eclipse.ptp.debug.external.commands.ListLocalVariablesCommand;

public class VariableManager extends Manager {
	static final ICDIVariable[] EMPTY_VARIABLES = {};
	int MAX_STACK_DEPTH = Thread.STACKFRAME_DEFAULT_DEPTH;
	private Map variablesMap;

	public VariableManager(Session session) {
		super(session, true);
		variablesMap = new Hashtable();
	}
	public void shutdown() {
		variablesMap.clear();
	}	
	synchronized List getVariablesList(Target target) {
		List variablesList = (List) variablesMap.get(target);
		if (variablesList == null) {
			variablesList = Collections.synchronizedList(new ArrayList());
			variablesMap.put(target, variablesList);
		}
		return variablesList;
	}
	public Variable getVariable(int task_id, String varName) {
		Target target = (Target)((Session)getSession()).getTarget(task_id);
		return getVariable(target, varName);
	}
	public Variable getVariable(Target target, String varName) {
		Variable[] vars = getVariables(target);
		for (int i = 0; i < vars.length; i++) {
			if (vars[i].getName().equals(varName)) {
				return vars[i];
			}
			Variable v = vars[i].getChild(varName);
			if (v != null) {
				return v;
			}
		}
		return null;
	}
	Variable findVariable(VariableDescriptor v) throws CDIException {
		Target target = (Target)v.getTarget();
		ICDIStackFrame vstack = v.getStackFrame();
		ICDIThread vthread = v.getThread();
		String name = v.getName();
		int position = v.getPosition();
		int depth = v.getStackDepth();
		Variable[] vars = getVariables(target);
		for (int i = 0; i < vars.length; i++) {
			if (vars[i].getName().equals(name) && vars[i].getCastingArrayStart() == v.getCastingArrayStart() && vars[i].getCastingArrayEnd() == v.getCastingArrayEnd() && VariableDescriptor.equalsCasting(vars[i], v)) {
				// check threads
				ICDIThread thread = vars[i].getThread();
				if ((vthread == null && thread == null) || (vthread != null && thread != null && thread.equals(vthread))) {
					// check stackframes
					ICDIStackFrame frame = vars[i].getStackFrame();
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
	Variable[] getVariables(Target target) {
		List variableList = (List)variablesMap.get(target);
		if (variableList != null) {
			return (Variable[]) variableList.toArray(new Variable[variableList.size()]);
		}
		return new Variable[0];
	}
	public void checkType(StackFrame frame, String type) throws CDIException {
		if (type != null && type.length() > 0) {
			Target target = (Target)frame.getTarget();
			Thread currentThread = (Thread)target.getCurrentThread();
			StackFrame currentFrame = currentThread.getCurrentStackFrame();
			target.setCurrentThread(frame.getThread(), false);
			((Thread)frame.getThread()).setCurrentStackFrame(frame, false);

			try {
				//TODO -- dunno how to do here?
				//incorrect type will throw exception
				throw new CDIException("Not implemented yet - VariableManager: checkType");
			} finally {
				target.setCurrentThread(currentThread, false);
				currentThread.setCurrentStackFrame(currentFrame, false);
			}
		} else {
			throw new CDIException("VariableManager Unknown_type");
		}
	}
	void removeVar(Target target, Variable var) throws CDIException {
		//TODO - not implement yet
		//target.getDebugger().deletevar(((Session)getSession()).createBitList(target.getTargetID()), var.getName());
		throw new CDIException("Not implement yet - VariableManager: removeVar");		
	}
	public Variable removeVariableFromList(Target target, String varName) {
		List varList = getVariablesList(target);
		synchronized (varList) {
			for (Iterator iterator = varList.iterator(); iterator.hasNext();) {
				Variable variable = (Variable)iterator.next();
				if (variable.getName().equals(varName)) {
					iterator.remove();
					return variable;
				}
			}
		}
		return null;
	}
	public VariableDescriptor getVariableDescriptorAsArray(VariableDescriptor varDesc, int start, int length) throws CDIException {
		Target target = (Target)varDesc.getTarget();
		Thread thread = (Thread)varDesc.getThread();
		StackFrame frame = (StackFrame)varDesc.getStackFrame();
		String name = varDesc.getName();
		String fullName = varDesc.getFullName();
		int pos = varDesc.getPosition();
		int depth = varDesc.getStackDepth();
		IAIF aif = varDesc.getAIF();
		VariableDescriptor vo = null;

		if (varDesc instanceof ArgumentDescriptor || varDesc instanceof Argument) {
			vo = new ArgumentDescriptor(target, thread, frame, name, fullName, pos, depth, aif);
		} else if (varDesc instanceof LocalVariableDescriptor || varDesc instanceof LocalVariable) {
			vo = new LocalVariableDescriptor(target, thread, frame, name, fullName, pos, depth, aif);
		} else if (varDesc instanceof GlobalVariableDescriptor || varDesc instanceof GlobalVariable) {
			vo = new GlobalVariableDescriptor(target, thread, frame, name, fullName, pos, depth, aif);
		} else if (varDesc instanceof ThreadStorageDescriptor || varDesc instanceof ThreadStorage) {
			vo = new ThreadStorageDescriptor(target, thread, frame, name, fullName, pos, depth, aif);
		} else {
			throw new CDIException("VariableManager.Unknown_variable_object");			
		}
		vo.setCastingArrayStart(varDesc.getCastingArrayStart() + start);
		vo.setCastingArrayEnd(length);
		return vo;
	}
	public VariableDescriptor getVariableDescriptorAsType(VariableDescriptor varDesc, String type) throws CDIException {
		// throw an exception if not a good type.
		Target target = (Target)varDesc.getTarget();
		Thread thread = (Thread)varDesc.getThread();
		StackFrame frame = (StackFrame)varDesc.getStackFrame();
		String name = varDesc.getName();
		String fullName = varDesc.getFullName();
		int pos = varDesc.getPosition();
		int depth = varDesc.getStackDepth();
		IAIF aif = varDesc.getAIF();		

		// Check the type validity.
		{
			StackFrame f = frame;
			if (f == null) {
				if (thread != null) {
					f = thread.getCurrentStackFrame();
				} else {
					Thread t = (Thread)target.getCurrentThread();
					f = t.getCurrentStackFrame();
				}
			}
			checkType(f, type);
		}

		VariableDescriptor vo = null;

		if (varDesc instanceof ArgumentDescriptor || varDesc instanceof Argument) {
			vo = new ArgumentDescriptor(target, thread, frame, name, fullName, pos, depth, aif);
		} else if (varDesc instanceof LocalVariableDescriptor || varDesc instanceof LocalVariable) {
			vo = new LocalVariableDescriptor(target, thread, frame, name, fullName, pos, depth, aif);
		} else if (varDesc instanceof GlobalVariableDescriptor || varDesc instanceof GlobalVariable) {
			vo = new GlobalVariableDescriptor(target, thread, frame, name, fullName, pos, depth, aif);
		} else if (varDesc instanceof ThreadStorageDescriptor || varDesc instanceof ThreadStorage) {
			vo = new ThreadStorageDescriptor(target, thread, frame, name, fullName, pos, depth, aif);
		} else {
			throw new CDIException("VariableManager.Unknown_variable_object");			
		}

		String[] castings = varDesc.getCastingTypes();
		if (castings == null) {
			castings = new String[] { type };
		} else {
			String[] temp = new String[castings.length + 1];
			System.arraycopy(castings, 0, temp, 0, castings.length);
			temp[castings.length] = type;
			castings = temp;
		}
		vo.setCastingTypes(castings);
		return vo;
	}
	public Variable createVariable(VariableDescriptor varDesc) throws CDIException {
		if (varDesc instanceof ArgumentDescriptor) {
			return createArgument((ArgumentDescriptor)varDesc);
		} else if (varDesc instanceof LocalVariableDescriptor) {
			return createLocalVariable((LocalVariableDescriptor)varDesc);
		} else if (varDesc instanceof GlobalVariableDescriptor) {
			return createGlobalVariable((GlobalVariableDescriptor)varDesc);
		} else if (varDesc instanceof ThreadStorageDescriptor) {
			return createThreadStorage((ThreadStorageDescriptor)varDesc);
		}
		throw new CDIException("VariableManager.Unknown_variable_object");			
	}
	public Argument createArgument(ArgumentDescriptor argDesc) throws CDIException {
		Variable variable = findVariable(argDesc);
		Argument argument = null;
		if (variable != null && variable instanceof Argument) {
			argument = (Argument) variable;
		}
		if (argument == null) {
			//String name = argDesc.getQualifiedName();
			StackFrame stack = (StackFrame)argDesc.getStackFrame();
			Target target = (Target)argDesc.getTarget();
			Thread currentThread = (Thread)target.getCurrentThread();
			StackFrame currentFrame = currentThread.getCurrentStackFrame();
			target.setCurrentThread(stack.getThread(), false);
			((Thread)stack.getThread()).setCurrentStackFrame(stack, false);
			try {
				argument = new Argument(argDesc);
				List variablesList = getVariablesList(target);
				if (!variablesList.contains(argument))
					variablesList.add(argument);

				/*
				ICDIArgument[] args = target.getDebugger().listArguments(((Session)getSession()).createBitList(target.getTargetID()), currentFrame);
				for (int i=0; i<args.length; i++) {
					if (name.equals(args[i].getQualifiedName())) {
						argument = new Argument(argDesc);
						List variablesList = getVariablesList(target);
						variablesList.add(argument);
					}	
				}
				*/
			} finally {
				target.setCurrentThread(currentThread, false);
				currentThread.setCurrentStackFrame(currentFrame, false);
			}
		}
		return argument;
	}
	public ICDIArgumentDescriptor[] getArgumentDescriptors(StackFrame frame) throws CDIException {
		List argObjects = new ArrayList();
		Target target = (Target)frame.getTarget();
		Thread currentThread = (Thread)target.getCurrentThread();
		StackFrame currentFrame = currentThread.getCurrentStackFrame();
		target.setCurrentThread(frame.getThread(), false);
		((Thread)frame.getThread()).setCurrentStackFrame(frame, false);
		try {
			Session session = (Session)getSession();
			BitList tasks = session.createBitList(target.getTargetID());
			ListArgumentsCommand argCmd = new ListArgumentsCommand(tasks, frame);
			session.getDebugger().postCommand(argCmd);
			ICDIArgument[] args = argCmd.getArguments();
			if (args != null) {
				for (int i = 0; i < args.length; i++) {
					VariableDescriptor varDesc = (VariableDescriptor) args[i];
					Thread thread = (Thread)varDesc.getThread();
					String name = varDesc.getName();
					String fName = varDesc.getQualifiedName();
					int pos = varDesc.getPosition();
					int depth = varDesc.getStackDepth();
					IAIF aif = varDesc.getAIF();
					if (aif == null) {
						GetAIFCommand aifCmd = new GetAIFCommand(tasks, fName);
						session.getDebugger().postCommandAndWait(aifCmd);
						aif = aifCmd.getAIF();
					}
					argObjects.add(new ArgumentDescriptor(target, thread, frame, name, fName, pos, depth, aif));
				}
			}
		} finally {
			target.setCurrentThread(currentThread, false);
			currentThread.setCurrentStackFrame(currentFrame, false);
		}
		return (ICDIArgumentDescriptor[]) argObjects.toArray(new ICDIArgumentDescriptor[0]);
	}
	public GlobalVariableDescriptor getGlobalVariableDescriptor(Target target, String filename, String function, String name) throws CDIException {
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
		//TODO - put null for AIF
		return new GlobalVariableDescriptor(target, null, null, buffer.toString(), null, 0, 0, null);
	}
	public GlobalVariable createGlobalVariable(GlobalVariableDescriptor varDesc) throws CDIException {
		Variable variable = findVariable(varDesc);
		GlobalVariable global = null;
		if (variable instanceof GlobalVariable) {
			global = (GlobalVariable)variable;
		}
		if (global == null) {
			String name = varDesc.getQualifiedName();
			Target target = (Target)varDesc.getTarget();
			Session session = (Session)getSession();
			ListGlobalVariablesCommand varCmd = new ListGlobalVariablesCommand(session.createBitList(target.getTargetID()));
			session.getDebugger().postCommand(varCmd);
			ICDIGlobalVariable[] vars = varCmd.getGlobalVariables();
			System.out.println(" ++++++++++++++++ listGlobalVariables: " + vars.length + " ++++++++++++++++");
			for (int i = 0; i < vars.length; i++) {
				if (name.equals(vars[i].getQualifiedName())) {
					global = new GlobalVariable(varDesc);
					List variablesList = getVariablesList(target);
					variablesList.add(global);
					break;
				}
			}
		}
		return global;
	}
	public ICDILocalVariableDescriptor[] getLocalVariableDescriptors(StackFrame frame) throws CDIException {
		List varObjects = new ArrayList();
		Target target = (Target)frame.getTarget();
		Thread currentThread = (Thread)target.getCurrentThread();
		StackFrame currentFrame = currentThread.getCurrentStackFrame();
		target.setCurrentThread(frame.getThread(), false);
		((Thread)frame.getThread()).setCurrentStackFrame(frame, false);
		
		try {
			Session session = (Session)getSession();
			BitList tasks = session.createBitList(target.getTargetID());
			ListLocalVariablesCommand varCmd = new ListLocalVariablesCommand(tasks, currentFrame);
			session.getDebugger().postCommand(varCmd);
			ICDILocalVariable[] vars = varCmd.getLocalVariables();
			if (vars != null) {
				for (int i = 0; i < vars.length; i++) {
					VariableDescriptor varDesc = (VariableDescriptor)vars[i];
					Thread thread = (Thread)varDesc.getThread();
					String name = varDesc.getName();
					String fName = varDesc.getQualifiedName();
					int pos = varDesc.getPosition();
					int depth = varDesc.getStackDepth();
					IAIF aif = varDesc.getAIF();
					if (aif == null) {
						GetAIFCommand aifCmd = new GetAIFCommand(tasks, fName);
						session.getDebugger().postCommandAndWait(aifCmd);
						aif = aifCmd.getAIF();
					}
					varObjects.add(new LocalVariableDescriptor(target, thread, frame, name, fName, pos, depth, aif));
				}
			}
		} finally {
			target.setCurrentThread(currentThread, false);
			currentThread.setCurrentStackFrame(currentFrame, false);
		}
		return (ICDILocalVariableDescriptor[]) varObjects.toArray(new ICDILocalVariableDescriptor[0]);
	}
	public LocalVariable createLocalVariable(LocalVariableDescriptor varDesc) throws CDIException {
		Variable variable = findVariable(varDesc);
		LocalVariable local = null;
		if (variable instanceof LocalVariable) {
			local = (LocalVariable)variable;
		}
		if (local == null) {
			//String name = varDesc.getQualifiedName();
			StackFrame stack = (StackFrame)varDesc.getStackFrame();
			Target target = (Target)varDesc.getTarget();
			Thread currentThread = (Thread)target.getCurrentThread();
			StackFrame currentFrame = currentThread.getCurrentStackFrame();
			target.setCurrentThread(stack.getThread(), false);
			((Thread)stack.getThread()).setCurrentStackFrame(stack, false);
			try {
				local = new LocalVariable(varDesc);
				List variablesList = getVariablesList(target);
				if (!variablesList.contains(local))
					variablesList.add(local);

				/*
				ICDILocalVariable[] vars = target.getDebugger().listLocalVariables(((Session)getSession()).createBitList(target.getTargetID()), currentFrame);
				for (int i = 0; i < vars.length; i++) {
					if (name.equals(vars[i].getQualifiedName())) {
						local = new LocalVariable(varDesc);
						List variablesList = getVariablesList(target);
						variablesList.add(local);
					}
				}
				*/
			} finally {
				target.setCurrentThread(currentThread, false);
				currentThread.setCurrentStackFrame(currentFrame, false);
			}
		}
		return local;
	}
	public ICDIThreadStorageDescriptor[] getThreadStorageDescriptors(Thread thread) throws CDIException {
		return new ICDIThreadStorageDescriptor[0];
	}
	public ThreadStorage createThreadStorage(ThreadStorageDescriptor desc) throws CDIException {
		throw new CDIException("cdi.VariableManager.Unknown_variable_object");
	}
	public void destroyVariable(Variable variable) throws CDIException {
		Target target = (Target)variable.getTarget();
		List varList = getVariablesList(target);
		if (varList.contains(variable)) {
			removeVar(target, variable);
		}
		//TODO --fire var deleted event?? 
	}
	public void destroyAllVariables(Target target) throws CDIException {
		Variable[] variables = getVariables(target);
		for (int i = 0; i < variables.length; ++i) {
			removeVar(target, variables[i]);
			//TODO --fire var deleted event?? 
		}
	}

	public void update(Target target) throws CDIException {
		int highLevel = 0;
		int lowLevel = 0;
		List eventList = new ArrayList();
		Variable[] vars = getVariables(target);
		ICDIStackFrame[] frames = null;
		StackFrame currentStack = null;
		Thread currentThread = (Thread)target.getCurrentThread();
		if (currentThread != null) {
			currentStack = currentThread.getCurrentStackFrame();
			if (currentStack != null) {
				highLevel = currentStack.getLevel();
			}
			if (highLevel > MAX_STACK_DEPTH) {
				highLevel = MAX_STACK_DEPTH;
			}
			lowLevel = highLevel - MAX_STACK_DEPTH;
			if (lowLevel < 0) {
				lowLevel = 0;
			}
			frames = currentThread.getStackFrames(0, highLevel);
		}
		for (int i = 0; i < vars.length; i++) {
			Variable variable = vars[i];
			if (isVariableNeedsToBeUpdate(variable, currentStack, frames, lowLevel)) {
				//String varName = variable.getName();
				//TODO how to implement ?
				variable.setUpdated(true);
			} else {
				variable.setUpdated(false);
			}
		}
		ICDIEvent[] events = (ICDIEvent[]) eventList.toArray(new ICDIEvent[0]);
		((EventManager)((Session)getSession()).getEventManager()).fireEvents(events);		
		//throw new CDIException("Not implement yet - VariableManager: update");
	}
	public void update(Variable variable) throws CDIException {
		Target target = (Target)variable.getTarget();
		List eventList = new ArrayList();
		update(target, variable, eventList);
		ICDIEvent[] events = (ICDIEvent[]) eventList.toArray(new ICDIEvent[0]);
		((EventManager)((Session)getSession()).getEventManager()).fireEvents(events);		
	}
	public void update(Target target, Variable variable, List eventList) throws CDIException {
		//String varName = variable.getName();
		//TODO how to implement ?
		//variable.setUpdated(true);
		throw new CDIException("Not implement yet - VariableManager: update");
	}
	boolean isVariableNeedsToBeUpdate(Variable variable, ICDIStackFrame current, ICDIStackFrame[] frames, int lowLevel) throws CDIException {
		ICDIStackFrame varStack = variable.getStackFrame();
		boolean inScope = false;

		// Something wrong and the program terminated bail out here.
		if (current == null || frames == null) {
			return false;
		}

		// If the variable Stack is null, it means this is a global variable we should update
		if (varStack == null) {
			return true;
		} else if (varStack.equals(current)) {
			// The variable is in the current selected frame it should be updated
			return true;
		} else {
			if (varStack.getLevel() >= lowLevel) {
				for (int i = 0; i < frames.length; i++) {
					if (varStack.equals(frames[i])) {
						inScope = true;
					}
				}
			} else {
				inScope = true;
			}
		}
		return !inScope;
	}
}
