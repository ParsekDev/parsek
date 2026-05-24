package co.statu.parsek.util

import co.statu.parsek.config.ParsekConfig
import io.vertx.core.Handler
import io.vertx.ext.web.RoutingContext
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

/**
 * Centralized rate limit management for all API endpoints.
 *
 * Rate limit tiers are configured via the `rate-limit` section of the Parsek config. The "DEFAULT"
 * tier applies to every request under the router api-prefix. Plugins can register additional tiers
 * and map request paths to them via [registerTier] and [registerPathMatcher] (e.g. a stricter tier
 * for authentication endpoints).
 */
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
class RateLimitManager {

    companion object {
        private const val HEADER_LIMIT = "X-RateLimit-Limit"
        private const val HEADER_REMAINING = "X-RateLimit-Remaining"
        private const val HEADER_RETRY_AFTER = "Retry-After"

        private const val DEFAULT_TIER = "DEFAULT"

        private val logger = LoggerFactory.getLogger(RateLimitManager::class.java)
    }

    data class TierConfig(
        val name: String,
        val maxRequests: Int,
        val refillMs: Long
    )

    private val tiers = mutableMapOf<String, TierConfig>()
    private val limiters = mutableMapOf<String, RateLimiter>()

    /**
     * Path matchers that map URL path patterns to tier names.
     * Evaluated in registration order; first match wins.
     * The matcher receives the request path and returns true if it matches.
     */
    private val pathMatchers = mutableListOf<Pair<(String) -> Boolean, String>>()

    private var enabled = true

    /**
     * Header trusted to carry the real client IP when the app sits behind a reverse proxy. Only
     * this single header is honored (no spoofable fallback chain); when blank or absent the socket
     * peer address is used instead. Set it to the header your edge proxy *overwrites* (e.g.
     * "CF-Connecting-IP" for Cloudflare) and make sure the proxy strips client-supplied copies.
     */
    private var clientIpHeader: String = "CF-Connecting-IP"

    /**
     * Initializes rate limiting from the typed config. Falls back to a sane DEFAULT tier when the
     * config does not declare one.
     */
    fun init(config: ParsekConfig) {
        val rateLimitConfig = config.rateLimit

        enabled = rateLimitConfig.enabled
        clientIpHeader = rateLimitConfig.clientIpHeader

        if (!enabled) {
            logger.info("Rate limiting is disabled via config")
            return
        }

        rateLimitConfig.tiers.forEach { (tierName, tier) ->
            registerTier(TierConfig(tierName, tier.maxRequests, tier.refillMs))
        }

        if (!tiers.containsKey(DEFAULT_TIER)) {
            registerTier(TierConfig(DEFAULT_TIER, maxRequests = 300, refillMs = 200))
        }

        logger.info("Rate limiting initialized with ${tiers.size} tier(s): ${tiers.keys.joinToString(", ")}")
    }

    /**
     * Register a new rate limit tier. If a tier with the same name already exists, it is replaced.
     */
    fun registerTier(config: TierConfig) {
        tiers[config.name] = config
        limiters[config.name] = RateLimiter(
            maxTokens = config.maxRequests,
            refillRateMs = config.refillMs
        )
    }

    /**
     * Register a path matcher that maps matching request paths to a specific tier.
     * Matchers are evaluated in registration order; first match wins.
     *
     * Example:
     * ```
     * rateLimitManager.registerPathMatcher("AUTH") { path -> path.startsWith("/api/auth/") }
     * ```
     */
    fun registerPathMatcher(tierName: String, matcher: (String) -> Boolean) {
        pathMatchers.add(matcher to tierName)
    }

    /**
     * Determines the rate limit tier for a given request path.
     * First checks registered path matchers, then falls back to the default tier.
     */
    fun getTierForPath(path: String): String {
        for ((matcher, tierName) in pathMatchers) {
            if (matcher(path) && tiers.containsKey(tierName)) {
                return tierName
            }
        }
        return DEFAULT_TIER
    }

    fun getClientIp(context: RoutingContext): String {
        val request = context.request()

        // Trust only the single configured proxy header; never the spoofable XFF/X-Real-IP chain.
        if (clientIpHeader.isNotBlank()) {
            request.getHeader(clientIpHeader)?.substringBefore(",")?.trim()?.takeIf { it.isNotBlank() }
                ?.let { return it }
        }

        return request.remoteAddress()?.host() ?: "unknown"
    }

    fun isAllowed(clientIp: String, tierName: String): Boolean {
        val limiter = limiters[tierName] ?: return true
        return limiter.tryAcquire(clientIp)
    }

    fun remaining(clientIp: String, tierName: String): Int {
        val limiter = limiters[tierName] ?: return 0
        return limiter.remainingTokens(clientIp)
    }

    fun retryAfter(clientIp: String, tierName: String): Int {
        val limiter = limiters[tierName] ?: return 0
        return limiter.retryAfterSeconds(clientIp)
    }

    fun getTierConfig(tierName: String): TierConfig? = tiers[tierName]

    /**
     * Creates a Vert.x route handler that applies rate limiting to all requests under [apiPrefix].
     * This handler should be added to the router before any route-specific handlers.
     */
    fun createHandler(apiPrefix: String): Handler<RoutingContext> {
        return Handler { context ->
            if (!enabled) {
                context.next()
                return@Handler
            }

            val path = context.request().path() ?: ""

            if (!path.startsWith(apiPrefix)) {
                context.next()
                return@Handler
            }

            if (context.request().method().name() == "OPTIONS") {
                context.next()
                return@Handler
            }

            val clientIp = getClientIp(context)

            val tierName = getTierForPath(path)
            val tierConfig = tiers[tierName]

            if (tierConfig == null) {
                context.next()
                return@Handler
            }

            if (isAllowed(clientIp, tierName)) {
                context.response()
                    .putHeader(HEADER_LIMIT, tierConfig.maxRequests.toString())
                    .putHeader(HEADER_REMAINING, remaining(clientIp, tierName).toString())

                context.next()
            } else {
                val retryAfterVal = retryAfter(clientIp, tierName)

                val responseBody =
                    "{\"result\":\"error\",\"error\":\"RATE_LIMIT_EXCEEDED\",\"retryAfter\":$retryAfterVal}"

                context.response()
                    .putHeader(HEADER_LIMIT, tierConfig.maxRequests.toString())
                    .putHeader(HEADER_REMAINING, "0")
                    .putHeader(HEADER_RETRY_AFTER, retryAfterVal.toString())
                    .putHeader("content-type", "application/json; charset=utf-8")
                    .setStatusCode(429)
                    .setStatusMessage("Too Many Requests")
                    .end(responseBody)
            }
        }
    }
}
