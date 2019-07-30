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

package ai.skymind.pipelines.pmml;


import ai.skymind.pipelines.pipeline.iris.IrisClassificationTestData;

import static org.junit.Assert.assertEquals;


@org.junit.runner.RunWith(io.vertx.ext.unit.junit.VertxUnitRunner.class)
public class ColumnarVerticlePmmlTest extends pipelines.verticles.BaseVerticleTest {
    private ai.skymind.pipelines.verticles.inference.InferenceVerticle columnarVerticle;

    @org.junit.After
    public void after(io.vertx.ext.unit.TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }

    @Override
    public Class<? extends io.vertx.core.AbstractVerticle> getVerticalClazz() {
        return ai.skymind.pipelines.verticles.inference.InferenceVerticle.class;
    }

    @Override
    public io.vertx.core.Handler<io.vertx.core.http.HttpServerRequest> getRequest() {

        return req -> {
            //should be json body of classification
            req.bodyHandler(body -> {
                try {
                    ai.skymind.pipelines.output.types.ClassifierOutput classifierOutput = ai.skymind.pipelines.util.ObjectMapperHolder.getJsonMapper().readValue(body.toString(), ai.skymind.pipelines.output.types.ClassifierOutput.class);
                    assertEquals(1, classifierOutput.getDecisions()[0]);
                } catch (java.io.IOException e) {
                    e.printStackTrace();
                }
            });

            req.exceptionHandler(Throwable::printStackTrace);


        };
    }

    @Override
    public io.vertx.core.json.JsonObject getConfigObject() throws Exception {
        IrisClassificationTestData irisClassificationTestData = new IrisClassificationTestData();
        org.datavec.api.transform.schema.Schema outputSchema = irisClassificationTestData.outputSchema();
        org.datavec.api.transform.schema.Schema inputSchema = irisClassificationTestData.inputSchema();
        String modelPath = new org.nd4j.linalg.io.ClassPathResource("inference/iris/classification/single_iris_mlp.xml").getFile().getAbsolutePath();

        org.nd4j.linalg.factory.Nd4j.getRandom().setSeed(42);

        ai.skymind.pipelines.serving.ParallelInferenceConfig parallelInferenceConfig = ai.skymind.pipelines.serving.ParallelInferenceConfig.builder()
                .inferenceMode(org.deeplearning4j.parallelism.inference.InferenceMode.SEQUENTIAL).build();



        ai.skymind.pipelines.model.PmmlConfig modelConfig = ai.skymind.pipelines.model.PmmlConfig.builder()
                .modelConfigType(ai.skymind.pipelines.model.ModelConfigType.pmml(modelPath))
                .build();


        ai.skymind.pipelines.serving.ServingConfig servingConfig = ai.skymind.pipelines.serving.ServingConfig.builder()
                .predictionType(ai.skymind.pipelines.serving.Output.PredictionType.CLASSIFICATION)
                .parallelInferenceConfig(parallelInferenceConfig)
                .httpPort(port)
                .build();

        ai.skymind.pipelines.pipeline.PmmlPipelineStep modelPipelineStepConfig = ai.skymind.pipelines.pipeline.PmmlPipelineStep.builder()
                .inputSchema("default", ai.skymind.pipelines.util.SchemaTypeUtils.typesForSchema(inputSchema))
                .outputSchema("default", ai.skymind.pipelines.util.SchemaTypeUtils.typesForSchema(outputSchema))
                .inputColumnName("default", ai.skymind.pipelines.util.SchemaTypeUtils.columnNames(inputSchema))
                .outputColumnName("default", ai.skymind.pipelines.util.SchemaTypeUtils.columnNames(outputSchema))
                .servingConfig(servingConfig)
                .modelConfig(modelConfig)
                .build();

        ai.skymind.pipelines.InferenceConfiguration inferenceConfiguration = ai.skymind.pipelines.InferenceConfiguration.builder()
                .servingConfig(servingConfig)
                .pipelineStep(modelPipelineStepConfig)
                .build();


        String json = inferenceConfiguration.toJson();
        System.out.println(json);
        return new io.vertx.core.json.JsonObject(json);

    }

