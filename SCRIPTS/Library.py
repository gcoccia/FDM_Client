import matplotlib as mpl
mpl.use('Agg')
import matplotlib.pyplot as plt
import os
import datetime
import grads
import numpy as np
from mpl_toolkits.basemap import Basemap,cm
import time
#import fileinput
import netCDF4 as netcdf
#import pyhdf.SD as sd
#import scipy.stats as ss
#import subprocess
import dateutil.relativedelta as relativedelta
#from cython.parallel import prange
grads_exe = '../LIBRARIES/grads-2.0.1.oga.1/Contents/grads'
ga = grads.GrADS(Bin=grads_exe,Window=False,Echo=False)
xml_settings = '../web_nchaney/settings.xml'

def print_info_to_command_line(line):

 print "#######################################################################################"
 print "%s" % line
 print "#######################################################################################"
 print "\n"

 return

def Update_XML(group_name,var_name,dataset_name,itime):
 tree = ET.parse(xml_settings)
 root = tree.getroot()
 for group in root.find('variables'):
  if group.attrib['name'] == group_name:
   for var in group:
    if var.attrib['name'] == var_name:
     for dataset in var:
      if dataset.attrib['name'] == dataset_name:
       dataset.attrib['ftime'] = 'here'
       tree.write('test.xml')
 return

def Check_and_Make_Directory(dir):

 #Check if a directory exists and if not create it
 if os.path.exists(dir) == False:
  os.system("mkdir %s" % dir)
  
 return
 
def Setup_Routines(date):

 #Setup the main directories
 dir = "../DATA_GRID"
 Check_and_Make_Directory(dir)
 dir = "../DATA_CELL"
 Check_and_Make_Directory(dir)
 dir = "../IMAGES"
 Check_and_Make_Directory(dir)

 #Setup the control file directory
 dir = "../DATA_GRID/CTL"
 Check_and_Make_Directory(dir)

 #Setup the yearly directory
 dir = "../DATA_GRID/%04d" % (date.year)
 Check_and_Make_Directory(dir)
 dir = "../IMAGES/%04d" % (date.year)
 Check_and_Make_Directory(dir)

 #Setup the monthly directory
 dir = "../DATA_GRID/%04d/%02d" % (date.year,date.month)
 Check_and_Make_Directory(dir)
 dir = "../IMAGES/%04d/%02d" % (date.year,date.month)
 Check_and_Make_Directory(dir)

 #Setup the daily directory
 dir = "../DATA_GRID/%04d/%02d/%02d" % (date.year,date.month,date.day)
 Check_and_Make_Directory(dir)
 dir = "../IMAGES/%04d/%02d/%02d" % (date.year,date.month,date.day)
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

def Create_Mask(dims):

 #Define file
 file = '../DATA_GRID/MASKS/mask.nc' 
 if os.path.exists(file):
  return

 #Define http files
 http_file1 = 'http://freeze.princeton.edu:9090/dods/AFRICAN_WATER_CYCLE_MONITOR/MASK'
 http_file2 = 'http://freeze.princeton.edu:9090/dods/AFRICAN_WATER_CYCLE_MONITOR/MASK_200mm'
 http_file3 = 'http://freeze.princeton.edu:9090/dods/AFRICAN_WATER_CYCLE_MONITOR/MASK_SO'

 #Open file
 ga("sdfopen %s" % http_file1)
 ga("sdfopen %s" % http_file2)
 ga("sdfopen %s" % http_file3)
  
 #Set grads region
 ga("set lat %f %f" % (dims['minlat'],dims['maxlat']))
 ga("set lon %f %f" % (dims['minlon'],dims['maxlon']))

 #Regrid data
 Grads_Regrid('mask.1','mask1',dims)
 Grads_Regrid('mask.2','mask2',dims)
 Grads_Regrid('mask.3','mask3',dims)

 #Write to file
 fp = Create_NETCDF_File(dims,file,['mask','mask200','maskSO'],['mask','mask200','maskSO'],datetime.datetime(1900,1,1),'days',1)
 fp.variables['mask'][0] = np.ma.getdata(ga.exp("mask1"))
 fp.variables['mask200'][0] = np.ma.getdata(ga.exp("mask2"))
 fp.variables['maskSO'][0] = np.ma.getdata(ga.exp("mask3"))

 #Close files 
 ga("close 3")
 ga("close 2")
 ga("close 1")
 fp.close()

 return

