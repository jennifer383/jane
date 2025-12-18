-- chathistorytitle.lua
-- 用于批量获取用户历史会话标题（JSON字符串），避免多次 Java 调用 Redis
-- 脚本输入：
--   KEYS[1]：公共 key 前缀，比如 "JANE_CONVERSATION_TITLE_USER_PREFIX:userId:"
--   ARGV[i]：一组会话 ID，比如 "1", "2", "3"
-- 输出：
--   一个包含对应 Redis key 中 value 的字符串数组（可以为 nil）

-- 获取传入的公共前缀
local prefix = KEYS[1]

-- 初始化一个空表用于存储结果
local result = {}

-- 遍历 ARGV 中的所有会话 ID
for i = 1, #ARGV do
    -- 拼接完整的 Redis key：前缀 + 会话ID
    local key = prefix .. ARGV[i]

    -- 从 Redis 获取该 key 对应的 value（JSON字符串或 nil）
    local value = redis.call("GET", key)

    -- 将 value 插入结果数组中（即使为 nil 也插入）
    table.insert(result, value)
end

-- 返回包含所有 value 的数组
return result
