import json
#import sys
import numpy as np
import netCDF4 as netcdf
import datetime
import dateutil.relativedelta as relativedelta

#Define the main information
lat = -34.625
lon = 19.875
idate_datetime = datetime.datetime(2001,1,1)
fdate_datetime = datetime.datetime(2001,1,10)
idate = int(idate_datetime.strftime("%s"))
fdate = int(fdate_datetime.strftime("%s"))
tstep = "DAILY"
vars = ["spi1","spi3","spi6","spi12","vcpct"]

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
#data_out = []A
time = {'pointInterval':pointInterval,'iyear':idate_datetime.year,'imonth':idate_datetime.month,'iday':idate_datetime.day}
data_out = {}
data_out["TIME"] = time
data_out["VARIABLES"] = {}
data = []

#keys = fp.groups.keys()
#VARIABLES - time and data
grp_tstep = fp.groups[tstep]
for key_grp in grp_tstep.groups:
 grp = grp_tstep.groups[key_grp]
 #Figure out the time steps to extract
 time = np.array(grp.variables["time"][:])
 idx = list(np.where((time >= idate) & (time <= fdate)))[0]
 #Extract all the variables
 for var_key in grp.variables:
  if var_key != 'time' and (var_key in vars) == True:
   variables.append([key_grp,var_key])
   var_data = grp.variables[var_key][idx]
   #var_data[var_data == -9.99e+08] = float('NaN')
   data_out['VARIABLES'][var_key] = {}
   data_out['VARIABLES'][var_key]['data'] = [float(b) for b in list(var_data)]
   data_out['VARIABLES'][var_key]['minval'] = np.float64(np.min(var_data))
   data_out['VARIABLES'][var_key]['maxval'] = np.float64(np.max(var_data))
   data_out['VARIABLES'][var_key]['units'] = 'mm'
   data_out['VARIABLES'][var_key]['long_name'] = 'long_name'
   #data.append(list(np.float64(var_data)))

#Create final list for highcharts
#data_out.append(variables)
#for list in data:
# data_out.append(list)

#Print json
print json.dumps(data_out)

'''
#Define the initial time step
#variables = ("Precipitation","Evaporation","Runoff","Baseflow","Soil Moisture 1",
#       "Soil Moisture 2","Soil Moisture 3","Wetness 1","Wetness 2","Wetness 3","Drought Index")
variables = ("Discharge","Runoff","Percentile")
data_out = []
itime = ("daily",1990,1,1)
data_out.append(itime)
data_out.append(variables)
#Append the previous data
for j in range(data.shape[0]):
 data_out.append(list(data[j]))
#Print out the json data to php
print json.dumps(data_out)

#Script to read in the text file and prepare the string for javascript
f = open(sys.argv[1],'r')
table = []
count = 0
i = 0
for area in f:
	count = count + 1
	if count > 1:
		temp = area.strip().split()
		print temp
		if (table == []):
			itime = ("daily",temp[0],temp[1],temp[2])
			print itime
			table.append([])
			for j in range(0,len(temp)):
				table.append([])
		for j in range(0,len(temp)):
			table[j].append(float(temp[j]))
	
variables = ("Soil Moisture (%)","Precipitation (%)","Temperature (%)","Soil Moisture (mm)","Precipitation (mm/day)","Temperature (C)")
colors = ("#A0C544","#488AC7","#C34A2C","#A0C544","#488AC7","#C34A2C")
types = ("spline","spline","spline","spline","column","spline")
iflag = ("1","1","1","0","0","0")
groups = ("Percentiles","Actual Values")
igrp = ("1","0")
yaxis = (0,0,0,0,1,2)
data_out = []
data_out.append(itime)
data_out.append(variables)
data_out.append(types)
data_out.append(colors)
data_out.append(iflag)
data_out.append(sys.argv[2])
data_out.append(groups)
data_out.append(igrp)
data_out.append(yaxis)
#Append the previous data
for j in range(3,9):
	data_out.append(table[j])
#Print out the json data to php
print json.dumps(data_out)
'''
