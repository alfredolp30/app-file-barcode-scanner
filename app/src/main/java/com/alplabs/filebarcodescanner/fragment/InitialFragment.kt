package com.alplabs.filebarcodescanner.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.alplabs.filebarcodescanner.R
import kotlinx.android.synthetic.main.cell_initial.view.*
import java.lang.ref.WeakReference

class InitialFragment : BaseFragment() {

    private var listener: WeakReference<Listener>? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_initial, container, false)

        view.btnArchive.setOnClickListener {
            listener?.get()?.onSelectArchive()
        }

        view.btnCamera.setOnClickListener {
            listener?.get()?.onSelectCamera()
        }

        return view
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is Listener) {
            listener = WeakReference(context)
        } else {
            throw RuntimeException("$context must implement ProgressFragmentListener")
        }
    }


    interface Listener {
        fun onSelectArchive()
        fun onSelectCamera()
    }

}
