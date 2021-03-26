package org.devnews.android.ui.home

import android.accounts.Account
import android.accounts.AccountManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.*
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.auth0.android.jwt.JWT
import com.google.android.material.navigation.NavigationView
import com.squareup.picasso.Picasso
import org.devnews.android.R
import org.devnews.android.account.DevNewsAuthenticator
import org.devnews.android.account.getAccountDetails
import org.devnews.android.account.updateAccount
import org.devnews.android.base.Activity
import org.devnews.android.databinding.ActivityMainBinding
import org.devnews.android.ui.home.messages.MessageListFragment

class HomeActivity : Activity() {
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Setup content view using binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()

        // Setup drawer navigation, using the Navigation androidX library
        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.main_nav_host) as NavHostFragment
        navController = navHostFragment.navController

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.nav_home, R.id.nav_recent, R.id.nav_newest_comments, R.id.nav_message_list),
            drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        // Load the user's details from the identity token, and fill up the sidebar with information
        loadUserDetails()
    }

    private fun loadUserDetails() {
        val obtainDetailsTask = Thread {
            // Fetch the Identity Token
            val accountDetails = getAccountDetails(this)
            val accountName = accountDetails.getString(AccountManager.KEY_ACCOUNT_NAME)
            val accountType = accountDetails.getString(AccountManager.KEY_ACCOUNT_TYPE)
            val identityToken = accountDetails.getString(AccountManager.KEY_AUTHTOKEN)
                ?: return@Thread
            val jwt = JWT(identityToken)

            // XXX: This is here because there really isn't any better place to put this.
            // Here's the verbose explanation: If we fix the username from the Authenticator, it
            // usually causes an issue during first login, because this function and the HomeFragment
            // loading the stories will cause a token to be obtained for googleuser twice,
            // and it will not be able to find googleuser the second time and will pop up a Re-enter
            // credentials window. Because that window is not escapable (TokenInterceptor's lock
            // waits on a response from AccountManager), we've now deadlocked ourselves.

            // Check if the username of the account that's saved on the device matches
            // the one that was sent back to us.
            if (jwt.subject != accountName) {
                val account = Account(accountName, accountType)
                updateAccount(
                    this,
                    jwt.subject!!,
                    AccountManager.get(this).getPassword(account),
                    initialUsername = account.name
                )
            }

            // Fill in the sidebar details
            runOnUiThread {
                val username: TextView = findViewById(R.id.user_username)
                val email: TextView = findViewById(R.id.user_email)
                val avatar: ImageView = findViewById(R.id.user_avatar)

                username.text = jwt.subject
                email.text = jwt.getClaim("email").asString()
                Picasso.get().load(jwt.getClaim("avatar").asString()).resize(128, 128).into(avatar)
            }
        }

        obtainDetailsTask.start()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent == null) return

        // If we were asked to start the message composer, navigate to the messages fragment and tell
        // it to start the composer.
        if (intent.getBooleanExtra(ARG_COMPOSING, false)) {
            val username = intent.getStringExtra(ARG_COMPOSE_TARGET) ?: throw IllegalStateException(
                "Did not pass username with composing intent!"
            )

            navController.navigate(
                R.id.nav_message_list, bundleOf(
                    MessageListFragment.ARG_COMPOSING to true,
                    MessageListFragment.ARG_COMPOSE_TARGET to username
                ), navOptions {
                    popUpTo(R.id.nav_home) { inclusive = true }
                    launchSingleTop = true
                }
            )
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // The base Activity class we use has its own idea of managing the back button which we
        // don't want.
        return false
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.main_nav_host)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    companion object {
        const val ARG_COMPOSING = "COMPOSING"
        const val ARG_COMPOSE_TARGET = "COMPOSE_TARGET"

        /**
         * Launch home and navigate to the messages fragment, with a username to compose a message
         * to.
         *
         * @param context Activity context
         * @param username The username of the user to compose a message to
         */
        fun launchMessageComposer(context: Context, username: String) {
            context.startActivity(Intent(context, HomeActivity::class.java).apply {
                putExtra(ARG_COMPOSING, true)
                putExtra(ARG_COMPOSE_TARGET, username)
                // The way we reach this point is:
                // home -> story/comment -> user -> home (MessageListFragment).
                // Thsi c
            })
        }
    }
}