def Download_and_Process_and_Create_Images_Seasonal_Forecast(date,dims,Reprocess_Flag):

 print_info_to_command_line('Downloading and Processing the 6-month Seasonal Forecast') 
 #Iterate through the seasonal forecasts
 models = ['CMC1-CanCM3','CMC2-CanCM4','COLA-RSMAS-CCSM3','GFDL-CM2p1-aer04','MultiModel','NASA-GMAO-062012']
 idate = datetime.datetime(2013,1,1)
 #Determine the ensemble number
 iensemble = 12*(date.year - idate.year) + max(date.month - idate.month,0) + 1
 #Download data
 for model in models:
  print model
  file = '../DATA_GRID/%04d/%02d/%s_monthly.nc' % (date.year,date.month,model)
  if os.path.exists(file) == False or Reprocess_Flag == True:
   #Open grads access
   http_file = "http://freeze.princeton.edu:9090/dods/AFRICAN_WATER_CYCLE_MONITOR/SEASONAL_FORECAST/%s" % model
   ga("sdfopen %s" % http_file)
   #Set the ensemble number
   ga("set e %d" % iensemble)
   #Extract variables
   qh = ga.query("file")
   vars = qh.vars
   vars_info = qh.var_titles
   #Create file
   fp = Create_NETCDF_File(dims,file,vars,vars_info,datetime.datetime(date.year,date.month,1),'months',6)
   #Add data
   date = date.replace(day=1)
   for i in xrange(0,6):
    ga("set time %s" % datetime2gradstime(date + i*relativedelta.relativedelta(months=1)))
    for var in vars:
     Grads_Regrid(var,'data',dims)
     fp.variables[var][i] = np.ma.getdata(ga.exp('data'))
   #Close files 
   ga("close 1")
   fp.close()

  #Create the updated control file
  fdate = date
  nmonths = 12*(fdate.year - idate.year) + max(fdate.month-idate.month,0) + 1
  ctl = '../DATA_GRID/CTL/%s_MONTHLY.ctl' % model
  fp = open(ctl,'w')
  fp.write('dset ^../%s/%s_monthly.nc\n' % ('%e',model))
  fp.write('options template\n')
  fp.write('dtype netcdf\n')
  fp.write('title Seasonal Forecast %s\n' % model)
  fp.write('undef -9.99e+08\n')
  fp.write('xdef %d  linear %f %f\n' % (dims['nlon'],dims['minlon'],dims['res']))
  fp.write('ydef %d  linear %f %f\n' % (dims['nlat'],dims['minlat'],dims['res']))
  fp.write('tdef %d linear 00Z01%s%d 1mo\n' % (nmonths+6,idate.strftime('%b'),idate.year))
  fp.write('zdef 1 linear 1 1\n')
  fp.write('edef %d\n' % nmonths)
  date_tmp = idate
  while date_tmp <= fdate:
   fp.write('%04d/%02d 6 00Z01%s%d 1mo\n' % (date_tmp.year,date_tmp.month,date_tmp.strftime('%b'),date_tmp.year))
   date_tmp = date_tmp + relativedelta.relativedelta(months=1)
  fp.write('endedef\n')
  fp.write('vars 7\n')
  fp.write('spi1 1 t,y,x data\n')
  fp.write('spi3 1 t,y,x data\n')
  fp.write('spi6 1 t,y,x data\n')
  fp.write('spi12 1 t,y,x data\n')
  fp.write('prec 1 t,y,x data\n')
  fp.write('t2ano 1 t,y,x data\n')
  fp.write('t2m 1 t,y,x data\n')
  fp.write('endvars\n')
  fp.close()

  #Open access to create images
  print_info_to_command_line('Creating Images for the Seasonal Forecast')

  #Open control file
  ga("open %s" % ctl)
  #Load mask file
  ga("sdfopen ../DATA_GRID/MASKS/mask.nc")
  #Extract all variable information
  qh = ga.query("file")
  variables = qh.vars
  #Create images for all variables
  date = date.replace(day=1)
  #Set the ensemble number
  print iensemble
  #Create model directory
  dir = '../IMAGES/%04d/%02d/%s'  % (date.year,date.month,model)
  if os.path.exists(dir) == False:
   os.mkdir(dir)
  ga("set e %d" % (iensemble))
  for t in xrange(0,6):
   #Set time step
   date_tmp = date + t*relativedelta.relativedelta(months=1)
   print datetime2gradstime(date_tmp)
   ga("set time %s" % datetime2gradstime(date_tmp))  
   print date
   for var in variables:#qh.vars:
    image_file = '../IMAGES/%04d/%02d/%s_%s_%04d%02d.png'  % (date.year,date.month,model,var,date_tmp.year,date_tmp.month)
    #Skip image if it exists and we don't want to reprocess it
    if os.path.exists(image_file) and Reprocess_Flag == False:
     continue
    #Add data
    ga("data = %s" % var)
    data = ga.exp("data")
    (cmap,levels,norm) = Define_Colormap(var,'DAILY')
    cflag = True
    Create_Image(image_file,data,cmap,levels,norm,cflag)
    colormap_file = '../IMAGES/COLORBARS/%s_%s_%s.png' % (model,var,'DAILY')
    Create_Colorbar(colormap_file,cmap,norm,var,levels)

  #Close access to file
  ga("close 2")
  ga("close 1")

 return

