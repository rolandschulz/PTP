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

# [type, req, info]
# type:
#    k    -> keywords
#    s    -> string
#    d    -> integer
#    D    -> Date
# req:
#    M    -> Mandatory
#    O    -> Optional

$LML_specs::LMLattributes = {
    "job" => {
	         "owner"          => ["s","M", undef],
	         "group"          => ["s","M", undef],
	         "state"          => ["k","M",{
		                                "Running"    => 1,
						"Completed"  => 1,
						"Idle"       => 1,
						"Not Queued" => 1,
						"Removed"    => 1,
						"User Hold"  => 1,
						"System Hold"=> 1,
				      }
		                     ],
		 "wall"         => ["d","M",undef],
		 "wallsoft"     => ["d","O",undef],
		 "queuedate"    => ["D","M",undef], 
		 "dispatchdate" => ["D","O",undef],
		 "enddate"      => ["D","O",undef],
		 "name"         => ["s","O",undef],
		 "step"         => ["s","M",undef],
		 "comment"      => ["s","O",undef],
		 "totalcores"   => ["i","M",undef],
		 "totaltasks"   => ["i","M",undef],
		 "nodelist"     => ["s","M",undef],
		 "queue"        => ["s","M",undef],
		 "dependency"   => ["s","O",undef],
		 "executable"   => ["s","O",undef],
# LL optional
		 "classprio"    => ["d","O",undef], 
		 "groupprio"    => ["d","O",undef], 
		 "userprio"     => ["d","O",undef], 
		 "favored"      => ["s","O",undef], 
		 "restart"      => ["s","O",undef], 
# BG/P optional
		 "bgp_partalloc"      => ["s","O",undef],
		 "bgp_size_alloc"     => ["s","O",undef],
		 "bgp_size_req"       => ["s","O",undef],
		 "bgp_shape_alloc"    => ["s","O",undef],
		 "bgp_shape_req"      => ["s","O",undef],
		 "bgp_state"          => ["s","O",undef],
		 "bgp_type"           => ["s","O",undef],
   },
    "node" => {
	         "id"             => ["s","M", undef],
	         "ncores"         => ["i","M", undef],
	         "physmem"        => ["i","M", undef],
	         "availmem"       => ["i","M", undef],
	         "state"          => ["k","M",{
		                                "Running"    => 1,
						"Idle"       => 1,
						"Drained"    => 1,
						"Down"       => 1,
						"Unknown"    => 1,
				      }
		                     ],
    }
};


1;
