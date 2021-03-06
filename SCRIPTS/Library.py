import matplotlib as mpl
mpl.use('agg')
import matplotlib.pyplot as plt
import mpl_toolkits.basemap.pyproj as pyproj
import os
import datetime
import grads
import numpy as np
import scipy.stats
from mpl_toolkits.basemap import Basemap,cm
import time
import netCDF4 as netcdf
import dateutil.relativedelta as relativedelta
import xml.etree.ElementTree as ET
import multiprocessing as mp
#import multiprocessing as mp
grads_exe = '../LIBRARIES/grads-2.0.1.oga.1/Contents/grads'
ga = grads.GrADS(Bin=grads_exe,Window=False,Echo=False)

def Determine_Dataset_Boundaries(dataset,tstep,info,dims,idate_all,fdate_all,http_base):

 print_info_to_command_line('Updating the time information for %s/%s' % (dataset,tstep))

 #Define the grads server root
 http_file = http_base + "/%s/%s" % (dataset,tstep)

 #Open access to grads data ser
 ga("sdfopen %s" % http_file)

 #Extract basic info
 qh = ga.query("file")
 vars = qh.vars
 vars_info = qh.var_titles

 #Determine the last date
 ga("set t last")
 fdate = gradstime2datetime(ga.exp(vars[0]).grid.time[0])
 ga("set t 1")
 idate = gradstime2datetime(ga.exp(vars[0]).grid.time[0])
 if info['group'] == 'Forecast' and tstep == 'MONTHLY':
  fdate = fdate - 5*relativedelta.relativedelta(months=1)
  idate = fdate
 if info['group'] == 'Forecast' and tstep == 'DAILY':
  fdate = fdate - 6*relativedelta.relativedelta(days=1)
  idate = fdate
 info['timestep'][tstep] = {}
 info['timestep'][tstep]['fdate'] = fdate
 info['timestep'][tstep]['idate'] = idate

 #Close grads file
 ga("close 1")
 
 #if info['group'] != 'Forecast':
 #Download first time step if necessary
 if tstep == "DAILY":
  file = '../DATA_GRID/%04d/%02d/%02d/%s_%04d%02d%02d_daily.nc' % (idate.year,idate.month,idate.day,dataset,idate.year,idate.month,idate.day)
 if tstep == "MONTHLY":
  file = '../DATA_GRID/%04d/%02d/%s_%04d%02d_monthly.nc' % (idate.year,idate.month,dataset,idate.year,idate.month)
 if tstep == "YEARLY":
  file = '../DATA_GRID/%04d/%s_%04d_yearly.nc' % (idate.year,dataset,idate.year)
 if os.path.exists(file) == False:
  Setup_Routines(idate)
  Download_and_Process(idate,dims,tstep,dataset,info,True,True,http_base)

 #Determine the size of the file
 info['timestep'][tstep]['fsize'] = os.stat(file).st_size

 #Adjust the idate_all and fdate_all if necessary
 if idate < idate_all:
  idate_all = idate
 if fdate > fdate_all:
  fdate_all = fdate

 return (info,idate_all,fdate_all)

