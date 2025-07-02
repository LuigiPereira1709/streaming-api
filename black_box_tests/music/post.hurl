# Test for posting a valid music data
POST http://d130vf1311tsqd.cloudfront.net/music
[Multipart]
title: Music
year: 2003
explicit: true 
thumbnailFile: file,./thumbnail.jpeg; image/jpeg;
contentFile: file,./content.mp3; audio/mpeg;
artist: Artist
feats: Feat 1
feats: Feat 2
album: Album
genre: Rock
moods: Happy
moods: Sad
HTTP 200 # Music created successfully
[Asserts]
# Check the response body
jsonpath "$.id" exists
jsonpath "$.title" == "Music"
jsonpath "$.thumbnailUrl" exists
jsonpath "$.year" == 2003
jsonpath "$.explicit" == true
jsonpath "$.artist" == "Artist"
jsonpath "$.feats" contains "Feat 1" 
jsonpath "$.feats" contains "Feat 2"
jsonpath "$.album" == "Album"
jsonpath "$.genre" == "Rock"
jsonpath "$.moods" contains "Happy"
jsonpath "$.moods" contains "Sad"

# Test for posting a invalid music data
# 1. Missing fields (e.g., title, artist, genre, thumbnailFile, contentFile)
POST http://d130vf1311tsqd.cloudfront.net/music
[Multipart]
year: 2003
explicit: true
feats: Feat 1
feats: Feat 2
album: Album
moods: Happy
moods: Sad
HTTP 400 # Bad Request
[Asserts]
jsonpath "$.message" == "Validation error occurred"
jsonpath "$.details.validationErrors.title" count == 2 # When missing title, there are 2 validation errors: non-null and non-emtpy 
jsonpath "$.details.validationErrors.artist" count == 2 # When missing artist, there is 2 validation error: non-nul and non-empty
jsonpath "$.details.validationErrors.genre" count == 2 # When missing genre, there is 2 validation error: non-null and non-empty
jsonpath "$.details.validationErrors.thumbnailFile" count == 1 # When missing thumbnailFile, there is 1 validation error: non-null
jsonpath "$.details.validationErrors.contentFile" count == 1 # When missing contentFile, there is 1 validation error: non-null

# 2. Invalid range for year (e.g., year < 1900 or year > current year)
POST http://d130vf1311tsqd.cloudfront.net/music
[Multipart]
year: 42 # Invalid year
HTTP 400 # Bad Request
[Asserts]
jsonpath "$.message" == "Validation error occurred"
jsonpath "$.details.validationErrors.year" count == 1 # Invalid year

# 3. Invalid type of files (e.g., thumbnailFile is not an image, contentFile is not audio) 
# Note: see the api documentation for valid file types
POST http://d130vf1311tsqd.cloudfront.net/music
[Multipart]
thumbnailFile: file,./content.mp3; audio/mpeg; # Invalid type for thumbnailFile
contentFile: file,./thumbnail.jpeg; image/jpeg; # Invalid type for contentFile
HTTP 400 # Bad Request
[Asserts]
jsonpath "$.message" == "Validation error occurred"
jsonpath "$.details.validationErrors.thumbnailFile" count == 1 # Invalid type for thumbnailFile 
jsonpath "$.details.validationErrors.contentFile" count == 1 # Invalid type for contentFile 

# 4. Invalid size of array fields (e.g., moods)
POST http://d130vf1311tsqd.cloudfront.net/music
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

# 5. Invalid enum values (e.g., genre, moods)
POST http://d130vf1311tsqd.cloudfront.net/music
[Multipart]
genre: InvalidGenre
moods: InvalidMood
HTTP 400 # Bad Request
[Asserts]
jsonpath "$.message" == "Validation error occurred"
jsonpath "$.details.validationErrors.genre" count == 1 # Invalid genre
jsonpath "$.details.validationErrors.moods" count == 1 # Invalid mood
