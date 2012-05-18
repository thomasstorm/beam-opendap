package org.esa.beam.opendap;


import opendap.dap.BaseType;

import java.io.IOException;
import java.net.URL;
import java.util.Set;

/**
 * @author Thomas Storm
 */
interface OpenDapInterface {
    Set<BaseType> getVariables(URL url) throws IOException;
}
