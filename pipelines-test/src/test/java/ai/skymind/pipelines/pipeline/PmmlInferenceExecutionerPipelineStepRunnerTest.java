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

package ai.skymind.pipelines.pipeline;



import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PmmlInferenceExecutionerPipelineStepRunnerTest {

    @org.junit.Test
    public void testClassName() throws Exception {
        Class.forName(new PmmlPipelineStep().pipelineStepClazz());
    }

    @org.junit.Test
    public void testPipelining() throws Exception {
        testIrisClassificationPipelining("inference/iris/classification/ensemble_iris_dectree.xml");
        testIrisClassificationPipelining("inference/iris/classification/ensemble_iris_mlp.xml");
        testIrisClassificationPipelining("inference/iris/classification/ensemble_iris_svm.xml");
        testIrisClassificationPipelining("inference/iris/classification/IrisRandomForest.xml");
        testIrisClassificationPipelining("inference/iris/classification/IrisTree.xml");
        testIrisClassificationPipelining("inference/iris/classification/single_iris_mlp.xml");
        testIrisClassificationPipelining("inference/iris/classification/single_iris_svm.xml");


    }


    @org.junit.Test
    public void irisTest() throws Exception {
        testIrisClassification("inference/iris/classification/ensemble_iris_dectree.xml");
        testIrisClassification("inference/iris/classification/ensemble_iris_mlp.xml");
        testIrisClassification("inference/iris/classification/ensemble_iris_svm.xml");
        testIrisClassification("inference/iris/classification/IrisRandomForest.xml");
        testIrisClassification("inference/iris/classification/IrisTree.xml");
        testIrisClassification("inference/iris/classification/single_iris_mlp.xml");
        testIrisClassification("inference/iris/classification/single_iris_svm.xml");


    }


    @org.junit.Test
    public void irisTestRegression() throws Exception {
        testIrisRegression("inference/iris/regression/ensemble_iris_linreg.xml",new ai.skymind.pipelines.pipeline.iris.IrisRegressionTestData());
        testIrisRegression("inference/iris/regression/IrisGeneralRegression.xml",new ai.skymind.pipelines.pipeline.iris.IrisRegressionTestData());
        testIrisRegression("inference/iris/regression/IrisMultinomReg.xml",new ai.skymind.pipelines.pipeline.iris.IrisClassificationTestData());
        testIrisRegression("inference/iris/regression/single_iris_logreg.xml", new ai.skymind.pipelines.pipeline.iris.IrisClassificationTestData());



    }

    @org.junit.Test
    public void regressionTest() throws Exception {
        testElninoRegression("inference/elnino/regression/ElNinoPolReg.xml");
        testElninoRegression("inference/elnino/regression/ElNinoLinearReg.xml");


    }



    private void testElninoRegression(String resourceName) throws Exception {
        ai.skymind.pipelines.pipeline.elnino.ElNinoTestDataSet elNinoTestDataSet = new ai.skymind.pipelines.pipeline.elnino.ElNinoTestDataSet();
        org.nd4j.linalg.io.ClassPathResource classPathResource = new org.nd4j.linalg.io.ClassPathResource(resourceName);
        org.dmg.pmml.PMML pmml = org.jpmml.model.PMMLUtil.unmarshal(classPathResource.getInputStream());
        ai.skymind.pipelines.model.PmmlConfig config = ai.skymind.pipelines.model.PmmlConfig.builder()
                .modelConfigType(ai.skymind.pipelines.model.ModelConfigType.pmml(classPathResource.getFile().getAbsolutePath()))
                .build();


        org.datavec.api.transform.schema.Schema schemaConvert = ai.skymind.pipelines.pmml.util.PmmlUtils.inputSchema(pmml);
        org.jpmml.evaluator.ModelEvaluatorFactory modelEvaluatorFactory = org.jpmml.evaluator.ModelEvaluatorFactory.newInstance();
        org.datavec.api.transform.schema.Schema outputSchema =  elNinoTestDataSet.outputSchema();


        ai.skymind.pipelines.serving.ServingConfig servingConfig = ai.skymind.pipelines.serving.ServingConfig.builder()
                .parallelInferenceConfig(ai.skymind.pipelines.serving.ParallelInferenceConfig.defaultConfig())
                .build();


        PmmlPipelineStep pmmlPipelineStepConfig = PmmlPipelineStep.builder()
                .modelConfig(config)

                .servingConfig(servingConfig)
                .inputSchema("default", ai.skymind.pipelines.util.SchemaTypeUtils.typesForSchema(schemaConvert))
                .inputColumnName("default", ai.skymind.pipelines.util.SchemaTypeUtils.columnNames(schemaConvert))
                .outputSchema("default", ai.skymind.pipelines.util.SchemaTypeUtils.typesForSchema(outputSchema))
                .outputColumnName("default", ai.skymind.pipelines.util.SchemaTypeUtils.columnNames(outputSchema))
                .build();

        PmmlInferenceExecutionerPipelineStepRunner step = new PmmlInferenceExecutionerPipelineStepRunner(pmmlPipelineStepConfig);
        org.datavec.api.records.Record row =  elNinoTestDataSet.inputRow();
        org.datavec.api.records.Record[] transform = step.transform(new org.datavec.api.records.Record[]{row});
        assertEquals(1,transform.length);
        org.datavec.api.writable.Text json = (org.datavec.api.writable.Text) transform[0].getRecord().get(0);
        ai.skymind.pipelines.util.ObjectMapperHolder.getJsonMapper().readValue(json.toString(), java.util.List.class);


    }


    private void testIrisRegression(String resourceName, TestData testData) throws Exception {
        org.nd4j.linalg.io.ClassPathResource classPathResource = new org.nd4j.linalg.io.ClassPathResource(resourceName);
        ai.skymind.pipelines.model.PmmlConfig config = ai.skymind.pipelines.model.PmmlConfig.builder()
                .modelConfigType(ai.skymind.pipelines.model.ModelConfigType.pmml(classPathResource.getFile().getAbsolutePath()))
                .build();
        org.dmg.pmml.PMML pmml = org.jpmml.model.PMMLUtil.unmarshal(classPathResource.getInputStream());

        org.jpmml.evaluator.ModelEvaluatorFactory modelEvaluatorFactory = org.jpmml.evaluator.ModelEvaluatorFactory.newInstance();
        org.datavec.api.transform.schema.Schema schemaConvert = ai.skymind.pipelines.pmml.util.PmmlUtils.inputSchema(pmml);
        assertEquals(4,schemaConvert.numColumns());
        System.out.println("Input schema for resource " + resourceName + " is " + schemaConvert);

        ai.skymind.pipelines.serving.ServingConfig servingConfig = ai.skymind.pipelines.serving.ServingConfig.builder()
                .parallelInferenceConfig(ai.skymind.pipelines.serving.ParallelInferenceConfig.defaultConfig())
                .build();

        org.datavec.api.transform.schema.Schema outputSchema = ai.skymind.pipelines.pmml.util.PmmlUtils.outputSchema(pmml);
        System.out.println("Output schema for resource " + resourceName + " is " + outputSchema);

        PmmlPipelineStep pmmlPipelineStepConfig = PmmlPipelineStep.builder()
                .modelConfig(config)
                .servingConfig(servingConfig)

                .inputSchema("default", ai.skymind.pipelines.util.SchemaTypeUtils.typesForSchema(schemaConvert))
                .inputColumnName("default", ai.skymind.pipelines.util.SchemaTypeUtils.columnNames(schemaConvert))
                .outputSchema("default", ai.skymind.pipelines.util.SchemaTypeUtils.typesForSchema(outputSchema))
                .outputColumnName("default", ai.skymind.pipelines.util.SchemaTypeUtils.columnNames(outputSchema))
                .build();

        PmmlInferenceExecutionerPipelineStepRunner step = new PmmlInferenceExecutionerPipelineStepRunner(pmmlPipelineStepConfig);


        org.datavec.api.records.Record record = testData.inputRow();
        org.datavec.api.records.Record[] transform = step.transform(new org.datavec.api.records.Record[]{record});
        assertEquals(1,transform.length);
        org.datavec.api.writable.Text json = (org.datavec.api.writable.Text) transform[0].getRecord().get(0);
        ai.skymind.pipelines.util.ObjectMapperHolder.getJsonMapper().readValue(json.toString(), java.util.List.class);


    }


    private void testIrisClassification(String resourceName) throws Exception {
        org.nd4j.linalg.io.ClassPathResource classPathResource = new org.nd4j.linalg.io.ClassPathResource(resourceName);
       TestData testData = new ai.skymind.pipelines.pipeline.iris.IrisClassificationTestData();
        ai.skymind.pipelines.model.PmmlConfig config = ai.skymind.pipelines.model.PmmlConfig.builder()
                .modelConfigType(ai.skymind.pipelines.model.ModelConfigType.pmml(classPathResource.getFile().getAbsolutePath()))
                .build();
        org.dmg.pmml.PMML pmml = org.jpmml.model.PMMLUtil.unmarshal(classPathResource.getInputStream());

        org.jpmml.evaluator.ModelEvaluatorFactory modelEvaluatorFactory = org.jpmml.evaluator.ModelEvaluatorFactory.newInstance();
        org.datavec.api.transform.schema.Schema schemaConvert = ai.skymind.pipelines.pmml.util.PmmlUtils.inputSchema(pmml);
        assertEquals(4,schemaConvert.numColumns());
        System.out.println("Input schema for resource " + resourceName + " is " + schemaConvert);

        ai.skymind.pipelines.serving.ServingConfig servingConfig = ai.skymind.pipelines.serving.ServingConfig.builder()
                .parallelInferenceConfig(ai.skymind.pipelines.serving.ParallelInferenceConfig.defaultConfig())
                .build();

        org.datavec.api.transform.schema.Schema outputSchema = ai.skymind.pipelines.pmml.util.PmmlUtils.outputSchema(pmml);
        System.out.println("Output schema for resource " + resourceName + " is " + outputSchema);

        PmmlPipelineStep pmmlPipelineStepConfig = PmmlPipelineStep.builder()
                .modelConfig(config)
                .servingConfig(servingConfig)

                .inputSchema("default", ai.skymind.pipelines.util.SchemaTypeUtils.typesForSchema(schemaConvert))
                .inputColumnName("default", ai.skymind.pipelines.util.SchemaTypeUtils.columnNames(schemaConvert))
                .outputSchema("default", ai.skymind.pipelines.util.SchemaTypeUtils.typesForSchema(outputSchema))
                .outputColumnName("default", ai.skymind.pipelines.util.SchemaTypeUtils.columnNames(outputSchema))
                .build();

        PmmlInferenceExecutionerPipelineStepRunner step = new PmmlInferenceExecutionerPipelineStepRunner(pmmlPipelineStepConfig);


        org.datavec.api.records.Record record = testData.inputRow();
        org.datavec.api.records.Record[] transform = step.transform(new org.datavec.api.records.Record[]{record});
        assertEquals(1,transform.length);
        org.datavec.api.writable.Text json = (org.datavec.api.writable.Text) transform[0].getRecord().get(0);
        ai.skymind.pipelines.util.ObjectMapperHolder.getJsonMapper().readValue(json.toString(), java.util.List.class);

        //3 probabilities from iris
        System.out.println(resourceName + "  has writable type of " + transform[0].getRecord().get(0).getType());

    }


    private void testIrisClassificationPipelining(String resourceName) throws Exception {
        org.nd4j.linalg.io.ClassPathResource classPathResource = new org.nd4j.linalg.io.ClassPathResource(resourceName);
        TestData testData = new ai.skymind.pipelines.pipeline.iris.IrisClassificationTestData();
        ai.skymind.pipelines.model.PmmlConfig config = ai.skymind.pipelines.model.PmmlConfig.builder()
                .modelConfigType(ai.skymind.pipelines.model.ModelConfigType.pmml(classPathResource.getFile().getAbsolutePath()))
                .build();
        org.dmg.pmml.PMML pmml = org.jpmml.model.PMMLUtil.unmarshal(classPathResource.getInputStream());

        org.jpmml.evaluator.ModelEvaluatorFactory modelEvaluatorFactory = org.jpmml.evaluator.ModelEvaluatorFactory.newInstance();

        org.datavec.api.transform.schema.Schema schemaConvert = ai.skymind.pipelines.pmml.util.PmmlUtils.inputSchema(pmml);

        ai.skymind.pipelines.serving.ServingConfig servingConfig = ai.skymind.pipelines.serving.ServingConfig.builder()
                .parallelInferenceConfig(ai.skymind.pipelines.serving.ParallelInferenceConfig.defaultConfig())
                .build();

        org.datavec.api.transform.schema.Schema outputSchema = ai.skymind.pipelines.pmml.util.PmmlUtils.outputSchema(pmml);
        PmmlPipelineStep pmmlPipelineStepConfig = PmmlPipelineStep.builder()
                .modelConfig(config)
                .servingConfig(servingConfig)

                .inputSchema("default", ai.skymind.pipelines.util.SchemaTypeUtils.typesForSchema(testData.inputSchema()))
                .inputColumnName("default", ai.skymind.pipelines.util.SchemaTypeUtils.columnNames(testData.inputSchema()))
                .outputSchema("default", ai.skymind.pipelines.util.SchemaTypeUtils.typesForSchema(outputSchema))
                .outputColumnName("default", ai.skymind.pipelines.util.SchemaTypeUtils.columnNames(outputSchema))
                .build();

        PmmlInferenceExecutionerPipelineStepRunner step = new PmmlInferenceExecutionerPipelineStepRunner(pmmlPipelineStepConfig);


        org.datavec.api.records.Record record = testData.inputRow();
        org.datavec.api.records.Record[] transform = step.transform(new org.datavec.api.records.Record[]{record});
        assertEquals(1,transform.length);
        //3 probabilities from iris
        org.datavec.api.writable.Writable writable =  transform[0].getRecord().get(0);
        if(writable instanceof  org.datavec.api.writable.NDArrayWritable) {
            org.datavec.api.writable.NDArrayWritable ndArrayWritable = (org.datavec.api.writable.NDArrayWritable)writable;
            assertTrue(ndArrayWritable.get().length() > 0);
        }
        else if(writable instanceof org.datavec.api.writable.Text) {
            org.datavec.api.writable.Text text = (org.datavec.api.writable.Text) writable;
            System.out.println(text);
        }

        org.datavec.api.writable.Text json = (org.datavec.api.writable.Text) transform[0].getRecord().get(0);


    }



}
