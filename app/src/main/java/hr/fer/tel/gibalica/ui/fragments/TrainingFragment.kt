package hr.fer.tel.gibalica.ui.fragments

import android.graphics.SurfaceTexture
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import hr.fer.tel.gibalica.R
import hr.fer.tel.gibalica.base.BaseDetectionFragment
import hr.fer.tel.gibalica.base.REQUEST_CODE_PERMISSIONS
import hr.fer.tel.gibalica.databinding.FragmentTrainingBinding
import hr.fer.tel.gibalica.utils.PoseDetectionEvent
import hr.fer.tel.gibalica.utils.invisible
import hr.fer.tel.gibalica.utils.visible
import hr.fer.tel.gibalica.viewModel.MainViewModel
import timber.log.Timber

class TrainingFragment : BaseDetectionFragment() {

    private var _binding: FragmentTrainingBinding? = null
    private val binding: FragmentTrainingBinding
        get() = _binding!!
    private val args: TrainingFragmentArgs by navArgs()
    private val viewModel by viewModels<MainViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTrainingBinding.inflate(inflater, container, false)
        Timber.d("Inflated!")
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeAndStartCamera(binding.txvViewFinder, viewModel)
        defineObserver()
        setupOverlayViews()

        val trainingType = args.trainingType
        Timber.d("Starting training for type ${trainingType.name}")
        viewModel.initializeTraining(trainingType)
        viewModel.startTraining()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (permissionsGranted())
                initializeAndStartCamera(binding.txvViewFinder, viewModel)
            else {
                showErrorToast()
                returnToMainFragment()
            }
        }
    }

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
        initializeAndStartCamera(binding.txvViewFinder, viewModel)
    }

    private fun defineObserver() {
        viewModel.detectionResultLiveData.observe(
            requireActivity(),
            {
                it?.let { event ->
                    when (event) {
                        PoseDetectionEvent.NOT_DETECTED -> {
                            hideMessage()
                            showResponse(R.string.response_negative)
                        }
                        PoseDetectionEvent.INITIAL_POSE_DETECTED -> {
                            showMessage(R.string.message_start)
                        }
                        PoseDetectionEvent.STARTING_POSE_DETECTED -> {
                            viewModel.poseToBeDetectedMessage?.let { resId ->
                                showMessage(resId)
                            }
                        }
                        PoseDetectionEvent.WANTED_POSE_DETECTED -> {
                            hideMessage()
                            showResponse(R.string.response_positive)
                        }
                        PoseDetectionEvent.UPDATE_MESSAGE -> {
                            viewModel.poseToBeDetectedMessage?.let { resId ->
                                showMessage(resId)
                            }
                            hideResponse()
                        }
                        PoseDetectionEvent.FINISH_DETECTION -> {
                            Timber.d("Finishing training.")
                            findNavController().navigate(R.id.action_trainingFragment_to_finishFragment)
                        }
                        PoseDetectionEvent.HIDE_RESPONSE -> {
                            hideResponse()
                        }
                    }
                }
            }
        )
    }

    private fun setupOverlayViews() {
        showMessage(R.string.message_initial)
        hideResponse()
        binding.btnClose.setOnClickListener { returnToMainFragment() }
    }

    private fun returnToMainFragment() {
        findNavController().navigate(R.id.action_trainingFragment_to_mainFragment)
    }

    private fun showResponse(resId: Int) = binding.apply {
        tvResponse.setText(resId)
        cvResponse.visible()
    }

    private fun hideResponse() = binding.apply {
        cvResponse.invisible()
    }

    private fun showMessage(resId: Int) = binding.apply {
        tvMessage.setText(resId)
        tvMessage.visible()
    }

    private fun hideMessage() = binding.apply {
        tvMessage.invisible()
    }
}
