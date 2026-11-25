package com.znaky.feature.car

import androidx.compose.ui.graphics.Color
import com.znaky.core.art.Car3D

/** What the 3D car shows. */
enum class CarKind(internal val art: Car3D.Kind) { ENGINE(Car3D.Kind.ENGINE), ELECTRIC(Car3D.Kind.ELECTRIC) }

/** 3D = solid body, X_RAY = see-through body with the parts inside, SCHEME = top-down x-ray. */
enum class CarView(internal val art: Car3D.View) { SOLID(Car3D.View.SOLID), X_RAY(Car3D.View.X_RAY), SCHEME(Car3D.View.SCHEME) }

internal val EngineColor = Color(Car3D.ENGINE)
internal val GearboxColor = Color(Car3D.GEARBOX)
internal val BatteryColor = Color(Car3D.BATTERY)
internal val MotorColor = Color(Car3D.MOTOR)
internal val FuelColor = Color(Car3D.FUEL)
