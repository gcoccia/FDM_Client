#--------------------------------------------------------------------------
#
#    Copyright (C) 2006-2008 by Arlindo da Silva <dasilva@opengrads.org>
#    All Rights Reserved.
#
#    This program is free software; you can redistribute it and/or modify
#    it under the terms of the GNU General Public License as published by
#    the Free Software Foundation# using version 2 of the License.
#
#    This program is distributed in the hope that it will be useful,
#    but WITHOUT ANY WARRANTY# without even the implied warranty of
#    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#    GNU General Public License for more details.
#
#    You should have received a copy of the GNU General Public License
#    along with this program# if not, please consult  
#              
#              http://www.gnu.org/licenses/licenses.html
#
#    or write to the Free Software Foundation, Inc., 59 Temple Place,
#    Suite 330, Boston, MA 02111-1307 USA
#
#------------------------------------------------------------------------

""" 
This module extends the GrADS client class by providing methods for
exchanging n-dimensional NumPy array data between Python and
GrADS.
"""

__version__ = '1.1.1'


from gacore       import *
from numtypes     import *

from numpy        import zeros, ones, average, newaxis, sqrt, pi, cos, inner, \
                         arange, fromfile, float32, ma, reshape, ndarray, \
                         abs
from numpy.linalg import svd, lstsq

class GaNum(GaCore):
    """
    This class extends the GrADS client class by providing methods
    for data exchange and numerical computations:

    _Methods provided:
       exp  -  exports a GrADS expression into a NumPy array, with metada
       imp  -  imports NumPy array (+metadata) into GrADS
       eof  -  compute Empirical Orthogonal Functions (EOFS) from expressions 
       lsq  -  least square parameter estimation from expressions 

    """

#........................................................................

    def exp (self, expr):
        """
        Exports GrADS expression *expr*, returning a GrADS Field.

            F = self.exp(expr)

        where

            F  ---  GrADS field
            
        Generalized Expressions
        =======================

        For convenience, *expr* can also be a GrADS Field.  In such
        cases, the input Field is just returned back. This
        *overloading* is useful for writing high level functions that
        work equaly on a GrADS expression to be exported or on GrADS
        fields already known to python.

        Limitation
        ==========

        This function does not handle varying ensemble dimensions in
        GrADS v2.

        """

#       If IPC extension is not available, then try expr() instead
#       ----------------------------------------------------------
        if not self.HAS_IPC:
            return self.expr(expr)

#       For convenience, allows calls where expr is not a string, in which
#        case it returns back the input field or raise an exception
#       -------------------------------------------------------------------
        if type(expr) == StringType:
            pass # OK, will proceed to export it from GrADS
        elif isinstance(expr,GaField):
            return expr # just return input
        elif isinstance(expr,ndarray):
            return expr # this is handy for 'lsq'
        else:
            raise GrADSError, "input <expr> has invalid type: %s"%type(expr)

#       Retrieve dimension environment
#       ------------------------------
        dh = self.query("dims", Quiet=True) 
        t1, t2 = dh.t
        z1, z2 = dh.z 
        nx, ny, nz, nt = (dh.nx, dh.ny, dh.nz, dh.nt)
   

#       Shortcut for 2D slices (any 2 dimensions)
#       -----------------------------------------
        if dh.rank ==2:
            return self._exp2d(expr)

#       Initial implementation: require x,y to vary for rank>2
#       Note: remove this restriction is not very hard, but requires
#             special handling the different dimension permutations separately
#             given the way GrADS invokes functions for XZ, YZ, ZT, etc
#       ----------------------------------------------------------------------
        if nx==1: raise GrADSError, 'lon must be varying but got nx=1'
        if ny==1: raise GrADSError, 'lat must be varying but got ny=1'

