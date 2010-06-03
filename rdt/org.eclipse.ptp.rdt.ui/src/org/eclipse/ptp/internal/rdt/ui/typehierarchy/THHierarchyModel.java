/*******************************************************************************
 * Copyright (c) 2007, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *    IBM Corporation
 *******************************************************************************/ 

/* -- ST-Origin --
 * Source folder: org.eclipse.cdt.ui/src
 * Class: org.eclipse.cdt.internal.ui.typehierarchy.THHierarchyModel
 * Version: 1.16
 */
package org.eclipse.ptp.internal.rdt.ui.typehierarchy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IFunctionDeclaration;
import org.eclipse.cdt.internal.ui.typehierarchy.ITHModelPresenter;
import org.eclipse.cdt.internal.ui.typehierarchy.Messages;
import org.eclipse.cdt.internal.ui.typehierarchy.THNode;
import org.eclipse.cdt.internal.ui.typehierarchy.THSchedulingRule;
import org.eclipse.cdt.internal.ui.viewsupport.IndexUI;
import org.eclipse.cdt.internal.ui.viewsupport.WorkingSetFilterUI;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ptp.internal.rdt.core.model.Scope;
import org.eclipse.ptp.internal.rdt.core.typehierarchy.ITypeHierarchyService;
import org.eclipse.ptp.internal.rdt.core.typehierarchy.THGraph;
import org.eclipse.ptp.internal.rdt.core.typehierarchy.THGraphEdge;
import org.eclipse.ptp.internal.rdt.core.typehierarchy.THGraphNode;
import org.eclipse.ptp.rdt.core.serviceproviders.IIndexServiceProvider;
import org.eclipse.ptp.rdt.core.services.IRDTServiceConstants;
import org.eclipse.ptp.services.core.IService;
import org.eclipse.ptp.services.core.IServiceConfiguration;
import org.eclipse.ptp.services.core.IServiceModelManager;
import org.eclipse.ptp.services.core.IServiceProvider;
import org.eclipse.ptp.services.core.ServiceModelManager;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.progress.IWorkbenchSiteProgressService;

public class THHierarchyModel {
    public class BackgroundJob extends Job {
		public BackgroundJob() {
			super(Messages.THHierarchyModel_Job_title);
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			return onComputeGraph(this, monitor);
		}
	}
    
	static final int TYPE_HIERARCHY = 0;
    static final int SUB_TYPE_HIERARCHY = 1;
    static final int SUPER_TYPE_HIERARCHY = 2;

	static final int END_OF_COMPUTATION = 0;
	
	private static final ISchedulingRule RULE = new THSchedulingRule();
	private static final Object[] NO_CHILDREN= new Object[0];

	private ICElement fInput;
	private int fHierarchyKind;
	private boolean fShowInheritedMembers;
	
	private THGraph fGraph;
	private THNode[] fRootNodes;
	private THNode fSelectedTypeNode;
	private ICElement fTypeToSelect;
	private ICElement fSelectedMember;
	private String fMemberSignatureToSelect;
	
	private Job fJob;
	private Display fDisplay;
	private ITHModelPresenter fView;
	private WorkingSetFilterUI fFilter;
	
	public THHierarchyModel(ITHModelPresenter view, Display display) {
		fDisplay= display;
		fView= view;
	}
	
	public ICElement getInput() {
		return fInput;
	}

	public int getHierarchyKind() {
		return fHierarchyKind;
	}

	public void setHierarchyKind(int hierarchyKind) {
		fHierarchyKind = hierarchyKind;
		computeNodes();
	}

	public boolean isShowInheritedMembers() {
		return fShowInheritedMembers;
	}

	public void setShowInheritedMembers(boolean showInheritedMembers) {
		fShowInheritedMembers = showInheritedMembers;
		updateSelectedMember();
		updateImplementors();
	}

	public Object[] getHierarchyRootElements() {
		if (fRootNodes == null) {
			return new Object[] {"..."}; //$NON-NLS-1$
		}
		return fRootNodes;
	}

	public void setWorkingSetFilter(WorkingSetFilterUI filterUI) {
		fFilter= filterUI;
		computeNodes();
	}

	synchronized public void setInput(ICElement input, ICElement member) {
		stopGraphComputation();
		fInput= input;
		fSelectedMember= member;
		fMemberSignatureToSelect= getLocalElementSignature(fSelectedMember);
		fRootNodes= null;
		fSelectedTypeNode= null;
		fTypeToSelect= input;
	}

	synchronized public void computeGraph() {
		if (fJob != null) {
			fJob.cancel();
		}
		fJob= new BackgroundJob();
		fJob.setRule(RULE);
		IWorkbenchSiteProgressService ps= fView.getProgressService();
		if (ps != null) {
			ps.schedule(fJob, 0L, true);
		}
		else {
			fJob.schedule();
		}
	}

