#!/bin/bash

cd "$(dirname -- "$0")" || exit

CONTENT_TYPE_HDR='Content-Type:application/json'
URL=http://localhost:9000

curl  -X DELETE -H $CONTENT_TYPE_HDR "$URL/tables/foo"
curl  -X DELETE -H $CONTENT_TYPE_HDR "$URL/schemas/foo"