def Download_and_Process(date,dims,tstep,dataset,info,Reprocess_Flag):

 if dataset in ['CMC1-CanCM3','CMC2-CanCM4','COLA-RSMAS-CCSM3','GFDL-CM2p1-aer04','MultiModel','NASA-GMAO-062012']:
  Download_and_Process_and_Create_Images_Seasonal_Forecast(date,dims,Reprocess_Flag)
  return

 idate = info['itime']
 if date < idate:
  return info

 #If monthly time step only extract at end of month
 if tstep == "MONTHLY" and (date + datetime.timedelta(days=1)).month == date.month:
  return info

 #If yearly time step only extract at end of month
 if tstep == "YEARLY" and (date + datetime.timedelta(days=1)).year == date.year:
  return info

 print_info_to_command_line('Dataset: %s Timestep: %s (Downloading and Processing data)' % (dataset,tstep))

 #Define the grads server root
 http_file = "http://freeze.princeton.edu:9090/dods/AFRICAN_WATER_CYCLE_MONITOR/%s/%s" % (dataset,tstep)

 #Open access to grads data server
 ga("sdfopen %s" % http_file)

 #Extract basic info
 qh = ga.query("file")
 vars = qh.vars
 vars_info = qh.var_titles
 nt = qh.nt
 #ga("set t 1")
 #idate = gradstime2datetime(ga.exp(vars[0]).grid.time[0])

 #If the date is before the first time step return
 if date < idate:
  ga("close 1")
  return info

 #Determine the last date
 ga("set t last")
 fdate = gradstime2datetime(ga.exp(vars[0]).grid.time[0])
 info['ftime'] = fdate

 #Create/Update the control file
 file = '../DATA_GRID/CTL/%s_%s.ctl' % (dataset,tstep)
 fp = open(file,'w')
 if tstep == "DAILY":
  fp.write('dset ^../%s/%s/%s/%s_%s%s%s_daily.nc\n' % ('%y4','%m2','%d2',dataset,'%y4','%m2','%d2'))
 if tstep == "MONTHLY":
  fp.write('dset ^../%s/%s/%s_%s%s_monthly.nc\n' % ('%y4','%m2',dataset,'%y4','%m2'))
 if tstep == "YEARLY":
  fp.write('dset ^../%s/%s_%s_yearly.nc\n' % ('%y4',dataset,'%y4'))
 fp.write('options template\n')
 fp.write('dtype netcdf\n')
 if tstep == "DAILY":
  fp.write('tdef t %d linear %s %s\n' % (nt,datetime2gradstime(idate),'1dy'))
 if tstep == "MONTHLY":
  fp.write('tdef t %d linear %s %s\n' % (nt,datetime2gradstime(idate),'1mo'))
 if tstep == "YEARLY":
  fp.write('tdef t %d linear %s %s\n' % (nt,datetime2gradstime(idate),'1yr'))
 fp.close()
 
 #Set grads region
 ga("set lat %f %f" % (dims['minlat'],dims['maxlat']))
 ga("set lon %f %f" % (dims['minlon'],dims['maxlon']))

 #Regrid and write variables to file
 timestamp = datetime2gradstime(date)
 ga("set time %s" % timestamp)
 
 #Define new filename
 if tstep == "DAILY":
  file = '../DATA_GRID/%04d/%02d/%02d/%s_%04d%02d%02d_daily.nc' % (date.year,date.month,date.day,dataset,date.year,date.month,date.day)
 if tstep == "MONTHLY":
  file = '../DATA_GRID/%04d/%02d/%s_%04d%02d_monthly.nc' % (date.year,date.month,dataset,date.year,date.month)
 if tstep == "YEARLY":
  file = '../DATA_GRID/%04d/%s_%04d_yearly.nc' % (date.year,dataset,date.year)

  #If file exists, exit
 if os.path.exists(file) == True and Reprocess_Flag == False:
  ga("close 1")
  return info

 fp = Create_NETCDF_File(dims,file,vars,vars_info,date,'days',1)
 data = []
 for var in vars:
  Grads_Regrid(var,'data',dims)
  fp.variables[var][0] = np.ma.getdata(ga.exp('data'))

 #Close files 
 ga("close 1")
 fp.close()

 #Update the time parameters

 return info

