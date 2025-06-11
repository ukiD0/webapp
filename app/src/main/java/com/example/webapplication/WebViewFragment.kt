package com.example.webapplication

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.AppCompatButton
import com.example.webapplication.databinding.FragmentWebViewBinding


class WebViewFragment : Fragment() {

    private var _binding: FragmentWebViewBinding? = null
    private val binding get() = _binding!!
    private lateinit var webView: WebView
    private var errorDialog: AlertDialog? = null
    private var currentUrl: String? = null
    private var hasMainPageError = false

    fun checkNetwork(context: Context): Boolean {
        return try {
            (context.applicationContext as MyApp).isNetworkAvailable()
        } catch (e: Exception) {
            false
        }
    }

    private val backPressedCallback = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() {
            if (webView.canGoBack()) {
                webView.goBack()
            } else {
                isEnabled = false
                requireActivity().onBackPressedDispatcher.onBackPressed()
                isEnabled = true
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(this, backPressedCallback)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWebViewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        webView = binding.webviewidcontainer.apply {
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                loadWithOverviewMode = true
                useWideViewPort = true
                setSupportZoom(false)
                cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
                blockNetworkLoads = false // Разрешаем загрузку ресурсов
            }

            isVerticalScrollBarEnabled = false
            isHorizontalScrollBarEnabled = false

            webViewClient = object : WebViewClient() {
                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    super.onPageStarted(view, url, favicon)
                    backPressedCallback.isEnabled = true
                    currentUrl = url
                    hasMainPageError = false
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    if (url == currentUrl && !hasMainPageError) {
                        // Основная страница загрузилась без ошибок
                    }
                }

                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
                ): Boolean {
                    return false
                }

                @SuppressLint("NewApi")
                override fun onReceivedHttpError(
                    view: WebView,
                    request: WebResourceRequest,
                    errorResponse: WebResourceResponse
                ) {
                    // Обрабатываем только ошибки основного URL
                    if (request.url.toString() == currentUrl) {
                        hasMainPageError = true
                        showMainErrorDialog(
                            if (errorResponse.statusCode == 404)
                                R.string.error_404_message
                            else
                                R.string.error_http_message + errorResponse.statusCode
                        )
                    }
                }

                override fun onReceivedError(
                    view: WebView,
                    request: WebResourceRequest,
                    error: WebResourceError
                ) {
                    // Обрабатываем только ошибки основного URL
                    if (request.url.toString() == currentUrl) {
                        hasMainPageError = true
                        if (!checkNetwork(requireContext())) {
                            showNetworkErrorDialog()
                            binding.webviewidcontainer.visibility = View.GONE
                        } else {
                            showMainErrorDialog(R.string.error_loading_message)
                            binding.webviewidcontainer.visibility = View.GONE
                        }
                    }
                }

                override fun onLoadResource(view: WebView?, url: String?) {
                    view?.setOnLongClickListener { true }
                    super.onLoadResource(view, url)
                }
            }
        }
        checkNetworkAndLoad()
        binding.webviewidcontainer.visibility = View.VISIBLE
    }

    private fun checkNetworkAndLoad() {
        if (checkNetwork(requireContext())) {
            webView.loadUrl("https://www.furnitu.ru/")
            binding.webviewidcontainer.visibility = View.VISIBLE
        } else {
            showNetworkErrorDialog()
            binding.webviewidcontainer.visibility = View.GONE
        }
    }

    private fun showNetworkErrorDialog() {
        errorDialog?.dismiss()
        errorDialog = AlertDialoggg.create(
            requireActivity(),
            R.string.network_error_title,
            R.string.network_error_message,
            { checkNetworkAndLoad() },
            { requireActivity().finish() }
        )
    }

    private fun showMainErrorDialog(message: Int) {
        if (!hasMainPageError) return
        errorDialog?.dismiss()
        errorDialog = AlertDialoggg.create(
            requireActivity(),
            R.string.error_title,
            message,
            { checkNetworkAndLoad() },
            { requireActivity().finish() }
        )
    }

    override fun onDestroyView() {
        errorDialog?.dismiss()
        errorDialog = null
        _binding = null
        super.onDestroyView()
    }
}