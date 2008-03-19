package org.eclipse.ptp.pldt.sampleCDTstaticAnalysis.handlers;

import java.util.Iterator;

import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ptp.pldt.mpi.analysis.cdt.graphs.GraphCreator;
import org.eclipse.ptp.pldt.mpi.analysis.cdt.graphs.IBlock;
import org.eclipse.ptp.pldt.mpi.analysis.cdt.graphs.ICallGraph;
import org.eclipse.ptp.pldt.mpi.analysis.cdt.graphs.ICallGraphNode;
import org.eclipse.ptp.pldt.mpi.analysis.cdt.graphs.IControlFlowGraph;
import org.eclipse.ptp.pldt.mpi.analysis.cdt.graphs.impl.ControlFlowGraph;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Build Control Flow Graph
 * 
 * @author Beth Tibbitts  tibbitts@us.ibm.com
 *  
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 * @see org.eclipse.ptp.pldt.mpi.analysis.cdt.graphs.GraphCreator
 */
public class BuildCFGraphHandler extends AbstractHandler implements
		ISelectionListener {
	private IStructuredSelection selection;
	protected ICallGraph callGraph;
	protected GraphCreator graphCreator;
	protected IWorkbenchWindow window;

	/**
	 * The constructor.
	 */
	public BuildCFGraphHandler() {
		graphCreator = new GraphCreator();

	}

	/**
	 * Execute the command: extract extract the needed information from the
	 * application context. <br>
	 * A new call graph is constructed for the selected resources.
	 * 
	 * @param event
	 * @return the result of the execution. Reserved for future use by IHandler
	 *         interface, must be <code>null</code>.
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
		window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		System.out.println("BuildCFGraphHandler.execute()");
		// create an empty call graph to begin with
		callGraph = graphCreator.initCallGraph();
		// Get the current selection
		if (selection == null) {
			ISelection sel = HandlerUtil.getCurrentSelection(event);
			if (sel instanceof IStructuredSelection) {
				selection = (IStructuredSelection) sel;
			}
			if (selection == null) {
				message("No selection detected. Please select a file, folder, or project in the Projects View.");
				selection=null;
				return null;
			}
		}

		// We iterate here only to handle the possibility of multiple-selection.
		// graphCreator will descend into child nodes.
		/*final*/ IResource res=null;
		for (Iterator iter = selection.iterator(); iter.hasNext();) {
			Object obj = (Object) iter.next();
			// It can be a Project, Folder, File, etc...
			if (obj instanceof IAdaptable) {
				IAdaptable iad = (IAdaptable) obj;
				res = (IResource) iad.getAdapter(IResource.class);
				// Note: put this in a Job or WorkspaceModifyOperation if resources
				// will be changed.
				if (res != null) {
					callGraph = graphCreator.initCallGraph(res);
				} else {
					message("Please select a file, folder, or project in the Projects view");
					selection=null;
					return null;
				}
			}
		}

		graphCreator.computeCallGraph(callGraph);
		// get the first node
		ICallGraphNode topNode = callGraph.topEntry();
		String topName=topNode.getFuncName();
		
		ICallGraphNode firstNode=callGraph.getAllNodes().get(0);
		String firstName=firstNode.getFuncName();
		// get a node by function name
		ICallGraphNode namedNode = callGraph.getNode(res.getName(), "foo");
		String namedName="";
		if(namedNode!=null) {
			namedName=namedNode.getFuncName();
		}
		
		ICallGraphNode node = topNode;
		IASTStatement funcBody=node.getFuncDef().getBody();
		IControlFlowGraph cfg = new ControlFlowGraph(funcBody);
		cfg.buildCFG();
		
		IBlock entryBlock = cfg.getEntry();
		for (IBlock block= cfg.getEntry(); block!=null; block = block.getTopNext()) {
			block.print();
		}
		return null;
	}

	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			this.selection = (IStructuredSelection) selection;
			System.out.println("BuildCallGraphHandler got selection");
		}

	}

	public void message(String msg) {
		MessageDialog.openInformation(window.getShell(),
				"Houston, we have a problem", msg);
	}
}
