package org.cytoscape.diffusion.internal.task;

/**
 * Creates {@link org.cytoscape.diffusion.internal.task.DiffusionCallable} objects
 * @author churas
 */
public class DiffusionCallableFactory {
    
    /**
     * Factory to get  {@link org.cytoscape.diffusion.internal.task.DiffusionCallable}
     * objects. 
     * @param cx network in CX format
     * @param inputHeatCol input heat column parameter
     * @param time time parameter for diffusion service
     * @param diffusionURL URL for diffusion service
     * @return {@link org.cytoscape.diffusion.internal.task.DiffusionCallable} that can run diffusion
     */
    public DiffusionCallable getDiffusionCallable(final String cx,
            final String inputHeatCol, final Double time,
            final String diffusionURL){
        return new DiffusionCallable(cx, inputHeatCol, time, diffusionURL);
    }
}
