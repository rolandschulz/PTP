package org.eclipse.ptp.internal.rdt.core.typehierarchy;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ptp.internal.rdt.core.model.ModelAdapter;
import org.eclipse.ptp.internal.rdt.core.model.Scope;
import org.eclipse.ptp.internal.rdt.core.serviceproviders.AbstractRemoteService;
import org.eclipse.ptp.internal.rdt.core.subsystems.ICIndexSubsystem;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.subsystems.IConnectorService;

public class RemoteTypeHierarchyService extends AbstractRemoteService implements ITypeHierarchyService {

	public RemoteTypeHierarchyService(IHost host, IConnectorService connectorService) {
		super(host, connectorService);
	}
	
	public THGraph computeGraph(Scope scope, ICElement input, IProgressMonitor monitor) throws CoreException, InterruptedException {
		ICIndexSubsystem subsystem = getSubSystem();
		subsystem.checkAllProjects(monitor);
		
		ICElement element = ModelAdapter.adaptElement(null, input, 0, true);
		return subsystem.computeTypeGraph(scope, element, monitor);
	}

	public ICElement[] findInput(Scope scope, ICElement input, IProgressMonitor monitor) {
		ICIndexSubsystem subsystem = getSubSystem();
		IProject project = input.getCProject().getProject();
		subsystem.checkProject(project, monitor);
		
		ICElement element;
		try {
			element = ModelAdapter.adaptElement(null, input, 0, true);
		} catch (CModelException e) {
			return null;
		}
		return subsystem.findTypeHierarchyInput(scope, element);
	}

	public ICElement[] findInput(Scope scope, ICProject project, IWorkingCopy workingCopy, int selectionStart, int selectionLength, IProgressMonitor monitor) throws CoreException {
		ITranslationUnit unit = adaptWorkingCopy(workingCopy);
		ICIndexSubsystem subsystem = getSubSystem();
		subsystem.checkProject(project.getProject(), monitor);
		
		return subsystem.findTypeHierarchyInput(scope, unit, selectionStart, selectionLength);
	}

}
