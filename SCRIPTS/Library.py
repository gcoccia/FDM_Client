import matplotlib as mpl
mpl.use('Agg')
import matplotlib.pyplot as plt
import os
import datetime
import grads
import numpy as np
from mpl_toolkits.basemap import Basemap,cm
#import fileinput
import netCDF4 as netcdf
#import pyhdf.SD as sd
#import scipy.stats as ss
#import subprocess
import dateutil.relativedelta as relativedelta
#from cython.parallel import prange
grads_exe = '../LIBRARIES/grads-2.0.1.oga.1/Contents/grads'
ga = grads.GrADS(Bin=grads_exe,Window=False,Echo=False)

def Check_and_Make_Directory(dir):

 #Check if a directory exists and if not create it
 if os.path.exists(dir) == False:
  os.system("mkdir %s" % dir)
  
 return
 
def Setup_Routines(date):

 #Setup the main directories
 dir = "../DATA"
 Check_and_Make_Directory(dir)
 dir = "../IMAGES"
 Check_and_Make_Directory(dir)

 #Setup the daily directories
 dir = "../DATA/DAILY"
 Check_and_Make_Directory(dir)
 dir = "../DATA/DAILY/%04d%02d%02d" % (date.year,date.month,date.day)
 Check_and_Make_Directory(dir)
 dir = "../IMAGES/DAILY"
 Check_and_Make_Directory(dir)
 dir = "../IMAGES/DAILY/%04d%02d%02d" % (date.year,date.month,date.day)
 Check_and_Make_Directory(dir)

 #Setup the monthly directories
 dir = "../DATA/MONTHLY"
 Check_and_Make_Directory(dir)
 dir = "../DATA/MONTHLY/%04d%02d" % (date.year,date.month)
 Check_and_Make_Directory(dir)
 dir = "../IMAGES/MONTHLY"
 Check_and_Make_Directory(dir)
 dir = "../IMAGES/MONTHLY/%04d%02d" % (date.year,date.month)
 Check_and_Make_Directory(dir)

 #Setup the yearly directories
 dir = "../DATA/YEARLY"
 Check_and_Make_Directory(dir)
 dir = "../DATA/YEARLY/%04d" % (date.year)
 Check_and_Make_Directory(dir)
 dir = "../IMAGES/YEARLY"
 Check_and_Make_Directory(dir)
 dir = "../IMAGES/YEARLY/%04d" % (date.year)
 Check_and_Make_Directory(dir)

 return

def datetime2gradstime(date):

 #Convert datetime to grads time
 str = date.strftime('%HZ%d%b%Y')

 return str

def gradstime2datetime(str):

 #Convert grads time to datetime
 date = datetime.datetime.strptime(str,'%HZ%d%b%Y')

 return date

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

def Download_and_Process(date,dims,tstep,dataset,info):

 #If monthly time step only extract at end of month
 if tstep == "MONTHLY" and (date + datetime.timedelta(days=1)).month == date.month:
  return

 #If yearly time step only extract at end of month
 if tstep == "YEARLY" and (date + datetime.timedelta(days=1)).year == date.year:
  return

 #Define the grads server root
 http_file = "http://freeze.princeton.edu:9090/dods/AFRICAN_WATER_CYCLE_MONITOR/%s/%s" % (dataset,tstep)

 #Open access to grads data server
 ga("sdfopen %s" % http_file)

 #Extract basic info
 qh = ga.query("file")
 #vars = []
 #vars_info = []
 #vars_file = qh.vars
 #vars_info_file = qh.var_titles
 #for var in info:
 # vars.append(var)
 # vars_info.append(vars_info_file[vars_file.index(var)])
 vars = qh.vars
 vars_info = qh.var_titles
 nt = qh.nt
 ga("set t 1")
 idate = gradstime2datetime(ga.exp(vars[0]).grid.time[0])

 #If the date is before the first time step return
 if date < idate:
  ga("close 1")
  return
 
 #Set grads region
 ga("set lat %f %f" % (dims['minlat'],dims['maxlat']))
 ga("set lon %f %f" % (dims['minlon'],dims['maxlon']))

 #Regrid and write variables to file
 time = datetime2gradstime(date)
 ga("set time %s" % time)
 if tstep == "DAILY":
  file = '../DATA/DAILY/%04d%02d%02d/%s_%04d%02d%02d_daily.nc' % (date.year,date.month,date.day,dataset,date.year,date.month,date.day)
 if tstep == "MONTHLY":
  file = '../DATA/MONTHLY/%04d%02d/%s_%04d%02d_monthly.nc' % (date.year,date.month,dataset,date.year,date.month)
 if tstep == "YEARLY":
  file = '../DATA/YEARLY/%04d/%s_%04d_yearly.nc' % (date.year,dataset,date.year)
 if os.path.exists(file) == True:
  ga("close 1")
  return
 fp = Create_NETCDF_File(dims,file,vars,vars_info,date,'days',1)
 data = []
 for var in vars:
  Grads_Regrid(var,'data',dims)
  fp.variables[var][0] = np.ma.getdata(ga.exp('data'))

 #Close files 
 ga("close 1")
 fp.close()

 #Create/Update the control file
 file = '../DATA/%s/%s_%s.ctl' % (tstep,dataset,tstep)
 fp = open(file,'w')
 if tstep == "DAILY":
  fp.write('dset ^%s%s%s/%s_%s%s%s_daily.nc\n' % ('%y4','%m2','%d2',dataset,'%y4','%m2','%d2'))
 if tstep == "MONTHLY":
  fp.write('dset ^%s%s/%s_%s%s_monthly.nc\n' % ('%y4','%m2',dataset,'%y4','%m2'))
 if tstep == "YEARLY":
  fp.write('dset ^%s/%s_%s_yearly.nc\n' % ('%y4',dataset,'%y4'))
 fp.write('options template\n')
 fp.write('dtype netcdf\n')
 if tstep == "DAILY":
  fp.write('tdef t %d linear %s %s\n' % (nt,datetime2gradstime(idate),'1dy'))
 if tstep == "MONTHLY":
  fp.write('tdef t %d linear %s %s\n' % (nt,datetime2gradstime(idate),'1mo'))
 if tstep == "YEARLY":
  fp.write('tdef t %d linear %s %s\n' % (nt,datetime2gradstime(idate),'1yr'))
 fp.close()

 return

