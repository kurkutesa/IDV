//
// Gridded3DSet.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2009 Bill Hibbard, Curtis Rueden, Tom
Rink, Dave Glowacki, Steve Emmerson, Tom Whittaker, Don Murray, and
Tommy Jasmin.

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Library General Public
License as published by the Free Software Foundation; either
version 2 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Library General Public License for more details.

You should have received a copy of the GNU Library General Public
License along with this library; if not, write to the Free
Software Foundation, Inc., 59 Temple Place - Suite 330, Boston,
MA 02111-1307, USA
*/

package ucar.unidata.data;
import visad.*;

import java.io.InputStreamReader;
import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import visad.util.Trace;

/**
 * Gridded3DSet represents a finite set of samples of R^3.
 * <P>
 */
public class CachedGridded2DSet extends Gridded2DSet {
    private Object cacheId;

  public CachedGridded2DSet(MathType type, float[][] samples, int lengthX, int lengthY,
               CoordinateSystem coord_sys, Unit[] units,
               ErrorEstimate[] errors, boolean copy, boolean test)
               throws VisADException {
      super(type, samples, lengthX, lengthY, coord_sys, units, errors, copy, test);
      cacheId = DataCacheManager.getCacheManager().addToCache(samples);
      super.setMySamples(null);
  }


    public void finalize()  throws Throwable {
        super.finalize();
        DataCacheManager.getCacheManager().removeFromCache(cacheId);
    }

  protected void setMySamples(float[][]samples) {
      DataCacheManager.getCacheManager().updateData(cacheId, myFloatValues);
      super.setMySamples(null);
  }

  protected float[][] getMySamples() {
      System.err.println (" getMySamples");
      //      if(cachedSamples==null && haveCached) {
      //          readCache();
      //      }
      return cachedSamples;
      //      return super.getMySamples();
  }

    private void readCache() {

    }


    protected String getCacheFile() {
        if ((cacheFile == null) && (cacheDir != null)) {
            String uniqueName = "field_" + System.currentTimeMillis() + "_"
                                + (cnt++);
            cacheFile = cacheDir+ File.separator+ uniqueName;
        }
        return cacheFile;
    }

    /**
     * Set where we write to
     *
     * @param f Cache dir
     */
    public static void setCacheDir(File f) {
        cacheDir = f;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public static File getCacheDir() {
        return cacheDir;
    }



}