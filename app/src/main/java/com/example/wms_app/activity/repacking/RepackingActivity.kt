package com.example.wms_app.activity.repacking

import android.content.DialogInterface
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.example.wms_app.R
import com.example.wms_app.databinding.ActivityRepackingBinding
import com.example.wms_app.utilities.ApiResponseEvent
import com.example.wms_app.utilities.DialogBuilder
import com.example.wms_app.utilities.bindingdelegate.viewBinding
import com.example.wms_app.viewmodel.repacking.RepackingViewModel
import kotlinx.coroutines.flow.collect

class RepackingActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private val binding by viewBinding(ActivityRepackingBinding::inflate)
    private val navController by lazy {
        Navigation.findNavController(this, R.id.repackingNavigationHostFragment)
    }
    private val viewModel: RepackingViewModel by viewModels()

    private val loadingDialog by lazy {
        DialogBuilder.getLoadingDialog(this)
    }

    private val errorDialog by lazy {
        DialogBuilder.getOkDialogWithCallback(this, getString(R.string.error), "") { _: DialogInterface, _: Int ->
            this.onBackPressed()
        }
    }

    private val warningDialog by lazy {
        DialogBuilder.showOkDialogWithoutCallback(this, getString(R.string.error_happened_main), "")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.repackingToolbar)
        setupDrawerLayout()
        setupObservers()
    }


    private fun setupDrawerLayout() {
        NavigationUI.setupActionBarWithNavController(this, navController, binding.repackingDrawerLayout)
        binding.repackingNavigationView.setupWithNavController(navController)
        binding.repackingNavigationView.setNavigationItemSelectedListener(this)
    }

    private fun setupObservers() {
        lifecycleScope.launchWhenStarted {
            viewModel.apiResponseEvent.collect { event ->
                when (event) {
                    ApiResponseEvent.SuccessEvent -> {
                        dismissLoadingDialog()
                    }
                    ApiResponseEvent.LoadingEvent -> {
                        loadingDialog.show()
                    }
                    is ApiResponseEvent.SuccessWithActionEvent -> {
                        dismissLoadingDialog()
                        Snackbar.make(findViewById(android.R.id.content), event.message, Snackbar.LENGTH_LONG)
                                .show()
                    }
                    is ApiResponseEvent.WarningEvent -> {
                        dismissLoadingDialog()
                        warningDialog.setMessage(getString(R.string.error_string, event.message))
                        warningDialog.show()
                    }
                    is ApiResponseEvent.ErrorEvent -> {
                        dismissLoadingDialog()
                        errorDialog.setMessage(getString(R.string.error_string, event.message))
                        errorDialog.show()
                    }
                    is ApiResponseEvent.PromptEvent -> TODO()
                }
            }
        }
    }

    private fun dismissLoadingDialog() {
        if (loadingDialog != null && loadingDialog.isShowing)
            loadingDialog.dismiss()
    }

    override fun onBackPressed() {
        if (binding.repackingDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.repackingDrawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return NavigationUI.navigateUp(navController, binding.repackingDrawerLayout)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        item.isChecked = true
        binding.repackingDrawerLayout.closeDrawers();
        if (item.itemId == R.id.repackingTempList)
            navController.navigate(R.id.repackingTempListFragment)
        return true
    }

}