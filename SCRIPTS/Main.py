#################################################################
# Drought Monitor V2
# Written by: Nathaniel W. Chaney
# Date: 12 July 2013
# Location: Princeton University
# Contact: nchaney@princeton.edu
# Purpose: Driver of the client side of the monitor
##################################################################

import Library as cl
import datetime
import numpy as np
import xml.etree.ElementTree as ET
import datetime

def Read_and_Process_Main_Info():

 tree = ET.parse('../web_nchaney/settings.xml')
 root = tree.getroot()

 #Pull dimensions
 dims = {}
 dims['res'] = float(root.find('dimensions').find('res').text)
 dims['minlat'] = float(root.find('dimensions').find('minlat').text)
 dims['minlon'] = float(root.find('dimensions').find('minlon').text)
 dims['nlat'] = int(root.find('dimensions').find('nlat').text)
 dims['nlon'] = int(root.find('dimensions').find('nlon').text)
 dims['maxlat'] = dims['minlat'] + dims['res']*(dims['nlat']-1)
 dims['maxlon'] = dims['minlon'] + dims['res']*(dims['nlon']-1)

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
     datasets[dataset_name]['timestep'] = []
     datasets[dataset_name]['itime'] = dataset_itime
     datasets[dataset_name]['ftime'] = dataset_ftime
     datasets[dataset_name]['group'] = group_name
     for tstep in dataset_timestep:
      if tstep == 'D':
       datasets[dataset_name]['timestep'].append('DAILY')
      if tstep == 'M':
       datasets[dataset_name]['timestep'].append('MONTHLY')
      if tstep == 'Y':
       datasets[dataset_name]['timestep'].append('YEARLY')
    #Add the variable information 
    datasets[dataset_name]['variables'][variable_name] = {}
    datasets[dataset_name]['variables'][variable_name]['units'] = variable_units
    datasets[dataset_name]['variables'][variable_name]['mask'] = variable_mask
 return (dims,datasets)
 
#1. Determine the dimensions
(dims,datasets) = Read_and_Process_Main_Info()
dt = datetime.timedelta(days=1)
date = datetime.datetime.today()

#Always redownload and reprocess the last 30 days
idate = datetime.datetime(date.year,date.month,date.day) - 32*dt
fdate = idate + datetime.timedelta(days=30)
idate = datetime.datetime(2013,9,12)
fdate = datetime.datetime(2013,9,12)

#Prepare the mask
cl.Create_Mask(dims)

#Download all the requested data
date = idate
while date <= fdate:

 print date

 #Setup routines
 cl.Setup_Routines(date)

 #For each availabe data set:
 for dataset in datasets:
  for tstep in datasets[dataset]['timestep']:

   #Download and process the data
   print datasets[dataset]['group']
   datasets[dataset] = cl.Download_and_Process(date,dims,tstep,dataset,datasets[dataset],True)
   
 date = date + dt

#Preparing all the images
date = idate 
while date <= fdate:

 print date

 #Create Images

 #For each availabe data set:
 for dataset in datasets:
  for tstep in datasets[dataset]['timestep']:

   #Create Images
   cl.Create_Images(date,dims,dataset,tstep,datasets[dataset],True)
 
 date = date + dt

'''
#3. Create and update the point data
#for dataset in datasets:
for tstep in ['DAILY','MONTHLY','YEARLY']:
  print "%s" % (tstep)
  cl.Create_and_Update_All_Point_Data(idate,fdate,datasets,tstep)
  #cl.Create_and_Update_Point_Data(idate,fdate,tstep,dataset,datasets[dataset]['variables'])

#4. Update the xml file
#cl.Update_XML_File(datasets)
'''
