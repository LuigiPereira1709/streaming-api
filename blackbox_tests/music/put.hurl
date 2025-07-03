# Test for updating an existing music data
# 1. With files (e.g., contentfile, thumbnailfile) 
PUT http://d130vf1311tsqd.cloudfront.net/music
[Multipart]
id: pitanguinha
title: pitanguinha
thumbnailFile: file,./thumbnail.jpeg; image/jpeg;
contentFile: file,./content.mp3; audio/mpeg;
HTTP 200 # Music updated successfully
[Asserts]
jsonpath "$.id" == "pitanguinha"
jsonpath "$.title" == "pitanguinha"

# 2. With files (e.g., contentfile) and additional fields
PUT http://d130vf1311tsqd.cloudfront.net/music
[Multipart]
id: pitanguinha
title: pitanguinha
contentFile: file,./content.mp3; audio/mpeg;
HTTP 200 # Music updated successfully
[Asserts]
jsonpath "$.id" == "pitanguinha"
jsonpath "$.title" == "pitanguinha"

# 3. With files (e.g., thumbnailfile) and additional fields
PUT http://d130vf1311tsqd.cloudfront.net/music
[Multipart]
id: pitanguinha
title: pitanguinha
thumbnailFile: file,./thumbnail.jpeg; image/jpeg;
HTTP 200 # Music updated successfully
[Asserts]
jsonpath "$.id" == "pitanguinha"
jsonpath "$.title" == "pitanguinha"

# 4. Without files (e.g., contentfile, thumbnailfile)
PUT http://d130vf1311tsqd.cloudfront.net/music
[Multipart]
id: pitanguinha
title: pitanguinha
HTTP 200 # Music updated successfully
[Asserts]
jsonpath "$.id" == "pitanguinha"
jsonpath "$.title" == "pitanguinha"

# 5. Id not found (e.g., id does not exist in the database)
PUT http://d130vf1311tsqd.cloudfront.net/music
[Multipart]
id: non_existent_id
HTTP 404 # Not Found

# 6. Invalid content type of files (e.g., contentfile, thumbnailfile)
PUT http://d130vf1311tsqd.cloudfront.net/music
[Multipart]
thumbnailFile: file,./content.mp3; audio/mpeg;  # Invalid content type
contentFile: file,./thumbnail.jpeg; image/jpeg;  # Invalid content type
HTTP 400 # Bad Request
[Asserts]
jsonpath "$.message" == "Validation error occurred"
jsonpath "$.details.validationErrors.thumbnailFile" count == 1 # Invalid type for thumbnailFile
jsonpath "$.details.validationErrors.contentFile" count == 1 # Invalid type for contentFile

# 7. Invalid enum values (e.g., genre, moods)
PUT http://d130vf1311tsqd.cloudfront.net/music
[Multipart]
genre: InvalidGenre
moods: InvalidMood
HTTP 400 # Bad Request
[Asserts]
jsonpath "$.details.validationErrors.genre" count == 1 # Invalid genre
jsonpath "$.message" == "Validation error occurred"
jsonpath "$.details.validationErrors.moods" count == 1 # Invalid mood

# 8. Invalid array size (e.g., moods)
PUT http://d130vf1311tsqd.cloudfront.net/music
[Multipart]
# Moods limits is 6
moods: Happy
moods: Sad
moods: Sad
moods: Sad
moods: Happy
moods: Sad
moods: Sad
HTTP 400 # Bad Request
[Asserts]
jsonpath "$.message" == "Validation error occurred"
jsonpath "$.details.validationErrors.moods" count == 1 # Invalid size for moods, more than 6 items
