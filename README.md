# Pipelines


## Overview

Pipelines is a serving system and framework focused on deploying machine learning 
pipelines to production. The core abstraction is an idea called a "pipeline step".

An individual step is meant to perform a task as part of using a machine learning 
model in a deployment. These steps generally include:

1. Pre- or post-processing steps
2. One or more machine learning models
3. Transforming the output in a way that can be understood by humans, 
such as labels in a classification example.


For instance, if you want to run arbitrary Python code for preprocessing purposes,
you can use a`PythonPipelineStep`. To perform inference on a (mix of) TensorFlow,
Keras, DL4J or PMML models, use `ModelPipelineStep`. Pipelines also contain
functionality for other preprocessing tasks, such as DataVec transform processes, 
or image transforms.


Pipelines operate on the concept of input types and output types.
Core input and outputs types include:

1. Raw binary numpy arrays (the npy format)
2. JSON format
3. Raw images (jpegs etc.)
4. Apache Arrow records


## Usage

### Python SDK:

See the `client` subdirectory for our Python SDK.


### Core workflow:

The core intended workflow is a few simple steps:

1. Configure a server setting up:

      a. `InputType`s of your pipeline
      b. `OutputType`s of your pipelin
      c. A `ServingConfiguration` containing things like
         host and port information
      d. A series of `PipelineStep`s that represent
      what steps a deployed pipeline should perform

2. Configure a client to connect to the server.

## Building/Installation

Dependencies:

1. [Jdk 8](https://adoptopenjdk.net/) is preferred.
2. [mvnw](https://stackoverflow.com/questions/38723833/what-is-the-purpose-of-mvnw-and-mvnw-cmd-files/41239572) will download and setup
maven automatically 

In order to build pipelines, you need to configure:
1. Chip (-Dchip=YOURCHIP)
2. OS   (-Djavacpp.platform=YOUR PLATFORM)
3. Way of packaging (-P<YOUR-PACKAGING>)
4. Modules to include for your pipeline steps(-P<MODULE-TO-INCLUDE>)

-D is a jvm argument and and -P is a maven profile.
Below we specify the requirements for each configuration.

### Chips

Pipelines can run on a wide variety of chips including:

- ARM:       `-Dchip=arm`
- Intel/X86: `-Dchip=cpu`
- CUDA:      `-Dchip=gpu`

### Operating systems

Supported operating systems include:
- Linux: `-Djavacpp.platform=linux`
- Mac: `-Djavacpp.platform=mac`
- Windows: `-Djavacpp.platform=windows`

Untested but should work (please let us know if you would like to try setting this up!):
- Android
- IOS

### Packaging options:

-  Standard Uberjar: `-Puberjar`
-  Debian: `-Pdeb`
-  RPM:  `-Prpm`
-  Docker: `-Pdocker`
-  WAR file: `-Pwar`
-  TAR file: `-Ptar`
    
### Modules to include

- Python support: `-Ppython`
- PMML support: `-Ppmml`

In order to configure pipelines for your platform, you use a maven based build profile.
An example running on cpu:

```bash
./mvnw -Ppython -Ppmml -Dchip=cpu -Djavacpp.platform=windows-x86_64 -Puberjar clean install -Dmaven.test.skip=true
```

This will automatically download and setup a pipelines uber jar file (see the uber jar sub directory)
containing all dependencies needed to run the platform.

The output will be in the target directory of whatever packaging mechanism you specify
(docker, tar, ..)

For example if you build an uber jar, you need to use the -Puberjar
profile, and the output will be found in model-server-uber-jar/target.

## License

Every module in this repo is apache license 2.0, save for `pipelines-pmml` 
which is agpl to comply with the JPML license.