#       Loop over time/z, get a GrADS 2D slice at a time/z
#       --------------------------------------------------
        l = rc = 0  
        Data = None # defer allocations until we know the size
        grid = GaGrid(expr)
        grid.meta = zeros((nt,nz,20),dtype=float32)
        grid.denv = dh 
        grid.time = [] 
        grid.lev = zeros(nz,dtype=float32)         
        try:
            for t in range(t1,t2+1):
                self.cmd("set t %d"%t,Quiet=True) 
                self.cmd("q time",Quiet=True)
                grid.time.append(self.rword(1,3))
                k = 0
                for z in range(z1,z2+1):
                    self.cmd("set z %d"%z,Quiet=True)
                    field = self._exp2d(expr)
                    if Data==None:
                        ny_, nx_ = field.shape # may differ from dh.nx/dh.ny
                        Data = zeros(shape=(nt,nz,ny_,nx_), dtype=float32)
                    Data[l,k,:,:] = field.data
                    grid.lev[k] = field.grid.lev[0]
                    grid.meta[l,k,:] = field.grid.meta
                    k = k + 1
                l = l + 1

#           Record lat/lon
#           --------------
            grid.lat = field.grid.lat
            grid.lon = field.grid.lon
            amiss = grid.meta[0]

#           Remove dimensions with size 1
#           -----------------------------
            if nz==1: 
                Data = Data.reshape(nt,ny_,nx_)
                grid.dims = [ 'time', 'lat', 'lon' ]
                grid.meta = grid.meta.reshape(nt,20)
            elif nt==1: 
                Data = Data.reshape(nz,ny_,nx_)
                grid.dims = [ 'lev', 'lat', 'lon' ]
                grid.meta = grid.meta.reshape(nz,20)
            else:
                grid.dims = [ 'time', 'lev', 'lat', 'lon' ]

        except:
            self.setdim(dh)
            raise GrADSError, 'could not export <%s>'%expr


        grid.tyme = array([gat2dt(t) for t in grid.time])

#       Restore dimension environment
#       -----------------------------
        self.setdim(dh)
        return GaField(Data, name=expr, grid=grid, 
                       mask=(Data==amiss), dtype=float32)
    
#........................................................................

    def _exp2d ( self, expr, dh=None ):
        """ 
        Exports GrADS expression *expr* as a GrADS Field.
        The stdio pipes are used for data exchange.
        This is an internal version handling 2D xy slices.
        In here, *expr* must be a string.
        """

        if dh==None:
            dh = self.query("dims",Quiet=True)

#       Check environmnet
#       -----------------
        nx, ny = (dh.nx, dh.ny)
        if dh.rank !=2:
            raise GrADSError, 'expecting rank=2 but got rank=%d'%dh.rank

#       Create output handle, fill in some metadata
#       -------------------------------------------
        grid = GaGrid(expr)
        grid.denv = dh

#       Issue GrADS command, will check rc later
#       -----------------------------------------
        if self.Version[1] is '1':
            cmd = 'ipc_define void = ipc_save('+expr+',-)\n'
        else:
            cmd = 'define void = ipc_save('+expr+',-)\n'

        self.Writer.write(cmd)

#       Position stream pointer after <EXP> marker
#       ------------------------------------------
        got = ''
        while got[:5] != '<EXP>' :
            got = self.Reader.readline()

#       Read header
#       -----------
        grid.meta = fromfile(self.Reader,count=20,dtype=float32)

        amiss = grid.meta[0]
        id = int(grid.meta[1])
        jd = int(grid.meta[2])
        nx_ = int(grid.meta[3])
        ny_ = int(grid.meta[4])

#        if id!=0 or jd!=1:
        if id<0 or id>3 or jd<0 or jd>3 or id==jd:
            self.flush()
            raise GrADSError, \
                  'invalid exchange metadata (idim,jdim)=(%d,%d) - make sure <%s> is valid and that lon/lat is varying.'%(id,jd,expr)

#       Read data and coords
#       --------------------
        try:
            array_ = fromfile(self.Reader,count=nx_*ny_,dtype=float32)
            grid.lon = fromfile(self.Reader,count=nx_,dtype=float32)
            grid.lat = fromfile(self.Reader,count=ny_,dtype=float32)
        except:
            self.flush()
            raise GrADSError, 'problems exporting <'+expr+'>, fromfile() failed'

#       Annotate grid - assumes lon, lat
#       --------------------------------
        dims = ( 'lon', 'lat', 'lev', 'time' )
        grid.dims = [dims[jd],dims[id]]
        grid.time = [ dh.time[0] ]
        grid.lev = ones(1,dtype=float32) * float(dh.lev[0])

