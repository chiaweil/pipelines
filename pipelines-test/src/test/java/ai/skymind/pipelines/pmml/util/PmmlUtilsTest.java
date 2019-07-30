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

package ai.skymind.pipelines.verticles;

import static org.junit.Assert.assertEquals;

public class PmmlUtilsTest {

    @org.junit.Test
    public void testOutputSchema() throws Exception {
        org.nd4j.linalg.io.ClassPathResource classPathResource = new org.nd4j.linalg.io.ClassPathResource("inference/iris/classification/IrisTree.xml");
        try(java.io.InputStream is = classPathResource.getInputStream()) {
            org.dmg.pmml.PMML unmarshal = org.jpmml.model.PMMLUtil.unmarshal(is);
            org.datavec.api.transform.schema.Schema schema = ai.skymind.pipelines.pmml.util.PmmlUtils.outputSchema(unmarshal);
            /**
             *  <OutputField name="class" optype="categorical" dataType="string" feature="predictedValue"/>
             *    <OutputField name="Probability_Iris-setosa" optype="continuous" dataType="double" feature="probability" value="Iris-setosa"/>
             *    <OutputField name="Probability_Iris-versicolor" optype="continuous" dataType="double" feature="probability" value="Iris-versicolor"/>
             *    <OutputField name="Probability_Iris-virginica" optype="continuous" dataType="double" feature="probability" value="Iris-virginica"/>
             */

            org.datavec.api.transform.schema.Schema schemaAssertion = new org.datavec.api.transform.schema.Schema.Builder()
                    .addColumnString("class")
                    .addColumnDouble("Probability_Iris-setosa")
                    .addColumnDouble("Probability_Iris-versicolor")
                    .addColumnDouble("Probability_Iris-virginica")
                    .build();

            assertEquals(schemaAssertion,schema);
        }
    }

    @org.junit.Test
    public void testRecordConversion() {
        org.datavec.api.transform.schema.Schema inputSchema = new org.datavec.api.transform.schema.Schema.Builder()
                .addColumnInteger("first")
                .addColumnString("second")
                .build();

        java.util.List<java.util.Map<org.dmg.pmml.FieldName,Object>> inputPmml = new java.util.ArrayList<>();
        org.dmg.pmml.FieldName first = org.dmg.pmml.FieldName.create("first");
        org.dmg.pmml.FieldName second = org.dmg.pmml.FieldName.create("second");
        int numRecords = 5;
        for(int i = 0; i < numRecords; i++)  {
            java.util.Map<org.dmg.pmml.FieldName,Object> record = new java.util.HashMap<>();
            record.put(first,i);
            record.put(second,String.valueOf(i));
            inputPmml.add(record);
        }

        org.datavec.api.records.Record[] records = ai.skymind.pipelines.pmml.util.PmmlUtils.toRecords(inputPmml, inputSchema);
        assertEquals(numRecords,records.length);
        for(int i = 0; i < records.length; i++) {
            assertEquals(inputSchema.numColumns(),records[i].getRecord().size());
        }


    }

}
