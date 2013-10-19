/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.vesalainen.gpx;

import com.topografix.gpx._1._1.GpxType;
import com.topografix.gpx._1._1.ObjectFactory;
import com.topografix.gpx._1._1.TrkType;
import com.topografix.gpx._1._1.TrksegType;
import com.topografix.gpx._1._1.WptType;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;

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
            jaxbCtx = JAXBContext.newInstance("com.topografix.gpx._1._1");
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
     * @param args the command line arguments
     */
    public static void main(String[] args) 
    {
        try
        {
            File f = new File("trackit.gpx");
            GPX gpx = new GPX(f);
            JAXBElement<GpxType> gpx1 = gpx.getGpx();
            GpxType gpxType = gpx1.getValue();
            List<TrkType> trkList = gpxType.getTrk();
            for (TrkType trk : trkList)
            {
                List<TrksegType> trksegList = trk.getTrkseg();
                for (TrksegType tt : trksegList)
                {
                    List<WptType> trkptList = tt.getTrkpt();
                    for (WptType wt : trkptList)
                    {
                        System.err.println(wt.getLat()+" "+wt.getLon()+" "+wt.getTime());
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
