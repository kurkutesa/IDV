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
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import visad.util.Trace;

/**
 * Gridded3DSet represents a finite set of samples of R^3.
 * <P>
 */
public class CachedGridded3DSet extends Gridded3DSet {

  public CachedGridded3DSet(MathType type, float[][] samples, int lengthX,
      int lengthY, int lengthZ, CoordinateSystem coord_sys, Unit[] units,
      ErrorEstimate[] errors, boolean copy, boolean test) throws VisADException {
    super(type, samples, lengthX, lengthY, lengthZ,  coord_sys,
        units, errors, copy,test);
  }



  public CachedGridded3DSet(MathType type, float[][] samples, int lengthX,
      CoordinateSystem coord_sys, Unit[] units, ErrorEstimate[] errors,
      boolean copy) throws VisADException {
    super(type, samples, lengthX, coord_sys, units,
        errors, copy);
  }


  public CachedGridded3DSet(MathType type, float[][] samples, int lengthX,
      int lengthY, CoordinateSystem coord_sys, Unit[] units,
      ErrorEstimate[] errors, boolean copy) throws VisADException {
      super(type, samples, lengthX, lengthY, coord_sys, units, errors, copy);
  }



  protected void setMySamples(float[][]samples) {
      System.err.println ("setMySamples");
      super.setMySamples(samples);
  }

  protected float[][] getMySamples() {
      System.err.println ("getMySamples");
      return super.getMySamples();
  }

}