def Read_and_Process_Main_Info():

 tree = ET.parse('../settings.xml')
 root = tree.getroot()

 #Pull dimensions
 dims = {}
 dims['res'] = float(root.find('dimensions').find('res').text)
 dims['minlat'] = float(root.find('dimensions').find('minlat').text)
 dims['minlon'] = float(root.find('dimensions').find('minlon').text)
 dims['nlat'] = int(root.find('dimensions').find('nlat').text)
 dims['nlon'] = int(root.find('dimensions').find('nlon').text)
 dims['minprec'] = float(root.find('dimensions').find('minprec').text)
 dims['minso'] = float(root.find('dimensions').find('minso').text)
 dims['maxlat'] = dims['minlat'] + dims['res']*(dims['nlat']-1)
 dims['maxlon'] = dims['minlon'] + dims['res']*(dims['nlon']-1)
 http_base = root.find('dimensions').find('base').text

 #Pull datasets
 datasets = {}
 groups = root.find('variables').findall('group')
 for group in groups:
  group_name = group.attrib['name']
  for variable in group.findall('datatype'):
   variable_name = variable.attrib['name']
   variable_units = variable.attrib['units']
   variable_mask = variable.attrib['mask']
   for dataset in variable.findall('dataset'):
    dataset_name = dataset.attrib['name']
    dataset_timestep = dataset.attrib['ts']
    dataset_itime = datetime.datetime.strptime(dataset.attrib['itime'],'%Y/%m/%d')
    dataset_ftime = datetime.datetime.strptime(dataset.attrib['ftime'],'%Y/%m/%d')
    try:
     datasets[dataset_name]
    except:
     datasets[dataset_name] = {}
     datasets[dataset_name]['variables'] = {}
     datasets[dataset_name]['timestep'] = {}
     #datasets[dataset_name]['itime'] = dataset_itime
     #datasets[dataset_name]['ftime'] = dataset_ftime
     datasets[dataset_name]['group'] = group_name
     for tstep in dataset_timestep:
      if tstep == 'D':
       datasets[dataset_name]['timestep']['DAILY'] = {}
       datasets[dataset_name]['timestep']['DAILY']['idate'] = dataset_itime
       datasets[dataset_name]['timestep']['DAILY']['fdate'] = dataset_ftime
      if tstep == 'M':
       datasets[dataset_name]['timestep']['MONTHLY'] = {}
       datasets[dataset_name]['timestep']['MONTHLY']['idate'] = dataset_itime
       datasets[dataset_name]['timestep']['MONTHLY']['fdate'] = dataset_ftime
      if tstep == 'Y':
       datasets[dataset_name]['timestep']['YEARLY'] = {}
       datasets[dataset_name]['timestep']['YEARLY']['idate'] = dataset_itime
       datasets[dataset_name]['timestep']['YEARLY']['fdate'] = dataset_ftime
    #Add the variable information 
    datasets[dataset_name]['variables'][variable_name] = {}
    datasets[dataset_name]['variables'][variable_name]['units'] = variable_units
    datasets[dataset_name]['variables'][variable_name]['mask'] = variable_mask
 return (dims,datasets,http_base)

def Update_XML_File(datasets):
 print_info_to_command_line('Updating the XML file')
 xml_settings = '../settings.xml'
 tree = ET.parse(xml_settings)
 for dataset in datasets:
  if 'DAILY' in datasets[dataset]['timestep']:
   tstep = 'DAILY'
  elif 'MONTHLY' in datasets[dataset]['timestep']:
   tstep = 'MONTHLY'
  elif 'YEARLY' in datasets[dataset]['timestep']:
   tstep = 'YEARLY'
  itime = datasets[dataset]['timestep'][tstep]['idate']
  ftime = datasets[dataset]['timestep'][tstep]['fdate']
  for variable in datasets[dataset]['variables']:
   tree = Update_XML_Contents(tree,variable,dataset,itime,ftime)
 #Write the new xml file
 tree.write('../settings.xml')
 return

def Update_XML_Contents(tree,var_name,dataset_name,itime,ftime):
 #tree = ET.parse(xml_settings)
 root = tree.getroot()
 groups = root.find('variables').findall('group')
 for group in root.find('variables').findall('group'):
  for variable in group.findall('datatype'):
   if variable.attrib['name'] == var_name:
    for dataset in variable.findall('dataset'):
     if dataset.attrib['name'] == dataset_name:
      dataset.attrib['ftime'] = ftime.strftime('%Y/%m/%d')
      dataset.attrib['itime'] = itime.strftime('%Y/%m/%d')
 return tree

def print_info_to_command_line(line):

 print "#######################################################################################"
 print "%s" % line
 print "#######################################################################################"
 print "\n"

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
 dir = "../WORKSPACE"
 if os.path.exists(dir) == False:
  os.system("mkdir %s" % dir)
  os.system("chmod 777 %s" % dir)

 #Setup miscellanous directories
 dir = "../DATA_GRID/CTL"
 Check_and_Make_Directory(dir)
 dir = "../DATA_GRID/MASKS"
 Check_and_Make_Directory(dir)
 dir = "../IMAGES/COLORBARS"
 Check_and_Make_Directory(dir)

 #Setup the decadal directory
 dir = "../DATA_CELL/%04d" % (10*np.floor(date.year/10.0))
 #if date.year % 10 == 0:
 # dir = "../DATA_CELL/%04d" % (date.year)
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

def Grads_Regrid(var_in,var_out,dims,type):

 if type == 'ba':
  ga("%s = smth9(re(%s,%d,linear,%f,%f,%d,linear,%f,%f,ba))" % (var_out,var_in,dims['nlon'],dims['minlon'],dims['res'],dims['nlat'],dims['minlat'],dims['res']))
 if type == 'vt':
  ga("%s = re(%s,%d,linear,%f,%f,%d,linear,%f,%f,vt,0,0)" % (var_out,var_in,dims['nlon'],dims['minlon'],dims['res'],dims['nlat'],dims['minlat'],dims['res']))
 if type == 'bl':
  ga("%s = re(%s,%d,linear,%f,%f,%d,linear,%f,%f,bl)" % (var_out,var_in,dims['nlon'],dims['minlon'],dims['res'],dims['nlat'],dims['minlat'],dims['res']))

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

