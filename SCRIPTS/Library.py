import os
import datetime
import grads
import numpy as np
#import fileinput
import netCDF4 as netcdf
#import pyhdf.SD as sd
#import matplotlib.pyplot as plt
#import scipy.stats as ss
#import subprocess
#import dateutil.relativedelta as relativedelta
#from cython.parallel import prange
grads_exe = '../LIBRARIES/grads-2.0.1.oga.1/Contents/grads'
ga = grads.GrADS(Bin=grads_exe,Window=False,Echo=False)

def Check_and_Make_Directory(dir):

 #Check if a directory exists and if not create it
 if os.path.exists(dir) == False:
  os.system("mkdir %s" % dir)
  
 return
 
def Setup_Routines():

 #Setup the main directories
 data_dir = "../DATA"
 Check_and_Make_Directory(data_dir)

 #Install the required software

 return

def datetime2gradstime(date):

 #Convert datetime to grads time
 str = date.strftime('%HZ%d%b%Y')

 return str

def Grads_Regrid(var_in,var_out,dims):

 ga("%s = re(%s,%d,linear,%f,%f,%d,linear,%f,%f)" % (var_out,var_in,dims['nlon'],dims['minlon'],dims['res'],dims['nlat'],dims['minlat'],dims['res']))

 return

def Create_NETCDF_File(dims,file,vars,vars_info,tinitial,tstep,nt):

 nlat = dims['nlat']
 nlon = dims['nlon']
 res = dims['res']
 minlon = dims['minlon']
 minlat = dims['minlat']
 t = np.arange(0,nt)

 #Prepare the netcdf file
 #Create file
 f = netcdf.Dataset(file, 'w')

 #Define dimensions
 f.createDimension('lon',nlon)
 f.createDimension('lat',nlat)
 f.createDimension('t',len(t))

 #Longitude
 f.createVariable('lon','d',('lon',))
 f.variables['lon'][:] = np.linspace(minlon,minlon+res*(nlon-1),nlon)
 f.variables['lon'].units = 'degrees_east'
 f.variables['lon'].long_name = 'Longitude'
 f.variables['lon'].res = res

 #Latitude
 f.createVariable('lat','d',('lat',))
 f.variables['lat'][:] = np.linspace(minlat,minlat+res*(nlat-1),nlat)
 f.variables['lat'].units = 'degrees_north'
 f.variables['lat'].long_name = 'Latitude'
 f.variables['lat'].res = res

 #Time
 times = f.createVariable('t','d',('t',)) 
 f.variables['t'][:] = t
 f.variables['t'].units = '%s since %04d-%02d-%02d %02d:00:00.0' % (tstep,tinitial.year,tinitial.month,tinitial.day,tinitial.hour)
 f.variables['t'].long_name = 'Time'

 #Data
 i = 0
 for var in vars:
  f.createVariable(var,'f',('t','lat','lon'),fill_value=-9.99e+08)
  f.variables[var].long_name = vars_info[i]
  i = i + 1

 return f

def Download_and_Process_Forcing(date,dims):

 #Define the grads server root
 if date <= datetime.datetime(2008,12,31):
  http_root = "http://freeze.princeton.edu:9090/dods/AFRICAN_WATER_MONITOR/PRINCETON_GLOBAL_FORCING"

 #Make sure the main directory exists
 forcing_dir = "../DATA/FORCING/"
 Check_and_Make_Directory(forcing_dir)

 #Make sure the daily directory exists
 daily_dir = "../DATA/FORCING/DAILY"
 Check_and_Make_Directory(daily_dir)

 #Download the daily data
 vars = ["prec","tmax","tmin","wind"]
 vars_info = ["daily tmax (k)","daily tmin (k)","daily total precip (mm)","daily mean wind speed (m/s)"]
 ga("sdfopen %s/DAILY" % http_root)

 #Set grads region
 ga("set lat %f %f" % (dims['minlat'],dims['maxlat']))
 ga("set lon %f %f" % (dims['minlon'],dims['maxlon']))

 #Regrid and write variables to file
 time = datetime2gradstime(date)
 ga("set time %s" % time)
 file = '../DATA/FORCING/DAILY/forcing_%04d%02d%02d_daily.nc' % (date.year,date.month,date.day)
 fp = Create_NETCDF_File(dims,file,vars,vars_info,date,'days',1)
 data = []
 for var in vars:
  Grads_Regrid(var,'data',dims)
  fp.variables[var][0] = np.ma.getdata(ga.exp('data'))

 #Close files 
 ga("close 1")
 fp.close()

 #Create/Update the control file
 file = '../DATA/FORCING/DAILY/forcing_daily.ctl'
 fp = open(file,'w')
 fp.write('dset ^forcing_%s%s%s_daily.nc\n' % ('%y4','%m2','%d2'))
 fp.write('options template\n')
 fp.write('dtype netcdf\n')
 fp.write('tdef t %d linear %s %s\n' % (100,'1JAN1948','1dy'))
 fp.close()

 #Download the monthly file
 #Make sure the daily directory exists
 monthly_dir = "../DATA/FORCING/MONTHLY"
 Check_and_Make_Directory(monthly_dir)

 #Make sure the daily directory exists
 yearly_dir = "../DATA/FORCING/YEARLY"
 Check_and_Make_Directory(yearly_dir)
 #ga("sdfopen %s/MONTHLY" % http_root)

 #ga("close 1")

 #Download the yearly file
 #ga("sdfopen %s/YEARLY" % http_root)

 #ga("close 1")

 return
 
