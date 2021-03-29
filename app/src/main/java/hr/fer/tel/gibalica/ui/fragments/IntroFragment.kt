package hr.fer.tel.gibalica.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import hr.fer.tel.gibalica.databinding.FragmentIntroBinding
import hr.fer.tel.gibalica.viewModel.NavigationViewModel

class IntroFragment : Fragment() {

    private val navigationViewModel: NavigationViewModel by activityViewModels()

    private var _binding: FragmentIntroBinding? = null
    private val binding: FragmentIntroBinding
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentIntroBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            btnStartGuide.setOnClickListener {
                navigationViewModel.navigate(IntroFragmentDirections.actionIntroFragmentToGuideFragment())
            }
            btnSkip.setOnClickListener {
//                navigationViewModel.navigate(IntroFragmentDirections.actionIntroFragmentToMainActivity())
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