def datetime2outputtime(date,timestep):
 
 #Convert datetime to output time
 if timestep == "DAILY":
  str = date.strftime('%Y%m%d')
 elif timestep == "MONTHLY":
  str = date.strftime('%Y%m')
 elif timestep == "YEARLY":
  str = date.strftime('%Y')

 return str

def Create_Images(date,dims,dataset,timestep):
 
 #Create mask
 ga("xdfopen ../DATA/DAILY/VIC_PGF_DAILY.ctl")
 ga("mask = const(const(sm1,1),-1,-u)")
 mask = np.ma.getdata(ga.exp("mask"))
 ga("close 1")

 #Open control file
 ga("xdfopen ../DATA/%s/%s_%s.ctl" % (timestep,dataset,timestep))
 #Define timestamp
 date_output = datetime2outputtime(date,timestep) 
 #Extract all variable information
 qh = ga.query("file")
 #Create images for all variables
 for var in qh.vars:
  image_file = '../IMAGES/%s/%s/%s_%s_%s.png'  % (timestep,date_output,dataset,var,date_output)
  ga("set time %s" % datetime2gradstime(date))
  data = ga.exp("maskout(%s,mask)" % var)
  #data = ga.exp("%s" % var)
  #data = np.ma.masked_where(mask == 0,data)
  Create_Image(image_file,data)

 #Close access to file
 ga("close 1")

 return

def Create_Image(file,data):

 #Extract grid info
 undef = -9.99e+08
 lats = data.grid.lat
 lons = data.grid.lon
 #levels = [0,1,2.5,5,7.5,10,15,20,30,40,50,70,100,150,200,250,300,400,500,600,750]
 #levels = np.linspace(np.min(data[np.isnan(data) == 0]),np.max(data[np.isnan(data) == 0]),20)
 levels = np.linspace(np.min(data),np.max(data),30)
 #Create Basemap instance for Google maps mercator projection
 res = lats[1] - lats[0]
 llcrnrlat = lats[0]# - res/2
 urcrnrlat = lats[-1]# + res/2
 llcrnrlon = lons[0]# - res/2
 urcrnrlon = lons[-1]# + res/2
 m = Basemap(llcrnrlat=llcrnrlat,urcrnrlat=urcrnrlat,llcrnrlon=llcrnrlon,urcrnrlon=urcrnrlon,epsg=3857)
 lons,lats = np.meshgrid(lons,lats)
 width = 10
 height = width*m.aspect
 #Plot image
 fig = plt.figure(frameon=False)
 fig.set_size_inches(width,height)
 ax = plt.Axes(fig,[0., 0., 1., 1.])
 #ax.set_axis_bgcolor('none')
 ax.set_axis_off()
 ax.m = m
 fig.add_axes(ax)
 plt.axis('off')
 #x,y = m(x,y)
 #x, y = m(*np.meshgrid(lons,lats))
 #cs = m.contourf(x,y,data,levels=levels,linewidhts=0,cmap=cm.s3pcpn,linestyles=None,linewidths=None)
 #cs = m.contourf(x,y,data,linewidhts=0,cmap=cm.s3pcpn,linestyles=None,linewidths=None)
 cs = m.pcolormesh(lons,lats,data,latlon=True,shading='flat')
 #cs = m.contourf(lons,lats,data,latlon=True,levels=levels)
 #cs = m.imshow(data,interpolation='nearest')
 #plt.imshow(np.flipud(data),interpolation='nearest')
 plt.savefig(file,transparent=True)#,pad_inches=0.0,bbox_inches='tight')#,transparent=True)
 #os.system("convert %s -trim %s" % (file,file))

 return

