#!/usr/bin/env python

import os
import sys
import traceback
import argparse
import logging
import random
import time
import json
import pandas
import statistics
import diffusiontestutil
import py4cytoscape as py4
from tqdm import tqdm


logger = logging.getLogger(__name__)


class Formatter(argparse.ArgumentDefaultsHelpFormatter,
                argparse.RawDescriptionHelpFormatter):
    pass


LOG_FORMAT = "%(asctime)-15s %(levelname)s %(relativeCreated)dms " \
             "%(filename)s::%(funcName)s():%(lineno)d %(message)s"


DATA_DIR = os.path.join(os.path.dirname(__file__), 'testnetworks')


def _parse_arguments(desc, args):
    """
    Parses command line arguments
    :param desc:
    :param args:
    :return:
    """

    parser = argparse.ArgumentParser(description=desc,
                                     formatter_class=Formatter)
    parser.add_argument('--endpoints',
                        default='http://v3.heat-diffusion.cytoscape.io',
                        help='Comma delimited list of REST endpoints')
    parser.add_argument('--cxdir', default=DATA_DIR,
                        help='Directory containing CX files to use for comparison')
    parser.add_argument('--cyresturl', default=diffusiontestutil.CYREST_URL,
                        help='CyREST endpoint')
    parser.add_argument('--number_iterations', default=5, type=int,
                        help='# of diffusions to run on each network')
    parser.add_argument('--nodes_to_select', default='1%', type=str,
                        help='Number or percent of nodes to select. '
                             'If a number, then that number of nodes will '
                             'be selected in network. If suffixed with %% then '
                             'that percentage of nodes will be selected')
    parser.add_argument('--jsonreport',
                        help='If set, dumps results of '
                             'benchmark in JSON format')
    parser.add_argument('--csvreport',
                        help='If set, dumps results of '
                             'benchmark in CSV format')
    parser.add_argument('--verbose', '-v', action='count', default=0,
                        help='Increases verbosity of logger to standard '
                             'error for log messages in this module and '
                             '. Messages are '
                             'output at these python logging levels '
                             '-v = ERROR, -vv = WARNING, -vvv = INFO, '
                             '-vvvv = DEBUG, -vvvvv = NOTSET')
    parser.add_argument('--logconf', default=None,
                        help='Path to python logging configuration file in '
                             'format consumable by fileConfig. See '
                             'https://docs.python.org/3/library/logging.html '
                             'for more information. '
                             'Setting this overrides -v|--verbose parameter '
                             'which uses default logger.')
    return parser.parse_args(args)


def _setup_logging(args):
    """
    Sets up logging based on parsed command line arguments.
    If args.logconf is set use that configuration otherwise look
    at args.verbose and set logging for this module and the one
    in ndexutil specified by TSV2NICECXMODULE constant
    :param args: parsed command line arguments from argparse
    :raises AttributeError: If args is None or args.logconf is None
    :return: None
    """
    if args.logconf is None:
        level = (50 - (10 * args.verbose))
        handler = logging.StreamHandler()
        handler.setFormatter(logging.Formatter(LOG_FORMAT))
        handler.setLevel(level)
        logger.setLevel(level)
        logger.addHandler(handler)
        return
    else:
        # logconf was set use that file
        logging.config.fileConfig(args.logconf,
                                  disable_existing_loggers=False)


def _get_cx_files(cxdir):
    """

    :param cxdir:
    :return:
    """
    cxfiles = []
    if not os.path.isdir(cxdir):
        logger.debug(str(cxdir) + ' is not a directory')
        return []

    for file_name in os.listdir(cxdir):
        if not file_name.endswith('.cx'):
            continue
        fp = os.path.join(cxdir, file_name)
        if not os.path.isfile(fp):
            continue
        cxfiles.append(fp)
    return cxfiles


def revert_cytoscape_to_original_diffusion_url(theargs, orig_diffusion_prop=None):
    """
    Reverts Cytoscape back to original diffusion service either by setting
    **diffusion.url** property back to its original value or by removing
    it entirely

    :param theargs: Parameters from command line
    :type theargs: :py:class:`argparse.Namespace`
    :param orig_diffusion_prop: Original value of **diffusion.url**,
                                ``None`` if not set
    :raises Exception: if there was erorr deleting diffusion.url property
    :return: None
    """
    if orig_diffusion_prop is None:
        res = diffusiontestutil.delete_diffusion_url_property(base_url=theargs.cyresturl)
        if res is not None:
            raise Exception('Unable to delete diffusion.url: ' + str(res))
    else:
        logger.info('Setting diffusion.url back to: ' + str(orig_diffusion_prop))
        diffusiontestutil.set_diffusion_url_property(orig_diffusion_prop, base_url=theargs.cyresturl)