def datetime2outputtime(date,timestep):
 
 #Convert datetime to output time
 if timestep == "DAILY":
  date_output = date.strftime('%Y%m%d')
  dir_output = date.strftime('%Y/%m/%d')
 elif timestep == "MONTHLY":
  date_output = date.strftime('%Y%m')
  dir_output = date.strftime('%Y/%m')
 elif timestep == "YEARLY":
  date_output= date.strftime('%Y')
  dir_output = date.strftime('%Y')

 return (dir_output,date_output)

def Create_Images(date,dims,dataset,timestep,info,Reprocess_Flag):
 
 variables = info['variables']
 idate = info['itime']
 fdate = info['ftime']
 if date < idate or date > fdate:
  return

 #If monthly time step only extract at end of month
 if timestep == "MONTHLY":
  if (date + datetime.timedelta(days=1)).month == date.month:
   return
  date = datetime.datetime(date.year,date.month,1)

 #If yearly time step only extract at end of month
 if timestep == "YEARLY": 
  if (date + datetime.timedelta(days=1)).year == date.year:
   return
  date = datetime.datetime(date.year,1,1)

 print_info_to_command_line('Dataset: %s Timestep: %s (Creating Images)' % (dataset,timestep))
 
 #Open control file
 ga("xdfopen ../DATA_GRID/CTL/%s_%s.ctl" % (dataset,timestep))
 #Load mask file
 ga("sdfopen ../DATA_GRID/MASKS/mask.nc")
 #Define timestamp
 (dir_output,date_output) = datetime2outputtime(date,timestep) 
 #Extract all variable information
 #qh = ga.query("file")
 #Create images for all variables
 for var in variables:#qh.vars:
  image_file = '../IMAGES/%s/%s_%s_%s.png'  % (dir_output,dataset,var,date_output)
  #Skip image if it exists and we don't want to reprocess it
  if os.path.exists(image_file) and Reprocess_Flag == False:
   continue
  ga("set time %s" % datetime2gradstime(date))
  ga("data = maskout(%s,%s.2(t=1))" % (var,variables[var]['mask']))
  if var in ["spi1","spi3","spi6","spi12","vcpct","vc1","vc2","pct30day"]:
   ga("data = smth9(data)")
  data = ga.exp("data")
  (cmap,levels,norm) = Define_Colormap(var,timestep)
  cflag = True
  if var in ['flw','flw_pct']:
   cflag = False
  Create_Image(image_file,data,cmap,levels,norm,cflag)
  colormap_file = '../IMAGES/COLORBARS/%s_%s_%s.png' % (dataset,var,timestep)
  Create_Colorbar(colormap_file,cmap,norm,var,levels)

 #Close access to file
 ga("close 2")
 ga("close 1")

 return

def Create_Colorbar(file,cmap,norm,var,levels):
 
 #if os.path.exists(file):
 # return
 #fig = plt.figure(figsize=(8,0.5))
 #ax = fig.add_axes([0.2,0.5,0.8,0.8]) 
 if var in ["prec"]:
  levels = levels[0:-1:2]#[0,2,3,5,10,15,20,30,50,100]
 if var in ["tmax"]:
  levels = [280,285,290,295,300,305,310,315]
 if var in ["tmin"]:
  levels = [265,270,275,280,285,290,295,300]
 if var in ["wind"]:
  levels = [0,0.5,1,1.5,2,2.5,3,3.5,4,4.5,5]
 if var in ["vc1","vc2"]:
  levels = [0,10,20,30,40,50,60,70,80,90,100]
 if var in ["r_net","net_short","net_long"]:
  levels = [-200,-100,0,100,200]
 if var in ["ndvi30"]:
  levels = [0,0.1,0.2,0.3,0.4,0.5,0.6,0.7,0.8,0.9,1.0]
 if var in ['t2ano']:
  levels = [-4,-3,-2,-1,0,1,2,3,4]
 if var in ['t2m']:
  levels = [0,5,10,15,20,25,30,35,40]
 fig = plt.figure(figsize=(8,0.5))
 ax = fig.add_axes([0.05, 0.4, 0.9, 0.8])
 cb = mpl.colorbar.ColorbarBase(ax,cmap=cmap,norm=norm,orientation='horizontal',ticks=levels)
 plt.savefig(file,transparent=True)
 plt.close()

 return

