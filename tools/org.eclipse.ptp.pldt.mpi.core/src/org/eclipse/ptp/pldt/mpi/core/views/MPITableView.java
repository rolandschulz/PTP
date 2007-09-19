package org.eclipse.ptp.pldt.mpi.core.views;

import org.eclipse.ptp.pldt.common.views.SimpleTableMarkerView;
import org.eclipse.ptp.pldt.mpi.core.MpiIDs;
import org.eclipse.ptp.pldt.mpi.core.MpiPlugin;

/**
 * An MPI artifact view based on SimpleTableView <br>
 * Note that the ID must be unique.
 * <p>
 * If you need to read icon images, etc. from the plug-in, be sure to pass in an
 * actual Plugin class instance for first arg
 */
public class MPITableView extends SimpleTableMarkerView {
	public MPITableView() {

		super(MpiPlugin.getDefault(), "Artifact", "Artifacts",
				"Construct", MpiIDs.MARKER_ID);
	}
}
