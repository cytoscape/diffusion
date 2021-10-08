# Diffusion Tests

This directory contains scripts to test the
Cytoscape Diffusion App on a running instance
of [Cytoscape](https://cytoscape.org).

### Testing scripts 

#### ``Diffusion Test.py``

This script runs diffusion a couple times on a running instance
of [Cytoscape](https://cytoscape.org). The script requires Python > 3 
with [requests](https://pypi/projects/requests) installed and a running
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
