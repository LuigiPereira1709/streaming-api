# Get signed url for content by id
GET http://d130vf1311tsqd.cloudfront.net/music/content?id=6861bc0cca33134492220030
HTTP 200 # Content url returned successfully 
[Asserts]
url contains "pitanguinha" 

# Id not found  
GET http://d130vf1311tsqd.cloudfront.net/music/content?id=non_existent_id
HTTP 404 # Not Found
[Asserts]
jsonpath "$.message" == "Resource not found"
jsonpath "$.details" exists
