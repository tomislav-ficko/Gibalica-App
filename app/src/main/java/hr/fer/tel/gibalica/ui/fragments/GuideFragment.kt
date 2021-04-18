package hr.fer.tel.gibalica.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import hr.fer.tel.gibalica.databinding.FragmentGuideBinding
import hr.fer.tel.gibalica.ui.MainActivity
import hr.fer.tel.gibalica.utils.SliderAdapter
import hr.fer.tel.gibalica.viewModel.IntroViewModel
import timber.log.Timber

class GuideFragment : Fragment() {

    private var _binding: FragmentGuideBinding? = null
    private val binding: FragmentGuideBinding
        get() = _binding!!

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
            startActivity(Intent(requireContext(), MainActivity::class.java))
        }
    }

    private fun defineListener() {
        viewModel.nextButtonLiveData.observe(requireActivity(), {
            it?.let { isButtonVisible ->
                binding.apply {
                    if (isButtonVisible) btnNext.visible()
                    else btnNext.invisible()
                }
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

fun View.visible() {
    visibility = View.VISIBLE
}

fun View.invisible() {
    visibility = View.INVISIBLE
}
