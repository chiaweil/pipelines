from jnius_config import set_classpath
try:
    set_classpath('pipelines.jar')
except:
    print("VM already running from previous test")


from pipes.inference import *
import json
from pipes.json_utils import json_with_type
from jnius import autoclass


InferenceConfigurationJava = autoclass('ai.skymind.pipelines.InferenceConfiguration')


def test_json_compare():
    parallel_inference_config = ParallelInferenceConfig(workers=1)
    serving_config = ServingConfig(http_port=1300,
                                   input_data_type='NUMPY',
                                   output_data_type='NUMPY',
                                   parallel_inference_config=parallel_inference_config)

    tensorflow_config = TensorFlowConfig(model_config_type=ModelConfigType(model_type='TENSORFLOW',
                                                                           model_loading_path='model.pb'))

    model_pipeline_step = ModelPipelineStep(model_config=tensorflow_config,
                                            serving_config=serving_config,
                                            input_names=["IteratorGetNext:0", "IteratorGetNext:1", "IteratorGetNext:4"],
                                            output_names=["loss/Softmax"])

    inference = InferenceConfiguration(serving_config=serving_config,
                                       pipeline_steps=[model_pipeline_step])

    assert_config_works(inference)



def test_python_serde():
    input_names = ['default']
    output_names = ['default']

    python_config = PythonConfig(
        python_code='first += 2'
        , python_inputs={'first': 'NDARRAY'},
        python_outputs={'first': 'NDARRAY'}
    )

    port = 1300
    parallel_inference_config = ParallelInferenceConfig(workers=1)
    serving_config = ServingConfig(http_port=port,
                                   input_data_type='NUMPY',
                                   output_data_type='NUMPY',
                                   log_timings=True,
                                   parallel_inference_config=parallel_inference_config)

    python_pipeline_step = PythonPipelineStep(input_names=input_names,
                                              output_names=output_names,
                                              python_configs=DictWrapper({'default': python_config}))

    inference = InferenceConfiguration(serving_config=serving_config,
                                       pipeline_steps=[python_pipeline_step])

    assert_config_works(inference)


def assert_config_works(config):
    config_json = json_with_type(config)
    config = InferenceConfigurationJava.fromJson(json.dumps(config_json))
    test_json = config.toJson()
    test_json_dict = json.loads(test_json)
    config_json_dict = config_json
    test_pipeline_steps = test_json_dict['pipelineSteps']
    config_pipeline_steps = config_json_dict['pipelineSteps']
    assert len(test_pipeline_steps) == len(config_pipeline_steps)
