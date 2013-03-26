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
package LML_color_manager;
use strict;
my($debug)=0;
my $patint="([\\+\\-\\d]+)";   # Pattern for Integer number
my $patfp ="([\\+\\-\\d.E]+)"; # Pattern for Floating Point number
my $patwrd="([\^\\s]+)";       # Pattern for Work (all noblank characters)
my $patbl ="\\s+";             # Pattern for blank space (variable length)

sub new {
    my $self  = {};
    my $proto = shift;
    my $class = ref($proto) || $proto;
    my($path,$i,$cat,$hex);
    printf("\t\LLview_LML_colormanager: new %s %s\n",ref($proto),caller()) if($debug>=2);

    my $colorconfigfname = shift;

    $self->{SCHEMES}       = ["PREDEFINED","GRAY","HSV","RGB","FILE"];

    $self->{COLORS}        = [];
    $self->{COLORS_SORTED} = [];
    
    bless $self, $class;

    # initialize obj
    $self->read_colorconfig($colorconfigfname);

    $i=0;
    for $cat (@{$self->{CATEGORIES}}) {
	$self->{CAT2NR}->{$cat}=$i;
	$i++;
    }

    for($i=0;$i<255;$i++) {
	$hex=sprintf("%02x",$i);
	$self->{HEX2INT}{$hex}=$i;
    }

	# initialize list of fix colors
    $self->initcolor();

    # initialize obj
    $self->init();

    return $self;
}

