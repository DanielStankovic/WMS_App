package com.example.wms_app.fragment.repacking

import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.wms_app.R
import com.example.wms_app.databinding.FragmentRepackingTempListBinding
import com.example.wms_app.utilities.bindingdelegate.viewBinding
import com.example.wms_app.viewmodel.repacking.RepackingViewModel


class RepackingTempListFragment : Fragment(R.layout.fragment_repacking_temp_list) {

    private val binding by viewBinding(FragmentRepackingTempListBinding::bind)
    private val viewModel: RepackingViewModel by activityViewModels()

}