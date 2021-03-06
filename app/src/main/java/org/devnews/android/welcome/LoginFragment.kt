package org.devnews.android.welcome

import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import com.google.android.material.textfield.TextInputLayout
import org.devnews.android.DevNews
import org.devnews.android.R
import org.devnews.android.databinding.FragmentLoginBinding
import org.devnews.android.utils.TextChanged

class LoginFragment : Fragment() {
    private lateinit var binding: FragmentLoginBinding
    private val viewModel: LoginViewModel by activityViewModels(factoryProducer = {
        (requireActivity().application as DevNews).container.loginViewModelFactory
    })

    // Used to provide the layout with an error message that is sourced from either a R.string
    // code, or an actual string (from the server).
    private val errorMessage = MutableLiveData("")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.viewmodel = viewModel
        binding.error = errorMessage

        // When login is clicked, login the user
        binding.loginButton.setOnClickListener {
            if (validate()) {
                viewModel.loginUser()
            }
        }

        // When error message or code is updated in the viewmodel, fetch it
        viewModel.errorCode.observe(viewLifecycleOwner) { updateErrorMessage() }
        viewModel.errorMessage.observe(viewLifecycleOwner) { updateErrorMessage() }

        // When the user changes the contents of the username or password field, hide the error
        binding.loginUsername.editText?.addTextChangedListener(TextChanged { validateUsername() })
        binding.loginPassword.editText?.addTextChangedListener(TextChanged { validatePassword() })

        return binding.root
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

    private fun updateErrorMessage() {
        errorMessage.value = when {
            viewModel.errorCode.value != 0 ->
                getString(viewModel.errorCode.value!!)
            viewModel.errorMessage.value != "" ->
                viewModel.errorMessage.value
            else -> ""
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = LoginFragment()
    }
}