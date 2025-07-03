# Find by id
GET http://d130vf1311tsqd.cloudfront.net/music/pitanguinha
HTTP 200 # Music found successfully
[Asserts]
jsonpath "$.id" == "pitanguinha"

# Find by id not found
GET http://d130vf1311tsqd.cloudfront.net/music/non_existent_id
HTTP 404 # Not Found
[Asserts]
jsonpath "$.message" == "Resource not found"
jsonpath "$.details" exists

# Search by moods
POST http://d130vf1311tsqd.cloudfront.net/music/search/by-moods
["Happy"]
HTTP 200 # Search results returned successfully
[Asserts]
jsonpath "$.*" count > 0 # Ensure results are returned
jsonpath "$.[0].moods" contains "Happy" # Ensure the mood is present in the results

# Search by feats
POST http://d130vf1311tsqd.cloudfront.net/music/search/by-feats
["pitanguinha"]
HTTP 200 # Search results returned successfully
[Asserts]
jsonpath "$.*" count > 0 # Ensure results are returned
jsonpath "$.[0].feats" contains "pitanguinha" # Ensure the feat is present in the results

# Search by title
GET http://d130vf1311tsqd.cloudfront.net/music/search?title=pitanguinha
HTTP 200 # Search results returned successfully
[Asserts]
jsonpath "$.*" count > 0 # Ensure results are returned
jsonpath "$.[0].title" contains "pitanguinha" # Ensure the title matches

# Search by artist
GET http://d130vf1311tsqd.cloudfront.net/music/search?artist=pitanguinha
HTTP 200 # Search results returned successfully
[Asserts]
jsonpath "$.*" count > 0 # ensure results are returned
jsonpath "$.*.artist" contains "pitanguinha marvada" # ensure the artist matches

# Search by genre
GET http://d130vf1311tsqd.cloudfront.net/music/search?genre=rock
HTTP 200 # Search results returned successfully
[Asserts]
jsonpath "$.*" count > 0 # Ensure results are returned
jsonpath "$.*.genre" contains "Rock" # Ensure the genre matches

# Search by album
GET http://d130vf1311tsqd.cloudfront.net/music/search?album=album
HTTP 200 # Search results returned successfully
[Asserts]
jsonpath "$.*" count > 0 # Ensure results are returned
jsonpath "$.*.album" contains "Album" # Ensure the album matches

# Search by feat 
GET http://d130vf1311tsqd.cloudfront.net/music/search?feat=feat
HTTP 200 # Search results returned successfully
[Asserts]
jsonpath "$.*" count > 0 # Ensure results are returned
jsonpath "$.[0].feats" contains "Feat 1" # Ensure the feat matches

# Search by year
GET http://d130vf1311tsqd.cloudfront.net/music/search?year=2003
HTTP 200 # Search results returned successfully
[Asserts]
jsonpath "$.*" count > 0 # Ensure results are returned
jsonpath "$.*.year" contains 2003 # Ensure the year matches

# Search by year range 
GET http://d130vf1311tsqd.cloudfront.net/music/search?startYear=2000&endYear=2025
HTTP 200 # Search results returned successfully
[Asserts]
jsonpath "$.*" count > 0 # Ensure results are returned
jsonpath "$.[0].year" >= 2000 # Ensure the year is within the range