def Create_Mask(dims,http_base,Reprocess_Flag):

 #Define file
 file = '../DATA_GRID/MASKS/mask.nc' 
 if os.path.exists(file) and Reprocess_Flag == False:
  return

 #Define http files
 http_file1 = http_base + '/MASK'
 http_file2 = http_base + '/MASK_200mm'
 http_file3 = http_base + '/MASK_100mm'
 http_file4 = http_base + '/PREC_ANNUAL'
 http_file5 = http_base + '/STREAM_ORDER'
 http_file6 = http_base + '/FLOW_ANNUAL'

 #Open file
 ga("sdfopen %s" % http_file1)
 ga("sdfopen %s" % http_file2)
 ga("sdfopen %s" % http_file3)
 ga("sdfopen %s" % http_file4)
 ga("sdfopen %s" % http_file5)
 ga("sdfopen %s" % http_file6)
  
 #Set grads region
 ga("set lat %f %f" % (dims['minlat'],dims['maxlat']))
 ga("set lon %f %f" % (dims['minlon'],dims['maxlon']))

 #Regrid data
 Grads_Regrid('mask.1','mask1',dims,'vt')
 Grads_Regrid('mask.2','mask2',dims,'vt')
 Grads_Regrid('mask.3','mask3',dims,'vt')
 Grads_Regrid('precmean.4','pmean',dims,'vt')
 Grads_Regrid('so.5(t=1)','so',dims,'vt')
 ga("mask5 = const(maskout(maskout(pmean,pmean-%f),mask1),1)" % dims['minprec'])
 #ga("maskso = maskout(maskout(so,so-%f),mask5)" % dims['minso'])
 ga("maskso = const(maskout(maskout(so,so-%f),mask3),-9.99e+08, -u))" % dims['minso'])

 #Determine the flow to display
 flwmean = np.ma.getdata(ga.exp("flwmean.6(t=1)"))
 flwmean = flwmean[flwmean >= 0]
 #pcts = np.linspace(0,100,15)
 pcts = [0,1,5,10,15,20,30,50,70,80,85,90,95,99,100] 
 flwvals = []
 for pct in pcts:
  flwvals.append(scipy.stats.scoreatpercentile(flwmean,pct))
 dims['flwvals'] = np.unique(np.floor(np.array(flwvals)))

 #Write to file
 fp = Create_NETCDF_File(dims,file,['mask','mask200','maskSO','mask100','maskcs'],['mask','mask200','maskSO','mask100','maskcs'],datetime.datetime(1900,1,1),'days',1)
 fp.variables['mask'][0] = np.ma.getdata(ga.exp("mask1"))
 fp.variables['mask200'][0] = np.ma.getdata(ga.exp("mask2"))
 fp.variables['maskSO'][0] = np.ma.getdata(ga.exp("maskso"))
 fp.variables['mask100'][0] = np.ma.getdata(ga.exp("mask3"))
 fp.variables['maskcs'][0] = np.ma.getdata(ga.exp("mask5"))

 #Close files 
 ga("close 6")
 ga("close 5")
 ga("close 4")
 ga("close 3")
 ga("close 2")
 ga("close 1")
 fp.close()

 return dims

def Find_Ensemble_Number(group,timestep,idate,date):

 if group == 'Forecast':
  ga("set t 1")
  idate = gradstime2datetime(ga.exp('prec').grid.time[0])
  if timestep == 'MONTHLY':
   #iensemble = 12*(date.year - idate.year) + max(date.month - idate.month,0) + 1  
   rd = relativedelta.relativedelta(date,idate)
   iensemble = 12*rd.years + rd.months + 1
   nt = 6
  if timestep == 'DAILY':
   iensemble = (date - idate).days + 1
   nt = 7
 else:
  iensemble = 1
  nt = 1

 return (nt,iensemble)

