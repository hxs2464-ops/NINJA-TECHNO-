package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.backend.AuthResult
import com.example.backend.GroupResult
import com.example.backend.NinjaBackend
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class NinjaBackendTest {

    private lateinit var backend: NinjaBackend

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        // Clear prefs for isolated tests
        context.getSharedPreferences("ninja_techno_db", Context.MODE_PRIVATE).edit().clear().commit()
        backend = NinjaBackend(context)
    }

    @Test
    fun testRegistrationValidationAndSuccess() = runBlocking {
        // Short username
        val shortRes = backend.register("ab", "pass123", "pass123")
        assertTrue(shortRes is AuthResult.Error)

        // Passwords mismatch
        val mismatchRes = backend.register("operator1", "pass123", "pass456")
        assertTrue(mismatchRes is AuthResult.Error)

        // Successful registration
        val successRes = backend.register("operator1", "secret123", "secret123")
        assertTrue(successRes is AuthResult.Success)
        val user = (successRes as AuthResult.Success).user
        assertEquals("operator1", user.username)

        // Duplicate username rejected
        val dupRes = backend.register("operator1", "otherpass", "otherpass")
        assertTrue(dupRes is AuthResult.Error)
    }

    @Test
    fun testLoginAndSessionPersistence() = runBlocking {
        backend.register("ghost_recon", "password123", "password123")
        backend.logout()

        // Wrong password
        val wrongLogin = backend.login("ghost_recon", "wrongpass")
        assertTrue(wrongLogin is AuthResult.Error)

        // Correct password
        val correctLogin = backend.login("ghost_recon", "password123")
        assertTrue(correctLogin is AuthResult.Success)
        assertNotNull(backend.currentUser.value)
    }

    @Test
    fun testCreateAndJoinGroup() = runBlocking {
        val auth = backend.register("squad_leader", "securePass12", "securePass12")
        assertTrue(auth is AuthResult.Success)

        val createRes = backend.createGroup("Echo Tactical", "Recon mission", false)
        assertTrue(createRes is GroupResult.Success)
        val group = (createRes as GroupResult.Success).group
        assertEquals("Echo Tactical", group.name)
        assertNotNull(group.code)

        // Join by code
        val joinRes = backend.joinGroupByCode(group.code)
        assertTrue(joinRes is GroupResult.Success)
    }
}
