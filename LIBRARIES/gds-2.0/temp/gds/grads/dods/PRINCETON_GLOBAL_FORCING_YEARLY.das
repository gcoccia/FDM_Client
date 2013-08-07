Attributes {
    NC_GLOBAL {
        String title "PRINCETON_GLOBAL_FORCING_YEARLY (untitled)";
        String Conventions "COARDS", "GrADS";
        String dataType "Grid";
        String history "Sat Aug 03 12:29:47 EDT 2013 : imported by GrADS Data Server 2.0";
    }
    lon {
        String grads_dim "x";
        String grads_mapping "linear";
        String grads_size "296";
        String units "degrees_east";
        String long_name "longitude";
        Float64 minimum -18.87500000000;
        Float64 maximum 54.87500000000;
        Float32 resolution 0.25;
    }
    lat {
        String grads_dim "y";
        String grads_mapping "linear";
        String grads_size "292";
        String units "degrees_north";
        String long_name "latitude";
        Float64 minimum -34.87500000000;
        Float64 maximum 37.87500000000;
        Float32 resolution 0.25;
    }
    time {
        String grads_dim "t";
        String grads_mapping "linear";
        String grads_size "61";
        String grads_min "00z01jan1948";
        String grads_step "12mo";
        String units "days since 1-1-1 00:00:0.0";
        String long_name "time";
        String minimum "00z01jan1948";
        String maximum "00z01jan2008";
        Float32 resolution 365.25;
    }
    tmax {
        Float32 _FillValue -9.99E8;
        Float32 missing_value -9.99E8;
        String long_name "tmax0t,y,xdaily tmax (k) ";
    }
    tmin {
        Float32 _FillValue -9.99E8;
        Float32 missing_value -9.99E8;
        String long_name "tmin0t,y,xdaily tmin (k) ";
    }
    prec {
        Float32 _FillValue -9.99E8;
        Float32 missing_value -9.99E8;
        String long_name "prec0t,y,xdaily total precip (mm) ";
    }
    wind {
        Float32 _FillValue -9.99E8;
        Float32 missing_value -9.99E8;
        String long_name "wind0t,y,xdaily mean wind speed (m/s) ";
    }
}
