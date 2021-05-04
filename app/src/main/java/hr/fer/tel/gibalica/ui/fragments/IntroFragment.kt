package hr.fer.tel.gibalica.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import hr.fer.tel.gibalica.databinding.FragmentIntroBinding
import hr.fer.tel.gibalica.ui.MainActivity
import timber.log.Timber

class IntroFragment : Fragment() {

    private var _binding: FragmentIntroBinding? = null
    private val binding: FragmentIntroBinding
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentIntroBinding.inflate(inflater, container, false)
        Timber.d("Inflated!")
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            btnStartGuide.setOnClickListener { navigateToGuideFragment() }
            btnSkip.setOnClickListener {
                startActivity(Intent(requireContext(), MainActivity::class.java))
            }
        }
    }

    private fun navigateToGuideFragment() {
        findNavController().navigate(
            IntroFragmentDirections.actionIntroFragmentToGuideFragment()
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
