import json

from CyCaller import CyCaller


class Diffusion:
    """ Cover functions for Diffusion functions """

    def __init__(self, cy_rest_instance=None):
        """ Constructor remembers CyREST location """
        self._cy_caller = CyCaller(cy_rest_instance)

    def diffuse(self):
        """ Execute a simple diffusion on current network """
        return self._cy_caller.execute_post("/diffusion/v1/currentView/diffuse")

    def diffuse_with_options(self, heat_column_name="diffusion_input", diffusion_time=0.1):
        """ Execute a diffusion with options on current network """
        return self._cy_caller.execute_post("/diffusion/v1/currentView/diffuse_with_options",
                                            json.dumps({"heatColumnName": heat_column_name, "time": diffusion_time}))
