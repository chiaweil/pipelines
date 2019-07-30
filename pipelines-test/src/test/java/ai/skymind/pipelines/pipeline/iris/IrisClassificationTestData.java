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

package ai.skymind.pipelines.pipeline.iris;

import ai.skymind.pipelines.pipeline.TestData;

public class IrisClassificationTestData implements TestData  {

    private org.datavec.api.transform.schema.Schema inputSchema,outputSchema;

    public IrisClassificationTestData() throws Exception {
        org.nd4j.linalg.io.ClassPathResource classPathResource = new org.nd4j.linalg.io.ClassPathResource("inference/iris/classification/single_iris_mlp.xml");
        org.dmg.pmml.PMML pmml = org.jpmml.model.PMMLUtil.unmarshal(classPathResource.getInputStream());
        org.datavec.api.transform.schema.Schema schema = ai.skymind.pipelines.pmml.util.PmmlUtils.inputSchema(pmml);

        java.util.List<org.datavec.api.transform.metadata.ColumnMetaData> inputColumns = schema.getColumnMetaData();
        org.datavec.api.transform.schema.Schema inputSchema = new org.datavec.api.transform.schema.Schema(inputColumns);
        this.inputSchema = inputSchema;
        this.outputSchema = ai.skymind.pipelines.pmml.util.PmmlUtils.outputSchema(pmml);
    }


    @Override
    public org.datavec.api.records.Record inputRow() {
        java.util.List<org.datavec.api.writable.Writable> row = java.util.Arrays.asList(
                new org.datavec.api.writable.DoubleWritable(5.1),new org.datavec.api.writable.DoubleWritable(3.5),
                new org.datavec.api.writable.DoubleWritable(4.0),new org.datavec.api.writable.DoubleWritable(2.0)
        );

        return new org.datavec.api.records.impl.Record(row,null);
    }

    @Override
    public org.datavec.api.transform.schema.Schema inputSchema() {
        return inputSchema;
    }

    @Override
    public org.datavec.api.transform.schema.Schema outputSchema() {
        return outputSchema;
    }

    @Override
    public int expectedNumberInputColumns() {
        return 1;
    }

    @Override
    public int expectedNumberOutputColumns() {
        return 4;
    }
}