def Define_Colormap(var,timestep):

 #SPI
 if var in ["spi1","spi3","spi6","spi12"]:
  cpool = [ '#000000', '#813f41', '#ff0000', '#fd7e00', '#ffff00','#ffffff', '#b3ff00', '#00b400', '#017172', '#0000ff','#9600fe']
  cmap = mpl.colors.ListedColormap(cpool, 'indexed')
  levels = [-100.0,-2.00,-1.6,-1.3,-0.8,-0.5,0.5,0.8,1.3,1.6,2.00,100.0]
  norm = mpl.colors.BoundaryNorm(levels,ncolors=len(levels),clip=False)

 #Precipitation
 if var == "prec":
  levels = np.array([0,1,2,3,4,5,7.5,10,12.5,15,17.5,20,25.0,30,35.0,40,50,70,100])
  if timestep == "MONTHLY":
   levels = 25*levels
  if timestep == "YEARLY":
   levels = 80*levels
  cmap = cm.s3pcpn
  norm = mpl.colors.BoundaryNorm(levels,ncolors=len(levels), clip=False)
   
 #Temperature
 if var in ["tmax","tmin","t2ano","t2m"]:
  if var == "tmax":
   levels = np.linspace(280,315,40)
  if var == "tmin":
   levels = np.linspace(265,300,40)
  if var == "t2ano":
   levels = np.linspace(-4,4,40)
  if var == "t2m":
   levels = np.linspace(0,40,40)
  cmap = plt.cm.RdBu_r
  norm = mpl.colors.Normalize(vmin=np.min(levels),vmax=np.max(levels), clip=False)

 #Wind
 if var in ["wind"]:
  levels = np.linspace(0,5,40)
  cmap = plt.cm.RdBu_r
  norm = mpl.colors.Normalize(vmin=np.min(levels),vmax=np.max(levels), clip=False)

 #Soil Moisture Volumetric Water Content
 if var in ["vc1","vc2"]:
  levels = np.linspace(0,100,40)
  cmap = plt.cm.jet_r
  norm = mpl.colors.Normalize(vmin=np.min(levels),vmax=np.max(levels), clip=False)

 if var in ["vcpct","pct30day","flw_pct"]:
  levels = [1,5,10,20,30,70,80,90,95,99]
  cmap = plt.cm.RdYlGn
  norm = mpl.colors.BoundaryNorm(levels,ncolors=256, clip=False)

 #Streamflow
 if var in ["flw"]:
  #levels = [0,5000,10000,15000,20000,25000,30000,35000,40000]
  levels = [0,100,200,300,400,500,1000,1500,2000,2500,5000,10000,20000,30000]
  cmap = plt.cm.jet_r
  norm = mpl.colors.BoundaryNorm(levels,ncolors=256, clip=False)

 #Baseflow and Surface Runoff
 if var in ["evap","runoff","baseflow"]:
  levels = [0,1,2,3,4,5,7.5,10,12.5,15,17.5,20,25.0,30,35.0,40,50,70,100]
  cmap = cm.s3pcpn
  norm = mpl.colors.BoundaryNorm(levels,ncolors=len(levels), clip=False)

 #Surface Fluxes
 if var in ["r_net","net_short","net_long"]:
  levels = np.linspace(-200,200,40)
  cmap = plt.cm.jet
  norm = mpl.colors.Normalize(vmin=np.min(levels),vmax=np.max(levels), clip=False)

 #NDVI
 if var in ["ndvi30"]:
  levels = np.linspace(0,1,40)
  cmap = plt.cm.YlGn
  norm = mpl.colors.Normalize(vmin=np.min(levels),vmax=np.max(levels), clip=False)

 #VIC output
 #if var in ["

 return (cmap,levels,norm)

