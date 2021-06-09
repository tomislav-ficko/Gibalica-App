package hr.fer.tel.gibalica.utils

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import com.smarteist.autoimageslider.SliderViewAdapter
import hr.fer.tel.gibalica.R
import hr.fer.tel.gibalica.databinding.ItemLayoutImageSliderBinding
import hr.fer.tel.gibalica.viewModel.IntroViewModel
import timber.log.Timber

class SliderAdapter(
    private val viewModel: IntroViewModel
) : SliderViewAdapter<SliderAdapter.SliderViewHolder>() {

    class SliderViewHolder(
        itemView: View
    ) : SliderViewAdapter.ViewHolder(itemView) {

        val binding = ItemLayoutImageSliderBinding.bind(itemView)
    }

    companion object {
        private const val LAST_ITEM_POSITION = 0
    }

    override fun onCreateViewHolder(parent: ViewGroup?): SliderViewHolder {
        val inflatedView = LayoutInflater
            .from(parent?.context)
            .inflate(R.layout.item_layout_image_slider, parent, false)
        return SliderViewHolder(inflatedView)
    }

    override fun onBindViewHolder(viewHolder: SliderViewHolder?, position: Int) {
        viewHolder?.apply {
            Timber.d("Binding viewHolder at position $position")
            when (position) {
                0 -> setImage(R.drawable.illustration_main, binding, position)
                1 -> setImage(R.drawable.guide_image, binding, position)
                2 -> setImage(R.drawable.guide_image, binding, position)
                3 -> setImage(R.drawable.guide_image, binding, position)
                4 -> setImage(R.drawable.guide_image, binding, position)
                else -> setImage(R.drawable.guide_image, binding, position)
            }
        }
    }

    override fun getCount(): Int = 6

    private fun setImage(
        @DrawableRes resId: Int,
        binding: ItemLayoutImageSliderBinding,
        position: Int
    ) {
        binding.ivGuideImage.setImageResource(resId)
        if (position == LAST_ITEM_POSITION) viewModel.showNextButton()
        else viewModel.hideNextButton()
    }
}