#       Check rc from asynchronous ipc_save
#       -----------------------------------
        rc = self._parseReader(Quiet=True)
        if rc:
            self.flush()
            raise GrADSError, 'problems exporting <'+expr+'>, ipc_save() failed'

        grid.tyme = array([gat2dt(t) for t in grid.time])

#       Create the GaField object
#       -------------------------
        data = array_.reshape(ny_,nx_)
        return GaField(data, name=expr, grid=grid, mask=(data==amiss) )
    
#........................................................................

    def imp ( self, name, Field ):
        """
        Sends a GrADS Field containing a NumPy array and associated 
        grid information to GrADS, defining it in GrADS as *name*.
        Notice that *Field* can be an instance of the GaField
        class or a tuple with the (Array,Grid) components.

        Limitation
        ==========

        This function does not handle varying ensemble dimensions in
        GrADS v2.

        """

#       If IPC extension is not available, barf
#       ---------------------------------------
        if not self.HAS_IPC:
            raise GrADSError, "IPC extension not available - cannot import!"

#       Resolve Field
#       -------------
        if isinstance(Field,GaField):
            grid = Field.grid
        else:
            raise GrADSError, "Field has invalid type"
                
#       Retrieve dimension environment
#       ------------------------------
        dh = self.query("dims", Quiet=True) 
        t1, t2 = dh.t
        z1, z2 = dh.z 
        nx, ny, nz, nt = (dh.nx, dh.ny, dh.nz, dh.nt)
        nxy = nx * ny

#       Initial implementation: require x,y to vary
#       Note: remove this restriction is not very hard, but requires
#             special handling the different dimension permutations separately
#             given the way GrADS invokes functions for XZ, YZ, ZT, etc
#       ----------------------------------------------------------------------
        if nx==1: raise GrADSError, 'lon must be varying but got nx=1'
        if ny==1: raise GrADSError, 'lat must be varying but got ny=1'

#       Determine actual load command
#       -----------------------------
        if name == '<display>':
            cmd = 'display ipc_load()\n'
            if nz>1 and nt>1:
                raise GrADSError, \
                      'for <display> only one of z/t can vary'+\
                      ' but got (nz,nt)=(%d,%d)'%(nz,nt) 
        else:
            if self.Version[1] is '1':
                cmd = 'ipc_define %s = ipc_load()\n'%name
            else:
                cmd = 'define %s = ipc_load()\n'%name

#       Tell GrADS to start looking for data in transfer stream
#       -------------------------------------------------------
        try:
            self.cmd("ipc_open - r")
        except GrADSError:
            raise GrADSError, '<ipc_open - r> failed; is IPC installad?'
        self.Writer.write(cmd) # asynchronous transfer

#       Reshape and get original t/z offset
#       -----------------------------------
        t1_, z1_ = (grid.denv.t[0], grid.denv.z[0])
        nt_, nz_ = (grid.denv.nt,grid.denv.nz)
        nx_ = len(grid.lon)
        ny_ = len(grid.lat)
        nxy_ = nx_ * ny_
        data = Field.data.reshape(nt_,nz_,ny_,nx_)
        meta = grid.meta.reshape(nt_,nz_,20)

#       Write the data to transfer stream
#       ----------------------------------
        try:
            for t in range(t1,t2+1):
                l = t - t1_
                for z in range(z1,z2+1):
                    k = z - z1_
                    mx = int(meta[l,k,3])
                    my = int(meta[l,k,4])
                    if mx!=nx_ or my!=ny_:
                        self.flush()
                        raise GrADSError, \
                             'nx/ny mismatch; got (%d,%d), expected (%d,%d)'%\
                             (mx,my,nx_,ny_)
                    meta[l,k,:].tofile(self.Writer)
                    data[l,k,:,:].tofile(self.Writer)
                    grid.lon.tofile(self.Writer)
                    grid.lat.tofile(self.Writer)
                    self.Writer.flush()
        except:
            self.flush()
            self.setdim(dh)
            raise GrADSError, \
                  'could not import <%s>, tofile() may have failed'%name


#       Check rc from asynchronous ipc_save
#       -----------------------------------
        rc = self._parseReader(Quiet=True)
        self.flush()

