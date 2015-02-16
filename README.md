# leaderboard API
Return ordered list by value
Persist the results in a file called "leaderboard.txt"

# Get Leaderboard table
GET /test/Leaderboard

# Create/Update new row in table
POST /test/Leaderboard?name=moshe&value=25&color=red

# Delete row if exists
DELETE /test/Leaderboard?name=moshe

# Override table from persistence
PUT  /test/Leaderboard