    @Override
    public void setupVertx(io.vertx.core.Vertx vertx) {
        columnarVerticle = new ai.skymind.pipelines.verticles.inference.InferenceVerticle();

        this.vertx.registerVerticleFactory(new io.vertx.core.spi.VerticleFactory() {
            @Override
            public String prefix() {
                return "InferenceVerticle";
            }

            @Override
            public io.vertx.core.Verticle createVerticle(String verticleName, ClassLoader classLoader) {
                return columnarVerticle;
            }
        });
    }


    @org.junit.Test(timeout = 60000)
    public void testInferenceResult(io.vertx.ext.unit.TestContext context) throws Exception {
        io.vertx.core.json.JsonArray jsonArray = new io.vertx.core.json.JsonArray();
        double[] vals = {5.1, 3.5,1.9,2.0};

        for (int i = 0; i < vals.length; i++) {
            jsonArray.add(vals[i]);
        }


        io.vertx.core.json.JsonArray wrapper = new io.vertx.core.json.JsonArray();
        wrapper.add(jsonArray);

        com.jayway.restassured.response.Response response = com.jayway.restassured.RestAssured.given().port(port)
                .contentType("application/json")
                .accept("application/json")
                .body(wrapper.toBuffer().getBytes())
                .post("/classification/dict")
                .andReturn();
        assertEquals(200,response.getStatusCode());
        io.vertx.core.json.JsonObject jsonArray1 = new io.vertx.core.json.JsonObject(response.getBody().prettyPrint());
        assertEquals(1,jsonArray1.size());
    }





    @org.junit.Test(timeout = 60000)

    public void testInferenceResultWithErrorInput(io.vertx.ext.unit.TestContext context) {
        io.vertx.core.json.JsonArray jsonArray = new io.vertx.core.json.JsonArray();
        double[] vals = {5.1,3.5};
        for(int i = 0; i < 2; i++)  {
            jsonArray.add(vals[i]);
        }


        io.vertx.core.json.JsonArray invalidInput = new io.vertx.core.json.JsonArray();
        for(int i = 0; i < 2; i++) {
            invalidInput.add(vals[i]);
        }


        io.vertx.core.json.JsonArray wrapper = new io.vertx.core.json.JsonArray();
        wrapper.add(jsonArray);
        wrapper.add(invalidInput);
        io.vertx.core.http.HttpClient client2 = vertx.createHttpClient();
        client2.post(port,"localhost","/classification/csv/error")
                .handler(resp -> {
                    resp.bodyHandler(body -> {
                        try {
                            org.nd4j.shade.jackson.core.type.TypeReference<ai.skymind.pipelines.output.types.ErrorResult<ai.skymind.pipelines.output.types.ClassifierOutput>> typeRef = new org.nd4j.shade.jackson.core.type.TypeReference<ai.skymind.pipelines.output.types.ErrorResult<ai.skymind.pipelines.output.types.ClassifierOutput>>() {};
                            ai.skymind.pipelines.output.types.ErrorResult<ai.skymind.pipelines.output.types.ClassifierOutput> result = ai.skymind.pipelines.util.ObjectMapperHolder.getJsonMapper().readValue(body.toString(),typeRef);
                            // context.assertEquals(1,result.getResults().getDecisions()[0]);
                        } catch (java.io.IOException e) {
                            context.fail(e.getMessage());
                        }

                        System.out.println(body);
                    });
                    resp.exceptionHandler(exception -> {
                        context.fail(exception.getMessage());
                        exception.printStackTrace();
                    });
                }).putHeader("Content-Length",String.valueOf(wrapper.toBuffer().length()))
                .write(wrapper.toString())
                .putHeader("Content-Type","application/json").endHandler(end -> {
            System.out.println(end);
        }).endHandler(new io.vertx.core.Handler<Void>() {
            @Override
            public void handle(Void event) {

            }
        });


    }



}