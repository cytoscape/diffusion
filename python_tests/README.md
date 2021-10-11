# Diffusion Tests

This directory contains scripts to test the
[Cytoscape Core Diffusion App](https://apps.cytoscape.org/apps/diffusion) 
on a running instance of [Cytoscape](https://cytoscape.org).


## ``Diffusion Test.py`` test script

This script runs diffusion a couple times on a running instance
of [Cytoscape](https://cytoscape.org). The script requires Python > 3 
with [requests](https://pypi.org/project/requests) installed and a running
instance of [Cytoscape](https://cytoscape.org). 

In addition, the ``_SESSION_FILE`` parameter in ``Diffusion Test.py`` must be set to
the ``galFiltered.cys`` session file found under the ``sampleData`` directory
of the Cytoscape installation.

To run via terminal:

```Bash

python "Diffusion Test.py"

```

The script prompts the user to perform an action in [Cytoscape](https://cytoscape.org)
and then hit enter.

**Example output:**

```Bash
$ python "Diffusion Test.py" 
On Cytoscape, select a network and a few nodes and hit <enter>
..On Cytoscape, deselect all networks and hit <enter>
.On Cytoscape, select a network and a few nodes and hit <enter>
.
----------------------------------------------------------------------
Ran 4 tests in 21.859s

OK
$ 
```

## ``bench_mark_diffusion.py`` test script

This script takes the [CX](https://home.ndexbio.org/data-model) networks found in a directory
and runs them on diffusion service reporting time it takes to diffuse. 
 
This script requires Python 3.6+, [py4cytoscape](https://pypi.org/project/py4cytoscape),
[tqdm](https://pypi.org/project/tqdm), and 
a running instance of [Cytoscape](https://cytoscape.org)


**To run via terminal:**

```Bash
python bench_mark_diffusion.py
```

**Example output:**

```Bash

CX File,# Nodes,# Edges,# Node Attributes,# Edge Attributes,# Selected Nodes, # Diffused Nodes, http://v3.diffusion.io time in secs, http://someotherservice time in secs
galFiltered.cx,331,362,25,5,2,5.5,7.3
```