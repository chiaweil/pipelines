/*
 *
 *  * ******************************************************************************
 *  *  * Copyright (c) 2015-2019 Skymind, Inc.
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

package ai.skymind.pipelines.verticles.image.tensorflow;

import ai.skymind.pipelines.InferenceConfiguration;
import ai.skymind.pipelines.model.ModelConfigType;
import ai.skymind.pipelines.model.TensorFlowConfig;
import ai.skymind.pipelines.model.loader.tensorflow.TensorflowGraphHolder;
import ai.skymind.pipelines.model.loader.tensorflow.TensorflowModelLoader;
import ai.skymind.pipelines.output.types.ClassifierOutput;
import ai.skymind.pipelines.pipeline.ArrayConcatneationStep;
import ai.skymind.pipelines.pipeline.ImageLoading;
import ai.skymind.pipelines.pipeline.ModelPipelineStep;
import ai.skymind.pipelines.serving.Input;
import ai.skymind.pipelines.serving.Output;
import ai.skymind.pipelines.serving.ParallelInferenceConfig;
import ai.skymind.pipelines.serving.ServingConfig;
import ai.skymind.pipelines.util.ObjectMapperHolder;
import ai.skymind.pipelines.verticles.inference.InferenceVerticle;
import com.jayway.restassured.specification.RequestSpecification;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.apache.commons.io.FileUtils;
import org.deeplearning4j.parallelism.inference.InferenceMode;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nd4j.linalg.io.ClassPathResource;
import org.nd4j.tensorflow.conversion.graphrunner.GraphRunner;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.jayway.restassured.RestAssured.given;
import static org.junit.Assert.assertEquals;

@RunWith(VertxUnitRunner.class)
public class TestMultiImageVerticle extends BaseMultiImageUploadVerticalTest {

    private List<String> inputs;

    @Override
    public Class<? extends AbstractVerticle> getVerticalClazz() {
        return InferenceVerticle.class;
    }

    @After
    public void after(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }

    @Override
    public Handler<HttpServerRequest> getRequest() {

        return req -> {
            //should be json body of classification
            req.bodyHandler(body -> {
                try {
                    ClassifierOutput classifierOutput = ObjectMapperHolder.getJsonMapper().readValue(body.toString(), ClassifierOutput.class);
                    assertEquals(1, classifierOutput.getDecisions()[0]);
                } catch (IOException e) {
                    context.fail(e);
                }
                System.out.println("Finish body" + body);
            });

            req.exceptionHandler(exception -> {
                context.fail(exception);

            });

        };
    }

    @Override
    public JsonObject getConfigObject() throws Exception {
        ParallelInferenceConfig parallelInferenceConfig = ParallelInferenceConfig.defaultConfig();
        parallelInferenceConfig.setInferenceMode(InferenceMode.SEQUENTIAL);
        String path = new ClassPathResource("inference/tensorflow/mnist/lenet_frozen.pb").getFile().getAbsolutePath();


        TensorflowModelLoader tensorflowGraphHolder = TensorflowModelLoader.builder()
                .protoFile(new File(path))
                .inputNames(Arrays.asList("input"))
                .outputNames(Arrays.asList("output"))
                .build();

        TensorflowGraphHolder tensorflowGraphHolder1 = tensorflowGraphHolder.loadModel();

        GraphRunner graphRunner = new GraphRunner(tensorflowGraphHolder1.getInputNames(),
                tensorflowGraphHolder1.getTfGraph(),tensorflowGraphHolder1.getGraphDef());
        inputs = graphRunner.getInputOrder();
        String outputKey = "output";
        Map<String,Long[]> dims = new LinkedHashMap<>();
        dims.put("input",new Long[]{28L,28L,1L});

        ImageLoading imageLoadingConfig = ImageLoading.builder()
                .inputName("input")
                .outputName("output")
                .dimensionsConfigs(dims)
                .build();

        ServingConfig servingConfig = ServingConfig.builder()
                .predictionType(Output.PredictionType.CLASSIFICATION)
                .inputDataType(Input.DataType.IMAGE)
                .parallelInferenceConfig(parallelInferenceConfig)
                .httpPort(port)
                .build();

        ArrayConcatneationStep arrayConcatneationStepConfig = ArrayConcatneationStep.builder()
                .concatDimension(0,0)
                .inputName("input")
                .build();

        ModelConfigType type = ModelConfigType.tensorFlow(path);

        TensorFlowConfig modelConfig = TensorFlowConfig.builder()
                .modelConfigType(type)
                .build();

        ModelPipelineStep modelPipelineStep = ModelPipelineStep.builder()
                .inputName("input")
                .outputName("output")
                .modelConfig(modelConfig)
                .servingConfig(servingConfig)
                .build();


        InferenceConfiguration inferenceConfiguration = InferenceConfiguration.builder()
                .servingConfig(servingConfig)
                .pipelineStep(imageLoadingConfig)
                .pipelineStep(arrayConcatneationStepConfig)
                .pipelineStep(modelPipelineStep)
                .build();

        return new JsonObject(inferenceConfiguration.toJson());
    }


    @Test
    public void testInferenceResult(TestContext context) throws Exception {
        String[] names = {inputs.get(0)};
        int batchSize = 2;
        int buffers = names.length * batchSize;
        String[] partNames = new String[buffers];
        String[] fileNames = new String[buffers];
        Buffer[] data = new Buffer[buffers];
        int buffersIdx = 0;
        RequestSpecification requestSpecification = given();
        requestSpecification.port(port);


        for(int j = 0; j < batchSize; j++) {
            for (int i = 0; i < names.length; i++) {
                partNames[buffersIdx] = String.format(names[i] + "[%d]",j);
                fileNames[buffersIdx] = partNames[i * j] + ".png";
                requestSpecification.multiPart(partNames[i],fileNames[i],getImageBuffer().getBytes());
                buffersIdx++;
            }
        }

        requestSpecification.when()
                .expect().statusCode(200)
                .post("/classification/image");



    }

    public Buffer getImageBuffer() throws Exception {
        File f = new ClassPathResource("data/5.png").getFile();
        byte[] content = FileUtils.readFileToByteArray(f);
        return Buffer.buffer(content);
    }





}