#       Restore dimension environment
#       -----------------------------
        self.setdim(dh)
        self.cmd("ipc_close")
        if rc:
            raise GrADSError, 'problems importing <'+name+'>, ipc_load() failed'

#........................................................................

    def expr (self, expr):
        """
        Evaluates a GrADS expression returning a GrADS Field. This is similar
        to the exp() method except that the resulting GaField cannot be
        imported back into GrADS. It relies on *gacore* methods eval()
        and coords() to retrieve the data and coordinate information. 
        """

#       For convenience, allows calls where expr is not a string, in which
#        case it returns back the input field or raise an exception
#       -------------------------------------------------------------------
        if type(expr) == StringType:
            pass # OK, will proceed to retrieve it from GrADS
        elif isinstance(expr,GaField):
            return expr # just return input
        elif isinstance(expr,ndarray):
            return expr # this is handy for 'lsq'
        else:
            raise GrADSError, "input <expr> has invalid type: %s"%type(expr)

        d = self.eval(expr)
        c = self.coords()
        g = GaGrid(expr,coords=c)

        Data = reshape(d,c.shape)
        F = GaField(Data,mask=(Data==c.undef),name=expr,grid=g)

        return F

#........................................................................

    def eof ( self, expr, transf='anomaly', metric='area', keep=None):
        """ 
        Given a GrADS generalized expression *expr*, calculates Empirical 
        Orthogonal Functions (EOFS) using Singular Value Decomposition (SVD). 
        
            V, d, c = self.eof(expr)
        
        where

            V  ---  A specialized GrADS Field holding eigenvectors
                    with *add* offset and *scale* factors to aid
                    subsequent decomposition in terms of V
            d  ---  NumPy array with eigenvalues
            c  ---  NumPy array with principal components

        The other optional parameters are:

        transf     
            Type of pre-processing transform to be applied:
            None     ---  time series as is
            anomaly  ---  remove time mean
            z-score  ---  remove time mean and divide 
                          by standard deviation
        metric    
            Determines whether to scale the timeseries prior
            to calculation of the EOFs; this is equivalent to 
            choosing a particular norm. Acceptable values are:
            None   ---  do not scale
            'area' ---  multiply by cos(lat), the default

        keep
            How many eigenvectors to keep:
            None  ---  in this case keep as many vectors as 
                       there are timesteps (nt) 
            n     ---  keep "n" eigenvectors

        Notice that *expr* on input is a *generalized expression* in the
        sense that it can contain a string with a GrADS expression to be
        evaluated or a valid GrADS field. See method *exp* for additional
        information.

        """

#       At least 2 time steps
#       ---------------------
        dh = self.query("dims",Quiet=True)
        if dh.nt < 2:
            raise GrADSError, \
                  'need at least 2 time steps for EOFS but got nt=%d'%dh.nt
        nt, nz, ny, nx = (dh.nt, dh.nz, dh.ny, dh.nx)

#       Export N-dimensional array
#       --------------------------
        u = self.exp(expr)
        g = u.grid

#       Reshape as 4D
#       -------------
        nx = len(g.lon) # may differ from dh.nx
        ny = len(g.lat) # may differ from dh.ny
        u = u.reshape(nt,nz,ny,nx)

#       Remove time mean if necessary
#       -----------------------------
        offset = ma.zeros((nz,ny,nx),dtype=float32) # place holder
        if transf==None:
            pass
        elif transf=='anomaly' or transf=='z-score':
            offset = average(u,axis=0)
            u = u - offset[newaxis,:,:,:]
        else:
            raise GrADSError, 'Unknown transf <%s>'%transf
    
#       Scale by stdv if z-scores required
#       ----------------------------------
        scale = ma.ones((nz,ny,nx),dtype=float32) # place holder
        if transf=='z-score':
            scale = sqrt(average(u*u,axis=0))
            u = u / eofs.stdv[newaxis,:,:,:]

#       Apply metric if so desired
#       Note: may need delp for cases with nz>1
#       ---------------------------------------
        if metric=='area':
            factor = sqrt(cos(pi*g.lat/180.))
            u = u * factor[newaxis,newaxis,:,newaxis]
            scale = scale * factor[newaxis,newaxis,:,newaxis]