def Download_and_Process(date,dims,tstep,dataset,info,Reprocess_Flag,Initial_Flag,http_base):

 fdate = info['timestep'][tstep]['fdate']
 idate = info['timestep'][tstep]['idate']
 nt = (fdate - idate).days + 1
 real_date = date
 #If we are not within the bounds then exit
 if tstep == "MONTHLY":
  idate = datetime.datetime(idate.year,idate.month,1)
  fdate = datetime.datetime(fdate.year,fdate.month,1)
  date = datetime.datetime(date.year,date.month,1)
  #nt = (fdate - idate).days + 1
 if tstep == "YEARLY":
  idate = datetime.datetime(idate.year,1,1)
  fdate = datetime.datetime(fdate.year,1,1)
  date = datetime.datetime(date.year,1,1)
  #nt = (fdate - idate).days + 1

 if info['group'] != 'Forecast':
  '''
  #Download initial time step if not available
  if tstep == "DAILY":
   file = '../DATA_GRID/%04d/%02d/%02d/%s_%04d%02d%02d_daily.nc' % (idate.year,idate.month,idate.day,dataset,idate.year,idate.month,idate.day)
  if tstep == "MONTHLY":
   file = '../DATA_GRID/%04d/%02d/%s_%04d%02d_monthly.nc' % (idate.year,idate.month,dataset,idate.year,idate.month)
  if tstep == "YEARLY":
   file = '../DATA_GRID/%04d/%s_%04d_yearly.nc' % (idate.year,dataset,idate.year)
  if os.path.exists(file) == False and date != idate:
   Setup_Routines(idate)
   Download_and_Process(idate,dims,tstep,dataset,info,Reprocess_Flag)
  '''
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

 if date < idate or date > fdate:
  return info

 #Define new filename
 if tstep == "DAILY":
  file = '../DATA_GRID/%04d/%02d/%02d/%s_%04d%02d%02d_daily.nc' % (date.year,date.month,date.day,dataset,date.year,date.month,date.day)
  dt = relativedelta.relativedelta(days=1)
  nc_tstep = 'days'
 if tstep == "MONTHLY":
  file = '../DATA_GRID/%04d/%02d/%s_%04d%02d_monthly.nc' % (date.year,date.month,dataset,date.year,date.month)
  dt = relativedelta.relativedelta(months=1)
  nc_tstep = 'months'
  date = datetime.datetime(date.year,date.month,1)
 if tstep == "YEARLY":
  file = '../DATA_GRID/%04d/%s_%04d_yearly.nc' % (date.year,dataset,date.year)
  dt = relativedelta.relativedelta(years=1)
  nc_tstep = 'years'
  date = datetime.datetime(date.year,1,1)

 #If file is the appropriate size
 #if os.path.exists(file) == True and Reprocess_Flag == False:
 if os.path.exists(file) and abs(1-float(os.stat(file).st_size)/float(info['timestep'][tstep]['fsize'])) < 0.1 and Reprocess_Flag == False:
  return info

 #If reprocessing, don't redo monthly and yearly if you don't have to...
 if Reprocess_Flag == True and os.path.exists(file) == True and Initial_Flag == False:
  if tstep == "MONTHLY" and real_date.day != 1:
   return info
  if tstep == "YEARLY" and (real_date.month + real_date.day) != 2:
   return info

 print_info_to_command_line('Dataset: %s Timestep: %s (Downloading and Processing data)' % (dataset,tstep))

 #Define the grads server root
 http_file = http_base + "/%s/%s" % (dataset,tstep)

 #Open access to grads data server
 connection_info = {'flag':False,'count':0,'seconds':60}
 while connection_info['flag'] == False:
  try:
   ga("sdfopen %s" % http_file)
   connection_info['flag'] = True  
  except: 
   print "Cannot connect to GDS server on try %d, will retry in %d seconds" % (connection_info['count']+1,connection_info['seconds'])
   connection_info['count'] += 1
   time.sleep(connection_info['seconds'])
   if connection_info['count'] == 60:
     print "Too many failed attempts, please check connection to server"

 #Extract basic info
 qh = ga.query("file")
 vars = qh.vars
 vars_info = qh.var_titles

 #Determine the ensemble number
 (nt,iensemble) = Find_Ensemble_Number(info['group'],tstep,idate,date)#12*(date.year - idate.year) + max(date.month - idate.month,0) + 1
 
 #Set grads region
 ga("set lat %f %f" % (dims['minlat'],dims['maxlat']))
 ga("set lon %f %f" % (dims['minlon'],dims['maxlon']))

 fp = Create_NETCDF_File(dims,file,vars,vars_info,date,nc_tstep,nt)
 data = []
 #Set the ensemble number
 ga("set e %d" % iensemble)
 #Regrid and write variables to file
 for t in xrange(0,nt):
  timestamp = datetime2gradstime(date + t*dt)
  ga("set time %s" % timestamp)
  for var in vars:
   if var == 'flw_pct' or var == 'flw':
    Grads_Regrid(var,'data',dims,'vt')
   else:
    Grads_Regrid(var,'data',dims,'ba')
   data = ga.exp('data')
   #if info['group'] == "Forecast" and var == "prec" and tstep == "MONTHLY":
   # data = 30.5*data #NEED TO CHANGE.. NOT CORRECT
   fp.variables[var][t] = data

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

 ga = grads.GrADS(Bin=grads_exe,Window=False,Echo=False)
 variables = info['variables']
 idate = info['timestep'][timestep]['idate']
 fdate = info['timestep'][timestep]['fdate']

 #If we are not within the bounds then exit
 real_date = date
 if timestep == "MONTHLY":
  idate = datetime.datetime(idate.year,idate.month,1)
  fdate = datetime.datetime(fdate.year,fdate.month,1)
  date = datetime.datetime(date.year,date.month,1)
 if timestep == "YEARLY":
  idate = datetime.datetime(idate.year,1,1)
  fdate = datetime.datetime(fdate.year,1,1)
  date = datetime.datetime(date.year,1,1)
 if date < idate or date > fdate:
  return

 #Define the file to read
 (dir_output,date_output) = datetime2outputtime(date,timestep) 
 image_dir = '../IMAGES/%s/%s'  % (dir_output,dataset)

 if os.path.exists(image_dir) == True and Reprocess_Flag == False:
  return

 if Reprocess_Flag == True and os.path.exists(image_dir) == True:
  if timestep == "MONTHLY" and real_date.day != 1:
   return
  if timestep == "YEARLY" and real_date.month != 1 and real_date.day != 1:
   return

 print_info_to_command_line('Dataset: %s Timestep: %s (Creating Images)' % (dataset,timestep))
 
 file_netcdf = '../DATA_GRID/%s/%s_%s_%s.nc' % (dir_output,dataset,date_output,timestep.lower())
 #Open control file
 ga("sdfopen %s" % file_netcdf)
 #Load mask file
 ga("sdfopen ../DATA_GRID/MASKS/mask.nc")
 #Determine the number of time steps
 ga("set t 1 last")
 nt = ga.query('dims').nt

 #Define the dt
 if timestep == 'DAILY':
  dt = relativedelta.relativedelta(days = 1)
 elif timestep == 'MONTHLY':
  dt = relativedelta.relativedelta(months = 1)
 elif timestep == 'YEARLY':
  dt = relativedelta.relativedelta(years = 1)

 #Create images for all variables
 image_dir = '../IMAGES/%s/%s'  % (dir_output,dataset)
 if os.path.exists(image_dir) == False:
  os.mkdir(image_dir)
 #Iterate through all the time steps (if monitor it will only be one...)
 tic = time.clock()
 for t in xrange(0,nt):
  #Set time step
  date_tmp = date + t*dt#relativedelta.relativedelta(dt[timestep] = 1)
  (dir_output,date_output) = datetime2outputtime(date_tmp,timestep)
  ga("set time %s" % datetime2gradstime(date_tmp))
  #Create image and colorbars for the Google Maps images
  process = []
  for var in variables:#qh.vars:
   image_file = '%s/%s_%s.png'  % (image_dir,var,date_output)
   #Skip image if it exists and we don't want to reprocess it
   if os.path.exists(image_file) and Reprocess_Flag == False:
    continue
   ga("data = maskout(%s,%s.2(t=1))" % (var,variables[var]['mask']))
   if var in ["spi1","spi3","spi6","spi12","vcpct","vc1","vc2","pct30day"]:
    ga("data = smth9(data)")
   data = ga.exp("data")
   (cmap,levels,norm) = Define_Colormap(var,timestep,dims)
   cflag = True
   if var in ['flw','flw_pct']:
    cflag = False
   Create_Image(image_file,data,cmap,levels,norm,cflag,'Google Maps') 
   #p = mp.Process(target=Create_Image,args=(image_file,data,cmap,levels,norm,cflag,'Google Maps'))
   colormap_file = '../IMAGES/COLORBARS/%s--%s_%s.png' % (dataset,var,timestep)
   Create_Colorbar(colormap_file,cmap,norm,var,levels,True)#False)
   #p.start()
   #process.append(p)
  #for p in process:
   #p.join()
  #Create static image
  process = []
  for var in variables:
   image_file = '%s/%s_%s_s.png'  % (image_dir,var,date_output)
   #Skip image if it exists and we don't want to reprocess it
   if os.path.exists(image_file) and Reprocess_Flag == False:
    continue
   ga("data = maskout(%s,%s.2(t=1))" % (var,variables[var]['mask']))
   if var in ["spi1","spi3","spi6","spi12","vcpct","vc1","vc2","pct30day"]:
    ga("data = smth9(data)")
   data = ga.exp("data")
   (cmap,levels,norm) = Define_Colormap(var,timestep,dims)
   cflag = True
   if var in ['flw','flw_pct']:
    cflag = False
   Create_Image(image_file,data,cmap,levels,norm,cflag,'Static')
   #p = mp.Process(target=Create_Image,args=(image_file,data,cmap,levels,norm,cflag,'Static'))
   #p.start()
   #process.append(p)
  #for p in process:
   #p.join()

 #Close access to file
 ga("close 2")
 ga("close 1")

 return

