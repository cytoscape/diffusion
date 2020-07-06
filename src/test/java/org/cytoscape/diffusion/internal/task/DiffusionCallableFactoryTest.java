package org.cytoscape.diffusion.internal.task;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author churas
 */
public class DiffusionCallableFactoryTest {
    
    @Test
    public void testGetDiffusionCallable(){
        DiffusionCallableFactory fac = new DiffusionCallableFactory();
        DiffusionCallable dc = fac.getDiffusionCallable("cx", null, 0.0, null);
        assertNotNull(dc);
    }
    
}
