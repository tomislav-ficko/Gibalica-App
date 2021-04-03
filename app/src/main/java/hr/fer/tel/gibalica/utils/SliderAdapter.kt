package hr.fer.tel.gibalica.utils

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.smarteist.autoimageslider.SliderViewAdapter
import hr.fer.tel.gibalica.R
import hr.fer.tel.gibalica.databinding.ItemLayoutImageSliderBinding
import hr.fer.tel.gibalica.viewModel.IntroViewModel

class SliderAdapter(
    private val viewModel: IntroViewModel
) : SliderViewAdapter<SliderAdapter.SliderViewHolder>() {

    class SliderViewHolder(
        itemView: View
    ) : SliderViewAdapter.ViewHolder(itemView) {

        val binding = ItemLayoutImageSliderBinding.bind(itemView)
    }

    override fun onCreateViewHolder(parent: ViewGroup?): SliderViewHolder {
        val inflatedView = LayoutInflater
            .from(parent?.context)
            .inflate(R.layout.item_layout_image_slider, parent, false)
        return SliderViewHolder(inflatedView)
    }

    override fun onBindViewHolder(viewHolder: SliderViewHolder?, position: Int) {
        viewHolder?.apply {
            when (position) {
                0 -> binding.ivGuideImage.setImageResource(R.drawable.guide_image)
                1 -> binding.ivGuideImage.setImageResource(R.drawable.guide_image)
                2 -> binding.ivGuideImage.setImageResource(R.drawable.guide_image)
                3 -> binding.ivGuideImage.setImageResource(R.drawable.guide_image)
                4 -> binding.ivGuideImage.setImageResource(R.drawable.guide_image)
                5 -> {
                    binding.ivGuideImage.setImageResource(R.drawable.guide_image)
                    viewModel.showNextButton()
                }
            }
        }
    }

    override fun getCount(): Int = 6
}