def Create_Colorbar(file,cmap,norm,var,levels,Reprocess_Flag):
 
 if os.path.exists(file) and Reprocess_Flag == False:
  return
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
  levels = [-4.0,-2.0,-1.0,0,1.0,2.0,4.0]
 if var in ['t2m']:
  levels = [270,275,280,285,290,295,300,305,310]
 fig = plt.figure(figsize=(8,0.5))
 ax = fig.add_axes([0.05, 0.4, 0.9, 0.8])
 cb = mpl.colorbar.ColorbarBase(ax,cmap=cmap,norm=norm,orientation='horizontal',ticks=levels)
 plt.savefig(file,transparent=True)
 plt.close()

 return

def Define_Colormap(var,timestep,dims):

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
   levels = np.linspace(270,310,40)
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

 #NDVI and drought index
 if var in ["vcpct","pct30day","flw_pct"]:
  levels = [1,5,10,20,30,70,80,90,95,99]
  cmap = plt.cm.RdYlGn
  norm = mpl.colors.BoundaryNorm(levels,ncolors=256, clip=False)

 #Streamflow percentiles
 if var in ["flw_pct"]:
  levels = [1,5,10,20,30,70,80,90,95,99]
  cmap = plt.cm.jet_r
  norm = mpl.colors.BoundaryNorm(levels,ncolors=256, clip=False)

 #Streamflow
 if var in ["flw"]:
  #levels = [0,5000,10000,15000,20000,25000,30000,35000,40000]
  #levels = [0,100,200,300,400,500,1000,1500,2000,2500,5000,10000,20000,30000]
  levels =  dims['flwvals']#[   0. ,   1.,    2.,    3.,    4.,    8. ,  16.,  137.] #CAREFUL!
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

 return (cmap,levels,norm)

