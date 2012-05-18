package org.esa.beam.opendap;

import opendap.dap.BaseType;
import opendap.dap.DAP2Exception;
import opendap.dap.DConnect2;
import opendap.dap.DDS;
import org.esa.beam.framework.datamodel.ProductData;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import static junit.framework.Assert.*;

/**
 * @author Thomas Storm
 */
public class FilterTest {

    // todo - somehow remove references to opendap test server (e.g. by introducing a specific 'DataSource'-interface)

    @Test
    public void testFilterByFileName() throws Exception {
        final Filter filter = new Filter(new URL("http://test.opendap.org/dap/data/nc"), "fnoc1\\.nc|fnoc2\\.nc", null, -1, -1, -1, -1, null, null);
        final Set<URL> urls = filter.filterByFileName();
        assertEquals(2, urls.size());
        final URL[] urlsArray = urls.toArray(new URL[urls.size()]);
        Arrays.sort(urlsArray, new Comparator<URL>() {
            @Override
            public int compare(URL o1, URL o2) {
                return o1.toString().compareTo(o2.toString());
            }
        });
        assertTrue(new URL("http://test.opendap.org/dap/data/nc/fnoc1.nc").sameFile(urlsArray[0]));
        assertTrue(new URL("http://test.opendap.org/dap/data/nc/fnoc2.nc").sameFile(urlsArray[1]));
    }

    @Test
    public void testFilter() throws Exception {
        double minLat = -89.0;
        double maxLat = 89.0;
        double minLon = -179.0;
        double maxLon = 180.0;
        Filter filter = new Filter(new URL("http://test.opendap.org/dap/data/nc"), "sst.*\\.nc\\.gz", new MyOpenDapInterface(), minLat, maxLat, minLon, maxLon, ProductData.UTC.parse("1970-01-01", "yyyy-MM-dd"), ProductData.UTC.parse("2200-01-01", "yyyy-MM-dd"));
        Set<URL> productUrls = filter.filter();
        boolean containsSst = false;
        for (URL productUrl : productUrls) {
            if (productUrl.toString().equals("http://test.opendap.org/dap/data/nc/sst.mnmean.nc.gz")) {
                containsSst = true;
            }
        }
        assertTrue(containsSst);

        minLat = -80.0; // greater than product's minimum latitude, so product is expected to be filtered out
        maxLat = 89.0;
        minLon = -179.0;
        maxLon = 180.0;
        filter = new Filter(new URL("http://test.opendap.org/dap/data/nc"), "sst.*\\.nc\\.gz", new MyOpenDapInterface(), minLat, maxLat, minLon, maxLon, ProductData.UTC.parse("1970-01-01", "yyyy-MM-dd"), ProductData.UTC.parse("2200-01-01", "yyyy-MM-dd"));
        productUrls = filter.filter();
        containsSst = false;
        for (URL productUrl : productUrls) {
            if (productUrl.toString().equals("http://test.opendap.org/dap/data/nc/sst.mnmean.nc.gz")) {
                containsSst = true;
            }
        }
        assertFalse(containsSst);

        minLat = -89.0;
        maxLat = 89.0;
        minLon = -20.0;  // greater than product's minimum latitude, so product is expected to be filtered out
        maxLon = 180.0;
        filter = new Filter(new URL("http://test.opendap.org/dap/data/nc"), "sst.*\\.nc\\.gz", new MyOpenDapInterface(), minLat, maxLat, minLon, maxLon, ProductData.UTC.parse("1970-01-01", "yyyy-MM-dd"), ProductData.UTC.parse("2200-01-01", "yyyy-MM-dd"));
        productUrls = filter.filter();
        containsSst = false;
        for (URL productUrl : productUrls) {
            if (productUrl.toString().equals("http://test.opendap.org/dap/data/nc/sst.mnmean.nc.gz")) {
                containsSst = true;
            }
        }
        assertFalse(containsSst);
    }

    private static class MyOpenDapInterface implements OpenDapInterface {
        public Set<BaseType> getVariables(URL url) throws IOException {
            final DConnect2 dConnect;
            final DDS dds;
            try {
                dConnect = new DConnect2(url.openStream());
                dds = dConnect.getDDS();
            } catch (DAP2Exception e) {
                throw new IOException(e);
            }
            final Enumeration variables = dds.getVariables();
            Set<BaseType> variableNames = new HashSet<BaseType>();
            while (variables.hasMoreElements()) {
                final BaseType variable = (BaseType) variables.nextElement();
                variableNames.add(variable);
            }
            return variableNames;
        }
    }
}
