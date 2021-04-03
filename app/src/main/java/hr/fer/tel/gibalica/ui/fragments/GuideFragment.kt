package hr.fer.tel.gibalica.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.NavHostFragment
import hr.fer.tel.gibalica.databinding.FragmentGuideBinding
import hr.fer.tel.gibalica.utils.SliderAdapter
import hr.fer.tel.gibalica.viewModel.IntroViewModel
import hr.fer.tel.gibalica.viewModel.NavigationViewModel

class GuideFragment : NavHostFragment() {

    private var _binding: FragmentGuideBinding? = null
    private val binding: FragmentGuideBinding
        get() = _binding!!

    private val introViewModel: IntroViewModel by viewModels()
    private val navigationViewModel: NavigationViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGuideBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.imageSlider.setSliderAdapter(SliderAdapter(introViewModel))
        binding.btnNext.setOnClickListener {
//            navigationViewModel.navigate(IntroFragmentDirections.actionIntroFragmentToMainActivity())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
