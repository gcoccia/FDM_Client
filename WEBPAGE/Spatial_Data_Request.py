import sys
import smtplib
from email.mime.text import MIMEText
import datetime
import dateutil.relativedelta as relativedelta
import grads
import numpy as np
import netCDF4 as netcdf
import random
import os

def Write_Arc_Ascii(dims,file,data):

 #Output data in arc ascii format
 ncols = dims["nlon"]
 nrows = dims["nlat"]
 xllcorner = dims["minlon"] - dims["res"]/2
 yllcorner = dims["minlat"] - dims["res"]/2
 cellsize = dims["res"]
 undef = -9.99e+08
 header = 'ncols %d\nnrows %d\nxllcorner %f\nyllcorner %f\ncellsize %f\n NODATA_value %.3f' % (ncols,nrows,xllcorner,yllcorner,cellsize,undef)
 comments=''
 data = np.flipud(data)
 np.savetxt(file,data,fmt='%.3f',header=header,comments=comments)
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

def Grads_Regrid(var_in,var_out,dims):

 ga("%s = re(%s,%d,linear,%f,%f,%d,linear,%f,%f)" % (var_out,var_in,dims['nlon'],dims['minlon'],dims['res'],dims['nlat'],dims['minlat'],dims['res']))

 return

def datetime2gradstime(date):

 #Convert datetime to grads time
 str = date.strftime('%HZ%d%b%Y')

 return str

def Send_Email(txt):

 sender = "nchaney@princeton.edu"
 receiver = email
 msg = MIMEText(txt)
 msg['Subject'] = 'African Water Monitor Data Request'
 msg['From'] = 'nchaney@princeton.edu'
 msg['To'] = email
 s = smtplib.SMTP('localhost')
 s.sendmail(sender,receiver,msg.as_string())
 s.quit()

 return

#Read the command line arguments
#tstep $llclat $llclon $urclat $urclon $iyear $imonth $iday $fyear $fmonth $fday $format $email $variables
tstep = sys.argv[1]
llclat = float(sys.argv[2])
llclon = float(sys.argv[3])
urclat = float(sys.argv[4])
urclon = float(sys.argv[5])
idate = datetime.datetime(int(sys.argv[6]),int(sys.argv[7]),int(sys.argv[8]))
fdate = datetime.datetime(int(sys.argv[9]),int(sys.argv[10]),int(sys.argv[11]))
format= sys.argv[12]
email = sys.argv[13]
variables = sys.argv[14].split("/")
res = float(sys.argv[15])
http_root = 'http://freeze.princeton.edu/'

#Determine the time step
print tstep
if tstep == "daily":
 dt = relativedelta.relativedelta(days=1)
 nt = (fdate-idate).days
if tstep == "monthly":
 dt = relativedelta.relativedelta(month=1)
 nt = (fdate-idate).days/30
if tstep == "yearly":
 dt = relativedelta.relativedelta(year=1)
 nt = (fdate-idate).days/30

#Define the monitor's boundaries
minlat = -35.0
minlon = -19.0
maxlat = 38.0
maxlon = 55.0

#Not allow for more than 1 gb request
nlat = (urclat - llclat)/res
nlon = (urclon - llclon)/res
nvars = len(variables)
nmb = 4*nlat*nlon*nvars*nt/1024/1024

#Define dimensions
dims = {}
dims['minlat'] = minlat #-89.8750
dims['minlon'] = minlon #0.1250
dims['maxlat'] = maxlat
dims['maxlon'] = maxlon
dims['res'] = res
dims['nlat'] = np.int(np.ceil((dims['maxlat'] - dims['minlat'])/ dims['res'] + 1))
dims['nlon'] = np.int(np.ceil((dims['maxlon'] - dims['minlon'])/ dims['res'] + 1))
dims['maxlat'] = dims['minlat'] + dims['res']*(dims['nlat']-1)
dims['maxlon'] = dims['minlon'] + dims['res']*(dims['nlon']-1)

#Remove old files
os.system("find ../WORKSPACE/* -mmin +1 -exec rm -rf {} \;")

#Run some initial checks 
if idate < datetime.datetime(1950,1,1):
 Send_Email('The monitor does not have data before January, 1st 1950. Please select a new time period.')
if len(variables) < 1:
 Send_Email('No variables were selected. Please select at least one variable.')
if llclat < minlat or llclon < minlon or urclat > maxlat or urclon > maxlon:
 Send_Email("Part or all of the requested region is outside of the monitor's domain. Please change the chosen region")
#FLAG - need to adjust if netcdf or arcascii
if nmb > 1000.0:
 Send_Email("Request is larger than 1 gigabyte. Please change either the domain, spatial resolution, temporal resolution, time range, or the number of variables.")

#Extract the data
#Create temporary directory
val = int(1000000.0*random.random())
dir = "../WORKSPACE/request_%d" % val
os.system("mkdir %s" % dir)

#Open Grads
grads_exe = '../LIBRARIES/grads-2.0.1.oga.1/Contents/grads'
ga = grads.GrADS(Bin=grads_exe,Window=False,Echo=False)
for var in variables:
 print var

 #Create directory for variable
 var_dir = dir + "/" + var
 os.system("mkdir %s" % var_dir)
 dataset = var.split("_")[1]
 ctl_file = "../DATA/DAILY/%s_%s.ctl" % (dataset,tstep)
 ga("xdfopen %s" % ctl_file)
 date = idate
 var = var.split("_")[0] 
 qh = ga.query("file")
 var_info = qh.var_titles[qh.vars.index(var)]

 #Set grads region
 ga("set lat %f %f" % (dims['minlat'],dims['maxlat']))
 ga("set lon %f %f" % (dims['minlon'],dims['maxlon']))

 while date <= fdate:
  time = datetime2gradstime(date)
  #Set time
  ga("set time %s" % time)
  #Regrid data
  Grads_Regrid(var,'data',dims)
  #Write data
  if format == "netcdf":
   file = var_dir + "/%s_%s_%04d%02d%02d.nc" % (var,dataset,date.year,date.month,date.day)
   fp = Create_NETCDF_File(dims,file,[var,],[var_info,],date,'days',1)
   fp.variables[var][0] = np.ma.getdata(ga.exp("data"))
   fp.close()
  elif format == "arc_ascii":
   #Send_Email("ARC-ascii output not yet implemented")
   #exit()
   file = var_dir + "/%s_%s_%04d%02d%02d.asc" % (var,dataset,date.year,date.month,date.day)
   Write_Arc_Ascii(dims,file,np.ma.getdata(ga.exp("data")))
  #Move to next time step
  date = date + dt
 ga("close 1")

#Zip up the directory
os.chdir('../WORKSPACE')
os.system("tar -czf request_%d.tar.gz request_%d" % (val,val) )
os.system("rm -rf request_%d" % val)

#Send the email confirming that it succeeded and the location of the zipped archive
http_file = http_root  + "/ADM/WORKSPACE/request_%d.tar.gz" % val
Send_Email("The data was processed and can be dowloaded at %s. The data will be removed in 6 hours." % http_file)
