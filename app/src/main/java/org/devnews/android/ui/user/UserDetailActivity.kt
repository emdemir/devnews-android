package org.devnews.android.ui.user

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.squareup.picasso.Picasso
import org.devnews.android.DevNews
import org.devnews.android.R
import org.devnews.android.base.Activity
import org.devnews.android.databinding.ActivityUserDetailBinding
import org.devnews.android.repository.objects.User
import org.devnews.android.ui.home.HomeActivity
import org.devnews.android.utils.setProgressState
import java.lang.IllegalStateException

class UserDetailActivity : Activity() {
    private val viewModel: UserDetailViewModel by viewModels {
        (application as DevNews).container.userDetailViewModelFactory
    }
    private lateinit var binding: ActivityUserDetailBinding
    private lateinit var username: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Fetch the username from intent args.
        username = intent.getStringExtra(ARG_USERNAME)
            ?: throw IllegalStateException("UserDetailActivity was not sent the username!")

        // Setup view binding
        binding = ActivityUserDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar(true)

        // --- User Details Setup ---

        // When the user is updated, update the binding as well.
        viewModel.user.observe(this) {
            if (it == null) return@observe

            binding.user = it
            // Load the profile picture of the avatar
            Picasso.get().load(it.avatarImage).resize(80, 80).into(binding.userAvatar)
        }

        viewModel.loading.observe(this) {
            setProgressState(
                it,
                viewModel.error.value,
                binding.progress,
                binding.userDetailsContainer
            )
        }

        viewModel.error.observe(this) {
            binding.error = it
            // Conditionally display the options menu as well
            invalidateOptionsMenu()

            if (it != null) {
                // Hide user details
                binding.userDetailsContainer.visibility = GONE
            }
        }

        // When the activity is created, load the user data.
        lifecycleScope.launchWhenCreated {
            viewModel.setUsername(username)
            viewModel.loadUser(this@UserDetailActivity)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if (viewModel.error.value == null) {
            menuInflater.inflate(R.menu.user_details, menu)
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.send_message -> {
                HomeActivity.launchMessageComposer(this, username)
            }
        }

        return super.onOptionsItemSelected(item)
    }

    companion object {
        private const val TAG = "UserDetailActivity"
        const val ARG_USERNAME = "USERNAME"

        /**
         * Launch UserDetailActivity for the given user.
         *
         * @param context Activity context
         * @param username The username of the user
         */
        fun launchUserDetails(context: Context, username: String) {
            val intent = Intent(context, UserDetailActivity::class.java)
            intent.putExtra(ARG_USERNAME, username)
            context.startActivity(intent)
        }
    }
}