def Create_Image(file,data,cmap,levels,norm,cflag,type):

 #Extract grid info
 lats = data.grid.lat
 lons = data.grid.lon

 #Create Basemap instance for Google maps mercator projection
 res = lats[1] - lats[0]
 llcrnrlat = lats[0]# - res/2
 urcrnrlat = lats[-1]# + res/2
 llcrnrlon = lons[0]# - res/2
 urcrnrlon = lons[-1]# + res/2
 tic = time.clock()
 if type == 'Google Maps':
  m = Basemap(llcrnrlat=llcrnrlat,urcrnrlat=urcrnrlat,llcrnrlon=llcrnrlon,urcrnrlon=urcrnrlon,epsg=3857)
  lons,lats = np.meshgrid(lons,lats)
  width = 10.0
  height = width*m.aspect
  #Plot image
  fig = plt.figure(frameon=False)
  fig.set_size_inches(width,height)
  ax = plt.Axes(fig,[0., 0., 1., 1.])
  #ax.set_axis_bgcolor('none')
  ax.set_axis_off()
  ax.m = m#Basemap(llcrnrlat=llcrnrlat,urcrnrlat=urcrnrlat,llcrnrlon=llcrnrlon,urcrnrlon=urcrnrlon,epsg=3857)
  #lons,lats = np.meshgrid(lons,lats)
  fig.add_axes(ax)
  plt.axis('off')
  #x,y = m(x,y)
  #x, y = m(*np.meshgrid(lons,lats))
  cs = m.pcolormesh(lons,lats,data,latlon=True,shading='flat',cmap=cmap, norm=norm)
  if cflag == True:
   cs = m.contourf(lons,lats,data,latlon=True,levels=levels,cmap=cmap,norm=norm)
  plt.savefig(file,transparent=True)
  plt.close()
 elif type == 'Static':
  m = Basemap(llcrnrlat=llcrnrlat,urcrnrlat=urcrnrlat,llcrnrlon=llcrnrlon,urcrnrlon=urcrnrlon,epsg=3857)
  lons,lats = np.meshgrid(lons,lats)
  width = 10
  height = width*m.aspect
  #Plot image
  fig = plt.figure()
  fig.set_size_inches(width,height)
  ax = plt.Axes(fig,[0.15, 0.05, 0.75, 0.9])
  ax.set_axis_bgcolor('#E5E5E5')
  #ax.set_axis_off()
  ax.m = m
  fig.add_axes(ax)
  cs = m.pcolormesh(lons,lats,data,latlon=True,shading='flat',cmap=cmap, norm=norm)
  if cflag == True:
   cs = m.contourf(lons,lats,data,latlon=True,levels=levels,cmap=cmap,norm=norm)
  m.drawcoastlines(linewidth=0.15)
  m.drawcountries(linewidth=0.15)
  #m.drawrivers(linewidth=0.025)
  npar = len(data.grid.lat)/2
  nmer = len(data.grid.lat)/2
  parallels = data.grid.lat[npar/2:-1:npar]
  meridians = data.grid.lon[nmer/2:-1:nmer]
  m.drawparallels(parallels,labels=[1,0,0,0])
  m.drawmeridians(meridians,labels=[0,0,0,1])
  plt.colorbar(shrink=0.6)
  plt.savefig(file)
  plt.close()
  os.system('convert %s -trim %s' % (file,file))
 
 return

