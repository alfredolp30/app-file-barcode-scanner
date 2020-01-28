package com.alplabs.filebarcodescanner

import android.os.Bundle
import androidx.fragment.app.FragmentTransaction
import com.alplabs.filebarcodescanner.fragment.BarcodeFragment

class BarcodeActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_barcode)

        showFragment()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }


    private fun showFragment() {
        supportFragmentManager
            .beginTransaction()
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            .replace(R.id.frame, BarcodeFragment.newInstance(false))
            .commitAllowingStateLoss()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

}
