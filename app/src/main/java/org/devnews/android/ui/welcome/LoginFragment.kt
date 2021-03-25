package org.devnews.android.ui.welcome

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.material.snackbar.Snackbar
import org.devnews.android.BuildConfig
import org.devnews.android.DevNews
import org.devnews.android.R
import org.devnews.android.databinding.FragmentLoginBinding
import org.devnews.android.utils.TextChanged

class LoginFragment : Fragment(), View.OnClickListener {
    private lateinit var binding: FragmentLoginBinding
    private val viewModel: LoginViewModel by activityViewModels(factoryProducer = {
        (requireActivity().application as DevNews).container.loginViewModelFactory
    })

    private lateinit var gsiClient: GoogleSignInClient

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.viewmodel = viewModel

        // When login is clicked, login the user
        binding.loginButton.setOnClickListener {
            if (validate()) {
                viewModel.loginUser(requireContext())
            }
        }

        // When the user changes the contents of the username or password field, hide the error
        binding.loginUsername.editText?.addTextChangedListener(TextChanged { validateUsername() })
        binding.loginPassword.editText?.addTextChangedListener(TextChanged { validatePassword() })

        val context = requireContext()
        // Set-up Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestServerAuthCode(BuildConfig.OAUTH_CLIENT_ID)
            .requestIdToken(BuildConfig.OAUTH_CLIENT_ID)
            .requestEmail()
            .requestProfile()
            .build()
        gsiClient = GoogleSignIn.getClient(context, gso)

        binding.signInButton.setSize(SignInButton.SIZE_STANDARD)
        binding.signInButton.setOnClickListener(this)

        return binding.root
    }

    /**
     * Called when a click happens. For Google Sign-In.
     */
    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.sign_in_button -> {
                signInWithGoogle()
            }
        }
    }

    /**
     * Attempt to sign in the user via Google.
     */
    @Suppress("DEPRECATION")
    private fun signInWithGoogle() {
        val signInIntent = gsiClient.signInIntent

        // I know this is deprecated, but the Activity Result API looks like a whole world of pain
        // that I really don't have time to get into. Assume this uses registerActivityForResult.
        // Plus, the Google Sign-In docs still use this function so :^)
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    @Suppress("DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            RC_SIGN_IN -> {
                try {
                    val account = GoogleSignIn.getSignedInAccountFromIntent(data)
                        .getResult(ApiException::class.java)
                    // :^)
                    viewModel.loginWithGoogle(account!!)
                } catch (e: ApiException) {
                    // :^(
                    Log.e(TAG, "Got an API exception while trying to get the Google account!!! ${e.statusCode}")
                    Snackbar.make(binding.root, R.string.error_unknown, Snackbar.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun validate(): Boolean {
        validateUsername()
        validatePassword()
        return viewModel.isValid()
    }

    private fun validateUsername() {
        binding.loginUsername.error = viewModel.validateUsername().let {
            if (it == null)
                null
            else
                getString(it)
        }
    }

    private fun validatePassword() {
        binding.loginPassword.error = viewModel.validatePassword().let {
            if (it == null)
                null
            else
                getString(it)
        }
    }

    companion object {
        private const val RC_SIGN_IN = 1

        private const val TAG = "LoginFragment"

    }
}