	synchronized public void stopGraphComputation() {
		if (fJob != null) {
			fJob.cancel();
		}
		fJob= null;
	}

	protected IStatus onComputeGraph(Job job, IProgressMonitor monitor) {
		THGraph graph = null;
		try {
			IProject project = fInput.getCProject().getProject();
			IServiceModelManager smm = ServiceModelManager.getInstance();
			IServiceConfiguration serviceConfig = smm.getActiveConfiguration(project);

			IService indexingService = smm.getService(IRDTServiceConstants.SERVICE_C_INDEX);

			IServiceProvider serviceProvider = serviceConfig.getServiceProvider(indexingService);

			if (serviceProvider instanceof IIndexServiceProvider) {
				ITypeHierarchyService service = ((IIndexServiceProvider) serviceProvider).getTypeHierarchyService();
				graph = service.computeGraph(Scope.WORKSPACE_ROOT_SCOPE, fInput, monitor);
			}
			if (monitor.isCanceled()) {
				return Status.CANCEL_STATUS;
			}
		} catch (CoreException e) {
			CUIPlugin.log(e);
		} catch (InterruptedException e) {
			return Status.CANCEL_STATUS;
		}
		finally {
			onJobDone(graph, job);
		}			
		return Status.OK_STATUS;
	}
	
	
	protected void computeNodes() {
		if (fGraph == null) {
			return;
		}
		boolean fwd= fHierarchyKind == SUPER_TYPE_HIERARCHY;
		ArrayList<THNode> stack= new ArrayList<THNode>();
		ArrayList<THNode> roots= new ArrayList<THNode>();
		ArrayList<THNode> leafs= new ArrayList<THNode>();
		
		THGraphNode inputNode= fGraph.getInputNode();
		Collection<THGraphNode> groots;
		
		if (fHierarchyKind == TYPE_HIERARCHY) {
			groots= fGraph.getLeaveNodes();
		}
		else {
			THGraphNode node= fGraph.getInputNode();
			if (node != null) {
				groots= Collections.singleton(node);
			}
			else {
				groots= Collections.emptySet();
			}
		}
		
		for (Iterator<THGraphNode> iterator = groots.iterator(); iterator.hasNext();) {
			THGraphNode gnode = iterator.next();
			THNode node = createNode(null, gnode, inputNode);
			roots.add(node);
			stack.add(node);
		}
		
		while(!stack.isEmpty()) {
			THNode node= stack.remove(stack.size()-1);
			THGraphNode gnode= fGraph.getNode(node.getElement());
			List<THGraphEdge> edges= fwd ? gnode.getOutgoing() : gnode.getIncoming();
			if (edges.isEmpty()) {
				leafs.add(node);
			}
			else {
				for (Iterator<THGraphEdge> iterator = edges.iterator(); iterator.hasNext();) {
					THGraphEdge edge = iterator.next();
					THGraphNode gchildNode= fwd ? edge.getEndNode() : edge.getStartNode();
					THNode childNode= createNode(node, gchildNode, inputNode);
					node.addChild(childNode);
					stack.add(childNode);
				}
			}
		}
		fRootNodes= roots.toArray(new THNode[roots.size()]);
		removeFilteredLeafs(fRootNodes);
		fSelectedTypeNode= findSelection(fRootNodes);
		if (fSelectedTypeNode != null) {
			fTypeToSelect= fSelectedTypeNode.getElement();
			updateSelectedMember();
		}
		
		updateImplementors();
	}

	private void removeFilteredLeafs(THNode[] rootNodes) {
		for (int i = 0; i < rootNodes.length; i++) {
			THNode node = rootNodes[i];
			node.removeFilteredLeafs();
		}
	}

	private THNode findSelection(THNode[] searchme) {
		THNode[] result= new THNode[2];
		findSelection(searchme, result);
		if (result[0] != null) {
			return result[0];
		}
		return result[1];
	}

	private void findSelection(THNode[] seachme, THNode[] result) {
		for (int i = 0; i < seachme.length; i++) {
			findSelection(seachme[i], result);
			if (result[0] != null) {
				break;
			}
		}
	}

	private void findSelection(THNode node, THNode[] result) {
		if (node.equals(fSelectedTypeNode)) {
			result[0]= node;
			return;
		}
		else if (result[1] == null) {
			if (node.getElement().equals(fTypeToSelect)) {
				result[1]= node;
			}
		}
		THNode[] children= node.getChildren();
		findSelection(children, result);
	}

