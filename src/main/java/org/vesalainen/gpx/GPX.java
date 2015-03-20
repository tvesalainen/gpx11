/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.vesalainen.gpx;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import org.vesalainen.jaxb.gpx.ExtensionsType;
import org.vesalainen.jaxb.gpx.GpxType;
import org.vesalainen.jaxb.gpx.ObjectFactory;
import org.vesalainen.jaxb.gpx.TrkType;
import org.vesalainen.jaxb.gpx.TrksegType;
import org.vesalainen.jaxb.gpx.WptType;
import org.w3c.dom.Element;

/**
 * @author Timo Vesalainen
 */
public class GPX
{
    protected static JAXBContext jaxbCtx;
    protected static ObjectFactory factory;
    protected static DatatypeFactory dtFactory;
    protected JAXBElement<GpxType> gpx;

    static
    {
        try
        {
            jaxbCtx = JAXBContext.newInstance("org.vesalainen.jaxb.gpx");
            factory = new ObjectFactory();
            dtFactory = DatatypeFactory.newInstance();
        }
        catch (DatatypeConfigurationException ex)
        {
            throw new IllegalArgumentException(ex);
        }
        catch (JAXBException ex)
        {
            throw new IllegalArgumentException(ex);
        }
    }
    public GPX()
    {
        gpx = factory.createGpx(factory.createGpxType());
    }

    public GPX(File file) throws IOException, JAXBException
    {
        Unmarshaller unmarshaller = jaxbCtx.createUnmarshaller();
        try (FileInputStream fis = new FileInputStream(file))
        {
            gpx = (JAXBElement<GpxType>) unmarshaller.unmarshal(fis); //NOI18N
        }
    }

    public GPX(InputStream is) throws IOException, JAXBException
    {
        Unmarshaller unmarshaller = jaxbCtx.createUnmarshaller();
        gpx = (JAXBElement<GpxType>) unmarshaller.unmarshal(is);
    }

    public ObjectFactory getFactory()
    {
        return factory;
    }

    public JAXBElement<GpxType> getGpx()
    {
        return gpx;
    }

    public DatatypeFactory getDtFactory()
    {
        return dtFactory;
    }
    public void write(Writer writer) throws IOException
    {
        try
        {
            Marshaller marshaller = jaxbCtx.createMarshaller();
            marshaller.marshal(gpx, writer);
        }
        catch (JAXBException ex)
        {
            throw new IOException(ex);
        }
    }
    /**
     * Browse gpx file and drop aligned waypoints
     * @param bearingTolerance If bearing is under the waypoints is considered aligned
     * @param minDistance Waypoints closer are dropped.
     * @param handler 
     */
    public void browse(double bearingTolerance, double minDistance, final TrackHandler handler)
    {
        browse(bearingTolerance, minDistance, Double.MAX_VALUE, handler);
    }
    /**
     * Browse gpx file and drop aligned waypoints
     * @param bearingTolerance If bearing is under the waypoints is considered aligned
     * @param minDistance Waypoints closer are dropped.
     * @param maxSpeed Knots. If waypoints distance implies greater speed the waypoint dropped.
     * @param handler 
     */
    public void browse(double bearingTolerance, double minDistance, double maxSpeed, final TrackHandler handler)
    {
        JAXBElement<GpxType> gpx1 = getGpx();
        GpxType gpxType = gpx1.getValue();
        List<TrkType> trkList = gpxType.getTrk();
        for (TrkType trk : trkList)
        {
            String name = trk.getName();
            ExtensionsType extensions = trk.getExtensions();
            boolean cont = handler.startTrack(name, extensions.getAny());
            if (cont)
            {
                List<TrksegType> trksegList = trk.getTrkseg();
                for (TrksegType tt : trksegList)
                {
                    handler.startTrackSeq();
                    WaypointFilter filter = new WaypointFilter(bearingTolerance, minDistance, maxSpeed)
                    {
                        @Override
                        protected void output(WptType wt)
                        {
                            handler.trackPoint(
                                    wt.getLat().doubleValue(), 
                                    wt.getLon().doubleValue(), 
                                    wt.getTime().toGregorianCalendar().getTimeInMillis()
                                    );
                        }
                    };

                    List<WptType> trkptList = tt.getTrkpt();
                    for (WptType wt : trkptList)
                    {
                        filter.input(wt);
                    }
                    filter.flush();
                    handler.endTrackSeq();
                }
            }
            handler.endTrack();
        }
    }
    public static String getText(ExtensionsType extensions, String namespaceURI, String localname)
    {
        for (Object ob : extensions.getAny())
        {
            if (ob instanceof Element)
            {
                Element el = (Element) ob;
                if (
                        namespaceURI.equals(el.getNamespaceURI()) &&
                        localname.equals(el.getLocalName())
                        )
                {
                    return el.getTextContent();
                }
            }
        }
        return null;
    }
    public static double departure(WptType loc1, WptType loc2)
    {
        return Math.cos(Math.toRadians((loc2.getLat().doubleValue()+loc1.getLat().doubleValue())/2));
    }
    /**
     * Return bearing from wp1 to wp2 in degrees
     * @param wp1
     * @param wp2
     * @return 
     */
    public static double bearing(WptType wp1, WptType wp2)
    {
        double dep = departure(wp1, wp2);
        double aa = dep*(wp2.getLon().doubleValue()-wp1.getLon().doubleValue());
        double bb = wp2.getLat().doubleValue()-wp1.getLat().doubleValue();
        double dd = Math.atan2(aa, bb);
        if (dd < 0)
        {
            dd += 2*Math.PI;
        }
        return Math.toDegrees(dd);
    }
    /**
     * Return distance between wp1 and wp2 in NM
     * @param wp1
     * @param wp2
     * @return 
     */
    public static double distance(WptType wp1, WptType wp2)
    {
        double dep = departure(wp1, wp2);
        return 60*Math.hypot(
                wp1.getLat().doubleValue()-wp2.getLat().doubleValue(),
                dep*(wp1.getLon().doubleValue()-wp2.getLon().doubleValue())
                );
    }
    public static double speed(WptType wp1, WptType wp2)
    {
        double distance = distance(wp1, wp2);
        double duration = 
                wp2.getTime().toGregorianCalendar().getTimeInMillis()-
                wp1.getTime().toGregorianCalendar().getTimeInMillis();
        double hours = duration/3600000.0;
        double speed = distance/hours;
        return speed;
    }
    public abstract static class WaypointFilter
    {
        private final double bearingTolerance;
        private final double minDistance;
        private final double maxSpeed;
        private double lastBearing = Double.NaN;
        private WptType last;
        private final List<WptType> buffer = new ArrayList<>();
        /**
         * 
         * @param bearingTolerance
         * @param minDistance
         * @param maxSpeed Knots. If waypoints distance implies greater speed the waypoint dropped.
         */
        public WaypointFilter(double bearingTolerance, double minDistance, double maxSpeed)
        {
            this.bearingTolerance = bearingTolerance;
            this.minDistance = minDistance;
            this.maxSpeed = maxSpeed;
        }

