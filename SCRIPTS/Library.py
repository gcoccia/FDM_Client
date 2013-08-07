import os
import datetime
import grads
#import numpy as np
#import fileinput
#import netCDF4 as netcdf
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

def Download_and_Process_Forcing(date,dims):

 #Define the grads server root
 if date <= datetime.datetime(2008,12,31)
  http_root = "http://freeze.princeton.edu:9090/dods/AFRICAN_WATER_MONITOR/PRINCETON_GLOBAL_FORCING"

 #Make sure the main directory exists
 forcing_dir = "../DATA/FORCING/"
 Check_and_Make_Directory(pgf_dir)

 #Make sure the daily directory exists
 daily_dir = "../DATA/FORCING/DAILY"
 Check_and_Make_Directory(daily_dir)

 #Make sure the daily directory exists
 monthly_dir = "../DATA/FORCING/MONTHLY"
 Check_and_Make_Directory(monthly_dir)

 #Make sure the daily directory exists
 yearly_dir = "../DATA/FORCING/YEARLY"
 Check_and_Make_Directory(yearly_dir)

 #Download the daily file

 return
 
