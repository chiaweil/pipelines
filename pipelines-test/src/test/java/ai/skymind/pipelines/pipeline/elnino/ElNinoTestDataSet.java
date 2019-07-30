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

package ai.skymind.pipelines.pipeline.elnino;

import ai.skymind.pipelines.pipeline.TestData;

import static org.junit.Assert.assertEquals;

public class ElNinoTestDataSet implements TestData {

    private org.datavec.api.transform.schema.Schema inputSchema,outputSchema;

    public ElNinoTestDataSet() throws Exception {
        org.nd4j.linalg.io.ClassPathResource classPathResource = new org.nd4j.linalg.io.ClassPathResource("inference/elnino/regression/ElNinoPolReg.xml");
        org.dmg.pmml.PMML pmml = org.jpmml.model.PMMLUtil.unmarshal(classPathResource.getInputStream());
        org.datavec.api.transform.schema.Schema schema = ai.skymind.pipelines.pmml.util.PmmlUtils.inputSchema(pmml);
        assertEquals(6,schema.numColumns());
        //used for inference
        org.datavec.api.transform.schema.Schema outputSchema =  ai.skymind.pipelines.pmml.util.PmmlUtils.outputSchema(pmml);
        this.inputSchema = schema;
        this.outputSchema = outputSchema;

    }
    @Override
    public org.datavec.api.records.Record inputRow() {
        java.util.List<org.datavec.api.writable.Writable> row =  ai.skymind.pipelines.util.SchemaTypeUtils.getRecord(8.96,-140.32,-6.3,-6.4,83.5,27.32);
        org.datavec.api.records.Record record = new org.datavec.api.records.impl.Record(row,null);
        return record;
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
        return inputSchema.numColumns();
    }

    @Override
    public int expectedNumberOutputColumns() {
        return outputSchema.numColumns();
    }
}
