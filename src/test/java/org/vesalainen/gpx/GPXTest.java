/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.vesalainen.gpx;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen
 */
public class GPXTest
{
    
    public GPXTest()
    {
    }

    @Test
    public void test0()
    {
        GPX gpx = new GPX();
        assertNotNull(gpx);
    }

}
