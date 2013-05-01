package org.eclipse.ptp.internal.debug.ui.sourcelookup;

import org.eclipse.ptp.internal.debug.core.sourcelookup.PSourceLookupDirector;

public class DefaultSourceLocator extends PSourceLookupDirector {
	/*
	 * public void initializeFromMemento(String memento, ILaunchConfiguration configuration) throws CoreException {
	 * Element rootElement = DebugPlugin.parseDocument(memento);
	 * if (rootElement.getNodeName().equalsIgnoreCase(OldDefaultSourceLocator.ELEMENT_NAME)) {
	 * initializeFromOldMemento(memento, configuration);
	 * } else {
	 * super.initializeFromMemento(memento, configuration);
	 * }
	 * }
	 * private void initializeFromOldMemento(String memento, ILaunchConfiguration configuration) throws CoreException {
	 * dispose();
	 * setLaunchConfiguration(configuration);
	 * OldDefaultSourceLocator old = new OldDefaultSourceLocator();
	 * old.initializeFromMemento(memento);
	 * IPSourceLocator csl = (IPSourceLocator) old.getAdapter(IPSourceLocator.class);
	 * setFindDuplicates(csl.searchForDuplicateFiles());
	 * IPSourceLocation[] locations = csl.getSourceLocations();
	 * 
	 * // Check if the old source locator includes all referenced projects.
	 * // If so, DefaultSpourceContainer should be used.
	 * IProject project = csl.getProject();
	 * List<IProject> list = PDebugUtils.getReferencedProjects(project);
	 * HashSet<String> names = new HashSet<String>(list.size() + 1);
	 * names.add(project.getName());
	 * Iterator<IProject> it = list.iterator();
	 * while (it.hasNext()) {
	 * names.add(((IProject) it.next()).getName());
	 * }
	 * boolean includesDefault = true;
	 * for (int i = 0; i < locations.length; ++i) {
	 * if (locations[i] instanceof IProjectSourceLocation && ((IProjectSourceLocation) locations[i]).isGeneric()) {
	 * if (!names.contains(((IProjectSourceLocation) locations[i]).getProject().getName())) {
	 * includesDefault = false;
	 * break;
	 * }
	 * }
	 * }
	 * // Generate an array of new source containers including DefaultSourceContainer
	 * List<IPSourceLocation> locs = new ArrayList<IPSourceLocation>(locations.length);
	 * for (int i = 0; i < locations.length; ++i) {
	 * if (!includesDefault || !(locations[i] instanceof IProjectSourceLocation && names.contains(((IProjectSourceLocation)
	 * locations[i]).getProject().getName())))
	 * locs.add(locations[i]);
	 * }
	 * ISourceContainer[] containers = SourceUtils.convertSourceLocations((IPSourceLocation[]) locs.toArray(new
	 * IPSourceLocation[locs.size()]));
	 * List<ISourceContainer> cons = new ArrayList<ISourceContainer>(Arrays.asList(containers));
	 * if (includesDefault) {
	 * DefaultSourceContainer defaultContainer = new DefaultSourceContainer();
	 * defaultContainer.init(this);
	 * cons.add(0, defaultContainer);
	 * }
	 * setSourceContainers((ISourceContainer[]) cons.toArray(new ISourceContainer[cons.size()]));
	 * initializeParticipants();
	 * }
	 */
}
