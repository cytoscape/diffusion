
import os
import requests

CYREST_URL = 'http://localhost:1234/v1'
"""
Default Cytoscape REST API endpoint
"""

DIFFUSION_URL = 'http://v3.heat-diffusion.cytoscape.io/'

JSON_HEADERS = {'Content-Type': 'application/json',
                'Accept': 'application/json'}
"""
Default Headers to pass to all CyREST implemented here
"""


def delete_diffusion_url_property(base_url=CYREST_URL):
    """
    Deletes ``diffusion.url`` property from Cytoscape

    This is here cause py4cytoscape does not have methods to do this yet

    :param base_url: Endpoint of Cytoscape cyREST service
    :type base_url: str
    :return: ``None`` upon success or ``dict`` containing error
    :rtype: dict
    """
    resp = requests.delete(base_url + '/properties/cytoscape3.props/diffusion.url',
                           headers=JSON_HEADERS)
    if resp.status_code == 200:
        return None
    return resp.json()


def get_diffusion_url_property(base_url=CYREST_URL):
    """
    Gets ``diffusion.url`` property from Cytoscape

    This is here cause py4cytoscape does not have methods to do this yet

    :param base_url: Endpoint of Cytoscape cyREST service
    :type base_url: str
    :return: Value of property or ``None`` if not found or there is an error
    :rtype: str
    """
    resp = requests.get(base_url + '/properties/cytoscape3.props/diffusion.url',
                        headers=JSON_HEADERS)
    if resp.status_code != 200:
        return None
    return resp.json()['data']['value']


def set_diffusion_url_property(new_value, base_url=CYREST_URL):
    """
    Sets ``diffusion.url`` property in Cytoscape to **new_value**

    This is here cause py4cytoscape does not have methods to do this yet

    :param new_value: Value to set ``diffusion.url`` property
    :type new_value: str
    :param base_url: Endpoint of Cytoscape cyREST service
    :type base_url: str
    :return: ``None`` upon success or output from endpoint upon error
    :rtype: str
    """
    orig_prop = get_diffusion_url_property(base_url=base_url)
    if orig_prop is None:
        resp = requests.post(base_url + '/properties/cytoscape3.props',
                            headers=JSON_HEADERS,
                            json={'key': 'diffusion.url',
                                  'value': new_value})
    else:
        resp = requests.put(base_url + '/properties/cytoscape3.props/diffusion.url',
                            headers=JSON_HEADERS,
                            json={'value': new_value})
    if resp.status_code == 200:
        return None

    return resp.text