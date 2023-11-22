-- Sliding WindowLog Rate Limiter
if #ARGV < 3 then
    return redis.error_reply("Stream key or Time Frame or Permits not provided")
end

-- Extract the parameters from the arguments
local streamKey = ARGV[1]
local timeFrameMilliseconds = tonumber(ARGV[2]) * 1000
local permits = tonumber(ARGV[3])

if timeFrameMilliseconds <= 0 or permits <= 0 then
    return redis.error_reply("Invalid time frame or permits")
end

-- Get the current time in seconds and microseconds
local time = redis.call('TIME')

-- Convert seconds and microseconds to milliseconds
local currentTimeMilliseconds = tonumber(time[1]) * 1000 + math.floor(tonumber(time[2]) / 1000)
local minIdToKeep = currentTimeMilliseconds - timeFrameMilliseconds

-- Append currentTimeMilliseconds to the stream
local maxLength = permits * 2
local messageId = redis.call('XADD', streamKey, 'MAXLEN', maxLength, '*', 'currentTimeMilliseconds', currentTimeMilliseconds)

-- Trim the stream
local trimmed = redis.call('XTRIM', streamKey, 'MINID', minIdToKeep)

local length = redis.call('XLEN', streamKey)

local remain = 0
if(length <= permits) then
    remain = permits - length;
end

-- Return boolean of the request is permitted by comparing the length of stream and permits
return tostring(length <= permits) .. "," .. tostring(remain)