        public void input(WptType wp)
        {
            switch (buffer.size())
            {
                case 0:
                    buffer.add(wp);
                    break;
                case 1:
                    if (speed(buffer.get(0), wp) <= maxSpeed)
                    {
                        doInput(buffer.get(0));
                        doInput(wp);
                        buffer.clear();
                    }
                    else
                    {
                        buffer.add(wp);
                    }
                    break;
                case 2:
                    if (speed(buffer.get(0), wp) <= maxSpeed)
                    {
                        doInput(buffer.get(0));
                        doInput(wp);
                        buffer.clear();
                    }
                    else
                    {
                        if (speed(buffer.get(1), wp) <= maxSpeed)
                        {
                            doInput(buffer.get(1));
                            doInput(wp);
                            buffer.clear();
                        }
                        else
                        {
                            buffer.clear();
                        }
                    }
                    break;
            }
        }
        private void doInput(WptType wp)
        {
            if (last == null)
            {
                last = wp;
                output(wp);
            }
            else
            {
                if (Double.isNaN(lastBearing))
                {
                    lastBearing = bearing(last, wp);
                    output(wp);
                }
                else
                {
                    double bearing = bearing(last, wp);
                    double distance = distance(last, wp);
                    if (
                            Math.abs(bearing-lastBearing) > bearingTolerance &&
                            distance > minDistance
                            )
                    {
                        last = wp;
                        lastBearing = bearing;
                        output(wp);
                    }
                }
            }
        }

        protected void flush()
        {
            
        }
        protected abstract void output(WptType wp);
        
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) 
    {
        try
        {
            File f = new File("C:\\Users\\tkv\\Dropbox\\Brest - La Coruna.gpx");
            GPX gpx = new GPX(f);
            JAXBElement<GpxType> gpx1 = gpx.getGpx();
            GpxType gpxType = gpx1.getValue();
            List<TrkType> trkList = gpxType.getTrk();
            for (TrkType trk : trkList)
            {
                ExtensionsType extensions = trk.getExtensions();
                System.err.println(getText(extensions, "http://www.opencpn.org", "guid"));
                List<TrksegType> trksegList = trk.getTrkseg();
                for (TrksegType tt : trksegList)
                {
                    WaypointFilter filter = new WaypointFilter(1.0, 0.1, 15)
                    {
                        @Override
                        protected void output(WptType wt)
                        {
                            System.err.println(wt.getLat()+" "+wt.getLon()+" "+wt.getTime());
                        }
                    };

                    List<WptType> trkptList = tt.getTrkpt();
                    for (WptType wt : trkptList)
                    {
                        filter.input(wt);
                    }
                }
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

}
