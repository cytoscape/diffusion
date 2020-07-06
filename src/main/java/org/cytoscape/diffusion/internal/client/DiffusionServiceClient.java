package org.cytoscape.diffusion.internal.client;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.cytoscape.diffusion.internal.task.DiffuseSelectedTask;
import org.cytoscape.diffusion.internal.task.DiffusionCallableFactory;
import org.cytoscape.property.CyProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client that sends diffusion requests to diffusion service
 */
public class DiffusionServiceClient {

        private static final String BASE_URL = "http://v3.heat-diffusion.cytoscape.io/";

	private final CyProperty<Properties> props;
	private final static Logger logger = LoggerFactory.getLogger(DiffusionServiceClient.class);
	private DiffusionCallableFactory _dcf;
        
        /**
	 * Time to sleep in milliseconds while waiting for index
	 * building and search query threads to complete
	 */
	private static final long SLEEP_TIME = 1000;

	public DiffusionServiceClient(CyProperty<Properties> props) {
		this.props = props;
                _dcf = new DiffusionCallableFactory();
	}
        
        /**
         * For testing, this method lets one set an alternate Factory for 
         * {@link org.cytoscape.diffusion.internal.task.DiffusionCallable} 
         * objects which actually submit the diffusion task
         * to the service
         * @param dcf 
         */
        protected void setAlternateDiffusionCallableFactory(DiffusionCallableFactory dcf){
            _dcf = dcf;
        }
        
        /**
         * Submit diffusion task to service
         * @param cx
         * @param inputHeatCol
         * @param time
         * @param task The invoking task. If not {@code null} this method checks if parent canceled the
         *             task and if yes, the {@link org.cytoscape.diffusion.internal.task.DiffusionCallable} 
         *             is killed and the method returns.
         * @return String with response from call to service or {@code null} if service was canceled
         * @throws IOException If there was an error of some type with request
         */
        public String diffuse(final String cx, final String inputHeatCol, final Double time, DiffuseSelectedTask task) throws IOException {
            // put long running tasks in another thread so we can continue to monitor if 
		// user called cancel
                ExecutorService executor = Executors.newSingleThreadExecutor();
                try {
                    String theURL = props.getProperties().getProperty("diffusion.url", BASE_URL);
                    Future<String> futureTask = executor.submit(_dcf.getDiffusionCallable(cx, inputHeatCol, time, theURL));
                    try {
			while(futureTask.isDone() == false) {
				if (task != null && task.isCanceled()) {
					futureTask.cancel(true);
					return null;
				}
				try {
					Thread.sleep(SLEEP_TIME);
				} catch(InterruptedException ie) {
					// do nothing
				}
			}
			return futureTask.get();
		} catch(InterruptedException ie) {
			throw new IOException("Diffusion interrupted");
		} catch(ExecutionException ee) {
			throw new IOException("Error running diffusion: " + ee.getMessage());
		} 
                } finally {
                    executor.shutdownNow();
                }
        }
        
}
