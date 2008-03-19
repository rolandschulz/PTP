package org.eclipse.ptp.pldt.sampleCDTstaticAnalysis.handlers;

import java.util.Iterator;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ptp.pldt.mpi.analysis.cdt.graphs.GraphCreator;
import org.eclipse.ptp.pldt.mpi.analysis.cdt.graphs.ICallGraph;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * This handler extends AbstractHandler, an IHandler base class. This handler
 * builds a call graph for the selected resource(s) in the Project Explorer view. <br>
 * 
 * Two steps to building call graph: <br>
 * 1. collect all function definitions in the call graph <br>
 * 2. construct the caller and callee relationship among the CallGraphNodes
 * <br>Most of the work is done by GraphCreator
 * 
 * @author Beth Tibbitts tibbitts@us.ibm.com
 * 
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 * @see org.eclipse.ptp.pldt.mpi.analysis.cdt.graphs.GraphCreator
 */
public class BuildCallGraphHandler extends AbstractHandler implements
		ISelectionListener {
	private IStructuredSelection selection;
	protected ICallGraph callGraph;
	protected GraphCreator graphCreator;
	protected IWorkbenchWindow window;

	/**
	 * The constructor.
	 */
	public BuildCallGraphHandler() {
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
		System.out.println("BuildCallGraphHandler.execute()");
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
		for (Iterator iter = selection.iterator(); iter.hasNext();) {
			Object obj = (Object) iter.next();
			// It can be a Project, Folder, File, etc...
			if (obj instanceof IAdaptable) {
				IAdaptable iad = (IAdaptable) obj;
				final IResource res = (IResource) iad.getAdapter(IResource.class);
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
		System.out.println("resources scanned. Now search for callers/callees");
		graphCreator.computeCallGraph(callGraph);
		System.out.println("showCallGraph...");
		graphCreator.showCallGraph(callGraph);
		System.out.println("showCallGraph complete.");
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
