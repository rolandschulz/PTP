/*******************************************************************************
 * Copyright (c) 2013 The University of Tennessee and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Roland Schulz - initial implementation
 *******************************************************************************/

package org.eclipse.ptp.internal.rdt.sync.git.core;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoFilepatternException;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.dircache.DirCacheIterator;
import org.eclipse.jgit.ignore.IgnoreNode;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.FileTreeIterator;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.WorkingTreeIterator;
import org.eclipse.jgit.treewalk.filter.IndexDiffFilter;
import org.eclipse.ptp.internal.rdt.sync.core.RDTSyncCorePlugin;
import org.eclipse.ptp.internal.rdt.sync.git.core.messages.Messages;
import org.eclipse.ptp.rdt.sync.core.AbstractSyncFileFilter;
import org.eclipse.ptp.rdt.sync.core.SyncConfig;
import org.eclipse.ptp.rdt.sync.core.SyncConfigManager;
import org.eclipse.ptp.rdt.sync.core.SyncManager;

/**
 * File filtering using git build in filtering. All file filter patterns are stored in .ptp-sync/info/exclude.
 * The git syntax is used (see man gitignore). 
 * 
 */
public class GitSyncFileFilter extends AbstractSyncFileFilter {
	public static final String REMOTE_FILTER_IS_DIRTY = "remote_filter_is_dirty"; //$NON-NLS-1$
	// Map of projects to file filters along with basic getter and setter methods. These static data and methods operate
	// independently of the rest of the class and could be moved into a separate class if desired.
	private static Map<IProject, GitSyncFileFilter> projectToFilterMap = new HashMap<IProject, GitSyncFileFilter>();
	public static GitSyncFileFilter getFilter(IProject project) {
		GitSyncFileFilter filter = projectToFilterMap.get(project);
		if (filter==null) {
			try {
				filter = new GitSyncFileFilter(GitRemoteSyncConnection.getLocalRepo(project, project.getLocation().toString()), project);
				filter.loadFilter();
				projectToFilterMap.put(project, filter);
			} catch (IOException e) {
				RDTSyncCorePlugin.log(Messages.GitSyncFileFilter_UnableToLoad + project.getName(), e);
			}
		}
		return filter;
	}

	public static void setFilter(IProject project, AbstractSyncFileFilter filter, Repository repository) {
		GitSyncFileFilter newGitFilter = new GitSyncFileFilter(repository, project);
		newGitFilter.initialize(filter);
		try {
			newGitFilter.saveFilter();
		} catch (IOException e) {
			RDTSyncCorePlugin.log(Messages.GitSyncFileFilter_UnableToSave + project.getName(), e);
		}
		projectToFilterMap.put(project, newGitFilter);
	}

	private Repository repository;
	private IProject project;
	
	private static Set<Character> escapifyCharSet = new HashSet<Character>();
	static {
		CharacterIterator it = new StringCharacterIterator("#\\!*?[]"); //$NON-NLS-1$
		for (char c = it.first(); c != CharacterIterator.DONE; c = it.next())
			escapifyCharSet.add(c);
	}
	public class GitIgnoreRule extends AbstractIgnoreRule {
		private org.eclipse.jgit.ignore.IgnoreRule rule;
		
		public GitIgnoreRule(String pattern, boolean exclude) {
			if (!exclude) pattern = "!" + pattern; //$NON-NLS-1$
			rule = new org.eclipse.jgit.ignore.IgnoreRule(pattern);
		}
		
		private String charEscapify(String inputString) {
			StringBuffer newString = new StringBuffer(""); //$NON-NLS-1$
			CharacterIterator it = new StringCharacterIterator(inputString);
			for (char c = it.first(); c != CharacterIterator.DONE; c = it.next()) {
				if (escapifyCharSet.contains(c)) { // Do not escape non-ASCII characters (> 127)
					newString.append("\\" + c); //$NON-NLS-1$
				} else {
					newString.append(c);
				}
			}
			inputString = newString.toString();
			return inputString;
		}
		
		public GitIgnoreRule(IResource resource, boolean exclude) {
			String pattern = charEscapify(resource.getProjectRelativePath().toString());
			if (resource.getType()==IResource.FOLDER) pattern += "/"; //$NON-NLS-1$
			if (!exclude) pattern = "!" + pattern; //$NON-NLS-1$
			rule = new org.eclipse.jgit.ignore.IgnoreRule(pattern);
		}
		
