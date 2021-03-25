package org.devnews.android.ui.welcome

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import org.devnews.android.DevNews
import org.devnews.android.databinding.FragmentRegisterBinding
import org.devnews.android.utils.TextChanged

class RegisterFragment : Fragment() {
    private val viewModel: RegisterViewModel by activityViewModels {
        (requireActivity().application as DevNews).container.registerViewModelFactory
    }
    private lateinit var binding: FragmentRegisterBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRegisterBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.viewmodel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Attach validation listeners
        binding.registerUsername.apply {
            editText!!.addTextChangedListener(TextChanged {
                error = viewModel.validateUsername(context)
            })
        }
        binding.registerEmail.apply {
            editText!!.addTextChangedListener(TextChanged {
                error = viewModel.validateEmail(context)
            })
        }
        binding.registerPassword.apply {
            editText!!.addTextChangedListener(TextChanged {
                error = viewModel.validatePassword(context)
            })
        }
        binding.registerVerify.apply {
            editText!!.addTextChangedListener(TextChanged {
                error = viewModel.validateVerify(context)
            })
        }

        // When the user clicks the button, validate everything and then call register
        binding.registerButton.setOnClickListener {
            val context = requireContext()
            binding.registerUsername.error = viewModel.validateUsername(context)
            binding.registerEmail.error = viewModel.validateEmail(context, full = true)
            binding.registerPassword.error = viewModel.validatePassword(context, full = true)
            binding.registerVerify.error = viewModel.validateVerify(context)

            viewModel.register(context)
        }
    }

    companion object
}