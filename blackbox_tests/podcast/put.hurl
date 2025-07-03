# Test for updating an exisiting podcast data
# 1. With files (e.g., contentfile, thumbnailfile)
PUT http://d130vf1311tsqd.cloudfront.net/podcast
[Multipart]
id: pitanguinha
# title: pitanguinha
HTTP 200 # Podcast updated successfully
[Asserts]
jsonpath "$.id" == "pitanguinha"
jsonpath "$.title" == "pitanguinha"

# 2. With files (e.g., contentfile) and additional fields
PUT http://d130vf1311tsqd.cloudfront.net/podcast
[Multipart]
id: pitanguinha
title: pitanguinha
contentFile: file,./content.mp3; audio/mpeg;
HTTP 200 # Podcast updated successfully
[Asserts]
jsonpath "$.id" == "pitanguinha"
jsonpath "$.title" == "pitanguinha"

# 3. With files (e.g., thumbnailfile) and additional fields
PUT http://d130vf1311tsqd.cloudfront.net/podcast
[Multipart]
id: pitanguinha
title: pitanguinha
thumbnailFile: file,./thumbnail.jpeg; image/jpeg;
HTTP 200 # Podcast updated successfully
[Asserts]
jsonpath "$.id" == "pitanguinha"
jsonpath "$.title" == "pitanguinha"

# 4. Without files (e.g., contentfile, thumbnailfile)
PUT http://d130vf1311tsqd.cloudfront.net/podcast
[Multipart]
id: pitanguinha
title: pitanguinha
HTTP 200 # Podcast updated successfully
[Asserts]
jsonpath "$.id" == "pitanguinha"
jsonpath "$.title" == "pitanguinha"

# 5. Id not found (e.g., id does not exist in the database)
PUT http://d130vf1311tsqd.cloudfront.net/podcast
[Multipart]
id: non_existent_id
HTTP 404 # Not Found
[Asserts]
jsonpath "$.message" == "Resource not found"
jsonpath "$.details" exists

# 6. Invalid content type of files (e.g., contentfile, thumbnailfile)
PUT http://d130vf1311tsqd.cloudfront.net/podcast
[Multipart]
thumbnailFile: file,./content.mp3; audio/mpeg;  # Invalid content type
contentFile: file,./thumbnail.jpeg; image/jpeg;  # Invalid content type
HTTP 400 # Bad Request
[Asserts]
jsonpath "$.message" == "Validation error occurred"
jsonpath "$.details.validationErrors.thumbnailFile" count == 1 # Invalid type for thumbnailFile
jsonpath "$.details.validationErrors.contentFile" count == 1 # Invalid type for contentFile

# 7. Invalid enum values (e.g., categories)
PUT http://d130vf1311tsqd.cloudfront.net/podcast
[Multipart]
id: pitanguinha
categories: InvalidCategory
HTTP 400 # Bad Request
[Asserts]
jsonpath "$.message" == "Validation error occurred"
jsonpath "$.details.validationErrors.categories" count == 1 # Invalid category

# 8. Invalid size of array fields (e.g., categories)
PUT http://d130vf1311tsqd.cloudfront.net/podcast
[Multipart]
# Categories limits is 3
categories: Technology 
categories: Technology 
categories: Technology 
categories: Technology 
HTTP 400 # Bad Request
[Asserts]
jsonpath "$.message" == "Validation error occurred"
jsonpath "$.details.validationErrors.categories" count == 1 # Invalid size for categories, more than 3 items
