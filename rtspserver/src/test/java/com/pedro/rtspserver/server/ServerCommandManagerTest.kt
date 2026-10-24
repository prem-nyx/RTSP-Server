
package com.pedro.rtspserver.server

import android.util.Base64
import com.pedro.rtsp.rtsp.commands.Method
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class ServerCommandManagerTest {

    private lateinit var manager: ServerCommandManager
    private val username = "opencam-test"
    private val password = "test-password-123"

    @Before
    fun setUp() {
        manager = ServerCommandManager()
        manager.setAuth(username, password)
    }

    private fun authorizationHeader(user: String, pass: String): String {
        val credentials = "$user:$pass"
        val encoded = Base64.encodeToString(
            credentials.toByteArray(Charsets.UTF_8),
            Base64.NO_WRAP
        )
        return "Authorization: Basic $encoded\r\n"
    }

    private fun request(authHeader: String = ""): String {
        return "OPTIONS rtsp://127.0.0.1:8554/stream RTSP/1.0\r\n" +
            "CSeq: 1\r\n" +
            authHeader +
            "\r\n"
    }

    @Test
    fun missingCredentials_returnsUnauthorized() {
        val response = manager.createResponse(Method.OPTIONS, request(), 1, "127.0.0.1")
        assertTrue(response.contains("401 Unauthorized"))
    }

    @Test
    fun incorrectCredentials_returnsUnauthorized() {
        val response = manager.createResponse(
            Method.OPTIONS,
            request(authorizationHeader(username, "wrong-password")),
            1,
            "127.0.0.1"
        )
        assertTrue(response.contains("401 Unauthorized"))
    }

    @Test
    fun correctCredentials_allowOptions() {
        val response = manager.createResponse(
            Method.OPTIONS,
            request(authorizationHeader(username, password)),
            1,
            "127.0.0.1"
        )
        assertTrue(response.contains("200 OK"))
    }

    @Test
    fun malformedAuthorization_returnsUnauthorized() {
        val response = manager.createResponse(
            Method.OPTIONS,
            request("Authorization: Basic !!!\r\n"),
            1,
            "127.0.0.1"
        )
        assertTrue(response.contains("401 Unauthorized"))
    }

    @Test
    fun unknownMethod_returnsBadRequest() {
        val response = manager.createResponse(Method.UNKNOWN, request(), 1, "127.0.0.1")
        assertTrue(response.contains("400 Bad Request"))
    }

    @Test
    fun everySupportedMethod_requiresAuthentication() {
        val methods = listOf(
            Method.OPTIONS,
            Method.DESCRIBE,
            Method.SETUP,
            Method.PLAY,
            Method.PAUSE,
            Method.TEARDOWN
        )

        for (method in methods) {
            val response = manager.createResponse(method, request(), 1, "127.0.0.1")
            assertTrue(
                "$method should reject missing credentials",
                response.contains("401 Unauthorized")
            )
        }
    }


    @Test
    fun emptyServerCredentials_stillRequiresAuthentication() {
        manager = ServerCommandManager()
        manager.setAuth("", "")

        val response = manager.createResponse(
            Method.OPTIONS,
            request(),
            1,
            "127.0.0.1"
        )

        assertTrue(response.contains("401 Unauthorized"))
    }
}