def load_network_in_cytoscape(cxfile=None, base_url=diffusiontestutil.CYREST_URL):
    """

    :param cxfile:
    :param base_url:
    :return:
    """
    logger.debug('Importing network: ' + cxfile)
    res = py4.import_network_from_file(cxfile,
                                       base_url=base_url)
    net_suid = None
    if 'networks' in res:
        net_suid = res['networks']
        logger.debug('Network loaded with SUID: ' + str(net_suid))
    else:
        logger.warning('Network NOT loaded')
    return net_suid


def select_nodes_for_diffusion(net_suid=None,
                               nodes_to_select='1',
                               base_url=diffusiontestutil.CYREST_URL):
    """

    :param net_suid:
    :param nodes_to_select:
    :param base_url:
    :return:
    """
    if nodes_to_select.endswith('%'):
        node_count = py4.get_node_count(network=net_suid)
        num_nodes_to_select = round(float(nodes_to_select[:-1])/100.0*float(node_count))
        logger.debug(nodes_to_select + ' of ' + str(node_count) +
                     ' nodes in network is: ' + str(num_nodes_to_select))
    else:
        num_nodes_to_select = int(nodes_to_select)
    all_nodes = py4.select_all_nodes(network=net_suid, base_url=base_url)
    logger.debug('Selecting ' + str(num_nodes_to_select) + ' node(s)')
    py4.select_nodes(random.sample(all_nodes, num_nodes_to_select),
                     network=net_suid,
                     preserve_current_selection=False,
                     base_url=base_url)
    logger.debug('Selected node count: ' +
                 str(py4.get_selected_node_count(network=net_suid,
                                                 base_url=base_url)))
    return py4.get_selected_nodes(network=net_suid, base_url=base_url)


def run_diffusion(net_suid=None, time_val=None,
                  base_url=diffusiontestutil.CYREST_URL):
    """

    :param net_suid:
    :param base_url:
    :return:
    """
    logger.info('Running diffusion')
    start_time = int(time.time())
    res = py4.diffusion_advanced(time=time_val, base_url=base_url)
    run_sum = {'duration': int(time.time()) - start_time}
    logger.debug(res)

    run_sum['selected_cnt'] = int(py4.get_selected_node_count(network=net_suid,
                                                              base_url=base_url))
    run_sum['selected'] = py4.get_selected_nodes(network=net_suid, base_url=base_url)

    py4.delete_table_column(res['heatColumn'], network=net_suid, base_url=base_url)
    py4.delete_table_column(res['rankColumn'], network=net_suid, base_url=base_url)
    return run_sum


def diffusion_loop(net_suid=None, base_url=diffusiontestutil.CYREST_URL,
                   number_iterations=5, nodes_to_select=1,
                   endpoints=None):
    """

    :param net_suid:
    :param base_url:
    :return:
    """
    if endpoints is None or isinstance(endpoints, list) is False:
        raise Exception('one or more diffusion REST endpoints must be '
                        'specified in a list')

    run_summary = []
    for x in tqdm(range(number_iterations), desc='Iteration'):
        cur_run = {'iteration': x}
        selected_nodes = select_nodes_for_diffusion(net_suid=net_suid, nodes_to_select=nodes_to_select,
                                                    base_url=base_url)
        cur_run['selected_nodes'] = selected_nodes
        cur_run['endpoints'] = {}
        for service_endpoint in tqdm(endpoints, desc='Diffusion'):
            diffusiontestutil.set_diffusion_url_property(service_endpoint, base_url=base_url)
            run_sum = run_diffusion(net_suid=net_suid,
                                    base_url=base_url)
            cur_run['endpoints'][service_endpoint] = run_sum
        run_summary.append(cur_run)

    return run_summary


def annotate_run_summary(net_suid=None, run_summary=None,
                         base_url=diffusiontestutil.CYREST_URL):
    """

    :param net_suid:
    :param run_summary:
    :return:
    """
    if run_summary is None:
        raise Exception('run_summary should be a dict')

    run_summary['num_nodes'] = py4.get_node_count(network=net_suid, base_url=base_url)
    run_summary['num_edges'] = py4.get_edge_count(network=net_suid, base_url=base_url)
    run_summary['node_attribute_names'] = py4.get_table_column_names(table='node',
                                                                     network=net_suid,
                                                                     base_url=base_url)
    run_summary['edge_attribute_names'] = py4.get_table_column_names(table='edge',
                                                                     network=net_suid,
                                                                     base_url=base_url)


