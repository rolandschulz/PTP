#*******************************************************************************
#* Copyright (c) 2011 Forschungszentrum Juelich GmbH.
#* All rights reserved. This program and the accompanying materials
#* are made available under the terms of the Eclipse Public License v1.0
#* which accompanies this distribution, and is available at
#* http://www.eclipse.org/legal/epl-v10.html
#*
#* Contributors:
#*    Wolfgang Frings (Forschungszentrum Juelich GmbH) 
#*******************************************************************************/ 
package LML_specs;

use strict;

# [type, req, info, description ]
# type:
#    k    -> keywords
#    s    -> string
#    d    -> integer
#    D    -> Date
#    f    -> float
# req:
#    M    -> Mandatory (also in job list table)
#    m    -> Mandatory
#    O    -> Optional
# description: 
#    -> string

$LML_specs::LMLattributes = {
    "job" => {
	         "owner"          => ["s","M", undef, "uid of owner of job"],
	         "group"          => ["s","M", undef, "gid of owner of job"],
	         "status"         => ["k","M",{
		                                "UNDETERMINED" => 1,
						"SUBMITTED"    => 1,
						"RUNNING"      => 1,
						"SUSPENDED"    => 1,
						"COMPLETED"    => 1,
				      }, "Primary status of job" 
		                     ],
	         "detailedstatus" => ["k","M",{
		                                "QUEUED_ACTIVE"        => 1, 
						"SYSTEM_ON_HOLD"       => 1, # Submitted
						"USER_ON_HOLD"         => 1, # Submitted
						"USER_SYSTEM_ON_HOLD"  => 1, # Submitted
						"SYSTEM_SUSPENDED"     => 1, # Suspended
						"USER_SUSPENDED"       => 1, # Suspended
						"USER_SYSTEM_SUSPENDED"=> 1, # Suspended
						"FAILED"               => 1, # Completed
						"CANCELED"             => 1, # Completed
						"JOB_OUTERR_READY"     => 1, # Completed
				      }, "Detailed status of job" 
		                     ],
	         "state"          => ["k","O",{
		                                "Running"    => 1,
						"Completed"  => 1,
						"Idle"       => 1,
						"Not Queued" => 1,
						"Removed"    => 1,
						"User Hold"  => 1,
						"System Hold"=> 1,
				      }, "Status of Job" 
		                     ],
		 "wall"         => ["d","m",undef, "requested wall time for this job (s)" ],
		 "wallsoft"     => ["d","O",undef, "requested wall time for this job (s), soft limit" ],
		 "queuedate"    => ["D","m",undef, "date job was inserted in queue (submit)"], 
		 "dispatchdate" => ["D","O",undef, "date job changed to running state"],
		 "enddate"      => ["D","O",undef, "date when job will if running to wall limit"],
		 "name"         => ["s","O",undef, "used defined name of job" ],
		 "step"         => ["s","M",undef, "unique job id" ],
		 "comment"      => ["s","O",undef, "comment" ],
		 "totalcores"   => ["d","M",undef, "total number of machine cores requested"],
		 "totaltasks"   => ["d","M",undef, "total number of (MPI) tasks"],
		 "totalgpus"    => ["d","O",undef, "total number of GPUs requested"],
		 "nodelist"     => ["s","M",undef, "node on which a job is running" ],
		 "gpulist"      => ["s","O",undef, "gpu nodes on which a job is running" ],
		 "vnodelist"    => ["s","O",undef, "virtual node list (PBS), used first" ],
		 "queue"        => ["s","M",undef, "queue"],
		 "dependency"   => ["s","O",undef, "dependency string"],
		 "executable"   => ["s","O",undef, "path and name of executable"],
		 "spec"         => ["s","O",undef, "size specification (like n<n>p<p>t<t>)"],
# LL optional
		 "classprio"    => ["d","O",undef, ""], 
		 "groupprio"    => ["d","O",undef, ""], 
		 "userprio"     => ["d","O",undef, ""], 
		 "favored"      => ["s","O",undef, ""], 
		 "restart"      => ["s","O",undef, ""], 
# BG/P optional
		 "bgp_partalloc"      => ["s","O",undef, ""],
		 "bgp_size_alloc"     => ["s","O",undef, ""],
		 "bgp_size_req"       => ["s","O",undef, ""],
		 "bgp_shape_alloc"    => ["s","O",undef, ""],
		 "bgp_shape_req"      => ["s","O",undef, ""],
		 "bgp_state"          => ["s","O",undef, ""],
		 "bgp_type"           => ["s","O",undef, ""],
# SLURM optional
         "account"            => ["s","O",undef, "SLURM usage account"],
         "command"            => ["s","O",undef, "SLURM running batch command"],
         "runtime"            => ["d","O",undef, "SLURM current job runtime"],
   },
    "node" => {
	         "id"             => ["s","M", undef, ""],
	         "ncores"         => ["i","M", undef, ""],
	         "physmem"        => ["i","M", undef, ""],
	         "availmem"       => ["i","M", undef, ""],
	         "state"          => ["k","M",{
		                                "Running"    => 1,
						"Idle"       => 1,
						"Drained"    => 1,
						"Down"       => 1,
						"Unknown"    => 1,
				      }, ""
		                     ],
    }
};


1;