def Create_and_Update_Point_Data(idate,fdate,tstep,dataset,info):

 #Only works for daily for now
 #If monthly time step only extract at end of month
 print tstep
 if tstep == "MONTHLY" and ((fdate+datetime.timedelta(days=1)).month == idate.month) and ((fdate+datetime.timedelta(days=1)).year == idate.year):
  return
 #If yearly time step only extract at end of month
 if tstep == "YEARLY" and ((fdate+datetime.timedelta(days=1)).year == idate.year):
  return

 #Create mask
 ga("xdfopen ../DATA/DAILY/VIC_PGF_DAILY.ctl")
 ga("mask = const(const(sm1,1),0,-u)")
 mask = ga.exp("mask")
 ga("close 1")
 lats = mask.grid.lat[0:2]
 lons = mask.grid.lon#[0:100]
 mask = np.ma.getdata(mask)

 #Open dataset control file and read information
 try:
  ga("xdfopen ../DATA/%s/%s_%s.ctl" % (tstep,dataset,tstep))
 except:
  return
 group = dataset
 vars_file = ga.query("file").vars
 vars_info_file = ga.query("file").var_titles
 variables = []
 for var in info:
  variables.append(var)
  #vars_info.append(vars_info_file[vars_file.index(var)])
 ga("set t 1")
 idate_dataset = gradstime2datetime(ga.exp(variables[0]).grid.time[0])

 #Define current time step
 if tstep == "DAILY":
  t_initial = (idate - idate_dataset).days
  t_final = (fdate - idate_dataset).days
 if tstep == "MONTHLY":
  t_initial = idate.month - idate_dataset.month + (idate.year - idate_dataset.year) * 12
  t_final = fdate.month - idate_dataset.month + (fdate.year - idate_dataset.year) * 12
  idate = datetime.datetime(idate.year,idate.month,1)
  fdate = datetime.datetime(fdate.year,fdate.month,1)
 if tstep == "YEARLY":
  t_initial = idate.year - idate_dataset.year
  t_final = fdate.year - idate_dataset.year
  idate = datetime.datetime(idate.year,1,1)
  fdate = datetime.datetime(fdate.year,1,1)

 #Leave if before initial time step
 if idate < idate_dataset:
  ga("close 1")
  return

 #Iterate through grid cells
 ga("set time %s %s" % (datetime2gradstime(idate),datetime2gradstime(fdate)))
 data = []
 for var in variables:
  data.append(ga.exp(var))

 #Create date string
 if tstep == "DAILY": 
  dt = datetime.timedelta(days=1)
 if tstep == "MONTHLY": 
  dt = relativedelta.relativedelta(months=1)
 if tstep == "YEARLY": 
  dt = relativedelta.relativedelta(years=1)

 time_str = []
 date = idate
 while date <= fdate:
  time_str.append(int(date.strftime("%s")))
  date = date + dt

 for ilat in range(lats.size):
  print lats[ilat]
  for ilon in range(lons.size):
   if mask[ilat,ilon] != 0:
    #Assign region
    #ga("set lat %f" % lats[ilat])
    #ga("set lon %f" % lons[ilon])

    #Determine if file exists 
    file = '../DATA/CELL/cell_%0.3f_%0.3f.nc' % (lats[ilat],lons[ilon])
    if os.path.exists(file) == False:
     fp = netcdf.Dataset(file,'w',format='NETCDF4')
    else:
     fp = netcdf.Dataset(file,'a',format='NETCDF4')

    #Determine if time step group exists
    try:
     grp_tstep = fp.groups[tstep]
    except:
     grp_tstep = fp.createGroup(tstep)

    #Determine if the dataset group exists
    try:
     grp = grp_tstep.groups[group]
    except:
     grp = grp_tstep.createGroup(group)

    #Determine if dimensions exist
    try:
     dim = grp.dimensions['time']
    except:
     dim = grp.createDimension('time',None)

    #Determine if time variable exists
    try: 
     time = grp.variables['time']
    except:
     time = grp.createVariable('time','i4',('time',))

    ivar = 0
    for variable in variables:
     #Determine if variables exist
     try:
      var = grp.variables[variable]
     except:
      var = grp.createVariable(variable,'f4',('time',))

     #Assign data
     #data = ga.eval(variable)
     #data = variable#ga.eval(variable)
     if t_final - t_initial == 0:
      var[t_initial:t_final+1] = data[ivar][ilat,ilon]
     else:
      var[t_initial:t_final+1] = data[ivar][:,ilat,ilon]
     time[t_initial:t_final+1] = time_str
     ivar = ivar + 1
     
    #Close the file
    fp.close() 
  
 #Close the grads file
 ga("close 1")

 return
