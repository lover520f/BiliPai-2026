package com.android.purebilibili.feature.login

internal data class ImportedLoginCookies(
    val sessData: String,
    val csrf: String?,
    val buvid3: String?,
    val dedeUserId: String?
) {
    fun toCookieHeader(): String = buildList {
        add("SESSDATA=$sessData")
        csrf?.takeIf { it.isNotBlank() }?.let { add("bili_jct=$it") }
        dedeUserId?.takeIf { it.isNotBlank() }?.let { add("DedeUserID=$it") }
        buvid3?.takeIf { it.isNotBlank() }?.let { add("buvid3=$it") }
    }.joinToString(separator = "; ")
}

internal fun parseLoginCookieHeader(rawCookieHeader: String): ImportedLoginCookies? {
    val values = rawCookieHeader
        .lineSequence()
        .flatMap { it.split(';').asSequence() }
        .map { it.trim() }
        .mapNotNull { segment ->
            val separator = segment.indexOf('=')
            if (separator <= 0) null else {
                segment.substring(0, separator).trim() to segment.substring(separator + 1).trim()
            }
        }
        .toMap()

    val sessData = values["SESSDATA"].orEmpty()
    if (sessData.isBlank()) return null

    return ImportedLoginCookies(
        sessData = sessData,
        csrf = values["bili_jct"],
        buvid3 = values["buvid3"],
        dedeUserId = values["DedeUserID"]
    )
}
