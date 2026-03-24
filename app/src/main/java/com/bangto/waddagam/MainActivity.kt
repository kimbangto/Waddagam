package com.bangto.waddagam

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ImageCapture
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val context = LocalContext.current

            // 핵심 1: 순수 Compose API를 활용한 권한 상태 관리
            var hasCameraPermission by remember {
                mutableStateOf(
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.CAMERA
                    ) == PackageManager.PERMISSION_GRANTED
                )
            }

            val permissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission(),
                onResult = { isGranted ->
                    hasCameraPermission = isGranted
                }
            )

            // 핵심 2: 화면의 UI 상태 관리 (MVC의 Model을 UI단에서 관리)
            // 앞서 정의했던 DoodleCaptureUiState 데이터 클래스를 사용합니다.
            // 💡 1. 캡처 기능을 담당할 ImageCapture UseCase 생성
            val imageCapture = remember { ImageCapture.Builder().build() }
            var uiState by remember { mutableStateOf(DoodleCaptureUiState()) }

            Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                if (hasCameraPermission) {
                    Box(modifier = Modifier.fillMaxSize()) {

                        if (uiState.capturedImage == null) {
                            // 💡 2. 방금 만든 CameraPreview 띄우기 (imageCapture 전달)
                            CameraPreview(
                                imageCapture = imageCapture,
                                modifier = Modifier.fillMaxSize()
                            )

                            Button(
                                onClick = {
                                    capturePhoto(
                                        context = context,
                                        imageCapture = imageCapture
                                    ) { capturedBitmap ->
                                        // 사진 촬영이 완료되면 uiState를 업데이트합니다.
                                        // (상태가 변경되면서 화면이 자동으로 DoodleCanvas(드로잉 모드)로 넘어갑니다)
                                        uiState = uiState.copy(capturedImage = capturedBitmap)
                                    }
                                },
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = 32.dp)
                            ) {
                                Text("사진 촬영")
                            }
                        } else {
                            DoodleCanvas(
                                backgroundImage = uiState.capturedImage,
                                modifier = Modifier.fillMaxSize(),
                                onRetake = {
                                    // 다시 찍기를 누르면 이미지를 비워서 카메라 프리뷰로 돌아가게 합니다.
                                    uiState = uiState.copy(capturedImage = null)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}