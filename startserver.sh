#!/bin/bash
$JAVA_HOME/bin/java \
  -DZOOKEEPER_EMBEDDED=false \
  -DZOOKEEPER_HOST=localhost \
  -DSPARK_HOME=/opt/spark \
  -DSKIL_HOME=/opt/skil \
  -DSKIL_LICENSE_PATH=/etc/skil/license.txt \
  -DSKIL_PUBLIC_KEY_PATH=/etc/skil/publickey.txt \
  -DSKIL_CLASS_PATH=/etc/skil/logging/*:/opt/skil/lib/*:/opt/skil/native/*:/opt/skil/jackson-2.5.1/*:/etc/skil/* \
  -DSKIL_SERVER_PROD_MODE=true -DSKIL_LOG_DIR=/var/log/skil -Dservice.id=d4b87b2a-e4d4-401d-9fb3-62074bd16dd4 \
  -Dservice.type=dataVecTransform -Dlogback.configurationFile=/etc/skil/logback.xml \
  -cp /etc/skil/logging/*:/opt/skil/lib/*:/opt/skil/native/*:/opt/skil/jackson-2.5.1/*:/etc/skil/*:/opt/skil/native/*:/opt/skil/spark/*:/opt/skil/plugins/*:/opt/skil/jersey-2.0.1/*:/opt/skil/jackson-2.5.1/* io.skymind.spark.transform.SparkTransformServerChooser \
  --jsonPath /var/skil/models/transform/deployment-1/model-8/ver-0.transform \
  --dataVecPort 9899 \
  --jsonInputType CSV \
  --agentId 1f81ae45-1844-4058-a48a-d263f0f8da54222
