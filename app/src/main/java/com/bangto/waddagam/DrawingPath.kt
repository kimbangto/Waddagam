package com.bangto.waddagam

import android.graphics.Bitmap
import androidx.compose.ui.graphics.Path

// 드로잉 정보를 담는 클래스
data class DrawingPath(
    val path: Path,
    val color: androidx.compose.ui.graphics.Color = androidx.compose.ui.graphics.Color.Red,
    val strokeWidth: Float = 10f
)

// 화면 전체의 상태
data class DoodleCaptureUiState(
    val capturedImage: Bitmap? = null, // 촬영된 사진 (배경용)
    val isCameraReady: Boolean = false, // 카메라 미리보기 준비 완료 여부
    val paths: List<DrawingPath> = emptyList(), // 사용자가 그린 선들의 리스트
    val currentPath: DrawingPath? = null // 현재 그리고 있는 선
)