def Create_Image(file,data,cmap,levels,norm,cflag):

 #Extract grid info
 undef = -9.99e+08
 lats = data.grid.lat
 lons = data.grid.lon

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
 cs = m.pcolormesh(lons,lats,data,latlon=True,shading='flat',cmap=cmap, norm=norm)
 if cflag == True:
  cs = m.contourf(lons,lats,data,latlon=True,levels=levels,cmap=cmap,norm=norm)
 plt.savefig(file,transparent=True)
 plt.close()

 return

def Extract_Gridded_Data(dataset,tstep,idate,fdate,info):

 #Open dataset control file and read information
 ga("xdfopen ../DATA_GRID/CTL/%s_%s.ctl" % (dataset,tstep))
 group = dataset
 vars_file = ga.query("file").vars
 vars_info_file = ga.query("file").var_titles
 variables = []
 for var in info['variables']:
  variables.append(var)
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

 #Extract the gridded data
 ga("set time %s %s" % (datetime2gradstime(idate),datetime2gradstime(fdate)))
 data = []
 for var in variables:
  print "Extracting %s data" % var
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

 #Prepare dictionary
 OUTPUT = {}
 OUTPUT['data'] = data
 OUTPUT['time'] = time_str
 OUTPUT['variables'] = variables
 OUTPUT['t_initial'] = t_initial
 OUTPUT['t_final'] = t_final

 #Close grads data
 ga("close 1")

 return OUTPUT

def Create_and_Update_All_Point_Data(idate,fdate,info,tstep):

 #Load mask
 ga("sdfopen ../DATA_GRID/MASKS/mask.nc")
 mask = ga.exp("mask")
 ga("close 1")
 lats = mask.grid.lat
 lons = mask.grid.lon
 mask = np.ma.getdata(mask)

 #Iterate through all datasets extracting the necessary info
 GRID_DATA = {}
 for dataset in info:
  print dataset
  TEMP = Extract_Gridded_Data(dataset,tstep,idate,fdate,info[dataset])
  GRID_DATA[dataset] = TEMP

 for ilat in range(lats.size):
  print lats[ilat]
  for ilon in range(lons.size):
   if mask[ilat,ilon] == 1:

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

    for group in info:

     #Define time intervals
     try:
      t_initial = GRID_DATA[group]['t_initial']
      t_final = GRID_DATA[group]['t_final']
     except:
      continue

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
      timeg = grp.variables['time']
     except:
      timeg = grp.createVariable('time','i4',('time',))

     ivar = 0
     for variable in info[group]['variables']:
      #Determine if variables exist
      try:
       var = grp.variables[variable]
      except:
       var = grp.createVariable(variable,'f4',('time',))

      #Assign data
      if t_final - t_initial == 0:
       var[t_initial:t_final+1] = GRID_DATA[group]['data'][ivar][ilat,ilon]
      else:
       var[t_initial:t_final+1] = GRID_DATA[group]['data'][ivar][:,ilat,ilon]
      timeg[t_initial:t_final+1] = GRID_DATA[group]['time']#time_str
      ivar = ivar + 1

    #Close the file
    fp.close()

 #Close the grads file
 ga("close 1")

 return

def Create_and_Update_Point_Data(idate,fdate,tstep,dataset,info):

 #If monthly time step only extract at end of month
 print tstep
 if tstep == "MONTHLY" and ((fdate+datetime.timedelta(days=1)).month == idate.month) and ((fdate+datetime.timedelta(days=1)).year == idate.year):
  return
 #If yearly time step only extract at end of month
 if tstep == "YEARLY" and ((fdate+datetime.timedelta(days=1)).year == idate.year):
  return

 print_info_to_command_line('Timestep: %s (Outputing Point Data)' % (dataset,tstep))

 #Load mask
 ga("sdfopen ../DATA_GRID/MASKS/mask.nc")
 mask = ga.exp("mask")
 ga("close 1")
 lats = mask.grid.lat
 lons = mask.grid.lon
 mask = np.ma.getdata(mask)

 #Open dataset control file and read information
 ga("xdfopen ../DATA_GRID/CTL/%s_%s.ctl" % (dataset,tstep))
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
 
 #Extract the gridded data
 ga("set time %s %s" % (datetime2gradstime(idate),datetime2gradstime(fdate)))
 data = []
 for var in variables:
  print "Extracting %s data" % var
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
   if mask[ilat,ilon] == 1:
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
     timeg = grp.variables['time']
    except:
     timeg = grp.createVariable('time','i4',('time',))

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
     timeg[t_initial:t_final+1] = time_str
     ivar = ivar + 1
     
    #Close the file
    fp.close() 
  
 #Close the grads file
 ga("close 1")

 return
