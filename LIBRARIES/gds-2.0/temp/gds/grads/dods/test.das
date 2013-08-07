Attributes {
    NC_GLOBAL {
        String title "Real-Time Three Hourly TRMM and Other Satellite Rainfall (3B42RT)";
        String Conventions "COARDS", "GrADS";
        String dataType "Grid";
        String history "Tue Jul 16 20:28:19 EDT 2013 : imported by GrADS Data Server 2.0";
        String extra_das_attribute "This is an example of metadata added using a supplementary DAS file. See testdata/extra.das for the source.";
    }
    sst {
        String extra_config_file_attribute "An attribute from the GDS configuration file.", " Attributes can contain multiple lines if needed.";
    }
    lon {
        String grads_dim "x";
        String grads_mapping "linear";
        String grads_size "1440";
        String units "degrees_east";
        String long_name "longitude";
        Float64 minimum 0.12500000000;
        Float64 maximum 359.87500000000;
        Float32 resolution 0.25;
    }
    lat {
        String grads_dim "y";
        String grads_mapping "linear";
        String grads_size "480";
        String units "degrees_north";
        String long_name "latitude";
        Float64 minimum -59.87500000000;
        Float64 maximum 59.87500000000;
        Float32 resolution 0.25;
    }
    time {
        String grads_dim "t";
        String grads_mapping "linear";
        String grads_size "9";
        String grads_min "00z12jun2013";
        String grads_step "3hr";
        String units "days since 1-1-1 00:00:0.0";
        String long_name "time";
        String minimum "00z12jun2013";
        String maximum "00z13jun2013";
        Float32 resolution 0.125;
    }
    p {
        Float32 _FillValue -99999.0;
        Float32 missing_value -99999.0;
        String long_name "precipitation (mm/hr) ";
    }
}
