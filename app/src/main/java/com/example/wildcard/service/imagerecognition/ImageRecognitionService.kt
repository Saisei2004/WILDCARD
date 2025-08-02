package com.example.wildcard.service.imagerecognition

import android.graphics.Rect
import android.media.Image
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * 画像認識サービス
 *
 * Google ML KitのFace Detection APIを使用して、
 * カメラからの映像ストリームから顔を検出し、目の開閉状態を判定します。
 */
class ImageRecognitionService {

    private val options = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
        .build()

    private val detector = FaceDetection.getClient(options)

    /**
     * 画像から顔を検出し、目の開いている確率を返します。
     * @param image ML KitのInputImageオブジェクト
     * @return 目の開いている確率 (0.0f - 1.0f)。顔が検出されない場合は-1.0f。
     */
    suspend fun detectFaceAndEyeOpenProbability(image: InputImage): Float = suspendCoroutine { continuation ->
        detector.process(image)
            .addOnSuccessListener {
                if (it.isNotEmpty()) {
                    val face = it[0] // 最初の顔を使用
                    val leftEyeOpenProb = face.leftEyeOpenProbability ?: 0.0f
                    val rightEyeOpenProb = face.rightEyeOpenProbability ?: 0.0f
                    // 両目の開いている確率の平均を返す
                    continuation.resume((leftEyeOpenProb + rightEyeOpenProb) / 2.0f)
                } else {
                    continuation.resume(-1.0f) // 顔が検出されない場合
                }
            }
            .addOnFailureListener {
                it.printStackTrace()
                continuation.resume(-1.0f) // エラーの場合
            }
    }

    /**
     * 目の開いている確率が閾値を超えているか判定します。
     * @param probability 目の開いている確率 (0.0f - 1.0f)
     * @param threshold 閾値 (デフォルト: 0.7f)
     * @return 閾値を超えていればtrue、そうでなければfalse
     */
    fun verifyEyesOpen(probability: Float, threshold: Float = 0.7f): Boolean {
        return probability > threshold
    }

    /**
     * カメラからのImageオブジェクトをML KitのInputImageに変換します。
     * @param image Android CameraXのImageオブジェクト
     * @param rotationDegrees 画像の回転角度
     * @return 変換されたInputImageオブジェクト
     */
    fun convertMediaImageToInputImage(image: Image, rotationDegrees: Int): InputImage {
        return InputImage.fromMediaImage(image, rotationDegrees)
    }
}