sub read_colorconfig {
    my($self) = shift;
    my($fname) = @_;
    my($cat,$catname,$rest,@rest,$line,$pair,$key,$value);
    print "read_colorconfig: reading color definition file $fname ...\n" if($debug>=2); 
    open(IN,$fname) || return(0);
    $cat=0;
    while($line=<IN>) {
	next if($line=~/^\s*\#/);
	if($line=~/^\s*$patwrd\s*\:\s*(.*)\s*$/) {
	    $catname=lc($1);
	    $rest=$2;
	    # parse key value pairs of configuration options
	    print "read_colorconfig[$cat]: catname=$catname rest=$rest\n" if($debug>=2);
	    @rest=split(/\s*\,\s*/,$rest);
	    foreach $pair (@rest) {
		($key,$value)=split(/\s*\=\s*/,$pair);
		$key=uc($key);
		print "read_colorconfig[$cat]: scan option $key=$value\n" if($debug>=2);
		if($key eq "TYPE") {
		    $self->{PATTERN}[$cat]=lc($value);
		    next;
		}
		if($key eq "COLORTYPE") {
		    $self->{SCHEME}[$cat]=uc($value);
		    next;
		}
		if ( ($key eq "BUFFER") || ($key eq "BUFFERSIZE") ) {
		    $self->{BUFFERSIZE}[$cat]=$value;
		    next;
		}
		if (($key=~/^[HSVRGB]$/) || 
		    ($key=~/GRAY/) ){
		    if($value=~/$patint\.\.$patint/) {
			$self->{"VALUE${key}START"}[$cat]=$1;
			$self->{"VALUE${key}END"}[$cat]=$2;
			next;
		    }
		    if($value=~/$patint/) {
			$self->{"VALUE${key}START"}[$cat]=$1;
			$self->{"VALUE${key}END"}[$cat]=$1;
			$self->{"VALUE_${key}"}[$cat]=$1;
			next;
		    }
		}
		if( $key=~ "FILE" ){
			#If file not existent try to find it relatively placed to the calling script
			if(!(-e $value)){
				my $srcDir = $0;
				print $0."\n";
				if($srcDir =~ /\// ){
					$srcDir =~ s/[^\/]+\.pl$//;
				}
				else{
					$srcDir = "./";
				}
				$value=$srcDir.$value;
			}
			
			if(!(-e $value)){
				print STDERR "ERROR: could not find color config file for category $catname at $value\n";
			}
			
			$self->{VALUECOLFILE}[$cat] = $value;
			next;
		}
		if( $key=~ "RANDOM" ){
			$self->{RANDOM}[$cat] = int($value);
			next;
		}
		
		print "read_colorconfig[$cat]: unknown option $key=$value\n" if($debug>=0);
	    }
	    # register new category
	    push(@{$self->{CATEGORIES}},$catname);
	    $cat++;

	}
    }
    
    close(IN);
    print "read_colorconfig: reading color definition file $fname ... $cat categories found\n" if($debug>=2); 

}

sub init {
    my($self) = shift;
    my $optobj = $self->{OPTIONSOBJ};
    my($name,$cat,$category);

    # initalize buffers
    for($cat=0;$cat<=$#{$self->{CATEGORIES}};$cat++) {
	#Randomize if not otherwise defined
	if(!exists($self->{RANDOM}[$cat])){
		$self->{RANDOM}[$cat]        = 1;
	}
 	$self->{BUFFER}[$cat]        = [];

	$self->{KNOWNIDSSIZE}[$cat]  = 0;
	$self->{KNOWNIDS}[$cat]      = {};

	$self->{USEDCOLORSSIZE}[$cat]= 0;
	$self->{USEDCOLORS}[$cat]    = {};

	$self->{COLORTONR}[$cat]     = {};
	$self->{NRTOCOLOR}[$cat]     = [];
    }
   
    my $off=0;
    for($cat=0;$cat<=$#{$self->{CATEGORIES}};$cat++) {
	$self->{BUFFERSIZE_INT}[$cat] = $self->{BUFFERSIZE}[$cat];
	$self->{BUFFEROFFSET}[$cat]  = $off; $off+=$self->{BUFFERSIZE_INT}[$cat];
    }

    for($cat=0;$cat<=$#{$self->{CATEGORIES}};$cat++) {
	$self->reset_idcache($cat);
	$self->fill_buffer($cat);
    }

    return();
}

sub fill_buffer {
    my($self) = shift;
    my($cat)=@_;
    my $scheme=$self->{SCHEME}[$cat];
    print "fill_buffer: color, fill_buffer($cat) scheme=$scheme\n" if($debug>=2); 
    
    if($scheme eq "PREDEFINED") {
	my($color,$id,$c,@work);
	$self->{COLORTONR}[$cat]={}; 
	$self->{NRTOCOLOR}[$cat]=[];

	@work=(@{$self->{COLORS}});
	$#{$self->{BUFFER}[$cat]}=-1;

	$c=1;
	foreach $color (@{$self->{COLORS_SORTED}}) {
	    unshift(@{$self->{BUFFER}[$cat]},$color);
	    $self->{COLORTONR}[$cat]->{$color}=$c;
	    $self->{NRTOCOLOR}[$cat]->[$c]=$color;
	    $c++;
	    last if($c>$self->{BUFFERSIZE_INT}[$cat]);
	}

	while(@work) {
	    $color=splice(@work,int(rand(scalar @work)),1);
	    if(!(exists($self->{COLORTONR}[$cat]->{$color}))) {
		push(@{$self->{BUFFER}[$cat]},$color);
		$self->{COLORTONR}[$cat]->{$color}=$c;
		$self->{NRTOCOLOR}[$cat]->[$c]=$color;
		$c++;
		last if($c>$self->{BUFFERSIZE_INT}[$cat]);
	    }
	}

	# fill rest with grey colors
	if($c<$self->{BUFFERSIZE_INT}[$cat]) {
	    my ($r);
	    for($r=0;$r<($self->{BUFFERSIZE_INT}[$cat]-$c);$r++) {
		$work[$r]=sprintf( "\#%02x%02x%02x", $r, $r, $r);
	    }
	    while(@work) {
		$color=splice(@work,int(rand(scalar @work)),1);
		if(!(exists($self->{COLORTONR}[$cat]->{$color}))) {
		    push(@{$self->{BUFFER}[$cat]},$color);
		    $self->{COLORTONR}[$cat]->{$color}=$c;
		    $self->{NRTOCOLOR}[$cat]->[$c]=$color;
		    $c++;
		    last if($c>$self->{BUFFERSIZE_INT}[$cat]);
		}
	    }
	}
    
	return();
    }
    if($scheme eq "GRAY") {
	my (@work,$r,$color,$c,$i);
	my $numcol=$self->{BUFFERSIZE_INT}[$cat];

	$#{$self->{BUFFER}[$cat]}=-1; # reset buffer
	$self->{COLORTONR}[$cat]={}; 
	$self->{NRTOCOLOR}[$cat]=[];

	my $off=0;
	$c=1;
	while($c<$self->{BUFFERSIZE_INT}[$cat]) {
	    # generate colors
	    $i=0;
	    for($r=$self->{VALUEGRAYSTART}[$cat];$r<$self->{VALUEGRAYEND}[$cat];$r++) {
		$work[$i++]=sprintf( "\#%02x%02x%02x", $r, $r, $r+$off);
	    }
	    # put colors in buffer
	    while(@work) {
		if($self->{RANDOM}[$cat]==1) {
		    $color=splice(@work,int(rand(scalar @work)),1);
		} else {
		    $color=shift(@work);
		}
		if(!(exists($self->{COLORTONR}[$cat]->{$color}))) {
		    push(@{$self->{BUFFER}[$cat]},$color);
		    $self->{COLORTONR}[$cat]->{$color}=$c;
		    $self->{NRTOCOLOR}[$cat]->[$c]=$color;
		    $c++;
		    last if($c>$self->{BUFFERSIZE_INT}[$cat]);
		}
	    }
	    $off++; 
	    last if($c>$self->{BUFFERSIZE_INT}[$cat]);
	}
    }
    if($scheme eq "HSV") {
	my (@work,$h,$s,$v,$r,$g,$b,$color,$c,$diff,$i);
	my $numcol=$self->{BUFFERSIZE_INT}[$cat];

	$#{$self->{BUFFER}[$cat]}=-1; # reset buffer
	$self->{COLORTONR}[$cat]={}; 
	$self->{NRTOCOLOR}[$cat]=[];


	my $off=0;
	$c=1;
	while($c<$self->{BUFFERSIZE_INT}[$cat]) {

	    # generate colors
	    if($self->{VALUEHEND}[$cat]>$self->{VALUEHSTART}[$cat]) {
		$diff=int $self->{VALUEHEND}[$cat]-$self->{VALUEHSTART}[$cat];
	    } else {
		$diff=int $self->{VALUEHEND}[$cat]+360-$self->{VALUEHSTART}[$cat];
	    }
#	    $diff=-$diff if($diff<0); # only a hack, panel has to take care about this
	    for($i=0;$i<$diff;$i++) {
		$h = ($self->{VALUEHSTART}[$cat]+$i)%360; 
		$s=$self->{VALUE_S}[$cat]; 
		$v=$self->{VALUE_V}[$cat]; 

		$v= ($v+$off)%100;
#		if($v+$off<100) {$v+=$off;} else {$v-=$off;$v=0 if ($v<0);}

		($r,$g,$b)=&hsv2rgb($h,$s,$v);
		$r = int 255.0/100.0 * $r;	$g = int 255.0/100.0 *$g;	$b = int 255.0/100.0 * $b;
		$work[$i]=sprintf( "\#%02x%02x%02x", $r, $g, $b);
	    }
	    
	    # put colors in buffer
	    while(@work) {
		if($self->{RANDOM}[$cat]==1) {
		    $color=splice(@work,int(rand(scalar @work)),1);
		} else {
		    $color=shift(@work);
		}
		if(!(exists($self->{COLORTONR}[$cat]->{$color}))) {
		    push(@{$self->{BUFFER}[$cat]},$color);
		    $self->{COLORTONR}[$cat]->{$color}=$c;
		    $self->{NRTOCOLOR}[$cat]->[$c]=$color;
		    $c++;
		    last if($c>$self->{BUFFERSIZE_INT}[$cat]);
		}
	    }
	    $off++; 
	    last if($c>$self->{BUFFERSIZE_INT}[$cat]);
	    last if($off>100);
	}
    }
    if($scheme eq "RGB") {
	my (@work,$r,$g,$b,$color,$c,$i);
	my $numcol=$self->{BUFFERSIZE_INT}[$cat];

	$#{$self->{BUFFER}[$cat]}=-1; # reset buffer
	$self->{COLORTONR}[$cat]={}; 
	$self->{NRTOCOLOR}[$cat]=[];

	my $off=0;
	$c=1;
	while($c<$self->{BUFFERSIZE_INT}[$cat]) {
	    # generate colors
	    $i=0;
	    $r=$self->{VALUERSTART}[$cat];
	    $g=$self->{VALUEGSTART}[$cat];
	    $b=$self->{VALUEBSTART}[$cat];
	    COL: 
	    for($r=$self->{VALUERSTART}[$cat];$r<$self->{VALUEREND}[$cat];$r++) {
		for($g=$self->{VALUEGSTART}[$cat];$r<$self->{VALUEGEND}[$cat];$g++) {
		    for($b=$self->{VALUEBSTART}[$cat];$b<$self->{VALUEBEND}[$cat];$b++) {
			$work[$i++]=sprintf( "\#%02x%02x%02x", $r, $g, $b+$off);
			last COL if ($i>$self->{BUFFERSIZE_INT}[$cat]);
		    }
		}
	    }

	    # put colors in buffer
	    while(@work) {
		if($self->{RANDOM}[$cat]==1) {
		    $color=splice(@work,int(rand(scalar @work)),1);
		} else {
		    $color=shift(@work);
		}
		if(!(exists($self->{COLORTONR}[$cat]->{$color}))) {
		    push(@{$self->{BUFFER}[$cat]},$color);
		    $self->{COLORTONR}[$cat]->{$color}=$c;
		    $self->{NRTOCOLOR}[$cat]->[$c]=$color;
		    $c++;
		    last if($c>$self->{BUFFERSIZE_INT}[$cat]);
		}
	    }
	    $off++; 
	    last if($c>$self->{BUFFERSIZE_INT}[$cat]);
	}
    }

    if($scheme eq "FILE") {
	my(@work,$color,$id,$c,$line);
	$self->{COLORTONR}[$cat]={}; 
	$self->{NRTOCOLOR}[$cat]=[];

	$#{$self->{BUFFER}[$cat]}=-1;

	open(IN,$self->{VALUECOLFILE}[$cat]) || return(0);
	
	my $i=0;
	while($line=<IN>) {
	    if($line=~/^\s*\d+\s*([^\s]+)\s*$/) {
			$color=lc($1);
			$work[$i++]=$color;
	    }
	}
	close(IN);
	
	$c=1;  
	# put colors in buffer
	while(@work) {
		if($self->{RANDOM}[$cat]==1) {
		    $color=splice(@work,int(rand(scalar @work)),1);
		} else {
		    $color=shift(@work);
		}
		if(!(exists($self->{COLORTONR}[$cat]->{$color}))) {
		    push(@{$self->{BUFFER}[$cat]},$color);
		    $self->{COLORTONR}[$cat]->{$color}=$c;
		    $self->{NRTOCOLOR}[$cat]->[$c]=$color;
		    $c++;
		    last if($c>$self->{BUFFERSIZE_INT}[$cat]);
		}
	}
	
	# fill rest with random colors
	while($c<=$self->{BUFFERSIZE_INT}[$cat]) {
	    my $red = int(rand(256));
	    my $green = int(rand(256));
	    my $blue = int(rand(256));
	    
	    $color = sprintf( "\#%02x%02x%02x", $red, $green, $blue);
	    if(!(exists($self->{COLORTONR}[$cat]->{$color}))) {
	    	push(@{$self->{BUFFER}[$cat]},$color);
		    $self->{COLORTONR}[$cat]->{$color}=$c;
		    $self->{NRTOCOLOR}[$cat]->[$c]=$color;
		    $c++;
	    }
	}
    
	return();
    }


}


# reset id cache, delete all known ids
sub reset_idcache {
    my($self) = shift;
    my($cat)=@_;
    my($color,$id);
    
    $self->{KNOWNIDSSIZE}[$cat]  = 0;
    $self->{KNOWNIDS}[$cat]      = {};
    
    $self->{USEDCOLORSSIZE}[$cat]= 0;
    $self->{USEDCOLORS}[$cat]    = {};

}

# save current mapping in DB
sub save_db {
    my($self) = shift;
    my($dbfile)=@_;
    my($color,$id,$cat);

    if(open(OUT,"> $dbfile")) {

	for($cat=0;$cat<=$#{$self->{CATEGORIES}};$cat++) {
	    foreach $id (keys(%{$self->{KNOWNIDS}[$cat]})) {
		$color=$self->{KNOWNIDS}[$cat]->{$id};
		print OUT "$cat;$id;$color\n";
	    }
	}

	close(OUT);
    } else {
	print STDERR "ERROR: could not open $dbfile for writing\n";
    }

}

# save current mapping in DB
sub load_db {
    my($self) = shift;
    my($dbfile)=@_;
    my($color,$id,$cat,$line,$c);

    if(open(IN," $dbfile")) {
	while($line=<IN>) {
	    chomp($line);
	    ($cat,$id,$color)=split(/;/,$line);
	    $self->{KNOWNIDS}[$cat]->{$id}=$color;
	    $self->{USEDCOLORS}[$cat]->{$color}=$id;
#	    print STDERR "load color entry: $cat,$id,$color \n";
	}
	close(IN);
	
	# remove used colors from corresponding color buffer
	for($cat=0;$cat<=$#{$self->{CATEGORIES}};$cat++) {
	    $c=0;
	    while($c<=$#{$self->{BUFFER}[$cat]}) {
		$color=$self->{BUFFER}[$cat][$c];
		if(exists($self->{USEDCOLORS}[$cat]->{$color})) {
		    $color=splice(@{$self->{BUFFER}[$cat]},$c,1);
#		    print STDERR "removed color entry: $cat,$id,$color $c of $#{$self->{BUFFER}[$cat]}\n";
		} else {
		    $c++;
		}
	    }
	}
    } else {
	print STDERR "WARNING: could not open $dbfile for reading\n";
    }

}

# search in BUFFER for a color with maximum distance to colors in USEDCOLORS
sub find_opt_color {
    my($self) = shift;
    my($cat)=@_;
    my($c,$col,$color,$colused,$r,$g,$b,$ru,$gu,$bu);
    my($distance,$mindistance,$maxdistance);
    my($maxdistancenum);
    $maxdistance=-1e20;
    $maxdistancenum=0;

    for($c=0;$c<$#{$self->{BUFFER}[$cat]};$c++) {
	# search minimum distance to used colors
	$mindistance=1e20;
	$col=$self->{BUFFER}[$cat][$c];
	$r=$self->{HEX2INT}{substr($col,1,2)};
	$g=$self->{HEX2INT}{substr($col,3,2)};
	$b=$self->{HEX2INT}{substr($col,5,2)};
	foreach $colused (keys(%{$self->{USEDCOLORS}[$cat]})) {
	    $ru=$self->{HEX2INT}{substr($colused,1,2)};
	    $gu=$self->{HEX2INT}{substr($colused,3,2)};
	    $bu=$self->{HEX2INT}{substr($colused,5,2)};
	    $distance=($r-$ru)*($r-$ru) + ($g-$gu)*($g-$gu) + ($b-$bu)*($b-$bu);
	    $mindistance=$distance if($distance<$mindistance);
	}
	if($mindistance>$maxdistance) {
	    $maxdistance=$mindistance;
	    $maxdistancenum=$c;
	}
    }
    $color=splice(@{$self->{BUFFER}[$cat]},$maxdistancenum,1);
    return($color);
}

sub get_color {
    my($self) = shift;
    my($category,$id)=@_;
    my($cat,$color);
    $category=lc($category);
    if(!exists($self->{CAT2NR}->{$category})) {
	print "ERROR in get_color wrong category $category from ",caller(),"\n";
	return(undef);
    }
    $cat=$self->{CAT2NR}->{$category};
    if(!$id) {
	print "ERROR in get_color no id, category $category from ",caller(),"\n";
    }
    if(exists($self->{KNOWNIDS}[$cat]->{$id})) {
	$color=$self->{KNOWNIDS}[$cat]->{$id};
    } else {
	# new color
	if($#{$self->{BUFFER}[$cat]}>0) {
	    if(!$self->{OPTIMIZE}[$cat]) {
		$color=shift(@{$self->{BUFFER}[$cat]}); 
	    } else {
		$color=$self->find_opt_color($cat); 
	    }
	} else {
	    printf("llview_manage_color: not enough colors in category %s(%d) buffersize=%d...\n",$category,$cat,$self->{BUFFERSIZE}[$cat]);
	    $color="#ff0000"; 
	}
	if($self->{USEDCOLORS}[$cat]->{$color}) {
	    printf( "llview_manage_color: warning color $color in use by: %10s %-15s -> %-20s #buffer=%3d\n",
		    $self->{USEDCOLORS}[$cat]->{$color},$id,$color,$#{$self->{BUFFER}[$cat]});
	}
	$self->{KNOWNIDS}[$cat]->{$id}=$color;
	$self->{USEDCOLORS}[$cat]->{$color}=$id;
	printf( "llview_manage_color: %-15s new color for %-15s -> %-20s nr=%3d #buffer=%3d (%s)\n",$category,$id,$color,
		$self->{COLORTONR}[$cat]->{$color},$#{$self->{BUFFER}[$cat]},join(",",caller())) if($debug>=3);
    }
    
    return($color);
}

sub colortonr {
    my($self) = shift;
    my($category,$color)=@_;
    my $cat=$self->{CAT2NR}->{$category};
    my $nr=$self->{COLORTONR}[$cat]->{$color}+$self->{BUFFEROFFSET}[$cat];
    return($nr);
}

sub nrtocolor {
    my($self) = shift;
    my($category,$nr)=@_;
    my $cat=$self->{CAT2NR}->{$category};
    $nr-=$self->{BUFFEROFFSET}[$cat];
    my $color=$self->{NRTOCOLOR}[$cat]->[$nr];
    return($color);
}

sub nrtocat {
    my($self) = shift;
    my($nr)=@_;
    my ($cat,$category);
    for($cat=0;$cat<=$#{$self->{CATEGORIES}};$cat++) {
	last if( ($nr>=$self->{BUFFEROFFSET}[$cat]) 
		 && ($nr<$self->{BUFFEROFFSET}[$cat]+$self->{BUFFERSIZE_INT}[$cat]) );
    }
    return("UNKNOWN") if($cat>$#{$self->{CATEGORIES}});  
    $category=$self->{CATEGORIES}->[$cat];
    return($category);
}

sub nrtoid {
    my($self) = shift;
    my($category,$nr)=@_;
    my $cat=$self->{CAT2NR}->{$category};
    $nr-=$self->{BUFFEROFFSET}[$cat];
    my $color=$self->{NRTOCOLOR}[$cat]->[$nr];
    my $id=$self->{USEDCOLORS}[$cat]->{$color};
    return($id);
}

sub idtonr {
    my($self) = shift;
    my($category,$id)=@_;
    my $color=$self->get_color($category,$id);
    my $nr=$self->colortonr($category,$color);
    return($nr);
}

sub getusednrs {
    my($self) = shift;
    my($category)=@_;
    my $cat=$self->{CAT2NR}->{$category};
    my(@nrs,$color);
    foreach $color (keys(%{$self->{USEDCOLORS}[$cat]})) {
	push(@nrs,$self->colortonr($category,$color));
    }
    return(@nrs);
}

sub getusednrs_all {
    my($self) = shift;
    my(@nrs,$color,$cat);

    for($cat=0;$cat<=$#{$self->{CATEGORIES}};$cat++) {
	push(@nrs,$self->getusednrs($self->{CATEGORIES}->[$cat]));
    }
    return(@nrs);
}


sub free {
    my($self) = shift;
    my($category,$id)=@_;
    my $cat=$self->{CAT2NR}->{$category};
    my($color);
    if(!$cat) {
	print "unknown category $category\n";
	return("");
    }
    if(exists($self->{KNOWNIDS}[$cat]->{$id})) {
	$color=$self->{KNOWNIDS}[$cat]->{$id};
	delete($self->{KNOWNIDS}[$cat]->{$id});
	delete($self->{USEDCOLORS}[$cat]->{$color});
	push(@{$self->{BUFFER}[$cat]},$color);
	printf( "llview_manage_color: $category free color for %-15s -> %-20s #buffer=%3d\n",$id,$color,$#{$self->{BUFFER}[$cat]}) if($debug>=2);
    } else {
	printf("llview_manage_color: $category freeing color for unknown id %s ...\n",$id) if($debug>=2);
    }
    return($color);
}

sub free_unused {
    my($self) = shift;
    my $category= shift;
    my $cat=$self->{CAT2NR}->{$category};
    my @ids=@_;
    my($id,$num);
    my %checkids=();
    
#    printf("llview_manage_color: free_unused: $category check IDs @ids\n");
    foreach $id (@ids) {
	if(exists($self->{KNOWNIDS}[$cat]->{$id})) {
	    $checkids{$id}=1;
	} else {
#	    printf("llview_manage_color: free_unused: new id %s ...\n",$id);
	}
    }

    $num=0;
    foreach $id (keys(%{$self->{KNOWNIDS}[$cat]})) {
	if(!exists($checkids{$id})) {
#	    print "llview_manage_colors: free_unused: $id \n";
	    $self->free($category,$id);
	    $num++;
	}
    }
#    printf("llview_manage_color: free_unused: $category check IDs @ids ready num=$num\n");
    return($num);
}


sub fill_examplecolors {
    my($self) = shift;
    my($cat)=@_;
    my $category=$self->{CATEGORIES}[$cat];
    my $ssection=$cat."_".$category;
    my $canvas=$self->{OPTIONOBJECT}->get_canvas_obj("Color","COLORSLIDE", -subtype => $ssection);
    my $width =$self->{COLORSEXAMPLEWIDTH};
    my $height=$self->{COLORSEXAMPLEHEIGHT};
    my $numcol=$self->{BUFFERSIZE_INT}[$cat];
    my($id,$c);
    my ($nx,$ny,$x,$y,$dx,$dy);
    $nx=20;$ny=$numcol/$nx; 
    $ny=int($ny)+1 if($ny != int($ny));
    $dx=$width/$nx;
    $dy=$height/$ny;
    
    # remove old rectangles
    while ($id=shift(@{$self->{ITEMS}[$cat]})) {
	$canvas->delete($id);
    }

    $c=1;
    for($y=0;$y<$ny;$y++) {
	for($x=0;$x<$nx;$x++) {
	    my $text=sprintf("%d%s",$c,
			     ($self->{USEDCOLORS}[$cat]->{$self->{NRTOCOLOR}[$cat]->[$c]})?" Used":""
			     );
	    $id=$canvas->createRectangle($x*$dx    ,$y*$dy,
					 $x*$dx+$dx,$y*$dy+$dy,
					 -fill => $self->{NRTOCOLOR}[$cat]->[$c]);
	    push(@{$self->{ITEMS}[$cat]},$id);
	    $id=$canvas->createText($x*$dx,$y*$dy,-text => $text, 
				    anchor=> "nw", -font => $self->{FONT1});
	    push(@{$self->{FIXEDITEMS}},$id);
	    $c++;
	}
    }
    if($self->{DUMPTOFILE}[$cat]) {
	open(OUT,"> ".$self->{FILENAME}[$cat]);
	for($c=1;$c<$numcol;$c++) {
	    printf(OUT "%4d %s\n",$c,$self->{NRTOCOLOR}[$cat]->[$c]);
	}
	close(OUT);
	printf("\t\llview_manage_color: dumped %d colors to %s\n",$numcol,$self->{FILENAME}[$cat]);

    }

}


sub hsv2rgb {
    my ($h, $s, $v) = @_;
    my ($sel, $res1, $val1, $val2, $val3, $r, $g, $b);
    if ($s == 0) {
	$r = $b = $g = $v;
    } else {
	$h /= 60;
	$sel = int $h;
	$res1 = $h - $sel;
	$val1 = $v/100.0 * (100 - $s);
	$val2 = $v/100.0 * (100 - $s * $res1);
	$val3 = $v/100.0 * (100 - $s * (1 - $res1));
	if ($sel == 0) {
	    $r = $v;
	    $g = $val3;
	    $b = $val1;
	  } elsif ($sel == 1) {
	      $r = $val2;
	      $g = $v;
	      $b = $val1;
	  } elsif ($sel == 2) {
	      $r = $val1;
	      $g = $v;
	      $b = $val3;
	  } elsif ($sel == 3) {
	      $r = $val1;
	      $g = $val2;
	      $b = $v;
	  } elsif ($sel == 4) {
	      $r = $val3;
	      $g = $val1;
	      $b = $v;
	  } else {
	      $r = $v;
	      $g = $val1;
	      $b = $val2;
	  }
    }
    return ($r, $g, $b);
}

# initialize list of fix colors
sub initcolor {
    my($self) = shift;
    $self->{COLORS_SORTED} = [
			'light goldenrod',
			'aquamarine2',                
			'azure',
			'bisque', 
			'blue',
			'blue violet',
			'brown',               
			'chartreuse',                  
			'chocolate',                   
			'coral',                       
			'CornflowerBlue',              
			'cyan',                        
			'dark blue',                   
			'dark red',                    
			'DarkCyan',                    
			'DarkRed',                     
			'DarkSeaGreen',                
			'deep pink',                   
			'DeepSkyBlue',                 
			'DodgerBlue',                  
			'firebrick',                   
			'FloralWhite',                 
			'ForestGreen',                 
			'gold',                        
			'goldenrod',                   
			'green',                       
			'green yellow',                
			'honeydew',                    
			'HotPink',
			'IndianRed',   
			'khaki',
			'lavender',
			'LemonChiffon',
			'light blue',                 
			'light salmon',                 
			'light pink',                 
			'light sky blue',             
			'LightSkyBlue',               
			'linen',                      
			'magenta',                    
			'maroon',                     
			'medium purple',              
			'orange',                     
			'PaleGreen',                  
			'PeachPuff',                  
			'peru',                       
			'plum',                       
			'purple',                     
			'RosyBrown',                  
			'RoyalBlue',                  
			'salmon',                     
			];

# can be filled with further color names
    $self->{COLORS} = [
			     "light blue",
			     "lime green",
			     "yellow",
			    	       ];
}			    
1;
