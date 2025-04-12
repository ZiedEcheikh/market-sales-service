aws dynamodb list-tables --endpoint-url http://localhost:8000  --region eu-west-1

aws dynamodb create-table --cli-input-json file://sale_table_schema.json --endpoint-url http://localhost:8000

aws dynamodb scan --table-name market-sale \
--endpoint-url http://localhost:8000

aws dynamodb delete-table --table-name  market-sale \
--endpoint-url http://localhost:8000

#Counter

aws dynamodb create-table --cli-input-json file://metadata_table_schema.json --endpoint-url http://localhost:8000

aws dynamodb put-item --table-name metadata --item '{"id": { "S": "Sale" }, "increment_id": { "N": "0" }}' \
      --endpoint-url http://localhost:8000

aws dynamodb query --table-name metadata \
    --key-condition-expression "id = :v_id" \
    --expression-attribute-values '{":v_id":{"S":"Sale"} }' \
    --endpoint-url http://localhost:8000
