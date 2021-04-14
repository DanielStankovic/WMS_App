package com.example.wms_app.fragment.repacking


import android.content.*
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.wms_app.R
import com.example.wms_app.databinding.FragmentRepackingDefaultBinding
import com.example.wms_app.utilities.*
import com.example.wms_app.utilities.bindingdelegate.viewBinding
import com.example.wms_app.viewmodel.repacking.RepackingViewModel


class RepackingDefaultFragment : Fragment(R.layout.fragment_repacking_default) {
    private val binding by viewBinding(FragmentRepackingDefaultBinding::bind)
    private val viewModel: RepackingViewModel by activityViewModels()
    private var isTempListEmpty: Boolean = true
    private lateinit var errorDialog: AlertDialog
    private val scanReceiver = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            when (p1?.action) {
                resources.getString(R.string.activity_intent_filter_action) -> {
                    try {
                        val barcode = p1.getStringExtra(resources.getString(R.string.datawedge_intent_key_data))
                        if (!barcode.isNullOrBlank() && barcode.length == Constants.POSITION_BARCODE_LENGTH) {

                            InternetCheck({ hasInternet ->
                                if (hasInternet) {
                                    viewModel.codeScanned(barcode)
                                } else {
                                    DialogBuilder.showNoInternetDialog(context)
                                }
                            }, context)

                        } else {
                            viewModel.codeScanned(barcode)
                        }
                    } catch (e: Exception) {
                        errorDialog.setMessage(resources.getString(R.string.scanning_error, e.message))
                        errorDialog.show()
                    }
                }
            }
        }

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListeners()
        setupObservers()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        registerScanReceiver(context)
    }

    override fun onDetach() {
        super.onDetach()
        context?.unregisterReceiver(scanReceiver)
    }

    private fun setupListeners() {
        binding.apply {
            repackingSpinnerProduct.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {

                }

                override fun onNothingSelected(p0: AdapterView<*>?) {

                }

            }
            repackingFromPosBtn.setOnClickListener(object : OnOneOffClickListener() {
                override fun onSingleClick(v: View?) {
                    if (binding.repackingFromPosEt.isPositionEditTextValid(getString(R.string.pos_must_have_num_of_chars, Constants.POSITION_BARCODE_LENGTH), Constants.POSITION_BARCODE_LENGTH)) {
                        InternetCheck({ hasInternet ->
//                            if (hasInternet) {
                            viewModel.positionFromSelected(binding.repackingFromPosEt.text.toString().trim())
//                            } else {
//                                DialogBuilder.showNoInternetDialog(context)
//                            }
                        }, context)
                    }
                }

            })
            repackingToPosBtn.setOnClickListener(object : OnOneOffClickListener() {
                override fun onSingleClick(v: View?) {
                    if (binding.repackingToPosEt.isPositionEditTextValid(getString(R.string.pos_must_have_num_of_chars, Constants.POSITION_BARCODE_LENGTH), Constants.POSITION_BARCODE_LENGTH)) {
                        InternetCheck({ hasInternet ->
                            if (hasInternet) {
                                viewModel.positionToSelected(binding.repackingToPosEt.text.toString().trim())
                            } else {
                                DialogBuilder.showNoInternetDialog(context)
                            }
                        }, context)
                    }
                }
            })
            repackingCancelCurrentPickup.setOnClickListener {
                DialogBuilder.showDialogWithYesCallback(context, getString(R.string.warning),
                        getString(R.string.delete_scanned_product_prompt)) { _, _ -> viewModel.removeAllFromTempList() }
            }
            repackingUndoLastPickup.setOnClickListener { viewModel.removeLastFromTempList() }
            repackingAddBtn.setOnClickListener {}
            repackingAddNoSrNumBtn.setOnClickListener {}
        }
    }

    private fun setupObservers() {
        viewModel.isTempListEmpty.observe(viewLifecycleOwner) {
            isTempListEmpty = it
            binding.repackingCancelCurrentPickup.isEnabled = !it
            binding.repackingUndoLastPickup.isEnabled = !it
        }
    }

    private fun registerScanReceiver(context: Context) {
        try {
            val intentFilter = IntentFilter()
            intentFilter.addCategory(Intent.CATEGORY_DEFAULT)
            intentFilter.addAction(resources.getString(R.string.activity_intent_filter_action))
            context.registerReceiver(scanReceiver, intentFilter)

        } catch (e: Exception) {
            ErrorClass.handle(e, requireActivity())
        }
    }

}