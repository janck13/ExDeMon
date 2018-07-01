package ch.cern.spark.metrics.schema;

import org.apache.log4j.Logger;
import org.apache.spark.streaming.api.java.JavaDStream;

import ch.cern.properties.Properties;
import ch.cern.spark.metrics.Metric;

public class MetricSchemas {
	
	private transient final static Logger LOG = Logger.getLogger(MetricSchemas.class.getName());
	
	private static final int JSON_MAX_SIZE = 64000;

    public static final String PARAM = "metrics.schema";

	public static JavaDStream<Metric> generate(JavaDStream<String> jsons, Properties propertiesSourceProps, String sourceId) {
	    jsons = jsons.filter(string -> {
                	        if(string.length() > JSON_MAX_SIZE) {
                	            LOG.warn("Event dropped because exceeds max size ("+JSON_MAX_SIZE+"): " + string.substring(0, 10000) + "...");
                	            
                	            return false;
                	        }
                	        
                	        return true;
                	    });
	    
		return jsons.flatMap(new MetricSchemasF(propertiesSourceProps, sourceId));
	}

}
