from Diffusion import Diffusion
from CyRESTInstance import CyRESTInstance
from TestConfig import BASE_URL, IMPOSSIBLE_URL
from Core import Core
from CyFailedCIError import CyFailedCIError
from requests.status_codes import codes

""" Built from http://pyunit.sourceforge.net/pyunit.html """


import unittest

_diffusion = Diffusion(CyRESTInstance(base_url=BASE_URL))  # assumes Cytoscape answers at base_url
_bad_diffusion = Diffusion(CyRESTInstance(base_url=IMPOSSIBLE_URL))  # # for verifying proper exception thrown

_core = Core(CyRESTInstance(base_url=BASE_URL))  # assumes Cytoscape answers at base_url


class DiffusionTestCase(unittest.TestCase):
    _SESSION_FILE = "/git/cytoscape/cytoscape/gui-distribution/assembly/target/cytoscape/sampleData/galFiltered.cys"

    def setUp(self):
        result = _core.read_session_file(self._SESSION_FILE)
        loaded_file = result["file"]
        assert loaded_file == self._SESSION_FILE, "SetUp loaded incorrect session file: " + loaded_file
        result = _core.get_network_suids()
        assert len(result) == 1, "SetUp found network list of wrong length: " + len(result)
        self._collection_suid = result[0]
        pass

    def tearDown(self):
        pass

    def test_diffusion_no_network(self):
        input("On Cytoscape, deselect all networks and hit <enter>")
        try:
            _diffusion.diffuse()
        except CyFailedCIError as e:
            error = e.args[0]
            assert error["type"] == "urn:cytoscape:ci:diffusion-app:v1:diffuse_current_view:1" \
                   and error["status"] == codes.NOT_FOUND \
                   and error["message"] is not None \
                   and error["link"] is not None, "test_diffusion_no_network returned invalid CyFaileError: " + str(e)
        else:
            assert False, "test_diffusion_no_network did not get the expected CyFailedError exception"

    def test_diffusion(self):
        input("On Cytoscape, select a network and a few nodes and hit <enter>")
        _diffusion.diffuse()

    def test_diffusion_with_options(self):
        input("On Cytoscape, select a network and a few nodes and hit <enter>")
        DIFFUSION_INPUT_COLUMN = "diffusion_input"
        result = _core.create_table_column(self._collection_suid, DIFFUSION_INPUT_COLUMN, data_type=Core.DATA_DOUBLE)
        result = _core.fill_table_column(self._collection_suid, DIFFUSION_INPUT_COLUMN, 0.0)
        result = _diffusion.diffuse_with_options(heat_column_name=DIFFUSION_INPUT_COLUMN)
        pass

    def test_diffusion_exception(self):
        try:
            _bad_diffusion.diffuse()
        except CyFailedCIError:
            assert False, "test_diffusion_exception got unexpected CyFailedError"
        except BaseException:
            pass
        else:
            assert False, "test_diffusion_exception expected exception"


def suite():
    diffusion_suite = unittest.makeSuite(DiffusionTestCase, "test")
    return unittest.TestSuite((diffusion_suite))


if __name__ == "__main__":
    unittest.TextTestRunner().run(suite())
