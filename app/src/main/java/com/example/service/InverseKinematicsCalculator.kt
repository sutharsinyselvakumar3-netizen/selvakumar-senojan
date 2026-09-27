package com.example.service

import com.example.data.model.AppSettings
import kotlin.math.acos
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class ArmKinematicsResult(
    val servo1Angle: Int,
    val servo2Angle: Int,
    val servo3Angle: Int,
    val groundX: Double,
    val groundY: Double,
    val armX: Double,
    val armY: Double,
    val armZ: Double,
    val isReachable: Boolean
)

class InverseKinematicsCalculator {

    fun calculateKinematics(
        pixelCenterX: Int,
        pixelCenterY: Int,
        imageWidth: Int,
        imageHeight: Int,
        settings: AppSettings
    ): ArmKinematicsResult {
        val safeW = if (imageWidth <= 0) 640 else imageWidth
        val safeH = if (imageHeight <= 0) 480 else imageHeight

        // 1. Image normalized offset relative to optical center
        val normX = (pixelCenterX - safeW / 2.0)
        val normY = (pixelCenterY - safeH / 2.0)

        // 2. Camera calibration & perspective ground projection (cm)
        // Camera looks downwards toward ground ahead of the chassis
        val groundScale = settings.cameraCalibrationScale
        val groundX = normX * groundScale * settings.groundScaleX
        val baseGroundY = 20.0 + (normY * groundScale * settings.groundScaleY) // 20cm baseline forward
        val groundY = baseGroundY.coerceAtLeast(10.0)

        // 3. Transformation to Arm base coordinate frame (arm mounted at bottom/base)
        val armX = groundX - settings.armOriginX
        val armY = groundY - settings.armOriginY
        val armZ = -2.0 // Target ground drill depth (2cm below arm plane)

        // 4. Inverse Kinematics for 3-DOF Arm (Base, Elbow/Reach, Tool/Wrist)
        val l1 = settings.link1Length.toDouble()
        val l2 = settings.link2Length.toDouble()
        val l3 = settings.link3Length.toDouble()

        // Base rotation (Servo 1): horizontal angle
        var baseAngleDeg = Math.toDegrees(atan2(armX, armY)) + 90.0 + settings.servo1Offset
        baseAngleDeg = baseAngleDeg.coerceIn(0.0, 180.0)

        // Planar reach in arm radial plane
        val rTotal = sqrt(armX * armX + armY * armY)
        // Wrist positioning tool points vertically downward onto target:
        val rReach = rTotal - (l3 * 0.4)
        val zReach = armZ + l3

        val distSq = rReach * rReach + zReach * zReach
        val dist = sqrt(distSq)

        val isReachable = dist <= (l1 + l2) && dist >= kotlin.math.abs(l1 - l2)

        // Law of Cosines for 2-link planar reach
        val cosAngleElbow = ((distSq - l1 * l1 - l2 * l2) / (2.0 * l1 * l2)).coerceIn(-1.0, 1.0)
        val elbowRad = acos(cosAngleElbow)
        var elbowAngleDeg = Math.toDegrees(elbowRad) + settings.servo2Offset
        elbowAngleDeg = elbowAngleDeg.coerceIn(10.0, 170.0)

        val alpha = atan2(zReach, rReach)
        val beta = atan2(l2 * sin(elbowRad), l1 + l2 * cos(elbowRad))
        var shoulderAngleDeg = Math.toDegrees(alpha + beta) + 90.0 + settings.servo2Offset
        shoulderAngleDeg = shoulderAngleDeg.coerceIn(15.0, 165.0)

        // Tool / Wrist leveling (Servo 3) pointing straight into soil
        var wristAngleDeg = (180.0 - shoulderAngleDeg - elbowAngleDeg + 90.0) + settings.servo3Offset
        wristAngleDeg = wristAngleDeg.coerceIn(0.0, 180.0)

        return ArmKinematicsResult(
            servo1Angle = baseAngleDeg.toInt(),
            servo2Angle = shoulderAngleDeg.toInt(),
            servo3Angle = wristAngleDeg.toInt(),
            groundX = groundX,
            groundY = groundY,
            armX = armX,
            armY = armY,
            armZ = armZ,
            isReachable = isReachable
        )
    }
}
