# `pipes` - Python client for Skymind Pipelines

## Installation

To install this Python package, run `python setup.py install`. To run any examples with
`pipes` you need to build a SKIL Pipelines JAR first:

```bash
cd ..
python build_jar.py -- os <your-platform>
```

where `<your-platform>` is picked from `windows-x86_64`,`linux-x86_64`,`linux-x86_64-gpu`,
`macosx-x86_64`, `linux-armhf` and `windows-x86_64-gpu`, depending on your operating system
and architecture. 

## Running tests

The resulting JAR will be generated at the base of the `pipelines` project. 
Copy it to the `client/tests` folder. To validate that this process worked 
you can now run:

```bash
cd client/tests
python -m pytest .
```

## Developing `pipes` from source

`pipes` is largely generated from Java source files. If you change the interface of
_Pipelines_, you should regenerate `pipes` as well:

```bash
cd ..
sh build_client.sh
```

This script uses the `jsonschema2popo` tool underneath, which transforms JSON to Python objects.
That means you first need to install this tool with (note that this requires you to use python 3.4+, 
which you could install in a virtual environment):

```bash
pip install jsonschema2popo
```

and then you have to put `jsonschema2popo` on your `PATH`, so that you can use it from anywhere. To test
the installation, simply type `jsonschema2popo` in your console. If the command is recognized
by your shell, you should be able to build `pipes` from source.