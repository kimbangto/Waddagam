package com.bangto.waddagam

import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat

@Composable
fun CameraPreview(
    imageCapture: ImageCapture, // MainActivity에서 넘겨받을 캡처 객체
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    
    // 카메라 프로바이더를 비동기로 가져오기 위한 Future 객체
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    // 기존 View 시스템의 PreviewView를 Compose에서 사용하기 위해 AndroidView 활용
    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { ctx ->
            // 1. PreviewView 초기화
            val previewView = PreviewView(ctx).apply {
                scaleType = PreviewView.ScaleType.FILL_CENTER
            }

            // 2. 메인 스레드에서 실행될 Executor
            val executor = ContextCompat.getMainExecutor(ctx)

            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()

                // 3. 미리보기(Preview) UseCase 설정
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                // 4. 후면 카메라 선택
                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                try {
                    // 기존에 바인딩된 UseCase가 있다면 해제
                    cameraProvider.unbindAll()

                    // 5. 카메라 생명주기를 Compose 화면(LifecycleOwner)에 바인딩
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageCapture // 캡처 UseCase도 함께 바인딩!
                    )
                } catch (exc: Exception) {
                    Log.e("CameraPreview", "카메라 바인딩 실패", exc)
                }
            }, executor)

            // 최종적으로 구성된 PreviewView 반환
            previewView
        }
    )
}