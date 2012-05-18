package org.esa.beam.opendap;

import opendap.dap.Attribute;
import opendap.dap.BaseType;
import opendap.dap.DAS;
import opendap.dap.DConnect2;
import opendap.dap.DDS;
import org.junit.Before;
import org.junit.Test;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * @author Thomas Storm
 */
public class TestConnection {

    private DConnect2 dConnect;

    @Before
    public void setUp() throws Exception {
        String url = "http://test.opendap.org/dap/data/nc/sst.mnmean.nc.gz";
        dConnect = new DConnect2(url);
    }

    @Test
    public void testGetDDS() throws Exception {
        final DDS dds = dConnect.getDDS();
        final Enumeration variables = dds.getVariables();
        Set<String> variableNames = new HashSet<String>();
        while (variables.hasMoreElements()) {
            final BaseType variable = (BaseType) variables.nextElement();
            variableNames.add(variable.toString());
//            assertFalse(variable.getAttributeNames().hasMoreElements());
        }
        assertTrue(variableNames.contains("lat"));
        assertTrue(variableNames.contains("lon"));
        assertTrue(variableNames.contains("time"));
        assertTrue(variableNames.contains("time_bnds"));
        assertTrue(variableNames.contains("sst"));
    }

    @Test
    public void testGetDAS() throws Exception {
        final DAS das = dConnect.getDAS();
        final Enumeration attributeNames = das.getNames();
        assertTrue(attributeNames.hasMoreElements());
        final HashSet<String> attributeNameSet = new HashSet<String>();
        while(attributeNames.hasMoreElements()) {
            attributeNameSet.add(attributeNames.nextElement().toString());
        }
        assertTrue(attributeNameSet.contains("lat"));
        assertTrue(attributeNameSet.contains("lon"));
        assertTrue(attributeNameSet.contains("time"));
        assertTrue(attributeNameSet.contains("time_bnds"));
        assertTrue(attributeNameSet.contains("sst"));
        assertTrue(attributeNameSet.contains("NC_GLOBAL"));

        final Attribute latAttribute = das.getAttributeTable("lat").getAttribute("lat");
        final Enumeration enumeration = latAttribute.getContainer().getNames();
        while (enumeration.hasMoreElements()) {
            System.out.println("enumeration = " + enumeration.nextElement().toString());
        }
    }

    @Test
    public void testDownloadData() throws Exception {
        InputStream inputStream = null;
        OutputStream os = null;
        try {
            URL url = new URL("http://test.opendap.org/dap/data/nc/data.nc");
            final URLConnection connection = url.openConnection();
            inputStream = connection.getInputStream();
            os = new FileOutputStream("c:\\Users\\Thomas\\Desktop\\deleteme.nc");
            final byte[] buffer = new byte[50 * 1024];
            while (inputStream.read(buffer) != -1) {
                os.write(buffer);
            }
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
            if (os != null) {
                os.close();
            }
        }
    }
}