	private void updateSelectedMember() {
		ICElement oldSelection= fSelectedMember;
		fSelectedMember= null;
		if (fSelectedTypeNode != null && fMemberSignatureToSelect != null) {
			THGraphNode gnode= fGraph.getNode(fSelectedTypeNode.getElement());
			if (gnode != null) {
				ICElement[] members= gnode.getMembers(fShowInheritedMembers);
				if (members != null) {
					for (int i = 0; i < members.length; i++) {
						ICElement member= members[i];
						if (member.equals(oldSelection)) {
							fSelectedMember= member;
							return;
						}
					}
					for (int i = 0; i < members.length; i++) {
						ICElement member= members[i];
						if (fMemberSignatureToSelect.equals(getLocalElementSignature(member))) {
							fSelectedMember= member;
							return;
						}
					}
				}
			}
		}	
	}

	private THNode createNode(THNode parent, THGraphNode gnode, THGraphNode inputNode) {
		ICElement element = gnode.getElement();
		THNode node= new THNode(parent, element);
		if (gnode != inputNode && fFilter != null && !fFilter.isPartOfWorkingSet(element)) {
			node.setIsFiltered(true);
		}
		return node;
	}

	synchronized private void onJobDone(final THGraph graph, Job job) {
		if (fJob == job) {
			fJob= null;
			fDisplay.asyncExec(new Runnable(){
				public void run() {
					fGraph= graph;
					THGraphNode inputNode= fGraph.getInputNode();
					if (!fGraph.isFileIndexed()) {
						fView.setMessage(IndexUI.getFileNotIndexedMessage(fInput));
					}
					else if (inputNode == null) {
						fView.setMessage(Messages.THHierarchyModel_errorComputingHierarchy);
					}
					else {
						if (fTypeToSelect == fInput) {
							fTypeToSelect= inputNode.getElement();
						}
						fInput= inputNode.getElement();
					}
					computeNodes();
					notifyEvent(END_OF_COMPUTATION);
				}
			});
		}
	}

	private void notifyEvent(int event) {
		fView.onEvent(event);
	}

	synchronized public void refresh() {
		computeGraph();
	}

	public boolean isComputed() {
		return fRootNodes!=null;
	}

	public THNode getSelectionInHierarchy() {
		return fSelectedTypeNode;
	}

	public void onHierarchySelectionChanged(THNode node) {
		fSelectedTypeNode= node;
		if (node != null) {
			fTypeToSelect= node.getElement();
		}
		updateSelectedMember();
		updateImplementors();
	}

	public Object[] getMembers() {
		if (fSelectedTypeNode != null) {
			THGraphNode gnode= fGraph.getNode(fSelectedTypeNode.getElement());
			Object[] result= gnode.getMembers(fShowInheritedMembers);
			if (result != null) {
				return result;
			}
		}
		return NO_CHILDREN;
	}

	public void onMemberSelectionChanged(ICElement elem) {
		fSelectedMember= elem;
		if (fSelectedMember != null) {
			fMemberSignatureToSelect= getLocalElementSignature(fSelectedMember);
		}
		updateImplementors();
	}

	private void updateImplementors() {
		if (fRootNodes != null) {
			for (int i = 0; i < fRootNodes.length; i++) {
				THNode node = fRootNodes[i];
				updateImplementors(node);
			}
		}
	}

	private void updateImplementors(THNode node) {
		node.setIsImplementor(isImplementor(node.getElement()));
		THNode[] children= node.getChildren();
		for (int i = 0; i < children.length; i++) {
			THNode child = children[i];
			updateImplementors(child);
		}
	}

	private boolean isImplementor(ICElement element) {
		if (element == null 
				|| fSelectedMember == null || fMemberSignatureToSelect == null) {
			return false;
		}
		THGraphNode gnode= fGraph.getNode(element);
		if (gnode != null) {
			ICElement[] members= gnode.getMembers(false);
			if (members != null) {
				for (int i = 0; i < members.length; i++) {
					ICElement member = members[i];
					if (member == fSelectedMember) {
						return true;
					}
					if (fMemberSignatureToSelect.equals(getLocalElementSignature(member))) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public ICElement getSelectedMember() {
		return fSelectedMember;
	}

	public boolean hasTrivialHierarchy() {
		if (fRootNodes == null || fRootNodes.length == 0) {
			return true;
		}
		return fRootNodes.length == 1 && !fRootNodes[0].hasChildren();
	}
	
	String getLocalElementSignature(ICElement element) {
		if (element != null) {
			try {
				switch (element.getElementType()) {
				case ICElement.C_METHOD:
				case ICElement.C_METHOD_DECLARATION:
					return ((IFunctionDeclaration) element).getSignature();
				case ICElement.C_FIELD:
					return element.getElementName();
				}
			} catch (CModelException e) {
				CUIPlugin.log(e);
			}
		}
		return null;
	}
}
