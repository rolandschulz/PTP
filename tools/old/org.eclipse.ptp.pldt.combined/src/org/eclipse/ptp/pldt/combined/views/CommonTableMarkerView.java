package org.eclipse.ptp.pldt.combined.views;

import org.eclipse.ptp.pldt.common.CommonPlugin;
import org.eclipse.ptp.pldt.common.views.SimpleTableMarkerView;

/**
 * An MPI artifact view based on SimpleTableView <br>
 * Note that the ID must be unique.
 * <p>
 * If you need to read icon images, etc. from the plug-in, be sure to pass in an
 * actual Plugin class instance for first arg
 */
public class CommonTableMarkerView extends SimpleTableMarkerView {
	// FIXME need to use ids from other plugins.
	// but in that case, this view belongs in another plugin that
	// can reference the other projects.
	final String MPI_MARKER_ID       = "org.eclipse.ptp.pldt.mpi.core.mpiMarker";
	final String LAPI_MARKER_ID      = "org.eclipse.ptp.pldt.openmp.core.openMPMarker";
	final String OPENMP_MARKER_ID    = "org.eclipse.ptp.pldt.openmp.core.openMPMarker";
	final String[] markers = {MPI_MARKER_ID, OPENMP_MARKER_ID, LAPI_MARKER_ID};
	final String[] m = {"org.eclipse.ptp.pldt.mpi.core.mpiMarker", "org.eclipse.ptp.pldt.openmp.core.openMPMarker", "org.eclipse.ptp.pldt.openmp.core.openMPMarker"};
	  
	public CommonTableMarkerView() {

		super(CommonPlugin.getDefault(), "MPI Artifact", "MPI Artifacts", "Construct", 
				new String[]{"org.eclipse.ptp.pldt.mpi.core.mpiMarker", "org.eclipse.ptp.pldt.openmp.core.openMPMarker", "org.eclipse.ptp.pldt.openmp.core.openMPMarker"} 
				  );
	}
}
