package org.devnews.android.ui.welcome

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import org.devnews.android.databinding.FragmentWelcomeBinding

class WelcomeFragment : Fragment() {
    private lateinit var binding: FragmentWelcomeBinding

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        binding = FragmentWelcomeBinding.inflate(inflater, container, false)

        // Login button -> login fragment
        binding.welcomeLogin.setOnClickListener {
            val action = WelcomeFragmentDirections.actionWelcomeToLogin()
            findNavController().navigate(action)
        }
        // Register button -> register fragment
        binding.welcomeRegister.setOnClickListener {
            val action = WelcomeFragmentDirections.actionWelcomeToRegister()
            findNavController().navigate(action)
        }

        return binding.root
    }

    companion object
}