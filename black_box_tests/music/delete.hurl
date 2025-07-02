# Delete a resource by existing id 
DELETE http://d130vf1311tsqd.cloudfront.net/music/pitanguinha
HTTP 204 # Music deleted successfully

# Delete a resource by non-existing id
DELETE http://d130vf1311tsqd.cloudfront.net/music/non_existent_id
HTTP 404 # Not Found
[Asserts]
jsonpath "$.message" == "Resource not found"
jsonpath "$.details" exists
