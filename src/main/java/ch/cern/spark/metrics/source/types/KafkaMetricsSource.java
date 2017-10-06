package ch.cern.spark.metrics.source.types;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.VoidFunction;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka010.CanCommitOffsets;
import org.apache.spark.streaming.kafka010.ConsumerStrategies;
import org.apache.spark.streaming.kafka010.HasOffsetRanges;
import org.apache.spark.streaming.kafka010.KafkaUtils;
import org.apache.spark.streaming.kafka010.LocationStrategies;
import org.apache.spark.streaming.kafka010.OffsetRange;

import ch.cern.spark.Properties;
import ch.cern.spark.json.JSONObject;
import ch.cern.spark.json.JSONObjectDeserializer;
import ch.cern.spark.metrics.Metric;
import ch.cern.spark.metrics.source.MetricsSource;

public class KafkaMetricsSource extends MetricsSource {

    private static final long serialVersionUID = 4110858617715602562L;
    
    private Map<String, Object> kafkaParams;
    private Set<String> kafkaTopics;
    
    public static String ATTRIBUTES_PARAM = "parser.attributes";
    private String[] attributes;
    
    public static String VALUE_ATTRIBUTE_PARAM = "parser.value.attribute";
    private String value_attribute;
    
    public static String TIMESTAMP_FORMAT_PARAM = "parser.timestamp.format";
    public static String TIMESTAMP_FORMAT_DEFAULT = "yyyy-MM-dd'T'HH:mm:ssZ";
    private SimpleDateFormat timestamp_format;

    public static String TIMESTAMP_ATTRIBUTE_PARAM = "parser.timestamp.attribute";
    private String timestamp_attribute;

    public KafkaMetricsSource() {
        super(KafkaMetricsSource.class, "kafka");
    }
    
    @Override
    public void config(Properties properties) throws Exception {
        kafkaParams = getKafkaConsumerParams(properties);
        kafkaTopics = new HashSet<String>(Arrays.asList(properties.getProperty("topics").split(",")));
        
        attributes = properties.getProperty(ATTRIBUTES_PARAM).split("\\s");
        value_attribute = properties.getProperty(VALUE_ATTRIBUTE_PARAM);
        timestamp_attribute = properties.getProperty(TIMESTAMP_ATTRIBUTE_PARAM);
        timestamp_format = new SimpleDateFormat(properties.getProperty(TIMESTAMP_FORMAT_PARAM, TIMESTAMP_FORMAT_DEFAULT));
    }
    
    @Override
    protected JavaDStream<Metric> createStream(JavaStreamingContext ssc) {
        JavaDStream<JSONObject> inputStream = createKafkaInputStream(ssc);

        JavaDStream<Metric> metricStream = parse(inputStream); 
        
        return metricStream;
    }
    
    public JavaDStream<JSONObject> createKafkaInputStream(JavaStreamingContext ssc) {
        final JavaInputDStream<ConsumerRecord<String, JSONObject>> inputStream = KafkaUtils.createDirectStream(
                ssc,
                LocationStrategies.PreferConsistent(),
                ConsumerStrategies.<String, JSONObject>Subscribe(kafkaTopics, kafkaParams));
        
        inputStream.foreachRDD(new VoidFunction<JavaRDD<ConsumerRecord<String,JSONObject>>>() {
            private static final long serialVersionUID = -7317892726324251129L;

            @Override
            public void call(JavaRDD<ConsumerRecord<String, JSONObject>> rdd) throws Exception {
                OffsetRange[] offsetRanges = ((HasOffsetRanges) rdd.rdd()).offsetRanges();

                ((CanCommitOffsets) inputStream.inputDStream()).commitAsync(offsetRanges);
            }
        });
        
        return inputStream.map(new Function<ConsumerRecord<String,JSONObject>, JSONObject>() {
            private static final long serialVersionUID = 2782425224401441788L;

            @Override
            public JSONObject call(ConsumerRecord<String, JSONObject> record) throws Exception {
                return record.value();
            }
        });
    }

    private Map<String, Object> getKafkaConsumerParams(Properties props) {
        Map<String, Object> kafkaParams = new HashMap<String, Object>();
        
        kafkaParams.put("key.deserializer", StringDeserializer.class);
        kafkaParams.put("value.deserializer", JSONObjectDeserializer.class);
        
        Properties kafkaPropertiesFromConf = props.getSubset("consumer");
        for (Entry<Object, Object> kafkaPropertyFromConf : kafkaPropertiesFromConf.entrySet()) {
            String key = (String) kafkaPropertyFromConf.getKey();
            String value = (String) kafkaPropertyFromConf.getValue();
            
            kafkaParams.put(key, value);
        }
        
        return kafkaParams;
    }
    
    private JavaDStream<Metric> parse(JavaDStream<JSONObject> inputStream) {
        return inputStream.map(new Function<JSONObject, Metric>() {
            private static final long serialVersionUID = -7684212727326278652L;

            @Override
            public Metric call(JSONObject jsonObject) throws Exception {
                Date timestamp = toDate(jsonObject.getProperty(timestamp_attribute));
                
                float value = Float.parseFloat(jsonObject.getProperty(value_attribute));

                Map<String, String> ids = new HashMap<>();
                for (String attribute : attributes)
                    ids.put(attribute, jsonObject.getProperty(attribute));
                
                Metric metric = new Metric(timestamp, value, ids);
                    
                metric.setIDs(ids);
                
                return metric;
            }
        });
    }
    
    private Date toDate(String date_string) throws ParseException {
        return timestamp_format.parse(date_string);
    }

}
