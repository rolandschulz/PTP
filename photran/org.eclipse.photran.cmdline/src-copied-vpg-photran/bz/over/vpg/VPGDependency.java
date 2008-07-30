package bz.over.vpg;

/**
 * A dependency in a VPG.
 * <a href="../../../overview-summary.html#DEA">More Information</a>
 * 
 * @author Jeff Overbey
 */
public class VPGDependency<A, T, R extends TokenRef<T>>
{
	@SuppressWarnings("unused") private VPG<A, T, R, ?> vpg;
	private String dependentFile;
	private String dependsOnFile;

    /**
     * Constructor. Creates a dependency between two files in the given VPG.
     * <p>
     * The dependency is <i>not</i> added to the VPG database automatically.
     */
	public VPGDependency(VPG<A, T, R, ?> vpg,
	                        String dependencyFrom,
	                        String dependsOn)
	{
		this.vpg = vpg;
		this.dependentFile = dependencyFrom;
		this.dependsOnFile = dependsOn;
	}

    ///////////////////////////////////////////////////////////////////////////
    // Accessors
    ///////////////////////////////////////////////////////////////////////////

	/** @return the file which is dependent upon another file */
	public String getDependentFile()
	{
		return dependentFile;
	}

	/** @return the file which is depended upon */
	public String getDependsOnFile()
	{
		return dependsOnFile;
	}
}
