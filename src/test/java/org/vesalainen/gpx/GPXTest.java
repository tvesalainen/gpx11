/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.vesalainen.gpx;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBException;
import static org.junit.Assert.*;
import org.junit.Test;

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

    @Test
    public void test1()
    {
        try (InputStream is = GPXTest.class.getClassLoader().getResourceAsStream("laspalmas-lasgalletas.gpx");)
        {
            GPX gpx = new GPX(is);
            assertNotNull(gpx);
            TrackHandler handler = new TrackHandler()
            {
                private double lat;
                private double lon;
                private long tim;
                
                @Override
                public boolean startTrack(String name, Collection<Object> extensions)
                {
                    return true;
                }
                @Override
                public void endTrack()
                {
                }
                @Override
                public void startTrackSeq()
                {
                }
                @Override
                public void endTrackSeq()
                {
                }
                @Override
                public void trackPoint(double latitude, double longitude, long time)
                {
                    if (lat > 0.0)
                    {
                        double dep = Math.cos(Math.toRadians((latitude+lat)/2));
                        double dist = 60*Math.hypot(latitude-lat, dep*(longitude-lon));
                        double hours = (time-tim)/3600000.0;
                        double speed = dist/hours;
                        assertTrue(speed < 10);
                    }
                    lat = latitude;
                    lon = longitude;
                    tim = time;
                }
            };
            gpx.browse(3, 0.1, 10, handler);
        }
        catch(IOException | JAXBException ex)
        {
            fail(ex.getMessage());
        }
    }

}
