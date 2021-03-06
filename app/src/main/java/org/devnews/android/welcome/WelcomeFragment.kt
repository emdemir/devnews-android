package org.devnews.android.welcome

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import org.devnews.android.databinding.FragmentWelcomeBinding
import java.io.Serializable

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

        return binding.root
    }

    companion object {
        @JvmStatic
        fun newInstance() = WelcomeFragment()
    }
}