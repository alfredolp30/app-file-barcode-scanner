package com.alplabs.filebarcodescanner.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.alplabs.filebarcodescanner.R
import java.io.Serializable

enum class ErrorType : Serializable {
    INIT,
    NOT_CONTAINS_BARCODE,
    DURANT_LOADING
}

private const val ERROR_TYPE = "error_type"

class ErrorFragment : Fragment() {

    private var errorType: ErrorType? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            errorType = (it.getSerializable(ERROR_TYPE) as? ErrorType) ?: ErrorType.INIT
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_error, container, false)
    }


//    override fun onAttach(context: Context) {
//        super.onAttach(context)
//        if (context is OnFragmentInteractionListener) {
//            listener = context
//        } else {
//            throw RuntimeException(context.toString() + " must implement OnFragmentInteractionListener")
//        }
//    }
//
//    override fun onDetach() {
//        super.onDetach()
//
//    }


    companion object {

        @JvmStatic
        fun newInstance(errorType: ErrorType) =
            ErrorFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ERROR_TYPE, errorType)
                }
            }
    }
}