#       Singular value decomposition, reuse u
#       ------------------------------------------
        pc, d, u = svd(u.reshape(nt,nz*ny*nx),
                                full_matrices=0)

#       Trim eigenvectors
#       -----------------
        if keep==None:
            nv = nt
        else:
            nv = min(keep,nt)
            u = u[0:nv,:]
            d = d[0:nv]
            pc = pc[:,0:nv]

#       Adjust grid properties
#       ----------------------
        g.dims[0] = 'eof'
        g.time = arange(nv)
        g.eof = arange(nv)

#       Eigenvalues/coefficients
#       ------------------------
        d = d * d / (nt - 1)
        pc = (nt -1) * pc.transpose()

#       Normalize eigenvectors
#       ----------------------
        for i in range(nv):
            vnorm = _norm(u[i,:])
            u[i,:] = u[i,:] / vnorm
            pc[i,:] = pc[i,:] * vnorm
    
#       Reshape eigenvectors as original array
#       --------------------------------------
        if nz==1:
            u = u.reshape(nv,ny,nx)
            offset = offset.reshape(ny,nx)
            scale = scale.reshape(ny,nx)
            g.meta = g.meta[0:nv]
        else:
            u = u.reshape(nv,nz,ny,nx)
            offset = offset.reshape(nz,ny,nx)
            scale = scale.reshape(nz,ny,nx)
            g.meta = g.meta[0:nv,:]

#       Let's make sure "u" is a bonafide GaGield
#       -----------------------------------------
        u = GaField(u.data, name=expr, grid=g, mask=u.mask)
        u.offset = offset
        u.scale = scale

#       Note: since GrADS v1 does not know about e-dimensions yet,
#       we let it think that the EOF dimension is the time dimension

#       All done
#       --------
        return (u, d, pc)

#.....................................................................

    def lsq (self, y_expr, x_exprs, Bias=False, Mask=None):
        """
        Given a target GrADS expression *y_expr* and a tuple of predictor
        GrADS expressions *x_exprs*, returns a NumPy array with linear 
        regression coefficients 

            c, info = self.lsq(y_expr, x_exprs)

        where *info* contains information about the minimization:

            info.residuals  ---  sum square of residuals
            info.rank       ---  rank of the predictor matrix
            info.s          ---  singular values of predictor matrix

        When Bias=False (default) the residual

            y - c[0] * x[:,0] + c[1] * x[:,1] + ... + c[n] * x[:,N-1]

        is minimized in the last square sense, where *x* and *y* are
        NumPy arrays associated with the following GrADS Fields:

            Y = self.exp(y_expr)
            X[:,n] = self.exp(x_exprs[n]),  n = 0, ..., N-1

       When Bias=True, the additional predictor array 

            x[N] = ones(y)

       is included in the calculation, resulting in an output array of
       size N+1. This is equivalent to allowing for an *intercept* in
       the regression equation.

       The optional Mask is a 1D logical array with the location of the
       data to be included (see compress() method).

       On input, all expressions are *generalized expressions* in the
       sense that they can contain a string with a GrADS expression to be
       evaluated or a valid GrADS field. See method *expr* for additional
       information.

       """

        N = len(x_exprs)
        if N<1:
            raise GrADSError, \
                'expecting at least one predictor but got %d'%N
        if Bias: N = N + 1
        
#       Retrieve target
#       ---------------
        f = self.exp(y_expr)
        y = f.ravel()
        if Mask!=None:  
            y = y.compress(Mask)
        M = y.size

#       Retrieve predictors
#       -------------------
        X = ones((M,N),dtype=float32)
        for n in range(len(x_exprs)):
            f = self.exp(x_exprs[n])
            x = f.ravel()
            if Mask!=None: 
                x = x.compress(Mask)
            X[:,n] = x

#       Perform LS minimization
#       -----------------------
        info = GaHandle('lsq')
        (c, info.residuals, info.rank, info.s) = lstsq(X,y)

#       All done
#       --------
        return (c, info)

#.....................................................................

def _norm(x):
    """
    L-2 norm, internal use
    """
    return sqrt(inner(x,x))

