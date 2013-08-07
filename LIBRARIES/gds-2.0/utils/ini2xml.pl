#!/usr/bin/perl -w

sub printhelp {

print <<HERE

Converts property-style configuration files used by GDS 1.1 to the XML format 
used by GDS 1.2. 

Usage: ini2xml.pl [ inputfile [ outputfile ] ]

If inputfile is unspecified, the script will read from "grads_dods.ini". 
If outputfile is unspecified, the script will write to "gds.xml.migrated".
The output file should be renamed to "gds.xml" and placed in the home
directory of your GDS 1.2 installation in order to become effective.

* Note: there are a number of new configuration options in the GDS 1.2, and
* some options in 1.1 are now obsolete. It is recommended that 
* you read the GDS 1.2 documentation, and review the contents of the
* configuration file generated by this script, before using it for an
* operational server.

HERE
    ;

}

print "ini2xml.pl - migrates GDS 1.1 config files -> GDS 1.2 config XML\n";

$ininame = "grads_dods.ini";
$xmlname = "gds.xml.migrated";

if ($#ARGV >= 0) {
    $ininame = $ARGV[0]; 
    if ($#ARGV >= 1) {
	$xmlname = $ARGV[1]; 
    }
}

if (!open(INIFILE, $ininame)) { 
    printhelp;
    print "couldn't open input file: $ininame : $!\n";
    die "\n";
};

if (!open(XMLFILE, ">", $xmlname) ) { 
    printhelp;
    print "couldn't open output file: $xmlname : $!\n";
    die "\n";
};

LINE: while (<INIFILE>) {
    # skip whitespace and comments (";" is comment char)
    if (/^;/ or /^\s*$/) { 
	next LINE; 
    }

    # match text in brackets - section headers
    if (/^\[(\w*)\]/) {
	$section = $1;

	# multiple access levels can be defined, all using 
        # the section name "access_level"
	if ($section =~ /access_level/) {
	    $#access_levels++;

	    # invent a reasonable name for each access level since
            # GDS 1.2 requires it
	    if ($section =~ /default/) {
		$access_levels[$#access_levels]{"name"} = "default_level";
	    } else {
		$access_levels[$#access_levels]{"name"} = 
		    "level_" . $#access_levels;
	    }
	}
	next LINE;
    }
    
    # match "param_name=param_value"
    # %params is a hash of hash references - one hash for each section of the
    # config file. each name/value pair is placed in the appropriate sub-hash.
    if (/^(\w*)\s*=\s*(.*)$/) {
	$key = $1;
	$value = $2;
	# put access levels in a separate hash so they can be looped through
	if ($section =~ /access_level/) {
	    $access_levels[$#access_levels]{$key} = $value;
	} else {	    
	    $params{$section}{$key} = $value;
	}
    } else {
	next LINE;
    }
    
}

%current = %{$params{"grads"}};

$grads_exec = ($current{"grads_exec"} or "/usr/bin/grads");

# ignore these - not supported in GDS 1.2
#$grads_env = ($current{"grads_env"} or "/usr/local/grads");
#$grads_util = ($current{"grads_util" or "/usr/bin"});

$max_connect = ($current{"max_connect"} or 20);
$cache_size = ($current{"cache_size"} or 50);
$buffer_size = ($current{"buffer_size"} or 16384);
$temp_dataset_limit = ($current{"temp_dataset_limit"} or 1000);
$temp_expire_time = ($current{"temp_expire_time"} or 1440);

%ctl_datasets = defined %{$params{"binary_datasets"}} ? 
    %{$params{"binary_datasets"}} : 
     %empty;
%sdf_datasets = defined %{$params{"sdf_datasets"}} ? 
    %{$params{"sdf_datasets"}} : 
     %empty;
%xdf_datasets = defined %{$params{"xdf_datasets"}} ? 
    %{$params{"xdf_datasets"}} : 
     %empty;

print XMLFILE <<BOB
<gds>
   <grads>
     <invoker grads_bin="$grads_exec" />
     <dods buffer_size="$buffer_size" />
   </grads>
   <servlet>
      <filter-overload limit="$max_connect" />
   </servlet>

   <catalog temp_entries = "$temp_dataset_limit"
            temp_storage = "$cache_size"
            temp_age = "$temp_expire_time">
      <data>
BOB
;

while (($name,$file) = each %ctl_datasets) {
    print XMLFILE "        <dataset name=\"$name\" file=\"$file\" />\n";
} 
while (($name,$file) = each %sdf_datasets) {
    print XMLFILE "        <!-- the correct format for the following may be \"hdf\" - see documentation on the <dataset> tag -->";
    print XMLFILE "        <dataset name=\"$name\" file=\"$file\" format=\"nc\"/>\n";
} 
while (($name,$file) = each %xdf_datasets) {
    print XMLFILE "        <!-- the correct format for the following may be \"hdf\" - see documentation on the <dataset> tag -->";
    print XMLFILE "        <dataset name=\"$name\" file=\"$file\" format=\"nc\"/>\n";
} 

print XMLFILE <<BOB
      </data>
   </catalog>

   <privilege_mgr default="default_level">
BOB
;

for $level (@access_levels) {
    $name = ${$level}{"name"};
    $ip_match = (${$level}{"ip_match"} or "");
    $subset_limit = (${$level}{"subset_limit"} or "");
    $generate_limit = (${$level}{"generate_limit"} or "");
    $upload_limit = (${$level}{"upload_limit"} or "");
    $hit_limit = (${$level}{"hit_limit"} or "");
    $abuse_timeout = (${$level}{"abuse_timeout"} or "");
    $deny_datasets = (${$level}{"deny_datasets"} or "");
    $allow_datasets = (${$level}{"allow_datasets"} or "");
    print XMLFILE <<BOB
     <ip_range mask="$ip_match" privilege="$name" />

     <privilege name="$name" 
                dods_subset_size = "$subset_limit"
                analyze_storage = "$generate_limit"
                upload_storage = "$upload_limit"
                abuse_hits = "$hit_limit"
                abuse_timeout = "$abuse_timeout" />

BOB
    ; 
    for ($deny_datasets) {
	if (/./) {
	    print XMLFILE "      <deny path=\"$_\" />\n";
	}
    }
    for ($allow_datasets) {
	if (/./) {
	    print XMLFILE "      <allow path=\"$_\" />\n";
	}
    }
}


print XMLFILE <<BOB
  </privilege_mgr>
</gds>
BOB
    ;

print "successfully converted $ininame to $xmlname\n";

#end of script
