package hr.fer.tel.gibalica.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import hr.fer.tel.gibalica.base.BaseFragment
import hr.fer.tel.gibalica.databinding.FragmentGuideBinding
import hr.fer.tel.gibalica.ui.MainActivity
import hr.fer.tel.gibalica.utils.SliderAdapter
import hr.fer.tel.gibalica.utils.invisible
import hr.fer.tel.gibalica.utils.visible
import hr.fer.tel.gibalica.viewModel.IntroViewModel
import timber.log.Timber

class GuideFragment : BaseFragment() {

    private var _binding: FragmentGuideBinding? = null
    private val binding: FragmentGuideBinding
        get() = _binding!!

    private val args: GuideFragmentArgs by navArgs()
    private val viewModel: IntroViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGuideBinding.inflate(inflater, container, false)
        Timber.d("Inflated!")
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupIllustrations()
        defineListener()
    }

    private fun setupIllustrations() {
        binding.imageSlider.setSliderAdapter(SliderAdapter(viewModel))
        binding.btnNext.setOnClickListener {
            if (args.initiatedFromIntroScreen) {
                startActivity(Intent(requireContext(), MainActivity::class.java))
            } else {
                activity?.onBackPressed()
            }
        }
    }

    private fun defineListener() {
        viewModel.nextButtonLiveData.observe(
            requireActivity(),
            {
                it?.let { isButtonVisible ->
                    binding.apply {
                        if (isButtonVisible) btnNext.visible()
                        else btnNext.invisible()
                    }
                }
            }
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
