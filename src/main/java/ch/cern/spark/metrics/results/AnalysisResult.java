package ch.cern.spark.metrics.results;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.spark.streaming.Time;

import ch.cern.spark.metrics.Metric;
import ch.cern.spark.metrics.MonitorIDMetricIDs;
import ch.cern.spark.metrics.monitor.Monitor;

public class AnalysisResult implements Serializable {

    private static final long serialVersionUID = -5307381437257371176L;

    public enum Status {OK, WARNING, ERROR, EXCEPTION};
    
    public Date analysis_timestamp;
    
    public Metric analyzed_metric;
    
    public Status status;
    public String status_reason;
    
    private Map<String, Object> monitor_params;
    
    public AnalysisResult() {
        analysis_timestamp = new Date();
        monitor_params = new HashMap<String, Object>();
    }

    public void setAnalyzedMetric(Metric metric) {
        this.analyzed_metric = metric;
    }

    public void setStatus(Status status, String reason) {
        this.status = status;
        this.status_reason = reason;
    }

    public Status getStatus() {
        return status;
    }

    public void addMonitorParam(String key, Object value) {
        monitor_params.put(key, value);
    }

    public boolean hasStatus() {
        return status != null;
    }
    
    public Date getAnalysisTimestamp(){
        return analysis_timestamp;
    }
    
    public Metric getAnalyzedMetric(){
        return analyzed_metric;
    }
    
    public String getStatusReason() {
        return status_reason;
    }
    
    public static AnalysisResult buildWithStatus(Status status, String reason){
        AnalysisResult result = new AnalysisResult();
        
        result.setStatus(status, reason);
        
        return result;
    }
    
    @Override
    public String toString() {
        return "AnalysisResult [analysis_timestamp=" + analysis_timestamp + ", analyzed_metric=" + analyzed_metric
                + ", status=" + status + ", status_reason=" + status_reason + ", monitor_params=" + monitor_params
                + "]";
    }

    public static AnalysisResult buildTimingOut(MonitorIDMetricIDs ids, Monitor monitor, Time time) {
        AnalysisResult result = AnalysisResult.buildWithStatus(Status.EXCEPTION, "Metric has timmed out.");
        
        result.setAnalyzedMetric(new Metric(new Date(time.milliseconds()), 0f, ids.getMetricIDs()));
        result.addMonitorParam("name", ids.getMonitorID());
        
        return result;
    }
    
    public static AnalysisResult buildMissingMetric(MonitorIDMetricIDs ids, Monitor monitor, Time time, long elapsedTime) {
        AnalysisResult result = AnalysisResult.buildWithStatus(Status.EXCEPTION, "Metric missing for "+elapsedTime+" seconds.");
        
        result.setAnalyzedMetric(new Metric(new Date(time.milliseconds()), 0f, ids.getMetricIDs()));
        result.addMonitorParam("name", ids.getMonitorID());
        
        return result;
    }

    public Map<String, Object> getMonitorParams() {
        return monitor_params;
    }
    
}
