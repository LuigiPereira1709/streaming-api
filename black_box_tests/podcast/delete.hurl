# Delete a resource by existing id 
DELETE http://d130vf1311tsqd.cloudfront.net/podcast/pitanguinha
HTTP 204 # Podcast deleted successfully

# Delete a resource by non-existing id
DELETE http://d130vf1311tsqd.cloudfront.net/podcast/non_existent_id
HTTP 404 # Not Found
[Asserts]
jsonpath "$.message" == "Resource not found"
jsonpath "$.details" exists
