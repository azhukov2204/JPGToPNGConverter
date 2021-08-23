package ru.androidlearning.jpgtopngconverter.presentation

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import by.kirich1409.viewbindingdelegate.viewBinding
import moxy.MvpAppCompatActivity
import moxy.ktx.moxyPresenter
import ru.androidlearning.jpgtopngconverter.R
import ru.androidlearning.jpgtopngconverter.data.ImageProcessorFactory
import ru.androidlearning.jpgtopngconverter.databinding.ActivityMainBinding
import ru.androidlearning.jpgtopngconverter.scheduler.WorkSchedulersFactory

private const val READ_FILES_PERMISSION = Manifest.permission.READ_EXTERNAL_STORAGE
private const val JPEG_MIME_TYPE = "image/jpeg"
private const val PNG_MIME_TYPE = "image/png"

class MainActivity : MvpAppCompatActivity(R.layout.activity_main), MainView {
    private val binding by viewBinding(ActivityMainBinding::bind)
    private val presenter: MainPresenter by moxyPresenter {
        MainPresenter(
            schedulers = WorkSchedulersFactory.create(),
            imageProcessor = ImageProcessorFactory.create(this)
        )
    }

    private val requestPermissionResult: ActivityResultLauncher<String> = registerForActivityResult(ActivityResultContracts.RequestPermission()) { resultIsOK ->
        if (resultIsOK) {
            openJpeg()
        } else {
            showExplanationDialog()
        }
    }

    private val openJpgActivityResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
        if (activityResult.resultCode == RESULT_OK) {
            presenter.loadJpegToBitmap(activityResult.data?.data)
        }
    }

    private val savePngActivityResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
        if (activityResult.resultCode == RESULT_OK) {
            activityResult.data?.data?.let { uri ->
                presenter.convertBitmapToPng(uri)
            }
        }
    }

    override fun initButtons() {
        println("initButtons")
        with(binding) {
            openPictureButton.setOnClickListener { presenter.openJpeg() }
            saveToPngButton.setOnClickListener { presenter.savePng() }
            cancelButton.setOnClickListener { presenter.cancelConversion() }
        }
    }

    override fun checkPermissionAndOpenJpeg() {
        when {
            isPermitted() -> openJpeg()
            (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && shouldShowRequestPermissionRationale(READ_FILES_PERMISSION)) -> showPermissionsRequiredDialog()
            else -> requestPermissionResult.launch(READ_FILES_PERMISSION)
        }
    }

    private fun isPermitted(): Boolean =
        ContextCompat.checkSelfPermission(this, READ_FILES_PERMISSION) == PackageManager.PERMISSION_GRANTED

    private fun openJpeg() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = JPEG_MIME_TYPE
        }
        openJpgActivityResult.launch(intent)
    }

    override fun savePng(fileName: String?) {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = PNG_MIME_TYPE
            fileName?.let { putExtra(Intent.EXTRA_TITLE, it) }
        }
        savePngActivityResult.launch(intent)
    }

    override fun showBitmapImage(image: Bitmap) {
        binding.jpegImageView.setImageBitmap(image)
    }

    override fun setEnabledSaveToPNGButton(isEnabled: Boolean) {
        binding.saveToPngButton.isEnabled = isEnabled
    }

    override fun setEnabledOpenJpegButton(isEnabled: Boolean) {
        binding.openPictureButton.isEnabled = isEnabled
    }

    override fun setVisibleProgressIndicator(isVisible: Boolean) {
        binding.progressIndicator.isVisible = isVisible
    }

    override fun setVisibleCancelButton(isVisible: Boolean) {
        binding.cancelButton.isVisible = isVisible
    }

    override fun showSuccessConversionMessage() {
        Toast.makeText(this, getString(R.string.сonversion_completed_message), Toast.LENGTH_SHORT).show()
    }

    override fun showConversionError(error: Throwable) {
        Toast.makeText(this, getString(R.string.error_conversion_message) + error.message, Toast.LENGTH_SHORT).show()
    }

    override fun showReadImageError(error: Throwable) {
        Toast.makeText(this, getString(R.string.error_read_image_file_message) + error.message, Toast.LENGTH_SHORT).show()
    }

    override fun showCancelConversionMessage() {
        Toast.makeText(this, getString(R.string.conversion_canceled_message), Toast.LENGTH_SHORT).show()
    }

    private fun showExplanationDialog() = AlertDialog.Builder(this)
        .setTitle(getString(R.string.permissions_required_alert_title))
        .setMessage(getString(R.string.explanation_of_permission) + READ_FILES_PERMISSION)
        .setNegativeButton(getString(R.string.close_button_text)) { dialog, _ -> dialog.dismiss() }
        .create()
        .show()

    private fun showPermissionsRequiredDialog() = AlertDialog.Builder(this)
        .setTitle(getString(R.string.permissions_required_alert_title))
        .setMessage(getString(R.string.permissions_required_alert_message) + READ_FILES_PERMISSION)
        .setPositiveButton(getString(R.string.grant_access_button_text)) { _, _ -> requestPermissionResult.launch(READ_FILES_PERMISSION) }
        .setNegativeButton(getString(R.string.negative_button_text)) { dialog, _ -> dialog.dismiss() }
        .create()
        .show()
}
