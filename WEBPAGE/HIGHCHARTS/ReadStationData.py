import json
import numpy as np
import netCDF4 as netcdf
import datetime

#Parse the JSON string
metadata = json.loads(raw_input())
idate = int(metadata["idate"])
fdate = int(metadata["fdate"])
lat = float(metadata["lat"])
lon = float(metadata["lon"])
tstep = metadata["tstep"]
idate_datetime = datetime.datetime.utcfromtimestamp(idate)
fdate_datetime = datetime.datetime.utcfromtimestamp(fdate)
info = {"SPI":["spi1","spi3","spi6","spi12"]}#["spi1","spi3","spi6","spi12","vcpct"]

#Define the time step for highcharts
if tstep == "DAILY":
 pointInterval = 24*3600*1000
elif tstep == "MONTHLY":
 pointInterval = 30.4375*24*3600*1000
elif tstep == "YEARLY":
 pointInterval = 365.25*24*3600*1000

#Read in the desired data
file = '../../DATA/CELL/cell_%0.3f_%0.3f.nc' % (lat,lon)
fp = netcdf.Dataset(file,'r',format='NETCDF4')
variables = []
date = {'pointInterval':pointInterval,'iyear':idate_datetime.year,'imonth':idate_datetime.month,'iday':idate_datetime.day}
data_out = {}
data_out["TIME"] = date
data_out["VARIABLES"] = {}
data = []

#Choose the datasets
for dataset in info:
 date = np.array(fp.groups[tstep].groups[dataset].variables["time"][:])
 idx = list(np.where((date >= idate) & (date <= fdate)))[0]
 #Choose the variables
 for var in info[dataset]:
  var_data = fp.groups[tstep].groups[dataset].variables[var][idx]
  data_out['VARIABLES'][var] = {}
  data_out['VARIABLES'][var]['data'] = list(np.float64(var_data))
  data_out['VARIABLES'][var]['units'] = 'mm'
  data_out['VARIABLES'][var]['long_name'] = 'long_name'
  data_out['VARIABLES'][var]['dataset'] = dataset

#Print json
print json.dumps(data_out)
