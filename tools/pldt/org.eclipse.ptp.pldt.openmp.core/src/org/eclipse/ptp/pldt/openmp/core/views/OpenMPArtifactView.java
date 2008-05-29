package org.eclipse.ptp.pldt.openmp.core.views;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ptp.pldt.openmp.analysis.PAST.PASTOMPPragma;
import org.eclipse.ptp.pldt.common.Artifact;
import org.eclipse.ptp.pldt.common.ArtifactManager;
import org.eclipse.ptp.pldt.common.views.SimpleTableMarkerView;
import org.eclipse.ptp.pldt.openmp.core.OpenMPPlugin;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.parser.ParserUtil;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.ui.util.EditorUtility;
import org.eclipse.ui.texteditor.AbstractTextEditor;

/**
 * An OpenMP artifact view based on SimpleTableView <br>
 * Note that the ID must be unique.
 * 
 */
public class OpenMPArtifactView extends SimpleTableMarkerView {
	public OpenMPArtifactView() {
		// if you need to read icon images, etc. from the plug-in, be sure to
		// pass in an actual Plugin class instance for first arg
		// super(OpenMPPlugin.getDefault(), "OpenMP Artifact", "OpenMP Artifacts", "Construct", OpenMPPlugin.MARKER_ID);
		super(
				OpenMPPlugin.getDefault(),
				OpenMPPlugin.getResourceString("OpenMPArtifactView.thingname"),
				OpenMPPlugin.getResourceString("OpenMPArtifactView.thingnames"),
				OpenMPPlugin.getResourceString("OpenMPArtifactView.extraColName"),
				OpenMPPlugin.MARKER_ID);

	}

	/**
	 * Provide custom info for filling in the last column
	 */
	protected String getConstructStr(IMarker marker) throws CoreException {
		Integer temp = (Integer) marker.getAttribute(columnID_);
		if (temp != null) {
			Integer constructType = (Integer) temp;
			int i = constructType.intValue();
			String val;// = CONSTRUCT_TYPE_NAMES[i];
			val = "value is " + i; // BRT need a more robust lookup
			if (i == 0)
				val = "OpenMP Pragma";
			if (i == 1)
				val = "Function Call";

			return val;
		} else
			return " ";
	}
    

    /**
     * Make "show info" action to display artifact information
     */
    protected void makeShowInfoAction()
    {
        infoAction = new Action() {

            /* (non-Javadoc)
             * @see org.eclipse.jface.action.IAction#run()
             */
            @SuppressWarnings("restriction")
            public void run()
            {
                ISelection selection = viewer.getSelection();
                Object obj = ((IStructuredSelection) selection).getFirstElement();
                IMarker marker = (IMarker) obj;
                if(marker==null){
                	MessageDialog.openInformation(null, "No selection", "No artifact selected");
                	return;
                }

                try {
                    //Object o = artifactManager_.getArtifact((String)marker.getAttribute(IDs.ID));
                    Object o = ArtifactManager.getArtifact(marker);
                    if (o==null || !(o instanceof Artifact))  return;
                    Artifact artifact = (Artifact)o;
                    Object p = artifact.getArtifactAssist();
                    if (p==null || !(p instanceof PASTOMPPragma))  return;
                    PASTOMPPragma ompPragma = (PASTOMPPragma)p;
                    IASTNode      iRegion    = ompPragma.getRegion();
                    ASTNode       region     = (iRegion instanceof ASTNode ? (ASTNode)iRegion : null);
                    if (region==null)  return;
                    
                    // determine if we collected location information for this omp pragma
                    String filename = ompPragma.getRegionFilename(); //region.getContainingFilename(); 
                    if (filename==null)  return;
                    
                    IResource r = ParserUtil.getResourceForFilename(filename);
                    
                    IEditorPart aPart = null;
                    if (r!=null) {
                        try{
                            aPart = EditorUtility.openInEditor(r);
                        }
                        catch(PartInitException pie) { return; }
                        catch(CModelException e)     { return; }
                    }
                    else {
                        return;
                    }
                    
                    if (aPart instanceof AbstractTextEditor) 
                    {
                        int offset = ompPragma.getRegionOffset(); //ifl.getNodeOffset();  //locs[0].getNodeOffset();
                        int length = ompPragma.getRegionLength(); //ifl.getNodeLength();  //region.getLength(); 
                         ((AbstractTextEditor)aPart).selectAndReveal( offset, length);
                    }  
                     
                    
                } catch (Exception e) {
                    System.out.println("ATV.doubleclickAction: Error positioning editor page from marker line number");
                    //showStatusMessage("Error positioning editor from marker line number", "error marker goto");
                    e.printStackTrace();
                }
                 
            }
        };
        infoAction.setText("Show pragma region");
        infoAction.setToolTipText("Show region for selected " + thingname_);
        infoAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(
                ISharedImages.IMG_OBJS_INFO_TSK));

    }

}
