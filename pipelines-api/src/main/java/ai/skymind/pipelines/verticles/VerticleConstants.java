/*
 *
 *  * ******************************************************************************
 *  *  * Copyright (c) 2015-2018 Skymind, Inc.
 *  *  *
 *  *  * This program and the accompanying materials are made available under the
 *  *  * terms of the Apache License, Version 2.0 which is available at
 *  *  * https://www.apache.org/licenses/LICENSE-2.0.
 *  *  *
 *  *  * Unless required by applicable law or agreed to in writing, software
 *  *  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  *  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *  *  * License for the specific language governing permissions and limitations
 *  *  * under the License.
 *  *  *
 *  *  * SPDX-License-Identifier: Apache-2.0
 *  *  *****************************************************************************
 *
 *
 */

package ai.skymind.pipelines.verticles;

/**
 * Constants for the config.json
 * for initializing verticles.
 *
 * @author Adam Gibson
 */
public class VerticleConstants {

    // General
    public final static String MODEL_LOADING_CONFIG_KEY = "modelLoadingConfig";
    public final static String INFERENCE_LOADING_CONFIG_KEY = "parallelInferenceConfig";
    public final static String PUB_SUB_CONFIG_KEY = "pubSubConfig";
    public final static String HANDLER_TYPE_KEY = "handlerType";
    public final static String BATCH_ID_HEADER = "batchId";
    public final static String INPUT_DATA_TYPE = "inputDataType";
    public final static String DIMENSIONS_KEY = "dimensionsConfig";
    public final static String PREDICTION_TYPE_KEY = "predictionType";
    public final static String NORMALIZATION_LOADING_CONFIG_KEY = "normalizationLoadingConfig";
    public final static String INPUT_DATA_TYPES = "inputDataTypes";
    public final static String OUTPUT_DATA_TYPES = "outputDataTypes";
    public final static String OUTPUT_DATA_TYPE = "outputDataType";
    public final static String CONVERTED_INFERENCE_DATA = "convertedInferenceData";
    public final static String HTTP_PORT_KEY = "httpPort";
    public final static String TRANSACTION_ID = "transactionId";

    // Object detection
    public final static String OBJECT_DETECTION_LABELS_PATH = "labelsPath";
    public final static String OBJECT_DETECTION_THRESHOLD = "threshold";
    public final static String OBJECT_DETECTION_NUM_LABELS = "numLabels";
    //keys for the routing context when doing object recognition
    public final static String ORIGINAL_IMAGE_HEIGHT = "originalImageHeight";
    public final static String ORIGINAL_IMAGE_WIDTH = "originalImageWidth";


    // PMML
    public final static String MODEL_EVALUATOR_FACTORY_CLASS_KEY = "evaluatorFactoryName";
    public final static String PMML_PATH_KEY = "pmmlPath";

    // Schemas
    public final static String OUTPUT_SCHEMA_KEY = "outputSchema";
    public final static String INPUT_SCHEMA_KEY = "inputSchema";

    // TransformProcesses
    public final static String TRANSFORM_PROCESS_KEY = "transformProcess";
    public final static String IMAGE_TRANSFORM_PROCESS_KEY = "imageTransformProcess";


    // Preprocessing
    public final static String PRE_PROCESSING_INPUT_NAMES = "preProcessingInputNames";
    public final static String PRE_PROCESSING_OUTPUT_NAMES = "preProcessingOutputNames";
    public final static String PRE_PROCESSING_OPERATION = "preProcessingOperation";


    // Postprocessing
    public final static String POST_PROCESSING_INPUT_NAMES = "postProcessingInputNames";
    public final static String POST_PROCESSING_OUTPUT_NAMES = "postProcessingOutputNames";
    public final static String OP_POST_PROCESSING_OPERATION = "postProcessingOperation";


    // Mem map
    public final static String MEM_MAP_VECTOR_PATH = "memMapVectorPath";

    // Transform server constants
    public final static String PYTHON_CODE = "pythonCode";
    public final static String PYTHON_INPUTS = "pythonInputs";
    public final static String PYTHON_OUTPUTS = "pythonOutputs";
    public final static String PYTHON_EXTRA_INPUTS_KEY = "extra_inputs";

}
