# Post a valid podcast data
POST http://d130vf1311tsqd.cloudfront.net/podcast
[Multipart]
title: My Podcast
year: 2023
explicit: true
thumbnailFile: file,./thumbnail.jpeg; image/jpeg;
contentFile: file,./content.mp3; audio/mpeg;
presenter: Podcast Presenter 
guests: Guest
description: This is a podcast description.
categories: Technology
categories: Education
episodeNumber: 1
seasonNumber: 1
HTTP 200 # Podcast created successfully
[Asserts]
# Check the response body
jsonpath "$.id" exists
jsonpath "$.title" == "My Podcast"
jsonpath "$.thumbnailUrl" exists
jsonpath "$.year" == 2023
jsonpath "$.explicit" == true
jsonpath "$.presenter" == "Podcast Presenter"
jsonpath "$.guests" contains "Guest"
jsonpath "$.description" == "This is a podcast description."
jsonpath "$.categories" contains "Technology"
jsonpath "$.categories" contains "Education"
jsonpath "$.episodeNumber" == 1
jsonpath "$.seasonNumber" == 1

# Tests for posting a invalid podcast data
# 1. Missing fields (e.g., title, presenter, description, categories, thumbnailFile, contentFile)
POST http://d130vf1311tsqd.cloudfront.net/podcast
[Multipart]
year: 2023
explicit: true
guests: Guest
episodeNumber: 1
seasonNumber: 1
HTTP 400 # Bad Request
[Asserts]
jsonpath "$.message" == "Validation error occurred"
jsonpath "$.details.validationErrors.title" count == 2 # When missing title, there are 2 validation errors: non-null and non-empty
jsonpath "$.details.validationErrors.description" count == 2 # When missing description, there is 2 validation error: non-null and non-empty
jsonpath "$.details.validationErrors.categories" count == 1 # When missing categories, there is 2 validation error: non-null
jsonpath "$.details.validationErrors.thumbnailFile" count == 1 # When missing thumbnailFile, there is 1 validation error: non-null
jsonpath "$.details.validationErrors.contentFile" count == 1 # When missing contentFile, there is 1 validation error: non-null

# 3. Invalid range for year (e.g., year < 1900 or year > current year)
POST http://d130vf1311tsqd.cloudfront.net/podcast
[Multipart]
year: 1800 # Invalid year
HTTP 400 # Bad Request
[Asserts]
jsonpath "$.message" == "Validation error occurred"
jsonpath "$.details.validationErrors.year" count == 1 # When invalid year, there is 1 validation error: range (1900 - current year)

# 4. Invalid type of files (e.g., thumbnailFile, contentFile) 
POST http://d130vf1311tsqd.cloudfront.net/podcast
[Multipart]
thumbnailFile: file,./content.mp3; audio/mpeg; # Invalid thumbnailFile (not an image)
contentFile: file,./thumbnail.jpeg; image/jpeg; # Invalid contentFile (not an audio)
HTTP 400 # Bad Request
[Asserts]
jsonpath "$.message" == "Validation error occurred"
jsonpath "$.details.validationErrors.thumbnailFile" count == 1 # Invalid type for thumbnailFile
jsonpath "$.details.validationErrors.contentFile" count == 1 # Invalid type for contentFile

# 5. Invalid size of array fields (e.g., categories)
POST http://d130vf1311tsqd.cloudfront.net/podcast
[Multipart]
# Categories limit is 3
categories: Technology
categories: Technology
categories: Technology
categories: Technology
HTTP 400 # Bad Request
[Asserts]
jsonpath "$.message" == "Validation error occurred"
jsonpath "$.details.validationErrors.categories" count == 1 # Invalid size of categories array (max 3)

# 6. Invalid enum values (e.g., categories)
POST http://d130vf1311tsqd.cloudfront.net/podcast
[Multipart]
categories: InvalidCategory # Invalid category
HTTP 400 # Bad Request
[Asserts]
jsonpath "$.message" == "Validation error occurred"
jsonpath "$.details.validationErrors.categories" count == 1 # Invalid enum value for categories