def get_average_run_per_iteration_by_service(iter_sum=None):
    """

    :param iter_sum:
    :return:
    """
    endpoint_set = set()
    endpoint_set.update(iter_sum[0]['endpoints'].keys())

    avg_run = {}
    for cur_rest in endpoint_set:
        avg_run[cur_rest] = {'durations': [],
                             'selected_cnt': set()}

    for cur_iter in iter_sum:
        for cur_rest in endpoint_set:
            avg_run[cur_rest]['durations'].append(cur_iter['endpoints'][cur_rest]['duration'])
            avg_run[cur_rest]['selected_cnt'].add(cur_iter['endpoints'][cur_rest]['selected_cnt'])

    for cur_rest in endpoint_set:
        avg_run[cur_rest]['selected_cnt'] = list(avg_run[cur_rest]['selected_cnt'])

    return avg_run


def get_report_as_pandas(benchmark_run=None):
    """

    :param benchmark_run:
    :return:
    """
    pandas_dict = {'networks': [],
                   '# nodes': [],
                   '# edges': [],
                   '# node attrs': [],
                   '# edge attrs': []}

    endpoint_set = set()
    endpoint_set.update(benchmark_run[0]['run_summary'].keys())

    avg_time_suffix = ' avg time (secs)'

    for e in endpoint_set:
        pandas_dict[e + avg_time_suffix]  = []

    for network_run in benchmark_run:
        pandas_dict['networks'].append(network_run['network'])
        pandas_dict['# nodes'].append(network_run['num_nodes'])
        pandas_dict['# edges'].append(network_run['num_edges'])
        pandas_dict['# node attrs'].append(len(network_run['node_attribute_names']))
        pandas_dict['# edge attrs'].append(len(network_run['edge_attribute_names']))
        for e in endpoint_set:
            pandas_dict[e + avg_time_suffix].append(statistics.mean(network_run['run_summary'][e]['durations']))

    return pandas.DataFrame.from_dict(pandas_dict)


def run_diffusion_benchmarks(theargs):
    """

    :param theargs:
    :return:
    """
    logger.debug('Running benchmark')
    cxfiles = _get_cx_files(os.path.abspath(theargs.cxdir))

    if cxfiles is None or len(cxfiles) == 0:
        raise Exception('No CX files found')

    orig_diffusion_prop = diffusiontestutil.get_diffusion_url_property(base_url=theargs.cyresturl)
    logger.info('Original diffusion.url property value: ' +
                str(orig_diffusion_prop))

    benchmark_run = []
    try:
        for cxfile in tqdm(cxfiles, desc='Network'):
            net_suid = None
            try:
                net_suid = load_network_in_cytoscape(cxfile=cxfile,
                                                     base_url=theargs.cyresturl)
                iter_sum = diffusion_loop(net_suid=net_suid, base_url=theargs.cyresturl,
                                          nodes_to_select=theargs.nodes_to_select,
                                          endpoints=theargs.endpoints.split(','),
                                          number_iterations=theargs.number_iterations)
                run_summary = {'network': os.path.basename(cxfile),
                               'runs': iter_sum,
                               'run_summary': get_average_run_per_iteration_by_service(iter_sum)}
                annotate_run_summary(net_suid=net_suid, run_summary=run_summary)

                benchmark_run.append(run_summary)
            finally:
                if net_suid is not None:
                    logger.debug('Deleting network: ' + str(net_suid))
                    py4.delete_network(net_suid, base_url=theargs.cyresturl)
        df = get_report_as_pandas(benchmark_run=benchmark_run)
        print('\n\n\n')
        print(df.to_csv(index=False))
        if theargs.csvreport is not None:
            df.to_csv(theargs.csvreport, index=False)
    finally:
        revert_cytoscape_to_original_diffusion_url(theargs,
                                                   orig_diffusion_prop=orig_diffusion_prop)

    if theargs.jsonreport is not None:
        with open(theargs.jsonreport, 'w') as f:
            json.dump(benchmark_run, f)


def main(args):
    """
    Main entry point for program
    :param args: command line arguments usually :py:const:`sys.argv`
    :return: 0 for success otherwise failure
    :rtype: int
    """
    desc = """
    Runs Diffusion on command line, sending output to standard
    out in new 
    """
    theargs = _parse_arguments(desc, args[1:])

    try:
        _setup_logging(theargs)
        return run_diffusion_benchmarks(theargs)
    except Exception as e:
        sys.stderr.write('\n\nCaught exception: ' + str(e))
        traceback.print_exc()
        return 2


if __name__ == '__main__':  # pragma: no cover
    sys.exit(main(sys.argv))