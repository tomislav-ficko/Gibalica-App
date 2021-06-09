package hr.fer.tel.gibalica.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import hr.fer.tel.gibalica.R
import hr.fer.tel.gibalica.base.BaseFragment
import hr.fer.tel.gibalica.databinding.FragmentFinishBinding
import hr.fer.tel.gibalica.utils.DetectionUseCase
import hr.fer.tel.gibalica.utils.invisible
import hr.fer.tel.gibalica.utils.visible
import timber.log.Timber

class FinishFragment : BaseFragment() {

    companion object {
        private const val RESULT_THRESHOLD_GOOD = 0.75
        private const val RESULT_THRESHOLD_ACCEPTABLE = 0.4
    }

    private var _binding: FragmentFinishBinding? = null
    private val binding: FragmentFinishBinding
        get() = _binding!!

    private val args: FinishFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFinishBinding.inflate(inflater, container, false)
        Timber.d("Inflated!")
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setData()
    }

    override fun improveViewAccessability() {
        binding.apply {
            tvTitle.textSize = getAccessibleTitleTextSize()
            tvDescription.textSize = getAccessibleBodyTextSize()
            btnFinish.textSize = getAccessibleButtonTextSize()
        }
    }

    private fun setData() {
        binding.apply {
            when (args.detectionUseCase) {
                DetectionUseCase.TRAINING -> {
                    tvTitle.setText(R.string.finish_title_good)
                    tvDescription.setText(R.string.finish_description)
                    binding.ivEmoji.setImageResource(R.drawable.ic_emoji_great)
                    tvResult.invisible()
                }
                DetectionUseCase.COMPETITION, DetectionUseCase.DAY_NIGHT -> {
                    tvDescription.setText(R.string.finish_description_result)
                    tvResult.visible()
                    setDataAccordingToResult()
                }
            }
            btnFinish.setOnClickListener { returnToMainFragment() }
        }
    }

    private fun setDataAccordingToResult() {
        val result = try {
            args.correctPoses.div(args.totalPoses)
        } catch (e: ArithmeticException) {
            0
        }
        binding.apply {
            tvResult.text = getString(R.string.finish_result_format_specifier, args.correctPoses, args.totalPoses)
            when {
                result < RESULT_THRESHOLD_ACCEPTABLE -> {
                    binding.tvTitle.setText(R.string.finish_title_bad)
                    binding.ivEmoji.setImageResource(R.drawable.ic_emoji_bad)
                }
                result >= RESULT_THRESHOLD_ACCEPTABLE
                        && result < RESULT_THRESHOLD_GOOD -> {
                    binding.tvTitle.setText(R.string.finish_title_acceptable)
                    binding.ivEmoji.setImageResource(R.drawable.ic_emoji_good)
                }
                result >= RESULT_THRESHOLD_GOOD -> {
                    binding.tvTitle.setText(R.string.finish_title_good)
                    binding.ivEmoji.setImageResource(R.drawable.ic_emoji_great)
                }
            }
        }
    }

    private fun returnToMainFragment() {
        findNavController().navigate(
            FinishFragmentDirections.actionFinishFragmentToMainFragment()
        )
    }
}