def Extract_Gridded_Data(dataset,tstep,idate,fdate,info,open_type,ga,idecade):

 idate_dataset = info['timestep'][tstep]['idate']
 fdate_dataset = info['timestep'][tstep]['fdate']
 #Leave if before initial time step
 if idate < idate_dataset:
  idate = idate_dataset
 if fdate > fdate_dataset:
  fdate = fdate_dataset
 if tstep == 'MONTHLY':
  idate = datetime.datetime(idate.year,idate.month,1)
  fdate = datetime.datetime(fdate.year,fdate.month,1)
 if tstep == 'YEARLY':
  idate = datetime.datetime(idate.year,1,1)
  fdate = datetime.datetime(fdate.year,1,1)
 if idate > fdate:
  return

 #Open dataset control file and read information
 if open_type == 'xdfopen':
  ga("xdfopen ../DATA_GRID/CTL/%s_%s.ctl" % (dataset,tstep))
 elif open_type == 'sdfopen':
  if tstep == 'DAILY':
   ga("sdfopen ../DATA_GRID/%04d/%02d/%02d/%s_%04d%02d%02d_daily.nc" % (idate.year,idate.month,idate.day,dataset,idate.year,idate.month,idate.day))
   fdate = fdate + 6*relativedelta.relativedelta(days=1)
  elif tstep == 'MONTHLY':
   ga("sdfopen ../DATA_GRID/%04d/%02d/%s_%04d%02d_monthly.nc" % (idate.year,idate.month,dataset,idate.year,idate.month))
   fdate = fdate + 5*relativedelta.relativedelta(months=1)
 group = dataset
 vars_file = ga.query("file").vars
 vars_info_file = ga.query("file").var_titles
 variables = []
 for var in info['variables']:
  variables.append(var)
 #variables = vars_file

 #Define current time step
 idate_tmp = datetime.datetime(idecade,1,1)
 if idate_tmp < idate_dataset:
  idate_tmp = idate_dataset
 if tstep == "DAILY":
  t_initial = (idate - idate_tmp).days
  t_final = (fdate - idate_tmp).days
 if tstep == "MONTHLY":
  t_initial = idate.month - idate_tmp.month + (idate.year - idate_tmp.year) * 12
  t_final = fdate.month - idate_tmp.month + (fdate.year - idate_tmp.year) * 12
  idate = datetime.datetime(idate.year,idate.month,1)
  fdate = datetime.datetime(fdate.year,fdate.month,1)
 if tstep == "YEARLY":
  t_initial = idate.year - idate_tmp.year
  t_final = fdate.year - idate_tmp.year
  idate = datetime.datetime(idate.year,1,1)
  fdate = datetime.datetime(fdate.year,1,1)
 print tstep,idate,fdate,t_initial,t_final

 #Extract the gridded data
 ga("set time %s %s" % (datetime2gradstime(idate),datetime2gradstime(fdate)))
 data = []
 for var in variables:
  print "Extracting %s data" % var
  data.append(np.ma.getdata(ga.exp(var)))

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
  time_str.append(24*3600*(date - datetime.datetime(1970,1,1)).days)
  date = date + dt

 #Prepare dictionary
 OUTPUT = {}
 OUTPUT['data'] = np.array(data)
 OUTPUT['time'] = time_str
 OUTPUT['variables'] = variables
 OUTPUT['t_initial'] = t_initial
 OUTPUT['t_final'] = t_final

 #Close grads data
 ga("close 1")

 return OUTPUT

