package org.eclipse.ptp.pldt.common;

import java.util.List;

import org.eclipse.cdt.core.model.ITranslationUnit;

/**
 * An artifact analysis for MPI, OpenMP, or OpenACC.
 * <p>
 * This interface is implemented by contributions to the following extension points:
 * <ul>
 * <li>org.eclipse.ptp.pldt.mpi.core.artifactAnalysis
 * <li>org.eclipse.ptp.pldt.openmp.core.artifactAnalysis
 * <li>org.eclipse.ptp.pldt.openacc.artifactAnalysis
 * </ul>
 * <p>
 * Implementations targeting C and C++ will typically subclass {@link ArtifactAnalysisBase}.
 * 
 * @since 6.0
 * 
 * @see ArtifactAnalysisBase
 * 
 */
public interface IArtifactAnalysis {
	/**
	 * Run artifact analysis, presumably to locate artifacts within a given file (translation unit).
	 * @param languageID
	 * @param tu
	 * @param includes
	 * @param allowPrefixOnlyMatch
	 * @return
	 */
	ScanReturn runArtifactAnalysis(String languageID, ITranslationUnit tu, List<String> includes, boolean allowPrefixOnlyMatch);
}
