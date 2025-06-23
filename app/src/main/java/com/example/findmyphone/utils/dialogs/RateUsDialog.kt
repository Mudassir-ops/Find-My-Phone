package com.example.findmyphone.utils.dialogs


import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.DialogFragment
import com.findmyphone.clapping.clapfinder.soundalert.databinding.RateUsDialogBinding
import com.example.findmyphone.utils.feedBackWithEmail
import dagger.hilt.android.AndroidEntryPoint

@SuppressLint("SetTextI18n")
@AndroidEntryPoint
class RateUsDialog(
    private val dismissCallBack: () -> Unit
) : DialogFragment() {
    private var _binding: RateUsDialogBinding? = null
    private val binding get() = _binding

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            val margin = 32
            val marginPx = (margin * resources.displayMetrics.density).toInt()
            val width = resources.displayMetrics.widthPixels - (2 * marginPx)
            setLayout(
                width,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): ConstraintLayout? {
        _binding = RateUsDialogBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.apply {
            btnClose.setOnClickListener {
                dismiss()
                dismissCallBack.invoke()
            }
            ratingBar.setOnRatingBarChangeListener { _, rating, _ ->
                ratingBar.rating = 0F
                when (rating.toInt()) {
                    in 1..3 -> {
                        context?.feedBackWithEmail(
                            title = "Feedback",
                            message = "Any Feedback",
                            emailId = "atonomushub127@gmail.com"
                        )
                        dismiss()
                    }

                    4, 5 -> {
                        val packageName = context?.packageName
                        val intent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("market://details?id=$packageName")
                        )
                        context?.startActivity(intent)
                        dismiss()
                    }
                }
            }
            btnRateUs.setOnClickListener {
                dismiss()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
