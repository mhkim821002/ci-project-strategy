#!/bin/sh

export API_KEY=$1
export PROJECT_KEY=$2
export API_URL=$3
export BOM=$4
export IS_SET_BOM=0

if [[ "$BOM" == "{"* ]]; then
    echo $BOM > bom-upload.json
    export IS_SET_BOM=1
fi

if [ -f "$BOM" ]; then
    cp $BOM bom-upload.json
    export IS_SET_BOM=1
fi

if [ $IS_SET_BOM -eq 0 ]; then
    echo "Payload is not defined ..."
    exit 1
fi

curl -X "POST" -H "Content-Type: multipart/form-data" -H "X-Api-Key: $API_KEY" -F "project=$PROJECT_KEY" -F "bom=@bom-upload.json" ${API_URL}/api/v1/bom

echo "api-url=${API_URL}/api/v1/bom" >> $GITHUB_OUTPUT

rm -rf bom-upload.json
