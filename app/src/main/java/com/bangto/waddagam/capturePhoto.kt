package com.bangto.waddagam

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Log
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.core.content.ContextCompat

// 사진을 찍고 Bitmap으로 변환해 돌려주는 헬퍼 함수
fun capturePhoto(
    context: Context,
    imageCapture: ImageCapture,
    onPhotoCaptured: (Bitmap) -> Unit
) {
    val executor = ContextCompat.getMainExecutor(context)

    // 메모리 내에서 이미지를 처리하기 위해 in-memory 방식 사용
    imageCapture.takePicture(
        executor,
        object : ImageCapture.OnImageCapturedCallback() {
            override fun onCaptureSuccess(image: ImageProxy) {
                // 1. ImageProxy를 Bitmap으로 변환
                val bitmap = image.toBitmap()
                val rotationDegrees = image.imageInfo.rotationDegrees.toFloat()

                // 2. OOM(메모리 부족, 흰 화면) 방지를 위해 이미지 크기를 절반으로 줄입니다.
                val scaleFactor = 0.5f

                // 3. 회전 및 크기 축소 적용
                val matrix = Matrix().apply {
                    postRotate(rotationDegrees)
                    postScale(scaleFactor, scaleFactor)
                }

                val processedBitmap = Bitmap.createBitmap(
                    bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true
                )
// 💡여기에 로그를 추가해 주세요!
                Log.d(
                    "DoodleTest",
                    "사진 캡처 성공! 가로: ${processedBitmap.width}, 세로: ${processedBitmap.height}"
                )
                // 4. 변환된 최종 Bitmap을 메인 화면으로 전달
                onPhotoCaptured(processedBitmap)

                // 5. 처리가 끝난 이미지는 반드시 닫아주기 (안 닫으면 다음 촬영 안 됨)
                image.close()
            }

            override fun onError(exception: ImageCaptureException) {
                Log.e("CameraCapture", "사진 촬영 실패: ${exception.message}", exception)
            }
        }
    )
}