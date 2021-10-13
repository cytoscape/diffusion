
import os
import diffusiontestutil
import py4cytoscape as py4

import unittest


class DiffusionTestCase(unittest.TestCase):
    _SESSION_FILE = os.path.abspath(os.path.join(os.path.dirname(__file__),
                                                 'testsessions',
                                                 'galFiltered.cys'))

    @classmethod
    def setUpClass(cls):
        diffusiontestutil.CYREST_URL = os.getenv('CYREST_URL',
                                                 diffusiontestutil.CYREST_URL)
        cls._orig_diffusion_prop = diffusiontestutil.get_diffusion_url_property()

    @classmethod
    def tearDownClass(cls):
        if cls._orig_diffusion_prop is None:
            if diffusiontestutil.get_diffusion_url_property() is not None:
                res = diffusiontestutil.delete_diffusion_url_property()
                if res is not None:
                    raise Exception('Unable to delete diffusion.url: ' + str(res))
        else:
            diffusiontestutil.set_diffusion_url_property(cls._orig_diffusion_prop)

    def setUp(self):
        alt_diff_url = os.getenv('DIFFUSION_URL')
        if alt_diff_url is not None:
            diffusiontestutil.set_diffusion_url_property(alt_diff_url)
        result = py4.open_session(file_location=self._SESSION_FILE,
                                  base_url=diffusiontestutil.CYREST_URL)
        self.assertEqual({}, result)
        self._network_suid = py4.get_network_suid(base_url=diffusiontestutil.CYREST_URL)

    def tearDown(self):
        pass

    def test_py4_diffusion_no_network(self):
        py4.close_session(False, base_url=diffusiontestutil.CYREST_URL)
        try:
            py4.diffusion_basic(base_url=diffusiontestutil.CYREST_URL)
            self.fail("Expected exception")
        except Exception as e:
            pass

    def test_py4_diffusion_on_a_few_nodes(self):

        py4.select_nodes(['RAP1', 'MCM1'])
        res = py4.diffusion_basic(base_url=diffusiontestutil.CYREST_URL)
        self.assertEqual({'heatColumn': 'diffusion_output_heat',
                          'rankColumn': 'diffusion_output_rank'}, res)
        selected_nodes = py4.get_selected_nodes(network=self._network_suid,
                                                base_url=diffusiontestutil.CYREST_URL)
        self.assertEqual(33, len(selected_nodes))
        # cant really test if nodes coming back are the same cause
        # the old service incorrectly sorted rank not taking into account
        # identical heats

    def test_py4_diffusion_on_a_few_nodes_with_time_set(self):
        py4.select_nodes(['RAP1', 'MCM1'])
        res = py4.diffusion_advanced(time=1.0,
                                     base_url=diffusiontestutil.CYREST_URL)
        self.assertEqual({'heatColumn': 'diffusion_output_heat',
                          'rankColumn': 'diffusion_output_rank'}, res)
        selected_nodes = py4.get_selected_nodes(network=self._network_suid,
                                                base_url=diffusiontestutil.CYREST_URL)
        self.assertEqual(33, len(selected_nodes))
        # cant really test if nodes coming back are the same cause
        # the old service incorrectly sorted rank not taking into account
        # identical heats

    def test_py4_diffusion_on_a_few_nodes_with_time_and_gal1rgsig_as_input(self):
        py4.clear_selection(network=self._network_suid,
                            base_url=diffusiontestutil.CYREST_URL)
        res = py4.diffusion_advanced(time=1.0,
                                     heat_column_name='gal1RGsig',
                                     base_url=diffusiontestutil.CYREST_URL)
        self.assertEqual({'heatColumn': 'diffusion_output_heat',
                          'rankColumn': 'diffusion_output_rank'}, res)
        selected_nodes = py4.get_selected_nodes(network=self._network_suid,
                                                base_url=diffusiontestutil.CYREST_URL)
        self.assertEqual(33, len(selected_nodes))
        expected_nodes = ['YLR197W', 'YPR041W', 'YDR299W', 'YER116C',
                          'YIL070C', 'YJL194W', 'YLR175W', 'YOR204W',
                          'YGL097W', 'YGR218W', 'YMR146C', 'YOR039W',
                          'YGR048W', 'YDR100W', 'YGL161C', 'YDR335W',
                          'YKL074C', 'YJR066W', 'YKL204W', 'YNL154C',
                          'YLR310C', 'YNL098C', 'YBR160W', 'YKL101W',
                          'YML054C', 'YOR303W', 'YDR184C', 'YLR116W',
                          'YKL012W', 'YOR310C', 'YDL014W', 'YOR361C',
                          'YMR309C']
        for node in expected_nodes:
            self.assertTrue(node in selected_nodes,
                            node + ' not in selected nodes')

    @unittest.skip("Need to implement")
    def test_diffusion_specify_networkviewsuid(self):
        pass

    @unittest.skip("Need to implement")
    def test_diffusion_with_options_specify_networkviewsuid(self):
        pass


def suite():
    diffusion_suite = unittest.makeSuite(DiffusionTestCase, "test")
    return unittest.TestSuite(diffusion_suite)


if __name__ == "__main__":
    unittest.TextTestRunner(verbosity=2).run(suite())
