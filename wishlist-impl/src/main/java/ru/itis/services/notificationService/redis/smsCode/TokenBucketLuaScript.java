package ru.itis.services.notificationService.redis.smsCode;

import lombok.Getter;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

@Component
@Getter
public class TokenBucketLuaScript {

    private final DefaultRedisScript<Long> script;

    public TokenBucketLuaScript() {
        this.script = new DefaultRedisScript<>();

        this.script.setResultType(Long.class);
        this.script.setScriptText("""

                local cooldownKey = KEYS[1]
                local windowKey = KEYS[2]
                
                local now = tonumber(ARGV[1])
                
                -- limits
                local cooldown = tonumber(ARGV[2])
                local windowLimit = tonumber(ARGV[3])
                local windowTtl = tonumber(ARGV[4])
                
                -- 1. CHECK COOLDOWN (1 SMS per minute)
                if redis.call("EXISTS", cooldownKey) == 1 then
                    return 0
                end
                
                -- 2. CHECK WINDOW LIMIT (5 per 10 min)
                local count = tonumber(redis.call("GET", windowKey) or "0")
                
                if count >= windowLimit then
                    return 0
                end
                
                -- 3. APPLY CHANGES
                
                -- set cooldown
                redis.call("SET", cooldownKey, "1", "EX", cooldown)
                
                -- increment window counter
                redis.call("INCR", windowKey)
                
                -- ensure TTL for window
                redis.call("EXPIRE", windowKey, windowTtl)
                
                return 1
                """);
    }
}