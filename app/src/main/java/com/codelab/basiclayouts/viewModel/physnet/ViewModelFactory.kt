//package com.codelab.basiclayouts.viewModel.physnet
//
//import android.content.Context
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.ViewModelProvider
//import com.codelab.basiclayouts.data.physnet.RppgProcessor
//import com.codelab.basiclayouts.data.physnet.RppgRepository
//import com.codelab.basiclayouts.data.physnet.VideoRecorder
//import com.codelab.basiclayouts.viewmodel.physnet.RppgViewModel
//
//class RppgViewModelFactory(
//    private val context: Context,
//    private val videoRecorder: VideoRecorder,
//    private val rppgProcessor: RppgProcessor,
//    private val repository: RppgRepository
//) : ViewModelProvider.Factory {
//
//    override fun <T : ViewModel> create(modelClass: Class<T>): T {
//        if (modelClass.isAssignableFrom(RppgViewModel::class.java)) {
//            @Suppress("UNCHECKED_CAST")
//            return RppgViewModel(
//                context = context,
//                videoRecorder = videoRecorder,
//                rppgProcessor = rppgProcessor,
//                repository = repository
//            ) as T
//        }
//        throw IllegalArgumentException("Unknown ViewModel class")
//    }
//}