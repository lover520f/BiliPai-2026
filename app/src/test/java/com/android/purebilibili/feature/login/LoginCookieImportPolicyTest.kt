package com.android.purebilibili.feature.login

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class LoginCookieImportPolicyTest {

    @Test
    fun `parses session cookie and optional account values`() {
        val cookies = parseLoginCookieHeader(
            "SESSDATA=session%2Cvalue; bili_jct=csrf; DedeUserID=42; buvid3=device"
        )

        requireNotNull(cookies)
        assertEquals("session%2Cvalue", cookies.sessData)
        assertEquals("csrf", cookies.csrf)
        assertEquals("42", cookies.dedeUserId)
        assertEquals("device", cookies.buvid3)
        assertEquals(
            "SESSDATA=session%2Cvalue; bili_jct=csrf; DedeUserID=42; buvid3=device",
            cookies.toCookieHeader()
        )
    }

    @Test
    fun `rejects cookie input without session cookie`() {
        assertNull(parseLoginCookieHeader("bili_jct=csrf; DedeUserID=42"))
    }
}
