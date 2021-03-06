package org.devnews.android.ui.welcome

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import org.devnews.android.DevNews
import org.devnews.android.R
import org.devnews.android.account.DevNewsAuthenticator
import org.devnews.android.account.addAccount
import org.devnews.android.account.hasAccount
import org.devnews.android.base.Activity
import org.devnews.android.databinding.ActivityWelcomeBinding
import org.devnews.android.ui.home.HomeActivity

class WelcomeActivity : Activity() {
    private lateinit var binding: ActivityWelcomeBinding
    private lateinit var appBarConfiguration: AppBarConfiguration

    // Subscribe to these two to know when the user has successfully authenticated (either by logging
    // in, or by registering).
    private val loginViewModel: LoginViewModel by viewModels(factoryProducer = {
        (application as DevNews).container.loginViewModelFactory
    })
    private val registerViewModel: RegisterViewModel by viewModels(factoryProducer = {
        (application as DevNews).container.registerViewModelFactory
    })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // If the user has already logged in, then there's no need to welcome them.
        if (hasAccount(this)) {
            Log.d(TAG, "User already has an account on device, sending off to home")
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }

        // Setup content view using binding
        binding = ActivityWelcomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()

        // Setup Android Navigation
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.login_nav_host) as NavHostFragment
        val navController = navHostFragment.navController

        appBarConfiguration = AppBarConfiguration(setOf(R.id.nav_welcome))
        setupActionBarWithNavController(navController, appBarConfiguration)

        // If the user logged in successfully, then continue onto the home fragment.
        loginViewModel.loggedIn.observe(this) {
            if (!it) return@observe
            addAccountAndGoHome(
                loginViewModel.username.value!!,
                loginViewModel.password.value!!,
                loginViewModel.token.value,
                loginViewModel.source.value!!
            )
        }

        // If the user registered successfully, then continue onto the home fragment.
        registerViewModel.registered.observe(this) {
            if (!it) return@observe
            addAccountAndGoHome(
                registerViewModel.username.value!!,
                registerViewModel.password.value!!,
                registerViewModel.token.value,
                DevNewsAuthenticator.ACCOUNT_SOURCE_LOCAL
            )
        }
    }

    /**
     * Save the account with the given parameters, and navigate to the home activity, finishing
     * this one.
     *
     * @param username The username of the account to be added
     * @param password The password of the account to be added
     * @param token If a token was obtained, save it with the account to avoid a round-trip
     * @param source The authentication source
     */
    private fun addAccountAndGoHome(username: String, password: String, token: String?, source: String) {
        addAccount(
            this,
            username,
            password,
            source = source,
            token = token
        )

        Log.d(TAG, "Authentication complete! Sending user to home")
        val sendOff = Intent(this, HomeActivity::class.java)
        startActivity(sendOff)
        finish()
    }

    override fun onResume() {
        super.onResume()

        // While testing I found that after completing the "Add Account" dialog that can be triggered
        // by Settings > Accounts I can drop back to this activity. So let's check whether we now have
        // an active account when we're back to this activity.
        if (hasAccount(this)) {
            Log.d(TAG, "Dropped back here, but already have an account; sending to home")
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }
    }

    override fun onBackPressed() {
        if (loginViewModel.loading.value == true)
            return
        if (registerViewModel.loading.value == true)
            return

        super.onBackPressed()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // The base Activity class we use has its own idea of managing the back button which we
        // don't want.
        return false
    }

    override fun onSupportNavigateUp(): Boolean {
        if (loginViewModel.loading.value == true)
            return false

        val navController = findNavController(R.id.login_nav_host)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    companion object {
        private const val TAG = "WelcomeActivity"
    }
}