package org.esa.beam.opendap;

import opendap.dap.BaseType;
import org.esa.beam.framework.datamodel.ProductData;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * @author Thomas Storm
 */
class Filter {


    private final URL url;
    private final String fileNamePattern;
    private final OpenDapInterface openDapInterface;
    private final double minLat;
    private final double maxLat;
    private final double minLon;
    private final double maxLon;
    private final ProductData.UTC startDate;
    private final ProductData.UTC endDate;

    public Filter(URL url, String fileNamePattern, OpenDapInterface openDapInterface, double minLat, double maxLat, double minLon, double maxLon, ProductData.UTC startDate, ProductData.UTC endDate) {
        // todo - move this insanity into config class and create builder around it
        this.url = url;
        this.fileNamePattern = fileNamePattern;
        this.openDapInterface = openDapInterface;
        this.minLat = minLat;
        this.maxLat = maxLat;
        this.minLon = minLon;
        this.maxLon = maxLon;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public Set<URL> filter() throws Exception {
        final Set<URL> productUrls = filterByFileName();
        for (URL productUrl : productUrls) {
            final Set<BaseType> variables = openDapInterface.getVariables(url);
            // todo - continue implementation here
        }
        return productUrls;
    }

    Set<URL> filterByFileName() throws IOException {
        final Set<URL> productUrls = new HashSet<URL>();
        final StringWriter stringWriter = new StringWriter();
        FilterInputStream urlContent = null;
        try {
            URLConnection urlConnection = url.openConnection();
            urlContent = (FilterInputStream) urlConnection.getContent();
            CharsetDecoder decoder = getContentDecoder(urlConnection.getContentEncoding());
            final byte[] buffer = new byte[120 * 1024];
            while (urlContent.read(buffer) != -1) {
                final CharBuffer charBuffer = decoder.decode(ByteBuffer.wrap(buffer));
                stringWriter.write(charBuffer.array());
            }
            final List<String> lines = findReferences(stringWriter.toString());
            for (String line : lines) {
                final String fileName = line.split("\">")[0].split("<a.*href=\"")[1];
                if (fileName.matches(fileNamePattern)) {
                    productUrls.add(new URL(url.toString() + "/" + fileName));
                }
            }
        } catch (CharacterCodingException e) {
            throw new IOException(e);
        } catch (MalformedURLException e) {
            throw new IOException(e);
        } finally {
            if (urlContent != null) {
                urlContent.close();
            }
            stringWriter.close();
        }
        return productUrls;
    }

    private List<String> findReferences(String html) {
        final StringTokenizer stringTokenizer = new StringTokenizer(html, "\n");
        final List<String> lines = new ArrayList<String>();
        while (stringTokenizer.hasMoreTokens()) {
            final String line = stringTokenizer.nextToken();
            if (line.matches("\\s*<a\\s*href\\=.*")) {
                lines.add(line.trim());
            }
        }
        return lines;
    }

    private CharsetDecoder getContentDecoder(String contentEncoding) {
        CharsetDecoder decoder;
        if (contentEncoding == null) {
            Charset charset = Charset.forName("UTF-8");
            decoder = charset.newDecoder();
        } else {
            decoder = Charset.forName(contentEncoding).newDecoder();
        }
        return decoder;
    }

}