def Create_and_Update_Point_Data(idate,fdate,info,nthreads):

 #Open grads
 ga = grads.GrADS(Bin=grads_exe,Window=False,Echo=False)

 #Load mask
 ga("sdfopen ../DATA_GRID/MASKS/mask.nc")
 mask = ga.exp("mask")
 ga("close 1")
 lats = mask.grid.lat
 lons = mask.grid.lon
 mask = np.ma.getdata(mask)
 
 #Find the decade
 idecade = np.int(10*np.floor(idate.year/10))

 #Iterate through all datasets extracting the necessary info
 GRID_DATA = {}
 open_type = 'xdfopen'
 tsteps = ['DAILY','MONTHLY','YEARLY']
 for tstep in tsteps:
  GRID_DATA[tstep] = {}
  for dataset in info:
   if tstep in info[dataset]['timestep']:
    if info[dataset]['group'] == 'Forecast':
     open_type = 'sdfopen'
    else:
     open_type = 'xdfopen'
    TEMP = Extract_Gridded_Data(dataset,tstep,idate,fdate,info[dataset],open_type,ga,idecade)
    if TEMP != None:
     GRID_DATA[tstep][dataset] = TEMP
 count = 0
 process = []
 #nthreads = 10
 for ilat in range(lats.size):
  print lats[ilat]
  p = mp.Process(target=Write_Data_Cell,args=(GRID_DATA,lats,lons,ilat,info,mask,idecade))
  #Write_Data_Cell(GRID_DATA,lats,lons,ilat,info,mask,idate.year)
  p.start()
  process.append(p)
  if len(process) == nthreads:
   for p in process:
    p.join()
    process = []

 return

def Write_Data_Cell(GRID_DATA,lats,lons,ilat,info,mask,idecade):

 for ilon in range(lons.size):
  if mask[ilat,ilon] == 1:

   undef = -9.99e+08
   #Determine if file exists 
   file = '../DATA_CELL/%d/cell_%0.3f_%0.3f.nc' % (idecade,lats[ilat],lons[ilon])
   if os.path.exists(file) == False:
    fp = netcdf.Dataset(file,'w',format='NETCDF4')
   else:
    fp = netcdf.Dataset(file,'a',format='NETCDF4')

   for tstep in GRID_DATA:
      
    #Determine if time step group exists
    if tstep in fp.groups.keys():
     grp_tstep = fp.groups[tstep]
    else:
     grp_tstep = fp.createGroup(tstep)

    for group in GRID_DATA[tstep]:
     #Define time intervals
     try:
      t_initial = GRID_DATA[tstep][group]['t_initial']
      t_final = GRID_DATA[tstep][group]['t_final']
      #t_final = t_final - t_initial
      #t_initial = 0
     except:
      continue

     #Determine if the dataset group exists
     if group in grp_tstep.groups.keys():
      grp = grp_tstep.groups[group]
     else:
      grp = grp_tstep.createGroup(group)
      dim = grp.createDimension('time',None)
      timeg = grp.createVariable('time','i4',('time',),chunksizes=(1000,),complevel=1)#,fill_value=undef)
      for variable in info[group]['variables']:#GRID_DATA[tstep][group]['variables']:
       var = grp.createVariable(variable,'f4',('time',),chunksizes=(1000,),complevel=1)#,fill_value=undef)

     timeg = grp.variables['time']
     timeg[t_initial:t_final+1] = GRID_DATA[tstep][group]['time']#time_str
 
     ivar = 0
      #Determine if variables exist
     for variable in info[group]['variables']:
      var = grp.variables[variable]
      #Assign data
      if t_final - t_initial == 0:
       var[t_initial:t_final+1] = GRID_DATA[tstep][group]['data'][ivar][ilat,ilon]
      else:
       var[t_initial:t_final+1] = GRID_DATA[tstep][group]['data'][ivar][:,ilat,ilon]
      ivar = ivar + 1

   #Close the file
   fp.close()

 return