		private GitIgnoreRule(org.eclipse.jgit.ignore.IgnoreRule rule) {
			this.rule = rule;
		}
		
		@Override
		public boolean isMatch(IResource target) {
			return rule.isMatch(target.getProjectRelativePath().toString(), 
					target.getType()==IResource.FOLDER);
		}
		
		@Override
		public boolean isMatch(String target, boolean isFolder) {
			return rule.isMatch(target,isFolder);
		}

		@Override
		public boolean getResult() {
			return rule.getResult();
		}
		
		@Override
		public String toString() {
			return (rule.getNegation()?"!":"") + rule.getPattern() + (rule.dirOnly()?"/":""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		}
		
		@Override
		public String getPattern() {
			return rule.getPattern() + (rule.dirOnly()?"/":""); //$NON-NLS-1$ //$NON-NLS-2$ 
		}
	}
	
	GitSyncFileFilter(Repository repository, IProject project) {
		this.repository = repository;
		this.project = project;
	}
	
	public GitSyncFileFilter(GitSyncFileFilter filter) {
		this.repository = filter.repository;
		this.project = filter.project;
		rules.addAll(filter.rules);
	}

	@Override
	public void addPattern(String pattern, boolean exclude, int index) {
		rules.add(index, new GitIgnoreRule(pattern, exclude));

	}

	@Override
	public void addPattern(IResource resource, boolean exclude, int index) {
		rules.add(index, new GitIgnoreRule(resource, exclude));

	}

	public void saveFilter() throws IOException  {
		File exclude = repository.getFS().resolve(repository.getDirectory(),
				Constants.INFO_EXCLUDE);
		exclude.getParentFile().mkdirs();
		FileOutputStream file = new FileOutputStream(exclude);
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(file,Constants.CHARSET));
		try {
			for (AbstractIgnoreRule rule : rules) {
				out.write(rule.toString());
				out.newLine();
			}
		} finally {
			out.close();
		}
		final RmCommandCached rmCommand = new RmCommandCached(repository);
		for (String fileName : getIgnoredFiles(null)) {
			rmCommand.addFilepattern(fileName);
		}
		try {
			rmCommand.call();
		} catch (NoFilepatternException e) {
			new IOException(e);  //TODO: a bit ugly to wrap it into IOExcpetion
		} catch (GitAPIException e) {
			new IOException(e);
		}
		
		for (SyncConfig config : SyncConfigManager.getConfigs(project))
			config.setProperty(REMOTE_FILTER_IS_DIRTY, "TRUE"); //$NON-NLS-1$
	}
	
	public void loadFilter() throws IOException {
		File exclude = repository.getFS().resolve(repository.getDirectory(),
				Constants.INFO_EXCLUDE);
		if (exclude.exists()) {
			FileInputStream in = new FileInputStream(exclude);
			try {
				IgnoreNode node = new IgnoreNode();
				node.parse(in);
				for (org.eclipse.jgit.ignore.IgnoreRule rule : node.getRules())
					rules.add(new GitIgnoreRule(rule));
			} finally {
				in.close();
			}
		} else {
			initialize(SyncManager.getDefaultFileFilter());
		}
	}

	/**
	 * Initialize based on other filter. Adds provider dependent default. 
	 * 
	 * @param filter to copy 
	 */
	public void initialize(AbstractSyncFileFilter fileFilter) {
		if (fileFilter instanceof GitSyncFileFilter) { 
			rules.addAll(fileFilter.rules);
		} else {								//convert rules and add git specific rule 
			for (AbstractIgnoreRule rule : fileFilter.rules)
				rules.add(new GitIgnoreRule(rule.getPattern(),rule.getResult()));
			rules.add(new GitIgnoreRule("/.ptp-sync/", true)); //$NON-NLS-1$
		}  
	}

