package ch.cern.spark.metrics.notifications;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.apache.spark.api.java.Optional;
import org.apache.spark.streaming.State;
import org.apache.spark.streaming.StateImpl;
import org.junit.Test;

import ch.cern.spark.Properties.Expirable;
import ch.cern.spark.PropertiesTest;
import ch.cern.spark.metrics.Metric;
import ch.cern.spark.metrics.notificator.NotificatorID;
import ch.cern.spark.metrics.results.AnalysisResult;
import ch.cern.spark.metrics.results.AnalysisResult.Status;
import ch.cern.spark.metrics.store.Store;

public class UpdateNotificationStatusesFTest {

    @Test
    public void raiseAlwaysSameStatus() throws Exception{
        Expirable props = PropertiesTest.mockedExpirable();
        props.get().setProperty("monitor.monID.notificator.notID.type", "constant");
        props.get().setProperty("monitor.monID.notificator.notID.statuses", "error");
        props.get().setProperty("monitor.monID.notificator.notID.period", "10s");
        
        UpdateNotificationStatusesF func = new UpdateNotificationStatusesF(props);
        
        Optional<Notification> notification = null;
                
        NotificatorID ids = new NotificatorID("monID", "notID", null);
        Optional<AnalysisResult> resuktOpt = Optional.of(new AnalysisResult());
        resuktOpt.get().setStatus(Status.ERROR, "");
        
        State<Store> storeState = new StateImpl<Store>();
        
        resuktOpt.get().setAnalyzedMetric(new Metric(new Date(10000), 0f, null));
        notification = func.call(null, ids, resuktOpt, storeState);
        assertFalse(notification.isPresent());
        
        resuktOpt.get().setAnalyzedMetric(new Metric(new Date(21000), 0f, null));
        notification = func.call(null, ids, resuktOpt, storeState);
        assertTrue(notification.isPresent());
    }
    
    @Test
    public void notRaiseAfterRaising() throws Exception{
        Expirable props = PropertiesTest.mockedExpirable();
        props.get().setProperty("monitor.monID.notificator.notID.type", "constant");
        props.get().setProperty("monitor.monID.notificator.notID.statuses", "error");
        props.get().setProperty("monitor.monID.notificator.notID.period", "10s");
        
        UpdateNotificationStatusesF func = new UpdateNotificationStatusesF(props);
        
        Optional<Notification> notification = null;
                
        NotificatorID ids = new NotificatorID("monID", "notID", null);
        Optional<AnalysisResult> resuktOpt = Optional.of(new AnalysisResult());
        resuktOpt.get().setStatus(Status.ERROR, "");
        
        State<Store> storeState = new StateImpl<Store>();
        
        resuktOpt.get().setAnalyzedMetric(new Metric(new Date(10000), 0f, null));
        notification = func.call(null, ids, resuktOpt, storeState);
        assertFalse(notification.isPresent());
        
        resuktOpt.get().setAnalyzedMetric(new Metric(new Date(21000), 0f, null));
        notification = func.call(null, ids, resuktOpt, storeState);
        assertTrue(notification.isPresent());
        
        resuktOpt.get().setAnalyzedMetric(new Metric(new Date(31000), 0f, null));
        notification = func.call(null, ids, resuktOpt, storeState);
        assertFalse(notification.isPresent());
    }
    
    @Test
    public void raiseChangingStatus() throws Exception{
        Expirable props = PropertiesTest.mockedExpirable();
        props.get().setProperty("monitor.monID.notificator.notID.type", "constant");
        props.get().setProperty("monitor.monID.notificator.notID.statuses", "error");
        props.get().setProperty("monitor.monID.notificator.notID.period", "10s");
        
        UpdateNotificationStatusesF func = new UpdateNotificationStatusesF(props);
        
        Optional<Notification> notification = null;
                
        NotificatorID ids = new NotificatorID("monID", "notID", null);
        Optional<AnalysisResult> resuktOpt = Optional.of(new AnalysisResult());
        
        State<Store> storeState = new StateImpl<Store>();
        
        resuktOpt.get().setStatus(Status.ERROR, "");
        resuktOpt.get().setAnalyzedMetric(new Metric(new Date(10000), 0f, null));
        notification = func.call(null, ids, resuktOpt, storeState);
        assertFalse(notification.isPresent());
        
        resuktOpt.get().setStatus(Status.WARNING, "");
        resuktOpt.get().setAnalyzedMetric(new Metric(new Date(21000), 0f, null));
        notification = func.call(null, ids, resuktOpt, storeState);
        assertFalse(notification.isPresent());
        
        resuktOpt.get().setStatus(Status.ERROR, "");
        resuktOpt.get().setAnalyzedMetric(new Metric(new Date(31000), 0f, null));
        notification = func.call(null, ids, resuktOpt, storeState);
        assertFalse(notification.isPresent());
        
        resuktOpt.get().setStatus(Status.ERROR, "");
        resuktOpt.get().setAnalyzedMetric(new Metric(new Date(35000), 0f, null));
        notification = func.call(null, ids, resuktOpt, storeState);
        assertFalse(notification.isPresent());
        
        resuktOpt.get().setStatus(Status.ERROR, "");
        resuktOpt.get().setAnalyzedMetric(new Metric(new Date(42000), 0f, null));
        notification = func.call(null, ids, resuktOpt, storeState);
        assertTrue(notification.isPresent());
    }
    
}
