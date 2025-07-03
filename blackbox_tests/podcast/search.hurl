# Find by id
GET http://d130vf1311tsqd.cloudfront.net/podcast/pitanguinha
HTTP 200 # Podcast found successfully
[Asserts]
jsonpath "$.id" == "pitanguinha"

# Find by id not found
GET http://d130vf1311tsqd.cloudfront.net/podcast/non_existent_id
HTTP 404 # Not Found
[Asserts]
jsonpath "$.message" == "Resource not found"
jsonpath "$.details" exists

# Search by guests
POST http://d130vf1311tsqd.cloudfront.net/podcast/search/by-guests
["pitanguinha"]
HTTP 200 # Podcasts found by guests successfully
[Asserts]
jsonpath "$.*" count > 0 # Ensure results are returned 
jsonpath "$.[0].guests" contains "pitanguinha" # Ensure guest is in results

# Search by categories
POST http://d130vf1311tsqd.cloudfront.net/podcast/search/by-categories
["technology"]
HTTP 200 # Podcasts found by categories successfully
[Asserts]
jsonpath "$.*" count > 0 # Ensure results are returned
jsonpath "$.[0].categories" contains "Technology" # Ensure category is in results

# Search by title
GET http://d130vf1311tsqd.cloudfront.net/podcast/search?title=pitanguinha
HTTP 200 # Search results returned successfully
[Asserts]
jsonpath "$.*" count > 0 # Ensure results are returned
jsonpath "$.[0].title" contains "pitanguinha" # Ensure the title matches

# Search by presenter
GET http://d130vf1311tsqd.cloudfront.net/podcast/search?presenter=pitanguinha
HTTP 200 # Search results returned successfully
[Asserts]
jsonpath "$.*" count > 0 # Ensure results are returned
jsonpath "$.*.presenter" contains "pitanguinha marvada" # Ensure the presenter matches

# Search by guest
GET http://d130vf1311tsqd.cloudfront.net/podcast/search?guest=pitanguinha
HTTP 200 # Search results returned successfully
[Asserts]
jsonpath "$.*" count > 0 # Ensure results are returned
jsonpath "$.[0].guests" contains "pitanguinha" # Ensure the guest matches

# Search by year 
GET http://d130vf1311tsqd.cloudfront.net/podcast/search?year=2003
HTTP 200 # Search results returned successfully
[Asserts]
jsonpath "$.*" count > 0 # Ensure results are returned
jsonpath "$.[0].year" == 2003 # Ensure the year matches

# Search by year range
GET http://d130vf1311tsqd.cloudfront.net/podcast/search?startYear=2000&endYear=2008
HTTP 200 # Search results returned successfully
[Asserts]
jsonpath "$.*" count > 0 # Ensure results are returned
jsonpath "$.[0].year" >= 2000 # Ensure the year is within the range
jsonpath "$.[0].year" <= 2008 # Ensure the year is within the range
