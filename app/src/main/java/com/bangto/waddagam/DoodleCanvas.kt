package com.bangto.waddagam// DoodleCanvas.kt (View)

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.GraphicsLayerScope
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@Composable
fun DoodleCanvas(
    backgroundImage: Bitmap?, // 배경 사진
    modifier: Modifier = Modifier,
    onRetake: () -> Unit // 💡 다시 찍기 기능을 위해 추가
) {
    // 사용자가 그린 선들을 관리하는 리스트 (MVC에서는 Controller나 Model로 이동해야 함)
    val paths = remember { mutableStateListOf<DrawingPath>() }
    var currentPath by remember { mutableStateOf<DrawingPath?>(null) }

    Box(modifier = modifier.fillMaxSize().background(Color.Gray)) {
        // 1. 배경 사진 (찍은 사진이 있다면 표시)
        backgroundImage?.let { bitmap ->
            Image(
                bitmap = bitmap.asImageBitmap(), // Bitmap을 Compose용으로 변환
                contentDescription = "Captured Photo",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop // 화면에 꽉 차게 비율 조정
            )
        }
        // 2. 그림을 그리는 캔버스
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    // 터치 및 드래그 감지 로직
                    detectDragGestures(
                        onDragStart = { offset ->
                            // 터치 시작: 새로운 Path 생성
                            currentPath = DrawingPath(path = Path().apply { moveTo(offset.x, offset.y) })
                        },
                        onDrag = { change, dragAmount ->
                            currentPath?.let { drawingPath ->
                                // 1. 중요! 기존 선 데이터를 통째로 복사한 '새로운 Path 객체'를 만듭니다.
                                val newPath = Path().apply {
                                    addPath(drawingPath.path) // 기존 데이터 복사
                                    lineTo(change.position.x, change.position.y) // 새로운 선 추가
                                }

                                // 2. 중요! drawingPath를 copy하면서 새로운 Path 객체(newPath)를 할당합니다.
                                // 이 과정에서 '주소값'이 변하므로 Compose가 100% 변화를 인지합니다.
                                currentPath = drawingPath.copy(path = newPath)
                            }
                        },
                        onDragEnd = {
                            // 터치 종료: 완성된 Path를 리스트에 추가하고 초기화
                            currentPath?.let { paths.add(it) }
                            currentPath = null
                        },
                        onDragCancel = {
                            currentPath = null
                        }
                    )
                }
        ) {
            // 저장된 모든 선 그리기
            paths.forEach { drawingPath ->
                drawPath(
                    path = drawingPath.path,
                    color = drawingPath.color,
                    style = Stroke(width = drawingPath.strokeWidth)
                )
            }
            // 현재 그리고 있는 선 실시간으로 그리기
            currentPath?.let { drawingPath ->
                drawPath(
                    path = drawingPath.path,
                    color = drawingPath.color,
                    style = Stroke(width = drawingPath.strokeWidth)
                )
            }
        }
        Button(
            onClick = onRetake,
            modifier = Modifier.align(Alignment.BottomCenter).padding(32.dp)
        ) {
            Text("다시 찍기")
        }
    }
}