# Get signed url for content by id
GET http://d130vf1311tsqd.cloudfront.net/podcast/content?id=pitanguinha
HTTP 200 # Content url returned successfully
[Asserts]
url contains "pitanguinha"

# Id not found
GET http://d130vf1311tsqd.cloudfront.net/podcast/content?id=non_existent_id
HTTP 404 # Not Found
[Asserts]
jsonpath "$.message" == "Resource not found"
jsonpath "$.details" exists
