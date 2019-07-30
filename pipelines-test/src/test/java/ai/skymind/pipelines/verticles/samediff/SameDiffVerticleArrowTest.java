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

package ai.skymind.pipelines.verticles.samediff;

import ai.skymind.pipelines.InferenceConfiguration;
import ai.skymind.pipelines.model.ModelConfig;
import ai.skymind.pipelines.model.ModelConfigType;
import ai.skymind.pipelines.model.SameDiffConfig;
import ai.skymind.pipelines.pipeline.ArrayConcatneationStep;
import ai.skymind.pipelines.pipeline.ModelPipelineStep;
import ai.skymind.pipelines.serving.Input;
import ai.skymind.pipelines.serving.Output;
import ai.skymind.pipelines.serving.ParallelInferenceConfig;
import ai.skymind.pipelines.serving.ServingConfig;
import ai.skymind.pipelines.verticles.BaseVerticleTest;
import ai.skymind.pipelines.verticles.VerticleConstants;
import ai.skymind.pipelines.verticles.inference.InferenceVerticle;
import com.jayway.restassured.response.Response;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.apache.arrow.flatbuf.Tensor;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nd4j.arrow.ArrowSerde;
import org.nd4j.autodiff.samediff.SDVariable;
import org.nd4j.autodiff.samediff.SameDiff;
import org.nd4j.linalg.api.buffer.DataType;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import javax.annotation.concurrent.NotThreadSafe;
import java.io.File;
import java.nio.ByteBuffer;
import java.util.Arrays;

import static com.jayway.restassured.RestAssured.given;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;


@RunWith(VertxUnitRunner.class)
@NotThreadSafe
public class SameDiffVerticleArrowTest extends BaseVerticleTest {
    @Override
    public Class<? extends AbstractVerticle> getVerticalClazz() {
        return InferenceVerticle.class;
    }
    
    @Override
    public Handler<HttpServerRequest> getRequest() {
        return null;
    }
    
    @Override
    public JsonObject getConfigObject() throws Exception {
        SameDiff sameDiff = SameDiff.create();
        SDVariable x = sameDiff.placeHolder("x", DataType.FLOAT,1,2);
        SDVariable y = sameDiff.placeHolder("y",DataType.FLOAT,1,2);
        SDVariable add = x.add("output",y);
        File tmpSameDiffFile = temporary.newFile();
        sameDiff.asFlatFile(tmpSameDiffFile);
        SameDiff values = SameDiff.fromFlatFile(tmpSameDiffFile);
        
        ParallelInferenceConfig parallelInferenceConfig = ParallelInferenceConfig.defaultConfig();
        
        
        ServingConfig servingConfig = ServingConfig.builder()
                .outputDataType(Output.DataType.ARROW)
                .parallelInferenceConfig(parallelInferenceConfig)
                .httpPort(port)
                .inputDataType(Input.DataType.NUMPY)
                .predictionType(Output.PredictionType.RAW)
                .build();
        
        SameDiffConfig modelConfig = SameDiffConfig.builder()
                .modelConfigType(
                        ModelConfigType.builder()
                                .modelType(ModelConfig.ModelType.SAMEDIFF)
                                .modelLoadingPath(tmpSameDiffFile.getAbsolutePath())
                                .build()
                )                .build();
        
        ArrayConcatneationStep arrayConcat = ArrayConcatneationStep.builder()
                .build();
        
        ModelPipelineStep modelPipelineStepConfig = ModelPipelineStep.builder()
                .inputNames(Arrays.asList(new String[]{"x","y"}))
                .outputNames(Arrays.asList(new String[]{"output"}))
                .modelConfig(modelConfig)
                .servingConfig(servingConfig)
                .build();
        
        
        
        InferenceConfiguration inferenceConfiguration = InferenceConfiguration.builder()
                .pipelineStep(arrayConcat)
                .pipelineStep(modelPipelineStepConfig)
                .servingConfig(servingConfig)
                .build();
        
        return new JsonObject(inferenceConfiguration.toJson());
    }
    
    
    @Test
    public void runAdd(TestContext testContext) throws Exception {
        INDArray x = Nd4j.create(new float[]{1.0f,2.0f});
        INDArray y = Nd4j.create(new float[]{2.0f,3.0f});
        byte[] xNpy = Nd4j.toNpyByteArray(x);
        byte[] yNpy = Nd4j.toNpyByteArray(y);
        
        
        File xFile = temporary.newFile();
        FileUtils.writeByteArrayToFile(xFile,xNpy);
        
        File yFile = temporary.newFile();
        FileUtils.writeByteArrayToFile(yFile,yNpy);
        
        
        Response response = given().port(port)
                .multiPart("x", xFile)
                .multiPart("y", yFile)
                .post("/arrow/numpy")
                .andReturn();
        
        assertEquals("Response failed",200,response.getStatusCode());
        INDArray bodyResult = ArrowSerde.fromTensor(Tensor.getRootAsTensor(ByteBuffer.wrap(response.getBody().asByteArray())));
        assertArrayEquals(new long[]{1,2},bodyResult.shape());
        assertEquals(Nd4j.create(new float[]{3.0f,5.0f}).reshape(1,2),bodyResult);
        assertTrue(response.getHeader(VerticleConstants.BATCH_ID_HEADER) != null);
        
        
    }
    
    
}
