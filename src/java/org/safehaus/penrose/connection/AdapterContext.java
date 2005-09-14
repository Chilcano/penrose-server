package org.safehaus.penrose.connection;

import org.safehaus.penrose.engine.TransformEngine;
import org.safehaus.penrose.config.Config;
import org.safehaus.penrose.mapping.Source;

/**
 * @author Endi S. Dewata
 */
public interface AdapterContext {

    public Config getConfig(Source source) throws Exception;
    public TransformEngine getTransformEngine() throws Exception;
}