	/* returns ignored files in the index 
	 * @param ref reference to compute list of files for. If null use index.
	 * */
	public Set<String> getIgnoredFiles(RevTree ref) throws IOException {
		TreeWalk treeWalk = new TreeWalk(repository);
		if (ref==null) {
			DirCache dirCache = repository.readDirCache();
			treeWalk.addTree(new DirCacheIterator(dirCache));
		} else
			treeWalk.addTree(ref);
		HashSet<String> ignoredFiles = new HashSet<String>();
		int ignoreDepth = Integer.MAX_VALUE; //if the current subtree is ignored - than this is the depth at which to ignoring starts
		while (treeWalk.next()) {
			boolean isSubtree = treeWalk.isSubtree();
			int depth = treeWalk.getDepth();
			String path = treeWalk.getPathString();
			if (isSubtree) treeWalk.enterSubtree();
			if (depth > ignoreDepth) {
				if (!isSubtree) ignoredFiles.add(path);
				continue;
			}
			if (depth <= ignoreDepth) { //sibling or parent of ignore subtree => reset
				ignoreDepth = Integer.MAX_VALUE;
			}
			if (shouldIgnore(path, isSubtree)) {
				if (isSubtree) ignoreDepth = depth;
				else ignoredFiles.add(path);
			}
		}
		return ignoredFiles;
	}
	
	public class DiffFiles {
		public Set<String> added = new HashSet<String>(); //modified and added
		public Set<String> removed = new HashSet<String>();
	}
	
	/* get all different files (modified/changed, missing/removed, untracked/added)
	 * 
	 * assumes that no files are in conflict (don't call during merge) 
	 * */
	public DiffFiles getDiffFiles() throws IOException {
		final int INDEX = 0;
		final int WORKDIR = 1;
		
		TreeWalk treeWalk = new TreeWalk(repository);
		treeWalk.addTree(new DirCacheIterator(repository.readDirCache()));
		treeWalk.addTree(new FileTreeIterator(repository));

		//don't honor ignores - we do it manual instead. Doing it all with the filter
		//would require a WorkingTreeIteraotr which does the ignore handing correct 
		//(both directory including bugs 401161 and only using info/exclude not .gitignore)
		treeWalk.setFilter(new IndexDiffFilter(INDEX, WORKDIR,false)); 
		DiffFiles diffFiles = new DiffFiles();
		int ignoreDepth = Integer.MAX_VALUE; //if the current subtree is ignored - than this is the depth at which to ignoring starts
		while (treeWalk.next()) {
			DirCacheIterator dirCacheIterator = treeWalk.getTree(INDEX,
					DirCacheIterator.class);
			String path = treeWalk.getPathString();
			boolean isSubtree = treeWalk.isSubtree();
			int depth = treeWalk.getDepth();
			if (dirCacheIterator != null ||          //in index => either missing or modified
					!shouldIgnore(path, isSubtree)) { //not in index => untracked
				if (depth <= ignoreDepth)  //sibling or parent of ignore subtree => reset
					ignoreDepth = Integer.MAX_VALUE;
				if (dirCacheIterator != null && isSubtree && ignoreDepth==Integer.MAX_VALUE &&
						shouldIgnore(path, isSubtree))
					ignoreDepth = depth;
				if (isSubtree) 
					treeWalk.enterSubtree(); 
				else if(dirCacheIterator != null || ignoreDepth==Integer.MAX_VALUE) {
					WorkingTreeIterator workTreeIter = treeWalk.getTree(WORKDIR, WorkingTreeIterator.class);
					if (workTreeIter != null)   //in work-tree => modified or untracked 
						diffFiles.added.add(path);
					else
						diffFiles.removed.add(path);
				}
			}
		}
		return diffFiles;
	}
	
	
	//for testing. args: work folder, git folder
	public static void main(String [] args) throws IOException
	{
		final File localDir = new File(args[0]);
		final FileRepositoryBuilder repoBuilder = new FileRepositoryBuilder();
		File gitDirFile = new File(localDir + File.separator + args[1]); 
		Repository repository = repoBuilder.setWorkTree(localDir).setGitDir(gitDirFile).build();
		GitSyncFileFilter filter = new GitSyncFileFilter(repository, null);
		filter.loadFilter();
		//List<String> files = filter.getIgnoredFiles();
		Set<String> files = filter.getDiffFiles().added;
		for (String path : files) 
			System.out.println(path);
	}

	@Override
	public AbstractSyncFileFilter clone() {
		return new GitSyncFileFilter(this);
	}
}
